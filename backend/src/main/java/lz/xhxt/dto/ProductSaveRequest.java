package lz.xhxt.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductSaveRequest {
    private Long id;
    private Long categoryId;
    private String productName;
    private BigDecimal price;
    private Integer stock;
    private String coverImg;
    private String detailText;
    private List<String> detailImgs;
    private Integer status;
}