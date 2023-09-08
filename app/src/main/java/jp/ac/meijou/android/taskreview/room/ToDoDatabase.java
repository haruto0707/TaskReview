package jp.ac.meijou.android.taskreview.room;

import android.content.Context;

import androidx.room.Room;

/**
 * RoomによるDBのデータを格納するクラスの定義、DBの操作を行うクラスを格納するクラス
 */
public class ToDoDatabase {
    /** DB名 */
    private static final String DB_NAME = "todo_list-db";
    /** DBのインスタンス */
    private static AppDatabase db;
    /**
     * DBのインスタンスを取得する
     */
    public static AppDatabase getInstance(Context context) {
        if (db == null) {
            db = Room.databaseBuilder(context.getApplicationContext(),
                    AppDatabase.class, DB_NAME)
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return db;
    }
}
