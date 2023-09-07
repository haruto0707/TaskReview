package jp.ac.meijou.android.taskreview.room;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil.ItemCallback;
import androidx.recyclerview.widget.ListAdapter;

import jp.ac.meijou.android.taskreview.databinding.ViewTodoBinding;


public class ToDoListAdapter extends ListAdapter<ToDo, ToDoViewHolder> {
    public ToDoListAdapter(@NonNull ItemCallback<ToDo> diffCallback) {
        super(diffCallback);
    }

    @NonNull
    @Override
    public ToDoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        var inflater = LayoutInflater.from(parent.getContext());
        var binding = ViewTodoBinding.inflate(inflater, parent, false);
        return new ToDoViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ToDoViewHolder holder, int position) {
        var item = getCurrentList().get(position);
        holder.onBind(item);
    }
}
