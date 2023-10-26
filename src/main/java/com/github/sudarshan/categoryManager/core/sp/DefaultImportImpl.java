package com.github.sudarshan.categoryManager.core.sp;

import com.github.sudarshan.categoryManager.core.spi.Data;
import com.github.sudarshan.categoryManager.core.spi.Import;
import com.github.sudarshan.categoryManager.core.pojo.CoreConstants;
import lombok.Getter;
import lombok.Setter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    private final Node unlinkedNode = new Node();
    public DefaultImportImpl(Data<String, Node> data) {
        this.data = data;
        this.linkedData = data.getLinkedData();
        this.unlinkedData = data.getUnlinkedData();
        this.roots = data.getRootData();

        this.headNode.set_id(CoreConstants.HEAD_NODE_ID);
        this.unlinkedNode.set_id(CoreConstants.UNLINKED_NODE_ID);

        this.linkedData.putIfAbsent(CoreConstants.HEAD_NODE_ID, this.headNode);
        this.unlinkedData.putIfAbsent(CoreConstants.UNLINKED_NODE_ID, this.unlinkedNode);
    }
    public DefaultImportImpl configure(Connection connection, String importQuery, Function<ResultSet, Node> importRowMapper){
        this.connection = connection;
        this.importQuery = importQuery;
        this.rowMapper = importRowMapper;
        return this;
    }
    private Map<String, Set<String>> build(HashMap<String, Node> map) {
        Map<String, Set<String>> missingParents = new HashMap<>();
        for (String key: map.keySet()) {
            Node currentNode = map.get(key);
            // if parents are empty
            if(currentNode.getParents().isEmpty()) {
                currentNode.getParents().add(headNode.get_id());
                this.headNode.getChildren().add(currentNode.get_id());
            } else if(currentNode.getParents().size() == 1) {   // if `head`, `unlinked` are explicitly specified.
                if(currentNode.getParents().contains(CoreConstants.HEAD_NODE_ID))
                    this.headNode.getChildren().add(key);
                else if(currentNode.getParents().contains(CoreConstants.UNLINKED_NODE_ID))
                    this.unlinkedNode.getChildren().add(key);
            }
            currentNode.getParents().forEach(parent -> {
                if(key.equals(parent)){ // if nodeId == parentId
                    currentNode.getParents().clear();
                    currentNode.getParents().add(headNode.get_id());
                    headNode.getChildren().add(key);
                }
                if(map.containsKey(parent))
                    map.get(parent).getChildren().add(currentNode.get_id());
                else {
                    if(missingParents.containsKey(key)) {
                        missingParents.get(key).add(parent);
                    } else {
                        missingParents.put(key, new HashSet<>(){{ add(parent); }});
                    }
                }
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
        return missingParents;
    }

    @Override
    public void importData() {
        flushAllExistingDataInMemory();
        HashMap<String, Node> raw = new HashMap<>();
        try(PreparedStatement ps = connection.prepareStatement(importQuery)) {
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                Node node = this.rowMapper.apply(rs);
                raw.put(node.get_id(), node);
            }
        } catch(SQLException sqlException) {
            System.err.println(sqlException.getMessage());
        }
        Map<String, Set<String>> missingParents = build(raw);
        for (Map.Entry<String, Set<String>> entry: missingParents.entrySet()) {
            String currentNodeId = entry.getKey();
            Set<String> inValidParents = entry.getValue();
            Set<String> validParents = raw.get(currentNodeId).getParents().stream()
                    .filter(p -> !inValidParents.contains(p))
                    .collect(Collectors.toSet());
            if(validParents.isEmpty()) {
                raw.get(currentNodeId).setParents(new HashSet<>(){{ add(headNode.get_id()); }});
            } else {
                raw.get(currentNodeId).setParents(new HashSet<>(validParents));
            }
        }
        System.out.println("imported " + raw.keySet().size() + " valid categories");
    }

    private void flushAllExistingDataInMemory() {
        this.linkedData.clear();
        this.unlinkedData.clear();
        this.roots.clear();

        // RE-INITIALIZE BASIC NODES
        this.headNode.set_id(CoreConstants.HEAD_NODE_ID);
        this.unlinkedNode.set_id(CoreConstants.UNLINKED_NODE_ID);

        this.linkedData.putIfAbsent(CoreConstants.HEAD_NODE_ID, this.headNode);
        this.unlinkedData.putIfAbsent(CoreConstants.UNLINKED_NODE_ID, this.unlinkedNode);

    }
}
