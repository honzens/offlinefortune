package com.honzens.offlinefortune.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public final class DBInitializer {
    private static final String DATA_VERSION = "1";

    private DBInitializer() {}

    public static void initialize(Context context, DBHelper helper) throws Exception {
        SQLiteDatabase db = helper.getWritableDatabase();
        String current = null;
        try (android.database.Cursor c = db.rawQuery(
                "SELECT meta_value FROM app_meta WHERE meta_key='seed_version'", null)) {
            if (c.moveToFirst()) current = c.getString(0);
        }
        if (DATA_VERSION.equals(current)) return;

        String json = readAsset(context, "database/seed_data.json");
        JSONObject root = new JSONObject(json);
        db.beginTransaction();
        try {
            // Seed data is imported only once per data version.
            db.delete("interpretations", null, null);
            db.delete("fortune_stories", null, null);
            db.delete("fortune_poems", null, null);
            db.delete("fortune_versions", null, null);
            db.delete("fortune_systems", null, null);
            db.delete("deities", null, null);
            db.delete("fortune_categories", null, null);

            JSONArray deities = root.getJSONArray("deities");
            for (int i = 0; i < deities.length(); i++) {
                JSONObject o = deities.getJSONObject(i);
                db.execSQL("INSERT INTO deities(id,name,description) VALUES(?,?,?)",
                        new Object[]{o.getInt("id"), o.getString("name"), o.optString("description", "")});
            }

            JSONArray categories = root.getJSONArray("categories");
            for (int i = 0; i < categories.length(); i++) {
                JSONObject o = categories.getJSONObject(i);
                db.execSQL("INSERT INTO fortune_categories(id,code,name) VALUES(?,?,?)",
                        new Object[]{o.getInt("id"), o.getString("code"), o.getString("name")});
            }

            JSONArray systems = root.getJSONArray("systems");
            for (int i = 0; i < systems.length(); i++) {
                JSONObject system = systems.getJSONObject(i);
                int systemId = system.getInt("id");
                db.execSQL("INSERT INTO fortune_systems(id,deity_id,name,total_lots,description) VALUES(?,?,?,?,?)",
                        new Object[]{systemId, system.getInt("deity_id"), system.getString("name"),
                                system.getInt("total_lots"), system.optString("description", "")});
                int versionId = system.getInt("version_id");
                db.execSQL("INSERT INTO fortune_versions(id,system_id,temple_id,version_name,source_note) VALUES(?,?,NULL,?,?)",
                        new Object[]{versionId, systemId, system.optString("version_name", "通用示範版本"),
                                system.optString("source_note", "專案示範資料，非特定廟宇正式版本")});

                JSONArray poems = system.getJSONArray("poems");
                for (int j = 0; j < poems.length(); j++) {
                    JSONObject poem = poems.getJSONObject(j);
                    int poemId = poem.getInt("id");
                    db.execSQL("INSERT INTO fortune_poems(id,version_id,lot_no,rank,poem_text) VALUES(?,?,?,?,?)",
                            new Object[]{poemId, versionId, poem.getInt("lot_no"),
                                    poem.optString("rank", ""), poem.getString("poem_text")});

                    JSONArray interpretations = poem.optJSONArray("interpretations");
                    if (interpretations != null) {
                        for (int k = 0; k < interpretations.length(); k++) {
                            JSONObject item = interpretations.getJSONObject(k);
                            db.execSQL("INSERT INTO interpretations(id,poem_id,category_id,content) VALUES(?,?,?,?)",
                                    new Object[]{item.getInt("id"), poemId, item.getInt("category_id"),
                                            item.getString("content")});
                        }
                    }
                    JSONObject story = poem.optJSONObject("story");
                    if (story != null) {
                        db.execSQL("INSERT INTO fortune_stories(id,poem_id,title,content) VALUES(?,?,?,?)",
                                new Object[]{story.getInt("id"), poemId, story.getString("title"),
                                        story.getString("content")});
                    }
                }
            }
            db.execSQL("INSERT OR REPLACE INTO app_meta(meta_key,meta_value) VALUES('seed_version',?)",
                    new Object[]{DATA_VERSION});
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    private static String readAsset(Context context, String path) throws Exception {
        try (InputStream in = context.getAssets().open(path);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[4096];
            int count;
            while ((count = in.read(buffer)) != -1) out.write(buffer, 0, count);
            return out.toString(StandardCharsets.UTF_8.name());
        }
    }
}
