package com.example.samplestickerapp.repository.implementations;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.example.samplestickerapp.exception.StickerException;
import com.example.samplestickerapp.exception.enums.StickerDBExceptionEnum;
import com.example.samplestickerapp.model.Sticker;
import com.example.samplestickerapp.model.StickerPack;
import com.example.samplestickerapp.repository.Repository;

import java.util.ArrayList;
import java.util.List;

public class StickerRepository extends Repository<Sticker> {

    private String SAVE = "INSERT INTO stickers VALUES (null, ?, ?, ?, ?)";
    private String FIND_ALL_BY_PACKIDENTIFIER = "SELECT * FROM stickers WHERE packIdentifier=%d";

    private SQLiteDatabase sqLiteDatabase;

    public StickerRepository(SQLiteDatabase sqLiteDatabase) {
        super(Sticker.NM_TABELA);
        this.sqLiteDatabase = sqLiteDatabase;
    }

    @Override
    public Sticker save(Sticker sticker, Context context) throws StickerException {
        try {
            SQLiteStatement stmt = sqLiteDatabase.compileStatement(SAVE);

            /*
            *  "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "emoji TEXT NOT NULL," +
                    "imageFile TEXT NOT NULL," +
                    "packIdentifier INTEGER, " +
                    "size INTEGER, " +
                    "FOREIGN KEY (packIdentifier) REFERENCES packs(identifier)" +
                    ")";
            * */

            stmt.bindString(1, "");
            stmt.bindString(2, sticker.getStickerImageFile());
            stmt.bindLong(3, sticker.getPackIdentifier());
            stmt.bindLong(4, sticker.getSize());

            long result = stmt.executeInsert();
            if (result != -1) {
                Cursor cursor = sqLiteDatabase.rawQuery("select last_insert_rowid()", null);
                cursor.moveToFirst();
                sticker.setIdentifier(cursor.getInt(0));

                return sticker;
            } else {
                throw new StickerException(null, StickerDBExceptionEnum.INSERT, "Erro ao inserir dado, retorno -1");
            }

        } catch (StickerException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new StickerException(ex, StickerDBExceptionEnum.INSERT, ex.getMessage());
        }
    }

    @Override
    public Sticker update(Sticker obj, Context context) throws StickerException {
        return null;
    }

    @Override
    public Integer remove(Sticker obj, Context context) throws StickerException {
        return null;
    }

    public Integer removeByPackIdentifier(Integer packIdentifier, Context context) throws StickerException {
        try {
            String deleteStickers = "DELETE FROM stickers WHERE packIdentifier=?";
            SQLiteStatement stmt = sqLiteDatabase.compileStatement(deleteStickers);
            stmt.bindLong(1, packIdentifier);
            stmt.executeUpdateDelete();
            return null;
        } catch (Exception ex) {
            throw new StickerException(ex, StickerDBExceptionEnum.DELETE, "Erro ao deletar figurinhas do pacote");
        }
    }

    @Override
    public Integer remove(Integer id, Context context) throws StickerException {
        return null;
    }

    @Override
    public Sticker findById(Integer id) throws StickerException {
        return null;
    }

    @Override
    public List<Sticker> findAll() throws StickerException {
        return null;
    }

    public List<Sticker> findByPackIdentifier(int packIdentifier) throws Exception {
        try {
            String selectStickersQuery = String.format(FIND_ALL_BY_PACKIDENTIFIER, packIdentifier);
            Cursor cursor = sqLiteDatabase.rawQuery(selectStickersQuery, null);

            if (cursor != null) {
                cursor.moveToFirst();
            }

            List<Sticker> stickersList = new ArrayList<>();
            while (!cursor.isAfterLast()) {
                stickersList.add(new Sticker(cursor.getInt(cursor.getColumnIndexOrThrow(Sticker.IDENTIFIER)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(Sticker.PACK_IDENTIFIER)),
                        cursor.getString(cursor.getColumnIndexOrThrow(Sticker.STICKER_IMAGE_FILE))));
                cursor.moveToNext();
            }

            return stickersList;

        } catch (Exception ex) {
            throw new StickerException(ex, StickerDBExceptionEnum.SELECT, "Erro ao selecionar stickers do pacote " + packIdentifier + ". " + ex.getMessage());
        }
    }
}
