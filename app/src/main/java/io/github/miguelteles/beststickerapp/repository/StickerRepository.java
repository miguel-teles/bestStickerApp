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
import java.util.UUID;

public class StickerRepository extends CommonRepository implements io.github.miguelteles.beststickerapp.repository.interfaces.Repository<Sticker> {

    private String SAVE = "INSERT INTO stickers VALUES (?, ?, ?)";
    private String FIND_ALL_BY_PACKIDENTIFIER = "SELECT * FROM stickers WHERE packIdentifier='%s'";

    private SQLiteDatabase sqLiteDatabase;

    public StickerRepository(SQLiteDatabase sqLiteDatabase) {
        super(Sticker.NM_TABELA);
        this.sqLiteDatabase = sqLiteDatabase;
    }

    @Override
    public Sticker save(Sticker sticker) throws StickerException {
        try {
            SQLiteStatement stmt = sqLiteDatabase.compileStatement(SAVE);

            UUID id = UUID.randomUUID();
            stmt.bindString(1, id.toString());
            stmt.bindString(2, sticker.getStickerImageFile());
            stmt.bindString(3, sticker.getPackIdentifier().toString());

            long result = stmt.executeInsert();
            if (result != -1) {
                sticker.setIdentifier(id);

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
    public void remove(Sticker sticker) throws StickerException {
        this.remove(sticker.getIdentifier());
    }

    @Override
    public void remove(UUID identifier) throws StickerException {
        try {
            String deleteStickers = "DELETE FROM stickers WHERE identifier=?";
            SQLiteStatement stmt = sqLiteDatabase.compileStatement(deleteStickers);
            stmt.bindString(1, identifier.toString());
            stmt.executeUpdateDelete();
        } catch (Exception ex) {
            throw new StickerDataBaseException(ex, StickerDataBaseExceptionEnum.DELETE, "Erro ao deletar figurinhas do pacote");
        }
    }

    public void removeByPackIdentifier(UUID packIdentifier) throws StickerException {
        try {
            SQLiteStatement stmt = sqLiteDatabase.compileStatement(DELETE_BY_ID);
            stmt.bindString(1, packIdentifier.toString());
            stmt.executeUpdateDelete();
        } catch (Exception ex) {
            throw new StickerDataBaseException(ex, StickerDataBaseExceptionEnum.DELETE, "Erro ao deletar figurinhas do pacote");
        }
    }

    @Override
    public Sticker findById(UUID id) throws StickerException {
        return null;
    }

    @Override
    public List<Sticker> findAll() throws StickerException {
        return null;
    }

    public List<Sticker> findByPackIdentifier(UUID packIdentifier) throws StickerDataBaseException {
        Cursor cursor = null;
        try {
            String selectStickersQuery = String.format(FIND_ALL_BY_PACKIDENTIFIER, packIdentifier.toString());
            cursor = sqLiteDatabase.rawQuery(selectStickersQuery, null);
            cursor.moveToFirst();

            List<Sticker> stickersList = new ArrayList<>();
            while (!cursor.isAfterLast()) {
                stickersList.add(new Sticker(UUID.fromString(cursor.getString(cursor.getColumnIndexOrThrow(Sticker.IDENTIFIER))),
                        UUID.fromString(cursor.getString(cursor.getColumnIndexOrThrow(Sticker.PACK_IDENTIFIER))),
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
