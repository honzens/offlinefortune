package com.honzens.offlinefortune.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
    public static final String DB_NAME = "fortune.db";
    public static final int DB_VERSION = 1;

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        setWriteAheadLoggingEnabled(true);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE deities (" +
                "id INTEGER PRIMARY KEY, name TEXT NOT NULL, description TEXT)");
        db.execSQL("CREATE TABLE temples (" +
                "id INTEGER PRIMARY KEY, name TEXT NOT NULL, address TEXT, description TEXT)");
        db.execSQL("CREATE TABLE fortune_systems (" +
                "id INTEGER PRIMARY KEY, deity_id INTEGER NOT NULL, name TEXT NOT NULL, " +
                "total_lots INTEGER NOT NULL, description TEXT, " +
                "FOREIGN KEY(deity_id) REFERENCES deities(id))");
        db.execSQL("CREATE TABLE fortune_versions (" +
                "id INTEGER PRIMARY KEY, system_id INTEGER NOT NULL, temple_id INTEGER, " +
                "version_name TEXT NOT NULL, source_note TEXT, " +
                "FOREIGN KEY(system_id) REFERENCES fortune_systems(id), " +
                "FOREIGN KEY(temple_id) REFERENCES temples(id))");
        db.execSQL("CREATE TABLE fortune_poems (" +
                "id INTEGER PRIMARY KEY, version_id INTEGER NOT NULL, lot_no INTEGER NOT NULL, " +
                "rank TEXT, poem_text TEXT NOT NULL, " +
                "FOREIGN KEY(version_id) REFERENCES fortune_versions(id), " +
                "UNIQUE(version_id, lot_no))");
        db.execSQL("CREATE TABLE fortune_categories (" +
                "id INTEGER PRIMARY KEY, code TEXT NOT NULL UNIQUE, name TEXT NOT NULL)");
        db.execSQL("CREATE TABLE interpretations (" +
                "id INTEGER PRIMARY KEY, poem_id INTEGER NOT NULL, category_id INTEGER NOT NULL, " +
                "content TEXT NOT NULL, FOREIGN KEY(poem_id) REFERENCES fortune_poems(id), " +
                "FOREIGN KEY(category_id) REFERENCES fortune_categories(id), " +
                "UNIQUE(poem_id, category_id))");
        db.execSQL("CREATE TABLE fortune_stories (" +
                "id INTEGER PRIMARY KEY, poem_id INTEGER NOT NULL, title TEXT NOT NULL, content TEXT NOT NULL, " +
                "FOREIGN KEY(poem_id) REFERENCES fortune_poems(id))");
        db.execSQL("CREATE TABLE fortune_history (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, poem_id INTEGER NOT NULL, question TEXT, " +
                "created_at INTEGER NOT NULL, FOREIGN KEY(poem_id) REFERENCES fortune_poems(id))");
        db.execSQL("CREATE TABLE favorites (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, poem_id INTEGER NOT NULL UNIQUE, created_at INTEGER NOT NULL, " +
                "FOREIGN KEY(poem_id) REFERENCES fortune_poems(id))");
        db.execSQL("CREATE TABLE app_meta (meta_key TEXT PRIMARY KEY, meta_value TEXT NOT NULL)");

        db.execSQL("CREATE INDEX idx_poems_version ON fortune_poems(version_id)");
        db.execSQL("CREATE INDEX idx_interpretations_poem ON interpretations(poem_id)");
        db.execSQL("CREATE INDEX idx_history_created ON fortune_history(created_at DESC)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 後續版本在此加入逐版 migration；不要直接刪除使用者資料。
    }
}
