# 설계서

## 1. 시스템 아키텍처

### 1.1 전체 아키텍처
```
┌─────────────────────────────────────────────┐
│           Presentation Layer                │
│  (Thymeleaf Templates + HTML/CSS/JS)        │
└─────────────────┬───────────────────────────┘
                  │
┌─────────────────▼───────────────────────────┐
│           Controller Layer                  │
│        (Spring MVC Controllers)             │
└─────────────────┬───────────────────────────┘
                  │
┌─────────────────▼───────────────────────────┐
│           Service Layer                     │
│         (Business Logic)                    │
└─────────────────┬───────────────────────────┘
                  │
┌─────────────────▼───────────────────────────┐
│           Repository Layer                  │
│      (Spring Data JPA Repositories)         │
└─────────────────┬───────────────────────────┘
                  │
┌─────────────────▼───────────────────────────┐
│           Data Layer                        │
│          (H2/MySQL Database)                │
└─────────────────────────────────────────────┘
```

### 1.2 설계 패턴
- **MVC Pattern**: Model-View-Controller 패턴 적용
- **Layered Architecture**: 계층형 아키텍처
- **Repository Pattern**: 데이터 접근 추상화
- **DTO Pattern**: 데이터 전송 객체 사용

## 2. 데이터베이스 설계

### 2.1 ERD (Entity Relationship Diagram)
```
┌─────────────────────────────────┐
│         Schedule                │
├─────────────────────────────────┤
│ id (PK)          : BIGINT       │
│ team_name        : VARCHAR(50)  │
│ schedule_date    : DATE         │
│ start_time       : TIME         │
│ end_time         : TIME         │
│ created_at       : TIMESTAMP    │
│ updated_at       : TIMESTAMP    │
└─────────────────────────────────┘
```

### 2.2 테이블 상세 정의

#### Schedule 테이블
| 컬럼명 | 데이터 타입 | 제약조건 | 설명 |
|--------|-------------|----------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 일정 고유 ID |
| team_name | VARCHAR(50) | NOT NULL | 팀 이름 |
| schedule_date | DATE | NOT NULL | 일정 날짜 |
| start_time | TIME | NOT NULL | 시작 시간 |
| end_time | TIME | NOT NULL | 종료 시간 |
| created_at | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | 등록 일시 |
| updated_at | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE | 수정 일시 |

**인덱스**:
- `idx_schedule_date`: (schedule_date) - 날짜별 조회 성능 향상
- `idx_team_name`: (team_name) - 팀별 조회 성능 향상

**제약조건**:
- `chk_time`: end_time > start_time (종료 시간이 시작 시간보다 늦어야 함)

## 3. 클래스 설계

### 3.1 패키지 구조
```
com.example.demo
├── controller
│   └── ScheduleController.java
├── service
│   ├── ScheduleService.java
│   └── ScheduleServiceImpl.java
├── repository
│   └── ScheduleRepository.java
├── entity
│   └── Schedule.java
├── dto
│   ├── ScheduleRequestDto.java
│   └── ScheduleResponseDto.java
├── exception
│   ├── GlobalExceptionHandler.java
│   └── ResourceNotFoundException.java
└── DemoApplication.java
```

### 3.2 주요 클래스 설계

#### 3.2.1 Entity Layer

**Schedule.java**
```java
@Entity
@Table(name = "schedule")
public class Schedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 50)
    private String teamName;
    
    @Column(nullable = false)
    private LocalDate scheduleDate;
    
    @Column(nullable = false)
    private LocalTime startTime;
    
    @Column(nullable = false)
    private LocalTime endTime;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    // Getters, Setters, Constructors
}
```

#### 3.2.2 DTO Layer

**ScheduleRequestDto.java**
```java
public class ScheduleRequestDto {
    @NotBlank(message = "팀 이름은 필수입니다")
    @Size(max = 50, message = "팀 이름은 50자를 초과할 수 없습니다")
    private String teamName;
    
    @NotNull(message = "날짜는 필수입니다")
    @Future(message = "과거 날짜는 선택할 수 없습니다")
    private LocalDate scheduleDate;
    
    @NotNull(message = "시작 시간은 필수입니다")
    private LocalTime startTime;
    
    @NotNull(message = "종료 시간은 필수입니다")
    private LocalTime endTime;
    
    // Getters, Setters
}
```

**ScheduleResponseDto.java**
```java
public class ScheduleResponseDto {
    private Long id;
    private String teamName;
    private LocalDate scheduleDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Getters, Setters, Constructor from Entity
}
```

#### 3.2.3 Repository Layer

**ScheduleRepository.java**
```java
@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    // 특정 날짜의 모든 일정 조회
    List<Schedule> findByScheduleDate(LocalDate date);
    
    // 특정 기간의 일정 조회
    List<Schedule> findByScheduleDateBetween(LocalDate startDate, LocalDate endDate);
    
    // 특정 팀의 일정 조회
    List<Schedule> findByTeamName(String teamName);
    
    // 특정 날짜와 시간대에 겹치는 일정 조회
    @Query("SELECT s FROM Schedule s WHERE s.scheduleDate = :date " +
           "AND ((s.startTime < :endTime AND s.endTime > :startTime))")
    List<Schedule> findOverlappingSchedules(
        @Param("date") LocalDate date,
        @Param("startTime") LocalTime startTime,
        @Param("endTime") LocalTime endTime
    );
}
```

#### 3.2.4 Service Layer

**ScheduleService.java**
```java
public interface ScheduleService {
    ScheduleResponseDto createSchedule(ScheduleRequestDto requestDto);
    ScheduleResponseDto getScheduleById(Long id);
    List<ScheduleResponseDto> getAllSchedules();
    List<ScheduleResponseDto> getSchedulesByMonth(int year, int month);
    List<ScheduleResponseDto> getSchedulesByDate(LocalDate date);
    ScheduleResponseDto updateSchedule(Long id, ScheduleRequestDto requestDto);
    void deleteSchedule(Long id);
    boolean hasOverlappingSchedule(LocalDate date, LocalTime startTime, LocalTime endTime);
}
```

**ScheduleServiceImpl.java**
```java
@Service
@Transactional
public class ScheduleServiceImpl implements ScheduleService {
    private final ScheduleRepository scheduleRepository;
    
    // 생성자 주입
    public ScheduleServiceImpl(ScheduleRepository scheduleRepository) {
        this.scheduleRepository = scheduleRepository;
    }
    
    // 메서드 구현...
}
```

#### 3.2.5 Controller Layer

**ScheduleController.java**
```java
@Controller
@RequestMapping("/schedules")
public class ScheduleController {
    private final ScheduleService scheduleService;
    
    // 달력 페이지 표시
    @GetMapping
    public String showCalendar(
        @RequestParam(required = false) Integer year,
        @RequestParam(required = false) Integer month,
        Model model
    ) {
        // 현재 월 또는 지정된 월의 일정 조회
        // 모델에 데이터 추가
        return "calendar/index";
    }
    
    // 일정 등록 폼
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("schedule", new ScheduleRequestDto());
        return "schedule/form";
    }
    
    // 일정 등록 처리
    @PostMapping
    public String createSchedule(
        @Valid @ModelAttribute ScheduleRequestDto requestDto,
        BindingResult result
    ) {
        if (result.hasErrors()) {
            return "schedule/form";
        }
        scheduleService.createSchedule(requestDto);
        return "redirect:/schedules";
    }
    
    // 일정 수정 폼
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        ScheduleResponseDto schedule = scheduleService.getScheduleById(id);
        model.addAttribute("schedule", schedule);
        return "schedule/form";
    }
    
    // 일정 수정 처리
    @PostMapping("/{id}")
    public String updateSchedule(
        @PathVariable Long id,
        @Valid @ModelAttribute ScheduleRequestDto requestDto,
        BindingResult result
    ) {
        if (result.hasErrors()) {
            return "schedule/form";
        }
        scheduleService.updateSchedule(id, requestDto);
        return "redirect:/schedules";
    }
    
    // 일정 삭제
    @PostMapping("/{id}/delete")
    public String deleteSchedule(@PathVariable Long id) {
        scheduleService.deleteSchedule(id);
        return "redirect:/schedules";
    }
    
    // REST API - 월별 일정 조회
    @GetMapping("/api/monthly")
    @ResponseBody
    public List<ScheduleResponseDto> getMonthlySchedules(
        @RequestParam int year,
        @RequestParam int month
    ) {
        return scheduleService.getSchedulesByMonth(year, month);
    }
}
```

## 4. 화면 설계

### 4.1 화면 구성

#### 4.1.1 메인 화면 (달력 뷰)
```
┌─────────────────────────────────────────────────────────┐
│  합주 일정 캘린더                         [일정 추가]    │
├─────────────────────────────────────────────────────────┤
│  [◀] 2025년 11월 [▶]                                    │
├────┬────┬────┬────┬────┬────┬────────────────────────┤
│ 일 │ 월 │ 화 │ 수 │ 목 │ 금 │ 토                     │
├────┼────┼────┼────┼────┼────┼────────────────────────┤
│    │    │    │    │    │ 1  │ 2                      │
├────┼────┼────┼────┼────┼────┼────────────────────────┤
│ 3  │ 4  │ 5  │ 6  │ 7  │ 8  │ 9                      │
│    │    │████│    │    │    │                        │
│    │    │████│    │    │    │    ← 일정이 있는 날   │
├────┼────┼────┼────┼────┼────┼────────────────────────┤
│ ...                                                    │
└─────────────────────────────────────────────────────────┘

* 파란색 영역: 일정이 있는 시간대
* 마우스 오버 시: 팀 이름, 시작-종료 시간 표시
```

#### 4.1.2 일정 등록/수정 폼
```
┌─────────────────────────────────────────┐
│  일정 등록                              │
├─────────────────────────────────────────┤
│  팀 이름: [___________________]         │
│                                         │
│  날짜:    [2025-11-14 ▼]                │
│                                         │
│  시작 시간: [14:00 ▼]                   │
│                                         │
│  종료 시간: [16:00 ▼]                   │
│                                         │
│  [취소]              [저장]             │
└─────────────────────────────────────────┘
```

### 4.2 화면 플로우
```
메인 달력 화면
    │
    ├─→ [일정 추가] → 일정 등록 폼 → [저장] → 메인 달력 화면
    │
    ├─→ [일정 클릭] → 일정 상세/수정 폼 → [수정] → 메인 달력 화면
    │                                  └→ [삭제] → 메인 달력 화면
    │
    └─→ [이전/다음 월] → 다른 월 달력 표시
```

## 5. API 설계

### 5.1 REST API 엔드포인트

| Method | URI | 설명 | Request Body | Response |
|--------|-----|------|--------------|----------|
| GET | /schedules | 달력 페이지 표시 | - | HTML |
| GET | /schedules/new | 일정 등록 폼 | - | HTML |
| POST | /schedules | 일정 등록 | ScheduleRequestDto | Redirect |
| GET | /schedules/{id}/edit | 일정 수정 폼 | - | HTML |
| POST | /schedules/{id} | 일정 수정 | ScheduleRequestDto | Redirect |
| POST | /schedules/{id}/delete | 일정 삭제 | - | Redirect |
| GET | /schedules/api/monthly | 월별 일정 조회 (AJAX) | year, month | JSON |

### 5.2 API 상세

#### 일정 등록 API
```
POST /schedules
Content-Type: application/x-www-form-urlencoded

Request:
teamName=밴드1&scheduleDate=2025-11-15&startTime=14:00&endTime=16:00

Response:
302 Redirect to /schedules
```

#### 월별 일정 조회 API (AJAX)
```
GET /schedules/api/monthly?year=2025&month=11

Response:
[
  {
    "id": 1,
    "teamName": "밴드1",
    "scheduleDate": "2025-11-15",
    "startTime": "14:00:00",
    "endTime": "16:00:00",
    "createdAt": "2025-11-14T10:00:00",
    "updatedAt": "2025-11-14T10:00:00"
  },
  ...
]
```

## 6. 프론트엔드 설계

### 6.1 템플릿 구조
```
templates/
├── layout/
│   └── default.html          # 공통 레이아웃
├── calendar/
│   └── index.html            # 메인 달력 화면
├── schedule/
│   ├── form.html             # 일정 등록/수정 폼
│   └── detail.html           # 일정 상세 (툴팁용)
└── fragments/
    ├── header.html           # 헤더
    └── footer.html           # 푸터
```

### 6.2 JavaScript 컴포넌트
- **CalendarRenderer**: 달력 렌더링
- **ScheduleManager**: 일정 CRUD 처리
- **TooltipManager**: 일정 상세 정보 툴팁 표시
- **DateNavigator**: 월 이동 기능

### 6.3 CSS 설계
- **calendar.css**: 달력 스타일
- **schedule.css**: 일정 표시 스타일
- **form.css**: 폼 스타일
- **responsive.css**: 반응형 디자인

## 7. 보안 설계

### 7.1 입력 검증
- 서버 측 Bean Validation 적용
- XSS 방지: HTML 이스케이프 처리 (Thymeleaf 기본 제공)
- CSRF 보호: Spring Security CSRF 토큰 적용

### 7.2 데이터 검증
- 날짜/시간 형식 검증
- 시작 시간 < 종료 시간 검증
- 팀 이름 길이 제한

## 8. 성능 최적화

### 8.1 데이터베이스 최적화
- 적절한 인덱스 생성 (schedule_date, team_name)
- Connection Pool 설정 (HikariCP)
- 쿼리 최적화 (N+1 문제 방지)

### 8.2 프론트엔드 최적화
- AJAX를 통한 부분 갱신
- CSS/JS 파일 압축
- 이미지 최적화

### 8.3 캐싱 전략
- 정적 리소스 브라우저 캐싱
- 월별 일정 데이터 캐싱 고려

## 9. 테스트 설계

### 9.1 단위 테스트
- Service Layer 테스트
- Repository Layer 테스트
- Util/Helper 클래스 테스트

### 9.2 통합 테스트
- Controller 테스트 (MockMvc)
- End-to-End 시나리오 테스트

### 9.3 테스트 커버리지 목표
- 라인 커버리지: 80% 이상
- 브랜치 커버리지: 70% 이상

## 10. 배포 설계

### 10.1 빌드 프로세스
```bash
# 빌드
./gradlew clean build

# 실행
java -jar build/libs/demo-0.0.1-SNAPSHOT.jar
```

### 10.2 환경 설정
- **개발 환경**: H2 Database (인메모리)
- **운영 환경**: MySQL/PostgreSQL
- 환경별 application.properties 분리

### 10.3 배포 옵션
- 로컬 서버 배포
- Docker 컨테이너화
- 클라우드 배포 (AWS, Azure, GCP)

## 11. 개정 이력

| 버전 | 날짜 | 작성자 | 변경 내용 |
|------|------|--------|-----------|
| 1.0 | 2025-11-14 | - | 초안 작성 |
