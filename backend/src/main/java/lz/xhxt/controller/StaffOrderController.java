package lz.xhxt.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lz.xhxt.common.Result;
import lz.xhxt.entity.UserOrder;
import lz.xhxt.mapper.UserOrderMapper;
import lz.xhxt.service.DeliveryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/staff/order")
public class StaffOrderController {

    @Autowired
    private DeliveryService deliveryService;

    @Autowired
    private UserOrderMapper userOrderMapper;

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

    @PostMapping("/delivery")
    public Result deliver(@RequestParam Long id,
                          @RequestParam String company,
                          @RequestParam String no) {
        deliveryService.doDelivery(id, company, no);
        return Result.ok(null);
    }
}