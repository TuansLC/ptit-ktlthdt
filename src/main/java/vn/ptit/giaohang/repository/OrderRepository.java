package vn.ptit.giaohang.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.ptit.giaohang.domain.order.Order;
import vn.ptit.giaohang.domain.order.OrderStatus;
import vn.ptit.giaohang.domain.user.Shipper;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    /** Tim don theo bo loc tuy chon; tham so null nghia la khong loc theo tieu chi do. */
    @Query("""
            select o from Order o
            where (:status is null or o.status = :status)
              and (:shopId is null or o.shop.id = :shopId)
              and (:shipperId is null or o.shipper.id = :shipperId)
              and (:keyword is null
                   or lower(o.code) like lower(concat('%', :keyword, '%'))
                   or o.receiverPhone like concat('%', :keyword, '%')
                   or lower(o.receiverName) like lower(concat('%', :keyword, '%')))
            order by o.createdAt desc
            """)
    List<Order> search(@Param("status") OrderStatus status,
                       @Param("shopId") Long shopId,
                       @Param("shipperId") Long shipperId,
                       @Param("keyword") String keyword);

    List<Order> findByShipperAndStatusInOrderByCreatedAtAsc(Shipper shipper, Collection<OrderStatus> statuses);

    @Query("""
            select o from Order o
            where o.shipper = :shipper and o.status = vn.ptit.giaohang.domain.order.OrderStatus.GIAO_THANH_CONG
              and o.collectedAmount > 0 and o.codRemittance is null
            order by o.deliveredAt asc
            """)
    List<Order> findCodPendingRemittance(@Param("shipper") Shipper shipper);

    @Query("""
            select o from Order o left join o.codRemittance r
            where o.shop.id = :shopId and o.settlement is null
              and (o.status = vn.ptit.giaohang.domain.order.OrderStatus.DA_HOAN
                   or (o.status = vn.ptit.giaohang.domain.order.OrderStatus.GIAO_THANH_CONG
                       and (o.collectedAmount = 0
                            or r.status = vn.ptit.giaohang.domain.finance.RemittanceStatus.DA_XAC_NHAN)))
            order by o.createdAt asc
            """)
    List<Order> findReadyForSettlement(@Param("shopId") Long shopId);

    long countByStatus(OrderStatus status);

    long countByShopIdAndStatus(Long shopId, OrderStatus status);

    long countByShopId(Long shopId);

    @Query("""
            select coalesce(sum(o.shippingFee), 0) from Order o
            where o.status in :statuses and o.updatedAt >= :from and o.updatedAt < :to
            """)
    long sumShippingFee(@Param("statuses") Collection<OrderStatus> statuses,
                        @Param("from") LocalDateTime from,
                        @Param("to") LocalDateTime to);

    @Query("""
            select count(o) from Order o
            where o.status = :status and o.updatedAt >= :from and o.updatedAt < :to
            """)
    long countFinishedBetween(@Param("status") OrderStatus status,
                              @Param("from") LocalDateTime from,
                              @Param("to") LocalDateTime to);
}
