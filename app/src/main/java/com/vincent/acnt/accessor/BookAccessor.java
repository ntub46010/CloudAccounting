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
import com.google.firebase.firestore.WriteBatch;
import com.vincent.acnt.MyApp;
import com.vincent.acnt.TaskFinishListener;
import com.vincent.acnt.data.Constant;
import com.vincent.acnt.entity.Book;
import com.vincent.acnt.entity.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

public class BookAccessor {
    private CollectionReference collection;

    public BookAccessor(CollectionReference collection) {
        this.collection = collection;
    }

    public void createBook(final Book book, final RetrieveBookListener listener) {
        collection
                .add(book)
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        if (task.isSuccessful()) {
                            book.defineDocumentId(task.getResult().getId());
                            listener.onRetrieve(book);
                        } else {
                            listener.onRetrieve(null);
                        }
                    }
                });
    }

    public void loadBooksByList(List<String> ids, final RetrieveBooksListener listener) {
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
                            List<Book> books = new ArrayList<>();
                            Book book;

                            for (int i = 0, len = documentSnapshots.size(); i < len; i++) {
                                book = documentSnapshots.get(i).toObject(Book.class);
                                book.defineDocumentId(documentSnapshots.get(i).getId());
                                books.add(book);
                            }

                            listener.onRetrieve(books);
                        } else {

                        }
                    }
                });
    }

    public void patchBook(String documentId, String field, Object value, final TaskFinishListener listener) {
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

    public void patchBook(String documentId, Map<String, Object> properties, final TaskFinishListener listener) {
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

                        }
                    }
                });
    }

    public void deleteBook(String documentId, final TaskFinishListener listener) {
        collection
                .document(documentId)
                .delete()
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

    public ListenerRegistration observeBookMembersById(String documentId, final RetrieveBookMembersListener listener) {
        return collection
                .document(documentId)
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                        Book book = documentSnapshot.toObject(Book.class);
                        book.defineDocumentId(documentSnapshot.getId());

                        List<String> memberIds = new ArrayList<>();
                        memberIds.addAll(book.getAdminMembers());
                        memberIds.addAll(book.getApprovedMembers());
                        memberIds.addAll(book.getWaitingMembers());

                        UserAccessor accessor = new UserAccessor(MyApp.db.collection(Constant.KEY_USERS));
                        accessor.loadUsersByList(
                                memberIds,
                                new UserAccessor.RetrieveUsersListener() {
                                    @Override
                                    public void onRetrieve(List<User> users) {
                                        listener.onRetrieve(users);
                                    }
                                }
                        );

                    }
                });
    }

    public interface RetrieveBookListener {
        void onRetrieve(Book book);
    }

    public interface RetrieveBooksListener {
        void onRetrieve(List<Book> books);
    }

    public interface RetrieveBookMembersListener {
        void onRetrieve(List<User> members);
    }
}
