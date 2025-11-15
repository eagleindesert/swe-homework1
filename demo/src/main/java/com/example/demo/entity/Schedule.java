package com.example.demo.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "schedule", indexes = {
        @Index(name = "idx_schedule_date", columnList = "schedule_date"),
        @Index(name = "idx_team_name", columnList = "team_name")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50, name = "team_name")
    private String teamName;

    @Column(length = 500, name = "team_members")
    private String teamMembers;

    @Column(nullable = false, name = "schedule_date")
    private LocalDate scheduleDate;

    @Column(nullable = false, name = "start_time")
    private LocalTime startTime;

    @Column(nullable = false, name = "end_time")
    private LocalTime endTime;

    @CreationTimestamp
    @Column(nullable = false, updatable = false, name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false, name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    private void validateTimeRange() {
        if (endTime != null && startTime != null && !endTime.isAfter(startTime)) {
            throw new IllegalArgumentException("종료 시간은 시작 시간보다 늦어야 합니다.");
        }
    }
}
