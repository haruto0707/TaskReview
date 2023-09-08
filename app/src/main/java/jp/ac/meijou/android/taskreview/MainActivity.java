package jp.ac.meijou.android.taskreview;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.os.Process;
import android.os.Handler;
import android.os.HandlerThread;

import jp.ac.meijou.android.taskreview.databinding.ActivityMainBinding;
import jp.ac.meijou.android.taskreview.room.IToDoDao;
import jp.ac.meijou.android.taskreview.room.ToDo;
import jp.ac.meijou.android.taskreview.room.ToDoDatabase;
import jp.ac.meijou.android.taskreview.room.ToDoDiffCallback;
import jp.ac.meijou.android.taskreview.room.ToDoListAdapter;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;

    // DBアクセス用のスレッド
    private HandlerThread handlerThread;
    // DBアクセス用のスレッドのハンドラ
    private Handler asyncHandler;
    // DBアクセス用のDAO
    private IToDoDao dao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initToDoList(binding.toDoView);
    }

    /**
     * ToDoリストを初期化するメソッド<br>
     * データベース取得用のスレッドを初期化、開始し、RecycleViewの初期化し、データベースからToDoリストを取得する。
     * スレッドを生成した場合は終了時({@link jp.ac.meijou.android.taskreview.MainActivity#onDestroy()})
     * や画面遷移時などに終了するように設定する<br>
     * {@link ToDoDatabase} データベースの管理するクラス<br>
     * {@link ToDoDiffCallback} ToDoリストに変更があった際に、通知するためのクラス<br>
     * {@link ToDoListAdapter} ToDoリストの表示を制御するためのクラス<br>
     * @param recyclerView ToDoリストを表示するRecyclerView
     */
    private void initToDoList(RecyclerView recyclerView) {

        handlerThread = new HandlerThread("db-thread", Process.THREAD_PRIORITY_DEFAULT);
        handlerThread.start();
        asyncHandler = new Handler(handlerThread.getLooper());

        var db = ToDoDatabase.getInstance(this);
        dao = db.toDoDao();

        var callback = new ToDoDiffCallback();
        var adapter = new ToDoListAdapter(callback);

        asyncHandler.post(() -> {
            var list = dao.getAll();
            adapter.submitList(list);
        });
        recyclerView.setAdapter(adapter);
        var layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        var itemDecoration = new DividerItemDecoration(this, layoutManager.getOrientation());
        recyclerView.addItemDecoration(itemDecoration);
        binding.button.setOnClickListener(v -> {
            var toDo = new ToDo("タイトル", "科目", "推定時間", "期限", ToDo.Priority.LOW, "詳細", "メモ");
            asyncHandler.post(() -> {
                dao.insertAll(toDo);
                var list = dao.getAll();
                adapter.submitList(list);
            });
        });
        binding.button2.setOnClickListener(v -> {
            asyncHandler.post(() -> {
                dao.deleteAll();
                var list = dao.getAll();
                adapter.submitList(list);
            });
        });
    }

    /**
     * 終了時はスレッドを終了させる。
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        handlerThread.quit();
    }
}