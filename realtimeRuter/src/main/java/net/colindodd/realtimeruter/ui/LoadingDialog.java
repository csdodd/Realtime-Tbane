package net.colindodd.realtimeruter.ui;

import android.app.Activity;
import android.app.ProgressDialog;

import net.colindodd.realtimeruter.R;
import net.colindodd.realtimeruter.library.model.Lines;

public class LoadingDialog {

    private final Activity parentActivity;
    private final ProgressDialog loadingBox;

    private int numberOfStationsLoaded = 0;

    public LoadingDialog(final Activity parentActivity) {
        this.parentActivity = parentActivity;
        this.loadingBox = new ProgressDialog(parentActivity);

        init();
    }

    private void init() {
        this.loadingBox.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        this.loadingBox.setMessage(this.parentActivity.getResources().getString(R.string.loading_ruter));
        this.loadingBox.setTitle("");
        this.loadingBox.setIndeterminate(false);
        this.loadingBox.setCancelable(false);
        this.loadingBox.setMax(Lines.AllStops.length);
    }

    public void show() {
        numberOfStationsLoaded = 0;
        this.parentActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                loadingBox.show();
            }
        });
    }

    public void hide() {
        this.parentActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (loadingBox.isShowing()) {
                    loadingBox.dismiss();
                }
            }
        });
    }

    public void update(final String stationName) {
        this.parentActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (loadingBox.isShowing()) {
                    loadingBox.setProgress(++numberOfStationsLoaded);
                    loadingBox.setMessage(parentActivity.getResources().getString(R.string.loading_ruter) + "\n" + stationName);
                }
            }
        });
    }
}
