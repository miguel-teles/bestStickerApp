/*
 * Copyright (c) WhatsApp Inc. and its affiliates.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.github.miguelteles.beststickerapp.view.recyclerViewAdapters.stickers;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import io.github.miguelteles.beststickerapp.R;
import io.github.miguelteles.beststickerapp.domain.entity.Sticker;
import io.github.miguelteles.beststickerapp.domain.entity.StickerPack;
import io.github.miguelteles.beststickerapp.repository.contentProvider.StickerUriProvider;
import io.github.miguelteles.beststickerapp.view.StickerPackDetailsActivity;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;

public class StickerPreviewAdapter extends RecyclerView.Adapter<StickerPreviewViewHolder> {

    private static final float COLLAPSED_STICKER_PREVIEW_BACKGROUND_ALPHA = 1f;
    private static final float EXPANDED_STICKER_PREVIEW_BACKGROUND_ALPHA = 0.2f;
    @NonNull
    private final StickerPack stickerPack;
    private final int cellSize;
    private final int cellLimit;
    private final int cellPadding;
    private final int errorResource;
    private final LinearLayout expandedPreview;
    private SimpleDraweeView expandedStickerPreview;
    private ImageView btnDeleteSticker;
    private Sticker expandedSticker;
    private final LayoutInflater layoutInflater;
    private RecyclerView recyclerView;
    private View clickedStickerPreview;

    private StickerPackDetailsActivity stickerPackDetailsActivity;
    float expandedViewLeftX;
    float expandedViewTopY;

    Context context;

    public StickerPreviewAdapter(
            @NonNull final LayoutInflater layoutInflater,
            final int errorResource,
            final int cellSize,
            final int cellPadding,
            @NonNull final StickerPack stickerPack,
            final LinearLayout expandedStickerView,
            final StickerPackDetailsActivity stickerPackDetailsActivity,
            Context context) {
        this.cellSize = cellSize;
        this.cellPadding = cellPadding;
        this.cellLimit = 0;
        this.layoutInflater = layoutInflater;
        this.errorResource = errorResource;
        this.stickerPack = stickerPack;
        this.expandedPreview = expandedStickerView;
        this.context = context;
        this.stickerPackDetailsActivity = stickerPackDetailsActivity;

        for (int childComponentIndex = 0; childComponentIndex < expandedPreview.getChildCount(); childComponentIndex++) {
            View childComponent = expandedPreview.getChildAt(childComponentIndex);
            if (childComponent.getId() == R.id.sticker_details_expanded_sticker) {
                this.expandedStickerPreview = (SimpleDraweeView) childComponent;
            } else if (childComponent.getId() == R.id.btn_delete_sticker) {
                this.btnDeleteSticker = (ImageView) childComponent;
            }
        }
    }

    @NonNull
    @Override
    public StickerPreviewViewHolder onCreateViewHolder(@NonNull final ViewGroup viewGroup, final int i) {
        View itemView = layoutInflater.inflate(R.layout.sticker_image_item, viewGroup, false);
        StickerPreviewViewHolder vh = new StickerPreviewViewHolder(itemView);

        ViewGroup.LayoutParams layoutParams = vh.stickerPreviewView.getLayoutParams();
        layoutParams.height = cellSize;
        layoutParams.width = cellSize;
        vh.stickerPreviewView.setLayoutParams(layoutParams);
        vh.stickerPreviewView.setPadding(cellPadding, cellPadding, cellPadding, cellPadding);

        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull final StickerPreviewViewHolder stickerPreviewViewHolder, final int i) {
        stickerPreviewViewHolder.stickerPreviewView.setImageResource(errorResource);
        stickerPreviewViewHolder.stickerPreviewView.setImageURI(StickerUriProvider.getInstance().getStickerAssetUri(stickerPack.getIdentifier(), stickerPack.getStickers().get(i).getStickerImageFile()));
        stickerPreviewViewHolder.stickerPreviewView.setOnClickListener(v -> expandPreview(i, stickerPreviewViewHolder.stickerPreviewView));
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.recyclerView = recyclerView;
        recyclerView.addOnScrollListener(hideExpandedViewScrollListener);
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        recyclerView.removeOnScrollListener(hideExpandedViewScrollListener);
        this.recyclerView = null;
    }

    private final RecyclerView.OnScrollListener hideExpandedViewScrollListener =
            new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    if (dx != 0 || dy != 0) {
                        hideExpandedStickerPreview();
                    }
                }
            };

    private void positionExpandedStickerPreview(int selectedPosition) {
        if (expandedPreview != null) {
            // Calculate the view's center (x, y), then use expandedStickerPreview's height and
            // width to
            // figure out what where to position it.
            final ViewGroup.MarginLayoutParams recyclerViewLayoutParams =
                    ((ViewGroup.MarginLayoutParams) recyclerView.getLayoutParams());
            final int recyclerViewLeftMargin = recyclerViewLayoutParams.leftMargin;
            final int recyclerViewRightMargin = recyclerViewLayoutParams.rightMargin;
            final int recyclerViewWidth = recyclerView.getWidth();
            final int recyclerViewHeight = recyclerView.getHeight();

            final StickerPreviewViewHolder clickedViewHolder =
                    (StickerPreviewViewHolder)
                            recyclerView.findViewHolderForAdapterPosition(selectedPosition);
            if (clickedViewHolder == null) {
                hideExpandedStickerPreview();
                return;
            }
            clickedStickerPreview = clickedViewHolder.itemView;
            final float clickedViewCenterX =
                    clickedStickerPreview.getX()
                            + recyclerViewLeftMargin
                            + clickedStickerPreview.getWidth() / 2f;
            final float clickedViewCenterY =
                    clickedStickerPreview.getY() + clickedStickerPreview.getHeight() / 2f;

            expandedViewLeftX = clickedViewCenterX - expandedPreview.getWidth() / 2f;
            expandedViewTopY = clickedViewCenterY - expandedPreview.getHeight() / 2f;

            // If the new x or y positions are negative, anchor them to 0 to avoid clipping
            // the left side of the device and the top of the recycler view.
            expandedViewLeftX = Math.max(expandedViewLeftX, 0);
            expandedViewTopY = Math.max(expandedViewTopY, 0);

            // If the bottom or right sides are clipped, we need to move the top left positions
            // so that those sides are no longer clipped.
            final float adjustmentX =
                    Math.max(
                            expandedViewLeftX
                                    + expandedPreview.getWidth()
                                    - recyclerViewWidth
                                    - recyclerViewRightMargin,
                            0);
            final float adjustmentY =
                    Math.max(expandedViewTopY + expandedPreview.getHeight() - recyclerViewHeight, 0);

            expandedViewLeftX -= adjustmentX;
            expandedViewTopY -= adjustmentY;


            expandedPreview.setX(expandedViewLeftX);
            expandedPreview.setY(expandedViewTopY);
        }
    }

    private void expandPreview(int position, View clickedStickerPreview) {
        if (isStickerPreviewExpanded()) {
            hideExpandedStickerPreview();
            return;
        }

        this.clickedStickerPreview = clickedStickerPreview;
        if (expandedPreview != null) {
            positionExpandedStickerPreview(position);
            this.expandedSticker = stickerPack.getStickers().get(position);
            final Uri stickerAssetUri = StickerUriProvider.getInstance().getStickerAssetUri(stickerPack.getIdentifier(), expandedSticker.getStickerImageFile());
            DraweeController controller = Fresco.newDraweeControllerBuilder()
                    .setUri(stickerAssetUri)
                    .setAutoPlayAnimations(true)
                    .build();
            expandedStickerPreview.setImageResource(errorResource);
            expandedStickerPreview.setController(controller);

            expandedPreview.setVisibility(View.VISIBLE);
            recyclerView.setAlpha(EXPANDED_STICKER_PREVIEW_BACKGROUND_ALPHA);

            expandedPreview.setOnClickListener(v -> hideExpandedStickerPreview());
            btnDeleteSticker.setOnClickListener(v -> deleteSticker(expandedSticker));
        }
    }

    private void deleteSticker(Sticker sticker) {
        hideExpandedStickerPreview();
        stickerPackDetailsActivity.deleteSticker(sticker, stickerPack);
    }

    public void hideExpandedStickerPreview() {
        if (isStickerPreviewExpanded() && expandedPreview != null) {
            clickedStickerPreview.setVisibility(View.VISIBLE);
            expandedPreview.setVisibility(View.INVISIBLE);
            recyclerView.setAlpha(COLLAPSED_STICKER_PREVIEW_BACKGROUND_ALPHA);
        }
    }

    private boolean isStickerPreviewExpanded() {
        return expandedPreview != null && expandedPreview.getVisibility() == View.VISIBLE;
    }

    @Override
    public int getItemCount() {
        int numberOfPreviewImagesInPack;
        numberOfPreviewImagesInPack = stickerPack.getStickers().size();
        if (cellLimit > 0) {
            return Math.min(numberOfPreviewImagesInPack, cellLimit);
        }
        return numberOfPreviewImagesInPack;
    }
}
