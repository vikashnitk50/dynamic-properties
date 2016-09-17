package com.properties.reader.common.exception;


public class PropertiesReaderRuntimeException extends RuntimeException {

	
	private static final long serialVersionUID = 1287506188023072691L;

	
	public PropertiesReaderRuntimeException(String message) {
		super(message);
	}

	
	public PropertiesReaderRuntimeException(Throwable cause) {
		super(cause);
	}

	
	public PropertiesReaderRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

}
