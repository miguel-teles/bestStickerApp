package com.example.samplestickerapp.repository;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.example.samplestickerapp.exception.StickerException;
import com.example.samplestickerapp.exception.enums.StickerDBExceptionEnum;
import com.example.samplestickerapp.model.Sticker;

import java.util.ArrayList;
import java.util.List;

public class StickerRepository extends Repository<Sticker> {

    private String SAVE = "INSERT INTO stickers VALUES (null, ?, ?, ?)";

    public StickerRepository() {
        super(Sticker.NM_TABELA);
    }

    @Override
    public Sticker save(Sticker sticker, Context context) throws StickerException {
        try {
            SQLiteStatement stmt = getMyDB(context).compileStatement(SAVE);

            /*
            *  "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "emoji TEXT NOT NULL," +
                    "imageFile TEXT NOT NULL," +
                    "packIdentifier INTEGER, " +
                    "FOREIGN KEY (packIdentifier) REFERENCES packs(identifier)" +
                    ")";
            * */

            stmt.bindString(1, "");
            stmt.bindString(1, sticker.getImageFileName());
            stmt.bindLong(2, sticker.getPackIdentifier());

            long result = stmt.executeInsert();
            if (result != -1) {
                Cursor cursor = getMyDB(context).rawQuery("select last_insert_rowid()", null);
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
        SQLiteDatabase sqLiteDatabase = getMyDB(context);

        try {
            String deleteStickers = "DELETE FROM stickers WHERE packIdentifier=?";
            SQLiteStatement stmt = sqLiteDatabase.compileStatement(deleteStickers);
            stmt.bindLong(0, packIdentifier);
            if (stmt.executeUpdateDelete() != -1) {
                throw new StickerException(null, StickerDBExceptionEnum.DELETE, "Figurinhas não foram deletadas");
            }
            return null;
        } catch (StickerException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new StickerException(ex, StickerDBExceptionEnum.DELETE, "Erro ao deletar figurinhas do pacote");
        }
    }

    @Override
    public Integer remove(Integer id, Context context) throws StickerException {
        return null;
    }

    @Override
    public Sticker find(Integer id, Context context) throws StickerException {
        return null;
    }

    @Override
    public List<Sticker> findAll(Context context) throws StickerException {
        return null;
    }

    public List<Sticker> findByPackIdentifier(int packIdentifier, Context context) throws Exception {
        try {
            String selectStickersQuery = "SELECT * FROM stickers WHERE packIdentifier=" + packIdentifier;
            Cursor cursor = getMyDB(context).rawQuery(selectStickersQuery, null);

            if (cursor != null) {
                cursor.moveToFirst();
            }

            List<Sticker> stickersList = new ArrayList<>();
            while (!cursor.isAfterLast()) {
                List<String> emojiList = new ArrayList<>();
                for (String str : cursor.getString(1).split("|")) {
                    emojiList.add(str);
                }

                stickersList.add(new Sticker(cursor.getString(2),
                        emojiList,
                        packIdentifier));
                cursor.moveToNext();
            }

            return stickersList;

        } catch (Exception ex) {
            throw new StickerException(ex, StickerDBExceptionEnum.SELECT, "Erro ao selecionar stickers do pacote " + packIdentifier + ". " + ex.getMessage());
        }
    }
}
