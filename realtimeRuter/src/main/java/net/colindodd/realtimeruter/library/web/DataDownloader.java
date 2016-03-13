package net.colindodd.realtimeruter.library.web;

import net.colindodd.realtimeruter.library.DownloadedDataListener;
import net.colindodd.realtimeruter.library.dataaccess.RuterStation;
import net.colindodd.realtimeruter.library.model.Station;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public final class DataDownloader {

    private final DownloadedDataListener listener;

    private ArrayList<RuterStation> allStations;
    private boolean hasFinishedProcessing;
    private boolean wasLoadedCorrectly;

    public DataDownloader(final DownloadedDataListener listener) {
        this.listener = listener;
    }

    public void run(final Station[] stations) {
        final ArrayList<Station> stationArrayList = new ArrayList<>(Arrays.asList(stations));
        run(stationArrayList);
    }

    public void run(final ArrayList<Station> stations) {
        hasFinishedProcessing = false;
        wasLoadedCorrectly = true;
        allStations = new ArrayList<>(stations.size());

        final ExecutorService pool = Executors.newFixedThreadPool(10);
        for (final Station station : stations) {
            pool.submit(new DownloadStationTask(station));
        }
        pool.shutdown();

        try {
            pool.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        } catch (final InterruptedException e) {
            e.printStackTrace();
        }

        hasFinishedProcessing = true;
    }

    private class DownloadStationTask implements Runnable {
        private final Station stationToDownload;

        public DownloadStationTask(final Station stationToDownload) {
            this.stationToDownload = stationToDownload;
        }

        @Override
        public void run() {
            final RuterStation station = new RuterStation(stationToDownload);
            if (!station.loadedCorrectly()) {
                handleErrorLoadingStation();
            }

            allStations.add(station);
            handleStationLoaded();
        }

        private void handleErrorLoadingStation() {
            wasLoadedCorrectly = false;
            if (listener != null) {
                listener.errorOccurred();
            }
        }

        private void handleStationLoaded() {
            if (listener != null) {
                listener.newStationLoaded(stationToDownload.name());
            }
        }
    }

    public ArrayList<RuterStation> getDownloadedData() {
        return hasFinishedProcessing
                ? allStations
                : null;
    }

    public boolean timedOut() {
        return hasFinishedProcessing && !wasLoadedCorrectly;
    }

    public boolean hasFinishedProcessing() {
        return hasFinishedProcessing;
    }
}
