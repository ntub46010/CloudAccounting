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
                            listener.onFailure(task.getException());
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
                            listener.onFailure(task.getException());
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
                            listener.onFailure(task.getException());
                        }
                    }
                });
    }

    public ListenerRegistration observeTodayStatement(final RetrieveTodayStatementListener listener) {
        final int today = Integer.parseInt(new SimpleDateFormat("yyyyMMdd").format(new Date()));

        final int thisMonthStartDate = (today / 100) * 100 + 1;
        int thisMonthEndDate = thisMonthStartDate + 30;
        int lastMonthStartDate = thisMonthStartDate % 10000 == 101 ?
                ((thisMonthStartDate - 10000) / 10000) * 10000 + 1201 :
                thisMonthStartDate - 100;

        return collection
                .whereGreaterThanOrEqualTo(Constant.PRO_DATE, lastMonthStartDate)
                .whereLessThanOrEqualTo(Constant.PRO_DATE, thisMonthEndDate)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        List<DocumentSnapshot> documentSnapshots = queryDocumentSnapshots.getDocuments();
                        List<Entry> monthEntries = new ArrayList<>();
                        List<Entry> todayEntries = new ArrayList<>();
                        List<Subject> subjects;
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

                                //累計本月支出
                                if (subject.getNo().substring(0, 1).equals(Constant.CODE_TYPE[4])) {
                                    if (entry.getDate() >= thisMonthStartDate) {
                                        thisMonthExpanseCredit += subject.getCredit();
                                        thisMonthExpanseDebit += subject.getDebit();

                                        //若為今日的分錄，則保存起來
                                        if (entry.getDate() == today) {
                                            entry.defineDocumentId(documentSnapshots.get(i).getId());
                                            todayEntries.add(entry);
                                        }
                                    } else {
                                        lastMonthExpanseCredit += subject.getCredit();
                                        lastMonthExpanseDebit += subject.getDebit();
                                    }
                                }
                            }

                            //保存本月的分錄
                            monthEntries.add(entry);
                        }

                        listener.onRetrieve(
                                lastMonthExpanseCredit - lastMonthExpanseDebit,
                                thisMonthExpanseCredit - thisMonthExpanseDebit,
                                monthEntries,
                                todayEntries
                        );
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
                            listener.onFailure(task.getException());
                        }
                    }
                });
    }

    public interface RetrieveEntryListener {
        void onRetrieve(Entry entry);
        void onFailure(Exception e);
    }

    public interface RetrieveEntriesListener {
        void onRetrieve(List<Entry> entries);
        void onFailure(Exception e);
    }

    public interface RetrieveTodayStatementListener {
        void onRetrieve(int lastMonthExpanse, int thisMonthExpanse, List<Entry> thisMonthEntries, List<Entry> todayEntries);
    }
}
