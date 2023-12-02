package jp.ac.meijou.android.taskreview.firebase;

import static jp.ac.meijou.android.taskreview.room.ToDo.MESSAGE_ERROR;
import static jp.ac.meijou.android.taskreview.room.ToDo.parsePriority;

import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import jp.ac.meijou.android.taskreview.room.ToDo;

public class FirebaseManager {
    private static final String EVAL_PATH = "evaluations";
    private static final String TODO_PATH = "tasks";
    private static final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private static final DatabaseReference ref = database.getReference();

    public static CompletableFuture<Void> sendTo(ToDoEvaluation value) {
        var future = new CompletableFuture<Void>();
        DatabaseReference newRef = ref.child(EVAL_PATH);
        newRef.push().setValue(value, (error, ref) -> {
            future.complete(null);
        });
        return future;
    }
    public static CompletableFuture<String> sendTo(ToDo value) {
        DatabaseReference newRef = ref.child(TODO_PATH);
        CompletableFuture<String> key = new CompletableFuture<>();
        newRef.push().setValue(new FirebaseToDo(value), (error, ref) -> {
            key.complete(ref.getKey());
        });
        return key;
    }

    public static CompletableFuture<List<ToDo>> getAll() {
        var toDoList = new CompletableFuture<List<ToDo>>();
        ref.child(TODO_PATH)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        var futures = new ArrayList<CompletableFuture<Void>>();
                        var list = Collections.synchronizedList(new ArrayList<ToDo>());
                        for(var child : snapshot.getChildren()) {
                            var firebaseToDo = child.getValue(FirebaseToDo.class);
                            if(firebaseToDo != null) {
                                var future = CompletableFuture
                                        .completedFuture(firebaseToDo)
                                        .thenAccept(f -> {
                                            var key = child.getKey();
                                            var toDo = parseToDo(f, key);
                                            list.add(toDo);
                                        });
                                futures.add(future);
                            }
                        }
                        CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new))
                                .thenRun(() -> {
                                    toDoList.complete(list);
                                });
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.d("FirebaseManager", "onCancelled: " + error.getMessage());
                    }
                });
        return toDoList;
    }
    public static CompletableFuture<ToDo> getFromKey(String key) {
        var firebaseToDo = new CompletableFuture<ToDo>();
        ref.child(TODO_PATH)
                .child(key)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        var value = snapshot.getValue(FirebaseToDo.class);
                        if(value != null) {
                            var toDo = parseToDo(value, key);
                            firebaseToDo.complete(toDo);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.d("FirebaseManager", "onCancelled: " + error.getMessage());
                    }
                });
        return firebaseToDo;
    }
    public static ToDo parseToDo(FirebaseToDo firebaseToDo, String key) {

        return new ToDo(key, firebaseToDo.title, firebaseToDo.subject,
                firebaseToDo.estimatedTime, firebaseToDo.deadline,
                parsePriority(firebaseToDo.priority), "", true);
    }


    public static CompletableFuture<List<String>> getSubject(boolean isSubject, String... keys) {
        var subjectList = new CompletableFuture<List<String>>();
        var newRef = ref.child("subjects");
        for(var key : keys) {
            newRef = newRef.child(key);
        }
        newRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                var list = new ArrayList<String>();
                for(var child : snapshot.getChildren()) {
                    String value;
                    if(isSubject) value = child.getKey();
                    else value = child.child("name").getValue(String.class);
                    if(value != null && !value.equals("name")) list.add(value);
                }
                subjectList.complete(list);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("FirebaseManager", "onCancelled: " + error.getMessage());
            }
        });
        return subjectList;
    }

    public static CompletableFuture<String> getKeyFromName(String name, String... keys) {
        var key = new CompletableFuture<String>();
        var newRef = ref.child("subjects");
        for(var child : keys) {
            newRef = newRef.child(child);
        }
        newRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        boolean found = false;
                        for(var value : snapshot.getChildren()) {
                            var val = value.child("name").getValue(String.class);
                            if(val.equals(name)) {
                                key.complete(value.getKey());
                                found = true;
                                break;
                            }
                        }
                        if(!found) key.complete(MESSAGE_ERROR);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.d("FirebaseManager", "onCancelled: " + error.getMessage());
                    }
                });
        return key;
    }
}
