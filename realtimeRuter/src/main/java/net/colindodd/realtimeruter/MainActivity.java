package net.colindodd.realtimeruter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.WindowManager.BadTokenException;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.greysonparrelli.permiso.Permiso;

import net.colindodd.realtimeruter.library.DownloadedDataListener;
import net.colindodd.realtimeruter.library.RealtimeRuterLibrary;
import net.colindodd.realtimeruter.library.model.RuterEvent;
import net.colindodd.realtimeruter.ui.AboutDialog;
import net.colindodd.realtimeruter.ui.LoadingDialog;
import net.colindodd.realtimeruter.ui.PopupAdapter;
import net.colindodd.realtimeruter.util.CurrentUserLocation;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    private RealtimeRuterLibrary ruterLib;
    private GoogleMap gMap;
    private LoadingDialog loadingDialog;
    private Context context = this;
    private DownloadedDataListener downloadedDataListener;
    private CurrentUserLocation userLocation;
    private int currentFilteredLineSelection = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        Permiso.getInstance().setActivity(this);

        init();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Permiso.getInstance().setActivity(this);
        loadData();
    }

    @Override
    protected void onPause() {
        super.onPause();
        ruterLib.stop();
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull final String[] permissions, @NonNull final int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Permiso.getInstance().onRequestPermissionResult(requestCode, permissions, grantResults);
    }

    private void init() {
        initGoogleMap();
        initRuterLib();
        initUi();
        initUserLocation();
    }


    private final CurrentUserLocation.OnLocationPermissionGrantedListener locationPermissionGrantedListener = new CurrentUserLocation.OnLocationPermissionGrantedListener() {
        @Override
        public void onLocationPermissionGranted() {
            zoomIntoMap();
        }
    };

    private void zoomIntoMap() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final Location currentLocation = userLocation.getCurrentLocation();
                if (currentLocation != null) {
                    gMap.setMyLocationEnabled(true);
                    gMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude())));
                }

                final CameraUpdate zoom = CameraUpdateFactory.zoomTo(12);
                gMap.animateCamera(zoom, 2000, null);
            }
        });
    }

    private void initRuterLib() {
        this.ruterLib = new RealtimeRuterLibrary();
        this.downloadedDataListener = new DownloadedDataListener() {
            @Override
            public void downloadStarted() {
                loadingDialog.show();
            }

            @Override
            public void downloadEnded() {
                loadingDialog.hide();
                userLocation.requestLocationPermission();
                showAllEvents();
            }

            @Override
            public void errorOccurred() {
                showErrorLoadingDataDialog();
            }

            @Override
            public void timedOut() {
                showTimeOutDialog();
            }

            @Override
            public void newStationLoaded(final String stationName) {
                loadingDialog.update(stationName);
            }
        };
    }

    private void initGoogleMap() {
        final SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void initUi() {
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        this.loadingDialog = new LoadingDialog(this);
    }

    private void initUserLocation() {
        this.userLocation = new CurrentUserLocation(this, locationPermissionGrantedListener);
    }

    private void loadData() {
        ruterLib.loadLiveData(this.downloadedDataListener);
    }

    private boolean showAllEvents() {
        final ArrayList<RuterEvent> events = ruterLib.getAllEvents();
        final Hashtable<String, RuterEvent> activeEvents = new Hashtable<>();
        if (events != null) {
            for (final RuterEvent event : events) {
                if (event != null && event.isValidForMap()) {
                    activeEvents.put(event.getVehicleRef(), event);
                }
            }
            handleMarkers(activeEvents);
            formatSnippets();
            return true;
        }
        return false;
    }

    private void showTimeOutDialog() {
        loadingDialog.hide();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    new AlertDialog.Builder(context)
                            .setMessage(getResources().getString(R.string.timeout_error))
                            .setNeutralButton(getResources().getString(R.string.close), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    MainActivity.this.finish();
                                }
                            })
                            .show();
                } catch (BadTokenException e) {
                    //Do nothing -- the app is closed.
                }
            }
        });
    }

    private void handleDeviceNotSupported() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    new AlertDialog.Builder(context)
                            .setMessage(getResources().getString(R.string.device_error))
                            .setNeutralButton(getResources().getString(R.string.close), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    MainActivity.this.finish();
                                }
                            })
                            .show();
                } catch (final BadTokenException e) {
                    //Do nothing -- the app is closed.
                }
            }
        });
    }

    boolean shownErrorLoadingDataDialog = false;

    private void showErrorLoadingDataDialog() {
        if (shownErrorLoadingDataDialog) return;
        shownErrorLoadingDataDialog = true;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    new AlertDialog.Builder(context)
                            .setMessage(getResources().getString(R.string.errorLoadingData))
                            .setPositiveButton(getResources().getString(R.string.close), null)
                            .show();
                } catch (final BadTokenException e) {
                    //Do nothing -- the app is closed.
                }
            }
        });
    }

    private Hashtable<String, Marker> currentMarkers = new Hashtable<>();

    private void handleMarkers(final Hashtable<String, RuterEvent> activeEvents) {
        Enumeration<String> e = currentMarkers.keys();
        while (e.hasMoreElements()) {
            String vehicleRef = e.nextElement();
            if (activeEvents.containsKey(vehicleRef)) {
                addMarker(activeEvents.get(vehicleRef), false);
                activeEvents.remove(vehicleRef);
            } else {
                removeMarker(currentMarkers.get(vehicleRef));
                currentMarkers.remove(vehicleRef);
            }
        }

        e = activeEvents.keys();
        while (e.hasMoreElements()) {
            RuterEvent event = activeEvents.get(e.nextElement());
            addMarker(event, true);
        }
    }

    private void addMarker(final RuterEvent event, final boolean createMarker) {
        runOnUiThread(new Runnable() {
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

            private String generateSnippet(RuterEvent event) {
                StringBuilder retVal = new StringBuilder();
                if (event.isVehicleAtStop()) {
                    retVal.append(getResources().getString(R.string.at) + " ");
                    retVal.append(event.previousStationName() + "\n");
                    retVal.append(getResources().getString(R.string.next_station) + " " + event.getStationName());
                } else {
                    retVal.append(getResources().getString(R.string.next_station) + " " + event.getStationName() + "\n");
                    retVal.append(getResources().getString(R.string.arriving_in) + " ");
                    retVal.append(event.getDurationUntilExpectedArrivalAsString());
                    retVal.append("\n" + event.getDelayInfo(getResources().getString(R.string.late), getResources().getString(R.string.early)));
                }

                return retVal.toString();
            }
        });
    }

    private void removeMarker(final Marker marker) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (marker != null) {
                    marker.setVisible(false);
                    marker.remove();
                }
            }
        });
    }

    private void formatSnippets() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                gMap.setInfoWindowAdapter(new PopupAdapter(getLayoutInflater()));
                gMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {

                    public void onInfoWindowClick(Marker marker) {
                        marker.hideInfoWindow();
                    }
                });
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        final MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_itemlist, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.about:
                new AboutDialog().show(getSupportFragmentManager(), null);
                return true;
            case R.id.menuLineAll:
                currentFilteredLineSelection = 0;
                break;
            case R.id.menuLine1:
                currentFilteredLineSelection = 1;
                break;
            case R.id.menuLine2:
                currentFilteredLineSelection = 2;
                break;
            case R.id.menuLine3:
                currentFilteredLineSelection = 3;
                break;
            case R.id.menuLine4:
                currentFilteredLineSelection = 4;
                break;
            case R.id.menuLine5:
                currentFilteredLineSelection = 5;
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        LinesOverlay.highlightLine(currentFilteredLineSelection);
        showAllEvents();
        return true;
    }

    private boolean filterEvent(int lineNumber) {
        switch (currentFilteredLineSelection) {
            case 0:
                return false;
            case 4:
                return lineNumber != 4 && lineNumber != 6;
            default:
                return lineNumber != currentFilteredLineSelection;
        }
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        this.gMap = googleMap;
        if (gMap == null) {
            handleDeviceNotSupported();
            return;
        }

        gMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        LinesOverlay.renderLines(getApplicationContext(), gMap);
        lookAtOslo();
    }

    private void lookAtOslo() {
        final LatLng Oslo = new LatLng(59.912095, 10.752182);
        final CameraUpdate center = CameraUpdateFactory.newLatLng(Oslo);
        final CameraUpdate zoom = CameraUpdateFactory.zoomTo(10);

        gMap.moveCamera(center);
        gMap.animateCamera(zoom);
    }
}