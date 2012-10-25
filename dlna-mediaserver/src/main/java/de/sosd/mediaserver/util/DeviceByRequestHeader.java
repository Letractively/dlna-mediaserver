package de.sosd.mediaserver.util;

import javax.servlet.http.HttpServletRequest;

public class DeviceByRequestHeader {

	private final String[] device_headers = {"x-av-client-info", "user-agent"};
	
	private String device = "";
	
	public DeviceByRequestHeader() {
	}

	public DeviceByRequestHeader(final HttpServletRequest request) { 
		for (final String header : this.device_headers) {
			final String value = request.getHeader(header);
			if (value != null) {
				this.device += header + ": " + value + ", ";
			}
		}
	}

	public boolean isPS3() {
		return this.device.contains("PLAYSTATION");
	}
	
	
}
