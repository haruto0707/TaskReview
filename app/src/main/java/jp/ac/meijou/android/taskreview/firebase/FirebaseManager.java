package jp.ac.meijou.android.taskreview.firebase;

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
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

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
    public static ToDo[] getFromSubject(String subject) {
        var toDoList = new ArrayList<ToDo>();
        ref.child(TODO_PATH)
                .orderByChild("subject")
                .equalTo(subject)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(var child : snapshot.getChildren()) {
                            var firebaseToDo = child.getValue(FirebaseToDo.class);
                            if(firebaseToDo != null) {
                                toDoList.add(parseToDo(firebaseToDo, child.getKey()));
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.d("FirebaseManager", "onCancelled: " + error.getMessage());
                    }
                });
        return toDoList.toArray(new ToDo[0]);
    }
    private static ToDo parseToDo(FirebaseToDo firebaseToDo, String key) {
        if(getIdFromKey(key) == -1) return null;
        return new ToDo(getIdFromKey(key), firebaseToDo.title, firebaseToDo.subject,
                firebaseToDo.estimatedTime, firebaseToDo.deadline,
                parsePriority(firebaseToDo.priority), "", true);
    }

    private static int getIdFromKey(String key) {
        AtomicInteger id = new AtomicInteger(-1);
        ref.child("ids")
                .child(key)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        id.set(Optional
                                .ofNullable(snapshot.getValue(Integer.class))
                                .orElse(-1));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError ignored) {
                    }
                });
        return id.get();
    }

    public static CompletableFuture<List<String>> getSubject(String... keys) {
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
                    list.add(Objects.requireNonNull(child.getValue(String.class)));
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
}
