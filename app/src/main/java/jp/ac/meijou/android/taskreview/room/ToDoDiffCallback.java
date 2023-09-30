package jp.ac.meijou.android.taskreview.room;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil.ItemCallback;

import java.util.Objects;

import jp.ac.meijou.android.taskreview.ui.ToDoListAdapter;

/**
 * データベースの変更を検知するためのクラス<br>
 * {@link ToDoListAdapter}のコールバックとして使用する。
 */
public class ToDoDiffCallback extends ItemCallback<ToDo> {
    /**
     * ToDoが同じidかどうかを検知するためのメソッド
     */
    @Override
    public boolean areItemsTheSame(@NonNull ToDo oldItem, @NonNull ToDo newItem) {
        return oldItem.id == newItem.id;
    }

    /**
     *  ToDoの内容が一致するかどうかを検知するためのメソッド<br>
     *  DBの構造が変化した場合(ToDoの項目が増えた場合など)は、このメソッドを変更する必要がある。
     */
    @Override
    public boolean areContentsTheSame(@NonNull ToDo oldItem, @NonNull ToDo newItem) {
        return Objects.equals(oldItem.title, newItem.title)
                && Objects.equals(oldItem.subject, newItem.subject)
                && Objects.equals(oldItem.deadline, newItem.deadline)
                && Objects.equals(oldItem.estimatedTime, newItem.estimatedTime)
                && Objects.equals(oldItem.detail, newItem.detail)
                && oldItem.isPersonal == newItem.isPersonal
                && oldItem.visible == newItem.visible
                && oldItem.priority == newItem.priority;
    }
}
