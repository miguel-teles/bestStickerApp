package com.example.samplestickerapp.view;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.samplestickerapp.R;
import com.example.samplestickerapp.modelView.StickerPackViewModel;
import com.example.samplestickerapp.modelView.factory.StickerPackViewModelFactory;
import com.example.samplestickerapp.exception.StickerException;
import com.example.samplestickerapp.exception.StickerExceptionHandler;
import com.example.samplestickerapp.exception.enums.StickerExceptionEnum;
import com.example.samplestickerapp.model.StickerPack;
import com.example.samplestickerapp.utils.Utils;
import com.google.android.material.textfield.TextInputEditText;

public class StickerPackFormActivity extends AppCompatActivity {

    private StickerPack stickerPackBeingEdited;
    private TextView btnAdicionarStickerPack;
    private TextInputEditText txtNomePacote, txtAutor;
    private ImageView stickerPackImageView;
    private Uri uriImagemStickerPack;

    private StickerPackViewModel stickerPackViewModel;

    public static final String STICKER_PACK = "stickerpack";

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_sticker_create_pack);

        declaraCampos();
        populateFieldsWithStickerPackBeingEdited();
        try {
            setaOnClickListeners();
            stickerPackViewModel = StickerPackViewModelFactory.create(this, getApplicationContext());
        } catch (StickerException ex) {
            StickerExceptionHandler.handleException(ex, this);
        }
        verificaCamposObrigatorios();

    }

    private void populateFieldsWithStickerPackBeingEdited() {
        Intent intent = getIntent();
        if (isStickerPackEdit(intent)) {
            this.stickerPackBeingEdited = (StickerPack) intent.getExtras().get(STICKER_PACK);
            txtNomePacote.setText(stickerPackBeingEdited.getName());
            txtAutor.setText(stickerPackBeingEdited.getPublisher());
            uriImagemStickerPack = StickerPackLoader.getStickerAssetUri(stickerPackBeingEdited.getIdentifier().toString(), stickerPackBeingEdited.getOriginalTrayImageFile());
            stickerPackImageView.setImageURI(uriImagemStickerPack);
            stickerPackImageView.setScaleType(ImageView.ScaleType.FIT_XY);
            stickerPackImageView.setTag("modified");
        }
    }

    private static boolean isStickerPackEdit(Intent intent) {
        return intent.getExtras() != null && intent.getExtras().get(STICKER_PACK) != null;
    }

    private void declaraCampos() {
        btnAdicionarStickerPack = findViewById(R.id.adicionarStickerPack);
        btnAdicionarStickerPack.setEnabled(false);
        txtNomePacote = findViewById(R.id.txtInpEdtNomePacote);
        txtAutor = findViewById(R.id.txtInpEdtAutor);
        stickerPackImageView = findViewById(R.id.pacoteImageView);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void setaOnClickListeners() throws StickerException {
        if (stickerPackBeingEdited == null) {
            stickerPackImageView.setOnClickListener(pacoteImageViewOnClick());
        }

        btnAdicionarStickerPack.setOnClickListener(btnSalvarStickerPackOnClick());
        stickerPackImageView.setOnFocusChangeListener(onFocusChangeListener());
        txtNomePacote.setOnFocusChangeListener(onFocusChangeListener());
        txtNomePacote.addTextChangedListener(createTextChangedListener());

    }

    @NonNull
    private TextWatcher createTextChangedListener() {
        return new TextWatcher() {
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
        };
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
            btnAdicionarStickerPack.setBackground(getResources().getDrawable(R.drawable.btn_default));
        } else {
            btnAdicionarStickerPack.setEnabled(false);
            btnAdicionarStickerPack.setBackground(getResources().getDrawable(R.drawable.btn_default_disabled));
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

    private View.OnClickListener createOrUpdateStickerPackOnClick() {
        Context context = this;
        return new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                try {
                    String nmAutorInput = txtAutor.getText().toString();
                    String nomePacoteInput = txtNomePacote.getText().toString();

                    if (stickerPackBeingEdited == null) {
                        stickerPackBeingEdited = stickerPackViewModel.createStickerPack(nmAutorInput,
                                nomePacoteInput,
                                uriImagemStickerPack,
                                getApplicationContext());
                    } else {
                        stickerPackBeingEdited = stickerPackViewModel.updateStickerPack(stickerPackBeingEdited,
                                nmAutorInput,
                                nomePacoteInput,
                                getApplicationContext());
                    }

                    redirecionaStickerPackDetailsActivity(stickerPackBeingEdited);
                } catch (StickerException ex) {
                    StickerExceptionHandler.handleException(ex, context);
                }
            }
        };
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
