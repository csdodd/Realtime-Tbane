package net.colindodd.realtimeruter.ui;

import android.app.Activity;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import net.colindodd.realtimeruter.R;
import net.colindodd.realtimeruter.library.model.RuterEvent;

import java.util.Enumeration;
import java.util.Hashtable;

public class MapMarkers {

    private final Activity parentActivity;
    private final Hashtable<String, Marker> currentMarkers;

    private int currentFilteredLineSelection;

    public MapMarkers(final Activity parentActivity) {
        this.parentActivity = parentActivity;
        this.currentMarkers = new Hashtable<>();
    }

    public void updateMap(final GoogleMap gMap, final Hashtable<String, RuterEvent> activeEvents, final int currentFilteredLineSelection) {
        this.currentFilteredLineSelection = currentFilteredLineSelection;

        final Enumeration<String> vehicleRefs = currentMarkers.keys();
        while (vehicleRefs.hasMoreElements()) {
            final String vehicleRef = vehicleRefs.nextElement();
            if (activeEvents.containsKey(vehicleRef)) {
                updateMarker(gMap, activeEvents.get(vehicleRef));
                activeEvents.remove(vehicleRef);
            } else {
                removeMarker(currentMarkers.get(vehicleRef));
                currentMarkers.remove(vehicleRef);
            }
        }

        final Enumeration<String> events = activeEvents.keys();
        while (events.hasMoreElements()) {
            final RuterEvent event = activeEvents.get(events.nextElement());
            addMarker(gMap, event);
        }
    }

    private void updateMarker(final GoogleMap gMap, final RuterEvent event) {
        handleMarker(gMap, event, false);
    }

    private void addMarker(final GoogleMap gMap, final RuterEvent event) {
        handleMarker(gMap, event, true);
    }

    private void handleMarker(final GoogleMap gMap, final RuterEvent event, final boolean createMarker) {
        parentActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (createMarker) {
                    Marker newMarker = gMap.addMarker(new MarkerOptions()
                            .position(new LatLng(event.getGeographicalLocation().getLatitude(), event.getGeographicalLocation().getLongitude()))
                            .title(event.getPublishedLineName() + event.getDestinationDisplay())
                            .snippet(generateSnippet(event))
                            .icon(getBitmapDescriptor(event.getPublishedLineName()))
                            .anchor(0.5f, 0.5f));
                    newMarker.setVisible(!filterEvent(event.getPublishedLineName()));
                    currentMarkers.put(event.getVehicleRef(), newMarker);
                } else {
                    Marker currentMarker = currentMarkers.get(event.getVehicleRef());
                    if (currentMarker == null) return;
                    currentMarker.setPosition(new LatLng(event.getGeographicalLocation().getLatitude(), event.getGeographicalLocation().getLongitude()));
                    currentMarker.setTitle(event.getPublishedLineName() + event.getDestinationDisplay());
                    currentMarker.setSnippet(generateSnippet(event));
                    currentMarker.setVisible(!filterEvent(event.getPublishedLineName()));
                    if (currentMarker.isInfoWindowShown()) {
                        currentMarker.hideInfoWindow();
                        currentMarker.showInfoWindow();
                    }
                }
            }

            private BitmapDescriptor getBitmapDescriptor(final int lineName) {
                switch (lineName) {
                    case 1:
                        return BitmapDescriptorFactory.fromResource(R.drawable.logo_line_1_small);
                    case 2:
                        return BitmapDescriptorFactory.fromResource(R.drawable.logo_line_2_small);
                    case 3:
                        return BitmapDescriptorFactory.fromResource(R.drawable.logo_line_3_small);
                    case 4:
                        return BitmapDescriptorFactory.fromResource(R.drawable.logo_line_4_small);
                    case 5:
                        return BitmapDescriptorFactory.fromResource(R.drawable.logo_line_5_small);
                }
                return BitmapDescriptorFactory.fromResource(R.drawable.ic_launcher);
            }

            private String generateSnippet(final RuterEvent event) {
                StringBuilder retVal = new StringBuilder();
                if (event.isVehicleAtStop()) {
                    retVal.append(parentActivity.getResources().getString(R.string.at) + " ");
                    retVal.append(event.previousStationName() + "\n");
                    retVal.append(parentActivity.getResources().getString(R.string.next_station) + " " + event.getStationName());
                } else {
                    retVal.append(parentActivity.getResources().getString(R.string.next_station) + " " + event.getStationName() + "\n");
                    retVal.append(parentActivity.getResources().getString(R.string.arriving_in) + " ");
                    retVal.append(event.getDurationUntilExpectedArrivalAsString());
                    retVal.append("\n" + event.getDelayInfo(parentActivity.getResources().getString(R.string.late), parentActivity.getResources().getString(R.string.early)));
                }

                return retVal.toString();
            }
        });
    }

    private void removeMarker(final Marker marker) {
        parentActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (marker != null) {
                    marker.setVisible(false);
                    marker.remove();
                }
            }
        });
    }

    private boolean filterEvent(final int lineNumber) {
        if (currentFilteredLineSelection == 0) {
            return false;
        }

        return lineNumber != currentFilteredLineSelection;
    }
}
