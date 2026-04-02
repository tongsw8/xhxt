package lz.xhxt.service;

import lz.xhxt.common.Result;

public interface OrderService {

    Result createFromCart(Long userId, Long addressId, String cardMessage, String deliveryExpectTime);

    Result createDirect(Long userId, Long addressId, Long productId, Integer quantity, String cardMessage, String deliveryExpectTime);

    Result getOrderDetail(Long userId, String orderNo);

    Result payOrder(Long userId, String orderNo);

    Result listOrders(Long userId);

    Result urgeDelivery(Long userId, String orderNo);

    Result listProductReviews(Long userId, Long productId);

    Result addProductReview(Long userId, String orderNo, Long productId, String content);

    Result toggleReviewLike(Long userId, Long commentId);
}