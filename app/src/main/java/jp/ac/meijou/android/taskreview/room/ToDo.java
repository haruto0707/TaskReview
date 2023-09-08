package jp.ac.meijou.android.taskreview.room;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "todo_list")
public class ToDo {
    public static final String MESSAGE_ERROR = "ERROR";
    @PrimaryKey(autoGenerate = true)
    public int id;
    @ColumnInfo(name = "title")
    public String title;
    @ColumnInfo(name = "subject")
    public String subject;
    @ColumnInfo(name = "estimated_time")
    public String estimatedTime;
    @ColumnInfo(name = "deadline")
    public String deadline;
    @ColumnInfo(name = "priority")
    public int priority;
    @ColumnInfo(name = "detail")
    public String detail;
    @ColumnInfo(name = "note")
    public String note;
    @ColumnInfo(name = "visible")
    public boolean visible;

    public ToDo() {
        this.title = MESSAGE_ERROR;
        this.subject = MESSAGE_ERROR;
        this.estimatedTime = MESSAGE_ERROR;
        this.deadline = MESSAGE_ERROR;
        this.priority = -1;
        this.detail = MESSAGE_ERROR;
        this.note = MESSAGE_ERROR;
        this.visible = false;
    }
    public ToDo(String title, String subject, String estimatedTime, String deadline, Priority priority, String detail, String note, boolean visible) {
        this.title = title;
        this.subject = subject;
        this.estimatedTime = estimatedTime;
        this.deadline = deadline;
        this.priority = toInt(priority);
        this.detail = detail;
        this.note = note;
        this.visible = visible;
    }

    public enum Priority {
        LOW,
        MEDIUM,
        HIGH
    }
    private int toInt(Priority priority) {
        switch (priority) {
            case LOW: return 0;
            case MEDIUM: return 1;
            case HIGH: return 2;
            default: return -1;
        }
    }
    public String getPriorityString() {
        switch (priority) {
            case 0: return "LOW";
            case 1: return "MEDIUM";
            case 2: return "HIGH";
            default: return "ERROR";
        }
    }
}