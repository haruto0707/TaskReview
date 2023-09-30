package jp.ac.meijou.android.taskreview.room;

import androidx.room.Database;
import androidx.room.RoomDatabase;

/**
 * DBの定義を行うクラス<br>
 * このクラスを通じてDBの定義を行う。<br>
 * DBの構成が変わった場合、versionの値を増加させる必要がある。なお、versionの値を変更された場合は
 * 以前のデータベースの中のデータは削除させる。<br>
 * {@code @Database(entities = {使用するクラス}, version = DBのバージョン, exportSchema = false)}
 * のように定義する。<br>
 * このクラスを使用する場合は{@link jp.ac.meijou.android.taskreview.room.ToDoDatabase}
 * を通じてインスタンスを取得する。
 */
@Database(entities = {ToDo.class}, version = 4, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract IToDoDao toDoDao();
}
