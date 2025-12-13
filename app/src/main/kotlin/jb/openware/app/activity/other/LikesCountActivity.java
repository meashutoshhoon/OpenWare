package in.afi.codekosh.activity.other;

import android.app.Activity;
import android.content.SharedPreferences;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;

import java.util.ArrayList;
import java.util.HashMap;

import in.afi.codekosh.R;
import in.afi.codekosh.activity.profile.ProfileActivity;
import in.afi.codekosh.cells.UsersCell;
import in.afi.codekosh.tools.BaseFragment;

public class LikesCountActivity extends BaseFragment {
    private final FirebaseDatabase _firebase = FirebaseDatabase.getInstance();
    private final ArrayList<HashMap<String, Object>> all_map = new ArrayList<>();
    private final DatabaseReference Users = _firebase.getReference("Users");
    private final DatabaseReference likes = _firebase.getReference("likes");
    private final HashMap<String, String> user_names = new HashMap<>();
    private final HashMap<String, String> user_dp = new HashMap<>();
    private final HashMap<String, String> user_color = new HashMap<>();
    private final HashMap<String, String> user_badge = new HashMap<>();
    private final HashMap<String, String> user_verified = new HashMap<>();
    private String key = "";
    private LinearLayout progressbar1, text;
    private RecyclerView recyclerview1;
    private ImageView back;


    @Override
    protected boolean isHomeFragment() {
        return false;
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_likes_count;
    }

    @Override
    public void getThemeDescriptions(ThemeBuilder themeBuilder) {
        themeBuilder.setImageColorFilter(back, BLACK, WHITE);
        themeBuilder.setTextColor(findViewById(R.id.title), BLACK, WHITE);
        themeBuilder.setTextColor(findViewById(R.id.ref), BLACK, WHITE);
    }

    @Override
    protected void initialize() {
        recyclerview1 = findViewById(R.id.recyclerview1);
        progressbar1 = findViewById(R.id.progressbar1);
        text = findViewById(R.id.text);
        back = findViewById(R.id.back);
        LinearLayout layout_progress = findViewById(R.id.layout_progress);
        RadialProgressView progress = new RadialProgressView(this);
        progress.setProgressColor(0xFF006493);
        layout_progress.addView(progress);
        ChildEventListener likes_count = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot _param1, String _param2) {
                final HashMap<String, Object> _childValue = _param1.getValue(new GenericTypeIndicator<HashMap<String, Object>>() {});
                if (_childValue != null) {
                    processData(_childValue);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot _param1, String _param2) {
                final HashMap<String, Object> _childValue = _param1.getValue(new GenericTypeIndicator<HashMap<String, Object>>() {});
                if (_childValue != null) {
                    processData(_childValue);
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
        likes.addChildEventListener(likes_count);
        ChildEventListener _users_child_listener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot _param1, String _param2) {
                GenericTypeIndicator<HashMap<String, Object>> _ind = new GenericTypeIndicator<HashMap<String, Object>>() {
                };
                final HashMap<String, Object> _childValue = _param1.getValue(_ind);
                if (_childValue != null && _childValue.containsKey("uid")) {
                    String uid = (String) _childValue.get("uid");
                    if (_childValue.containsKey("name")) {
                        user_names.put(uid, (String) _childValue.get("name"));
                    }
                    if (_childValue.containsKey("avatar")) {
                        user_dp.put(uid, (String) _childValue.get("avatar"));
                    }
                    if (_childValue.containsKey("color")) {
                        user_color.put(uid, (String) _childValue.get("color"));
                    }
                    if (_childValue.containsKey("badge")) {
                        user_badge.put(uid, (String) _childValue.get("badge"));
                    }
                    if (_childValue.containsKey("verified")) {
                        user_verified.put(uid, (String) _childValue.get("verified"));
                    }
                }

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot _param1, String _param2) {
                GenericTypeIndicator<HashMap<String, Object>> _ind = new GenericTypeIndicator<HashMap<String, Object>>() {
                };
                final HashMap<String, Object> _childValue = _param1.getValue(_ind);
                if (_childValue != null && _childValue.containsKey("uid")) {
                    String uid = (String) _childValue.get("uid");
                    if (_childValue.containsKey("name")) {
                        user_names.put(uid, (String) _childValue.get("name"));
                    }
                    if (_childValue.containsKey("avatar")) {
                        user_dp.put(uid, (String) _childValue.get("avatar"));
                    }
                    if (_childValue.containsKey("color")) {
                        user_color.put(uid, (String) _childValue.get("color"));
                    }
                    if (_childValue.containsKey("badge")) {
                        user_badge.put(uid, (String) _childValue.get("badge"));
                    }
                    if (_childValue.containsKey("verified")) {
                        user_verified.put(uid, (String) _childValue.get("verified"));
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
    }

    private void processData(HashMap<String, Object> childValue) {
        if (key.equals(String.valueOf(childValue.get("key")))) {
            if ("true".equals(String.valueOf(childValue.get("value")))) {
                all_map.add(0, childValue);
                recyclerview1.setAdapter(new BetaListAdapter(all_map));
                setView(all_map.isEmpty() ? 3 : 2);
            }else {
                setView(3);
            }
        }

    }

    @Override
    protected void initializeLogic() {
        setView(1);
        recyclerview1.setLayoutManager(new LinearLayoutManager(this));
        key = getIntent().getStringExtra("key");
        back.setOnClickListener(v -> goBack());
    }


    private void setView(int code) {
        if (code == 1) {
            showViews(progressbar1);
            hideViews(text, recyclerview1);
        } else if (code == 2) {
            showViews(recyclerview1);
            hideViews(text, progressbar1);
        } else if (code == 3) {
            showViews(text);
            hideViews(progressbar1, recyclerview1);
        }
    }

    public class BetaListAdapter extends RecyclerView.Adapter<BetaListAdapter.ViewHolder> {
        ArrayList<HashMap<String, Object>> _data;

        public BetaListAdapter(ArrayList<HashMap<String, Object>> arr) {
            _data = arr;
        }

        @NonNull
        @Override
        public BetaListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            UsersCell UsersCell = new UsersCell(LikesCountActivity.this);
            ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            UsersCell.setLayoutParams(layoutParams);
            return new BetaListAdapter.ViewHolder(UsersCell);
        }

        @Override
        public void onBindViewHolder(@NonNull BetaListAdapter.ViewHolder holder, final int position) {
            HashMap<String, Object> item = _data.get(position);
            if (item.containsKey("uid")) {
                String uid = String.valueOf(item.get("uid"));
                if (user_names.containsKey(uid) && user_dp.containsKey(uid) && user_color.containsKey(uid) && user_badge.containsKey(uid) && user_verified.containsKey(uid)) {
                    String name = user_names.get(uid);
                    String avatar = user_dp.get(uid);
                    String color = user_color.get(uid);
                    String badge = user_badge.get(uid);
                    String verified = user_verified.get(uid);
                    ((UsersCell) holder.itemView).setData(avatar, name, color, badge, verified);
                }
            }

            holder.itemView.setOnClickListener(v -> {
                SharedPreferences developer = getSharedPreferences("developer", Activity.MODE_PRIVATE);
                developer.edit().putString("uid", String.valueOf(item.get("uid"))).apply();
                startActivity(ProfileActivity.class);
            });
        }

        @Override
        public int getItemCount() {
            return _data.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public ViewHolder(View v) {
                super(v);
            }
        }
    }

}