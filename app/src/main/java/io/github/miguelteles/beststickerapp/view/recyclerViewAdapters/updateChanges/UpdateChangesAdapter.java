package io.github.miguelteles.beststickerapp.view.recyclerViewAdapters.updateChanges;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import io.github.miguelteles.beststickerapp.R;

public class UpdateChangesAdapter extends RecyclerView.Adapter<UpdateChangesViewHolder>{

    private List<String> changes;

    public UpdateChangesAdapter(List<String> changes) {
        this.changes = changes;
    }

    @NonNull
    @Override
    public UpdateChangesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final Context context = parent.getContext();
        final LayoutInflater layoutInflater = LayoutInflater.from(context);
        final View changesRow = layoutInflater.inflate(R.layout.update_changes_list_item, parent, false);
        return new UpdateChangesViewHolder(changesRow);
    }

    @Override
    public void onBindViewHolder(@NonNull UpdateChangesViewHolder holder, int position) {
        String change = changes.get(position);
        holder.getChangesView().setText(change);
    }

    @Override
    public int getItemCount() {
        return changes.size();
    }
}
