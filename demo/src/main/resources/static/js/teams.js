// teams.js
// 최근 추가한 팀 이름을 localStorage에 저장하고 블럭 형태로 표시

(function() {
    const STORAGE_KEY = 'recentTeams';
    const MAX_TEAMS = 8;

    // localStorage에서 최근 팀 목록 가져오기
    function getRecentTeams() {
        try {
            const raw = localStorage.getItem(STORAGE_KEY);
            if (!raw) return [];
            const arr = JSON.parse(raw);
            return Array.isArray(arr) ? arr : [];
        } catch (e) {
            console.error('Error reading recent teams from localStorage', e);
            return [];
        }
    }

    // localStorage에 최근 팀 목록 저장
    function saveRecentTeams(arr) {
        try {
            localStorage.setItem(STORAGE_KEY, JSON.stringify(arr));
        } catch (e) {
            console.error('Error saving recent teams to localStorage', e);
        }
    }

    // 새로운 팀 추가
    function addRecentTeam(teamInfo) {
        if (!teamInfo || !teamInfo.teamName) return;
        const name = teamInfo.teamName.trim();
        if (!name) return;

        let list = getRecentTeams();
        
        // 중복 제거 (같은 팀 이름이면 제거)
        list = list.filter(t => {
            if (typeof t === 'string') return t.toLowerCase() !== name.toLowerCase();
            return t.teamName.toLowerCase() !== name.toLowerCase();
        });
        
        // 맨 앞에 추가
        list.unshift({
            teamName: name,
            teamMembers: teamInfo.teamMembers ? teamInfo.teamMembers.trim() : ''
        });
        
        // 최대 개수 유지
        if (list.length > MAX_TEAMS) {
            list = list.slice(0, MAX_TEAMS);
        }
        
        saveRecentTeams(list);
    }

    // 최근 팀 블럭 렌더링
    function renderRecentTeams() {
        const container = document.getElementById('recentSchedules');
        if (!container) return;

        const teams = getRecentTeams();
        container.innerHTML = '';

        if (teams.length === 0) {
            container.innerHTML = '<div class="no-schedules-msg">최근 추가한 팀이 없습니다</div>';
            return;
        }

        teams.forEach(team => {
            // 하위 호환성: 문자열인 경우 객체로 변환
            const teamInfo = typeof team === 'string' ? { teamName: team, teamMembers: '' } : team;
            
            const block = document.createElement('div');
            block.className = 'schedule-block';
            block.innerHTML = `
                <div class="schedule-team">${teamInfo.teamName}</div>
            `;
            block.title = '클릭하여 팀 정보 입력';
            
            // 클릭 시 팀 이름과 팀원 구성 모두 입력
            block.addEventListener('click', function() {
                document.getElementById('teamName').value = teamInfo.teamName;
                const teamMembersInput = document.getElementById('teamMembers');
                if (teamMembersInput && teamInfo.teamMembers) {
                    teamMembersInput.value = teamInfo.teamMembers;
                }
                
                // 선택된 블럭 하이라이트
                document.querySelectorAll('.schedule-block').forEach(b => b.classList.remove('selected'));
                block.classList.add('selected');
            });

            container.appendChild(block);
        });
    }

    // 초기화
    document.addEventListener('DOMContentLoaded', function() {
        // 최근 팀 목록 렌더링
        renderRecentTeams();

        // 폼 제출 시 팀 정보 저장
        const form = document.querySelector('form');
        if (form) {
            form.addEventListener('submit', function() {
                const teamInfo = {
                    teamName: document.getElementById('teamName')?.value || '',
                    teamMembers: document.getElementById('teamMembers')?.value || ''
                };
                
                if (teamInfo.teamName) {
                    addRecentTeam(teamInfo);
                }
            });
        }
    });

    // 외부에서 접근 가능하도록 노출
    window.RecentTeams = {
        getRecentTeams,
        addRecentTeam,
        renderRecentTeams
    };
})();
