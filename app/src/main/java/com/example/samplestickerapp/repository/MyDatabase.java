package com.example.samplestickerapp.repository;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.samplestickerapp.exception.StickerDataBaseException;
import com.example.samplestickerapp.exception.StickerException;
import com.example.samplestickerapp.exception.enums.StickerDataBaseExceptionEnum;

public class MyDatabase extends SQLiteOpenHelper {

    private static MyDatabase instance;
    private SQLiteDatabase myDB;
    final private static String dbName = "stickersDB.db";

    private MyDatabase(Context context) throws StickerException {
        super(context, dbName, null, 5);
        myDB = getWritableDatabase();
        criaTabelas(myDB);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        myDB = db;
        criaTabelas(db);
    }

    //TODO: criar o negócio pra fechar o banco -

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        dropaTabelas(db);
        onCreate(db);
    }

    public static MyDatabase getInstance(Context context) throws StickerException {
        if (instance == null) {
            instance = new MyDatabase(context);
        }
        return instance;
    }

    private void dropaTabelas(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS packs");
        db.execSQL("DROP TABLE IF EXISTS stickers");
    }

    private void criaTabelas(SQLiteDatabase db) throws StickerException {
        criaTabelaPacks(db);
        criaTabelaSticker(db);
    }

    private void criaTabelaPacks(SQLiteDatabase db) throws StickerException {
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

            db.execSQL(tbPack);
        } catch (Exception ex) {
            throw new StickerDataBaseException(ex, StickerDataBaseExceptionEnum.CREATE_TBL, "Tabela Packs não foi criada com sucesso - " + ex.getMessage());
        }
    }

    private void criaTabelaSticker(SQLiteDatabase db) throws StickerException {
        try {
            String tbPack = "CREATE TABLE IF NOT EXISTS stickers(" +
                    "identifier INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "emoji TEXT NOT NULL," +
                    "stickerImageFile TEXT NOT NULL," +
                    "packIdentifier INTEGER, " +
                    "size INTEGER, " +
                    "FOREIGN KEY (packIdentifier) REFERENCES packs(identifier)" +
                    ")";

            db.execSQL(tbPack);
        } catch (Exception ex) {
            throw new StickerDataBaseException(ex, StickerDataBaseExceptionEnum.CREATE_TBL, "Tabela Sticker não foi criada com sucesso");
        }
    }

    public SQLiteDatabase getMyDB() {
        return myDB;
    }
}
