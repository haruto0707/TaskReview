package jp.ac.meijou.android.taskreview.room;

import android.os.Handler;
import android.os.Process;
import android.os.HandlerThread;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import java.util.stream.Collectors;

import jp.ac.meijou.android.taskreview.databinding.ViewTodoBinding;

/**
 * DB内のデータをtoDoクラスに格納し、それを表示するViewToDoBindingに格納するクラス
 *
 */
public class ToDoViewHolder extends ViewHolder {
    /** ToDoリストの要素のバインディングクラス */
    private ViewTodoBinding binding;
    private IToDoDao dao;
    private ToDoListAdapter adapter;

    private HandlerThread handlerThread;
    private Handler asyncHandler;

    /**
     * ToDoリストの要素を生成する時のコンストラクタ
     * @param binding ToDoリストの要素のバインディングクラス
     */
    public ToDoViewHolder(@NonNull ViewTodoBinding binding, IToDoDao dao, ToDoListAdapter adapter) {
        this(binding.getRoot());
        this.binding = binding;
        this.dao = dao;
        this.adapter = adapter;
        handlerThread = new HandlerThread("db-thread", Process.THREAD_PRIORITY_DEFAULT);
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
     * 実際に表示する内容のみを取得し格納する。<br>
     * title : やること<br>
     * subject : 科目<br>
     * priority : 優先度<br>
     * estimatedTime:予想時間
     * @param toDo DB内のデータを格納したtoDoクラス
     */
    public void onBind(ToDo toDo) {
        binding.titleView.setText(toDo.title);
        binding.subjectView.setText(toDo.subject);
        binding.priorityView.setText(toDo.getPriorityString());
        binding.estimatedTimeView.setText(toDo.estimatedTime);
        binding.getRoot().setOnClickListener(v -> {
            asyncHandler.post(() -> {
                toDo.visible = false;
                dao.update(toDo);
                var list = dao.getAll()
                        .stream()
                        .filter(t -> t.visible)
                        .collect(Collectors.toList());
                adapter.submitList(list);
            });
        });

    }

    public void finishThread() {
        handlerThread.quit();
    }
}
