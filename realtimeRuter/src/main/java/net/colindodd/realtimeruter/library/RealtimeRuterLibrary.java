package net.colindodd.realtimeruter.library;

import net.colindodd.realtimeruter.library.dataaccess.RuterStation;
import net.colindodd.realtimeruter.library.dataaccess.VehicleEvents;
import net.colindodd.realtimeruter.library.model.Lines;
import net.colindodd.realtimeruter.library.model.RuterEvent;
import net.colindodd.realtimeruter.library.web.DataDownloader;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.ArrayList;

public class RealtimeRuterLibrary {
    private final int MILLIS_BETWEEN_RELOAD = 180000;
    private Thread runForeverThread;
    private Thread loadDataThread;
    private ArrayList<RuterStation> allStations;
    private VehicleEvents allVehicles;
    private DataDownloader dd;

    public void loadLiveData(final DownloadedDataListener listener) {
        if (this.runForeverThread == null || !this.runForeverThread.isAlive()) {
            startRunForeverThread(listener);
        }
    }

    public void stop() {
        this.loadDataThread.interrupt();
        this.runForeverThread.interrupt();
    }

    private void startRunForeverThread(final DownloadedDataListener listener) {
        this.runForeverThread = new Thread(new Runnable() {
            public void run() {
                boolean keepRunning = true;
                while (keepRunning) {
                    if (loadDataThread == null || !loadDataThread.isAlive()) {
                        getLatestDataThread(listener);
                    }
                    try {
                        Thread.sleep(MILLIS_BETWEEN_RELOAD);
                    } catch (final InterruptedException e) {
                        e.printStackTrace();
                        keepRunning = false;
                    }
                }
            }
        });
        runForeverThread.start();
    }

    private void getLatestDataThread(final DownloadedDataListener listener) {
        getLatestData(listener);

        try {
            loadDataThread.join(MILLIS_BETWEEN_RELOAD);
        } catch (final InterruptedException e) {
            e.printStackTrace();
            System.out.println("Loading thread didn't join");
            listener.timedOut();
            return;
        }

        if (loadDataThread.isAlive()) {
            System.out.println("Loadingthread timed out");
            listener.timedOut();
        }
    }

    private void getLatestData(final DownloadedDataListener listener) {
        loadDataThread = new Thread(new Runnable() {
            public void run() {
                if (dd == null) {
                    dd = new DataDownloader(listener);
                }

                listener.downloadStarted();
                dd.run(Lines.AllStops);
                while (!dd.hasFinishedProcessing()) {
                    try {
                        Thread.sleep(100);
                    } catch (final Exception e) {
                        listener.errorOccurred();
                    }
                }
                allStations = dd.getDownloadedData();
                allVehicles = null;
                listener.downloadEnded();
            }
        });
        loadDataThread.start();
    }

    public ArrayList<RuterEvent> getAllEvents() {
        if (allStations == null) {
            return null;
        }

        if (allVehicles == null) {
            getAllInformation();
        }

        return allVehicles.getEvents();
    }

    private void getAllInformation() {
        allVehicles = new VehicleEvents();

        for (final RuterStation station : allStations) {
            if (station == null) {
                break;
            }
            allVehicles.putAll(station.getVehicles());
        }
    }

    public static DateTime getAppTime() {
        final DateTimeZone zone = DateTimeZone.forID("Europe/Oslo");
        return new DateTime(zone);
    }

    public boolean timedOut() {
        return dd != null && dd.timedOut();
    }
}
