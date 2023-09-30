package jp.ac.meijou.android.taskreview.ui;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Handler;
import android.os.Process;
import android.os.HandlerThread;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.*;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import java.util.function.Function;

import jp.ac.meijou.android.taskreview.R;
import jp.ac.meijou.android.taskreview.databinding.ViewTodoBinding;
import jp.ac.meijou.android.taskreview.room.ToDo;

/**
 * DB内のデータをtoDoクラスに格納し、それを表示するViewToDoBindingに格納するクラス
 *
 */
public class ToDoViewHolder extends ViewHolder {
    /** 変更内容をDBに反映するスレッド名 */
    private static final String THREAD_NAME = "todo_view_holder-update-thread";
    private static final int MIN_SWIPE_DISTANCE = 5;
    /** ToDoリストの要素のバインディングクラス */
    private ViewTodoBinding binding;
    /** 変更内容をDBに反映するスレッド */
    private HandlerThread handlerThread;
    /** 変更内容をDBに反映するスレッドのハンドラ */
    private Handler asyncHandler;
    /** ToDOリストからToDoを削除するRunnableを生成する関数インターフェース */
    private Function<ToDo, Runnable> hideToDo;
    /** ToDoの詳細画面を開くためのIntentを生成する関数インターフェース */
    private Function<ToDo, OnClickListener> openDetailIntent;

    /**
     * ToDoリストの要素を生成する時のコンストラクタ<br>
     * 引数を反映させ、スレッドの初期化、開始を行う。
     * @param binding ToDoリストの要素のバインディングクラス
     * @param hideToDo ToDoの変更をDB上に変更するRunnableを生成する関数インターフェース
     */
    public ToDoViewHolder(@NonNull ViewTodoBinding binding, Function<ToDo, Runnable> hideToDo, Function<ToDo, View.OnClickListener> openDetailIntent) {
        this(binding.getRoot());
        this.binding = binding;
        this.hideToDo = hideToDo;
        this.openDetailIntent = openDetailIntent;
        handlerThread = new HandlerThread(THREAD_NAME, Process.THREAD_PRIORITY_DEFAULT);
        handlerThread.start();
        asyncHandler = new Handler(handlerThread.getLooper());
    }

    /**
     * 親クラスのコンストラクタを呼び出す。ToDoリストの要素を生成する時には使用しない。<br>
     * {@link androidx.recyclerview.widget.RecyclerView.ViewHolder#ViewHolder(View itemView)}
     */
    protected ToDoViewHolder(@NonNull View itemView) {
        super(itemView);
    }

    /**
     * ToDoリストの要素のバインディングクラスにデータを格納するメソッド<br>
     * 実際にRecyclerView内に表示する内容のみを取得し格納する。<br>
     * title : やること<br>
     * subject : 科目<br>
     * priority : 優先度<br>
     * estimatedTime:予想時間
     * @param toDo DB内のデータを格納したtoDoクラス
     */
    @SuppressLint({"SetTextI18n", "ClickableViewAccessibility"})
    public void onBind(ToDo toDo) {
        var gesture = new GestureDetector(binding.getRoot().getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onFling(@NonNull MotionEvent beforeEvent, @NonNull MotionEvent afterEvent, float velocityX, float velocityY) {
                if(beforeEvent.getX() - afterEvent.getX() > MIN_SWIPE_DISTANCE) {

                }
                return false;
            }
        });
        binding.getRoot().setOnTouchListener((v, event) -> gesture.onTouchEvent(event));
        binding.titleView.setText(toDo.title);
        binding.subjectView.setText(toDo.subject);
        binding.priorityView.setText(toDo.getPriorityString());
        binding.estimatedTimeView.setText(toDo.getStringTime(ToDo.TimeFormat.LABELED));
        binding.deadlineView.setText("期限 : " + toDo.deadline);
        binding.deleteButton
                .setOnClickListener(v -> asyncHandler.post(hideToDo.apply(toDo)));
        binding.getRoot()
                .setOnClickListener(openDetailIntent.apply(toDo));
    }
    /**
     * スレッドを終了するメソッド<br>
     */
    public void finishThread() {
        handlerThread.quit();
    }
}
