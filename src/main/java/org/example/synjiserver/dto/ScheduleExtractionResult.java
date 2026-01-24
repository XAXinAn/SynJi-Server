package org.example.synjiserver.dto;

import java.util.List;

public class ScheduleExtractionResult {
    private List<ScheduleExtractionData> schedules;

    public List<ScheduleExtractionData> getSchedules() {
        return schedules;
    }

    public void setSchedules(List<ScheduleExtractionData> schedules) {
        this.schedules = schedules;
    }
}
