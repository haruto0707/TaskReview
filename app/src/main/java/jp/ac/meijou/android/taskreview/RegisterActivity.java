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

public class RegisterActivity extends AppCompatActivity {
    private ActivityRegisterBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initRegisterButton();
    }

    /**
     * ボタンの動作を実装するメソッド<br>
     * {@code registerButton} ボタンを押した際に、データベースにデータを登録する<br>
     * {@code todoButton} メニュー画面に戻るボタンを押した際に、メニュー画面に戻る<br>
     */
    private void initRegisterButton() {
        // DBアクセス用のDAOを初期化する、データベース内のデータを取得
        var db = ToDoDatabase.getInstance(this);
        var dao = db.toDoDao();

        // 登録ボタン
        binding.registerButton.setOnClickListener(v -> {
            // DBアクセス用のスレッドを初期化、開始する
            var handlerThread = new HandlerThread("db-register-thread", Process.THREAD_PRIORITY_DEFAULT);
            handlerThread.start();
            var asyncHandler = new Handler(handlerThread.getLooper());

            // データベースに登録するデータを作成する
            var content = binding.toDoEditText.getText().toString();
            var subject = binding.subjectEditText.getText().toString();
            var estimatedTime = binding.editTextNumber.getText().toString();
            var priority = getPriority();

            // ToDoリストのデータを作成する
            var toDo = new ToDo(content, subject, estimatedTime, "2021-07-01", priority, "detail", "note", true);

            // DBアクセス用スレッド内でデータベースにデータを挿入する
            asyncHandler.post(() -> {
                dao.insert(toDo);
                // DBアクセス用スレッドを終了する
                handlerThread.quit();
            });
        });

        // メニュー画面に戻るボタン
        binding.menu.todoButton.setOnClickListener(v -> {
            var intent = new Intent();
            // メニュー画面に戻るボタンを押した際に、resultLauncherのメソッドを呼び出す
            setResult(RESULT_OK, intent);
            finish();
        });
    }

    /**
     * ラジオボタンの選択状態から、優先度を取得するメソッド
     */
    private ToDo.Priority getPriority() {
        if(binding.radioLow.isChecked()) {
            return ToDo.Priority.LOW;
        } else if(binding.radioMiddle.isChecked()) {
            return ToDo.Priority.MEDIUM;
        } else if(binding.radioHigh.isChecked()) {
            return ToDo.Priority.HIGH;
        }
        return ToDo.Priority.LOW;
    }
}