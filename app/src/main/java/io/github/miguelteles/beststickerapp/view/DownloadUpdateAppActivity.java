package io.github.miguelteles.beststickerapp.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;

import io.github.miguelteles.beststickerapp.R;
import io.github.miguelteles.beststickerapp.domain.pojo.Version;
import io.github.miguelteles.beststickerapp.exception.StickerException;
import io.github.miguelteles.beststickerapp.exception.handler.StickerExceptionHandler;
import io.github.miguelteles.beststickerapp.services.UpdateAppService;
import io.github.miguelteles.beststickerapp.services.interfaces.DownloadCallback;
import io.github.miguelteles.beststickerapp.view.recyclerViewAdapters.updateChanges.UpdateChangesAdapter;

public class DownloadUpdateAppActivity extends AppCompatActivity {

    private TextView txtUpdateTitle;
    private TextView txtUpdateActivityMessage;
    private RecyclerView changesRecyclerView;
    private UpdateAppService updateAppService;
    private TextView btnDownloadUpdate;
    private ProgressBar pgbDownloadUpdate;
    private Version version;
    private static final int REQUEST_INSTALL_PACKAGE = 100; //apenas uma constante que eu defino aqui e uso para identificar depois que a permissão for permitida. O número 100 não tem relação com nada, eu apenas escolhi.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_update_app);
        loadContentFromIntent();

        declareComponents();
        updateAppService = new UpdateAppService(version);
        loadVersionDescriptionTexts();
        loadUpdateChangesList();
        loadOnClickListeners();

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
    }

    private void loadContentFromIntent() {
        version = getIntent().getParcelableExtra(Extras.EXTRA_VERSION);
        if (version == null) {
            throw new IllegalArgumentException("Version não passado no intent");
        }
    }

    private void loadOnClickListeners() {
        Context context = this;

        File downloadedApk = updateAppService.verifyDownloadedApkVersion();
        if (downloadedApk != null) {
            btnDownloadUpdateOnClickListenerSkip(downloadedApk, context);
        } else {
            btnDownloadUpdateOnClickListenerDownload(context);
        }
    }

    private void btnDownloadUpdateOnClickListenerSkip(File file, Context context) {
        Activity activity = this;
        btnDownloadUpdate.setText(R.string.skip_already_downloaded);
        btnDownloadUpdate.setOnClickListener(v -> {
            goToInstallAppUpdateAppActivity(file, context, activity);
        });
    }

    private void btnDownloadUpdateOnClickListenerDownload(Context context) {
        btnDownloadUpdate.setOnClickListener(v -> {
            changeBtnDownloadDesignOnClick();
            updateAppService.downloadUpdate(version.getVersion(), createDownloadCallback(context));
        });
    }

    @NonNull
    private DownloadCallback createDownloadCallback(Context context) {
        Activity activity = this;
        return new DownloadCallback() {
            @Override
            public void onProgressFinish(File file) {
                goToInstallAppUpdateAppActivity(file, context, activity);
            }

            @Override
            public void onProgressUpdate(int process) {
                pgbDownloadUpdate.setProgress(process, true);
                btnDownloadUpdate.setText(process + "%");
            }

            @Override
            public void onDownloadException(StickerException ex) {
                StickerExceptionHandler.handleException(ex, context);
                btnDownloadOnException();
            }
        };
    }

    private static void goToInstallAppUpdateAppActivity(File file, Context context, Activity activity) {
        Intent intent = new Intent(context, InstallUpdateAppActivity.class);
        intent.putExtra("apk", file.getAbsolutePath());
        activity.startActivity(intent);
    }

    private void changeBtnDownloadDesignOnClick() {
        pgbDownloadUpdate.setVisibility(View.VISIBLE);
        btnDownloadUpdate.setBackground(ResourcesCompat.getDrawable(getResources(), R.color.transparent, null));
        btnDownloadUpdate.setTextColor(ResourcesCompat.getColor(getResources(), R.color.textColor, null));
    }

    private void btnDownloadOnException() {
        pgbDownloadUpdate.setVisibility(View.INVISIBLE);
        pgbDownloadUpdate.setProgress(0);
        btnDownloadUpdate.setBackground(ResourcesCompat.getDrawable(getResources(), R.color.buttonColor, null));
        btnDownloadUpdate.setTextColor(ResourcesCompat.getColor(getResources(), R.color.white, null));
    }

    private void loadVersionDescriptionTexts() {
        txtUpdateTitle.setText(getString(R.string.version, version.getVersion()));
        txtUpdateActivityMessage.setText(version.getMessage());
    }

    private void declareComponents() {
        changesRecyclerView = findViewById(R.id.changesList);
        txtUpdateTitle = findViewById(R.id.txt_update_activity_title);
        txtUpdateActivityMessage = findViewById(R.id.txt_update_activity_message);
        btnDownloadUpdate = findViewById(R.id.btn_download_update);
        pgbDownloadUpdate = findViewById(R.id.pgb_download_update);
    }

    private void loadUpdateChangesList() {
        UpdateChangesAdapter updateChangesAdapter = new UpdateChangesAdapter(version.getChanges());
        changesRecyclerView.setAdapter(updateChangesAdapter);
        LinearLayoutManager changesLayoutManager = new LinearLayoutManager(this);
        changesLayoutManager.setOrientation(RecyclerView.VERTICAL);
        changesRecyclerView.setLayoutManager(changesLayoutManager);
    }

    public static class Extras {
        public static final String EXTRA_VERSION = "version";
    }
}