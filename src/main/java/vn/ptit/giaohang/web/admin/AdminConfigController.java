package vn.ptit.giaohang.web.admin;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.ptit.giaohang.common.BusinessException;
import vn.ptit.giaohang.domain.pricing.PriceConfig;
import vn.ptit.giaohang.domain.pricing.ServiceType;
import vn.ptit.giaohang.dto.PriceConfigForm;
import vn.ptit.giaohang.dto.ZoneForm;
import vn.ptit.giaohang.service.PricingService;
import vn.ptit.giaohang.service.ZoneService;

import vn.ptit.giaohang.domain.zone.Ward;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Cau hinh vung giao hang (gan phuong vao vung) va bang gia cuoc.
 */
@Controller
@RequestMapping("/admin")
public class AdminConfigController {

    private final ZoneService zoneService;
    private final PricingService pricingService;

    public AdminConfigController(ZoneService zoneService, PricingService pricingService) {
        this.zoneService = zoneService;
        this.pricingService = pricingService;
    }

    @GetMapping("/zones")
    public String zones(Model model) {
        if (!model.containsAttribute("zoneForm")) {
            model.addAttribute("zoneForm", new ZoneForm());
        }
        addZoneData(model);
        return "admin/zones";
    }

    private void addZoneData(Model model) {
        List<Ward> wards = zoneService.allWards();
        Map<Long, Long> wardCount = wards.stream()
                .filter(Ward::isServed)
                .collect(Collectors.groupingBy(w -> w.getZone().getId(), Collectors.counting()));
        model.addAttribute("zones", zoneService.allZones());
        model.addAttribute("wards", wards);
        model.addAttribute("wardCount", wardCount);
    }

    @PostMapping("/zones")
    public String createZone(@Valid @ModelAttribute("zoneForm") ZoneForm form, BindingResult result,
                             Model model, RedirectAttributes redirect) {
        if (!result.hasErrors()) {
            try {
                zoneService.createZone(form);
                redirect.addFlashAttribute("success", "Đã tạo vùng " + form.getName());
                return "redirect:/admin/zones";
            } catch (BusinessException e) {
                model.addAttribute("error", e.getMessage());
            }
        }
        addZoneData(model);
        return "admin/zones";
    }

    @PostMapping("/wards/{id}/zone")
    public String assignWard(@PathVariable Long id, @RequestParam(required = false) Long zoneId,
                             RedirectAttributes redirect) {
        zoneService.assignWardToZone(id, zoneId);
        redirect.addFlashAttribute("success", "Đã cập nhật vùng cho " + zoneService.getWard(id));
        return "redirect:/admin/zones#ward-" + id;
    }

    @GetMapping("/prices")
    public String prices(Model model) {
        Map<ServiceType, PriceConfigForm> forms = new EnumMap<>(ServiceType.class);
        for (PriceConfig config : pricingService.allConfigs()) {
            forms.put(config.getServiceType(), PriceConfigForm.from(config));
        }
        model.addAttribute("forms", forms);
        return "admin/prices";
    }

    @PostMapping("/prices/{type}")
    public String updatePrice(@PathVariable ServiceType type, @Valid @ModelAttribute PriceConfigForm form,
                              BindingResult result, RedirectAttributes redirect) {
        if (result.hasErrors()) {
            throw new BusinessException("Giá trị bảng giá không hợp lệ, vui lòng kiểm tra lại");
        }
        pricingService.updateConfig(type, form);
        redirect.addFlashAttribute("success", "Đã cập nhật bảng giá dịch vụ " + type.getLabel());
        return "redirect:/admin/prices";
    }
}
