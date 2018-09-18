package com.vincent.acnt.accessor;

import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.WriteBatch;
import com.vincent.acnt.MyApp;
import com.vincent.acnt.entity.Entity;

import java.util.Map;

public class BaseAccessor {
    protected CollectionReference collection;

    public void create(final Entity entity, final RetrieveEntityListener listener) {
        collection
                .add(entity)
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        if (task.isSuccessful()) {
                            entity.defineDocumentId(task.getResult().getId());
                            listener.onRetrieve(entity);
                        } else {
                            listener.onFailure(task.getException());
                        }
                    }
                });
    }

    public void update(String documentId, Entity entity, final TaskFinishListener listener) {
        collection
                .document(documentId)
                .set(entity)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            listener.onFinish();
                        } else {
                            listener.onFailure(task.getException());
                        }
                    }
                });
    }

    public void patch(String documentId, String field, Object value, final TaskFinishListener listener) {
        collection
                .document(documentId)
                .update(field, value)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            listener.onFinish();
                        } else {
                            listener.onFailure(task.getException());
                        }
                    }
                });
    }

    public void patch(String documentId, Map<String, Object> properties, final TaskFinishListener listener) {
        DocumentReference documentReference = collection.document(documentId);
        WriteBatch writeBatch = MyApp.db.batch();

        for (String field : properties.keySet()) {
            writeBatch.update(documentReference, field, properties.get(field));
        }

        writeBatch
                .commit()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            listener.onFinish();
                        } else {
                            listener.onFailure(task.getException());
                        }
                    }
                });
    }

    public void delete(String documentId, final TaskFinishListener listener) {
        collection
                .document(documentId)
                .delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            listener.onFinish();
                        } else {
                            listener.onFailure(task.getException());
                        }
                    }
                });
    }
}
