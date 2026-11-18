package vn.ptit.giaohang.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.util.UriComponentsBuilder;
import vn.ptit.giaohang.domain.order.OrderAction;
import vn.ptit.giaohang.domain.order.OrderStatus;
import vn.ptit.giaohang.domain.pricing.ServiceType;

import java.util.HashMap;
import java.util.Map;

/**
 * Gia tri dung chung cho moi template: enum, duong dan hien tai (to dam menu), co so URL phan trang.
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

    /** Duong dan hien tai, khong gom context path, vi du "/shop/orders". */
    @ModelAttribute("currentPath")
    public String currentPath(HttpServletRequest request) {
        return request.getRequestURI().substring(request.getContextPath().length());
    }

    /**
     * URL trang hien tai giu nguyen bo loc nhung bo tham so page, ket thuc bang '?' hoac '&'.
     * Template chi can noi them "page=" + so trang.
     */
    @ModelAttribute("pageUrlBase")
    public String pageUrlBase(HttpServletRequest request) {
        String url = UriComponentsBuilder.fromPath(currentPath(request))
                .query(request.getQueryString())
                .replaceQueryParam("page")
                .build()
                .toUriString();
        return url + (url.contains("?") ? "&" : "?");
    }
}
