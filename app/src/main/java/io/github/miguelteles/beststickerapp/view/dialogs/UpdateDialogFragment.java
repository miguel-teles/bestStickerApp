package io.github.miguelteles.beststickerapp.view.dialogs;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import io.github.miguelteles.beststickerapp.R;
import io.github.miguelteles.beststickerapp.domain.pojo.Version;
import io.github.miguelteles.beststickerapp.view.DownloadUpdateAppActivity;

public class UpdateDialogFragment extends DialogFragment {

    private final Version version;

    public UpdateDialogFragment(Version version) {
        this.version = version;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_update_info, container, false);
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        if (version.isUpdateOptional()) {
            customizeScreenWithOptionalUpdate(view);
        } else {
            customizeScreenWithMandatoryUpdate(view);
        }
        createUpdateButtonOnClickListener(view, version);
        return view;
    }

    private void createUpdateButtonOnClickListener(View view, Version version) {
        TextView btnUpdateApp = view.findViewById(R.id.btnUpdateApp);
        btnUpdateApp.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), DownloadUpdateAppActivity.class);
            intent.putExtra(DownloadUpdateAppActivity.Extras.EXTRA_VERSION, version);
            startActivity(intent);
        });
    }

    private void customizeScreenWithMandatoryUpdate(View view) {
        TextView txtAppVersion = view.findViewById(R.id.txtAppVersion);
        TextView txtUpdateText = view.findViewById(R.id.txtUpdateDialogMessage);
        TextView txtPressOutsideToCloseUpdateDialog = view.findViewById(R.id.txtPressOutsideToCloseUpdateDialog);

        txtAppVersion.setText(getContext().getString(R.string.mandatory_version_name, version.getVersion()));
        txtUpdateText.setText(R.string.new_mandatory_update_text);
        txtAppVersion.setTextColor(getResources().getColor(R.color.colorRed));
        getDialog().setCanceledOnTouchOutside(false);
        txtPressOutsideToCloseUpdateDialog.setVisibility(View.GONE);
    }

    private void customizeScreenWithOptionalUpdate(View view) {
        TextView txtAppVersion = view.findViewById(R.id.txtAppVersion);
        TextView txtUpdateText = view.findViewById(R.id.txtUpdateDialogMessage);

        txtAppVersion.setText(getContext().getString(R.string.optional_version_name, version.getVersion()));
        txtUpdateText.setText(R.string.new_optional_update_text);
        txtAppVersion.setTextColor(getResources().getColor(R.color.colorAccent));
    }

}
