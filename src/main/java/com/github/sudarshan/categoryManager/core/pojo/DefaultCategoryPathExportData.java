package com.github.sudarshan.categoryManager.core.pojo;

import com.github.sudarshan.categoryManager.core.spi.ICategoryPathExportData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DefaultCategoryPathExportData implements ICategoryPathExportData {
    private String categoryId;
    private List<String> ancestorPaths;
}
