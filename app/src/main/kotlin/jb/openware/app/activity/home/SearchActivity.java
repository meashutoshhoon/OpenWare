package in.afi.codekosh.activity.home;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import in.afi.codekosh.R;
import in.afi.codekosh.activity.project.ProjectViewActivity;
import in.afi.codekosh.cells.ProjectCell;
import in.afi.codekosh.tools.BaseFragment;

public class SearchActivity extends BaseFragment {
    private final DatabaseReference normal = FirebaseDatabase.getInstance().getReference("projects/normal");
    private LinearLayout progressbar1;
    private SearchBarView search_bar;
    private RecyclerView recyclerview1;
    private String textSearch;
    private TextView textview1;

    @Override
    protected boolean isHomeFragment() {
        return false;
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_search;
    }

    @Override
    public void getThemeDescriptions(ThemeBuilder themeBuilder) {
        themeBuilder.setTextColor(textview1, BLACK, WHITE);
    }

    @Override
    protected void initialize() {
        // Initialize UI elements
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        SearchEditText editText = findViewById(R.id.search_view);
        recyclerview1 = findViewById(R.id.recyclerview1);
        textview1 = findViewById(R.id.textview1);
        progressbar1 = findViewById(R.id.progressbar1);
        LinearLayout layout_progress = findViewById(R.id.layout_progress);

        RadialProgressView progress = new RadialProgressView(this);
        progress.setProgressColor(0xFF006493);
        layout_progress.addView(progress);

        openKeyboard(editText);

        // SearchBar initialization
        search_bar = new SearchBarView(this);
        search_bar.init(toolbar, editText);
        search_bar.setEditTextEnable(true);
        search_bar.setToolbarClickListener(v -> goBack());
        search_bar.setMenuVisibility(false);
        search_bar.setSearchSubmitListener(v -> {
            textSearch = search_bar.getSearchText().trim();
            result(textSearch);
        });

        // Set editor action listener for search functionality
        editText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                result(search_bar.getSearchText().trim());
                return true;
            }
            return false;
        });

        // Hide unnecessary views initially
        hideViews(recyclerview1, progressbar1, textview1);
    }


    @Override
    protected void initializeLogic() {
        recyclerview1.setLayoutManager(new LinearLayoutManager(this));
    }

    public void result(final String keyString) {
        if (keyString.isEmpty()) {
            showToast("Enter Something to search");
            return;
        }
        ArrayList<HashMap<String, Object>> arrayList = new ArrayList<>();
        hideViews(recyclerview1, textview1);
        showViews(progressbar1);

        normal.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot _dataSnapshot) {
                for (DataSnapshot _data : _dataSnapshot.getChildren()) {
                    HashMap<String, Object> _map = _data.getValue(new GenericTypeIndicator<HashMap<String, Object>>() {
                    });
                    if (_map != null && String.valueOf(_map.get("title")).toLowerCase().contains(keyString.toLowerCase())) {
                        HashMap<String, Object> map = new HashMap<>();
                        map.put("title", String.valueOf(_map.get("title")));
                        map.put("icon", String.valueOf(_map.get("icon")));
                        map.put("name", String.valueOf(_map.get("name")));
                        map.put("size", String.valueOf(_map.get("size")));
                        map.put("key", String.valueOf(_map.get("key")));
                        map.put("uid", String.valueOf(_map.get("uid")));
                        arrayList.add(map);
                    }
                }

                delayTask(() -> {
                    if (arrayList.isEmpty()) {
                        showViews(textview1);
                        hideViews(recyclerview1, progressbar1);
                    } else {
                        showViews(recyclerview1);
                        hideViews(textview1, progressbar1);
                        hideKeyboard(getApplicationContext());
                    }
                }, 100);

                recyclerview1.setAdapter(new BannerAdapter(arrayList));
                Objects.requireNonNull(recyclerview1.getAdapter()).notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError _databaseError) {
            }
        });
    }


    public class BannerAdapter extends RecyclerView.Adapter<BannerAdapter.ViewHolder> {

        private final ArrayList<HashMap<String, Object>> _data;

        public BannerAdapter(ArrayList<HashMap<String, Object>> _data) {
            this._data = _data;

        }

        @NonNull
        @Override
        public BannerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(new ProjectCell(SearchActivity.this));
        }

        @Override
        public void onBindViewHolder(BannerAdapter.ViewHolder holder, int position) {
            HashMap<String, Object> item = _data.get(position);
            ((ProjectCell) holder.itemView).setData(item);

            holder.itemView.setOnClickListener(view -> {
                SharedPreferences developer = getSharedPreferences("developer", Activity.MODE_PRIVATE);
                developer.edit().putString("type", "Free").apply();
                Intent intent = new Intent(SearchActivity.this, ProjectViewActivity.class);
                intent.putExtra("key", String.valueOf(item.get("key")));
                intent.putExtra("uid", String.valueOf(item.get("uid")));
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return _data.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            ViewHolder(View v) {
                super(v);
            }
        }
    }
}