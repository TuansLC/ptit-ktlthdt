# Hệ thống quản lý giao hàng nội thành Hà Nội và đối soát tiền thu hộ (COD)

Đề tài môn Kỹ thuật lập trình hướng đối tượng. Ứng dụng web Java Spring Boot + Thymeleaf.

## Chạy ứng dụng

Yêu cầu: JDK 17 trở lên, Maven 3.9.

```powershell
mvn spring-boot:run
```

Mở http://localhost:8080. Mặc định ứng dụng dùng H2 lưu file trong thư mục `data/`, không cần cài thêm gì.
Lần chạy đầu tiên hệ thống tự tạo dữ liệu mẫu: 2 vùng giao hàng, 51 phường Hà Nội, bảng giá, tài khoản và 9 đơn ở các trạng thái khác nhau.
Muốn tạo lại dữ liệu mẫu từ đầu: tắt ứng dụng, xóa thư mục `data/`, chạy lại.

### Chạy với MySQL

```powershell
$env:DB_USERNAME = "root"
$env:DB_PASSWORD = "<mật khẩu MySQL>"
mvn spring-boot:run "-Dspring-boot.run.profiles=mysql"
```

CSDL `giaohang` được tạo tự động nếu chưa có. Có thể đổi host/port/tên CSDL qua `DB_HOST`, `DB_PORT`, `DB_NAME`.

### Tài khoản mẫu (mật khẩu đều là `123456`, chỉ dùng để demo)

| Tài khoản | Vai trò | Ghi chú |
|---|---|---|
| `admin` | Quản trị viên | Điều phối, đối soát, cấu hình |
| `shop_hoa` | Shop | Hoa Linh Fashion, phường Yên Hòa |
| `shop_minh` | Shop | Minh Tech Phụ Kiện, phường Hà Đông |
| `shipper_an` | Shipper | Phụ trách Vùng 1 |
| `shipper_binh` | Shipper | Phụ trách Vùng 2 |

## Kịch bản demo gợi ý

1. `shop_hoa` tạo đơn mới: chọn phường giao, nhập khối lượng, xem phí tạm tính thay đổi theo dịch vụ. Thử chọn Hỏa tốc với phường giao ở Vùng 2 để thấy hệ thống từ chối.
2. `admin` vào **Đơn hàng**, lọc "Chờ phân công", phân công cho `shipper_an`.
3. `shipper_an` xác nhận lấy hàng, rồi giao thành công. Thử nhập sai số tiền thu hộ để thấy hệ thống chặn.
4. `shipper_an` vào **Tiền COD**, lập phiếu nộp tiền.
5. `admin` vào **Nộp COD**, xác nhận phiếu; vào **Đối soát**, lập phiếu cho shop và ghi nhận chuyển khoản.
6. `shop_hoa` vào **Đối soát** xem phiếu và mã giao dịch.
7. Với đơn giao thất bại, `admin` cho giao lại tối đa 3 lần, sau đó chỉ còn nút **Chuyển hoàn**.

## Cấu trúc mã nguồn

```
src/main/java/vn/ptit/giaohang
├── domain            Lớp nghiệp vụ (entity + quy tắc), không phụ thuộc giao diện
│   ├── user          User (trừu tượng) → Shop, Shipper, Admin
│   ├── zone          DeliveryZone, Ward
│   ├── pricing       ShippingFeeStrategy và các cách tính phí
│   ├── order         Order, OrderStatus, các lớp OrderState
│   └── finance       Voucher (trừu tượng) → CodRemittance, Settlement
├── repository        Spring Data JPA
├── service           Điều phối ca sử dụng, transaction, sự kiện
├── security          Đăng nhập, phân quyền theo vai trò
├── web               Controller (shop, shipper, admin), xử lý lỗi
├── dto               Form nhập liệu có kiểm tra @Valid
└── config            Dữ liệu mẫu
docs/uml              Biểu đồ UML dạng PlantUML
```

## Áp dụng lập trình hướng đối tượng

| Nguyên lý / mẫu | Vị trí trong mã | Giải thích ngắn |
|---|---|---|
| Trừu tượng, kế thừa | `domain/user/User.java` và 3 lớp con | Phần chung nằm ở `User`; mỗi lớp con tự khai báo `getRole()` |
| Trừu tượng, kế thừa | `domain/finance/Voucher.java` | Phiếu nộp COD và phiếu đối soát dùng chung mã phiếu, ngày tạo, cách sinh mã |
| Đa hình | `User.getRole()`, `Voucher.getAmount()`, `OrderState` | Cùng một lời gọi, mỗi lớp xử lý theo cách riêng |
| Đóng gói | `domain/order/Order.java` | Không có setter công khai. Trạng thái chỉ đổi qua các phương thức nghiệp vụ; hàm thay đổi nội bộ để package-private |
| State | `domain/order/*State.java`, `OrderStatus` | Mỗi trạng thái là một lớp, tự quyết định thao tác hợp lệ. `Order` không có `switch` theo trạng thái |
| Strategy | `domain/pricing/ShippingFeeStrategy.java` | Mỗi dịch vụ một cách tính phí; thêm dịch vụ mới không sửa code cũ |
| Template Method | `domain/pricing/AbstractShippingFeeStrategy.java` | Cố định trình tự tính phí, lớp con chỉ ghi đè bước khác biệt |
| Factory | `domain/pricing/ShippingFeeStrategyFactory.java` | Trả về strategy theo loại dịch vụ |
| Observer | `service/OrderStatusChangedEvent.java`, `OrderHistoryRecorder.java` | Mỗi lần đổi trạng thái phát sự kiện, listener ghi lịch sử |

## Quy tắc nghiệp vụ chính

- Phạm vi phục vụ: các phường nội thành Hà Nội theo địa giới 2 cấp (từ 01/7/2025), chia thành vùng giao hàng. Phường chưa gán vùng nằm ngoài phạm vi.
- Phí = cước cơ bản (cùng vùng / khác vùng) + cước vượt cân (mỗi 500 g, làm tròn lên) + phụ phí dịch vụ.
- Hỏa tốc chỉ nhận đơn lấy và giao trong cùng một vùng, tối đa 5 kg.
- Shop chỉ hủy được đơn trước khi shipper lấy hàng.
- Shipper phải nhập đúng số tiền thu hộ khi xác nhận giao thành công.
- Mỗi đơn giao thất bại tối đa 3 lần, sau đó phải chuyển hoàn.
- Đơn giao thành công chỉ được đối soát khi tiền COD đã được quản trị viên xác nhận nhận từ shipper.
- Tiền trả shop = tổng COD đơn giao thành công - tổng phí vận chuyển (cả đơn hoàn). Nếu shop được nhận tiền thì phải có tài khoản ngân hàng.

## Kiểm thử

```powershell
mvn test
```

29 test gồm:
- `OrderStateTest`: chuyển trạng thái hợp lệ / không hợp lệ, giới hạn 3 lần giao.
- `ShippingFeeStrategyTest`: tính phí cùng vùng, khác vùng, vượt cân, hỏa tốc, ngoài phạm vi.
- `DeliveryFlowIntegrationTest`: toàn bộ quy trình tạo đơn → giao → nộp COD → đối soát trên H2.
- `SecurityAccessTest`: phân quyền URL theo vai trò, chặn POST thiếu CSRF token.

## Biểu đồ UML

Thư mục `docs/uml` chứa mã PlantUML: use case, lớp domain, State pattern, Strategy pattern, trạng thái đơn hàng,
tuần tự (tạo đơn, giao hàng, đối soát), hoạt động và ERD.
Xem trực tiếp bằng extension PlantUML trong VS Code / IntelliJ, hoặc dán nội dung vào https://www.plantuml.com/plantuml để xuất ảnh PNG đưa vào báo cáo.
