package in.afi.codekosh.activity.project;

import static jb.openware.app.net.DownloadServiceKt.downloadFile;
import static jb.openware.app.net.DownloadServiceKt.downloadFile2;
import static jb.openware.app.util.StringUtilsKt.moderatorUrl;
import static jb.openware.app.util.StringUtilsKt.websiteUrl;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.net.Uri;
import android.os.Environment;
import android.text.util.Linkify;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.AutoTransition;
import androidx.transition.TransitionManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.stfalcon.imageviewer.StfalconImageViewer;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Objects;

import in.afi.codekosh.R;
import in.afi.codekosh.activity.drawer.UploadActivity;
import in.afi.codekosh.activity.other.LikesCountActivity;
import in.afi.codekosh.activity.profile.ProfileActivity;
import in.afi.codekosh.components.FirebaseUtils;
import in.afi.codekosh.components.MenuCreator;
import in.afi.codekosh.components.SharedPreferencesManager;
import in.afi.codekosh.model.Project;
import in.afi.codekosh.nativeAds.MobileAdsLoader;
import jb.openware.app.util.net.DownloadCallback;
import in.afi.codekosh.tools.AndroidUtils;
import in.afi.codekosh.tools.BaseFragment;
import in.afi.codekosh.tools.ThemeBuilder;

public class ProjectViewActivity extends BaseFragment {
    private final FirebaseDatabase firebase = FirebaseDatabase.getInstance();
    private final DatabaseReference project = firebase.getReference("projects/normal");
    private final DatabaseReference premium = firebase.getReference("projects/premium");
    private final DatabaseReference like = firebase.getReference("likes");
    private final DatabaseReference comment = firebase.getReference("comments");
    private final DatabaseReference Users = firebase.getReference("Users");
    private final DatabaseReference suggestion = firebase.getReference("Report/project");
    private final String getID = "";
    private final String temp = "";
    private String dir = "";
    private ArrayList<String> data_screenshots = new ArrayList<>();
    private HashMap<String, Object> like_map = new HashMap<>();
    private HashMap<String, Object> comment_map = new HashMap<>();
    private String key = "";
    private String uid = "";
    private String like_key = "";
    private String download_url = "";
    private String name = "";
    private View divider;
    private ChildEventListener _project_child_listener, _premium_child_listener, _like_child_listener, _comment_child_listener, _Users_child_listener;
    private Intent intent;
    private boolean liked = false;
    private double comment_num = 0;
    private double like_num = 0;
    private SharedPreferences developer;
    private ImageView back, like_btn, options, icon;
    private TextView title, username, likes, comments, download_txt, type1, content, category_text, project_Type_text;
    private LinearLayout likes_lin, editor_choice, verified, category, project_type, download_btn, read_more, list, comments_lin, private_project, premium_project;
    private Project project_data;
    private FloatingActionButton fab;
    private HashMap<String, Object> update_map = new HashMap<>();
    private ArrayList<String> moderator_id = new ArrayList<>();
    private int downloadID;
    private ProgressButton progressButton;
    private DownloadCallback callback;
    private InterstitialAd interstitialAd;

    @Override
    protected boolean isHomeFragment() {
        return false;
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_project_view;
    }

    @Override
    public void getThemeDescriptions(ThemeBuilder themeBuilder) {
        themeBuilder.setImageColorFilter(back, BLACK, WHITE);
        themeBuilder.setImageColorFilter(options, BLACK, WHITE);
        themeBuilder.setTextColor(title, BLACK, WHITE);
        themeBuilder.setTextColor(username, 0xFF006685, 0xFF6AD3FF);
        themeBuilder.setTextColor(project_Type_text, BLACK, WHITE);
        themeBuilder.setTextColor(type1, BLACK, WHITE);
        themeBuilder.setTextColor(findViewById(R.id.read_more_text), BLACK, WHITE);
        themeBuilder.setTextColor(findViewById(R.id.textview11), BLACK, WHITE);
        themeBuilder.setTextColor(comments, BLACK, WHITE);
        themeBuilder.setTextColor(content, 0xFF181818, TEXT_WHITE);
        themeBuilder.setTextColor(category_text, BLACK, WHITE);
        themeBuilder.setLinearLayoutBackgroundColor(download_btn, 0xFF006493, 0xFF006493);
        if (themeBuilder.isNightMode()) {
            divider.setBackgroundColor(ContextCompat.getColor(this, R.color.divider_color_night));
            findViewById(R.id.linear28).setBackground(createGradientDrawable(30, 2, 0xFF41474D, Color.TRANSPARENT));
            category.setBackground(createGradientDrawable(50, 2, 0xFF9E9E9E, Color.TRANSPARENT));
        } else {
            divider.setBackgroundColor(ContextCompat.getColor(this, R.color.divider_color));
            category.setBackground(createGradientDrawable(50, 2, 0xFF9E9E9E, 0xFFFFFFFF));
            findViewById(R.id.linear28).setBackground(createGradientDrawable(30, 2, 0xFF000000, Color.TRANSPARENT));
        }
    }

    @Override
    protected void initialize() {
        // ImageView
        back = findViewById(R.id.back);
        like_btn = findViewById(R.id.like_btn);
        options = findViewById(R.id.options);
        icon = findViewById(R.id.icon);


        // TextView
        title = findViewById(R.id.title);
        username = findViewById(R.id.username);
        likes = findViewById(R.id.likes);
        category = findViewById(R.id.category);
        project_type = findViewById(R.id.project_type);
        type1 = findViewById(R.id.type1);
        content = findViewById(R.id.content);
        comments = findViewById(R.id.comments);
        category_text = findViewById(R.id.category_text);
        project_Type_text = findViewById(R.id.project_Type_text);


        // LinearLayout
        likes_lin = findViewById(R.id.likes_lin);
        editor_choice = findViewById(R.id.editor_choice);
        verified = findViewById(R.id.verified);
        download_btn = findViewById(R.id.download_btn);
        read_more = findViewById(R.id.read_more);
        list = findViewById(R.id.list);
        comments_lin = findViewById(R.id.comments_lin);
        private_project = findViewById(R.id.private_project);
        premium_project = findViewById(R.id.premium_project);

        // Other
        fab = findViewById(R.id._fab);
        progressButton = new ProgressButton(this);


        developer = getSharedPreferences("developer", Activity.MODE_PRIVATE);
        likes_lin.setBackground(createGradientDrawable(50, 0, Color.TRANSPARENT, 0xFFFF5722));
        editor_choice.setBackground(createGradientDrawable(50, 0, Color.TRANSPARENT, 0xFF2196F3));
        private_project.setBackground(createGradientDrawable(50, 0, Color.TRANSPARENT, 0xFFAA47BC));
        premium_project.setBackground(createGradientDrawable(50, 0, Color.TRANSPARENT, 0xFF009688));
        verified.setBackground(createGradientDrawable(50, 0, Color.TRANSPARENT, 0xFF4CAF50));
        project_type.setBackground(createGradientDrawable(50, 2, 0xFF9E9E9E, Color.TRANSPARENT));
        progressButton.setText("Free".equals(developer.getString("type", "")) ? "Download" : "Get");
        progressButton.setProgressColor(0xFFFFFFFF);
        progressButton.setTextColor(WHITE);
        hideViews(fab);


        removeScrollbar(findViewById(R.id.scroll2));
        RecyclerView recycler_screenshots = new RecyclerView(this);
        recycler_screenshots.setNestedScrollingEnabled(false);
        recycler_screenshots.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        recycler_screenshots.setAdapter(new Video_listAdapter(data_screenshots));
        list.addView(recycler_screenshots);


        // Listeners

        getConnectionsManager(moderatorUrl, GET, new RequestListener() {
            @Override
            public void onResponse(String tag, String response, HashMap<String, Object> responseHeaders) {
                ArrayList<HashMap<String, Object>> data = new Gson().fromJson(response, new TypeToken<ArrayList<HashMap<String, Object>>>() {
                }.getType());
                if (!data.isEmpty()) {
                    moderator_id = new ArrayList<>();
                    for (HashMap<String, Object> item : data) {
                        String uidValue = (String) item.get("uid");
                        if (uidValue != null) {
                            moderator_id.add(uidValue);
                        }
                    }
                }
            }

            @Override
            public void onErrorResponse(String tag, String message) {

            }
        });

        _project_child_listener = new ChildEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onChildAdded(@NonNull DataSnapshot _param1, String _param2) {
                GenericTypeIndicator<HashMap<String, Object>> _ind = new GenericTypeIndicator<HashMap<String, Object>>() {
                };
                final String _childKey = _param1.getKey();
                final HashMap<String, Object> _childValue = _param1.getValue(_ind);
                String developerType = developer.getString("type", "");
                if ("Free".equals(developerType) && _childKey != null && _childKey.equals(key) && _childValue != null) {
                    hideViews(premium_project);
                    update_map = _childValue;
                    project_data = new Project(_childValue);
                    if (!isDestroyed()) {
                        Glide.with(ProjectViewActivity.this).load(Uri.parse(project_data.getIcon())).into(icon);
                    }
                    title.setText(project_data.getTitle());
                    username.setText(String.valueOf(_childValue.get("name")));
                    delayTask(() -> {
                        transitionManager(download_btn, 400);
                        if (Objects.equals(project_data.getProjectType(), "Sketchware Pro(Mod)")) {
                            progressButton.setText("Download SWB ( ".concat(project_data.getSize().concat(" )")));
                        } else {
                            progressButton.setText("Download Project ( ".concat(project_data.getSize().concat(" )")));
                        }

                    }, 700);
                    likes.setText(formatNumber(project_data.getLikes()));
                    comments.setText(formatNumber(project_data.getComments()));
                    download_url = project_data.getDownloadUrl();
                    if (project_data.getWhatsNew().equals("none")) {
                        type1.setText("About Project");
                        TextFormatter.formatText(content, project_data.getDescription());
                    } else {
                        type1.setText("What's new");
                        TextFormatter.formatText(content, project_data.getWhatsNew());
                    }
                    detectLinks(content);
                    category_text.setText(project_data.getCategory());
                    project_Type_text.setText(project_data.getProjectType());
                    verified.setVisibility(project_data.isVerified() ? View.VISIBLE : View.GONE);
                    editor_choice.setVisibility(project_data.isEditorsChoice() ? View.VISIBLE : View.GONE);
                    comments_lin.setVisibility(project_data.isCommentsVisible() ? View.VISIBLE : View.GONE);
                    private_project.setVisibility(!project_data.isVisible() ? View.VISIBLE : View.GONE);
                    data_screenshots = project_data.getScreenshots();
                    recycler_screenshots.setAdapter(new Video_listAdapter(data_screenshots));

                }
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onChildChanged(@NonNull DataSnapshot _param1, String _param2) {
                GenericTypeIndicator<HashMap<String, Object>> _ind = new GenericTypeIndicator<HashMap<String, Object>>() {
                };
                final String _childKey = _param1.getKey();
                final HashMap<String, Object> _childValue = _param1.getValue(_ind);
                String developerType = developer.getString("type", "");
                if ("Free".equals(developerType) && _childKey != null && _childKey.equals(key) && _childValue != null) {
                    hideViews(premium_project);
                    update_map = _childValue;
                    project_data = new Project(_childValue);
                    if (!isDestroyed()) {
                        Glide.with(ProjectViewActivity.this).load(Uri.parse(project_data.getIcon())).into(icon);
                    }
                    title.setText(project_data.getTitle());
                    username.setText(String.valueOf(_childValue.get("name")));
                    delayTask(() -> {
                        transitionManager(download_btn, 400);
                        if (Objects.equals(project_data.getProjectType(), "Sketchware Pro(Mod)")) {
                            progressButton.setText("Download SWB ( ".concat(project_data.getSize().concat(" )")));
                        } else {
                            progressButton.setText("Download Project ( ".concat(project_data.getSize().concat(" )")));
                        }

                    }, 700);
                    likes.setText(formatNumber(project_data.getLikes()));
                    comments.setText(formatNumber(project_data.getComments()));
                    download_url = project_data.getDownloadUrl();
                    if (project_data.getWhatsNew().equals("none")) {
                        type1.setText("About Project");
                        TextFormatter.formatText(content, project_data.getDescription());
                    } else {
                        type1.setText("What's new");
                        TextFormatter.formatText(content, project_data.getWhatsNew());
                    }
                    detectLinks(content);
                    category_text.setText(project_data.getCategory());
                    project_Type_text.setText(project_data.getProjectType());
                    verified.setVisibility(project_data.isVerified() ? View.VISIBLE : View.GONE);
                    editor_choice.setVisibility(project_data.isEditorsChoice() ? View.VISIBLE : View.GONE);
                    comments_lin.setVisibility(project_data.isCommentsVisible() ? View.VISIBLE : View.GONE);
                    private_project.setVisibility(!project_data.isVisible() ? View.VISIBLE : View.GONE);
                    data_screenshots = project_data.getScreenshots();
                    recycler_screenshots.setAdapter(new Video_listAdapter(data_screenshots));

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
        project.addChildEventListener(_project_child_listener);

        _premium_child_listener = new ChildEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onChildAdded(@NonNull DataSnapshot _param1, String _param2) {
                GenericTypeIndicator<HashMap<String, Object>> _ind = new GenericTypeIndicator<HashMap<String, Object>>() {
                };
                final String _childKey = _param1.getKey();
                final HashMap<String, Object> _childValue = _param1.getValue(_ind);
                String developerType = developer.getString("type", "");
                if ("Paid".equals(developerType) && _childKey != null && _childKey.equals(key) && _childValue != null) {
                    showViews(premium_project);
                    update_map = _childValue;
                    project_data = new Project(_childValue);
                    if (!isDestroyed()) {
                        Glide.with(ProjectViewActivity.this).load(Uri.parse(project_data.getIcon())).into(icon);
                    }
                    title.setText(project_data.getTitle());
                    username.setText(String.valueOf(_childValue.get("name")));
                    delayTask(() -> {
                        transitionManager(download_btn, 400);
                        if (Objects.equals(project_data.getProjectType(), "Sketchware Pro(Mod)")) {
                            progressButton.setText("Get SWB ( ".concat(project_data.getSize().concat(" )")));
                        } else {
                            progressButton.setText("Get Project ( ".concat(project_data.getSize().concat(" )")));
                        }

                    }, 700);
                    likes.setText(formatNumber(project_data.getLikes()));
                    comments.setText(formatNumber(project_data.getComments()));
                    download_url = project_data.getDownloadUrl();
                    if (project_data.getWhatsNew().equals("none")) {
                        type1.setText("About Project");
                        TextFormatter.formatText(content, project_data.getDescription());
                    } else {
                        type1.setText("What's new");
                        TextFormatter.formatText(content, project_data.getWhatsNew());
                    }
                    detectLinks(content);
                    category_text.setText(project_data.getCategory());
                    project_Type_text.setText(project_data.getProjectType());
                    verified.setVisibility(project_data.isVerified() ? View.VISIBLE : View.GONE);
                    editor_choice.setVisibility(project_data.isEditorsChoice() ? View.VISIBLE : View.GONE);
                    comments_lin.setVisibility(project_data.isCommentsVisible() ? View.VISIBLE : View.GONE);
                    private_project.setVisibility(!project_data.isVisible() ? View.VISIBLE : View.GONE);
                    data_screenshots = project_data.getScreenshots();
                    recycler_screenshots.setAdapter(new Video_listAdapter(data_screenshots));
                }
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onChildChanged(@NonNull DataSnapshot _param1, String _param2) {
                GenericTypeIndicator<HashMap<String, Object>> _ind = new GenericTypeIndicator<HashMap<String, Object>>() {
                };
                final String _childKey = _param1.getKey();
                final HashMap<String, Object> _childValue = _param1.getValue(_ind);
                String developerType = developer.getString("type", "");
                if ("Paid".equals(developerType) && _childKey != null && _childKey.equals(key) && _childValue != null) {
                    showViews(premium_project);
                    update_map = _childValue;
                    project_data = new Project(_childValue);
                    if (!isDestroyed()) {
                        Glide.with(ProjectViewActivity.this).load(Uri.parse(project_data.getIcon())).into(icon);
                    }
                    title.setText(project_data.getTitle());
                    username.setText(String.valueOf(_childValue.get("name")));
                    delayTask(() -> {
                        transitionManager(download_btn, 400);
                        if (Objects.equals(project_data.getProjectType(), "Sketchware Pro(Mod)")) {
                            progressButton.setText("Get SWB ( ".concat(project_data.getSize().concat(" )")));
                        } else {
                            progressButton.setText("Get Project ( ".concat(project_data.getSize().concat(" )")));
                        }

                    }, 700);
                    likes.setText(formatNumber(project_data.getLikes()));
                    comments.setText(formatNumber(project_data.getComments()));
                    download_url = project_data.getDownloadUrl();
                    if (project_data.getWhatsNew().equals("none")) {
                        type1.setText("About Project");
                        TextFormatter.formatText(content, project_data.getDescription());
                    } else {
                        type1.setText("What's new");
                        TextFormatter.formatText(content, project_data.getWhatsNew());
                    }
                    detectLinks(content);
                    category_text.setText(project_data.getCategory());
                    project_Type_text.setText(project_data.getProjectType());
                    verified.setVisibility(project_data.isVerified() ? View.VISIBLE : View.GONE);
                    editor_choice.setVisibility(project_data.isEditorsChoice() ? View.VISIBLE : View.GONE);
                    comments_lin.setVisibility(project_data.isCommentsVisible() ? View.VISIBLE : View.GONE);
                    private_project.setVisibility(!project_data.isVisible() ? View.VISIBLE : View.GONE);
                    data_screenshots = project_data.getScreenshots();
                    recycler_screenshots.setAdapter(new Video_listAdapter(data_screenshots));
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
        premium.addChildEventListener(_premium_child_listener);

        _Users_child_listener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot _param1, String _param2) {
                GenericTypeIndicator<HashMap<String, Object>> _ind = new GenericTypeIndicator<HashMap<String, Object>>() {
                };
                final String _childKey = _param1.getKey();
                final HashMap<String, Object> _childValue = _param1.getValue(_ind);
                if (_childKey != null && _childKey.equals(uid) && _childValue != null && _childValue.containsKey("name")) {
                    String n = String.valueOf(_childValue.get("name"));
                    username.setText(n);
                    name = n;
                }


            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot _param1, String _param2) {
                GenericTypeIndicator<HashMap<String, Object>> _ind = new GenericTypeIndicator<HashMap<String, Object>>() {
                };
                final String _childKey = _param1.getKey();
                final HashMap<String, Object> _childValue = _param1.getValue(_ind);
                if (_childKey != null && _childKey.equals(uid) && _childValue != null && _childValue.containsKey("name")) {
                    String n = String.valueOf(_childValue.get("name"));
                    username.setText(n);
                    name = n;
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
        Users.addChildEventListener(_Users_child_listener);

        _comment_child_listener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot _param1, String _param2) {
                GenericTypeIndicator<HashMap<String, Object>> _ind = new GenericTypeIndicator<HashMap<String, Object>>() {
                };
                final HashMap<String, Object> _childValue = _param1.getValue(_ind);
                if (_childValue != null) {
                    if (_childValue.containsKey("post_key")) {
                        if (developer.getString("type", "").equals("Free")) {
                            if (key.equals(String.valueOf(_childValue.get("post_key")))) {
                                comment_num++;
                                comment_map = new HashMap<>();
                                comment_map.put("comments", String.valueOf((long) (comment_num)));
                                project.child(key).updateChildren(comment_map);
                                comment_map.clear();
                            }
                        }
                        if (developer.getString("type", "").equals("Paid")) {
                            if (key.equals(String.valueOf(_childValue.get("post_key")))) {
                                comment_num++;
                                comment_map = new HashMap<>();
                                comment_map.put("comments", String.valueOf((long) (comment_num)));
                                premium.child(key).updateChildren(comment_map);
                                comment_map.clear();
                            }
                        }
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
                GenericTypeIndicator<HashMap<String, Object>> _ind = new GenericTypeIndicator<HashMap<String, Object>>() {
                };
                final HashMap<String, Object> _childValue = _param1.getValue(_ind);
                if (_childValue != null) {
                    if (_childValue.containsKey("post_key")) {
                        if (developer.getString("type", "").equals("Free")) {
                            if (key.equals(String.valueOf(_childValue.get("post_key")))) {
                                comment_num--;
                                comment_map = new HashMap<>();
                                comment_map.put("comments", String.valueOf((long) (comment_num)));
                                project.child(key).updateChildren(comment_map);
                                comment_map.clear();
                            }
                        }
                        if (developer.getString("type", "").equals("Paid")) {
                            if (key.equals(String.valueOf(_childValue.get("post_key")))) {
                                comment_num--;
                                comment_map = new HashMap<>();
                                comment_map.put("comments", String.valueOf((long) (comment_num)));
                                premium.child(key).updateChildren(comment_map);
                                comment_map.clear();
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError _param1) {

            }
        };
        comment.addChildEventListener(_comment_child_listener);

        _like_child_listener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot _param1, String _param2) {
                GenericTypeIndicator<HashMap<String, Object>> _ind = new GenericTypeIndicator<HashMap<String, Object>>() {
                };
                final HashMap<String, Object> _childValue = _param1.getValue(_ind);
                if (_childValue != null && (_childValue.containsKey("key") && _childValue.containsKey("value")) && _childValue.containsKey("uid")) {
                    if (developer.getString("type", "").equals("Free")) {
                        if (key.equals(String.valueOf(_childValue.get("key"))) && String.valueOf(_childValue.get("value")).equals("true")) {
                            like_num++;
                            like_map = new HashMap<>();
                            like_map.put("likes", String.valueOf((long) (like_num)));
                            project.child(key).updateChildren(like_map);
                            like_map.clear();
                        }
                        if (key.equals(String.valueOf(_childValue.get("key"))) && String.valueOf(_childValue.get("uid")).equals(getUID())) {
                            if (String.valueOf(_childValue.get("value")).equals("true")) {
                                like_btn.setImageResource(R.drawable.like_fill);
                                liked = true;
                            } else {
                                like_btn.setImageResource(R.drawable.like);
                                liked = false;
                            }
                        }
                    }
                    if (developer.getString("type", "").equals("Paid")) {
                        if (key.equals(String.valueOf(_childValue.get("key"))) && String.valueOf(_childValue.get("value")).equals("true")) {
                            like_num++;
                            like_map = new HashMap<>();
                            like_map.put("likes", String.valueOf((long) (like_num)));
                            premium.child(key).updateChildren(like_map);
                            like_map.clear();
                        }
                        if (key.equals(String.valueOf(_childValue.get("key"))) && String.valueOf(_childValue.get("uid")).equals(getUID())) {
                            if (String.valueOf(_childValue.get("value")).equals("true")) {
                                like_btn.setImageResource(R.drawable.like_fill);
                                liked = true;
                            } else {
                                like_btn.setImageResource(R.drawable.like);
                                liked = false;
                            }
                        }
                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot _param1, String _param2) {
                GenericTypeIndicator<HashMap<String, Object>> _ind = new GenericTypeIndicator<HashMap<String, Object>>() {
                };
                final HashMap<String, Object> _childValue = _param1.getValue(_ind);
                if (developer.getString("type", "").equals("Free")) {
                    if (_childValue != null && String.valueOf(_childValue.get("key")).equals(key)) {
                        if (String.valueOf(_childValue.get("value")).equals("true")) {
                            like_num++;
                        } else {
                            like_num--;
                        }
                        like_map = new HashMap<>();
                        like_map.put("likes", String.valueOf((long) (like_num)));
                        project.child(key).updateChildren(like_map);
                        like_map.clear();
                    }
                    if (_childValue != null && String.valueOf(_childValue.get("key")).equals(key) && String.valueOf(_childValue.get("uid")).equals(getUID())) {
                        if (String.valueOf(_childValue.get("value")).equals("true")) {
                            like_btn.setImageResource(R.drawable.like_fill);
                            liked = true;
                        } else {
                            like_btn.setImageResource(R.drawable.like);
                            liked = false;
                        }
                    }
                }
                if (developer.getString("type", "").equals("Paid")) {
                    if (_childValue != null && String.valueOf(_childValue.get("key")).equals(key)) {
                        if (String.valueOf(_childValue.get("value")).equals("true")) {
                            like_num++;
                        } else {
                            like_num--;
                        }
                        like_map = new HashMap<>();
                        like_map.put("likes", String.valueOf((long) (like_num)));
                        premium.child(key).updateChildren(like_map);
                        like_map.clear();
                    }
                    if (_childValue != null && String.valueOf(_childValue.get("key")).equals(key) && String.valueOf(_childValue.get("uid")).equals(getUID())) {
                        if (String.valueOf(_childValue.get("value")).equals("true")) {
                            like_btn.setImageResource(R.drawable.like_fill);
                            liked = true;
                        } else {
                            like_btn.setImageResource(R.drawable.like);
                            liked = false;
                        }
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
        like.addChildEventListener(_like_child_listener);

    }

    public void showMenu(View v) {
        MenuCreator menuCreator = new MenuCreator(this, v);
        if (moderator_id.contains(getUID()) || getUID().equals(uid)) {
            menuCreator.addItem(Menu.NONE, Menu.FIRST, Menu.NONE, "Delete");
        }
        menuCreator.addItem(Menu.NONE, Menu.FIRST + 1, Menu.NONE, "Report");
        menuCreator.addItem(Menu.NONE, Menu.FIRST + 2, Menu.NONE, "Share");
        menuCreator.setIcon(Menu.FIRST, R.drawable.delete);
        menuCreator.setIcon(Menu.FIRST + 1, R.drawable.report);
        menuCreator.setIcon(Menu.FIRST + 2, R.drawable.share);
        menuCreator.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case Menu.FIRST + 1:
                    report();
                    return true;
                case Menu.FIRST:
                    delete();
                    return true;
                case Menu.FIRST + 2:
                    share();
                    return true;

                default:
                    return false;
            }
        });
        menuCreator.show();
    }

    private void delete() {
        MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(this, com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog_Centered);
        dialogBuilder.setIcon(R.drawable.delete);
        dialogBuilder.setTitle("Delete");
        dialogBuilder.setMessage("Do you really want to delete this project? This action cannot be undone.");
        dialogBuilder.setPositiveButton("Delete", (dialog, which) -> syncTask(new SyncTaskListener() {
            @Override
            public void beforeTaskStart() {
                showProgressDialog();
            }

            @Override
            public void onBackground() {
                new FirebaseUtils().deleteFileFromStorageByUrl(project_data.getIcon(), project_data.getDownloadUrl());
                for (String screenshot : project_data.getScreenshots()) {
                    new FirebaseUtils().deleteFileFromStorageByUrl(screenshot);
                }
                if (developer.getString("type", "").equals("Free")) {
                    project.child(key).removeValue();
                } else {
                    premium.child(key).removeValue();
                }
            }

            @Override
            public void onTaskComplete() {
                dismissProgressDialog();
                new FirebaseUtils().decreaseUserKeyData("projects", getUID());
                alertCreator("Project Deleted successfully.", (dialog1, which1) -> goBack());
            }
        }));
        dialogBuilder.setNegativeButton("Cancel", null);
        dialogBuilder.show();
    }

    private void share() {
        String baseUrl = websiteUrl + (developer.getString("type", "").equals("Free") ? "p/n/" : "p/p/");
        getProjectId(key, !developer.getString("type", "").equals("Free"), value -> AndroidUtils.shareText(baseUrl + value, ProjectViewActivity.this));
    }

    @SuppressLint("SimpleDateFormat")
    private void report() {
        createBottomSheetDialog(R.layout.report);
        TextView h1 = (TextView) bsId(R.id.h1);
        TextView t1 = (TextView) bsId(R.id.t1);
        final EditText enterText = (EditText) bsId(R.id.edittext1);
        enterText.setFocusableInTouchMode(true);
        if (isNightMode()) {
            h1.setTextColor(WHITE);
            t1.setTextColor(TEXT_WHITE);
        } else {
            h1.setTextColor(BLACK);
            t1.setTextColor(0xFF424242);
        }
        MaterialButton b1 = (MaterialButton) bsId(R.id.b1);
        b1.setOnClickListener(v -> {
            String edittext_value = enterText.getText().toString();
            if (AndroidUtils.isConnected(getParentActivity())) {
                if (edittext_value.length() > 5) {
                    Calendar cal = Calendar.getInstance();
                    String report_key = suggestion.push().getKey();
                    HashMap<String, Object> report_map = new HashMap<>();
                    report_map.put("project_name", title.getText().toString());
                    report_map.put("message", edittext_value);
                    report_map.put("project_key", key);
                    report_map.put("project_uid", uid);
                    report_map.put("key", report_key);
                    report_map.put("type", developer.getString("type", ""));
                    report_map.put("mode", "Report");
                    report_map.put("time", new SimpleDateFormat("dd-MM-yyyy hh:mm:ss").format(cal.getTime()));
                    report_map.put("user_name", getUserConfig().getName());
                    report_map.put("user_email", getUserConfig().getEmail());
                    report_map.put("user_uid", getUID());
                    suggestion.child(report_key).updateChildren(report_map);
                    report_map.clear();
                    alertToast("We Will Review Your Report");
                    dismissBottomSheetDialog();
                } else {
                    showToast("Please Explain Your Report");
                }
            } else {
                alertToast("No Internet");
            }
        });
        showBottomSheetDialog();
    }

    @Override
    public void onResume() {
        super.onResume();
        delayTask(() -> {
            AdView adv = findViewById(R.id.adv);
            if (new SharedPreferencesManager(getParentActivity()).getBoolean("ads", true)) {
                MobileAds.initialize(getParentActivity(), initializationStatus -> {
                });
                RequestConfiguration configuration = new RequestConfiguration.Builder().setTestDeviceIds(Collections.singletonList(new MobileAdsLoader(getParentActivity()).testDeviceId)).build();
                MobileAds.setRequestConfiguration(configuration);
                adv.loadAd(new AdRequest.Builder().build());
                showViews(adv);
            } else {
                hideViews(adv);
            }
        });

    }

    private void loadInterstitialAd() {
        AdRequest adRequest = new AdRequest.Builder().build();

        InterstitialAd.load(getParentActivity(), "ca-app-pub-8844795823361502/1912228374", adRequest, new InterstitialAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull InterstitialAd interstitialAds) {
                interstitialAd = interstitialAds;
                interstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                    @Override
                    public void onAdDismissedFullScreenContent() {
                        // Called when fullscreen content is dismissed.

                    }

                    @Override
                    public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                        // Called when fullscreen content failed to show.
                    }

                    @Override
                    public void onAdShowedFullScreenContent() {
                        loadInterstitialAd();
                        interstitialAd = null;
                    }
                });
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                interstitialAd = null;
            }
        });
    }

    @Override
    protected void initializeLogic() {
        setUpDivider();
        key = getIntent().getStringExtra("key");
        uid = getIntent().getStringExtra("uid");
        like_num = 0;
        comment_num = 0;
        liked = false;
        like_key = key.concat(getUID());
        if (getUID().equals(uid)) {
            fab.setVisibility(View.VISIBLE);
        }
        hideViews(findViewById(R.id.linear5), findViewById(R.id.photos));
        delayTask(() -> {
            transitionManager(findViewById(R.id.linear1), 400);
            showViews(findViewById(R.id.linear5), findViewById(R.id.photos));
        });
        options.setOnClickListener(this::showMenu);
        loadInterstitialAd();
        back.setOnClickListener(v -> goBack());
        comments_lin.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setClass(this, CommentsActivity.class);
            intent.putExtra("title", project_data.getTitle());
            intent.putExtra("key", key);
            intent.putExtra("uid", uid);
            startActivity(intent);
        });
        read_more.setOnClickListener(v -> {
            Intent intent1 = new Intent(this, AboutActivity.class);
            HashMap<String, Object> dataMap = new HashMap<>();
            dataMap.put("title", project_data.getTitle());
            dataMap.put("isSketchwarePro", project_data.getSketchwareProVersion());
            dataMap.put("project_type", project_data.getProjectType());
            dataMap.put("whats_new", project_data.getWhatsNew());
            dataMap.put("description", project_data.getDescription());
            dataMap.put("downloads", project_data.getDownloads());
            dataMap.put("time", project_data.getTime());
            dataMap.put("update_time", project_data.getUpdateTime());
            dataMap.put("uid", uid);
            intent1.putExtra("data", dataMap);
            startActivity(intent1);

        });
        fab.setOnClickListener(v -> {
            intent = new Intent(this, UploadActivity.class);
            intent.putExtra("hashmap", update_map);
            startActivity(intent);
        });
        detectLinks(content);
        category.setOnClickListener(v -> tags("category"));
        editor_choice.setOnClickListener(v -> tags("editors_choice"));
        project_type.setOnClickListener(v -> tags("project_type"));
        verified.setOnClickListener(v -> tags("verify"));
        premium_project.setOnClickListener(v -> tags("premium"));
        username.setOnClickListener(v -> {
            if (AndroidUtils.isConnected(this)) {
                if (!uid.equals("")) {
                    developer.edit().putString("uid", uid).apply();
                    startActivity(ProfileActivity.class);
                } else {
                    alertToast("Something Went Wrong");
                }
            } else {
                alertToast("No Internet");

            }
        });
        likes_lin.setOnClickListener(v -> {
            Intent intent1 = new Intent(ProjectViewActivity.this, LikesCountActivity.class);
            intent1.putExtra("key", key);
            startActivity(intent1);
        });
        like_btn.setOnClickListener(v -> {
            clickAnimation(like_btn);
            performHapticFeedback(like_btn);
            if (AndroidUtils.isConnected(this)) {
                if (liked) {
                    like_map = new HashMap<>();
                    like_map.put("value", "false");
                    like_map.put("key", key);
                    like_map.put("like_key", like_key);
                    like_map.put("uid", getUID());
                    like.child(like_key).updateChildren(like_map).addOnSuccessListener(unused -> new FirebaseUtils().decreaseUserKeyData("likes", uid));
                } else {
                    like_map = new HashMap<>();
                    like_map.put("value", "true");
                    like_map.put("key", key);
                    like_map.put("like_key", like_key);
                    like_map.put("uid", getUID());
                    like.child(like_key).updateChildren(like_map).addOnSuccessListener(unused -> new FirebaseUtils().increaseUserKeyData("likes", uid));
                }
            } else {
                alertToast("No Internet Connection");
            }
        });
        callback = new DownloadCallback() {
            @Override
            public void onDownloadStart() {

            }

            @Override
            public void onProgressUpdate(int progress) {
                progressButton.setProgress(progress);
            }

            @Override
            public void onDownloadComplete() {
                new FirebaseUtils().increaseUserKeyData("downloads", uid);
                String separator = " -> ";
                String downloadPath = "/Download";
                int indexOfDownload = dir.indexOf(downloadPath);
                if (project_data.getProjectType().equals("Sketchware")) {
                    syncTask(new SyncTaskListener() {
                        @Override
                        public void beforeTaskStart() {
                            progressButton.showProgress(false);
                            progressButton.setText("Importing project");
                        }

                        @Override
                        public void onBackground() {
                            new SketchwareUtils().importProject(dir);
                        }

                        @Override
                        public void onTaskComplete() {
                            new SketchwareUtils().deleteTemp2();
                            progressButton.setText("Importing completed.");
                        }
                    });

                } else if (project_data.getProjectType().equals("Sketchware Pro(Mod)")) {
                    String f = getAppConfig().getSwbInstaller();
                    if (f.equals("sketchware")) {
                        progressButton.showProgress(false);
                        progressButton.setText("Download completed.");
                        new Swb_restore(getParentActivity()).openSwb(dir);
                    } else if (f.equals("built_in")) {
                        SyncTaskListener listener = new SyncTaskListener() {
                            @Override
                            public void beforeTaskStart() {
                                progressButton.showProgress(false);
                                progressButton.setText("Importing project");
                            }

                            @Override
                            public void onBackground() {
                                Swb_restore.selectSWB(dir, ProjectViewActivity.this);
                            }

                            @Override
                            public void onTaskComplete() {
                                progressButton.setText("Importing completed.");
                            }
                        };

                        syncTask(listener);
                    } else {
                        if (indexOfDownload != -1) {
                            String pathAfterDownload = dir.substring(indexOfDownload + downloadPath.length() + 1);
                            String xyz = pathAfterDownload.replace("/", separator);
                            new MaterialAlertDialogBuilder(ProjectViewActivity.this).setTitle("Success").setMessage("File has been downloaded successfully to: Downloads -> " + xyz).setPositiveButton("OK", null).show();
                        }
                    }
                } else {
                    progressButton.showProgress(false);
                    progressButton.setText("Download complete");

                    if (indexOfDownload != -1) {
                        String pathAfterDownload = dir.substring(indexOfDownload + downloadPath.length() + 1);
                        String xyz = pathAfterDownload.replace("/", separator);
                        new MaterialAlertDialogBuilder(ProjectViewActivity.this).setTitle("Success").setMessage("File has been downloaded successfully to: Downloads -> " + xyz).setPositiveButton("OK", null).show();

                    }
                }
            }

            @Override
            public void onDownloadFailed(Exception e) {
                alertCreator(e.getMessage());
            }
        };
        icon.setOnClickListener(v -> {
            ArrayList<String> icon_uir = new ArrayList<>();
            icon_uir.add(project_data.getIcon());
            new StfalconImageViewer.Builder<>(getParentActivity(), icon_uir, (imageView, image) -> Glide.with(ProjectViewActivity.this).load(image).transition(DrawableTransitionOptions.withCrossFade()).placeholder(new ColorDrawable(0xffD3D3D3)).into(imageView)).withStartPosition(0).withHiddenStatusBar(true).allowZooming(true).withDismissListener(() -> icon.setVisibility(View.VISIBLE)).allowSwipeToDismiss(true).withTransitionFrom(icon).show();// Use the method from ProjectScreenshotCell to get the ImageView
        });


        download_btn.addView(progressButton);
        download_btn.setOnClickListener(v -> {
            String developerType = developer.getString("type", "");
            if ("Paid".equals(developerType)) {
                if (project_data.getUnlockCode().equals("")) {
                    runDownload();
                } else {
                    createBottomSheetDialog(R.layout.premium_bottom_cell);
                    TextView h1 = (TextView) bsId(R.id.h1);
                    TextView t1 = (TextView) bsId(R.id.t1);
                    final EditText enterText = (EditText) bsId(R.id.edittext1);
                    enterText.setFocusableInTouchMode(true);
                    if (isNightMode()) {
                        h1.setTextColor(WHITE);
                        t1.setTextColor(TEXT_WHITE);
                    } else {
                        h1.setTextColor(BLACK);
                        t1.setTextColor(0xFF424242);
                    }
                    MaterialButton b1 = (MaterialButton) bsId(R.id.b1);
                    b1.setOnClickListener(v12 -> {
                        String edittext_value = enterText.getText().toString();
                        if (AndroidUtils.isConnected(getParentActivity())) {
                            if (project_data.getUnlockCode().equals(edittext_value)) {
                                runDownload();
                                if (interstitialAd != null) {
                                    interstitialAd.show(this);
                                }
                                dismissBottomSheetDialog();
                            } else {
                                dismissBottomSheetDialog();
                                alertToast("Wrong code");
                            }
                        } else {
                            alertToast("No Internet");
                        }
                    });
                    showBottomSheetDialog();
                }
            } else {
                if (interstitialAd != null) {
                    interstitialAd.show(this);
                }
                runDownload();
            }
        });
    }


    private void runDownload() {
        if (project_data.getProjectType().equals("Sketchware")) {
            if (!checkPermission()) {
                alertCreator("Storage permission is required in order to import the sketchware project.", (dialog, which) -> checkStorage(new PermissionListener() {
                    @Override
                    public void onGranted() {
                        processDownload();
                    }

                    @Override
                    public void onNotGranted() {
                        alertCreator("Storage permission is required in order to import the sketchware project.");
                    }
                }));
            } else {
                processDownload();
            }

        } else {
            processDownload();
        }
    }

    private void processDownload() {
        progressButton.showProgress(true);
        switch (project_data.getProjectType()) {
            case "Sketchware":
                dir = Environment.getExternalStorageDirectory() + "/Download/CodeKosh/Sketchware/.temp/temp.zip";
                break;
            case "Sketchware Pro(Mod)":
                dir = Environment.getExternalStorageDirectory() + "/Download/CodeKosh/Sketchware Pro/" + project_data.getTitle() + ".swb";
                break;
            case "Android Studio":
                dir = Environment.getExternalStorageDirectory() + "/Download/CodeKosh/Android Studio/" + project_data.getTitle() + ".zip";
                break;
            default:
                dir = Environment.getExternalStorageDirectory() + "/Download/CodeKosh/HTML/" + project_data.getTitle() + ".zip";
                break;
        }
        String f = getAppConfig().getDownloadManager();
        if (f.equals("0")) {
            downloadFile2(project_data.getDownloadUrl(), dir, callback);
        } else {
            downloadFile(project_data.getDownloadUrl(), dir, callback);
        }
    }


    private void tags(String tag) {
        Intent intent1 = new Intent();
        intent1.setClass(ProjectViewActivity.this, CategoryActivity.class);
        intent1.putExtra("code", tag);
        switch (tag) {
            case "category":
                intent1.putExtra("title", project_data.getCategory());
                break;
            case "editors_choice":
            case "verify":
            case "premium":
                intent1.putExtra("title", "true");
                break;
            case "project_type":
                intent1.putExtra("title", project_data.getProjectType());
                break;
        }

        startActivity(intent1);
    }

    private void clickAnimation(@NonNull View view) {
        ScaleAnimation fade_in = new ScaleAnimation(0.9f, 1f, 0.9f, 1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.7f);
        fade_in.setDuration(300);
        fade_in.setFillAfter(true);
        view.startAnimation(fade_in);
    }

    private void setUpDivider() {
        NestedScrollView nestedScrollView = findViewById(R.id.nested_scroll_view);
        divider = findViewById(R.id.divider);
        divider.setVisibility(View.GONE);
        nestedScrollView.setVerticalScrollBarEnabled(false);
        nestedScrollView.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> divider.setVisibility(scrollY > 0 ? View.VISIBLE : View.GONE));
    }

    private void removeScrollbar(View scrollView) {
        scrollView.setHorizontalScrollBarEnabled(false);
        scrollView.setVerticalScrollBarEnabled(false);
    }

    public void detectLinks(final TextView textView) {
        textView.setClickable(true);
        Linkify.addLinks(textView, Linkify.ALL);
        textView.setLinkTextColor(Color.parseColor("#2196F3"));
        textView.setLinksClickable(true);
    }

    public void transitionManager(final View view, final double duration) {
        LinearLayout viewGroup = (LinearLayout) view;

        AutoTransition autoTransition = new AutoTransition();
        autoTransition.setDuration((long) duration);
        autoTransition.setInterpolator(new DecelerateInterpolator());
        TransitionManager.beginDelayedTransition(viewGroup, autoTransition);
    }

    public void applyRoundCornerAndRipple(View view, double radius, double elevation, String color, boolean ripple) {
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(Color.parseColor(color));
        gd.setCornerRadius((float) radius);
        view.setElevation((float) elevation);

        if (ripple) {
            ColorStateList colorStateList = new ColorStateList(new int[][]{new int[]{}}, new int[]{Color.parseColor("#9e9e9e")});
            RippleDrawable rippleDrawable = new RippleDrawable(colorStateList, gd, null);
            view.setClickable(true);
            view.setBackground(rippleDrawable);
        } else {
            view.setBackground(gd);
        }
    }

    public void applyRippleRoundStroke(View view, String focusColor, String pressedColor, double round, double stroke, String strokeColor) {
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(Color.parseColor(focusColor));
        gd.setCornerRadius((float) round);
        gd.setStroke((int) stroke, Color.parseColor("#" + strokeColor.replace("#", "")));

        ColorStateList colorStateList = new ColorStateList(new int[][]{new int[]{}}, new int[]{Color.parseColor(pressedColor)});
        RippleDrawable rippleDrawable = new RippleDrawable(colorStateList, gd, null);

        view.setBackground(rippleDrawable);
    }

    public void rippleEffects(final String color, final View view) {
        ColorStateList colorStateList = new ColorStateList(new int[][]{new int[]{}}, new int[]{Color.parseColor(color)});
        RippleDrawable rippleDrawable = new RippleDrawable(colorStateList, null, null);
        view.setBackground(rippleDrawable);
    }

    public class Video_listAdapter extends RecyclerView.Adapter<Video_listAdapter.ViewHolder> {
        private final ArrayList<String> data;

        public Video_listAdapter(ArrayList<String> arrayList) {
            data = arrayList;
        }

        @NonNull
        @Override
        public Video_listAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(new ProjectScreenshotCell(parent.getContext()));
        }

        @Override
        public void onBindViewHolder(Video_listAdapter.ViewHolder _holder, final int _position) {
            ProjectScreenshotCell cell = (ProjectScreenshotCell) _holder.itemView;
            cell.setImage(data.get(_position));

            _holder.itemView.setOnClickListener(view -> new StfalconImageViewer.Builder<>(ProjectViewActivity.this, data, (imageView, image) -> Glide.with(ProjectViewActivity.this).load(image).transition(DrawableTransitionOptions.withCrossFade()).placeholder(new ColorDrawable(0xffD3D3D3)).into(imageView)).withStartPosition(_position).withHiddenStatusBar(true).allowZooming(true).withDismissListener(() -> cell.getImageImageView().setVisibility(View.VISIBLE)).allowSwipeToDismiss(true).withTransitionFrom(cell.getImageImageView())  // Use the method from ProjectScreenshotCell to get the ImageView
                    .show());
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public ImageView imageView;

            public ViewHolder(ProjectScreenshotCell cell) {
                super(cell);
                imageView = cell.getImageImageView();
            }
        }
    }

}