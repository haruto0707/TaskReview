package jp.ac.meijou.android.taskreview;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;
import android.util.Log;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import jp.ac.meijou.android.taskreview.databinding.ActivityEvaluateBinding;
import jp.ac.meijou.android.taskreview.firebase.FirebaseManager;
import jp.ac.meijou.android.taskreview.firebase.ToDoEvaluation;
import jp.ac.meijou.android.taskreview.room.ToDo;
import jp.ac.meijou.android.taskreview.room.ToDoDatabase;

public class EvaluateActivity extends AppCompatActivity {
    private ActivityEvaluateBinding binding;
    private boolean isPersonal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEvaluateBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initView();
    }

    private void initView() {
        isPersonal = getIntent().getBooleanExtra(MainActivity.KEY_IS_PERSONAL, true);
        binding.toDoFinishButton.setChecked(true);
        binding.evaluateFinishButton.setOnClickListener(v -> {
            var id = getIntent().getIntExtra(MainActivity.KEY_TODO_ID, -1);
            if(!isPersonal) {
                var firebaseKey = getIntent().getStringExtra(MainActivity.KEY_FIREBASE_KEY);
                var isFinished = binding.toDoFinishButton.isChecked();
                var evaluation = (int) binding.evaluationBar.getRating();
                // 送信系の処理は以下に記述
                CompletableFuture<Void> future = FirebaseManager.sendTo(
                        new ToDoEvaluation(firebaseKey, isFinished, evaluation));
                future.thenRunAsync(() -> {
                    var db = ToDoDatabase.getInstance(this);
                    var dao = db.toDoDao();
                    dao.delete(id);
                }).thenRunAsync(() -> {
                        var intent = new Intent();
                        setResult(RESULT_OK, intent);
                        finish();
                });
            } else {
                CompletableFuture.runAsync(() -> {
                    var db = ToDoDatabase.getInstance(this);
                    var dao = db.toDoDao();
                    dao.delete(id);
                }).thenRunAsync(() -> {
                    var intent = new Intent();
                    setResult(RESULT_OK, intent);
                    finish();
                });
            }
        });
        binding.revertButton.setOnClickListener(v -> {
            var intent = new Intent();
            setResult(RESULT_OK, intent);
            finish();
        });
    }
}