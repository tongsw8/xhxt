package lz.xhxt.controller;

import lz.xhxt.common.Result;
import lz.xhxt.common.ResultCode;
import lz.xhxt.dto.CreateOrderRequest;
import lz.xhxt.service.JwtService;
import lz.xhxt.service.OrderService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api/user/order")
public class OrderController {

    @Resource
    private OrderService orderService;

    @Resource
    private JwtService jwtService;

    @PostMapping("/create-from-cart")
    public Result createFromCart(@RequestHeader(value = "Authorization", required = false) String authorization,
                                 @RequestBody CreateOrderRequest req) {
        Long userId = getUserIdFromToken(authorization);
        if (userId == null) return Result.error(ResultCode.ILLEGAL_PARAMETER.code(), "请先登录");
        if (req == null || req.getAddressId() == null) return Result.error(ResultCode.ILLEGAL_PARAMETER.code(), "请选择收货地址");
        return orderService.createFromCart(userId, req.getAddressId());
    }

    @GetMapping("/detail/{orderNo}")
    public Result detail(@RequestHeader(value = "Authorization", required = false) String authorization,
                         @PathVariable String orderNo) {
        Long userId = getUserIdFromToken(authorization);
        if (userId == null) return Result.error(ResultCode.ILLEGAL_PARAMETER.code(), "请先登录");
        return orderService.getOrderDetail(userId, orderNo);
    }

    @PostMapping("/pay/{orderNo}")
    public Result pay(@RequestHeader(value = "Authorization", required = false) String authorization,
                      @PathVariable String orderNo) {
        Long userId = getUserIdFromToken(authorization);
        if (userId == null) return Result.error(ResultCode.ILLEGAL_PARAMETER.code(), "请先登录");
        return orderService.payOrder(userId, orderNo);
    }

    @GetMapping("/list")
    public Result list(@RequestHeader(value = "Authorization", required = false) String authorization) {
        Long userId = getUserIdFromToken(authorization);
        if (userId == null) return Result.error(ResultCode.ILLEGAL_PARAMETER.code(), "请先登录");
        return orderService.listOrders(userId);
    }

    private Long getUserIdFromToken(String authorization) {
        try {
            return jwtService.parseUserId(authorization);
        } catch (Exception e) {
            return null;
        }
    }
}
