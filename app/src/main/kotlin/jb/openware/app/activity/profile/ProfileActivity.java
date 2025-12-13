package in.afi.codekosh.activity.profile;

import static jb.openware.app.util.StringUtilsKt.websiteUrl;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.AutoTransition;
import androidx.transition.TransitionManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.stfalcon.imageviewer.StfalconImageViewer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import in.afi.codekosh.R;
import in.afi.codekosh.activity.login.LoginActivity;
import in.afi.codekosh.adapter.BannerProjectAdapter;
import in.afi.codekosh.adapter.BaseProjectAdapter;
import in.afi.codekosh.components.SharedPreferencesManager;
import in.afi.codekosh.nativeAds.MobileAdsLoader;
import in.afi.codekosh.tools.AndroidUtils;
import in.afi.codekosh.tools.BaseFragment;

public class ProfileActivity extends BaseFragment {
    // Firebase Database
    private final FirebaseDatabase _firebase = FirebaseDatabase.getInstance();
    private final DatabaseReference Users = _firebase.getReference("Users");
    private final DatabaseReference normal = _firebase.getReference("projects/normal");
    private final DatabaseReference premium = _firebase.getReference("projects/premium");

    // ArrayLists to store data
    private final ArrayList<HashMap<String, Object>> normal_map = new ArrayList<>();
    private final ArrayList<HashMap<String, Object>> verify_map = new ArrayList<>();
    private final ArrayList<HashMap<String, Object>> most_map = new ArrayList<>();
    private final ArrayList<HashMap<String, Object>> editor_map = new ArrayList<>();
    private final ArrayList<HashMap<String, Object>> private_map = new ArrayList<>();
    private final ArrayList<HashMap<String, Object>> premium_map = new ArrayList<>();
    // Child Event Listeners
    private ChildEventListener _premium_child_listener, userChildEventListener, _normal_child_listener;
    // User Information
    private String name, url, color, bio;
    // Views
    private LinearLayout loading2, profileLayout, linear_word, loading, refer, projects, tools;
    private ImageView logout, back, share, edit, im_verified;
    private SharedPreferences developer;
    private View divider;
    private NestedScrollView content;
    private RecyclerView all_projects, premium_r, verify_r, editor_r, private_projects_r, most_r;
    // TextViews
    private TextView usernameText, total_projects, roleText, tx_word, total_downloads, likes, textview2;
    private CircleImageView circleImageView;

    @Override
    protected boolean isHomeFragment() {
        return false;
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_profile;
    }

    @Override
    public void getThemeDescriptions(ThemeBuilder themeBuilder) {
        // Set image color filters
        ImageView[] imageViews = {back, logout, share, edit};
        int[] textIds = {R.id.t1, R.id.t2, R.id.t3};
        TextView[] textViews = {total_projects, likes, total_downloads, usernameText};
        int[] viewIds = {R.id.s1, R.id.s2, R.id.s3, R.id.s4, R.id.s5, R.id.s6, R.id.tx_word, R.id.textview2};

        for (ImageView imageView : imageViews) {
            themeBuilder.setImageColorFilter(imageView, BLACK, WHITE);
        }

        for (int textId : textIds) {
            themeBuilder.setTextColor(findViewById(textId), 0xFF4D4D4D, TEXT_GREY);
        }

        for (TextView textView : textViews) {
            themeBuilder.setTextColor(textView, BLACK, WHITE);
        }

        for (int viewId : viewIds) {
            themeBuilder.setTextColor(findViewById(viewId), BLACK, WHITE);
        }

        themeBuilder.setLinearLayoutBackgroundColor(findViewById(R.id.layout_id), WHITE, BLACK);

        // divider
        if (themeBuilder.isNightMode()) {
            divider.setBackgroundColor(ContextCompat.getColor(this, R.color.divider_color_night));
        } else {
            divider.setBackgroundColor(ContextCompat.getColor(this, R.color.divider_color));
        }
    }

    @Override
    protected void initialize() {
        // Initialize views
        back = findViewById(R.id.back);
        logout = findViewById(R.id.logout);
        loading2 = findViewById(R.id.loading2);
        likes = findViewById(R.id.likes);
        total_projects = findViewById(R.id.total_projects);
        im_verified = findViewById(R.id.im_verified);
        profileLayout = findViewById(R.id.profileLayout);
        circleImageView = findViewById(R.id.circleimageview1);
        linear_word = findViewById(R.id.linear_word);
        textview2 = findViewById(R.id.textview2);
        all_projects = findViewById(R.id.all_projects);
        total_downloads = findViewById(R.id.total_downloads);
        usernameText = findViewById(R.id.usernameText);
        roleText = findViewById(R.id.roleText);
        edit = findViewById(R.id.edit);
        projects = findViewById(R.id.projects);
        tools = findViewById(R.id.tools_l);
        tx_word = findViewById(R.id.tx_word);
        share = findViewById(R.id.share);
        most_r = findViewById(R.id.most_r);
        loading = findViewById(R.id.loading);
        content = findViewById(R.id.nested_scroll_view);
        refer = findViewById(R.id.refer);
        premium_r = findViewById(R.id.premium);
        editor_r = findViewById(R.id.editor_r);
        verify_r = findViewById(R.id.verify_r);
        private_projects_r = findViewById(R.id.private_projects_r);
        developer = getSharedPreferences("developer", Activity.MODE_PRIVATE);

        RadialProgressView progress = new RadialProgressView(this);
        progress.setProgressColor(0xFF006493);
        loading2.addView(progress);

        setParameters(2);
        transitionManager(refer, 400);

        delayTask(() -> {
            userChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot _param1, String _param2) {
                    GenericTypeIndicator<HashMap<String, Object>> _ind = new GenericTypeIndicator<HashMap<String, Object>>() {
                    };
                    final String _childKey = _param1.getKey();
                    final HashMap<String, Object> _childValue = _param1.getValue(_ind);
                    if (_childKey != null) {
                        if (_childKey.equals(developer.getString("uid", ""))) {
                            if (_childValue != null) {
                                if (_childValue.containsKey("name")) {
                                    usernameText.setText(stringFormat(_childValue.get("name")));
                                    name = stringFormat(_childValue.get("name"));
                                }
                                if (_childValue.containsKey("badge")) {
                                    String badge = stringFormat(_childValue.get("badge"));
                                    if (!badge.equals("0")) {
                                        new BadgeDrawable(ProfileActivity.this).setBadge(badge, findViewById(R.id.badge_img));
                                        showViews(findViewById(R.id.badge));
                                    } else {
                                        hideViews(findViewById(R.id.badge));
                                    }
                                }
                                if (_childValue.containsKey("projects")) {
                                    total_projects.setText(formatNumber(stringFormat(_childValue.get("projects"))));
                                }
                                if (_childValue.containsKey("verified")) {
                                    boolean isVerified = Boolean.parseBoolean(stringFormat(_childValue.get("verified")));
                                    im_verified.setVisibility(isVerified ? View.VISIBLE : View.GONE);
                                }
                                if (_childValue.containsKey("bio")) {
                                    bio = stringFormat(_childValue.get("bio"));
                                    delayTask(() -> uiThread(() -> {
                                        performTransition(profileLayout, 400);
                                        roleText.setVisibility(View.VISIBLE);
                                        roleText.setText(String.valueOf(_childValue.get("bio")));
                                    }), 700);
                                }
                                if (String.valueOf(_childValue.get("avatar")).equals("none")) {
                                    showViews(linear_word);
                                    hideViews(circleImageView);
                                    tx_word.setText(Objects.requireNonNull(_childValue.get("name")).toString().substring(0, 1));
                                    url = "none";
                                } else {
                                    Glide.with(ProfileActivity.this).load(Uri.parse(Objects.requireNonNull(_childValue.get("avatar")).toString())).into(circleImageView);
                                    showViews(circleImageView);
                                    hideViews(linear_word);
                                    url = Objects.requireNonNull(_childValue.get("avatar")).toString();
                                }
                                if (_childValue.containsKey("downloads")) {
                                    total_downloads.setText(formatNumber(String.valueOf(_childValue.get("downloads"))));
                                }
                                if (_childValue.containsKey("likes")) {
                                    likes.setText(formatNumber(String.valueOf(_childValue.get("likes"))));
                                }
                                linear_word.setBackground(new GradientDrawable() {
                                    public GradientDrawable getIns(int a, int b) {
                                        this.setCornerRadius(a);
                                        this.setColor(b);
                                        return this;
                                    }
                                }.getIns(360, Color.parseColor(String.valueOf(_childValue.get("color")))));
                                color = String.valueOf(_childValue.get("color"));
                                toggle(false);


                            }
                        } else {
                            hideViews(edit, logout);
                        }
                    }
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot _param1, String _param2) {
                    GenericTypeIndicator<HashMap<String, Object>> _ind = new GenericTypeIndicator<HashMap<String, Object>>() {
                    };
                    final String _childKey = _param1.getKey();
                    final HashMap<String, Object> _childValue = _param1.getValue(_ind);
                    if (_childKey != null) {
                        if (_childKey.equals(developer.getString("uid", ""))) {
                            if (_childValue != null) {
                                if (_childValue.containsKey("name")) {
                                    usernameText.setText(String.valueOf(_childValue.get("name")));
                                    name = String.valueOf(_childValue.get("name"));
                                }
                                if (_childValue.containsKey("badge")) {
                                    String badge = String.valueOf(_childValue.get("badge"));
                                    if (!badge.equals("0")) {
                                        new BadgeDrawable(ProfileActivity.this).setBadge(badge, findViewById(R.id.badge_img));
                                        showViews(findViewById(R.id.badge));
                                    } else {
                                        hideViews(findViewById(R.id.badge));
                                    }
                                }
                                if (_childValue.containsKey("projects")) {
                                    total_projects.setText(formatNumber(String.valueOf(_childValue.get("projects"))));
                                }
                                if (_childValue.containsKey("verified")) {
                                    boolean isVerified = Boolean.parseBoolean(String.valueOf(_childValue.get("verified")));
                                    im_verified.setVisibility(isVerified ? View.VISIBLE : View.GONE);
                                }

                                if (_childValue.containsKey("bio")) {
                                    bio = String.valueOf(_childValue.get("bio"));
                                    delayTask(() -> uiThread(() -> {
                                        performTransition(profileLayout, 400);
                                        roleText.setVisibility(View.VISIBLE);
                                        roleText.setText(String.valueOf(_childValue.get("bio")));
                                    }), 700);
                                }
                                if (String.valueOf(_childValue.get("avatar")).equals("none")) {
                                    showViews(linear_word);
                                    hideViews(circleImageView);
                                    tx_word.setText(Objects.requireNonNull(_childValue.get("name")).toString().substring(0, 1));
                                    url = "none";
                                } else {
                                    Glide.with(ProfileActivity.this).load(Uri.parse(Objects.requireNonNull(_childValue.get("avatar")).toString())).into(circleImageView);
                                    showViews(circleImageView);
                                    hideViews(linear_word);
                                    url = Objects.requireNonNull(_childValue.get("avatar")).toString();
                                }
                                if (_childValue.containsKey("downloads")) {
                                    total_downloads.setText(formatNumber(String.valueOf(_childValue.get("downloads"))));
                                }
                                if (_childValue.containsKey("likes")) {
                                    likes.setText(formatNumber(String.valueOf(_childValue.get("likes"))));
                                }
                                linear_word.setBackground(new GradientDrawable() {
                                    public GradientDrawable getIns(int a, int b) {
                                        this.setCornerRadius(a);
                                        this.setColor(b);
                                        return this;
                                    }
                                }.getIns(360, Color.parseColor(String.valueOf(_childValue.get("color")))));
                                color = String.valueOf(_childValue.get("color"));
                                toggle(false);


                            }
                        } else {
                            hideViews(edit, logout);
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
            Users.addChildEventListener(userChildEventListener);

            _premium_child_listener = new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot _param1, String _param2) {
                    GenericTypeIndicator<HashMap<String, Object>> _ind = new GenericTypeIndicator<HashMap<String, Object>>() {
                    };
                    final HashMap<String, Object> _childValue = _param1.getValue(_ind);
                    if (_childValue != null && _childValue.containsKey("uid")) {
                        String uid = developer.getString("uid", "");
                        String visibility = String.valueOf(_childValue.get("visibility"));

                        if (uid.equals(String.valueOf(_childValue.get("uid"))) && visibility.equals("true")) {
                            premium_map.add(0, _childValue);
                            premium_r.setAdapter(new BannerProjectAdapter(premium_map, ProfileActivity.this, 0));
                            showViews(findViewById(R.id.premium_layout));
                        } else {
                            hideViews(findViewById(R.id.premium_layout));
                        }
                    }

                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot _param1, String _param2) {
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
            premium.addChildEventListener(_premium_child_listener);

            _normal_child_listener = new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot _param1, String _param2) {
                    GenericTypeIndicator<HashMap<String, Object>> _ind = new GenericTypeIndicator<HashMap<String, Object>>() {
                    };
                    final HashMap<String, Object> _childValue = _param1.getValue(_ind);
                    if (_childValue != null && _childValue.containsKey("uid")) {
                        if (String.valueOf(_childValue.get("uid")).equals(developer.getString("uid", ""))) {
                            processData(_childValue);
                            setParameters(1);
                        } else {
                            setParameters(3);
                        }
                    }
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot _param1, String _param2) {

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
            normal.addChildEventListener(_normal_child_listener);
        });


    }

    private void processData(HashMap<String, Object> childValue) {
        verifyList(childValue);
        editorList(childValue);
        privateList(childValue);
        allList(childValue);
        mostList(childValue);
    }

    private void verifyList(HashMap<String, Object> childValue) {
        String visibility = String.valueOf(childValue.get("visibility"));
        String verify = String.valueOf(childValue.get("verify"));

        if ("true".equals(visibility) && "true".equals(verify)) {
            verify_map.add(0, childValue);
            BaseProjectAdapter verifiedAdapter = new BaseProjectAdapter(verify_map, ProfileActivity.this);
            verify_r.setAdapter(verifiedAdapter);

            if (!verify_map.isEmpty()) {
                showViews(findViewById(R.id.verified));
            } else {
                hideViews(findViewById(R.id.verified));
            }
        }

    }

    private void editorList(HashMap<String, Object> childValue) {
        String visibility = String.valueOf(childValue.get("visibility"));
        String editor = String.valueOf(childValue.get("editors_choice"));

        if ("true".equals(visibility) && "true".equals(editor)) {
            editor_map.add(0, childValue);
            BaseProjectAdapter verifiedAdapter = new BaseProjectAdapter(editor_map, ProfileActivity.this);
            editor_r.setAdapter(verifiedAdapter);

            if (!editor_map.isEmpty()) {
                showViews(findViewById(R.id.editor));
            } else {
                hideViews(findViewById(R.id.editor));
            }
        }

    }

    private void privateList(HashMap<String, Object> childValue) {
        String visibility = String.valueOf(childValue.get("visibility"));

        if ("false".equals(visibility)) {
            private_map.add(0, childValue);
            BaseProjectAdapter verifiedAdapter = new BaseProjectAdapter(private_map, ProfileActivity.this);
            private_projects_r.setAdapter(verifiedAdapter);

            if (!private_map.isEmpty()) {
                showViews(findViewById(R.id.private_l));
            } else {
                hideViews(findViewById(R.id.private_l));
            }
        }

    }

    private void allList(HashMap<String, Object> childValue) {
        normal_map.add(0, childValue);
        BaseProjectAdapter normalAdapter = new BaseProjectAdapter(normal_map, ProfileActivity.this);
        all_projects.setAdapter(normalAdapter);

        if (!normal_map.isEmpty()) {
            showViews(findViewById(R.id.all_p));
        } else {
            hideViews(findViewById(R.id.all_p));
        }


    }

    private void mostList(HashMap<String, Object> childValue) {
        String visibility = String.valueOf(childValue.get("visibility"));
        if ("true".equals(visibility)) {
            ArrayList<HashMap<String, Object>> newArrayList = new ArrayList<>();
            syncTask(new SyncTaskListener() {
                @Override
                public void beforeTaskStart() {

                }

                @Override
                public void onBackground() {
                    most_map.add(0, childValue);
                    sortMapListByKeyValuePair(most_map, "likes", true, false);
                    if (!most_map.isEmpty()) {
                        newArrayList.add(most_map.get(0));
                    } else {
                        hideViews(findViewById(R.id.most));
                    }
                }

                @Override
                public void onTaskComplete() {
                    if (String.valueOf(newArrayList.get(0).get("likes")).equals("0")) {
                        hideViews(findViewById(R.id.most));
                    } else {
                        BannerProjectAdapter mostAdapter = new BannerProjectAdapter(newArrayList, ProfileActivity.this, 1);
                        most_r.setAdapter(mostAdapter);
                        transitionManager(refer, 400);
                        delayTask(() -> {
                            showViews(findViewById(R.id.most));
                            if (!newArrayList.isEmpty()) {
                                showViews(findViewById(R.id.most));
                            } else {
                                hideViews(findViewById(R.id.most));
                            }
                        });


                    }
                }
            });


        }

    }

    public void sortMapListByKeyValuePair(final ArrayList<HashMap<String, Object>> mapList, final String key, final boolean isNumeric, final boolean isAscending) {
        mapList.sort((map1, map2) -> {
            if (isNumeric) {
                int count1 = Integer.parseInt(String.valueOf(map1.get(key)));
                int count2 = Integer.parseInt(String.valueOf(map2.get(key)));
                if (isAscending) {
                    return Integer.compare(count1, count2);
                } else {
                    return Integer.compare(count2, count1);
                }
            } else {
                String value1 = String.valueOf(map1.get(key));
                String value2 = String.valueOf(map2.get(key));
                if (isAscending) {
                    return value1.compareTo(value2);
                } else {
                    return value2.compareTo(value1);
                }
            }
        });
    }

    private void setParameters(int s) {
        if (s == 1) {
            hideViews(tools);
            showViews(projects);
        } else if (s == 2) {
            hideViews(projects, textview2);
            showViews(tools, loading2);
        } else if (s == 3) {
            hideViews(projects, loading2);
            showViews(tools, textview2);
        }
    }

    public void transitionManager(final View view, final double duration) {
        LinearLayout viewGroup = (LinearLayout) view;
        AutoTransition autoTransition = new AutoTransition();
        autoTransition.setDuration((long) duration);
        autoTransition.setInterpolator(new DecelerateInterpolator());
        TransitionManager.beginDelayedTransition(viewGroup, autoTransition);
    }

    @Override
    protected void onDestroy() {
        if (userChildEventListener != null) {
            Users.removeEventListener(userChildEventListener);
        }
        if (_premium_child_listener != null) {
            premium.removeEventListener(_premium_child_listener);
        }
        if (_normal_child_listener != null) {
            normal.removeEventListener(_normal_child_listener);
        }
        super.onDestroy();
    }

    @Override
    protected void initializeLogic() {
        setUpDivider();
        setLoading();
        back.setOnClickListener(v -> goBack());
        logout.setOnClickListener(v -> {
            MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(this, com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog_Centered);
            dialog.setTitle("Logout Confirmation");
            dialog.setMessage("Are you sure you want to log out of your account? This action will end your current session.");
            dialog.setIcon(R.drawable.logout);
            dialog.setPositiveButton("Log Out", (dialog1, which) -> {
                new UserConfig(this).logout();
                FirebaseAuth.getInstance().signOut();
                developer.edit().remove("uid").apply();
                developer.edit().remove("type").apply();
                openActivity(new LoginActivity());
            });
            dialog.setNegativeButton("Cancel", null);
            dialog.show();
        });
        edit.setOnClickListener(v -> {
            Intent intent = new Intent(this, ProfileEditActivity.class);
            intent.putExtra("name", name);
            intent.putExtra("url", url);
            intent.putExtra("color", color);
            intent.putExtra("bio", bio);
            startActivity(intent);

        });
        share.setOnClickListener(v -> AndroidUtils.shareText(websiteUrl + "u/" + name, this));
        findViewById(R.id.badge).setOnClickListener(v -> {
            performHapticFeedback(findViewById(R.id.badge));
            createBottomSheetDialog(R.layout.badge_layout);
            TextView title = (TextView) bsId(R.id.title);
            int[] imageViewIds = {R.id.i1, R.id.i2, R.id.i3, R.id.i4, R.id.i5, R.id.i6};
            int[] textViewIds = {R.id.t1, R.id.t2, R.id.t3, R.id.t4, R.id.t5, R.id.t6};
            int colorFilter = isNightMode() ? 0xFF8DCDFF : 0xFF006493;
            int textColorFilter = isNightMode() ? 0xFFDDDDDD : 0xFF222222;
            for (int imageViewId : imageViewIds) {
                ImageView imageView = (ImageView) bsId(imageViewId);
                imageView.setColorFilter(colorFilter, PorterDuff.Mode.SRC_IN);
            }
            for (int textViewId : textViewIds) {
                TextView textView = (TextView) bsId(textViewId);
                textView.setTextColor(textColorFilter);
            }
            title.setTextColor(isNightMode() ? 0xFFFFFFFF : 0xFF000000);
            showBottomSheetDialog();
        });
        circleImageView.setOnClickListener(v -> {
            ArrayList<String> icon_urd = new ArrayList<>();
            icon_urd.add(getUserConfig().getProfileUrl());
            new StfalconImageViewer.Builder<>(getParentActivity(), icon_urd, (imageView, image) -> Glide.with(ProfileActivity.this).load(image).transition(DrawableTransitionOptions.withCrossFade()).placeholder(new ColorDrawable(0xffD3D3D3)).into(imageView)).withStartPosition(0).withHiddenStatusBar(true).allowZooming(true).withDismissListener(() -> circleImageView.setVisibility(View.VISIBLE)).allowSwipeToDismiss(true).withTransitionFrom(circleImageView).show();

        });
        setLayoutManager(all_projects, premium_r, verify_r, editor_r, most_r, private_projects_r);
        hideViews(findViewById(R.id.premium_layout), findViewById(R.id.verified), findViewById(R.id.editor), findViewById(R.id.private_l), findViewById(R.id.most), findViewById(R.id.all_p));
        delayTask(() -> {
            if (new SharedPreferencesManager(getParentActivity()).getBoolean("ads", true)) {
                new MobileAdsLoader(getParentActivity()).builtConfig().loadNativeAd(findViewById(R.id.nativeTemplateView), refer);
            }
        });
    }

    private void setLayoutManager(RecyclerView... recyclerView) {
        for (RecyclerView recycle : recyclerView) {
            recycle.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            if (recycle != all_projects) {
                recycle.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            } else {
                recycle.setLayoutManager(new GridLayoutManager(this, 3));
            }
        }
    }

    private void setLoading() {
        LoadingCell cell = new LoadingCell(this);
        cell.toggle(true);
        loading.addView(cell);
        toggle(true);
    }

    private void toggle(boolean bool) {
        TransitionManager.beginDelayedTransition(refer);
        if (bool) {
            showViews(loading);
            hideViews(content, share, logout, edit);
        } else {
            showViews(content, share, logout, edit);
            hideViews(loading);
        }
    }

    private void setUpDivider() {
        NestedScrollView nestedScrollView = findViewById(R.id.nested_scroll_view);
        divider = findViewById(R.id.divider);
        divider.setVisibility(View.GONE);
        nestedScrollView.setVerticalScrollBarEnabled(false);
        nestedScrollView.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> divider.setVisibility(scrollY > 0 ? View.VISIBLE : View.GONE));
    }

    public void performTransition(View view, double durationMillis) {
        LinearLayout viewGroup = (LinearLayout) view;
        AutoTransition transition = new AutoTransition();
        transition.setDuration((long) durationMillis);
        transition.setInterpolator(new DecelerateInterpolator());
        TransitionManager.beginDelayedTransition(viewGroup, transition);
    }


}