package jp.ac.meijou.android.taskreview.room;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * RoomによるDBのデータを格納するクラスの定義<br>
 * データ操作を行う場合は{@link jp.ac.meijou.android.taskreview.room.IToDoDao}を通じて行う。<br>
 * tableNameはデータベースのテーブル名を指定する。
 */
@Entity(tableName = "todo_list")
public class ToDo {
    /** エラーメッセージ */
    public static final String MESSAGE_ERROR = "ERROR";
    /** IDは主キーで自動生成 */
    @PrimaryKey(autoGenerate = true)
    public int id;
    /** やることリストでTODO画面で上側に表示される文字 */
    @ColumnInfo(name = "title")
    public String title;
    /** TODO画面で表示される科目名 */
    @ColumnInfo(name = "subject")
    public String subject;
    /** TODO画面で表示される推定時間 */
    @ColumnInfo(name = "estimated_time")
    public String estimatedTime;
    /** TODO画面で表示される優先度 */
    @ColumnInfo(name = "priority")
    public int priority;
    /** 期限 */
    @ColumnInfo(name = "deadline")
    public String deadline;
    /** 詳細 */
    @ColumnInfo(name = "detail")
    public String detail;
    /** メモ */
    @ColumnInfo(name = "note")
    public String note;
    /** 表示フラグで{@code false}の場合はTODOリストに表示されない */
    @ColumnInfo(name = "visible")
    public boolean visible;

    /**
     * デフォルトコンストラクタ<br>
     * すべてのフィールドに{@link #MESSAGE_ERROR}を設定する。
     * {@link androidx.room.Room}がデフォルトコンストラクタを必要とするために存在する。
     * 基本使用しない。
     */
    protected ToDo() {
        this.title = MESSAGE_ERROR;
        this.subject = MESSAGE_ERROR;
        this.estimatedTime = MESSAGE_ERROR;
        this.deadline = MESSAGE_ERROR;
        this.priority = -1;
        this.detail = MESSAGE_ERROR;
        this.note = MESSAGE_ERROR;
        this.visible = false;
    }

    /**
     * DBに格納するデータを設定するコンストラクタ<br>
     * @param title やることリストでTODO画面で上側に表示される文字
     * @param subject TODO画面で表示される科目名
     * @param estimatedTime TODO画面で表示される推定時間
     * @param deadline 期限
     * @param priority 優先度
     * @param detail 詳細
     * @param note メモ
     * @param visible 表示フラグで{@code false}の場合はTODOリストに表示されない
     */
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

    /**
     * 優先度を表す列挙型<br>
     * {@link #toInt(Priority)}でint型に変換する。<br>
     * {@link #getPriorityString()}で文字列に変換する。<br>
     * {@link #priority}に格納する。<br>
     * {@link #priority}は{@code 0}が低、{@code 1}が中、{@code 2}が高を表す。<br>
     */
    public enum Priority {
        LOW,
        MEDIUM,
        HIGH
    }

    /**
     * 優先度をint型に変換する<br>
     * {@link #priority}は{@code 0}が低、{@code 1}が中、{@code 2}が高を表す。<br>
     * {@code -1}はエラーを表す。
     * @param priority 優先度
     * @return int型の優先度
     */
    public int toInt(Priority priority) {
        switch (priority) {
            case LOW: return 0;
            case MEDIUM: return 1;
            case HIGH: return 2;
            default: return -1;
        }
    }

    /**
     * int型の優先度を{@link Priority}に変換する<br>
     * @return 優先度
     */
    public Priority getPriority() {
        switch (priority) {
            case 1: return Priority.MEDIUM;
            case 2: return Priority.HIGH;
            default: return Priority.LOW;
        }
    }

    /**
     * 優先度を文字列に変換する<br>
     * @return 優先度を表す文字列
     */
    public String getPriorityString() {
        switch (priority) {
            case 0: return "LOW";
            case 1: return "MEDIUM";
            case 2: return "HIGH";
            default: return "ERROR";
        }
    }
}