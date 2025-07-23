package io.github.miguelteles.beststickerapp.view;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import io.github.miguelteles.beststickerapp.R;

public class UpdateAppActivity extends AppCompatActivity {

    private RecyclerView changesRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_app);

        changesRecyclerView = findViewById(R.id.changesList);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
    }
}