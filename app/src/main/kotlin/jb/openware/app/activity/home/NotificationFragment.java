package in.afi.codekosh.activity.home;

import static in.afi.codekosh.tools.StringUtilsKt.notificationUrl;

import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.stfalcon.imageviewer.StfalconImageViewer;

import java.util.ArrayList;
import java.util.HashMap;

import in.afi.codekosh.R;

public class NotificationFragment extends Fragment {
    private RecyclerView listView;
    private SwipeRefreshLayout refreshLayout_user;
    private RequestNetwork requestNetwork;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_notification, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        LinearLayout layout1 = view.findViewById(R.id.layout1);
        requestNetwork = new RequestNetwork(requireActivity());

        listView = new RecyclerView(requireActivity());
        listView.setLayoutManager(new LinearLayoutManager(requireActivity()));
        listView.setNestedScrollingEnabled(true);
        listView.setPaddingRelative(0, 0, 0, 0);

        refreshLayout_user = new SwipeRefreshLayout(requireActivity());
        refreshLayout_user.addView(listView);
        refreshLayout_user.setOnRefreshListener(this::refreshData);
        layout1.addView(refreshLayout_user, new ViewGroup.LayoutParams(-1, -1));

        TextView tool_text = view.findViewById(R.id.tool_text);
        tool_text.setTextColor(isNightMode() ? 0xFFFFFFFF : 0xFF000000);

        // Load initial data
        refreshData();
    }

    private void refreshData() {
        requestNetwork.startRequestNetwork("GET", notificationUrl, "A", new RequestNetwork.RequestListener() {
            @Override
            public void onResponse(String tag, String response, HashMap<String, Object> responseHeaders) {
                ArrayList<HashMap<String, Object>> meta_map = new Gson().fromJson(response, new TypeToken<ArrayList<HashMap<String, Object>>>() {
                }.getType());
                listView.setAdapter(new VideoListAdapter(meta_map));
                refreshLayout_user.setRefreshing(false);
            }

            @Override
            public void onErrorResponse(String tag, String message) {
                MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireActivity());
                builder.setTitle("Alert").setMessage(message).setPositiveButton(android.R.string.ok, null).show();
                refreshLayout_user.setRefreshing(false);
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
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.notification_cell, parent, false);
            RecyclerView.LayoutParams _lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            view.setLayoutParams(_lp);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            HashMap<String, Object> item = data.get(position);
            String url = String.valueOf(item.get("url"));
            String msgTitle = String.valueOf(item.get("title"));
            String msgMessage = String.valueOf(item.get("message"));
            String msgDate = String.valueOf(item.get("date"));

            int textColor = isNightMode() ? 0xFFFFFFFF : 0xFF222222;
            int dateColor = 0xFF989FA7;

            holder.title.setTextColor(textColor);
            holder.date.setTextColor(dateColor);
            holder.message.setTextColor(textColor);

            holder.title.setText(msgTitle);
            holder.message.setText(msgMessage);
            holder.date.setText(msgDate);
            TextFormatter.formatText(holder.message, msgMessage);
            holder.message.setMovementMethod(LinkMovementMethod.getInstance());

            ArrayList<String> ss = new ArrayList<>();
            ss.add(url);

            holder.image.setOnClickListener(view -> new StfalconImageViewer.Builder<>(holder.itemView.getContext(), ss, (imageView, image) -> Glide.with(holder.itemView.getContext())
                    .load(image)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .placeholder(new ColorDrawable(0xffD3D3D3))
                    .into(imageView))
                    .withStartPosition(0)
                    .withHiddenStatusBar(true)
                    .allowZooming(true)
                    .allowSwipeToDismiss(true)
                    .withTransitionFrom(holder.image)
                    .withDismissListener(() -> holder.image.setVisibility(View.VISIBLE))// Use the method from ProjectScreenshotCell to get the ImageView
                    .show());

            if (url.equals("none")) {
                holder.image.setVisibility(View.GONE);
            } else {
                Glide.with(holder.itemView.getContext())
                        .load(url)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .placeholder(R.color.gray)
                        .centerCrop()
                        .into(holder.image);
            }
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView title, message, date;
            ImageView image;

            public ViewHolder(View itemView) {
                super(itemView);
                title = itemView.findViewById(R.id.title);
                message = itemView.findViewById(R.id.message);
                date = itemView.findViewById(R.id.date);
                image = itemView.findViewById(R.id.image);
            }
        }
    }

}