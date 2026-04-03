package lz.xhxt.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lz.xhxt.common.Result;
import lz.xhxt.entity.User;
import lz.xhxt.entity.UserOrder;
import lz.xhxt.entity.UserOrderItem;
import lz.xhxt.mapper.UserMapper;
import lz.xhxt.mapper.UserOrderItemMapper;
import lz.xhxt.mapper.UserOrderMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/order")
public class AdminOrderController {

    @Resource
    private UserOrderMapper userOrderMapper;

    @Resource
    private UserOrderItemMapper userOrderItemMapper;

    @Resource
    private UserMapper userMapper;

    @GetMapping("/page")
    public Result page(@RequestParam(required = false) String userKeyword,
                       @RequestParam(required = false) Integer status,
                       @RequestParam(required = false, defaultValue = "1") Integer pageNo,
                       @RequestParam(required = false, defaultValue = "10") Integer pageSize) {
        int pNo = pageNo == null || pageNo < 1 ? 1 : pageNo;
        int pSize = pageSize == null || pageSize < 1 ? 10 : Math.min(100, pageSize);

        LambdaQueryWrapper<UserOrder> qw = new LambdaQueryWrapper<>();
        if (status != null) qw.eq(UserOrder::getStatus, status);

        if (userKeyword != null && !userKeyword.trim().isEmpty()) {
            List<User> users = userMapper.selectList(new LambdaQueryWrapper<User>()
                    .like(User::getAccount, userKeyword.trim())
                    .or()
                    .like(User::getNickname, userKeyword.trim())
                    .or()
                    .like(User::getPhone, userKeyword.trim()));
            List<Long> ids = new ArrayList<>();
            for (User u : users) ids.add(u.getId());
            if (ids.isEmpty()) {
                Map<String, Object> empty = new HashMap<>();
                empty.put("records", new ArrayList<>());
                empty.put("total", 0);
                empty.put("pageNo", pNo);
                empty.put("pageSize", pSize);
                return Result.ok(empty);
            }
            qw.in(UserOrder::getUserId, ids);
        }

        qw.orderByDesc(UserOrder::getCreateTime);
        Page<UserOrder> page = userOrderMapper.selectPage(new Page<>(pNo, pSize), qw);

        List<Map<String, Object>> rows = new ArrayList<>();
        for (UserOrder order : page.getRecords()) {
            User user = userMapper.selectById(order.getUserId());
            Map<String, Object> row = new HashMap<>();
            row.put("id", order.getId());
            row.put("orderNo", order.getOrderNo());
            row.put("userId", order.getUserId());
            row.put("userAccount", user == null ? "" : user.getAccount());
            row.put("userNickname", user == null ? "" : user.getNickname());
            row.put("userPhone", user == null ? "" : user.getPhone());
            row.put("status", order.getStatus());
            row.put("totalAmount", order.getTotalAmount());
            row.put("receiverName", order.getReceiverName());
            row.put("receiverPhone", order.getReceiverPhone());
            row.put("receiverAddress", order.getReceiverAddress());
            row.put("cardMessage", order.getCardMessage());
            row.put("deliveryExpectTime", order.getDeliveryExpectTime());
            row.put("createTime", order.getCreateTime());
            row.put("payTime", order.getPayTime());
            row.put("expireTime", order.getExpireTime());
            rows.add(row);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("records", rows);
        data.put("total", page.getTotal());
        data.put("pageNo", pNo);
        data.put("pageSize", pSize);
        return Result.ok(data);
    }

    @GetMapping("/detail/{orderNo}")
    public Result detail(@PathVariable String orderNo) {
        UserOrder order = userOrderMapper.selectOne(new LambdaQueryWrapper<UserOrder>().eq(UserOrder::getOrderNo, orderNo));
        if (order == null) return Result.ok(null);
        User user = userMapper.selectById(order.getUserId());
        List<UserOrderItem> items = userOrderItemMapper.selectList(new LambdaQueryWrapper<UserOrderItem>()
                .eq(UserOrderItem::getOrderId, order.getId()));

        Map<String, Object> data = new HashMap<>();
        data.put("order", order);
        data.put("user", user);
        data.put("items", items);
        return Result.ok(data);
    }

    @GetMapping("/notify/list")
    public Result notifyList() {
        List<UserOrder> orders = userOrderMapper.selectList(new LambdaQueryWrapper<UserOrder>()
                .eq(UserOrder::getStatus, 1)
                .eq(UserOrder::getNotifyReadAdmin, 0)
                .orderByDesc(UserOrder::getPayTime));
        List<Map<String, Object>> rows = new ArrayList<>();
        for (UserOrder o : orders) {
            User u = userMapper.selectById(o.getUserId());
            List<UserOrderItem> items = userOrderItemMapper.selectList(new LambdaQueryWrapper<UserOrderItem>()
                    .eq(UserOrderItem::getOrderId, o.getId()));
            Map<String, Object> m = new HashMap<>();
            m.put("orderNo", o.getOrderNo());
            m.put("username", u == null ? "" : (u.getNickname() == null ? u.getAccount() : u.getNickname()));
            m.put("phone", o.getReceiverPhone());
            m.put("address", o.getReceiverAddress());
            m.put("items", items);
            m.put("amount", o.getTotalAmount());
            m.put("createTime", o.getCreateTime());
            m.put("cardMessage", o.getCardMessage());
            m.put("deliveryExpectTime", o.getDeliveryExpectTime());
            rows.add(m);
        }
        return Result.ok(rows);
    }

    @PostMapping("/notify/read/{orderNo}")
    public Result notifyRead(@PathVariable String orderNo) {
        UserOrder order = userOrderMapper.selectOne(new LambdaQueryWrapper<UserOrder>()
                .eq(UserOrder::getOrderNo, orderNo));
        if (order != null) {
            order.setNotifyReadAdmin(1);
            userOrderMapper.updateById(order);
        }
        return Result.ok(null);
    }
}