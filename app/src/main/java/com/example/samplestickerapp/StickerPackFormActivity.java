package com.example.samplestickerapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.samplestickerapp.database.MyDatabase;
import com.example.samplestickerapp.exception.StickerException;
import com.example.samplestickerapp.exception.StickerExceptionHandler;
import com.example.samplestickerapp.exception.enums.StickerDBExceptionEnum;
import com.example.samplestickerapp.exception.enums.StickerExceptionEnum;
import com.example.samplestickerapp.model.StickerPack;
import com.example.samplestickerapp.utils.Folders;
import com.example.samplestickerapp.utils.Utils;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.util.Date;

public class StickerPackFormActivity extends Activity {

    private StickerPack stickerPack;
    private TextView btnAdicionarStickerPack;
    private TextInputEditText txtNomePacote, txtAutor;
    private ImageView stickerPackImageView;
    private Uri uriImagemStickerPack;
    private CheckBox cbAnimated;
    private final String STICKER_PACK_IMAGE_NAME = "packImg";
    public static final String STICKER_PACK = "stickerpack";

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_sticker_create_pack);

        btnAdicionarStickerPack = findViewById(R.id.adicionarStickerPack);
        btnAdicionarStickerPack.setEnabled(false);
        txtNomePacote = findViewById(R.id.txtInpEdtNomePacote);
        txtAutor = findViewById(R.id.txtInpEdtAutor);
        stickerPackImageView = findViewById(R.id.pacoteImageView);
        cbAnimated = findViewById(R.id.cbAnimado);
        cbAnimated.setActivated(true);

        stickerPackImageView.setOnClickListener(pacoteImageViewOnClick());
        try {
            btnAdicionarStickerPack.setOnClickListener(btnSalvarStickerPackOnClick());
            stickerPackImageView.setOnFocusChangeListener(onFocusChangeListener());
            txtNomePacote.setOnFocusChangeListener(onFocusChangeListener());
            txtNomePacote.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    verificaCamposObrigatorios();
                }
            });
        } catch (StickerException ex) {
            StickerExceptionHandler.handleException(ex, this);
        }

        Intent intent = getIntent();
        if (intent.getExtras() != null && intent.getExtras().get(STICKER_PACK) != null) {
            this.stickerPack = (StickerPack) intent.getExtras().get(STICKER_PACK);
            txtNomePacote.setText(stickerPack.getName());
            txtAutor.setText(stickerPack.getPublisher());
            stickerPackImageView.setImageURI(StickerPackLoader.getStickerAssetUri(stickerPack.getIdentifier(), stickerPack.getOriginalTrayImageFile()));
            stickerPackImageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            stickerPackImageView.setTag("modified");
            cbAnimated.setActivated(false);
        }

        verificaCamposObrigatorios();
    }

//    @Override
//    public void onBackPressed() {
//        try {
//            verificaCamposObrigatorios();
//        } catch (Exception ex) {
//            StickerExceptionHandler.handleException(new StickerException(ex,
//                            StickerExceptionEnum.CSP,
//                            null),
//                    this);
//        }
//    }

    private View.OnFocusChangeListener onFocusChangeListener() throws StickerException {
        return new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                verificaCamposObrigatorios();
            }
        };
    }

    private void verificaCamposObrigatorios() {
        if (!Utils.isNothing(txtNomePacote.getText()) && stickerPackImageView.getTag() != null && stickerPackImageView.getTag().equals("modified")) {
            btnAdicionarStickerPack.setEnabled(true);
            btnAdicionarStickerPack.setBackground(getResources().getDrawable(R.drawable.btn_green));
        } else {
            btnAdicionarStickerPack.setEnabled(false);
            btnAdicionarStickerPack.setBackground(getResources().getDrawable(R.color.gray));
        }
    }

    public View.OnClickListener pacoteImageViewOnClick() {
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
            Uri imageUri = data.getData();
            stickerPackImageView.setImageURI(imageUri);
            uriImagemStickerPack = imageUri;
            stickerPackImageView.setTag("modified");
            stickerPackImageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            verificaCamposObrigatorios();
        }
    }

    public View.OnClickListener btnSalvarStickerPackOnClick() throws StickerException {
        try {
            return createOrUpdateStickerPackOnClick();
        } catch (Exception ex) {
            throw new StickerException(ex, StickerExceptionEnum.CSP, null);
        }
    }

    private void updateStickerPack(View view) {
        return;
    }

    private View.OnClickListener createOrUpdateStickerPackOnClick() {
        return new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (stickerPack == null) {
                    createStickerPack(view);
                } else {
                    updateStickerPack(view);
                }
            }
        };
    }

    private void createStickerPack(View view) {
        String publisher = txtAutor.getText().toString();
        if (Utils.isNothing(publisher)) {
            publisher = getResources().getString(R.string.defaultPublisher);
        }
        String name = txtNomePacote.getText().toString();
        String stickerPackFolderName = name + Utils.formatData(new Date(), "yyyy.MM.dd.HH.mm.ss");
        Long identifier = null;
        File stickerPackFolder = null;
        try {
            stickerPackFolder = Folders.makeDirPackIdentifier(stickerPackFolderName, view.getContext());
            File[] imgsCopiada = Folders.copiaFotoParaPastaPacote(stickerPackFolderName,
                    Folders.getRealPathFromURI(uriImagemStickerPack, view.getContext()),
                    STICKER_PACK_IMAGE_NAME,
                    Folders.TRAY_IMAGE_SIZE,
                    Folders.TRAY_IMAGE_MAX_FILE_SIZE,
                    view.getContext());
            StickerPack stickerPack = new StickerPack(null,
                    name,
                    publisher,
                    imgsCopiada[0].getPath(),
                    imgsCopiada[1].getPath(),
                    stickerPackFolderName,
                    "1",
                    cbAnimated.isChecked());
            MyDatabase.inserirPacote(stickerPack, view.getContext());
            if (stickerPack.getIdentifier() != null) {
                redirecionaStickerPackDetailsActivity(stickerPack);
            } else {
                throw new StickerException(null, StickerDBExceptionEnum.INSERT, "Erro ao salvar pacote no banco");
            }
        } catch (StickerException ex) {
            if (identifier != null) {
                try {
                    MyDatabase.deleteStickerPack(identifier.toString(), view.getContext());
                } catch (Exception e) {
                }
            }
            try {
                Folders.deleteStickerPackFolder(stickerPackFolder, view.getContext());
            } catch (Exception e) {
            }
            StickerExceptionHandler.handleException(ex, view.getContext());
        }
    }

    private void redirecionaStickerPackDetailsActivity(StickerPack stickerPack) {
        final Intent intent = new Intent(this, StickerPackDetailsActivity.class);
        intent.putExtra(StickerPackDetailsActivity.EXTRA_SHOW_UP_BUTTON, false);
        intent.putExtra(StickerPackDetailsActivity.EXTRA_STICKER_PACK_DATA, stickerPack);
        startActivity(intent);
        finish();
        overridePendingTransition(0, 0);
    }


}
