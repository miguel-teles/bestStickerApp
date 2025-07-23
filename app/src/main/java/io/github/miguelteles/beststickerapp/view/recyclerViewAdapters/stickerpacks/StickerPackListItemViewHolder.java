/*
 * Copyright (c) WhatsApp Inc. and its affiliates.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.github.miguelteles.beststickerapp.view.recyclerViewAdapters.stickerpacks;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import io.github.miguelteles.beststickerapp.R;

public class StickerPackListItemViewHolder extends RecyclerView.ViewHolder {

    private final View container;
    private final TextView titleView;
    private final TextView publisherView;
    private final TextView filesizeView;
    private final ImageView animatedStickerPackIndicator;
    private final LinearLayout imageRowView;

    public StickerPackListItemViewHolder(final View itemView) {
        super(itemView);
        container = itemView;
        titleView = itemView.findViewById(R.id.sticker_pack_title);
        publisherView = itemView.findViewById(R.id.sticker_pack_publisher);
        filesizeView = itemView.findViewById(R.id.sticker_pack_filesize);
        imageRowView = itemView.findViewById(R.id.sticker_packs_list_item_image_list);
        animatedStickerPackIndicator = itemView.findViewById(R.id.sticker_pack_animation_indicator);
    }

    public View getContainer() {
        return container;
    }

    public TextView getTitleView() {
        return titleView;
    }

    public TextView getPublisherView() {
        return publisherView;
    }

    public TextView getFilesizeView() {
        return filesizeView;
    }

    public ImageView getAnimatedStickerPackIndicator() {
        return animatedStickerPackIndicator;
    }

    public LinearLayout getImageRowView() {
        return imageRowView;
    }
}