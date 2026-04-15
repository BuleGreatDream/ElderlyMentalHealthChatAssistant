package org.demo.learn_langchain4j.Service;

public interface MedicationScheduleService {

    String buildMedicationAwareMessage(String message, String medicationTimes);
}

