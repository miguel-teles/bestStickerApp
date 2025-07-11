package io.github.miguelteles.beststickerapp.repository;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import io.github.miguelteles.beststickerapp.exception.StickerDataBaseException;
import io.github.miguelteles.beststickerapp.exception.StickerException;
import io.github.miguelteles.beststickerapp.exception.enums.StickerDataBaseExceptionEnum;
import io.github.miguelteles.beststickerapp.domain.entity.Sticker;

import java.util.ArrayList;
import java.util.List;

public class StickerRepository extends CommonRepository implements io.github.miguelteles.beststickerapp.repository.interfaces.Repository<Sticker> {

    private String SAVE = "INSERT INTO stickers VALUES (null, ?, ?, ?, ?)";
    private String FIND_ALL_BY_PACKIDENTIFIER = "SELECT * FROM stickers WHERE packIdentifier=%d";

    private SQLiteDatabase sqLiteDatabase;

    public StickerRepository(SQLiteDatabase sqLiteDatabase) {
        super(Sticker.NM_TABELA);
        this.sqLiteDatabase = sqLiteDatabase;
    }

    @Override
    public Sticker save(Sticker sticker) throws StickerException {
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
                throw new StickerDataBaseException(null, StickerDataBaseExceptionEnum.INSERT, "Erro ao inserir dado, retorno -1");
            }

        } catch (StickerException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new StickerDataBaseException(ex, StickerDataBaseExceptionEnum.INSERT, ex.getMessage());
        }
    }

    @Override
    public Sticker update(Sticker obj) throws StickerException {
        return null;
    }

    @Override
    public Integer remove(Sticker sticker) throws StickerException {
        return this.remove(sticker.getIdentifier());
    }

    @Override
    public Integer remove(Integer identifier) throws StickerException {
        try {
            String deleteStickers = "DELETE FROM stickers WHERE identifier=?";
            SQLiteStatement stmt = sqLiteDatabase.compileStatement(deleteStickers);
            stmt.bindLong(1, identifier);
            stmt.executeUpdateDelete();
            return null;
        } catch (Exception ex) {
            throw new StickerDataBaseException(ex, StickerDataBaseExceptionEnum.DELETE, "Erro ao deletar figurinhas do pacote");
        }
    }

    public void removeByPackIdentifier(Integer packIdentifier) throws StickerException {
        try {
            SQLiteStatement stmt = sqLiteDatabase.compileStatement(DELETE_BY_ID);
            stmt.bindLong(1, packIdentifier);
            stmt.executeUpdateDelete();
        } catch (Exception ex) {
            throw new StickerDataBaseException(ex, StickerDataBaseExceptionEnum.DELETE, "Erro ao deletar figurinhas do pacote");
        }
    }

    @Override
    public Sticker findById(Integer id) throws StickerException {
        return null;
    }

    @Override
    public List<Sticker> findAll() throws StickerException {
        return null;
    }

    public List<Sticker> findByPackIdentifier(int packIdentifier) throws StickerDataBaseException {
        Cursor cursor = null;
        try {
            String selectStickersQuery = String.format(FIND_ALL_BY_PACKIDENTIFIER, packIdentifier);
            cursor = sqLiteDatabase.rawQuery(selectStickersQuery, null);
            cursor.moveToFirst();

            List<Sticker> stickersList = new ArrayList<>();
            while (!cursor.isAfterLast()) {
                stickersList.add(new Sticker(cursor.getInt(cursor.getColumnIndexOrThrow(Sticker.IDENTIFIER)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(Sticker.PACK_IDENTIFIER)),
                        cursor.getString(cursor.getColumnIndexOrThrow(Sticker.STICKER_IMAGE_FILE))));
                cursor.moveToNext();
            }

            return stickersList;

        } catch (Exception ex) {
            throw new StickerDataBaseException(ex, StickerDataBaseExceptionEnum.SELECT, "Erro ao selecionar stickers do pacote " + packIdentifier + ". " + ex.getMessage());
        } finally {
            if (cursor != null) cursor.close();
        }
    }
}
