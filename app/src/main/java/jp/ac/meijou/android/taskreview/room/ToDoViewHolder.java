package jp.ac.meijou.android.taskreview.room;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import jp.ac.meijou.android.taskreview.databinding.ViewTodoBinding;

public class ToDoViewHolder extends ViewHolder {
    private ViewTodoBinding binding;

    public ToDoViewHolder(@NonNull ViewTodoBinding binding) {
        this(binding.getRoot());
        this.binding = binding;
    }

    public ToDoViewHolder(@NonNull View itemView) {
        super(itemView);
    }

    public void onBind(ToDo toDo) {
        binding.titleView.setText(toDo.title);
        binding.subjectView.setText(toDo.subject);
        binding.priorityView.setText(toDo.getPriorityString());
        binding.estimatedTimeView.setText(toDo.estimatedTime);
    }
}
