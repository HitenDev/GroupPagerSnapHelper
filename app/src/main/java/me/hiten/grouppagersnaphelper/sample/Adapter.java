package me.hiten.grouppagersnaphelper.sample;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {


    private List<String> mData;

    private AverageMeasure mAverageMeasure;

    public Adapter(List<String> data, AverageMeasure averageMeasure) {
        this.mData = data;
        this.mAverageMeasure = averageMeasure;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.adapter, viewGroup, false);
        if (mAverageMeasure != null) {
            mAverageMeasure.onViewHolderCreate((RecyclerView) viewGroup, view);
        }
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        String item = mData.get(i);
        if (TextUtils.isEmpty(item)){
            viewHolder.itemView.setVisibility(View.INVISIBLE);
        }else {
            viewHolder.itemView.setVisibility(View.VISIBLE);
        }
        viewHolder.textView.setText(item);
    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public ImageView imageView;
        public TextView textView;

        public ViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            imageView = itemView.findViewById(R.id.imageView);
            textView = itemView.findViewById(R.id.textView);
        }

        @Override
        public void onClick(View v) {
        }
    }
}
