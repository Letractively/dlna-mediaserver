package de.sosd.mediaserver;

import java.util.ArrayList;
import java.util.List;

import de.sosd.mediaserver.bean.ProcessToWatch;

public class SplitCutTest {

	/**
	 * @param args
	 */
	public static void main(final String[] args) {
		
		final String list_cmd_seperator = ",";
		final String list_cmd_pid_col = "processid";
		final String list_cmd_command_col = "commandline";
		
		final String[] lines = {
				"Node,CommandLine,ProcessId",
				"CARBON,,4",
				"CARBON,c:\\mplayer -vo null,12421", 
				"CARBON,c:\\mplayer -vo null,3523", 
				"CARBON,c:\\mplayer -vo null,543",
				"CARBON,c:\\mplayer -vo null,3463",
				"CARBON,c:\\mplayer -vo null,636",

		};
		boolean first = true;
		int pid_col = 0;
		int cmd_col = 0;
		final List<ProcessToWatch> result = new ArrayList<ProcessToWatch>();
		for (final String line : lines) {
			if (first) {
				final String[] columns = line.split(list_cmd_seperator);
				int idx = 0;
				for (final String col : columns) {
					if (col.toLowerCase().contains(list_cmd_pid_col)) {
						pid_col = idx;
					}
					if (col.toLowerCase().contains(list_cmd_command_col)) {
						cmd_col = idx;
					}
					idx++;
				}
				
				first = false;
			} else {
				final String[] columns = line.split(list_cmd_seperator);
				result.add(
						new ProcessToWatch(columns[pid_col].replaceAll(" ", ""), columns[cmd_col])
				);
			}		
		}
		for (final ProcessToWatch pct : result) {
			System.out.println(pct.toString());
		}	
	}

}
