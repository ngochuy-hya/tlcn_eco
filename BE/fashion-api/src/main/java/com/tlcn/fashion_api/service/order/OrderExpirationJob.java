package com.tlcn.fashion_api.service.order;

import com.tlcn.fashion_api.entity.order.Order;
import com.tlcn.fashion_api.repository.order.OrderRepository;
import com.tlcn.fashion_api.service.inventory.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderExpirationJob {

    private final OrderRepository orderRepository;
    private final InventoryService inventoryService;

    @Scheduled(fixedDelay = 60_000) // mỗi 60s quét 1 lần
    @Transactional
    public void cancelExpiredOrders() {
        LocalDateTime now = LocalDateTime.now();
        List<Order> expired = orderRepository.findExpiredUnpaid(now);
        // findExpiredUnpaid: status = 'pending', paymentStatus = 'unpaid', paymentExpiresAt <= :now

        for (Order o : expired) {
            o.setStatus("cancelled");
            o.setPaymentStatus("expired");
            o.setCancelledAt(now);
            o.setCancelReason("Thanh toán quá hạn 10 phút");

            // 🔥 TRẢ KHO: các reservation đang held -> expired
            inventoryService.releaseForOrder(o, true);
        }

        if (!expired.isEmpty()) {
            orderRepository.saveAll(expired);
        }
    }
}
