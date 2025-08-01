package io.github.miguelteles.beststickerapp.view;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import io.github.miguelteles.beststickerapp.R;
import io.github.miguelteles.beststickerapp.domain.pojo.Version;
import io.github.miguelteles.beststickerapp.exception.StickerException;
import io.github.miguelteles.beststickerapp.exception.handler.StickerExceptionHandler;
import io.github.miguelteles.beststickerapp.services.UpdateAppService;
import io.github.miguelteles.beststickerapp.services.interfaces.DownloadCallback;
import io.github.miguelteles.beststickerapp.view.recyclerViewAdapters.updateChanges.UpdateChangesAdapter;

public class UpdateAppActivity extends AppCompatActivity {

    private TextView txtUpdateTitle;
    private TextView txtUpdateActivityMessage;
    private RecyclerView changesRecyclerView;
    private UpdateChangesAdapter updateChangesAdapter;
    private LinearLayoutManager changesLayoutManager;
    private UpdateAppService updateAppService;
    private TextView btnDownloadUpdate;
    private ProgressBar pgbDownloadUpdate;
    private Version version;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_app);

        loadComponents();
        updateAppService = new UpdateAppService();
        loadVersionDescriptionTexts();
        loadUpdateChangesList();
        loadOnClickListeners();

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
    }

    private void loadOnClickListeners() {
        Context context = this;
        btnDownloadUpdate.setOnClickListener(v -> {
            changeButtonDesignOnClick();
            updateAppService.downloadUpdate(version.getVersion(), new DownloadCallback() {
                @Override
                public void onProgressUpdate(int process) {
                    pgbDownloadUpdate.setProgress(process, true);
                    btnDownloadUpdate.setText(process + "%");
                }

                @Override
                public void onDownloadException(StickerException ex) {
                    StickerExceptionHandler.handleException(ex, context);
                }
            });
        });
    }

    private void changeButtonDesignOnClick() {
        pgbDownloadUpdate.setVisibility(View.VISIBLE);
        btnDownloadUpdate.setBackground(ResourcesCompat.getDrawable(getResources(), R.color.transparent, null));
        btnDownloadUpdate.setTextColor(ResourcesCompat.getColor(getResources(), R.color.textColor, null));
    }

    private void loadVersionDescriptionTexts() {
        version = getIntent().getParcelableExtra(Extras.EXTRA_VERSION);
        if (version == null) {
            throw new IllegalArgumentException("Version não passado no intent");
        }
        txtUpdateTitle.setText(getString(R.string.version, version.getVersion()));
        txtUpdateActivityMessage.setText(version.getMessage());
    }

    private void loadComponents() {
        changesRecyclerView = findViewById(R.id.changesList);
        txtUpdateTitle = findViewById(R.id.txt_update_activity_title);
        txtUpdateActivityMessage = findViewById(R.id.txt_update_activity_message);
        btnDownloadUpdate = findViewById(R.id.btn_download_update);
        pgbDownloadUpdate = findViewById(R.id.pgb_download_update);
    }

    private void loadUpdateChangesList() {
        updateChangesAdapter = new UpdateChangesAdapter(version.getChanges());
        changesRecyclerView.setAdapter(updateChangesAdapter);
        changesLayoutManager = new LinearLayoutManager(this);
        changesLayoutManager.setOrientation(RecyclerView.VERTICAL);
        changesRecyclerView.setLayoutManager(changesLayoutManager);
    }

    public static class Extras {
        public static final String EXTRA_VERSION = "version";
    }
}