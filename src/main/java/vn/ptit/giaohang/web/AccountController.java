package vn.ptit.giaohang.web;

import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.ptit.giaohang.common.BusinessException;
import vn.ptit.giaohang.dto.ChangePasswordForm;
import vn.ptit.giaohang.security.AppUserDetails;
import vn.ptit.giaohang.service.AccountService;

/**
 * Chuc nang dung chung cho moi vai tro da dang nhap.
 */
@Controller
@RequestMapping("/account")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/password")
    public String passwordForm(Model model) {
        model.addAttribute("form", new ChangePasswordForm());
        return "account/password";
    }

    @PostMapping("/password")
    public String changePassword(@AuthenticationPrincipal AppUserDetails user,
                                 @Valid @ModelAttribute("form") ChangePasswordForm form, BindingResult result,
                                 Model model, RedirectAttributes redirect) {
        if (!result.hasErrors()) {
            try {
                accountService.changePassword(user.getId(), form.getCurrentPassword(), form.getNewPassword());
                redirect.addFlashAttribute("success", "Đã đổi mật khẩu");
                return "redirect:/account/password";
            } catch (BusinessException e) {
                model.addAttribute("error", e.getMessage());
            }
        }
        return "account/password";
    }
}
