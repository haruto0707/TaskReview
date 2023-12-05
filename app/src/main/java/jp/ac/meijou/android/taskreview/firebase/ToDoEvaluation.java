package jp.ac.meijou.android.taskreview.firebase;

public class ToDoEvaluation {
    public final String firebaseKey;
    public final boolean isFinished;
    public final int evaluation;
    public ToDoEvaluation(String firebaseKey, boolean isFinished, int evaluation) {
        this.firebaseKey = firebaseKey;
        this.isFinished = isFinished;
        this.evaluation = evaluation;
    }
}