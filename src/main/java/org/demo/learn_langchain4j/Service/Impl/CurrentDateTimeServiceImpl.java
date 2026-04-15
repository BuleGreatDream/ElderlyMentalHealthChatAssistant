package org.demo.learn_langchain4j.Service.Impl;

import org.demo.learn_langchain4j.Service.CurrentDateTimeService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Service
public class CurrentDateTimeServiceImpl implements CurrentDateTimeService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public String getCurrentDateTime() {
        LocalDateTime now = LocalDateTime.now();
        ZoneId zoneId = ZoneId.systemDefault();
        return "Current local date and time: " + now.format(DATE_TIME_FORMATTER) + " (" + zoneId.getId() + ")";
    }
}

