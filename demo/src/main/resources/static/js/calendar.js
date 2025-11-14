// 달력 관련 JavaScript

document.addEventListener('DOMContentLoaded', function() {
    // 일정 아이템 클릭 시 수정 페이지로 이동
    const scheduleItems = document.querySelectorAll('.schedule-item');
    
    scheduleItems.forEach(item => {
        item.addEventListener('click', function(e) {
            e.stopPropagation();
            const scheduleId = this.getAttribute('data-id');
            if (scheduleId) {
                window.location.href = `/schedules/${scheduleId}/edit`;
            }
        });
    });

    // 알림 메시지 자동 숨김
    const alerts = document.querySelectorAll('.alert');
    alerts.forEach(alert => {
        setTimeout(() => {
            alert.style.opacity = '0';
            alert.style.transition = 'opacity 0.5s ease';
            setTimeout(() => alert.remove(), 500);
        }, 3000);
    });
});

// 날짜 클릭 시 상세 보기
function viewDayDetail(date) {
    window.location.href = `/schedules/daily/${date}`;
}

// 일정 충돌 확인
function checkScheduleOverlap(date, startTime, endTime) {
    fetch(`/schedules/api/daily/${date}`)
        .then(response => response.json())
        .then(schedules => {
            const hasOverlap = schedules.some(schedule => {
                const scheduleStart = new Date(`${date}T${schedule.startTime}`);
                const scheduleEnd = new Date(`${date}T${schedule.endTime}`);
                const newStart = new Date(`${date}T${startTime}`);
                const newEnd = new Date(`${date}T${endTime}`);
                
                return (newStart < scheduleEnd && newEnd > scheduleStart);
            });
            
            if (hasOverlap) {
                alert('선택한 시간대에 이미 다른 일정이 있습니다.');
            }
        })
        .catch(error => {
            console.error('Error checking schedule overlap:', error);
        });
}
