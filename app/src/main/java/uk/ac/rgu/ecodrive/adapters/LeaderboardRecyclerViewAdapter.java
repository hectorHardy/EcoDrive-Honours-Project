package uk.ac.rgu.ecodrive.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import uk.ac.rgu.ecodrive.R;
import uk.ac.rgu.ecodrive.models.UserData;

public class LeaderboardRecyclerViewAdapter extends RecyclerView.Adapter<LeaderboardRecyclerViewAdapter.LeaderboardViewHolder>{
    private Context context;
    private List<UserData> drives;

    public LeaderboardRecyclerViewAdapter(Context context, List<UserData> drives){

        super();
        this.context = context;
        this.drives = drives;
        //Log.d("IS IT NULL", this.recipes.get(1).getTitle()); // error, null

    }

    @NonNull
    @Override
    public LeaderboardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){

        View itemView = LayoutInflater.from(this.context).inflate(R.layout.leaderboard_item, parent, false);
        LeaderboardViewHolder viewHolder = new LeaderboardViewHolder(itemView, this);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull LeaderboardViewHolder holder, int position) {
        UserData driveData = this.drives.get(position);

        TextView tv_position = holder.itemView.findViewById(R.id.tv_position);
        tv_position.setText(String.valueOf(position + 1));
        Log.d("position test", "" + position);

        TextView tv_userName = holder.itemView.findViewById(R.id.tv_userName);
        tv_userName.setText(driveData.getUserName());

        TextView tv_score = holder.itemView.findViewById(R.id.tv_totalScore);
        tv_score.setText(String.valueOf(driveData.getTotalScore()));
    }

    @Override
    public int getItemCount() {
        return drives != null ? drives.size() : 0;
    }

    class LeaderboardViewHolder extends RecyclerView.ViewHolder{

        private View itemView;
        private LeaderboardRecyclerViewAdapter adapter;

        public LeaderboardViewHolder(@NonNull View itemView, LeaderboardRecyclerViewAdapter adapter) {
            super(itemView);
            this.itemView = itemView;
            this.adapter = adapter;
        }
    }
}
