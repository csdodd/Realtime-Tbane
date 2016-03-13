package net.colindodd.realtimeruter;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.model.Marker;

class PopupAdapter implements InfoWindowAdapter {
    LayoutInflater inflater = null;

    PopupAdapter(LayoutInflater inflater) {
        this.inflater = inflater;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return (null);
    }

    @Override
    public View getInfoContents(Marker marker) {
        View popup = inflater.inflate(R.layout.popup, null);

        ImageView icon = (ImageView) popup.findViewById(R.id.icon);
        icon.setImageResource(getDrawable(marker.getTitle().subSequence(0, 1)));
        icon.setContentDescription(getContentDescription(marker.getTitle().subSequence(0, 1)));

        TextView tv = (TextView) popup.findViewById(R.id.title);

        tv.setText(marker.getTitle().substring(1));
        tv = (TextView) popup.findViewById(R.id.snippet);
        tv.setText(marker.getSnippet());

        LinesOverlay.highlightLine(Integer.valueOf(marker.getTitle().subSequence(0, 1).toString()));

        return (popup);
    }


    private CharSequence getContentDescription(CharSequence lineNumber) {
        if (lineNumber.equals("1")) return inflater.getContext().getString(R.string.line1);
        if (lineNumber.equals("2")) return inflater.getContext().getString(R.string.line2);
        if (lineNumber.equals("3")) return inflater.getContext().getString(R.string.line3);
        if (lineNumber.equals("4")) return inflater.getContext().getString(R.string.line4);
        if (lineNumber.equals("5")) return inflater.getContext().getString(R.string.line5);
        return inflater.getContext().getString(R.string.line);
    }

    private int getDrawable(CharSequence lineNumber) {
        if (lineNumber.equals("1")) return R.drawable.logo_line_1;
        if (lineNumber.equals("2")) return R.drawable.logo_line_2;
        if (lineNumber.equals("3")) return R.drawable.logo_line_3;
        if (lineNumber.equals("4")) return R.drawable.logo_line_4;
        if (lineNumber.equals("5")) return R.drawable.logo_line_5;
        return R.drawable.ic_launcher;
    }
}