package io.github.miguelteles.beststickerapp.repository;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import io.github.miguelteles.beststickerapp.exception.StickerDataBaseException;
import io.github.miguelteles.beststickerapp.exception.StickerException;
import io.github.miguelteles.beststickerapp.exception.enums.StickerDataBaseExceptionEnum;

public class MyDatabase extends SQLiteOpenHelper {

    private static MyDatabase instance;
    private SQLiteDatabase sqLiteDatabase;
    final private static String dbName = "stickersDB.db";

    private MyDatabase(Context context) throws StickerException {
        super(context, dbName, null, 5);
        sqLiteDatabase = getWritableDatabase();
        criaTabelas(sqLiteDatabase);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        sqLiteDatabase = db;
        try {
            criaTabelas(db);
        } catch (StickerException e) {
            throw new RuntimeException(e);
        }
    }

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

    public SQLiteDatabase getSqLiteDatabase() {
        return sqLiteDatabase;
    }
}
