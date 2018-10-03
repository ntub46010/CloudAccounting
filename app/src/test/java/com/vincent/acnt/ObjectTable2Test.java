package com.vincent.acnt;

import android.util.NoSuchPropertyException;

import com.vincent.acnt.data.ObjectTable2;
import com.vincent.acnt.entity.Subject;

import org.junit.Test;

import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.Assert.*;

public class ObjectTable2Test {

    @Test
    public void insertObject() throws Exception {
        ObjectTable2<Long, Subject> table = new ObjectTable2<>(Subject.class, "id");
        Subject subject = new Subject();
        subject.setId(1);
        subject.setNo("101");
        subject.setName("現金");
        subject.setCredit(10000);
        subject.setDebit(0);
        subject.defineDocumentId("A1B2C3D4E5");

        table.add(subject);

        Subject resultSubject = table.findFirstByProperty("id", 1L);

        assertEquals(1, table.size());
        assertEquals(subject.getNo(), resultSubject.getNo());
        assertEquals(subject.getName(), resultSubject.getName());
        assertEquals(subject.getCredit(), resultSubject.getCredit());
        assertEquals(subject.getDebit(), resultSubject.getDebit());
        assertEquals(subject.obtainDocumentId(), resultSubject.obtainDocumentId());
    }

    @Test
    public void removeObject() throws Exception {
        ObjectTable2<Long, Subject> table = generateFullTable();
        int originalSize = table.size();

        table.remove(0L);

        Subject resultSubject = table.findFirstByProperty("id", 0);

        assertEquals(originalSize - 1, table.size());
        assertNull(resultSubject);
    }

    @Test
    public void findFirstObjectBySingleProperty() throws Exception {
        ObjectTable2<Long, Subject> table = generateFullTable();

        assertEquals(2, table.findFirstByProperty("name", "Name of 2").getId());
        assertEquals("Name of 0", table.findFirstByProperty("no", "no. 0").getName());
        assertNull(table.findFirstByProperty("id", -1));
        assertNull(table.findFirstByProperty("no", "no. -1"));
    }

    @Test
    public void findListBySingleProperty() throws Exception {
        ObjectTable2<Long, Subject> table = generateFullTable();

        assertEquals(table.size() / 3 + 1, table.findAllByProperty("credit", 0).size());
        assertEquals(1, table.findAllByProperty("no", "no. 0").size());
        assertTrue(table.findAllByProperty("id", -1).isEmpty());
        assertTrue(table.findAllByProperty("no", "no. -1").isEmpty());
    }

    @Test
    public void findAll() throws Exception {
        ObjectTable2<Long, Subject> table = generateFullTable();

        List<Subject> subjects = table.findAll();

        assertEquals(table.size(), subjects.size());

        for (int i = 0; i < table.size(); i++) {
            assertEquals(i, subjects.get(i).getId());
        }
    }

    @Test
    public void findAllPropertyValues() throws Exception {
        ObjectTable2<Long, Subject> table = generateFullTable();

        List<String> numbers = (List<String>)(List<?>) table.findAllPropertyValues("no");

        assertEquals(table.size(), numbers.size());

        for (int i = 0; i < table.size(); i++) {
            assertEquals("no. " + String.valueOf(i), numbers.get(i));
        }
    }

    @Test
    public void checkTupleIsExistByValue() throws Exception {
        ObjectTable2<Long, Subject> table = generateFullTable();

        assertTrue(table.existByProperty("no", "no. 0"));
        assertFalse(table.existByProperty("no", "no. -1"));
        assertTrue(table.existByProperty("name", "Name of 0"));
        assertFalse(table.existByProperty("name", "Name of -1"));
    }

    @Test
    public void findSiblingValueByProperty() throws Exception {
        ObjectTable2<Long, Subject> table = generateFullTable();

        assertEquals(3, table.findSiblingValueByProperty("id", 3, "id"));
        assertEquals("no. 3", table.findSiblingValueByProperty("id", 3L, "no"));
        assertEquals(0, table.findSiblingValueByProperty("no", "no. 3", "credit"));
        assertNull(table.findSiblingValueByProperty("id", "-1", "credit"));
    }

    @Test(expected = NoSuchElementException.class)
    public void removeObjectFromNonexistentIndex() throws Exception {
        ObjectTable2<Long, Subject> table = generateFullTable();

        table.remove(-1L);
    }

    @Test(expected = NoSuchPropertyException.class)
    public void findObjectByNonexistentProperty() throws Exception {
        ObjectTable2<Long, Subject> table = generateFullTable();

        table.findFirstByProperty("createTime", 20180929);
    }

    @Test(expected = NoSuchPropertyException.class)
    public void createTableWithNonexistentPrimaryField() throws Exception {
        ObjectTable2<Long, Subject> table = new ObjectTable2<>(Subject.class, "studentId");
    }

    @Test
    public void clearTable() throws Exception {
        ObjectTable2<Long, Subject> table = generateFullTable();
        table.clear();

        assertEquals(0, table.size());
    }

    private ObjectTable2<Long, Subject> generateFullTable() throws Exception {
        ObjectTable2<Long, Subject> table = new ObjectTable2<>(Subject.class, "id");
        Subject subject;

        for (int i = 0; i < 10; i++) {
            subject = new Subject();
            subject.setId(i);
            subject.setNo("no. " + String.valueOf(i));
            subject.setName("Name of " + String.valueOf(i));
            subject.setCredit(i % 3);

            table.add(subject);
        }

        return table;
    }
}
