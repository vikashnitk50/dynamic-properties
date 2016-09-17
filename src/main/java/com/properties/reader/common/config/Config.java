package com.properties.reader.common.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.properties.reader.common.exception.PropertiesReaderRuntimeException;
import com.properties.reader.common.util.DynamicPropertiesReader;
import com.properties.reader.common.util.DynamicPropertyNames;


public class Config {
	
	private final static Logger LOG = LoggerFactory.getLogger(Config.class);
	private static DynamicPropertiesReader DYNAMIC_PROPERTIES_READER;

	static {
		try {
			DYNAMIC_PROPERTIES_READER = DynamicPropertiesReader.getInstance();
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
	}
	
	public static final String getIPAddress() {
		try {
			String tmaIp = DYNAMIC_PROPERTIES_READER
					.getStringPropertyValue(DynamicPropertyNames.IP_ADDRESS);
			return tmaIp;
		} catch (Exception e) {
			throw new PropertiesReaderRuntimeException("identify ip is not given.", e);
		}
	}

}
