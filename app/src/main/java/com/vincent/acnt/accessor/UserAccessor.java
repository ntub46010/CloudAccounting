package com.vincent.acnt.accessor;

import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import com.vincent.acnt.data.Constant;
import com.vincent.acnt.entity.User;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

public class UserAccessor extends BaseAccessor {

    public UserAccessor(CollectionReference collection) {
        super.collection = collection;
    }

    public void loadUserById(String id, final RetrieveEntityListener listener) {
        collection
                .whereEqualTo(Constant.PRO_ID, id)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<DocumentSnapshot> documentSnapshots = task.getResult().getDocuments();

                            //檢查資料庫有無該使用者
                            if (documentSnapshots.isEmpty()) {
                                listener.onRetrieve(null);
                            } else {
                                User user = documentSnapshots.get(0).toObject(User.class);
                                user.defineDocumentId(documentSnapshots.get(0).getId());

                                listener.onRetrieve(user);
                            }
                        } else {
                            listener.onFailure(task.getException());
                        }
                    }
                });
    }

    public ListenerRegistration observeUserById(String documentId, final RetrieveEntityListener listener) {
        return super.collection
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

    public void loadUsersByList(List<String> ids, final RetrieveEntitiesListener listener) {
        for (int i = 0, len = ids.size(); i < len; i++) {
            super.collection.whereEqualTo(Constant.PRO_ID, ids.get(i));
        }

        super.collection
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
                            listener.onFailure(task.getException());
                        }
                    }
                });
    }

}
