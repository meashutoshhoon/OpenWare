package in.afi.codekosh.activity.project;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.text.util.Linkify;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Objects;

import in.afi.codekosh.R;
import in.afi.codekosh.tools.BaseFragment;

public class AboutActivity extends BaseFragment {
    private final DatabaseReference Users = FirebaseDatabase.getInstance().getReference("Users");
    private TextView title, version, whats_new, about, downloads, date, name, date_r;
    private HashMap<String, Object> dataMap;
    private MaterialCardView card;
    private LinearLayout linear6;
    private ImageView back;
    private View divider;

    @Override
    protected boolean isHomeFragment() {
        return false;
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_about;
    }

    @Override
    public void getThemeDescriptions(ThemeBuilder themeBuilder) {
        int[] textViewIds = {R.id.title, R.id.t1, R.id.whats_new_t, R.id.about_app, R.id.more_info};
        TextView[] textViews = {downloads, date, name, date_r};
        int[] textViewIds_2 = {R.id.textview9, R.id.textview11, R.id.textview13, R.id.textview19, R.id.textview3, R.id.whats_new, R.id.about, R.id.version};

        for (int id : textViewIds) {
            TextView textView = findViewById(id);
            themeBuilder.setTextColor(textView, BLACK, WHITE);
        }
        for (TextView textView : textViews) {
            themeBuilder.setTextColor(textView, TEXT_GREY, TEXT_WHITE);
        }
        for (int id : textViewIds_2) {
            themeBuilder.setTextColor(findViewById(id), 0xFF696969, 0xFF979A9E);
        }

        themeBuilder.setImageColorFilter(back, BLACK, WHITE);
        themeBuilder.setImageColorFilter(findViewById(R.id.warning_i), BLACK, WHITE);

        if (themeBuilder.isNightMode()) {
            divider.setBackgroundColor(ContextCompat.getColor(this, R.color.divider_color_night));
        } else {
            divider.setBackgroundColor(ContextCompat.getColor(this, R.color.divider_color));
        }
    }

    @Override
    protected void initialize() {
        title = findViewById(R.id.title);
        version = findViewById(R.id.version);
        whats_new = findViewById(R.id.whats_new);
        about = findViewById(R.id.about);
        downloads = findViewById(R.id.downloads);
        date = findViewById(R.id.date);
        name = findViewById(R.id.name);
        date_r = findViewById(R.id.date_r);
        card = findViewById(R.id.card);
        linear6 = findViewById(R.id.linear6);
        back = findViewById(R.id.back);
    }

    @Override
    protected void initializeLogic() {
        setUpDivider();
        back.setOnClickListener(v -> goBack());
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("data")) {
            dataMap = (HashMap<String, Object>) intent.getSerializableExtra("data");

            if (dataMap != null) {
                updateUI(dataMap);
                String uid = (String) dataMap.get("uid");
                Users.orderByKey().equalTo(uid).addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String previousChildName) {
                        HashMap<String, Object> childValue = (HashMap<String, Object>) dataSnapshot.getValue();

                        if (childValue != null && childValue.containsKey("name")) {
                            String n = String.valueOf(childValue.get("name"));
                            name.setText(n);
                        }
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, String previousChildName) {
                        HashMap<String, Object> childValue = (HashMap<String, Object>) dataSnapshot.getValue();

                        if (childValue != null && childValue.containsKey("name")) {
                            String n = String.valueOf(childValue.get("name"));
                            name.setText(n);
                        }
                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, String previousChildName) {
                        // This method is triggered when a child location's priority changes.
                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                        // This method is triggered when a child node is removed from the specified database location.
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // This method will be triggered in the event that this listener either failed
                        // at the server, or is removed as a result of the security and Firebase rules.
                    }
                });
            }
        }

    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState, @NonNull PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        if (dataMap != null) {
            outState.putSerializable("dataMap", dataMap);
        }
    }

    @Override
    public void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState.containsKey("dataMap")) {
            dataMap = (HashMap<String, Object>) savedInstanceState.getSerializable("dataMap");
            if (dataMap != null) {
                updateUI(dataMap);
            }
        }
    }

    private void setUpDivider() {
        NestedScrollView nestedScrollView = findViewById(R.id.nested_scroll_view);
        divider = findViewById(R.id.divider);
        divider.setVisibility(View.GONE);
        nestedScrollView.setVerticalScrollBarEnabled(false);
        nestedScrollView.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> divider.setVisibility(scrollY > 0 ? View.VISIBLE : View.GONE));
    }

    @SuppressLint("SetTextI18n")
    private void updateUI(HashMap<String, Object> hashMap) {
        String titleText = String.valueOf(hashMap.get("title"));
        String projectType = String.valueOf(hashMap.get("project_type"));
        String isSketchwarePro = String.valueOf(hashMap.get("isSketchwarePro"));
        String whatsNew = String.valueOf(hashMap.get("whats_new"));
        String description = String.valueOf(hashMap.get("description"));
        String downloadsText = String.valueOf(hashMap.get("downloads"));
        String time = String.valueOf(hashMap.get("time"));
        String updateTime = String.valueOf(hashMap.get("update_time"));

        title.setText(titleText);


        if (Objects.equals(projectType, "Sketchware Pro(Mod)")) {
            showViews(card);
            TextFormatter.formatText(version, "Use *b" + isSketchwarePro + "*b version to avoid errors.");
        } else {
            hideViews(card);
        }

        if (!Objects.equals(whatsNew, "none")) {
            showViews(linear6);
            TextFormatter.formatText(whats_new, whatsNew);
        } else {
            hideViews(linear6, findViewById(R.id.divider_l));
        }

        TextFormatter.formatText(about, description);
        downloads.setText(downloadsText);
        date_r.setText(time);

        if (!Objects.equals(updateTime, "none")) {
            showViews(findViewById(R.id.linear13));
            date.setText(updateTime);
        } else {
            hideViews(findViewById(R.id.linear13));
        }
        detectLinks(whats_new);
        detectLinks(about);

    }

    public void detectLinks(final TextView textView) {
        textView.setClickable(true);
        Linkify.addLinks(textView, Linkify.ALL);
        textView.setLinkTextColor(Color.parseColor("#2196F3"));
        textView.setLinksClickable(true);
    }
}
