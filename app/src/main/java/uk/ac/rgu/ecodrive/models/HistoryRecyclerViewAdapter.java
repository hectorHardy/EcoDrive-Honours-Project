package uk.ac.rgu.ecodrive.models;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import uk.ac.rgu.ecodrive.R;

public class HistoryRecyclerViewAdapter extends RecyclerView.Adapter<HistoryRecyclerViewAdapter.HistoryViewHolder>{
    private Context context;
    private List<DriveData> drives;

    public HistoryRecyclerViewAdapter(Context context, List<DriveData> drives){

        super();
        this.context = context;
        this.drives = drives;
        //Log.d("IS IT NULL", this.recipes.get(1).getTitle()); // error, null

    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){

        View itemView = LayoutInflater.from(this.context).inflate(R.layout.history_item, parent, false);
        HistoryViewHolder viewHolder = new HistoryViewHolder(itemView, this);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        DriveData driveData = this.drives.get(position);

        TextView tv_date = holder.itemView.findViewById(R.id.tv_driveDate);
        tv_date.setText(driveData.getDate());

        TextView tv_score = holder.itemView.findViewById(R.id.tv_score);
        tv_score.setText(String.valueOf(driveData.getScore()));
    }

    @Override
    public int getItemCount() {
        return drives != null ? drives.size() : 0;
    }

    class HistoryViewHolder extends RecyclerView.ViewHolder{

        private View itemView;
        private HistoryRecyclerViewAdapter adapter;

        public HistoryViewHolder(@NonNull View itemView, HistoryRecyclerViewAdapter adapter) {
            super(itemView);
            this.itemView = itemView;
            this.adapter = adapter;
        }
    }
}
