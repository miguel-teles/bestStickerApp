package io.github.miguelteles.beststickerapp.view;

import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
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
import androidx.media3.exoplayer.source.ClippingMediaSource;
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory;
import androidx.media3.exoplayer.source.MediaSource;
import androidx.media3.ui.AspectRatioFrameLayout;
import androidx.media3.ui.PlayerView;

import io.github.miguelteles.beststickerapp.R;
import io.github.miguelteles.beststickerapp.domain.entity.Sticker;
import io.github.miguelteles.beststickerapp.domain.entity.StickerPack;
import io.github.miguelteles.beststickerapp.domain.pojo.VisualMediaType;
import io.github.miguelteles.beststickerapp.exception.StickerException;
import io.github.miguelteles.beststickerapp.exception.handler.StickerExceptionHandler;
import io.github.miguelteles.beststickerapp.services.FileResourceManagement;
import io.github.miguelteles.beststickerapp.services.interfaces.operationcallback.OperationCallback;
import io.github.miguelteles.beststickerapp.services.mediaconvertion.StickerVideoConvertionService;
import io.github.miguelteles.beststickerapp.viewmodel.StickerViewModel;
import pl.droidsonroids.gif.GifImageView;

public class AddStickerActivity extends AppCompatActivity {
    private static final String TAG_MODIFIED = "modified";
    private static final String TAG_UNMODIFIED = "";

    private VisualMediaType typeSelectedMedia;
    private ImageView btnPickImageOrVideo;
    private ImageView stickerImageView;
    private PlayerView stickerPlayerView;
    private ImageView stickerVideoThumbnailImageView;
    private Uri uriStickerMedia;
    private TextView btnAdicionarSticker;
    private StickerPack stickerPack;
    private ProgressBar creationProgressBar;
    private ActivityResultLauncher<PickVisualMediaRequest> pickMedia;
    private TextView txtWarnAddSticker;
    private ExoPlayer exoPlayer;

    private GifImageView stickerGifImageView;

    private StickerViewModel stickerViewModel;
    private FileResourceManagement fileResourceManagement;

    private static MediaItem.ClippingConfiguration clippingConfiguration;

    private long selectedMediaSize = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sticker_create_sticker);
        declaraCampos();

        try {
            stickerViewModel = StickerViewModel.getInstance();
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
        btnAdicionarSticker.setOnClickListener(addStickerOnClickListener());
    }

    @SuppressLint("UnsafeOptInUsageError")
    private void declaraCampos() {
        this.stickerImageView = findViewById(R.id.sticker_imageview);
        this.stickerGifImageView = findViewById(R.id.sticker_gifimageview);
        this.stickerGifImageView.setScaleType(ImageView.ScaleType.FIT_XY);
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
                            this.typeSelectedMedia = fileResourceManagement.getTypeOfVisualMedia(uri);
                            this.selectedMediaSize = fileResourceManagement.getFileSize(uri);
                            if (typeSelectedMedia.isImage()) {
                                addContentToImageView(uri);
                            } else {
                                addContentToVideoView(uri);
                            }

                            verificaCamposObrigatoriosEIntegridade();
                        } catch (StickerException ex) {
                            StickerExceptionHandler.handleException(ex, this);
                        }
                    }
                });
        this.stickerPlayerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);
        this.stickerPlayerView.setUseController(false);
        this.stickerImageView.setScaleType(ImageView.ScaleType.FIT_XY);
        this.stickerVideoThumbnailImageView.setScaleType(ImageView.ScaleType.FIT_XY);
        this.exoPlayer = new ExoPlayer.Builder(getApplicationContext()).build();
        this.exoPlayer.setVolume(0);
        this.stickerPlayerView.setPlayer(exoPlayer);
        final Context context = this;
        this.exoPlayer.addListener(new Player.Listener() {

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                if (playbackState == ExoPlayer.STATE_READY) {
                    try {
                        verificaCamposObrigatoriosEIntegridade();
                    } catch (StickerException ex) {
                        StickerExceptionHandler.handleException(ex, context);
                    }
                }
            }

            @Override
            public void onIsPlayingChanged(boolean isPlaying) {
                if (isPlaying) {
                    stickerVideoThumbnailImageView.setVisibility(INVISIBLE);
                }
            }
        });
        this.txtWarnAddSticker = findViewById(R.id.txt_warning_add_sticker);
    }

    @OptIn(markerClass = UnstableApi.class)
    private void addContentToVideoView(Uri uri) {
        stickerImageView.setTag(TAG_UNMODIFIED);
        stickerImageView.setVisibility(INVISIBLE);
        stickerVideoThumbnailImageView.setVisibility(INVISIBLE);
        stickerPlayerView.setVisibility(INVISIBLE);
        stickerGifImageView.setVisibility(INVISIBLE);
        if (this.typeSelectedMedia.isGif()) {
            addContentToGifImageView(uri);
        } else {
            addContentToPlayerView(uri);
        }
    }

    @OptIn(markerClass = UnstableApi.class)
    private void addContentToPlayerView(Uri uri) {
        stickerVideoThumbnailImageView.setVisibility(VISIBLE);
        stickerVideoThumbnailImageView.setImageBitmap(getVideoThumbnail(this, uri));
        stickerPlayerView.setVisibility(VISIBLE);

        exoPlayer.setMediaItem(
                new MediaItem.Builder()
                        .setUri(uri)
                        .setClippingConfiguration(getClippingConfiguration())
                        .build()
        );
        exoPlayer.prepare();
        exoPlayer.setRepeatMode(Player.REPEAT_MODE_ALL);
        exoPlayer.play();

        stickerPlayerView.setTag(TAG_MODIFIED);
    }

    private static MediaItem.ClippingConfiguration getClippingConfiguration() {
        if (clippingConfiguration == null) {
            clippingConfiguration = new MediaItem.ClippingConfiguration.Builder()
                    .setEndPositionMs(StickerVideoConvertionService.MAX_ANIMATION_DURATION_SECONDS * 1000)
                    .build();
        }
        return clippingConfiguration;
    }

    private void addContentToGifImageView(Uri uri) {
        stickerGifImageView.setImageURI(uri);
        stickerGifImageView.setVisibility(VISIBLE);
        stickerGifImageView.setTag(TAG_MODIFIED);
    }

    private void addContentToImageView(Uri uri) {
        stickerImageView.setImageURI(uri);
        stickerImageView.setVisibility(VISIBLE);
        stickerVideoThumbnailImageView.setVisibility(INVISIBLE);
        stickerImageView.setTag(TAG_MODIFIED);
        stickerPlayerView.setTag(TAG_UNMODIFIED);
        exoPlayer.clearMediaItems();
        exoPlayer.stop();
    }

    private View.OnClickListener addStickerOnClickListener() {
        Context context = this;
        return v -> {
            disableBtnAddSticker();
            creationProgressBar.setVisibility(VISIBLE);
            stickerViewModel.createSticker(stickerPack,
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
                int current = creationProgressBar.getProgress();
                creationProgressBar.setProgress(current + process, true);
            }

            @Override
            public void onProgressUpdate() {
                creationProgressBar.setProgress(
                        this.calculateProgress(
                                creationProgressBar.getProgress()
                        )
                );
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

    private void verificaCamposObrigatoriosEIntegridade() throws StickerException {
        if (isEverythingUnmodifed()) {
            disableBtnAddSticker();
            txtWarnAddSticker.setVisibility(INVISIBLE);
            return;
        } else if (isStickerStandardButPackIsAnimated()) {
            txtWarnAddSticker.setVisibility(VISIBLE);
            txtWarnAddSticker.setText(R.string.WARN_WRONG_TYPE_STICKER_PACK);
            disableBtnAddSticker();
            return;
        } else if (isStickerAnimatedButPackIsStandard()) {
            txtWarnAddSticker.setVisibility(VISIBLE);
            txtWarnAddSticker.setText(R.string.WARN_WRONG_TYPE_STICKER_PACK);
            disableBtnAddSticker();
            return;
        } else if (isVideoSelected() && isVideoFileSizeTooBig()) {
            txtWarnAddSticker.setVisibility(VISIBLE);
            txtWarnAddSticker.setText(R.string.WARN_FILE_BIGGER_THAN_ALLOWED);
            disableBtnAddSticker();
            return;
        } else if (isVideoSelected() && !exoPlayer.isPlaying()) {
            disableBtnAddSticker();
            return;
        } else if (isVideoSelected() && exoPlayer.isPlaying()) {
            txtWarnAddSticker.setVisibility(VISIBLE);
            txtWarnAddSticker.setText(R.string.WARN_DURATION_OF_VIDEO);
            enableBtnAddSticker();
            return;
        }
        txtWarnAddSticker.setVisibility(INVISIBLE);
        enableBtnAddSticker();
    }

    private boolean isVideoFileSizeTooBig() {
        return this.selectedMediaSize >= this.stickerViewModel.getMaxFileSizeAllowed();
    }

    private boolean isEverythingUnmodifed() {
        return TAG_UNMODIFIED.equals(stickerImageView.getTag()) &&
                TAG_UNMODIFIED.equals(stickerPlayerView.getTag()) &&
                TAG_UNMODIFIED.equals(stickerGifImageView.getTag());
    }

    private boolean isStickerStandardButPackIsAnimated() {
        return typeSelectedMedia.isImage() && stickerPack.isAnimatedStickerPack();
    }

    private boolean isStickerAnimatedButPackIsStandard() {
        return typeSelectedMedia.isAnimated() && stickerPack.isStandardStickerPack();
    }

    private boolean isVideoSelected() {
        return TAG_MODIFIED.equals(stickerPlayerView.getTag());
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
