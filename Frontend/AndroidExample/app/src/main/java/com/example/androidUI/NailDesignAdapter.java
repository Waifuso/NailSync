package com.example.androidUI;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.toolbox.ImageLoader;
import com.example.androidexample.R;
import com.example.androidexample.VolleySingleton;

import java.util.List;

public class NailDesignAdapter extends RecyclerView.Adapter<NailDesignAdapter.NailDesignViewHolder> {

    private final Context context;
    private final List<NailDesignModel> designs;
    private final OnNailDesignSelectedListener listener;
    private final ImageLoader imageLoader;

    public interface OnNailDesignSelectedListener {
        void onNailDesignSelected(NailDesignModel design);
    }

    public NailDesignAdapter(Context context, List<NailDesignModel> designs, OnNailDesignSelectedListener listener) {
        this.context = context;
        this.designs = designs;
        this.listener = listener;
        this.imageLoader = VolleySingleton.getInstance(context).getImageLoader();
    }

    @NonNull
    @Override
    public NailDesignViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_nail_design, parent, false);
        return new NailDesignViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NailDesignViewHolder holder, int position) {
        NailDesignModel design = designs.get(position);
        holder.designName.setText(design.getName());

        // Load image with Volley
        imageLoader.get(design.getImageUrl(), new ImageLoader.ImageListener() {
            @Override
            public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                Bitmap bitmap = response.getBitmap();
                if (bitmap != null) {
                    holder.designImage.setImageBitmap(bitmap);
                }
            }

            @Override
            public void onErrorResponse(com.android.volley.VolleyError error) {
                // You could set a placeholder error image here
                holder.designImage.setImageResource(android.R.drawable.ic_dialog_alert);
            }
        });

        // Set click listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onNailDesignSelected(design);
            }
        });
    }

    @Override
    public int getItemCount() {
        return designs.size();
    }

    static class NailDesignViewHolder extends RecyclerView.ViewHolder {
        final ImageView designImage;
        final TextView designName;

        NailDesignViewHolder(@NonNull View itemView) {
            super(itemView);
            designImage = itemView.findViewById(R.id.nailDesignImage);
            designName = itemView.findViewById(R.id.nailDesignName);
        }
    }
}