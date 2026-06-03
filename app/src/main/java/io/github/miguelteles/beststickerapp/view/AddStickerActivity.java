package io.github.miguelteles.beststickerapp.view;

import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.AspectRatioFrameLayout;
import androidx.media3.ui.PlayerView;

import io.github.miguelteles.beststickerapp.R;
import io.github.miguelteles.beststickerapp.domain.entity.Sticker;
import io.github.miguelteles.beststickerapp.domain.entity.StickerPack;
import io.github.miguelteles.beststickerapp.domain.pojo.VisualMediaType;
import io.github.miguelteles.beststickerapp.exception.StickerException;
import io.github.miguelteles.beststickerapp.exception.handler.StickerExceptionHandler;
import io.github.miguelteles.beststickerapp.services.FileResourceManagement;
import io.github.miguelteles.beststickerapp.services.StickerPackService;
import io.github.miguelteles.beststickerapp.services.interfaces.OperationCallback;

public class AddStickerActivity extends AppCompatActivity {
    private static final String TAG_MODIFIED = "modified";
    private static final String TAG_UNMODIFIED = "";

    private ImageView btnPickImageOrVideo = null;
    private ImageView stickerImageView = null;
    private PlayerView stickerPlayerView = null;
    private ImageView stickerVideoThumbnailImageView = null;
    private Uri uriStickerMedia = null;
    private TextView btnAdicionarSticker = null;
    private StickerPack stickerPack;
    private ProgressBar creationProgressBar;
    private ActivityResultLauncher<PickVisualMediaRequest> pickMedia;
    private ExoPlayer exoPlayer;

    //services
    private StickerPackService stickerPackService;
    private FileResourceManagement fileResourceManagement;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sticker_create_sticker);
        declaraCampos();

        try {
            stickerPackService = StickerPackService.getInstance();
            fileResourceManagement = FileResourceManagement.getInstance();
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
        btnPickImageOrVideo.setOnClickListener(selecionaImagem());
        btnAdicionarSticker.setOnClickListener(adicionarSticker());
    }

    @SuppressLint("UnsafeOptInUsageError")
    private void declaraCampos() {
        this.stickerImageView = findViewById(R.id.sticker_imageview);
        this.stickerVideoThumbnailImageView = findViewById(R.id.sticker_playerview_thumbnail);
        this.stickerPlayerView = findViewById(R.id.sticker_playerview);
        this.btnAdicionarSticker = findViewById(R.id.btn_adicionar_sticker);
        this.creationProgressBar = findViewById(R.id.pg_sticker_creation);
        this.btnPickImageOrVideo = findViewById(R.id.btn_pick_image_button);
        this.pickMedia =
                registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                    if (uri != null) {
                        try {
                            uriStickerMedia = uri;
                            VisualMediaType typeOfVisualMedia = fileResourceManagement.getTypeOfVisualMedia(uri);
                            if (typeOfVisualMedia.isImage()) {
                                addContentToImageView(uri);
                            } else {
                                addContentToVideoView(uri);
                            }

                        } catch (StickerException ex) {
                            StickerExceptionHandler.handleException(ex, this);
                        }

                    }
                    verificaCamposObrigatorios();
                });
        this.stickerPlayerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);
        this.stickerPlayerView.setUseController(false);
        this.stickerImageView.setScaleType(ImageView.ScaleType.FIT_XY);
        this.stickerVideoThumbnailImageView.setScaleType(ImageView.ScaleType.FIT_XY);
        this.exoPlayer = new ExoPlayer.Builder(getApplicationContext()).build();
        this.stickerPlayerView.setPlayer(exoPlayer);
        this.exoPlayer.addListener(new Player.Listener() {

            @Override
            public void onIsPlayingChanged(boolean isPlaying) {
                if (isPlaying) {
                    stickerVideoThumbnailImageView.setVisibility(INVISIBLE);
                }
            }


        });
    }

    @OptIn(markerClass = UnstableApi.class)
    private void addContentToVideoView(Uri uri) {
        stickerVideoThumbnailImageView.setVisibility(VISIBLE);
        stickerVideoThumbnailImageView.setImageBitmap(getVideoThumbnail(this, uri));
        stickerPlayerView.setVisibility(VISIBLE);
        stickerImageView.setVisibility(INVISIBLE);
        exoPlayer.prepare();
        exoPlayer.setMediaItem(MediaItem.fromUri(uri));
        exoPlayer.setRepeatMode(Player.REPEAT_MODE_ALL);
        exoPlayer.play();
        stickerPlayerView.setTag(TAG_MODIFIED);
        stickerImageView.setTag(TAG_UNMODIFIED);
    }

    private void addContentToImageView(Uri uri) {
        stickerImageView.setImageURI(uri);
        stickerImageView.setVisibility(VISIBLE);
        stickerVideoThumbnailImageView.setVisibility(INVISIBLE);
        stickerImageView.setTag(TAG_MODIFIED);
        stickerPlayerView.setTag(TAG_UNMODIFIED);
        exoPlayer.stop();
    }

    private View.OnClickListener adicionarSticker() {
        Context context = this;
        return v -> {
            disableBtnAddSticker();
            creationProgressBar.setVisibility(VISIBLE);
            stickerPackService.createSticker(stickerPack,
                    uriStickerMedia,
                    createStickerCreationCallback(context));
        };
    }

    @NonNull
    private OperationCallback<Sticker> createStickerCreationCallback(Context context) {
        return new OperationCallback<>() {

            @Override
            public void onCreationFinish(Sticker createdEntity, StickerException stickerException) {
                if (createdEntity != null && createdEntity.getIdentifier() != null) {
                    onProgressUpdate(100);
                    finish();
                } else {
                    enableBtnAddSticker();
                    creationProgressBar.setProgress(0);
                    creationProgressBar.setVisibility(GONE);
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
            pickMedia.launch(new PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageAndVideo.INSTANCE)
                    .build());
        };
    }

    private void verificaCamposObrigatorios() {
        if (TAG_MODIFIED.equals(stickerImageView.getTag()) || TAG_MODIFIED.equals(stickerPlayerView.getTag())) {
            enableBtnAddSticker();
        } else {
            disableBtnAddSticker();
        }
    }

    private void disableBtnAddSticker() {
        btnAdicionarSticker.setEnabled(false);
        btnAdicionarSticker.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.shape_btn_default_disabled, null));
    }

    private void enableBtnAddSticker() {
        btnAdicionarSticker.setEnabled(true);
        btnAdicionarSticker.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.shape_btn_default, null));
    }

    private Bitmap getVideoThumbnail(Context context, Uri videoUri) {
        try (MediaMetadataRetriever retriever = new MediaMetadataRetriever()) {
            retriever.setDataSource(context, videoUri);

            return retriever.getFrameAtTime(
                    0,
                    MediaMetadataRetriever.OPTION_CLOSEST_SYNC
            );
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
