package jp.ac.meijou.android.taskreview;

import static jp.ac.meijou.android.taskreview.MainActivity.KEY_FIREBASE_KEY;
import static jp.ac.meijou.android.taskreview.MainActivity.KEY_IS_PERSONAL;
import static jp.ac.meijou.android.taskreview.MainActivity.KEY_TODO_ID;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Process;
import android.os.HandlerThread;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import jp.ac.meijou.android.taskreview.databinding.ActivityImportBinding;
import jp.ac.meijou.android.taskreview.firebase.FirebaseManager;
import jp.ac.meijou.android.taskreview.room.ToDo;
import jp.ac.meijou.android.taskreview.room.ToDoDiffCallback;
import jp.ac.meijou.android.taskreview.ui.ToDoListAdapter;

public class ImportActivity extends AppCompatActivity {
    private ActivityImportBinding binding;
    private static final String THREAD_NAME = "import_activity-thread";

    private HandlerThread handlerThread;
    private Handler asyncHandler;
    private ToDoListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityImportBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initToDoList(binding.toDoView);
        initButton();
    }

    private void initToDoList(RecyclerView recyclerView) {
        handlerThread = new HandlerThread(THREAD_NAME, Process.THREAD_PRIORITY_DEFAULT);
        handlerThread.start();
        asyncHandler = new Handler(handlerThread.getLooper());
        initToDoListAdapter();
        asyncHandler.post(() -> {
            FirebaseManager.getAll().thenAccept(list -> {
                adapter.submitList(list);
            });
        });
        recyclerView.setAdapter(adapter);
        var layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        var itemDecoration = new DividerItemDecoration(this, layoutManager.getOrientation());
        recyclerView.addItemDecoration(itemDecoration);
    }

    private void initToDoListAdapter() {
        var callback = new ToDoDiffCallback();

        Function<ToDo, View.OnClickListener> openDetailIntent = toDo -> v -> {
            var intent = new Intent(this, RegisterActivity.class);
            intent.putExtra(KEY_IS_PERSONAL, toDo.isPersonal);
            intent.putExtra(KEY_FIREBASE_KEY, toDo.firebaseKey);
            startActivity(intent);
        };
        adapter = new ToDoListAdapter(callback, openDetailIntent);
    }

    private void initButton() {
        binding.revertButton.setOnClickListener(v -> {
            var intent = new Intent();
            setResult(RESULT_OK, intent);
            finish();
        });
    }
    

    private void initSpinner(Spinner spinner, String defaultValue, String... keys) {
        var adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapter.add(defaultValue);
        handlerThread = new HandlerThread(THREAD_NAME, Process.THREAD_PRIORITY_DEFAULT);
        handlerThread.start();
        asyncHandler = new Handler(handlerThread.getLooper());
        asyncHandler.post(() -> {
            var future = FirebaseManager.getSubject(true, keys);
            try {
                var list = future.get();
                Optional.ofNullable(list).ifPresent(adapter::addAll);
            } catch (Exception ignored) {
            }
            handlerThread.quitSafely();
            runOnUiThread(() -> spinner.setAdapter(adapter));
        });
    }
}