package de.sosd.mediaserver.service.dlna;

import java.io.IOException;
import java.util.ArrayList;

import javax.xml.bind.JAXBException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.upnp.schemas.service.contentdirectory._1.Browse;
import org.upnp.schemas.service.contentdirectory._1.BrowseResponse;
import org.upnp.schemas.service.contentdirectory._1.ContentResponse;
import org.upnp.schemas.service.contentdirectory._1.GetSearchCapabilities;
import org.upnp.schemas.service.contentdirectory._1.GetSearchCapabilitiesResponse;
import org.upnp.schemas.service.contentdirectory._1.GetSortCapabilities;
import org.upnp.schemas.service.contentdirectory._1.GetSortCapabilitiesResponse;
import org.upnp.schemas.service.contentdirectory._1.Search;
import org.upnp.schemas.service.contentdirectory._1.SearchResponse;

import de.sosd.mediaserver.domain.db.SystemDomain;
import de.sosd.mediaserver.service.MediaserverConfiguration;
import de.sosd.mediaserver.service.db.StorageHelper;
import de.sosd.mediaserver.service.db.StorageService;
import de.sosd.mediaserver.util.DeviceByRequestHeader;
import de.sosd.mediaserver.util.DidlXmlCreator;

@Service
public class ContentDirectoryService {

	private final static Log logger = LogFactory.getLog(ContentDirectoryService.class);

////NOTIFY * HTTP/1.1
////Host:239.255.255.250:1900
////NT:urn:schemas-upnp-org:service:ContentDirectory:1
////NTS:ssdp:alive
////Location:http://192.168.101.227:2869/upnphost/udhisapi.dll?content=uuid:cdca376d-cb81-4b2e-95a7-9bf7dd2347a3
////USN:uuid:cdca376d-cb81-4b2e-95a7-9bf7dd2347a3::urn:schemas-upnp-org:service:ContentDirectory:1
////Cache-Control:max-age=900
////Server:Microsoft-Windows-NT/5.1 UPnP/1.0 UPnP-Device-Host/1.0
////OPT:"http://schemas.upnp.org/upnp/1/0/"; ns=01
////01-NLS:b6d7028a8b1e541d7e757d0b6db33a00
////
////7d0b6db33a00	
	
	
	@Autowired
	private StorageService storage;
	
	@Autowired
	private StorageHelper helper;
	
	@Autowired
	private MediaserverConfiguration cfg;
	
	public int getSystemUpdateId() {
		SystemDomain stats = this.storage.getSystemProperties();
		
		if (stats == null) {
			stats = this.storage.initSystem();
		}
		logger.info("GetSystemUpdateId -> " + stats.getUpdateId());
		return stats.getUpdateId();
	}

	public void browseMetadata(final Browse request, final BrowseResponse response, final DeviceByRequestHeader device) throws JAXBException, IOException {
		final String objectId = getLocalObjectId(request.getObjectID());
		final String filter = this.helper.translateFilter( request.getFilter());
		final String sort = this.helper.translateSortCriteria( request.getSortCriteria() );		
		final DidlXmlCreator didlLite = this.storage.getInfoById(
				objectId, 
				request.getStartingIndex(), 
				request.getRequestedCount(),
				filter,
				sort			
				);
		String responseText = createResponse(response, didlLite);
//		System.out.println("BrowseMetadata ["+request.getObjectID()+","+request.getStartingIndex()+","+request.getRequestedCount()+"] -> ["+response.getNumberReturned().getValue()+","+response.getTotalMatches().getValue()+"] ");
//		System.out.println(responseText);		
		logger.info("BrowseMetadata ["+request.getObjectID()+","+request.getStartingIndex()+","+request.getRequestedCount()+"] -> ["+response.getNumberReturned()+","+response.getTotalMatches()+"]");	
		
	}

	private String getLocalObjectId(final String objectID) {
		if (objectID.equals("0")) {
			return this.cfg.getHostname();
		}
		return objectID;
	}

	private String createResponse(final ContentResponse response, final DidlXmlCreator didlLite) throws JAXBException, IOException {
//		JAXBContext context = JAXBContext.newInstance(DIDLLite.class);
//		Marshaller m = context.createMarshaller();
//		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, false);
//		m.setProperty("com.sun.xml.internal.bind.namespacePrefixMapper",  new PreferredNamespaceMapper());
//		m.setProperty("jaxb.fragment", Boolean.TRUE);
//		
//		StringWriter sw = new StringWriter();
//		m.marshal(didlLite , sw);
//		sw.close();
		response.setResult(didlLite.getXml());
		response.setNumberReturned(didlLite.getTotalObjectCount());
		response.setTotalMatches(didlLite.getTotalMatchesCount());	
		response.setUpdateID(getSystemUpdateId());
		return response.getResult();
	}

//	private int countContainer(List<ContainerType> container) {
//		int result = container.size();
//		for (ContainerType ct : container) {
//			result += countContainer(ct.getContainer());
//			result += ct.getItem().size();
//		}
//		
//		return result;
//	}

	public void browseDirectChildren(final Browse request,	final BrowseResponse response, final DeviceByRequestHeader device) throws JAXBException, IOException {	
		final String objectId = getLocalObjectId(request.getObjectID());
		final String filter = this.helper.translateFilter( request.getFilter());
		final String sort = this.helper.translateSortCriteria( request.getSortCriteria() );		
		final DidlXmlCreator didl = this.storage.getContainerContentById(
					objectId, 
					request.getStartingIndex(), 
					request.getRequestedCount(),
					filter,
					sort
					);
		
		
		String responseText = createResponse(response, didl);
//		System.out.println("BrowseDirectChildren ["+request.getObjectID()+","+request.getStartingIndex()+","+request.getRequestedCount()+"] -> ["+response.getNumberReturned().getValue()+","+response.getTotalMatches().getValue()+"] ");
//		System.out.println(responseText);
		logger.info("BrowseDirectChildren ["+request.getObjectID()+","+request.getStartingIndex()+","+request.getRequestedCount()+"] -> ["+response.getNumberReturned()+","+response.getTotalMatches()+"] ");	
	}

	public void search(final Search request, final SearchResponse response,
			final DeviceByRequestHeader device) throws JAXBException, IOException {
		// String objectId = getLocalObjectId(request.getContainerId());
		final String filter = this.helper.translateFilter( request.getFilter());
		final String sort = this.helper.translateSortCriteria( request.getSortCriteria() );
		
		final ArrayList<Object> searchParameters = new ArrayList<Object>();
		final String where = this.helper.translateSearchCriteria(request.getSearchCriteria(), searchParameters);
		
//		if (request.getContainerId().equals("0")) {
		final DidlXmlCreator didl = this.storage.getAllItems(
					where,
					searchParameters,
					request.getStartingIndex(), 
					request.getRequestedCount(),
					filter,
					sort
				);		
//		
//		
		
		String responseText = createResponse(response, didl);
//		System.out.println("Search ["+request.getContainerId()+","+request.getStartingIndex()+","+request.getRequestedCount()+"] -> ["+response.getNumberReturned().getValue()+","+response.getTotalMatches().getValue()+"] ");
//		System.out.println(responseText);		
		logger.info("Search ["+request.getContainerID()+","+request.getStartingIndex()+","+request.getRequestedCount()+","+request.getSearchCriteria()+"] -> ["+response.getNumberReturned()+","+response.getTotalMatches()+"] ");	
	}

	public void getSearchCapabilities(final GetSearchCapabilities request,
			final GetSearchCapabilitiesResponse response) {
		response.setSearchCaps(this.helper.getGetFilters());
		logger.info("GetSearchCapabilities -> " + response.getSearchCaps());	
	}

	public void getSortCapabilities(final GetSortCapabilities request,
			final GetSortCapabilitiesResponse response) {
		response.setSortCaps(this.helper.getGetSortCriterias());
		logger.info("GetSortCapabilities -> " + response.getSortCaps());	
	}

}
