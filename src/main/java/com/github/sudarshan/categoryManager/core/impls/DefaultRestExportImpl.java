package com.github.sudarshan.categoryManager.core.impls;

import com.github.sudarshan.categoryManager.core.interfaces.Data;
import com.github.sudarshan.categoryManager.core.pojos.Categories;
import com.github.sudarshan.categoryManager.core.pojos.CategoriesPaths;
import com.github.sudarshan.categoryManager.core.pojos.PathResponse;
import com.github.sudarshan.categoryManager.core.pojos.Utility;
import com.github.sudarshan.categoryManager.core.interfaces.Export;

import java.util.HashMap;

public class DefaultRestExportImpl implements Export<String, Node> {

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
        pathResponse.setCategoryId(categoryId);
        pathResponse.getAncestorPaths().addAll(ancestorPaths);
        categoriesPaths.getCategoriesPaths().add(pathResponse);
        return categoriesPaths;
    }

    @Override
    public <Val> Val exportAllPaths() {
        return null;
    }

    @Override
    public <Val> Val exportAll() {
        return null;
    }

    @Override
    public <Val> Val exportById(String id) {
        return null;
    }

    @Override
    public <Val> Val exportPathById(String id) {
        return null;
    }
}
