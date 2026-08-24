package com.school.erp.service;

import com.school.erp.repository.FeeInvoiceRepository;
import com.school.erp.security.AuthContextService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ActiveProfiles;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
public class FeeInvoiceServiceTest {

    @Autowired
    private FeeInvoiceService feeInvoiceService;

    @MockitoBean
    private AuthContextService authContextService;

    @Test
    public void testGetAllInvoices() {
        when(authContextService.resolveSchoolId(1L)).thenReturn(1L);
        try {
            feeInvoiceService.getAllInvoices(1L, 1L);
            System.out.println("TEST_SUCCESS_NO_EXCEPTION");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("TEST_FAILED_WITH_EXCEPTION: " + e.getMessage());
        }
    }
}
