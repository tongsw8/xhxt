package lz.xhxt.dto;

import lombok.Data;

@Data
public class OrderCommentSaveRequest {
    private String orderNo;
    private Long productId;
    private String content;
}