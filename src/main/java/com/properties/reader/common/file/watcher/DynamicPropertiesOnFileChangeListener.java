package com.properties.reader.common.file.watcher;

import com.properties.reader.common.file.watcher.DirectoryWatchService.OnFileChangeListener;
import com.properties.reader.common.util.DynamicPropertiesReader;

public class DynamicPropertiesOnFileChangeListener implements OnFileChangeListener{

	@Override
	public void onFileCreate(String filePath) {
		// DO NOTHING
		
	}

	@Override
	public void onFileModify(String filePath) {
		DynamicPropertiesReader.getInstance().load();
		
	}

	@Override
	public void onFileDelete(String filePath) {
		// DO NOTHING
		
	}

}
