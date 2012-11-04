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

import de.sosd.mediaserver.bean.WebappLocationBean;
import de.sosd.mediaserver.dao.DidlDao;
import de.sosd.mediaserver.dao.SystemDao;
import de.sosd.mediaserver.dao.support.DidlDaoSupport;
import de.sosd.mediaserver.service.MediaserverConfiguration;
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
	private DidlDao didlDao;
	
	@Autowired
	private SystemDao systemDao;
	
	@Autowired
	private DidlDaoSupport helper;
	
	@Autowired
	private MediaserverConfiguration cfg;
	
	public int getSystemUpdateId() {
		return systemDao.getSystemUpdateId(cfg.getUSN());
	}

	public void browseMetadata(final Browse request, final BrowseResponse response, final DeviceByRequestHeader device, WebappLocationBean wlb) throws JAXBException, IOException {
		final String objectId = getLocalObjectId(request.getObjectID());
		final String filter = this.helper.translateFilter( request.getFilter());
		final String sort = this.helper.translateSortCriteria( request.getSortCriteria() );		
		
		final Integer requestedCount = request.getRequestedCount();
		int fetchSize = Integer.MAX_VALUE;
		if (requestedCount != null && requestedCount.intValue() > 0) {
			fetchSize = requestedCount.intValue();
		}
		final DidlXmlCreator didlLite = this.didlDao.getInfoById(
				objectId, 
				request.getStartingIndex(), 
				fetchSize,
				filter,
				sort,			
				wlb);
		String responseText = createResponse(response, didlLite);
//		System.out.println("BrowseMetadata ["+request.getObjectID()+","+request.getStartingIndex()+","+request.getRequestedCount()+"] -> ["+response.getNumberReturned().getValue()+","+response.getTotalMatches().getValue()+"] ");
//		System.out.println(responseText);		
		logger.info("BrowseMetadata ["+request.getObjectID()+","+request.getStartingIndex()+","+request.getRequestedCount()+"] -> ["+response.getNumberReturned()+","+response.getTotalMatches()+"]");	
		
	}

	private String getLocalObjectId(final String objectID) {
		if (objectID.equals("0")) {
			return this.cfg.getUSN();
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

	public void browseDirectChildren(final Browse request,	final BrowseResponse response, final DeviceByRequestHeader device, WebappLocationBean wlb) throws JAXBException, IOException {	
		final String objectId = getLocalObjectId(request.getObjectID());
		final String filter = this.helper.translateFilter( request.getFilter());
		final String sort = this.helper.translateSortCriteria( request.getSortCriteria() );
		
		final Integer requestedCount = request.getRequestedCount();
		int fetchSize = Integer.MAX_VALUE;
		if (requestedCount != null && requestedCount.intValue() > 0) {
			fetchSize = requestedCount.intValue();
		}
		final DidlXmlCreator didl = this.didlDao.getContainerContentById(
					objectId, 
					request.getStartingIndex(), 
					fetchSize,
					filter,
					sort,
					wlb);
		
		
		String responseText = createResponse(response, didl);
//		System.out.println("BrowseDirectChildren ["+request.getObjectID()+","+request.getStartingIndex()+","+request.getRequestedCount()+"] -> ["+response.getNumberReturned().getValue()+","+response.getTotalMatches().getValue()+"] ");
//		System.out.println(responseText);
		logger.info("BrowseDirectChildren ["+request.getObjectID()+","+request.getStartingIndex()+","+request.getRequestedCount()+"] -> ["+response.getNumberReturned()+","+response.getTotalMatches()+"] ");	
	}

	public void search(final Search request, final SearchResponse response,
			final DeviceByRequestHeader device, WebappLocationBean wlb) throws JAXBException, IOException {
		logger.info("Search ["+request.getContainerID()+","+request.getStartingIndex()+","+request.getRequestedCount()+","+request.getSearchCriteria()+"] -> ["+response.getNumberReturned()+","+response.getTotalMatches()+"] ");	

		final String objectId = getLocalObjectId(request.getContainerID());
		final String filter = this.helper.translateFilter( request.getFilter());
		final String sort = this.helper.translateSortCriteria( request.getSortCriteria() );
		
		final ArrayList<Object> searchParameters = new ArrayList<Object>();
		final String where = this.helper.translateSearchCriteria(request.getSearchCriteria(), searchParameters);
		
		final Integer requestedCount = request.getRequestedCount();
		int fetchSize = Integer.MAX_VALUE;
		if (requestedCount != null && requestedCount.intValue() > 0) {
			fetchSize = requestedCount.intValue();
		}
		
//		if (request.getContainerId().equals("0")) {
		final DidlXmlCreator didl = this.didlDao.getSearchItems(objectId,
					where,
					searchParameters,
					request.getStartingIndex(), 
					fetchSize,
					filter,
					sort,
					wlb
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
