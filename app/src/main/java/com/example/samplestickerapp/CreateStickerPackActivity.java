package com.example.samplestickerapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

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

public class CreateStickerPackActivity extends Activity {

    private TextView btnAdicionarStickerPack;
    private TextInputEditText txtNomePacote, txtAutor;
    private ImageView stickerPackImageView;
    private Uri uriImagemStickerPack;
    private CheckBox cbAnimated;

    private final String STICKER_PACK_IMAGE_NAME = "packImg";

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

        stickerPackImageView.setOnClickListener(pacoteImageViewOnClick());
        try {
            btnAdicionarStickerPack.setOnClickListener(btnAdicionarStickerPackOnClick());
            stickerPackImageView.setOnFocusChangeListener(onFocusChangeListener());
            txtNomePacote.setOnFocusChangeListener(onFocusChangeListener());
        } catch (StickerException ex) {
            StickerExceptionHandler.handleException(ex, this);
        }
    }

    @Override
    public void onBackPressed() {
        try {
            verificaCamposObrigatorios();
        } catch (Exception ex) {
            StickerExceptionHandler.handleException(new StickerException(ex,
                    StickerExceptionEnum.CSP,
                    null), this);
        }
    }

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

    public View.OnClickListener btnAdicionarStickerPackOnClick() throws StickerException {
        Context context = this;
        try {
            return new View.OnClickListener() {

                @Override
                public void onClick(View view) {

                    String publisher = txtAutor.getText().toString();
                    if (Utils.isNothing(publisher)) {
                        publisher = getResources().getString(R.string.defaultPublisher);
                    }
                    String name = txtNomePacote.getText().toString();
                    String stickerPackFolderName = name + Utils.formatData(new Date(), "yyyy.MM.dd.HH.mm.ss");
                    Long identifier = null;
                    try {
                        Folders.makeDirPackIdentifier(stickerPackFolderName, context);
                        File imgCopiada = Folders.copiaFotoParaPastaPacote(stickerPackFolderName,
                                Folders.getRealPathFromURI(uriImagemStickerPack, context),
                                STICKER_PACK_IMAGE_NAME,
                                context);

                        StickerPack stickerPack = new StickerPack(null,
                                name,
                                publisher,
                                imgCopiada.getPath(),
                                stickerPackFolderName,
                                "1",
                                cbAnimated.isChecked());

                        identifier = MyDatabase.inserirPacote(stickerPack, context);
                        if (identifier != -1) {
                            redirecionaStickerPackDetailsActivity(stickerPack);
                        } else {
                            throw new StickerException(null, StickerDBExceptionEnum.INSERT, "Erro ao salvar pacote no banco");
                        }
                    } catch (StickerException ex) {
                        if (identifier != null) {
                            try {
                                MyDatabase.deleteStickerPack(identifier.toString(), context);
                            } catch (Exception e) {}
                        }
                        StickerExceptionHandler.handleException(ex, context);
                    }
                }

            };
        } catch (Exception ex) {
            throw new StickerException(ex, StickerExceptionEnum.CSP, null);
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
