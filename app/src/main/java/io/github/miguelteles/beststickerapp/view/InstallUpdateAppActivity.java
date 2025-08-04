package io.github.miguelteles.beststickerapp.view;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.ActivityResultRegistry;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.content.res.ResourcesCompat;

import java.io.File;
import java.util.Objects;

import io.github.miguelteles.beststickerapp.R;
import io.github.miguelteles.beststickerapp.utils.Utils;
import io.github.miguelteles.beststickerapp.validator.MethodInputValidator;

public class InstallUpdateAppActivity extends AppCompatActivity {

    TextView btnAskPermissionInstallUnknownApp;
    TextView btnInstall;
    ImageView imgAllowed;
    TextView txtAllowed;
    File apkFile;

    private ActivityResultLauncher<Intent> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                loadComponentsDesign();
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_install_update_app);

        declareComponents();
        loadComponentsDesign();
        loadOnClickListeners();

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
    }

    private void loadOnClickListeners() {
        btnInstall.setOnClickListener(btnInstallOnClickListener());
        btnAskPermissionInstallUnknownApp.setOnClickListener(btnAskPermissionOnClickListener());
    }

    private void loadComponentsDesign() {
        if (isInstallationPermissionGranted()) {
            enableBtnInstall();
            disableBtnAskPermission();
            setAllowedText();
        } else {
            disableBtnInstall();
            enableBtnAskPermission();
            setNotAllowedText();
        }
    }

    private void disableBtnAskPermission() {
        btnAskPermissionInstallUnknownApp.setEnabled(false);
    }

    private void enableBtnAskPermission() {
        btnAskPermissionInstallUnknownApp.setEnabled(true);
    }

    private View.OnClickListener btnInstallOnClickListener() {
        return v -> {
            installAPK(apkFile);
        };
    }

    private View.OnClickListener btnAskPermissionOnClickListener() {
        return v -> {
            Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
                    .setData(Uri.parse("package:" + getPackageName()));
            requestPermissionLauncher.launch(intent);
        };
    }

    private void declareComponents() {
        btnAskPermissionInstallUnknownApp = findViewById(R.id.btn_ask_permission_install_unknown_app);
        btnInstall = findViewById(R.id.btn_install);
        imgAllowed = findViewById(R.id.allowed_imageview);
        txtAllowed = findViewById(R.id.txt_allowed);
        apkFile = new File(MethodInputValidator.requireNotNull(getIntent().getStringExtra(Extras.APK), "intent APK"));
    }

    private void setNotAllowedText() {
        txtAllowed.setText(R.string.not_allowed);
        imgAllowed.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.outline_close_24, null));
    }

    private void setAllowedText() {
        txtAllowed.setText(R.string.allowed);
        imgAllowed.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.outline_check_24, null));
    }

    private void disableBtnInstall() {
        btnInstall.setEnabled(false);
        btnInstall.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.shape_btn_default_disabled, null));
    }

    private void enableBtnInstall() {
        btnInstall.setEnabled(true);
        btnInstall.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.shape_btn_default, null));
    }

    private boolean isInstallationPermissionGranted() {
        return getPackageManager().canRequestPackageInstalls();
    }

    private void installAPK(File downloadedUpdateFile) {
        Uri apkUri = FileProvider.getUriForFile(
                getApplicationContext(),
                getApplicationContext().getPackageName() + ".provider",
                downloadedUpdateFile
        );

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        startActivity(intent);
    }

    public static class Extras {
        public static final String APK = "apk";
    }
}