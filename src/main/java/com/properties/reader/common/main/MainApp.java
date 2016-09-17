package com.properties.reader.common.main;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.properties.reader.common.config.Config;
import com.properties.reader.common.file.watcher.DirectoryWatchService;
import com.properties.reader.common.file.watcher.DynamicPropertiesOnFileChangeListener;
import com.properties.reader.common.file.watcher.SimpleDirectoryWatchService;
import com.properties.reader.common.file.watcher.DirectoryWatchService.OnFileChangeListener;
import com.properties.reader.common.util.DynamicPropertiesReader;

public class MainApp {
	
	private static final Logger LOGGER = LogManager.getLogger(MainApp.class);
	
	
	public static void main(String[] args) throws Exception {
		String ip = Config.getIPAddress();
		System.out.println(ip);
		DirectoryWatchService watchService = new SimpleDirectoryWatchService();
		OnFileChangeListener listener=new DynamicPropertiesOnFileChangeListener();
		watchService.register(listener, DynamicPropertiesReader.getInstance().getDirPath() , "*.properties");
		LOGGER.info("Starting file watcher:");
		watchService.start();
		Thread.sleep(60000);
		String newIp = Config.getIPAddress();
		System.out.println(newIp);
		watchService.stop();
	}

}
