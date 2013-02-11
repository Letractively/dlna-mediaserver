package de.sosd.mediaserver;

import java.net.URISyntaxException;

import javax.xml.bind.JAXBException;

public class TestSoap {

    /**
     * @param args
     * @throws JAXBException
     * @throws URISyntaxException
     */
    public static void main(final String[] args) throws JAXBException,
            URISyntaxException {
        // // create JAXB context and instantiate marshaller
        // PreferredNamespaceMapper preferredNamespaceMapper = new
        // de.sosd.mediaserver.xml.PreferredNamespaceMapper();
        // JAXBContext context = JAXBContext.newInstance(BrowseResponse.class);
        //
        // Marshaller m = context.createMarshaller();
        // m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        // m.setProperty("com.sun.xml.internal.bind.namespacePrefixMapper",
        // preferredNamespaceMapper);
        // BrowseResponse response = new BrowseResponse();
        // fillResponse(response);
        //
        // m.marshal(response , System.out);
        //
        // // get variables from our xml file, created before
        // System.out.println();
        //
        // context = JAXBContext.newInstance(DidleLite.class);
        // m = context.createMarshaller();
        // m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        // m.setProperty("com.sun.xml.internal.bind.namespacePrefixMapper",
        // preferredNamespaceMapper);
        // m.marshal(response.getResult().getItem() , System.out);
        // // create JAXB context and instantiate marshaller
        // context = JAXBContext.newInstance(BrowseRequest.class);
        // m = context.createMarshaller();
        //
        // System.out.println();
        //
        // m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        // m.setProperty("com.sun.xml.internal.bind.namespacePrefixMapper",
        // preferredNamespaceMapper);
        // BrowseRequest request = new BrowseRequest();
        // fillRequest(request);
        //
        // m.marshal(request , System.out);
        //
        // // get variables from our xml file, created before
        // System.out.println();
        //
        // context = JAXBContext.newInstance(GetSystemUpdateIDResponse.class);
        // m = context.createMarshaller();
        // m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        // m.setProperty("com.sun.xml.internal.bind.namespacePrefixMapper",
        // preferredNamespaceMapper);
        // m.marshal(getUpdateId() , System.out);
        //
        //
        // System.out.println();
        //
        // context =
        // JAXBContext.newInstance("org.upnp.schemas.metadata_1_0.didl_lite");
        // m = context.createMarshaller();
        //
        // m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        // m.setProperty("com.sun.xml.internal.bind.namespacePrefixMapper",
        // preferredNamespaceMapper);
        // m.marshal(getGenDIDL() , System.out);
        //
        // //
        // // StringWriter sw = new StringWriter();
        // // try {
        // // m = JAXBContext.newInstance(DIDLLite.class).createMarshaller();
        // // m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        // // m.setProperty("com.sun.xml.bind.namespacePrefixMapper",
        // preferredNamespaceMapper);
        // // m.marshal(getGenDIDL(), sw);
        // // String result = sw.getBuffer().toString();
        // // int start = result.indexOf("?>");
        // //
        // // String didl = result.substring(start+2);
        // //
        // // context =
        // JAXBContext.newInstance(org.upnp.schemas.service.contentdirectory._1.BrowseResponse.class);
        // // m = context.createMarshaller();
        // //
        // // m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        // // m.setProperty("com.sun.xml.bind.namespacePrefixMapper",
        // preferredNamespaceMapper);
        // // org.upnp.schemas.service.contentdirectory._1.BrowseResponse br =
        // new org.upnp.schemas.service.contentdirectory._1.BrowseResponse();
        // //
        // // ResultType resultType = new ResultType();
        // // resultType.setDIDLLite(didl);
        // // br.setResult(didl);
        // // br.setUpdateID(2);
        // // br.setTotalMatches(1);
        // // br.setNumberReturned(1);
        // // m.marshal(br , System.out);
        // //
        // // } catch (JAXBException e) {
        // //
        // // } finally {
        // // try {
        // // sw.close();
        // // } catch (IOException e) {
        // //
        // // }
        // }
        //
    }
    //
    // private static DIDLLite getGenDIDL() {
    // DIDLLite result = new DIDLLite();
    // DIDLContainer dc = new DIDLContainer();
    //
    // result.getContainer().add(dc);
    //
    // dc.setId(UUID.randomUUID().toString());
    // dc.setParentID(UUID.randomUUID().toString());
    // dc.setRestricted(false);
    // dc.setTitle("My Title");
    // dc.setClazz(ClassType.OBJECT_CONTAINER_STORAGE_FOLDER);
    // dc.setChildCount(0l);
    // DIDLResource res = new DIDLResource();
    // dc.getRes().add(res);
    //
    // res.setValue("http://localhost:9090/mediaserver?1");
    // res.setProtocolInfo("http-get:*:image/jpeg:DLNA.ORG_PN=JPEG_TN");
    // return result;
    // }
    //
    // private static GetSystemUpdateIDResponse getUpdateId() {
    // GetSystemUpdateIDResponse sysup = new GetSystemUpdateIDResponse();
    // sysup.setId(2);
    // return sysup;
    // }
    //
    // private static void fillRequest(BrowseRequest request) {
    // request.setBrowseFlag("BrowseMetadata");
    // request.setFilter("@id, upnp:class, res, res@protocolInfo, res@av:authenticationUri, res@size, dc:title, upnp:albumArtURI, res@dlna:ifoFileURI, res@protection, res@bitrate, res@duration, res@sampleFrequency, res@bitsPerSample, res@nrAudioChannels, res@resolution, res@colorDepth, dc:date, av:dateTime, upnp:artist, upnp:album, upnp:genre, dc:contributer, upnp:storageFree, upnp:storageUsed, upnp:originalTrackNumber, dc:publisher, dc:language, dc:region, dc:description, upnp:toc, @childCount, upnp:albumArtURI@dlna:profileID");
    // request.setObjectID("0");
    // request.setRequestedCount(10);
    // request.setStartingIndex(0);
    // request.setSortCriteria("");
    //
    // }
    //
    // private static void fillResponse(BrowseResponse response) throws
    // URISyntaxException {
    // response.setResult(new MsDataResult());
    // response.getResult().setItem(new DidleLite());
    // response.setTotalMatches(1);
    // response.setUpdateId(4);
    // response.setNumberReturned(1);
    //
    // DidleContainer didleContainer = new DidleContainer();
    // ((DidleLite)
    // response.getResult().getItem()).getContainerList().add(didleContainer);
    // didleContainer.setId(UUID.randomUUID().toString());
    // didleContainer.setParentID(UUID.randomUUID().toString());
    // didleContainer.setRestricted(false);
    // didleContainer.setTitle("My Title");
    // didleContainer.setType("object.container");
    // didleContainer.setChildCount(0);
    // didleContainer.getResources().add(new
    // URI("http://www.test.de:8080/dl/item?1"));
    // }

}
