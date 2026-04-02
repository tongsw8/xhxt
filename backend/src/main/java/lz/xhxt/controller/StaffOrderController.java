package lz.xhxt.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lz.xhxt.common.Result;
import lz.xhxt.entity.User;
import lz.xhxt.entity.UserOrder;
import lz.xhxt.entity.UserOrderItem;
import lz.xhxt.mapper.UserMapper;
import lz.xhxt.mapper.UserOrderItemMapper;
import lz.xhxt.mapper.UserOrderMapper;
import lz.xhxt.service.DeliveryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/staff/order")
public class StaffOrderController {

    @Autowired
    private DeliveryService deliveryService;

    @Autowired
    private UserOrderMapper userOrderMapper;

    @Autowired
    private UserOrderItemMapper userOrderItemMapper;

    @Autowired
    private UserMapper userMapper;

    @GetMapping("/list")
    public Result list(@RequestParam(required = false) Integer status) {
        LambdaQueryWrapper<UserOrder> query = new LambdaQueryWrapper<>();
        if (status != null) {
            query.eq(UserOrder::getStatus, status);
        } else {
            query.in(UserOrder::getStatus, 1, 2);
        }
        query.orderByDesc(UserOrder::getCreateTime);
        List<UserOrder> list = userOrderMapper.selectList(query);
        return Result.ok(list);
    }

    @GetMapping("/notify/list")
    public Result notifyList() {
        List<UserOrder> orders = userOrderMapper.selectList(new LambdaQueryWrapper<UserOrder>()
                .eq(UserOrder::getStatus, 1)
                .eq(UserOrder::getNotifyReadStaff, 0)
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
            order.setNotifyReadStaff(1);
            userOrderMapper.updateById(order);
        }
        return Result.ok(null);
    }

    @PostMapping("/delivery")
    public Result deliver(@RequestParam Long id,
                          @RequestParam String company,
                          @RequestParam String no) {
        deliveryService.doDelivery(id, company, no);
        return Result.ok(null);
    }
}