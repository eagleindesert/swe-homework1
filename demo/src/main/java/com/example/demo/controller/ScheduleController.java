package com.example.demo.controller;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.dto.ScheduleRequestDto;
import com.example.demo.dto.ScheduleResponseDto;
import com.example.demo.service.ScheduleService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/schedules")
@RequiredArgsConstructor
@Slf4j
public class ScheduleController {

    private final ScheduleService scheduleService;

    /**
     * 메인 달력 페이지
     */
    @GetMapping
    public String showCalendar(
            @RequestParam(name = "year", required = false) Integer year,
            @RequestParam(name = "month", required = false) Integer month,
            Model model
    ) {
        // 현재 연월 설정
        YearMonth currentYearMonth = (year != null && month != null)
                ? YearMonth.of(year, month)
                : YearMonth.now();

        int currentYear = currentYearMonth.getYear();
        int currentMonth = currentYearMonth.getMonthValue();

        // 월별 일정 조회
        List<ScheduleResponseDto> schedules = scheduleService.getSchedulesByMonth(currentYear, currentMonth);

        // 달력 데이터 계산
        LocalDate firstDay = currentYearMonth.atDay(1);
        int daysInMonth = currentYearMonth.lengthOfMonth();
        int startDayOfWeek = firstDay.getDayOfWeek().getValue() % 7; // 일요일=0

        model.addAttribute("year", currentYear);
        model.addAttribute("month", currentMonth);
        model.addAttribute("schedules", schedules);
        model.addAttribute("firstDay", firstDay);
        model.addAttribute("daysInMonth", daysInMonth);
        model.addAttribute("startDayOfWeek", startDayOfWeek);

        return "schedules/calendar";
    }

    /**
     * 일정 등록 폼
     */
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("schedule", new ScheduleRequestDto());
        model.addAttribute("isEdit", false);
        return "schedules/form";
    }

    /**
     * 일정 등록 처리
     */
    @PostMapping
    public String createSchedule(
            @Valid @ModelAttribute("schedule") ScheduleRequestDto requestDto,
            BindingResult result,
            RedirectAttributes redirectAttributes
    ) {
        if (result.hasErrors()) {
            return "schedules/form";
        }

        try {
            scheduleService.createSchedule(requestDto);
            redirectAttributes.addFlashAttribute("success", "일정이 성공적으로 등록되었습니다.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/schedules";
    }

    /**
     * 일정 수정 폼
     */
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable("id") Long id, Model model) {
        ScheduleResponseDto schedule = scheduleService.getScheduleById(id);
        
        ScheduleRequestDto requestDto = ScheduleRequestDto.builder()
                .teamName(schedule.getTeamName())
                .scheduleDate(schedule.getScheduleDate())
                .startTime(schedule.getStartTime())
                .endTime(schedule.getEndTime())
                .build();
        
        model.addAttribute("schedule", requestDto);
        model.addAttribute("scheduleId", id);
        model.addAttribute("isEdit", true);
        return "schedules/form";
    }

    /**
     * 일정 수정 처리
     */
    @PostMapping("/{id}")
    public String updateSchedule(
            @PathVariable("id") Long id,
            @Valid @ModelAttribute("schedule") ScheduleRequestDto requestDto,
            BindingResult result,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        if (result.hasErrors()) {
            model.addAttribute("scheduleId", id);
            model.addAttribute("isEdit", true);
            return "schedules/form";
        }

        try {
            scheduleService.updateSchedule(id, requestDto);
            redirectAttributes.addFlashAttribute("success", "일정이 성공적으로 수정되었습니다.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/schedules";
    }

    /**
     * 일정 삭제
     */
    @PostMapping("/{id}/delete")
    public String deleteSchedule(
            @PathVariable("id") Long id,
            RedirectAttributes redirectAttributes
    ) {
        try {
            scheduleService.deleteSchedule(id);
            redirectAttributes.addFlashAttribute("success", "일정이 성공적으로 삭제되었습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "일정 삭제 중 오류가 발생했습니다.");
        }

        return "redirect:/schedules";
    }

    /**
     * 일별 상세 보기 페이지
     */
    @GetMapping("/daily/{date}")
    public String showDailyDetail(
            @PathVariable("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Model model
    ) {
        List<ScheduleResponseDto> schedules = scheduleService.getDailyDetailSchedules(date);
        model.addAttribute("selectedDate", date);
        model.addAttribute("schedules", schedules);
        return "schedules/daily";
    }

    /**
     * REST API - 월별 일정 조회 (AJAX)
     */
    @GetMapping("/api/monthly")
    @ResponseBody
    public List<ScheduleResponseDto> getMonthlySchedules(
            @RequestParam("year") int year,
            @RequestParam("month") int month
    ) {
        return scheduleService.getSchedulesByMonth(year, month);
    }

    /**
     * REST API - 일별 상세 일정 조회 (AJAX)
     */
    @GetMapping("/api/daily/{date}")
    @ResponseBody
    public List<ScheduleResponseDto> getDailySchedules(
            @PathVariable("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return scheduleService.getDailyDetailSchedules(date);
    }
}
