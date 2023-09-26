package com.example.samplestickerapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.samplestickerapp.model.StickerPack;
import com.example.samplestickerapp.utils.Utils;
import com.google.gson.Gson;

public class CreateNewStickerPack extends Activity {

    public static final String EXTRA_STICKER_PACK_LIST_DATA = "sticker_pack_list";
    private static final int STICKER_PREVIEW_DISPLAY_LIMIT = 5;
    private TextView btnAdicionarStickerPack;
    private EditText edtNomePacote;
    private ImageView stickerPackImageView;
    private Gson gson;

    private Uri uriImagemStickerPack;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_create_sticker_pack);

        btnAdicionarStickerPack = findViewById(R.id.adicionarStickerPack);
        btnAdicionarStickerPack.setOnClickListener(btnAdicionarStickerPackOnClick());
        edtNomePacote = findViewById(R.id.edtNomePacote);
        stickerPackImageView = findViewById(R.id.pacoteImageView);
        stickerPackImageView.setOnClickListener(pacoteImageViewOnClick());

        gson = Utils.configuraGson();
    }

    public View.OnClickListener pacoteImageViewOnClick() {
        Context context = this;
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, Utils.PICK_IMAGE_REQUEST_CODE);
            }
        };
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Utils.PICK_IMAGE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            stickerPackImageView.setImageURI(imageUri);
            uriImagemStickerPack = imageUri;
            stickerPackImageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        }
    }

    public View.OnClickListener btnAdicionarStickerPackOnClick() {
        Context context = this;
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                    StickerPack stickerPackToAdd = new StickerPack(StickerPackLoader.getNewIdentifier(context).toString(),
                            edtNomePacote.getText().toString(),
                            "Aplicativo Foda",
                            uriImagemStickerPack.getPath(),
                            "934",
                            false);



                }

        };
    }

}
