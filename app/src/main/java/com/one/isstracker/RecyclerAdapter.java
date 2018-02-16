package com.one.isstracker;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.EntityHolder> {
    LayoutInflater inflater;
    ArrayList<Entity> alPasses;

    public RecyclerAdapter(ArrayList<Entity> alPasses, LayoutInflater inflater)
    {
        this.alPasses = alPasses;
        this.inflater = inflater;
    }

    @Override
    public EntityHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View convertView = inflater.inflate(R.layout.custom_recycle_view, null);
        EntityHolder viewHolder = new EntityHolder(convertView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(EntityHolder holder, int position) {
        Entity entity = alPasses.get(position);

        holder.tvRiseTime.setText(entity.getRiseTimeEntity());
        holder.tvDuration.setText(entity.getDurationEntity());
    }

    @Override
    public int getItemCount() {
        return alPasses.size();
    }

    class EntityHolder extends RecyclerView.ViewHolder{
        TextView tvRiseTime;
        TextView tvDuration;

        public EntityHolder(View convertView) {
            super(convertView);
            tvRiseTime = (TextView) convertView.findViewById(R.id.tvRiseTime);
            tvDuration = (TextView) convertView.findViewById(R.id.tvDuration);
        }
    }
}
