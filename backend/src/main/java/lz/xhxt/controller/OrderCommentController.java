package lz.xhxt.controller;

import lz.xhxt.common.Result;
import lz.xhxt.common.ResultCode;
import lz.xhxt.dto.OrderCommentSaveRequest;
import lz.xhxt.service.JwtService;
import lz.xhxt.service.OrderService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api/user/order/comment")
public class OrderCommentController {

    @Resource
    private OrderService orderService;

    @Resource
    private JwtService jwtService;

    @GetMapping("/list")
    public Result list(@RequestHeader(value = "Authorization", required = false) String authorization,
                       @RequestParam Long productId) {
        Long userId = getUserIdFromToken(authorization);
        return orderService.listProductReviews(userId, productId);
    }

    @PostMapping("/save")
    public Result save(@RequestHeader(value = "Authorization", required = false) String authorization,
                       @RequestBody OrderCommentSaveRequest req) {
        Long userId = getUserIdFromToken(authorization);
        if (userId == null) return Result.error(ResultCode.ILLEGAL_PARAMETER.code(), "请先登录");
        if (req == null || req.getOrderNo() == null || req.getProductId() == null || req.getContent() == null || req.getContent().trim().isEmpty()) {
            return Result.error(ResultCode.ILLEGAL_PARAMETER.code(), "参数不完整");
        }
        return orderService.addProductReview(userId, req.getOrderNo(), req.getProductId(), req.getContent());
    }

    @PostMapping("/like/{commentId}")
    public Result like(@RequestHeader(value = "Authorization", required = false) String authorization,
                       @PathVariable Long commentId) {
        Long userId = getUserIdFromToken(authorization);
        if (userId == null) return Result.error(ResultCode.ILLEGAL_PARAMETER.code(), "请先登录");
        return orderService.toggleReviewLike(userId, commentId);
    }

    private Long getUserIdFromToken(String authorization) {
        try {
            return jwtService.parseUserId(authorization);
        } catch (Exception e) {
            return null;
        }
    }
}