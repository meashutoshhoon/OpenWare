package in.afi.codekosh.activity.other;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

import in.afi.codekosh.R;
import in.afi.codekosh.tools.BaseFragment;
import in.afi.codekosh.tools.ThemeBuilder;

public class CategoryActivity extends BaseFragment {
    private final FirebaseDatabase _firebase = FirebaseDatabase.getInstance();
    private final DatabaseReference Users = _firebase.getReference("Users");
    private final ArrayList<HashMap<String, Object>> all_map = new ArrayList<>();
    private final DatabaseReference normal = _firebase.getReference("projects/normal");
    private final DatabaseReference premium = _firebase.getReference("projects/premium");
    private final HashMap<String, String> user_names = new HashMap<>();
    private String key = "";
    private double limit = 0;
    private LinearLayout progressbar1;
    private final ValueEventListener valueEventListener1 = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot _param1) {
            try {
                all_map.clear();
                GenericTypeIndicator<HashMap<String, Object>> _ind = new GenericTypeIndicator<>() {
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
    private RecyclerView recyclerview1;
    private Query query1;
    private TextView title;
    private ImageView back;

    @Override
    protected boolean isHomeFragment() {
        return false;
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_category;
    }

    @Override
    public void getThemeDescriptions(ThemeBuilder themeBuilder) {
        themeBuilder.setImageColorFilter(back, BLACK, WHITE);
        themeBuilder.setTextColor(findViewById(R.id.title), BLACK, WHITE);
    }

    @Override
    protected void initialize() {
        recyclerview1 = findViewById(R.id.recyclerview1);
        progressbar1 = findViewById(R.id.progressbar1);
        back = findViewById(R.id.back);
        title = findViewById(R.id.title);
        LinearLayout layout_progress = findViewById(R.id.layout_progress);
        RadialProgressView progress = new RadialProgressView(this);
        progress.setProgressColor(0xFF006493);
        layout_progress.addView(progress);
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

    @SuppressLint("SetTextI18n")
    @Override
    protected void initializeLogic() {
        back.setOnClickListener(v -> goBack());
        recyclerview1.setAdapter(new ListProjectAdapter(all_map, this, user_names, 1));
        recyclerview1.setLayoutManager(new LinearLayoutManager(this));
        limit = 30;
        key = getIntent().getStringExtra("code");
        if (key != null) {
            switch (key) {
                case "category":
                    title.setText(getIntent().getStringExtra("title"));
                    break;
                case "editors_choice":
                    title.setText("Editor's Choice Projects");
                    break;
                case "verify":
                    title.setText("Verified Projects");
                    break;
                case "project_type":
                    title.setText(getIntent().getStringExtra("title") + "Projects");
                    break;
            }
            if (key.equals("premium")) {
                title.setText("Premium Projects");
                recyclerview1.setAdapter(new ListProjectAdapter(all_map, this, user_names, 0));
                query1 = premium.limitToLast((int) limit);
                query1.addValueEventListener(valueEventListener1);
            } else {
                recyclerview1.setAdapter(new ListProjectAdapter(all_map, this, user_names, 1));
                query1 = normal.limitToLast((int) limit).orderByChild(key).startAt(getIntent().getStringExtra("title")).endAt(getIntent().getStringExtra("title"));
                query1.addValueEventListener(valueEventListener1);
            }
        }

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
                        if (key.equals("premium")) {
                            query1 = premium.limitToLast((int) limit);
                            query1.addValueEventListener(valueEventListener1);
                        } else {
                            query1 = normal.limitToLast((int) limit).orderByChild(key).startAt(getIntent().getStringExtra("title")).endAt(getIntent().getStringExtra("title"));
                            query1.addValueEventListener(valueEventListener1);
                        }
                    }
                    progressbar1.setVisibility(View.VISIBLE);
                }
            }
        });
    }

}