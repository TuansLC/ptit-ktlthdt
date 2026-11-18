package vn.ptit.giaohang.web.admin;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import vn.ptit.giaohang.common.BusinessException;
import vn.ptit.giaohang.service.StatisticsService;

import java.time.LocalDate;

@Controller
@RequestMapping("/admin")
public class AdminDashboardController {

    private final StatisticsService statisticsService;

    public AdminDashboardController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    private static final int DEFAULT_RANGE_DAYS = 30;

    /** Mac dinh thong ke 30 ngay gan nhat, tinh ca hom nay. */
    @GetMapping
    public String dashboard(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                            Model model) {
        LocalDate today = LocalDate.now();
        if (to == null) {
            to = today;
        }
        if (from == null) {
            from = to.minusDays(DEFAULT_RANGE_DAYS - 1);
        }
        try {
            model.addAttribute("stats", statisticsService.adminStats(from, to));
        } catch (BusinessException e) {
            // Khoang ngay khong hop le: bao loi va hien thang hien tai
            model.addAttribute("error", e.getMessage());
            model.addAttribute("stats", statisticsService.adminStats(today.minusDays(DEFAULT_RANGE_DAYS - 1), today));
        }
        return "admin/dashboard";
    }
}
