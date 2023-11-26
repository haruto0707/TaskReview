package jp.ac.meijou.android.taskreview.firebase;

import jp.ac.meijou.android.taskreview.room.ToDo;

public class FirebaseToDo {
    public final String title;
    public final String subject;
    public final int estimatedTime;
    public final int priority;
    public final String deadline;

    public FirebaseToDo(ToDo toDo) {
        this.title = toDo.title;
        this.subject = toDo.subject;
        this.estimatedTime = toDo.estimatedTime;
        this.priority = toDo.priority;
        this.deadline = toDo.deadline;
    }
}
