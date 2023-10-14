package com.github.sudarshan.categoryManager.core.pojos;


import com.github.sudarshan.categoryManager.core.impls.Node;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Utility {
    public static List<String> generateAncestorPaths(String nodeId, HashMap<String, Node> data) {
        if(nodeId.equals(CoreConstants.HEAD_NODE_ID) || nodeId.equals(CoreConstants.UNLINKED_NODE_ID)) return List.of(nodeId);

        List<List<String>> allPaths = new ArrayList<>();
        Stack<String> stack = new Stack<>();
        HashMap<String, List<String>> visitedNodeAndPathMap = new HashMap<>();

        Node currentNode = data.get(nodeId);
        stack.add(nodeId);
        visitedNodeAndPathMap.put(nodeId, List.of(nodeId));

        while(!stack.empty()) {
            HashSet<String> parents = currentNode.getParents();
            if(Objects.isNull(parents)) {
                List<String> path;
                if(Objects.isNull(visitedNodeAndPathMap.get(CoreConstants.UNLINKED_NODE_ID)))
                    path = visitedNodeAndPathMap.get(CoreConstants.HEAD_NODE_ID);
                else
                    path = visitedNodeAndPathMap.get(CoreConstants.UNLINKED_NODE_ID);
                allPaths.add(new ArrayList<>(path));
                while(!stack.isEmpty() && visitedNodeAndPathMap.containsKey(stack.peek())){
                    stack.pop();
                    path.remove(path.size() -1);
                }
                if(!stack.isEmpty()){
                    List<String> parentNodePath = new ArrayList<>(visitedNodeAndPathMap.get(currentNode.get_id()));
                    currentNode = data.get(stack.peek());
                    parentNodePath.add(currentNode.get_id());
                    visitedNodeAndPathMap.put(currentNode.get_id(), parentNodePath);
                }
                continue;
            }
            stack.addAll(parents);

            List<String> parentNodePath = new ArrayList<>(visitedNodeAndPathMap.get(currentNode.get_id()));
            currentNode = data.get(stack.peek());
            parentNodePath.add(currentNode.get_id());
            visitedNodeAndPathMap.put(currentNode.get_id(), parentNodePath);
        }

        return allPaths.stream()
                .peek(Collections::reverse)
                .map(path -> String.join(".", path))
                .collect(Collectors.toList());
    }

    public static List<String> generateDescendantPaths(String nodeId, HashMap<String, Node> data) {
        if(Objects.isNull(nodeId) || !data.containsKey(nodeId)) {
            return new ArrayList<>();
        }
        List<List<String>> allPaths = new ArrayList<>();
        List<String> path = new ArrayList<>();
        Set<String> visitedPath = new HashSet<>();
        Stack<String> stack = new Stack<>();
        Node currentNode;
        stack.push(nodeId);

        while(!stack.empty()) {
            currentNode = data.get(stack.peek());
            visitedPath.add(currentNode.get_id());
            path.add(stack.peek());
            Set<String> children = currentNode.getChildren().stream().filter(Predicate.not(path::contains)).collect(Collectors.toSet()); // avoid cyclic path.
            stack.addAll(children);
            allPaths.add(new ArrayList<>(path));

            if(currentNode.getChildren().isEmpty()) {
                while(!stack.isEmpty() && visitedPath.contains(stack.peek())) {
                    path.remove(path.size() -1);
                    String visitedNode = stack.pop();
                    visitedPath.remove(visitedNode);
                }
            }
        }

        return allPaths.stream()
                .map(p -> String.join(".", p))
                .collect(Collectors.toList());
    }
}
