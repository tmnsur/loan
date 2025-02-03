package com.tanermansur.loan.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
class TimeServiceTest {
    @InjectMocks
    private TimeService timeService;

    @Test
    void now() {
        assertNotNull(timeService.now());
    }

    @Test
    void localDateNow() {
        assertNotNull(timeService.localDateNow());
    }
}