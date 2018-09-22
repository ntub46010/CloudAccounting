package com.vincent.acnt.accessor;

import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.vincent.acnt.MyApp;
import com.vincent.acnt.data.Constant;
import com.vincent.acnt.data.Utility;
import com.vincent.acnt.entity.Entry;
import com.vincent.acnt.entity.LedgerRecord;
import com.vincent.acnt.entity.ReportItem;
import com.vincent.acnt.entity.Subject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

public class EntryAccessor extends BaseAccessor {

    public EntryAccessor(CollectionReference collection) {
        super.collection = collection;
    }

    public ListenerRegistration observeTodayStatement(final RetrieveTodayStatementListener listener) {
        final int today = Integer.parseInt(new SimpleDateFormat("yyyyMMdd").format(new Date()));

        final int thisMonthStartDate = (today / 100) * 100 + 1;
        int thisMonthEndDate = thisMonthStartDate + 30;
        int lastMonthStartDate = thisMonthStartDate % 10000 == 101 ?
                ((thisMonthStartDate - 10000) / 10000) * 10000 + 1201 :
                thisMonthStartDate - 100;

        return super.collection
                .whereGreaterThanOrEqualTo(Constant.PRO_DATE, lastMonthStartDate)
                .whereLessThanOrEqualTo(Constant.PRO_DATE, thisMonthEndDate)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        List<DocumentSnapshot> documentSnapshots = queryDocumentSnapshots.getDocuments();
                        List<Entry> monthEntries = new ArrayList<>(128);
                        List<Entry> todayEntries = new ArrayList<>(16);
                        List<Subject> subjects;
                        Entry entry;
                        Subject subject, s;

                        int thisMonthExpanseCredit = 0;
                        int thisMonthExpanseDebit = 0;
                        int lastMonthExpanseCredit = 0;
                        int lastMonthExpanseDebit = 0;

                        for (int i = 0, len = documentSnapshots.size(); i < len; i++) {
                            entry = documentSnapshots.get(i).toObject(Entry.class);
                            entry.defineDocumentId(documentSnapshots.get(i).getId());

                            subjects = entry.getSubjects();
                            for (int j = 0, len2 = subjects.size(); j < len2; j++) {
                                //將分錄中的科目補上名稱
                                subject = subjects.get(j);
                                s = MyApp.mapSubjectById.get(subject.getId());
                                subject.setName(s.getName());

                                //累計支出
                                if (s.getNo().substring(0, 1).equals(Constant.CODE_TYPE[4])) {
                                    if (entry.getDate() >= thisMonthStartDate) {
                                        thisMonthExpanseCredit += subject.getCredit();
                                        thisMonthExpanseDebit += subject.getDebit();
                                    } else {
                                        lastMonthExpanseCredit += subject.getCredit();
                                        lastMonthExpanseDebit += subject.getDebit();
                                    }
                                }
                            }

                            //若為本月的分錄，則保存起來
                            if (entry.getDate() >= thisMonthStartDate) {
                                monthEntries.add(entry);

                                //若為今日的分錄，則保存起來
                                if (entry.getDate() == today) {
                                    todayEntries.add(entry);
                                }
                            }

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

    public ListenerRegistration observeEntriesByMonth(int year, int month, final RetrieveEntitiesListener listener) {
        int startDate = Utility.getDateNumber(year, month, 1);
        int endDate = startDate + 30;

        return super.collection
                .orderBy(Constant.PRO_DATE, Query.Direction.DESCENDING)
                .orderBy(Constant.PRO_MEMO, Query.Direction.ASCENDING)
                .whereGreaterThanOrEqualTo(Constant.PRO_DATE, startDate)
                .whereLessThanOrEqualTo(Constant.PRO_DATE, endDate)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        List<DocumentSnapshot> documentSnapshots = queryDocumentSnapshots.getDocuments();
                        List<Entry> entries = new ArrayList<>(128);
                        List<Subject> subjects;
                        Entry entry;
                        Subject subject;

                        for (int i = 0, len = documentSnapshots.size(); i < len; i++) {
                            entry = documentSnapshots.get(i).toObject(Entry.class);
                            entry.defineDocumentId(documentSnapshots.get(i).getId());

                            //將分錄中的科目補上名稱
                            subjects = entry.getSubjects();
                            for (int j = 0, len2 = subjects.size(); j < len2; j++) {
                                subject = subjects.get(j);
                                subject.setName(MyApp.mapSubjectById.get(subject.getId()).getName());
                            }

                            entries.add(entry);
                        }

                        listener.onRetrieve(entries);
                    }
                });
    }

    public void loadLedgerItems(final long subjectId, final int endOfMonth, final RetrieveLedgerRecordListener listener) {
        super.collection
                .orderBy(Constant.PRO_DATE, Query.Direction.DESCENDING)
                .orderBy(Constant.PRO_MEMO, Query.Direction.ASCENDING)
                .whereLessThanOrEqualTo(Constant.PRO_DATE, endOfMonth)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (!task.isSuccessful()) {
                            listener.onFailure(task.getException());
                            return;
                        }

                        List<DocumentSnapshot> documentSnapshots = task.getResult().getDocuments();
                        List<LedgerRecord> items = new ArrayList<>(32);
                        List<Entry> entries = new ArrayList<>(128);

                        LedgerRecord item;
                        Entry entry;
                        Subject subject;

                        int startOfMonth = (endOfMonth / 100) * 100;
                        int totalCredit = 0, totalDebit = 0;
                        int cnt = 0;

                        for (int i = 0, len = documentSnapshots.size(); i < len; i++) {
                            entry = documentSnapshots.get(i).toObject(Entry.class);
                            List<Subject> subjects = entry.getSubjects();

                            for (int j = 0, len2 = subjects.size(); j < len2; j++) {
                                subject = subjects.get(j);

                                if (entry.getDate() < startOfMonth && subject.getId() == subjectId) {
                                    //從歷史分錄中計算累積借貸總額
                                    totalCredit += subject.getCredit();
                                    totalDebit += subject.getDebit();
                                    cnt++;
                                    continue;
                                }

                                if (subject.getId() == subjectId) {
                                    //儲存該月分錄，供點擊清單後能顯示詳情
                                    entries.add(entry);

                                    //儲存明細
                                    item = new LedgerRecord();
                                    item.setDate(entry.getDate());
                                    item.setMemo(entry.getMemo());
                                    item.setCredit(subject.getCredit());
                                    item.setDebit(subject.getDebit());

                                    items.add(item);
                                }
                            }
                        }

                        //加入歷史借貸方總額
                        if (cnt > 0) {
                            LedgerRecord historyItem = new LedgerRecord();
                            historyItem.setDate(endOfMonth);
                            historyItem.setMemo("(歷史累積紀錄)");
                            historyItem.setCredit(totalCredit);
                            historyItem.setDebit(totalDebit);

                            items.add(historyItem);
                            entries.add(null);
                        }

                        //加入期初餘額
                        Subject s = MyApp.mapSubjectById.get(subjectId);
                        LedgerRecord originalItem = new LedgerRecord();
                        originalItem.setDate((endOfMonth / 10000) * 10000 + 101); //修正為當年1/1，如20180101
                        originalItem.setMemo("(初始餘額)");
                        originalItem.setCredit(s.getCredit());
                        originalItem.setDebit(s.getDebit());

                        items.add(originalItem);
                        entries.add(null);

                        //由舊至新，計算清單項目所要顯示的餘額
                        LedgerRecord itm = items.get(items.size() - 1);
                        itm.setBalance(itm.getCredit() - itm.getDebit());
                        for (int i = items.size() - 2; i >= 0; i--) {
                            itm = items.get(i);
                            itm.setBalance(items.get(i + 1).getBalance() + itm.getCredit() - itm.getDebit());
                        }

                        listener.onRetrieve(items, entries);
                    }
                });
    }

    public void loadReportItems(int endDate, final Map<String, ReportItem> mapReportItem, final RetrieveReportItemsListener listener) {
        super.collection
                .orderBy(Constant.PRO_DATE, Query.Direction.DESCENDING)
                .orderBy(Constant.PRO_MEMO, Query.Direction.ASCENDING)
                .whereLessThanOrEqualTo(Constant.PRO_DATE, endDate)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (!task.isSuccessful()) {
                            listener.onFailure(task.getException());
                            return;
                        }

                        List<DocumentSnapshot> documentSnapshots = task.getResult().getDocuments();
                        List<Entry> entries = new ArrayList<>(128);
                        List<Subject> subjects;
                        Subject subject, s;

                        //儲存分錄
                        for (int i = 0, len = documentSnapshots.size(); i < len; i++) {
                            entries.add(documentSnapshots.get(i).toObject(Entry.class));
                        }

                        //從各個分錄中將科目金額逐一儲存
                        ReportItem item;
                        for (int i = 0, len = entries.size(); i < len; i++) {
                            subjects = entries.get(i).getSubjects();

                            for (int j = 0, len2 = subjects.size(); j < len2; j++) {
                                subject = subjects.get(j);
                                s = MyApp.mapSubjectById.get(subject.getId());

                                if (mapReportItem.containsKey(s.getNo())) {
                                    //若科目已存在於待輸出結果，取出後累積金額，再放置回去
                                    item = mapReportItem.get(s.getNo());

                                    item.addCredit(subject.getCredit());
                                    item.addDebit(subject.getDebit());
                                } else {
                                    item = new ReportItem();

                                    item.setId(s.getNo());
                                    item.setName(s.getName());
                                    item.addCredit(subject.getCredit());
                                    item.addDebit(subject.getDebit());
                                }

                                mapReportItem.put(s.getNo(), item);
                            }
                        }

                        listener.onRetrieve(mapReportItem);
                    }
                });
    }

    public interface RetrieveTodayStatementListener {
        void onRetrieve(int lastMonthExpanse, int thisMonthExpanse, List<Entry> thisMonthEntries, List<Entry> todayEntries);
    }

    public interface RetrieveLedgerRecordListener {
        void onRetrieve(List<LedgerRecord> records, List<Entry> entries);
        void onFailure(Exception e);
    }

    public interface RetrieveReportItemsListener {
        void onRetrieve(Map<String, ReportItem> mapReportItem);
        void onFailure(Exception e);
    }
}
