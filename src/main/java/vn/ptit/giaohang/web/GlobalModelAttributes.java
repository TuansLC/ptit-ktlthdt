package vn.ptit.giaohang.web;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import vn.ptit.giaohang.domain.order.OrderAction;
import vn.ptit.giaohang.domain.order.OrderStatus;
import vn.ptit.giaohang.domain.pricing.ServiceType;

import java.util.HashMap;
import java.util.Map;

/**
 * Cac gia tri enum dung chung cho moi template (bo loc trang thai, danh sach dich vu...).
 */
@ControllerAdvice
public class GlobalModelAttributes {

    @ModelAttribute("allStatuses")
    public OrderStatus[] allStatuses() {
        return OrderStatus.values();
    }

    @ModelAttribute("serviceTypes")
    public ServiceType[] serviceTypes() {
        return ServiceType.values();
    }

    /** Dung trong template: ${order.allows(act['ASSIGN'])}. */
    @ModelAttribute("act")
    public Map<String, OrderAction> actions() {
        Map<String, OrderAction> map = new HashMap<>();
        for (OrderAction action : OrderAction.values()) {
            map.put(action.name(), action);
        }
        return map;
    }
}
