package io.github.miguelteles.beststickerapp.view;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import io.github.miguelteles.beststickerapp.R;
import io.github.miguelteles.beststickerapp.domain.pojo.Version;
import io.github.miguelteles.beststickerapp.view.recyclerViewAdapters.updateChanges.UpdateChangesAdapter;

public class UpdateAppActivity extends AppCompatActivity {

    private TextView txtUpdateTitle;
    private TextView txtUpdateActivityMessage;
    private RecyclerView changesRecyclerView;
    private UpdateChangesAdapter updateChangesAdapter;
    private LinearLayoutManager changesLayoutManager;
    private Version version;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_app);
        loadComponents();

        version = getIntent().getParcelableExtra(Extras.EXTRA_VERSION);
        if (version == null) {
            throw new IllegalArgumentException("Version não passado no intent");
        }
        loadUpdateChangesList();
        txtUpdateTitle.setText(getString(R.string.VERSION, version.getVersion()));
        txtUpdateActivityMessage.setText(version.getMessage());

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
    }

    private void loadComponents() {
        changesRecyclerView = findViewById(R.id.changesList);
        txtUpdateTitle = findViewById(R.id.txtUpdateActivityTitle);
        txtUpdateActivityMessage = findViewById(R.id.txtUpdateActivityMessage);
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