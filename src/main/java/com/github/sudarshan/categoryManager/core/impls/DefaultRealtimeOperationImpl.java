package com.github.sudarshan.categoryManager.core.impls;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.sudarshan.categoryManager.core.interfaces.Data;
import com.github.sudarshan.categoryManager.core.interfaces.RealtimeOperation;
import com.github.sudarshan.categoryManager.core.pojos.CoreConstants;
import com.github.sudarshan.categoryManager.core.pojos.NodePresence;
import com.github.sudarshan.categoryManager.core.pojos.PathResponse;
import com.github.sudarshan.categoryManager.core.pojos.Utility;
import org.apache.commons.collections4.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

public class DefaultRealtimeOperationImpl implements RealtimeOperation<String, Node> {
    private final HashMap<String, Node> linkedData;
    private final HashMap<String, Node> unlinkedData;
    private final HashMap<String, Node> roots;
    private final Node headNode = new Node();
    public DefaultRealtimeOperationImpl(Data<String, Node> data) {
        this.linkedData = data.getLinkedData();
        this.unlinkedData = data.getUnlinkedData();
        this.roots = data.getRootData();
        this.headNode.set_id(CoreConstants.HEAD_NODE_ID);
        this.linkedData.putIfAbsent(CoreConstants.HEAD_NODE_ID, this.headNode);
    }

    private List<String> generatePaths(String nodeId, String pathType, HashMap<String, Node> data) {
        if( !validateIfAlreadyPresent(nodeId) && !areParentsPresent(Set.of(nodeId)) )
            return null;
        List<String> paths = new ArrayList<>();
        if(pathType.equals(CoreConstants.ANCESTOR_PATH)) {
            paths = Utility.generateAncestorPaths(nodeId, data);
        } else if(pathType.equals(CoreConstants.DESCENDANT_PATH)) {
            paths = Utility.generateDescendantPaths(nodeId, data);
        }
        return paths;
    }

    public PathResponse generatePathsForLinkedNodes(String nodeId) {
        List<String> ancestorPaths = generatePaths(nodeId, CoreConstants.ANCESTOR_PATH, linkedData);
        List<String> descendantPaths = generatePaths(nodeId, CoreConstants.DESCENDANT_PATH, linkedData);
        PathResponse pathResponse =  new PathResponse();
        pathResponse.setAncestorPaths(ancestorPaths);
        pathResponse.setDescendantPaths(descendantPaths);
        pathResponse.setNodeMap(linkedData);
        return pathResponse;
    }
    public PathResponse generatePathsForUnLinkedNodes(String nodeId) {
        List<String> ancestorPaths = generatePaths(nodeId, CoreConstants.ANCESTOR_PATH, unlinkedData);
        List<String> descendantPaths = generatePaths(nodeId, CoreConstants.DESCENDANT_PATH, unlinkedData);
        PathResponse pathResponse =  new PathResponse();
        pathResponse.setAncestorPaths(ancestorPaths);
        pathResponse.setDescendantPaths(descendantPaths);
        pathResponse.setNodeMap(unlinkedData);
        return pathResponse;
    }

    @Override
    public void delete(HashSet<String> nodeIds) {
        nodeIds.forEach(this.roots::remove);
        unlinkedData.get(CoreConstants.UNLINKED_NODE_ID).getChildren().addAll(nodeIds);
        nodeIds.stream().filter(this::validateIfAlreadyPresent).forEach(nodeId -> {
            NodePresence OldNodePresence = getNodePresence(nodeId);
            if(OldNodePresence.equals(NodePresence.BOTH) || OldNodePresence.equals(NodePresence.ONLY_LINKED)) {
                Node currentNode = linkedData.get(nodeId);
                currentNode.getParents().forEach(parentId -> {
                    linkedData.get(parentId).getChildren().remove(nodeId);
                });
                currentNode.getParents().clear();
                currentNode.getParents().add(CoreConstants.UNLINKED_NODE_ID);
//                this.linkedData.remove(nodeId);
//                this.unlinkedData.putIfAbsent(nodeId, currentNode);
                relinkSubtree(OldNodePresence, currentNode);
            }
        });
    }
    @Override
    public Node update(Node updatedNode) {
        if(validateIfAlreadyPresent(updatedNode.get_id()) && areParentsPresent(updatedNode.getParents())) {
            HashSet<String> updatedParents = updatedNode.getParents();
            HashSet<String> currentParents;
            String nodeId = updatedNode.get_id();
            boolean isParentsUpdated = true;
            if(linkedData.containsKey(nodeId)) {
                isParentsUpdated = !linkedData.get(nodeId).getParents().equals(updatedParents);
                currentParents = linkedData.get(nodeId).getParents();
            } else if(unlinkedData.containsKey(nodeId)) {
                isParentsUpdated = !unlinkedData.get(nodeId).getParents().equals(updatedParents);
                currentParents = unlinkedData.get(nodeId).getParents();
            } else {
                currentParents = new HashSet<>();
            }
            if(isParentsUpdated) {

                Set<String> newParentsAdded = updatedParents.stream().filter(parent -> !currentParents.contains(parent)).collect(Collectors.toSet());
                Set<String> oldParentsRemoved = currentParents.stream().filter(parent -> !updatedParents.contains(parent)).collect(Collectors.toSet());
                newParentsAdded.forEach(parent -> {
                    if(linkedData.containsKey(parent)) {
                        linkedData.get(parent).getChildren().add(nodeId);
                    } else {
                        unlinkedData.get(parent).getChildren().add(nodeId);
                    }
                });
                oldParentsRemoved.forEach(parent -> {
                    if(linkedData.containsKey(parent)) {
                        linkedData.get(parent).getChildren().remove(nodeId);
                    } else {
                        unlinkedData.get(parent).getChildren().remove(nodeId);
                    }
                });
                NodePresence updatedNodePresence =  getNodePresence(updatedParents);
                NodePresence oldNodePresence =  getNodePresence(currentParents);
                JsonNode data;
                Node currentNode = oldNodePresence.equals(NodePresence.BOTH) || oldNodePresence.equals(NodePresence.ONLY_LINKED)?
                        linkedData.get(nodeId): unlinkedData.get(nodeId);
                switch (updatedNodePresence){
                    case ONLY_LINKED:
                        linkedData.putIfAbsent(nodeId, currentNode);
                        linkedData.get(nodeId).setParents(updatedParents);
                        data = updatedNode.getData();
                        currentNode.setData(data);
                        if(!oldNodePresence.equals(updatedNodePresence)){
                            relinkSubtree(oldNodePresence, updatedNode);
                        }
                        if(updatedParents.size() == 1 && updatedParents.contains(CoreConstants.HEAD_NODE_ID))
                            this.roots.putIfAbsent(nodeId,  linkedData.get(nodeId)); // NOTE: always store only the reference to the node, not a new object of node.
                        break;
                    case ONLY_UNLINKED:
                        unlinkedData.putIfAbsent(nodeId, currentNode);
                        unlinkedData.get(nodeId).setParents(updatedParents);
                        data = updatedNode.getData(); // unlinkedData.get(nodeId).getData();
                        unlinkedData.get(nodeId).setData(data);
                        if(!oldNodePresence.equals(updatedNodePresence)){
                            relinkSubtree(oldNodePresence, updatedNode);
                        }
                        this.roots.remove(nodeId);
                        break;
                    default:
                        linkedData.get(nodeId).setParents(updatedParents);
                        unlinkedData.get(nodeId).setParents(updatedParents);
                        data = updatedNode.getData(); // linkedData.get(nodeId).getData();
                        linkedData.get(nodeId).setData(data);
                        unlinkedData.get(nodeId).setData(data);
                        if(!oldNodePresence.equals(updatedNodePresence)){
                            relinkSubtree(oldNodePresence, updatedNode);
                        }
                        this.roots.remove(nodeId);
                }
            }
        }
        return updatedNode;
    }

    private void relinkSubtree(NodePresence oldNodePresence, Node node) {

        Stack<String> stack = new Stack<>();
        Set<String> visitedNodeSet = new HashSet<>();
        stack.push(node.get_id());
        Node currentNode;
        String currentNodeId;
        while(!stack.isEmpty()) {
            currentNodeId = stack.peek();
            currentNode = oldNodePresence.equals(NodePresence.ONLY_LINKED) ?
                    this.linkedData.get(currentNodeId): this.unlinkedData.get(currentNodeId);

            NodePresence currentNodePresence = getNodePresence(currentNode.getParents());
            switch (currentNodePresence){
                case ONLY_LINKED:
                    this.unlinkedData.remove(currentNodeId);
                    this.linkedData.putIfAbsent(currentNodeId, currentNode);
                    break;
                case ONLY_UNLINKED:
                    this.linkedData.remove(currentNodeId);
                    this.unlinkedData.putIfAbsent(currentNodeId, currentNode);
                    break;
                default:
                    this.unlinkedData.putIfAbsent(currentNodeId, currentNode);
                    this.linkedData.putIfAbsent(currentNodeId, currentNode);
            }
            Set<String> children = currentNode.getChildren().stream().filter(child -> !visitedNodeSet.contains(child))
                    .collect(Collectors.toSet());
            stack.addAll(children);
            visitedNodeSet.add(currentNodeId);
            if( ( !stack.isEmpty() && children.isEmpty()) )
                stack.pop();
            while( !stack.isEmpty() && visitedNodeSet.contains(stack.peek()) ) {
                stack.pop();
            }
        }
    }

    private NodePresence getNodePresence(String nodeId) {
        return linkedData.containsKey(nodeId) && unlinkedData.containsKey(nodeId) ? NodePresence.BOTH :
                unlinkedData.containsKey(nodeId) ? NodePresence.ONLY_UNLINKED: NodePresence.ONLY_LINKED;
    }
    private NodePresence getNodePresence(HashSet<String> parents) {
        return (
                CollectionUtils.containsAny(linkedData.keySet(), parents) &&
                CollectionUtils.containsAny(unlinkedData.keySet(), parents)) ?
        NodePresence.BOTH:
        (CollectionUtils.containsAny(unlinkedData.keySet(), parents)) ?
                NodePresence.ONLY_UNLINKED:
                NodePresence.ONLY_LINKED;
    }
    private void addNodeBasedOnNodePresence(NodePresence nodePresence, Node node) {
        String nodeId = node.get_id();
        switch (nodePresence){
            case ONLY_LINKED:
                this.linkedData.put(nodeId, node);
                if(node.getParents().size() == 1 && node.getParents().contains(CoreConstants.HEAD_NODE_ID))
                    this.roots.putIfAbsent(nodeId,  linkedData.get(nodeId)); // NOTE: always store only the reference to the node, not a new object of node.
                break;
            case ONLY_UNLINKED:
                this.unlinkedData.put(nodeId, node);
                break;
            default:
                this.linkedData.put(nodeId, node);
                this.unlinkedData.put(nodeId, node);
        }
    }

    public Node add(Node node) {
        String id = node.get_id();
        boolean isNodeAlreadyPresent = validateIfAlreadyPresent(id);
        HashSet<String> parents = node.getParents();
        if(isNodeAlreadyPresent || !areParentsPresent(parents))
            return node;
        if(node.getParents().size() == 1 && node.getParents().contains(CoreConstants.HEAD_NODE_ID)){
            this.linkedData.put(id, node);
            this.roots.put(id, node);
        } else if(node.getParents().size() == 1 && node.getParents().contains(CoreConstants.UNLINKED_NODE_ID)){
            this.unlinkedData.put(id, node);
        }
        addNodeToParentsChildren(node.getParents(), id);
        NodePresence nodePresence = getNodePresence(node.getParents());
        addNodeBasedOnNodePresence(nodePresence, node);
        return node;
    }
    private void addNodeToParentsChildren(HashSet<String> parents, String id) {
        parents.forEach(parent -> {
            if(this.linkedData.containsKey(parent))
                this.linkedData.get(parent).getChildren().add(id);
            if(this.unlinkedData.containsKey(parent))
                this.unlinkedData.get(parent).getChildren().add(id);
        });
    }
    private boolean validateIfAlreadyPresent(String id) {
        return linkedData.containsKey(id) || unlinkedData.containsKey(id);
    }
    private boolean areParentsPresent(Set<String> ids) {
        return ids.isEmpty() || ids.stream().noneMatch(id -> !(linkedData.containsKey(id) || unlinkedData.containsKey(id)));
    }

    private long getCurrentNodeCount() {
        return this.linkedData.size() + this.unlinkedData.size();
    }
}
