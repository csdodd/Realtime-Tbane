package net.colindodd.realtimeruter;

import android.app.AlertDialog;
import android.app.ProgressDialog;
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
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.BadTokenException;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.greysonparrelli.permiso.Permiso;

import net.colindodd.realtimeruter.library.DataDownloadListener;
import net.colindodd.realtimeruter.library.RealtimeRuterLibrary;
import net.colindodd.realtimeruter.library.model.Lines;
import net.colindodd.realtimeruter.library.model.RuterEvent;
import net.colindodd.realtimeruter.util.CurrentUserLocation;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

public class MainActivity extends AppCompatActivity {
    private RealtimeRuterLibrary ruterLib;
    private GoogleMap gMap;
    private ProgressDialog loadingBox;
    private Context context = this;
    private DataDownloadListener listener;
    private CurrentUserLocation userLocation;
    private int currentFilteredLineSelection = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        Permiso.getInstance().setActivity(this);

        init();
        if (savedInstanceState == null) {
            lookAtOslo();
        }

        loadData();
        mainLoop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Permiso.getInstance().setActivity(this);
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull final String[] permissions, @NonNull final int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Permiso.getInstance().onRequestPermissionResult(requestCode, permissions, grantResults);
    }

    private void init() {
        if (!initGoogleMap()) {
            handleDeviceNotSupported();
            return;
        }

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
        final Location currentLocation = userLocation.getCurrentLocation();
        if (currentLocation != null) {
            gMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude())));
        }

        final CameraUpdate zoom = CameraUpdateFactory.zoomTo(12);
        gMap.animateCamera(zoom, 2000, null);
    }

    private void initRuterLib() {
        ruterLib = new RealtimeRuterLibrary();
        listener = new DataDownloadListener();
    }

    private boolean initGoogleMap() {
        gMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
        if (gMap == null) return false;
        gMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        LinesOverlay.renderLines(getApplicationContext(), gMap);
        return true;
    }

    private void initUi() {
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final ImageView imgDataLoadError = (ImageView) findViewById(R.id.imgDataLoadError);
        imgDataLoadError.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Toast.makeText(MainActivity.this,
                        getResources().getString(R.string.errorLoadingData),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void initUserLocation() {
        this.userLocation = new CurrentUserLocation(this, locationPermissionGrantedListener);
    }

    private void lookAtOslo() {
        final LatLng Oslo = new LatLng(59.912095, 10.752182);
        final CameraUpdate center = CameraUpdateFactory.newLatLng(Oslo);
        final CameraUpdate zoom = CameraUpdateFactory.zoomTo(10);

        gMap.moveCamera(center);
        gMap.animateCamera(zoom);
    }

    private void loadData() {
        showLoadingDialog();
        new Thread(new Runnable() {
            public void run() {
                ruterLib.loadLiveData(listener);
            }
        }).start();
    }

    private void mainLoop() {
        Thread thread = new Thread(new Runnable() {
            public void run() {
                boolean keepRunning = true;
                while (keepRunning) {
                    try {
                        if (!showAllEvents()) {
                            updateLoadingDialog();
                            if (ruterLib.timedOut()) {
                                keepRunning = false;
                                showTimeOutDialog();
                            }
                            Thread.sleep(100);
                        } else {
                            Thread.sleep(1000);
                        }
                        updateLoadingUi();
                    } catch (final InterruptedException e) {
                        e.printStackTrace();
                        keepRunning = false;
                    }
                }
            }
        });
        thread.start();
    }

    private void showLoadingDialog() {
        loadingBox = new ProgressDialog(this);
        loadingBox.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        loadingBox.setMessage(getResources().getString(R.string.loading_ruter));
        loadingBox.setTitle("");
        loadingBox.setIndeterminate(false);
        loadingBox.setCancelable(false);
        loadingBox.setMax(Lines.AllStops.length);
        loadingBox.show();
    }

    private void updateLoadingDialog() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (loadingBox.isShowing()) {
                    loadingBox.setProgress(listener.getLoadedCounter());
                    loadingBox.setMessage(getResources().getString(R.string.loading_ruter) + "\n" + listener.getLastLoadedStation());
                }
            }
        });
    }

    private void hideLoadingDialog() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (loadingBox.isShowing()) {
                    loadingBox.dismiss();
                    userLocation.requestLocationPermission();
                }
            }
        });
    }

    private void updateLoadingUi() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ProgressBar loadingSpinner = (ProgressBar) findViewById(R.id.spinLoader);
                ImageView imgDataLoadError = (ImageView) findViewById(R.id.imgDataLoadError);
                if (listener.isDownloading()) loadingSpinner.setVisibility(ProgressBar.VISIBLE);
                else {
                    if (loadingSpinner.isShown()) clearMap();
                    loadingSpinner.setVisibility(ProgressBar.GONE);
                }

                if (listener.isLoadError()) {
                    imgDataLoadError.setVisibility(ImageView.VISIBLE);
                    showErrorLoadingDataDialog();
                } else imgDataLoadError.setVisibility(ImageView.GONE);
            }

            private void clearMap() {
                gMap.clear();
                currentMarkers.clear();
                LinesOverlay.renderLines(getApplicationContext(), gMap);
            }
        });
    }

    private boolean showAllEvents() {
        final ArrayList<RuterEvent> events = ruterLib.getAllEvents();
        final Hashtable<String, RuterEvent> activeEvents = new Hashtable<>();
        if (events != null) {
            hideLoadingDialog();
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
        hideLoadingDialog();
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
}