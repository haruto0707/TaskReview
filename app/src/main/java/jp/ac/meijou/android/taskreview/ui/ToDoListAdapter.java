package jp.ac.meijou.android.taskreview.ui;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil.ItemCallback;
import androidx.recyclerview.widget.ListAdapter;

import java.util.Optional;
import java.util.function.Function;

import jp.ac.meijou.android.taskreview.databinding.ViewTodoBinding;
import jp.ac.meijou.android.taskreview.room.ToDo;
import jp.ac.meijou.android.taskreview.ui.ToDoViewHolder;

/**
 * RecyclerViewにToDoリストの要素を表示するためのクラス
 * @see androidx.recyclerview.widget.ListAdapter
 */
public class ToDoListAdapter extends ListAdapter<ToDo, ToDoViewHolder> {
    /** 生成したToDoリストの要素をRecyclerViewに反映するためのクラス */
    private ToDoViewHolder viewHolder;
    /** DBのデータを更新し、RecyclerViewに変更を反映するためのRunnableを生成する関数インターフェース */
    private Function<ToDo, Runnable> hideToDo;

    private Function<ToDo, View.OnClickListener> openDetailIntent;
    private Function<ToDo, View.OnClickListener> openEvaluateIntent;
    private boolean hideButton = false;

    public ToDoListAdapter(@NonNull ItemCallback<ToDo> diffCallback,
                           Function<ToDo, Runnable> hideToDo,
                           Function<ToDo, View.OnClickListener> openDetailIntent,
                           Function<ToDo, View.OnClickListener> openEvaluateIntent) {
        super(diffCallback);
        this.hideToDo = hideToDo;
        this.openDetailIntent = openDetailIntent;
        this.openEvaluateIntent = openEvaluateIntent;
    }

    public ToDoListAdapter(@NonNull ItemCallback<ToDo> diffCallback, Function<ToDo, View.OnClickListener> openDetailIntent) {
        super(diffCallback);
        this.openDetailIntent = openDetailIntent;
        this.hideButton = true;
    }

    /**
     * MainActivity終了時にスレッドを停止させるメソッド
     */
    public void finishThread() {
        Optional.ofNullable(viewHolder)
                .ifPresent(ToDoViewHolder::finishThread);
    }

    /**
     * RecyclerViewに表示するためのクラスを生成するメソッド
     * スレッドを終了させるために生成したクラスを保持。<br><br>
     * {@inheritDoc}
     */
    @NonNull
    @Override
    public ToDoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        var inflater = LayoutInflater.from(parent.getContext());
        var binding = ViewTodoBinding.inflate(inflater, parent, false);
        viewHolder = new ToDoViewHolder(binding, hideToDo, openDetailIntent, openEvaluateIntent, hideButton);
        return viewHolder;
    }

    /**
     * RecyclerViewに表示する要素のデータをバインディングするメソッド<br><br>
     * {@inheritDoc}
     */
    @Override
    public void onBindViewHolder(@NonNull ToDoViewHolder holder, int position) {
        var item = getCurrentList().get(position);
        holder.onBind(item);
    }
}
