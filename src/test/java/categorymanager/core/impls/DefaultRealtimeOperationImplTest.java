package categorymanager.core.impls;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.sudarshan.categoryManager.core.impls.DefaultData;
import com.github.sudarshan.categoryManager.core.impls.DefaultRealtimeOperationImpl;
import com.github.sudarshan.categoryManager.core.impls.Node;
import com.github.sudarshan.categoryManager.core.pojos.CoreConstants;
import com.github.sudarshan.categoryManager.core.pojos.PathResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;

public class DefaultRealtimeOperationImplTest {

    private DefaultData<String, Node> data;
    private HashMap<String, Node> linked;
    private HashMap<String, Node> unlinked;
    HashMap<String, Node> roots;

    @BeforeEach
    public void setupBeforeTest() {
        this.data = new DefaultData<>();
        this.unlinked = data.getUnlinkedData();
        this.roots = data.getRootData();
        this.linked = data.getLinkedData();
        initializeData(data);
//        printData();
    }

    // testing the UPDATE operation.
    @Test
    public void should_update_data_and_related_nodes() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode objectNode = mapper.createObjectNode();
        objectNode.put("name", "clothing");
        Node cat3 = new Node();
        cat3.set_id("cat3");
        cat3.getParents().add("cat2");
        cat3.setData(objectNode);

        DefaultRealtimeOperationImpl rop = new DefaultRealtimeOperationImpl(data);
        rop.update(cat3);
        Assertions.assertFalse(linked.get("cat1").getChildren().contains("cat3"));
        Assertions.assertTrue(linked.get("cat2").getChildren().contains("cat3"));
        Assertions.assertEquals(objectNode, linked.get("cat3").getData());
    }
    @Test
    public void should_update_node_as_root_node_when_parent_is_updated_to_head () {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode objectNode = mapper.createObjectNode();
        objectNode.put("name", "clothing");
        Node cat3 = new Node();
        cat3.set_id("cat3");
        cat3.getParents().add(CoreConstants.HEAD_NODE_ID);
        cat3.setData(objectNode);

        DefaultRealtimeOperationImpl rop = new DefaultRealtimeOperationImpl(data);
        rop.update(cat3);
        Assertions.assertFalse(linked.get("cat1").getChildren().contains("cat3"));
        Assertions.assertTrue(roots.containsKey("cat3"));
        Assertions.assertEquals(objectNode, linked.get("cat3").getData());
    }
    @Test
    public void should_move_node_and_descendant_nodes_to_unlinked_when_linked_parents_are_removed() {
        Node cat2 = new Node();
        cat2.set_id("cat2");
        cat2.getParents().add(CoreConstants.UNLINKED_NODE_ID);

        DefaultRealtimeOperationImpl rop = new DefaultRealtimeOperationImpl(data);
        rop.update(cat2);

        Assertions.assertTrue(this.unlinked.containsKey("cat5"));
        Assertions.assertTrue(this.unlinked.containsKey("cat6"));
        Assertions.assertTrue(this.unlinked.containsKey("cat2"));

        Assertions.assertFalse(this.linked.containsKey("cat5"));
        Assertions.assertFalse(this.linked.containsKey("cat6"));
        Assertions.assertFalse(this.linked.containsKey("cat2"));
    }
    @Test
    public void should_move_node_and_descendant_nodes_to_linked_when_unlinked_parents_are_removed() {

        Node cat11 = new Node();
        cat11.set_id("cat11");
        cat11.getParents().add("cat1");

        DefaultRealtimeOperationImpl rop = new DefaultRealtimeOperationImpl(data);
        rop.update(cat11);

        Assertions.assertFalse(this.unlinked.containsKey("cat11"));
        Assertions.assertFalse(this.unlinked.containsKey("cat22"));
        Assertions.assertTrue(this.linked.containsKey("cat11"));
        Assertions.assertTrue(this.linked.containsKey("cat22"));
    }
    @Test
    public void should_update_node_as_unlinked_node_when_parent_is_updated_to_unlinked () {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode objectNode = mapper.createObjectNode();
        objectNode.put("name", "clothing");
        Node cat3 = new Node();
        cat3.set_id("cat3");
        cat3.getParents().add(CoreConstants.UNLINKED_NODE_ID);
        cat3.setData(objectNode);

        DefaultRealtimeOperationImpl rop = new DefaultRealtimeOperationImpl(data);
        rop.update(cat3);

        Assertions.assertFalse(linked.get("cat1").getChildren().contains("cat3"));
        Assertions.assertTrue(unlinked.containsKey("cat3"));
        Assertions.assertFalse(linked.containsKey("cat3"));
        Assertions.assertEquals(objectNode, unlinked.get("cat3").getData());
    }
    @Test
    public void should_not_update_data_and_related_nodes_when_parent_node_is_absent() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode objectNode = mapper.createObjectNode();
        objectNode.put("name", "clothing");
        Node cat3 = new Node();
        cat3.set_id("cat3");
        cat3.getParents().add("cat7");
        cat3.setData(objectNode);

        DefaultRealtimeOperationImpl rop = new DefaultRealtimeOperationImpl(data);
        rop.update(cat3);

        Assertions.assertTrue(linked.get("cat1").getChildren().contains("cat3"));
        Assertions.assertTrue(
                linked.get("cat3").getParents().contains("cat1") &&
                linked.get("cat3").getParents().size() == 1
        );
        Assertions.assertNull(linked.get("cat7") );
        Assertions.assertNull(unlinked.get("cat7"));
        Assertions.assertNotEquals(objectNode, linked.get("cat3").getData());
    }
    @Test
    public void should_not_update_when_node_does_not_exist() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode objectNode = mapper.createObjectNode();
        objectNode.put("name", "clothing");
        Node cat3 = new Node();
        cat3.set_id("cat8");
        cat3.getParents().add("cat7");
        cat3.setData(objectNode);

        DefaultRealtimeOperationImpl rop = new DefaultRealtimeOperationImpl(data);
        rop.update(cat3);

        Assertions.assertNull(linked.get("cat8"));
        Assertions.assertNull(unlinked.get("cat8"));
        Assertions.assertNull(linked.get("cat7") );
        Assertions.assertNull(unlinked.get("cat7"));
        Assertions.assertTrue(linked.get("cat1").getChildren().contains("cat3"));
        Assertions.assertNotEquals(objectNode, linked.get("cat3").getData());
    }

    // Testing the generatePathsForLinkedNodes operation
    @Test
    public void generate_paths_for_linked_nodes_when_node_exists() {
        Node cat11 = new Node();
        cat11.set_id("cat11");
        cat11.getParents().add("cat1");

        DefaultRealtimeOperationImpl rop = new DefaultRealtimeOperationImpl(data);
        rop.update(cat11);

        PathResponse pathResponse = rop.generatePathsForLinkedNodes("cat22");
        Assertions.assertEquals( 1, pathResponse.getAncestorPaths().size());
        Assertions.assertEquals( "head.cat1.cat11.cat22", pathResponse.getAncestorPaths().get(0));
    }
    @Test
    public void return_null_for_linked_nodes_path_when_node_does_not_exist(){
        DefaultRealtimeOperationImpl rop = new DefaultRealtimeOperationImpl(data);
        PathResponse pathResponse = rop.generatePathsForLinkedNodes("cat21");
        Assertions.assertNull( pathResponse.getAncestorPaths());
    }
    // Testing the generatePathsForUnLinkedNodes operation
    @Test
    public void generate_paths_for_unlinked_nodes_when_node_exists() {

        Node cat99 = new Node();
        cat99.set_id("cat99");
        cat99.getParents().add("cat15");

        Node cat2 = new Node();
        cat2.set_id("cat2");
        cat2.getParents().add("cat15");

        Node cat6 = new Node();
        cat6.set_id("cat6");
        cat6.getParents().add("cat2");
        cat6.getParents().add("cat99");

        DefaultRealtimeOperationImpl rop = new DefaultRealtimeOperationImpl(data);

        rop.add(cat99);
        rop.update(cat2);
        rop.update(cat6);

        PathResponse pathResponse = rop.generatePathsForUnLinkedNodes("cat6");
        Assertions.assertEquals( 2, pathResponse.getAncestorPaths().size());
        Assertions.assertEquals( "unlinked.cat15.cat2.cat6", pathResponse.getAncestorPaths().get(1));
        Assertions.assertEquals( "unlinked.cat15.cat99.cat6", pathResponse.getAncestorPaths().get(0));
    }
    @Test
    public void return_null_for_unlinked_nodes_path_when_node_does_not_exist(){
        DefaultRealtimeOperationImpl rop = new DefaultRealtimeOperationImpl(data);
        PathResponse pathResponse = rop.generatePathsForUnLinkedNodes("cat7");
        Assertions.assertNull( pathResponse.getAncestorPaths());
    }
    private void printData() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            System.out.println(mapper.writeValueAsString(this.data));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
    private void initializeData(DefaultData<String, Node> data) {
        /*

                   head
                /         \
           cat1             cat2
            /              /    \
         cat3            cat5   cat6

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

        Node cat2 = new Node();
        cat2.set_id("cat2");
        cat2.getParents().add("head");
        cat2.getChildren().addAll(List.of("cat5", "cat6"));

        Node cat3 = new Node();
        cat3.set_id("cat3");
        cat3.getParents().add("cat1");
        cat3.setData(objectNode);

        Node cat5 = new Node();
        cat5.set_id("cat5");
        cat5.getParents().add("cat2");

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

        Node head = new Node();
        head.set_id(CoreConstants.HEAD_NODE_ID);
        head.getChildren().addAll(List.of("cat1", "cat2"));
        head.setParents(null);

        Node unlinkedHead = new Node();
        unlinkedHead.set_id(CoreConstants.UNLINKED_NODE_ID);
        unlinkedHead.getChildren().addAll(List.of("cat11", "cat15"));
        unlinkedHead.setParents(null);

        this.linked.put("cat1", cat1);
        this.linked.put("cat2", cat2);
        this.linked.put("cat3", cat3);
        this.linked.put("cat5", cat5);
        this.linked.put("cat6", cat6);
        this.linked.put(CoreConstants.HEAD_NODE_ID, head);
//        this.linked.put("cat22", cat22); // this node falls in the unlinked path.

        this.unlinked.put("cat11", cat11);
        this.unlinked.put("cat15", cat15);
        this.unlinked.put("cat22", cat22);
        this.unlinked.put(CoreConstants.UNLINKED_NODE_ID, unlinkedHead);

        this.roots.put("cat1", cat1);
        this.roots.put("cat2", cat2);

    }
}
