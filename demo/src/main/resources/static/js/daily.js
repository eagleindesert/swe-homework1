// 일별 상세 보기 JavaScript

document.addEventListener('DOMContentLoaded', function() {
    // 스케줄 블록 애니메이션
    const scheduleBlocks = document.querySelectorAll('.schedule-block');
    
    scheduleBlocks.forEach((block, index) => {
        block.style.opacity = '0';
        block.style.transform = 'translateX(-20px)';
        
        setTimeout(() => {
            block.style.transition = 'all 0.5s ease';
            block.style.opacity = '1';
            block.style.transform = 'translateX(0)';
        }, index * 100);
    });

    // 삭제 확인
    const deleteForms = document.querySelectorAll('form[action*="/delete"]');
    deleteForms.forEach(form => {
        form.addEventListener('submit', function(e) {
            if (!confirm('정말 이 일정을 삭제하시겠습니까?')) {
                e.preventDefault();
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

// 현재 시간으로 스크롤
function scrollToCurrentTime() {
    const now = new Date();
    const currentHour = now.getHours();
    const timelineRows = document.querySelectorAll('.timeline-row');
    
    if (timelineRows[currentHour]) {
        timelineRows[currentHour].scrollIntoView({ 
            behavior: 'smooth', 
            block: 'center' 
        });
    }
}

// 페이지 로드 시 현재 시간으로 스크롤
window.addEventListener('load', function() {
    setTimeout(scrollToCurrentTime, 500);
});
