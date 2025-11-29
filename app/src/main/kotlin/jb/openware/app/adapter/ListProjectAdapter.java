package in.afi.codekosh.adapter;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;

import in.afi.codekosh.activity.project.ProjectViewActivity;

public class ListProjectAdapter extends RecyclerView.Adapter<ListProjectAdapter.ViewHolder> {

    private final ArrayList<HashMap<String, Object>> _data;
    private final Activity context;
    private final HashMap<String, String> stringHashMap;
    private final int code;

    public ListProjectAdapter(ArrayList<HashMap<String, Object>> _data, Activity context, HashMap<String, String> hashMap, int code) {
        this._data = _data;
        this.context = context;
        this.stringHashMap = hashMap;
        this.code = code;
    }

    @NonNull
    @Override
    public ListProjectAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ProjectListCell projectListCell = new ProjectListCell(context);
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        projectListCell.setLayoutParams(layoutParams);
        return new ListProjectAdapter.ViewHolder(projectListCell);
    }

    @Override
    public void onBindViewHolder(@NonNull ListProjectAdapter.ViewHolder holder, final int position) {
        HashMap<String, Object> item = _data.get(position);
        String icon = String.valueOf(item.get("icon"));
        String title = String.valueOf(item.get("title"));
        String comments = String.valueOf(item.get("comments"));
        String likes = String.valueOf(item.get("likes"));
        String downloads = String.valueOf(item.get("downloads"));
        HashMap<String, String> hashMap = new HashMap<>();
        if (stringHashMap.containsKey(String.valueOf(item.get("uid")))) {
            hashMap.put("name", stringHashMap.get(String.valueOf(item.get("uid"))));
        }
        hashMap.put("name", String.valueOf(item.get("name")));
        hashMap.put("title", title);
        hashMap.put("icon", icon);
        hashMap.put("comments", comments);
        hashMap.put("likes", likes);
        hashMap.put("downloads", downloads);
        ((ProjectListCell) holder.itemView).setData(hashMap);

        holder.itemView.setOnClickListener(view -> {
            SharedPreferences developer = context.getSharedPreferences("developer", Activity.MODE_PRIVATE);
            if (code == 1) {
                developer.edit().putString("type", "Free").apply();
            }else {
                developer.edit().putString("type", "Paid").apply();
            }
            Intent intent = new Intent(context, ProjectViewActivity.class);
            intent.putExtra("key", String.valueOf(item.get("key")));
            intent.putExtra("uid", String.valueOf(item.get("uid")));
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return _data.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ViewHolder(View v) {
            super(v);
        }
    }
}


