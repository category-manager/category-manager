package com.github.sudarshan.categoryManager.examples;

import com.github.sudarshan.categoryManager.core.clientService.CategoryManagerClient;
import com.github.sudarshan.categoryManager.core.sp.Node;
import com.github.sudarshan.categoryManager.core.spi.*;
import com.github.sudarshan.categoryManager.core.pojo.*;
import org.postgresql.ds.PGSimpleDataSource;

import static com.github.sudarshan.categoryManager.core.clientService.CategoryManagerClient.CategoryManagerClientBuilder;

import java.sql.*;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

public class TestDbData {

    public static void main(String[] args) {
        testDbData();
    }

    private static void testDbData() {
        CategoryManagerClientBuilder builder = CategoryManagerClient.getBuilder();
        Connection connection = getConnection("postgres", "postgres", "postgres");
        try {
            System.out.println(Objects.isNull(connection) ? "connection not established": connection.isClosed());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        // DB-IMPORT CONFIG
        String importSql = "SELECT * FROM CATEGORY";
        Function<ResultSet, Node> rowMapper = getImportRowMapper();
        // DB-EXPORT CONFIG
        String exportAllCategoryQuery = CoreConstants.EXPORT_ALL_CATEGORY_SQL,
                exportAllCategoryPathsQuery = CoreConstants.EXPORT_CATEGORY_ALL_PATH_SQL,
                exportCategoryPsQuery = CoreConstants.EXPORT_CATEGORY_SQL,
                exportCategoryPathsPsQuery = CoreConstants.EXPORT_CATEGORY_PATH_SQL;;

        // BUILDER CONFIGS
        builder.configureImport(connection, importSql, rowMapper);
        builder.configureRop();
        builder.configureRestExport();
        builder.configureDbExport(connection, exportAllCategoryQuery,
                exportAllCategoryPathsQuery,
                exportCategoryPsQuery,
                exportCategoryPathsPsQuery,
                getExportCategoryPsMapper(),
                getExportCategoryPathPsMapper() );
        CategoryManagerClient client  = builder.build();

        // GET OPERATIONS FROM CLIENT
        RealtimeOperation<String, Node> ropClient = client.getRealtimeOperation();
        Import<String, Node> dbImportClient = client.getDbImport();
        Export<String, Node> restExportClient = client.getRestExport();
        Export<String, Node> dbExportClient = client.getDbExport();

        // PERFORM TEST OPERATIONS
        dbImportClient.importData();
        CategoriesPaths paths = (CategoriesPaths) restExportClient.exportPathById("cat4220483");
        Categories cats = (Categories) restExportClient.exportById(CoreConstants.HEAD_NODE_ID);
        CategoriesPaths exportedDbPaths = (CategoriesPaths) dbExportClient.exportAllPaths();

        // VERIFY WITH CONSOLE OUTPUT
        cats.getCategoryList().forEach(cat -> {
            System.out.println("children size = " + cat.getChildren().size());
        });
        paths.getCategoriesPaths().forEach(p -> {
            System.out.println(p.getCategoryId() + "- ancestorPaths => " + p.getAncestorPaths());
            System.out.println(p.getCategoryId() + "- descendantPaths => " + p.getDescendantPaths().size());
        });

        System.out.println("Exported paths for " + exportedDbPaths.getCategoriesPaths().size() + " categories");
    }

    private static BiFunction<PreparedStatement, ICategoryPathExportData, PreparedStatement> getExportCategoryPathPsMapper() {

        return (ps, data) -> {
            try {
                Connection connection = ps.getConnection();
                DefaultCategoryPathExportData d = (DefaultCategoryPathExportData)data;
                String categoryId = d.getCategoryId();
                List<String> ancestorPaths = d.getAncestorPaths();
                ps.setString(1, categoryId);
                ps.setArray(2, connection.createArrayOf("text", ancestorPaths.toArray()));
                ps.setTimestamp(3, new Timestamp(ZonedDateTime.now().toInstant().toEpochMilli()), Calendar.getInstance(TimeZone.getTimeZone("UTC")));
                ps.setTimestamp(4, new Timestamp(ZonedDateTime.now().toInstant().toEpochMilli()), Calendar.getInstance(TimeZone.getTimeZone("UTC")));

            } catch(SQLException sqlException) {
                System.err.println(sqlException.getMessage());
            }
            return ps;
        } ;
    }

    private static BiFunction<PreparedStatement, ICategoryExportData, PreparedStatement> getExportCategoryPsMapper() {
        return (ps, data) -> {
            try {
                var d = (DefaultCategoryExportData)data;
                String categoryId = d.getCategoryId();
                Node node = d.getNode();
                Connection connection = ps.getConnection();
                ps.setString(1, categoryId);
                ps.setString(2, node.getData().toString());
                ps.setArray(3, connection.createArrayOf("text",node.getParents().toArray()));
                ps.setArray(4, connection.createArrayOf("text",node.getChildren().toArray()));
                ps.setTimestamp(5, new Timestamp(ZonedDateTime.now().toInstant().toEpochMilli()), Calendar.getInstance(TimeZone.getTimeZone("UTC")));
                ps.setTimestamp(6, new Timestamp(ZonedDateTime.now().toInstant().toEpochMilli()), Calendar.getInstance(TimeZone.getTimeZone("UTC")));

            } catch (SQLException sqlException) {
                System.err.println(sqlException.getMessage());
            }
            return ps;
        } ;
    }

    private static Function<ResultSet, Node> getImportRowMapper() {
        return (rs) -> {
            Node node = new Node();
            try {
                String id = rs.getString("id");
//                JsonNode data = rs.getObject("data", JsonNode.class);
                String[] parentCategoryIds = (String[]) rs.getArray("parent_category_ids").getArray();
                if(Objects.isNull(parentCategoryIds)) {
                    parentCategoryIds = new String[0];
                    System.out.println("found null as parent for " + id);
                }
                node.set_id(id);
                node.setChildren(new HashSet<>());
                node.setParents(new HashSet<>(Arrays.asList(parentCategoryIds)));
//                node.setData(data);
                return node;
            } catch (SQLException e) {
//                throw new RuntimeException(e);
                System.err.println(e.getMessage());
            }
            return node;
        };
    }
    private static Connection getConnection(final String db, final String user, final String password) {
        PGSimpleDataSource dataSource = new PGSimpleDataSource();
        try {
            dataSource.setDatabaseName(db);
            dataSource.setUser(user);
            dataSource.setPassword(password);
            dataSource.setURL("jdbc:postgresql://localhost:5432/");
            return dataSource.getConnection();
        } catch(SQLException sqlException) {
            System.err.println(sqlException.getMessage());
        }
        return null;
    }
}
