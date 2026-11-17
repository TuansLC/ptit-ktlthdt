package vn.ptit.giaohang.web;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.ptit.giaohang.domain.finance.CodRemittance;
import vn.ptit.giaohang.domain.order.Order;
import vn.ptit.giaohang.security.AppUserDetails;
import vn.ptit.giaohang.service.OrderService;
import vn.ptit.giaohang.service.RemittanceService;

@Controller
@RequestMapping("/shipper")
public class ShipperController {

    private final OrderService orderService;
    private final RemittanceService remittanceService;
    private final ViewFormatter fmt;

    public ShipperController(OrderService orderService, RemittanceService remittanceService, ViewFormatter fmt) {
        this.orderService = orderService;
        this.remittanceService = remittanceService;
        this.fmt = fmt;
    }

    /** Trang chinh: cac don dang can xu ly va so tien COD dang giu. */
    @GetMapping
    public String activeOrders(@AuthenticationPrincipal AppUserDetails user, Model model) {
        model.addAttribute("orders", orderService.activeForShipper(user.getId()));
        model.addAttribute("holding", remittanceService.holdingAmount(user.getId()));
        return "shipper/orders";
    }

    @GetMapping("/history")
    public String history(@AuthenticationPrincipal AppUserDetails user, Model model) {
        model.addAttribute("orders", orderService.historyForShipper(user.getId()));
        return "shipper/history";
    }

    @GetMapping("/orders/{id}")
    public String detail(@AuthenticationPrincipal AppUserDetails user, @PathVariable Long id, Model model) {
        Order order = orderService.findForShipper(user.getId(), id);
        model.addAttribute("order", order);
        model.addAttribute("history", orderService.history(order));
        return "shipper/order-detail";
    }

    @PostMapping("/orders/{id}/pickup")
    public String pickUp(@AuthenticationPrincipal AppUserDetails user, @PathVariable Long id, RedirectAttributes r) {
        orderService.pickUp(user.getId(), id);
        r.addFlashAttribute("success", "Đã xác nhận lấy hàng");
        return "redirect:/shipper/orders/" + id;
    }

    @PostMapping("/orders/{id}/success")
    public String success(@AuthenticationPrincipal AppUserDetails user, @PathVariable Long id,
                          @RequestParam(defaultValue = "0") long collectedAmount, RedirectAttributes r) {
        orderService.deliverSuccess(user.getId(), id, collectedAmount);
        r.addFlashAttribute("success", "Đã giao thành công");
        return "redirect:/shipper/orders/" + id;
    }

    @PostMapping("/orders/{id}/fail")
    public String fail(@AuthenticationPrincipal AppUserDetails user, @PathVariable Long id,
                       @RequestParam String reason, RedirectAttributes r) {
        orderService.deliverFail(user.getId(), id, reason);
        r.addFlashAttribute("success", "Đã ghi nhận giao thất bại");
        return "redirect:/shipper/orders/" + id;
    }

    @PostMapping("/orders/{id}/returned")
    public String returned(@AuthenticationPrincipal AppUserDetails user, @PathVariable Long id, RedirectAttributes r) {
        orderService.confirmReturned(user.getId(), id);
        r.addFlashAttribute("success", "Đã xác nhận hoàn hàng cho shop");
        return "redirect:/shipper/orders/" + id;
    }

    // ---------------- Tien COD ----------------

    @GetMapping("/cod")
    public String cod(@AuthenticationPrincipal AppUserDetails user, Model model) {
        model.addAttribute("pendingOrders", remittanceService.pendingOrders(user.getId()));
        model.addAttribute("holding", remittanceService.holdingAmount(user.getId()));
        model.addAttribute("remittances", remittanceService.listForShipper(user.getId()));
        return "shipper/cod";
    }

    @PostMapping("/cod/submit")
    public String submit(@AuthenticationPrincipal AppUserDetails user,
                         @RequestParam(required = false) String note, RedirectAttributes r) {
        CodRemittance remittance = remittanceService.submit(user.getId(), note);
        r.addFlashAttribute("success", "Đã lập phiếu " + remittance.getCode() + " với số tiền "
                + fmt.money(remittance.getTotalAmount()) + ", chờ quản trị viên xác nhận");
        return "redirect:/shipper/cod";
    }

    @GetMapping("/cod/{id}")
    public String remittanceDetail(@AuthenticationPrincipal AppUserDetails user, @PathVariable Long id, Model model) {
        model.addAttribute("remittance", remittanceService.getForShipper(user.getId(), id));
        return "shipper/remittance-detail";
    }
}
