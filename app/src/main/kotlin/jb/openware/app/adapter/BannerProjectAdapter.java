package in.afi.codekosh.adapter;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.HashMap;

import in.afi.codekosh.activity.project.ProjectViewActivity;


public class BannerProjectAdapter extends RecyclerView.Adapter<BannerProjectAdapter.ViewHolder> {

    private final ArrayList<HashMap<String, Object>> _data;
    private final Activity context;
    private final int id;

    public BannerProjectAdapter(ArrayList<HashMap<String, Object>> _data, Activity context, int id) {
        this._data = _data;
        this.context = context;
        this.id = id;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(new BannerCell(context));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        HashMap<String, Object> item = _data.get(position);
        String icon = String.valueOf(item.get("icon"));
        String title = String.valueOf(item.get("title"));
        String category = String.valueOf(item.get("category"));
        String size = String.valueOf(item.get("size"));
        ArrayList<String> ss = new Gson().fromJson(String.valueOf(item.get("screenshots")), new TypeToken<ArrayList<String>>() {}.getType());
        String screen = ss.get(0);
        ((BannerCell) holder.itemView).setData(icon, title, category, size, screen);

        holder.itemView.setOnClickListener(view -> {
            SharedPreferences developer = context.getSharedPreferences("developer", Activity.MODE_PRIVATE);
            if (id == 1) {
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

