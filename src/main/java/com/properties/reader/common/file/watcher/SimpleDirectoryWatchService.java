package com.properties.reader.common.file.watcher;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static java.nio.file.StandardWatchEventKinds.*;

/**
 * https://gist.github.com/hindol-viz/394ebc553673e2cd0699
 * @author Vikash
 *
 */
public class SimpleDirectoryWatchService implements DirectoryWatchService,
		Runnable {

	private static final Logger LOGGER = LogManager
			.getLogger(SimpleDirectoryWatchService.class);
	private final WatchService mWatchService;
	private final AtomicBoolean mIsRunning;
	private final ConcurrentMap<WatchKey, Path> mWatchKeyToDirPathMap;
	private final ConcurrentMap<Path, Set<OnFileChangeListener>> mDirPathToListenersMap;
	private final ConcurrentMap<OnFileChangeListener, Set<PathMatcher>> mListenerToFilePatternsMap;

	public SimpleDirectoryWatchService() throws IOException {
		mWatchService = FileSystems.getDefault().newWatchService();
		mIsRunning = new AtomicBoolean(false);
		mWatchKeyToDirPathMap = newConcurrentMap();
		mDirPathToListenersMap = newConcurrentMap();
		mListenerToFilePatternsMap = newConcurrentMap();
	}

	@SuppressWarnings("unchecked")
	private static <T> WatchEvent<T> cast(WatchEvent<?> event) {
		return (WatchEvent<T>) event;
	}

	private static <K, V> ConcurrentMap<K, V> newConcurrentMap() {
		return new ConcurrentHashMap<>();
	}

	private static <T> Set<T> newConcurrentSet() {
		return Collections.newSetFromMap(newConcurrentMap());
	}

	public static PathMatcher matcherForGlobExpression(String globPattern) {
		return FileSystems.getDefault().getPathMatcher("glob:" + globPattern);
	}

	public static boolean matches(Path input, PathMatcher pattern) {
		return pattern.matches(input);
	}

	public static boolean matchesAny(Path input, Set<PathMatcher> patterns) {
		for (PathMatcher pattern : patterns) {
			if (matches(input, pattern)) {
				return true;
			}
		}

		return false;
	}

	private Path getDirPath(WatchKey key) {
		return mWatchKeyToDirPathMap.get(key);
	}

	private Set<OnFileChangeListener> getListeners(Path dir) {
		return mDirPathToListenersMap.get(dir);
	}

	private Set<PathMatcher> getPatterns(OnFileChangeListener listener) {
		return mListenerToFilePatternsMap.get(listener);
	}

	private Set<OnFileChangeListener> matchedListeners(Path dir, Path file) {
		return getListeners(dir).stream()
				.filter(listener -> matchesAny(file, getPatterns(listener)))
				.collect(Collectors.toSet());
	}

	private void notifyListeners(WatchKey key) {
		for (WatchEvent<?> event : key.pollEvents()) {
			@SuppressWarnings("rawtypes")
			WatchEvent.Kind eventKind = event.kind();
			if (eventKind.equals(OVERFLOW)) {
				return;
			}
			WatchEvent<Path> pathEvent = cast(event);
			Path file = pathEvent.context();

			if (eventKind.equals(ENTRY_CREATE)) {
				matchedListeners(getDirPath(key), file).forEach(
						listener -> listener.onFileCreate(file.toString()));
			} else if (eventKind.equals(ENTRY_MODIFY)) {
				matchedListeners(getDirPath(key), file).forEach(
						listener -> listener.onFileModify(file.toString()));
			} else if (eventKind.equals(ENTRY_DELETE)) {
				matchedListeners(getDirPath(key), file).forEach(
						listener -> listener.onFileDelete(file.toString()));
			}
		}
	}

	@Override
	public void register(OnFileChangeListener listener, String dirPath,
			String... globPatterns) throws IOException {
		Path dir = Paths.get(dirPath);

		if (!Files.isDirectory(dir)) {
			throw new IllegalArgumentException(dirPath + " is not a directory.");
		}

		if (!mDirPathToListenersMap.containsKey(dir)) {
			// May throw
			WatchKey key = dir.register(mWatchService, ENTRY_CREATE,
					ENTRY_MODIFY, ENTRY_DELETE);

			mWatchKeyToDirPathMap.put(key, dir);
			mDirPathToListenersMap.put(dir, newConcurrentSet());
		}

		getListeners(dir).add(listener);

		Set<PathMatcher> patterns = newConcurrentSet();

		for (String globPattern : globPatterns) {
			patterns.add(matcherForGlobExpression(globPattern));
		}

		if (patterns.isEmpty()) {
			patterns.add(matcherForGlobExpression("*")); 
		}

		mListenerToFilePatternsMap.put(listener, patterns);

		LOGGER.info("Watching files matching " + Arrays.toString(globPatterns)
				+ " under " + dirPath + " for changes.");
	}

	/**
	 * Start this <code>SimpleDirectoryWatchService</code> instance by spawning
	 * a new thread.
	 * 
	 * @see #stop()
	 */
	public void start() {
		if (mIsRunning.compareAndSet(false, true)) {
			Thread runnerThread = new Thread(this,
					DirectoryWatchService.class.getSimpleName());
			runnerThread.start();
		}
	}

	/**
	 * Stop this <code>SimpleDirectoryWatchService</code> thread. The killing
	 * happens lazily, giving the running thread an opportunity to finish the
	 * work at hand.
	 * 
	 * @see #start()
	 */
	public void stop() {
		// Kill thread lazily
		mIsRunning.set(false);
	}

	/**
	 * {@inheritDoc}
	 */
	public void run() {
		LOGGER.info("Starting file watcher service.");

		while (mIsRunning.get()) {
			WatchKey key;
			try {
				key = mWatchService.take();
			} catch (InterruptedException e) {
				LOGGER.info(DirectoryWatchService.class.getSimpleName()
						+ " service interrupted.");
				break;
			}

			if (null == getDirPath(key)) {
				LOGGER.error("Watch key not recognized.");
				continue;
			}

			notifyListeners(key);

			boolean valid = key.reset();
			if (!valid) {
				mWatchKeyToDirPathMap.remove(key);
				if (mWatchKeyToDirPathMap.isEmpty()) {
					break;
				}
			}
		}

		mIsRunning.set(false);
		LOGGER.info("Stopping file watcher service.");
	}
}