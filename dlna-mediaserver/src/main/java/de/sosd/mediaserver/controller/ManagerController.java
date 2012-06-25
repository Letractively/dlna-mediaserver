package de.sosd.mediaserver.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import de.sosd.mediaserver.bean.FrontendFolderBean;
import de.sosd.mediaserver.bean.FrontendSettingsBean;
import de.sosd.mediaserver.service.WebInterfaceBackendService;

/**
 * Handles requests for the application home page.
 */
@Controller
@RequestMapping("/manager/*")
public class ManagerController {
	
	private static final Logger logger = LoggerFactory.getLogger(ManagerController.class);
	
	
	@Autowired
	private WebInterfaceBackendService backend;
	
//	private final static FrontendSettingsBean server = new FrontendSettingsBean("carbon", 9090, "eth3","http://192.168.101.227:/mediaserver", "", "", "");
//	private final static List<FrontendFolderBean> folders = new ArrayList<FrontendFolderBean>();
	
//	static {
//		folders.add(new FrontendFolderBean("/mnt/mldonkey/incoming"));
//	}
	
	/**
	 * Simply selects the home view to render by returning its name.
	 */
	@RequestMapping(value = "", method = RequestMethod.GET)
	public RedirectView home() {
		logger.debug("home");
		return new RedirectView("/manager/show", true);
	}
	
	/**
	 * Simply selects the home view to render by returning its name.
	 */
	@RequestMapping(value = "show", method = RequestMethod.GET)
	public ModelAndView show() {
		logger.debug("manager");
		return buildManagerModelAndView();
	}
	
	private ModelAndView buildManagerModelAndView() {
		final ModelAndView modelAndView = new ModelAndView("manager");
		
		final FrontendSettingsBean settings = this.backend.loadSettings();
		final List<FrontendFolderBean> folders = this.backend.loadScanFolders();
		modelAndView.addObject("server", settings);
		modelAndView.addObject("folderList", folders);
		return modelAndView;
	}
	
	@RequestMapping(value = "settings/update", method = RequestMethod.POST)
	public RedirectView updateServerSettings(
			@RequestParam("name") final String name,
			@RequestParam("mencoder") final String mencoder,
			@RequestParam("mplayer") final String mplayer,
			@RequestParam("networkInterface") final String networkInterface,
			@RequestParam("previews") final String previews
	) {
		logger.debug("updateServerSettings");
		
		this.backend.updateServerSettings(name,networkInterface, previews, mplayer, mencoder);
		
		return new RedirectView("/manager/show", true);
	}
	
	@RequestMapping(value = "scanfolder/remove")
	public RedirectView removeScanfolder(@RequestParam("folder") final String folderId) {
		logger.debug("removeScanfolder");		
		this.backend.removeFolderById(folderId);
		
		return new RedirectView("/manager/show", true);
	}
	
	@RequestMapping(value = "scanfolder/add")
	public RedirectView addScanfolder(@RequestParam("folder") final String folderPath) {
		logger.debug("addScanfolder");
		
		this.backend.addScanFolder(folderPath);

		return new RedirectView("/manager/show", true);
	}	

	@RequestMapping(value = "scanfolder/update")
	public RedirectView updateScanfolder(@RequestParam("folder") final String folderId, @RequestParam("scan_interval") final String scanInterval) {
		logger.debug("updateScanfolder");
		
		try {
			
			if ((scanInterval != null) && (scanInterval.length() > 0)) {
				final int parseInt = Integer.parseInt(scanInterval);
				
				if ((parseInt > 0) && (parseInt < (60 * 24 * 7 * 365))) {
					this.backend.updateScanInterval(folderId, parseInt);
				}
			}
			
		} catch (final NumberFormatException nfe) {
			// TODO say that something went wrong
		}
		
	

		return new RedirectView("/manager/show", true);
	}		
}
