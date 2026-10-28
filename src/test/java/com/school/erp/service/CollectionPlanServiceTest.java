package com.school.erp.service;

import com.school.erp.dto.finance.CollectionPlanItemRequest;
import com.school.erp.dto.finance.CollectionPlanRequest;
import com.school.erp.dto.finance.CollectionPlanResponse;
import com.school.erp.entity.finance.CollectionPlan;
import com.school.erp.entity.superadmin.School;
import com.school.erp.repository.finance.CollectionPlanItemRepository;
import com.school.erp.repository.finance.CollectionPlanRepository;
import com.school.erp.repository.superadmin.SchoolRepository;
import com.school.erp.service.finance.CollectionPlanService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CollectionPlanServiceTest {

    @Mock
    private CollectionPlanRepository collectionPlanRepository;

    @Mock
    private CollectionPlanItemRepository collectionPlanItemRepository;

    @Mock
    private SchoolRepository schoolRepository;

    @InjectMocks
    private CollectionPlanService collectionPlanService;

    private School testSchool;

    @BeforeEach
    void setUp() {
        testSchool = new School();
        testSchool.setId(1000L);
        testSchool.setName("Test School");
    }

    @Test
    void testCreatePlan_Success_WithPercentagesSummingTo100() {
        when(schoolRepository.findById(1000L)).thenReturn(Optional.of(testSchool));
        when(collectionPlanRepository.save(any(CollectionPlan.class))).thenAnswer(invocation -> {
            CollectionPlan p = invocation.getArgument(0);
            p.setId(99L);
            return p;
        });

        CollectionPlanRequest request = new CollectionPlanRequest();
        request.setName("3-Term Plan");
        request.setIsActive(true);

        List<CollectionPlanItemRequest> items = new ArrayList<>();
        CollectionPlanItemRequest item1 = new CollectionPlanItemRequest();
        item1.setLabel("Term 1");
        item1.setAmountType("PERCENTAGE");
        item1.setAmountValue(new BigDecimal("40.00"));
        item1.setSequenceOrder(1);
        item1.setDueDate(LocalDate.now().plusMonths(1));

        CollectionPlanItemRequest item2 = new CollectionPlanItemRequest();
        item2.setLabel("Term 2");
        item2.setAmountType("PERCENTAGE");
        item2.setAmountValue(new BigDecimal("30.00"));
        item2.setSequenceOrder(2);
        item2.setDueDate(LocalDate.now().plusMonths(4));

        CollectionPlanItemRequest item3 = new CollectionPlanItemRequest();
        item3.setLabel("Term 3");
        item3.setAmountType("PERCENTAGE");
        item3.setAmountValue(new BigDecimal("30.00"));
        item3.setSequenceOrder(3);
        item3.setDueDate(LocalDate.now().plusMonths(7));

        items.add(item1);
        items.add(item2);
        items.add(item3);
        request.setItems(items);

        CollectionPlanResponse response = collectionPlanService.createPlan(1000L, request);
        assertNotNull(response);
        assertEquals("3-Term Plan", response.getName());
        assertEquals(3, response.getItems().size());
    }

    @Test
    void testCreatePlan_Success_WithRemainder() {
        when(schoolRepository.findById(1000L)).thenReturn(Optional.of(testSchool));
        when(collectionPlanRepository.save(any(CollectionPlan.class))).thenAnswer(invocation -> {
            CollectionPlan p = invocation.getArgument(0);
            p.setId(101L);
            return p;
        });

        CollectionPlanRequest request = new CollectionPlanRequest();
        request.setName("2-Term with Remainder");
        request.setIsActive(true);

        List<CollectionPlanItemRequest> items = new ArrayList<>();
        CollectionPlanItemRequest item1 = new CollectionPlanItemRequest();
        item1.setLabel("Term 1");
        item1.setAmountType("PERCENTAGE");
        item1.setAmountValue(new BigDecimal("50.00"));
        item1.setSequenceOrder(1);
        item1.setDueDate(LocalDate.now().plusMonths(1));

        CollectionPlanItemRequest item2 = new CollectionPlanItemRequest();
        item2.setLabel("Final Term");
        item2.setAmountType("REMAINDER");
        item2.setSequenceOrder(2);
        item2.setDueDate(LocalDate.now().plusMonths(4));

        items.add(item1);
        items.add(item2);
        request.setItems(items);

        CollectionPlanResponse response = collectionPlanService.createPlan(1000L, request);
        assertNotNull(response);
        assertEquals("2-Term with Remainder", response.getName());
    }

    @Test
    void testCreatePlan_Fails_WhenFixedAmountIsProvided() {
        when(schoolRepository.findById(1000L)).thenReturn(Optional.of(testSchool));

        CollectionPlanRequest request = new CollectionPlanRequest();
        request.setName("Invalid Fixed Plan");
        request.setIsActive(true);

        List<CollectionPlanItemRequest> items = new ArrayList<>();
        CollectionPlanItemRequest item1 = new CollectionPlanItemRequest();
        item1.setLabel("Tuition Fee");
        item1.setAmountType("FIXED");
        item1.setAmountValue(new BigDecimal("10000.00"));
        item1.setSequenceOrder(1);
        items.add(item1);
        request.setItems(items);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            collectionPlanService.createPlan(1000L, request);
        });
        assertTrue(ex.getMessage().contains("Fixed amounts are not permitted in collection plans"));
    }

    @Test
    void testCreatePlan_Fails_WhenPercentagesDoNotSumTo100() {
        when(schoolRepository.findById(1000L)).thenReturn(Optional.of(testSchool));

        CollectionPlanRequest request = new CollectionPlanRequest();
        request.setName("Imbalanced Plan");
        request.setIsActive(true);

        List<CollectionPlanItemRequest> items = new ArrayList<>();
        CollectionPlanItemRequest item1 = new CollectionPlanItemRequest();
        item1.setLabel("Term 1");
        item1.setAmountType("PERCENTAGE");
        item1.setAmountValue(new BigDecimal("40.00"));
        item1.setSequenceOrder(1);

        CollectionPlanItemRequest item2 = new CollectionPlanItemRequest();
        item2.setLabel("Term 2");
        item2.setAmountType("PERCENTAGE");
        item2.setAmountValue(new BigDecimal("40.00")); // Sum = 80% != 100%
        item2.setSequenceOrder(2);

        items.add(item1);
        items.add(item2);
        request.setItems(items);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            collectionPlanService.createPlan(1000L, request);
        });
        assertTrue(ex.getMessage().contains("must sum exactly to 100%"));
    }
}
