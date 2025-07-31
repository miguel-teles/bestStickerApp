package io.github.miguelteles.beststickerapp.view;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import io.github.miguelteles.beststickerapp.R;
import io.github.miguelteles.beststickerapp.exception.StickerException;
import io.github.miguelteles.beststickerapp.exception.handler.StickerExceptionHandler;
import io.github.miguelteles.beststickerapp.exception.enums.StickerExceptionEnum;
import io.github.miguelteles.beststickerapp.domain.entity.StickerPack;
import io.github.miguelteles.beststickerapp.repository.contentProvider.StickerUriProvider;
import io.github.miguelteles.beststickerapp.services.FileResourceManagement;
import io.github.miguelteles.beststickerapp.services.StickerPackService;
import io.github.miguelteles.beststickerapp.services.interfaces.EntityOperationCallback;
import io.github.miguelteles.beststickerapp.services.interfaces.ResourcesManagement;
import io.github.miguelteles.beststickerapp.utils.Utils;

import com.google.android.material.textfield.TextInputEditText;


public class AddStickerPackActivity extends AppCompatActivity {

    private StickerPack stickerPackBeingEdited;
    private TextView btnAddStickerPack;
    private TextInputEditText txtNomePacote, txtAutor;
    private ImageView stickerPackImageView;
    private Uri uriImagemStickerPack;
    private StickerPackService stickerPackService;
    private ResourcesManagement resourcesManagement;
    private ProgressBar creationProgressBar;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_sticker_create_pack);

        declaraCampos();
        populateFieldsWithStickerPackBeingEdited();
        try {
            setaOnClickListeners();
            stickerPackService = StickerPackService.getInstance();
            resourcesManagement = FileResourceManagement.getInstance();
        } catch (StickerException ex) {
            StickerExceptionHandler.handleException(ex, this);
        }
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        verifyMandatoryFields();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void populateFieldsWithStickerPackBeingEdited() {
        Intent intent = getIntent();
        this.btnAddStickerPack.setText(R.string.ADD_PACK);
        if (isStickerPackEdit(intent)) {
            this.stickerPackBeingEdited = (StickerPack) intent.getExtras().get(Extras.STICKER_PACK);
            txtNomePacote.setText(stickerPackBeingEdited.getName());
            txtAutor.setText(stickerPackBeingEdited.getPublisher());
            try {
                uriImagemStickerPack = this.resourcesManagement.getFile(stickerPackBeingEdited.getFolderName(), stickerPackBeingEdited.getOriginalTrayImageFile());
                stickerPackImageView.setImageURI(uriImagemStickerPack);
                stickerPackImageView.setScaleType(ImageView.ScaleType.FIT_XY);
                stickerPackImageView.setTag("modified");
                this.btnAddStickerPack.setText(R.string.SAVE_PACK);
            } catch (StickerException ex) {
                StickerExceptionHandler.handleException(ex, this);
                this.finish();
            }
        }
    }

    private static boolean isStickerPackEdit(Intent intent) {
        return intent.getExtras() != null && intent.getExtras().get(Extras.STICKER_PACK) != null;
    }

    private void declaraCampos() {
        btnAddStickerPack = findViewById(R.id.adicionarStickerPack);
        btnAddStickerPack.setEnabled(false);
        txtNomePacote = findViewById(R.id.txtInpEdtNomePacote);
        txtAutor = findViewById(R.id.txtInpEdtAutor);
        stickerPackImageView = findViewById(R.id.pacoteImageView);
        creationProgressBar = findViewById(R.id.pg_sticker_pack_creation);
    }

    private void setaOnClickListeners() throws StickerException {
        if (stickerPackBeingEdited == null) {
            stickerPackImageView.setOnClickListener(pacoteImageViewOnClick());
        }

        btnAddStickerPack.setOnClickListener(btnSaveStickerPackOnClick());
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
                verifyMandatoryFields();
            }
        };
    }

    private View.OnFocusChangeListener onFocusChangeListener() {
        return (view, b) -> {
            verifyMandatoryFields();
        };
    }

    private void verifyMandatoryFields() {
        if (!Utils.isNothing(txtNomePacote.getText()) && stickerPackImageView.getTag() != null && stickerPackImageView.getTag().equals("modified")) {
            enableBtnAddStickerPack();
        } else {
            disableBtnAddStickerPack();
        }
    }

    private void enableBtnAddStickerPack() {
        btnAddStickerPack.setEnabled(true);
        btnAddStickerPack.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.shape_btn_default, null));
    }

    private void disableBtnAddStickerPack() {
        btnAddStickerPack.setEnabled(false);
        btnAddStickerPack.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.shape_btn_default_disabled, null));
    }

    public View.OnClickListener pacoteImageViewOnClick() {
        return v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, Utils.PICK_IMAGE_REQUEST_CODE);
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
            stickerPackImageView.setScaleType(ImageView.ScaleType.FIT_XY);
            verifyMandatoryFields();
        }
    }

    public View.OnClickListener btnSaveStickerPackOnClick() throws StickerException {
        try {
            return v -> {
                disableBtnAddStickerPack();
                String nmAutorInput = txtAutor.getText().toString();
                String nomePacoteInput = txtNomePacote.getText().toString();

                creationProgressBar.setVisibility(View.VISIBLE);
                if (stickerPackBeingEdited == null) {
                    stickerPackService.createStickerPack(nmAutorInput,
                            nomePacoteInput,
                            uriImagemStickerPack,
                            createStickerPackCreationCallback());
                } else {
                    stickerPackService.updateStickerPack(stickerPackBeingEdited,
                            nmAutorInput,
                            nomePacoteInput,
                            createStickerPackCreationCallback());
                }
            };
        } catch (Exception ex) {
            throw new StickerException(ex, StickerExceptionEnum.CSP, null);
        }
    }

    private EntityOperationCallback<StickerPack> createStickerPackCreationCallback() {
        Context context = this;
        return new EntityOperationCallback<>() {
            @Override
            public void onCreationFinish(StickerPack createdStickerPack, StickerException stickerException) {
                stickerPackBeingEdited = createdStickerPack;
                if (stickerException != null) {
                    enableBtnAddStickerPack();
                    creationProgressBar.setVisibility(View.GONE);
                    creationProgressBar.setProgress(0);
                    StickerExceptionHandler.handleException(stickerException, context);
                } else {
                    onProgressUpdate(100);
                    redirecionaStickerPackDetailsActivity(stickerPackBeingEdited);
                }
            }

            @Override
            public void onProgressUpdate(int process) {
                creationProgressBar.setProgress(process, true);
            }
        };
    }

    private void redirecionaStickerPackDetailsActivity(StickerPack stickerPack) {
        final Intent intent = new Intent(this, StickerPackDetailsActivity.class);
        intent.putExtra(StickerPackDetailsActivity.Extras.EXTRA_SHOW_UP_BUTTON, false);
        intent.putExtra(StickerPackDetailsActivity.Extras.EXTRA_STICKER_PACK_DATA, stickerPack);
        startActivity(intent);
        finish();
        overridePendingTransition(0, 0);
    }

    public static class Extras {
        public static final String STICKER_PACK = "stickerpack";
    }
}
