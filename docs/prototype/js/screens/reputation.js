/**
 * ArenaFlow Screens: Reputation, Ratings, Reports & Disputes (REP-01..10)
 * Classic non-module script compatible with file:// protocol.
 */
(function (global) {
  'use strict';

  const UI = global.ArenaUI;
  const Store = global.ArenaStore;
  const Router = global.ArenaRouter;

  const REPORT_TYPES = [
    { value: 'NOSHOW', label: 'Bùng kèo (NO-SHOW)', penalty: 5 },
    { value: 'BAD_PLAY', label: 'Chơi xấu / Bạo lực', penalty: 3 },
    { value: 'BAD_COMMUNICATION', label: 'Giao tiếp tệ / Xúc phạm', penalty: 2 },
    { value: 'SKILL_MISMATCH', label: 'Không đúng trình độ', penalty: 1 },
    { value: 'OTHER', label: 'Lý do khác', penalty: 0 }
  ];

  function getReportLabel(type) {
    const found = REPORT_TYPES.find((t) => t.value === type);
    if (found) return found.label;
    if (type === 'FAKE_INFO') return 'Thông tin sai sự thật';
    return type || 'Khác';
  }

  function getPenaltyPoints(type) {
    const found = REPORT_TYPES.find((t) => t.value === type);
    return found ? found.penalty : 0;
  }

  function getScoreBadge(score) {
    if (score >= 95) return UI.badge(`Xuất sắc (${score})`, 'success');
    if (score >= 70) return UI.badge(`Tốt (${score})`, 'primary');
    if (score >= 50) return UI.badge(`Cảnh báo (${score})`, 'warning');
    return UI.badge(`Báo động (${score})`, 'error');
  }

  function formatDate(iso) {
    if (!iso) return '--';
    try {
      return new Date(iso).toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit', year: 'numeric' });
    } catch (e) {
      return iso;
    }
  }

  function formatDateTime(iso) {
    if (!iso) return '--';
    try {
      return new Date(iso).toLocaleString('vi-VN', {
        hour: '2-digit', minute: '2-digit', day: '2-digit', month: '2-digit', year: 'numeric'
      });
    } catch (e) {
      return iso;
    }
  }

  function renderStarPicker(selected, idPrefix) {
    let html = '<div class="flex items-center gap-1.5">';
    for (let i = 1; i <= 5; i++) {
      const active = i <= selected;
      html += `
        <button type="button" data-${idPrefix}-star="${i}"
                class="w-10 h-10 rounded-[8px] border ${active ? 'bg-[#FEF3C7] border-[#F59E0B] text-[#D97706]' : 'bg-white border-[#DDE5D9] text-[#9CA3AF]'} flex items-center justify-center transition-colors">
          <span class="material-symbols-outlined text-[22px]" style="font-variation-settings:'FILL' ${active ? 1 : 0};">star</span>
        </button>
      `;
    }
    html += '</div>';
    return html;
  }

  function renderStarsReadonly(count) {
    let html = '<span class="inline-flex items-center gap-0.5 text-[#F59E0B]">';
    for (let i = 1; i <= 5; i++) {
      html += `<span class="material-symbols-outlined text-[16px]" style="font-variation-settings:'FILL' ${i <= count ? 1 : 0};">star</span>`;
    }
    html += '</span>';
    return html;
  }

  /* =========================================================================
     REP-06: Open Violation Report Sheet
     ========================================================================= */
  function openReportSheet(opts) {
    const o = opts || {};
    const state = Store.getState();
    const uid = state.currentUserId;
    if (!uid) {
      UI.toast('Vui lòng đăng nhập để gửi báo cáo.', 'error');
      return;
    }

    const teams = state.teams || [];
    const defaultTargetTeam = o.reportedId || (teams[1] && teams[1].id) || 'team-beta';

    const bodyHtml = `
      <div class="space-y-3">
        <div>
          <label class="block text-xs font-semibold text-[#2D342C] mb-1">Đối tượng báo cáo</label>
          <select id="rep-sheet-target-type" class="w-full rounded-[8px] border border-[#DDE5D9] px-3 py-2 text-xs bg-white">
            <option value="TEAM" ${o.reporterType !== 'INDIVIDUAL' ? 'selected' : ''}>Đội bóng đối thủ</option>
            <option value="INDIVIDUAL" ${o.reporterType === 'INDIVIDUAL' ? 'selected' : ''}>Cá nhân đá lẻ</option>
          </select>
        </div>
        <div>
          <label class="block text-xs font-semibold text-[#2D342C] mb-1">Tên đội / người bị báo cáo</label>
          <input id="rep-sheet-target-id" type="text" value="${UI.esc(defaultTargetTeam)}"
                 class="w-full rounded-[8px] border border-[#DDE5D9] px-3 py-2 text-xs bg-white" />
        </div>
        <div>
          <label class="block text-xs font-semibold text-[#2D342C] mb-1">Loại vi phạm *</label>
          <select id="rep-sheet-type" class="w-full rounded-[8px] border border-[#DDE5D9] px-3 py-2 text-xs bg-white">
            ${REPORT_TYPES.map((t) => `<option value="${t.value}">${UI.esc(t.label)} (-${t.penalty} điểm)</option>`).join('')}
          </select>
        </div>
        <div>
          <label class="block text-xs font-semibold text-[#2D342C] mb-1">Mô tả sự việc *</label>
          <textarea id="rep-sheet-reason" rows="3" placeholder="Mô tả cụ thể sự việc (tối thiểu 10 ký tự)..."
                    class="w-full rounded-[8px] border border-[#DDE5D9] px-3 py-2 text-xs bg-white"></textarea>
        </div>
      </div>
    `;

    const footerHtml = `
      <div class="flex items-center gap-2">
        <button type="button" onclick="window.ArenaUI.closeSheet()"
                class="flex-1 py-2.5 rounded-[8px] border border-[#DDE5D9] bg-white text-xs font-semibold">Hủy</button>
        <button type="button" id="btn-rep-sheet-submit"
                class="flex-1 py-2.5 rounded-[8px] bg-[#DC2626] text-white text-xs font-semibold">Gửi báo cáo</button>
      </div>
    `;

    UI.openSheet({ title: 'Gửi báo cáo vi phạm (REP-06)', bodyHtml, footerHtml });

    document.getElementById('btn-rep-sheet-submit')?.addEventListener('click', () => {
      const reporterType = document.getElementById('rep-sheet-target-type')?.value || 'TEAM';
      const reportedId = (document.getElementById('rep-sheet-target-id')?.value || '').trim() || defaultTargetTeam;
      const type = document.getElementById('rep-sheet-type')?.value || 'OTHER';
      const reason = (document.getElementById('rep-sheet-reason')?.value || '').trim();
      if (reason.length < 10) {
        UI.toast('Vui lòng nhập mô tả tối thiểu 10 ký tự.', 'error');
        return;
      }

      const now = Store.getMockClock();
      const until = new Date(now.getTime() + 24 * 3600 * 1000).toISOString();
      const actingTeam = Store.getActingTeam();
      const newRep = {
        id: 'rep-' + Date.now(),
        reporterType,
        reporterId: reporterType === 'TEAM' ? (actingTeam ? actingTeam.id : uid) : uid,
        reportedId,
        matchId: o.matchId || 'match-completed-1',
        type,
        reason,
        status: type === 'NOSHOW' ? 'AUTO_ACCEPTED' : 'PENDING',
        createdAt: now.toISOString(),
        canDisputeUntil: type === 'NOSHOW' ? until : null,
        dispute: null
      };
      Store.setState({ reports: [newRep, ...(Store.getState().reports || [])] });
      UI.closeSheet();
      UI.toast('Đã gửi báo cáo vi phạm.', 'success');
      Router.navigate('REP-08', { reportId: newRep.id });
    });
  }

  /* =========================================================================
     REP-01: Đánh giá đối thủ sau trận (1-5 sao)
     ========================================================================= */
  Router.register('REP-01', (container, params) => {
    const state = Store.getState();
    const uid = state.currentUserId;
    const matchId = (params && params.matchId) || 'match-completed-1';
    const match = (state.matches || []).find((m) => m.id === matchId);

    if (!match) {
      container.innerHTML = `<div class="p-4">${UI.errorState({ title: 'Không tìm thấy trận đấu', code: '404' })}</div>`;
      return;
    }

    const hostTeam = (state.teams || []).find((t) => t.id === match.hostTeamId);
    const guestTeam = (state.teams || []).find((t) => t.id === (match.guestTeamId || match.confirmedGuestTeamId));
    let myTeam = null;
    let oppTeam = null;
    if (hostTeam && hostTeam.captainId === uid) {
      myTeam = hostTeam; oppTeam = guestTeam;
    } else if (guestTeam && guestTeam.captainId === uid) {
      myTeam = guestTeam; oppTeam = hostTeam;
    } else {
      myTeam = Store.getActingTeam() || hostTeam;
      oppTeam = myTeam && hostTeam && myTeam.id === hostTeam.id ? guestTeam : hostTeam;
    }

    if (hostTeam && guestTeam && hostTeam.captainId === uid && guestTeam.captainId === uid) {
      container.innerHTML = `<div class="p-4">${UI.errorState({
        title: 'Không thể tự đánh giá (RATING_003)',
        message: 'Bạn đang là đội trưởng của cả hai đội trong trận này.',
        code: 'RATING_003'
      })}</div>`;
      return;
    }

    if (match.status !== 'COMPLETED' && match.status !== 'ARCHIVED') {
      container.innerHTML = `<div class="p-4">${UI.emptyState({
        icon: 'schedule',
        title: 'Trận đấu chưa kết thúc',
        description: `Chỉ có thể đánh giá khi trận ở trạng thái COMPLETED / ARCHIVED (hiện tại: ${match.status}).`
      })}</div>`;
      return;
    }

    const existing = myTeam && (state.reviews || []).find(
      (r) => r.matchId === match.id && r.reviewerId === myTeam.id && r.reviewerType === 'TEAM'
    );

    if (existing) {
      container.innerHTML = `
        <div class="p-4 space-y-3">
          <div class="bg-white rounded-[8px] border border-[#DDE5D9] p-4 space-y-3">
            <div class="flex items-center gap-2 text-[#16A34A]">
              <span class="material-symbols-outlined text-[22px]">check_circle</span>
              <h3 class="text-sm font-bold">Đội bạn đã hoàn tất đánh giá trận này</h3>
            </div>
            <p class="text-xs text-[#596158]">Đánh giá sau trận chỉ được gửi 1 lần và không thể chỉnh sửa (RATING_002).</p>
            <button type="button" onclick="window.ArenaRouter.navigate('REP-02', { matchId: '${UI.esc(match.id)}' })"
                    class="w-full py-2.5 rounded-[8px] bg-[#3D6943] text-white text-xs font-semibold">
              Xem kết quả đánh giá (REP-02)
            </button>
          </div>
        </div>
      `;
      return;
    }

    let stars = 5;
    let isNoShow = false;

    function draw() {
      container.innerHTML = `
        <div class="p-4 space-y-4">
          <div class="bg-white rounded-[8px] border border-[#DDE5D9] p-4 flex items-center justify-between gap-3">
            <div class="min-w-0">
              ${UI.badge('Đã kết thúc', 'success')}
              <h2 class="text-base font-bold text-[#111827] mt-1 truncate">Đối thủ: ${UI.esc(oppTeam ? oppTeam.name : 'Đội khách')}</h2>
              <p class="text-xs text-[#596158]">Đội của bạn: <strong>${UI.esc(myTeam ? myTeam.name : 'Đội nhà')}</strong> • ${formatDate(match.matchDate)}</p>
            </div>
            ${UI.avatar(oppTeam || { shortName: 'FC' }, { size: 46 })}
          </div>

          <div class="bg-white rounded-[8px] border border-[#DDE5D9] p-4 space-y-4">
            <label class="flex items-center justify-between p-3 rounded-[8px] bg-[#F8FAF3] border border-[#DDE5D9] cursor-pointer">
              <div>
                <div class="text-xs font-bold text-[#111827]">Đối phương không đến sân (NO-SHOW)</div>
                <div class="text-[11px] text-[#596158]">Bỏ qua chấm sao nếu đối thủ bùng kèo</div>
              </div>
              <input id="rep01-noshow" type="checkbox" ${isNoShow ? 'checked' : ''} class="w-4 h-4 rounded text-[#3D6943]" />
            </label>

            ${!isNoShow ? `
              <div>
                <label class="block text-xs font-semibold text-[#2D342C] mb-2">Chấm điểm tinh thần & fair play (1–5 sao)</label>
                ${renderStarPicker(stars, 'rep01')}
              </div>
              <div>
                <label class="block text-xs font-semibold text-[#2D342C] mb-1">Tỉ số trận đấu (tùy chọn)</label>
                <input id="rep01-score" type="text" placeholder="Ví dụ: 3-2"
                       class="w-full rounded-[8px] border border-[#DDE5D9] px-3 py-2 text-xs bg-white" />
              </div>
            ` : `
              <div class="p-3 rounded-[8px] bg-[#FEF2F2] border border-[#FECACA] text-xs text-[#991B1B]">
                Hệ thống sẽ ghi nhận sự cố NO-SHOW và mở khiếu nại trong vòng 24 giờ cho đối phương.
              </div>
            `}

            <div>
              <label class="block text-xs font-semibold text-[#2D342C] mb-1">Nhận xét sau trận</label>
              <textarea id="rep01-comment" rows="3" placeholder="Chia sẻ trải nghiệm thi đấu cùng đối thủ..."
                        class="w-full rounded-[8px] border border-[#DDE5D9] px-3 py-2 text-xs bg-white"></textarea>
            </div>

            <button type="button" id="btn-rep01-submit"
                    class="w-full py-3 rounded-[8px] bg-[#3D6943] hover:bg-[#315C38] text-white text-sm font-semibold">
              Gửi đánh giá
            </button>
          </div>
        </div>
      `;

      container.querySelector('#rep01-noshow')?.addEventListener('change', (e) => {
        isNoShow = e.target.checked;
        draw();
      });
      container.querySelectorAll('[data-rep01-star]').forEach((btn) => {
        btn.addEventListener('click', () => {
          stars = Number(btn.getAttribute('data-rep01-star'));
          draw();
        });
      });
      container.querySelector('#btn-rep01-submit')?.addEventListener('click', () => {
        const comment = (container.querySelector('#rep01-comment')?.value || '').trim();
        const scoreGiven = (container.querySelector('#rep01-score')?.value || '').trim();
        const newRev = {
          id: 'rev-' + Date.now(),
          matchId: match.id,
          reviewerId: myTeam ? myTeam.id : uid,
          revieweeId: oppTeam ? oppTeam.id : 'team-gamma',
          reviewerType: 'TEAM',
          stars: isNoShow ? 1 : stars,
          score: isNoShow ? 0 : stars * 2,
          comment: comment || (isNoShow ? 'Đối thủ không đến sân.' : 'Trận đấu giao lưu tốt đẹp.'),
          noShow: isNoShow,
          scoreGiven,
          createdAt: Store.getMockClock().toISOString()
        };
        Store.setState({ reviews: [...(Store.getState().reviews || []), newRev] });
        UI.toast('Đã ghi nhận đánh giá sau trận!', 'success');
        Router.navigate('REP-02', { matchId: match.id });
      });
    }

    draw();
  }, {
    title: 'Đánh giá sau trận',
    showBack: true,
    group: 'Uy tín & Tranh chấp',
    aliases: ['rating-match']
  });

  /* =========================================================================
     REP-02: Kết quả đánh giá (Blind Rating)
     ========================================================================= */
  Router.register('REP-02', (container, params) => {
    const state = Store.getState();
    const matchId = (params && params.matchId) || 'match-completed-1';
    const match = (state.matches || []).find((m) => m.id === matchId);
    const actingTeam = Store.getActingTeam();
    const myTeamId = actingTeam ? actingTeam.id : (match ? match.hostTeamId : 'team-alpha');
    const reviews = (state.reviews || []).filter((r) => r.matchId === matchId && r.reviewerType === 'TEAM');
    const myRev = reviews.find((r) => r.reviewerId === myTeamId);
    const oppRev = reviews.find((r) => r.reviewerId !== myTeamId);
    const bothSubmitted = !!(myRev && oppRev);

    container.innerHTML = `
      <div class="p-4 space-y-3">
        <div class="p-3 rounded-[8px] bg-[#EAF0E6] border border-[#DDE5D9] text-xs text-[#2D342C] flex items-start gap-2">
          <span class="material-symbols-outlined text-[18px] text-[#3D6943] shrink-0">visibility_off</span>
          <div>
            <strong>Cơ chế Blind Rating (REP-02):</strong> Để đảm bảo khách quan, đánh giá của đối thủ chỉ được mở khóa khi cả hai đội đã gửi đánh giá (không tiết lộ đối phương đã chấm hay chưa trước khi mở khóa).
          </div>
        </div>

        <div class="bg-white rounded-[8px] border border-[#DDE5D9] p-4 space-y-2">
          <h3 class="text-sm font-bold text-[#111827]">Đánh giá của đội bạn</h3>
          ${myRev ? `
            <div class="flex items-center justify-between">
              ${renderStarsReadonly(myRev.stars)}
              <span class="text-[11px] text-[#596158]">${formatDateTime(myRev.createdAt)}</span>
            </div>
            <p class="text-xs text-[#2D342C] bg-[#F8FAF3] p-2.5 rounded-[8px]">${UI.esc(myRev.comment)}</p>
          ` : `
            <p class="text-xs text-[#596158]">Đội bạn chưa gửi đánh giá cho trận này.</p>
            <button type="button" onclick="window.ArenaRouter.navigate('REP-01', { matchId: '${UI.esc(matchId)}' })"
                    class="px-3 py-2 rounded-[8px] bg-[#3D6943] text-white text-xs font-semibold">
              Đánh giá ngay
            </button>
          `}
        </div>

        <div class="bg-white rounded-[8px] border border-[#DDE5D9] p-4 space-y-2">
          <h3 class="text-sm font-bold text-[#111827]">Đánh giá từ đối thủ</h3>
          ${bothSubmitted ? `
            <div class="flex items-center justify-between">
              ${renderStarsReadonly(oppRev.stars)}
              <span class="text-[11px] text-[#596158]">${formatDateTime(oppRev.createdAt)}</span>
            </div>
            <p class="text-xs text-[#2D342C] bg-[#F8FAF3] p-2.5 rounded-[8px]">${UI.esc(oppRev.comment)}</p>
          ` : `
            <div class="py-4 text-center space-y-1">
              <span class="material-symbols-outlined text-[28px] text-[#757D73]">lock_clock</span>
              <p class="text-xs font-semibold text-[#2D342C]">Đang ẩn theo quy tắc Blind Rating</p>
              <p class="text-[11px] text-[#596158]">Kết quả sẽ hiển thị sau khi cả hai bên hoàn tất đánh giá.</p>
            </div>
          `}
        </div>
      </div>
    `;
  }, {
    title: 'Kết quả đánh giá trận',
    showBack: true,
    group: 'Uy tín & Tranh chấp',
    aliases: ['match-reviews']
  });

  /* =========================================================================
     REP-03: Đánh giá đá lẻ 2 chiều
     ========================================================================= */
  Router.register('REP-03', (container) => {
    const state = Store.getState();
    const uid = state.currentUserId;
    const apps = state.applications || [];
    const approvedApp = apps.find((a) => a.status === 'APPROVED') || apps[0];
    const rec = approvedApp && (state.recruitments || []).find((r) => r.id === approvedApp.recruitmentId);
    const team = rec && (state.teams || []).find((t) => t.id === rec.teamId);
    const applicant = approvedApp && state.users ? state.users[approvedApp.applicantId] : null;

    let direction = (approvedApp && approvedApp.applicantId === uid) ? 'PLAYER_TO_TEAM' : 'TEAM_TO_PLAYER';
    let stars = 5;

    function draw() {
      const targetName = direction === 'TEAM_TO_PLAYER'
        ? (applicant ? applicant.displayName : 'Ứng viên đá lẻ')
        : (team ? team.name : 'Đội tuyển quân');

      container.innerHTML = `
        <div class="p-4 space-y-4">
          <div class="flex gap-1.5 bg-[#EAF0E6] p-1 rounded-[8px]">
            <button type="button" data-dir="TEAM_TO_PLAYER"
                    class="flex-1 py-2 rounded-[6px] text-xs font-semibold ${direction === 'TEAM_TO_PLAYER' ? 'bg-white text-[#14532D] shadow-sm' : 'text-[#596158]'}">
              Đội chấm cá nhân
            </button>
            <button type="button" data-dir="PLAYER_TO_TEAM"
                    class="flex-1 py-2 rounded-[6px] text-xs font-semibold ${direction === 'PLAYER_TO_TEAM' ? 'bg-white text-[#14532D] shadow-sm' : 'text-[#596158]'}">
              Cá nhân chấm đội
            </button>
          </div>

          <div class="bg-white rounded-[8px] border border-[#DDE5D9] p-4 space-y-4">
            <div class="flex items-center gap-3">
              ${UI.avatar({ displayName: targetName }, { size: 44 })}
              <div>
                <div class="text-[11px] text-[#596158]">${direction === 'TEAM_TO_PLAYER' ? 'Đánh giá người chơi đá lẻ' : 'Đánh giá đội bóng'}</div>
                <h3 class="text-sm font-bold text-[#111827]">${UI.esc(targetName)}</h3>
              </div>
            </div>

            <div>
              <label class="block text-xs font-semibold text-[#2D342C] mb-2">Mức độ hài lòng (1–5 sao)</label>
              ${renderStarPicker(stars, 'rep03')}
            </div>

            <div>
              <label class="block text-xs font-semibold text-[#2D342C] mb-1">Nhận xét</label>
              <textarea id="rep03-comment" rows="3" placeholder="Đúng giờ, thái độ hòa đồng, phối hợp tốt..."
                        class="w-full rounded-[8px] border border-[#DDE5D9] px-3 py-2 text-xs bg-white"></textarea>
            </div>

            <button type="button" id="btn-rep03-submit"
                    class="w-full py-2.5 rounded-[8px] bg-[#3D6943] text-white text-xs font-semibold">
              Gửi đánh giá đá lẻ
            </button>
          </div>
        </div>
      `;

      container.querySelectorAll('[data-dir]').forEach((btn) => {
        btn.addEventListener('click', () => {
          direction = btn.getAttribute('data-dir');
          draw();
        });
      });
      container.querySelectorAll('[data-rep03-star]').forEach((btn) => {
        btn.addEventListener('click', () => {
          stars = Number(btn.getAttribute('data-rep03-star'));
          draw();
        });
      });
      container.querySelector('#btn-rep03-submit')?.addEventListener('click', () => {
        const comment = (container.querySelector('#rep03-comment')?.value || '').trim() || 'Hợp tác tốt.';
        const newRev = {
          id: 'rev-merc-' + Date.now(),
          matchId: approvedApp ? approvedApp.id : 'merc-1',
          reviewerId: uid || 'user-cap-beta',
          revieweeId: direction === 'TEAM_TO_PLAYER' ? (applicant?.id || 'user-player') : (team?.id || 'team-beta'),
          reviewerType: direction,
          stars,
          comment,
          createdAt: Store.getMockClock().toISOString()
        };
        Store.setState({ reviews: [...(Store.getState().reviews || []), newRev] });
        UI.toast('Đã gửi đánh giá đá lẻ 2 chiều!', 'success');
      });
    }

    draw();
  }, {
    title: 'Đánh giá đá lẻ 2 chiều',
    showBack: true,
    group: 'Uy tín & Tranh chấp',
    aliases: ['rating-mercenary']
  });

  /* =========================================================================
     REP-04: Uy tín đội (completion rate only when >= 5 matches)
     ========================================================================= */
  Router.register('REP-04', (container, params) => {
    const state = Store.getState();
    const teams = state.teams || [];
    let selectedTeamId = (params && params.teamId) || state.actingTeamId || (teams[0] && teams[0].id);

    function render() {
      const team = teams.find((t) => t.id === selectedTeamId) || teams[0];
      if (!team) {
        container.innerHTML = `<div class="p-4">${UI.emptyState({ title: 'Chưa có đội bóng' })}</div>`;
        return;
      }
      const hasEnoughMatches = (team.matchCount || 0) >= 5;

      container.innerHTML = `
        <div class="p-4 space-y-4">
          <div class="bg-white rounded-[8px] border border-[#DDE5D9] p-3">
            <label class="block text-[11px] font-semibold text-[#596158] mb-1">Chọn đội để xem chỉ số uy tín</label>
            <select id="rep04-team-select" class="w-full rounded-[8px] border border-[#DDE5D9] px-3 py-2 text-xs bg-white font-semibold">
              ${teams.map((t) => `<option value="${t.id}" ${t.id === team.id ? 'selected' : ''}>${UI.esc(t.name)} (${t.matchCount || 0} trận)</option>`).join('')}
            </select>
          </div>

          <div class="bg-[#14532D] text-white rounded-[12px] p-4 space-y-3">
            <div class="flex items-center justify-between">
              <div class="flex items-center gap-3">
                ${UI.avatar(team, { size: 48 })}
                <div>
                  <h2 class="text-base font-bold">${UI.esc(team.name)}</h2>
                  <div class="mt-1">${getScoreBadge(team.reputationScore || 100)}</div>
                </div>
              </div>
              <div class="text-right">
                <div class="text-2xl font-bold">${team.reputationScore || 100}</div>
                <div class="text-[10px] text-white/75">Điểm uy tín</div>
              </div>
            </div>

            <div class="grid grid-cols-3 gap-2 pt-2 border-t border-white/15 text-center">
              <div>
                <div class="text-sm font-bold">${team.ratingAvg || '5.0'} ★</div>
                <div class="text-[10px] text-white/75">Đánh giá TB</div>
              </div>
              <div>
                <div class="text-sm font-bold">${team.matchCount || 0}</div>
                <div class="text-[10px] text-white/75">Số trận</div>
              </div>
              <div>
                <div class="text-sm font-bold">${hasEnoughMatches ? (team.completionRate + '%') : 'Chưa mở'}</div>
                <div class="text-[10px] text-white/75">Hoàn thành</div>
              </div>
            </div>
          </div>

          ${!hasEnoughMatches ? `
            <div class="p-3 rounded-[8px] bg-[#FEF3C7] border border-[#FDE68A] text-xs text-[#B45309]">
              <strong>Quy tắc 5 trận (REP-04):</strong> Tỉ lệ hoàn thành trận chỉ hiển thị khi đội có tối thiểu <strong>5 trận</strong> (hiện tại: ${team.matchCount || 0}/5 trận).
            </div>
          ` : ''}

          <div class="flex gap-2">
            <button type="button" onclick="window.ArenaRouter.navigate('REP-07')"
                    class="flex-1 py-2.5 rounded-[8px] bg-white border border-[#DDE5D9] text-xs font-semibold text-[#2D342C]">
              Báo cáo liên quan (REP-07)
            </button>
            <button type="button" id="btn-rep04-report"
                    class="flex-1 py-2.5 rounded-[8px] bg-[#FEF2F2] border border-[#FECACA] text-xs font-semibold text-[#A73B21]">
              Báo cáo vi phạm
            </button>
          </div>
        </div>
      `;

      container.querySelector('#rep04-team-select')?.addEventListener('change', (e) => {
        selectedTeamId = e.target.value;
        render();
      });
      container.querySelector('#btn-rep04-report')?.addEventListener('click', () => {
        openReportSheet({ reportedId: team.id, reporterType: 'TEAM' });
      });
    }

    render();
  }, {
    title: 'Uy tín đội bóng',
    showBack: true,
    group: 'Uy tín & Tranh chấp',
    aliases: ['team-reputation']
  });

  /* =========================================================================
     REP-05: Uy tín cá nhân (independent of team reputation)
     ========================================================================= */
  Router.register('REP-05', (container, params) => {
    const state = Store.getState();
    const targetId = (params && params.userId) || state.currentUserId || 'user-player';
    const user = (state.users && state.users[targetId]) || Store.getCurrentUser();

    if (!user) {
      container.innerHTML = `<div class="p-4">${UI.emptyState({ title: 'Chưa có thông tin người chơi' })}</div>`;
      return;
    }

    const score = user.reputationScore || 100;
    const hasEnough = (user.matchCount || 0) >= 5;

    container.innerHTML = `
      <div class="p-4 space-y-4">
        <div class="bg-white rounded-[12px] border border-[#DDE5D9] p-4 space-y-3">
          <div class="flex items-center justify-between">
            <div class="flex items-center gap-3">
              ${UI.avatar(user, { size: 48 })}
              <div>
                <h2 class="text-base font-bold text-[#111827]">${UI.esc(user.displayName || 'Người chơi')}</h2>
                <div class="mt-1">${getScoreBadge(score)}</div>
              </div>
            </div>
            <div class="text-right">
              <div class="text-2xl font-bold text-[#3D6943]">${score}</div>
              <div class="text-[10px] text-[#596158]">Điểm cá nhân</div>
            </div>
          </div>

          <div class="grid grid-cols-2 gap-2 pt-3 border-t border-[#EAF0E6] text-center">
            <div class="p-2.5 rounded-[8px] bg-[#F8FAF3]">
              <div class="text-base font-bold text-[#111827]">${user.matchCount || 0}</div>
              <div class="text-[11px] text-[#596158]">Trận đá lẻ</div>
            </div>
            <div class="p-2.5 rounded-[8px] bg-[#F8FAF3]">
              <div class="text-base font-bold text-[#111827]">${hasEnough ? (user.completionRate + '%') : 'Chưa đủ 5 trận'}</div>
              <div class="text-[11px] text-[#596158]">Hoàn thành</div>
            </div>
          </div>
        </div>

        <div class="p-3 rounded-[8px] bg-[#EAF0E6] border border-[#DDE5D9] text-xs text-[#2D342C]">
          <strong>Độc lập với điểm đội (REP-05):</strong> Điểm uy tín cá nhân phản ánh lịch sử đá lẻ & thái độ thi đấu cá nhân, tách biệt hoàn toàn với điểm của đội bóng.
        </div>

        <button type="button" onclick="window.ArenaRouter.navigate('REP-07')"
                class="w-full py-2.5 rounded-[8px] bg-[#3D6943] text-white text-xs font-semibold">
          Xem lịch sử báo cáo liên quan
        </button>
      </div>
    `;
  }, {
    title: 'Uy tín cá nhân',
    showBack: true,
    group: 'Uy tín & Tranh chấp',
    aliases: ['player-reputation']
  });

  /* =========================================================================
     REP-06: Route wrapper for Gửi báo cáo vi phạm
     ========================================================================= */
  Router.register('REP-06', (container) => {
    container.innerHTML = `
      <div class="p-4 space-y-3">
        <div class="bg-white rounded-[8px] border border-[#DDE5D9] p-4 space-y-3">
          <h2 class="text-base font-bold text-[#111827]">Gửi báo cáo vi phạm (REP-06)</h2>
          <p class="text-xs text-[#596158] leading-relaxed">
            Báo cáo đối thủ hoặc người chơi đá lẻ vi phạm quy định (NO-SHOW, chơi xấu, giao tiếp kém...).
          </p>
          <button type="button" id="btn-open-rep06"
                  class="w-full py-2.5 rounded-[8px] bg-[#DC2626] text-white text-xs font-semibold">
            Mở Sheet gửi báo cáo
          </button>
        </div>
      </div>
    `;
    container.querySelector('#btn-open-rep06')?.addEventListener('click', () => openReportSheet());
  }, {
    title: 'Gửi báo cáo vi phạm',
    showBack: true,
    group: 'Uy tín & Tranh chấp',
    aliases: ['create-report']
  });

  /* =========================================================================
     REP-07: Báo cáo liên quan
     ========================================================================= */
  Router.register('REP-07', (container) => {
    let tab = 'ALL'; // ALL | SENT | RECEIVED

    function render() {
      const state = Store.getState();
      const uid = state.currentUserId;
      const myTeamIds = Store.getMyTeams().map((t) => t.id);
      const reports = (state.reports || []).filter((r) => r.reporterType !== 'VENUE');

      const filtered = reports.filter((r) => {
        const isSender = r.reporterId === uid || myTeamIds.includes(r.reporterId);
        const isTarget = r.reportedId === uid || myTeamIds.includes(r.reportedId);
        if (tab === 'SENT') return isSender;
        if (tab === 'RECEIVED') return isTarget;
        return true;
      });

      container.innerHTML = `
        <div class="p-4 space-y-3">
          <div class="flex items-center justify-between">
            <h2 class="text-base font-bold text-[#111827]">Báo cáo & Tranh chấp</h2>
            <button type="button" id="btn-rep07-new"
                    class="px-2.5 py-1.5 rounded-[8px] bg-[#DC2626] text-white text-xs font-semibold">
              + Báo cáo mới
            </button>
          </div>

          <div class="flex gap-1.5">
            ${[
              { id: 'ALL', label: 'Tất cả' },
              { id: 'SENT', label: 'Đã gửi' },
              { id: 'RECEIVED', label: 'Bị báo cáo' }
            ].map((t) => `
              <button type="button" data-rep-tab="${t.id}"
                      class="px-3 py-1.5 rounded-full text-xs font-semibold border ${tab === t.id ? 'bg-[#3D6943] text-white border-[#3D6943]' : 'bg-white text-[#596158] border-[#DDE5D9]'}">
                ${t.label}
              </button>
            `).join('')}
          </div>

          ${filtered.length === 0 ? UI.emptyState({
            icon: 'verified_user',
            title: 'Không có báo cáo nào',
            description: 'Chưa có báo cáo vi phạm nào trong mục này.'
          }) : `
            <div class="space-y-2.5">
              ${filtered.map((r) => {
                const statusBadge = r.status === 'DISPUTED'
                  ? UI.badge('Đang khiếu nại', 'warning')
                  : (r.status === 'UPHELD' ? UI.badge('Khiếu nại thành công', 'success')
                  : (r.status === 'REJECTED' ? UI.badge('Bác khiếu nại', 'error')
                  : UI.badge(r.status, 'neutral')));
                return `
                  <article data-open-rep="${UI.esc(r.id)}"
                           class="bg-white rounded-[8px] border border-[#DDE5D9] p-3 space-y-1.5 cursor-pointer hover:border-[#3D6943]">
                    <div class="flex items-center justify-between gap-2">
                      <span class="text-xs font-bold text-[#111827]">${UI.esc(getReportLabel(r.type))}</span>
                      ${statusBadge}
                    </div>
                    <p class="text-xs text-[#596158] line-clamp-2">${UI.esc(r.reason)}</p>
                    <div class="flex items-center justify-between text-[10px] text-[#757D73]">
                      <span>${formatDate(r.createdAt)}</span>
                      <span class="font-mono">${UI.esc(r.id)}</span>
                    </div>
                  </article>
                `;
              }).join('')}
            </div>
          `}
        </div>
      `;

      container.querySelector('#btn-rep07-new')?.addEventListener('click', () => openReportSheet());
      container.querySelectorAll('[data-rep-tab]').forEach((btn) => {
        btn.addEventListener('click', () => {
          tab = btn.getAttribute('data-rep-tab');
          render();
        });
      });
      container.querySelectorAll('[data-open-rep]').forEach((el) => {
        el.addEventListener('click', () => {
          Router.navigate('REP-08', { reportId: el.getAttribute('data-open-rep') });
        });
      });
    }

    render();
  }, {
    title: 'Báo cáo liên quan',
    showBack: true,
    group: 'Uy tín & Tranh chấp',
    aliases: ['my-reports']
  });

  /* =========================================================================
     REP-08: Chi tiết báo cáo
     ========================================================================= */
  Router.register('REP-08', (container, params) => {
    const state = Store.getState();
    const repId = (params && params.reportId) || 'rep-noshow-active';
    const rep = (state.reports || []).find((r) => r.id === repId);

    if (!rep) {
      container.innerHTML = `<div class="p-4">${UI.errorState({ title: 'Không tìm thấy báo cáo', code: '404' })}</div>`;
      return;
    }

    const now = Store.getMockClock();
    const within24h = rep.canDisputeUntil ? (now <= new Date(rep.canDisputeUntil)) : false;
    const canAppeal = rep.type === 'NOSHOW' && within24h && !rep.dispute && rep.status !== 'RESOLVED';

    container.innerHTML = `
      <div class="p-4 space-y-4">
        <div class="bg-white rounded-[8px] border border-[#DDE5D9] p-4 space-y-3">
          <div class="flex items-center justify-between gap-2">
            <h2 class="text-base font-bold text-[#111827]">${UI.esc(getReportLabel(rep.type))}</h2>
            ${UI.badge(rep.status, rep.status === 'DISPUTED' ? 'warning' : 'neutral')}
          </div>

          <div class="text-xs text-[#596158] space-y-1">
            <div><strong>Mã báo cáo:</strong> <span class="font-mono">${UI.esc(rep.id)}</span></div>
            <div><strong>Loại đối tượng:</strong> ${UI.esc(rep.reporterType)}</div>
            <div><strong>Thời gian tạo:</strong> ${formatDateTime(rep.createdAt)}</div>
            <div><strong>Mức trừ uy tín:</strong> <span class="text-[#DC2626] font-bold">-${getPenaltyPoints(rep.type)} điểm</span></div>
            ${rep.canDisputeUntil ? `<div><strong>Hạn khiếu nại (24h):</strong> ${formatDateTime(rep.canDisputeUntil)} ${within24h ? '(Còn hạn)' : '(Đã hết hạn)'}</div>` : ''}
          </div>

          <div class="p-3 rounded-[8px] bg-[#F8FAF3] border border-[#DDE5D9] text-xs text-[#2D342C]">
            <strong>Nội dung phản ánh:</strong><br/>
            ${UI.esc(rep.reason)}
          </div>
        </div>

        ${canAppeal ? `
          <button type="button" onclick="window.ArenaRouter.navigate('REP-09', { reportId: '${UI.esc(rep.id)}' })"
                  class="w-full py-3 rounded-[8px] bg-[#3D6943] text-white text-xs font-semibold">
            Khiếu nại NO-SHOW trong 24h (REP-09)
          </button>
        ` : ''}

        ${(rep.dispute || rep.status === 'DISPUTED' || rep.status === 'UPHELD' || rep.status === 'REJECTED') ? `
          <button type="button" onclick="window.ArenaRouter.navigate('REP-10', { reportId: '${UI.esc(rep.id)}' })"
                  class="w-full py-2.5 rounded-[8px] bg-[#EAF0E6] text-[#3D6943] text-xs font-semibold">
            Xem kết quả tranh chấp (REP-10)
          </button>
        ` : ''}
      </div>
    `;
  }, {
    title: 'Chi tiết báo cáo',
    showBack: true,
    group: 'Uy tín & Tranh chấp',
    aliases: ['report-detail']
  });

  /* =========================================================================
     REP-09: Khiếu nại NO-SHOW (within 24h)
     ========================================================================= */
  Router.register('REP-09', (container, params) => {
    const state = Store.getState();
    const repId = (params && params.reportId) || 'rep-noshow-active';
    const rep = (state.reports || []).find((r) => r.id === repId);

    if (!rep) {
      container.innerHTML = `<div class="p-4">${UI.errorState({ title: 'Không tìm thấy báo cáo', code: '404' })}</div>`;
      return;
    }

    const now = Store.getMockClock();
    const within24h = rep.canDisputeUntil ? (now <= new Date(rep.canDisputeUntil)) : false;

    if (rep.dispute || rep.status === 'DISPUTED') {
      container.innerHTML = `<div class="p-4">${UI.errorState({
        title: 'Đã gửi khiếu nại trước đó (409)',
        message: 'Mỗi báo cáo NO-SHOW chỉ được gửi khiếu nại một lần.',
        code: '409'
      })}</div>`;
      return;
    }

    if (rep.type !== 'NOSHOW' || !within24h) {
      container.innerHTML = `<div class="p-4">${UI.errorState({
        title: 'Ngoài thời hạn khiếu nại 24h',
        message: 'Chỉ báo cáo NO-SHOW còn trong vòng 24 giờ kể từ lúc ghi nhận mới có thể khiếu nại.',
        code: 'DISPUTE_EXPIRED'
      })}</div>`;
      return;
    }

    container.innerHTML = `
      <div class="p-4 space-y-4">
        <div class="p-3 rounded-[8px] bg-[#FEF3C7] border border-[#FDE68A] text-xs text-[#B45309]">
          <strong>Thời hạn 24h (REP-09):</strong> Hạn chót gửi bằng chứng đến <strong>${formatDateTime(rep.canDisputeUntil)}</strong>.
        </div>

        <div class="bg-white rounded-[8px] border border-[#DDE5D9] p-4 space-y-3">
          <h2 class="text-sm font-bold text-[#111827]">Giải trình khiếu nại NO-SHOW</h2>
          <div>
            <label class="block text-xs font-semibold text-[#2D342C] mb-1">Nội dung giải trình *</label>
            <textarea id="rep09-note" rows="4" placeholder="Đội tôi đã có mặt tại sân lúc 18:55, có ảnh check-in và tin nhắn..."
                      class="w-full rounded-[8px] border border-[#DDE5D9] px-3 py-2 text-xs bg-white"></textarea>
          </div>
          <div>
            <label class="block text-xs font-semibold text-[#2D342C] mb-1">Bằng chứng đính kèm (mô phỏng)</label>
            <input id="rep09-evidence" type="text" value="checkin_san_an_phu_1855.jpg"
                   class="w-full rounded-[8px] border border-[#DDE5D9] px-3 py-2 text-xs bg-[#F8FAF3]" />
          </div>
          <button type="button" id="btn-rep09-submit"
                  class="w-full py-3 rounded-[8px] bg-[#3D6943] text-white text-xs font-semibold">
            Gửi khiếu nại tới Admin
          </button>
        </div>
      </div>
    `;

    container.querySelector('#btn-rep09-submit')?.addEventListener('click', () => {
      const note = (container.querySelector('#rep09-note')?.value || '').trim();
      const evidence = (container.querySelector('#rep09-evidence')?.value || '').trim();
      if (note.length < 10) {
        UI.toast('Vui lòng nhập giải trình tối thiểu 10 ký tự.', 'error');
        return;
      }
      const nextReports = (Store.getState().reports || []).map((r) => {
        if (r.id !== rep.id) return r;
        return {
          ...r,
          status: 'DISPUTED',
          dispute: {
            note,
            evidence,
            submittedAt: Store.getMockClock().toISOString(),
            submittedBy: Store.getState().currentUserId
          }
        };
      });
      Store.setState({ reports: nextReports });
      UI.toast('Đã gửi khiếu nại NO-SHOW thành công.', 'success');
      Router.navigate('REP-10', { reportId: rep.id });
    });
  }, {
    title: 'Khiếu nại NO-SHOW',
    showBack: true,
    group: 'Uy tín & Tranh chấp',
    aliases: ['submit-dispute']
  });

  /* =========================================================================
     REP-10: Kết quả tranh chấp
     ========================================================================= */
  Router.register('REP-10', (container, params) => {
    const state = Store.getState();
    const repId = (params && params.reportId) || 'rep-noshow-active';
    const rep = (state.reports || []).find((r) => r.id === repId);

    if (!rep) {
      container.innerHTML = `<div class="p-4">${UI.emptyState({ title: 'Không tìm thấy tranh chấp' })}</div>`;
      return;
    }

    const statusInfo = {
      DISPUTED: { title: 'Đang chờ Admin xem xét', badge: UI.badge('Đang xét duyệt', 'warning'), impact: 'Tạm giữ trạng thái trừ điểm cho đến khi có quyết định.' },
      UPHELD: { title: 'Chấp thuận khiếu nại', badge: UI.badge('Hoàn lại điểm', 'success'), impact: 'Server đã hoàn lại +5 điểm uy tín và hủy cờ NO-SHOW.' },
      REJECTED: { title: 'Bác bỏ khiếu nại', badge: UI.badge('Giữ nguyên xử phạt', 'error'), impact: 'Giữ nguyên mức trừ -5 điểm uy tín theo báo cáo ban đầu.' }
    }[rep.status] || { title: 'Trạng thái: ' + rep.status, badge: UI.badge(rep.status, 'neutral'), impact: 'Theo tính toán từ máy chủ.' };

    container.innerHTML = `
      <div class="p-4 space-y-4">
        <div class="bg-white rounded-[12px] border border-[#DDE5D9] p-4 space-y-3">
          <div class="flex items-center justify-between gap-2">
            <h2 class="text-base font-bold text-[#111827]">${UI.esc(statusInfo.title)}</h2>
            ${statusInfo.badge}
          </div>
          <p class="text-xs text-[#2D342C] leading-relaxed">${UI.esc(statusInfo.impact)}</p>
          ${rep.dispute ? `
            <div class="p-3 rounded-[8px] bg-[#F8FAF3] border border-[#DDE5D9] text-xs space-y-1">
              <div class="font-semibold text-[#111827]">Giải trình đã gửi:</div>
              <div class="text-[#596158]">${UI.esc(rep.dispute.note)}</div>
              ${rep.dispute.evidence ? `<div class="text-[11px] font-mono text-[#3D6943]">📎 ${UI.esc(rep.dispute.evidence)}</div>` : ''}
            </div>
          ` : ''}
          ${rep.adminDecisionNote ? `
            <div class="p-3 rounded-[8px] bg-[#EAF0E6] border border-[#DDE5D9] text-xs space-y-1">
              <div class="font-semibold text-[#14532D]">Ghi chú quyết định từ Admin:</div>
              <div class="text-[#2D342C]">${UI.esc(rep.adminDecisionNote)}</div>
            </div>
          ` : ''}
        </div>
      </div>
    `;
  }, {
    title: 'Kết quả tranh chấp',
    showBack: true,
    group: 'Uy tín & Tranh chấp',
    aliases: ['dispute-result']
  });

  global.ArenaReputation = { openReportSheet };
})(window);
