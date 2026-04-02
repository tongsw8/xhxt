package lz.xhxt.dto;

import lombok.Data;

@Data
public class CreateDirectOrderRequest {
    private Long addressId;
    private Long productId;
    private Integer quantity;
    private String cardMessage;
    private String deliveryExpectTime;
}