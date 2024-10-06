package com.example.samplestickerapp.repository;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.example.samplestickerapp.activity.StickerPackLoader;
import com.example.samplestickerapp.exception.StickerException;
import com.example.samplestickerapp.exception.enums.StickerDBExceptionEnum;
import com.example.samplestickerapp.model.Sticker;
import com.example.samplestickerapp.model.StickerPack;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MyDatabase {

    private static SQLiteDatabase myDB;
    private static File dbFile;
    private static StickerPackRepository stickerPackRepository = new StickerPackRepository();
    private static StickerRepository stickerRepository = new StickerRepository();
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
                    "originalTrayImageFile TEXT NOT NULL," +
                    "resizedTrayImageFile TEXT NOT NULL," +
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

    static SQLiteDatabase getMyDB(Context context) throws StickerException {
        if (myDB == null) {
            inicializaBancoETabelas(context);
            return myDB;
        } else {
            return myDB;
        }
    }

    public static StickerPackRepository getStickerPackRepository() {
        return stickerPackRepository;
    }

    public static StickerRepository getStickerRepository() {
        return stickerRepository;
    }
}
