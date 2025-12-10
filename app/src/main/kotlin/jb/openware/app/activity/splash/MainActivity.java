package in.afi.codekosh.activity.splash;

import static in.afi.codekosh.pages.components.LayoutHelper.MATCH_PARENT;
import static in.afi.codekosh.pages.components.LayoutHelper.WRAP_CONTENT;
import static in.afi.codekosh.tools.StringUtilsKt.serverUrl;
import static in.afi.codekosh.tools.StringUtilsKt.versionCode;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.res.ResourcesCompat;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieDrawable;
import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.HashMap;

import in.afi.codekosh.R;
import in.afi.codekosh.activity.home.HomeActivity;
import in.afi.codekosh.activity.login.LoginActivity;
import in.afi.codekosh.components.SharedPreferencesManager;
import in.afi.codekosh.pages.components.SlideContainer;
import jb.openware.app.pages.components.SlideView;
import in.afi.codekosh.tools.AndroidUtils;
import in.afi.codekosh.tools.BaseFragment;
import in.afi.codekosh.tools.ThemeBuilder;

public class MainActivity extends BaseFragment {
    private final String get_link = "null";
    private final OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {
            onNavigateBack();
        }
    };
    private HashMap<String, Object> server_data = new HashMap<>();
    private SlideContainer container;
    private String message;
    private SlideView[] views;

    @Override
    protected boolean isHomeFragment() {
        return false;
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_main;
    }

    @Override
    public void getThemeDescriptions(ThemeBuilder themeBuilder) {
    }

    @Override
    protected void initialize() {
//        Uri uri = getIntent().getData();
//        if (uri != null) {
//            List<String> s = uri.getPathSegments();
//            showToast(s.get(s.size() - 1));
//        }
        String theme = getAppConfig().getAppTheme();
        if (theme.equals("dark")) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else if (theme.equals("light")) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        }

        // Initialize variables
        LinearLayout baseLayout = findViewById(R.id.baseLayout);
        container = new SlideContainer(this);
        views = new SlideView[3];

        // Create views
        views[0] = new MainPanel(this);
        views[1] = new NoInternetPanel(this);


        // Add views
        for (int a = 0; a < views.length - 1; a++) {
            views[a].setVisibility(a == 0 ? View.VISIBLE : View.GONE);
            container.addSlideView(views[a]);
        }

        // Add Container
        baseLayout.addView(container, LayoutHelper.createLinear(MATCH_PARENT, MATCH_PARENT));
    }

    @Override
    protected void initializeLogic() {
    }

    @Override
    public void onResume() {
        super.onResume();
        // Back button logic
        getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        onBackPressedCallback.setEnabled(false);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save the current slide index to restore later
        outState.putInt("currentSlideIndex", container.getCurrentSlideIndex());
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        int currentSlideIndex = savedInstanceState.getInt("currentSlideIndex", 0);
        // Restore the state
        container.showSlide(currentSlideIndex);
    }

    private void onNavigateBack() {
        finishAffinity();
    }

    @Override
    protected void activityLoad() {
        super.activityLoad();
        delayTask(this::processData);
    }

    private void processData() {
        getConnectionsManager(serverUrl, GET, new RequestListener() {
            @Override
            public void onResponse(String tag, String response, HashMap<String, Object> responseHeaders) {
                server_data = new Gson().fromJson(response, new TypeToken<HashMap<String, Object>>() {
                }.getType());
                double serverVersion = Double.parseDouble(String.valueOf(server_data.get("version")));
                double appVersion = Double.parseDouble(versionCode);

                if (appVersion < serverVersion) {
                    showUpdateDialog(server_data);
                } else if ("false".equals(server_data.get("server_status"))) {
                    message = String.valueOf(server_data.get("server_message"));
                    views[2] = new MaintenancePanel(getParentActivity());
                    container.addSlideView(views[2]);
                    delayTask(() -> container.showSlide(2));
                } else {
                    go();
                }

            }

            @Override
            public void onErrorResponse(String tag, String message) {
                if (!AndroidUtils.isConnected(getParentActivity())) {
                    container.showSlide(1);
                } else {
                    alertCreator(message);
                }
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void showUpdateDialog(HashMap<String, Object> serverData) {
        double necessaryVersion = Double.parseDouble(String.valueOf(serverData.get("necessary_update_version")));
        double appVersion = Double.parseDouble(versionCode);
        delayTask(() -> {
            createBottomSheetDialog(R.layout.update_cus);
            setBottomSheetCancelable(false);
            TextView version = (TextView) bsId(R.id.version);
            TextView message = (TextView) bsId(R.id.message);
            MaterialButton later = (MaterialButton) bsId(R.id.later);
            MaterialButton update = (MaterialButton) bsId(R.id.update);
            version.setText("v" + serverData.get("typo_version"));
            TextFormatter.formatText(message, String.valueOf(serverData.get("update_message")));
            later.setVisibility(appVersion < necessaryVersion ? View.VISIBLE : View.GONE);
            update.setOnClickListener(v -> openUrl(stringFormat(serverData.get("update_link"))));
            later.setOnClickListener(v -> go());
            showBottomSheetDialog();
        }, 500);
    }

    private void go() {
        delayTask(() -> {
            new SharedPreferencesManager(getParentActivity()).saveString("link", get_link);
            Intent go = new Intent(MainActivity.this, getUserConfig().isLoggedIn() ? (get_link.equals("null") ? HomeActivity.class : LinkageActivity.class) : LoginActivity.class);
            startActivity(go);
            finish();
        }, 100);
    }

    public class MainPanel extends SlideView {


        @SuppressLint("SetTextI18n")
        public MainPanel(Context context) {
            super(context);
            // Create the parent LinearLayout
            LinearLayout parentLayout = new LinearLayout(context);
            parentLayout.setGravity(Gravity.CENTER);
            parentLayout.setOrientation(LinearLayout.VERTICAL);

            // Create an ImageView
            ImageView imageView = new ImageView(context);
            imageView.setImageResource(R.drawable.demo_icon);

            // Create the bottom LinearLayout with layout weight 1
            LinearLayout bottomLayout = new LinearLayout(context);
            bottomLayout.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM);

            // Create the TextView
            TextView nameTextView = new TextView(context);
            nameTextView.setText(getString(R.string.company_name));
            nameTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
            nameTextView.setTextColor(isNightMode() ? WHITE : BLACK);
            nameTextView.setTypeface(ResourcesCompat.getFont(context, R.font.opensans_regular));

            // Add views to the parent layout
            parentLayout.addView(new View(context), LayoutHelper.createLinear(MATCH_PARENT, 0, 1));
            parentLayout.addView(imageView, LayoutHelper.createLinear(dp(150), dp(150)));
            parentLayout.addView(bottomLayout, LayoutHelper.createLinear(MATCH_PARENT, 0, 1));
            bottomLayout.addView(nameTextView, LayoutHelper.createLinear(WRAP_CONTENT, WRAP_CONTENT, 0, 0, 0, dp(15)));


            // Add default layout
            addView(parentLayout, LayoutHelper.createLinear(MATCH_PARENT, MATCH_PARENT));
        }

    }

    public class NoInternetPanel extends SlideView {

        @SuppressLint("SetTextI18n")
        public NoInternetPanel(Context context) {
            super(context);
            // Create the parent LinearLayout
            LinearLayout parentLayout = new LinearLayout(context);
            parentLayout.setGravity(Gravity.CENTER);
            parentLayout.setOrientation(LinearLayout.VERTICAL);

            // Create LottieAnimationView
            LottieAnimationView lottieAnimationView = new LottieAnimationView(context);
            lottieAnimationView.setAnimation("no_internet.json");
            lottieAnimationView.setRepeatCount(LottieDrawable.INFINITE);
            lottieAnimationView.playAnimation();

            // Create TextView
            TextView noInternetTextView = new TextView(context);
            noInternetTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
            noInternetTextView.setTextColor(isNightMode() ? WHITE : BLACK);
            noInternetTextView.setTypeface(ResourcesCompat.getFont(context, R.font.en_light));
            noInternetTextView.setText("No Internet Connection");

            // Create the bottom LinearLayout
            LinearLayout bottomLayout = new LinearLayout(context);
            bottomLayout.setGravity(Gravity.CENTER_VERTICAL | Gravity.END);
            bottomLayout.setOrientation(LinearLayout.HORIZONTAL);
            bottomLayout.setPaddingRelative(0, 0, dp(10), 0);

            // Create the Exit Button
            MaterialButton exitButton = new MaterialButton(context, null, R.attr.myButtonStyle);
            exitButton.setText("Exit");
            exitButton.setTypeface(ResourcesCompat.getFont(context, R.font.opensans_regular));
            exitButton.setOnClickListener(v -> finishAffinity());

            // Create the Try Again Button
            MaterialButton tryAgainButton = new MaterialButton(context);
            tryAgainButton.setTypeface(ResourcesCompat.getFont(context, R.font.opensans_medium));
            tryAgainButton.setText("Try again");
            tryAgainButton.setOnClickListener(v -> {
                container.showSlide(0);
                delayTask(MainActivity.this::processData);
            });

            // Add views to the parent layout
            parentLayout.addView(new View(context), LayoutHelper.createLinear(MATCH_PARENT, 0, 1));
            parentLayout.addView(lottieAnimationView, LayoutHelper.createLinear(dp(300), dp(300), 0, dp(8), 0, 0));
            parentLayout.addView(noInternetTextView, LayoutHelper.createLinear(WRAP_CONTENT, WRAP_CONTENT, 0, dp(15), 0, 0));
            parentLayout.addView(new View(context), LayoutHelper.createLinear(MATCH_PARENT, 0, 1));
            parentLayout.addView(bottomLayout, LayoutHelper.createLinear(MATCH_PARENT, WRAP_CONTENT));
            bottomLayout.addView(exitButton, LayoutHelper.createLinear(WRAP_CONTENT, WRAP_CONTENT, 0, 0, dp(5), 0));
            bottomLayout.addView(tryAgainButton, LayoutHelper.createLinear(WRAP_CONTENT, WRAP_CONTENT, 0, dp(10), 0, dp(10)));


            // Add default layout
            addView(parentLayout, LayoutHelper.createLinear(MATCH_PARENT, MATCH_PARENT));
        }

    }

    public class MaintenancePanel extends SlideView {
        public TextView messageTextView;

        @SuppressLint("SetTextI18n")
        public MaintenancePanel(Context context) {
            super(context);
            // Create the parent LinearLayout
            LinearLayout parentLayout = new LinearLayout(context);
            parentLayout.setGravity(Gravity.CENTER_HORIZONTAL);
            parentLayout.setOrientation(LinearLayout.VERTICAL);
            parentLayout.setPadding(0, 0, 0, dp(25));

            // Create the title TextView
            TextView titleTextView = new TextView(context);
            titleTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 28);
            titleTextView.setTextColor(isNightMode() ? WHITE : BLACK);
            titleTextView.setTypeface(ResourcesCompat.getFont(context, R.font.en_light));
            titleTextView.setText("Maintenance");

            // Create the LottieAnimationView
            LottieAnimationView lottieAnimationView = new LottieAnimationView(context);
            lottieAnimationView.setAnimation("maintenance.json");
            lottieAnimationView.setRepeatCount(LottieDrawable.INFINITE);
            lottieAnimationView.playAnimation();

            // Create the message TextView
            messageTextView = new TextView(context);
            messageTextView.setGravity(Gravity.CENTER);
            messageTextView.setText("Due To Some Technical Issues Maintenance Is Scheduled For 3 Hours.");
            messageTextView.setTextColor(GREY);
            messageTextView.setTypeface(ResourcesCompat.getFont(context, R.font.en_light));
            messageTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);

            // Add views to the parent layout
            parentLayout.addView(titleTextView, LayoutHelper.createLinear(WRAP_CONTENT, WRAP_CONTENT, 0, dp(20), 0, 0));
            parentLayout.addView(lottieAnimationView, LayoutHelper.createLinear(MATCH_PARENT, 0, 1, 0, dp(16), 0, 0));
            parentLayout.addView(messageTextView, LayoutHelper.createLinear(WRAP_CONTENT, WRAP_CONTENT, dp(25), dp(8), dp(25), dp(15)));


            messageTextView.setText(message);


            // Add default layout
            addView(parentLayout, LayoutHelper.createLinear(MATCH_PARENT, MATCH_PARENT));
        }

    }


    //    @Override
//    protected void onStart() {
////        Intent intent = getIntent();
//
//        // Check if the intent contains data
////        if (intent != null && intent.getData() != null) {
////            // Extract data from the deep link URL
////            Uri uri = intent.getData();
////            get_link = uri.toString();
////        }
//        super.onStart();
//        Intent intent = getIntent();
//        if (intent != null) {
//            Uri data = intent.getData();
//            if (data != null) {
//                get_link = data.toString();
//            } else {
//                get_link = "null";
//            }
//        } else {
//            get_link = "null";
//        }
//    }

}