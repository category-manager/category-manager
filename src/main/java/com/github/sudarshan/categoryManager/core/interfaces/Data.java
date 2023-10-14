package com.github.sudarshan.categoryManager.core.interfaces;

import java.util.HashMap;

public interface Data<Key,Val> {
    public HashMap<Key,Val> getLinkedData();
    public HashMap<Key,Val> getUnlinkedData();
    public HashMap<Key,Val> getRootData();
}
