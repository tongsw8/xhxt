package lz.xhxt.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lz.xhxt.common.Result;
import lz.xhxt.common.ResultCode;
import lz.xhxt.entity.Comment;
import lz.xhxt.entity.ProductInfo;
import lz.xhxt.entity.User;
import lz.xhxt.entity.UserAction;
import lz.xhxt.entity.UserAddress;
import lz.xhxt.entity.UserCart;
import lz.xhxt.entity.UserOrder;
import lz.xhxt.entity.UserOrderItem;
import lz.xhxt.mapper.CommentMapper;
import lz.xhxt.mapper.ProductInfoMapper;
import lz.xhxt.mapper.UserActionMapper;
import lz.xhxt.mapper.UserAddressMapper;
import lz.xhxt.mapper.UserCartMapper;
import lz.xhxt.mapper.UserMapper;
import lz.xhxt.mapper.UserOrderItemMapper;
import lz.xhxt.mapper.UserOrderMapper;
import lz.xhxt.service.OrderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Service
public class OrderServiceImpl implements OrderService {

    @Resource
    private UserCartMapper cartMapper;

    @Resource
    private ProductInfoMapper productMapper;

    @Resource
    private UserOrderMapper orderMapper;

    @Resource
    private UserOrderItemMapper orderItemMapper;

    @Resource
    private UserAddressMapper userAddressMapper;

    @Resource
    private CommentMapper commentMapper;

    @Resource
    private UserActionMapper userActionMapper;

    @Resource
    private UserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result createFromCart(Long userId, Long addressId, String cardMessage, String deliveryExpectTime) {
        if (userId == null || addressId == null) {
            return Result.error(ResultCode.ILLEGAL_PARAMETER.code(), "参数不完整");
        }

        UserAddress address = userAddressMapper.selectOne(new LambdaQueryWrapper<UserAddress>()
                .eq(UserAddress::getId, addressId)
                .eq(UserAddress::getUserId, userId));
        if (address == null) {
            return Result.error(ResultCode.ILLEGAL_PARAMETER.code(), "收货地址不存在");
        }

        closeExpired();

        List<UserCart> carts = cartMapper.selectList(new LambdaQueryWrapper<UserCart>()
                .eq(UserCart::getUserId, userId)
                .orderByDesc(UserCart::getCreateTime));
        if (carts == null || carts.isEmpty()) {
            return Result.error(ResultCode.ILLEGAL_PARAMETER.code(), "购物车为空");
        }

        List<UserOrderItem> items = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        int totalQty = 0;

        for (UserCart cart : carts) {
            ProductInfo p = productMapper.selectById(cart.getProductId());
            if (p == null || p.getStatus() == null || p.getStatus() != 1) continue;

            int need = cart.getQuantity() == null ? 0 : cart.getQuantity();
            int stock = p.getStock() == null ? 0 : p.getStock();
            if (need <= 0) continue;
            if (stock < need) {
                return Result.error(ResultCode.ILLEGAL_PARAMETER.code(), "商品【" + p.getProductName() + "】库存不足");
            }

            p.setStock(stock - need);
            productMapper.updateById(p);

            UserOrderItem item = new UserOrderItem();
            item.setProductId(p.getId());
            item.setProductName(p.getProductName());
            item.setProductPrice(p.getPrice());
            item.setQuantity(need);
            item.setCoverImg(p.getCoverImg());
            items.add(item);

            total = total.add(p.getPrice().multiply(BigDecimal.valueOf(need)));
            totalQty += need;
        }

        if (items.isEmpty()) {
            return Result.error(ResultCode.ILLEGAL_PARAMETER.code(), "购物车中无可下单商品");
        }

        UserOrder order = buildOrder(userId, address, total, cardMessage, deliveryExpectTime);
        orderMapper.insert(order);
        for (UserOrderItem item : items) {
            item.setOrderId(order.getId());
            orderItemMapper.insert(item);
        }

        cartMapper.delete(new LambdaQueryWrapper<UserCart>().eq(UserCart::getUserId, userId));

        Map<String, Object> data = new HashMap<>();
        data.put("orderNo", order.getOrderNo());
        data.put("totalAmount", total);
        data.put("totalQuantity", totalQty);
        data.put("expireTime", order.getExpireTime());
        data.put("status", order.getStatus());
        return Result.ok(data);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result createDirect(Long userId, Long addressId, Long productId, Integer quantity, String cardMessage, String deliveryExpectTime) {
        if (userId == null || addressId == null || productId == null || quantity == null || quantity < 1) {
            return Result.error(ResultCode.ILLEGAL_PARAMETER.code(), "参数不完整");
        }

        UserAddress address = userAddressMapper.selectOne(new LambdaQueryWrapper<UserAddress>()
                .eq(UserAddress::getId, addressId)
                .eq(UserAddress::getUserId, userId));
        if (address == null) {
            return Result.error(ResultCode.ILLEGAL_PARAMETER.code(), "收货地址不存在");
        }

        closeExpired();

        ProductInfo p = productMapper.selectById(productId);
        if (p == null || p.getStatus() == null || p.getStatus() != 1) {
            return Result.error(ResultCode.ILLEGAL_PARAMETER.code(), "商品不存在或已下架");
        }
        int stock = p.getStock() == null ? 0 : p.getStock();
        if (stock < quantity) {
            return Result.error(ResultCode.ILLEGAL_PARAMETER.code(), "库存不足");
        }

        p.setStock(stock - quantity);
        productMapper.updateById(p);

        BigDecimal total = p.getPrice().multiply(BigDecimal.valueOf(quantity));
        UserOrder order = buildOrder(userId, address, total, cardMessage, deliveryExpectTime);
        orderMapper.insert(order);

        UserOrderItem item = new UserOrderItem();
        item.setOrderId(order.getId());
        item.setProductId(p.getId());
        item.setProductName(p.getProductName());
        item.setProductPrice(p.getPrice());
        item.setQuantity(quantity);
        item.setCoverImg(p.getCoverImg());
        orderItemMapper.insert(item);

        Map<String, Object> data = new HashMap<>();
        data.put("orderNo", order.getOrderNo());
        data.put("totalAmount", total);
        data.put("totalQuantity", quantity);
        data.put("expireTime", order.getExpireTime());
        data.put("status", order.getStatus());
        return Result.ok(data);
    }

    @Override
    public Result getOrderDetail(Long userId, String orderNo) {
        if (userId == null || orderNo == null || orderNo.trim().isEmpty()) {
            return Result.error(ResultCode.ILLEGAL_PARAMETER.code(), "参数不完整");
        }
        closeExpired();

        UserOrder order = orderMapper.selectOne(new LambdaQueryWrapper<UserOrder>()
                .eq(UserOrder::getUserId, userId)
                .eq(UserOrder::getOrderNo, orderNo));
        if (order == null) {
            return Result.error(ResultCode.ILLEGAL_PARAMETER.code(), "订单不存在");
        }

        List<UserOrderItem> items = orderItemMapper.selectList(new LambdaQueryWrapper<UserOrderItem>()
                .eq(UserOrderItem::getOrderId, order.getId()));
        int totalQty = items.stream().mapToInt(x -> x.getQuantity() == null ? 0 : x.getQuantity()).sum();

        Map<String, Object> data = new HashMap<>();
        data.put("order", order);
        data.put("items", items);
        data.put("totalQuantity", totalQty);
        return Result.ok(data);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result payOrder(Long userId, String orderNo) {
        if (userId == null || orderNo == null || orderNo.trim().isEmpty()) {
            return Result.error(ResultCode.ILLEGAL_PARAMETER.code(), "参数不完整");
        }
        closeExpired();

        UserOrder order = orderMapper.selectOne(new LambdaQueryWrapper<UserOrder>()
                .eq(UserOrder::getUserId, userId)
                .eq(UserOrder::getOrderNo, orderNo));
        if (order == null) {
            return Result.error(ResultCode.ILLEGAL_PARAMETER.code(), "订单不存在");
        }
        if (order.getStatus() != null && order.getStatus() == 4) {
            return Result.error(ResultCode.ILLEGAL_PARAMETER.code(), "订单已关闭，无法支付");
        }
        if (order.getStatus() != null && order.getStatus() == 1) {
            return Result.ok(null);
        }

        order.setStatus(1);
        order.setPayTime(new Date());
        order.setNotifyReadAdmin(0);
        order.setNotifyReadStaff(0);
        orderMapper.updateById(order);
        return Result.ok(null);
    }

    @Override
    public Result listOrders(Long userId) {
        if (userId == null) return Result.error(ResultCode.ILLEGAL_PARAMETER.code(), "请先登录");
        closeExpired();

        List<UserOrder> orders = orderMapper.selectList(new LambdaQueryWrapper<UserOrder>()
                .eq(UserOrder::getUserId, userId)
                .orderByDesc(UserOrder::getCreateTime));

        List<Map<String, Object>> rows = new ArrayList<>();
        for (UserOrder order : orders) {
            List<UserOrderItem> items = orderItemMapper.selectList(new LambdaQueryWrapper<UserOrderItem>()
                    .eq(UserOrderItem::getOrderId, order.getId()));
            int qty = items.stream().mapToInt(x -> x.getQuantity() == null ? 0 : x.getQuantity()).sum();
            Map<String, Object> r = new HashMap<>();
            r.put("orderNo", order.getOrderNo());
            r.put("status", order.getStatus());
            r.put("totalAmount", order.getTotalAmount());
            r.put("totalQuantity", qty);
            r.put("expireTime", order.getExpireTime());
            r.put("receiverName", order.getReceiverName());
            r.put("receiverPhone", order.getReceiverPhone());
            r.put("receiverAddress", order.getReceiverAddress());
            r.put("cardMessage", order.getCardMessage());
            r.put("deliveryExpectTime", order.getDeliveryExpectTime());
            r.put("urgeShip", order.getUrgeShip());
            r.put("payTime", order.getPayTime());
            r.put("createTime", order.getCreateTime());
            rows.add(r);
        }
        return Result.ok(rows);
    }

    @Override
    public Result urgeDelivery(Long userId, String orderNo) {
        if (userId == null || orderNo == null || orderNo.trim().isEmpty()) {
            return Result.error(ResultCode.ILLEGAL_PARAMETER.code(), "参数不完整");
        }
        UserOrder order = orderMapper.selectOne(new LambdaQueryWrapper<UserOrder>()
                .eq(UserOrder::getUserId, userId)
                .eq(UserOrder::getOrderNo, orderNo));
        if (order == null) return Result.error(ResultCode.ILLEGAL_PARAMETER.code(), "订单不存在");
        if (order.getStatus() == null || order.getStatus() != 1) {
            return Result.error(ResultCode.ILLEGAL_PARAMETER.code(), "当前订单不可催发货");
        }
        order.setUrgeShip(1);
        order.setUrgeTime(new Date());
        orderMapper.updateById(order);
        return Result.ok(null);
    }

    @Override
    public Result toggleReviewLike(Long userId, Long commentId) {
        if (userId == null || commentId == null) {
            return Result.error(ResultCode.ILLEGAL_PARAMETER.code(), "参数不完整");
        }
        Comment c = commentMapper.selectById(commentId);
        if (c == null || !"PRODUCT_REVIEW".equals(c.getTargetType())) {
            return Result.error(ResultCode.ILLEGAL_PARAMETER.code(), "评价不存在");
        }
        LambdaQueryWrapper<UserAction> q = new LambdaQueryWrapper<UserAction>()
                .eq(UserAction::getUserId, userId)
                .eq(UserAction::getTargetId, commentId)
                .eq(UserAction::getTargetType, "PRODUCT_REVIEW")
                .eq(UserAction::getActionType, "LIKE");
        Integer cnt = userActionMapper.selectCount(q);
        if (cnt != null && cnt > 0) userActionMapper.delete(q);
        else {
            UserAction a = new UserAction();
            a.setUserId(userId);
            a.setTargetId(commentId);
            a.setTargetType("PRODUCT_REVIEW");
            a.setActionType("LIKE");
            a.setCreateTime(new Date());
            userActionMapper.insert(a);
        }
        return Result.ok(null);
    }

    @Override
    public Result listProductReviews(Long userId, Long productId) {
        if (productId == null) return Result.error(ResultCode.ILLEGAL_PARAMETER.code(), "参数不完整");
        List<Comment> comments = commentMapper.selectList(new LambdaQueryWrapper<Comment>()
                .eq(Comment::getTargetType, "PRODUCT_REVIEW")
                .eq(Comment::getTargetId, productId)
                .eq(Comment::getStatus, 1)
                .orderByAsc(Comment::getCreateTime));

        java.util.Set<Long> allCommentIds = comments.stream()
                .map(Comment::getId)
                .filter(java.util.Objects::nonNull)
                .collect(java.util.stream.Collectors.toSet());

        List<Comment> roots = comments.stream().filter(c -> {
            if (c.getId() == null) return false;
            if (c.getIsStaff() != null && c.getIsStaff() == 1) return false;
            Long p = c.getParentId();
            return p == null || p == 0L || !allCommentIds.contains(p);
        }).collect(java.util.stream.Collectors.toList());
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Comment c : roots) {
            User u = userMapper.selectById(c.getUserId());
            int likeCount = userActionMapper.selectCount(new LambdaQueryWrapper<UserAction>()
                    .eq(UserAction::getTargetType, "PRODUCT_REVIEW")
                    .eq(UserAction::getTargetId, c.getId())
                    .eq(UserAction::getActionType, "LIKE"));
            boolean liked = false;
            if (userId != null) {
                Integer likedCnt = userActionMapper.selectCount(new LambdaQueryWrapper<UserAction>()
                        .eq(UserAction::getUserId, userId)
                        .eq(UserAction::getTargetType, "PRODUCT_REVIEW")
                        .eq(UserAction::getTargetId, c.getId())
                        .eq(UserAction::getActionType, "LIKE"));
                liked = likedCnt != null && likedCnt > 0;
            }

            List<Map<String, Object>> replies = comments.stream()
                    .filter(x -> x.getIsStaff() != null && x.getIsStaff() == 1 && x.getParentId() != null && x.getParentId().equals(c.getId()))
                    .map(x -> {
                        User su = userMapper.selectById(x.getUserId());
                        Map<String, Object> r = new HashMap<>();
                        r.put("id", x.getId());
                        r.put("content", x.getContent());
                        r.put("createTime", x.getCreateTime());
                        r.put("userName", su == null ? "官方" : (su.getNickname() == null ? su.getAccount() : su.getNickname()));
                        r.put("isStaff", 1);
                        return r;
                    }).collect(java.util.stream.Collectors.toList());

            Map<String, Object> m = new HashMap<>();
            m.put("id", c.getId());
            m.put("content", c.getContent());
            m.put("createTime", c.getCreateTime());
            m.put("userName", u == null ? "匿名" : (u.getNickname() == null ? u.getAccount() : u.getNickname()));
            m.put("likeCount", likeCount);
            m.put("liked", liked);
            m.put("replies", replies);
            rows.add(m);
        }
        java.util.Collections.reverse(rows);
        return Result.ok(rows);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result addProductReview(Long userId, String orderNo, Long productId, String content) {
        if (userId == null || orderNo == null || productId == null || content == null || content.trim().isEmpty()) {
            return Result.error(ResultCode.ILLEGAL_PARAMETER.code(), "参数不完整");
        }
        UserOrder order = orderMapper.selectOne(new LambdaQueryWrapper<UserOrder>()
                .eq(UserOrder::getUserId, userId)
                .eq(UserOrder::getOrderNo, orderNo));
        if (order == null) return Result.error(ResultCode.ILLEGAL_PARAMETER.code(), "订单不存在");
        if (order.getStatus() == null || order.getStatus() != 3) {
            return Result.error(ResultCode.ILLEGAL_PARAMETER.code(), "订单未完成，暂不可评价");
        }
        Integer itemCnt = orderItemMapper.selectCount(new LambdaQueryWrapper<UserOrderItem>()
                .eq(UserOrderItem::getOrderId, order.getId())
                .eq(UserOrderItem::getProductId, productId));
        if (itemCnt == null || itemCnt == 0) {
            return Result.error(ResultCode.ILLEGAL_PARAMETER.code(), "未购买该商品，无法评价");
        }
        Integer dup = commentMapper.selectCount(new LambdaQueryWrapper<Comment>()
                .eq(Comment::getUserId, userId)
                .eq(Comment::getTargetType, "PRODUCT_REVIEW")
                .eq(Comment::getTargetId, productId)
                .eq(Comment::getParentId, order.getId()));
        if (dup != null && dup > 0) {
            return Result.error(ResultCode.ILLEGAL_PARAMETER.code(), "该订单下此商品已评价");
        }
        Comment c = new Comment();
        c.setTargetType("PRODUCT_REVIEW");
        c.setTargetId(productId);
        c.setUserId(userId);
        c.setParentId(order.getId());
        c.setContent(content.trim());
        c.setIsStaff(0);
        c.setStatus(1);
        c.setCreateTime(new Date());
        commentMapper.insert(c);
        return Result.ok(null);
    }

    private void closeExpired() {
        Date now = new Date();
        List<UserOrder> pendings = orderMapper.selectList(new LambdaQueryWrapper<UserOrder>()
                .eq(UserOrder::getStatus, 0)
                .lt(UserOrder::getExpireTime, now));
        for (UserOrder o : pendings) {
            o.setStatus(4);
            o.setCloseTime(now);
            orderMapper.updateById(o);
            restoreStock(o.getId());
        }
    }

    private void restoreStock(Long orderId) {
        List<UserOrderItem> items = orderItemMapper.selectList(new LambdaQueryWrapper<UserOrderItem>()
                .eq(UserOrderItem::getOrderId, orderId));
        for (UserOrderItem item : items) {
            ProductInfo p = productMapper.selectById(item.getProductId());
            if (p == null) continue;
            int stock = p.getStock() == null ? 0 : p.getStock();
            int add = item.getQuantity() == null ? 0 : item.getQuantity();
            p.setStock(stock + add);
            productMapper.updateById(p);
        }
    }

    private UserOrder buildOrder(Long userId, UserAddress address, BigDecimal total, String cardMessage, String deliveryExpectTime) {
        Date now = new Date();
        Date expire = new Date(now.getTime() + 30 * 60 * 1000L);
        UserOrder order = new UserOrder();
        order.setOrderNo(genOrderNo(userId));
        order.setUserId(userId);
        order.setTotalAmount(total);
        order.setStatus(0);
        order.setExpireTime(expire);
        order.setReceiverName(address.getReceiverName());
        order.setReceiverPhone(address.getReceiverPhone());
        order.setReceiverAddress(joinAddress(address));
        order.setCardMessage(cardMessage == null ? "" : cardMessage.trim());
        order.setDeliveryExpectTime(deliveryExpectTime == null ? "" : deliveryExpectTime.trim());
        order.setNotifyReadAdmin(1);
        order.setNotifyReadStaff(1);
        order.setUrgeShip(0);
        return order;
    }

    private String joinAddress(UserAddress a) {
        return nvl(a.getProvince()) + nvl(a.getCity()) + nvl(a.getDistrict()) + nvl(a.getDetailAddress());
    }

    private String nvl(String s) {
        return s == null ? "" : s;
    }

    private String genOrderNo(Long userId) {
        String ts = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        int rand = new Random().nextInt(9000) + 1000;
        return "OD" + ts + userId + rand;
    }
}