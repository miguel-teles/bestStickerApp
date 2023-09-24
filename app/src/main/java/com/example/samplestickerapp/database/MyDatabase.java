package com.example.samplestickerapp.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.samplestickerapp.exception.StickerException;
import com.example.samplestickerapp.exception.StickerExceptionHandler;
import com.example.samplestickerapp.exception.enums.StickerDBExceptionEnum;
import com.example.samplestickerapp.utils.Folders;

import java.io.File;

public class MyDatabase {

    private static SQLiteDatabase myDB;

    private static File dbFile;

    final private static String dbName = "stickersDB";

    public static void inicializaBancoETabelas(Context context) throws StickerException {
        try {
            myDB = criaBancoOuBusca(context);
            criaTabelas(context);

        } catch (StickerException ste) {
            throw ste;
        } catch (Exception ex) {
            throw new StickerException(ex, "inicializaBancoETabelas", StickerDBExceptionEnum.INI, null);
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
            throw new StickerException(ex, "criaBancoOuBusca", StickerDBExceptionEnum.CREATE_OR_OPEN, "");
        }
    }

    private static void criaTabelas(Context context) throws StickerException {
        criaTabelaPacks(context);
        criaTabelaSticker(context);

        Cursor cursor = myDB.rawQuery("select * from packs", null);
    }

    private static void criaTabelaPacks(Context context) throws StickerException {
        try {
            String tbPack = "CREATE TABLE IF NOT EXISTS packs(" +
                    "identifier INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "name TEXT NOT NULL," +
                    "publisher TEXT NOT NULL," +
                    "trayImageFile TEXT NOT NULL," +
                    "imageDataVersion INT NOT NULL," +
                    "avoidCache INTEGER NOT NULL," +
                    "publisherEmail TEXT," +
                    "publisherWebsite TEXT," +
                    "privacyPolicyWebsite TEXT," +
                    "licenseAgreementWebsite TEXT" +
                    ")";

            getMyDB(context).execSQL(tbPack);

            String insert = "INSERT INTO packs VALUES ('miguel', 'oasdmaso', 'asdsad', 'asdasda','asdasdsa')";
            getMyDB(context).execSQL(insert);
        } catch (StickerException ste) {
            throw ste;
        } catch (Exception ex) {
            throw new StickerException(ex, "criaTabelaPacks", StickerDBExceptionEnum.CREATE_TBL, "Tabela Packs não foi criada com sucesso");
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
            throw new StickerException(ex, "criaTabelaSticker", StickerDBExceptionEnum.CREATE_TBL, "Tabela Sticker não foi criada com sucesso");
        }
    }

    private static SQLiteDatabase getMyDB(Context context) throws StickerException {
        if (myDB == null || !myDB.isOpen()) {
            inicializaBancoETabelas(context);
            return myDB;
        } else {
            return myDB;
        }
    }


}
