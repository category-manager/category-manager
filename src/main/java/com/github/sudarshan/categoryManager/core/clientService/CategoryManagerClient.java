package com.github.sudarshan.categoryManager.core.clientService;

import com.github.sudarshan.categoryManager.core.sp.*;
import com.github.sudarshan.categoryManager.core.spi.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.function.BiFunction;
import java.util.function.Function;

public class CategoryManagerClient {
    private DefaultData<String, Node> dataImpl = new DefaultData<>();
    private DefaultDbExportImpl defaultExport;
    private DefaultRestExportImpl defaultRestExport;
    private DefaultImportImpl defaultImport;
    private DefaultRealtimeOperationImpl realtimeOperation;
    private static boolean isAlreadySetup = false;
    private static CategoryManagerClientBuilder builder;

    private CategoryManagerClient() {
    }
    public static CategoryManagerClientBuilder getBuilder() {
        if(isAlreadySetup) {
            return CategoryManagerClient.builder;
        }
        CategoryManagerClient cmc = new CategoryManagerClient();
        builder = new CategoryManagerClientBuilder(cmc);
        isAlreadySetup = true;
        return builder;
    }

    public Import<String, Node> getDbImport() {
        return this.defaultImport;
    }
    public Export<String, Node> getDbExport() {
        return this.defaultExport;
    }
    public Export<String, Node> getRestExport() {
        return this.defaultRestExport;
    }
    public RealtimeOperation<String, Node> getRealtimeOperation() {
        return this.realtimeOperation;
    }
    public static class CategoryManagerClientBuilder {
        private final CategoryManagerClient cmc;
        // IT'S A SINGLETON BUILDER IN THE SINGLETON CLIENT CLASS.
        private CategoryManagerClientBuilder(CategoryManagerClient cmc) {
            this.cmc = cmc;
        }
        public CategoryManagerClientBuilder configureImport(Connection connection, String importQuery, Function<ResultSet, Node> rowMapper) {
            cmc.defaultImport = new DefaultImportImpl(cmc.dataImpl)
                    .configure(connection, importQuery, rowMapper);
            return this;
        }
        public CategoryManagerClientBuilder configureDbExport(Connection connection,
                                                              String exportAllCategoryQuery,
                                                              String exportAllCategoryPathsQuery,
                                                              String exportCategoryPsQuery,
                                                              String exportCategoryPathsPsQuery,
                                                              BiFunction<PreparedStatement, ICategoryExportData, PreparedStatement> exportCategoryPsMapper,
                                                              BiFunction<PreparedStatement, ICategoryPathExportData, PreparedStatement> exportCategoryPathPsMapper) {
            cmc.defaultExport = new DefaultDbExportImpl(cmc.dataImpl)
                    .configure(connection, exportAllCategoryQuery,
                            exportAllCategoryPathsQuery,exportCategoryPsQuery, exportCategoryPathsPsQuery, exportCategoryPsMapper, exportCategoryPathPsMapper);
            return this;
        }

        public CategoryManagerClientBuilder configureRestExport() {
            cmc.defaultRestExport = new DefaultRestExportImpl(cmc.dataImpl);
            return this;
        }

        public CategoryManagerClientBuilder configureRop() {
            cmc.realtimeOperation = new DefaultRealtimeOperationImpl(cmc.dataImpl);
            return this;
        }

        public CategoryManagerClient build() {
            return cmc;
        }
    }
}
