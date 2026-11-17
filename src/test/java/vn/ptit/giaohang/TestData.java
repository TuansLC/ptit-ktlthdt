package vn.ptit.giaohang;

import vn.ptit.giaohang.domain.order.Order;
import vn.ptit.giaohang.domain.pricing.FeeQuote;
import vn.ptit.giaohang.domain.pricing.PriceConfig;
import vn.ptit.giaohang.domain.pricing.ServiceType;
import vn.ptit.giaohang.domain.user.Shipper;
import vn.ptit.giaohang.domain.user.Shop;
import vn.ptit.giaohang.domain.zone.DeliveryZone;
import vn.ptit.giaohang.domain.zone.Ward;

/**
 * Doi tuong mau cho unit test, khong can CSDL.
 */
public final class TestData {

    public static final DeliveryZone ZONE_1 = new DeliveryZone("V1", "Vùng 1", null);
    public static final DeliveryZone ZONE_2 = new DeliveryZone("V2", "Vùng 2", null);

    public static final Ward CAU_GIAY = new Ward("Cầu Giấy", ZONE_1);
    public static final Ward KIM_LIEN = new Ward("Kim Liên", ZONE_1);
    public static final Ward HA_DONG = new Ward("Hà Đông", ZONE_2);
    public static final Ward SON_TAY = new Ward("Sơn Tây", null);

    private TestData() {
    }

    public static PriceConfig standardPrice() {
        return new PriceConfig(ServiceType.TIEU_CHUAN, 20_000, 30_000, 2_000, 500, 2_500, 30_000, 0);
    }

    public static PriceConfig expressPrice() {
        return new PriceConfig(ServiceType.HOA_TOC, 35_000, 35_000, 2_000, 500, 5_000, 5_000, 15_000);
    }

    public static Shop shop() {
        return new Shop("shop", "x", "Chủ shop", "0900000000", "Shop A", "1 Trần Duy Hưng", CAU_GIAY);
    }

    public static Shipper shipper() {
        return new Shipper("shipper", "x", "Shipper A", "0900000001", "29B1", ZONE_1);
    }

    public static Order order(Shop shop, long cod) {
        FeeQuote fee = new FeeQuote(ServiceType.TIEU_CHUAN, true, 20_000, 0, 0);
        return new Order(shop, "1 Trần Duy Hưng", CAU_GIAY, "Người nhận", "0911111111", "2 Xã Đàn", KIM_LIEN,
                "Áo", 500, cod, fee, null);
    }
}
