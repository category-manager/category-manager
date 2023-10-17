package com.github.sudarshan.categoryManager.examples;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.sudarshan.categoryManager.core.pojo.CoreConstants;
import com.github.sudarshan.categoryManager.core.pojo.PathResponse;
import com.github.sudarshan.categoryManager.core.sp.DefaultData;
import com.github.sudarshan.categoryManager.core.sp.DefaultRealtimeOperationImpl;
import com.github.sudarshan.categoryManager.core.sp.Node;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class TestServiceInMemory {
    static DefaultData<String, Node> data;
    static HashMap<String, Node> linked;
    static HashMap<String, Node> unlinked;
    static HashMap<String, Node> roots;
    public static void main(String[] args) {
        testInMemoryData();
    }
    private static void testInMemoryData() {
        data = new DefaultData<>();
        unlinked = data.getUnlinkedData();
        roots = data.getRootData();
        linked = data.getLinkedData();
        initializeData(data);

        DefaultRealtimeOperationImpl rop = new DefaultRealtimeOperationImpl(data);

        PathResponse pathResponse = rop.generatePathsForLinkedNodes(CoreConstants.HEAD_NODE_ID);
        System.out.println(pathResponse.getDescendantPaths());

        HashSet<String> deleteNodeIds = new HashSet<>(Collections.singleton("cat2"));
        rop.delete(deleteNodeIds);

        pathResponse = rop.generatePathsForLinkedNodes(CoreConstants.HEAD_NODE_ID);
        System.out.println(pathResponse.getDescendantPaths());

        pathResponse = rop.generatePathsForUnLinkedNodes(CoreConstants.UNLINKED_NODE_ID);
        System.out.println("unlinked paths\n" + pathResponse.getDescendantPaths());
    }

    private static void initializeData(DefaultData<String, Node> data) {
        /*
        DAG: directed acyclic graph.
                     head
                /            \
               cat1             cat2
            /         \        /    \
         cat3            cat5       cat6
             \      /
                cat7


                 unlinked
                /         \
           cat11           cat15
            /
         cat22


         */
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode objectNode = mapper.createObjectNode();
        objectNode.put("name", "footwear");

        Node cat1 = new Node();
        cat1.set_id("cat1");
        cat1.getParents().add("head");
        cat1.getChildren().add("cat3");
        cat1.getChildren().add("cat5");


        Node cat2 = new Node();
        cat2.set_id("cat2");
        cat2.getParents().add("head");
        cat2.getChildren().addAll(List.of("cat5", "cat6"));

        Node cat3 = new Node();
        cat3.set_id("cat3");
        cat3.getParents().add("cat1");
        cat3.getChildren().add("cat7");
        cat3.setData(objectNode);

        Node cat5 = new Node();
        cat5.set_id("cat5");
        cat5.getParents().addAll(List.of("cat1", "cat2"));
        cat5.getChildren().add("cat7");

        Node cat7 = new Node();
        cat7.set_id("cat7");
        cat7.getParents().addAll(List.of("cat3", "cat5"));

        Node cat6 = new Node();
        cat6.set_id("cat6");
        cat6.getParents().add("cat2");

        Node cat11 = new Node();
        cat11.set_id("cat11");
        cat11.getParents().add("unlinked");
        cat11.getChildren().add("cat22");

        Node cat15 = new Node();
        cat15.set_id("cat15");
        cat15.getParents().add("unlinked");

        Node cat22 = new Node();
        cat22.set_id("cat22");
        cat22.getParents().add("cat11");
        cat22.getParents().add(CoreConstants.UNLINKED_NODE_ID); // adding cyclic link.


        Node head = new Node();
        head.set_id(CoreConstants.HEAD_NODE_ID);
        head.getChildren().addAll(List.of("cat1", "cat2"));
        head.setParents(null);

        Node unlinkedHead = new Node();
        unlinkedHead.set_id(CoreConstants.UNLINKED_NODE_ID);
        unlinkedHead.getChildren().addAll(List.of("cat11", "cat15"));
        unlinkedHead.setParents(null);

        linked.put("cat1", cat1);
        linked.put("cat2", cat2);
        linked.put("cat3", cat3);
        linked.put("cat5", cat5);
        linked.put("cat6", cat6);
        linked.put(CoreConstants.HEAD_NODE_ID, head);
        linked.put("cat7", cat7);
        linked.put("cat22", cat22); // this node falls in the unlinked path.

        unlinked.put("cat11", cat11);
        unlinked.put("cat15", cat15);
        unlinked.put("cat22", cat22); // this node falls in the unlinked path.
        unlinked.put(CoreConstants.UNLINKED_NODE_ID, unlinkedHead);

        roots.put("cat1", cat1);
        roots.put("cat2", cat2);

    }
}
