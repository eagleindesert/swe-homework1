package com.example.demo.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import com.example.demo.entity.Schedule;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleResponseDto {

    private Long id;
    private String teamName;
    private String teamMembers;
    private LocalDate scheduleDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ScheduleResponseDto fromEntity(Schedule schedule) {
        return ScheduleResponseDto.builder()
                .id(schedule.getId())
                .teamName(schedule.getTeamName())
                .teamMembers(schedule.getTeamMembers())
                .scheduleDate(schedule.getScheduleDate())
                .startTime(schedule.getStartTime())
                .endTime(schedule.getEndTime())
                .createdAt(schedule.getCreatedAt())
                .updatedAt(schedule.getUpdatedAt())
                .build();
    }
}
