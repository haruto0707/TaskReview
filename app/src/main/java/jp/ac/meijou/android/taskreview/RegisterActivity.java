package jp.ac.meijou.android.taskreview;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;

import jp.ac.meijou.android.taskreview.databinding.ActivityRegisterBinding;
import jp.ac.meijou.android.taskreview.room.ToDo;
import jp.ac.meijou.android.taskreview.room.ToDoDatabase;
import jp.ac.meijou.android.taskreview.room.ToDoDiffCallback;
import jp.ac.meijou.android.taskreview.room.ToDoListAdapter;

public class RegisterActivity extends AppCompatActivity {
    private ActivityRegisterBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }

    private void initRegisterButton() {
        // DBアクセス用のDAOを初期化する、データベース内のデータを取得
        var db = ToDoDatabase.getInstance(this);
        var dao = db.toDoDao();

        // ToDoListを管理するクラスを初期化する
        var callback = new ToDoDiffCallback();
        var adapter = new ToDoListAdapter(callback, dao);

        binding.button3.setOnClickListener(v -> {
            var handlerThread = new HandlerThread("db-register-thread", Process.THREAD_PRIORITY_DEFAULT);
            handlerThread.start();
            var asyncHandler = new Handler(handlerThread.getLooper());
            var content = binding.toDoEditText.getText().toString();
            var subject = binding.subjectEditText.getText().toString();
            var estimatedTime = binding.editTextNumber.getText().toString();
            var toDo = new ToDo(content, subject, estimatedTime, "2021-07-01", ToDo.Priority.LOW, "detail", "note", true);
            // DBアクセス用スレッド内でデータベースからToDoリストを取得する
            asyncHandler.post(() -> {
                dao.insertAll(toDo);
                handlerThread.quit();
            });
        });
        binding.menu.todoButton.setOnClickListener(v -> {
            var intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        });
    }
}