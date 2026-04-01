package lz.xhxt.service.impl;

import lz.xhxt.entity.UserOrder;
import lz.xhxt.mapper.UserOrderMapper;
import lz.xhxt.service.DeliveryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class DeliveryServiceImpl implements DeliveryService {

    @Autowired
    private UserOrderMapper orderMapper;

    @Override
    public void doDelivery(Long orderId, String expressCompany, String trackingNo) {
        UserOrder order = orderMapper.selectById(orderId);
        if (order == null) {
            return;
        }
        if (order.getStatus() != null && order.getStatus() == 1) {
            order.setExpressCompany(expressCompany);
            order.setTrackingNo(trackingNo);
            order.setDeliveryTime(new Date());
            order.setStatus(2);
            orderMapper.updateById(order);
        }
    }
}