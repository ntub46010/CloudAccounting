package com.vincent.acnt.data;

import android.util.NoSuchPropertyException;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

public class ObjectTable2<R, E> {
    private Table<R, String, Object> table;
    private String primaryFieldName;

    private Field[] fields;
    private String[] fieldNames;
    private List<Method> mutatorMethods;
    private Constructor constructor;
    private Map<String, Field> mapFieldByName;

    private String[] ACCEPTED_MUTATOR_METHODS_PREFIX = {"set", "define"};

    public ObjectTable2(Class clz, String primaryFieldName) {
        table = HashBasedTable.create();
        this.fields = clz.getDeclaredFields();
        this.mapFieldByName = new HashMap<>();
        this.primaryFieldName = primaryFieldName;

        prepareUtils(clz);
    }

    private void prepareUtils(Class clz) {
        try {
            this.constructor = clz.getConstructor();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

        fieldNames = new String[fields.length];
        Field field;

        for (int i = 0; i < fields.length; i++) {
            field = fields[i];

            field.setAccessible(true);
            this.mapFieldByName.put(field.getName().toLowerCase(), field);
            if (!field.getName().equals(primaryFieldName)) {
                fieldNames[i] = field.getName().toLowerCase();
            }
        }
        checkFieldIsExist(primaryFieldName);

        Method[] allMethods = clz.getMethods();
        mutatorMethods = new ArrayList<>();

        for (int i = 0; i < allMethods.length; i++) {
            if (allMethods[i].getName().startsWith("set") || allMethods[i].getName().startsWith("define")) {
                mutatorMethods.add(allMethods[i]);
            }
        }
    }

    public void add(E object) {
        try {
            Field primaryField = mapFieldByName.get(primaryFieldName);
            R primaryKey = (R) primaryField.get(object);
            Field field;
            Object value;

            for (int i = 0, len = fields.length; i < len; i++) {
                field = fields[i];

                if (!field.getName().equals(primaryFieldName)) {
                    value = field.get(object);

                    if (value != null) {
                        table.put(primaryKey, field.getName().toLowerCase(), value);
                    }
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public void remove(R rowKey) {
        if (!table.rowKeySet().contains(rowKey)) {
            throw new NoSuchElementException("There is not tuple which row key is " + String.valueOf(rowKey));
        }

        for (int i = 0, len = fieldNames.length; i < len; i++) {
            table.remove(rowKey, fieldNames[i]);
        }
    }

    public List<E> findAll() {
        List<E> results = new ArrayList<>();

        for (R primary : table.rowKeySet()) {
            results.add(constructObject(primary, table.row(primary)));
        }

        return results;
    }

    public E findFirstByProperty(String fieldName, Object expectedValue) {
        checkFieldIsExist(fieldName);

        if (fieldName.equals(primaryFieldName)) {
            return constructObject((R) expectedValue, table.row((R) expectedValue));
        }

        Map<R, Object> mapValueByPrimaryKey = table.column(fieldName);

        for (R primaryKey : mapValueByPrimaryKey.keySet()) {
            if (isValueEqual(mapValueByPrimaryKey.get(primaryKey), expectedValue)) {
                return constructObject(primaryKey, table.row(primaryKey));
            }
        }

        return null;
    }

    public List<E> findAllByProperty(String fieldName, Object expectedValue) {
        checkFieldIsExist(fieldName);

        if (fieldName.equals(primaryFieldName)) {
            List<E> results = new ArrayList<>();
            E object = constructObject((R) expectedValue, table.row((R) expectedValue));

            if (object != null) {
                results.add(object);
            }

            return results;
        }

        return findAllBySlaveProperty(fieldName, expectedValue);
    }

    private List<E> findAllBySlaveProperty(String fieldName, Object expectedValue) {
        Map<R, Object> mapValueByPrimaryKey = table.column(fieldName);
        List<E> results = new ArrayList<>();
        E object;

        for (R primaryKey : mapValueByPrimaryKey.keySet()) {
            if (isValueEqual(mapValueByPrimaryKey.get(primaryKey), expectedValue)) {
                object = constructObject(primaryKey, table.row(primaryKey));

                if (object != null) {
                    results.add(object);
                }
            }
        }

        return results;
    }

    public Object findSiblingValueByProperty(String fieldName, Object expectedValue, String siblingFieldName) {
        checkFieldIsExist(fieldName);
        checkFieldIsExist(siblingFieldName);

        if (fieldName.equals(primaryFieldName)) {
            if (siblingFieldName.equals(primaryFieldName)) {
                return expectedValue;
            }

            return table.get(expectedValue, siblingFieldName);
        }

        return findSiblingValueBySlaveProperty(fieldName, expectedValue, siblingFieldName);
    }

    private Object findSiblingValueBySlaveProperty(String fieldName, Object expectedValue, String siblingFieldName) {
        Map<R, Object> mapValueByPrimaryKey = table.column(fieldName);

        for (R primaryKey : mapValueByPrimaryKey.keySet()) {
            if (isValueEqual(mapValueByPrimaryKey.get(primaryKey), expectedValue)) {
                return table.get(primaryKey, siblingFieldName);
            }
        }

        return null;
    }

    public List<Object> findAllPropertyValues(String fieldName) {
        checkFieldIsExist(fieldName);

        if (!fieldName.equals(primaryFieldName)) {
            return new ArrayList<>(table.column(fieldName).values());
        }

        return (List<Object>) new ArrayList<>(table.rowKeySet());
    }

    public boolean existByProperty(String fieldName, Object expectedValue) {
        checkFieldIsExist(fieldName);

        if (fieldName.equals(primaryFieldName)) {
            return table.rowKeySet().contains(expectedValue);

        } else {
            Map<R, Object> mapValueByPrimaryKey = table.column(fieldName);

            for (R primaryKey : mapValueByPrimaryKey.keySet()) {
                if (isValueEqual(mapValueByPrimaryKey.get(primaryKey), expectedValue)) {
                    return true;
                }
            }
        }


        return false;
    }

    public int size() {
        return table.rowKeySet().size();
    }

    public void clear() {
        table.clear();
    }

    public void setMutatorNamePrefix(String[] prefix) {
        this.ACCEPTED_MUTATOR_METHODS_PREFIX = prefix;
    }

    private E constructObject(R primaryKey, Map<String, Object> propertyValues) {
        if (propertyValues.size() == 0) {
            return null;
        }

        try {
            E output = (E) constructor.newInstance();
            String methodName;
            String propertyName;

            for (int i = 0; i < mutatorMethods.size(); i++) {
                methodName = mutatorMethods.get(i).getName();
                int lenMutatorPrefix = calMutatorPrefixLen(methodName);

                propertyName = methodName.substring(lenMutatorPrefix).toLowerCase();
                if (propertyName.equals(primaryFieldName)) {
                    mutatorMethods.get(i).invoke(output, primaryKey);
                } else {
                    mutatorMethods.get(i).invoke(output, propertyValues.get(propertyName));
                }
            }

            return output;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private int calMutatorPrefixLen(String methodName) {
        String prefix;

        for (int i = 0; i < ACCEPTED_MUTATOR_METHODS_PREFIX.length; i++) {
            prefix = ACCEPTED_MUTATOR_METHODS_PREFIX[i];

            if (methodName.startsWith(prefix) && methodName.length() > prefix.length()) {
                return prefix.length();
            }
        }

        return -1;
    }

    private void checkFieldIsExist(String fieldName) {
        if (!mapFieldByName.containsKey(fieldName)) {
            throw new NoSuchPropertyException("No such property called " + fieldName);
        }
    }

    private boolean isValueEqual(Object o1, Object o2) {
        return String.valueOf(o1).equals(String.valueOf(o2));
    }
}
