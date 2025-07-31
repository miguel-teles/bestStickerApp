/*
 * Copyright (c) WhatsApp Inc. and its affiliates.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.github.miguelteles.beststickerapp.view.recyclerViewAdapters.stickerpacks;

import android.content.Context;
import android.content.Intent;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import io.github.miguelteles.beststickerapp.R;
import io.github.miguelteles.beststickerapp.domain.entity.Sticker;
import io.github.miguelteles.beststickerapp.domain.entity.StickerPack;
import io.github.miguelteles.beststickerapp.exception.StickerException;
import io.github.miguelteles.beststickerapp.exception.handler.StickerExceptionHandler;
import io.github.miguelteles.beststickerapp.repository.contentProvider.StickerUriProvider;
import io.github.miguelteles.beststickerapp.services.FileResourceManagement;
import io.github.miguelteles.beststickerapp.utils.Utils;
import io.github.miguelteles.beststickerapp.view.StickerPackDetailsActivity;

import com.facebook.drawee.view.SimpleDraweeView;

import java.util.List;

public class StickerPackListAdapter extends RecyclerView.Adapter<StickerPackListItemViewHolder> {
    @NonNull
    private List<StickerPack> stickerPacks;
    private int maxNumberOfStickersInARow;
    private int minMarginBetweenImages;

    public StickerPackListAdapter(@NonNull List<StickerPack> stickerPacks) {
        this.stickerPacks = stickerPacks;
    }

    @NonNull
    @Override
    public StickerPackListItemViewHolder onCreateViewHolder(@NonNull final ViewGroup viewGroup, final int i) {
        final Context context = viewGroup.getContext();
        final LayoutInflater layoutInflater = LayoutInflater.from(context);
        final View stickerPackRow = layoutInflater.inflate(R.layout.sticker_packs_list_item, viewGroup, false);
        return new StickerPackListItemViewHolder(stickerPackRow);
    }

    @Override
    public void onBindViewHolder(@NonNull final StickerPackListItemViewHolder viewHolder, final int index) {

        StickerPack pack = stickerPacks.get(index);
        final Context context = viewHolder.getPublisherView().getContext();
        viewHolder.getPublisherView().setText(pack.getPublisher());
        viewHolder.getFilesizeView().setText(Formatter.formatShortFileSize(context, pack.getTotalSize()));

        viewHolder.getTitleView().setText(pack.getName());
        viewHolder.getContainer().setOnClickListener(view -> {
            Intent intent = new Intent(view.getContext(), StickerPackDetailsActivity.class);
            intent.putExtra(StickerPackDetailsActivity.Extras.EXTRA_SHOW_UP_BUTTON, true);
            intent.putExtra(StickerPackDetailsActivity.Extras.EXTRA_STICKER_PACK_DATA, pack);
            view.getContext().startActivity(intent);
        });
        viewHolder.getImageRowView().removeAllViews();
        //if this sticker pack contains less stickers than the max, then take the smaller size.
        int actualNumberOfStickersToShow = Math.min(maxNumberOfStickersInARow, pack.getStickers().size());

        try {
            for (int i = 0; i < actualNumberOfStickersToShow; i++) {
                final SimpleDraweeView rowImage = (SimpleDraweeView) LayoutInflater.from(context).inflate(R.layout.sticker_packs_list_image_item, viewHolder.getImageRowView(), false);
                rowImage.setImageURI(FileResourceManagement.getInstance().getFile(pack.getFolderName(), pack.getStickers().get(i).getStickerImageFile()));
                final LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) rowImage.getLayoutParams();
                final int marginBetweenImages = minMarginBetweenImages - lp.leftMargin - lp.rightMargin;
                if (i != actualNumberOfStickersToShow - 1 && marginBetweenImages > 0) { //do not set the margin for the last image
                    lp.setMargins(lp.leftMargin, lp.topMargin, lp.rightMargin + marginBetweenImages, lp.bottomMargin);
                    rowImage.setLayoutParams(lp);
                }
                viewHolder.getImageRowView().addView(rowImage);
            }
            viewHolder.getAnimatedStickerPackIndicator().setVisibility(pack.isAnimatedStickerPack() ? View.VISIBLE : View.GONE);
        } catch (StickerException ex) {
            StickerExceptionHandler.handleException(ex, Utils.getApplicationContext());
        }
    }

    @Override
    public int getItemCount() {
        return stickerPacks.size();
    }

    public void setImageRowSpec(int maxNumberOfStickersInARow, int minMarginBetweenImages) {
        this.minMarginBetweenImages = minMarginBetweenImages;
        if (this.maxNumberOfStickersInARow != maxNumberOfStickersInARow) {
            this.maxNumberOfStickersInARow = maxNumberOfStickersInARow;
            notifyDataSetChanged();
        }
    }

    public void setStickerPackList(List<StickerPack> stickerPackList) {
        this.stickerPacks = stickerPackList;
    }

    public interface OnAddButtonClickedListener {
        void onAddButtonClicked(StickerPack stickerPack);
    }
}
