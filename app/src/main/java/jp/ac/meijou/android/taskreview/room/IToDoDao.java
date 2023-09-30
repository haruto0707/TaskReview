package jp.ac.meijou.android.taskreview.room;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

/**
 * DBの操作を行うクラス<br>
 * このクラスを通じてDBの操作を行う。メソッドの定義は{@code @Query("SQL文")}などのアノテーションで定義する。<br>
 * このクラスを使用する場合は{@link jp.ac.meijou.android.taskreview.room.ToDoDatabase}
 * を通じてインスタンスを取得する。また、このクラスを使用する場合は別のスレッドで実行する必要がある。
 * <pre>{@code
 *    //サンプルコード
 *    var handlerThread = new HandlerThread("threadName");
 *    handlerThread.start();
 *    var asyncHandler = new Handler(handlerThread.getLooper());
 *    //thisはContextで、呼び出すActivityやServiceなどを指定
 *    var db = ToDoDatabase.getInstance(this);
 *    dao = db.toDoDao();
 *    asyncHandler.post(() -> {
 *        //DBの操作を行う
 *        var to_Do = new ToDo(isPersonal, "やること", "科目", "推定時間", "期限", Priority, "詳細", 表示フラグ);
 *        dao.insert(to_Do);
 *        // 必要に応じてスレッドを終了する。onDestroy内で行ってもよい。
 *        handlerThread.quit();
 *    });
 * }</pre>
 */
@Dao
public interface IToDoDao {
    /**
     * DBに格納されている全てのデータを取得する
     * @return DBに格納されている全てのデータ
     */
    @Query("SELECT * FROM todo_list")
    List<ToDo> getAll();
    /**
     * DBに格納されているデータの内、表示が{@code visible}と一致するデータを全て取得する
     * @param visible 表示可能なデータを取得する場合はtrue、そうでない場合はfalse
     * @return DBに格納されているデータの内、表示が{@code visible}と一致するデータ
     */
    @Query("SELECT * FROM todo_list WHERE visible = :visible")
    List<ToDo> getVisibilityAll(boolean visible);
    /**
     * DBに格納されているデータの内、{@code id}と一致するデータを取得する
     * @param id 取得するデータのID
     * @param isPersonal 取得するデータが個人で作成されたものであるかどうか
     * @return DBに格納されているデータの内、{@code id}と一致するデータ
     */
    @Query("SELECT * FROM todo_list WHERE id = :id AND is_personal = :isPersonal")
    ToDo get(int id, boolean isPersonal);
    /**
     * DBにデータを追加する
     * @param todo 追加するデータ
     */
    @Insert
    void insert(ToDo todo);
    /**
     * DB内のデータを全て削除する
     */
    @Query("DELETE FROM todo_list")
    void deleteAll();
    /**
     * DB内のデータの内、{@code id}と一致するデータを削除する
     * @param id 削除するデータのID
     */
    @Query("DELETE FROM todo_list WHERE id = :id")
    void delete(int id);

    /**
     * DB内のデータを更新する
     * @param todo 更新するデータ
     */
    @Update
    void update(ToDo todo);
}
