package jp.ac.meijou.android.taskreview.room;

import android.os.Handler;
import android.os.HandlerThread;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil.ItemCallback;
import androidx.recyclerview.widget.ListAdapter;

import jp.ac.meijou.android.taskreview.databinding.ViewTodoBinding;

/**
 * RecyclerViewにToDoリストの要素を表示するためのクラス
 * @see androidx.recyclerview.widget.ListAdapter
 */
public class ToDoListAdapter extends ListAdapter<ToDo, ToDoViewHolder> {
    private IToDoDao dao;
    public ToDoListAdapter(@NonNull ItemCallback<ToDo> diffCallback, IToDoDao dao) {
        super(diffCallback);
        this.dao = dao;
    }

    @NonNull
    @Override
    public ToDoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        var inflater = LayoutInflater.from(parent.getContext());
        var binding = ViewTodoBinding.inflate(inflater, parent, false);
        return new ToDoViewHolder(binding, dao, this);
    }

    @Override
    public void onBindViewHolder(@NonNull ToDoViewHolder holder, int position) {
        var item = getCurrentList().get(position);
        holder.onBind(item);
    }
}
