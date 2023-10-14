package com.github.sudarshan.categoryManager.core.pojos;

import com.github.sudarshan.categoryManager.core.impls.Node;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Categories {
    private List<Node> categoryList = new ArrayList<>();

}
