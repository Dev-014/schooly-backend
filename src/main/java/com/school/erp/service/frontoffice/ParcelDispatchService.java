package com.school.erp.service.frontoffice;

import com.school.erp.dto.frontoffice.ParcelDispatchRequest;
import com.school.erp.dto.frontoffice.ParcelDispatchResponse;
import com.school.erp.dto.frontoffice.ParcelDispatchStatsResponse;
import com.school.erp.entity.School;
import com.school.erp.entity.frontoffice.ParcelDispatch;
import com.school.erp.exception.ResourceNotFoundException;
import com.school.erp.repository.SchoolRepository;
import com.school.erp.repository.frontoffice.ParcelDispatchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class ParcelDispatchService {

    private final ParcelDispatchRepository parcelDispatchRepository;
    private final SchoolRepository schoolRepository;

    @Transactional(readOnly = true)
    public Page<ParcelDispatchResponse> filterParcelDispatches(
            Long schoolId,
            String search,
            LocalDate date,
            String status,
            Pageable pageable) {

        String cleanSearch = (search != null && !search.isBlank()) ? search.trim() : null;
        String cleanStatus = (status != null && !status.isBlank() && !status.equalsIgnoreCase("all")) ? status.trim() : null;

        Page<ParcelDispatch> page = parcelDispatchRepository.filterParcelDispatches(schoolId, cleanSearch, date, cleanStatus, pageable);
        return page.map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public ParcelDispatchResponse getParcelDispatchById(Long schoolId, Long id) {
        ParcelDispatch parcel = parcelDispatchRepository.findByIdAndSchoolId(id, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Parcel dispatch log not found with id: " + id));
        return mapToResponse(parcel);
    }

    @Transactional
    public ParcelDispatchResponse createParcelDispatch(Long schoolId, ParcelDispatchRequest request) {
        School school = schoolRepository.findById(schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("School not found with id: " + schoolId));

        ParcelDispatch parcel = new ParcelDispatch();
        parcel.setSchool(school);
        parcel.setReceiverName(request.getReceiverName());
        parcel.setReceiverInstitution(request.getReceiverInstitution());
        parcel.setDeliveryAddress(request.getDeliveryAddress());
        parcel.setPhoneNumber(request.getPhoneNumber());
        parcel.setDispatchDate(request.getDispatchDate() != null ? request.getDispatchDate() : LocalDate.now());
        parcel.setItemDetails(request.getItemDetails());
        parcel.setCourierName(request.getCourierName());

        if (request.getTrackingNumber() != null && !request.getTrackingNumber().isBlank()) {
            parcel.setTrackingNumber(request.getTrackingNumber().trim());
        } else {
            long count = parcelDispatchRepository.countDispatchedOnDate(schoolId, LocalDate.now()) + 1;
            parcel.setTrackingNumber(String.format("#TRK-%04d", 9000 + count));
        }

        parcel.setStatus("IN_TRANSIT");
        parcel.setNotes(request.getNotes());

        ParcelDispatch saved = parcelDispatchRepository.save(parcel);
        return mapToResponse(saved);
    }

    @Transactional
    public ParcelDispatchResponse updateStatus(Long schoolId, Long id, String status) {
        ParcelDispatch parcel = parcelDispatchRepository.findByIdAndSchoolId(id, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Parcel dispatch log not found with id: " + id));

        String normalizedStatus = status.toUpperCase();
        parcel.setStatus(normalizedStatus);
        if ("DELIVERED".equals(normalizedStatus) && parcel.getDeliveredDate() == null) {
            parcel.setDeliveredDate(LocalDate.now());
        }

        ParcelDispatch updated = parcelDispatchRepository.save(parcel);
        return mapToResponse(updated);
    }

    @Transactional(readOnly = true)
    public ParcelDispatchStatsResponse getParcelDispatchStats(Long schoolId) {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        long todayDispatches = parcelDispatchRepository.countDispatchedOnDate(schoolId, today);
        long yesterdayDispatches = parcelDispatchRepository.countDispatchedOnDate(schoolId, yesterday);

        double pctChange = 0.0;
        if (yesterdayDispatches > 0) {
            pctChange = ((double) (todayDispatches - yesterdayDispatches) / yesterdayDispatches) * 100.0;
        } else if (todayDispatches > 0) {
            pctChange = 100.0;
        }

        long pendingDelivery = parcelDispatchRepository.countPendingDelivery(schoolId);
        long couriersCount = parcelDispatchRepository.countCouriersWithPendingDeliveries(schoolId);

        LocalDate startOfMonth = today.withDayOfMonth(1);
        LocalDate endOfMonth = today.withDayOfMonth(today.lengthOfMonth());
        long successfulThisMonth = parcelDispatchRepository.countDeliveredInPeriod(schoolId, startOfMonth, endOfMonth);

        return ParcelDispatchStatsResponse.builder()
                .todayDispatches(todayDispatches)
                .percentageChangeFromYesterday(Math.round(pctChange * 10.0) / 10.0)
                .pendingDelivery(pendingDelivery)
                .pendingDeliveryCouriersCount(couriersCount)
                .successfulDeliveriesThisMonth(successfulThisMonth)
                .build();
    }

    @Transactional
    public void deleteParcelDispatch(Long schoolId, Long id) {
        ParcelDispatch parcel = parcelDispatchRepository.findByIdAndSchoolId(id, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Parcel dispatch log not found with id: " + id));
        parcelDispatchRepository.delete(parcel);
    }

    private ParcelDispatchResponse mapToResponse(ParcelDispatch p) {
        return ParcelDispatchResponse.builder()
                .id(p.getId())
                .schoolId(p.getSchool().getId())
                .receiverName(p.getReceiverName())
                .receiverInstitution(p.getReceiverInstitution())
                .deliveryAddress(p.getDeliveryAddress())
                .phoneNumber(p.getPhoneNumber())
                .dispatchDate(p.getDispatchDate())
                .itemDetails(p.getItemDetails())
                .courierName(p.getCourierName())
                .trackingNumber(p.getTrackingNumber())
                .status(p.getStatus())
                .deliveredDate(p.getDeliveredDate())
                .notes(p.getNotes())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }
}
