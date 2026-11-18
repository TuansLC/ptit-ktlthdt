package vn.ptit.giaohang.common;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Sinh ma chung tu dang PREFIX + yyMMdd + so thu tu 5 chu so, vi du GH25111800012.
 */
public final class CodeGenerator {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyMMdd");

    private CodeGenerator() {
    }

    public static String generate(String prefix, long id) {
        return generate(prefix, LocalDate.now(), id);
    }

    public static String generate(String prefix, LocalDate date, long id) {
        return prefix + date.format(DATE) + String.format("%05d", id);
    }
}
