package vn.ptit.giaohang.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.support.RequestContextUtils;
import vn.ptit.giaohang.common.BusinessException;
import vn.ptit.giaohang.common.NotFoundException;

import java.net.URI;

/**
 * Loi nghiep vu: quay lai trang truoc va hien thong bao loi.
 * Loi khong tim thay: hien trang 404.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public String handleBusiness(BusinessException ex, HttpServletRequest request) {
        RequestContextUtils.getOutputFlashMap(request).put("error", ex.getMessage());
        return "redirect:" + backUrl(request);
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound(NotFoundException ex, Model model) {
        model.addAttribute("message", ex.getMessage());
        return "error/404";
    }

    /**
     * Chi lay phan duong dan cua Referer (bo host) de khong the bi loi dung chuyen huong sang trang ngoai.
     */
    private static String backUrl(HttpServletRequest request) {
        String referer = request.getHeader("Referer");
        if (referer == null) {
            return sectionHome(request);
        }
        try {
            URI uri = URI.create(referer);
            String path = uri.getRawPath();
            if (path == null || path.isEmpty() || !path.startsWith("/") || path.startsWith("//")) {
                return "/";
            }
            String contextPath = request.getContextPath();
            if (!contextPath.isEmpty() && path.startsWith(contextPath)) {
                path = path.substring(contextPath.length());
            }
            return uri.getRawQuery() == null ? path : path + "?" + uri.getRawQuery();
        } catch (IllegalArgumentException e) {
            return sectionHome(request);
        }
    }

    /** Trang chu cua khu vuc dang thao tac (/shop, /shipper, /admin) de thong bao loi khong bi mat qua nhieu lan chuyen huong. */
    private static String sectionHome(HttpServletRequest request) {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        for (String section : new String[]{"/shop", "/shipper", "/admin"}) {
            if (path.startsWith(section + "/")) {
                return section;
            }
        }
        return "/";
    }
}
