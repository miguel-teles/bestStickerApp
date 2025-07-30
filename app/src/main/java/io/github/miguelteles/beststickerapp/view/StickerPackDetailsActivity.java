/*
 * Copyright (c) WhatsApp Inc. and its affiliates.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.github.miguelteles.beststickerapp.view;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.Formatter;
import android.view.Menu;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import io.github.miguelteles.beststickerapp.R;
import io.github.miguelteles.beststickerapp.domain.entity.Sticker;
import io.github.miguelteles.beststickerapp.domain.entity.StickerPack;
import io.github.miguelteles.beststickerapp.exception.StickerException;
import io.github.miguelteles.beststickerapp.exception.handler.StickerExceptionHandler;
import io.github.miguelteles.beststickerapp.repository.contentProvider.StickerUriProvider;
import io.github.miguelteles.beststickerapp.services.StickerPackService;
import io.github.miguelteles.beststickerapp.services.StickerService;
import io.github.miguelteles.beststickerapp.services.interfaces.EntityOperationCallback;
import io.github.miguelteles.beststickerapp.view.recyclerViewAdapters.stickers.StickerPreviewAdapter;

public class StickerPackDetailsActivity extends AddStickerPackToWhatsappActivity {

    private RecyclerView recyclerView;
    private GridLayoutManager layoutManager;
    private StickerPreviewAdapter stickerPreviewAdapter;
    private int numColumns;
    private View btnAddToWhatsapp;
    private StickerPack stickerPack;
    private View divider;
    private ImageView btnAddNewSticker;
    private ImageView btnEditStickerPack;
    private ImageView btnDeleteStickerPack;
    private ImageView btnGoBack;
    private StickerPackService stickerPackService;
    private StickerService stickerService;
    private ProgressBar progressBar;

    private TextView txtNotEnoughStickers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sticker_pack_details);
        stickerPack = getIntent().getParcelableExtra(Extras.EXTRA_STICKER_PACK_DATA);

        declareGlobalComponents();
        try {
            stickerPackService = StickerPackService.getInstance();
            stickerService = StickerService.getInstance();
        } catch (StickerException ex) {
            StickerExceptionHandler.handleException(ex, this);
        }

        assembleRecyclerView();
        loadStickersOnScreen();
        loadStickerPackInfoOnComponents();
        setOnClickListeners();
        setBtnAddToWhatsappProperties();
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        findViewById(R.id.sticker_pack_animation_indicator).setVisibility(stickerPack.isAnimatedStickerPack() ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            stickerPack = stickerPackService.fetchStickerPackAssets(stickerPack);
            loadStickersOnScreen();
            setBtnAddToWhatsappProperties();
        } catch (StickerException ex) {
            StickerExceptionHandler.handleException(ex, this);
        }
    }

    private void setBtnAddToWhatsappProperties() {
        Context context = this;
        if (stickerPack.getStickers() != null && stickerPack.getStickers().size() < 3) {
            btnAddToWhatsapp.setOnClickListener( v -> Toast.makeText(context, R.string.not_enough_stickers_to_add_to_whatsapp, Toast.LENGTH_LONG).show());
            btnAddToWhatsapp.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.shape_btn_default_disabled, null));

            txtNotEnoughStickers.setVisibility(View.VISIBLE);
        } else {
            setAddStickerPackToWhatsappListener();
            btnAddToWhatsapp.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.shape_btn_default, null));

            txtNotEnoughStickers.setVisibility(View.GONE);
        }
    }

    private void loadStickerPackInfoOnComponents() {
        TextView packNameTextView = findViewById(R.id.pack_name);
        TextView packPublisherTextView = findViewById(R.id.author);
        ImageView packTrayIcon = findViewById(R.id.tray_image);
        TextView packSizeTextView = findViewById(R.id.pack_size);

        packNameTextView.setText(stickerPack.getName());
        packPublisherTextView.setText("Autor: " + stickerPack.getPublisher());
        packTrayIcon.setImageURI(StickerUriProvider.getInstance().getStickerPackResizedAssetUri(stickerPack.getIdentifier(), stickerPack.getResizedTrayImageFile()));
        packSizeTextView.setText(Formatter.formatShortFileSize(this, stickerPack.getTotalSize()));
    }

    private void assembleRecyclerView() {
        layoutManager = new GridLayoutManager(this, 1);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.getViewTreeObserver().addOnGlobalLayoutListener(pageLayoutListener);
        recyclerView.addOnScrollListener(dividerScrollListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    protected void onPause() {
        super.onPause();
    }

    private void loadStickersOnScreen() {
        stickerPreviewAdapter = new StickerPreviewAdapter(getLayoutInflater(),
                R.drawable.sticker_error,
                getResources().getDimensionPixelSize(R.dimen.sticker_pack_details_image_size),
                getResources().getDimensionPixelSize(R.dimen.sticker_pack_details_image_padding),
                stickerPack,
                findViewById(R.id.sticker_details_expanded),
                this,
                this);
        recyclerView.setAdapter(stickerPreviewAdapter);
    }

    private void declareGlobalComponents() {
        btnAddNewSticker = findViewById(R.id.btn_add_new_sticker);
        btnEditStickerPack = findViewById(R.id.btn_edit_pack);
        btnDeleteStickerPack = findViewById(R.id.btn_delete_pack);
        btnAddToWhatsapp = findViewById(R.id.add_to_whatsapp_button);
        btnGoBack = findViewById(R.id.btn_sticker_pack_details_go_back);
        recyclerView = findViewById(R.id.sticker_list);
        divider = findViewById(R.id.divider);
        progressBar = findViewById(R.id.pg_sticker_pack_edit);
        txtNotEnoughStickers = findViewById(R.id.txtNotEnoughStickers);
    }

    private void setOnClickListeners() {
        btnEditStickerPack.setOnClickListener(btn -> editStickerPack());
        btnDeleteStickerPack.setOnClickListener(btn -> deleteStickerPack());
        btnAddNewSticker.setOnClickListener(btn -> addNewSticker());
        btnGoBack.setOnClickListener(btn -> addGoBack());
    }

    private void setAddStickerPackToWhatsappListener() {
        btnAddToWhatsapp.setOnClickListener(v -> addStickerPackToWhatsApp(stickerPack.getIdentifier(), stickerPack.getName()));
    }

    private void addGoBack() {
        finish();
    }

    private void addNewSticker() {
        Intent intent = new Intent(this, AddStickerActivity.class);
        intent.putExtra(AddStickerPackActivity.Extras.STICKER_PACK, stickerPack);
        startActivity(intent);
    }

    private void deleteStickerPack() {
        Context context = this;
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this, R.style.alertDialogStyle);
        alertBuilder.setMessage(R.string.SURE_DELETE_STICKER_PACK)
                .setPositiveButton(R.string.YES_SIR, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        progressBar.setVisibility(View.VISIBLE);
                        stickerPackService.deleteStickerPack(stickerPack, createStickerPackDeletionCallback(context));
                        Intent intent = new Intent(context, StickerPackListActivity.class);
                        startActivity(intent);
                    }
                })
                .setNegativeButton(R.string.NOT_TODAY, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }).create().show();
    }

    private EntityOperationCallback<StickerPack> createStickerPackDeletionCallback(Context context) {
        return new EntityOperationCallback<StickerPack>() {
            @Override
            public void onCreationFinish(StickerPack createdEntity, StickerException stickerException) {
                if (stickerException == null) {
                    onProgressUpdate(100);
                    finish();
                } else {
                    progressBar.setProgress(0);
                    progressBar.setVisibility(View.GONE);
                    StickerExceptionHandler.handleException(stickerException, context);
                }
            }

            @Override
            public void onProgressUpdate(int process) {
                progressBar.setProgress(process, true);
            }
        };
    }

    private void editStickerPack() {
        Intent intent = new Intent(this, AddStickerPackActivity.class);
        intent.putExtra(AddStickerPackActivity.Extras.STICKER_PACK, stickerPack);
        startActivityForResult(intent, 0);
    }

    private final ViewTreeObserver.OnGlobalLayoutListener pageLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            setNumColumns(recyclerView.getWidth() / recyclerView.getContext().getResources().getDimensionPixelSize(R.dimen.sticker_pack_details_image_size));
        }
    };

    private void setNumColumns(int numColumns) {
        if (this.numColumns != numColumns) {
            layoutManager.setSpanCount(numColumns);
            this.numColumns = numColumns;
            if (stickerPreviewAdapter != null) {
                stickerPreviewAdapter.notifyDataSetChanged();
            }
        }
    }

    private final RecyclerView.OnScrollListener dividerScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(@NonNull final RecyclerView recyclerView, final int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            updateDivider(recyclerView);
        }

        @Override
        public void onScrolled(@NonNull final RecyclerView recyclerView, final int dx, final int dy) {
            super.onScrolled(recyclerView, dx, dy);
            updateDivider(recyclerView);
        }

        private void updateDivider(RecyclerView recyclerView) {
            boolean showDivider = recyclerView.computeVerticalScrollOffset() > 0;
            if (divider != null) {
                divider.setVisibility(showDivider ? View.VISIBLE : View.INVISIBLE);
            }
        }
    };

    public void deleteSticker(Sticker sticker, StickerPack stickerPack) {
        try {
            this.stickerPackService.deleteSticker(sticker, stickerPack);
            stickerPack.getStickers().remove(sticker);
            loadStickersOnScreen();
            setBtnAddToWhatsappProperties();
        } catch (StickerException ex) {
            StickerExceptionHandler.handleException(ex, this);
        }
    }

    public static class Extras {
        /**
         * Do not change below values of below 3 lines as this is also used by WhatsApp
         */
        public static final String EXTRA_STICKER_PACK_ID = "sticker_pack_id";
        public static final String EXTRA_STICKER_PACK_AUTHORITY = "sticker_pack_authority";
        public static final String EXTRA_STICKER_PACK_NAME = "sticker_pack_name";
        public static final String EXTRA_STICKER_PACK_WEBSITE = "sticker_pack_website";
        public static final String EXTRA_STICKER_PACK_EMAIL = "sticker_pack_email";
        public static final String EXTRA_STICKER_PACK_PRIVACY_POLICY = "sticker_pack_privacy_policy";
        public static final String EXTRA_STICKER_PACK_LICENSE_AGREEMENT = "sticker_pack_license_agreement";
        public static final String EXTRA_STICKER_PACK_TRAY_ICON = "sticker_pack_tray_icon";
        public static final String EXTRA_SHOW_UP_BUTTON = "show_up_button";
        public static final String EXTRA_STICKER_PACK_DATA = "sticker_pack";
    }
}
