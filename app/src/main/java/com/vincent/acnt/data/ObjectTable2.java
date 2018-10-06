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

/**
 *  K: primary key type
 *  T: tuple type
 */
public class ObjectTable2<K, T> {
    private Table<K, String, Object> table;
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

    public void add(T object) {
        try {
            Field primaryField = mapFieldByName.get(primaryFieldName);
            K primaryKey = (K) primaryField.get(object);
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

    public void remove(K rowKey) {
        if (!table.rowKeySet().contains(rowKey)) {
            throw new NoSuchElementException("There is not tuple which row key is " + String.valueOf(rowKey));
        }

        for (int i = 0, len = fieldNames.length; i < len; i++) {
            table.remove(rowKey, fieldNames[i]);
        }
    }

    public List<T> findAll() {
        List<T> results = new ArrayList<>();

        for (K primary : table.rowKeySet()) {
            results.add(constructObject(primary, table.row(primary)));
        }

        return results;
    }

    public T findFirstByProperty(String fieldName, Object expectedValue) {
        checkFieldIsExist(fieldName);

        if (fieldName.equals(primaryFieldName)) {
            return constructObject((K) expectedValue, table.row((K) expectedValue));
        }

        Map<K, Object> mapValueByPrimaryKey = table.column(fieldName);

        for (K primaryKey : mapValueByPrimaryKey.keySet()) {
            if (isValueEqual(mapValueByPrimaryKey.get(primaryKey), expectedValue)) {
                return constructObject(primaryKey, table.row(primaryKey));
            }
        }

        return null;
    }

    public List<T> findAllByProperty(String fieldName, Object expectedValue) {
        checkFieldIsExist(fieldName);

        if (fieldName.equals(primaryFieldName)) {
            List<T> results = new ArrayList<>();
            T object = constructObject((K) expectedValue, table.row((K) expectedValue));

            if (object != null) {
                results.add(object);
            }

            return results;
        }

        return findAllBySlaveProperty(fieldName, expectedValue);
    }

    private List<T> findAllBySlaveProperty(String fieldName, Object expectedValue) {
        Map<K, Object> mapValueByPrimaryKey = table.column(fieldName);
        List<T> results = new ArrayList<>();
        T object;

        for (K primaryKey : mapValueByPrimaryKey.keySet()) {
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
        Map<K, Object> mapValueByPrimaryKey = table.column(fieldName);

        for (K primaryKey : mapValueByPrimaryKey.keySet()) {
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
            Map<K, Object> mapValueByPrimaryKey = table.column(fieldName);

            for (K primaryKey : mapValueByPrimaryKey.keySet()) {
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

    private T constructObject(K primaryKey, Map<String, Object> propertyValues) {
        if (propertyValues.size() == 0) {
            return null;
        }

        try {
            T output = (T) constructor.newInstance();
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
