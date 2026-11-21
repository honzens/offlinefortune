package com.honzens.offlinefortune;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.honzens.offlinefortune.database.DBHelper;
import com.honzens.offlinefortune.database.DBInitializer;
import com.honzens.offlinefortune.model.FortunePoem;
import com.honzens.offlinefortune.repository.FortuneRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends Activity {
    private FortuneRepository repository;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private Spinner systemSpinner;
    private ListView poemList;
    private TextView status;
    private List<String> systems = new ArrayList<>();
    private List<FortunePoem> poems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buildLayout();
        status.setText("正在初始化本機籤詩資料庫…");

        executor.execute(() -> {
            try {
                DBHelper helper = new DBHelper(getApplicationContext());
                DBInitializer.initialize(getApplicationContext(), helper);
                helper.close();
                repository = new FortuneRepository(getApplicationContext());
                runOnUiThread(this::loadSystems);
            } catch (Exception e) {
                runOnUiThread(() -> status.setText("資料庫初始化失敗：" + e.getMessage()));
            }
        });
    }

    private void buildLayout() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(18), dp(18), dp(18), dp(12));
        root.setBackgroundColor(0xFFF8F3E9);

        TextView title = new TextView(this);
        title.setText("靈籤寶典");
        title.setTextSize(26);
        title.setTextColor(0xFF603B24);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, dp(10), 0, dp(6));
        root.addView(title, new LinearLayout.LayoutParams(-1, -2));

        TextView subtitle = new TextView(this);
        subtitle.setText("離線籤詩查詢與解讀");
        subtitle.setTextSize(14);
        subtitle.setTextColor(0xFF806B54);
        subtitle.setGravity(Gravity.CENTER);
        root.addView(subtitle, new LinearLayout.LayoutParams(-1, -2));

        systemSpinner = new Spinner(this);
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(-1, -2);
        sp.topMargin = dp(20);
        root.addView(systemSpinner, sp);

        status = new TextView(this);
        status.setTextSize(13);
        status.setTextColor(0xFF806B54);
        status.setPadding(0, dp(10), 0, dp(8));
        root.addView(status);

        poemList = new ListView(this);
        root.addView(poemList, new LinearLayout.LayoutParams(-1, 0, 1));

        TextView footer = new TextView(this);
        footer.setText("所有資料儲存於手機，不需要網路或伺服器");
        footer.setTextSize(12);
        footer.setTextColor(0xFF806B54);
        footer.setGravity(Gravity.CENTER);
        footer.setPadding(0, dp(10), 0, dp(4));
        root.addView(footer);

        setContentView(root);
    }

    private void loadSystems() {
        systems = repository.getSystemNames();
        if (systems.isEmpty()) {
            status.setText("資料庫目前沒有籤詩資料。");
            return;
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, systems);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        systemSpinner.setAdapter(adapter);
        systemSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                loadPoems(systems.get(position));
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        poemList.setOnItemClickListener((parent, view, position, id) ->
                showPoemDetail(poems.get(position)));
    }

    private void loadPoems(String systemName) {
        executor.execute(() -> {
            int systemId = repository.getSystemIdByName(systemName);
            List<FortunePoem> result = repository.getPoems(systemId);
            runOnUiThread(() -> {
                poems = result;
                ArrayAdapter<FortunePoem> adapter = new ArrayAdapter<>(
                        this, android.R.layout.simple_list_item_1, poems);
                poemList.setAdapter(adapter);
                status.setText("共 " + poems.size() + " 支籤（示範資料）");
            });
        });
    }

    private void showPoemDetail(FortunePoem poem) {
        executor.execute(() -> {
            String interpretations = repository.getInterpretations(poem.id);
            String story = repository.getStory(poem.id);
            repository.addHistory(poem.id, "使用者從籤詩列表開啟");
            String message = poem.poemText + "\n\n" + interpretations + "\n" + story;
            runOnUiThread(() -> new AlertDialog.Builder(this)
                    .setTitle("第" + poem.lotNo + "籤 " + poem.rank)
                    .setMessage(message)
                    .setPositiveButton("關閉", null)
                    .show());
        });
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
        if (repository != null) repository.close();
    }
}
