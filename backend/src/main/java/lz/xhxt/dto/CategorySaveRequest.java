package lz.xhxt.dto;

import lombok.Data;

@Data
public class CategorySaveRequest {
    private Long id;
    private String categoryName;
    private Integer sortNo;
    private Integer status;
}
