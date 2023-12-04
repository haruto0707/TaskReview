package jp.ac.meijou.android.taskreview.room;

import android.annotation.SuppressLint;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.Objects;


/**
 * RoomによるDBのデータを格納するクラスの定義<br>
 * データ操作を行う場合は{@link jp.ac.meijou.android.taskreview.room.IToDoDao}を通じて行う。<br>
 * tableNameはデータベースのテーブル名を指定する。
 */
@Entity(tableName = "todo_list", indices = {@Index(value = {"id", "is_personal"}, unique = true)})
public class ToDo {
    /** エラーメッセージ */
    public static final String MESSAGE_ERROR = "ERROR";
    public static final String TIME_FORMAT_DEFAULT = "%02d:%02d";
    public static final String TIME_FORMAT_LABELED = "%02d時間%02d分";
    public static final String DATE_FORMAT = "%d-%02d-%02d";

    /** IDは主キーで自動生成 */
    @PrimaryKey(autoGenerate = true)
    public int id;
    /** 個人で設定したTODOであるかどうか */
    @ColumnInfo(name = "is_personal")
    public boolean isPersonal;
    /** やることリストでTODO画面で上側に表示される文字 */
    @ColumnInfo(name = "title")
    public String title;
    /** TODO画面で表示される科目名 */
    @ColumnInfo(name = "subject")
    public String subject;
    /** TODO画面で表示される推定時間 */
    @ColumnInfo(name = "estimated_time")
    public int estimatedTime;
    /** TODO画面で表示される優先度 */
    @ColumnInfo(name = "priority")
    public double priority;
    /** 期限 */
    @ColumnInfo(name = "deadline")
    public String deadline;
    /** 詳細 */
    @ColumnInfo(name = "detail")
    public String detail;
    /** 表示フラグで{@code false}の場合はTODOリストに表示されない */
    @ColumnInfo(name = "visible")
    public boolean visible;

    @ColumnInfo(name = "firebase_key")
    public String firebaseKey;

    /**
     * デフォルトコンストラクタ<br>
     * すべてのフィールドに{@link #MESSAGE_ERROR}を設定する。
     * {@link androidx.room.Room}がデフォルトコンストラクタを必要とするために存在する。
     * 基本使用しない。
     */
    protected ToDo() {
        this.title = MESSAGE_ERROR;
        this.subject = MESSAGE_ERROR;
        this.estimatedTime = -1;
        this.deadline = MESSAGE_ERROR;
        this.priority = -1;
        this.detail = MESSAGE_ERROR;
        this.isPersonal = true;
        this.visible = false;
        this.firebaseKey = MESSAGE_ERROR;
    }

    /**
     * DBに格納するデータを設定するコンストラクタ<br>
     * @param title やることリストでTODO画面で上側に表示される文字
     * @param subject TODO画面で表示される科目名
     * @param estimatedTime TODO画面で表示される推定時間
     * @param deadline 期限
     * @param priority 優先度
     * @param detail 詳細
     * @param visible 表示フラグで{@code false}の場合はTODOリストに表示されない
     */
    @Ignore
    public ToDo(boolean isPersonal, String title, String subject, int estimatedTime, String deadline, double priority, String detail, boolean visible) {
        this.isPersonal = isPersonal;
        this.title = title;
        this.subject = subject;
        this.estimatedTime = estimatedTime;
        this.deadline = deadline;
        this.priority = priority;
        this.detail = detail;
        this.visible = visible;
        this.firebaseKey = MESSAGE_ERROR;
    }
    @Ignore
    public ToDo(String firebaseKey, String title, String subject, int estimatedTime, String deadline, double priority, String detail, boolean visible) {
        this.isPersonal = false;
        this.firebaseKey = firebaseKey;
        this.title = title;
        this.subject = subject;
        this.estimatedTime = estimatedTime;
        this.deadline = deadline;
        this.priority = priority;
        this.detail = detail;
        this.visible = visible;
    }

    @Ignore
    public ToDo(int id, String title, String subject, int estimatedTime, String deadline, double priority, String firebaseKey, boolean visible) {
        this.id = id;
        this.isPersonal = false;
        this.title = title;
        this.subject = subject;
        this.estimatedTime = estimatedTime;
        this.deadline = deadline;
        this.priority = priority;
        this.visible = visible;
        this.firebaseKey = firebaseKey;
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
     * フォーマットの種類
     */
    public enum TimeFormat {
        DEFAULT,
        LABELED
    }

    /**
     * 優先度をint型に変換する<br>
     * {@link #priority}は{@code 0}が低、{@code 1}が中、{@code 2}が高を表す。<br>
     * {@code -1}はエラーを表す。
     * @param priority 優先度
     * @return int型の優先度
     */


    /**
     * int型の優先度を{@link Priority}に変換する<br>
     * @return 優先度
     */
    public Priority getPriority() {
        if(2.0 <= priority) return Priority.HIGH;
        if(1.0 <= priority) return Priority.MEDIUM;
        return Priority.LOW;
    }

    /**
     * 優先度を文字列に変換する<br>
     * @return 優先度を表す文字列
     */
    public String getPriorityString() {
        switch (getPriority()) {
            case LOW: return "低";
            case MEDIUM: return "中";
            case HIGH: return "高";
            default: return MESSAGE_ERROR;
        }
    }

    /**
     * 推定時間を文字列に変換する<br>
     * @return 推定時間を表す文字列
     */
    @SuppressLint("DefaultLocale")
    public String getStringTime(TimeFormat format) {
        var hour = estimatedTime / 60;
        var minute = estimatedTime % 60;
        if (Objects.requireNonNull(format) == TimeFormat.LABELED) {
            return String.format(TIME_FORMAT_LABELED, hour, minute);
        }
        return String.format(TIME_FORMAT_DEFAULT, hour, minute);
    }

    /**
     * 文字列を推定時間に変換する<br>
     * @param time 推定時間を表す文字列
     * @return 推定時間
     */
    public static int timeToInt(String time) {
        if(time.isEmpty()) return -1; // 空文字の場合は-1を返す
        var dateTime = time.split(":");
        switch(dateTime.length) {
            case 1: return Integer.parseInt(dateTime[0]);
            case 2: return Integer.parseInt(dateTime[0]) * 60 + Integer.parseInt(dateTime[1]);
            default: return -1;  // エラーの場合は-1を返す
        }
    }

    public static Priority parsePriority(double priority) {
        if(2.0 <= priority) return Priority.HIGH;
        if(1.0 <= priority) return Priority.MEDIUM;
        return Priority.LOW;
    }

    public static boolean checkIsValidString(String str) {
        return str != null && !str.isEmpty() && !str.equals(MESSAGE_ERROR);
    }
}