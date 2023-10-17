package com.github.sudarshan.categoryManager.core.pojo;

import com.github.sudarshan.categoryManager.core.spi.ICategoriesPaths;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CategoriesPaths implements ICategoriesPaths {
    private List<PathResponse> categoriesPaths = new ArrayList<>();
}
