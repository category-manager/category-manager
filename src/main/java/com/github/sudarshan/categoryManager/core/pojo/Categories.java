package com.github.sudarshan.categoryManager.core.pojo;

import com.github.sudarshan.categoryManager.core.sp.Node;
import com.github.sudarshan.categoryManager.core.spi.ICategories;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Categories implements ICategories {
    private List<Node> categoryList = new ArrayList<>();
}
