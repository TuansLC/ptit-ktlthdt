package vn.ptit.giaohang.web;

import org.springframework.stereotype.Component;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Ham dinh dang dung trong template Thymeleaf, goi qua ${@fmt.money(...)}.
 */
@Component("fmt")
public class ViewFormatter {

    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final DecimalFormat moneyFormat;

    public ViewFormatter() {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        symbols.setGroupingSeparator('.');
        moneyFormat = new DecimalFormat("#,##0", symbols);
    }

    public String money(long amount) {
        synchronized (moneyFormat) {
            return moneyFormat.format(amount) + " đ";
        }
    }

    public String dateTime(LocalDateTime time) {
        return time == null ? "" : time.format(DATE_TIME);
    }

    public String weight(int gram) {
        if (gram < 1000) {
            return gram + " g";
        }
        return String.format(Locale.US, "%.1f kg", gram / 1000.0).replace(".0 kg", " kg").replace('.', ',');
    }
}
