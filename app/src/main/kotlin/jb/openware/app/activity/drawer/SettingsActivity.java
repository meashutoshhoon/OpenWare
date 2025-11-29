package in.afi.codekosh.activity.drawer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import in.afi.codekosh.R;
import in.afi.codekosh.pages.components.DefaultLayout;
import in.afi.codekosh.pages.components.LayoutHelper;
import in.afi.codekosh.pages.components.SlideContainer;
import jb.openware.app.pages.components.SlideView;
import in.afi.codekosh.tools.BaseFragment;
import in.afi.codekosh.tools.ThemeBuilder;

public class SettingsActivity extends BaseFragment {
    private SlideContainer container;
    private SlideView[] views;
    private final OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {
            onNavigateBack();
        }
    };

    @Override
    protected boolean isHomeFragment() {
        return false;
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_settings;
    }

    @Override
    public void getThemeDescriptions(ThemeBuilder themeBuilder) {
    }

    @Override
    protected void initialize() {
        // Initialize variables
        LinearLayout baseLayout = findViewById(R.id.baseLayout);
        container = new SlideContainer(this);
        views = new SlideView[6];

        // Create views
        views[0] = new MainPanel(this);
        views[1] = new CodesPanel(this);
        views[2] = new AppPanel(this);
        views[3] = new DynamicPanel(this);
        views[4] = new DownloadPanel(this);
        views[5] = new SwbPanel(this);

        // Add views
        for (int a = 0; a < views.length; a++) {
            views[a].setVisibility(a == 0 ? View.VISIBLE : View.GONE);
            container.addSlideView(views[a]);
        }

        // Add Container
        baseLayout.addView(container, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
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
        if (container.isCurrentSlideView(views[0])) {
            goBack();
            return;
        }

        container.showSlide(0);
    }

    public class MainPanel extends SlideView {


        @SuppressLint("SetTextI18n")
        public MainPanel(Context context) {
            super(context);
            // Default layout
            DefaultLayout layout = new DefaultLayout(context);

            // Back pressed
            layout.backButtonClickListener(v -> goBack());

            // Title
            TextView titleView = new TextView(context);
            titleView.setSingleLine(true);
            titleView.setTypeface(ResourcesCompat.getFont(context, R.font.en_light));
            titleView.setLineSpacing(dp(2), 1.0f);
            titleView.setText("Settings");
            titleView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 30);
            titleView.setTextColor(isNightMode() ? WHITE : BLACK);
            layout.attachToRoot(titleView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, dp(30), dp(15), dp(0), dp(10)));

            RecyclerView recyclerView = new RecyclerView(context);
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            TitleListAdapter adapter = new TitleListAdapter(getItems(), (Activity) context);
            adapter.setOnItemClickListener(position -> container.showSlide(position + 1));
            recyclerView.setAdapter(adapter);
            recyclerView.setPaddingRelative(dp(5), dp(5), dp(5), dp(5));

            layout.attachToRoot(recyclerView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, dp(5), dp(5), dp(5), dp(5)));


            // Add default layout
            addView(layout, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
        }


        @NonNull
        private ArrayList<TitleListItem> getItems() {
            ArrayList<TitleListItem> listItems = new ArrayList<>();

            // Item 1
            TitleListItem item1 = new TitleListItem();
            item1.setTitle("Code Viewer theme");
            item1.setDescription("Android Studio, Dracula, Github, Google code, vs2005, Solarized dark");
            item1.setImage(R.drawable.code);
            listItems.add(item1);

            // Item 2
            TitleListItem item2 = new TitleListItem();
            item2.setTitle("App theme");
            item2.setDescription("System default, Dark, Light");
            item2.setImage(R.drawable.theme);
            listItems.add(item2);

            // Item 3
            TitleListItem item3 = new TitleListItem();
            item3.setTitle("Dynamic colors");
            item3.setDescription("Disabled, Enabled");
            item3.setImage(R.drawable.dynamic_colors);
            listItems.add(item3);

            // Item 4
            TitleListItem item4 = new TitleListItem();
            item4.setTitle("Download Manager");
            item4.setDescription("In-built downloader (Fast), Firebase Downloader");
            item4.setImage(R.drawable.download);
            listItems.add(item4);

            // Item 5
            TitleListItem item5 = new TitleListItem();
            item5.setTitle("Swb(.swb) auto-installer");
            item5.setDescription("Disabled, In-built installer, Sketchware Pro installer");
            item5.setImage(R.drawable.swb_install);
            listItems.add(item5);


            return listItems;
        }

    }

    public class CodesPanel extends SlideView {
        private ArrayList<String> listItems;

        @SuppressLint("SetTextI18n")
        public CodesPanel(Context context) {
            super(context);
            // Default layout
            DefaultLayout layout = new DefaultLayout(context);

            // Back pressed
            layout.backButtonClickListener(v -> container.showPreviousSlide());

            // Title
            TextView titleView = new TextView(context);
            titleView.setSingleLine(true);
            titleView.setTypeface(ResourcesCompat.getFont(context, R.font.en_light));
            titleView.setLineSpacing(dp(2), 1.0f);
            titleView.setText("Code Viewer theme");
            titleView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 30);
            titleView.setTextColor(isNightMode() ? WHITE : BLACK);
            layout.attachToRoot(titleView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, dp(30), dp(15), dp(0), dp(10)));

            int checkedItem = 0;
            String f = getAppConfig().getEditorTheme();
            switch (f) {
                case "dracula":
                    checkedItem = 1;
                    break;
                case "github":
                    checkedItem = 2;
                    break;
                case "google_code":
                    checkedItem = 3;
                    break;
                case "vs_code":
                    checkedItem = 4;
                    break;
                case "solarized_dark":
                    checkedItem = 5;
                    break;
                case "vs":
                    checkedItem = 6;
                    break;
                case "xcode":
                    checkedItem = 7;
                    break;
                case "ocean":
                    checkedItem = 8;
                    break;
                case "monokai":
                    checkedItem = 9;
                    break;
                case "dark":
                    checkedItem = 10;
                    break;
                case "hybrid":
                    checkedItem = 11;
                    break;
            }

            RecyclerView recyclerView = new RecyclerView(context);
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            recyclerView.setNestedScrollingEnabled(true);
            RatioListAdapter adapter = new RatioListAdapter(getItems(), checkedItem, (Activity) context);
            adapter.setOnItemClickListener(position -> {
                String selectedTheme = listItems.get(position);
                switch (selectedTheme) {
                    case "Android Studio":
                        getAppConfig().setEditorTheme("android_studio");
                        break;
                    case "Dracula":
                        getAppConfig().setEditorTheme("dracula");
                        break;
                    case "Github":
                        getAppConfig().setEditorTheme("github");
                        break;
                    case "Google code":
                        getAppConfig().setEditorTheme("google_code");
                        break;
                    case "vs2005":
                        getAppConfig().setEditorTheme("vs_code");
                        break;
                    case "Solarized dark":
                        getAppConfig().setEditorTheme("solarized_dark");
                        break;
                    case "VS":
                        getAppConfig().setEditorTheme("vs");
                        break;
                    case "X-Code":
                        getAppConfig().setEditorTheme("xcode");
                        break;
                    case "Ocean":
                        getAppConfig().setEditorTheme("ocean");
                        break;
                    case "Monokai":
                        getAppConfig().setEditorTheme("monokai");
                        break;
                    case "Dark":
                        getAppConfig().setEditorTheme("dark");
                        break;
                    case "Hybrid":
                        getAppConfig().setEditorTheme("hybrid");
                        break;
                }
            });
            recyclerView.setAdapter(adapter);
            recyclerView.setPaddingRelative(0, dp(5), 0, dp(5));

            layout.attachToRoot(recyclerView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, dp(5), dp(5), dp(5), dp(5)));


            // Add default layout
            addView(layout, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
        }


        @NonNull
        private ArrayList<String> getItems() {
            listItems = new ArrayList<>();

            // Item 1
            listItems.add("Android Studio");

            // Item 2
            listItems.add("Dracula");

            // Item 3
            listItems.add("Github");

            // Item 4
            listItems.add("Google code");

            // Item 5
            listItems.add("vs2005");

            // Item 6
            listItems.add("Solarized dark");

            // Item 7
            listItems.add("VS");

            // Item 8
            listItems.add("X-Code");

            // Item 9
            listItems.add("Ocean");

            // Item 10
            listItems.add("Monokai");

            // Item 11
            listItems.add("Dark");

            // Item 12
            listItems.add("Hybrid");


            return listItems;
        }

    }

    public class AppPanel extends SlideView {
        private ArrayList<String> listItems;

        @SuppressLint("SetTextI18n")
        public AppPanel(Context context) {
            super(context);
            // Default layout
            DefaultLayout layout = new DefaultLayout(context);

            // Back pressed
            layout.backButtonClickListener(v -> container.showSlide(0));

            // Title
            TextView titleView = new TextView(context);
            titleView.setSingleLine(true);
            titleView.setTypeface(ResourcesCompat.getFont(context, R.font.en_light));
            titleView.setLineSpacing(dp(2), 1.0f);
            titleView.setText("App theme");
            titleView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 30);
            titleView.setTextColor(isNightMode() ? WHITE : BLACK);
            layout.attachToRoot(titleView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, dp(30), dp(15), dp(0), dp(10)));
            int checkedItem = 0;
            String f = getAppConfig().getAppTheme();
            if (f.equals("dark")) {
                checkedItem = 1;
            } else if (f.equals("light")) {
                checkedItem = 2;
            }

            RecyclerView recyclerView = new RecyclerView(context);
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            recyclerView.setNestedScrollingEnabled(true);
            RatioListAdapter adapter = new RatioListAdapter(getItems(), checkedItem, (Activity) context);
            adapter.setOnItemClickListener(position -> {
                String selectedTheme = listItems.get(position);
                switch (selectedTheme) {
                    case "System default":
                        getAppConfig().setAppTheme("system");
                        new Handler().post(() -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM));
                        break;
                    case "Dark":
                        getAppConfig().setAppTheme("dark");
                        new Handler().post(() -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES));
                        break;
                    case "Light":
                        getAppConfig().setAppTheme("light");
                        new Handler().post(() -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO));
                }
            });
            recyclerView.setAdapter(adapter);
            recyclerView.setPaddingRelative(0, dp(5), 0, dp(5));

            layout.attachToRoot(recyclerView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, dp(5), dp(5), dp(5), dp(5)));


            // Add default layout
            addView(layout, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
        }


        @NonNull
        private ArrayList<String> getItems() {
            listItems = new ArrayList<>();

            // Item 1
            listItems.add("System default");

            // Item 2
            listItems.add("Dark");

            // Item 3
            listItems.add("Light");

            return listItems;
        }

    }

    public class DynamicPanel extends SlideView {
        private ArrayList<String> listItems;

        @SuppressLint("SetTextI18n")
        public DynamicPanel(Context context) {
            super(context);
            // Default layout
            DefaultLayout layout = new DefaultLayout(context);

            // Back pressed
            layout.backButtonClickListener(v -> container.showSlide(0));

            // Title
            TextView titleView = new TextView(context);
            titleView.setSingleLine(true);
            titleView.setTypeface(ResourcesCompat.getFont(context, R.font.en_light));
            titleView.setLineSpacing(dp(2), 1.0f);
            titleView.setText("Dynamic colors");
            titleView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 30);
            titleView.setTextColor(isNightMode() ? WHITE : BLACK);
            layout.attachToRoot(titleView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, dp(30), dp(15), dp(0), dp(10)));

            RecyclerView recyclerView = new RecyclerView(context);
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            recyclerView.setNestedScrollingEnabled(true);
            RatioListAdapter adapter = new RatioListAdapter(getItems(), getAppConfig().getDynamicTheme(), (Activity) context);
            adapter.setOnItemClickListener(position -> {
                String selectedTheme = listItems.get(position);
                ApplicationLoader applicationLoader = (ApplicationLoader) getApplication();
                switch (selectedTheme) {
                    case "Disabled":
                        getAppConfig().setDynamicTheme(0);
                        applicationLoader.init();
                        showToast("Restart the app");
                        break;
                    case "Enabled":
                        getAppConfig().setDynamicTheme(1);
                        applicationLoader.init();
                        showToast("Restart the app");
                        break;
                }
            });
            recyclerView.setAdapter(adapter);
            recyclerView.setPaddingRelative(0, dp(5), 0, dp(5));

            layout.attachToRoot(recyclerView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, dp(5), dp(5), dp(5), dp(5)));


            // Add default layout
            addView(layout, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
        }


        @NonNull
        private ArrayList<String> getItems() {
            listItems = new ArrayList<>();

            // Item 1
            listItems.add("Disabled");

            // Item 2
            listItems.add("Enabled");

            return listItems;
        }

    }

    public class DownloadPanel extends SlideView {
        private ArrayList<String> listItems;

        @SuppressLint("SetTextI18n")
        public DownloadPanel(Context context) {
            super(context);
            // Default layout
            DefaultLayout layout = new DefaultLayout(context);

            // Back pressed
            layout.backButtonClickListener(v -> container.showSlide(0));

            // Title
            TextView titleView = new TextView(context);
            titleView.setSingleLine(true);
            titleView.setTypeface(ResourcesCompat.getFont(context, R.font.en_light));
            titleView.setLineSpacing(dp(2), 1.0f);
            titleView.setText("Download Manager");
            titleView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 30);
            titleView.setTextColor(isNightMode() ? WHITE : BLACK);
            layout.attachToRoot(titleView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, dp(30), dp(15), dp(0), dp(10)));
            int checkedItem = 0;
            String f = getAppConfig().getDownloadManager();
            if (f.equals("1")) {
                checkedItem = 1;
            }

            RecyclerView recyclerView = new RecyclerView(context);
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            recyclerView.setNestedScrollingEnabled(true);
            RatioListAdapter adapter = new RatioListAdapter(getItems(), checkedItem, (Activity) context);
            adapter.setOnItemClickListener(position -> {
                String selectedTheme = listItems.get(position);
                switch (selectedTheme) {
                    case "In-built downloader (Fast)":
                        getAppConfig().setDownloadManager("0");
                        break;
                    case "Firebase Downloader":
                        getAppConfig().setDownloadManager("1");
                        break;
                }
            });
            recyclerView.setAdapter(adapter);
            recyclerView.setPaddingRelative(0, dp(5), 0, dp(5));

            layout.attachToRoot(recyclerView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, dp(5), dp(5), dp(5), dp(5)));


            // Add default layout
            addView(layout, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
        }


        @NonNull
        private ArrayList<String> getItems() {
            listItems = new ArrayList<>();

            // Item 1
            listItems.add("In-built downloader (Fast)");

            // Item 2
            listItems.add("Firebase Downloader");

            return listItems;
        }

    }

    public class SwbPanel extends SlideView {
        private ArrayList<String> listItems;

        @SuppressLint("SetTextI18n")
        public SwbPanel(Context context) {
            super(context);
            // Default layout
            DefaultLayout layout = new DefaultLayout(context);

            // Back pressed
            layout.backButtonClickListener(v -> container.showSlide(0));

            // Title
            TextView titleView = new TextView(context);
            titleView.setSingleLine(true);
            titleView.setTypeface(ResourcesCompat.getFont(context, R.font.en_light));
            titleView.setLineSpacing(dp(2), 1.0f);
            titleView.setText("Swb Installer");
            titleView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 30);
            titleView.setTextColor(isNightMode() ? WHITE : BLACK);
            layout.attachToRoot(titleView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, dp(30), dp(15), dp(0), dp(10)));
            int checkedItem = 0;
            String f = getAppConfig().getSwbInstaller();
            if (f.equals("sketchware")) {
                checkedItem = 2;
            } else if (f.equals("built_in")) {
                checkedItem = 1;
            }

            RecyclerView recyclerView = new RecyclerView(context);
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            recyclerView.setNestedScrollingEnabled(true);
            RatioListAdapter adapter = new RatioListAdapter(getItems(), checkedItem, (Activity) context);
            adapter.setOnItemClickListener(position -> {
                String selectedTheme = listItems.get(position);
                switch (selectedTheme) {
                    case "Disabled":
                        getAppConfig().setSwbInstaller("disable");
                        break;
                    case "In-built installer":
                        getAppConfig().setSwbInstaller("built_in");
                        break;
                    case "Sketchware Pro installer":
                        getAppConfig().setSwbInstaller("sketchware");
                        alertCreator("Sketchware Pro installer requires latest version of sketchware pro.");
                        break;
                }
            });
            recyclerView.setAdapter(adapter);
            recyclerView.setPaddingRelative(0, dp(5), 0, dp(5));

            layout.attachToRoot(recyclerView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, dp(5), dp(5), dp(5), dp(5)));


            // Add default layout
            addView(layout, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
        }


        @NonNull
        private ArrayList<String> getItems() {
            listItems = new ArrayList<>();

            // Item 1
            listItems.add("Disabled");

            // Item 2
            listItems.add("In-built installer");

            // Item 3
            listItems.add("Sketchware Pro installer");

            return listItems;
        }

    }
}