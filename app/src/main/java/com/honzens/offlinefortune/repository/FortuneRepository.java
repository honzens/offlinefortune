package com.honzens.offlinefortune.repository;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.honzens.offlinefortune.database.DBHelper;
import com.honzens.offlinefortune.model.FortunePoem;
import java.util.ArrayList;
import java.util.List;

public class FortuneRepository {
    private final DBHelper helper;

    public FortuneRepository(Context context) {
        helper = new DBHelper(context.getApplicationContext());
    }

    public List<String> getSystemNames() {
        List<String> names = new ArrayList<>();
        SQLiteDatabase db = helper.getReadableDatabase();
        try (Cursor c = db.rawQuery("SELECT name FROM fortune_systems ORDER BY id", null)) {
            while (c.moveToNext()) names.add(c.getString(0));
        }
        return names;
    }

    public int getSystemIdByName(String name) {
        SQLiteDatabase db = helper.getReadableDatabase();
        try (Cursor c = db.rawQuery("SELECT id FROM fortune_systems WHERE name=? LIMIT 1",
                new String[]{name})) {
            return c.moveToFirst() ? c.getInt(0) : -1;
        }
    }

    public List<FortunePoem> getPoems(int systemId) {
        List<FortunePoem> poems = new ArrayList<>();
        SQLiteDatabase db = helper.getReadableDatabase();
        String sql = "SELECT p.id,p.lot_no,p.rank,p.poem_text " +
                "FROM fortune_poems p JOIN fortune_versions v ON p.version_id=v.id " +
                "WHERE v.system_id=? ORDER BY p.lot_no";
        try (Cursor c = db.rawQuery(sql, new String[]{String.valueOf(systemId)})) {
            while (c.moveToNext()) {
                poems.add(new FortunePoem(c.getLong(0), c.getInt(1),
                        c.getString(2), c.getString(3)));
            }
        }
        return poems;
    }

    public String getInterpretations(long poemId) {
        StringBuilder result = new StringBuilder();
        SQLiteDatabase db = helper.getReadableDatabase();
        String sql = "SELECT c.name,i.content FROM interpretations i " +
                "JOIN fortune_categories c ON i.category_id=c.id " +
                "WHERE i.poem_id=? ORDER BY c.id";
        try (Cursor c = db.rawQuery(sql, new String[]{String.valueOf(poemId)})) {
            while (c.moveToNext()) {
                result.append("\n【").append(c.getString(0)).append("】\n")
                        .append(c.getString(1)).append("\n");
            }
        }
        return result.length() == 0 ? "目前沒有解釋資料。" : result.toString();
    }

    public String getStory(long poemId) {
        SQLiteDatabase db = helper.getReadableDatabase();
        try (Cursor c = db.rawQuery(
                "SELECT title,content FROM fortune_stories WHERE poem_id=? LIMIT 1",
                new String[]{String.valueOf(poemId)})) {
            if (c.moveToFirst()) return "【" + c.getString(0) + "】\n" + c.getString(1);
        }
        return "目前沒有典故資料。";
    }

    public void addHistory(long poemId, String question) {
        SQLiteDatabase db = helper.getWritableDatabase();
        db.execSQL("INSERT INTO fortune_history(poem_id,question,created_at) VALUES(?,?,?)",
                new Object[]{poemId, question, System.currentTimeMillis()});
    }

    public void close() {
        helper.close();
    }
}
