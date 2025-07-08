package com.example.samplestickerapp.modelView.factory;

import android.content.Context;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;

import com.example.samplestickerapp.exception.StickerDataBaseException;
import com.example.samplestickerapp.exception.StickerException;
import com.example.samplestickerapp.model.StickerPack;
import com.example.samplestickerapp.modelView.StickerPackViewModel;
import com.example.samplestickerapp.repository.MyDatabase;

public class StickerPackViewModelFactory implements ViewModelProvider.Factory {

    private final MyDatabase myDatabase;

    public StickerPackViewModelFactory(Context context) throws StickerException {
        this.myDatabase = MyDatabase.getInstance(context);
    }

    @Override
    public <T extends ViewModel> T create(Class<T> modelClass) {
        if (modelClass.isAssignableFrom(StickerPackViewModel.class)) {
            return (T) new StickerPackViewModel(myDatabase);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }

    public static StickerPackViewModel create(ViewModelStoreOwner viewModelStoreOwner, Context applicationContext) throws StickerException {
        return new ViewModelProvider(viewModelStoreOwner,
                new StickerPackViewModelFactory(applicationContext)).get(StickerPackViewModel.class);
    }

}
