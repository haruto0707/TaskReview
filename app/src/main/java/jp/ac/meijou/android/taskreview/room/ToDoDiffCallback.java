package jp.ac.meijou.android.taskreview.room;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil.ItemCallback;

import java.util.Objects;

public class ToDoDiffCallback extends ItemCallback<ToDo> {
    @Override
    public boolean areItemsTheSame(@NonNull ToDo oldItem, @NonNull ToDo newItem) {
        return oldItem.id == newItem.id;
    }

    @Override
    public boolean areContentsTheSame(@NonNull ToDo oldItem, @NonNull ToDo newItem) {
        return Objects.equals(oldItem.title, newItem.title)
                && Objects.equals(oldItem.subject, newItem.subject)
                && Objects.equals(oldItem.deadline, newItem.deadline)
                && Objects.equals(oldItem.estimatedTime, newItem.estimatedTime)
                && oldItem.priority == newItem.priority
                && Objects.equals(oldItem.detail, newItem.detail)
                && Objects.equals(oldItem.note, newItem.note);
    }
}
