package vn.ptit.giaohang.web.admin;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.ptit.giaohang.common.BusinessException;
import vn.ptit.giaohang.domain.user.Role;
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

    @PostMapping("/users/{id}/active")
    public String setActive(@PathVariable Long id, @RequestParam boolean active, RedirectAttributes redirect) {
        accountService.setActive(id, active);
        redirect.addFlashAttribute("success", active ? "Đã mở khóa tài khoản" : "Đã khóa tài khoản");
        return "redirect:" + (accountService.getUser(id).getRole() == Role.SHOP ? "/admin/shops" : "/admin/shippers");
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
