package jp.ac.meijou.android.taskreview.room;

import android.content.Context;

import androidx.room.Room;

public class ToDoDatabase {
    private static AppDatabase db;
    public static AppDatabase getInstance(Context context) {
        if (db == null) {
            db = Room.databaseBuilder(context.getApplicationContext(),
                    AppDatabase.class, "todo_list-db")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return db;
    }
}
