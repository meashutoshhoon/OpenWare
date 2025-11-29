package in.afi.codekosh.activity.home;

import static in.afi.codekosh.tools.StringUtilsKt.defaultReason;
import static in.afi.codekosh.tools.StringUtilsKt.websiteUrl;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.ads.MobileAds;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.ump.ConsentInformation;
import com.google.android.ump.ConsentRequestParameters;
import com.google.android.ump.UserMessagingPlatform;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import de.hdodenhof.circleimageview.CircleImageView;
import in.afi.codekosh.R;
import in.afi.codekosh.activity.drawer.AboutUsActivity;
import in.afi.codekosh.activity.drawer.SettingsActivity;
import in.afi.codekosh.activity.drawer.UploadActivity;
import in.afi.codekosh.activity.profile.ProfileActivity;
import in.afi.codekosh.adapter.ListProjectAdapter;
import in.afi.codekosh.components.FileLog;
import in.afi.codekosh.components.SearchBarView;
import in.afi.codekosh.components.SearchEditText;
import in.afi.codekosh.components.SharedPreferencesManager;
import in.afi.codekosh.tools.AndroidUtils;
import in.afi.codekosh.tools.BaseFragment;
import in.afi.codekosh.tools.ThemeBuilder;
import in.afi.codekosh.tools.UserConfig;

public class HomeActivity extends BaseFragment {
    private final DatabaseReference premium = FirebaseDatabase.getInstance().getReference("projects/premium");
    private final DatabaseReference Users = FirebaseDatabase.getInstance().getReference("Users");
    private final HashMap<String, String> user_names = new HashMap<>();
    private final ArrayList<HashMap<String, Object>> all_map = new ArrayList<>();
    private final AtomicBoolean isMobileAdsInitializeCalled = new AtomicBoolean(false);
    private DrawerLayout _drawer;
    private SharedPreferences sharedPreferences;
    private SharedPreferences developer;
    private int page = 0;
    private FloatingActionButton fab;
    private LinearLayout progressbar1, linear_word;
    private final ValueEventListener valueEventListener1 = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot _param1) {
            try {
                all_map.clear();
                GenericTypeIndicator<HashMap<String, Object>> _ind = new GenericTypeIndicator<HashMap<String, Object>>() {
                };
                for (DataSnapshot _data : _param1.getChildren()) {
                    HashMap<String, Object> _map = _data.getValue(_ind);
                    all_map.add(_map);
                }
                Collections.reverse(all_map);
                progressbar1.setVisibility(View.GONE);
            } catch (Exception e) {
                Toast.makeText(getParentActivity(), e.toString(), Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
        }
    };
    private ConsentInformation consentInformation;
    private RecyclerView recyclerview1;
    private Query query1;
    private double limit = 0;
    private TextView name, tx_word;
    private boolean enable = false;
    private CircleImageView circleImageView;
    private ImageView im_verified;

    private SearchBarView search_bar;
    private LinearLayout toolbar_card;

    private void navigateWithDelay(Class<?> destinationClass) {
        delayTask(() -> startActivity(destinationClass));
    }

    private void sendFeedbackEmail() {
        String name = new UserConfig(HomeActivity.this).getName();
        String email = new UserConfig(HomeActivity.this).getEmail();
        String body = "Hello CodeKosh Support,\n\n My Name Is :- " + name + "\nEmail:- " + email + "\n\nI just want to give you a feedback:-";
        sendEmail("CodeKosh", body);
    }

    private void shareApp() {
        String msg = "Hey, Download this awesome app!" + "\n" + websiteUrl;
        AndroidUtils.shareText(msg, HomeActivity.this);
    }


    @Override
    protected boolean isHomeFragment() {
        return true;
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_new;
    }

    @Override
    public void getThemeDescriptions(ThemeBuilder themeBuilder) {
        themeBuilder.setTextColor(name, BLACK, WHITE);
    }

    private void renderProfileImage(Drawable resource) {
        runnableTask(() -> search_bar.getMenuIcon().setIcon(resource));
    }


    @Override
    protected void initialize() {
        // Initialize UI components
        fab = findViewById(R.id._fab);
        _drawer = findViewById(R.id.drawer_layout);
        recyclerview1 = findViewById(R.id.recyclerview1);
        progressbar1 = findViewById(R.id.progressbar1);
        toolbar_card = findViewById(R.id.toolbar_card);
        LinearLayout layout_progress = findViewById(R.id.layout_progress);
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        SearchEditText editText = findViewById(R.id.search_view);
        NavigationView navView = findViewById(R.id.navigation_view);
        LinearLayout home = findViewById(R.id.home);
        LinearLayout premium = findViewById(R.id.premium);
        LinearLayout notifications = findViewById(R.id.notifications);
        LinearLayout category = findViewById(R.id.category);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        // Drawer Layouts
        View headerView = navView.getHeaderView(0);
        name = headerView.findViewById(R.id.textView);
        TextView navUsername = headerView.findViewById(R.id.version);
        LinearLayout profileLayout = headerView.findViewById(R.id.profileLayout);
        circleImageView = headerView.findViewById(R.id.circleimageview1);
        tx_word = headerView.findViewById(R.id.tx_word);
        linear_word = headerView.findViewById(R.id.linear_word);
        im_verified = headerView.findViewById(R.id.im_verified);

        // Radial
        RadialProgressView progress = new RadialProgressView(getParentActivity());
        progress.setProgressColor(0xFF006493);
        layout_progress.addView(progress);

        // SearchBar
        search_bar = new SearchBarView(this);
        search_bar.init(toolbar, editText);
        search_bar.setEditTextEnable(false);
        search_bar.setEditTextClickListener(v -> startActivity(SearchActivity.class));
        search_bar.setToolbarClickListener(item -> {
            if (enable) {
                _drawer.openDrawer(GravityCompat.START);
            }
        });
        search_bar.setMenuClickListener(item -> {
            if (item.getItemId() == R.id.profile) {
                if (enable) {
                    developer.edit().putString("uid", getUID()).apply();
                    startActivity(ProfileActivity.class);
                }
            }
            return false;
        });


        // Drawer
        navView.setNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.label_one) {
                navigateWithDelay(AboutUsActivity.class);
            } else if (itemId == R.id.label_five) {
                navigateWithDelay(ChangeLogActivity.class);
            } else if (itemId == R.id.accelerator_item) {
                navigateWithDelay(JavaCodeActivity.class);
            } else if (itemId == R.id.label_four) {
                navigateWithDelay(IntroActivity.class);
            } else if (itemId == R.id.rotation_item) {
                navigateWithDelay(SketchwareProjectsActivity.class);
            } else if (itemId == R.id.label_two) {
                sendFeedbackEmail();
            } else if (itemId == R.id.label_three) {
                shareApp();
            } else if (itemId == R.id.label_five_) {
                openUrl(websiteUrl);
            } else if (itemId == R.id.search_item) {
                navigateWithDelay(ProfileActivity.class);
                developer.edit().putString("uid", getUID()).apply();
            } else if (itemId == R.id.dashboard_item_two) {
                navigateWithDelay(SettingsActivity.class);
            } else if (itemId == R.id.dashboard_item) {
                navigateWithDelay(UploadActivity.class);
            } else if (itemId == R.id.dashboard_query) {
                navigateWithDelay(VerificationRequestActivity.class);
            }
            _drawer.closeDrawer(GravityCompat.START);
            return false;
        });

        // Bottom Navigation
        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (enable) {
                int itemId = item.getItemId();
                if (itemId == R.id.item_1) {
                    showViews(toolbar_card, home, fab);
                    hideViews(premium, notifications, category);
                    page = 0;
                    return true;
                } else if (itemId == R.id.item_2) {
                    showViews(premium, toolbar_card, fab);
                    hideViews(notifications, home, category);
                    page = 1;
                    return true;
                } else if (itemId == R.id.item_3) {
                    showViews(notifications);
                    hideViews(premium, home, category, toolbar_card, fab);
                    page = 2;
                    return true;
                } else if (itemId == R.id.item_4) {
                    showViews(category);
                    hideViews(premium, home, notifications, toolbar_card, fab);
                    page = 3;
                    return true;
                }
            }
            return false;
        });

        // Set text color for navigation view items
        int textColor = isNightMode() ? 0xFFDDDDDD : 0xFF000000;
        navView.setItemTextColor(ColorStateList.valueOf(textColor));

        // Set header text
        name.setText(getUserConfig().getName());
        navUsername.setText(getUserConfig().getEmail());


        // Set listeners
        profileLayout.setOnClickListener(v -> {
            developer.edit().putString("uid", getUID()).apply();
            startActivity(ProfileActivity.class);
            _drawer.closeDrawer(GravityCompat.START);
        });

        // More Methods
        setDrawerUnLock(false);
        runSavedData();

        // Views
        showViews(home, toolbar, circleImageView);
        hideViews(premium, notifications, category, progressbar1, im_verified, linear_word);
    }

    private void runSavedData() {
        if (getUserConfig().getProfileUrl() != null) {
            if (getUserConfig().getProfileUrl().equals("none")) {
                renderProfileImage(DrawableGenerator.generateDrawable(getParentActivity(), Color.parseColor("#006493"), getUserConfig().getName().substring(0, 1)));
            } else {
                loadImage(getUserConfig().getProfileUrl());
            }
        }
    }

    private void setDrawerUnLock(boolean shouldLock) {
        if (shouldLock) {
            _drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        } else {
            _drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        }
    }

    private void loadImage(String profileUrl) {
        try {
            Glide.with(HomeActivity.this).load(profileUrl).centerCrop().circleCrop().sizeMultiplier(0.50f) // optional
                    .addListener(new com.bumptech.glide.request.RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            return true;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            renderProfileImage(resource);
                            return true;
                        }
                    }).submit();
        } catch (Exception ignored) {
        }
    }


    @Override
    protected void initializeLogic() {
        sharedPreferences = getPreferences(Context.MODE_PRIVATE);
        developer = getSharedPreferences("developer", Activity.MODE_PRIVATE);
        boolean dialogShown = sharedPreferences.getBoolean("dialogShown", false);

        if (!dialogShown) {
            // Show the dialog
            delayTask(() -> {
                startActivity(IntroActivity.class);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("dialogShown", true);
                editor.apply();
            }, 650);
        }

        setupFragments();

        fab.setOnClickListener(v -> delayTask(() -> startActivity(UploadActivity.class)));
        findViewById(R.id.base).setOnTouchListener(new OnSwipeTouchListener(getParentActivity()) {
            public void onSwipeRight() {
                _drawer.openDrawer(GravityCompat.START);
            }

            public void onSwipeLeft() {
                _drawer.closeDrawer(GravityCompat.START);
            }
        });
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (_drawer.isDrawerOpen(GravityCompat.START)) {
                    _drawer.closeDrawer(GravityCompat.START);
                    return;
                }
                finishAffinity();
            }
        });
        built();
        if (consentInformation.canRequestAds()) {
            new SharedPreferencesManager(getParentActivity()).getBoolean("ads", true);
            initializeMobileAdsSdk();
        }
    }

    private void built() {
        ConsentRequestParameters params = new ConsentRequestParameters.Builder().setTagForUnderAgeOfConsent(false).build();

        consentInformation = UserMessagingPlatform.getConsentInformation(this);
        consentInformation.requestConsentInfoUpdate(this, params, () -> {
            if (consentInformation.isConsentFormAvailable()) {
                loadForm();
            }
        }, requestConsentError -> FileLog.c(String.format("%s: %s", requestConsentError.getErrorCode(), requestConsentError.getMessage())));
    }

    private void loadForm() {
        UserMessagingPlatform.loadConsentForm(this, consentForm -> {
            if (consentInformation.getConsentStatus() == ConsentInformation.ConsentStatus.REQUIRED) {
                consentForm.show(this, formError -> {
                    if (consentInformation.getConsentStatus() == ConsentInformation.ConsentStatus.OBTAINED) {
                        new SharedPreferencesManager(getParentActivity()).getBoolean("ads", true);
                        initializeMobileAdsSdk();
                    } else {
                        new SharedPreferencesManager(getParentActivity()).getBoolean("ads", false);
                    }
                });
            } else if (consentInformation.getConsentStatus() == ConsentInformation.ConsentStatus.OBTAINED) {
                new SharedPreferencesManager(getParentActivity()).getBoolean("ads", true);
                initializeMobileAdsSdk();
            } else {
                new SharedPreferencesManager(getParentActivity()).getBoolean("ads", true);
                initializeMobileAdsSdk();
            }
        }, formError -> alertCreator(formError.getMessage()));


    }

    private void initializeMobileAdsSdk() {
        if (isMobileAdsInitializeCalled.getAndSet(true)) {
            return;
        }

        MobileAds.initialize(this);

    }

    private void setupFragments() {
        replaceFragment(R.id.home, new HomeFragment());
        replaceFragment(R.id.notifications, new NotificationFragment());
        replaceFragment(R.id.category, new CategoryFragment());
        setUpPremiumClass();
    }

    private void setUpPremiumClass() {
        recyclerview1.setAdapter(new ListProjectAdapter(all_map, getParentActivity(), user_names, 0));
        recyclerview1.setLayoutManager(new LinearLayoutManager(getParentActivity()));
        ChildEventListener _users_child_listener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot _param1, String _param2) {
                GenericTypeIndicator<HashMap<String, Object>> _ind = new GenericTypeIndicator<HashMap<String, Object>>() {
                };
                final String _childKey = _param1.getKey();
                final HashMap<String, Object> _childValue = _param1.getValue(_ind);
                enable = true;
                setDrawerUnLock(true);
                showViews(im_verified);
                if (_childValue != null && _childValue.containsKey("uid")) {
                    String uid = (String) _childValue.get("uid");
                    if (_childValue.containsKey("name")) {
                        user_names.put(uid, (String) _childValue.get("name"));
                    }
                }
                if (_childKey != null && _childValue != null && _childKey.equals(getUID())) {
                    if (_childValue.containsKey("name")) {
                        getUserConfig().setName(String.valueOf(_childValue.get("name")));
                        name.setText(getUserConfig().getName());
                    }
                    if (String.valueOf(_childValue.get("avatar")).equals("none")) {
                        showViews(linear_word);
                        hideViews(circleImageView);
                        tx_word.setText(getUserConfig().getName().substring(0, 1));
                        renderProfileImage(DrawableGenerator.generateDrawable(getParentActivity(), Color.parseColor(String.valueOf(_childValue.get("color"))), getUserConfig().getName().substring(0, 1)));
                    } else {
                        if (!isDestroyed()) {
                            Glide.with(HomeActivity.this).load(Uri.parse(Objects.requireNonNull(_childValue.get("avatar")).toString())).into(circleImageView);
                            loadImage(getUserConfig().getProfileUrl());
                        }
                        showViews(circleImageView);
                        hideViews(linear_word);
                    }
                    getUserConfig().setProfileUrl(String.valueOf(_childValue.get("avatar")));
                    if (_childValue.containsKey("badge")) {
                        getUserConfig().setBadge(Integer.parseInt(String.valueOf(_childValue.get("badge"))));
                    }
                    linear_word.setBackground(new GradientDrawable() {
                        public GradientDrawable getIns(int a, int b) {
                            this.setCornerRadius(a);
                            this.setColor(b);
                            return this;
                        }
                    }.getIns(360, Color.parseColor(String.valueOf(_childValue.get("color")))));
                    if (_childValue.containsKey("block")) {
                        if (String.valueOf(_childValue.get("block")).equals("true")) {
                            createBottomSheetDialog(R.layout.block_cell);
                            setBottomSheetCancelable(false);
                            TextView h1 = (TextView) bsId(R.id.h1);
                            TextView t1 = (TextView) bsId(R.id.t1);
                            if (isNightMode()) {
                                h1.setTextColor(WHITE);
                                t1.setTextColor(TEXT_WHITE);
                            } else {
                                h1.setTextColor(BLACK);
                                t1.setTextColor(0xFF424242);
                            }
                            if (_childValue.containsKey("reason")) {
                                t1.setText(String.valueOf(_childValue.get("reason")));
                            } else {
                                t1.setText(defaultReason);
                            }
                            MaterialButton b1 = (MaterialButton) bsId(R.id.b1);
                            b1.setOnClickListener(v12 -> finishAffinity());
                            showBottomSheetDialog();
                        }
                    }
                    Object badgeValue = getUserConfig().getBadge();
                    Object verifiedValue = String.valueOf(_childValue.get("verified"));


                    final int NO_BADGE = 0;
                    int badgeInt = Integer.parseInt(String.valueOf(badgeValue));
                    boolean isVerified = Boolean.parseBoolean(String.valueOf(verifiedValue));

                    if (badgeInt == NO_BADGE) {
                        if (isVerified) {
                            im_verified.setImageResource(R.drawable.verify);
                            im_verified.setColorFilter(0xFF00C853, PorterDuff.Mode.SRC_IN);
                            name.setTextColor(0xFF00C853);
                        } else {
                            hideViews(im_verified);
                        }
                    } else {
                        new BadgeDrawable(HomeActivity.this).setBadge(String.valueOf(badgeInt), im_verified);
                        int colorFilter = isNightMode() ? 0xFF8DCDFF : 0xFF006493;
                        name.setTextColor(colorFilter);
                    }

                }

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot _param1, String _param2) {
                GenericTypeIndicator<HashMap<String, Object>> _ind = new GenericTypeIndicator<HashMap<String, Object>>() {
                };
                final String _childKey = _param1.getKey();
                final HashMap<String, Object> _childValue = _param1.getValue(_ind);
                enable = true;
                setDrawerUnLock(true);
                if (_childValue != null && _childValue.containsKey("uid")) {
                    String uid = (String) _childValue.get("uid");
                    if (_childValue.containsKey("name")) {
                        user_names.put(uid, (String) _childValue.get("name"));
                    }
                }
                if (_childKey != null && _childValue != null && _childKey.equals(getUID())) {
                    if (_childValue.containsKey("name")) {
                        getUserConfig().setName(String.valueOf(_childValue.get("name")));
                        name.setText(getUserConfig().getName());
                    }
                    if (String.valueOf(_childValue.get("avatar")).equals("none")) {
                        showViews(linear_word);
                        hideViews(circleImageView);
                        tx_word.setText(getUserConfig().getName().substring(0, 1));
                        renderProfileImage(DrawableGenerator.generateDrawable(getParentActivity(), Color.parseColor(String.valueOf(_childValue.get("color"))), getUserConfig().getName().substring(0, 1)));
                    } else {
                        if (!isDestroyed()) {
                            Glide.with(HomeActivity.this).load(Uri.parse(Objects.requireNonNull(_childValue.get("avatar")).toString())).into(circleImageView);
                            loadImage(getUserConfig().getProfileUrl());
                        }
                        showViews(circleImageView);
                        hideViews(linear_word);
                    }
                    getUserConfig().setProfileUrl(String.valueOf(_childValue.get("avatar")));
                    if (_childValue.containsKey("badge")) {
                        getUserConfig().setBadge(Integer.parseInt(String.valueOf(_childValue.get("badge"))));
                    }
                    linear_word.setBackground(new GradientDrawable() {
                        public GradientDrawable getIns(int a, int b) {
                            this.setCornerRadius(a);
                            this.setColor(b);
                            return this;
                        }
                    }.getIns(360, Color.parseColor(String.valueOf(_childValue.get("color")))));
                    if (_childValue.containsKey("block")) {
                        if (String.valueOf(_childValue.get("block")).equals("true")) {
                            createBottomSheetDialog(R.layout.block_cell);
                            setBottomSheetCancelable(false);
                            TextView h1 = (TextView) bsId(R.id.h1);
                            TextView t1 = (TextView) bsId(R.id.t1);
                            if (isNightMode()) {
                                h1.setTextColor(WHITE);
                                t1.setTextColor(TEXT_WHITE);
                            } else {
                                h1.setTextColor(BLACK);
                                t1.setTextColor(0xFF424242);
                            }
                            if (_childValue.containsKey("reason")) {
                                t1.setText(String.valueOf(_childValue.get("reason")));
                            } else {
                                t1.setText(defaultReason);
                            }
                            MaterialButton b1 = (MaterialButton) bsId(R.id.b1);
                            b1.setOnClickListener(v12 -> finishAffinity());
                            showBottomSheetDialog();
                        }
                    }
                    Object badgeValue = getUserConfig().getBadge();
                    Object verifiedValue = String.valueOf(_childValue.get("verified"));


                    final int NO_BADGE = 0;
                    int badgeInt = Integer.parseInt(String.valueOf(badgeValue));
                    boolean isVerified = Boolean.parseBoolean(String.valueOf(verifiedValue));

                    if (badgeInt == NO_BADGE) {
                        if (isVerified) {
                            im_verified.setImageResource(R.drawable.verify);
                            im_verified.setColorFilter(0xFF00C853, PorterDuff.Mode.SRC_IN);
                            name.setTextColor(0xFF00C853);
                        } else {
                            hideViews(im_verified);
                        }
                    } else {
                        new BadgeDrawable(HomeActivity.this).setBadge(String.valueOf(badgeInt), im_verified);
                        int colorFilter = isNightMode() ? 0xFF8DCDFF : 0xFF006493;
                        name.setTextColor(colorFilter);
                    }

                }

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot _param1, String _param2) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot _param1) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError _param1) {

            }
        };
        Users.addChildEventListener(_users_child_listener);
        limit = 30;
        query1 = premium.limitToLast((int) limit);
        query1.addValueEventListener(valueEventListener1);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        recyclerview1.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (!recyclerView.canScrollVertically(1)) {
                    limit = limit + 15;
                    query1 = premium.limitToLast((int) limit);
                    query1.addValueEventListener(valueEventListener1);
                    progressbar1.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void replaceFragment(int containerViewId, Fragment fragment) {
        if (!isFinishing()) {
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(containerViewId, fragment);
            fragmentTransaction.commitAllowingStateLoss();
        }
    }


    public static class OnSwipeTouchListener implements View.OnTouchListener {

        private final GestureDetector gestureDetector;

        public OnSwipeTouchListener(Context c) {
            gestureDetector = new GestureDetector(c, new GestureListener());
        }

        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            return gestureDetector.onTouchEvent(event);
        }

        public void onSwipeRight() {
        }

        public void onSwipeLeft() {
        }

        public void onSwipeTop() {
        }

        public void onSwipeBottom() {
        }

        private final class GestureListener extends GestureDetector.SimpleOnGestureListener {

            private static final int SWIPE_THRESHOLD = 100;
            private static final int SWIPE_VELOCITY_THRESHOLD = 100;

            @Override
            public boolean onDown(@NonNull MotionEvent e) {
                return true;
            }

            @Override
            public boolean onFling(MotionEvent e1, @NonNull MotionEvent e2, float velocityX, float velocityY) {
                boolean result = false;
                try {
                    float diffY = e2.getY() - e1.getY();
                    float diffX = e2.getX() - e1.getX();
                    if (Math.abs(diffX) > Math.abs(diffY)) {
                        if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                            if (diffX > 0) {
                                onSwipeRight();
                            } else {
                                onSwipeLeft();
                            }
                            result = true;
                        }
                    } else {
                        if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                            if (diffY > 0) {
                                onSwipeBottom();
                            } else {
                                onSwipeTop();
                            }
                            result = true;
                        }
                    }
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
                return result;
            }
        }

    }

}