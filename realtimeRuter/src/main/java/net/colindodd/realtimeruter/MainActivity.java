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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.greysonparrelli.permiso.Permiso;

import net.colindodd.realtimeruter.library.DownloadedDataListener;
import net.colindodd.realtimeruter.library.RealtimeRuterLibrary;
import net.colindodd.realtimeruter.library.model.RuterEvent;
import net.colindodd.realtimeruter.ui.AboutDialog;
import net.colindodd.realtimeruter.ui.LinesOverlay;
import net.colindodd.realtimeruter.ui.LoadingDialog;
import net.colindodd.realtimeruter.ui.MapMarkers;
import net.colindodd.realtimeruter.ui.PopupAdapter;
import net.colindodd.realtimeruter.util.CurrentUserLocation;

import java.util.ArrayList;
import java.util.Hashtable;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    private RealtimeRuterLibrary ruterLib;
    private GoogleMap gMap;
    private LoadingDialog loadingDialog;
    private Context context = this;
    private DownloadedDataListener downloadedDataListener;
    private CurrentUserLocation userLocation;
    private MapMarkers mapMarkers;
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

        if (!runForeverThread.isAlive()) {
            runForeverThread.start();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        loadData();
    }

    @Override
    protected void onPause() {
        super.onPause();
        ruterLib.stop();
        runForeverThread.interrupt();
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
                } else {
                    lookAtOslo();
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
        this.mapMarkers = new MapMarkers(this);
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

    private void showTimeOutDialog() {
        loadingDialog.hide();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    new AlertDialog.Builder(context)
                            .setMessage(getResources().getString(R.string.timeout_error))
                            .setNeutralButton(getResources().getString(R.string.close), null)
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

    private final Thread runForeverThread = new Thread(new Runnable() {
        @Override
        public void run() {
            while(true) {
                showAllEvents();
                try {
                    Thread.sleep(1000);
                } catch (final InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    });

    private void showAllEvents() {
        final ArrayList<RuterEvent> events = ruterLib.getAllEvents();
        final Hashtable<String, RuterEvent> activeEvents = new Hashtable<>();
        if (events != null) {
            for (final RuterEvent event : events) {
                if (event != null && event.isValidForMap()) {
                    activeEvents.put(event.getVehicleRef(), event);
                }
            }
            mapMarkers.updateMap(this.gMap, activeEvents, this.currentFilteredLineSelection);
        }
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
        return true;
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        this.gMap = googleMap;
        if (gMap == null) {
            handleDeviceNotSupported();
            return;
        }

        gMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        gMap.setPadding(0, 150, 0 , 0);

        LinesOverlay.renderLines(getApplicationContext(), gMap);
        formatSnippets();
        lookAtOslo();
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

    private void lookAtOslo() {
        if (this.gMap == null) {
            return;
        }

        final LatLng Oslo = new LatLng(59.912095, 10.752182);
        final CameraUpdate center = CameraUpdateFactory.newLatLngZoom(Oslo, 10);

        this.gMap.moveCamera(center);
    }
}