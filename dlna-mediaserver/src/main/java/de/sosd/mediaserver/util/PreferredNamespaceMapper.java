package de.sosd.mediaserver.util;

import de.sosd.mediaserver.domain.ws.DLNA_NAMESPACES;

@SuppressWarnings("restriction")
public class PreferredNamespaceMapper extends com.sun.xml.internal.bind.marshaller.NamespacePrefixMapper 
{

	@Override
	public String getPreferredPrefix(final String namespaceUri, final String suggestion,
			final boolean requirePrefix) {

		if (namespaceUri == null) {
			return suggestion;
		}
		
		try {		
			return DLNA_NAMESPACES.fromValue(namespaceUri).prefix();
		} catch (IllegalArgumentException iae) {
			
		}
		return suggestion;
	}

}
