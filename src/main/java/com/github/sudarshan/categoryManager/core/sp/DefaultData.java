package com.github.sudarshan.categoryManager.core.sp;


import com.github.sudarshan.categoryManager.core.pojo.CoreConstants;
import com.github.sudarshan.categoryManager.core.spi.Data;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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
