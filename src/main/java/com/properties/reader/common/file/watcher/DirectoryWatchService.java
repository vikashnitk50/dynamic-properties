package com.properties.reader.common.file.watcher;

import java.io.IOException;

public interface DirectoryWatchService extends Service {

	public void start() throws Exception;

	void register(OnFileChangeListener listener, String dirPath,
			String... globPatterns) throws IOException;

	interface OnFileChangeListener {

		void onFileCreate(String filePath);

		void onFileModify(String filePath);

		void onFileDelete(String filePath);
	}
}