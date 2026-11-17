package vn.ptit.giaohang.web;

import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.ptit.giaohang.common.BusinessException;
import vn.ptit.giaohang.domain.order.Order;
import vn.ptit.giaohang.domain.order.OrderStatus;
import vn.ptit.giaohang.domain.pricing.FeeQuote;
import vn.ptit.giaohang.domain.pricing.ServiceType;
import vn.ptit.giaohang.domain.user.Shop;
import vn.ptit.giaohang.dto.OrderForm;
import vn.ptit.giaohang.dto.ShopProfileForm;
import vn.ptit.giaohang.security.AppUserDetails;
import vn.ptit.giaohang.service.*;

import java.util.LinkedHashMap;
import java.util.Map;

@Controller
@RequestMapping("/shop")
public class ShopController {

    private final AccountService accountService;
    private final OrderService orderService;
    private final ZoneService zoneService;
    private final SettlementService settlementService;
    private final StatisticsService statisticsService;
    private final ViewFormatter fmt;

    public ShopController(AccountService accountService, OrderService orderService, ZoneService zoneService,
                          SettlementService settlementService, StatisticsService statisticsService,
                          ViewFormatter fmt) {
        this.accountService = accountService;
        this.orderService = orderService;
        this.zoneService = zoneService;
        this.settlementService = settlementService;
        this.statisticsService = statisticsService;
        this.fmt = fmt;
    }

    @GetMapping
    public String dashboard(@AuthenticationPrincipal AppUserDetails user, Model model) {
        Shop shop = accountService.getShop(user.getId());
        model.addAttribute("shop", shop);
        model.addAttribute("counts", statisticsService.shopCountByStatus(shop.getId()));
        model.addAttribute("ready", settlementService.summarize(shop));
        model.addAttribute("recentOrders", orderService.searchForShop(shop.getId(), null, null)
                .stream().limit(5).toList());
        return "shop/dashboard";
    }

    // ---------------- Don hang ----------------

    @GetMapping("/orders")
    public String orders(@AuthenticationPrincipal AppUserDetails user,
                         @RequestParam(required = false) OrderStatus status,
                         @RequestParam(required = false) String keyword, Model model) {
        model.addAttribute("orders", orderService.searchForShop(user.getId(), status, keyword));
        model.addAttribute("status", status);
        model.addAttribute("keyword", keyword);
        return "shop/orders";
    }

    @GetMapping("/orders/new")
    public String newOrder(@AuthenticationPrincipal AppUserDetails user, Model model) {
        Shop shop = accountService.getShop(user.getId());
        OrderForm form = new OrderForm();
        form.setPickupAddress(shop.getAddress());
        form.setPickupWardId(shop.getWard().getId());
        form.setCodAmount(0L);
        model.addAttribute("form", form);
        model.addAttribute("wards", zoneService.servedWards());
        return "shop/order-form";
    }

    @PostMapping("/orders")
    public String createOrder(@AuthenticationPrincipal AppUserDetails user,
                              @Valid @ModelAttribute("form") OrderForm form, BindingResult result,
                              Model model, RedirectAttributes redirect) {
        if (!result.hasErrors()) {
            try {
                Order order = orderService.createOrder(user.getId(), form);
                redirect.addFlashAttribute("success", "Đã tạo đơn " + order.getCode()
                        + ", phí vận chuyển " + fmt.money(order.getShippingFee()));
                return "redirect:/shop/orders/" + order.getId();
            } catch (BusinessException e) {
                model.addAttribute("error", e.getMessage());
            }
        }
        model.addAttribute("wards", zoneService.servedWards());
        return "shop/order-form";
    }

    /** Tinh phi tam tinh khi shop dang nhap form (goi bang fetch tu trinh duyet). */
    @GetMapping("/orders/quote")
    @ResponseBody
    public Map<String, Object> quote(@RequestParam Long pickupWardId, @RequestParam Long deliveryWardId,
                                     @RequestParam int weightGram, @RequestParam ServiceType serviceType) {
        Map<String, Object> body = new LinkedHashMap<>();
        try {
            FeeQuote q = orderService.quote(pickupWardId, deliveryWardId, weightGram, serviceType);
            body.put("ok", true);
            body.put("route", q.routeLabel());
            body.put("baseFee", fmt.money(q.baseFee()));
            body.put("weightFee", fmt.money(q.weightFee()));
            body.put("surcharge", fmt.money(q.surcharge()));
            body.put("total", fmt.money(q.total()));
        } catch (BusinessException e) {
            body.put("ok", false);
            body.put("message", e.getMessage());
        }
        return body;
    }

    @GetMapping("/orders/{id}")
    public String orderDetail(@AuthenticationPrincipal AppUserDetails user, @PathVariable Long id, Model model) {
        Order order = orderService.findForShop(user.getId(), id);
        model.addAttribute("order", order);
        model.addAttribute("history", orderService.history(order));
        return "shop/order-detail";
    }

    @PostMapping("/orders/{id}/cancel")
    public String cancel(@AuthenticationPrincipal AppUserDetails user, @PathVariable Long id,
                         RedirectAttributes redirect) {
        orderService.cancelByShop(user.getId(), id);
        redirect.addFlashAttribute("success", "Đã hủy đơn");
        return "redirect:/shop/orders/" + id;
    }

    // ---------------- Doi soat ----------------

    @GetMapping("/settlements")
    public String settlements(@AuthenticationPrincipal AppUserDetails user, Model model) {
        Shop shop = accountService.getShop(user.getId());
        model.addAttribute("settlements", settlementService.listForShop(shop.getId()));
        model.addAttribute("ready", settlementService.summarize(shop));
        model.addAttribute("readyOrders", settlementService.readyOrders(shop.getId()));
        return "shop/settlements";
    }

    @GetMapping("/settlements/{id}")
    public String settlementDetail(@AuthenticationPrincipal AppUserDetails user, @PathVariable Long id, Model model) {
        model.addAttribute("settlement", settlementService.getForShop(user.getId(), id));
        return "shop/settlement-detail";
    }

    // ---------------- Thong tin shop ----------------

    @GetMapping("/profile")
    public String profile(@AuthenticationPrincipal AppUserDetails user, Model model) {
        model.addAttribute("form", ShopProfileForm.from(accountService.getShop(user.getId())));
        model.addAttribute("wards", zoneService.servedWards());
        return "shop/profile";
    }

    @PostMapping("/profile")
    public String updateProfile(@AuthenticationPrincipal AppUserDetails user,
                                @Valid @ModelAttribute("form") ShopProfileForm form, BindingResult result,
                                Model model, RedirectAttributes redirect) {
        if (!result.hasErrors()) {
            try {
                accountService.updateShopProfile(user.getId(), form);
                redirect.addFlashAttribute("success", "Đã cập nhật thông tin shop");
                return "redirect:/shop/profile";
            } catch (BusinessException e) {
                model.addAttribute("error", e.getMessage());
            }
        }
        model.addAttribute("wards", zoneService.servedWards());
        return "shop/profile";
    }
}
