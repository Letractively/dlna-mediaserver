package de.sosd.mediaserver.task;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.Executor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Service;

import de.sosd.mediaserver.bean.ProcessToWatch;
import de.sosd.mediaserver.process.ProcessKilledNotifier;
import de.sosd.mediaserver.service.MPlayerFileService;

@Service
public class ProcessWatchdogService implements ApplicationListener<ContextClosedEvent> {

	private final static Log logger = LogFactory.getLog(ProcessWatchdogService.class);
	
	private final static Map<String, ProcessToWatch> watchedProcessMap = new ConcurrentHashMap<String, ProcessToWatch>();
	private final static Map<String, ProcessToWatch> newProcessMap = new ConcurrentHashMap<String, ProcessToWatch>();

	private final static String kill_cmd;
	private final static String[] kill_cmd_params;	
	private final static String kill_cmd_log;
	private final static String list_cmd;
	private final static String[] list_cmd_params;		
	private final static String list_cmd_log;
	private final static String list_cmd_seperator;	
	private final static String list_cmd_pid_col;
	private final static String list_cmd_command_col;
	
	static {
		
		if (isWindows()) {
			kill_cmd = "taskkill";
			kill_cmd_params = new String[]{"/f", "/im"};
			kill_cmd_log = "taskkill /f /im";
			list_cmd = "wmic";
			list_cmd_params = new String[]{"process" ,"get", "ProcessId,CommandLine", "/FORMAT:csv"};
			list_cmd_log = "wmic process get ProcessId,CommandLine /FORMAT:csv";
			list_cmd_seperator = ",";
			list_cmd_pid_col = "processid";
			list_cmd_command_col = "commandline";
		} else {
			kill_cmd = "kill";
			kill_cmd_params = new String[]{"-9"};
			kill_cmd_log = "/bin/kill -9";
			list_cmd = "ps";
			list_cmd_params = new String[]{"-eo", "\"%p|%a\""};
			list_cmd_log = "ps -eo \"%p|%a\"";
			list_cmd_seperator = "\\|";
			list_cmd_pid_col = "pid";
			list_cmd_command_col = "command";			
		}
		
	}
	
	private static boolean isWindows() {
		return File.separatorChar == '\\';
	}
	
	
	public void checkProcesses() {
		if (! getProcessMap().isEmpty() || !getNewProcessMap().isEmpty()) {
			final long timestamp = System.currentTimeMillis();
			final List<ProcessToWatch> processList = getProcessList();
			
			final Map<String, ProcessToWatch> unassignedProcessMap = new HashMap<String, ProcessToWatch>();
			final Set<String> all_pids = new HashSet<String>();
			for (final ProcessToWatch proc : processList) {
				if (!getProcessMap().containsKey(proc.getPid())) {
					unassignedProcessMap.put(proc.getPid(), proc);
				}
				// add all pids first
				all_pids.add(proc.getPid());
			}
			final Set<String> assignedIds = new HashSet<String>();
			for (final Entry<String, ProcessToWatch> newProc : getNewProcessMap().entrySet()) {						
				for (final Entry<String, ProcessToWatch> foundProc : unassignedProcessMap.entrySet()) {				
					if (newProc.getValue().fits(foundProc.getValue().getFullCommand())) {
						newProc.getValue().setPid(foundProc.getValue().getPid());
						newProc.getValue().setFullCommand(foundProc.getValue().getFullCommand());
						break;
					}		
				}
				if (newProc.getValue().hasPid()) {
					unassignedProcessMap.remove(newProc.getValue().getPid());
					assignedIds.add(newProc.getValue().getId());
					for (final ProcessToWatch pct : getProcessMap().values()) {
						if (pct.getFullCommand().equals(newProc.getValue().getFullCommand())) {
							pct.markAsSurvivor();
						}
					}
					getProcessMap().put(newProc.getValue().getPid(), newProc.getValue());
					logger.info("added process to watch : " + newProc.getValue().toString());
				} else {
					if (newProc.getValue().isSurvivor(timestamp)) {
						// process never showed up and probably never will, so remove it from watch
						assignedIds.add(newProc.getValue().getId());
					}
				}
			}
			
			for (final String id : assignedIds) {
				getNewProcessMap().remove(id);
			}
			final List<ProcessToWatch> doubles = new ArrayList<ProcessToWatch>();
			// check for doubles
			for (final Entry<String, ProcessToWatch> newProc : unassignedProcessMap.entrySet()) {
				for (final Entry<String, ProcessToWatch> currentProc : getProcessMap().entrySet()) {
					if (currentProc.getValue().fits(newProc.getValue().getFullCommand())) {
						// found double
						doubles.add(newProc.getValue());
						newProc.getValue().setCreationDate(currentProc.getValue().getCreationDate());
						newProc.getValue().setId(UUID.randomUUID().toString());
						newProc.getValue().setIdentifiers(currentProc.getValue().getIdentifiers());
						newProc.getValue().setMaxRuntimeInSeconds(currentProc.getValue().getMaxRuntimeInSeconds());
						newProc.getValue().setNotifier(currentProc.getValue().getNotifier());
					}
				}
			}
			for (final ProcessToWatch pct : doubles) {
				unassignedProcessMap.remove(pct.getPid());
				getProcessMap().put(pct.getPid(), pct);
			}
			
			final Set<String> removed_pids = new HashSet<String>( getProcessMap().keySet() );
			removed_pids.removeAll(	all_pids );

			final List<String> killed = new ArrayList<String>();
			
			

			// before killing anything log the state
			// log current state :
			
			final StringBuffer log = new StringBuffer();
			log.append("status count of [unassigned,watched,all] processes : ["+getNewProcessMap().size()+","+getProcessMap().size()+","+getProcessList().size()+"]");
			if (logger.isDebugEnabled()) {
				log.append("\nwatched processes : ["+getProcessMap().size()+"]\n");
				for (final ProcessToWatch pct : getProcessMap().values()) {
					log.append(pct.toString());
				}
	
				log.append("\nyet unassigned processes : ["+getNewProcessMap().size()+"]\n");
				for (final ProcessToWatch pct : getNewProcessMap().values()) {
					log.append(pct.toString());
				}
				log.append("f\nound processes : ["+getProcessList().size()+"]\n");
				for (final ProcessToWatch pct : getProcessList()) {
					log.append(pct.toString());
				}
			}
			logger.info("current state : " + log.toString());
			
			// start cleaning up
			for (final ProcessToWatch pct : getProcessMap().values()) {
				if (removed_pids.contains( pct.getPid() )) {
					// process completed gracefully
					killed.add(pct.getPid());
					logger.info("removed process from watch : " + pct.toString());
				} else {
					// check if it needs to be dealt with
					if (pct.isSurvivor(timestamp) && killSurvivor(pct.getPid())) {
						killed.add(pct.getPid());
						logger.info("killed process and removed from watch : " + pct.toString());
					}
				}
			}
			
			for (final String key : killed) {
				getProcessMap().remove(key);
			}
			

		}
	}
	
	
	public String addProcessToWatch(final String[] identifiers, final int maxTimeToLive, final ProcessKilledNotifier notyfier) {
		final String id = UUID.randomUUID().toString();
		getNewProcessMap().put(id, new ProcessToWatch(id, identifiers, maxTimeToLive, notyfier));
		return id;
	}
	
	private Map<String, ProcessToWatch> getProcessMap() {
		return watchedProcessMap;
	}
	
	private Map<String, ProcessToWatch> getNewProcessMap() {
		return newProcessMap;
	}
	
	
	private boolean killSurvivor(final String pid) {
		final ByteArrayOutputStream err = new ByteArrayOutputStream();
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		final CommandLine cmdLine = new CommandLine(kill_cmd);
		cmdLine.addArguments(kill_cmd_params);
		cmdLine.addArguments(pid);
		final Executor executor = new DefaultExecutor();
		executor.setExitValue(1);
		final DefaultExecuteResultHandler derh = new DefaultExecuteResultHandler();
//		executor.setProcessDestroyer(MPlayerFileService.PROCESS_SHUTDOWN_HOOK);
		executor.setStreamHandler(new PumpStreamHandler(out, err));
		try {
			logger.info("execute " + kill_cmd_log + " " + pid);
			executor.execute(cmdLine, derh);
			derh.waitFor();
			return true;
		} catch (final ExecuteException e) {
			logger.error("error on " + kill_cmd_log + pid, e);
		} catch (final IOException e) {
			logger.error("error on " + kill_cmd_log + pid, e);
		} catch (final InterruptedException e) {
			logger.error("error on " + kill_cmd_log + pid, e);
		}
		return false;
	}


	
	private List<ProcessToWatch> getProcessList() {
		final ByteArrayOutputStream err = new ByteArrayOutputStream();
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		final CommandLine cmdLine = new CommandLine(list_cmd);
		cmdLine.addArguments(list_cmd_params);
		final Executor executor = new DefaultExecutor();
		executor.setExitValue(1);

		final DefaultExecuteResultHandler derh = new DefaultExecuteResultHandler();
		executor.setProcessDestroyer(MPlayerFileService.PROCESS_SHUTDOWN_HOOK);
		executor.setStreamHandler(new PumpStreamHandler(out, err));
		try {
			logger.info("execute " + list_cmd_log);
			executor.execute(cmdLine, derh);
			derh.waitFor();
			
		} catch (final ExecuteException e) {
			logger.error("error on " + list_cmd_log, e);
		} catch (final IOException e) {
			logger.error("error on " + list_cmd_log, e);
		} catch (final InterruptedException e) {
			logger.error("error on " + list_cmd_log, e);
		}
		
		final String filtered_text = out.toString().replace('\r', '\n');
		final List<ProcessToWatch> result = new ArrayList<ProcessToWatch>();
		
		final String[] lines = filtered_text.split("\n");
		boolean header = true;
		int pid_col = 0;
		int cmd_col = 0;
		int expectedColCount = 0;
		for (final String line : lines) {
			if (header) {
				final String[] columns = line.split(list_cmd_seperator);
				int idx = 0;
				for (final String col : columns) {
					if (col.toLowerCase().contains(list_cmd_pid_col)) {
						pid_col = idx;
						header = false;
					}
					if (col.toLowerCase().contains(list_cmd_command_col)) {
						cmd_col = idx;
						header = false;
					}
					idx++;
					
				}
				if (! header) {
					expectedColCount = idx;
				}
			} else {
				String[] columns = line.split(list_cmd_seperator);
				if (columns.length > expectedColCount) {
					String[] found = columns;
					columns = new String[expectedColCount];
					int idx = 0;
					int dif = found.length - expectedColCount;
					String cmd = "";
					for (String s : found) {
						if (idx != cmd_col) {
							columns[idx++] = s;
						} else {
							if (dif != 0) {
								cmd += s + ",";
								dif--;
							} else {
								columns[idx++] = cmd + s;
							}
						}
					}
				}
				if (columns.length == expectedColCount) {
					result.add(
							new ProcessToWatch(columns[pid_col].replaceAll(" ", ""), columns[cmd_col])
					);					
				}
			}		
		}
		
		return result;
	}


	@Override
	public void onApplicationEvent(final ContextClosedEvent event) {
		logger.info("shutting down, so all remaining processes have to get killed, sorry.");
		try {
			while (! killEmAll()) {
				this.wait(50);
			}
			logger.info("all processes killed.");
		} catch (final InterruptedException e) {
			logger.error("Got interrupted! Some processes may survive this shutdown!");
		}
	}

	private boolean killEmAll() {
		for (final ProcessToWatch pct : getProcessMap().values()) {
			pct.markAsSurvivor();
		}
		for (final ProcessToWatch pct : getNewProcessMap().values()) {
			pct.markAsSurvivor();
		}
		checkProcesses();
		return getProcessMap().isEmpty() && getNewProcessMap().isEmpty();
	}	
	
	
}
