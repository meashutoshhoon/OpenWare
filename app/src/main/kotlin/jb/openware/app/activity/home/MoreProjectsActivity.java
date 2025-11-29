package in.afi.codekosh.activity.home;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.transition.TransitionManager;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.appbar.MaterialToolbar;
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

import in.afi.codekosh.R;
import in.afi.codekosh.activity.profile.ProfileActivity;
import in.afi.codekosh.adapter.BaseProjectAdapter;
import in.afi.codekosh.components.SearchBarView;
import in.afi.codekosh.components.SearchEditText;
import in.afi.codekosh.components.SharedPreferencesManager;
import in.afi.codekosh.tools.BaseFragment;
import in.afi.codekosh.tools.ThemeBuilder;

public class MoreProjectsActivity extends BaseFragment {

    private final ArrayList<HashMap<String, Object>> all_map = new ArrayList<>();
    private final DatabaseReference normal = FirebaseDatabase.getInstance().getReference("projects/normal");
    private final ValueEventListener valueEventListener1 = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot _param1) {
            try {
                all_map.clear();
                GenericTypeIndicator<HashMap<String, Object>> _ind = new GenericTypeIndicator<HashMap<String, Object>>() {
                };
                for (DataSnapshot _data : _param1.getChildren()) {
                    HashMap<String, Object> _map = _data.getValue(_ind);
                    if (Objects.equals(_map.get("visibility"), "true")) {
                        all_map.add(_map);
                    }
                }
                Collections.reverse(all_map);
                progressbar1.setVisibility(View.GONE);
            } catch (Exception e) {
                showToast(e.toString());
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
        }
    };
    private final String id = "true";
    private ArrayList<HashMap<String, Object>> most_projects = new ArrayList<>();
    private String key = "";
    private double limit = 0;
    private LinearLayout loading, content, progressbar1;
    private CoordinatorLayout refer;
    private SearchBarView search_bar;
    private RecyclerView recyclerview1;
    private Query query1;

    @Override
    protected boolean isHomeFragment() {
        return false;
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_home_extend;
    }

    @Override
    public void getThemeDescriptions(ThemeBuilder themeBuilder) {

    }

    private void renderProfileImage(Drawable resource) {
        runnableTask(() -> search_bar.getMenuIcon().setIcon(resource));
    }

    private void loadImage(String profileUrl) {
        try {
            Glide.with(MoreProjectsActivity.this).load(profileUrl).centerCrop().circleCrop().sizeMultiplier(0.50f).addListener(new com.bumptech.glide.request.RequestListener<Drawable>() {
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
    protected void initialize() {
        // Initialize UI elements
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        SearchEditText editText = findViewById(R.id.search_view);
        loading = findViewById(R.id.loading);
        content = findViewById(R.id.scroll);
        refer = findViewById(R.id.main_item);
        recyclerview1 = findViewById(R.id.recyclerview1);
        progressbar1 = findViewById(R.id.progressbar1);
        LinearLayout layout_progress = findViewById(R.id.layout_progress);


        RadialProgressView progress = new RadialProgressView(this);
        progress.setProgressColor(0xFF006493);
        layout_progress.addView(progress);


        // SearchBar
        search_bar = new SearchBarView(this);
        search_bar.init(toolbar, editText);
        search_bar.setEditTextEnable(false);
        search_bar.setEditTextClickListener(v -> startActivity(SearchActivity.class));
        search_bar.setToolbarClickListener(item -> goBack());
        search_bar.setMenuClickListener(item -> {
            if (item.getItemId() == R.id.profile) {
                SharedPreferences developer = getSharedPreferences("developer", Activity.MODE_PRIVATE);
                developer.edit().putString("uid", getUID()).apply();
                startActivity(ProfileActivity.class);

            }
            return false;
        });

        if (getUserConfig().getProfileUrl() != null) {
            if (getUserConfig().getProfileUrl().equals("none")) {
                renderProfileImage(DrawableGenerator.generateDrawable(getParentActivity(), Color.parseColor("#006493"), getUserConfig().getName().substring(0, 1)));
            } else {
                loadImage(getUserConfig().getProfileUrl());
            }
        }


    }

    @Override
    protected void initializeLogic() {
        recyclerview1.setLayoutManager(new GridLayoutManager(this, 3));
        limit = 30;
        key = new SharedPreferencesManager(getParentActivity()).getString("id", "all");
        BaseProjectAdapter baseProjectAdapter = new BaseProjectAdapter(all_map, MoreProjectsActivity.this);
        recyclerview1.setAdapter(baseProjectAdapter);
        if (key != null) {
            switch (key) {
                case "all":
                    BaseProjectAdapter normalAdapter = new BaseProjectAdapter(all_map, MoreProjectsActivity.this);
                    recyclerview1.setAdapter(normalAdapter);
                    query1 = normal.limitToLast((int) limit);
                    query1.addValueEventListener(valueEventListener1);
                    break;
                case "editors_choice":
                    BaseProjectAdapter Adapter = new BaseProjectAdapter(all_map, MoreProjectsActivity.this);
                    recyclerview1.setAdapter(Adapter);
                    query1 = normal.limitToLast((int) limit).orderByChild(key).startAt(id).endAt(id);
                    query1.addValueEventListener(valueEventListener1);
                    break;
                case "like":
                    query1 = normal.limitToLast((int) limit);
                    query1.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot _param1) {
                            try {
                                most_projects = new ArrayList<>();
                                GenericTypeIndicator<HashMap<String, Object>> _ind = new GenericTypeIndicator<HashMap<String, Object>>() {
                                };

                                for (DataSnapshot _data : _param1.getChildren()) {
                                    HashMap<String, Object> _map = _data.getValue(_ind);
                                    if (Objects.equals(_map.get("visibility"), "true")) {
                                        most_projects.add(_map);
                                    }
                                }

                                sortMapListByKeyValuePair(most_projects, "likes", true, false);
                                uiThread(() -> {
                                    BaseProjectAdapter mostAdapter = new BaseProjectAdapter(most_projects, MoreProjectsActivity.this);
                                    recyclerview1.setAdapter(mostAdapter);
                                });
                            } catch (Exception ignored) {
                                // Handle exceptions
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            // Handle cancellations
                        }
                    });
                    break;
            }
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

    @Override
    protected void onPostCreate(Bundle _savedInstanceState) {
        super.onPostCreate(_savedInstanceState);
        recyclerview1.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (!recyclerView.canScrollVertically(1)) {
                    limit = limit + 15;
                    if (key != null) {
                        if (key.equals("editors_choice")) {
                            query1 = normal.limitToLast((int) limit).orderByChild(key).startAt(id).endAt(id);
                            query1.addValueEventListener(valueEventListener1);
                        } else {
                            query1 = normal.limitToLast((int) limit);
                            query1.addValueEventListener(valueEventListener1);
                        }
                    }
                    progressbar1.setVisibility(View.VISIBLE);
                }
            }
        });
    }


    private void setLoading() {
        LoadingCell cell = new LoadingCell(this);
        cell.toggle(true);
        loading.addView(cell);
        toggle(true);
    }

    private void toggle(boolean bool) {
        TransitionManager.beginDelayedTransition(refer);
        content.setVisibility(bool ? View.GONE : View.VISIBLE);
        loading.setVisibility(bool ? View.VISIBLE : View.GONE);
    }


}