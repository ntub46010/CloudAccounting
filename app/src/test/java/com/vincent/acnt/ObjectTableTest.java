package com.vincent.acnt;

import android.util.NoSuchPropertyException;

import com.vincent.acnt.data.ObjectTable;
import com.vincent.acnt.entity.Subject;

import org.junit.Test;

import static org.junit.Assert.*;

public class ObjectTableTest {

    @Test
    public void createTableWithMoreCapacity() throws Exception {
        ObjectTable<Subject> table = new ObjectTable<>(Subject.class, 64);
        assertEquals(64, table.capacity());
    }

    @Test
    public void addObject() throws Exception {
        ObjectTable<Subject> table = new ObjectTable<>(Subject.class);
        Subject subject = new Subject();
        subject.setNo("101");
        subject.setName("現金");
        subject.setCredit(10000);
        subject.setDebit(0);
        subject.defineDocumentId("A1B2C3D4E5");

        subject.setId(1);
        table.add(subject);

        assertEquals(1, table.size());
    }

    @Test
    public void expandCapacity() throws Exception {
        ObjectTable<Subject> table = generateFullTable();

        assertEquals(16, table.capacity());

        table.add(new Subject());
        assertEquals(32, table.capacity());
    }

    @Test
    public void removeObject() throws Exception {
        ObjectTable<Subject> table = new ObjectTable<>(Subject.class);
        Subject subject = new Subject();

        for (int i = 0; i < 2; i++) {
            subject.setId(i);
            table.add(subject);
        }

        table.remove(0);
        assertEquals(1, table.size());
    }

    @Test
    public void findFirstObjectBySingleProperty() throws Exception {
        ObjectTable<Subject> table = generateFullTable();

        assertEquals("Name of 2", table.findFirstByProperty("credit", 2).getName());
    }

    @Test
    public void findNothingBySingleProperty() throws Exception {
        ObjectTable<Subject> table = new ObjectTable<>(Subject.class);
        assertNull(table.findFirstByProperty("id", 0));
    }

    @Test
    public void findListBySingleProperty() throws Exception {
        ObjectTable<Subject> table = generateFullTable();

        assertEquals(6, table.findAllByProperty("credit", 0).size());
    }

    @Test
    public void findEmptyBySingleProperty() throws Exception {
        ObjectTable<Subject> table = new ObjectTable<>(Subject.class);
        assertTrue(table.findAllByProperty("id", 0).isEmpty());
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void removeObjectFromNonexistentIndex() throws Exception {
        ObjectTable<Subject> table = new ObjectTable<>(Subject.class);
        Subject subject = new Subject();

        for (int i = 0; i < 2; i++) {
            subject.setId(i);
            table.add(subject);
        }

        table.remove(2);
    }

    @Test(expected = NoSuchPropertyException.class)
    public void findObjectByNonexistentProperty() throws Exception {
        ObjectTable<Subject> table = new ObjectTable<>(Subject.class);
        Subject subject;

        for (int i = 0; i < 3; i++) {
            subject = new Subject();
            subject.setId(i);
            subject.setName("Name of " + String.valueOf(i));

            table.add(subject);
        }

        table.findFirstByProperty("createTime", 20180929);
    }

    @Test
    public void clearTable() throws Exception {
        ObjectTable<Subject> table = new ObjectTable<>(Subject.class);
        Subject subject;

        for (int i = 0; i < table.capacity(); i++) {
            subject = new Subject();
            subject.setId(i);
            subject.setCredit(i % 3);

            table.add(subject);
        }

        table.clear();
        assertEquals(0, table.size());
    }

    private ObjectTable generateFullTable() throws Exception {
        ObjectTable<Subject> table = new ObjectTable<>(Subject.class);
        Subject subject;

        for (int i = 0; i < table.capacity(); i++) {
            subject = new Subject();
            subject.setId(i);
            subject.setName("Name of " + String.valueOf(i));
            subject.setCredit(i % 3);

            table.add(subject);
        }

        return table;
    }

}