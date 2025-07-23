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
import io.github.miguelteles.beststickerapp.view.UpdateAppActivity;

public class UpdateDialogFragment extends DialogFragment {

    private final boolean isUpdateOptional;
    private final String versionName;

    public UpdateDialogFragment(boolean isUpdateOptional, String versionName) {
        this.isUpdateOptional = isUpdateOptional;
        this.versionName = versionName;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_update_info, container, false);

        TextView txtAppVersion = view.findViewById(R.id.txtAppVersion);
        TextView txtUpdateText = view.findViewById(R.id.txtUpdateText);
        TextView txtPressOutsideToCloseUpdateDialog = view.findViewById(R.id.txtPressOutsideToCloseUpdateDialog);
        TextView btnUpdateApp = view.findViewById(R.id.btnUpdateApp);
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        if (isUpdateOptional) {
            customizeScreenWithOptionalUpdate(txtAppVersion, txtUpdateText);
        } else {
            customizeScreenWithMandatoryUpdate(txtAppVersion, txtUpdateText, txtPressOutsideToCloseUpdateDialog);
        }
        btnUpdateApp.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), UpdateAppActivity.class);
            startActivity(intent);
        });
        return view;
    }

    private void customizeScreenWithMandatoryUpdate(TextView txtAppVersion, TextView txtUpdateText, TextView txtPressOutsideToCloseUpdateDialog) {
        txtAppVersion.setText(getContext().getString(R.string.MANDATORY_VERSION_NAME, versionName));
        txtUpdateText.setText(R.string.NEW_MANDATORY_UPDATE_TEXT);
        txtAppVersion.setTextColor(getResources().getColor(R.color.colorRed));
        getDialog().setCanceledOnTouchOutside(false);
        txtPressOutsideToCloseUpdateDialog.setVisibility(View.GONE);
    }

    private void customizeScreenWithOptionalUpdate(TextView txtAppVersion, TextView txtUpdateText) {
        txtAppVersion.setText(getContext().getString(R.string.OPTIONAL_VERSION_NAME, versionName));
        txtUpdateText.setText(R.string.NEW_OPTIONAL_UPDATE_TEXT);
        txtAppVersion.setTextColor(getResources().getColor(R.color.colorAccent));
    }

}
