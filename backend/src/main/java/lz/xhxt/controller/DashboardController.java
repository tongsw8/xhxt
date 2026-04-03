package lz.xhxt.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lz.xhxt.common.Result;
import lz.xhxt.entity.Comment;
import lz.xhxt.entity.ProductInfo;
import lz.xhxt.entity.User;
import lz.xhxt.entity.UserOrder;
import lz.xhxt.entity.UserOrderItem;
import lz.xhxt.mapper.CommentMapper;
import lz.xhxt.mapper.ProductInfoMapper;
import lz.xhxt.mapper.UserMapper;
import lz.xhxt.mapper.UserOrderItemMapper;
import lz.xhxt.mapper.UserOrderMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/dashboard")
public class DashboardController {

    @Resource
    private UserMapper userMapper;

    @Resource
    private ProductInfoMapper productInfoMapper;

    @Resource
    private UserOrderMapper userOrderMapper;

    @Resource
    private UserOrderItemMapper userOrderItemMapper;

    @Resource
    private CommentMapper commentMapper;

    @GetMapping("/stats")
    public Result getStats(@RequestParam(required = false, defaultValue = "7") Integer days) {
        int n = (days != null && days == 30) ? 30 : 7;

        Integer userCount = userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getRole, "USER"));
        Integer pendingCommentCount = commentMapper.selectCount(new LambdaQueryWrapper<Comment>().eq(Comment::getStatus, 0));
        Integer onSaleProductCount = productInfoMapper.selectCount(new LambdaQueryWrapper<ProductInfo>().eq(ProductInfo::getStatus, 1));

        LocalDate today = LocalDate.now();
        Date todayBegin = toDate(today);
        Integer todayOrderCount = userOrderMapper.selectCount(new LambdaQueryWrapper<UserOrder>().ge(UserOrder::getCreateTime, todayBegin));

        LocalDate startDay = today.minusDays(n - 1L);
        Date trendBegin = toDate(startDay);

        List<UserOrder> paidOrders = userOrderMapper.selectList(new LambdaQueryWrapper<UserOrder>()
                .in(UserOrder::getStatus, 1, 2, 3)
                .ge(UserOrder::getPayTime, trendBegin));

        Map<LocalDate, BigDecimal> salesByDay = initDayDecimalMap(startDay, today);
        for (UserOrder order : paidOrders) {
            if (order.getPayTime() == null || order.getTotalAmount() == null) continue;
            LocalDate day = order.getPayTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            if (day.isBefore(startDay) || day.isAfter(today)) continue;
            salesByDay.put(day, salesByDay.get(day).add(order.getTotalAmount()));
        }

        List<User> users = userMapper.selectList(new LambdaQueryWrapper<User>()
                .eq(User::getRole, "USER")
                .ge(User::getRegTime, trendBegin));

        Map<LocalDate, Integer> userByDay = initDayIntMap(startDay, today);
        for (User user : users) {
            if (user.getRegTime() == null) continue;
            LocalDate day = user.getRegTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            if (day.isBefore(startDay) || day.isAfter(today)) continue;
            userByDay.put(day, userByDay.get(day) + 1);
        }

        Map<Long, Map<String, Object>> hotMap = new HashMap<>();
        for (UserOrder order : paidOrders) {
            if (order.getId() == null) continue;
            List<UserOrderItem> items = userOrderItemMapper.selectList(new LambdaQueryWrapper<UserOrderItem>()
                    .eq(UserOrderItem::getOrderId, order.getId()));
            for (UserOrderItem item : items) {
                if (item.getProductId() == null) continue;
                Map<String, Object> hot = hotMap.computeIfAbsent(item.getProductId(), k -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("productId", item.getProductId());
                    m.put("productName", item.getProductName());
                    m.put("quantity", 0);
                    m.put("amount", BigDecimal.ZERO);
                    return m;
                });
                Integer oldQty = (Integer) hot.get("quantity");
                int addQty = item.getQuantity() == null ? 0 : item.getQuantity();
                hot.put("quantity", oldQty + addQty);
                BigDecimal oldAmount = (BigDecimal) hot.get("amount");
                BigDecimal price = item.getProductPrice() == null ? BigDecimal.ZERO : item.getProductPrice();
                hot.put("amount", oldAmount.add(price.multiply(BigDecimal.valueOf(addQty))));
            }
        }

        List<Map<String, Object>> hotProducts = new ArrayList<>(hotMap.values());
        hotProducts.sort((a, b) -> Integer.compare((Integer) b.get("quantity"), (Integer) a.get("quantity")));
        if (hotProducts.size() > 10) hotProducts = hotProducts.subList(0, 10);

        Map<String, Object> map = new HashMap<>();
        map.put("days", n);
        map.put("userCount", userCount == null ? 0 : userCount);
        map.put("pendingCommentCount", pendingCommentCount == null ? 0 : pendingCommentCount);
        map.put("onSaleProductCount", onSaleProductCount == null ? 0 : onSaleProductCount);
        map.put("todayOrderCount", todayOrderCount == null ? 0 : todayOrderCount);
        map.put("salesTrend", toTrendList(salesByDay));
        map.put("userTrend", toTrendList(userByDay));
        map.put("hotProducts", hotProducts);
        return Result.ok(map);
    }

    private Date toDate(LocalDate day) {
        return Date.from(day.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private Map<LocalDate, BigDecimal> initDayDecimalMap(LocalDate start, LocalDate end) {
        Map<LocalDate, BigDecimal> map = new LinkedHashMap<>();
        LocalDate d = start;
        while (!d.isAfter(end)) {
            map.put(d, BigDecimal.ZERO);
            d = d.plusDays(1);
        }
        return map;
    }

    private Map<LocalDate, Integer> initDayIntMap(LocalDate start, LocalDate end) {
        Map<LocalDate, Integer> map = new LinkedHashMap<>();
        LocalDate d = start;
        while (!d.isAfter(end)) {
            map.put(d, 0);
            d = d.plusDays(1);
        }
        return map;
    }

    private List<Map<String, Object>> toTrendList(Map<LocalDate, ? extends Number> source) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (Map.Entry<LocalDate, ? extends Number> e : source.entrySet()) {
            Map<String, Object> row = new HashMap<>();
            row.put("date", e.getKey().toString());
            row.put("value", e.getValue());
            list.add(row);
        }
        return list;
    }
}