package vn.ptit.giaohang.service;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.ptit.giaohang.common.NotFoundException;
import vn.ptit.giaohang.domain.order.Order;
import vn.ptit.giaohang.domain.order.OrderStatus;
import vn.ptit.giaohang.domain.order.OrderStatusHistory;
import vn.ptit.giaohang.domain.pricing.FeeQuote;
import vn.ptit.giaohang.domain.pricing.ServiceType;
import vn.ptit.giaohang.domain.user.Shipper;
import vn.ptit.giaohang.domain.user.Shop;
import vn.ptit.giaohang.domain.user.User;
import vn.ptit.giaohang.domain.zone.Ward;
import vn.ptit.giaohang.dto.OrderForm;
import vn.ptit.giaohang.repository.OrderRepository;
import vn.ptit.giaohang.repository.OrderStatusHistoryRepository;

import java.util.EnumSet;
import java.util.List;
import java.util.function.Consumer;

/**
 * Dieu phoi cac ca su dung lien quan don hang.
 * Service chi kiem tra quyen so huu va goi phuong thuc nghiep vu cua Order;
 * quy tac chuyen trang thai nam trong cac lop OrderState.
 */
@Service
@Transactional(readOnly = true)
public class OrderService {

    /** Don shipper con phai xu ly. */
    private static final EnumSet<OrderStatus> SHIPPER_ACTIVE =
            EnumSet.of(OrderStatus.CHO_LAY_HANG, OrderStatus.DANG_GIAO, OrderStatus.DANG_HOAN);

    private final OrderRepository orderRepository;
    private final OrderStatusHistoryRepository historyRepository;
    private final AccountService accountService;
    private final ZoneService zoneService;
    private final PricingService pricingService;
    private final ApplicationEventPublisher eventPublisher;

    public OrderService(OrderRepository orderRepository, OrderStatusHistoryRepository historyRepository,
                        AccountService accountService, ZoneService zoneService, PricingService pricingService,
                        ApplicationEventPublisher eventPublisher) {
        this.orderRepository = orderRepository;
        this.historyRepository = historyRepository;
        this.accountService = accountService;
        this.zoneService = zoneService;
        this.pricingService = pricingService;
        this.eventPublisher = eventPublisher;
    }

    // ===================== Shop =====================

    public FeeQuote quote(Long pickupWardId, Long deliveryWardId, int weightGram, ServiceType serviceType) {
        return pricingService.quote(zoneService.getWard(pickupWardId), zoneService.getWard(deliveryWardId),
                weightGram, serviceType);
    }

    @Transactional
    public Order createOrder(Long shopId, OrderForm f) {
        Shop shop = accountService.getShop(shopId);
        Ward pickupWard = zoneService.getWard(f.getPickupWardId());
        Ward deliveryWard = zoneService.getWard(f.getDeliveryWardId());
        FeeQuote fee = pricingService.quote(pickupWard, deliveryWard, f.getWeightGram(), f.getServiceType());

        Order order = new Order(shop, f.getPickupAddress().trim(), pickupWard,
                f.getReceiverName().trim(), f.getReceiverPhone(), f.getReceiverAddress().trim(), deliveryWard,
                f.getItemDescription().trim(), f.getWeightGram(), f.getCodAmount(), fee, f.getNote());
        orderRepository.save(order);
        order.assignCode();

        eventPublisher.publishEvent(new OrderStatusChangedEvent(order, null, order.getStatus(), shop, "Tạo đơn"));
        return order;
    }

    @Transactional
    public void cancelByShop(Long shopId, Long orderId) {
        Order order = findForShop(shopId, orderId);
        transition(order, order.getShop(), "Shop hủy đơn", Order::cancel);
    }

    public Order findForShop(Long shopId, Long orderId) {
        Order order = findById(orderId);
        if (!order.belongsTo(accountService.getShop(shopId))) {
            throw new NotFoundException("Không tìm thấy đơn hàng");
        }
        return order;
    }

    public List<Order> searchForShop(Long shopId, OrderStatus status, String keyword) {
        return orderRepository.search(status, shopId, null, normalize(keyword));
    }

    // ===================== Shipper =====================

    public List<Order> activeForShipper(Long shipperId) {
        return orderRepository.findByShipperAndStatusInOrderByCreatedAtAsc(
                accountService.getShipper(shipperId), SHIPPER_ACTIVE);
    }

    public List<Order> historyForShipper(Long shipperId) {
        return orderRepository.search(null, null, shipperId, null);
    }

    public Order findForShipper(Long shipperId, Long orderId) {
        Order order = findById(orderId);
        if (!order.isAssignedTo(accountService.getShipper(shipperId))) {
            throw new NotFoundException("Không tìm thấy đơn hàng");
        }
        return order;
    }

    @Transactional
    public void pickUp(Long shipperId, Long orderId) {
        Order order = findForShipper(shipperId, orderId);
        transition(order, order.getShipper(), "Đã lấy hàng tại shop", Order::pickUp);
    }

    @Transactional
    public void deliverSuccess(Long shipperId, Long orderId, long collectedAmount) {
        Order order = findForShipper(shipperId, orderId);
        String note = collectedAmount > 0 ? "Đã thu hộ " + collectedAmount + " đ" : "Giao thành công";
        transition(order, order.getShipper(), note, o -> o.deliverSuccess(collectedAmount));
    }

    @Transactional
    public void deliverFail(Long shipperId, Long orderId, String reason) {
        Order order = findForShipper(shipperId, orderId);
        transition(order, order.getShipper(), reason, o -> o.deliverFail(reason));
    }

    @Transactional
    public void confirmReturned(Long shipperId, Long orderId) {
        Order order = findForShipper(shipperId, orderId);
        transition(order, order.getShipper(), "Đã trả hàng cho shop", Order::confirmReturned);
    }

    // ===================== Admin =====================

    public List<Order> searchForAdmin(OrderStatus status, Long shopId, Long shipperId, String keyword) {
        return orderRepository.search(status, shopId, shipperId, normalize(keyword));
    }

    @Transactional
    public void assign(Long adminId, Long orderId, Long shipperId) {
        Order order = findById(orderId);
        Shipper shipper = accountService.getShipper(shipperId);
        transition(order, accountService.getUser(adminId), "Phân công cho " + shipper.getFullName(),
                o -> o.assignShipper(shipper));
    }

    @Transactional
    public void retry(Long adminId, Long orderId, Long shipperId) {
        Order order = findById(orderId);
        Shipper shipper = accountService.getShipper(shipperId);
        transition(order, accountService.getUser(adminId),
                "Giao lại lần " + (order.getFailedAttempts() + 1) + ", shipper " + shipper.getFullName(),
                o -> o.retryDelivery(shipper));
    }

    @Transactional
    public void startReturn(Long adminId, Long orderId) {
        Order order = findById(orderId);
        transition(order, accountService.getUser(adminId), "Chuyển hoàn về shop", Order::startReturn);
    }

    // ===================== Dung chung =====================

    public Order findById(Long orderId) {
        return orderRepository.findById(orderId).orElseThrow(() -> new NotFoundException("Không tìm thấy đơn hàng"));
    }

    public List<OrderStatusHistory> history(Order order) {
        return historyRepository.findByOrderOrderByCreatedAtAscIdAsc(order);
    }

    /**
     * Thuc hien mot thao tac doi trang thai roi phat su kien cho cac observer.
     * Neu trang thai khong hop le, OrderState nem BusinessException va transaction rollback.
     */
    private void transition(Order order, User actor, String note, Consumer<Order> action) {
        OrderStatus before = order.getStatus();
        action.accept(order);
        eventPublisher.publishEvent(new OrderStatusChangedEvent(order, before, order.getStatus(), actor, note));
    }

    private static String normalize(String keyword) {
        return (keyword == null || keyword.isBlank()) ? null : keyword.trim();
    }
}
