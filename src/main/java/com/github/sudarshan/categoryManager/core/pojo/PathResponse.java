package com.github.sudarshan.categoryManager.core.pojo;

import com.github.sudarshan.categoryManager.core.sp.Node;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class PathResponse {
    private String categoryId;
    private List<String> ancestorPaths = new ArrayList<>();
    private List<String> descendantPaths = new ArrayList<>();
    private Map<String, Node> nodeMap = new HashMap<>();
}
