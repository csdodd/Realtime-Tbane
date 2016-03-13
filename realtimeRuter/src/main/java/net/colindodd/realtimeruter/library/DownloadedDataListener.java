package net.colindodd.realtimeruter.library;

public interface DownloadedDataListener {
    void downloadStarted();
    void downloadEnded();
    void errorOccurred();
    void timedOut();
    void newStationLoaded(final String stationName);
}
