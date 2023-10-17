package com.github.sudarshan.categoryManager.core.sp;


import com.github.sudarshan.categoryManager.core.pojo.*;
import com.github.sudarshan.categoryManager.core.spi.*;
import lombok.Getter;
import lombok.Setter;

import java.sql.*;
import java.util.*;
import java.util.function.BiFunction;

import static com.github.sudarshan.categoryManager.core.pojo.CoreConstants.EXPORT_CATEGORY_PATH_SQL;
import static com.github.sudarshan.categoryManager.core.pojo.CoreConstants.EXPORT_CATEGORY_SQL;

@Getter
@Setter
public class DefaultDbExportImpl implements Export<String, Node> {

    private final HashMap<String, Node> linkedData;
    private final HashMap<String, Node> unlinkedData;
    private String exportAllCategoryQuery;
    private String exportAllCategoryPathsQuery;
    private String exportCategoryPsQuery;
    private String exportCategoryPathsPsQuery;
    private BiFunction<PreparedStatement, ICategoryExportData, PreparedStatement> exportCategoryPsMapper;
    private BiFunction<PreparedStatement, ICategoryPathExportData, PreparedStatement> exportCategoryPathPsMapper;
    private Connection connection;

    public DefaultDbExportImpl(Data<String, Node> data) {
        this.linkedData = data.getLinkedData();
        this.unlinkedData = data.getUnlinkedData();
    }
    public DefaultDbExportImpl configure(Connection connection,
                                        String exportAllCategoryQuery,
                                        String exportAllCategoryPathsQuery,
                                        String exportCategoryPsQuery,
                                        String exportCategoryPathsPsQuery,
                                         BiFunction<PreparedStatement, ICategoryExportData, PreparedStatement> exportCategoryPsMapper,
                                         BiFunction<PreparedStatement, ICategoryPathExportData, PreparedStatement> exportCategoryPathPsMapper) {

        this.exportAllCategoryQuery = exportAllCategoryQuery;
        this.exportAllCategoryPathsQuery = exportAllCategoryPathsQuery;
        this.exportCategoryPsQuery = exportCategoryPsQuery;
        this.exportCategoryPathsPsQuery = exportCategoryPathsPsQuery;
        this.exportCategoryPsMapper = exportCategoryPsMapper;
        this.exportCategoryPathPsMapper = exportCategoryPathPsMapper;
        this.connection = connection;
        return this;
    }

    private final Categories exportCategoryIntoDb(String categoryId) {
        HashMap<String, Node> allCategories = new HashMap<>();
        allCategories.putAll(linkedData);
        allCategories.putAll(unlinkedData);
        Categories categories = new Categories();
        try(PreparedStatement ps = connection.prepareStatement(this.exportCategoryPsQuery)) {
            Node node = allCategories.get(categoryId);
            DefaultCategoryExportData categoryExportData = new DefaultCategoryExportData(categoryId, node);
            this.exportCategoryPsMapper.apply(ps, categoryExportData);
            boolean result = ps.execute();
            System.out.println(result);
            if(!allCategories.containsKey(categoryId))
                return categories;
            categories.getCategoryList().add(allCategories.get(categoryId));
            return categories;
        } catch(SQLException sqlException ) {
            System.err.println(sqlException.getMessage());
        }
        return categories;
    }
    private final Categories exportAllCategoriesIntoDb() {
        HashMap<String, Node> allCategories = new HashMap<>();
        allCategories.putAll(linkedData);
        allCategories.putAll(unlinkedData);

        Categories categories = new Categories();
        categories.getCategoryList().addAll(allCategories.values());

        try(PreparedStatement ps = connection.prepareStatement(this.exportAllCategoryQuery)) {
            for(Map.Entry<String, Node> entry: allCategories.entrySet()) {
                String categoryId = entry.getKey();
                Node node = allCategories.get(categoryId);
                DefaultCategoryExportData categoryExportData = new DefaultCategoryExportData(categoryId, node);
                this.exportCategoryPsMapper.apply(ps, categoryExportData);
                ps.addBatch();
            }
            int[] result = ps.executeBatch();
            System.out.println("batch exported " + result.length + " categories");
        } catch(SQLException sqlException ) {
            System.err.println(sqlException.getMessage());
        }
        return categories;
    }

    private final CategoriesPaths exportAncestorPathsForCategoryIntoDb(String categoryId) {
        HashMap<String, Node> allCategories = new HashMap<>();
        allCategories.putAll(linkedData);
        allCategories.putAll(unlinkedData);
        CategoriesPaths categoriesPaths = new CategoriesPaths();
        var ancestorPaths = Utility.generateAncestorPaths(categoryId, allCategories);
        try(var ps = connection.prepareStatement(this.exportCategoryPathsPsQuery)) {
            DefaultCategoryPathExportData categoryPathExportData = new DefaultCategoryPathExportData(categoryId, ancestorPaths);
            this.exportCategoryPathPsMapper.apply(ps, categoryPathExportData);
            var rs = ps.executeQuery();
            PathResponse pathResponse = new PathResponse();
            pathResponse.setCategoryId(categoryId);
            pathResponse.getAncestorPaths().addAll(ancestorPaths);
            categoriesPaths.getCategoriesPaths().add(pathResponse);
            return categoriesPaths;
        } catch(SQLException sqlException ) {
            System.err.println(sqlException.getMessage());
        }
        return categoriesPaths;
    }
    private final CategoriesPaths exportAllCategoryAncestorPathsIntoDb() {
        // for each node in linked and unlinked, find its ancestor path
        // O(N.d) time | O(N+d) space
        CategoriesPaths categoriesPaths = new CategoriesPaths();
        Map<String, List<String>> ancestorPathsMapping = new HashMap<>();
        HashMap<String, Node> allCategories = new HashMap<>();
        allCategories.putAll(linkedData);
        allCategories.putAll(unlinkedData);
        for(var entry: allCategories.entrySet()) {
            var key = entry.getKey();
            var ancestorPaths = Utility.generateAncestorPaths(key, allCategories);
            ancestorPathsMapping.put(key, ancestorPaths);

            PathResponse pathResponse = new PathResponse();
            pathResponse.setCategoryId(key);
            pathResponse.getAncestorPaths().addAll(ancestorPaths);
            categoriesPaths.getCategoriesPaths().add(pathResponse);

        }
        try(PreparedStatement ps = connection.prepareStatement(this.exportAllCategoryPathsQuery)) {
            for(var entry: ancestorPathsMapping.entrySet()) {
                String categoryId = entry.getKey();
                List<String> ancestorPaths = entry.getValue();
                DefaultCategoryPathExportData categoryPathExportData = new DefaultCategoryPathExportData(categoryId, ancestorPaths);
                this.exportCategoryPathPsMapper.apply(ps, categoryPathExportData);
                ps.addBatch();
            }
            int[] result = ps.executeBatch();
            System.out.println("batch exported paths for " + result.length + " categories");
        } catch(SQLException sqlException ) {
            System.err.println(sqlException.getMessage());
        }
        return categoriesPaths;
    }

    @Override
    public CategoriesPaths exportAllPaths() {
        return this.exportAllCategoryAncestorPathsIntoDb();
    }

    @Override
    public Categories exportAll() {
        return this.exportAllCategoriesIntoDb();
    }

    @Override
    public Categories exportById(String id) {
        return this.exportCategoryIntoDb(id);
    }

    @Override
    public CategoriesPaths exportPathById(String id) {
        return this.exportAncestorPathsForCategoryIntoDb(id);
    }
}
