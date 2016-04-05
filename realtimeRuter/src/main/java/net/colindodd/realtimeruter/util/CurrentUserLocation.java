package net.colindodd.realtimeruter.util;

import android.Manifest;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;

import com.greysonparrelli.permiso.Permiso;

public class CurrentUserLocation {

    public interface OnLocationPermissionGrantedListener {
        void onLocationPermissionGranted();
    }

    private final Context context;
    private final OnLocationPermissionGrantedListener listener;

    public CurrentUserLocation(final Context context, final OnLocationPermissionGrantedListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void requestLocationPermission() {
        Permiso.getInstance().requestPermissions(new Permiso.IOnPermissionResult() {
            @Override
            public void onPermissionResult(final Permiso.ResultSet resultSet) {
                if (resultSet.areAllPermissionsGranted() && listener != null) {
                    listener.onLocationPermissionGranted();
                }
            }

            @Override
            public void onRationaleRequested(final Permiso.IOnRationaleProvided callback, final String... permissions) {
                callback.onRationaleProvided();
            }
        }, Manifest.permission.ACCESS_FINE_LOCATION);
    }

    public Location getCurrentLocation() {
        try {
            final Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_FINE);

            final LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            final String provider = lm.getBestProvider(criteria, true);

            final Location location = lm.getLastKnownLocation(provider);
            return isValid(location) ? location : null;
        } catch (final SecurityException ex) {
            // User blocked us from accessing location.
            return null;
        }
    }

    private boolean isValid(final Location location) {
        final double longitude = location.getLongitude();
        final double latitude = location.getLatitude();

        final double minLat = 59;
        final double maxLat = 60;
        final double minLon = 10;
        final double maxLon = 11;

        return minLat <= latitude && latitude <= maxLat
                && minLon <= longitude && longitude <= maxLon;
    }
}
