package com.github.sudarshan.categoryManager.core.spi;

public interface Export<K, V> {
    public ICategoriesPaths exportAllPaths();
    public ICategories exportAll();
    public ICategories  exportById(K id);
    public ICategoriesPaths exportPathById(K id);
}
