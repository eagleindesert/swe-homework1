package com.example.demo.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import com.example.demo.dto.ScheduleRequestDto;
import com.example.demo.dto.ScheduleResponseDto;

public interface ScheduleService {

    /**
     * 일정 등록
     */
    ScheduleResponseDto createSchedule(ScheduleRequestDto requestDto);

    /**
     * 일정 ID로 조회
     */
    ScheduleResponseDto getScheduleById(Long id);

    /**
     * 모든 일정 조회
     */
    List<ScheduleResponseDto> getAllSchedules();

    /**
     * 월별 일정 조회
     */
    List<ScheduleResponseDto> getSchedulesByMonth(int year, int month);

    /**
     * 특정 날짜의 일정 조회
     */
    List<ScheduleResponseDto> getSchedulesByDate(LocalDate date);

    /**
     * 일정 수정
     */
    ScheduleResponseDto updateSchedule(Long id, ScheduleRequestDto requestDto);

    /**
     * 일정 삭제
     */
    void deleteSchedule(Long id);

    /**
     * 일정 충돌 확인
     */
    boolean hasOverlappingSchedule(LocalDate date, LocalTime startTime, LocalTime endTime);

    /**
     * 일별 상세 일정 조회 (시간대별)
     */
    List<ScheduleResponseDto> getDailyDetailSchedules(LocalDate date);
}
