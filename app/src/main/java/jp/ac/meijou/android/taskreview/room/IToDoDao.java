package jp.ac.meijou.android.taskreview.room;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface IToDoDao {
    @Query("SELECT * FROM todo_list")
    List<ToDo> getAll();

    @Query("SELECT * FROM todo_list WHERE visible = :visible")
    List<ToDo> getVisibilityAll(boolean visible);

    @Insert
    void insertAll(ToDo... todos);

    @Query("DELETE FROM todo_list")
    void deleteAll();

    @Query("DELETE FROM todo_list WHERE id = :id")
    void delete(int id);

    @Update
    void update(ToDo todo);
}
