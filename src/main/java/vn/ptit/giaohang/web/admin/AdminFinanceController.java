package vn.ptit.giaohang.web.admin;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.ptit.giaohang.domain.finance.Settlement;
import vn.ptit.giaohang.security.AppUserDetails;
import vn.ptit.giaohang.service.RemittanceService;
import vn.ptit.giaohang.service.SettlementService;

/**
 * Xac nhan tien COD shipper nop ve va doi soat, thanh toan cho shop.
 */
@Controller
@RequestMapping("/admin")
public class AdminFinanceController {

    private final RemittanceService remittanceService;
    private final SettlementService settlementService;

    public AdminFinanceController(RemittanceService remittanceService, SettlementService settlementService) {
        this.remittanceService = remittanceService;
        this.settlementService = settlementService;
    }

    // ---------------- Phieu nop COD ----------------

    @GetMapping("/remittances")
    public String remittances(Model model) {
        model.addAttribute("remittances", remittanceService.listAll());
        return "admin/remittances";
    }

    @GetMapping("/remittances/{id}")
    public String remittanceDetail(@PathVariable Long id, Model model) {
        model.addAttribute("remittance", remittanceService.get(id));
        return "admin/remittance-detail";
    }

    @PostMapping("/remittances/{id}/confirm")
    public String confirm(@AuthenticationPrincipal AppUserDetails user, @PathVariable Long id, RedirectAttributes r) {
        remittanceService.confirm(user.getId(), id);
        r.addFlashAttribute("success", "Đã xác nhận nhận tiền");
        return "redirect:/admin/remittances/" + id;
    }

    @PostMapping("/remittances/{id}/reject")
    public String reject(@AuthenticationPrincipal AppUserDetails user, @PathVariable Long id,
                         @RequestParam String reason, RedirectAttributes r) {
        remittanceService.reject(user.getId(), id, reason);
        r.addFlashAttribute("success", "Đã từ chối phiếu, các đơn được trả lại để shipper nộp lại");
        return "redirect:/admin/remittances/" + id;
    }

    // ---------------- Doi soat shop ----------------

    @GetMapping("/settlements")
    public String settlements(Model model) {
        model.addAttribute("summaries", settlementService.readySummaries());
        model.addAttribute("settlements", settlementService.listAll());
        return "admin/settlements";
    }

    @PostMapping("/settlements")
    public String create(@RequestParam Long shopId, @RequestParam(required = false) String note, RedirectAttributes r) {
        Settlement settlement = settlementService.create(shopId, note);
        r.addFlashAttribute("success", "Đã lập phiếu đối soát " + settlement.getCode());
        return "redirect:/admin/settlements/" + settlement.getId();
    }

    @GetMapping("/settlements/{id}")
    public String settlementDetail(@PathVariable Long id, Model model) {
        model.addAttribute("settlement", settlementService.get(id));
        return "admin/settlement-detail";
    }

    @PostMapping("/settlements/{id}/pay")
    public String pay(@PathVariable Long id, @RequestParam String paymentRef, RedirectAttributes r) {
        settlementService.markPaid(id, paymentRef);
        r.addFlashAttribute("success", "Đã ghi nhận thanh toán");
        return "redirect:/admin/settlements/" + id;
    }
}
