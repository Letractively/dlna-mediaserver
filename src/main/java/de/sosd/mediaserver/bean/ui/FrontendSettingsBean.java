package de.sosd.mediaserver.bean.ui;


public class FrontendSettingsBean {

	private String name;	
	private String url;
	private String networkInterface;
	private String previews;
	private String mplayer;
	private String mencoder;

	public FrontendSettingsBean() {}

	public FrontendSettingsBean(final String name,
			final String networkInterface, final String url, final String previews,
			final String mplayer, final String mencoder) {
		super();
		this.name = name;
		this.networkInterface = networkInterface;
		this.url = url;
		this.previews = previews;
		this.mplayer = mplayer;
		this.mencoder = mencoder;
	}

	public String getName() {
		return this.name;
	}
	
	public void setName(final String name) {
		this.name = name;
	}

	public String getNetworkInterface() {
		return getNotNullString(this.networkInterface);
	}

	public void setNetworkInterface(final String networkInterface) {
		this.networkInterface = getNotEmptyString(networkInterface);
	}

	public String getUrl() {
		return getNotNullString(this.url);
	}

	public void setUrl(final String url) {
		this.url = getNotEmptyString(url);
	}

	public String getPreviews() {
		return getNotNullString(this.previews);
	}

	public void setPreviews(final String previews) {
		this.previews = getNotEmptyString(previews);
	}

	public String getMplayer() {
		return getNotNullString(this.mplayer);
	}

	public void setMplayer(final String mplayer) {
		this.mplayer = getNotEmptyString(mplayer);
	}

	public String getMencoder() {
		return getNotNullString(this.mencoder);
	}

	public void setMencoder(final String mencoder) {
		this.mencoder = getNotEmptyString(mencoder);
	}
	
	private String getNotNullString(final String s) {
		if (s == null) {
			return "";
		}
		return s;
	}
	
	private String getNotEmptyString(final String s) {
		if (s == null) {
			return s;
		}
		if (s.length() == 0) {
			return null;
		}
		return s;
	}
}
