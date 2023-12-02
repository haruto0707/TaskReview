package jp.ac.meijou.android.taskreview;

import static jp.ac.meijou.android.taskreview.room.ToDo.MESSAGE_ERROR;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Process;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View.*;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.android.gms.common.api.Api;

import java.util.Optional;
import java.util.function.Function;

import jp.ac.meijou.android.taskreview.databinding.ActivityMainBinding;
import jp.ac.meijou.android.taskreview.firebase.FirebaseManager;
import jp.ac.meijou.android.taskreview.room.IToDoDao;
import jp.ac.meijou.android.taskreview.room.ToDo;
import jp.ac.meijou.android.taskreview.room.ToDoDatabase;
import jp.ac.meijou.android.taskreview.room.ToDoDiffCallback;
import jp.ac.meijou.android.taskreview.ui.ToDoListAdapter;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;

    public static final String KEY_IS_PERSONAL = "todo_is_personal";
    public static final String KEY_TODO_ID = "todo_id";
    public static final String KEY_FIREBASE_KEY = "todo_firebase_key";
    /** 呼び出すスレッド名 */
    private static final String THREAD_NAME = "main_activity-db-thread";
    /** DBアクセス用のスレッド */
    private HandlerThread handlerThread;
    /** DBアクセス用のスレッドのハンドラ */
    private Handler asyncHandler;
    /** ToDoリストを管理するクラス */
    private ToDoListAdapter adapter;
    /** 画面遷移用のクラス */
    private static ActivityResultLauncher<Intent> registerLauncher;
    private SortState sortState = SortState.NONE;

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

        /*
        ItemTouchHelper.SimpleCallback callback = new ItemTouchHelper.SimpleCallback(
                0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                if(actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                    var itemView = viewHolder.itemView;
                    var binding = ViewTodoSwipeBinding.inflate(getLayoutInflater());
                    var swipeThreshold = 0.5f;
                    if(Math.abs(dX) > swipeThreshold * itemView.getWidth()) {
                        dX = swipeThreshold * itemView.getWidth() * Math.signum(dX / 3);
                        isCurrentlyActive = false;
                    }
                    super.onChildDraw(c, recyclerView, viewHolder, dX / 3, dY, actionState, isCurrentlyActive);
                }
            }
        };
        ItemTouchHelper helper = new ItemTouchHelper(callback);
        helper.attachToRecyclerView(recyclerView);
        */

        // RecyclerViewの初期化、設定
        recyclerView.setAdapter(adapter);
        var layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        var itemDecoration = new DividerItemDecoration(this, layoutManager.getOrientation());
        recyclerView.addItemDecoration(itemDecoration);

        binding.sortButton.setOnClickListener(v -> asyncHandler.post(
                () -> {
                    if(sortState == SortState.NONE) {
                        var toast = Toast.makeText(this, "優先度順", Toast.LENGTH_SHORT);
                        toast.show();
                        sortState = SortState.PRIORITY;
                        var list = dao.getPrioritySorted(true);
                        adapter.submitList(list);
                    } else if(sortState == SortState.PRIORITY) {
                        var toast = Toast.makeText(this, "期限順", Toast.LENGTH_SHORT);
                        toast.show();
                        sortState = SortState.DEADLINE;
                        var list = dao.getDeadlineSorted(true);
                        adapter.submitList(list);
                    } else {
                        var toast = Toast.makeText(this, "ID順", Toast.LENGTH_SHORT);
                        toast.show();
                        sortState = SortState.NONE;
                        var list = dao.getVisibilityAll(true);
                        adapter.submitList(list);
                    }
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
            intent.putExtra(KEY_IS_PERSONAL, toDo.isPersonal);
            if(ToDo.checkIsValidString(toDo.firebaseKey)) {
                intent.putExtra(KEY_FIREBASE_KEY, toDo.firebaseKey);
            }
            registerLauncher.launch(intent);
        };

        Function<ToDo, OnClickListener> openEvaluateIntent = toDo -> v -> {
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

    /**
     * 画面下部メニューの初期化を行うメソッド
     */
    private void initMenu() {
        binding.registerButton.setOnClickListener(v -> {
            var intent = new Intent(this, RegisterActivity.class);
            registerLauncher.launch(intent);
        });
        binding.menu.historyButton.setOnClickListener(v -> {
            var intent = new Intent(this, HistoryActivity.class);
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

    public enum SortState {
        NONE,
        PRIORITY,
        DEADLINE
    }
}