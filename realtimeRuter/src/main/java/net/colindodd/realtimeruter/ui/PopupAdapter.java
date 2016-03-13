package net.colindodd.realtimeruter.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.model.Marker;

import net.colindodd.realtimeruter.LinesOverlay;
import net.colindodd.realtimeruter.R;

public class PopupAdapter implements InfoWindowAdapter {
    private final LayoutInflater inflater;

    public PopupAdapter(final LayoutInflater inflater) {
        this.inflater = inflater;
    }

    @Override
    public View getInfoWindow(final Marker marker) {
        return (null);
    }

    @Override
    public View getInfoContents(final Marker marker) {
        final View popup = inflater.inflate(R.layout.popup, null);

        final ImageView icon = (ImageView) popup.findViewById(R.id.icon);
        icon.setImageResource(getDrawable(marker.getTitle().subSequence(0, 1)));
        icon.setContentDescription(getContentDescription(marker.getTitle().subSequence(0, 1)));

        final TextView titleTv = (TextView) popup.findViewById(R.id.title);
        final TextView snippetTextView = (TextView) popup.findViewById(R.id.snippet);

        titleTv.setText(marker.getTitle().substring(1));
        snippetTextView.setText(marker.getSnippet());

        LinesOverlay.highlightLine(Integer.valueOf(marker.getTitle().subSequence(0, 1).toString()));

        return (popup);
    }


    private CharSequence getContentDescription(final CharSequence lineNumber) {
        if (lineNumber.equals("1")) return inflater.getContext().getString(R.string.line1);
        if (lineNumber.equals("2")) return inflater.getContext().getString(R.string.line2);
        if (lineNumber.equals("3")) return inflater.getContext().getString(R.string.line3);
        if (lineNumber.equals("4")) return inflater.getContext().getString(R.string.line4);
        if (lineNumber.equals("5")) return inflater.getContext().getString(R.string.line5);
        return inflater.getContext().getString(R.string.line);
    }

    private int getDrawable(final CharSequence lineNumber) {
        if (lineNumber.equals("1")) return R.drawable.logo_line_1;
        if (lineNumber.equals("2")) return R.drawable.logo_line_2;
        if (lineNumber.equals("3")) return R.drawable.logo_line_3;
        if (lineNumber.equals("4")) return R.drawable.logo_line_4;
        if (lineNumber.equals("5")) return R.drawable.logo_line_5;
        return R.drawable.ic_launcher;
    }
}