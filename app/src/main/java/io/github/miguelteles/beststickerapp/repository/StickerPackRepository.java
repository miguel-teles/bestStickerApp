package io.github.miguelteles.beststickerapp.repository;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import io.github.miguelteles.beststickerapp.exception.StickerDataBaseException;
import io.github.miguelteles.beststickerapp.exception.StickerException;
import io.github.miguelteles.beststickerapp.exception.enums.StickerDataBaseExceptionEnum;
import io.github.miguelteles.beststickerapp.domain.entity.StickerPack;
import io.github.miguelteles.beststickerapp.repository.interfaces.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class StickerPackRepository extends CommonRepository implements Repository<StickerPack> {

    private String PERSIST = "INSERT INTO packs VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private String UPDATE = "UPDATE packs SET name=?, publisher=?, imageDataVersion=(imageDataVersion+1) WHERE identifier=?";

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

            UUID id = UUID.randomUUID();
            stmt.bindString(1, id.toString());
            stmt.bindString(2, stickerPack.getName());
            stmt.bindString(3, stickerPack.getPublisher());
            stmt.bindString(4, stickerPack.getOriginalTrayImageFile());
            stmt.bindString(5, stickerPack.getResizedTrayImageFile());
            stmt.bindString(6, stickerPack.getFolderName());
            stmt.bindLong(7, stickerPack.getImageDataVersion());
            stmt.bindLong(8, stickerPack.isAvoidCache() ? 1 : 0);
            stmt.bindString(9, stickerPack.getPublisherEmail());
            stmt.bindString(10, stickerPack.getPublisherWebsite());
            stmt.bindString(11, stickerPack.getPrivacyPolicyWebsite());
            stmt.bindString(12, stickerPack.getLicenseAgreementWebsite());
            stmt.bindLong(13, stickerPack.isAnimatedStickerPack() ? 1 : 0);

            long result = stmt.executeInsert();
            if (result != -1) {
                stickerPack.setIdentifier(id);

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
            stmt.bindString(3, stickerPack.getIdentifier().toString());

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
    public void remove(StickerPack stickerPack) throws StickerException {
        remove(stickerPack.getIdentifier());
    }

    @Override
    public void remove(UUID identifier) throws StickerException {
        try {
            stickerRepository.removeByPackIdentifier(identifier);

            SQLiteStatement stmt = sqLiteDatabase.compileStatement(DELETE_BY_ID);
            stmt.bindString(1, identifier.toString());
            stmt.executeUpdateDelete();
        } catch (StickerException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new StickerDataBaseException(ex, StickerDataBaseExceptionEnum.DELETE, "Erro ao deletar pacote de figurinhas");
        }
    }

    @Override
    public StickerPack findById(UUID id) throws StickerException {
        Cursor cursor = null;
        try {
            cursor = sqLiteDatabase.rawQuery(FIND_BY_ID.replace("?", "'" + id.toString() + "'"), null);
            cursor.moveToFirst();

            return new StickerPack(UUID.fromString(cursor.getString(0)), //identifier
                    cursor.getString(1), //name
                    cursor.getString(2), //publisher
                    cursor.getString(3), //originalTrayImageFile
                    cursor.getString(4), //resizedTrayImageFile
                    cursor.getString(5), //folder
                    cursor.getInt(6), //imageDataVersion
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
                StickerPack stickerPack = new StickerPack(UUID.fromString(cursor.getString(0)), //identifier
                        cursor.getString(1), //name
                        cursor.getString(2), //publisher
                        cursor.getString(3), //originalTrayImageFile
                        cursor.getString(4), //resizedTrayImageFile
                        cursor.getString(5), //folder
                        cursor.getInt(6), //imageDataVersion
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
