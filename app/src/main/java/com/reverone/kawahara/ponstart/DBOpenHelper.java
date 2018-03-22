package com.reverone.kawahara.ponstart;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Locale;

/**
 * データベース接続
 * Created by kawahara on 2017/05/12.
 * 2017.10.8 J.Kawahara サンプルデータの英語化に対応
 * 2017.10.25 J.Kawahara 不要なログ出力を削除
 */

class DBOpenHelper {

    private static final String DB_NAME = "pon_start";
    private static final int DB_VERSION = 1;

    private static DBOpenHelper _instance;
    private DatabaseHelper _databaseHelper;

    private DBOpenHelper() {
        _databaseHelper = null;
    }

    public static DBOpenHelper getInstance() {
        if (_instance == null) {
            _instance = new DBOpenHelper();
        }
        return _instance;
    }

    void initialize(Context context) {
        _databaseHelper = new DatabaseHelper(context);
    }

    SQLiteDatabase openWritable() {
        if (_databaseHelper == null) {
            throw new RuntimeException("SQLieDatabase.openWritable() Database should be initialize.");
        }
        return _databaseHelper.getWritableDatabase();
    }

    SQLiteDatabase openReadable() {
        if (_databaseHelper == null) {
            throw new RuntimeException("SQLieDatabase.openReadable() Database should be initialize.");
        }
        return _databaseHelper.getReadableDatabase();
    }

    void close() {
        if (_databaseHelper != null) {
            _databaseHelper.close();
        }
    }

    private class DatabaseHelper extends SQLiteOpenHelper {
        private Context _context;
        DatabaseHelper(final Context context) {
            super(context, DB_NAME, null, DB_VERSION);
            this._context = context;
        }
        @Override
        public void onCreate(final SQLiteDatabase db) {
            // テーブルを作成する
            execFileSQL(db, "create_table.sql");

            // サンプルデータを作成する
            String sql_file = "insert_demo_data.sql";
            if (Locale.getDefault().equals(Locale.JAPAN)) {
                sql_file = "insert_demo_data_ja.sql";
            }
            execFileSQL(db, sql_file);
        }

        @Override
        public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {

        }

        private void execFileSQL(SQLiteDatabase db, String fileName){
            InputStream in = null;
            InputStreamReader inReader = null;
            BufferedReader reader = null;
            try {
                // 文字コード(UTF-8)を指定して、ファイルを読み込み
                in = _context.getAssets().open(fileName);
                inReader = new InputStreamReader(in, "UTF-8");
                reader = new BufferedReader(inReader);

                // ファイル内の全ての行を処理
                String s;
                while((s = reader.readLine()) != null){
                    // 先頭と末尾の空白除去
                    s = s.trim();

                    // 文字が存在する場合（空白行は処理しない）
                    if (0 < s.length()){
                        // SQL実行
                        db.execSQL(s);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {

                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if (inReader != null) {
                    try {
                        inReader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
