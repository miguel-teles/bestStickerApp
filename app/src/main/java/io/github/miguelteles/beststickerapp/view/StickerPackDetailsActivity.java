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
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.format.Formatter;
import android.view.Menu;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.lang.ref.WeakReference;

import io.github.miguelteles.beststickerapp.R;
import io.github.miguelteles.beststickerapp.domain.entity.Sticker;
import io.github.miguelteles.beststickerapp.domain.entity.StickerPack;
import io.github.miguelteles.beststickerapp.exception.StickerException;
import io.github.miguelteles.beststickerapp.exception.StickerExceptionHandler;
import io.github.miguelteles.beststickerapp.repository.contentProvider.StickerUriProvider;
import io.github.miguelteles.beststickerapp.services.StickerPackServiceImpl;
import io.github.miguelteles.beststickerapp.services.StickerServiceImpl;
import io.github.miguelteles.beststickerapp.services.interfaces.StickerPackService;
import io.github.miguelteles.beststickerapp.services.interfaces.StickerService;
import io.github.miguelteles.beststickerapp.validator.WhitelistCheck;

public class StickerPackDetailsActivity extends AddStickerPackToWhatsappActivity {

    private RecyclerView recyclerView;
    private GridLayoutManager layoutManager;
    private StickerPreviewAdapter stickerPreviewAdapter;
    private int numColumns;
    private View btnAddToWhatsapp;
    private View alreadyAddedText;
    private StickerPack stickerPack;
    private View divider;
    private ImageView btnAddNewSticker;
    private ImageView btnEditStickerPack;
    private ImageView btnDeleteStickerPack;
    private ImageView btnGoBack;
    private WhiteListCheckAsyncTask whiteListCheckAsyncTask;
    private StickerPackService stickerPackService;
    private StickerService stickerService;
    private int stickerBytes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sticker_pack_details);
        stickerPack = getIntent().getParcelableExtra(Extras.EXTRA_STICKER_PACK_DATA);

        declareGlobalComponents();
        try {
            stickerPackService = StickerPackServiceImpl.getInstace(getApplicationContext());
            stickerService = StickerServiceImpl.getInstance(getApplicationContext());
        } catch (StickerException ex) {
            StickerExceptionHandler.handleException(ex, this);
        }

        assembleRecyclerView();
        loadStickersOnScreen();
        loadStickerPackInfoOnComponents();
        setaOnClickListeners();
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        findViewById(R.id.sticker_pack_animation_indicator).setVisibility(stickerPack.isAnimatedStickerPack() ? View.VISIBLE : View.GONE);
    }

    private void loadStickerPackInfoOnComponents() {
        TextView packNameTextView = findViewById(R.id.pack_name);
        TextView packPublisherTextView = findViewById(R.id.author);
        ImageView packTrayIcon = findViewById(R.id.tray_image);
        TextView packSizeTextView = findViewById(R.id.pack_size);

        packNameTextView.setText(stickerPack.getName());
        packPublisherTextView.setText("Autor: " + stickerPack.getPublisher());
        packTrayIcon.setImageURI(StickerUriProvider.getStickerResizedAssetUri(stickerPack.getIdentifier().toString(), stickerPack.getResizedTrayImageFile()));
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
        getMenuInflater().inflate(R.menu.toolbar, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    protected void onResume() {
        super.onResume();
        try {
            stickerPack = stickerPackService.fetchUpdatedStickerPack(stickerPack);
            whiteListCheckAsyncTask = new WhiteListCheckAsyncTask(this);
            whiteListCheckAsyncTask.execute(stickerPack);
            loadStickersOnScreen();
        } catch (StickerException ex) {
            StickerExceptionHandler.handleException(ex, this);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (whiteListCheckAsyncTask != null && !whiteListCheckAsyncTask.isCancelled()) {
            whiteListCheckAsyncTask.cancel(true);
        }
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
        alreadyAddedText = findViewById(R.id.already_added_text);
        recyclerView = findViewById(R.id.sticker_list);
        divider = findViewById(R.id.divider);
    }

    private void setaOnClickListeners() {
        btnAddToWhatsapp.setOnClickListener(v -> addStickerPackToWhatsApp(stickerPack.getIdentifier().toString(), stickerPack.getName()));
        btnEditStickerPack.setOnClickListener(btn -> editStickerPack());
        btnDeleteStickerPack.setOnClickListener(btn -> deleteStickerPack());
        btnAddNewSticker.setOnClickListener(btn -> addNewSticker());
        btnGoBack.setOnClickListener(btn -> addGoBack());
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
                        try {
                            stickerPackService.deleteStickerPack(stickerPack, getApplicationContext());
                            Intent intent = new Intent(context, StickerPackListActivity.class);
                            startActivity(intent);
                        } catch (StickerException ex) {
                            StickerExceptionHandler.handleException(ex, context);
                        }
                    }
                })
                .setNegativeButton(R.string.NOT_TODAY, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }).create().show();
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

    private void updateAddUI(Boolean isWhitelisted) {
        if (isWhitelisted) {
            btnAddToWhatsapp.setVisibility(View.GONE);
            alreadyAddedText.setVisibility(View.VISIBLE);
            findViewById(R.id.sticker_pack_details_tap_to_preview).setVisibility(View.GONE);
        } else {
            btnAddToWhatsapp.setVisibility(View.VISIBLE);
            alreadyAddedText.setVisibility(View.GONE);
            findViewById(R.id.sticker_pack_details_tap_to_preview).setVisibility(View.VISIBLE);
        }
    }

    public void deleteSticker(Sticker sticker, StickerPack stickerPack, Context context) {
        try {
            this.stickerService.deleteSticker(sticker, stickerPack, context);
            stickerPack.getStickers().remove(sticker);
            this.loadStickersOnScreen();
        } catch (StickerException ex) {
            StickerExceptionHandler.handleException(ex, this);
        }
    }

    static class WhiteListCheckAsyncTask extends AsyncTask<StickerPack, Void, Boolean> {
        private final WeakReference<StickerPackDetailsActivity> stickerPackDetailsActivityWeakReference;

        WhiteListCheckAsyncTask(StickerPackDetailsActivity stickerPackListActivity) {
            this.stickerPackDetailsActivityWeakReference = new WeakReference<>(stickerPackListActivity);
        }

        @Override
        protected final Boolean doInBackground(StickerPack... stickerPacks) {
            StickerPack stickerPack = stickerPacks[0];
            final StickerPackDetailsActivity stickerPackDetailsActivity = stickerPackDetailsActivityWeakReference.get();
            if (stickerPackDetailsActivity == null) {
                return false;
            }
            return WhitelistCheck.isWhitelisted(stickerPackDetailsActivity, stickerPack.getIdentifier().toString());
        }

        @Override
        protected void onPostExecute(Boolean isWhitelisted) {
            final StickerPackDetailsActivity stickerPackDetailsActivity = stickerPackDetailsActivityWeakReference.get();
            if (stickerPackDetailsActivity != null) {
                stickerPackDetailsActivity.updateAddUI(isWhitelisted);
            }
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
