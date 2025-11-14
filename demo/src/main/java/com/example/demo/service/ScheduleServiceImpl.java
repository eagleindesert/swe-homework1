package com.example.demo.service;

import com.example.demo.dto.ScheduleRequestDto;
import com.example.demo.dto.ScheduleResponseDto;
import com.example.demo.entity.Schedule;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ScheduleServiceImpl implements ScheduleService {

    private final ScheduleRepository scheduleRepository;

    @Override
    public ScheduleResponseDto createSchedule(ScheduleRequestDto requestDto) {
        log.info("Creating schedule for team: {}", requestDto.getTeamName());

        // 시간 검증
        if (!requestDto.getEndTime().isAfter(requestDto.getStartTime())) {
            throw new IllegalArgumentException("종료 시간은 시작 시간보다 늦어야 합니다.");
        }

        Schedule schedule = Schedule.builder()
                .teamName(requestDto.getTeamName())
                .scheduleDate(requestDto.getScheduleDate())
                .startTime(requestDto.getStartTime())
                .endTime(requestDto.getEndTime())
                .build();

        Schedule savedSchedule = scheduleRepository.save(schedule);
        log.info("Schedule created with ID: {}", savedSchedule.getId());

        return ScheduleResponseDto.fromEntity(savedSchedule);
    }

    @Override
    @Transactional(readOnly = true)
    public ScheduleResponseDto getScheduleById(Long id) {
        log.info("Fetching schedule with ID: {}", id);
        Schedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule not found with id: " + id));
        return ScheduleResponseDto.fromEntity(schedule);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ScheduleResponseDto> getAllSchedules() {
        log.info("Fetching all schedules");
        return scheduleRepository.findAll().stream()
                .map(ScheduleResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ScheduleResponseDto> getSchedulesByMonth(int year, int month) {
        log.info("Fetching schedules for {}-{}", year, month);
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        return scheduleRepository
                .findByScheduleDateBetweenOrderByScheduleDateAscStartTimeAsc(startDate, endDate)
                .stream()
                .map(ScheduleResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ScheduleResponseDto> getSchedulesByDate(LocalDate date) {
        log.info("Fetching schedules for date: {}", date);
        return scheduleRepository.findByScheduleDateOrderByStartTimeAsc(date)
                .stream()
                .map(ScheduleResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public ScheduleResponseDto updateSchedule(Long id, ScheduleRequestDto requestDto) {
        log.info("Updating schedule with ID: {}", id);

        Schedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule not found with id: " + id));

        // 시간 검증
        if (!requestDto.getEndTime().isAfter(requestDto.getStartTime())) {
            throw new IllegalArgumentException("종료 시간은 시작 시간보다 늦어야 합니다.");
        }

        schedule.setTeamName(requestDto.getTeamName());
        schedule.setScheduleDate(requestDto.getScheduleDate());
        schedule.setStartTime(requestDto.getStartTime());
        schedule.setEndTime(requestDto.getEndTime());

        Schedule updatedSchedule = scheduleRepository.save(schedule);
        log.info("Schedule updated with ID: {}", updatedSchedule.getId());

        return ScheduleResponseDto.fromEntity(updatedSchedule);
    }

    @Override
    public void deleteSchedule(Long id) {
        log.info("Deleting schedule with ID: {}", id);
        Schedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule not found with id: " + id));
        scheduleRepository.delete(schedule);
        log.info("Schedule deleted with ID: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasOverlappingSchedule(LocalDate date, LocalTime startTime, LocalTime endTime) {
        List<Schedule> overlapping = scheduleRepository.findOverlappingSchedules(date, startTime, endTime);
        return !overlapping.isEmpty();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ScheduleResponseDto> getDailyDetailSchedules(LocalDate date) {
        log.info("Fetching daily detail schedules for date: {}", date);
        return scheduleRepository.findByScheduleDateOrderByStartTimeAsc(date)
                .stream()
                .map(ScheduleResponseDto::fromEntity)
                .collect(Collectors.toList());
    }
}
