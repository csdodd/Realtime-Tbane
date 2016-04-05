package net.colindodd.realtimeruter.ui;

import android.content.Context;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import net.colindodd.realtimeruter.R;
import net.colindodd.realtimeruter.library.model.GeographicalLocation;
import net.colindodd.realtimeruter.library.model.Lines;
import net.colindodd.realtimeruter.library.model.Location;
import net.colindodd.realtimeruter.library.model.Station;

public class LinesOverlay {

    private static Location location;
    private static Context context;
    private static Polyline lines[];

    private static void init(final Context context) {
        LinesOverlay.context = context;
        location = new Location();
        lines = new Polyline[6];
    }

    public static void renderLines(final Context context, final GoogleMap gMap) {
        if (LinesOverlay.context == null) init(context);
        lines[1] = drawLine(gMap, Lines.Line1, getInactiveColour(1));
        lines[2] = drawLine(gMap, Lines.Line2, getInactiveColour(2));
        lines[3] = drawLine(gMap, Lines.Line3, getInactiveColour(3));
        lines[4] = drawLine(gMap, Lines.Line4, getInactiveColour(4));
        lines[5] = drawLine(gMap, Lines.Line5, getInactiveColour(5));
    }

    private static int getInactiveColour(final int lineNumber) {
        return getColour(lineNumber, false);
    }

    private static int getActiveColour(final int lineNumber) {
        return getColour(lineNumber, true);
    }

    private static int getColour(final int lineNumber, final boolean active) {
        switch (lineNumber) {
            case 1:
                return active ? context.getResources().getColor(R.color.line1_overlay_active) : context.getResources().getColor(R.color.line1_overlay);
            case 2:
                return active ? context.getResources().getColor(R.color.line2_overlay_active) : context.getResources().getColor(R.color.line2_overlay);
            case 3:
                return active ? context.getResources().getColor(R.color.line3_overlay_active) : context.getResources().getColor(R.color.line3_overlay);
            case 4:
                return active ? context.getResources().getColor(R.color.line4_overlay_active) : context.getResources().getColor(R.color.line4_overlay);
            case 5:
            default:
                return active ? context.getResources().getColor(R.color.line5_overlay_active) : context.getResources().getColor(R.color.line5_overlay);
        }
    }

    private static Polyline drawLine(final GoogleMap gMap, final Station[] stations, final int colour) {
        PolylineOptions rectOptions = new PolylineOptions()
                .width(15)
                .color(colour);

        for (final Station station : stations) {
            rectOptions.add(getLatLng(station));
        }

        return gMap.addPolyline(rectOptions);
    }

    private static LatLng getLatLng(final Station station) {
        GeographicalLocation loc = location.getStationLocation(station);
        double lat = loc.getLatitude();
        double lng = loc.getLongitude();
        return new LatLng(lat, lng);
    }

    public static void highlightLine(final int lineNumber) {
        for (int i = 1; i < lines.length; i++) {
            if (i == lineNumber) lines[i].setColor(getActiveColour(i));
            else lines[i].setColor(getInactiveColour(i));
        }
    }
}
