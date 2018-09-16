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
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.vincent.acnt.MyApp;
import com.vincent.acnt.TaskFinishListener;
import com.vincent.acnt.data.Constant;
import com.vincent.acnt.entity.Entry;
import com.vincent.acnt.entity.Subject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Nullable;

public class EntryAccessor {
    private CollectionReference collection;

    public EntryAccessor(CollectionReference collection) {
        this.collection = collection;
    }

    public void createEntry(final Entry entry, final RetrieveEntryListener listener) {
        collection
                .add(entry)
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        if (task.isSuccessful()) {
                            entry.defineDocumentId(task.getResult().getId());

                            listener.onRetrieve(entry);
                        } else {
                            listener.onRetrieve(null);
                        }
                    }
                });
    }

    public void updateEntry(final Entry entry, final TaskFinishListener listener) {
        collection
                .document(entry.obtainDocumentId())
                .set(entry)
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

    public void loadEntriesByDate(int startDate, int endDate, final RetrieveEntriesListener listener) {
        collection
                .orderBy(Constant.PRO_DATE, Query.Direction.DESCENDING)
                .orderBy(Constant.PRO_MEMO, Query.Direction.ASCENDING)
                .whereGreaterThanOrEqualTo(Constant.PRO_DATE, startDate)
                .whereLessThan(Constant.PRO_DATE, endDate)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<DocumentSnapshot> documentSnapshots = task.getResult().getDocuments();
                            List<Entry> entries = new ArrayList<>();
                            List<Subject> subjects;
                            Entry entry;
                            Subject subject;

                            for (int i = 0, len = documentSnapshots.size(); i < len; i++) {
                                entry = documentSnapshots.get(i).toObject(Entry.class);
                                entry.defineDocumentId(documentSnapshots.get(i).getId());

                                //將分錄中的科目補上名稱
                                subjects = entry.getSubjects();
                                for (int j = 0, len2 = subjects.size(); i < len2; i++) {
                                    subject = subjects.get(j);
                                    subject.setName(MyApp.mapSubjectById.get(subject.getId()).getName());
                                }

                                entries.add(entry);
                            }

                            listener.onRetrieve(entries);
                        } else {
                            listener.onRetrieve(null);
                        }
                    }
                });
    }

    public ListenerRegistration observeTodayStatement() {
        final int today = Integer.parseInt(new SimpleDateFormat("yyyyMMdd").format(new Date()));
        int startDate = (today / 100) * 100 + 1;
        int endDate = startDate + 30;

        return collection
                .whereGreaterThanOrEqualTo(Constant.PRO_DATE, startDate)
                .whereLessThanOrEqualTo(Constant.PRO_DATE, endDate)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        List<DocumentSnapshot> documentSnapshots = queryDocumentSnapshots.getDocuments();
                        List<Entry> entries = new ArrayList<>();
                        List<Subject> subjects = new ArrayList<>();
                        Entry entry;
                        Subject subject;

                        int thisMonthExpanseCredit = 0;
                        int thisMonthExpanseDebit = 0;
                        int lastMonthExpanseCredit = 0;
                        int lastMonthExpanseDebit = 0;

                        for (int i = 0, len = documentSnapshots.size(); i < len; i++) {
                            entry = documentSnapshots.get(i).toObject(Entry.class);
                            entry.defineDocumentId(documentSnapshots.get(i).getId());

                            //將分錄中的科目補上名稱
                            subjects = entry.getSubjects();
                            for (int j = 0, len2 = subjects.size(); i < len2; i++) {
                                subject = subjects.get(j);
                                subject.setName(MyApp.mapSubjectById.get(subject.getId()).getName());

                                if (subject.getNo().substring(0, 1).equals(Constant.CODE_TYPE[4])) {
                                    thisMonthExpanseCredit += subject.getCredit();
                                    thisMonthExpanseDebit += subject.getDebit();
                                }
                            }

                            //若為今日的分錄，則保存起來
                            if (entry.getDate() == today) {
                                entry.defineDocumentId(documentSnapshots.get(i).getId());
                                entries.add(entry);
                            }

                            entries.add(entry);
                        }
                    }
                });
    }

    public void deleteEntry(String documentId, final TaskFinishListener listener) {
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

    public interface RetrieveEntryListener {
        void onRetrieve(Entry entry);
    }

    public interface RetrieveEntriesListener {
        void onRetrieve(List<Entry> entries);
    }
}
