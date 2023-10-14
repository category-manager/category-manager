package com.github.sudarshan.categoryManager.core.impls;


import com.github.sudarshan.categoryManager.core.interfaces.Data;
import com.github.sudarshan.categoryManager.core.interfaces.Export;
import com.github.sudarshan.categoryManager.core.pojos.Categories;
import com.github.sudarshan.categoryManager.core.pojos.CategoriesPaths;
import com.github.sudarshan.categoryManager.core.pojos.PathResponse;
import com.github.sudarshan.categoryManager.core.pojos.Utility;
import lombok.Getter;
import lombok.Setter;

import java.sql.*;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.Function;

import static com.github.sudarshan.categoryManager.core.pojos.CoreConstants.EXPORT_CATEGORY_PATH_SQL;
import static com.github.sudarshan.categoryManager.core.pojos.CoreConstants.EXPORT_CATEGORY_SQL;

public class DefaultDbExportImpl implements Export<String, Node> {
    private final HashMap<String, Node> linkedData;
    private final HashMap<String, Node> unlinkedData;
    @Getter
    @Setter
    private String exportAllCategoryQuery;
    @Getter
    @Setter
    private String exportAllCategoryPathsQuery;
    @Getter
    @Setter
    private String exportCategoryPsQuery;
    @Getter
    @Setter
    private String exportCategoryPathsPsQuery;
    @Getter
    @Setter
    private Function<PreparedStatement, PreparedStatement> exportCategoryPsMapper;
    @Getter
    @Setter
    private Function<PreparedStatement, PreparedStatement> exportCategoryPathPsMapper;
    @Getter
    @Setter
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
                                        Function<PreparedStatement, PreparedStatement> exportCategoryPsMapper,
                                        Function<PreparedStatement, PreparedStatement> exportCategoryPathPsMapper) {

        this.exportAllCategoryQuery = exportAllCategoryQuery;
        this.exportAllCategoryPathsQuery = exportAllCategoryPathsQuery;
        this.exportCategoryPsQuery = exportCategoryPsQuery;
        this.exportCategoryPathsPsQuery = exportCategoryPathsPsQuery;
        this.exportCategoryPsMapper = exportCategoryPsMapper;
        this.exportCategoryPathPsMapper = exportCategoryPathPsMapper;
        this.connection = connection;
        return this;
    }

    private final Categories exportAllCategoriesIntoDb() {
        HashMap<String, Node> allCategories = new HashMap<>();
        allCategories.putAll(linkedData);
        allCategories.putAll(unlinkedData);

        Categories categories = new Categories();
        categories.getCategoryList().addAll(allCategories.values());

        try(PreparedStatement ps = connection.prepareStatement(EXPORT_CATEGORY_SQL)) {
            for(Map.Entry<String, Node> entry: allCategories.entrySet()) {
                String categoryId = entry.getKey();
                Node node =allCategories.get(categoryId);
                ps.setString(1, categoryId);
                ps.setString(2, node.getData().toString());
                ps.setArray(3, connection.createArrayOf("text",node.getParents().toArray()));
                ps.setArray(4, connection.createArrayOf("text",node.getChildren().toArray()));
                ps.setTimestamp(5, new Timestamp(ZonedDateTime.now().toInstant().toEpochMilli()), Calendar.getInstance(TimeZone.getTimeZone("UTC")));
                ps.setTimestamp(6, new Timestamp(ZonedDateTime.now().toInstant().toEpochMilli()), Calendar.getInstance(TimeZone.getTimeZone("UTC")));
                ps.addBatch();
            }
            int[] result = ps.executeBatch();
            System.out.println(Arrays.toString(result));
        } catch(SQLException sqlException ) {
            System.err.println(sqlException.getMessage());
        }
        return categories;
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
        try(PreparedStatement ps = connection.prepareStatement(EXPORT_CATEGORY_PATH_SQL)) {
            for(var entry: ancestorPathsMapping.entrySet()) {
                String categoryId = entry.getKey();
                List<String> ancestorPaths = entry.getValue();

                ps.setString(1, categoryId);
                ps.setArray(2, connection.createArrayOf("text", ancestorPaths.toArray()));
                ps.setTimestamp(3, new Timestamp(ZonedDateTime.now().toInstant().toEpochMilli()), Calendar.getInstance(TimeZone.getTimeZone("UTC")));
                ps.setTimestamp(4, new Timestamp(ZonedDateTime.now().toInstant().toEpochMilli()), Calendar.getInstance(TimeZone.getTimeZone("UTC")));
                ps.addBatch();
            }
            int[] result = ps.executeBatch();
            System.out.println(Arrays.toString(result));
        } catch(SQLException sqlException ) {
            System.err.println(sqlException.getMessage());
        }
        return categoriesPaths;
    }

    private final Categories exportCategoryIntoDb(String categoryId) {
        HashMap<String, Node> allCategories = new HashMap<>();
        allCategories.putAll(linkedData);
        allCategories.putAll(unlinkedData);
        Categories categories = new Categories();
        try(PreparedStatement ps = connection.prepareStatement(EXPORT_CATEGORY_SQL)) {
            Node node = allCategories.get(categoryId);
            ps.setString(1, categoryId);
            ps.setString(2, node.getData().toString());
            ps.setArray(3, connection.createArrayOf("text",node.getParents().toArray()));
            ps.setArray(4, connection.createArrayOf("text",node.getChildren().toArray()));
            ps.setTimestamp(5,new Timestamp(ZonedDateTime.now().toInstant().toEpochMilli()), Calendar.getInstance(TimeZone.getTimeZone("UTC")));
            ps.setTimestamp(6, new Timestamp(ZonedDateTime.now().toInstant().toEpochMilli()), Calendar.getInstance(TimeZone.getTimeZone("UTC")));
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

    private final CategoriesPaths exportPathsForCategoryIntoDb(String categoryId) {
        HashMap<String, Node> allCategories = new HashMap<>();
        allCategories.putAll(linkedData);
        allCategories.putAll(unlinkedData);
        CategoriesPaths categoriesPaths = new CategoriesPaths();
        var ancestorPaths = Utility.generateAncestorPaths(categoryId, allCategories);
        try(var ps = connection.prepareStatement(EXPORT_CATEGORY_PATH_SQL)) {
            ps.setString(1, categoryId);
            ps.setArray(2, connection.createArrayOf("text", ancestorPaths.toArray()));
            ps.setTimestamp(3, new Timestamp(ZonedDateTime.now().toInstant().toEpochMilli()), Calendar.getInstance(TimeZone.getTimeZone("UTC")));
            ps.setTimestamp(4, new Timestamp(ZonedDateTime.now().toInstant().toEpochMilli()), Calendar.getInstance(TimeZone.getTimeZone("UTC")));
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

    @Override
    public <Val> Val exportAllPaths() {
        return (Val) this.exportAllCategoryAncestorPathsIntoDb();
    }

    @Override
    public <Val> Val exportAll() {
        return (Val) this.exportAllCategoriesIntoDb();
    }

    @Override
    public <Val> Val exportById(String id) {
        return (Val) this.exportCategoryIntoDb(id);
    }

    @Override
    public <Val> Val exportPathById(String id) {
        return (Val) this.exportPathsForCategoryIntoDb(id);
    }
}
