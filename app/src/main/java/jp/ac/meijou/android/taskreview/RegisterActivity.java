package jp.ac.meijou.android.taskreview;


import static jp.ac.meijou.android.taskreview.room.ToDo.MESSAGE_ERROR;
import static jp.ac.meijou.android.taskreview.room.ToDo.checkIsValidString;
import static jp.ac.meijou.android.taskreview.room.ToDo.timeToInt;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import jp.ac.meijou.android.taskreview.databinding.ActivityRegisterBinding;
import jp.ac.meijou.android.taskreview.firebase.FirebaseManager;
import jp.ac.meijou.android.taskreview.firebase.FirebaseToDo;
import jp.ac.meijou.android.taskreview.room.ToDo;
import jp.ac.meijou.android.taskreview.room.ToDo.Priority;
import jp.ac.meijou.android.taskreview.room.ToDoDatabase;
import jp.ac.meijou.android.taskreview.ui.DatePickerFragment;
import jp.ac.meijou.android.taskreview.ui.TimePickerFragment;

public class RegisterActivity extends AppCompatActivity {
    private ActivityRegisterBinding binding;
    /**
     * DBにアクセスするスレッドの名
     */
    private static final String THREAD_NAME = "register_activity-db-thread";
    /**
     * ToDoリストのID
     */
    private int id;
    private boolean isPersonal;
    private String firebaseKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initView();
        initRegisterButton();
        initRevertButton();
        initImportButton();
        Log.d("sakamaki", String.valueOf(firebaseKey));
    }
    /**
     * ToDoリストのデータからTextViewに文字列をセットするメソッド
     */
    @SuppressLint("ClickableViewAccessibility")
    private void initView() {
        initViewText();

        binding.scrollView.setOnTouchListener((v, event) -> {
            hideKeyboard();
            binding.scrollView.requestFocus();
            return false;
        });

        binding.deadlineEditNumber.setOnClickListener(v -> {
            var datePicker = new DatePickerFragment(binding.deadlineEditNumber);
            datePicker.show(getSupportFragmentManager(), "datePicker");
        });

        binding.editTextNumber.setOnClickListener(v -> {
            var timePicker = new TimePickerFragment(binding.editTextNumber);
            timePicker.show(getSupportFragmentManager(), "timePicker");
        });
    }

    /**
     * ToDoリストのデータからTextViewに文字列をセットするメソッド
     */
    private void initViewText() {
        id = getIntent().getIntExtra(MainActivity.KEY_TODO_ID, -1);
        firebaseKey = getIntent().getStringExtra(MainActivity.KEY_FIREBASE_KEY);
        isPersonal = getIntent().getBooleanExtra(MainActivity.KEY_IS_PERSONAL, true);
        if(id != -1) {
            // DBアクセス用のDAOを初期化する、データベース内のデータを取得
            var db = ToDoDatabase.getInstance(this);
            var dao = db.toDoDao();
            // DBアクセス用のスレッドを初期化、開始する
            var handlerThread = new HandlerThread(THREAD_NAME, Process.THREAD_PRIORITY_DEFAULT);
            handlerThread.start();
            var asyncHandler = new Handler(handlerThread.getLooper());

            //  ToDoをDBからデータを取得し、画面に表示する
            asyncHandler.post(() -> {
                Optional.ofNullable(dao.get(id, isPersonal))
                        .ifPresent(toDo -> {
                            binding.toDoEditText.setText(toDo.title);
                            binding.subjectEditText.setText(toDo.subject);
                            binding.editTextNumber.setText(toDo.getStringTime(ToDo.TimeFormat.DEFAULT));
                            binding.deadlineEditNumber.setText(toDo.deadline);
                            binding.detailEditText.setText(toDo.detail);
                            if(toDo.getPriority() == Priority.LOW) {
                                binding.radioLow.setChecked(true);
                            } else if(toDo.getPriority() == Priority.MEDIUM) {
                                binding.radioMiddle.setChecked(true);
                            } else if(toDo.getPriority() == Priority.HIGH) {
                                binding.radioHigh.setChecked(true);
                            }
                        });
                handlerThread.quit();
            });
        } else {
            if(firebaseKey == null) return;
            var handlerThread = new HandlerThread(THREAD_NAME, Process.THREAD_PRIORITY_DEFAULT);
            handlerThread.start();
            var asyncHandler = new Handler(handlerThread.getLooper());
            asyncHandler.post(() -> {
                var future = FirebaseManager.getFromKey(firebaseKey);
                try {
                    var firebaseToDo = future.get();
                    if(firebaseToDo != null) {
                        runOnUiThread(() -> {
                            binding.toDoEditText.setText(firebaseToDo.title);
                            binding.subjectEditText.setText(firebaseToDo.subject);
                            binding.editTextNumber.setText(String.valueOf(firebaseToDo.getStringTime(ToDo.TimeFormat.DEFAULT)));
                            binding.deadlineEditNumber.setText(firebaseToDo.deadline);
                            if(firebaseToDo.priority == 0) {
                                binding.radioLow.setChecked(true);
                            } else if(firebaseToDo.priority == 1) {
                                binding.radioMiddle.setChecked(true);
                            } else if(firebaseToDo.priority == 2) {
                                binding.radioHigh.setChecked(true);
                            }
                        });
                    }
                } catch (ExecutionException | InterruptedException ignored) {}
                handlerThread.quitSafely();
            });
        }
    }

    /**
     * ボタンの動作を実装するメソッド<br>
     * {@code registerButton} ボタンを押した際に、データベースにデータを登録する<br>
     * {@code todoButton} メニュー画面に戻るボタンを押した際に、メニュー画面に戻る<br>
     */
    private void initRegisterButton() {
        binding.registerButton.setText(id == -1 ? "登録" : "更新");
        // DBアクセス用のDAOを初期化する、データベース内のデータを取得
        var db = ToDoDatabase.getInstance(this);
        var dao = db.toDoDao();
        // 登録ボタン
        binding.registerButton.setOnClickListener(v -> {
            // DBアクセス用のスレッドを初期化、開始する
            var handlerThread = new HandlerThread(THREAD_NAME, Process.THREAD_PRIORITY_DEFAULT);
            handlerThread.start();
            var asyncHandler = new Handler(handlerThread.getLooper());

            // データベースに登録するデータを作成する
            var content = binding.toDoEditText.getText().toString();
            var subject = binding.subjectEditText.getText().toString();
            var estimatedTime = timeToInt(binding.editTextNumber.getText().toString());
            var deadline = binding.deadlineEditNumber.getText().toString();
            var detail = binding.detailEditText.getText().toString();
            var priority = getPriority();
            // DBアクセス用スレッド内でデータベースにデータを挿入する
            asyncHandler.post(() -> {
                CompletableFuture<String> future = null;
                ToDo toDo;
                if(id == -1) {
                    if(checkIsValidString(firebaseKey)) {
                        toDo = new ToDo(firebaseKey, content, subject, estimatedTime, deadline, priority, detail, true);
                    } else {
                        toDo = new ToDo(true, content, subject, estimatedTime, deadline, priority, detail, true);
                    }
                } else {
                    // ToDoリストのデータを更新する
                    toDo = dao.get(id, isPersonal);
                    toDo.title = content;
                    toDo.subject = subject;
                    toDo.estimatedTime = estimatedTime;
                    toDo.deadline = deadline;
                    toDo.detail = detail;
                    toDo.priority = toDo.toInt(priority);
                    toDo.visible = true;
                }
                if(binding.shareButton.isChecked() && !checkIsValidString(toDo.firebaseKey)) {
                    future = FirebaseManager.sendTo(toDo);
                }
                ToDo finalToDo = toDo;
                Optional.ofNullable(future).ifPresentOrElse(f -> {
                    try {
                        finalToDo.firebaseKey = f.get();
                    } catch (ExecutionException | InterruptedException ignored) {}
                    if(id == -1) {
                        dao.insert(finalToDo);
                    } else {
                        dao.update(finalToDo);
                    }
                    handlerThread.quitSafely();
                    runOnUiThread(() -> {
                        Toast.makeText(this, "登録しました", Toast.LENGTH_SHORT).show();
                        var intent = new Intent();
                        setResult(RESULT_OK, intent);
                        finish();
                    });
                }, () -> {
                    if(id == -1) {
                        dao.insert(toDo);
                    } else {
                        dao.update(toDo);
                    }
                    handlerThread.quitSafely();
                    runOnUiThread(() -> {
                        Toast.makeText(this, "登録しました", Toast.LENGTH_SHORT).show();
                        var intent = new Intent();
                        setResult(RESULT_OK, intent);
                        finish();
                    });
                });
            });
        });
    }

    private void initRevertButton() {
        binding.revertButton.setOnClickListener(v -> {
            var intent = new Intent();
            setResult(RESULT_OK, intent);
            finish();
        });
    }

    private void initImportButton() {
        if(checkIsValidString(firebaseKey)) {
            binding.importButton.setVisibility(android.view.View.GONE);
            binding.shareButton.setVisibility(android.view.View.GONE);
        }
        if(id != -1) {
            binding.importButton.setVisibility(android.view.View.GONE);
        }
        binding.importButton.setOnClickListener(v -> {
            var intent = new Intent(this, ImportActivity.class);
            startActivity(intent);
            finish();
        });
    }


    /**
     * ラジオボタンの選択状態から、優先度を取得するメソッド
     */
    private ToDo.Priority getPriority() {
        if (binding.radioLow.isChecked()) {
            return ToDo.Priority.LOW;
        } else if (binding.radioMiddle.isChecked()) {
            return ToDo.Priority.MEDIUM;
        } else if (binding.radioHigh.isChecked()) {
            return ToDo.Priority.HIGH;
        }
        return ToDo.Priority.LOW;
    }

    private void hideKeyboard() {
        var imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(binding.getRoot().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }
}