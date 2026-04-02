package lz.xhxt.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lz.xhxt.common.Result;
import lz.xhxt.common.ResultCode;
import lz.xhxt.dto.OrderCommentSaveRequest;
import lz.xhxt.entity.Comment;
import lz.xhxt.entity.User;
import lz.xhxt.entity.UserAction;
import lz.xhxt.entity.UserOrder;
import lz.xhxt.entity.UserOrderItem;
import lz.xhxt.mapper.CommentMapper;
import lz.xhxt.mapper.UserActionMapper;
import lz.xhxt.mapper.UserMapper;
import lz.xhxt.mapper.UserOrderItemMapper;
import lz.xhxt.mapper.UserOrderMapper;
import lz.xhxt.service.JwtService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user/order/comment")
public class OrderCommentController {

    @Resource
    private CommentMapper commentMapper;

    @Resource
    private UserActionMapper userActionMapper;

    @Resource
    private UserOrderMapper userOrderMapper;

    @Resource
    private UserOrderItemMapper userOrderItemMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private JwtService jwtService;

    @GetMapping("/list")
    public Result list(@RequestHeader(value = "Authorization", required = false) String authorization,
                       @RequestParam Long productId) {
        Long userId = getUserIdFromToken(authorization);
        if (userId == null) return Result.error(ResultCode.ILLEGAL_PARAMETER.code(), "请先登录");

        List<Comment> comments = commentMapper.selectList(new LambdaQueryWrapper<Comment>()
                .eq(Comment::getTargetType, "ORDER_PRODUCT")
                .eq(Comment::getTargetId, productId)
                .eq(Comment::getStatus, 1)
                .orderByDesc(Comment::getCreateTime));

        List<Map<String, Object>> rows = new ArrayList<>();
        for (Comment c : comments) {
            User u = userMapper.selectById(c.getUserId());
            Integer likeCount = userActionMapper.selectCount(new LambdaQueryWrapper<UserAction>()
                    .eq(UserAction::getTargetType, "ORDER_COMMENT")
                    .eq(UserAction::getActionType, "LIKE")
                    .eq(UserAction::getTargetId, c.getId()));
            Integer liked = userActionMapper.selectCount(new LambdaQueryWrapper<UserAction>()
                    .eq(UserAction::getTargetType, "ORDER_COMMENT")
                    .eq(UserAction::getActionType, "LIKE")
                    .eq(UserAction::getTargetId, c.getId())
                    .eq(UserAction::getUserId, userId));
            Map<String, Object> r = new HashMap<>();
            r.put("id", c.getId());
            r.put("content", c.getContent());
            r.put("createTime", c.getCreateTime());
            r.put("userId", c.getUserId());
            r.put("userName", u == null ? "匿名用户" : (u.getNickname() == null ? u.getAccount() : u.getNickname()));
            r.put("likeCount", likeCount == null ? 0 : likeCount);
            r.put("liked", liked != null && liked > 0);
            rows.add(r);
        }
        return Result.ok(rows);
    }

    @PostMapping("/save")
    public Result save(@RequestHeader(value = "Authorization", required = false) String authorization,
                       @RequestBody OrderCommentSaveRequest req) {
        Long userId = getUserIdFromToken(authorization);
        if (userId == null) return Result.error(ResultCode.ILLEGAL_PARAMETER.code(), "请先登录");
        if (req == null || req.getOrderNo() == null || req.getProductId() == null || req.getContent() == null || req.getContent().trim().isEmpty()) {
            return Result.error(ResultCode.ILLEGAL_PARAMETER.code(), "参数不完整");
        }

        UserOrder order = userOrderMapper.selectOne(new LambdaQueryWrapper<UserOrder>()
                .eq(UserOrder::getUserId, userId)
                .eq(UserOrder::getOrderNo, req.getOrderNo()));
        if (order == null) return Result.error(ResultCode.ILLEGAL_PARAMETER.code(), "订单不存在");
        if (order.getStatus() == null || order.getStatus() != 3) {
            return Result.error(ResultCode.ILLEGAL_PARAMETER.code(), "订单未完成，暂不可评价");
        }

        UserOrderItem item = userOrderItemMapper.selectOne(new LambdaQueryWrapper<UserOrderItem>()
                .eq(UserOrderItem::getOrderId, order.getId())
                .eq(UserOrderItem::getProductId, req.getProductId()));
        if (item == null) return Result.error(ResultCode.ILLEGAL_PARAMETER.code(), "仅可评价已购买商品");

        Comment c = new Comment();
        c.setTargetType("ORDER_PRODUCT");
        c.setTargetId(req.getProductId());
        c.setUserId(userId);
        c.setParentId(0L);
        c.setContent(req.getContent().trim());
        c.setIsStaff(0);
        c.setStatus(1);
        c.setCreateTime(new Date());
        commentMapper.insert(c);
        return Result.ok(null);
    }

    @PostMapping("/like/{commentId}")
    public Result like(@RequestHeader(value = "Authorization", required = false) String authorization,
                       @PathVariable Long commentId) {
        Long userId = getUserIdFromToken(authorization);
        if (userId == null) return Result.error(ResultCode.ILLEGAL_PARAMETER.code(), "请先登录");

        Comment c = commentMapper.selectById(commentId);
        if (c == null || !"ORDER_PRODUCT".equals(c.getTargetType())) {
            return Result.error(ResultCode.ILLEGAL_PARAMETER.code(), "评论不存在");
        }

        LambdaQueryWrapper<UserAction> qw = new LambdaQueryWrapper<UserAction>()
                .eq(UserAction::getUserId, userId)
                .eq(UserAction::getTargetType, "ORDER_COMMENT")
                .eq(UserAction::getActionType, "LIKE")
                .eq(UserAction::getTargetId, commentId);
        Integer count = userActionMapper.selectCount(qw);
        if (count != null && count > 0) {
            userActionMapper.delete(qw);
        } else {
            UserAction a = new UserAction();
            a.setUserId(userId);
            a.setTargetType("ORDER_COMMENT");
            a.setActionType("LIKE");
            a.setTargetId(commentId);
            a.setCreateTime(new Date());
            userActionMapper.insert(a);
        }
        return Result.ok(null);
    }

    private Long getUserIdFromToken(String authorization) {
        try {
            return jwtService.parseUserId(authorization);
        } catch (Exception e) {
            return null;
        }
    }
}