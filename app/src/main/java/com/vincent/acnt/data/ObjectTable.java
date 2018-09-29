package com.vincent.acnt.data;

import android.util.NoSuchPropertyException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ObjectTable<E> {
    private Object[][] objects;

    private Field[] fields;
    private List<Method> mutatorMethods;
    private Constructor constructor;
    private Map<String, Integer> mapFieldIndexByName;

    private int size;

    private final String[] ACCEPTED_MUTATOR_METHODS_PREFIX = {"set", "define"};
    private static final int DEFAULT_CAPACITY = 16;

    public ObjectTable(Class clz, int initialCapacity) throws NoSuchMethodException {
        this.fields = clz.getDeclaredFields();
        this.objects = new Object[initialCapacity][fields.length];
        this.constructor = clz.getConstructor();
        this.mapFieldIndexByName = new HashMap<>();

        prepareUtils(clz);
    }

    public ObjectTable(Class clz) throws NoSuchMethodException {
        this(clz, DEFAULT_CAPACITY);
    }

    public void add(E object) throws IllegalAccessException {
        if (size >= capacity()) {
            expandCapacity();
        }

        int fieldIndex;
        for (int i = 0;  i < fields.length; i++) {
            fieldIndex = mapFieldIndexByName.get(fields[i].getName().toLowerCase());
            objects[size][fieldIndex] = fields[i].get(object);
        }

        size++;
    }

    public void remove(int index) {
        if (index >= size) {
            throw new IndexOutOfBoundsException(String.format("size: %d, index: %d", size, index));
        }

        System.arraycopy(objects, index + 1, objects, index, size - 1 - index);
        size--;
    }

    public E findFirstByProperty(String fieldName, Object expectedValue) throws IllegalAccessException, InvocationTargetException, InstantiationException {
        if (!mapFieldIndexByName.containsKey(fieldName)) {
            throw new NoSuchPropertyException("No such property called " + fieldName);
        }

        int fieldIndex = mapFieldIndexByName.get(fieldName);

        for (int i = 0; i < size; i++) {
            if (String.valueOf(objects[i][fieldIndex]).equals(String.valueOf(expectedValue))) {
                return constructObject(objects[i]);
            }
        }

        return null;
    }

    public List<E> findAllByProperty(String fieldName, Object expectedValue) throws IllegalAccessException, InstantiationException, InvocationTargetException {
        if (!mapFieldIndexByName.containsKey(fieldName)) {
            throw new NoSuchPropertyException("No such property called " + fieldName);
        }

        List<E> results = new ArrayList<>();
        int fieldIndex = mapFieldIndexByName.get(fieldName);

        for (int i = 0; i < size; i++) {
            if (String.valueOf(objects[i][fieldIndex]).equals(String.valueOf(expectedValue))) {
                results.add(constructObject(objects[i]));
            }
        }

        return results;
    }

    public int capacity() {
        return objects.length;
    }

    public int size() {
        return size;
    }

    public void clear() {
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < fields.length; col++) {
                objects[row][col] = null;
            }
        }
        size = 0;
    }

    private void prepareUtils(Class clz) {
        for (int i = 0; i < fields.length; i++) {
            fields[i].setAccessible(true);
            this.mapFieldIndexByName.put(fields[i].getName().toLowerCase(), i);
        }

        Method[] allMethods = clz.getMethods();
        mutatorMethods = new ArrayList<>();

        for (int i = 0; i < allMethods.length; i++) {
            if (allMethods[i].getName().startsWith("set") || allMethods[i].getName().startsWith("define")) {
                mutatorMethods.add(allMethods[i]);
            }
        }
    }

    private void expandCapacity() {
        Object[][] newObjects = new Object[capacity() << 1][fields.length];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < objects[0].length; j++) {
                newObjects[i][j] = objects[i][j];
            }
        }

        objects = newObjects;
    }

    private E constructObject(Object[] propertyValues) throws IllegalAccessException, InvocationTargetException, InstantiationException {
        E output = (E) constructor.newInstance();
        String methodName;
        int fieldIndex;

        for (int i = 0; i < mutatorMethods.size(); i++) {
            methodName = mutatorMethods.get(i).getName();
            int lenMutatorPrefix = calMutatorPrefixLen(methodName);

            if (lenMutatorPrefix > 0) {
                fieldIndex = mapFieldIndexByName.get(methodName.substring(lenMutatorPrefix).toLowerCase());
                mutatorMethods.get(i).invoke(output, propertyValues[fieldIndex]);
            }
        }

        return output;
    }

    private int calMutatorPrefixLen(String methodName) {
        for (int i = 0; i < ACCEPTED_MUTATOR_METHODS_PREFIX.length; i++) {
            if (methodName.startsWith(ACCEPTED_MUTATOR_METHODS_PREFIX[i])
                    && methodName.length() > ACCEPTED_MUTATOR_METHODS_PREFIX[i].length()) {
                return ACCEPTED_MUTATOR_METHODS_PREFIX[i].length();
            }
        }
        return -1;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < fields.length; col++) {
                sb.append(String.valueOf(objects[row][col])).append("\t");
            }
            sb.append("\n");
        }

        return sb.toString();
    }

}
