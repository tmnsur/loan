package com.tanermansur.loan.service;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

@Service
public class TimeService {
    public Instant now() {
        return Instant.now();
    }

    public LocalDate localDateNow() {
        return now().atZone(ZoneId.of("UTC")).toLocalDate();
    }
}
