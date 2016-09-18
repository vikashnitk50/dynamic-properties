package com.properties.reader.common.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.properties.reader.common.constants.Constants;
import com.properties.reader.common.exception.PropertiesReaderRuntimeException;

public class DynamicPropertiesReader {

	private static final Logger LOG = LoggerFactory
			.getLogger(DynamicPropertiesReader.class);

	private Properties props;

	private String propertiesFileName;

	private String dirPath;

	private static final DynamicPropertiesReader INSTANCE = new DynamicPropertiesReader();

	/**
	 * private constructor
	 */
	private DynamicPropertiesReader() {
		this.propertiesFileName = Constants.DIRECTORY + Constants.PATH
				+ Constants.PROPERTIES_FILE_NAME;
		setDir();
		load();
	}

	public static DynamicPropertiesReader getInstance() {
		return INSTANCE;
	}

	private void setDir() {
		URL url = this.getClass().getClassLoader()
				.getResource(propertiesFileName);
		File path = new File(url.getPath());
		this.dirPath = path.getParent();
	}

	public void load() {
		InputStream in = null;
		try {
			in = this.getClass().getClassLoader()
					.getResourceAsStream(propertiesFileName);
			if (in != null) {
				Properties properties = new Properties();
				properties.load(in);
				this.props = properties;
				printLogMessage("Loaded DYNAMIC Properties: {}", props
						.entrySet().toString());
			} else {
				throw new PropertiesReaderRuntimeException(
						"Couldn't find properties file: " + propertiesFileName);
			}
		} catch (IOException e) {
			throw new PropertiesReaderRuntimeException(
					"IOException reading properties file: "
							+ propertiesFileName, e);
		} finally {
			SafeCloseUtil.closeQuietly(in);
		}
	}

	public String getStringPropertyValue(DynamicPropertyNames name) {
		return getStringPropertyValue(name, true);
	}

	public Integer getIntegerPropertyValue(DynamicPropertyNames name) {
		return getIntegerPropertyValue(name, true);
	}

	public Integer getIntegerPropertyValue(DynamicPropertyNames name,
			boolean mustExist) {
		String value = getStringPropertyValue(name, mustExist);
		try {
			if (value != null) {
				int intVal = Integer.parseInt(value);
				return intVal;
			}
		} catch (NumberFormatException ex) {
			throw new PropertiesReaderRuntimeException(
					"Unable to parse string into integer :" + value);
		}
		return null;
	}

	public String getStringPropertyValue(DynamicPropertyNames name,
			boolean mustExist) {
		if (props != null) {
			String val = props.getProperty(name.key());
			if (mustExist && val == null)
				throw new PropertiesReaderRuntimeException("Property " + name
						+ " not set in properties file.");
			return val;
		} else {
			throw new PropertiesReaderRuntimeException(
					"Dynamic properties never initialized.");
		}
	}

	public String getDirPath() {
		return dirPath;
	}

	private void printLogMessage(String logMessage, Object logParame) {
		if (LOG.isDebugEnabled()) {
			LOG.debug(logMessage, logParame);
		}
	}

}
