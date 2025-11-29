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

public class BaseProjectAdapter extends RecyclerView.Adapter<BaseProjectAdapter.ViewHolder> {

    private final ArrayList<HashMap<String, Object>> _data;
    private final Activity context;

    public BaseProjectAdapter(ArrayList<HashMap<String, Object>> _data, Activity context) {
        this._data = _data;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(new BaseProjectCell(context));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        HashMap<String, Object> item = _data.get(position);
        String icon = String.valueOf(item.get("icon"));
        String title = String.valueOf(item.get("title"));
        String comments = String.valueOf(item.get("comments"));
        String likes = String.valueOf(item.get("likes"));
        ((BaseProjectCell) holder.itemView).setData(icon, title, likes, comments);

        holder.itemView.setOnClickListener(view -> {
            SharedPreferences developer = context.getSharedPreferences("developer", Activity.MODE_PRIVATE);
            developer.edit().putString("type", "Free").apply();
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
