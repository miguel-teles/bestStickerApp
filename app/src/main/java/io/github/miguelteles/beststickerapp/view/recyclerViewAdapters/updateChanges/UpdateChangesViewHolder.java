package io.github.miguelteles.beststickerapp.view.recyclerViewAdapters.updateChanges;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import io.github.miguelteles.beststickerapp.R;

public class UpdateChangesViewHolder extends RecyclerView.ViewHolder {

    private final TextView changesView;

    public UpdateChangesViewHolder(@NonNull View itemView) {
        super(itemView);
        changesView = itemView.findViewById(R.id.txtChangeText);
    }

    public TextView getChangesView() {
        return changesView;
    }
}
