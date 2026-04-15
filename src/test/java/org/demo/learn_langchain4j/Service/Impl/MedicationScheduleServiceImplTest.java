package org.demo.learn_langchain4j.Service.Impl;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MedicationScheduleServiceImplTest {

    @Test
    void shouldAppendNoScheduleHintWhenMedicationTimesMissing() {
        Clock fixedClock = Clock.fixed(Instant.parse("2026-04-15T01:00:00Z"), ZoneId.of("Asia/Shanghai"));
        MedicationScheduleServiceImpl service = new MedicationScheduleServiceImpl(fixedClock);

        String prompt = service.buildMedicationAwareMessage("你好", "");

        assertTrue(prompt.contains("User medication times: not provided"));
        assertFalse(prompt.contains("proactive reminder"));
    }

    @Test
    void shouldAddProactiveReminderWhenWithinThirtyMinutes() {
        Clock fixedClock = Clock.fixed(Instant.parse("2026-04-15T00:10:00Z"), ZoneId.of("Asia/Shanghai"));
        MedicationScheduleServiceImpl service = new MedicationScheduleServiceImpl(fixedClock);

        String prompt = service.buildMedicationAwareMessage("今天状态怎么样？", "08:30,20:00");

        assertTrue(prompt.contains("proactive reminder"));
        assertTrue(prompt.contains("Nearest medication time is 08:30"));
    }

    @Test
    void shouldSkipReminderWhenOutsideThirtyMinutes() {
        Clock fixedClock = Clock.fixed(Instant.parse("2026-04-15T02:00:00Z"), ZoneId.of("Asia/Shanghai"));
        MedicationScheduleServiceImpl service = new MedicationScheduleServiceImpl(fixedClock);

        String prompt = service.buildMedicationAwareMessage("提醒我吧", "08:30");

        assertTrue(prompt.contains("No proactive reminder is required"));
    }

    @Test
    void shouldIgnoreInvalidTimeToken() {
        Clock fixedClock = Clock.fixed(Instant.parse("2026-04-15T02:00:00Z"), ZoneId.of("Asia/Shanghai"));
        MedicationScheduleServiceImpl service = new MedicationScheduleServiceImpl(fixedClock);

        String prompt = service.buildMedicationAwareMessage("测试", "08:00,abc,25:99");

        assertTrue(prompt.contains("Invalid medication time entries ignored: abc, 25:99"));
        assertTrue(prompt.contains("User medication times today: 08:00"));
    }
}

