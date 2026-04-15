package org.demo.learn_langchain4j.Service.Impl;

import org.demo.learn_langchain4j.Service.MedicationScheduleService;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class MedicationScheduleServiceImpl implements MedicationScheduleService {

    private static final int REMINDER_WINDOW_MINUTES = 30;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private final Clock clock;

    public MedicationScheduleServiceImpl() {
        this(Clock.systemDefaultZone());
    }

    MedicationScheduleServiceImpl(Clock clock) {
        this.clock = clock;
    }

    @Override
    public String buildMedicationAwareMessage(String message, String medicationTimes) {
        String cleanMessage = message == null ? "" : message.trim();
        ParseResult parseResult = parseTimes(medicationTimes);

        StringBuilder prompt = new StringBuilder(cleanMessage);
        prompt.append("\n\n[Medication Schedule]\n");

        if (parseResult.validTimes().isEmpty()) {
            prompt.append("- User medication times: not provided.\n");
            prompt.append("- If user asks health-related questions, ask for medication schedule in HH:mm format.\n");
            return prompt.toString();
        }

        String timesText = parseResult.validTimes().stream()
                .map(time -> time.format(TIME_FORMATTER))
                .collect(Collectors.joining(", "));

        prompt.append("- User medication times today: ").append(timesText).append(".\n");

        if (!parseResult.invalidTokens().isEmpty()) {
            prompt.append("- Invalid medication time entries ignored: ")
                    .append(String.join(", ", parseResult.invalidTokens()))
                    .append(".\n");
        }

        ReminderDecision reminderDecision = buildReminderDecision(parseResult.validTimes());
        if (reminderDecision.shouldRemind()) {
            prompt.append("- IMPORTANT: Start your reply with a short proactive reminder to take medicine now. ")
                    .append("Nearest medication time is ")
                    .append(reminderDecision.nearestTime())
                    .append(" and current time is within 30 minutes. ")
                    .append("After reminding, continue answering the user's request.\n");
        } else {
            prompt.append("- Current time is not within 30 minutes of any medication time. No proactive reminder is required.\n");
        }

        return prompt.toString();
    }

    private ParseResult parseTimes(String medicationTimes) {
        if (medicationTimes == null || medicationTimes.trim().isEmpty()) {
            return new ParseResult(List.of(), List.of());
        }

        String normalized = medicationTimes
                .replace('，', ',')
                .replace(';', ',')
                .replace('\n', ',')
                .replace('\r', ',');

        String[] tokens = normalized.split(",");
        Set<LocalTime> validTimes = new LinkedHashSet<>();
        List<String> invalidTokens = new ArrayList<>();

        for (String token : tokens) {
            String cleanToken = token.trim();
            if (cleanToken.isEmpty()) {
                continue;
            }
            try {
                validTimes.add(LocalTime.parse(cleanToken, TIME_FORMATTER));
            } catch (DateTimeParseException ex) {
                invalidTokens.add(cleanToken);
            }
        }

        List<LocalTime> sortedTimes = validTimes.stream()
                .sorted(Comparator.naturalOrder())
                .toList();

        return new ParseResult(sortedTimes, invalidTokens);
    }

    private ReminderDecision buildReminderDecision(List<LocalTime> times) {
        LocalTime now = LocalTime.now(clock);
        int nearestDiff = Integer.MAX_VALUE;
        LocalTime nearestTime = null;

        for (LocalTime time : times) {
            int diff = minuteDistance(now, time);
            if (diff < nearestDiff) {
                nearestDiff = diff;
                nearestTime = time;
            }
        }

        boolean shouldRemind = nearestDiff <= REMINDER_WINDOW_MINUTES;
        String nearestTimeText = nearestTime == null ? "N/A" : nearestTime.format(TIME_FORMATTER);
        return new ReminderDecision(shouldRemind, nearestTimeText);
    }

    private int minuteDistance(LocalTime a, LocalTime b) {
        int aMinutes = a.getHour() * 60 + a.getMinute();
        int bMinutes = b.getHour() * 60 + b.getMinute();
        int diff = Math.abs(aMinutes - bMinutes);
        return Math.min(diff, 1440 - diff);
    }

    private record ParseResult(List<LocalTime> validTimes, List<String> invalidTokens) {
    }

    private record ReminderDecision(boolean shouldRemind, String nearestTime) {
    }
}


