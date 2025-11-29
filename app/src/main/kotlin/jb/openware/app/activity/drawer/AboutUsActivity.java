package in.afi.codekosh.activity.drawer;

import static in.afi.codekosh.tools.StringUtilsKt.privacyUrl;
import static in.afi.codekosh.tools.StringUtilsKt.socialUrl;
import static in.afi.codekosh.tools.StringUtilsKt.termsUrl;
import static in.afi.codekosh.tools.StringUtilsKt.userPolicyUrl;
import static in.afi.codekosh.tools.StringUtilsKt.version;

import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import in.afi.codekosh.R;
import in.afi.codekosh.tools.BaseFragment;
import in.afi.codekosh.tools.ThemeBuilder;
import in.afi.codekosh.tools.UserConfig;

public class AboutUsActivity extends BaseFragment {
    private ImageView back;
    private View divider;
    private LinearLayout loading, refer;
    private NestedScrollView content;
    private RecyclerView meta_listView;

    @Override
    protected boolean isHomeFragment() {
        return false;
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_about_us;
    }

    @Override
    public void getThemeDescriptions(ThemeBuilder themeBuilder) {
        int[] textViews = {
                R.id.title1, R.id.title2, R.id.t1, R.id.t2, R.id.t3, R.id.t4, R.id.t5, R.id.t6, R.id.t7
        };

        for (int textViewId : textViews) {
            themeBuilder.setTextColor(findViewById(textViewId), BLACK, TEXT_WHITE);
        }

        themeBuilder.setImageColorFilter(back, BLACK, WHITE);

        if (themeBuilder.isNightMode()) {
            divider.setBackgroundColor(ContextCompat.getColor(this, R.color.divider_color_night));
        } else {
            divider.setBackgroundColor(ContextCompat.getColor(this, R.color.divider_color));
        }
    }


    @Override
    protected void initialize() {
        back = findViewById(R.id.back);
        loading = findViewById(R.id.loading);
        content = findViewById(R.id.nested_scroll_view);
        refer = findViewById(R.id.refer);
        meta_listView = findViewById(R.id.meta);
        TextView version_text = findViewById(R.id.version_text);
        version_text.setText(version);
        meta_listView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void initializeLogic() {
        setUpDivider();
        back.setOnClickListener(v -> goBack());
        setLoading();
        delayTask(() -> getConnectionsManager(socialUrl, GET, new RequestListener() {
            @Override
            public void onResponse(String tag, String response, HashMap<String, Object> responseHeaders) {
                ArrayList<HashMap<String, Object>> meta_map = new Gson().fromJson(response, new TypeToken<ArrayList<HashMap<String, Object>>>() {
                }.getType());
                meta_listView.setAdapter(new BetaListAdapter(meta_map));
                delayTask(() -> toggle(false));
            }

            @Override
            public void onErrorResponse(String tag, String message) {
                alertCreator("An error occurred");
            }
        }));
        findViewById(R.id.privacy).setOnClickListener(v -> openUrl(privacyUrl));
        findViewById(R.id.users).setOnClickListener(v -> openUrl(userPolicyUrl));
        findViewById(R.id.terms).setOnClickListener(v -> openUrl(termsUrl));
        findViewById(R.id.email).setOnClickListener(v -> sendEmail("CodeKosh", "Hello CodeKosh Support,\n\n My Name Is :- ".concat(new UserConfig(AboutUsActivity.this).getName()).concat("\nEmail:- ".concat(FirebaseAuth.getInstance().getCurrentUser().getEmail().concat("\n\nThis email is about:-\n")))));
        findViewById(R.id.abt_developer).setOnClickListener(v -> openActivity(new AboutDevActivity(), true));
        findViewById(R.id.betor).setOnClickListener(v -> openActivity(new BetaTesterActivity(), true));

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

    private void setUpDivider() {
        NestedScrollView nestedScrollView = findViewById(R.id.nested_scroll_view);
        divider = findViewById(R.id.divider);
        divider.setVisibility(View.GONE);
        nestedScrollView.setVerticalScrollBarEnabled(false);
        nestedScrollView.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> divider.setVisibility(scrollY > 0 ? View.VISIBLE : View.GONE));
    }

    public class BetaListAdapter extends RecyclerView.Adapter<BetaListAdapter.ViewHolder> {
        ArrayList<HashMap<String, Object>> data;

        public BetaListAdapter(ArrayList<HashMap<String, Object>> arr) {
            data = arr;
        }

        @NonNull
        @Override
        public BetaListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = getLayoutInflater();
            @SuppressLint("InflateParams") View view = inflater.inflate(R.layout.layout_social, null);
            RecyclerView.LayoutParams layoutParams = new RecyclerView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            view.setLayoutParams(layoutParams);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(BetaListAdapter.ViewHolder holder, final int position) {
            View itemView = holder.itemView;
            final LinearLayout body = itemView.findViewById(R.id.sketchware);
            final CircleImageView img = itemView.findViewById(R.id.img);
            final TextView name = itemView.findViewById(R.id.name);
            String url = Objects.requireNonNull(data.get(position).get("url")).toString();
            String avatar = Objects.requireNonNull(data.get(position).get("logo")).toString();

            name.setTextColor(isNightMode() ? WHITE : BLACK);

            if (avatar.equals("none")) {
                img.setImageResource(R.drawable.logo_avatar);
            } else {
                Glide.with(AboutUsActivity.this)
                        .load(Uri.parse(avatar))
                        .placeholder(new ColorDrawable(0xFFE0E0E0))
                        .fitCenter()
                        .into(img);
            }

            name.setText(Objects.requireNonNull(data.get(position).get("name")).toString());
            body.setOnClickListener(v -> openUrl(url));
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        private boolean isNightMode() {
            int nightModeFlags = AboutUsActivity.this.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
            return nightModeFlags == Configuration.UI_MODE_NIGHT_YES;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public ViewHolder(View v) {
                super(v);
            }
        }
    }


}