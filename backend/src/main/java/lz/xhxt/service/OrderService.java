package lz.xhxt.service;

import lz.xhxt.common.Result;

public interface OrderService {

    Result createFromCart(Long userId, Long addressId);

    Result getOrderDetail(Long userId, String orderNo);

    Result payOrder(Long userId, String orderNo);

    Result listOrders(Long userId);
}
