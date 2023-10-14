package com.github.sudarshan.categoryManager.core.impls;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.sudarshan.categoryManager.core.interfaces.Data;
import com.github.sudarshan.categoryManager.core.interfaces.Import;
import com.github.sudarshan.categoryManager.core.pojos.CoreConstants;
import lombok.Getter;
import lombok.Setter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.function.Function;

/*
 Design :
    This Class is designed to import data from Category table (refer @Entity category), where every node has a
    information about parents and not children.
    add() and update() are helper methods for build(..) method.
    add will only insert new nodes into linked or unlinkedData or root.
    update will update the currentNode and its corresponding parents and children

*/
public class DefaultImportImpl implements Import<String, Node> {
    private Data<String, Node> data;
    private HashMap<String, Node> linkedData;
    private HashMap<String, Node> unlinkedData;
    private HashMap<String, Node> roots;

    @Getter
    @Setter
    private String importQuery;
    @Getter
    @Setter
    private Function<ResultSet, Node> rowMapper;
    @Getter
    @Setter
    private Connection connection;
    private final Node headNode = new Node();
    public DefaultImportImpl(Data<String, Node> data) {
        this.data = data;
        this.linkedData = data.getLinkedData();
        this.unlinkedData = data.getUnlinkedData();
        this.roots = data.getRootData();
        this.headNode.set_id(CoreConstants.HEAD_NODE_ID);
        this.linkedData.putIfAbsent(CoreConstants.HEAD_NODE_ID, this.headNode);
    }
    public DefaultImportImpl configure(Connection connection, String importQuery, Function<ResultSet, Node> importRowMapper){
        this.connection = connection;
        this.importQuery = importQuery;
        this.rowMapper = importRowMapper;
        return this;
    }
    private void build(HashMap<String, Node> map) {
        for (String key: map.keySet()) {
            Node currentNode = map.get(key);
            currentNode.getParents().forEach(parent -> {
                if(key.equals(parent)){
                    currentNode.getParents().clear();
                    currentNode.getParents().add(headNode.get_id());
                    headNode.getChildren().add(key);
                }
                map.get(parent).getChildren().add(currentNode.get_id());
            });
        }
        for (String key: map.keySet()) {
            Node currentNode = map.get(key);
            if(currentNode.getParents().isEmpty()) {
                this.unlinkedData.put(currentNode.get_id(), currentNode);
                continue;
            }
            if(currentNode.getParents().size() == 1 && key.equals(CoreConstants.HEAD_NODE_ID))
                this.roots.put(key, currentNode);
            this.linkedData.put(key, currentNode);
        }
    }

    @Override
    public void importData() {
        HashMap<String, Node> raw = new HashMap<>();
        try(PreparedStatement ps = connection.prepareStatement(importQuery)) {
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                String id = rs.getString("id");
                rowMapper = this.rowMapper(); // todo: remove this line, and local obj impl
                Node node = rowMapper.apply(rs);
                raw.put(id, node);
            }
        } catch(SQLException sqlException) {
            System.err.println(sqlException.getMessage());
        }
        build(raw);
    }
    private Function<ResultSet, Node> rowMapper() {
        return (rs) -> {
            try {
                String id = rs.getString("id");
                JsonNode data = rs.getObject("data", JsonNode.class);
                String[] parentCategoryIds = (String[]) rs.getArray("parent_category_ids").getArray();
                Node node = new Node();
                node.set_id(id);
                node.setChildren(new HashSet<>());
                node.setParents(new HashSet<>(Arrays.asList(parentCategoryIds)));
                node.setData(data);
                return node;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        };
    }
}
