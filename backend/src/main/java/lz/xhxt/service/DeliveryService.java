package lz.xhxt.service;

public interface DeliveryService {
    void doDelivery(Long orderId, String expressCompany, String trackingNo);

    void finishDelivery(Long orderId);
}