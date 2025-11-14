package com.example.demo.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.Schedule;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    /**
     * 특정 날짜의 모든 일정 조회
     */
    List<Schedule> findByScheduleDateOrderByStartTimeAsc(LocalDate date);

    /**
     * 특정 기간의 일정 조회
     */
    List<Schedule> findByScheduleDateBetweenOrderByScheduleDateAscStartTimeAsc(LocalDate startDate, LocalDate endDate);

    /**
     * 특정 팀의 일정 조회
     */
    List<Schedule> findByTeamNameOrderByScheduleDateAscStartTimeAsc(String teamName);

    /**
     * 특정 날짜와 시간대에 겹치는 일정 조회
     */
    @Query("SELECT s FROM Schedule s WHERE s.scheduleDate = :date " +
           "AND ((s.startTime < :endTime AND s.endTime > :startTime))")
    List<Schedule> findOverlappingSchedules(
            @Param("date") LocalDate date,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime
    );

    /**
     * 월별 일정 개수 조회
     */
    @Query("SELECT COUNT(s) FROM Schedule s WHERE YEAR(s.scheduleDate) = :year AND MONTH(s.scheduleDate) = :month")
    long countByYearAndMonth(@Param("year") int year, @Param("month") int month);
}
