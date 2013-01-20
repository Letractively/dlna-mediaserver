package de.sosd.mediaserver.domain.db;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class ThumbnailDomain {

	@Column(name="thumb_type", length=5)
	private String type;
	
	@Column(name="thumb_resolution", length=9)
	private String resolution;

	public ThumbnailDomain() {}
	

	public ThumbnailDomain(final String type, final String resolution) {
		super();
		this.type = type;
		this.resolution = resolution;
	}


	/**
	 * @return the type
	 */
	public String getType() {
		return this.type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(final String type) {
		this.type = type;
	}

	/**
	 * @return the resolution
	 */
	public String getResolution() {
		return this.resolution;
	}

	/**
	 * @param resolution the resolution to set
	 */
	public void setResolution(final String resolution) {
		this.resolution = resolution;
	}
	
	
}
