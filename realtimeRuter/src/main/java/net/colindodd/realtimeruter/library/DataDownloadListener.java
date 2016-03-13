package net.colindodd.realtimeruter.library;

public class DataDownloadListener {
	private String lastLoadedStation;
	private int loadedCounter;
	private boolean isDownloading;
	private boolean loadError;
	
	public void startDownloading() {
		lastLoadedStation = "";
		loadedCounter = 0;
		isDownloading = true;
		loadError = false;
	}
	
	public void endDownloading() {
		isDownloading = false;
	}
	
	public void stationLoaded(String stationName) {
		lastLoadedStation = stationName;
		++loadedCounter;
	}
	
	public void errorInLoading() {
		loadError = true;
	}
	
	public String getLastLoadedStation() {
		return lastLoadedStation;
	}
	
	public int getLoadedCounter() {
		return loadedCounter;
	}
	
	public boolean isDownloading() {
		return isDownloading;
	}
	
	public boolean isLoadError() {
		return loadError;
	}
}
