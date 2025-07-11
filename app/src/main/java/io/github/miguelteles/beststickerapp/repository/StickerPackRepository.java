package io.github.miguelteles.beststickerapp.repository;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import io.github.miguelteles.beststickerapp.exception.StickerDataBaseException;
import io.github.miguelteles.beststickerapp.exception.StickerException;
import io.github.miguelteles.beststickerapp.exception.enums.StickerDataBaseExceptionEnum;
import io.github.miguelteles.beststickerapp.domain.entity.StickerPack;
import io.github.miguelteles.beststickerapp.repository.contentProvider.StickerUriProvider;
import io.github.miguelteles.beststickerapp.repository.interfaces.Repository;
import io.github.miguelteles.beststickerapp.services.StickerServiceImpl;
import io.github.miguelteles.beststickerapp.services.interfaces.StickerService;

import java.util.ArrayList;
import java.util.List;

public class StickerPackRepository extends CommonRepository implements Repository<StickerPack> {

    private String PERSIST = "INSERT INTO packs VALUES (null, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private String UPDATE = "UPDATE packs SET name=?, publisher=?, imageDataVersion=imageDataVersion+1 WHERE identifier=?";

    public StickerRepository stickerRepository;
    private final SQLiteDatabase sqLiteDatabase;

    public StickerPackRepository(SQLiteDatabase sqLiteDatabase) {
        super(StickerPack.NM_TABELA);
        this.sqLiteDatabase = sqLiteDatabase;
        stickerRepository = new StickerRepository(sqLiteDatabase);
    }

    @Override
    public StickerPack save(StickerPack stickerPack) throws StickerException {
        try {
            SQLiteStatement stmt = sqLiteDatabase.compileStatement(PERSIST);

            stmt.bindString(1, stickerPack.getName());
            stmt.bindString(2, stickerPack.getPublisher());
            stmt.bindString(3, stickerPack.getOriginalTrayImageFile());
            stmt.bindString(4, stickerPack.getResizedTrayImageFile());
            stmt.bindString(5, stickerPack.getFolderName());
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
                cursor.close();

                return stickerPack;
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
    public StickerPack update(StickerPack stickerPack) throws StickerException {
        try {
            SQLiteStatement stmt = sqLiteDatabase.compileStatement(UPDATE);

            stmt.bindString(1, stickerPack.getName());
            stmt.bindString(2, stickerPack.getPublisher());
            stmt.bindLong(3, stickerPack.getIdentifier());

            long result = (long) stmt.executeUpdateDelete();
            if (result != -1) {
                return stickerPack;
            } else {
                throw new StickerDataBaseException(null, StickerDataBaseExceptionEnum.UPDATE, "Erro ao chamar o update");
            }

        } catch (StickerException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new StickerDataBaseException(ex, StickerDataBaseExceptionEnum.UPDATE, "Erro ao atualizar dados do pacote " + stickerPack.getIdentifier());
        }
    }

    @Override
    public Integer remove(StickerPack stickerPack) throws StickerException {
        return remove(stickerPack.getIdentifier());
    }

    @Override
    public Integer remove(Integer identifier) throws StickerException {
        try {
            stickerRepository.removeByPackIdentifier(identifier);

            SQLiteStatement stmt = sqLiteDatabase.compileStatement(DELETE_BY_ID);
            stmt.bindLong(1, identifier);
            stmt.executeUpdateDelete();
        } catch (StickerException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new StickerDataBaseException(ex, StickerDataBaseExceptionEnum.DELETE, "Erro ao deletar pacote de figurinhas");
        }
        return null;
    }

    @Override
    public StickerPack findById(Integer id) throws StickerException {
        Cursor cursor = null;
        try {
            cursor = sqLiteDatabase.rawQuery(FIND_BY_ID.replace("?", id.toString()), null);
            cursor.moveToFirst();

            return new StickerPack(cursor.getInt(0), //identifier
                    cursor.getString(1), //name
                    cursor.getString(2), //publisher
                    cursor.getString(3), //originalTrayImageFile
                    cursor.getString(4), //resizedTrayImageFile
                    cursor.getString(5), //folder
                    cursor.getInt(6) == 0 ? null : cursor.getInt(4), //imageDataVersion
                    cursor.getInt(7) == 1, //avoidCache
                    cursor.getString(8), //publisher_email
                    cursor.getString(9), //publisher_website
                    cursor.getString(10), //privacy_policy_website
                    cursor.getString(11), //license_agreement_website
                    cursor.getInt(12) == 1,//animated
                    null);
        } catch (Exception ex) {
            throw new StickerDataBaseException(ex, StickerDataBaseExceptionEnum.SELECT, "Erro ao buscar pack por ID");
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    @Override
    public List<StickerPack> findAll() throws StickerException {
        Cursor cursor = null;
        try {
            List<StickerPack> stickerPackList = new ArrayList<>();
            cursor = sqLiteDatabase.rawQuery(FIND_ALL, null);
            cursor.moveToFirst();

            while (!cursor.isAfterLast()) {
                StickerPack stickerPack = new StickerPack(cursor.getInt(0), //identifier
                        cursor.getString(1), //name
                        cursor.getString(2), //publisher
                        cursor.getString(3), //originalTrayImageFile
                        cursor.getString(4), //resizedTrayImageFile
                        cursor.getString(5), //folder
                        cursor.getInt(6) == 0 ? null : Integer.valueOf(cursor.getInt(4)), //imageDataVersion
                        cursor.getInt(7) == 1, //avoidCache
                        cursor.getString(8), //publisher_email
                        cursor.getString(9), //publisher_website
                        cursor.getString(10), //privacy_policy_website
                        cursor.getString(11), //license_agreement_website
                        cursor.getInt(12) == 1,//animated
                        null);

                stickerPackList.add(stickerPack);
                cursor.moveToNext();
            }
            return stickerPackList;
        } catch (Exception ex) {
            throw new StickerDataBaseException(ex, StickerDataBaseExceptionEnum.SELECT, "Erro ao buscar todos os stickerPacks. " + ex.getMessage());
        } finally {
            if (cursor != null) cursor.close();
        }
    }
}
