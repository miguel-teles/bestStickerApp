package com.example.samplestickerapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;

import com.example.samplestickerapp.database.MyDatabase;
import com.example.samplestickerapp.exception.StickerException;
import com.example.samplestickerapp.exception.StickerExceptionHandler;
import com.example.samplestickerapp.exception.enums.StickerExceptionEnum;
import com.example.samplestickerapp.model.StickerPack;
import com.example.samplestickerapp.utils.Folders;
import com.example.samplestickerapp.utils.Utils;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;

import java.io.File;

public class StickerCreatePack extends Activity {

    private TextView btnAdicionarStickerPack;
    private TextInputEditText txtNomePacote, txtAutor;
    private ImageView stickerPackImageView;
    private Uri uriImagemStickerPack;
    private CheckBox cbAnimated;

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
    public void onBackPressed(){
        verificaCamposObrigatorios();
    }

    private View.OnFocusChangeListener onFocusChangeListener() throws StickerException {

        try {
            return new View.OnFocusChangeListener() {

                @Override
                public void onFocusChange(View view, boolean b) {
                   verificaCamposObrigatorios();
                }
            };
        } catch (Exception ex) {
            throw new StickerException(ex, "onFocusChangeListener", StickerExceptionEnum.CSP, null);
        }
    }

    private void verificaCamposObrigatorios() {
        if (!Utils.isNothing(txtNomePacote.getText().toString()) && stickerPackImageView.getTag().equals("modified")) {
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
                    String imgPath = uriImagemStickerPack.getPath();
                    String name = txtNomePacote.getText().toString();

                    StickerPack stickerPack = new StickerPack(null,
                            name,
                            publisher,
                            null,
                            "1",
                            cbAnimated.isChecked());

                    try {
                        Long identifier = MyDatabase.inserirPacote(stickerPack, context);
                        if (identifier != -1) {
                            try {
                                Folders.makeDirPackIdentifier(identifier.toString(), context);
                                File imgCopiada = Folders.copiaFotoParaPastaPacote(identifier.toString(), imgPath, context);

                                StickerPack stickerPackImgCerta = new StickerPack(identifier.toString(),
                                        name,
                                        publisher,
                                        imgCopiada.getName(),
                                        null,
                                        cbAnimated.isChecked());

                                //atualiza o banco pra colocar o novo nome da imagem!
                                MyDatabase.updateStickerPack(identifier.toString(), stickerPackImgCerta, context);

                                redirecionaStickerPackDetailsActivity(stickerPack);
                            } catch (StickerException ex) {
                                MyDatabase.deleteStickerPack(identifier.toString(), context);
                                throw ex;
                            }
                        }
                    } catch (StickerException ex) {
                        StickerExceptionHandler.handleException(ex, context);
                    }
                }

            };
        } catch (Exception ex) {
            throw new StickerException(ex, "btnAdicionarStickerPackOnClick", StickerExceptionEnum.CSP, null);
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
