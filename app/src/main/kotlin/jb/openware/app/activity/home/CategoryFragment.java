package in.afi.codekosh.activity.home;

import static in.afi.codekosh.tools.StringUtilsKt.categoryUrl;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.HashMap;

import in.afi.codekosh.R;
import in.afi.codekosh.activity.other.CategoryActivity;

public class CategoryFragment extends Fragment {
    private final ArrayList<HashMap<String, Object>> meta_map = new ArrayList<>();
    private RequestNetwork requestNetwork;
    private RecyclerView listView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_category, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        LinearLayout layout1 = view.findViewById(R.id.layout1);
        requestNetwork = new RequestNetwork(requireActivity());
        setData();
        listView = new RecyclerView(requireActivity());
        listView.setLayoutManager(new LinearLayoutManager(requireActivity()));
        listView.setNestedScrollingEnabled(true);
        listView.setPaddingRelative(0, 0, 0, 0);
        listView.setAdapter(new VideoListAdapter(meta_map));
        layout1.addView(listView, new ViewGroup.LayoutParams(-1, -1));

        TextView tool_text = view.findViewById(R.id.tool_text);
        tool_text.setTextColor(isNightMode() ? 0xFFFFFFFF : 0xFF000000);

    }

    private void setData() {
        requestNetwork.startRequestNetwork("GET", categoryUrl, "A", new RequestNetwork.RequestListener() {
            @Override
            public void onResponse(String tag, String response, HashMap<String, Object> responseHeaders) {
                ArrayList<HashMap<String, Object>> meta_map = new Gson().fromJson(response, new TypeToken<ArrayList<HashMap<String, Object>>>() {
                }.getType());
                listView.setAdapter(new VideoListAdapter(meta_map));
            }

            @Override
            public void onErrorResponse(String tag, String message) {
                MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireActivity());
                builder.setTitle("Alert").setMessage(message).setPositiveButton(android.R.string.ok, null).show();
            }
        });

    }

    private boolean isNightMode() {
        int nightModeFlags = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return nightModeFlags == Configuration.UI_MODE_NIGHT_YES;
    }

    public class VideoListAdapter extends RecyclerView.Adapter<VideoListAdapter.ViewHolder> {

        private final ArrayList<HashMap<String, Object>> data;

        public VideoListAdapter(ArrayList<HashMap<String, Object>> data) {
            this.data = data;
        }

        @NonNull
        @Override
        public VideoListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.category_cell, parent, false);
            RecyclerView.LayoutParams _lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            view.setLayoutParams(_lp);
            return new VideoListAdapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull VideoListAdapter.ViewHolder holder, int position) {
            HashMap<String, Object> item = data.get(position);
            String url = String.valueOf(item.get("url"));
            String name = String.valueOf(item.get("name"));

            int textColor = isNightMode() ? 0xFFFFFFFF : 0xFF222222;

            holder.title.setTextColor(textColor);


            holder.title.setText(name);

            int colorFilter = isNightMode() ? 0xFF8DCDFF : 0xFF006493;
            holder.image.setColorFilter(colorFilter, PorterDuff.Mode.SRC_IN);

            holder.itemView.setOnClickListener(v -> {
                Intent intent1 = new Intent();
                intent1.setClass(holder.itemView.getContext(), CategoryActivity.class);
                intent1.putExtra("code", "category");
                intent1.putExtra("title", name);
                startActivity(intent1);
            });


            Glide.with(holder.itemView.getContext())
                    .load(url)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .centerCrop()
                    .into(holder.image);

        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView title;
            ImageView image;

            public ViewHolder(View itemView) {
                super(itemView);
                title = itemView.findViewById(R.id.t2);
                image = itemView.findViewById(R.id.i1);
            }
        }
    }


}