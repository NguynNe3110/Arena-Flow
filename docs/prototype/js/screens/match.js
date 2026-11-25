/**
 * ArenaFlow Match Discovery, Creation & Lifecycle Screens (MATCH-01..14)
 * Classic non-module script compatible with file:// protocol.
 * Depends on window.ArenaStore, window.ArenaUI, window.ArenaRouter.
 */
(function (global) {
  'use strict';

  const FORMAT_LABELS = {
    FIVE_A_SIDE: 'Sân 5',
    SEVEN_A_SIDE: 'Sân 7',
    ELEVEN_A_SIDE: 'Sân 11'
  };

  const SKILL_LABELS = {
    BEGINNER: 'Mới chơi / Vui vẻ',
    INTERMEDIATE: 'Trung bình / Văn phòng',
    ADVANCED: 'Khá / Bán chuyên'
  };

  const COST_SPLIT_LABELS = {
    SPLIT_50_50: 'Chia đôi 50/50',
    HOST_100: 'Chủ nhà bao sân',
    SEVENTY_THIRTY_HOME: '70/30 (Chủ nhà 70%)',
    SEVENTY_THIRTY_LOSER: '70/30 (Đội thua 70%)',
    LOSER_PAYS_DRINKS: 'Đội thua trả nước'
  };

  const MATCH_STATUS_META = {
    OPEN: { label: 'Đang mở kèo', variant: 'primary' },
    CONFIRMED: { label: 'Đã chốt kèo', variant: 'success' },
    IN_PROGRESS: { label: 'Đang diễn ra', variant: 'info' },
    COMPLETED: { label: 'Đã kết thúc', variant: 'neutral' },
    CANCELLED: { label: 'Đã hủy', variant: 'error' },
    EXPIRED: { label: 'Hết hạn', variant: 'warning' }
  };

  const REQ_STATUS_META = {
    PENDING: { label: 'Chờ duyệt', variant: 'warning' },
    ACCEPTED: { label: 'Đã chấp nhận', variant: 'success' },
    REJECTED: { label: 'Bị từ chối', variant: 'error' },
    WITHDRAWN: { label: 'Đã rút', variant: 'neutral' },
    SUPERSEDED: { label: 'Đã thay thế', variant: 'neutral' }
  };

  // Persistent filter & draft state across navigation
  const _filters = {
    date: 'ALL',
    kind: 'ALL',
    format: 'ALL',
    skill: 'ALL',
    maxDistance: 'ALL',
    wardId: 'ALL'
  };

  function getDraft() {
    if (!global._matchCreateDraft) {
      const clock = global.ArenaStore.getMockClock();
      const defaultDate = new Date(clock.getTime() + 3 * 24 * 3600 * 1000).toISOString().slice(0, 10);
      global._matchCreateDraft = {
        kind: 'A',
        format: 'SEVEN_A_SIDE',
        skillLevel: 'INTERMEDIATE',
        matchDate: defaultDate,
        venueId: 'ven-anphu',
        startTime: '19:00',
        endTime: '20:30',
        proposedVenues: ['ven-anphu', 'ven-binhtrung'],
        proposedTimeSlots: ['18:00 - 19:30', '19:30 - 21:00'],
        totalCost: 600000,
        costSharing: 'SPLIT_50_50',
        depositAmount: 200000,
        depositHolder: 'HOST',
        expiresHoursBefore: 2,
        notes: ''
      };
    }
    return global._matchCreateDraft;
  }

  function resetDraft() {
    global._matchCreateDraft = null;
  }

  function formatMoney(n) {
    return Number(n || 0).toLocaleString('vi-VN') + ' đ';
  }

  function formatDateVi(dateStr) {
    if (!dateStr) return '—';
    const parts = dateStr.split('-');
    if (parts.length !== 3) return dateStr;
    return `${parts[2]}/${parts[1]}/${parts[0]}`;
  }

  function computeShares(totalCost, costSharing) {
    const total = Math.max(0, Number(totalCost) || 0);
    if (costSharing === 'HOST_100') return { host: total, guest: 0 };
    if (costSharing === 'SEVENTY_THIRTY_HOME') return { host: Math.round(total * 0.7), guest: Math.round(total * 0.3) };
    return { host: Math.round(total / 2), guest: Math.round(total / 2) };
  }

  function getEffectiveStatus(match) {
    if (!match) return 'OPEN';
    if (match.status === 'CANCELLED' || match.status === 'COMPLETED' || match.status === 'EXPIRED') {
      return match.status;
    }
    const now = global.ArenaStore.getMockClock().getTime();
    if (match.status === 'OPEN' && match.expiresAt) {
      if (now > new Date(match.expiresAt).getTime()) return 'EXPIRED';
    }
    if (match.status === 'CONFIRMED' && match.matchDate) {
      const start = new Date(`${match.matchDate}T${match.startTime || '19:00'}:00Z`).getTime();
      const end = new Date(`${match.matchDate}T${match.endTime || '20:30'}:00Z`).getTime();
      if (now > end) return 'COMPLETED';
      if (now >= start && now <= end) return 'IN_PROGRESS';
    }
    return match.status;
  }

  function calculateCancelPenalty(match) {
    if (!match || !match.matchDate) return { penalty: 0, blocked: false, text: 'Không trừ điểm uy tín' };
    const startMs = new Date(`${match.matchDate}T${match.startTime || '19:00'}:00Z`).getTime();
    const nowMs = global.ArenaStore.getMockClock().getTime();
    const hours = (startMs - nowMs) / (1000 * 3600);
    if (hours <= 0) {
      return { penalty: 0, blocked: true, text: 'Trận đấu đã bắt đầu hoặc kết thúc — không thể hủy/rút.' };
    }
    if (hours >= 24) return { penalty: -2, blocked: false, text: '-2 điểm uy tín (hủy trước >= 24 giờ)' };
    if (hours >= 4) return { penalty: -5, blocked: false, text: '-5 điểm uy tín (hủy trước 4–24 giờ)' };
    return { penalty: -10, blocked: false, text: '-10 điểm uy tín (hủy sát giờ < 4 giờ)' };
  }

  function getTeam(id) {
    return global.ArenaStore.findById('teams', id);
  }

  function getVenue(id) {
    return global.ArenaStore.findById('venues', id);
  }

  function getMatch(id) {
    return global.ArenaStore.findById('matches', id);
  }

  function openActingTeamSelector(onSelected) {
    const store = global.ArenaStore;
    const myCapTeams = store.getMyCaptainedTeams();
    if (myCapTeams.length === 0) {
      global.ArenaUI.toast('Bạn chưa làm đội trưởng đội nào đang hoạt động.', 'warning');
      return;
    }
    if (global.ArenaRouter.resolveId('TEAM-14') === 'TEAM-14' && global.ArenaRouter.routes.has('TEAM-14') && !onSelected) {
      global.ArenaRouter.navigate('TEAM-14');
      return;
    }
    const acting = store.getActingTeam();
    const bodyHtml = `
      <p class="text-xs text-[#596158] mb-3">Chọn đội bóng bạn muốn đại diện thực hiện thao tác:</p>
      <div class="space-y-2">
        ${myCapTeams.map((t) => {
          const isSel = acting && acting.id === t.id;
          return `
            <button type="button" data-pick-team="${global.ArenaUI.esc(t.id)}"
                    class="w-full p-3 rounded-[8px] border ${isSel ? 'border-[#3D6943] bg-[#EAF0E6]' : 'border-[#DDE5D9] bg-white'} flex items-center justify-between gap-2 text-left">
              <div class="flex items-center gap-2.5 min-w-0">
                ${global.ArenaUI.avatar(t, { size: 36 })}
                <div class="min-w-0">
                  <div class="font-bold text-sm text-[#111827] truncate">${global.ArenaUI.esc(t.name)}</div>
                  <div class="text-[11px] text-[#596158]">Uy tín ${t.reputationScore} • ${t.matchCount} trận</div>
                </div>
              </div>
              ${isSel ? '<span class="material-symbols-outlined text-[#3D6943]">check_circle</span>' : ''}
            </button>
          `;
        }).join('')}
      </div>
    `;
    global.ArenaUI.openSheet({ title: 'Chọn đội đang thao tác (TEAM-14)', bodyHtml });
    setTimeout(() => {
      document.querySelectorAll('[data-pick-team]').forEach((btn) => {
        btn.onclick = () => {
          store.setActingTeam(btn.getAttribute('data-pick-team'));
          global.ArenaUI.closeSheet();
          global.ArenaUI.toast('Đã chuyển đội thao tác.', 'success');
          if (typeof onSelected === 'function') onSelected(btn.getAttribute('data-pick-team'));
          else global.ArenaRouter.refresh();
        };
      });
    }, 0);
  }

  function actingTeamBarHtml() {
    const store = global.ArenaStore;
    const user = store.getCurrentUser();
    if (!user) {
      return `
        <div class="bg-[#FEF3C7] border border-[#FDE68A] rounded-[8px] px-3 py-2 flex items-center justify-between gap-2">
          <span class="text-xs text-[#92400E] font-medium">Bạn đang xem với tư cách Khách.</span>
          <button type="button" onclick="window.ArenaRouter.navigate('S1')"
                  class="px-2.5 py-1 rounded-[6px] bg-[#3D6943] text-white text-[11px] font-semibold shrink-0">Đăng nhập</button>
        </div>
      `;
    }
    const capTeams = store.getMyCaptainedTeams();
    const acting = store.getActingTeam();
    if (capTeams.length === 0) {
      return `
        <div class="bg-white border border-[#DDE5D9] rounded-[8px] px-3 py-2 flex items-center justify-between gap-2">
          <div class="flex items-center gap-2 text-xs text-[#596158]">
            <span class="material-symbols-outlined text-[18px] text-[#3D6943]">info</span>
            <span>Chỉ Đội trưởng mới có thể đăng hoặc bắt kèo.</span>
          </div>
          <button type="button" onclick="window.ArenaRouter.navigate('TEAM-03')"
                  class="text-xs font-semibold text-[#3D6943] shrink-0">Tạo đội</button>
        </div>
      `;
    }
    return `
      <div class="bg-white border border-[#DDE5D9] rounded-[8px] px-3 py-2 flex items-center justify-between gap-2">
        <div class="flex items-center gap-2 min-w-0">
          ${global.ArenaUI.avatar(acting || capTeams[0], { size: 28 })}
          <div class="min-w-0">
            <div class="text-[10px] text-[#596158] uppercase tracking-wider font-semibold">Đội thao tác</div>
            <div class="text-xs font-bold text-[#111827] truncate">${global.ArenaUI.esc((acting || capTeams[0]).name)}</div>
          </div>
        </div>
        ${capTeams.length > 1 ? `
          <button type="button" id="btn-switch-acting-team"
                  class="px-2.5 py-1 rounded-[6px] bg-[#EAF0E6] text-[#14532D] text-[11px] font-semibold flex items-center gap-1 shrink-0">
            <span class="material-symbols-outlined text-[14px]">swap_horiz</span>Đổi đội
          </button>
        ` : global.ArenaUI.badge('Đội trưởng', 'primary')}
      </div>
    `;
  }

  function renderMatchCard(match) {
    const ui = global.ArenaUI;
    const host = getTeam(match.hostTeamId);
    const effStatus = getEffectiveStatus(match);
    const statusMeta = MATCH_STATUS_META[effStatus] || MATCH_STATUS_META.OPEN;
    const isKindB = match.kind === 'B';

    let locationLine = '';
    let timeLine = '';
    if (isKindB) {
      const vNames = (match.proposedVenues || []).map((vid) => {
        const v = getVenue(vid);
        return v ? v.name : vid;
      });
      locationLine = vNames.length > 0 ? `${vNames.length} sân đề xuất: ${vNames[0]}${vNames.length > 1 ? ` +${vNames.length - 1}` : ''}` : 'Chưa chọn sân';
      const slots = match.proposedTimeSlots || [];
      timeLine = slots.length > 0 ? `${slots.length} khung giờ (${slots[0]}${slots.length > 1 ? '...' : ''})` : 'Thương lượng giờ';
    } else {
      const venue = getVenue(match.venueId);
      locationLine = venue ? `${venue.name} • ${venue.distanceKm || '2.0'} km` : 'Chưa rõ sân';
      timeLine = `${match.startTime || '19:00'} - ${match.endTime || '20:30'}`;
    }

    return `
      <article data-open-match="${ui.esc(match.id)}" data-kind="${ui.esc(match.kind)}"
               class="bg-white rounded-[8px] border border-[#DDE5D9] p-3.5 shadow-sm hover:border-[#3D6943] transition-colors cursor-pointer space-y-2.5">
        <div class="flex items-center justify-between gap-2">
          <div class="flex items-center gap-2.5 min-w-0">
            ${ui.avatar(host || { name: 'Đội bóng' }, { size: 36 })}
            <div class="font-bold text-sm text-[#111827] truncate">${ui.esc(host ? host.name : 'Đội chủ nhà')}</div>
          </div>
          <div class="flex items-center gap-1 shrink-0">
            ${ui.badge(isKindB ? 'Kèo B' : 'Kèo A', isKindB ? 'info' : 'dark')}
            ${ui.badge(statusMeta.label, statusMeta.variant)}
          </div>
        </div>

        <div class="space-y-1 text-xs text-[#2D342C]">
          <div class="flex items-center gap-2">
            <span class="material-symbols-outlined text-[15px] text-[#3D6943]">calendar_today</span>
            <span class="font-semibold">${formatDateVi(match.matchDate)}</span>
            <span class="text-[#596158]">•</span>
            <span>${ui.esc(timeLine)}</span>
          </div>
          <div class="flex items-center gap-2">
            <span class="material-symbols-outlined text-[15px] text-[#3D6943]">location_on</span>
            <span class="truncate">${ui.esc(locationLine)}</span>
          </div>
          <div class="flex items-center gap-2 text-[11px] text-[#596158]">
            <span class="material-symbols-outlined text-[15px] text-[#3D6943]">sports_soccer</span>
            <span>${ui.esc(FORMAT_LABELS[match.format] || 'Sân 7')} • ${ui.esc(SKILL_LABELS[match.skillLevel] || 'Trung bình')}</span>
          </div>
        </div>
      </article>
    `;
  }

  /* =========================================================================
     MATCH-02: Filter Sheet (standalone & modal)
     ========================================================================= */
  function getFilterFormHtml() {
    const wards = global.ArenaStore.getState().wards || [];
    const clock = global.ArenaStore.getMockClock();
    const dates = [];
    for (let i = 0; i < 6; i++) {
      const d = new Date(clock.getTime() + i * 24 * 3600 * 1000);
      const iso = d.toISOString().slice(0, 10);
      const label = i === 0 ? 'Hôm nay' : `${d.getDate()}/${d.getMonth() + 1}`;
      dates.push({ iso, label });
    }
    return `
      <div class="space-y-4">
        <div>
          <label class="block text-xs font-bold text-[#2D342C] mb-1.5">Ngày thi đấu</label>
          <select id="filter-date" class="w-full rounded-[8px] border-[#DDE5D9] text-sm">
            <option value="ALL" ${_filters.date === 'ALL' ? 'selected' : ''}>Mọi ngày</option>
            ${dates.map((d) => `<option value="${d.iso}" ${_filters.date === d.iso ? 'selected' : ''}>${global.ArenaUI.esc(d.label)} (${formatDateVi(d.iso)})</option>`).join('')}
          </select>
        </div>

        <div>
          <label class="block text-xs font-bold text-[#2D342C] mb-1.5">Loại kèo</label>
          <div class="grid grid-cols-3 gap-2">
            ${[
              { v: 'ALL', l: 'Tất cả' },
              { v: 'A', l: 'Kèo A (Có sân)' },
              { v: 'B', l: 'Kèo B (Thỏa thuận)' }
            ].map((o) => `
              <button type="button" data-f-kind="${o.v}"
                      class="py-2 px-2 rounded-[8px] border text-xs font-semibold ${_filters.kind === o.v ? 'bg-[#3D6943] text-white border-[#3D6943]' : 'bg-white text-[#2D342C] border-[#DDE5D9]'}">
                ${o.l}
              </button>
            `).join('')}
          </div>
        </div>

        <div>
          <label class="block text-xs font-bold text-[#2D342C] mb-1.5">Loại sân</label>
          <div class="grid grid-cols-4 gap-2">
            ${[
              { v: 'ALL', l: 'Tất cả' },
              { v: 'FIVE_A_SIDE', l: 'Sân 5' },
              { v: 'SEVEN_A_SIDE', l: 'Sân 7' },
              { v: 'ELEVEN_A_SIDE', l: 'Sân 11' }
            ].map((o) => `
              <button type="button" data-f-fmt="${o.v}"
                      class="py-2 px-2 rounded-[8px] border text-xs font-semibold ${_filters.format === o.v ? 'bg-[#3D6943] text-white border-[#3D6943]' : 'bg-white text-[#2D342C] border-[#DDE5D9]'}">
                ${o.l}
              </button>
            `).join('')}
          </div>
        </div>

        <div>
          <label class="block text-xs font-bold text-[#2D342C] mb-1.5">Trình độ</label>
          <select id="filter-skill" class="w-full rounded-[8px] border-[#DDE5D9] text-sm">
            <option value="ALL" ${_filters.skill === 'ALL' ? 'selected' : ''}>Tất cả trình độ</option>
            <option value="BEGINNER" ${_filters.skill === 'BEGINNER' ? 'selected' : ''}>Mới chơi / Vui vẻ</option>
            <option value="INTERMEDIATE" ${_filters.skill === 'INTERMEDIATE' ? 'selected' : ''}>Trung bình / Văn phòng</option>
            <option value="ADVANCED" ${_filters.skill === 'ADVANCED' ? 'selected' : ''}>Khá / Bán chuyên</option>
          </select>
        </div>

        <div class="grid grid-cols-2 gap-3">
          <div>
            <label class="block text-xs font-bold text-[#2D342C] mb-1.5">Khoảng cách tối đa</label>
            <select id="filter-dist" class="w-full rounded-[8px] border-[#DDE5D9] text-sm">
              <option value="ALL" ${_filters.maxDistance === 'ALL' ? 'selected' : ''}>Không giới hạn</option>
              <option value="3" ${_filters.maxDistance === '3' ? 'selected' : ''}>Trong vòng 3 km</option>
              <option value="5" ${_filters.maxDistance === '5' ? 'selected' : ''}>Trong vòng 5 km</option>
              <option value="10" ${_filters.maxDistance === '10' ? 'selected' : ''}>Trong vòng 10 km</option>
            </select>
          </div>
          <div>
            <label class="block text-xs font-bold text-[#2D342C] mb-1.5">Khu vực</label>
            <select id="filter-ward" class="w-full rounded-[8px] border-[#DDE5D9] text-sm">
              <option value="ALL" ${_filters.wardId === 'ALL' ? 'selected' : ''}>Tất cả khu vực</option>
              ${wards.map((w) => `<option value="${global.ArenaUI.esc(w.id)}" ${_filters.wardId === w.id ? 'selected' : ''}>${global.ArenaUI.esc(w.name)}</option>`).join('')}
            </select>
          </div>
        </div>
      </div>
    `;
  }

  function bindFilterEvents(onApply) {
    document.querySelectorAll('[data-f-kind]').forEach((b) => {
      b.onclick = () => {
        _filters.kind = b.getAttribute('data-f-kind');
        openFilterSheet();
      };
    });
    document.querySelectorAll('[data-f-fmt]').forEach((b) => {
      b.onclick = () => {
        _filters.format = b.getAttribute('data-f-fmt');
        openFilterSheet();
      };
    });
    const applyBtn = document.getElementById('btn-filter-apply');
    const resetBtn = document.getElementById('btn-filter-reset');
    if (applyBtn) {
      applyBtn.onclick = () => {
        const dt = document.getElementById('filter-date');
        const sk = document.getElementById('filter-skill');
        const di = document.getElementById('filter-dist');
        const wa = document.getElementById('filter-ward');
        if (dt) _filters.date = dt.value;
        if (sk) _filters.skill = sk.value;
        if (di) _filters.maxDistance = di.value;
        if (wa) _filters.wardId = wa.value;
        if (typeof onApply === 'function') onApply();
      };
    }
    if (resetBtn) {
      resetBtn.onclick = () => {
        _filters.date = 'ALL';
        _filters.kind = 'ALL';
        _filters.format = 'ALL';
        _filters.skill = 'ALL';
        _filters.maxDistance = 'ALL';
        _filters.wardId = 'ALL';
        if (typeof onApply === 'function') onApply();
      };
    }
  }

  function openFilterSheet() {
    global.ArenaUI.openSheet({
      title: 'Bộ lọc kèo nâng cao (MATCH-02)',
      bodyHtml: getFilterFormHtml(),
      footerHtml: `
        <div class="grid grid-cols-2 gap-2.5">
          <button type="button" id="btn-filter-reset" class="py-2.5 rounded-[8px] border border-[#DDE5D9] bg-white text-xs font-bold text-[#2D342C]">Đặt lại</button>
          <button type="button" id="btn-filter-apply" class="py-2.5 rounded-[8px] bg-[#3D6943] text-white text-xs font-bold">Áp dụng</button>
        </div>
      `
    });
    setTimeout(() => {
      bindFilterEvents(() => {
        global.ArenaUI.closeSheet();
        global.ArenaRouter.navigate('MATCH-01');
      });
    }, 0);
  }

  /* =========================================================================
     MATCH-01: Khám phá kèo
     ========================================================================= */
  function renderMatch01(container) {
    const store = global.ArenaStore;
    const ui = global.ArenaUI;
    const state = store.getState();
    const allMatches = state.matches || [];
    const clock = store.getMockClock();

    // Generate 6-day strip from mockClock
    const dates = [];
    for (let i = 0; i < 6; i++) {
      const d = new Date(clock.getTime() + i * 24 * 3600 * 1000);
      const iso = d.toISOString().slice(0, 10);
      const label = i === 0 ? 'Hôm nay' : `${d.getDate()}/${d.getMonth() + 1}`;
      dates.push({ iso, label });
    }

    const filtered = allMatches.filter((m) => {
      const eff = getEffectiveStatus(m);
      if (eff !== 'OPEN' && eff !== 'CONFIRMED') return false;
      if (_filters.date !== 'ALL' && m.matchDate !== _filters.date) return false;
      if (_filters.kind !== 'ALL' && m.kind !== _filters.kind) return false;
      if (_filters.format !== 'ALL' && m.format !== _filters.format) return false;
      if (_filters.skill !== 'ALL' && m.skillLevel !== _filters.skill) return false;
      if (_filters.maxDistance !== 'ALL') {
        const maxD = Number(_filters.maxDistance);
        const vId = m.kind === 'A' ? m.venueId : (m.proposedVenues && m.proposedVenues[0]);
        const v = getVenue(vId);
        if (v && v.distanceKm > maxD) return false;
      }
      if (_filters.wardId !== 'ALL') {
        const vId = m.kind === 'A' ? m.venueId : (m.proposedVenues && m.proposedVenues[0]);
        const v = getVenue(vId);
        if (v && v.wardId !== _filters.wardId) return false;
      }
      return true;
    });

    const activeFilterCount = [_filters.date, _filters.kind, _filters.format, _filters.skill, _filters.maxDistance, _filters.wardId]
      .filter((x) => x !== 'ALL').length;

    container.innerHTML = `
      <div class="w-full flex-1 flex flex-col bg-[#F8FAF3] pb-6">
        <!-- Pitch Hero Banner -->
        <div class="bg-[#14532D] text-white px-4 pt-3 pb-4 relative overflow-hidden">
          <div class="relative z-10 flex items-center justify-between gap-2">
            <div>
              <h1 class="font-bold text-lg leading-tight">Tìm kèo giao lưu</h1>
              <p class="text-xs text-white/80 mt-0.5">Kèo A (có sân) & Kèo B (thương lượng sân/giờ)</p>
            </div>
            <button type="button" id="btn-create-match-cta"
                    class="px-3 py-2 rounded-[8px] bg-[#3D6943] hover:bg-[#315C38] border border-white/20 text-white text-xs font-bold flex items-center gap-1 shrink-0 shadow-sm">
              <span class="material-symbols-outlined text-[16px]">add_circle</span>
              Đăng kèo
            </button>
          </div>

          <!-- Quick Navigation Pills -->
          <div class="relative z-10 flex items-center gap-1.5 mt-3 overflow-x-auto no-scrollbar">
            <button type="button" onclick="window.ArenaRouter.navigate('MATCH-07')"
                    class="px-2.5 py-1.5 rounded-full bg-white/10 hover:bg-white/20 text-[11px] font-semibold whitespace-nowrap flex items-center gap-1">
              <span class="material-symbols-outlined text-[14px]">event_note</span>Lịch đội
            </button>
            <button type="button" onclick="window.ArenaRouter.navigate('MATCH-08')"
                    class="px-2.5 py-1.5 rounded-full bg-white/10 hover:bg-white/20 text-[11px] font-semibold whitespace-nowrap flex items-center gap-1">
              <span class="material-symbols-outlined text-[14px]">outbox</span>Đã gửi
            </button>
            <button type="button" onclick="window.ArenaRouter.navigate('MATCH-09')"
                    class="px-2.5 py-1.5 rounded-full bg-white/10 hover:bg-white/20 text-[11px] font-semibold whitespace-nowrap flex items-center gap-1">
              <span class="material-symbols-outlined text-[14px]">inbox</span>Gửi đến
            </button>
            <button type="button" onclick="window.ArenaRouter.navigate('PAIR-01')"
                    class="px-2.5 py-1.5 rounded-full bg-white/10 hover:bg-white/20 text-[11px] font-semibold whitespace-nowrap flex items-center gap-1">
              <span class="material-symbols-outlined text-[14px]">handshake</span>Thương lượng B
            </button>
          </div>
        </div>

        <div class="px-4 pt-3 space-y-3">
          ${actingTeamBarHtml()}

          <!-- Compact Header Bar with Single Filter Icon Button -->
          <div class="flex items-center justify-between gap-2">
            <span class="text-xs font-bold text-[#2D342C]">
              Danh sách kèo (${filtered.length})
            </span>
            <button type="button" id="btn-open-filter-sheet" aria-label="Bộ lọc kèo"
                    class="relative w-9 h-9 rounded-[8px] bg-white border border-[#DDE5D9] text-[#2D342C] hover:border-[#3D6943] flex items-center justify-center shrink-0">
              <span class="material-symbols-outlined text-[20px]">tune</span>
              ${activeFilterCount > 0 ? `<span class="absolute -top-1 -right-1 min-w-[16px] h-4 px-1 rounded-full bg-[#3D6943] text-white text-[10px] font-bold flex items-center justify-center leading-none">${activeFilterCount}</span>` : ''}
            </button>
          </div>

          <!-- Match List -->
          <div class="space-y-3">
            ${allMatches.length === 0 ? ui.emptyState({
              icon: 'sports_soccer',
              title: 'Chưa có kèo nào trên hệ thống',
              description: 'Hãy là đội đầu tiên tạo kèo giao lưu để tìm đối thủ phù hợp.',
              actionLabel: 'Tạo kèo mới',
              onAction: "window.ArenaRouter.navigate('MATCH-05')"
            }) : (filtered.length === 0 ? ui.emptyState({
              icon: 'filter_alt_off',
              title: 'Không tìm thấy kèo phù hợp',
              description: 'Thử bỏ bớt điều kiện lọc hoặc chọn ngày khác để xem thêm kèo.',
              actionLabel: 'Xóa bộ lọc',
              onAction: "window.ArenaMatch.resetFilters()"
            }) : filtered.map(renderMatchCard).join(''))}
          </div>
        </div>
      </div>
    `;

    const switchBtn = container.querySelector('#btn-switch-acting-team');
    if (switchBtn) switchBtn.onclick = () => openActingTeamSelector();

    container.querySelector('#btn-create-match-cta').onclick = () => {
      global.ArenaRouter.navigate('MATCH-05');
    };
    container.querySelector('#btn-open-filter-sheet').onclick = openFilterSheet;

    container.querySelectorAll('[data-open-match]').forEach((card) => {
      card.onclick = () => {
        const id = card.getAttribute('data-open-match');
        const kind = card.getAttribute('data-kind');
        global.ArenaRouter.navigate(kind === 'B' ? 'MATCH-04' : 'MATCH-03', { id });
      };
    });
  }

  /* =========================================================================
     MATCH-14: Sửa chi phí kèo A Sheet
     ========================================================================= */
  function openEditCostSheet(matchId) {
    const store = global.ArenaStore;
    const match = getMatch(matchId);
    if (!match || match.kind !== 'A' || getEffectiveStatus(match) !== 'OPEN') {
      global.ArenaUI.toast('Chỉ có thể sửa chi phí khi Kèo A đang OPEN.', 'warning');
      return;
    }
    const ui = global.ArenaUI;
    ui.openSheet({
      title: 'Sửa thỏa thuận chi phí (MATCH-14)',
      bodyHtml: `
        <div class="space-y-3">
          <div class="bg-[#EAF0E6] border border-[#DDE5D9] rounded-[8px] p-2.5 text-xs text-[#14532D] flex items-start gap-2">
            <span class="material-symbols-outlined text-[16px] shrink-0 mt-0.5">info</span>
            <span>Kèo A áp dụng thỏa thuận chi phí sân trực tiếp, không có bước đặt cọc.</span>
          </div>
          <div>
            <label class="block text-xs font-bold text-[#2D342C] mb-1">Tổng chi phí sân (VNĐ)</label>
            <input id="edit-a-cost" type="number" min="0" step="50000" value="${Number(match.totalCost || 0)}"
                   class="w-full rounded-[8px] border-[#DDE5D9] text-sm" />
          </div>
          <div>
            <label class="block text-xs font-bold text-[#2D342C] mb-1">Hình thức chia tiền</label>
            <select id="edit-a-split" class="w-full rounded-[8px] border-[#DDE5D9] text-sm">
              ${Object.keys(COST_SPLIT_LABELS).map((k) => `
                <option value="${k}" ${match.costSharing === k ? 'selected' : ''}>${ui.esc(COST_SPLIT_LABELS[k])}</option>
              `).join('')}
            </select>
          </div>
        </div>
      `,
      footerHtml: `
        <button type="button" id="btn-save-cost-a" class="w-full py-2.5 rounded-[8px] bg-[#3D6943] text-white text-xs font-bold">
          Lưu chi phí mới
        </button>
      `
    });
    setTimeout(() => {
      const btn = document.getElementById('btn-save-cost-a');
      if (btn) {
        btn.onclick = () => {
          const cost = Number(document.getElementById('edit-a-cost').value);
          const split = document.getElementById('edit-a-split').value;
          if (isNaN(cost) || cost < 0) {
            ui.toast('Chi phí không được âm.', 'error');
            return;
          }
          const shares = computeShares(cost, split);
          store.setState((s) => {
            const target = s.matches.find((m) => m.id === matchId);
            if (target) {
              target.totalCost = cost;
              target.costSharing = split;
              target.hostShare = shares.host;
              target.guestShare = shares.guest;
            }
          });
          ui.closeSheet();
          ui.toast('Đã cập nhật thỏa thuận chi phí Kèo A.', 'success');
        };
      }
    }, 0);
  }

  /* =========================================================================
     MATCH-12: Hủy kèo Sheet
     ========================================================================= */
  function openCancelMatchSheet(matchId) {
    const store = global.ArenaStore;
    const ui = global.ArenaUI;
    const match = getMatch(matchId);
    if (!match) return;
    const eff = getEffectiveStatus(match);
    const isConfirmed = eff === 'CONFIRMED';
    const pen = isConfirmed ? calculateCancelPenalty(match) : { penalty: 0, blocked: false, text: 'Kèo đang OPEN — hủy không bị trừ điểm uy tín.' };
    const pendingReqs = (store.getState().matchRequests || []).filter((r) => r.matchId === matchId && r.status === 'PENDING').length;

    ui.openSheet({
      title: 'Hủy kèo đấu (MATCH-12)',
      bodyHtml: `
        <div class="space-y-3">
          <div class="p-3 rounded-[8px] border ${pen.blocked ? 'bg-[#FEF2F2] border-[#FECACA] text-[#991B1B]' : (isConfirmed ? 'bg-[#FEF3C7] border-[#FDE68A] text-[#92400E]' : 'bg-[#EAF0E6] border-[#DDE5D9] text-[#14532D]')} text-xs space-y-1">
            <div class="font-bold">Trạng thái hiện tại: ${ui.esc(MATCH_STATUS_META[eff] ? MATCH_STATUS_META[eff].label : eff)}</div>
            <div>${ui.esc(pen.text)}</div>
            ${!isConfirmed && pendingReqs > 0 ? `<div>• ${pendingReqs} yêu cầu đang chờ sẽ tự động bị từ chối.</div>` : ''}
          </div>
          ${!pen.blocked ? `
            <div>
              <label class="block text-xs font-bold text-[#2D342C] mb-1">Lý do hủy kèo ${isConfirmed ? '<span class="text-[#DC2626]">*</span>' : '(tùy chọn)'}</label>
              <textarea id="cancel-match-reason" rows="3" placeholder="Ví dụ: Đội thiếu người đột xuất, thời tiết xấu..."
                        class="w-full rounded-[8px] border-[#DDE5D9] text-sm"></textarea>
            </div>
          ` : ''}
        </div>
      `,
      footerHtml: pen.blocked ? '' : `
        <div class="grid grid-cols-2 gap-2">
          <button type="button" onclick="window.ArenaUI.closeSheet()" class="py-2.5 rounded-[8px] border border-[#DDE5D9] bg-white text-xs font-bold">Đóng</button>
          <button type="button" id="btn-confirm-cancel-match" class="py-2.5 rounded-[8px] bg-[#DC2626] text-white text-xs font-bold">Xác nhận hủy kèo</button>
        </div>
      `
    });

    setTimeout(() => {
      const btn = document.getElementById('btn-confirm-cancel-match');
      if (btn) {
        btn.onclick = () => {
          const reason = (document.getElementById('cancel-match-reason') || {}).value || '';
          if (isConfirmed && !reason.trim()) {
            ui.toast('Vui lòng nhập lý do hủy kèo đã chốt.', 'error');
            return;
          }
          // Check expectedStatus conflict
          const latest = getMatch(matchId);
          if (!latest || getEffectiveStatus(latest) !== eff) {
            ui.toast('409 Conflict: Trạng thái kèo vừa thay đổi, vui lòng tải lại.', 'error');
            ui.closeSheet();
            global.ArenaRouter.refresh();
            return;
          }
          store.setState((s) => {
            const m = s.matches.find((x) => x.id === matchId);
            if (m) {
              m.status = 'CANCELLED';
              m.cancelReason = reason.trim() || 'Đội chủ nhà hủy kèo';
            }
            (s.matchRequests || []).forEach((r) => {
              if (r.matchId === matchId && r.status === 'PENDING') r.status = 'REJECTED';
            });
            (s.pairings || []).forEach((p) => {
              if (p.matchId === matchId && p.status === 'NEGOTIATING') p.status = 'CANCELLED';
            });
            if (isConfirmed && pen.penalty < 0) {
              const actingTeam = s.teams.find((t) => t.id === s.actingTeamId);
              if (actingTeam) actingTeam.reputationScore = Math.max(0, (actingTeam.reputationScore || 100) + pen.penalty);
            }
          });
          ui.closeSheet();
          ui.toast('Đã hủy kèo đấu.', 'success');
        };
      }
    }, 0);
  }

  /* =========================================================================
     MATCH-11: Rút yêu cầu Sheet
     ========================================================================= */
  function openWithdrawRequestSheet(reqId) {
    const store = global.ArenaStore;
    const ui = global.ArenaUI;
    const req = store.findById('matchRequests', reqId);
    if (!req) return;
    const match = getMatch(req.matchId);
    const isAccepted = req.status === 'ACCEPTED';
    const pen = isAccepted ? calculateCancelPenalty(match) : { penalty: 0, blocked: false, text: 'Yêu cầu đang PENDING — rút miễn phí, không trừ uy tín.' };

    ui.openSheet({
      title: 'Rút yêu cầu bắt kèo (MATCH-11)',
      bodyHtml: `
        <div class="p-3 rounded-[8px] border ${pen.blocked ? 'bg-[#FEF2F2] border-[#FECACA] text-[#991B1B]' : (isAccepted ? 'bg-[#FEF3C7] border-[#FDE68A] text-[#92400E]' : 'bg-[#EAF0E6] border-[#DDE5D9] text-[#14532D]')} text-xs space-y-1.5">
          <div class="font-bold">${isAccepted ? 'Cảnh báo: Kèo đã được chốt!' : 'Xác nhận rút yêu cầu'}</div>
          <div>${ui.esc(pen.text)}</div>
          ${isAccepted && !pen.blocked ? '<div>Kèo đấu sẽ bị hủy và thông báo tới đội chủ nhà.</div>' : ''}
        </div>
      `,
      footerHtml: pen.blocked ? '' : `
        <div class="grid grid-cols-2 gap-2">
          <button type="button" onclick="window.ArenaUI.closeSheet()" class="py-2.5 rounded-[8px] border border-[#DDE5D9] bg-white text-xs font-bold">Giữ lại</button>
          <button type="button" id="btn-confirm-withdraw" class="py-2.5 rounded-[8px] bg-[#DC2626] text-white text-xs font-bold">Xác nhận rút</button>
        </div>
      `
    });

    setTimeout(() => {
      const btn = document.getElementById('btn-confirm-withdraw');
      if (btn) {
        btn.onclick = () => {
          store.setState((s) => {
            const target = s.matchRequests.find((r) => r.id === reqId);
            if (!target) return;
            target.status = 'WITHDRAWN';
            if (isAccepted) {
              const m = s.matches.find((x) => x.id === target.matchId);
              if (m) m.status = 'CANCELLED';
              const team = s.teams.find((t) => t.id === target.requesterTeamId);
              if (team && pen.penalty < 0) team.reputationScore = Math.max(0, (team.reputationScore || 100) + pen.penalty);
            }
          });
          ui.closeSheet();
          ui.toast('Đã rút yêu cầu bắt kèo.', 'success');
        };
      }
    }, 0);
  }

  /* =========================================================================
     MATCH-03: Chi tiết kèo A
     ========================================================================= */
  function renderMatch03(container, params) {
    const store = global.ArenaStore;
    const ui = global.ArenaUI;
    const state = store.getState();
    const matchId = params.id || 'match-a-open';
    const match = getMatch(matchId) || (state.matches || []).find((m) => m.kind === 'A');

    if (!match) {
      container.innerHTML = `<div class="p-4">${ui.errorState({ title: 'Không tìm thấy kèo đấu', message: 'Kèo này có thể đã bị xóa hoặc không tồn tại.', code: '404' })}</div>`;
      return;
    }
    if (match.kind === 'B' && !params._forceA) {
      renderMatch04(container, { id: match.id });
      return;
    }

    const eff = params._previewStatus || getEffectiveStatus(match);
    const statusMeta = MATCH_STATUS_META[eff] || MATCH_STATUS_META.OPEN;
    const host = getTeam(match.hostTeamId);
    const guest = getTeam(match.guestTeamId || match.confirmedGuestTeamId);
    const venue = getVenue(match.venueId);
    const acting = store.getActingTeam();
    const isHostCap = acting && acting.id === match.hostTeamId;
    const reqsForMatch = (state.matchRequests || []).filter((r) => r.matchId === match.id);
    const pendingCount = reqsForMatch.filter((r) => r.status === 'PENDING').length;
    const myReq = acting ? reqsForMatch.find((r) => r.requesterTeamId === acting.id && r.status !== 'WITHDRAWN') : null;
    const shares = computeShares(match.totalCost, match.costSharing);

    container.innerHTML = `
      <div class="w-full flex-1 flex flex-col bg-[#F8FAF3] pb-8">
        <div class="p-4 space-y-3">
          <!-- Status & Kind Header Card -->
          <div class="bg-white rounded-[8px] border border-[#DDE5D9] p-3.5 space-y-3">
            <div class="flex items-center justify-between gap-2">
              <div class="flex items-center gap-1.5">
                ${ui.badge('Kèo A • Sân cố định', 'dark')}
                ${ui.badge(statusMeta.label, statusMeta.variant)}
              </div>
              <button type="button" onclick="window.ArenaRouter.navigate('MATCH-13', { id: '${ui.esc(match.id)}' })"
                      class="text-[11px] font-semibold text-[#3D6943] underline">Vòng đời</button>
            </div>

            <!-- Host Team -->
            <div class="flex items-center justify-between gap-2 pt-1">
              <div class="flex items-center gap-2.5 min-w-0">
                ${ui.avatar(host || { name: 'Đội chủ nhà' }, { size: 42 })}
                <div class="min-w-0">
                  <div class="text-[11px] text-[#596158]">Đội chủ nhà</div>
                  <div class="font-bold text-sm text-[#111827] truncate">${ui.esc(host ? host.name : '—')}</div>
                  <div class="text-[11px] text-[#596158]">Uy tín ${host ? host.reputationScore : 90} • ${host ? host.ratingAvg : '4.5'} ★</div>
                </div>
              </div>
              <button type="button" onclick="window.ArenaRouter.navigate('TEAM-04', { id: '${ui.esc(match.hostTeamId)}' })"
                      class="px-2.5 py-1.5 rounded-[8px] bg-[#EAF0E6] text-[#14532D] text-xs font-semibold shrink-0">Xem đội</button>
            </div>

            ${guest ? `
              <div class="flex items-center justify-between gap-2 pt-2.5 border-t border-[#E5E7EB]">
                <div class="flex items-center gap-2.5 min-w-0">
                  ${ui.avatar(guest, { size: 42 })}
                  <div class="min-w-0">
                    <div class="text-[11px] text-[#16A34A] font-semibold">Đối thủ đã chốt</div>
                    <div class="font-bold text-sm text-[#111827] truncate">${ui.esc(guest.name)}</div>
                    <div class="text-[11px] text-[#596158]">Uy tín ${guest.reputationScore} • ${guest.ratingAvg} ★</div>
                  </div>
                </div>
                <button type="button" onclick="window.ArenaRouter.navigate('TEAM-04', { id: '${ui.esc(guest.id)}' })"
                        class="px-2.5 py-1.5 rounded-[8px] bg-[#F0FDF4] border border-[#BBF7D0] text-[#16A34A] text-xs font-semibold shrink-0">Hồ sơ</button>
              </div>
            ` : ''}
          </div>

          <!-- Time & Venue -->
          <div class="bg-white rounded-[8px] border border-[#DDE5D9] p-3.5 space-y-2.5">
            <h2 class="font-bold text-sm text-[#111827]">Thời gian & Địa điểm</h2>
            <div class="flex items-center gap-2 text-xs text-[#2D342C]">
              <span class="material-symbols-outlined text-[18px] text-[#3D6943]">calendar_month</span>
              <span class="font-semibold">${formatDateVi(match.matchDate)}</span>
              <span>•</span>
              <span class="font-semibold">${ui.esc(match.startTime || '19:00')} - ${ui.esc(match.endTime || '20:30')}</span>
            </div>
            <div class="flex items-start justify-between gap-2 text-xs text-[#2D342C]">
              <div class="flex items-start gap-2 min-w-0">
                <span class="material-symbols-outlined text-[18px] text-[#3D6943] shrink-0">stadium</span>
                <div class="min-w-0">
                  <div class="font-bold text-[#111827]">${ui.esc(venue ? venue.name : 'Chưa rõ sân')}</div>
                  <div class="text-[#596158]">${ui.esc(venue ? venue.address : '')}</div>
                </div>
              </div>
              ${venue ? `
                <button type="button" onclick="window.ArenaRouter.navigate('VEN-02', { id: '${ui.esc(venue.id)}' })"
                        class="text-xs font-semibold text-[#3D6943] shrink-0">Chi tiết sân</button>
              ` : ''}
            </div>
            <div class="flex items-center gap-2 text-xs text-[#596158]">
              <span class="material-symbols-outlined text-[18px] text-[#3D6943]">sports_soccer</span>
              <span>${ui.esc(FORMAT_LABELS[match.format] || 'Sân 7')} • ${ui.esc(SKILL_LABELS[match.skillLevel] || 'Trung bình')}</span>
            </div>
          </div>

          <!-- Cost Agreement (MATCH-14 hook) -->
          <div class="bg-white rounded-[8px] border border-[#DDE5D9] p-3.5 space-y-2">
            <div class="flex items-center justify-between gap-2">
              <h2 class="font-bold text-sm text-[#111827]">Thỏa thuận chi phí</h2>
              ${isHostCap && eff === 'OPEN' ? `
                <button type="button" id="btn-edit-cost-a" class="text-xs font-semibold text-[#3D6943] flex items-center gap-1">
                  <span class="material-symbols-outlined text-[15px]">edit</span>Sửa chi phí
                </button>
              ` : ''}
            </div>
            <div class="grid grid-cols-2 gap-2 text-xs bg-[#F8FAF3] p-2.5 rounded-[8px] border border-[#EAF0E6]">
              <div>
                <div class="text-[#596158]">Tổng tiền sân</div>
                <div class="font-bold text-[#14532D] text-sm">${formatMoney(match.totalCost)}</div>
              </div>
              <div>
                <div class="text-[#596158]">Hình thức chia</div>
                <div class="font-semibold text-[#111827]">${ui.esc(COST_SPLIT_LABELS[match.costSharing] || 'Chia đôi 50/50')}</div>
              </div>
              <div>
                <div class="text-[#596158]">Chủ nhà dự kiến</div>
                <div class="font-semibold">${formatMoney(shares.host)}</div>
              </div>
              <div>
                <div class="text-[#596158]">Đội khách dự kiến</div>
                <div class="font-semibold">${formatMoney(shares.guest)}</div>
              </div>
            </div>
            ${match.notes ? `<p class="text-xs text-[#596158] italic">"${ui.esc(match.notes)}"</p>` : ''}
          </div>

          <!-- Actions by Role & Lifecycle -->
          <div id="match-a-actions" class="space-y-2 pt-1"></div>
        </div>
      </div>
    `;

    const editCostBtn = container.querySelector('#btn-edit-cost-a');
    if (editCostBtn) editCostBtn.onclick = () => openEditCostSheet(match.id);

    const actionsEl = container.querySelector('#match-a-actions');
    if (eff === 'OPEN') {
      if (isHostCap) {
        actionsEl.innerHTML = `
          <button type="button" onclick="window.ArenaRouter.navigate('MATCH-09', { matchId: '${ui.esc(match.id)}' })"
                  class="w-full py-3 rounded-[8px] bg-[#3D6943] hover:bg-[#315C38] text-white text-sm font-bold flex items-center justify-center gap-2">
            <span class="material-symbols-outlined text-[18px]">inbox</span>
            Xem yêu cầu gửi đến (${pendingCount} chờ duyệt)
          </button>
          <button type="button" id="btn-cancel-match-a"
                  class="w-full py-2.5 rounded-[8px] bg-white border border-[#FECACA] text-[#DC2626] text-xs font-bold">
            Hủy kèo đấu
          </button>
        `;
        actionsEl.querySelector('#btn-cancel-match-a').onclick = () => openCancelMatchSheet(match.id);
      } else if (acting) {
        if (myReq) {
          const rMeta = REQ_STATUS_META[myReq.status] || REQ_STATUS_META.PENDING;
          actionsEl.innerHTML = `
            <div class="bg-white rounded-[8px] border border-[#DDE5D9] p-3 flex items-center justify-between gap-2">
              <div class="text-xs">
                <div class="text-[#596158]">Trạng thái yêu cầu của ${ui.esc(acting.name)}</div>
                <div class="mt-1">${ui.badge(rMeta.label, rMeta.variant)}</div>
              </div>
              <div class="flex items-center gap-2">
                <button type="button" onclick="window.ArenaRouter.navigate('CHAT-02', { contextId: '${ui.esc(myReq.id)}' })"
                        class="px-3 py-2 rounded-[8px] bg-[#EAF0E6] text-[#14532D] text-xs font-bold">Nhắn tin</button>
                ${myReq.status === 'PENDING' ? `
                  <button type="button" id="btn-withdraw-my-req" class="px-3 py-2 rounded-[8px] bg-[#FEF2F2] text-[#DC2626] text-xs font-bold">Rút</button>
                ` : ''}
              </div>
            </div>
          `;
          const wBtn = actionsEl.querySelector('#btn-withdraw-my-req');
          if (wBtn) wBtn.onclick = () => openWithdrawRequestSheet(myReq.id);
        } else {
          actionsEl.innerHTML = `
            <div class="bg-white rounded-[8px] border border-[#DDE5D9] p-3 space-y-2.5">
              <label class="block text-xs font-bold text-[#2D342C]">Lời nhắn gửi đội chủ nhà (${ui.esc(acting.name)})</label>
              <input id="req-send-msg" type="text" value="Đội mình muốn giao lưu trận này, đúng giờ fair play!"
                     class="w-full rounded-[8px] border-[#DDE5D9] text-sm" />
              <button type="button" id="btn-send-match-req"
                      class="w-full py-3 rounded-[8px] bg-[#3D6943] hover:bg-[#315C38] text-white text-sm font-bold">
                Gửi yêu cầu bắt kèo
              </button>
            </div>
          `;
          actionsEl.querySelector('#btn-send-match-req').onclick = () => {
            const msg = (actionsEl.querySelector('#req-send-msg').value || '').trim();
            const newId = 'req-' + Date.now();
            store.setState((s) => {
              s.matchRequests.push({
                id: newId,
                matchId: match.id,
                requesterTeamId: acting.id,
                status: 'PENDING',
                message: msg || 'Xin phép bắt kèo giao lưu!',
                createdAt: store.getMockClock().toISOString()
              });
            });
            ui.toast('Đã gửi yêu cầu bắt kèo!', 'success');
          };
        }
      } else {
        actionsEl.innerHTML = `
          <div class="bg-[#EAF0E6] rounded-[8px] p-3 text-xs text-[#14532D] text-center">
            Đăng nhập với vai trò Đội trưởng để gửi yêu cầu bắt kèo.
          </div>
        `;
      }
    } else if (eff === 'CONFIRMED') {
      actionsEl.innerHTML = `
        <div class="grid grid-cols-2 gap-2">
          <button type="button" onclick="window.ArenaRouter.navigate('CHAT-02')"
                  class="py-2.5 rounded-[8px] bg-[#3D6943] text-white text-xs font-bold flex items-center justify-center gap-1">
            <span class="material-symbols-outlined text-[16px]">chat</span>Chat 2 đội
          </button>
          <button type="button" onclick="window.ArenaRouter.navigate('MERC-08', { matchId: '${ui.esc(match.id)}' })"
                  class="py-2.5 rounded-[8px] bg-[#EAF0E6] text-[#14532D] text-xs font-bold flex items-center justify-center gap-1">
            <span class="material-symbols-outlined text-[16px]">person_add</span>Tuyển đá lẻ
          </button>
        </div>
        ${isHostCap ? `
          <button type="button" id="btn-cancel-confirmed-a"
                  class="w-full py-2.5 rounded-[8px] bg-white border border-[#FECACA] text-[#DC2626] text-xs font-bold">
            Hủy kèo đã chốt
          </button>
        ` : ''}
      `;
      const ccBtn = actionsEl.querySelector('#btn-cancel-confirmed-a');
      if (ccBtn) ccBtn.onclick = () => openCancelMatchSheet(match.id);
    } else if (eff === 'COMPLETED') {
      actionsEl.innerHTML = `
        <div class="grid grid-cols-2 gap-2">
          <button type="button" onclick="window.ArenaRouter.navigate('REP-01', { matchId: '${ui.esc(match.id)}' })"
                  class="py-2.5 rounded-[8px] bg-[#3D6943] text-white text-xs font-bold">Đánh giá đối thủ</button>
          <button type="button" onclick="window.ArenaRouter.navigate('REP-06', { matchId: '${ui.esc(match.id)}' })"
                  class="py-2.5 rounded-[8px] bg-white border border-[#FECACA] text-[#DC2626] text-xs font-bold">Báo cáo vi phạm</button>
        </div>
      `;
    } else {
      actionsEl.innerHTML = `
        <div class="bg-white rounded-[8px] border border-[#DDE5D9] p-3 text-xs text-[#596158] text-center">
          Kèo ở trạng thái <strong>${ui.esc(statusMeta.label)}</strong> — không thể nhận thêm yêu cầu.
        </div>
      `;
    }
  }

  /* =========================================================================
     MATCH-04: Chi tiết kèo B
     ========================================================================= */
  function renderMatch04(container, params) {
    const store = global.ArenaStore;
    const ui = global.ArenaUI;
    const state = store.getState();
    const matchId = params.id || 'match-b-open';
    const match = getMatch(matchId) || (state.matches || []).find((m) => m.kind === 'B');

    if (!match) {
      container.innerHTML = `<div class="p-4">${ui.errorState({ title: 'Không tìm thấy kèo B', code: '404' })}</div>`;
      return;
    }

    const eff = getEffectiveStatus(match);
    const statusMeta = MATCH_STATUS_META[eff] || MATCH_STATUS_META.OPEN;
    const host = getTeam(match.hostTeamId);
    const acting = store.getActingTeam();
    const isHostCap = acting && acting.id === match.hostTeamId;
    const matchPairings = (state.pairings || []).filter((p) => p.matchId === match.id);
    const myPairing = acting ? matchPairings.find((p) => p.guestTeamId === acting.id) : null;

    container.innerHTML = `
      <div class="w-full flex-1 flex flex-col bg-[#F8FAF3] pb-8">
        <div class="p-4 space-y-3">
          <div class="bg-white rounded-[8px] border border-[#DDE5D9] p-3.5 space-y-3">
            <div class="flex items-center justify-between gap-2">
              <div class="flex items-center gap-1.5">
                ${ui.badge('Kèo B • Thương lượng', 'info')}
                ${ui.badge(statusMeta.label, statusMeta.variant)}
              </div>
              <span class="text-xs font-semibold text-[#596158]">${formatDateVi(match.matchDate)}</span>
            </div>

            <div class="flex items-center justify-between gap-2">
              <div class="flex items-center gap-2.5 min-w-0">
                ${ui.avatar(host || { name: 'Chủ kèo' }, { size: 42 })}
                <div class="min-w-0">
                  <div class="text-[11px] text-[#596158]">Chủ kèo B</div>
                  <div class="font-bold text-sm text-[#111827] truncate">${ui.esc(host ? host.name : '—')}</div>
                  <div class="text-[11px] text-[#596158]">Uy tín ${host ? host.reputationScore : 88} • ${ui.esc(FORMAT_LABELS[match.format] || 'Sân 7')}</div>
                </div>
              </div>
              <button type="button" onclick="window.ArenaRouter.navigate('TEAM-04', { id: '${ui.esc(match.hostTeamId)}' })"
                      class="px-2.5 py-1.5 rounded-[8px] bg-[#EAF0E6] text-[#14532D] text-xs font-semibold shrink-0">Xem đội</button>
            </div>
          </div>

          <!-- 1-5 Proposed Venues -->
          <div class="bg-white rounded-[8px] border border-[#DDE5D9] p-3.5 space-y-2">
            <h2 class="font-bold text-sm text-[#111827]">Sân đề xuất (${(match.proposedVenues || []).length}/5 lựa chọn)</h2>
            <div class="space-y-2">
              ${(match.proposedVenues || []).map((vid) => {
                const v = getVenue(vid);
                return `
                  <div class="p-2.5 rounded-[8px] bg-[#F8FAF3] border border-[#EAF0E6] flex items-center justify-between gap-2">
                    <div class="min-w-0">
                      <div class="font-semibold text-xs text-[#111827] truncate">${ui.esc(v ? v.name : vid)}</div>
                      <div class="text-[11px] text-[#596158] truncate">${ui.esc(v ? v.address : '')}</div>
                    </div>
                    ${v ? `
                      <button type="button" onclick="window.ArenaRouter.navigate('VEN-02', { id: '${ui.esc(v.id)}' })"
                              class="text-[11px] font-semibold text-[#3D6943] shrink-0">Xem sân</button>
                    ` : ''}
                  </div>
                `;
              }).join('')}
            </div>
          </div>

          <!-- 1-5 Proposed Time Slots -->
          <div class="bg-white rounded-[8px] border border-[#DDE5D9] p-3.5 space-y-2">
            <h2 class="font-bold text-sm text-[#111827]">Khung giờ đề xuất (${(match.proposedTimeSlots || []).length}/5 lựa chọn)</h2>
            <div class="flex flex-wrap gap-2">
              ${(match.proposedTimeSlots || []).map((slot) => `
                <span class="px-3 py-1.5 rounded-[8px] bg-[#EAF0E6] text-[#14532D] text-xs font-semibold">${ui.esc(slot)}</span>
              `).join('')}
            </div>
          </div>

          <!-- Cost & Deposit -->
          <div class="bg-white rounded-[8px] border border-[#DDE5D9] p-3.5 space-y-2 text-xs">
            <h2 class="font-bold text-sm text-[#111827]">Chi phí & Đặt cọc dự kiến</h2>
            <div class="flex justify-between"><span class="text-[#596158]">Tổng tiền sân:</span><span class="font-bold">${formatMoney(match.totalCost)}</span></div>
            <div class="flex justify-between"><span class="text-[#596158]">Chia tiền:</span><span class="font-semibold">${ui.esc(COST_SPLIT_LABELS[match.costSharing] || '50/50')}</span></div>
            <div class="flex justify-between"><span class="text-[#596158]">Tiền cọc giữ sân:</span><span class="font-semibold">${formatMoney(match.depositAmount || 0)} (${match.depositHolder === 'HOST' ? 'Chủ kèo giữ' : 'Thỏa thuận'})</span></div>
            ${match.notes ? `<p class="text-[#596158] italic pt-1 border-t border-[#E5E7EB]">"${ui.esc(match.notes)}"</p>` : ''}
          </div>

          <!-- Actions -->
          <div class="space-y-2">
            ${isHostCap ? `
              <button type="button" onclick="window.ArenaRouter.navigate('PAIR-01', { matchId: '${ui.esc(match.id)}' })"
                      class="w-full py-3 rounded-[8px] bg-[#3D6943] hover:bg-[#315C38] text-white text-sm font-bold flex items-center justify-center gap-2">
                <span class="material-symbols-outlined text-[18px]">handshake</span>
                Mở danh sách thương lượng (${matchPairings.length} đội)
              </button>
              ${eff === 'OPEN' ? `
                <button type="button" id="btn-cancel-match-b" class="w-full py-2.5 rounded-[8px] bg-white border border-[#FECACA] text-[#DC2626] text-xs font-bold">
                  Hủy kèo B
                </button>
              ` : ''}
            ` : (acting && eff === 'OPEN' ? (myPairing ? `
              <button type="button" onclick="window.ArenaRouter.navigate('PAIR-03', { id: '${ui.esc(myPairing.id)}' })"
                      class="w-full py-3 rounded-[8px] bg-[#3D6943] text-white text-sm font-bold">
                Vào phòng thương lượng (Rev #${myPairing.package.revision})
              </button>
            ` : `
              <button type="button" id="btn-start-pairing"
                      class="w-full py-3 rounded-[8px] bg-[#3D6943] hover:bg-[#315C38] text-white text-sm font-bold flex items-center justify-center gap-2">
                <span class="material-symbols-outlined text-[18px]">handshake</span>
                Nhận kèo & Mở phòng thương lượng
              </button>
            `) : '')}
          </div>
        </div>
      </div>
    `;

    const cancelBtn = container.querySelector('#btn-cancel-match-b');
    if (cancelBtn) cancelBtn.onclick = () => openCancelMatchSheet(match.id);

    const startBtn = container.querySelector('#btn-start-pairing');
    if (startBtn && acting) {
      startBtn.onclick = () => {
        const newId = 'pairing-' + Date.now();
        store.setState((s) => {
          s.pairings.push({
            id: newId,
            matchId: match.id,
            hostTeamId: match.hostTeamId,
            guestTeamId: acting.id,
            status: 'NEGOTIATING',
            package: {
              venueId: (match.proposedVenues && match.proposedVenues[0]) || 'ven-anphu',
              timeSlot: (match.proposedTimeSlots && match.proposedTimeSlots[0]) || '19:00 - 20:30',
              totalCost: match.totalCost || 600000,
              costSharing: match.costSharing || 'SPLIT_50_50',
              depositAmount: match.depositAmount || 0,
              depositHolder: match.hostTeamId,
              revision: 1,
              confirms: {
                [match.hostTeamId]: false,
                [acting.id]: false
              }
            },
            createdAt: store.getMockClock().toISOString()
          });
        });
        ui.toast('Đã mở phòng thương lượng riêng với chủ kèo!', 'success');
        global.ArenaRouter.navigate('PAIR-03', { id: newId });
      };
    }
  }

  /* =========================================================================
     MATCH-05: Tạo kèo A/B
     ========================================================================= */
  function renderMatch05(container) {
    const store = global.ArenaStore;
    const ui = global.ArenaUI;
    const capTeams = store.getMyCaptainedTeams();

    if (capTeams.length === 0) {
      container.innerHTML = `<div class="p-4">${ui.emptyState({
        icon: 'groups',
        title: 'Cần có đội bóng để tạo kèo',
        description: 'Bạn phải là Đội trưởng của ít nhất 1 đội đang hoạt động mới có thể đăng kèo.',
        actionLabel: 'Tạo đội mới',
        onAction: "window.ArenaRouter.navigate('TEAM-03')"
      })}</div>`;
      return;
    }

    const draft = getDraft();
    const venues = (store.getState().venues || []);
    const activeVenues = venues.filter((v) => v.status === 'ACTIVE');

    container.innerHTML = `
      <div class="w-full flex-1 flex flex-col bg-[#F8FAF3] pb-8">
        <div class="p-4 space-y-3">
          ${actingTeamBarHtml()}

          <!-- Kind Toggle -->
          <div class="bg-white rounded-[8px] border border-[#DDE5D9] p-3">
            <label class="block text-xs font-bold text-[#2D342C] mb-2">Chọn loại kèo</label>
            <div class="grid grid-cols-2 gap-2">
              <button type="button" data-draft-kind="A"
                      class="p-2.5 rounded-[8px] border text-left ${draft.kind === 'A' ? 'bg-[#EAF0E6] border-[#3D6943]' : 'bg-white border-[#DDE5D9]'}">
                <div class="font-bold text-xs text-[#14532D]">Kèo A • Đã có sân</div>
                <div class="text-[11px] text-[#596158] mt-0.5">1 sân & 1 khung giờ cố định</div>
              </button>
              <button type="button" data-draft-kind="B"
                      class="p-2.5 rounded-[8px] border text-left ${draft.kind === 'B' ? 'bg-[#EAF0E6] border-[#3D6943]' : 'bg-white border-[#DDE5D9]'}">
                <div class="font-bold text-xs text-[#14532D]">Kèo B • Thương lượng</div>
                <div class="text-[11px] text-[#596158] mt-0.5">1–5 sân & 1–5 giờ đề xuất</div>
              </button>
            </div>
          </div>

          <!-- Basic Info -->
          <div class="bg-white rounded-[8px] border border-[#DDE5D9] p-3.5 space-y-3">
            <div class="grid grid-cols-2 gap-2.5">
              <div>
                <label class="block text-xs font-bold text-[#2D342C] mb-1">Loại sân</label>
                <select id="d-format" class="w-full rounded-[8px] border-[#DDE5D9] text-sm">
                  ${Object.keys(FORMAT_LABELS).map((k) => `<option value="${k}" ${draft.format === k ? 'selected' : ''}>${FORMAT_LABELS[k]}</option>`).join('')}
                </select>
              </div>
              <div>
                <label class="block text-xs font-bold text-[#2D342C] mb-1">Trình độ</label>
                <select id="d-skill" class="w-full rounded-[8px] border-[#DDE5D9] text-sm">
                  ${Object.keys(SKILL_LABELS).map((k) => `<option value="${k}" ${draft.skillLevel === k ? 'selected' : ''}>${SKILL_LABELS[k]}</option>`).join('')}
                </select>
              </div>
            </div>

            <div>
              <label class="block text-xs font-bold text-[#2D342C] mb-1">Ngày thi đấu</label>
              <input id="d-date" type="date" value="${ui.esc(draft.matchDate)}" class="w-full rounded-[8px] border-[#DDE5D9] text-sm" />
            </div>

            ${draft.kind === 'A' ? `
              <div>
                <div class="flex items-center justify-between mb-1">
                  <label class="text-xs font-bold text-[#2D342C]">Chọn sân bóng (chỉ sân ACTIVE)</label>
                  <button type="button" id="btn-pick-ven-01" class="text-xs font-semibold text-[#3D6943]">Tìm sân (VEN-01)</button>
                </div>
                <select id="d-venue-a" class="w-full rounded-[8px] border-[#DDE5D9] text-sm">
                  ${venues.map((v) => `
                    <option value="${ui.esc(v.id)}" ${v.status !== 'ACTIVE' ? 'disabled' : ''} ${draft.venueId === v.id ? 'selected' : ''}>
                      ${ui.esc(v.name)}${v.status !== 'ACTIVE' ? ' [NGỪNG HOẠT ĐỘNG]' : ''}
                    </option>
                  `).join('')}
                </select>
              </div>
              <div class="grid grid-cols-2 gap-2.5">
                <div>
                  <label class="block text-xs font-bold text-[#2D342C] mb-1">Giờ bắt đầu</label>
                  <input id="d-start" type="time" value="${ui.esc(draft.startTime)}" class="w-full rounded-[8px] border-[#DDE5D9] text-sm" />
                </div>
                <div>
                  <label class="block text-xs font-bold text-[#2D342C] mb-1">Giờ kết thúc</label>
                  <input id="d-end" type="time" value="${ui.esc(draft.endTime)}" class="w-full rounded-[8px] border-[#DDE5D9] text-sm" />
                </div>
              </div>
            ` : `
              <div>
                <label class="block text-xs font-bold text-[#2D342C] mb-1.5">Sân đề xuất (chọn 1–5 sân)</label>
                <div class="space-y-1.5">
                  ${activeVenues.map((v) => {
                    const checked = (draft.proposedVenues || []).includes(v.id);
                    return `
                      <label class="flex items-center gap-2 p-2 rounded-[8px] border border-[#DDE5D9] text-xs cursor-pointer">
                        <input type="checkbox" data-b-venue="${ui.esc(v.id)}" ${checked ? 'checked' : ''} class="rounded text-[#3D6943]" />
                        <span class="font-medium flex-1">${ui.esc(v.name)}</span>
                        <span class="text-[#596158]">${v.distanceKm} km</span>
                      </label>
                    `;
                  }).join('')}
                </div>
              </div>
              <div>
                <label class="block text-xs font-bold text-[#2D342C] mb-1.5">Khung giờ đề xuất (1–5 khung giờ)</label>
                <div class="flex flex-wrap gap-1.5 mb-2">
                  ${(draft.proposedTimeSlots || []).map((slot, idx) => `
                    <span class="inline-flex items-center gap-1 px-2.5 py-1 rounded-full bg-[#EAF0E6] text-[#14532D] text-xs font-semibold">
                      ${ui.esc(slot)}
                      <button type="button" data-rm-slot="${idx}" class="text-[#DC2626] font-bold">×</button>
                    </span>
                  `).join('')}
                </div>
                <div class="flex gap-2">
                  <input id="d-new-slot" type="text" placeholder="VD: 20:00 - 21:30" class="flex-1 rounded-[8px] border-[#DDE5D9] text-xs" />
                  <button type="button" id="btn-add-slot" class="px-3 py-1.5 rounded-[8px] bg-[#EAF0E6] text-[#14532D] text-xs font-bold">+ Thêm</button>
                </div>
              </div>
            `}
          </div>

          <!-- Cost & Notes -->
          <div class="bg-white rounded-[8px] border border-[#DDE5D9] p-3.5 space-y-3">
            <div class="grid grid-cols-2 gap-2.5">
              <div>
                <label class="block text-xs font-bold text-[#2D342C] mb-1">Chi phí dự kiến (VNĐ)</label>
                <input id="d-cost" type="number" min="0" step="50000" value="${Number(draft.totalCost || 0)}" class="w-full rounded-[8px] border-[#DDE5D9] text-sm" />
              </div>
              <div>
                <label class="block text-xs font-bold text-[#2D342C] mb-1">Chia tiền</label>
                <select id="d-split" class="w-full rounded-[8px] border-[#DDE5D9] text-sm">
                  ${Object.keys(COST_SPLIT_LABELS).map((k) => `<option value="${k}" ${draft.costSharing === k ? 'selected' : ''}>${COST_SPLIT_LABELS[k]}</option>`).join('')}
                </select>
              </div>
            </div>
            ${draft.kind === 'B' ? `
              <div class="grid grid-cols-2 gap-2.5">
                <div>
                  <label class="block text-xs font-bold text-[#2D342C] mb-1">Tiền cọc (VNĐ)</label>
                  <input id="d-deposit" type="number" min="0" step="50000" value="${Number(draft.depositAmount || 0)}" class="w-full rounded-[8px] border-[#DDE5D9] text-sm" />
                </div>
                <div>
                  <label class="block text-xs font-bold text-[#2D342C] mb-1">Người giữ cọc</label>
                  <select id="d-dep-holder" class="w-full rounded-[8px] border-[#DDE5D9] text-sm">
                    <option value="HOST" ${draft.depositHolder === 'HOST' ? 'selected' : ''}>Chủ kèo giữ</option>
                    <option value="GUEST" ${draft.depositHolder === 'GUEST' ? 'selected' : ''}>Đội khách giữ</option>
                  </select>
                </div>
              </div>
            ` : ''}
            <div>
              <label class="block text-xs font-bold text-[#2D342C] mb-1">Ghi chú thêm</label>
              <textarea id="d-notes" rows="2" placeholder="Yêu cầu áo đấu, trọng tài, tinh thần..." class="w-full rounded-[8px] border-[#DDE5D9] text-sm">${ui.esc(draft.notes || '')}</textarea>
            </div>
          </div>

          <div id="d-error" class="hidden p-2.5 rounded-[8px] bg-[#FEF2F2] border border-[#FECACA] text-xs text-[#DC2626] font-medium"></div>

          <button type="button" id="btn-review-match"
                  class="w-full py-3 rounded-[8px] bg-[#3D6943] hover:bg-[#315C38] text-white text-sm font-bold">
            Xem lại trước khi đăng
          </button>
        </div>
      </div>
    `;

    const switchBtn = container.querySelector('#btn-switch-acting-team');
    if (switchBtn) switchBtn.onclick = () => openActingTeamSelector();

    function syncDraftFields() {
      draft.format = container.querySelector('#d-format').value;
      draft.skillLevel = container.querySelector('#d-skill').value;
      draft.matchDate = container.querySelector('#d-date').value;
      draft.totalCost = Number(container.querySelector('#d-cost').value || 0);
      draft.costSharing = container.querySelector('#d-split').value;
      draft.notes = container.querySelector('#d-notes').value;
      if (draft.kind === 'A') {
        draft.venueId = container.querySelector('#d-venue-a').value;
        draft.startTime = container.querySelector('#d-start').value;
        draft.endTime = container.querySelector('#d-end').value;
      } else {
        draft.depositAmount = Number((container.querySelector('#d-deposit') || {}).value || 0);
        draft.depositHolder = (container.querySelector('#d-dep-holder') || {}).value || 'HOST';
      }
    }

    container.querySelectorAll('[data-draft-kind]').forEach((b) => {
      b.onclick = () => {
        syncDraftFields();
        draft.kind = b.getAttribute('data-draft-kind');
        renderMatch05(container);
      };
    });

    const ven01Btn = container.querySelector('#btn-pick-ven-01');
    if (ven01Btn) {
      ven01Btn.onclick = () => {
        syncDraftFields();
        global._arenaVenuePickerCallback = (venueId) => {
          draft.venueId = venueId;
          global.ArenaRouter.navigate('MATCH-05');
        };
        if (global.ArenaRouter.routes.has('VEN-01')) {
          global.ArenaRouter.navigate('VEN-01', { picker: 'match' });
        } else {
          ui.toast('Đã mở bộ chọn sân nhanh tại form.', 'info');
        }
      };
    }

    container.querySelectorAll('[data-b-venue]').forEach((cb) => {
      cb.onchange = () => {
        const checked = Array.from(container.querySelectorAll('[data-b-venue]:checked')).map((el) => el.getAttribute('data-b-venue'));
        if (checked.length > 5) {
          cb.checked = false;
          ui.toast('Tối đa 5 sân đề xuất cho Kèo B.', 'warning');
          return;
        }
        draft.proposedVenues = checked;
      };
    });

    container.querySelectorAll('[data-rm-slot]').forEach((b) => {
      b.onclick = () => {
        const idx = Number(b.getAttribute('data-rm-slot'));
        draft.proposedTimeSlots.splice(idx, 1);
        syncDraftFields();
        renderMatch05(container);
      };
    });

    const addSlotBtn = container.querySelector('#btn-add-slot');
    if (addSlotBtn) {
      addSlotBtn.onclick = () => {
        const val = (container.querySelector('#d-new-slot').value || '').trim();
        if (!val) return;
        if (draft.proposedTimeSlots.length >= 5) {
          ui.toast('Tối đa 5 khung giờ đề xuất.', 'warning');
          return;
        }
        draft.proposedTimeSlots.push(val);
        syncDraftFields();
        renderMatch05(container);
      };
    }

    container.querySelector('#btn-review-match').onclick = () => {
      syncDraftFields();
      const errEl = container.querySelector('#d-error');
      const showErr = (msg) => {
        errEl.textContent = msg;
        errEl.classList.remove('hidden');
      };

      if (!draft.matchDate) return showErr('Vui lòng chọn ngày thi đấu.');
      if (draft.totalCost < 0) return showErr('Chi phí sân không được âm.');

      const clockMs = store.getMockClock().getTime();
      if (draft.kind === 'A') {
        const v = getVenue(draft.venueId);
        if (!v || v.status !== 'ACTIVE') return showErr('Vui lòng chọn sân đang hoạt động.');
        const startMs = new Date(`${draft.matchDate}T${draft.startTime}:00Z`).getTime();
        const endMs = new Date(`${draft.matchDate}T${draft.endTime}:00Z`).getTime();
        if (startMs - clockMs < 3600 * 1000) return showErr('Giờ bắt đầu phải cách thời điểm hiện tại ít nhất 1 giờ.');
        const durHours = (endMs - startMs) / (1000 * 3600);
        if (durHours <= 0 || durHours > 12) return showErr('Thời lượng trận đấu phải lớn hơn 0 và không quá 12 giờ.');
      } else {
        if (!draft.proposedVenues || draft.proposedVenues.length < 1 || draft.proposedVenues.length > 5) {
          return showErr('Kèo B cần chọn từ 1 đến 5 sân đề xuất.');
        }
        if (!draft.proposedTimeSlots || draft.proposedTimeSlots.length < 1 || draft.proposedTimeSlots.length > 5) {
          return showErr('Kèo B cần từ 1 đến 5 khung giờ đề xuất.');
        }
      }

      global.ArenaRouter.navigate('MATCH-06');
    };
  }

  /* =========================================================================
     MATCH-06: Xem lại trước khi đăng
     ========================================================================= */
  function renderMatch06(container) {
    const store = global.ArenaStore;
    const ui = global.ArenaUI;
    const draft = getDraft();
    const acting = store.getActingTeam();
    let submitting = false;

    container.innerHTML = `
      <div class="w-full flex-1 flex flex-col bg-[#F8FAF3] pb-8">
        <div class="p-4 space-y-3">
          <div class="bg-[#EAF0E6] border border-[#DDE5D9] rounded-[8px] p-3 text-xs text-[#14532D] font-medium">
            Kiểm tra kỹ thông tin trước khi đăng. Sau khi đăng, kèo sẽ hiển thị công khai trên mục Tìm kèo.
          </div>

          <div class="bg-white rounded-[8px] border border-[#DDE5D9] p-4 space-y-2.5 text-xs">
            <div class="flex items-center justify-between">
              <span class="text-[#596158]">Đội đăng kèo</span>
              <span class="font-bold text-[#111827]">${ui.esc(acting ? acting.name : 'FC Sao Mai')}</span>
            </div>
            <div class="flex items-center justify-between">
              <span class="text-[#596158]">Loại kèo</span>
              ${ui.badge(draft.kind === 'B' ? 'Kèo B • Thương lượng' : 'Kèo A • Sân cố định', draft.kind === 'B' ? 'info' : 'dark')}
            </div>
            <div class="flex items-center justify-between">
              <span class="text-[#596158]">Ngày đấu</span>
              <span class="font-bold">${formatDateVi(draft.matchDate)}</span>
            </div>
            ${draft.kind === 'A' ? `
              <div class="flex items-center justify-between">
                <span class="text-[#596158]">Sân bóng</span>
                <span class="font-bold">${ui.esc((getVenue(draft.venueId) || {}).name || draft.venueId)}</span>
              </div>
              <div class="flex items-center justify-between">
                <span class="text-[#596158]">Khung giờ</span>
                <span class="font-bold">${ui.esc(draft.startTime)} - ${ui.esc(draft.endTime)}</span>
              </div>
            ` : `
              <div>
                <div class="text-[#596158] mb-1">Sân đề xuất (${draft.proposedVenues.length})</div>
                <div class="font-semibold">${draft.proposedVenues.map((id) => ui.esc((getVenue(id) || {}).name || id)).join(', ')}</div>
              </div>
              <div>
                <div class="text-[#596158] mb-1">Khung giờ đề xuất (${draft.proposedTimeSlots.length})</div>
                <div class="font-semibold">${draft.proposedTimeSlots.map(ui.esc).join(' • ')}</div>
              </div>
            `}
            <div class="flex items-center justify-between pt-2 border-t border-[#E5E7EB]">
              <span class="text-[#596158]">Chi phí</span>
              <span class="font-bold text-[#14532D]">${formatMoney(draft.totalCost)} (${ui.esc(COST_SPLIT_LABELS[draft.costSharing])})</span>
            </div>
          </div>

          <div class="grid grid-cols-2 gap-2.5 pt-2">
            <button type="button" onclick="window.ArenaRouter.back()"
                    class="py-3 rounded-[8px] bg-white border border-[#DDE5D9] text-xs font-bold text-[#2D342C]">
              Chỉnh sửa lại
            </button>
            <button type="button" id="btn-publish-match"
                    class="py-3 rounded-[8px] bg-[#3D6943] hover:bg-[#315C38] text-white text-xs font-bold">
              Đăng kèo ngay
            </button>
          </div>
        </div>
      </div>
    `;

    container.querySelector('#btn-publish-match').onclick = () => {
      if (submitting) return;
      submitting = true;
      const newId = 'match-' + Date.now();
      const shares = computeShares(draft.totalCost, draft.costSharing);
      const hostTeamId = acting ? acting.id : 'team-alpha';
      store.setState((s) => {
        s.matches.unshift({
          id: newId,
          kind: draft.kind,
          hostTeamId,
          sportId: 'sport-football',
          format: draft.format,
          skillLevel: draft.skillLevel,
          venueId: draft.kind === 'A' ? draft.venueId : null,
          proposedVenues: draft.kind === 'B' ? draft.proposedVenues.slice() : [],
          proposedTimeSlots: draft.kind === 'B' ? draft.proposedTimeSlots.slice() : [],
          matchDate: draft.matchDate,
          startTime: draft.kind === 'A' ? draft.startTime : null,
          endTime: draft.kind === 'A' ? draft.endTime : null,
          expiresAt: `${draft.matchDate}T17:00:00Z`,
          status: 'OPEN',
          totalCost: draft.totalCost,
          costSharing: draft.costSharing,
          hostShare: shares.host,
          guestShare: shares.guest,
          depositAmount: draft.kind === 'B' ? draft.depositAmount : 0,
          depositHolder: draft.kind === 'B' ? draft.depositHolder : null,
          notes: draft.notes || '',
          confirmedGuestTeamId: null,
          createdAt: store.getMockClock().toISOString()
        });
      });
      resetDraft();
      ui.toast('Đăng kèo thành công!', 'success');
      global.ArenaRouter.navigate(draft.kind === 'B' ? 'MATCH-04' : 'MATCH-03', { id: newId }, { replace: true });
    };
  }

  /* =========================================================================
     MATCH-07: Lịch thi đấu đội
     ========================================================================= */
  function renderMatch07(container, params) {
    const store = global.ArenaStore;
    const ui = global.ArenaUI;
    const acting = store.getActingTeam() || (store.getMyTeams()[0]) || getTeam('team-alpha');
    let tab = params.tab || 'UPCOMING'; // UPCOMING | COMPLETED | CANCELLED

    function render() {
      const matches = (store.getState().matches || []).filter((m) => {
        const involved = acting && (m.hostTeamId === acting.id || m.guestTeamId === acting.id || m.confirmedGuestTeamId === acting.id);
        if (!involved) return false;
        const eff = getEffectiveStatus(m);
        if (tab === 'UPCOMING') return eff === 'OPEN' || eff === 'CONFIRMED' || eff === 'IN_PROGRESS';
        if (tab === 'COMPLETED') return eff === 'COMPLETED';
        return eff === 'CANCELLED' || eff === 'EXPIRED';
      });

      container.innerHTML = `
        <div class="w-full flex-1 flex flex-col bg-[#F8FAF3] p-4 space-y-3">
          ${actingTeamBarHtml()}

          <div class="grid grid-cols-3 gap-1.5 bg-white p-1 rounded-[8px] border border-[#DDE5D9]">
            ${[
              { k: 'UPCOMING', l: 'Sắp tới' },
              { k: 'COMPLETED', l: 'Kết thúc' },
              { k: 'CANCELLED', l: 'Hủy / Quá hạn' }
            ].map((t) => `
              <button type="button" data-cal-tab="${t.k}"
                      class="py-2 rounded-[6px] text-xs font-bold ${tab === t.k ? 'bg-[#3D6943] text-white' : 'text-[#596158]'}">
                ${t.l}
              </button>
            `).join('')}
          </div>

          <div class="space-y-3">
            ${matches.length === 0 ? ui.emptyState({
              icon: 'event_busy',
              title: 'Chưa có trận đấu nào',
              description: 'Không có trận đấu thuộc nhóm này cho đội hiện tại.',
              actionLabel: 'Tìm kèo ngay',
              onAction: "window.ArenaRouter.navigate('MATCH-01')"
            }) : matches.map(renderMatchCard).join('')}
          </div>
        </div>
      `;

      const sw = container.querySelector('#btn-switch-acting-team');
      if (sw) sw.onclick = () => openActingTeamSelector();
      container.querySelectorAll('[data-cal-tab]').forEach((b) => {
        b.onclick = () => { tab = b.getAttribute('data-cal-tab'); render(); };
      });
      container.querySelectorAll('[data-open-match]').forEach((c) => {
        c.onclick = () => global.ArenaRouter.navigate(c.getAttribute('data-kind') === 'B' ? 'MATCH-04' : 'MATCH-03', { id: c.getAttribute('data-open-match') });
      });
    }
    render();
  }

  /* =========================================================================
     MATCH-08: Yêu cầu bắt kèo đã gửi
     ========================================================================= */
  function renderMatch08(container) {
    const store = global.ArenaStore;
    const ui = global.ArenaUI;
    const acting = store.getActingTeam();
    let statusFilter = 'ALL';

    function render() {
      const reqs = (store.getState().matchRequests || []).filter((r) => {
        if (acting && r.requesterTeamId !== acting.id) return false;
        if (statusFilter !== 'ALL' && r.status !== statusFilter) return false;
        return true;
      });

      container.innerHTML = `
        <div class="w-full flex-1 flex flex-col bg-[#F8FAF3] p-4 space-y-3">
          ${actingTeamBarHtml()}
          <div class="flex gap-1.5 overflow-x-auto no-scrollbar">
            ${['ALL', 'PENDING', 'ACCEPTED', 'REJECTED', 'WITHDRAWN', 'SUPERSEDED'].map((st) => `
              <button type="button" data-req-f="${st}"
                      class="px-2.5 py-1 rounded-full text-[11px] font-semibold border whitespace-nowrap ${statusFilter === st ? 'bg-[#3D6943] text-white border-[#3D6943]' : 'bg-white text-[#596158] border-[#DDE5D9]'}">
                ${st === 'ALL' ? 'Tất cả' : (REQ_STATUS_META[st] ? REQ_STATUS_META[st].label : st)}
              </button>
            `).join('')}
          </div>

          <div class="space-y-2.5">
            ${reqs.length === 0 ? ui.emptyState({
              icon: 'outbox',
              title: 'Chưa có yêu cầu đã gửi',
              description: 'Các yêu cầu bắt kèo A đội bạn đã gửi sẽ hiển thị tại đây.',
              actionLabel: 'Tìm kèo A',
              onAction: "window.ArenaRouter.navigate('MATCH-01')"
            }) : reqs.map((r) => {
              const m = getMatch(r.matchId);
              const host = m ? getTeam(m.hostTeamId) : null;
              const meta = REQ_STATUS_META[r.status] || REQ_STATUS_META.PENDING;
              return `
                <div class="bg-white rounded-[8px] border border-[#DDE5D9] p-3.5 space-y-2">
                  <div class="flex items-center justify-between gap-2">
                    <div class="font-bold text-sm text-[#111827] truncate">${ui.esc(host ? host.name : 'Đội chủ nhà')}</div>
                    ${ui.badge(meta.label, meta.variant)}
                  </div>
                  <div class="text-xs text-[#596158]">
                    ${m ? `${formatDateVi(m.matchDate)} • ${ui.esc(m.startTime || '')} - ${ui.esc(m.endTime || '')}` : 'Kèo không còn tồn tại'}
                  </div>
                  ${r.message ? `<p class="text-xs text-[#2D342C] bg-[#F8FAF3] p-2 rounded-[6px]">"${ui.esc(r.message)}"</p>` : ''}
                  <div class="flex items-center gap-2 pt-1">
                    ${m ? `<button type="button" onclick="window.ArenaRouter.navigate('MATCH-03', { id: '${ui.esc(m.id)}' })" class="px-3 py-1.5 rounded-[8px] bg-[#EAF0E6] text-[#14532D] text-xs font-semibold">Xem kèo</button>` : ''}
                    ${(r.status === 'PENDING' || r.status === 'ACCEPTED') ? `
                      <button type="button" data-wd-req="${ui.esc(r.id)}" class="px-3 py-1.5 rounded-[8px] bg-[#FEF2F2] text-[#DC2626] text-xs font-semibold">Rút yêu cầu</button>
                    ` : ''}
                  </div>
                </div>
              `;
            }).join('')}
          </div>
        </div>
      `;

      const sw = container.querySelector('#btn-switch-acting-team');
      if (sw) sw.onclick = () => openActingTeamSelector();
      container.querySelectorAll('[data-req-f]').forEach((b) => {
        b.onclick = () => { statusFilter = b.getAttribute('data-req-f'); render(); };
      });
      container.querySelectorAll('[data-wd-req]').forEach((b) => {
        b.onclick = () => openWithdrawRequestSheet(b.getAttribute('data-wd-req'));
      });
    }
    render();
  }

  /* =========================================================================
     MATCH-09: Yêu cầu gửi đến
     ========================================================================= */
  function renderMatch09(container, params) {
    const store = global.ArenaStore;
    const ui = global.ArenaUI;
    const acting = store.getActingTeam();
    const myMatchIds = (store.getState().matches || [])
      .filter((m) => (!acting || m.hostTeamId === acting.id) && (!params.matchId || m.id === params.matchId))
      .map((m) => m.id);

    const incoming = (store.getState().matchRequests || []).filter((r) => myMatchIds.includes(r.matchId));

    container.innerHTML = `
      <div class="w-full flex-1 flex flex-col bg-[#F8FAF3] p-4 space-y-3">
        ${actingTeamBarHtml()}
        <div class="space-y-2.5">
          ${incoming.length === 0 ? ui.emptyState({
            icon: 'inbox',
            title: 'Chưa có yêu cầu gửi đến',
            description: 'Khi có đội khác xin bắt kèo A của bạn, yêu cầu sẽ xuất hiện tại đây.'
          }) : incoming.map((r) => {
            const reqTeam = getTeam(r.requesterTeamId);
            const m = getMatch(r.matchId);
            const meta = REQ_STATUS_META[r.status] || REQ_STATUS_META.PENDING;
            return `
              <div class="bg-white rounded-[8px] border border-[#DDE5D9] p-3.5 space-y-2.5">
                <div class="flex items-center justify-between gap-2">
                  <div class="flex items-center gap-2.5 min-w-0">
                    ${ui.avatar(reqTeam || { name: 'Đội' }, { size: 38 })}
                    <div class="min-w-0">
                      <div class="font-bold text-sm text-[#111827] truncate">${ui.esc(reqTeam ? reqTeam.name : r.requesterTeamId)}</div>
                      <div class="text-[11px] text-[#596158]">Uy tín ${reqTeam ? reqTeam.reputationScore : 90} • ${m ? formatDateVi(m.matchDate) : ''}</div>
                    </div>
                  </div>
                  ${ui.badge(meta.label, meta.variant)}
                </div>
                ${r.message ? `<p class="text-xs text-[#2D342C] bg-[#F8FAF3] p-2 rounded-[6px]">"${ui.esc(r.message)}"</p>` : ''}
                <div class="flex items-center gap-2">
                  <button type="button" onclick="window.ArenaRouter.navigate('MATCH-10', { id: '${ui.esc(r.id)}' })"
                          class="flex-1 py-2 rounded-[8px] bg-[#3D6943] text-white text-xs font-bold">
                    ${r.status === 'PENDING' ? 'Xem & Duyệt' : 'Chi tiết'}
                  </button>
                  <button type="button" onclick="window.ArenaRouter.navigate('CHAT-02', { contextId: '${ui.esc(r.id)}' })"
                          class="px-3 py-2 rounded-[8px] bg-[#EAF0E6] text-[#14532D] text-xs font-bold">
                    Nhắn tin
                  </button>
                </div>
              </div>
            `;
          }).join('')}
        </div>
      </div>
    `;
    const sw = container.querySelector('#btn-switch-acting-team');
    if (sw) sw.onclick = () => openActingTeamSelector();
  }

  /* =========================================================================
     MATCH-10: Chi tiết & duyệt yêu cầu (AC-P03)
     ========================================================================= */
  function renderMatch10(container, params) {
    const store = global.ArenaStore;
    const ui = global.ArenaUI;
    const req = store.findById('matchRequests', params.id) || (store.getState().matchRequests || [])[0];
    if (!req) {
      container.innerHTML = `<div class="p-4">${ui.errorState({ title: 'Không tìm thấy yêu cầu', code: '404' })}</div>`;
      return;
    }
    const reqTeam = getTeam(req.requesterTeamId);
    const match = getMatch(req.matchId);
    const meta = REQ_STATUS_META[req.status] || REQ_STATUS_META.PENDING;
    const otherPending = (store.getState().matchRequests || []).filter((r) => r.matchId === req.matchId && r.id !== req.id && r.status === 'PENDING').length;

    container.innerHTML = `
      <div class="w-full flex-1 flex flex-col bg-[#F8FAF3] p-4 space-y-3">
        <div class="bg-white rounded-[8px] border border-[#DDE5D9] p-4 space-y-3">
          <div class="flex items-center justify-between">
            <span class="text-xs font-bold text-[#596158]">YÊU CẦU BẮT KÈO</span>
            ${ui.badge(meta.label, meta.variant)}
          </div>
          <div class="flex items-center gap-3">
            ${ui.avatar(reqTeam || { name: 'Đội' }, { size: 46 })}
            <div>
              <div class="font-bold text-base text-[#111827]">${ui.esc(reqTeam ? reqTeam.name : req.requesterTeamId)}</div>
              <div class="text-xs text-[#596158]">Uy tín ${reqTeam ? reqTeam.reputationScore : 90} • ${reqTeam ? reqTeam.matchCount : 10} trận</div>
            </div>
          </div>
          <div class="bg-[#F8FAF3] p-3 rounded-[8px] border border-[#EAF0E6] text-xs text-[#2D342C]">
            "${ui.esc(req.message || 'Xin phép tham gia giao lưu!')}"
          </div>
          ${match ? `
            <div class="text-xs text-[#596158] pt-2 border-t border-[#E5E7EB]">
              Kèo ngày <strong>${formatDateVi(match.matchDate)}</strong> • ${ui.esc(match.startTime || '')} - ${ui.esc(match.endTime || '')}
            </div>
          ` : ''}
        </div>

        ${req.status === 'PENDING' ? `
          ${otherPending > 0 ? `
            <div class="bg-[#FEF3C7] border border-[#FDE68A] rounded-[8px] p-3 text-xs text-[#92400E]">
              <strong>Lưu ý (AC-P03):</strong> Chấp nhận yêu cầu này sẽ chốt kèo ngay và tự động chuyển ${otherPending} yêu cầu khác sang trạng thái <strong>SUPERSEDED</strong>.
            </div>
          ` : ''}
          <div class="grid grid-cols-2 gap-2.5">
            <button type="button" id="btn-reject-req" class="py-3 rounded-[8px] bg-white border border-[#FECACA] text-[#DC2626] text-xs font-bold">
              Từ chối
            </button>
            <button type="button" id="btn-accept-req" class="py-3 rounded-[8px] bg-[#3D6943] hover:bg-[#315C38] text-white text-xs font-bold">
              Chấp nhận chốt kèo
            </button>
          </div>
        ` : ''}
      </div>
    `;

    const accBtn = container.querySelector('#btn-accept-req');
    if (accBtn) {
      accBtn.onclick = () => {
        if (!match || getEffectiveStatus(match) !== 'OPEN') {
          ui.toast('409 Conflict: Kèo không còn ở trạng thái OPEN.', 'error');
          return;
        }
        let supersededCount = 0;
        store.setState((s) => {
          const targetReq = s.matchRequests.find((r) => r.id === req.id);
          const targetMatch = s.matches.find((m) => m.id === req.matchId);
          if (!targetReq || !targetMatch) return;
          targetReq.status = 'ACCEPTED';
          targetMatch.status = 'CONFIRMED';
          targetMatch.guestTeamId = targetReq.requesterTeamId;
          targetMatch.confirmedGuestTeamId = targetReq.requesterTeamId;
          s.matchRequests.forEach((other) => {
            if (other.matchId === req.matchId && other.id !== req.id && other.status === 'PENDING') {
              other.status = 'SUPERSEDED';
              supersededCount++;
            }
          });
        });
        ui.toast(`Đã chốt kèo! ${supersededCount > 0 ? `${supersededCount} yêu cầu khác đã chuyển SUPERSEDED.` : ''}`, 'success');
        global.ArenaRouter.navigate('MATCH-03', { id: req.matchId }, { replace: true });
      };
    }

    const rejBtn = container.querySelector('#btn-reject-req');
    if (rejBtn) {
      rejBtn.onclick = () => {
        store.setState((s) => {
          const targetReq = s.matchRequests.find((r) => r.id === req.id);
          if (targetReq) targetReq.status = 'REJECTED';
        });
        ui.toast('Đã từ chối yêu cầu.', 'info');
        global.ArenaRouter.back();
      };
    }
  }

  /* =========================================================================
     MATCH-13: Vòng đời kèo (Variants Explorer)
     ========================================================================= */
  function renderMatch13(container, params) {
    const ui = global.ArenaUI;
    const matchId = params.id || 'match-a-open';
    const variants = [
      { s: 'OPEN', desc: 'Kèo đang mở, nhận yêu cầu bắt kèo hoặc thương lượng.' },
      { s: 'CONFIRMED', desc: 'Đã chốt đối thủ; mở chat 2 đội & tuyển đá lẻ; hủy có phạt uy tín.' },
      { s: 'IN_PROGRESS', desc: 'Đang trong khung giờ thi đấu; khóa hủy kèo.' },
      { s: 'COMPLETED', desc: 'Trận đấu đã kết thúc; mở đánh giá đối thủ (REP-01) & báo cáo.' },
      { s: 'CANCELLED', desc: 'Kèo đã bị hủy bởi một trong hai đội.' },
      { s: 'EXPIRED', desc: 'Quá hạn expiresAt mà chưa chốt được đối thủ.' }
    ];

    container.innerHTML = `
      <div class="w-full flex-1 flex flex-col bg-[#F8FAF3] p-4 space-y-3">
        <div class="bg-white rounded-[8px] border border-[#DDE5D9] p-3.5">
          <h2 class="font-bold text-sm text-[#111827]">Các trạng thái vòng đời kèo (MATCH-13)</h2>
          <p class="text-xs text-[#596158] mt-1">Bấm vào từng trạng thái để xem giao diện Chi tiết kèo A tương ứng.</p>
        </div>
        <div class="space-y-2">
          ${variants.map((v) => {
            const meta = MATCH_STATUS_META[v.s];
            return `
              <button type="button" onclick="window.ArenaRouter.navigate('MATCH-03', { id: '${ui.esc(matchId)}', _previewStatus: '${v.s}' })"
                      class="w-full bg-white rounded-[8px] border border-[#DDE5D9] p-3 text-left flex items-center justify-between gap-2 hover:border-[#3D6943]">
                <div>
                  <div class="mb-1">${ui.badge(meta.label, meta.variant)} <span class="text-[11px] font-mono text-[#596158]">${v.s}</span></div>
                  <div class="text-xs text-[#2D342C]">${ui.esc(v.desc)}</div>
                </div>
                <span class="material-symbols-outlined text-[#596158]">chevron_right</span>
              </button>
            `;
          }).join('')}
        </div>
      </div>
    `;
  }

  /* =========================================================================
     Route Registration
     ========================================================================= */
  const R = global.ArenaRouter;
  if (R) {
    R.register('MATCH-01', renderMatch01, { title: 'Tìm kèo', tab: 'MATCH-01', group: 'Kèo & Yêu cầu', aliases: ['matches', 'HOME'] });
    R.register('MATCH-02', (c) => { renderMatch01(c); setTimeout(openFilterSheet, 50); }, { title: 'Bộ lọc kèo', tab: 'MATCH-01', group: 'Kèo & Yêu cầu' });
    R.register('MATCH-03', renderMatch03, { title: 'Chi tiết kèo A', tab: 'MATCH-01', showBack: true, group: 'Kèo & Yêu cầu', aliases: ['match-detail'] });
    R.register('MATCH-04', renderMatch04, { title: 'Chi tiết kèo B', tab: 'MATCH-01', showBack: true, group: 'Kèo & Yêu cầu' });
    R.register('MATCH-05', renderMatch05, { title: 'Tạo kèo mới', tab: 'MATCH-01', showBack: true, group: 'Kèo & Yêu cầu', aliases: ['match-create'] });
    R.register('MATCH-06', renderMatch06, { title: 'Xem lại kèo', tab: 'MATCH-01', showBack: true, group: 'Kèo & Yêu cầu' });
    R.register('MATCH-07', renderMatch07, { title: 'Lịch thi đấu đội', tab: 'MATCH-01', showBack: true, group: 'Kèo & Yêu cầu' });
    R.register('MATCH-08', renderMatch08, { title: 'Yêu cầu đã gửi', tab: 'MATCH-01', showBack: true, group: 'Kèo & Yêu cầu' });
    R.register('MATCH-09', renderMatch09, { title: 'Yêu cầu gửi đến', tab: 'MATCH-01', showBack: true, group: 'Kèo & Yêu cầu' });
    R.register('MATCH-10', renderMatch10, { title: 'Duyệt yêu cầu', tab: 'MATCH-01', showBack: true, group: 'Kèo & Yêu cầu' });
    R.register('MATCH-11', (c) => {
      renderMatch08(c);
      const r = (global.ArenaStore.getState().matchRequests || [])[0];
      if (r) setTimeout(() => openWithdrawRequestSheet(r.id), 50);
    }, { title: 'Rút yêu cầu', tab: 'MATCH-01', showBack: true, group: 'Kèo & Yêu cầu' });
    R.register('MATCH-12', (c) => {
      renderMatch03(c, { id: 'match-a-open' });
      setTimeout(() => openCancelMatchSheet('match-a-open'), 50);
    }, { title: 'Hủy kèo', tab: 'MATCH-01', showBack: true, group: 'Kèo & Yêu cầu' });
    R.register('MATCH-13', renderMatch13, { title: 'Vòng đời kèo', tab: 'MATCH-01', showBack: true, group: 'Kèo & Yêu cầu' });
    R.register('MATCH-14', (c) => {
      renderMatch03(c, { id: 'match-a-open' });
      setTimeout(() => openEditCostSheet('match-a-open'), 50);
    }, { title: 'Sửa chi phí kèo A', tab: 'MATCH-01', showBack: true, group: 'Kèo & Yêu cầu' });
  }

  global.ArenaMatch = {
    resetFilters: function () {
      _filters.date = 'ALL';
      _filters.kind = 'ALL';
      _filters.format = 'ALL';
      _filters.skill = 'ALL';
      _filters.maxDistance = 'ALL';
      _filters.wardId = 'ALL';
      global.ArenaRouter.refresh();
    },
    openFilterSheet,
    openCancelMatchSheet,
    openWithdrawRequestSheet,
    openEditCostSheet
  };
})(window);
