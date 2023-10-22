package jp.ac.meijou.android.taskreview;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;

import java.util.Optional;

import jp.ac.meijou.android.taskreview.databinding.ActivityEvaluateBinding;
import jp.ac.meijou.android.taskreview.room.ToDo;
import jp.ac.meijou.android.taskreview.room.ToDoDatabase;

public class EvaluateActivity extends AppCompatActivity {
    private ActivityEvaluateBinding binding;
    private int id;
    private boolean isPersonal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEvaluateBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initView();
    }

    private void initView() {
        id = getIntent().getIntExtra(MainActivity.KEY_TODO_ID, -1);
        isPersonal =getIntent().getBooleanExtra(MainActivity.KEY_IS_PERSONAL, true);
        binding.toDoFinishButton.setChecked(true);
        binding.evaluateFinishButton.setOnClickListener(v -> {
            if(!isPersonal) {
                var isFinished = binding.toDoFinishButton.isChecked();
                var evaluation = binding.evaluationBar.getRating();
                var comment = binding.commentText.getText().toString();
                // 送信系の処理は以下に記述
            }
            var intent = new Intent();
            setResult(RESULT_OK, intent);
            finish();
        });
    }
}