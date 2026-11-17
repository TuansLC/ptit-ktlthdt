package vn.ptit.giaohang.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.ptit.giaohang.common.NotFoundException;
import vn.ptit.giaohang.domain.finance.Settlement;
import vn.ptit.giaohang.domain.order.Order;
import vn.ptit.giaohang.domain.user.Shop;
import vn.ptit.giaohang.repository.OrderRepository;
import vn.ptit.giaohang.repository.SettlementRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * Doi soat voi shop: gom cac don da hoan tat, tinh tien tra shop va ghi nhan thanh toan.
 */
@Service
@Transactional(readOnly = true)
public class SettlementService {

    /** Tom tat so tien du kien doi soat cua mot shop. */
    public record ReadySummary(Shop shop, int orderCount, long totalCod, long totalFee) {
        public long payout() {
            return totalCod - totalFee;
        }
    }

    private final SettlementRepository settlementRepository;
    private final OrderRepository orderRepository;
    private final AccountService accountService;

    public SettlementService(SettlementRepository settlementRepository, OrderRepository orderRepository,
                             AccountService accountService) {
        this.settlementRepository = settlementRepository;
        this.orderRepository = orderRepository;
        this.accountService = accountService;
    }

    public List<Order> readyOrders(Long shopId) {
        return orderRepository.findReadyForSettlement(shopId);
    }

    public ReadySummary summarize(Shop shop) {
        List<Order> orders = readyOrders(shop.getId());
        long cod = orders.stream().mapToLong(Order::settlementCod).sum();
        long fee = orders.stream().mapToLong(Order::getShippingFee).sum();
        return new ReadySummary(shop, orders.size(), cod, fee);
    }

    /** Cac shop dang co don cho doi soat. */
    public List<ReadySummary> readySummaries() {
        List<ReadySummary> result = new ArrayList<>();
        for (Shop shop : accountService.allShops()) {
            ReadySummary summary = summarize(shop);
            if (summary.orderCount() > 0) {
                result.add(summary);
            }
        }
        return result;
    }

    @Transactional
    public Settlement create(Long shopId, String note) {
        Shop shop = accountService.getShop(shopId);
        Settlement settlement = settlementRepository.save(new Settlement(shop, readyOrders(shopId), note));
        settlement.assignCode();
        return settlement;
    }

    @Transactional
    public void markPaid(Long settlementId, String paymentRef) {
        get(settlementId).markPaid(paymentRef);
    }

    public List<Settlement> listAll() {
        return settlementRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<Settlement> listForShop(Long shopId) {
        return settlementRepository.findByShopIdOrderByCreatedAtDesc(shopId);
    }

    public Settlement get(Long id) {
        return settlementRepository.findById(id).orElseThrow(() -> new NotFoundException("Không tìm thấy phiếu đối soát"));
    }

    public Settlement getForShop(Long shopId, Long id) {
        Settlement settlement = get(id);
        if (!settlement.getShop().getId().equals(shopId)) {
            throw new NotFoundException("Không tìm thấy phiếu đối soát");
        }
        return settlement;
    }
}
