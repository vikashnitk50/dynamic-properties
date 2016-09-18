package com.properties.reader.common.util;

import java.io.IOException;
import java.io.InputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public final class SafeCloseUtil {

	private static Logger LOG = LoggerFactory.getLogger(SafeCloseUtil.class);

	/**
	 * constructor
	 */
	private SafeCloseUtil() {
	}

	/**
	 * safeClose resource
	 * 
	 * @param InputStream
	 */
	public static void closeQuietly(InputStream is) {
		if (is != null) {
			try {
				is.close();
				is = null;
			} catch (IOException e) {
				LOG.error("close InputStream error.", e);
			}
		}
	}

	

}
