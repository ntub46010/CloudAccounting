package com.vincent.acnt.accessor;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.vincent.acnt.data.Constant;
import com.vincent.acnt.entity.Subject;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

public class SubjectAccessor extends BaseAccessor {

    public SubjectAccessor(CollectionReference collection) {
        super.collection = collection;
    }

    public ListenerRegistration observeSubjects(final RetrieveEntitiesListener listener) {
        return super.collection
                .orderBy(Constant.PRO_SUBJECT_NO, Query.Direction.ASCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        List<DocumentSnapshot> documentSnapshots = queryDocumentSnapshots.getDocuments();
                        List<Subject> subjects = new ArrayList<>(32);
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

}
