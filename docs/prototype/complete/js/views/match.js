/**
 * Match Views & Sheets (MATCH-01..14)
 * Owned by @ui-match-pair
 *
 * Implements:
 * - MATCH-01: Match list A/B with status tags & filter chips
 * - MATCH-02: Filters sheet (Kind, format, skill, distance)
 * - MATCH-03: Match A detail by viewer role (Host, Guest, Candidate, Member)
 * - MATCH-04: Match B detail (1-5 venues, 1-5 timeslots, candidate negotiations)
 * - MATCH-05: Create Match A or B (validation: start >= +1h, duration <= 12h, expiresAt)
 * - MATCH-06: Review before posting sheet (double-click safe)
 * - MATCH-07: Team match calendar / history
 * - MATCH-08: Sent match requests tracker
 * - MATCH-09: Received match requests for creator
 * - MATCH-10: Request detail / accept / reject dialog (AC-P03)
 * - MATCH-11: Withdraw request sheet (pending free, accepted cancels match with rep penalty)
 * - MATCH-12: Cancel match sheet (OPEN requires rejecting pending, CONFIRMED rep penalty)
 * - MATCH-13: States (CONFIRMED, in-progress, ARCHIVED, COMPLETED, CANCELLED, EXPIRED)
 * - MATCH-14: Cost agreement sheet for Match A
 */

// Global form draft to support return-to-form when selecting/adding venue (J-16)
let _matchCreateDraft = typeof window !== 'undefined' ? (window._matchCreateDraft || null) : null;
if (typeof window !== 'undefined') {
  window._matchCreateDraft = _matchCreateDraft;
}

export function initMatchViews(router, store) {
  // Helpers
  const formatCost = (num) => {
    if (!num && num !== 0) return '0 đ';
    return Number(num).toLocaleString('vi-VN') + ' đ';
  };

  const formatFormatLabel = (fmt) => {
    switch (fmt) {
      case 'FIVE_A_SIDE': return 'Sân 5';
      case 'SEVEN_A_SIDE': return 'Sân 7';
      case 'ELEVEN_A_SIDE': return 'Sân 11';
      default: return 'Sân 7';
    }
  };

  const formatCostSplit = (split) => {
    switch (split) {
      case 'SPLIT_50_50': return 'Chia đôi 50/50';
      case 'HOST_100': return 'Đội chủ nhà bao sân';
      case 'SEVENTY_THIRTY_HOME': return '70/30 (Chủ nhà 70%)';
      case 'SEVENTY_THIRTY_LOSER': return '70/30 (Đội thua 70%)';
      case 'LOSER_PAYS_DRINKS': return 'Đội thua trả nước';
      default: return 'Chia đôi 50/50';
    }
  };

  const getTeam = (teamId) => {
    const teams = store.getState().teams || [];
    return teams.find(t => t.id === teamId) || null;
  };

  const getVenue = (venueId) => {
    const venues = store.getState().venues || [];
    return venues.find(v => v.id === venueId) || null;
  };

  // Calculate penalty delta per Section 1.5 of MATCH-001
  const calculatePenalty = (matchDateStr, startTimeStr, nowIso) => {
    if (!matchDateStr) return { penalty: 0, text: 'Không trừ điểm' };
    const matchStart = new Date(`${matchDateStr}T${startTimeStr || '19:00'}:00Z`).getTime();
    const now = new Date(nowIso).getTime();
    const deltaHours = (matchStart - now) / (1000 * 3600);

    if (deltaHours <= 0) {
      return { penalty: -999, text: 'Trận đấu đã diễn ra (Không thể hủy/rút)', blocked: true };
    }
    if (deltaHours >= 24) {
      return { penalty: -2, text: '-2 điểm uy tín (Hủy trước trên 24 giờ)', blocked: false };
    }
    if (deltaHours >= 4) {
      return { penalty: -5, text: '-5 điểm uy tín (Hủy trong vòng 4 - 24 giờ)', blocked: false };
    }
    return { penalty: -10, text: '-10 điểm uy tín (Hủy sát giờ dưới 4 giờ)', blocked: false };
  };

  // Determine effective match state based on clock
  const getEffectiveMatchStatus = (match, nowClock) => {
    if (match.status === 'CANCELLED' || match.status === 'COMPLETED') return match.status;

    const now = new Date(nowClock).getTime();

    // Check expiration for OPEN matches
    if (match.status === 'OPEN' && match.expiresAt) {
      if (now > new Date(match.expiresAt).getTime()) {
        return 'EXPIRED';
      }
    }

    // Check in-progress / completed / archived for CONFIRMED
    if (match.status === 'CONFIRMED' && match.matchDate) {
      const matchStart = new Date(`${match.matchDate}T${match.startTime || '19:00'}:00Z`).getTime();
      const matchEnd = new Date(`${match.matchDate}T${match.endTime || '20:30'}:00Z`).getTime();

      if (now > matchEnd) {
        return 'COMPLETED'; // or ARCHIVED
      }
      if (now >= matchStart && now <= matchEnd) {
        return 'IN_PROGRESS';
      }
    }

    return match.status;
  };

  /* =========================================================================
     MATCH-01: Main Match Exploration View ('matches')
     ========================================================================= */
  router.registerView('matches', (container, params = {}) => {
    router.setHeaderTitle('Tìm kèo giao lưu');

    let activeTab = params.tab || 'ALL'; // 'ALL', 'KIND_A', 'KIND_B', 'MY_TEAM'
    let filterFormat = params.format || 'ALL';
    let filterSkill = params.skill || 'ALL';

    function renderMatches() {
      const state = store.getState();
      const currentUserId = state.currentUserId;
      const actingTeamId = state.actingTeamId;
      const nowClock = state.meta.mockClock;
      const matches = state.matches || [];

      // Filter matches
      const filtered = matches.filter(m => {
        const effStatus = getEffectiveMatchStatus(m, nowClock);

        // Tab filter
        if (activeTab === 'KIND_A' && m.kind !== 'A') return false;
        if (activeTab === 'KIND_B' && m.kind !== 'B') return false;
        if (activeTab === 'MY_TEAM') {
          const isMyTeam = m.hostTeamId === actingTeamId || m.guestTeamId === actingTeamId || m.confirmedGuestTeamId === actingTeamId;
          if (!isMyTeam) return false;
        }

        // Format filter
        if (filterFormat !== 'ALL' && m.format !== filterFormat) return false;

        // Skill filter
        if (filterSkill !== 'ALL' && m.skillLevel !== filterSkill) return false;

        return true;
      });

      container.innerHTML = `
        <div style="padding: 14px 0 24px 0;">
          <!-- Top Action Bar: Search summary, Filter & Post Match Button -->
          <div style="display: flex; gap: 8px; align-items: center; justify-content: space-between; margin-bottom: 12px;">
            <div style="display: flex; gap: 6px; align-items: center;">
              <button class="btn btn-secondary" id="match-open-filter-btn" style="min-height: 40px; padding: 6px 14px;">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <polygon points="22 3 2 3 10 12.46 10 19 14 21 14 12.46 22 3"></polygon>
                </svg>
                <span>Bộ lọc</span>
              </button>

              <button class="btn btn-ghost" id="match-venues-nav-btn" style="min-height: 40px; padding: 6px 12px; font-size: 13px;">
                📍 Địa điểm sân
              </button>
            </div>

            <button class="btn btn-primary" id="match-create-btn" style="min-height: 40px; padding: 6px 14px;">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <line x1="12" y1="5" x2="12" y2="19"></line>
                <line x1="5" y1="12" x2="19" y2="12"></line>
              </svg>
              <span>Đăng kèo</span>
            </button>
          </div>

          <!-- Filter Tabs (MATCH-01) -->
          <div class="chip-group" style="margin-bottom: 14px;">
            <button class="chip ${activeTab === 'ALL' ? 'is-active' : ''}" data-tab="ALL">Tất cả kèo</button>
            <button class="chip ${activeTab === 'KIND_A' ? 'is-active' : ''}" data-tab="KIND_A">Đã có sân (Luồng A)</button>
            <button class="chip ${activeTab === 'KIND_B' ? 'is-active' : ''}" data-tab="KIND_B">Chưa có sân (Luồng B)</button>
            <button class="chip ${activeTab === 'MY_TEAM' ? 'is-active' : ''}" data-tab="MY_TEAM">Kèo của đội tôi</button>
          </div>

          <!-- Match List -->
          ${filtered.length === 0 ? `
            <div class="empty-state">
              <div class="empty-state-icon">
                <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
                  <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"></polygon>
                </svg>
              </div>
              <h3 class="empty-state-title">Chưa có kèo nào phù hợp</h3>
              <p class="empty-state-desc">Hãy thay đổi bộ lọc hoặc chủ động đăng bài tìm đối giao lưu!</p>
              <button class="btn btn-primary" id="match-empty-create-btn" style="margin-top: 12px;">
                Đăng kèo giao lưu ngay
              </button>
            </div>
          ` : `
            <div class="list-group">
              ${filtered.map(m => {
                const effStatus = getEffectiveMatchStatus(m, nowClock);
                const hostTeam = getTeam(m.hostTeamId);
                const guestTeam = m.guestTeamId ? getTeam(m.guestTeamId) : (m.confirmedGuestTeamId ? getTeam(m.confirmedGuestTeamId) : null);
                const venue = m.venueId ? getVenue(m.venueId) : null;
                const isKindB = m.kind === 'B';

                // Status badge styling
                let statusBadge = '<span class="badge badge-green">ĐANG TÌM ĐỐI</span>';
                if (effStatus === 'CONFIRMED') statusBadge = '<span class="badge badge-blue">ĐÃ GHÉP KÈO</span>';
                else if (effStatus === 'IN_PROGRESS') statusBadge = '<span class="badge badge-amber">ĐANG DIỄN RA</span>';
                else if (effStatus === 'COMPLETED') statusBadge = '<span class="badge badge-gray">KẾT THÚC</span>';
                else if (effStatus === 'CANCELLED') statusBadge = '<span class="badge badge-red">ĐÃ HỦY</span>';
                else if (effStatus === 'EXPIRED') statusBadge = '<span class="badge badge-amber">HẾT HẠN</span>';

                return `
                  <div class="card card-interactive match-item-card" data-id="${m.id}">
                    <!-- Card Top: Sport & Status -->
                    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px;">
                      <div style="display: flex; gap: 6px; align-items: center;">
                        <span class="badge badge-gray" style="font-size: 11px;">⚽ Bóng đá</span>
                        <span class="badge ${isKindB ? 'badge-amber' : 'badge-green'}" style="font-size: 11px;">
                          ${isKindB ? 'Luồng B (Nhiều lựa chọn sân/giờ)' : 'Luồng A (Đã có sân)'}
                        </span>
                      </div>
                      ${statusBadge}
                    </div>

                    <!-- Teams matchup header -->
                    <div style="margin-bottom: 10px;">
                      <div style="font-size: var(--font-size-lg); font-weight: var(--font-weight-bold); color: var(--color-on-surface); line-height: 1.3;">
                        ${hostTeam ? hostTeam.name : 'Đội chủ nhà'}
                        ${guestTeam ? `<span style="color: var(--color-primary); font-weight: 500;"> vs </span>${guestTeam.name}` : '<span style="color: var(--color-outline); font-weight: 400;"> vs (Chờ đối thủ)</span>'}
                      </div>
                      <div style="display: flex; align-items: center; gap: 8px; margin-top: 3px; font-size: 12px; color: var(--color-on-surface-variant);">
                        <span>Điểm uy tín: <strong>${hostTeam?.reputationScore || 100}</strong></span>
                        <span>•</span>
                        <span>Định dạng: <strong>${formatFormatLabel(m.format)}</strong></span>
                      </div>
                    </div>

                    <!-- Date & Time & Venue info -->
                    <div style="background-color: var(--color-surface-container-low); padding: 8px 12px; border-radius: var(--radius-base); margin-bottom: 10px; font-size: 13px;">
                      <div style="display: flex; align-items: center; gap: 6px; margin-bottom: 4px; font-weight: 600; color: var(--color-on-surface);">
                        <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                          <rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect>
                          <line x1="16" y1="2" x2="16" y2="6"></line>
                          <line x1="8" y1="2" x2="8" y2="6"></line>
                          <line x1="3" y1="10" x2="21" y2="10"></line>
                        </svg>
                        <span>
                          ${m.matchDate || 'Ngày thỏa thuận'} (${m.startTime || '19:00'} - ${m.endTime || '20:30'})
                        </span>
                      </div>

                      <div style="display: flex; align-items: center; gap: 6px; color: var(--color-on-surface-variant); font-size: 12px;">
                        <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="flex-shrink: 0;">
                          <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"></path>
                          <circle cx="12" cy="10" r="3"></circle>
                        </svg>
                        ${isKindB && !m.venueId ? `
                          <span style="color: var(--color-warning);">
                            Đề xuất ${(m.proposedVenues || []).length} sân & ${(m.proposedTimeSlots || []).length} khung giờ
                          </span>
                        ` : `
                          <span class="text-truncate">${venue ? `${venue.name} (${venue.address})` : 'Chưa chốt sân'}</span>
                        `}
                      </div>
                    </div>

                    <!-- Cost & Skill Footer -->
                    <div style="display: flex; justify-content: space-between; align-items: center; font-size: 12px;">
                      <div style="font-weight: 600; color: var(--color-primary-dark);">
                        Tổng chi phí: ${formatCost(m.totalCost)} (${formatCostSplit(m.costSharing)})
                      </div>
                      <span class="badge badge-gray">${m.skillLevel || 'Mọi trình độ'}</span>
                    </div>
                  </div>
                `;
              }).join('')}
            </div>
          `}
        </div>
      `;

      // Event Listeners
      const createBtn = container.querySelector('#match-create-btn');
      if (createBtn) createBtn.addEventListener('click', () => router.navigate('match-create'));

      const emptyCreateBtn = container.querySelector('#match-empty-create-btn');
      if (emptyCreateBtn) emptyCreateBtn.addEventListener('click', () => router.navigate('match-create'));

      const filterBtn = container.querySelector('#match-open-filter-btn');
      if (filterBtn) {
        filterBtn.addEventListener('click', () => {
          router.openSheet('match-filter-sheet', {
            format: filterFormat,
            skill: filterSkill,
            onApply: (newFilters) => {
              filterFormat = newFilters.format;
              filterSkill = newFilters.skill;
              renderMatches();
            }
          });
        });
      }

      const venuesNavBtn = container.querySelector('#match-venues-nav-btn');
      if (venuesNavBtn) {
        venuesNavBtn.addEventListener('click', () => router.navigate('venues'));
      }

      const tabChips = container.querySelectorAll('.chip[data-tab]');
      tabChips.forEach(chip => {
        chip.addEventListener('click', () => {
          activeTab = chip.getAttribute('data-tab');
          renderMatches();
        });
      });

      const cards = container.querySelectorAll('.match-item-card');
      cards.forEach(card => {
        card.addEventListener('click', () => {
          const mId = card.getAttribute('data-id');
          router.navigate('match-detail', { id: mId });
        });
      });
    }

    renderMatches();
  });

  /* =========================================================================
     MATCH-02: Match Filters Sheet ('match-filter-sheet')
     ========================================================================= */
  router.registerSheet('match-filter-sheet', (body, footer, params = {}, titleElem) => {
    if (titleElem) titleElem.textContent = 'Bộ lọc tìm kèo';

    let curFormat = params.format || 'ALL';
    let curSkill = params.skill || 'ALL';

    body.innerHTML = `
      <div style="padding-bottom: 8px;">
        <div class="form-group">
          <label class="form-label">Quy mô sân / Định dạng</label>
          <select class="form-select" id="filter-format">
            <option value="ALL" ${curFormat === 'ALL' ? 'selected' : ''}>Tất cả các sân</option>
            <option value="SEVEN_A_SIDE" ${curFormat === 'SEVEN_A_SIDE' ? 'selected' : ''}>Sân 7 người</option>
            <option value="FIVE_A_SIDE" ${curFormat === 'FIVE_A_SIDE' ? 'selected' : ''}>Sân 5 người</option>
            <option value="ELEVEN_A_SIDE" ${curFormat === 'ELEVEN_A_SIDE' ? 'selected' : ''}>Sân 11 người</option>
          </select>
        </div>

        <div class="form-group">
          <label class="form-label">Trình độ thi đấu</label>
          <select class="form-select" id="filter-skill">
            <option value="ALL" ${curSkill === 'ALL' ? 'selected' : ''}>Tất cả trình độ</option>
            <option value="BEGINNER" ${curSkill === 'BEGINNER' ? 'selected' : ''}>Mới chơi (Beginner)</option>
            <option value="INTERMEDIATE" ${curSkill === 'INTERMEDIATE' ? 'selected' : ''}>Trung bình (Intermediate)</option>
            <option value="ADVANCED" ${curSkill === 'ADVANCED' ? 'selected' : ''}>Khá / Chuyên (Advanced)</option>
          </select>
        </div>

        <div class="form-group" style="margin-bottom: 0;">
          <label class="form-label">Khoảng cách định vị (GPS)</label>
          <select class="form-select" id="filter-radius">
            <option value="5">Bán kính 5 km</option>
            <option value="10" selected>Bán kính 10 km (Mặc định)</option>
            <option value="20">Bán kính 20 km</option>
            <option value="ALL">Toàn thành phố</option>
          </select>
        </div>
      </div>
    `;

    footer.innerHTML = `
      <div style="display: flex; gap: 8px; width: 100%;">
        <button class="btn btn-secondary" style="flex: 1;" id="filter-reset-btn">Đặt lại</button>
        <button class="btn btn-primary" style="flex: 2;" id="filter-apply-btn">Áp dụng</button>
      </div>
    `;

    const resetBtn = footer.querySelector('#filter-reset-btn');
    if (resetBtn) {
      resetBtn.addEventListener('click', () => {
        if (typeof params.onApply === 'function') {
          params.onApply({ format: 'ALL', skill: 'ALL' });
        }
        router.closeSheet();
      });
    }

    const applyBtn = footer.querySelector('#filter-apply-btn');
    if (applyBtn) {
      applyBtn.addEventListener('click', () => {
        const fmt = body.querySelector('#filter-format')?.value || 'ALL';
        const skl = body.querySelector('#filter-skill')?.value || 'ALL';
        if (typeof params.onApply === 'function') {
          params.onApply({ format: fmt, skill: skl });
        }
        router.closeSheet();
      });
    }
  });

  /* =========================================================================
     MATCH-03 & MATCH-04: Match Detail View ('match-detail')
     ========================================================================= */
  router.registerView('match-detail', (container, params = {}) => {
    const matchId = params.id;
    const state = store.getState();
    const currentUserId = state.currentUserId;
    const actingTeamId = state.actingTeamId;
    const nowClock = state.meta.mockClock;
    const match = (state.matches || []).find(m => m.id === matchId);

    if (!match) {
      router.setHeaderTitle('Chi tiết kèo');
      container.innerHTML = `
        <div class="empty-state">
          <h3 class="empty-state-title">Không tìm thấy kèo</h3>
          <p class="empty-state-desc">Kèo đấu này không tồn tại hoặc đã bị xóa.</p>
          <button class="btn btn-primary" onclick="window.AppRouter.navigate('matches')">Quay lại danh sách</button>
        </div>
      `;
      return;
    }

    const isKindB = match.kind === 'B';
    const effStatus = getEffectiveMatchStatus(match, nowClock);
    const hostTeam = getTeam(match.hostTeamId);
    const guestTeam = match.guestTeamId ? getTeam(match.guestTeamId) : (match.confirmedGuestTeamId ? getTeam(match.confirmedGuestTeamId) : null);
    const venue = match.venueId ? getVenue(match.venueId) : null;

    router.setHeaderTitle(isKindB ? 'Kèo Luồng B (Chưa có sân)' : 'Chi tiết kèo thi đấu');

    // Roles determination
    const isHostCaptain = hostTeam && hostTeam.captainId === currentUserId;
    const isGuestCaptain = guestTeam && guestTeam.captainId === currentUserId;
    const isParticipant = isHostCaptain || isGuestCaptain;

    // Requests count for host
    const receivedRequests = (state.matchRequests || []).filter(r => r.matchId === match.id);
    const pendingRequests = receivedRequests.filter(r => r.status === 'PENDING');

    // My team's request if candidate
    const myRequest = (state.matchRequests || []).find(r => r.matchId === match.id && r.requesterTeamId === actingTeamId);

    // Pairings for Kind B
    const matchPairings = (state.pairings || []).filter(p => p.matchId === match.id);
    const myPairing = matchPairings.find(p => p.guestTeamId === actingTeamId);

    container.innerHTML = `
      <div style="padding: 14px 0 28px 0;">
        <!-- Status Banner -->
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px;">
          <div style="display: flex; gap: 6px; align-items: center;">
            <span class="badge ${isKindB ? 'badge-amber' : 'badge-green'}">
              ${isKindB ? 'Luồng B (Nhiều lựa chọn)' : 'Luồng A (Đã có sân)'}
            </span>
            <span class="badge badge-gray">${formatFormatLabel(match.format)}</span>
          </div>

          <div>
            ${effStatus === 'OPEN' ? '<span class="badge badge-green">ĐANG MỞ</span>' :
              effStatus === 'CONFIRMED' ? '<span class="badge badge-blue">ĐÃ GHÉP KÈO</span>' :
              effStatus === 'IN_PROGRESS' ? '<span class="badge badge-amber">ĐANG DIỄN RA</span>' :
              effStatus === 'COMPLETED' ? '<span class="badge badge-gray">KẾT THÚC</span>' :
              effStatus === 'CANCELLED' ? '<span class="badge badge-red">ĐÃ HỦY</span>' :
              '<span class="badge badge-amber">HẾT HẠN</span>'}
          </div>
        </div>

        <!-- Cancellation Notice if CANCELLED -->
        ${effStatus === 'CANCELLED' ? `
          <div class="notice notice-danger" style="margin-top: 0; margin-bottom: 14px;">
            <strong>Kèo thi đấu đã bị hủy:</strong> ${match.cancelReason || 'Đội đã quyết định hủy trận đấu.'}
          </div>
        ` : ''}

        <!-- Expired Notice if EXPIRED -->
        ${effStatus === 'EXPIRED' ? `
          <div class="notice notice-warning" style="margin-top: 0; margin-bottom: 14px;">
            <strong>Kèo đã hết hạn:</strong> Quá hạn đăng bài mà chưa ghép được đối tác.
          </div>
        ` : ''}

        <!-- Opponents Matchup Card -->
        <div class="card" style="margin-bottom: 14px;">
          <div style="display: flex; align-items: center; justify-content: space-between; gap: 12px;">
            <!-- Host Side -->
            <div style="flex: 1; text-align: center;">
              <div class="avatar avatar-md" style="margin: 0 auto 6px auto; background-color: var(--color-primary-container);">
                <span>${(hostTeam?.shortName || hostTeam?.name || 'A').slice(0, 2)}</span>
              </div>
              <div style="font-weight: 700; font-size: 14px; color: var(--color-on-surface); line-height: 1.2;">
                ${hostTeam ? hostTeam.name : 'Đội chủ nhà'}
              </div>
              <div style="font-size: 11px; color: var(--color-on-surface-variant); margin-top: 2px;">
                Đội chủ nhà • Uy tín: ${hostTeam?.reputationScore || 100}
              </div>
            </div>

            <div style="font-size: 18px; font-weight: 800; color: var(--color-primary);">VS</div>

            <!-- Guest Side -->
            <div style="flex: 1; text-align: center;">
              <div class="avatar avatar-md" style="margin: 0 auto 6px auto; background-color: var(--color-secondary-container);">
                <span>${guestTeam ? (guestTeam.shortName || guestTeam.name || 'B').slice(0, 2) : '?'}</span>
              </div>
              <div style="font-weight: 700; font-size: 14px; color: var(--color-on-surface); line-height: 1.2;">
                ${guestTeam ? guestTeam.name : '(Chưa ghép đối)'}
              </div>
              <div style="font-size: 11px; color: var(--color-on-surface-variant); margin-top: 2px;">
                ${guestTeam ? `Đối thủ • Uy tín: ${guestTeam.reputationScore || 100}` : 'Đang chờ nhận kèo'}
              </div>
            </div>
          </div>
        </div>

        <!-- Time & Venue Details (Kind A vs Kind B) -->
        <div class="card" style="margin-bottom: 14px;">
          <div class="card-header">
            <h4 class="card-title" style="font-size: 15px;">Thời gian & Địa điểm thi đấu</h4>
          </div>

          <div class="list-group">
            ${!isKindB || match.venueId ? `
              <!-- Single Venue (Kind A or confirmed Kind B) -->
              <div class="list-item">
                <div>
                  <div style="font-size: 12px; color: var(--color-on-surface-variant);">Sân bóng:</div>
                  <div style="font-weight: 600; font-size: 14px; color: var(--color-on-surface);">
                    ${venue ? venue.name : 'Chưa xác định sân'}
                  </div>
                  <div style="font-size: 12px; color: var(--color-on-surface-variant);">
                    ${venue ? venue.address : ''}
                  </div>
                </div>
                ${venue ? `
                  <button class="btn btn-ghost" style="font-size: 12px; padding: 4px 8px;" onclick="window.AppRouter.navigate('venue-detail', { id: '${venue.id}' })">
                    Chi tiết sân >
                  </button>
                ` : ''}
              </div>

              <div class="list-item">
                <div>
                  <div style="font-size: 12px; color: var(--color-on-surface-variant);">Thời gian thi đấu:</div>
                  <div style="font-weight: 600; font-size: 14px; color: var(--color-on-surface);">
                    ${match.matchDate} (${match.startTime} - ${match.endTime})
                  </div>
                </div>
              </div>
            ` : `
              <!-- Kind B Proposed Options before confirmation -->
              <div class="list-item" style="flex-direction: column; align-items: flex-start; gap: 6px;">
                <div style="font-size: 12px; color: var(--color-on-surface-variant); font-weight: 600;">
                  Danh sách ${(match.proposedVenues || []).length} sân đề xuất:
                </div>
                <div style="display: flex; flex-direction: column; gap: 6px; width: 100%;">
                  ${(match.proposedVenues || []).map(vid => {
                    const v = getVenue(vid);
                    return `
                      <div style="padding: 6px 10px; background: var(--color-surface-container-low); border-radius: var(--radius-base); font-size: 13px; display: flex; justify-content: space-between;">
                        <span>📍 ${v ? v.name : vid}</span>
                        ${v?.status === 'INACTIVE' ? '<span class="badge badge-red" style="font-size: 10px;">NGỪNG HOẠT ĐỘNG</span>' : ''}
                      </div>
                    `;
                  }).join('')}
                </div>
              </div>

              <div class="list-item" style="flex-direction: column; align-items: flex-start; gap: 6px;">
                <div style="font-size: 12px; color: var(--color-on-surface-variant); font-weight: 600;">
                  Khung giờ đề xuất (${match.matchDate}):
                </div>
                <div style="display: flex; flex-wrap: wrap; gap: 6px;">
                  ${(match.proposedTimeSlots || []).map(slot => `
                    <span class="badge badge-gray" style="font-size: 12px; padding: 4px 8px;">⏰ ${slot}</span>
                  `).join('')}
                </div>
              </div>
            `}

            <div class="list-item">
              <div>
                <div style="font-size: 12px; color: var(--color-on-surface-variant);">Hạn chót đóng đăng bài:</div>
                <div style="font-size: 13px; color: var(--color-on-surface);">
                  ${match.expiresAt ? match.expiresAt.replace('T', ' ').slice(0, 16) : 'Trước giờ đá 1 tiếng'}
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Cost Agreement Card (MATCH-14) -->
        <div class="card" style="margin-bottom: 16px;">
          <div class="card-header">
            <h4 class="card-title" style="font-size: 15px;">Thỏa thuận chi phí & Tiền cọc</h4>
            ${isHostCaptain && effStatus === 'OPEN' && !isKindB ? `
              <button class="btn btn-ghost" id="match-edit-cost-btn" style="font-size: 12px; padding: 4px 8px; color: var(--color-primary);">
                ✏️ Sửa chi phí
              </button>
            ` : ''}
          </div>

          <div class="list-group">
            <div class="list-item">
              <div>
                <div style="font-size: 12px; color: var(--color-on-surface-variant);">Tổng chi phí ước tính:</div>
                <div style="font-weight: 700; font-size: 16px; color: var(--color-primary-dark);">
                  ${formatCost(match.totalCost)}
                </div>
              </div>
              <span class="badge badge-gray">${formatCostSplit(match.costSharing)}</span>
            </div>

            ${match.depositAmount ? `
              <div class="list-item">
                <div>
                  <div style="font-size: 12px; color: var(--color-on-surface-variant);">Tiền cọc giữ chân:</div>
                  <div style="font-weight: 600; font-size: 14px; color: var(--color-warning);">
                    ${formatCost(match.depositAmount)} (Bên giữ: ${match.depositHolder === 'HOST' ? 'Đội chủ nhà' : 'Sân'})
                  </div>
                </div>
              </div>
            ` : ''}

            ${match.notes ? `
              <div class="list-item" style="border-bottom: none;">
                <div style="font-size: 13px; color: var(--color-on-surface-variant);">
                  <strong>Ghi chú:</strong> ${match.notes}
                </div>
              </div>
            ` : ''}
          </div>
        </div>

        <!-- Actions Section Based on User Role & Match State -->
        <div style="display: flex; flex-direction: column; gap: 10px;">
          <!-- 1. HOST CAPTAIN ACTIONS -->
          ${isHostCaptain ? `
            ${effStatus === 'OPEN' ? `
              ${!isKindB ? `
                <!-- Kind A Host Actions -->
                <button class="btn btn-primary btn-block" id="host-view-requests-btn">
                  📥 Xem yêu cầu ghép trận (${pendingRequests.length} chờ duyệt)
                </button>
              ` : `
                <!-- Kind B Host Actions -->
                <button class="btn btn-primary btn-block" id="host-view-pairings-btn">
                  🤝 Xem các đội đang thương lượng (${matchPairings.length} đội)
                </button>
              `}

              <button class="btn btn-danger-outline btn-block" id="host-cancel-match-btn">
                Hủy bài đăng kèo này
              </button>
            ` : ''}

            ${effStatus === 'CONFIRMED' ? `
              <button class="btn btn-primary btn-block" id="match-open-chat-btn">
                💬 Chat với đội đối tác (${guestTeam?.name || 'Đội khách'})
              </button>
              <button class="btn btn-danger-outline btn-block" id="match-cancel-confirmed-btn">
                Hủy trận đấu (Có thể trừ điểm uy tín)
              </button>
            ` : ''}
          ` : ''}

          <!-- 2. GUEST CAPTAIN ACTIONS (Already Confirmed) -->
          ${isGuestCaptain && effStatus === 'CONFIRMED' ? `
            <button class="btn btn-primary btn-block" id="match-open-chat-btn">
              💬 Chat với đội chủ nhà (${hostTeam?.name})
            </button>
            <button class="btn btn-danger-outline btn-block" id="guest-withdraw-confirmed-btn">
              Rút khỏi trận đấu (Hủy trận & trừ uy tín)
            </button>
          ` : ''}

          <!-- 3. CANDIDATE TEAMS ACTIONS (When match is OPEN) -->
          ${!isHostCaptain && effStatus === 'OPEN' ? `
            ${!isKindB ? `
              <!-- Kind A Candidate Flow -->
              ${!myRequest ? `
                <div class="card" style="padding: 12px; background: var(--color-surface-container-low);">
                  <div class="form-group" style="margin-bottom: 8px;">
                    <label class="form-label">Lời nhắn kèm yêu cầu ghép trận:</label>
                    <input type="text" class="form-input" id="req-msg-input" placeholder="Ví dụ: Đội mình xin đăng ký giao lưu 7 người...">
                  </div>
                  <button class="btn btn-primary btn-block" id="candidate-send-request-btn">
                    🚀 Gửi yêu cầu ghép trận ngay
                  </button>
                </div>
              ` : `
                <div class="notice ${myRequest.status === 'PENDING' ? 'notice-info' : 'notice-warning'}">
                  <strong>Trạng thái yêu cầu của bạn:</strong>
                  ${myRequest.status === 'PENDING' ? 'Đang chờ đội chủ nhà duyệt yêu cầu.' :
                    myRequest.status === 'ACCEPTED' ? 'Yêu cầu của bạn đã được chấp nhận!' :
                    myRequest.status === 'REJECTED' ? 'Yêu cầu đã bị từ chối.' :
                    myRequest.status === 'SUPERSEDED' ? 'Kèo đã được ghép với đội khác.' : 'Đã rút.'}
                </div>

                ${myRequest.status === 'PENDING' ? `
                  <button class="btn btn-danger-outline btn-block" id="candidate-withdraw-req-btn">
                    Rút lại yêu cầu (Miễn phí)
                  </button>
                ` : ''}
              `}
            ` : `
              <!-- Kind B Candidate Flow -->
              ${!myPairing ? `
                <button class="btn btn-primary btn-block" id="candidate-open-pairing-btn" style="min-height: 48px;">
                  🤝 Nhận kèo & Mở thương lượng (Luồng B)
                </button>
              ` : `
                <div class="notice notice-info">
                  Bạn đang có phiên thương lượng gói cho kèo này.
                </div>
                <button class="btn btn-primary btn-block" id="candidate-goto-pairing-btn">
                  Vào màn hình thương lượng của bạn
                </button>
              `}
            `}
          ` : ''}

          <!-- General Back Button -->
          <button class="btn btn-secondary btn-block" onclick="window.AppRouter.navigate('matches')">
            Quay lại danh sách kèo
          </button>
        </div>
      </div>
    `;

    // Handlers
    const editCostBtn = container.querySelector('#match-edit-cost-btn');
    if (editCostBtn) {
      editCostBtn.addEventListener('click', () => {
        router.openSheet('match-cost-sheet', { matchId: match.id });
      });
    }

    const hostReqsBtn = container.querySelector('#host-view-requests-btn');
    if (hostReqsBtn) {
      hostReqsBtn.addEventListener('click', () => {
        router.navigate('match-requests-inbox', { matchId: match.id });
      });
    }

    const hostPairingsBtn = container.querySelector('#host-view-pairings-btn');
    if (hostPairingsBtn) {
      hostPairingsBtn.addEventListener('click', () => {
        router.navigate('pairings', { matchId: match.id });
      });
    }

    const cancelMatchBtn = container.querySelector('#host-cancel-match-btn');
    if (cancelMatchBtn) {
      cancelMatchBtn.addEventListener('click', () => {
        router.openSheet('match-cancel-sheet', { matchId: match.id });
      });
    }

    const cancelConfBtn = container.querySelector('#match-cancel-confirmed-btn');
    if (cancelConfBtn) {
      cancelConfBtn.addEventListener('click', () => {
        router.openSheet('match-cancel-sheet', { matchId: match.id, isConfirmed: true });
      });
    }

    const guestWithdrawBtn = container.querySelector('#guest-withdraw-confirmed-btn');
    if (guestWithdrawBtn) {
      guestWithdrawBtn.addEventListener('click', () => {
        const req = (state.matchRequests || []).find(r => r.matchId === match.id && r.requesterTeamId === guestTeam?.id);
        if (req) {
          router.openSheet('match-withdraw-sheet', { requestId: req.id });
        } else {
          router.openSheet('match-cancel-sheet', { matchId: match.id, isConfirmed: true });
        }
      });
    }

    const sendReqBtn = container.querySelector('#candidate-send-request-btn');
    if (sendReqBtn) {
      sendReqBtn.addEventListener('click', () => {
        if (!actingTeamId) {
          router.showToast('Vui lòng chọn đội của bạn trước khi gửi yêu cầu', 'warning');
          return;
        }
        const msg = container.querySelector('#req-msg-input')?.value || '';
        try {
          store.sendMatchRequest(match.id, actingTeamId, msg);
          router.showToast('Gửi yêu cầu ghép trận thành công!', 'success');
          router.navigate('match-detail', { id: match.id });
        } catch (err) {
          router.showToast(err.message, 'error');
        }
      });
    }

    const withdrawReqBtn = container.querySelector('#candidate-withdraw-req-btn');
    if (withdrawReqBtn && myRequest) {
      withdrawReqBtn.addEventListener('click', () => {
        router.openSheet('match-withdraw-sheet', { requestId: myRequest.id });
      });
    }

    const openPairingBtn = container.querySelector('#candidate-open-pairing-btn');
    if (openPairingBtn) {
      openPairingBtn.addEventListener('click', () => {
        if (!actingTeamId) {
          router.showToast('Vui lòng chọn đội nhận kèo', 'warning');
          return;
        }
        // Create new pairing
        const newPairingId = 'pairing-' + Date.now();
        const newPairing = {
          id: newPairingId,
          matchId: match.id,
          hostTeamId: match.hostTeamId,
          guestTeamId: actingTeamId,
          status: 'NEGOTIATING',
          package: {
            venueId: (match.proposedVenues || [])[0] || null,
            timeSlot: (match.proposedTimeSlots || [])[0] || null,
            totalCost: match.totalCost || 600000,
            costSharing: match.costSharing || 'SPLIT_50_50',
            depositAmount: match.depositAmount || 0,
            depositHolder: match.depositHolder || 'HOST',
            revision: 0,
            confirms: {
              [match.hostTeamId]: false,
              [actingTeamId]: false
            }
          },
          createdAt: state.meta.mockClock
        };

        store.setState(s => {
          if (!s.pairings) s.pairings = [];
          s.pairings.push(newPairing);
          return s;
        });

        router.showToast('Đã nhận kèo và mở phiên thương lượng!', 'success');
        router.navigate('pairing-detail', { id: newPairingId });
      });
    }

    const gotoPairingBtn = container.querySelector('#candidate-goto-pairing-btn');
    if (gotoPairingBtn && myPairing) {
      gotoPairingBtn.addEventListener('click', () => {
        router.navigate('pairing-detail', { id: myPairing.id });
      });
    }
  });

  /* =========================================================================
     MATCH-05: Create Match A or B View ('match-create')
     ========================================================================= */
  router.registerView('match-create', (container, params = {}) => {
    router.setHeaderTitle('Đăng kèo giao lưu');

    const state = store.getState();
    const currentUserId = state.currentUserId;
    const captainedTeams = store.getMyCaptainedTeams();
    const venues = (state.venues || []).filter(v => v.status === 'ACTIVE');
    const nowClock = new Date(state.meta.mockClock);

    if (captainedTeams.length === 0) {
      container.innerHTML = `
        <div class="empty-state">
          <h3 class="empty-state-title">Chỉ đội trưởng mới được đăng kèo</h3>
          <p class="empty-state-desc">Bạn chưa làm đội trưởng của đội bóng nào. Hãy tạo đội bóng trước khi đăng kèo.</p>
          <button class="btn btn-primary" onclick="window.AppRouter.navigate('team-create')">Tạo đội bóng mới</button>
        </div>
      `;
      return;
    }

    // Default dates & times
    // Starts at least +1h from mockClock (BR-M01)
    const minStart = new Date(nowClock.getTime() + 70 * 60 * 1000); // +1h10m
    const defaultDate = minStart.toISOString().slice(0, 10);
    const defaultStartTime = minStart.toTimeString().slice(0, 5);

    // End time default +1.5h
    const minEnd = new Date(minStart.getTime() + 90 * 60 * 1000);
    const defaultEndTime = minEnd.toTimeString().slice(0, 5);

    // Restore draft if returning from Venue Picker (J-16)
    const draft = (typeof window !== 'undefined' ? window._matchCreateDraft : _matchCreateDraft) || {
      kind: 'A',
      hostTeamId: state.actingTeamId || captainedTeams[0].id,
      format: 'SEVEN_A_SIDE',
      skillLevel: 'INTERMEDIATE',
      venueId: params.selectedVenueId || venues[0]?.id || '',
      proposedVenues: [venues[0]?.id || ''],
      matchDate: defaultDate,
      startTime: defaultStartTime,
      endTime: defaultEndTime,
      proposedTimeSlots: ['18:00 - 19:30', '19:30 - 21:00'],
      totalCost: 600000,
      costSharing: 'SPLIT_50_50',
      depositAmount: 0,
      depositHolder: 'HOST',
      notes: ''
    };

    if (params.selectedVenueId) {
      draft.venueId = params.selectedVenueId;
    }

    function renderForm() {
      const isKindB = draft.kind === 'B';

      container.innerHTML = `
        <div style="padding: 14px 0 28px 0;">
          <form id="match-create-form" novalidate>
            <!-- 1. Acting Team Selector -->
            <div class="form-group">
              <label class="form-label">Đội bóng của bạn</label>
              <select class="form-select" id="mc-team">
                ${captainedTeams.map(t => `
                  <option value="${t.id}" ${t.id === draft.hostTeamId ? 'selected' : ''}>${t.name}</option>
                `).join('')}
              </select>
            </div>

            <!-- 2. Kind A vs Kind B Toggle -->
            <div class="form-group">
              <label class="form-label">Loại kèo đăng</label>
              <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 8px;">
                <button type="button" class="btn ${!isKindB ? 'btn-primary' : 'btn-secondary'}" id="mc-kind-a-btn">
                  Luồng A (Đã có sân)
                </button>
                <button type="button" class="btn ${isKindB ? 'btn-primary' : 'btn-secondary'}" id="mc-kind-b-btn">
                  Luồng B (Chưa có sân)
                </button>
              </div>
              <div class="field-helper-msg">
                ${!isKindB ? 'Đội bạn đã đặt sẵn sân và giờ, tìm đối thủ đến giao lưu.' : 'Đề xuất 1-5 sân và 1-5 khung giờ để đối thủ lựa chọn.'}
              </div>
            </div>

            <!-- 3. Format & Skill -->
            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 10px;">
              <div class="form-group">
                <label class="form-label">Định dạng sân</label>
                <select class="form-select" id="mc-format">
                  <option value="SEVEN_A_SIDE" ${draft.format === 'SEVEN_A_SIDE' ? 'selected' : ''}>Sân 7 người</option>
                  <option value="FIVE_A_SIDE" ${draft.format === 'FIVE_A_SIDE' ? 'selected' : ''}>Sân 5 người</option>
                  <option value="ELEVEN_A_SIDE" ${draft.format === 'ELEVEN_A_SIDE' ? 'selected' : ''}>Sân 11 người</option>
                </select>
              </div>

              <div class="form-group">
                <label class="form-label">Trình độ nhận kèo</label>
                <select class="form-select" id="mc-skill">
                  <option value="INTERMEDIATE" ${draft.skillLevel === 'INTERMEDIATE' ? 'selected' : ''}>Trung bình</option>
                  <option value="BEGINNER" ${draft.skillLevel === 'BEGINNER' ? 'selected' : ''}>Mới chơi</option>
                  <option value="ADVANCED" ${draft.skillLevel === 'ADVANCED' ? 'selected' : ''}>Khá / Chuyên</option>
                </select>
              </div>
            </div>

            ${!isKindB ? `
              <!-- ================= KIND A INPUTS ================= -->
              <!-- Venue picker with return-to-form support (VEN-01, J-16) -->
              <div class="form-group">
                <label class="form-label">
                  Sân thi đấu đã đặt <span class="required-mark">*</span>
                </label>
                <div style="display: flex; gap: 8px;">
                  <select class="form-select" id="mc-venue" style="flex: 1;" required>
                    <option value="">-- Chọn sân bóng --</option>
                    ${venues.map(v => `
                      <option value="${v.id}" ${v.id === draft.venueId ? 'selected' : ''}>
                        ${v.name} (${v.address})
                      </option>
                    `).join('')}
                  </select>
                  <button type="button" class="btn btn-secondary" id="mc-open-venue-picker-btn" title="Mở danh bạ sân">
                    🔍 Tìm/Thêm
                  </button>
                </div>
                <div class="field-error-msg" id="mc-venue-error" style="display: none;"></div>
              </div>

              <!-- Date & Time -->
              <div class="form-group">
                <label class="form-label">Ngày thi đấu <span class="required-mark">*</span></label>
                <input type="date" class="form-input" id="mc-date" value="${draft.matchDate}" required>
                <div class="field-error-msg" id="mc-date-error" style="display: none;"></div>
              </div>

              <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 10px;">
                <div class="form-group">
                  <label class="form-label">Giờ bắt đầu <span class="required-mark">*</span></label>
                  <input type="time" class="form-input" id="mc-start-time" value="${draft.startTime}" required>
                  <div class="field-error-msg" id="mc-start-error" style="display: none;"></div>
                </div>

                <div class="form-group">
                  <label class="form-label">Giờ kết thúc <span class="required-mark">*</span></label>
                  <input type="time" class="form-input" id="mc-end-time" value="${draft.endTime}" required>
                  <div class="field-error-msg" id="mc-end-error" style="display: none;"></div>
                </div>
              </div>
            ` : `
              <!-- ================= KIND B INPUTS ================= -->
              <div class="form-group">
                <label class="form-label">
                  Đề xuất 1-5 sân bóng <span class="required-mark">*</span>
                </label>
                <div style="display: flex; flex-direction: column; gap: 6px; margin-bottom: 6px;">
                  ${venues.slice(0, 4).map(v => `
                    <label style="display: flex; align-items: center; gap: 8px; font-size: 13px; cursor: pointer; padding: 6px 8px; border: 1px solid var(--color-outline-variant); border-radius: var(--radius-base);">
                      <input type="checkbox" name="mc-b-venues" value="${v.id}"
                        ${(draft.proposedVenues || []).includes(v.id) ? 'checked' : ''}>
                      <span>${v.name} (${v.address})</span>
                    </label>
                  `).join('')}
                </div>
                <div class="field-error-msg" id="mc-b-venues-error" style="display: none;"></div>
              </div>

              <div class="form-group">
                <label class="form-label">Ngày thi đấu dự kiến <span class="required-mark">*</span></label>
                <input type="date" class="form-input" id="mc-date" value="${draft.matchDate}" required>
              </div>

              <div class="form-group">
                <label class="form-label">Đề xuất 1-5 khung giờ <span class="required-mark">*</span></label>
                <div style="display: flex; flex-direction: column; gap: 6px;">
                  ${['17:30 - 19:00', '19:00 - 20:30', '20:30 - 22:00'].map(slot => `
                    <label style="display: flex; align-items: center; gap: 8px; font-size: 13px; cursor: pointer; padding: 6px 8px; border: 1px solid var(--color-outline-variant); border-radius: var(--radius-base);">
                      <input type="checkbox" name="mc-b-slots" value="${slot}"
                        ${(draft.proposedTimeSlots || []).includes(slot) ? 'checked' : ''}>
                      <span>Khung giờ: ${slot}</span>
                    </label>
                  `).join('')}
                </div>
                <div class="field-error-msg" id="mc-b-slots-error" style="display: none;"></div>
              </div>

              <div class="form-group">
                <label class="form-label">Tiền cọc yêu cầu (VND)</label>
                <input type="number" class="form-input" id="mc-deposit" value="${draft.depositAmount || 0}" step="50000" min="0">
              </div>
            `}

            <!-- Cost sharing -->
            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 10px;">
              <div class="form-group">
                <label class="form-label">Tổng chi phí (VND)</label>
                <input type="number" class="form-input" id="mc-cost" value="${draft.totalCost || 600000}" step="50000" min="0">
              </div>

              <div class="form-group">
                <label class="form-label">Cách chia tiền</label>
                <select class="form-select" id="mc-split">
                  <option value="SPLIT_50_50" ${draft.costSharing === 'SPLIT_50_50' ? 'selected' : ''}>Chia đôi 50/50</option>
                  <option value="HOST_100" ${draft.costSharing === 'HOST_100' ? 'selected' : ''}>Đội nhà bao sân</option>
                  <option value="SEVENTY_THIRTY_LOSER" ${draft.costSharing === 'SEVENTY_THIRTY_LOSER' ? 'selected' : ''}>70/30 (Đội thua 70%)</option>
                  <option value="LOSER_PAYS_DRINKS" ${draft.costSharing === 'LOSER_PAYS_DRINKS' ? 'selected' : ''}>Đội thua trả nước</option>
                </select>
              </div>
            </div>

            <!-- Notes -->
            <div class="form-group">
              <label class="form-label">Ghi chú yêu cầu giao lưu</label>
              <textarea class="form-textarea" id="mc-notes" placeholder="Màu áo, tính chất trận đấu, yêu cầu trọng tài...">${draft.notes || ''}</textarea>
            </div>

            <!-- Submit -->
            <div style="margin-top: 20px; display: flex; flex-direction: column; gap: 10px;">
              <button type="submit" class="btn btn-primary btn-block" style="min-height: 48px;">
                Xem lại & Đăng kèo đấu
              </button>
              <button type="button" class="btn btn-secondary btn-block" id="mc-cancel-btn">
                Hủy bỏ
              </button>
            </div>
          </form>
        </div>
      `;

      // Save form inputs to draft on changes
      function syncDraft() {
        draft.hostTeamId = container.querySelector('#mc-team').value;
        draft.format = container.querySelector('#mc-format').value;
        draft.skillLevel = container.querySelector('#mc-skill').value;
        draft.matchDate = container.querySelector('#mc-date').value;
        draft.totalCost = Number(container.querySelector('#mc-cost').value) || 600000;
        draft.costSharing = container.querySelector('#mc-split').value;
        draft.notes = container.querySelector('#mc-notes').value;

        if (draft.kind === 'A') {
          draft.venueId = container.querySelector('#mc-venue')?.value || '';
          draft.startTime = container.querySelector('#mc-start-time')?.value || '';
          draft.endTime = container.querySelector('#mc-end-time')?.value || '';
        } else {
          const checkedVenues = Array.from(container.querySelectorAll('input[name="mc-b-venues"]:checked')).map(el => el.value);
          const checkedSlots = Array.from(container.querySelectorAll('input[name="mc-b-slots"]:checked')).map(el => el.value);
          draft.proposedVenues = checkedVenues;
          draft.proposedTimeSlots = checkedSlots;
          draft.depositAmount = Number(container.querySelector('#mc-deposit')?.value) || 0;
        }

        _matchCreateDraft = draft;
        if (typeof window !== 'undefined') window._matchCreateDraft = draft;
      }

      // Handlers
      const kindABtn = container.querySelector('#mc-kind-a-btn');
      if (kindABtn) {
        kindABtn.addEventListener('click', () => {
          syncDraft();
          draft.kind = 'A';
          renderForm();
        });
      }

      const kindBBtn = container.querySelector('#mc-kind-b-btn');
      if (kindBBtn) {
        kindBBtn.addEventListener('click', () => {
          syncDraft();
          draft.kind = 'B';
          renderForm();
        });
      }

      const openVenuePickerBtn = container.querySelector('#mc-open-venue-picker-btn');
      if (openVenuePickerBtn) {
        openVenuePickerBtn.addEventListener('click', () => {
          syncDraft();
          // Navigate to Venue Picker with returnRoute support (VEN-01, J-16)
          router.navigate('venues', { picker: 'true', returnRoute: 'match-create' });
        });
      }

      const cancelBtn = container.querySelector('#mc-cancel-btn');
      if (cancelBtn) {
        cancelBtn.addEventListener('click', () => {
          _matchCreateDraft = null;
          if (typeof window !== 'undefined') window._matchCreateDraft = null;
          router.goBack();
        });
      }

      // Submit & Validate
      const matchForm = container.querySelector('#match-create-form');
      if (matchForm) {
        matchForm.addEventListener('submit', (e) => {
          e.preventDefault();
          syncDraft();

        let hasError = false;

        // Reset error messages
        container.querySelectorAll('.field-error-msg').forEach(el => {
          el.style.display = 'none';
          el.textContent = '';
        });

        if (draft.kind === 'A') {
          if (!draft.venueId) {
            const el = container.querySelector('#mc-venue-error');
            el.textContent = 'Vui lòng chọn sân thi đấu';
            el.style.display = 'block';
            hasError = true;
          }

          // Validation: start time >= now + 1 hour (BR-M01)
          const startDateTime = new Date(`${draft.matchDate}T${draft.startTime}:00Z`).getTime();
          const minAllowedStart = nowClock.getTime() + 60 * 60 * 1000;

          if (startDateTime < minAllowedStart) {
            const el = container.querySelector('#mc-start-error');
            el.textContent = 'Giờ bắt đầu phải cách thời điểm hiện tại ít nhất 1 giờ (BR-M01)';
            el.style.display = 'block';
            hasError = true;
          }

          // Duration <= 12 hours
          const endDateTime = new Date(`${draft.matchDate}T${draft.endTime}:00Z`).getTime();
          const durationHours = (endDateTime - startDateTime) / (3600 * 1000);

          if (durationHours <= 0) {
            const el = container.querySelector('#mc-end-error');
            el.textContent = 'Giờ kết thúc phải sau giờ bắt đầu';
            el.style.display = 'block';
            hasError = true;
          } else if (durationHours > 12) {
            const el = container.querySelector('#mc-end-error');
            el.textContent = 'Thời lượng trận đấu không được quá 12 giờ';
            el.style.display = 'block';
            hasError = true;
          }
        } else {
          // Kind B validations: 1-5 venues, 1-5 slots
          if (!draft.proposedVenues || draft.proposedVenues.length === 0) {
            const el = container.querySelector('#mc-b-venues-error');
            el.textContent = 'Vui lòng chọn ít nhất 1 sân đề xuất';
            el.style.display = 'block';
            hasError = true;
          } else if (draft.proposedVenues.length > 5) {
            const el = container.querySelector('#mc-b-venues-error');
            el.textContent = 'Tối đa 5 địa điểm đề xuất (BR-M03)';
            el.style.display = 'block';
            hasError = true;
          }

          if (!draft.proposedTimeSlots || draft.proposedTimeSlots.length === 0) {
            const el = container.querySelector('#mc-b-slots-error');
            el.textContent = 'Vui lòng chọn ít nhất 1 khung giờ đề xuất';
            el.style.display = 'block';
            hasError = true;
          } else if (draft.proposedTimeSlots.length > 5) {
            const el = container.querySelector('#mc-b-slots-error');
            el.textContent = 'Tối đa 5 khung giờ đề xuất';
            el.style.display = 'block';
            hasError = true;
          }
        }

        if (hasError) return;

        // Open MATCH-06 Review Before Posting Sheet
        router.openSheet('match-review-sheet', {
          draftData: draft,
          onConfirmPost: () => {
            try {
              const newMatch = store.createMatch(draft);
              _matchCreateDraft = null;
        if (typeof window !== 'undefined') window._matchCreateDraft = null;
              router.closeSheet();
              router.showToast('Đăng kèo giao lưu thành công!', 'success');
              router.navigate('match-detail', { id: newMatch.id });
            } catch (err) {
              router.showToast(err.message, 'error');
            }
          }
        });
      });
    }
  }

  renderForm();
});

  /* =========================================================================
     MATCH-06: Review Before Posting Sheet ('match-review-sheet')
     ========================================================================= */
  router.registerSheet('match-review-sheet', (body, footer, params = {}, titleElem) => {
    if (titleElem) titleElem.textContent = 'Kiểm tra thông tin trước khi đăng';

    const draft = params.draftData || {};
    const hostTeam = getTeam(draft.hostTeamId);
    const venue = draft.venueId ? getVenue(draft.venueId) : null;
    const isKindB = draft.kind === 'B';

    body.innerHTML = `
      <div style="padding-bottom: 8px;">
        <div class="notice notice-info" style="margin-top: 0; margin-bottom: 12px;">
          Sau khi đăng, bạn không thể thay đổi giờ thi đấu và địa điểm đã chốt (BR-M02).
        </div>

        <div class="card" style="padding: 10px; margin-bottom: 8px;">
          <div style="font-size: 12px; color: var(--color-on-surface-variant);">Đội đăng bài:</div>
          <div style="font-weight: 700; font-size: 15px;">${hostTeam?.name || 'Đội chủ nhà'}</div>
          <div style="font-size: 12px; margin-top: 4px;">
            Định dạng: <strong>${formatFormatLabel(draft.format)}</strong> • Trình độ: <strong>${draft.skillLevel}</strong>
          </div>
        </div>

        <div class="card" style="padding: 10px; margin-bottom: 8px;">
          <div style="font-size: 12px; color: var(--color-on-surface-variant);">Địa điểm & Thời gian:</div>
          ${!isKindB ? `
            <div style="font-weight: 600; font-size: 14px;">${venue?.name || 'Chưa chọn'}</div>
            <div style="font-size: 12px; color: var(--color-on-surface-variant);">${venue?.address || ''}</div>
            <div style="font-size: 13px; font-weight: 600; margin-top: 4px; color: var(--color-primary-dark);">
              Ngày: ${draft.matchDate} (${draft.startTime} - ${draft.endTime})
            </div>
          ` : `
            <div style="font-size: 13px; font-weight: 600;">
              ${(draft.proposedVenues || []).length} sân đề xuất • ${(draft.proposedTimeSlots || []).length} khung giờ đề xuất
            </div>
          `}
        </div>

        <div class="card" style="padding: 10px; margin-bottom: 0;">
          <div style="display: flex; justify-content: space-between; font-size: 13px;">
            <span>Tổng chi phí:</span>
            <strong style="color: var(--color-primary);">${formatCost(draft.totalCost)}</strong>
          </div>
          <div style="display: flex; justify-content: space-between; font-size: 12px; color: var(--color-on-surface-variant); margin-top: 4px;">
            <span>Chia tiền:</span>
            <span>${formatCostSplit(draft.costSharing)}</span>
          </div>
        </div>
      </div>
    `;

    footer.innerHTML = `
      <div style="display: flex; gap: 8px; width: 100%;">
        <button class="btn btn-secondary" style="flex: 1;" onclick="window.AppRouter.closeSheet()">Sửa lại</button>
        <button class="btn btn-primary" style="flex: 2;" id="match-review-confirm-btn">
          Xác nhận đăng bài
        </button>
      </div>
    `;

    const confirmBtn = footer.querySelector('#match-review-confirm-btn');
    if (confirmBtn) {
      confirmBtn.addEventListener('click', () => {
        confirmBtn.disabled = true;
        confirmBtn.textContent = 'Đang đăng...';
        if (typeof params.onConfirmPost === 'function') {
          params.onConfirmPost();
        }
      });
    }
  });

  /* =========================================================================
     MATCH-07: Team Match Schedule / Calendar View ('match-schedule')
     ========================================================================= */
  router.registerView('match-schedule', (container, params = {}) => {
    router.setHeaderTitle('Lịch thi đấu của đội');

    const state = store.getState();
    const actingTeamId = state.actingTeamId;
    const nowClock = state.meta.mockClock;
    const team = getTeam(actingTeamId);

    if (!team) {
      container.innerHTML = `
        <div class="empty-state">
          <h3 class="empty-state-title">Chưa chọn đội bóng</h3>
          <p class="empty-state-desc">Vui lòng chọn đội bóng từ danh sách đội của bạn.</p>
          <button class="btn btn-primary" onclick="window.AppRouter.navigate('teams')">Đến danh sách đội</button>
        </div>
      `;
      return;
    }

    let filterStatus = params.status || 'ALL'; // 'ALL', 'UPCOMING', 'PAST'

    function renderSchedule() {
      const allMatches = state.matches || [];
      const teamMatches = allMatches.filter(m => {
        const isMyTeam = m.hostTeamId === actingTeamId || m.guestTeamId === actingTeamId || m.confirmedGuestTeamId === actingTeamId;
        if (!isMyTeam) return false;

        const eff = getEffectiveMatchStatus(m, nowClock);
        if (filterStatus === 'UPCOMING' && eff !== 'CONFIRMED' && eff !== 'IN_PROGRESS') return false;
        if (filterStatus === 'PAST' && eff !== 'COMPLETED' && eff !== 'CANCELLED' && eff !== 'EXPIRED') return false;

        return true;
      });

      container.innerHTML = `
        <div style="padding: 14px 0 24px 0;">
          <div style="margin-bottom: 12px;">
            <h3 style="margin: 0; font-size: 16px; font-weight: 700;">Lịch đấu: ${team.name}</h3>
            <p style="margin: 2px 0 0 0; font-size: 12px; color: var(--color-on-surface-variant);">
              Xem các trận sắp đá, đang diễn ra hoặc lịch sử các trận đã hoàn thành.
            </p>
          </div>

          <div class="chip-group" style="margin-bottom: 14px;">
            <button class="chip ${filterStatus === 'ALL' ? 'is-active' : ''}" data-status="ALL">Tất cả (${teamMatches.length})</button>
            <button class="chip ${filterStatus === 'UPCOMING' ? 'is-active' : ''}" data-status="UPCOMING">Sắp diễn ra</button>
            <button class="chip ${filterStatus === 'PAST' ? 'is-active' : ''}" data-status="PAST">Lịch sử trận</button>
          </div>

          ${teamMatches.length === 0 ? `
            <div class="empty-state">
              <h4 class="empty-state-title">Chưa có lịch đấu nào</h4>
              <p class="empty-state-desc">Đội của bạn hiện chưa có trận đấu nào trong danh mục này.</p>
              <button class="btn btn-primary" onclick="window.AppRouter.navigate('matches')">Tìm kèo ngay</button>
            </div>
          ` : `
            <div class="list-group">
              ${teamMatches.map(m => {
                const eff = getEffectiveMatchStatus(m, nowClock);
                const venue = getVenue(m.venueId);
                const isHost = m.hostTeamId === actingTeamId;
                const oppTeam = isHost ? getTeam(m.guestTeamId || m.confirmedGuestTeamId) : getTeam(m.hostTeamId);

                return `
                  <div class="card card-interactive match-schedule-item" data-id="${m.id}" style="margin-bottom: 10px;">
                    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 6px;">
                      <span class="badge ${eff === 'CONFIRMED' ? 'badge-blue' : eff === 'COMPLETED' ? 'badge-gray' : 'badge-amber'}">
                        ${eff === 'CONFIRMED' ? 'SẮP DIỄN RA' : eff === 'COMPLETED' ? 'ĐÃ KẾT THÚC' : eff}
                      </span>
                      <span style="font-size: 12px; color: var(--color-on-surface-variant);">
                        Vai trò: <strong>${isHost ? 'Đội chủ nhà' : 'Đội khách'}</strong>
                      </span>
                    </div>

                    <div style="font-weight: 700; font-size: 15px; margin-bottom: 4px;">
                      ${team.name} vs ${oppTeam ? oppTeam.name : '(Chờ đối thủ)'}
                    </div>

                    <div style="font-size: 12px; color: var(--color-on-surface-variant);">
                      ⏰ ${m.matchDate} (${m.startTime} - ${m.endTime}) • 📍 ${venue ? venue.name : 'Chưa chốt'}
                    </div>
                  </div>
                `;
              }).join('')}
            </div>
          `}
        </div>
      `;

      container.querySelectorAll('.chip[data-status]').forEach(chip => {
        chip.addEventListener('click', () => {
          filterStatus = chip.getAttribute('data-status');
          renderSchedule();
        });
      });

      container.querySelectorAll('.match-schedule-item').forEach(card => {
        card.addEventListener('click', () => {
          const mId = card.getAttribute('data-id');
          router.navigate('match-detail', { id: mId });
        });
      });
    }

    renderSchedule();
  });

  /* =========================================================================
     MATCH-08: Sent Match Requests Tracker ('match-requests-sent')
     ========================================================================= */
  router.registerView('match-requests-sent', (container, params = {}) => {
    router.setHeaderTitle('Yêu cầu đã gửi');

    const state = store.getState();
    const actingTeamId = state.actingTeamId;
    const allReqs = (state.matchRequests || []).filter(r => r.requesterTeamId === actingTeamId);

    container.innerHTML = `
      <div style="padding: 14px 0 24px 0;">
        <div style="margin-bottom: 12px;">
          <h3 style="margin: 0; font-size: 16px; font-weight: 700;">Yêu cầu ghép trận đã gửi (${allReqs.length})</h3>
          <p style="margin: 2px 0 0 0; font-size: 12px; color: var(--color-on-surface-variant);">
            Theo dõi trạng thái duyệt từ các đội chủ nhà hoặc rút yêu cầu.
          </p>
        </div>

        ${allReqs.length === 0 ? `
          <div class="empty-state">
            <h4 class="empty-state-title">Chưa gửi yêu cầu nào</h4>
            <p class="empty-state-desc">Đội của bạn chưa gửi yêu cầu ghép trận tới kèo đấu nào.</p>
            <button class="btn btn-primary" onclick="window.AppRouter.navigate('matches')">Khám phá kèo</button>
          </div>
        ` : `
          <div class="list-group">
            ${allReqs.map(r => {
              const match = (state.matches || []).find(m => m.id === r.matchId);
              const hostTeam = match ? getTeam(match.hostTeamId) : null;
              const isPending = r.status === 'PENDING';

              return `
                <div class="card" style="margin-bottom: 10px; padding: 12px;">
                  <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 6px;">
                    <span style="font-size: 13px; font-weight: 700; color: var(--color-on-surface);">
                      Tới: ${hostTeam ? hostTeam.name : 'Đội chủ nhà'}
                    </span>
                    <span class="badge ${r.status === 'PENDING' ? 'badge-amber' : r.status === 'ACCEPTED' ? 'badge-green' : 'badge-gray'}">
                      ${r.status === 'PENDING' ? 'CHỜ DUYỆT' :
                        r.status === 'ACCEPTED' ? 'ĐƯỢC CHẤP NHẬN' :
                        r.status === 'REJECTED' ? 'BỊ TỪ CHỐI' :
                        r.status === 'WITHDRAWN' ? 'ĐÃ RÚT' : 'THAY THẾ'}
                    </span>
                  </div>

                  <div style="font-size: 12px; color: var(--color-on-surface-variant); margin-bottom: 6px;">
                    Trận: ${match ? `${match.matchDate} (${match.startTime})` : 'Kèo đấu'}
                    ${r.message ? `• <em>"${r.message}"</em>` : ''}
                  </div>

                  <div style="display: flex; gap: 8px; justify-content: flex-end; margin-top: 8px; border-top: 1px solid var(--color-outline-variant); padding-top: 8px;">
                    <button class="btn btn-ghost" style="font-size: 12px; padding: 4px 10px;" onclick="window.AppRouter.navigate('match-detail', { id: '${r.matchId}' })">
                      Xem kèo >
                    </button>

                    ${isPending ? `
                      <button class="btn btn-danger-outline sent-req-withdraw-btn" data-req-id="${r.id}" style="font-size: 12px; padding: 4px 10px;">
                        Rút yêu cầu
                      </button>
                    ` : ''}
                  </div>
                </div>
              `;
            }).join('')}
          </div>
        `}
      </div>
    `;

    container.querySelectorAll('.sent-req-withdraw-btn').forEach(btn => {
      btn.addEventListener('click', () => {
        const reqId = btn.getAttribute('data-req-id');
        router.openSheet('match-withdraw-sheet', { requestId: reqId });
      });
    });
  });

  /* =========================================================================
     MATCH-09 & MATCH-10: Received Requests View ('match-requests-inbox')
     ========================================================================= */
  router.registerView('match-requests-inbox', (container, params = {}) => {
    const matchId = params.matchId;
    const state = store.getState();
    const actingTeamId = state.actingTeamId;

    router.setHeaderTitle('Yêu cầu ghép đến');

    let allReqs = state.matchRequests || [];
    if (matchId) {
      allReqs = allReqs.filter(r => r.matchId === matchId);
    } else {
      // All requests for my captained matches
      const myMatchIds = (state.matches || []).filter(m => m.hostTeamId === actingTeamId).map(m => m.id);
      allReqs = allReqs.filter(r => myMatchIds.includes(r.matchId));
    }

    container.innerHTML = `
      <div style="padding: 14px 0 24px 0;">
        <div style="margin-bottom: 12px;">
          <h3 style="margin: 0; font-size: 16px; font-weight: 700;">Hộp thư yêu cầu (${allReqs.length})</h3>
          <p style="margin: 2px 0 0 0; font-size: 12px; color: var(--color-on-surface-variant);">
            Chấp nhận 1 đội sẽ xác nhận kèo và tự động giải phóng (SUPERSEDED) các yêu cầu còn lại (AC-P03).
          </p>
        </div>

        ${allReqs.length === 0 ? `
          <div class="empty-state">
            <h4 class="empty-state-title">Chưa có yêu cầu nào</h4>
            <p class="empty-state-desc">Khi có đội khác gửi yêu cầu ghép trận, danh sách sẽ hiển thị tại đây.</p>
          </div>
        ` : `
          <div class="list-group">
            ${allReqs.map(r => {
              const reqTeam = getTeam(r.requesterTeamId);
              const isPending = r.status === 'PENDING';

              return `
                <div class="card" style="margin-bottom: 12px; padding: 14px;">
                  <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 6px;">
                    <div>
                      <h4 style="margin: 0 0 2px 0; font-size: 15px; font-weight: 700;">${reqTeam?.name || 'Đội gửi yêu cầu'}</h4>
                      <div style="font-size: 12px; color: var(--color-on-surface-variant);">
                        Điểm uy tín: <strong>${reqTeam?.reputationScore || 100}</strong> • Xếp hạng: 4.8★
                      </div>
                    </div>
                    <span class="badge ${r.status === 'PENDING' ? 'badge-amber' : r.status === 'ACCEPTED' ? 'badge-green' : 'badge-gray'}">
                      ${r.status === 'PENDING' ? 'ĐANG CHỜ' :
                        r.status === 'ACCEPTED' ? 'ĐÃ DUYỆT' :
                        r.status === 'REJECTED' ? 'TỪ CHỐI' : 'SUPERSEDED'}
                    </span>
                  </div>

                  ${r.message ? `
                    <div style="font-size: 13px; color: var(--color-on-surface); background: var(--color-surface-container-low); padding: 8px 10px; border-radius: var(--radius-base); margin: 8px 0;">
                      💬 "${r.message}"
                    </div>
                  ` : ''}

                  ${isPending ? `
                    <div style="display: flex; gap: 8px; margin-top: 10px;">
                      <button class="btn btn-primary req-accept-btn" data-req-id="${r.id}" style="flex: 2; min-height: 40px; font-size: 13px;">
                        ✓ Chấp nhận đối thủ
                      </button>
                      <button class="btn btn-secondary req-reject-btn" data-req-id="${r.id}" style="flex: 1; min-height: 40px; font-size: 13px;">
                        Từ chối
                      </button>
                    </div>
                  ` : ''}
                </div>
              `;
            }).join('')}
          </div>
        `}
      </div>
    `;

    // Handlers
    container.querySelectorAll('.req-accept-btn').forEach(btn => {
      btn.addEventListener('click', () => {
        const reqId = btn.getAttribute('data-req-id');
        try {
          // AC-P03: acceptMatchRequest confirms match, marks this accepted, other pending superseded!
          const match = store.acceptMatchRequest(reqId);
          router.showToast('Đã chấp nhận yêu cầu! Kèo đấu đã được chốt (CONFIRMED).', 'success');
          router.navigate('match-detail', { id: match.id });
        } catch (err) {
          router.showToast(err.message, 'error');
        }
      });
    });

    container.querySelectorAll('.req-reject-btn').forEach(btn => {
      btn.addEventListener('click', () => {
        const reqId = btn.getAttribute('data-req-id');
        try {
          store.rejectMatchRequest(reqId);
          router.showToast('Đã từ chối yêu cầu.', 'info');
          router.navigate('match-requests-inbox', { matchId });
        } catch (err) {
          router.showToast(err.message, 'error');
        }
      });
    });
  });

  /* =========================================================================
     MATCH-11: Withdraw Request Sheet ('match-withdraw-sheet')
     ========================================================================= */
  router.registerSheet('match-withdraw-sheet', (body, footer, params = {}, titleElem) => {
    if (titleElem) titleElem.textContent = 'Rút yêu cầu ghép trận';

    const requestId = params.requestId;
    const state = store.getState();
    const req = (state.matchRequests || []).find(r => r.id === requestId);
    const nowClock = state.meta.mockClock;

    if (!req) {
      body.innerHTML = `<div class="notice notice-danger">Không tìm thấy yêu cầu</div>`;
      footer.innerHTML = `<button class="btn btn-secondary btn-block" onclick="window.AppRouter.closeSheet()">Đóng</button>`;
      return;
    }

    const match = (state.matches || []).find(m => m.id === req.matchId);
    const isAccepted = req.status === 'ACCEPTED';
    const penaltyInfo = isAccepted && match ? calculatePenalty(match.matchDate, match.startTime, nowClock) : null;

    body.innerHTML = `
      <div style="padding-bottom: 8px;">
        ${isAccepted ? `
          <div class="notice notice-danger" style="margin-top: 0; margin-bottom: 12px;">
            <strong>CẢNH BÁO: Rút yêu cầu đã được chấp nhận!</strong>
            <p style="margin: 4px 0 0 0; font-size: 13px;">
              Yêu cầu của bạn đã được đối tác chốt trận. Việc rút yêu cầu lúc này tương đương hủy trận đấu đã xác nhận và sẽ bị trừ điểm uy tín của đội.
            </p>
            <div style="margin-top: 6px; font-weight: 700; color: var(--color-error);">
              Mức phạt dự kiến: ${penaltyInfo?.text || '-5 điểm'}
            </div>
          </div>

          <div class="form-group" style="margin-bottom: 0;">
            <label class="form-label">Lý do rút yêu cầu <span class="required-mark">*</span></label>
            <textarea class="form-textarea" id="withdraw-reason" placeholder="Bắt buộc nhập lý do vì trận đấu đã được chốt..." required></textarea>
          </div>
        ` : `
          <p style="font-size: 14px; color: var(--color-on-surface); margin-top: 0;">
            Yêu cầu của bạn đang ở trạng thái chờ duyệt (PENDING). Bạn có thể rút lại hoàn toàn miễn phí mà không bị trừ điểm uy tín.
          </p>
        `}
      </div>
    `;

    footer.innerHTML = `
      <div style="display: flex; gap: 8px; width: 100%;">
        <button class="btn btn-secondary" style="flex: 1;" onclick="window.AppRouter.closeSheet()">Hủy</button>
        <button class="btn btn-danger" style="flex: 2;" id="withdraw-confirm-btn">
          ${isAccepted ? 'Xác nhận rút & Chấp nhận trừ điểm' : 'Xác nhận rút yêu cầu'}
        </button>
      </div>
    `;

    const withdrawConfirmBtn = footer.querySelector('#withdraw-confirm-btn');
    if (withdrawConfirmBtn) {
      withdrawConfirmBtn.addEventListener('click', () => {
        let reason = '';
        if (isAccepted) {
          reason = body.querySelector('#withdraw-reason')?.value.trim();
          if (!reason) {
            router.showToast('Vui lòng nhập lý do rút yêu cầu', 'warning');
            return;
          }
        }

        try {
          store.withdrawMatchRequest(requestId);
          if (isAccepted && penaltyInfo && !penaltyInfo.blocked) {
            // Deduct team reputation
            store.setState(s => {
              const team = s.teams.find(t => t.id === req.requesterTeamId);
              if (team) {
                team.reputationScore = Math.max(0, (team.reputationScore || 100) + penaltyInfo.penalty);
              }
              return s;
            });
          }

          router.closeSheet();
          router.showToast('Đã rút yêu cầu ghép trận', 'info');
          router.navigate('matches');
        } catch (err) {
          router.showToast(err.message, 'error');
        }
      });
    }
  });

  /* =========================================================================
     MATCH-12: Cancel Match Sheet ('match-cancel-sheet')
     ========================================================================= */
  router.registerSheet('match-cancel-sheet', (body, footer, params = {}, titleElem) => {
    if (titleElem) titleElem.textContent = 'Hủy kèo giao lưu';

    const matchId = params.matchId;
    const state = store.getState();
    const match = (state.matches || []).find(m => m.id === matchId);
    const nowClock = state.meta.mockClock;
    const isConfirmed = params.isConfirmed || match?.status === 'CONFIRMED';

    if (!match) {
      body.innerHTML = `<div class="notice notice-danger">Không tìm thấy kèo</div>`;
      footer.innerHTML = `<button class="btn btn-secondary btn-block" onclick="window.AppRouter.closeSheet()">Đóng</button>`;
      return;
    }

    // Check pending requests for Kind A OPEN (BR-M05)
    const pendingReqs = (state.matchRequests || []).filter(r => r.matchId === match.id && r.status === 'PENDING');
    const hasPendingA = match.kind === 'A' && match.status === 'OPEN' && pendingReqs.length > 0;

    const penaltyInfo = isConfirmed ? calculatePenalty(match.matchDate, match.startTime, nowClock) : null;

    body.innerHTML = `
      <div style="padding-bottom: 8px;">
        ${hasPendingA ? `
          <div class="notice notice-warning" style="margin-top: 0;">
            <strong>Chưa thể hủy kèo:</strong> Kèo còn ${pendingReqs.length} yêu cầu ghép trận đang chờ xử lý. Bạn cần từ chối tất cả yêu cầu trước khi hủy bài đăng (BR-M05).
          </div>
        ` : isConfirmed ? `
          <div class="notice notice-danger" style="margin-top: 0; margin-bottom: 12px;">
            <strong>Hủy trận đấu đã xác nhận!</strong>
            <p style="margin: 4px 0 0 0; font-size: 13px;">
              Trận đấu đã được chốt. Hủy trận lúc này sẽ ảnh hưởng tới đối tác và bị trừ điểm uy tín đội bóng.
            </p>
            <div style="margin-top: 6px; font-weight: 700; color: var(--color-error);">
              Mức phạt: ${penaltyInfo?.text || '-5 điểm'}
            </div>
          </div>

          <div class="form-group" style="margin-bottom: 0;">
            <label class="form-label">Lý do hủy trận đấu <span class="required-mark">*</span></label>
            <textarea class="form-textarea" id="cancel-match-reason" placeholder="Ví dụ: Thiếu người, thời tiết xấu..." required></textarea>
          </div>
        ` : `
          <p style="font-size: 14px; color: var(--color-on-surface); margin-top: 0;">
            Bạn có chắc chắn muốn hủy bài đăng kèo này? Kèo sẽ chuyển sang trạng thái ĐÃ HỦY và không nhận thêm yêu cầu.
          </p>
        `}
      </div>
    `;

    footer.innerHTML = `
      <div style="display: flex; gap: 8px; width: 100%;">
        <button class="btn btn-secondary" style="flex: 1;" onclick="window.AppRouter.closeSheet()">Đóng</button>
        ${hasPendingA ? `
          <button class="btn btn-primary" style="flex: 2;" id="cancel-goto-reqs-btn">
            Xem & Từ chối yêu cầu
          </button>
        ` : `
          <button class="btn btn-danger" style="flex: 2;" id="cancel-match-confirm-btn">
            ${isConfirmed ? 'Xác nhận hủy trận' : 'Hủy bài đăng'}
          </button>
        `}
      </div>
    `;

    const gotoReqsBtn = footer.querySelector('#cancel-goto-reqs-btn');
    if (gotoReqsBtn) {
      gotoReqsBtn.addEventListener('click', () => {
        router.closeSheet();
        router.navigate('match-requests-inbox', { matchId: match.id });
      });
    }

    const confirmBtn = footer.querySelector('#cancel-match-confirm-btn');
    if (confirmBtn) {
      confirmBtn.addEventListener('click', () => {
        let reason = 'Đội hủy bài';
        if (isConfirmed) {
          reason = body.querySelector('#cancel-match-reason')?.value.trim();
          if (!reason) {
            router.showToast('Vui lòng nhập lý do hủy trận đấu', 'warning');
            return;
          }
        }

        store.setState(s => {
          const target = s.matches.find(m => m.id === match.id);
          if (target) {
            target.status = 'CANCELLED';
            target.cancelReason = reason;
            target.cancelledAt = s.meta.mockClock;
          }

          // If confirmed, deduct penalty on acting team
          if (isConfirmed && penaltyInfo && !penaltyInfo.blocked) {
            const team = s.teams.find(t => t.id === s.actingTeamId);
            if (team) {
              team.reputationScore = Math.max(0, (team.reputationScore || 100) + penaltyInfo.penalty);
            }
          }

          // Supersede all pairings if Kind B
          if (match.kind === 'B') {
            (s.pairings || []).forEach(p => {
              if (p.matchId === match.id && p.status === 'NEGOTIATING') {
                p.status = 'SUPERSEDED';
              }
            });
          }

          return s;
        });

        router.closeSheet();
        router.showToast('Đã hủy kèo giao lưu', 'info');
        router.navigate('matches');
      });
    }
  });

  /* =========================================================================
     MATCH-14: Cost Agreement Sheet ('match-cost-sheet')
     ========================================================================= */
  router.registerSheet('match-cost-sheet', (body, footer, params = {}, titleElem) => {
    if (titleElem) titleElem.textContent = 'Thỏa thuận chi phí trận đấu';

    const matchId = params.matchId;
    const state = store.getState();
    const match = (state.matches || []).find(m => m.id === matchId);

    if (!match) {
      body.innerHTML = `<div class="notice notice-danger">Không tìm thấy kèo</div>`;
      footer.innerHTML = `<button class="btn btn-secondary btn-block" onclick="window.AppRouter.closeSheet()">Đóng</button>`;
      return;
    }

    body.innerHTML = `
      <div style="padding-bottom: 8px;">
        <div class="form-group">
          <label class="form-label">Khoản tiền sân (VND)</label>
          <input type="number" class="form-input" id="cs-venue-amount" value="${match.totalCost || 600000}" step="50000">
        </div>

        <div class="form-group">
          <label class="form-label">Tiền nước uống dự kiến (VND)</label>
          <input type="number" class="form-input" id="cs-drinks-amount" value="100000" step="20000">
        </div>

        <div class="form-group">
          <label class="form-label">Tỷ lệ chia tiền</label>
          <select class="form-select" id="cs-split-ratio">
            <option value="SPLIT_50_50" ${match.costSharing === 'SPLIT_50_50' ? 'selected' : ''}>Chia đôi 50/50 (Phổ biến)</option>
            <option value="SEVENTY_THIRTY_HOME" ${match.costSharing === 'SEVENTY_THIRTY_HOME' ? 'selected' : ''}>70/30 (Chủ nhà chịu 70%)</option>
            <option value="SEVENTY_THIRTY_LOSER" ${match.costSharing === 'SEVENTY_THIRTY_LOSER' ? 'selected' : ''}>70/30 (Đội thua chịu 70%)</option>
            <option value="LOSER_PAYS_DRINKS" ${match.costSharing === 'LOSER_PAYS_DRINKS' ? 'selected' : ''}>Đội thua trả toàn bộ tiền nước</option>
            <option value="HOST_100" ${match.costSharing === 'HOST_100' ? 'selected' : ''}>Đội chủ nhà bao trọn gói</option>
          </select>
        </div>
      </div>
    `;

    footer.innerHTML = `
      <div style="display: flex; gap: 8px; width: 100%;">
        <button class="btn btn-secondary" style="flex: 1;" onclick="window.AppRouter.closeSheet()">Hủy</button>
        <button class="btn btn-primary" style="flex: 2;" id="cs-save-btn">Lưu thỏa thuận</button>
      </div>
    `;

    const saveBtn = footer.querySelector('#cs-save-btn');
    if (saveBtn) {
      saveBtn.addEventListener('click', () => {
        const vAmt = Number(body.querySelector('#cs-venue-amount')?.value) || 0;
        const dAmt = Number(body.querySelector('#cs-drinks-amount')?.value) || 0;
        const split = body.querySelector('#cs-split-ratio')?.value || 'SPLIT_50_50';

        store.setState(s => {
          const target = s.matches.find(m => m.id === match.id);
          if (target) {
            target.totalCost = vAmt + dAmt;
            target.costSharing = split;
          }
          return s;
        });

        router.closeSheet();
        router.showToast('Đã cập nhật thỏa thuận chi phí', 'success');
        router.navigate('match-detail', { id: match.id });
      });
    }
  });
}
