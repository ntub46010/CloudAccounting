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
import com.vincent.acnt.MyApp;
import com.vincent.acnt.data.Constant;
import com.vincent.acnt.entity.Book;
import com.vincent.acnt.entity.Entity;
import com.vincent.acnt.entity.User;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

public class BookAccessor extends BaseAccessor {

    public BookAccessor(CollectionReference collection) {
        super.collection = collection;
    }

    public void loadBookById(String id, final RetrieveEntityListener listener) {
        super.collection
                .whereEqualTo(Constant.PRO_ID, id)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<DocumentSnapshot> documentSnapshots = task.getResult().getDocuments();

                            if (documentSnapshots.isEmpty()) {
                                listener.onRetrieve(null);
                                return;
                            }

                            Book book;
                            book = documentSnapshots.get(0).toObject(Book.class);
                            book.defineDocumentId(documentSnapshots.get(0).getId());

                            listener.onRetrieve(book);
                        } else {
                            listener.onFailure(task.getException());
                        }
                    }
                });
    }

    public void loadBooksByList(List<String> ids, final RetrieveEntitiesListener listener) {
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
                            List<Book> books = new ArrayList<>(16);
                            Book book;

                            for (int i = 0, len = documentSnapshots.size(); i < len; i++) {
                                book = documentSnapshots.get(i).toObject(Book.class);
                                book.defineDocumentId(documentSnapshots.get(i).getId());
                                books.add(book);
                            }

                            listener.onRetrieve(books);
                        } else {
                            listener.onFailure(task.getException());
                        }
                    }
                });
    }

    public ListenerRegistration observeBookMembersById(String documentId, final RetrieveBookMembersListener listener) {
        return collection
                .document(documentId)
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                        Book book = documentSnapshot.toObject(Book.class);

                        List<String> memberIds = new ArrayList<>(32);
                        memberIds.addAll(book.getAdminMembers());
                        memberIds.addAll(book.getApprovedMembers());
                        memberIds.addAll(book.getWaitingMembers());

                        UserAccessor accessor = new UserAccessor(MyApp.db.collection(Constant.KEY_USERS));
                        accessor.loadUsersByList(
                                memberIds,
                                new RetrieveEntitiesListener() {
                                    @Override
                                    public void onRetrieve(List<? extends Entity> entities) {
                                        listener.onRetrieve((List<User>) entities);
                                    }

                                    @Override
                                    public void onFailure(Exception e) {
                                        listener.onFailure(e);
                                    }
                                }
                        );

                    }
                });
    }

    public interface RetrieveBookMembersListener {
        void onRetrieve(List<User> members);
        void onFailure(Exception e);
    }
}
