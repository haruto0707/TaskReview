package jp.ac.meijou.android.taskreview;

import static jp.ac.meijou.android.taskreview.MainActivity.KEY_FIREBASE_KEY;
import static jp.ac.meijou.android.taskreview.MainActivity.KEY_IS_PERSONAL;
import static jp.ac.meijou.android.taskreview.MainActivity.KEY_TODO_ID;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.Optional;
import java.util.function.Function;

import jp.ac.meijou.android.taskreview.databinding.ActivityHistoryBinding;
import jp.ac.meijou.android.taskreview.room.IToDoDao;
import jp.ac.meijou.android.taskreview.room.ToDo;
import jp.ac.meijou.android.taskreview.room.ToDoDatabase;
import jp.ac.meijou.android.taskreview.room.ToDoDiffCallback;
import jp.ac.meijou.android.taskreview.ui.ToDoListAdapter;

public class HistoryActivity extends AppCompatActivity {
    ActivityHistoryBinding binding;
    private static final String THREAD_NAME = "history_activity-db-thread";
    private HandlerThread handlerThread;
    private Handler asyncHandler;
    private ActivityResultLauncher<Intent> registerLauncher;
    private ToDoListAdapter adapter;
    private MainActivity.SortState sortState = MainActivity.SortState.NONE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHistoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initView();
        initMenu();
    }

    private void initView() {
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
                            var list = dao.getVisibilityAll(false);
                            adapter.submitList(list);
                        }));

        // ToDoリストを管理するクラスを初期化する
        initToDoListAdapter(dao);

        // DBアクセス用スレッド内でデータベースからToDoリストを取得する
        asyncHandler.post(() -> {
            var list = dao.getVisibilityAll(false);
            adapter.submitList(list);
        });

        // RecyclerViewの初期化、設定
        binding.toDoView.setAdapter(adapter);
        var layoutManager = new LinearLayoutManager(this);
        binding.toDoView.setLayoutManager(layoutManager);
        var itemDecoration = new DividerItemDecoration(this, layoutManager.getOrientation());
        binding.toDoView.addItemDecoration(itemDecoration);

        binding.sortButton.setOnClickListener(v -> asyncHandler.post(
                () -> {
                    if(sortState == MainActivity.SortState.NONE) {
                        var toast = Toast.makeText(this, "優先度順", Toast.LENGTH_SHORT);
                        toast.show();
                        sortState = MainActivity.SortState.PRIORITY;
                        var list = dao.getPrioritySorted(false);
                        adapter.submitList(list);
                    } else if(sortState == MainActivity.SortState.PRIORITY) {
                        var toast = Toast.makeText(this, "期限順", Toast.LENGTH_SHORT);
                        toast.show();
                        sortState = MainActivity.SortState.DEADLINE;
                        var list = dao.getDeadlineSorted(false);
                        adapter.submitList(list);
                    } else {
                        var toast = Toast.makeText(this, "ID順", Toast.LENGTH_SHORT);
                        toast.show();
                        sortState = MainActivity.SortState.NONE;
                        var list = dao.getVisibilityAll(false);
                        adapter.submitList(list);
                    }
                }));
    }

    private void initMenu() {
        binding.menu.todoButton.setOnClickListener(v -> {
            var intent = new Intent();
            setResult(RESULT_OK, intent);
            finish();
        });
    }
    private void initToDoListAdapter(IToDoDao dao) {
        // ToDoListを管理するクラスを初期化する
        var callback = new ToDoDiffCallback();

        // ToDoリストの表示を非表示にする処理をするRunnableを返す関数インターフェースを定義
        Function<ToDo, Runnable> hideToDo = toDo -> () -> {
            dao.delete(toDo.id);
            var list = dao.getVisibilityAll(false);
            adapter.submitList(list);
        };

        // ToDoリストをクリックした際に画面遷移をするOnClickListenerを返す関数インターフェースを定義
        Function<ToDo, View.OnClickListener> openDetailIntent = toDo -> v -> {
            var intent = new Intent(this, RegisterActivity.class);
            intent.putExtra(KEY_TODO_ID, toDo.id);
            intent.putExtra(KEY_IS_PERSONAL, toDo.isPersonal);
            registerLauncher.launch(intent);
        };

        Function<ToDo, View.OnClickListener> openEvaluateIntent = toDo -> v -> {
            var intent = new Intent(this, EvaluateActivity.class);
            intent.putExtra(KEY_IS_PERSONAL, toDo.firebaseKey.isEmpty() ||
                    toDo.firebaseKey.equals(ToDo.MESSAGE_ERROR));
            intent.putExtra(KEY_FIREBASE_KEY, Optional
                    .ofNullable(toDo.firebaseKey)
                    .filter(s -> !s.isEmpty())
                    .orElse("ERROR"));
            asyncHandler.post(() -> {
                toDo.visible = false;
                dao.update(toDo);
                var list = dao.getVisibilityAll(true);
                adapter.submitList(list);
            });
            registerLauncher.launch(intent);
        };

        // Adapterを初期化する
        adapter = new ToDoListAdapter(callback, hideToDo, openDetailIntent, openEvaluateIntent);
    }
}