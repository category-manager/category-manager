package com.github.sudarshan.categoryManager.core.impls;


import com.github.sudarshan.categoryManager.core.interfaces.Data;

import java.util.HashMap;

@lombok.Data
public class DefaultData<K extends String, V extends Node> implements Data<K, V> {
    private HashMap<K, V> linkedData = new HashMap<>();
    private HashMap<K, V> unlinkedData = new HashMap<>();
    private HashMap<K, V> rootData = new HashMap<>();

    @Override
    public HashMap<K, V> getLinkedData() {
        return linkedData;
    }

    @Override
    public HashMap<K, V> getUnlinkedData() {
        return unlinkedData;
    }

    @Override
    public HashMap<K, V> getRootData() {
        return rootData;
    }

}
