package io.github.miguelteles.beststickerapp.view;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import io.github.miguelteles.beststickerapp.R;
import io.github.miguelteles.beststickerapp.domain.entity.Sticker;
import io.github.miguelteles.beststickerapp.domain.entity.StickerPack;
import io.github.miguelteles.beststickerapp.exception.StickerException;
import io.github.miguelteles.beststickerapp.exception.handler.StickerExceptionHandler;
import io.github.miguelteles.beststickerapp.services.StickerPackService;
import io.github.miguelteles.beststickerapp.services.interfaces.EntityOperationCallback;
import io.github.miguelteles.beststickerapp.utils.Utils;

public class AddStickerActivity extends AppCompatActivity {

    private ImageView stickerImageView = null;
    private Uri uriStickerImage = null;
    private TextView btnAdicionarSticker = null;
    private StickerPackService stickerPackService;
    private StickerPack stickerPack;
    private ProgressBar creationProgressBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sticker_create_sticker);
        declaraCampos();

        try {
            stickerPackService = StickerPackService.getInstance();
        } catch (StickerException ex) {
            StickerExceptionHandler.handleException(ex, this);
        }

        loadStickerPackFromIntent();
        setOnClickListeners();
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
    }

    private void loadStickerPackFromIntent() {
        Intent intent = getIntent();
        if (intent.getExtras().get(AddStickerPackActivity.Extras.STICKER_PACK) != null) {
            stickerPack = intent.getParcelableExtra(AddStickerPackActivity.Extras.STICKER_PACK);
        }
    }

    private void setOnClickListeners() {
        stickerImageView.setOnClickListener(selecionaImagem());
        btnAdicionarSticker.setOnClickListener(adicionarSticker());
    }

    private void declaraCampos() {
        stickerImageView = findViewById(R.id.stickerImageView);
        btnAdicionarSticker = findViewById(R.id.adicionarSticker);
        creationProgressBar = findViewById(R.id.pg_sticker_creation);
    }

    private View.OnClickListener adicionarSticker() {
        Context context = this;
        return v -> {
            creationProgressBar.setVisibility(View.VISIBLE);
            stickerPackService.createSticker(stickerPack,
                    uriStickerImage,
                    createStickerCreationCallback(context));
        };
    }

    @NonNull
    private EntityOperationCallback<Sticker> createStickerCreationCallback(Context context) {
        return new EntityOperationCallback<>() {

            @Override
            public void onCreationFinish(Sticker createdEntity, StickerException stickerException) {
                if (createdEntity != null && createdEntity.getIdentifier() != null) {
                    onProgressUpdate(100);
                    finish();
                } else {
                    creationProgressBar.setProgress(0);
                    creationProgressBar.setVisibility(View.GONE);
                    StickerExceptionHandler.handleException(stickerException, context);
                }
            }

            @Override
            public void onProgressUpdate(int process) {
                creationProgressBar.setProgress(process, true);
            }
        };
    }

    private View.OnClickListener selecionaImagem() {
        return view -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, Utils.PICK_IMAGE_REQUEST_CODE);
        };
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Utils.PICK_IMAGE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            stickerImageView.setTag("modified");

            Uri imageUri = data.getData();
            stickerImageView.setImageURI(imageUri);
            uriStickerImage = imageUri;
            adjustPickedImageProportions();
        }
        verificaCamposObrigatorios();
    }

    private void adjustPickedImageProportions() {
        stickerImageView.setScaleType(ImageView.ScaleType.FIT_XY);
    }

    private void verificaCamposObrigatorios() {
        if (stickerImageView.getTag() != null && stickerImageView.getTag().equals("modified")) {
            btnAdicionarSticker.setEnabled(true);
            btnAdicionarSticker.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.shape_btn_default, null));
        } else {
            btnAdicionarSticker.setEnabled(false);
            btnAdicionarSticker.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.shape_btn_default_disabled, null));
        }
    }
}
