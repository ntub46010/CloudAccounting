package com.vincent.acnt.accessor;

import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import com.vincent.acnt.data.Constant;
import com.vincent.acnt.entity.User;
import com.vincent.acnt.TaskFinishListener;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

public class UserAccessor {
    private CollectionReference collection;

    public UserAccessor(CollectionReference collection) {
        this.collection = collection;
    }

    public void createUser(final User user, final RetrieveUserListener listener) {
        user.setBooks(new ArrayList<String>());

        collection
                .add(user)
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        if (task.isSuccessful()) {
                            DocumentReference documentReference = task.getResult();
                            user.defineDocumentId(documentReference.getId());

                            listener.onRetrieve(user);
                        } else {
                            listener.onRetrieve(null);
                        }
                    }
                });
    }

    public ListenerRegistration observeUserById(String documentId, final RetrieveUserListener listener) {
        return collection
                .document(documentId)
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                        User user = documentSnapshot.toObject(User.class);
                        user.defineDocumentId(documentSnapshot.getId());

                        listener.onRetrieve(user);
                    }
                });
    }

    public void loadUsersByList(List<String> ids, final RetrieveUsersListener listener) {
        for (int i = 0, len = ids.size(); i < len; i++) {
            collection.whereEqualTo(Constant.PRO_ID, ids.get(i));
        }

        collection
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<DocumentSnapshot> documentSnapshots = task.getResult().getDocuments();
                            List<User> users = new ArrayList<>();
                            User user;

                            for (int i = 0, len = documentSnapshots.size(); i < len; i++) {
                                user = documentSnapshots.get(i).toObject(User.class);
                                user.defineDocumentId(documentSnapshots.get(i).getId());
                                users.add(user);
                            }

                            listener.onRetrieve(users);
                        } else {

                        }
                    }
                });
    }

    public void patchUser(String documentId, String field, Object value, final TaskFinishListener listener) {
        collection
                .document(documentId)
                .update(field, value)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            listener.onFinish();
                        } else {

                        }
                    }
                });
    }

    public interface RetrieveUserListener {
        void onRetrieve(User user);
        void onFailure(Exception e);
    }

    public interface RetrieveUsersListener {
        void onRetrieve(List<User> users);
        void onFailure(Exception e);
    }
}
