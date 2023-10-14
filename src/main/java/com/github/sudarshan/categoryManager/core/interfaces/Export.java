package com.github.sudarshan.categoryManager.core.interfaces;

public interface Export<K, V> {
    public <Val> Val exportAllPaths();
    public <Val> Val exportAll();
    public <Val> Val exportById(K id);
    public <Val> Val exportPathById(K id);
}
