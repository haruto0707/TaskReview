package jp.ac.meijou.android.taskreview.firebase;

import static jp.ac.meijou.android.taskreview.room.ToDo.TIME_FORMAT_DEFAULT;
import static jp.ac.meijou.android.taskreview.room.ToDo.TIME_FORMAT_LABELED;

import android.annotation.SuppressLint;

import java.util.Objects;

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

    public FirebaseToDo() {}

    @SuppressLint("DefaultLocale")
    public String getStringTime(ToDo.TimeFormat format) {
        var hour = estimatedTime / 60;
        var minute = estimatedTime % 60;
        if (Objects.requireNonNull(format) == ToDo.TimeFormat.LABELED) {
            return String.format(TIME_FORMAT_LABELED, hour, minute);
        }
        return String.format(TIME_FORMAT_DEFAULT, hour, minute);
    }
}
