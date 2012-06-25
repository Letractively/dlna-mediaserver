package de.sosd.mediaserver.bean;

public class StringKeyValuePair {

	private String key;
	private String value;
	
	public StringKeyValuePair() {}
	
	public StringKeyValuePair(final String key, final String value) {
		super();
		this.key = key;
		this.value = value;
	}
	
	public String getKey() {
		return this.key;
	}
	public void setKey(final String key) {
		this.key = key;
	}
	public String getValue() {
		return this.value;
	}
	public void setValue(final String value) {
		this.value = value;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((this.key == null) ? 0 : this.key.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final StringKeyValuePair other = (StringKeyValuePair) obj;
		if (this.key == null) {
			if (other.key != null) {
				return false;
			}
		} else if (!this.key.equals(other.key)) {
			return false;
		}
		return true;
	}

	
	
	
	
	
}
