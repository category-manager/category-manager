package com.github.sudarshan.categoryManager.core.pojos;

public class CoreConstants {
    public static final String HEAD_NODE_ID = "head";
    public static final String UNLINKED_NODE_ID = "unlinked";
    public static final String ANCESTOR_PATH = "ancestorPath";
    public static final String DESCENDANT_PATH = "descendantPath";

    public static final String EXPORT_CATEGORY_PATH_SQL = "INSERT INTO CATEGORY_PATH(cat_id,paths,created_at,modified_at)" +
            " VALUES (?, ?, ?, ?) ON CONFLICT(cat_id) UPDATE CATEGORY_PATH set " +
            "(paths, modified_at)=(EXCLUDED.paths, EXCLUDED.modified_at) RETURNING *";
    public static final String EXPORT_CATEGORY_SQL = "INSERT INTO CATEGORY(id, data, parent_category_ids, child_category_ids, created_at, modified_at) " +
            "VALUES (?, to_json(?::json), ?, ?, ?, ?) ON CONFLICT(id) UPDATE CATEGORY set (data, parent_category_ids, child_category_ids, modified_at)=" +
            "(to_json(EXCLUDED.data::json), EXCLUDED.parent_category_ids, EXCLUDED.child_category_ids , EXCLUDED.modified_at) RETURNING *";


}
