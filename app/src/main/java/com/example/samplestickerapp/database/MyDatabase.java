package com.example.samplestickerapp.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.widget.ArrayAdapter;

import com.example.samplestickerapp.exception.StickerException;
import com.example.samplestickerapp.exception.StickerExceptionHandler;
import com.example.samplestickerapp.exception.enums.StickerCriticalExceptionEnum;
import com.example.samplestickerapp.exception.enums.StickerDBExceptionEnum;
import com.example.samplestickerapp.model.Sticker;
import com.example.samplestickerapp.model.StickerPack;
import com.example.samplestickerapp.utils.Folders;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MyDatabase {

    private static SQLiteDatabase myDB;

    private static File dbFile;

    final private static String dbName = "stickersDB.db";

    public static void inicializaBancoETabelas(Context context) throws StickerException {
        try {
            myDB = criaBancoOuBusca(context);
            criaTabelas(context);

        } catch (StickerException ste) {
            throw ste;
        } catch (Exception ex) {
            throw new StickerException(ex, StickerDBExceptionEnum.INI, null);
        }
    }

    public static SQLiteDatabase criaBancoOuBusca(Context c) throws StickerException {
        try {
            dbFile = c.getDatabasePath(dbName);
            if (!dbFile.getParentFile().exists()) {
                dbFile.getParentFile().mkdir();
            }
            SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(dbFile, null);
            return db;
        } catch (Exception ex) {
            throw new StickerException(ex, StickerDBExceptionEnum.CREATE_OR_OPEN, "");
        }
    }

    private static void criaTabelas(Context context) throws StickerException {
        criaTabelaPacks(context);
        criaTabelaSticker(context);
    }

    private static void criaTabelaPacks(Context context) throws StickerException {
        try {

//            String deleta = "DROP TABLE packs";
//            getMyDB(context).execSQL(deleta); //TODO: tirar isso

            String tbPack = "CREATE TABLE IF NOT EXISTS packs(" +
                    "identifier INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "name TEXT NOT NULL," +
                    "publisher TEXT NOT NULL," +
                    "trayImageFile TEXT NOT NULL," +
                    "folder TEXT NOT NULL," +
                    "imageDataVersion INTEGER NOT NULL," +
                    "avoidCache INTEGER NOT NULL," +
                    "publisherEmail TEXT," +
                    "publisherWebsite TEXT," +
                    "privacyPolicyWebsite TEXT," +
                    "licenseAgreementWebsite TEXT," +
                    "animatedStickerPack INTEGER NOT NULL" +
                    ")";

            getMyDB(context).execSQL(tbPack);
        } catch (StickerException ste) {
            throw ste;
        } catch (Exception ex) {
            throw new StickerException(ex, StickerDBExceptionEnum.CREATE_TBL, "Tabela Packs não foi criada com sucesso - " + ex.getMessage());
        }
    }

    private static void criaTabelaSticker(Context context) throws StickerException {
        try {
            String tbPack = "CREATE TABLE IF NOT EXISTS stickers(" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "emoji TEXT NOT NULL," +
                    "imageFile TEXT NOT NULL," +
                    "packIdentifier INTEGER, " +
                    "FOREIGN KEY (packIdentifier) REFERENCES packs(identifier)" +
                    ")";

            getMyDB(context).execSQL(tbPack);
        } catch (StickerException ste) {
            throw ste;
        } catch (Exception ex) {
            throw new StickerException(ex, StickerDBExceptionEnum.CREATE_TBL, "Tabela Sticker não foi criada com sucesso");
        }
    }

    private static SQLiteDatabase getMyDB(Context context) throws StickerException {
        if (myDB == null) {
            inicializaBancoETabelas(context);
            return myDB;
        } else {
            return myDB;
        }
    }

    public static Long inserirPacote(StickerPack stickerPack, Context context) throws StickerException {
        try {
            String insert = "INSERT INTO packs VALUES (null, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            SQLiteStatement stmt = getMyDB(context).compileStatement(insert);

            stmt.bindString(1, stickerPack.getName());
            stmt.bindString(2, stickerPack.getPublisher());
            stmt.bindString(3, stickerPack.getTrayImageFile() == null ? "" : stickerPack.getTrayImageFile());
            stmt.bindString(4, stickerPack.getFolder());
            stmt.bindLong(5, Integer.parseInt(stickerPack.getImageDataVersion()));
            stmt.bindLong(6, stickerPack.isAvoidCache() ? 1 : 0);
            stmt.bindString(7, stickerPack.getPublisherEmail());
            stmt.bindString(8, stickerPack.getPublisherWebsite());
            stmt.bindString(9, stickerPack.getPrivacyPolicyWebsite());
            stmt.bindString(10, stickerPack.getLicenseAgreementWebsite());
            stmt.bindLong(11, stickerPack.isAnimatedStickerPack() ? 1 : 0);

            long result = stmt.executeInsert();
            if (result != -1) {
                return result;
            } else {
                throw new StickerException(null, StickerDBExceptionEnum.INSERT, "Erro ao inserir dado, retorno -1");
            }

        } catch (StickerException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new StickerException(ex, StickerDBExceptionEnum.INSERT, ex.getMessage());
        }
    }


    public static List<StickerPack> selectAllStickerPacks(Context context) throws StickerException {
        try {

            List<StickerPack> stickerPackList = new ArrayList<>();

            Cursor meuCursor = getMyDB(context).rawQuery("SELECT * FROM packs", null);

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
                        meuCursor.getString(3), //trayImageFile
                        meuCursor.getString(4), //folder
                        meuCursor.getInt(5) == 0 ? null : Integer.valueOf(meuCursor.getInt(4)), //imageDataVersion
                        meuCursor.getInt(6) == 1 ? true : false, //avoidCache
                        meuCursor.getString(7), //publisher_email
                        meuCursor.getString(8), //publisher_website
                        meuCursor.getString(9), //privacy_policy_website
                        meuCursor.getString(10),
                        meuCursor.getInt(11) == 1 ? true : false,
                        selectStickersFromPack(meuCursor.getInt(0), context)); //license_agreement_website

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

    private static List<Sticker> selectStickersFromPack(int packIdentifier, Context context) throws Exception {
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
                        emojiList));
                cursor.moveToNext();
            }

            return stickersList;

        } catch (Exception ex) {
            throw new StickerException(ex, StickerDBExceptionEnum.SELECT, "Erro ao selecionar stickers do pacote " + packIdentifier + ". " + ex.getMessage());
        }
    }

    public static Long inserirFigurinha(Sticker sticker, Integer packIdentifier, Context context) throws StickerException {
        try {
            String insert = "INSERT INTO stickers VALUES (null, ?, ?, ?)";
            SQLiteStatement stmt = getMyDB(context).compileStatement(insert);

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
            stmt.bindLong(2, packIdentifier);

            long result = stmt.executeInsert();
            if (result != -1) {
                return result;
            } else {
                throw new StickerException(null, StickerDBExceptionEnum.INSERT, "Erro ao inserir dado, retorno -1");
            }

        } catch (StickerException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new StickerException(ex, StickerDBExceptionEnum.INSERT, ex.getMessage());
        }
    }

    public static Long updateStickerPack(String identifier, StickerPack stickerPack, Context context) throws StickerException {
        try {

            String updateQuery = "UPDATE packs SET name=?, publisher=?, trayImageFile=?, imageDataVersion=imageDataVersion+1 WHERE identifier=?";
            SQLiteStatement stmt = getMyDB(context).compileStatement(updateQuery);

            stmt.bindString(0, stickerPack.getName());
            stmt.bindString(1, stickerPack.getPublisher());
            stmt.bindString(2, stickerPack.getTrayImageFile());
            stmt.bindString(3, identifier);

            long result = (long) stmt.executeUpdateDelete();
            if (result != -1) {
                return result;
            } else {
                throw new StickerException(null, StickerDBExceptionEnum.UPDATE, "Erro ao chamar o update");
            }

        } catch (StickerException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new StickerException(ex, StickerDBExceptionEnum.UPDATE, "Erro ao atualizar dados do pacote " + identifier);
        }
    }

    public static void deleteStickerPack(String identifier, Context context) throws StickerException {
        SQLiteDatabase sqLiteDatabase = getMyDB(context);

        try {
            String deleteStickers = "DELETE FROM stickers WHERE packIdentifier=?";
            SQLiteStatement stmt = sqLiteDatabase.compileStatement(deleteStickers);
            stmt.bindString(0, identifier);
            if (stmt.executeUpdateDelete() != -1) {
                throw new StickerException(null, StickerDBExceptionEnum.DELETE, "Figurinhas não foram deletadas");
            }

        } catch (StickerException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new StickerException(ex, StickerDBExceptionEnum.DELETE, "Erro ao deletar figurinhas do pacote");
        }

        try {
            String deleteStickers = "DELETE FROM packs WHERE identifier=?";
            SQLiteStatement stmt = sqLiteDatabase.compileStatement(deleteStickers);
            stmt.bindString(0, identifier);
            if (stmt.executeUpdateDelete() != -1) {
                throw new StickerException(null, StickerDBExceptionEnum.DELETE, "Pacote não foi deletado!");
            }
        } catch (StickerException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new StickerException(ex, StickerDBExceptionEnum.DELETE, "Erro ao deletar pacote");
        }

    }
}
