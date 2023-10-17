package com.github.sudarshan.categoryManager.core.sp;


import com.fasterxml.jackson.databind.JsonNode;

import java.util.HashSet;

@lombok.Data
public class Node {
    private JsonNode data;
    private HashSet<String> parents = new HashSet<>();
    private HashSet<String> children = new HashSet<>();
    private String _id;

}
