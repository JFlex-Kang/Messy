package devicewills.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cracking.jflex.devicewilly.R;

import java.util.ArrayList;

import devicewills.objects.UserSettingProfile;
import devicewills.utils.TimeCalculator;

/**
 *
 */
public class DataListAdapter extends RecyclerView.Adapter<DataListAdapter.ViewHolder> {

    private ArrayList<UserSettingProfile> mDataset = new ArrayList<>();

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView mTextViewPercent;
        public TextView mTextViewMsg;
        public TextView mTextViewDate;

        public ViewHolder(View v) {
            super(v);
            mTextViewMsg = (TextView) v.findViewById(R.id.layout_item_msg);
            mTextViewPercent = (TextView) v.findViewById(R.id.layout_item_percent);
            mTextViewDate = (TextView) v.findViewById(R.id.layout_item_date);
        }
    }

    public DataListAdapter(ArrayList<UserSettingProfile> dataset) {
        mDataset.clear();
        mDataset.addAll(dataset);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_setting_list, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        TimeCalculator calculator = new TimeCalculator();

        holder.mTextViewPercent.setText("Percent: " + String.valueOf(mDataset.get(position).getmBatteryPerc()));
        holder.mTextViewMsg.setText("Message: " + mDataset.get(position).getmMessage());
        holder.mTextViewDate.setText(String.valueOf(calculator.calculateTime(mDataset.get(position).getmInsertDate())));
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

}
