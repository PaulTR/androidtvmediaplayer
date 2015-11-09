package com.apress.mediaplayer;

import android.app.SearchManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.provider.BaseColumns;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Paul on 11/8/15.
 */
public class VideoDatabaseHandler {

    private static final String DATABASE_NAME = "video_database_leanback";
    private static final int DATABASE_VERSION = 1;
    private static final String FTS_VIRTUAL_TABLE = "Leanback_table";
    private final VideoDatabaseOpenHelper mDatabaseOpenHelper;

    private static final HashMap<String, String> COLUMN_MAP = buildColumnMap();

    public static final String KEY_NAME = SearchManager.SUGGEST_COLUMN_TEXT_1;
    public static final String KEY_DATA_TYPE = SearchManager.SUGGEST_COLUMN_CONTENT_TYPE;
    public static final String KEY_PRODUCTION_YEAR = SearchManager.SUGGEST_COLUMN_PRODUCTION_YEAR;
    public static final String KEY_COLUMN_DURATION = SearchManager.SUGGEST_COLUMN_DURATION;
    public static final String KEY_ACTION = SearchManager.SUGGEST_COLUMN_INTENT_ACTION;


    public VideoDatabaseHandler(Context context) {
        mDatabaseOpenHelper = new VideoDatabaseOpenHelper(context);
    }

    public Cursor getWordMatch(String query, String[] columns) {
        /* This builds a query that looks like:
         *     SELECT <columns> FROM <table> WHERE <KEY_WORD> MATCH 'query*'
         * which is an FTS3 search for the query text (plus a wildcard) inside the word column.
         *
         * - "rowid" is the unique id for all rows but we need this value for the "_id" column in
         *    order for the Adapters to work, so the columns need to make "_id" an alias for "rowid"
         * - "rowid" also needs to be used by the SUGGEST_COLUMN_INTENT_DATA alias in order
         *   for suggestions to carry the proper intent data.SearchManager
         *   These aliases are defined in the VideoProvider when queries are made.
         * - This can be revised to also search the definition text with FTS3 by changing
         *   the selection clause to use FTS_VIRTUAL_TABLE instead of KEY_WORD (to search across
         *   the entire table, but sorting the relevance could be difficult.
         */
        String selection = KEY_NAME + " MATCH ?";
        String[] selectionArgs = new String[]{query + "*"};

        return query(selection, selectionArgs, columns);
    }

    private Cursor query(String selection, String[] selectionArgs, String[] columns) {
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables(FTS_VIRTUAL_TABLE);
        builder.setProjectionMap(COLUMN_MAP);

        Cursor cursor = builder.query(mDatabaseOpenHelper.getReadableDatabase(), columns, selection, selectionArgs, null, null, null);

        if (cursor == null) {
            return null;
        } else if (!cursor.moveToFirst()) {
            cursor.close();
            return null;
        }
        return cursor;
    }

    private static HashMap<String, String> buildColumnMap() {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(KEY_NAME, KEY_NAME);
        map.put(KEY_DATA_TYPE, KEY_DATA_TYPE);
        map.put(KEY_PRODUCTION_YEAR, KEY_PRODUCTION_YEAR);
        map.put(KEY_COLUMN_DURATION, KEY_COLUMN_DURATION);
        map.put( KEY_ACTION, KEY_ACTION );
        map.put(BaseColumns._ID, "rowid AS " +
                BaseColumns._ID);
        map.put(SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID, "rowid AS " +
                SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID);
        map.put(SearchManager.SUGGEST_COLUMN_SHORTCUT_ID, "rowid AS " +
                SearchManager.SUGGEST_COLUMN_SHORTCUT_ID);
        return map;
    }

    private static class VideoDatabaseOpenHelper extends SQLiteOpenHelper {

        private final Context mHelperContext;
        private SQLiteDatabase mDatabase;

        private static final String FTS_TABLE_CREATE =
                "CREATE VIRTUAL TABLE " + FTS_VIRTUAL_TABLE +
                        " USING fts3 (" +
                        KEY_NAME + ", " +
                        KEY_DATA_TYPE + "," +
                        KEY_ACTION + "," +
                        KEY_PRODUCTION_YEAR + "," +
                        KEY_COLUMN_DURATION + ");";

        VideoDatabaseOpenHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
            mHelperContext = context;
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            mDatabase = db;
            mDatabase.execSQL(FTS_TABLE_CREATE);
            loadDatabase();
        }

        private void loadDatabase() {
            new Thread(new Runnable() {
                public void run() {
                    try {
                        loadVideos();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }).start();
        }

        private void loadVideos() throws IOException {
            List<Video> videos;

            String json = Utils.loadJSONFromResource( mHelperContext, R.raw.videos );
            Type collection = new TypeToken<ArrayList<Video>>(){}.getType();

            Gson gson = new Gson();
            videos = gson.fromJson( json, collection );


            for( Video video : videos ) {
                addVideoForDeepLink( video );
            }
        }

        public void addVideoForDeepLink( Video video ) {
            ContentValues initialValues = new ContentValues();

            initialValues.put(KEY_NAME, video.getTitle());
            initialValues.put(KEY_DATA_TYPE, "video/mp4");
            initialValues.put(KEY_PRODUCTION_YEAR, "2015");
            initialValues.put(KEY_COLUMN_DURATION, 6400000);

            mDatabase.insert(FTS_VIRTUAL_TABLE, null, initialValues);
        }


        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }
}
