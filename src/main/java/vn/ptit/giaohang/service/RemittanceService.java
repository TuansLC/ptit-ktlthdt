package vn.ptit.giaohang.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.ptit.giaohang.common.NotFoundException;
import vn.ptit.giaohang.domain.finance.CodRemittance;
import vn.ptit.giaohang.domain.order.Order;
import vn.ptit.giaohang.domain.user.Shipper;
import vn.ptit.giaohang.repository.CodRemittanceRepository;
import vn.ptit.giaohang.repository.OrderRepository;

import java.util.List;

/**
 * Nop tien COD: shipper lap phieu nop toan bo tien dang giu, quan tri vien xac nhan hoac tu choi.
 */
@Service
@Transactional(readOnly = true)
public class RemittanceService {

    private final CodRemittanceRepository remittanceRepository;
    private final OrderRepository orderRepository;
    private final AccountService accountService;

    public RemittanceService(CodRemittanceRepository remittanceRepository, OrderRepository orderRepository,
                             AccountService accountService) {
        this.remittanceRepository = remittanceRepository;
        this.orderRepository = orderRepository;
        this.accountService = accountService;
    }

    /** Cac don shipper da thu tien nhung chua nop. */
    public List<Order> pendingOrders(Long shipperId) {
        return orderRepository.findCodPendingRemittance(accountService.getShipper(shipperId));
    }

    public long holdingAmount(Long shipperId) {
        return pendingOrders(shipperId).stream().mapToLong(Order::getCollectedAmount).sum();
    }

    @Transactional
    public CodRemittance submit(Long shipperId, String note) {
        Shipper shipper = accountService.getShipper(shipperId);
        List<Order> pending = orderRepository.findCodPendingRemittance(shipper);
        CodRemittance remittance = remittanceRepository.save(new CodRemittance(shipper, pending, note));
        remittance.assignCode();
        return remittance;
    }

    @Transactional
    public void confirm(Long adminId, Long remittanceId) {
        get(remittanceId).confirm(accountService.getUser(adminId).getFullName());
    }

    @Transactional
    public void reject(Long adminId, Long remittanceId, String reason) {
        get(remittanceId).reject(accountService.getUser(adminId).getFullName(), reason);
    }

    public List<CodRemittance> listForShipper(Long shipperId) {
        return remittanceRepository.findByShipperOrderByCreatedAtDesc(accountService.getShipper(shipperId));
    }

    public List<CodRemittance> listAll() {
        return remittanceRepository.findAllByOrderByCreatedAtDesc();
    }

    public CodRemittance get(Long id) {
        return remittanceRepository.findById(id).orElseThrow(() -> new NotFoundException("Không tìm thấy phiếu nộp tiền"));
    }

    public CodRemittance getForShipper(Long shipperId, Long id) {
        CodRemittance remittance = get(id);
        if (!remittance.getShipper().getId().equals(shipperId)) {
            throw new NotFoundException("Không tìm thấy phiếu nộp tiền");
        }
        return remittance;
    }
}
