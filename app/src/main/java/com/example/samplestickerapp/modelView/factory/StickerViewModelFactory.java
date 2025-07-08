package com.example.samplestickerapp.modelView.factory;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;

import com.example.samplestickerapp.exception.StickerDataBaseException;
import com.example.samplestickerapp.exception.StickerException;
import com.example.samplestickerapp.modelView.StickerPackViewModel;
import com.example.samplestickerapp.modelView.StickerViewModel;
import com.example.samplestickerapp.repository.MyDatabase;

public class StickerViewModelFactory implements ViewModelProvider.Factory{
    private final MyDatabase myDatabase;

    public StickerViewModelFactory(Context context) throws StickerException {
        this.myDatabase = MyDatabase.getInstance(context);
    }

    @Override
    public <T extends ViewModel> T create(Class<T> modelClass) {
        if (modelClass.isAssignableFrom(StickerViewModel.class)) {
            return (T) new StickerViewModel(myDatabase);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }

    public static StickerViewModel create(ViewModelStoreOwner viewModelStoreOwner, Context applicationContext) throws StickerException {
        return new ViewModelProvider(viewModelStoreOwner,
                new StickerViewModelFactory(applicationContext)).get(StickerViewModel.class);
    }
}
