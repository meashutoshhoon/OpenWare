package in.afi.codekosh.adapter;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class JavaCodesAdapter extends RecyclerView.Adapter<JavaCodesAdapter.ViewHolder> {

    private final ArrayList<TitleListItem> data;
    private final Activity activity;
    private OnItemClickListener onItemClickListener;

    public JavaCodesAdapter(ArrayList<TitleListItem> data, Activity activity) {
        this.data = data;
        this.activity = activity;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(new JavaCodesListCell(activity));
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }


    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        ((JavaCodesListCell) holder.itemView).setData(data.get(position));

        holder.itemView.setOnClickListener(view -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ViewHolder(View v) {
            super(v);
        }
    }
}