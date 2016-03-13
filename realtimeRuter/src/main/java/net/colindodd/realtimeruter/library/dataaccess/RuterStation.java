package net.colindodd.realtimeruter.library.dataaccess;

import com.google.gson.Gson;

import net.colindodd.realtimeruter.library.model.RuterEvent;
import net.colindodd.realtimeruter.library.model.Station;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class RuterStation {
    private final static String BaseStationUrl = "http://reis.trafikanten.no/reisrest/realtime/getrealtimedata/";
    private final static String StagingStationUrl = "http://api-test.trafikanten.no/RealTime/GetRealTimeData/";
    private final Station station;
    private final boolean useStagingUrl;

    private RuterEvent[] events;
    private VehicleEvents vehicles;
    private boolean loadedCorrectly;

    public RuterStation(final Station station) {
        this(station, false);
    }

    public RuterStation(final Station station, final boolean useStagingUrl) {
        this.station = station;
        this.useStagingUrl = useStagingUrl;
        loadEvents();
    }

    private void loadEvents() {
        events = new Gson().fromJson(readFromUrl(), RuterEvent[].class);
        loadedCorrectly = events != null;

        if (!loadedCorrectly) {
            return;
        }

        vehicles = new VehicleEvents();
        processEvents();
    }

    private void processEvents() {
        for (final RuterEvent event : events) {
            if (!event.isValid()) {
                continue;
            }
            brandEvent(event);
            addVehicle(event);
        }
    }

    private void brandEvent(final RuterEvent event) {
        event.setStation(station);
    }

    private void addVehicle(final RuterEvent event) {
        vehicles.put(event.getVehicleRef() + " " + event.getDirectionName() + " " + event.getPublishedLineName() + " " + event.getDestinationDisplay(), event);
    }

    public VehicleEvents getVehicles() {
        return vehicles;
    }

    private String readFromUrl() {
        final URLConnection con = generateUrlConnection();
        if (con == null) {
            return "";
        }

        return readResponseFromUrlConnection(con);
    }

    private String readResponseFromUrlConnection(final URLConnection con) {
        try {
            final BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            final StringBuilder response = new StringBuilder();
            String inputLine;

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }

            in.close();

            return response.toString();
        } catch (final IOException e) {
            System.out.println(station.name() + " borked");
            e.printStackTrace();
            return "";
        }

    }

    private URLConnection generateUrlConnection() {
        try {
            final URL url = generateUrl();
            final URLConnection con = url.openConnection();
            con.setRequestProperty("User-Agent", "Realtime Ruter Android App");
            con.setConnectTimeout(10000);
            con.setReadTimeout(20000);
            return con;
        } catch (final IOException e) {
            System.out.println(station.name() + " borked");
            e.printStackTrace();
            return null;
        }
    }

    private URL generateUrl() throws MalformedURLException {
        if (this.useStagingUrl) {
            return new URL(StagingStationUrl + station.number());
        }
        return new URL(BaseStationUrl + station.number());
    }

    public boolean loadedCorrectly() {
        return loadedCorrectly;
    }
}
