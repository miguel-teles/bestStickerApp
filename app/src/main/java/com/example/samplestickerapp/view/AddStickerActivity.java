package com.example.samplestickerapp.view;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.samplestickerapp.R;
import com.example.samplestickerapp.exception.StickerException;
import com.example.samplestickerapp.exception.StickerExceptionHandler;
import com.example.samplestickerapp.model.Sticker;
import com.example.samplestickerapp.model.StickerPack;
import com.example.samplestickerapp.modelView.StickerViewModel;
import com.example.samplestickerapp.modelView.factory.StickerViewModelFactory;
import com.example.samplestickerapp.utils.Utils;

public class AddStickerActivity extends AppCompatActivity {


    private ImageView stickerImageView = null;
    private Uri uriStickerImage = null;
    private TextView btnAdicionarSticker = null;
    private StickerViewModel stickerViewModel;
    private StickerPack stickerPack;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sticker_create_sticker);
        declaraCampos();

        try {
            stickerViewModel = StickerViewModelFactory.create(this, getApplicationContext());
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
        if (intent.getExtras().get(StickerPackFormActivity.STICKER_PACK) != null) {
            stickerPack = intent.getParcelableExtra(StickerPackFormActivity.STICKER_PACK);
        }
    }

    private void setOnClickListeners() {
        stickerImageView.setOnClickListener(selecionaImagem());
        btnAdicionarSticker.setOnClickListener(adicionarSticker());
    }

    private void declaraCampos() {
        stickerImageView = findViewById(R.id.stickerImageView);
        btnAdicionarSticker = findViewById(R.id.adicionarSticker);
    }

    private View.OnClickListener adicionarSticker() {
        Context context = this;
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Sticker sticker = stickerViewModel.createSticker(stickerPack,
                            uriStickerImage,
                            context);
                    if (sticker.getIdentifier() != null) {
                        finish();
                    }
                } catch (StickerException ex) {
                    StickerExceptionHandler.handleException(ex, context);
                }
            }
        };
    }

    private View.OnClickListener selecionaImagem() {
        Context context = this;
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, Utils.PICK_IMAGE_REQUEST_CODE);
            }
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
            btnAdicionarSticker.setBackground(getResources().getDrawable(R.drawable.shape_btn_default));
        } else {
            btnAdicionarSticker.setEnabled(false);
            btnAdicionarSticker.setBackground(getResources().getDrawable(R.drawable.shape_btn_default_disabled));
        }
    }
}
