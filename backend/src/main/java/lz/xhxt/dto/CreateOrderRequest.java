package lz.xhxt.dto;

import lombok.Data;

@Data
public class CreateOrderRequest {
    private Long addressId;
    private String cardMessage;
    private String deliveryExpectTime;
}