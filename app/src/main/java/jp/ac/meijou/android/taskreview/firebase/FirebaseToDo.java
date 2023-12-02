package jp.ac.meijou.android.taskreview.firebase;

import jp.ac.meijou.android.taskreview.room.ToDo;

public class FirebaseToDo {
    public String title;
    public String subject;
    public int estimatedTime;
    public double priority;
    public String deadline;

    public FirebaseToDo(ToDo toDo) {
        this.title = toDo.title;
        this.subject = toDo.subject;
        this.estimatedTime = toDo.estimatedTime;
        this.priority = toDo.priority;
        this.deadline = toDo.deadline;
    }

    public FirebaseToDo() {
    }
}
