package vn.ptit.giaohang.web.admin;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.ptit.giaohang.domain.order.Order;
import vn.ptit.giaohang.domain.order.OrderStatus;
import vn.ptit.giaohang.security.AppUserDetails;
import vn.ptit.giaohang.service.AccountService;
import vn.ptit.giaohang.service.OrderService;

@Controller
@RequestMapping("/admin/orders")
public class AdminOrderController {

    private final OrderService orderService;
    private final AccountService accountService;

    public AdminOrderController(OrderService orderService, AccountService accountService) {
        this.orderService = orderService;
        this.accountService = accountService;
    }

    @GetMapping
    public String list(@RequestParam(required = false) OrderStatus status,
                       @RequestParam(required = false) Long shopId,
                       @RequestParam(required = false) Long shipperId,
                       @RequestParam(required = false) String keyword, Model model) {
        model.addAttribute("orders", orderService.searchForAdmin(status, shopId, shipperId, keyword));
        model.addAttribute("shops", accountService.allShops());
        model.addAttribute("shippers", accountService.allShippers());
        model.addAttribute("activeShippers", accountService.activeShippers());
        model.addAttribute("status", status);
        model.addAttribute("shopId", shopId);
        model.addAttribute("shipperId", shipperId);
        model.addAttribute("keyword", keyword);
        return "admin/orders";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Order order = orderService.findById(id);
        model.addAttribute("order", order);
        model.addAttribute("history", orderService.history(order));
        model.addAttribute("activeShippers", accountService.activeShippers());
        return "admin/order-detail";
    }

    @PostMapping("/{id}/assign")
    public String assign(@AuthenticationPrincipal AppUserDetails user, @PathVariable Long id,
                         @RequestParam Long shipperId, @RequestParam(required = false) String back,
                         RedirectAttributes r) {
        orderService.assign(user.getId(), id, shipperId);
        r.addFlashAttribute("success", "Đã phân công shipper");
        return redirectBack(back, id);
    }

    @PostMapping("/{id}/retry")
    public String retry(@AuthenticationPrincipal AppUserDetails user, @PathVariable Long id,
                        @RequestParam Long shipperId, RedirectAttributes r) {
        orderService.retry(user.getId(), id, shipperId);
        r.addFlashAttribute("success", "Đã chuyển đơn sang giao lại");
        return "redirect:/admin/orders/" + id;
    }

    @PostMapping("/{id}/return")
    public String startReturn(@AuthenticationPrincipal AppUserDetails user, @PathVariable Long id,
                              RedirectAttributes r) {
        orderService.startReturn(user.getId(), id);
        r.addFlashAttribute("success", "Đã chuyển hoàn đơn về shop");
        return "redirect:/admin/orders/" + id;
    }

    /** Phan cong nhanh tu danh sach thi quay lai danh sach; con lai quay ve trang chi tiet. */
    private static String redirectBack(String back, Long id) {
        return "list".equals(back) ? "redirect:/admin/orders?status=CHO_PHAN_CONG" : "redirect:/admin/orders/" + id;
    }
}
