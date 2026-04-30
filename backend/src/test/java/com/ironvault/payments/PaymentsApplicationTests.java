package com.ironvault.payments;

import com.ironvault.payments.adapter.in.security.JwtTokenValidator;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class PaymentsApplicationTests {

    @MockBean
    private JwtTokenValidator jwtTokenValidator;

    @Test
    void contextLoads() {
    }
}
