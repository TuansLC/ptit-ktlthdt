package vn.ptit.giaohang.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.ptit.giaohang.domain.order.Order;
import vn.ptit.giaohang.domain.order.OrderStatusHistory;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderStatusHistoryRepository extends JpaRepository<OrderStatusHistory, Long> {

    List<OrderStatusHistory> findByOrderOrderByCreatedAtAscIdAsc(Order order);

    /**
     * Chi dung khi tao du lieu mau: dong "tao don" lay moc created, cac dong con lai lay moc finished.
     * Thu tu hien thi van dung vi sap xep phu theo id.
     */
    @Modifying
    @Query("""
            update OrderStatusHistory h
            set h.createdAt = case when h.fromStatus is null then :created else :finished end
            where h.order.id = :orderId
            """)
    int backdate(@Param("orderId") Long orderId, @Param("created") LocalDateTime created,
                 @Param("finished") LocalDateTime finished);
}
