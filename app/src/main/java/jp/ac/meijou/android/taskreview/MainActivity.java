package jp.ac.meijou.android.taskreview;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.os.Process;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.View.*;

import java.util.function.Function;

import jp.ac.meijou.android.taskreview.databinding.ActivityMainBinding;
import jp.ac.meijou.android.taskreview.room.IToDoDao;
import jp.ac.meijou.android.taskreview.room.ToDo;
import jp.ac.meijou.android.taskreview.room.ToDoDatabase;
import jp.ac.meijou.android.taskreview.room.ToDoDiffCallback;
import jp.ac.meijou.android.taskreview.ui.ToDoListAdapter;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;

    public static final String KEY_TODO_ID = "todo_id";
    /** 呼び出すスレッド名 */
    private static final String THREAD_NAME = "main_activity-db-thread";
    /** DBアクセス用のスレッド */
    private HandlerThread handlerThread;
    /** DBアクセス用のスレッドのハンドラ */
    private Handler asyncHandler;
    /** ToDoリストを管理するクラス */
    private ToDoListAdapter adapter;
    /** 画面遷移用のクラス */
    private ActivityResultLauncher<Intent> registerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initToDoList(binding.toDoView);
        initMenu();
    }

    /**
     * ToDoリストを初期化するメソッド<br>
     * データベース取得用のスレッドを初期化、開始し、RecycleViewの初期化し、データベースからToDoリストを取得する。
     * スレッドを生成した場合は終了時({@link jp.ac.meijou.android.taskreview.MainActivity#onDestroy()})
     * や画面遷移時などに終了するように設定する。<br>
     * {@link ToDoDatabase} データベースの管理するクラス<br>
     * {@link ToDoDiffCallback} ToDoリストに変更があった際に、通知するためのクラス<br>
     * {@link ToDoListAdapter} ToDoリストの表示を制御するためのクラス<br>
     * @param recyclerView ToDoリストを表示するRecyclerView
     */
    private void initToDoList(RecyclerView recyclerView) {
        // DBアクセス用のスレッドを初期化、開始する
        handlerThread = new HandlerThread(THREAD_NAME, Process.THREAD_PRIORITY_DEFAULT);
        handlerThread.start();
        asyncHandler = new Handler(handlerThread.getLooper());

        // DBアクセス用のDAOを初期化する、データベース内のデータを取得
        var db = ToDoDatabase.getInstance(this);
        var dao = db.toDoDao();

        // 画面遷移時の処理を設定
        registerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> asyncHandler.post(
                        () -> {
                            var list = dao.getVisibilityAll(true);
                            adapter.submitList(list);
                        }));

        // ToDoリストを管理するクラスを初期化する
        initToDoListAdapter(dao);

        // DBアクセス用スレッド内でデータベースからToDoリストを取得する
        asyncHandler.post(() -> {
            var list = dao.getVisibilityAll(true);
            adapter.submitList(list);
        });

        // RecyclerViewの初期化、設定
        recyclerView.setAdapter(adapter);
        var layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        var itemDecoration = new DividerItemDecoration(this, layoutManager.getOrientation());
        recyclerView.addItemDecoration(itemDecoration);

        // 削除ボタンの初期化、設定
        binding.button2.setOnClickListener(v -> asyncHandler.post(
                () -> {
                    dao.deleteAll();
                    var list = dao.getAll();
                    adapter.submitList(list);
                }));
    }

    /**
     * ToDoリストを管理するクラスを初期化するメソッド<br>
     * @param dao データベースのアクセス、操作を定義しているクラス
     */
    private void initToDoListAdapter(IToDoDao dao) {
        // ToDoListを管理するクラスを初期化する
        var callback = new ToDoDiffCallback();

        // ToDoリストの表示を非表示にする処理をするRunnableを返す関数インターフェースを定義
        Function<ToDo, Runnable> hideToDo = toDo -> () -> {
            toDo.visible = false;
            dao.update(toDo);
            var list = dao.getVisibilityAll(true);
            adapter.submitList(list);
        };

        // ToDoリストをクリックした際に画面遷移をするOnClickListenerを返す関数インターフェースを定義
        Function<ToDo, OnClickListener> openDetailIntent = toDo -> v -> {
            var intent = new Intent(this, RegisterActivity.class);
            intent.putExtra(KEY_TODO_ID, toDo.id);
            registerLauncher.launch(intent);
        };

        // Adapterを初期化する
        adapter = new ToDoListAdapter(callback, hideToDo, openDetailIntent);
    }

    /**
     * 画面下部メニューの初期化を行うメソッド
     */
    private void initMenu() {
        binding.menu.registerButton.setOnClickListener(v -> {
            var intent = new Intent(this, RegisterActivity.class);
            registerLauncher.launch(intent);
        });
    }

    /**
     * 終了時はスレッドを終了させる。
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        handlerThread.quit();
        adapter.finishThread();
    }
}