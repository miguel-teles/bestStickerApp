package io.github.miguelteles.beststickerapp.view;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import io.github.miguelteles.beststickerapp.R;
import io.github.miguelteles.beststickerapp.exception.StickerException;
import io.github.miguelteles.beststickerapp.exception.StickerExceptionHandler;
import io.github.miguelteles.beststickerapp.exception.enums.StickerExceptionEnum;
import io.github.miguelteles.beststickerapp.domain.entity.StickerPack;
import io.github.miguelteles.beststickerapp.repository.contentProvider.StickerUriProvider;
import io.github.miguelteles.beststickerapp.services.StickerPackServiceImpl;
import io.github.miguelteles.beststickerapp.services.interfaces.EntityOperationCallback;
import io.github.miguelteles.beststickerapp.services.interfaces.StickerPackService;
import io.github.miguelteles.beststickerapp.utils.Utils;

import com.google.android.material.textfield.TextInputEditText;


public class AddStickerPackActivity extends AppCompatActivity {

    private StickerPack stickerPackBeingEdited;
    private TextView btnAdicionarStickerPack;
    private TextInputEditText txtNomePacote, txtAutor;
    private ImageView stickerPackImageView;
    private Uri uriImagemStickerPack;
    private StickerPackService stickerPackService;
    private ProgressBar creationProgressBar;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_sticker_create_pack);

        declaraCampos();
        populateFieldsWithStickerPackBeingEdited();
        try {
            setaOnClickListeners();
            stickerPackService = StickerPackServiceImpl.getInstance();
        } catch (StickerException ex) {
            StickerExceptionHandler.handleException(ex, this);
        }
        verifyMandatoryFields();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void populateFieldsWithStickerPackBeingEdited() {
        Intent intent = getIntent();
        this.btnAdicionarStickerPack.setText(R.string.ADD_PACK);
        if (isStickerPackEdit(intent)) {
            this.stickerPackBeingEdited = (StickerPack) intent.getExtras().get(Extras.STICKER_PACK);
            txtNomePacote.setText(stickerPackBeingEdited.getName());
            txtAutor.setText(stickerPackBeingEdited.getPublisher());
            uriImagemStickerPack = StickerUriProvider.getInstance().getStickerPackOriginalAssetUri(stickerPackBeingEdited.getIdentifier().toString(), stickerPackBeingEdited.getOriginalTrayImageFile());
            stickerPackImageView.setImageURI(uriImagemStickerPack);
            stickerPackImageView.setScaleType(ImageView.ScaleType.FIT_XY);
            stickerPackImageView.setTag("modified");
            this.btnAdicionarStickerPack.setText(R.string.SAVE_PACK);
        }
    }

    private static boolean isStickerPackEdit(Intent intent) {
        return intent.getExtras() != null && intent.getExtras().get(Extras.STICKER_PACK) != null;
    }

    private void declaraCampos() {
        btnAdicionarStickerPack = findViewById(R.id.adicionarStickerPack);
        btnAdicionarStickerPack.setEnabled(false);
        txtNomePacote = findViewById(R.id.txtInpEdtNomePacote);
        txtAutor = findViewById(R.id.txtInpEdtAutor);
        stickerPackImageView = findViewById(R.id.pacoteImageView);
        creationProgressBar = findViewById(R.id.pg_sticker_pack_creation);
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
            btnAdicionarStickerPack.setEnabled(true);
            btnAdicionarStickerPack.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.shape_btn_default, null));
        } else {
            btnAdicionarStickerPack.setEnabled(false);
            btnAdicionarStickerPack.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.shape_btn_default_disabled, null));
        }
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

    public View.OnClickListener btnSalvarStickerPackOnClick() throws StickerException {
        try {
            return createOrUpdateStickerPackOnClick();
        } catch (Exception ex) {
            throw new StickerException(ex, StickerExceptionEnum.CSP, null);
        }
    }

    private View.OnClickListener createOrUpdateStickerPackOnClick() {
        return v -> {
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
    }

    private EntityOperationCallback<StickerPack> createStickerPackCreationCallback() {
        Context context = this;
        return new EntityOperationCallback<>() {
            @Override
            public void onCreationFinish(StickerPack createdStickerPack, StickerException stickerException) {
                stickerPackBeingEdited = createdStickerPack;
                if (stickerException != null) {
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
                runProgressBarAnimation(process);
                creationProgressBar.setProgress(process);
            }

            @Override
            public void runProgressBarAnimation(int process) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    ObjectAnimator animation = ObjectAnimator.ofInt(creationProgressBar, "progress", creationProgressBar.getProgress(), process);
                    animation.setDuration(500);
                    animation.setInterpolator(new DecelerateInterpolator());
                    animation.start();
                });
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
