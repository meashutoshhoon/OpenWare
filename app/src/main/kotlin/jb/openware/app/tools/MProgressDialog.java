package in.afi.codekosh.tools;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Window;
import android.widget.LinearLayout;

import java.util.Objects;

import in.afi.codekosh.R;

public class MProgressDialog {
    private final Activity activity;
    private ProgressDialog coreProgress;

    public MProgressDialog(Activity activity) {
        this.activity = activity;
    }

    public void show() {
        if (coreProgress == null) {
            coreProgress = new ProgressDialog(activity);
            coreProgress.setCancelable(false);
            coreProgress.setCanceledOnTouchOutside(false);

            coreProgress.requestWindowFeature(Window.FEATURE_NO_TITLE);
            Objects.requireNonNull(coreProgress.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        coreProgress.show();
        coreProgress.setContentView(R.layout.loading);

        LinearLayout linear2 = coreProgress.findViewById(R.id.linear2);
        LinearLayout background = coreProgress.findViewById(R.id.background);
        LinearLayout layout_progress = coreProgress.findViewById(R.id.layout_progress);

        android.graphics.drawable.GradientDrawable gd = new android.graphics.drawable.GradientDrawable();
        int uiMode = activity.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        switch (uiMode) {
            case Configuration.UI_MODE_NIGHT_YES:
                gd.setColor(Color.parseColor("#262626"));
                break;
            case Configuration.UI_MODE_NIGHT_NO:
                gd.setColor(Color.parseColor("#E0E0E0"));
                break;
        }
        gd.setCornerRadius(40);
        gd.setStroke(0, Color.WHITE);
        linear2.setBackground(gd);

        RadialProgressView progress = new RadialProgressView(activity);
        progress.setProgressColor(0xFF006493);
        layout_progress.addView(progress);
        background.setOnClickListener(_view -> coreProgress.dismiss());
    }

    public void hide() {
        if (coreProgress != null) {
            coreProgress.dismiss();
        }
    }
}