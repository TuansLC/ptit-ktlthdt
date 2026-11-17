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

    /** Mac dinh thong ke tu dau thang den hom nay. */
    @GetMapping
    public String dashboard(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                            Model model) {
        LocalDate today = LocalDate.now();
        if (to == null) {
            to = today;
        }
        if (from == null) {
            from = to.withDayOfMonth(1);
        }
        if (from.isAfter(to)) {
            throw new BusinessException("Ngày bắt đầu phải trước ngày kết thúc");
        }
        model.addAttribute("stats", statisticsService.adminStats(from, to));
        return "admin/dashboard";
    }
}
