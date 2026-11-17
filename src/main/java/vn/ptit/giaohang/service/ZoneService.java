package vn.ptit.giaohang.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.ptit.giaohang.common.BusinessException;
import vn.ptit.giaohang.common.NotFoundException;
import vn.ptit.giaohang.domain.zone.DeliveryZone;
import vn.ptit.giaohang.domain.zone.Ward;
import vn.ptit.giaohang.dto.ZoneForm;
import vn.ptit.giaohang.repository.DeliveryZoneRepository;
import vn.ptit.giaohang.repository.WardRepository;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ZoneService {

    private final DeliveryZoneRepository zoneRepository;
    private final WardRepository wardRepository;

    public ZoneService(DeliveryZoneRepository zoneRepository, WardRepository wardRepository) {
        this.zoneRepository = zoneRepository;
        this.wardRepository = wardRepository;
    }

    public List<DeliveryZone> allZones() {
        return zoneRepository.findAllByOrderByCodeAsc();
    }

    public List<Ward> allWards() {
        return wardRepository.findAllByOrderByNameAsc();
    }

    /** Cac phuong dang trong pham vi phuc vu (da gan vung). */
    public List<Ward> servedWards() {
        return wardRepository.findByZoneIsNotNullOrderByNameAsc();
    }

    public Ward getWard(Long id) {
        return wardRepository.findById(id).orElseThrow(() -> new NotFoundException("Không tìm thấy phường"));
    }

    public DeliveryZone getZone(Long id) {
        return zoneRepository.findById(id).orElseThrow(() -> new NotFoundException("Không tìm thấy vùng giao hàng"));
    }

    @Transactional
    public DeliveryZone createZone(ZoneForm form) {
        if (zoneRepository.existsByCode(form.getCode())) {
            throw new BusinessException("Mã vùng " + form.getCode() + " đã tồn tại");
        }
        return zoneRepository.save(new DeliveryZone(form.getCode(), form.getName(), form.getDescription()));
    }

    /** Gan phuong vao vung; zoneId = null nghia la dua phuong ra ngoai pham vi phuc vu. */
    @Transactional
    public void assignWardToZone(Long wardId, Long zoneId) {
        Ward ward = getWard(wardId);
        if (zoneId == null) {
            ward.removeFromService();
        } else {
            ward.assignZone(getZone(zoneId));
        }
    }
}
