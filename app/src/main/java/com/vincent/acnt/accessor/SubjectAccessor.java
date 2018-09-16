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
import com.vincent.acnt.entity.Subject;
import com.vincent.acnt.TaskFinishListener;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

public class SubjectAccessor {
    private CollectionReference collection;

    public SubjectAccessor(CollectionReference collection) {
        this.collection = collection;
    }

    public void createSubject(final Subject subject, final RetrieveSubjectListener listener) {
        collection
                .add(subject)
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        if (task.isSuccessful()) {
                            DocumentReference documentReference = task.getResult();
                            subject.defineDocumentId(documentReference.getId());

                            listener.onRetrieve(subject);
                        } else {
                            listener.onRetrieve(null);
                        }
                    }
                });
    }

    public void updateSubject(final Subject subject, final TaskFinishListener listener) {
        collection
                .document(subject.obtainDocumentId())
                .set(subject)
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

    public ListenerRegistration observeSubjects(final RetrieveSubjectsListener listener) {
        return collection
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        List<DocumentSnapshot> documentSnapshots = queryDocumentSnapshots.getDocuments();
                        List<Subject> subjects = new ArrayList<>();
                        Subject subject;

                        for (int i = 0, len = documentSnapshots.size(); i < len; i++) {
                            subject = documentSnapshots.get(i).toObject(Subject.class);
                            subject.defineDocumentId(documentSnapshots.get(i).getId());
                            subjects.add(subject);
                        }

                        listener.onRetrieve(subjects);
                    }
                });
    }

    public void deleteSubject(String documentId, final TaskFinishListener listener) {
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

    public interface RetrieveSubjectListener {
        void onRetrieve(Subject subject);
    }

    public interface RetrieveSubjectsListener {
        void onRetrieve(List<Subject> subjects);
    }
}
