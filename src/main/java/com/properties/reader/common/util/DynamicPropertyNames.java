package com.properties.reader.common.util;


public enum DynamicPropertyNames {
	
	IP_ADDRESS("ip.address");
	
	private final String key;

	private DynamicPropertyNames(String key) {
		this.key = key;
	}

	public String key() {
		return key;
	}

}
