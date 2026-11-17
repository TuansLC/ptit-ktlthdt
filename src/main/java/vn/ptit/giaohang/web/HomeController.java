package vn.ptit.giaohang.web;

import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.ptit.giaohang.common.BusinessException;
import vn.ptit.giaohang.dto.RegisterShopForm;
import vn.ptit.giaohang.security.AppUserDetails;
import vn.ptit.giaohang.service.AccountService;
import vn.ptit.giaohang.service.ZoneService;

@Controller
public class HomeController {

    private final AccountService accountService;
    private final ZoneService zoneService;

    public HomeController(AccountService accountService, ZoneService zoneService) {
        this.accountService = accountService;
        this.zoneService = zoneService;
    }

    @GetMapping("/")
    public String home(@AuthenticationPrincipal AppUserDetails user) {
        return "redirect:" + user.getRole().getHomeUrl();
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("form", new RegisterShopForm());
        model.addAttribute("wards", zoneService.servedWards());
        return "register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("form") RegisterShopForm form, BindingResult result,
                           Model model, RedirectAttributes redirect) {
        if (!result.hasErrors()) {
            try {
                accountService.registerShop(form);
                redirect.addFlashAttribute("success", "Đăng ký thành công, vui lòng đăng nhập");
                return "redirect:/login";
            } catch (BusinessException e) {
                model.addAttribute("error", e.getMessage());
            }
        }
        model.addAttribute("wards", zoneService.servedWards());
        return "register";
    }
}
