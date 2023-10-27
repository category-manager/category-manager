package com.github.sudarshan.categoryManager.core.sp;

import com.github.sudarshan.categoryManager.core.spi.Data;
import com.github.sudarshan.categoryManager.core.pojo.Categories;
import com.github.sudarshan.categoryManager.core.pojo.CategoriesPaths;
import com.github.sudarshan.categoryManager.core.pojo.PathResponse;
import com.github.sudarshan.categoryManager.core.spi.Export;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

public class DefaultRestExportImpl implements Export<String, Node> {
    private Logger log = LoggerFactory.getLogger(DefaultRestExportImpl.class);

    private HashMap<String, Node> linkedData;
    private HashMap<String, Node> unlinkedData;
    public DefaultRestExportImpl(Data<String, Node> data) {
        this.linkedData = data.getLinkedData();
        this.unlinkedData = data.getUnlinkedData();
    }
    public Categories exportAllCategoriesAsJson() {
        HashMap<String, Node> allCategories = new HashMap<>();
        allCategories.putAll(linkedData);
        allCategories.putAll(unlinkedData);
        Categories categories = new Categories();
        categories.getCategoryList().addAll(allCategories.values());
        return categories;
    }

    public CategoriesPaths exportAllCategoryAncestorPathsAsJson() {
        CategoriesPaths categoriesPaths = new CategoriesPaths();
        HashMap<String, Node> allCategories = new HashMap<>();
        allCategories.putAll(linkedData);
        allCategories.putAll(unlinkedData);
        for(var entry: allCategories.entrySet()) {
            PathResponse pathResponse = new PathResponse();
            var key = entry.getKey();
            var ancestorPaths = Utility.generateAncestorPaths(key, allCategories);
            pathResponse.setCategoryId(key);
            pathResponse.getAncestorPaths().addAll(ancestorPaths);
            categoriesPaths.getCategoriesPaths().add(pathResponse);
        }
        return categoriesPaths;
    }

    public Categories exportCategoryAsJson(String categoryId) {
        HashMap<String, Node> allCategories = new HashMap<>();
        allCategories.putAll(linkedData);
        allCategories.putAll(unlinkedData);
        Categories categories = new Categories();
        if(!allCategories.containsKey(categoryId))
            return categories;
        categories.getCategoryList().add(allCategories.get(categoryId));
        return categories;
    }

    public CategoriesPaths exportPathsForCategoryAsJson(String categoryId) {
        CategoriesPaths categoriesPaths = new CategoriesPaths();
        HashMap<String, Node> allCategories = new HashMap<>();
        allCategories.putAll(linkedData);
        allCategories.putAll(unlinkedData);
        if(!allCategories.containsKey(categoryId))
            return categoriesPaths;
        PathResponse pathResponse = new PathResponse();
        var ancestorPaths = Utility.generateAncestorPaths(categoryId, allCategories);
        var descendantPaths = Utility.generateDescendantPaths(categoryId, allCategories);
        pathResponse.setCategoryId(categoryId);
        pathResponse.getAncestorPaths().addAll(ancestorPaths);
        pathResponse.getDescendantPaths().addAll(descendantPaths);
        categoriesPaths.getCategoriesPaths().add(pathResponse);
        return categoriesPaths;
    }

    @Override
    public CategoriesPaths exportAllPaths() {
        return this.exportAllCategoryAncestorPathsAsJson();
    }

    @Override
    public Categories exportAll() {
        return this.exportAllCategoriesAsJson();
    }

    @Override
    public Categories exportById(String id) {
        return this.exportCategoryAsJson(id);
    }

    @Override
    public CategoriesPaths exportPathById(String id) {
        return this.exportPathsForCategoryAsJson(id);
    }
}
