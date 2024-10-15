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
import com.example.samplestickerapp.view.StickerPackLoader;

import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class StickerPackRepository extends Repository<StickerPack> {

    private String PERSIST = "INSERT INTO packs VALUES (null, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private String UPDATE = "UPDATE packs SET name=?, publisher=?, imageDataVersion=imageDataVersion+1 WHERE identifier=?";

    public StickerRepository stickerRepository;

    private SQLiteDatabase sqLiteDatabase;

    public StickerPackRepository(SQLiteDatabase sqLiteDatabase) {
        super(StickerPack.NM_TABELA);
        this.sqLiteDatabase = sqLiteDatabase;
        stickerRepository = new StickerRepository(sqLiteDatabase);
    }

    @Override
    public StickerPack save(StickerPack stickerPack, Context context) throws StickerException {
        try {
            SQLiteStatement stmt = sqLiteDatabase.compileStatement(PERSIST);

            stmt.bindString(1, stickerPack.getName());
            stmt.bindString(2, stickerPack.getPublisher());
            stmt.bindString(3, stickerPack.getOriginalTrayImageFile());
            stmt.bindString(4, stickerPack.getResizedTrayImageFile());
            stmt.bindString(5, stickerPack.getFolder());
            stmt.bindLong(6, Integer.parseInt(stickerPack.getImageDataVersion()));
            stmt.bindLong(7, stickerPack.isAvoidCache() ? 1 : 0);
            stmt.bindString(8, stickerPack.getPublisherEmail());
            stmt.bindString(9, stickerPack.getPublisherWebsite());
            stmt.bindString(10, stickerPack.getPrivacyPolicyWebsite());
            stmt.bindString(11, stickerPack.getLicenseAgreementWebsite());
            stmt.bindLong(12, stickerPack.isAnimatedStickerPack() ? 1 : 0);

            long result = stmt.executeInsert();
            if (result != -1) {
                Cursor cursor = sqLiteDatabase.rawQuery("select last_insert_rowid()", null);
                cursor.moveToFirst();
                stickerPack.setIdentifier(cursor.getInt(0));

                context.getContentResolver().insert(StickerPackLoader.getStickerPackInsertUri(), stickerPack.toContentValues());
                return stickerPack;
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
    public StickerPack update(StickerPack stickerPack, Context context) throws StickerException {
        try {
            SQLiteStatement stmt = sqLiteDatabase.compileStatement(UPDATE);

            stmt.bindString(1, stickerPack.getName());
            stmt.bindString(2, stickerPack.getPublisher());
            stmt.bindLong(3, stickerPack.getIdentifier());

            long result = (long) stmt.executeUpdateDelete();
            if (result != -1) {
                return stickerPack;
            } else {
                throw new StickerException(null, StickerDBExceptionEnum.UPDATE, "Erro ao chamar o update");
            }

        } catch (StickerException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new StickerException(ex, StickerDBExceptionEnum.UPDATE, "Erro ao atualizar dados do pacote " + stickerPack.getIdentifier());
        }
    }

    @Override
    public Integer remove(StickerPack stickerPack, Context context) throws StickerException {
        return remove(stickerPack.getIdentifier(), context);
    }

    @Override
    public Integer remove(Integer identifier, Context context) throws StickerException {
        try {
            stickerRepository.removeByPackIdentifier(identifier, context);

            SQLiteStatement stmt = sqLiteDatabase.compileStatement(DELETAR_BY_ID);
            stmt.bindLong(1, identifier);
            stmt.executeUpdateDelete();
        } catch (StickerException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new StickerException(ex, StickerDBExceptionEnum.DELETE, "Erro ao deletar pacote de figurinhas");
        }
        return null;
    }

    @Override
    public StickerPack findById(Integer id) throws StickerException {
        try {
            Cursor meuCursor = sqLiteDatabase.rawQuery(FIND_BY_ID.replace("?", id.toString()), null);
            if (meuCursor != null) {
                meuCursor.moveToFirst();
            }

            StickerPack stickerPack = new StickerPack(meuCursor.getInt(0), //identifier
                    meuCursor.getString(1), //name
                    meuCursor.getString(2), //publisher
                    meuCursor.getString(3), //originalTrayImageFile
                    meuCursor.getString(4), //resizedTrayImageFile
                    meuCursor.getString(5), //folder
                    meuCursor.getInt(6) == 0 ? null : Integer.valueOf(meuCursor.getInt(4)), //imageDataVersion
                    meuCursor.getInt(7) == 1 ? true : false, //avoidCache
                    meuCursor.getString(8), //publisher_email
                    meuCursor.getString(9), //publisher_website
                    meuCursor.getString(10), //privacy_policy_website
                    meuCursor.getString(11), //license_agreement_website
                    meuCursor.getInt(12) == 1 ? true : false,//animated
                    stickerRepository.findByPackIdentifier(meuCursor.getInt(0)));


            return stickerPack;
        } catch (StickerException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new StickerException(ex, StickerDBExceptionEnum.SELECT, "Erro ao buscar pack por ID");
        }
    }

    @Override
    public List<StickerPack> findAll() throws StickerException {
        try {
            List<StickerPack> stickerPackList = new ArrayList<>();
            Cursor meuCursor = sqLiteDatabase.rawQuery(FIND_ALL, null);

            if (meuCursor != null)
                meuCursor.moveToFirst();

            /*
            *  /*
            * "identifier INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "name TEXT NOT NULL," +
                    "publisher TEXT NOT NULL," +
                    "trayImageFile TEXT NOT NULL," +
                    "imageDataVersion INTEGER NOT NULL," +
                    "avoidCache INTEGER NOT NULL," +
                    "publisherEmail TEXT," +
                    "publisherWebsite TEXT," +
                    "privacyPolicyWebsite TEXT," +
                    "licenseAgreementWebsite TEXT," +
                    "animatedStickerPack INTEGER NOT NULL" +
                    ")";
            *  */


            while (!meuCursor.isAfterLast()) {
                StickerPack stickerPack = new StickerPack(meuCursor.getInt(0), //identifier
                        meuCursor.getString(1), //name
                        meuCursor.getString(2), //publisher
                        meuCursor.getString(3), //originalTrayImageFile
                        meuCursor.getString(4), //resizedTrayImageFile
                        meuCursor.getString(5), //folder
                        meuCursor.getInt(6) == 0 ? null : Integer.valueOf(meuCursor.getInt(4)), //imageDataVersion
                        meuCursor.getInt(7) == 1 ? true : false, //avoidCache
                        meuCursor.getString(8), //publisher_email
                        meuCursor.getString(9), //publisher_website
                        meuCursor.getString(10), //privacy_policy_website
                        meuCursor.getString(11), //license_agreement_website
                        meuCursor.getInt(12) == 1 ? true : false,//animated
                        stickerRepository.findByPackIdentifier(meuCursor.getInt(0)));

                stickerPackList.add(stickerPack);
                meuCursor.moveToNext();
            }

            return stickerPackList;

        } catch (StickerException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new StickerException(ex, StickerDBExceptionEnum.SELECT, "Erro ao buscar todos os stickerPacks. " + ex.getMessage());
        }
    }
}
