package vn.ptit.giaohang.web.admin;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.ptit.giaohang.common.BusinessException;
import vn.ptit.giaohang.common.NotFoundException;
import vn.ptit.giaohang.domain.user.Role;
import vn.ptit.giaohang.domain.user.User;
import vn.ptit.giaohang.dto.ShipperEditForm;
import vn.ptit.giaohang.dto.ShipperForm;
import vn.ptit.giaohang.service.AccountService;
import vn.ptit.giaohang.service.RemittanceService;
import vn.ptit.giaohang.service.ZoneService;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/admin")
public class AdminAccountController {

    private final AccountService accountService;
    private final ZoneService zoneService;
    private final RemittanceService remittanceService;

    public AdminAccountController(AccountService accountService, ZoneService zoneService,
                                  RemittanceService remittanceService) {
        this.accountService = accountService;
        this.zoneService = zoneService;
        this.remittanceService = remittanceService;
    }

    @GetMapping("/shops")
    public String shops(Model model) {
        model.addAttribute("shops", accountService.allShops());
        return "admin/shops";
    }

    @GetMapping("/shippers")
    public String shippers(Model model) {
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new ShipperForm());
        }
        addShipperListData(model);
        return "admin/shippers";
    }

    @PostMapping("/shippers")
    public String createShipper(@Valid @ModelAttribute("form") ShipperForm form, BindingResult result,
                                Model model, RedirectAttributes redirect) {
        if (!result.hasErrors()) {
            try {
                accountService.createShipper(form);
                redirect.addFlashAttribute("success", "Đã tạo tài khoản shipper " + form.getUsername());
                return "redirect:/admin/shippers";
            } catch (BusinessException e) {
                model.addAttribute("error", e.getMessage());
            }
        }
        addShipperListData(model);
        return "admin/shippers";
    }

    @GetMapping("/shippers/{id}/edit")
    public String editShipperForm(@PathVariable Long id, Model model) {
        model.addAttribute("shipper", accountService.getShipper(id));
        model.addAttribute("form", ShipperEditForm.from(accountService.getShipper(id)));
        model.addAttribute("zones", zoneService.allZones());
        return "admin/shipper-edit";
    }

    @PostMapping("/shippers/{id}/edit")
    public String editShipper(@PathVariable Long id, @Valid @ModelAttribute("form") ShipperEditForm form,
                              BindingResult result, Model model, RedirectAttributes redirect) {
        if (!result.hasErrors()) {
            accountService.updateShipper(id, form);
            redirect.addFlashAttribute("success", "Đã cập nhật thông tin shipper " + form.getFullName());
            return "redirect:/admin/shippers";
        }
        model.addAttribute("shipper", accountService.getShipper(id));
        model.addAttribute("zones", zoneService.allZones());
        return "admin/shipper-edit";
    }

    @GetMapping("/users/{id}/password")
    public String resetPasswordForm(@PathVariable Long id, Model model) {
        User user = accountService.getUser(id);
        if (user.getRole() == Role.ADMIN) {
            throw new NotFoundException("Không tìm thấy tài khoản");
        }
        model.addAttribute("user", user);
        return "admin/reset-password";
    }

    @PostMapping("/users/{id}/password")
    public String resetPassword(@PathVariable Long id, @RequestParam String newPassword,
                                RedirectAttributes redirect) {
        accountService.resetPassword(id, newPassword);
        User user = accountService.getUser(id);
        redirect.addFlashAttribute("success", "Đã đặt lại mật khẩu cho " + user.getUsername());
        return "redirect:" + listUrl(user);
    }

    @PostMapping("/users/{id}/active")
    public String setActive(@PathVariable Long id, @RequestParam boolean active, RedirectAttributes redirect) {
        accountService.setActive(id, active);
        redirect.addFlashAttribute("success", active ? "Đã mở khóa tài khoản" : "Đã khóa tài khoản");
        return "redirect:" + listUrl(accountService.getUser(id));
    }

    private static String listUrl(User user) {
        return user.getRole() == Role.SHOP ? "/admin/shops" : "/admin/shippers";
    }

    private void addShipperListData(Model model) {
        var shippers = accountService.allShippers();
        Map<Long, Long> holding = new HashMap<>();
        for (var s : shippers) {
            holding.put(s.getId(), remittanceService.holdingAmount(s.getId()));
        }
        model.addAttribute("shippers", shippers);
        model.addAttribute("holding", holding);
        model.addAttribute("zones", zoneService.allZones());
    }
}
