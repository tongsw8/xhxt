package lz.xhxt.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lz.xhxt.common.Result;
import lz.xhxt.entity.User;
import lz.xhxt.entity.UserOrder;
import lz.xhxt.mapper.UserMapper;
import lz.xhxt.mapper.UserOrderMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/user")
public class AdminUserController {

    @Resource
    private UserMapper userMapper;

    @Resource
    private UserOrderMapper userOrderMapper;

    @GetMapping("/list")
    public Result list(@RequestParam(required = false) String role) {
        LambdaQueryWrapper<User> query = new LambdaQueryWrapper<>();
        if (role != null && !role.trim().isEmpty()) {
            query.eq(User::getRole, role.trim());
        }
        query.orderByDesc(User::getId);
        List<User> list = userMapper.selectList(query);
        for (User user : list) {
            user.setPassword(null);
        }
        return Result.ok(list);
    }

    @PostMapping("/status")
    public Result updateStatus(@RequestParam Long id, @RequestParam Integer status) {
        User user = new User();
        user.setId(id);
        user.setStatus(status);
        userMapper.updateById(user);
        return Result.ok(null);
    }

    @GetMapping("/detail/{id}")
    public Result detail(@PathVariable Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            return Result.ok(null);
        }
        user.setPassword(null);

        List<UserOrder> paidOrders = userOrderMapper.selectList(new LambdaQueryWrapper<UserOrder>()
                .eq(UserOrder::getUserId, id)
                .in(UserOrder::getStatus, 1, 2, 3));

        BigDecimal paidTotalAmount = BigDecimal.ZERO;
        for (UserOrder order : paidOrders) {
            if (order.getTotalAmount() != null) {
                paidTotalAmount = paidTotalAmount.add(order.getTotalAmount());
            }
        }

        Map<String, Object> data = new HashMap<>();
        data.put("user", user);
        data.put("paidOrderCount", paidOrders.size());
        data.put("paidTotalAmount", paidTotalAmount);
        return Result.ok(data);
    }
}