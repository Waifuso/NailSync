package com.example.androidUI;

import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Custom item decoration for adding spacing around RecyclerView items.
 */
public class ServiceItemDecoration extends RecyclerView.ItemDecoration {
    private final int verticalSpacing;
    private final int horizontalSpacing;

    /**
     * Constructor for ItemDecoration.
     *
     * @param verticalSpacing   Vertical spacing in pixels.
     * @param horizontalSpacing Horizontal spacing in pixels.
     */
    public ServiceItemDecoration(int verticalSpacing, int horizontalSpacing) {
        this.verticalSpacing = verticalSpacing;
        this.horizontalSpacing = horizontalSpacing;
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view,
                               @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        outRect.left = horizontalSpacing;
        outRect.right = horizontalSpacing;

        // Add top spacing for all items except the first one
        if (parent.getChildAdapterPosition(view) > 0) {
            outRect.top = verticalSpacing;
        }

        // Add bottom spacing for all items
        outRect.bottom = verticalSpacing;
    }
}