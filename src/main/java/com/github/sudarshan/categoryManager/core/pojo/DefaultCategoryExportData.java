package com.github.sudarshan.categoryManager.core.pojo;

import com.github.sudarshan.categoryManager.core.sp.Node;
import com.github.sudarshan.categoryManager.core.spi.ICategoryExportData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DefaultCategoryExportData implements ICategoryExportData {
    private String categoryId;
    private Node node;
}
