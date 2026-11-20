/**
 * Pairing Views & Sheets (PAIR-01..08)
 * Owned by @ui-match-pair
 *
 * Implements:
 * - PAIR-01: Negotiations list for creator
 * - PAIR-02: Team's active negotiations list
 * - PAIR-03: Pairing detail (chat context, package, mutual confirmation states)
 * - PAIR-04: Select 1 venue + 1 timeslot sheet (inactive venue disabled)
 * - PAIR-05: Edit cost package & deposit sheet
 * - PAIR-06: Package preview & confirm sheet (triggers match CONFIRMED & other pairings SUPERSEDED)
 * - PAIR-07: Package revision conflict handling (clears both confirms, increments revision)
 * - PAIR-08: Terminal states (CONFIRMED, SUPERSEDED, EXPIRED)
 */

export function initPairingViews(router, store) {
  // Helpers
  const formatCost = (num) => {
    if (!num && num !== 0) return '0 đ';
    return Number(num).toLocaleString('vi-VN') + ' đ';
  };

  const formatCostSplit = (split) => {
    switch (split) {
      case 'SPLIT_50_50': return 'Chia đôi 50/50';
      case 'HOST_100': return 'Chủ nhà bao sân';
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

  const getMatch = (matchId) => {
    const matches = store.getState().matches || [];
    return matches.find(m => m.id === matchId) || null;
  };

  /* =========================================================================
     PAIR-01 & PAIR-02: Negotiations Directory View ('pairings')
     ========================================================================= */
  router.registerView('pairings', (container, params = {}) => {
    const matchId = params.matchId;
    const state = store.getState();
    const actingTeamId = state.actingTeamId;
    const parentMatch = matchId ? getMatch(matchId) : null;

    router.setHeaderTitle(parentMatch ? `Thương lượng: ${parentMatch.matchDate}` : 'Thương lượng kèo B');

    let filterTab = params.tab || 'NEGOTIATING'; // 'ALL', 'NEGOTIATING', 'CONFIRMED', 'TERMINAL'

    function renderList() {
      const allPairings = state.pairings || [];
      let list = allPairings;

      if (matchId) {
        list = list.filter(p => p.matchId === matchId);
      } else {
        // Filter by acting team (either host or guest)
        list = list.filter(p => p.hostTeamId === actingTeamId || p.guestTeamId === actingTeamId);
      }

      // Filter by tab
      const filtered = list.filter(p => {
        if (filterTab === 'ALL') return true;
        if (filterTab === 'NEGOTIATING') return p.status === 'NEGOTIATING';
        if (filterTab === 'CONFIRMED') return p.status === 'CONFIRMED';
        if (filterTab === 'TERMINAL') return p.status === 'SUPERSEDED' || p.status === 'EXPIRED';
        return true;
      });

      container.innerHTML = `
        <div style="padding: 14px 0 24px 0;">
          <div style="margin-bottom: 12px;">
            <h3 style="margin: 0; font-size: 16px; font-weight: 700;">
              ${parentMatch ? `Các đội đang thương lượng (${list.length})` : `Thương lượng của đội (${list.length})`}
            </h3>
            <p style="margin: 2px 0 0 0; font-size: 12px; color: var(--color-on-surface-variant);">
              Mỗi thương lượng có gói phương án riêng. Bên thứ 2 xác nhận sẽ chốt kèo và thay thế các đội còn lại (AC-P04).
            </p>
          </div>

          <!-- Filter Tabs -->
          <div class="chip-group" style="margin-bottom: 14px;">
            <button class="chip ${filterTab === 'NEGOTIATING' ? 'is-active' : ''}" data-tab="NEGOTIATING">Đang đàm phán</button>
            <button class="chip ${filterTab === 'CONFIRMED' ? 'is-active' : ''}" data-tab="CONFIRMED">Đã chốt kèo</button>
            <button class="chip ${filterTab === 'TERMINAL' ? 'is-active' : ''}" data-tab="TERMINAL">Đã kết thúc</button>
            <button class="chip ${filterTab === 'ALL' ? 'is-active' : ''}" data-tab="ALL">Tất cả</button>
          </div>

          ${filtered.length === 0 ? `
            <div class="empty-state">
              <h4 class="empty-state-title">Chưa có thương lượng nào</h4>
              <p class="empty-state-desc">Chưa có đội bóng nào tham gia thương lượng trong danh mục này.</p>
              ${parentMatch ? `
                <button class="btn btn-secondary" onclick="window.AppRouter.navigate('match-detail', { id: '${parentMatch.id}' })">
                  Quay lại kèo đấu
                </button>
              ` : `
                <button class="btn btn-primary" onclick="window.AppRouter.navigate('matches', { tab: 'KIND_B' })">
                  Xem các bài đăng Luồng B
                </button>
              `}
            </div>
          ` : `
            <div class="list-group">
              ${filtered.map(p => {
                const host = getTeam(p.hostTeamId);
                const guest = getTeam(p.guestTeamId);
                const match = getMatch(p.matchId);
                const venue = p.package?.venueId ? getVenue(p.package.venueId) : null;
                const isHost = p.hostTeamId === actingTeamId;
                const partnerTeam = isHost ? guest : host;

                const hostConf = p.package?.confirms?.[p.hostTeamId];
                const guestConf = p.package?.confirms?.[p.guestTeamId];

                return `
                  <div class="card card-interactive pairing-card-item" data-id="${p.id}" style="margin-bottom: 12px; padding: 14px;">
                    <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 6px;">
                      <div>
                        <div style="font-size: 15px; font-weight: 700; color: var(--color-on-surface);">
                          ${partnerTeam?.name || 'Đội đối tác'}
                        </div>
                        <div style="font-size: 12px; color: var(--color-on-surface-variant);">
                          Vai trò của bạn: <strong>${isHost ? 'Chủ bài đăng (Host)' : 'Đội nhận kèo'}</strong>
                        </div>
                      </div>

                      <span class="badge ${p.status === 'CONFIRMED' ? 'badge-green' : p.status === 'NEGOTIATING' ? 'badge-amber' : 'badge-gray'}">
                        ${p.status === 'CONFIRMED' ? 'ĐÃ GHÉP KÈO' :
                          p.status === 'NEGOTIATING' ? 'ĐANG ĐÀM PHÁN' :
                          p.status === 'SUPERSEDED' ? 'ĐÃ THAY THẾ' : 'HẾT HẠN'}
                      </span>
                    </div>

                    <!-- Package Summary -->
                    <div style="background: var(--color-surface-container-low); padding: 8px 10px; border-radius: var(--radius-base); margin: 8px 0; font-size: 13px;">
                      <div style="display: flex; justify-content: space-between; margin-bottom: 4px;">
                        <span>📍 Sân: <strong>${venue ? venue.name : '(Chưa chốt)'}</strong></span>
                        <span class="badge badge-gray" style="font-size: 10px;">Phiên bản #${p.package?.revision || 1}</span>
                      </div>
                      <div style="color: var(--color-on-surface-variant); font-size: 12px;">
                        ⏰ Giờ: <strong>${p.package?.timeSlot || '(Chưa chốt)'}</strong> • Tổng: ${formatCost(p.package?.totalCost)}
                      </div>
                    </div>

                    <!-- Confirmations Status Pill -->
                    <div style="display: flex; gap: 8px; font-size: 12px; margin-top: 6px;">
                      <span style="color: ${hostConf ? 'var(--color-primary)' : 'var(--color-outline)'};">
                        ${hostConf ? '✓ Đội chủ nhà đã duyệt' : '○ Đội chủ nhà chưa duyệt'}
                      </span>
                      <span>•</span>
                      <span style="color: ${guestConf ? 'var(--color-primary)' : 'var(--color-outline)'};">
                        ${guestConf ? '✓ Đội khách đã duyệt' : '○ Đội khách chưa duyệt'}
                      </span>
                    </div>
                  </div>
                `;
              }).join('')}
            </div>
          `}
        </div>
      `;

      // Handlers
      container.querySelectorAll('.chip[data-tab]').forEach(chip => {
        chip.addEventListener('click', () => {
          filterTab = chip.getAttribute('data-tab');
          renderList();
        });
      });

      container.querySelectorAll('.pairing-card-item').forEach(card => {
        card.addEventListener('click', () => {
          const pId = card.getAttribute('data-id');
          router.navigate('pairing-detail', { id: pId });
        });
      });
    }

    renderList();
  });

  /* =========================================================================
     PAIR-03: Pairing Detail View ('pairing-detail')
     ========================================================================= */
  router.registerView('pairing-detail', (container, params = {}) => {
    const pairingId = params.id;
    const state = store.getState();
    const currentUserId = state.currentUserId;
    const actingTeamId = state.actingTeamId;
    const pairing = (state.pairings || []).find(p => p.id === pairingId);

    if (!pairing) {
      router.setHeaderTitle('Chi tiết gói thương lượng');
      container.innerHTML = `
        <div class="empty-state">
          <h3 class="empty-state-title">Không tìm thấy thương lượng</h3>
          <p class="empty-state-desc">Phiên thương lượng này không tồn tại hoặc đã kết thúc.</p>
          <button class="btn btn-primary" onclick="window.AppRouter.navigate('pairings')">Danh sách thương lượng</button>
        </div>
      `;
      return;
    }

    const host = getTeam(pairing.hostTeamId);
    const guest = getTeam(pairing.guestTeamId);
    const parentMatch = getMatch(pairing.matchId);
    const pkg = pairing.package || {};
    const venue = pkg.venueId ? getVenue(pkg.venueId) : null;
    const isInactiveVenue = venue && venue.status === 'INACTIVE';

    router.setHeaderTitle(`Thương lượng: ${host?.name} - ${guest?.name}`);

    // Context & Status
    const isHost = pairing.hostTeamId === actingTeamId;
    const isGuest = pairing.guestTeamId === actingTeamId;
    const isNegotiating = pairing.status === 'NEGOTIATING';
    const isConfirmed = pairing.status === 'CONFIRMED';
    const isSuperseded = pairing.status === 'SUPERSEDED';
    const isExpired = pairing.status === 'EXPIRED';

    const hostConfirmed = pkg.confirms?.[pairing.hostTeamId] || false;
    const guestConfirmed = pkg.confirms?.[pairing.guestTeamId] || false;
    const mySideConfirmed = isHost ? hostConfirmed : guestConfirmed;
    const otherSideConfirmed = isHost ? guestConfirmed : hostConfirmed;

    // Check parent match options
    const matchVenues = (parentMatch?.proposedVenues || []).map(vid => getVenue(vid)).filter(Boolean);
    const matchSlots = parentMatch?.proposedTimeSlots || [];

    container.innerHTML = `
      <div style="padding: 14px 0 28px 0;">
        <!-- Terminal Status Notices -->
        ${isConfirmed ? `
          <div class="notice notice-info" style="margin-top: 0; margin-bottom: 14px;">
            <strong>KÈO ĐÃ ĐƯỢC GHÉP THÀNH CÔNG!</strong> Cả hai bên đã đồng thuận gói phương án. Trận đấu đã được chuyển sang trạng thái CONFIRMED.
          </div>
        ` : ''}

        ${isSuperseded ? `
          <div class="notice notice-warning" style="margin-top: 0; margin-bottom: 14px;">
            <strong>THƯƠNG LƯỢNG ĐÃ KẾT THÚC (SUPERSEDED):</strong> Kèo đấu đã được chốt với một đội bóng khác hoặc đội chủ nhà đã hủy bài đăng.
          </div>
        ` : ''}

        ${isExpired ? `
          <div class="notice notice-warning" style="margin-top: 0; margin-bottom: 14px;">
            <strong>HẾT HẠN THƯƠNG LƯỢNG:</strong> Bài đăng đã quá thời hạn tìm đối mà hai bên chưa hoàn tất xác nhận gói.
          </div>
        ` : ''}

        ${isInactiveVenue ? `
          <div class="notice notice-danger" style="margin-top: 0; margin-bottom: 14px;">
            <strong>SÂN ĐÃ NGỪNG HOẠT ĐỘNG:</strong> Địa điểm <strong>${venue.name}</strong> đã bị đánh dấu ngừng hoạt động. Hai bên cần chốt lại địa điểm khác trước khi có thể xác nhận (VENUE_002 / PAIR-04).
          </div>
        ` : ''}

        <!-- Matchup Header Card -->
        <div class="card" style="margin-bottom: 14px;">
          <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px;">
            <span class="badge ${isNegotiating ? 'badge-amber' : isConfirmed ? 'badge-green' : 'badge-gray'}">
              ${isNegotiating ? 'ĐANG ĐÀM PHÁN' : isConfirmed ? 'ĐÃ HOÀN TẤT GHÉP' : 'KẾT THÚC'}
            </span>
            <span class="badge badge-gray">Gói phiên bản #${pkg.revision || 1}</span>
          </div>

          <div style="display: flex; align-items: center; justify-content: space-between; text-align: center; gap: 8px;">
            <div style="flex: 1;">
              <div style="font-weight: 700; font-size: 15px;">${host?.name || 'Đội chủ nhà'}</div>
              <div style="font-size: 11px; color: var(--color-on-surface-variant);">Chủ bài đăng</div>
              <span class="badge ${hostConfirmed ? 'badge-green' : 'badge-gray'}" style="margin-top: 4px; font-size: 11px;">
                ${hostConfirmed ? '✓ Đã duyệt gói' : '○ Chưa duyệt'}
              </span>
            </div>

            <div style="font-weight: 800; color: var(--color-primary); font-size: 16px;">🤝</div>

            <div style="flex: 1;">
              <div style="font-weight: 700; font-size: 15px;">${guest?.name || 'Đội nhận kèo'}</div>
              <div style="font-size: 11px; color: var(--color-on-surface-variant);">Đội nhận kèo</div>
              <span class="badge ${guestConfirmed ? 'badge-green' : 'badge-gray'}" style="margin-top: 4px; font-size: 11px;">
                ${guestConfirmed ? '✓ Đã duyệt gói' : '○ Chưa duyệt'}
              </span>
            </div>
          </div>
        </div>

        <!-- Package Details Card -->
        <div class="card" style="margin-bottom: 14px;">
          <div class="card-header">
            <h4 class="card-title" style="font-size: 15px;">Gói phương án đề xuất</h4>
            ${isNegotiating ? `
              <span style="font-size: 11px; color: var(--color-primary); font-weight: 600;">Có thể sửa đổi</span>
            ` : ''}
          </div>

          <div class="list-group">
            <!-- 1. Venue & Slot Selection -->
            <div class="list-item" style="flex-direction: column; align-items: flex-start; gap: 4px;">
              <div style="display: flex; justify-content: space-between; width: 100%; align-items: center;">
                <span style="font-size: 12px; color: var(--color-on-surface-variant);">1. Địa điểm sân đã chốt:</span>
                ${isNegotiating ? `
                  <button class="btn btn-ghost" id="pairing-edit-proposal-btn" style="padding: 2px 8px; font-size: 12px; color: var(--color-primary);">
                    ✏️ Đổi sân & giờ
                  </button>
                ` : ''}
              </div>
              <div style="font-weight: 700; font-size: 14px; color: var(--color-on-surface);">
                ${venue ? venue.name : '<span style="color: var(--color-error); font-weight: 500;">Chưa chọn sân (Bắt buộc chọn 1 sân từ bài đăng)</span>'}
              </div>
              ${venue ? `<div style="font-size: 12px; color: var(--color-on-surface-variant);">${venue.address}</div>` : ''}
              ${isInactiveVenue ? '<span class="badge badge-red" style="margin-top: 2px;">SÂN NGỪNG HOẠT ĐỘNG</span>' : ''}
            </div>

            <!-- 2. Time Slot -->
            <div class="list-item">
              <div>
                <div style="font-size: 12px; color: var(--color-on-surface-variant);">2. Khung giờ thi đấu:</div>
                <div style="font-weight: 700; font-size: 14px; color: var(--color-on-surface);">
                  ${pkg.timeSlot ? `⏰ ${pkg.timeSlot}` : '<span style="color: var(--color-error); font-weight: 500;">Chưa chọn khung giờ</span>'}
                </div>
              </div>
            </div>

            <!-- 3. Cost & Deposit -->
            <div class="list-item" style="flex-direction: column; align-items: flex-start; gap: 4px;">
              <div style="display: flex; justify-content: space-between; width: 100%; align-items: center;">
                <span style="font-size: 12px; color: var(--color-on-surface-variant);">3. Chi phí & Tiền cọc:</span>
                ${isNegotiating ? `
                  <button class="btn btn-ghost" id="pairing-edit-cost-btn" style="padding: 2px 8px; font-size: 12px; color: var(--color-primary);">
                    ✏️ Sửa chi phí
                  </button>
                ` : ''}
              </div>
              <div style="display: flex; justify-content: space-between; width: 100%; font-size: 14px;">
                <span>Tổng chi phí:</span>
                <strong style="color: var(--color-primary-dark); font-size: 15px;">${formatCost(pkg.totalCost)}</strong>
              </div>
              <div style="display: flex; justify-content: space-between; width: 100%; font-size: 12px; color: var(--color-on-surface-variant);">
                <span>Phương thức chia:</span>
                <span>${formatCostSplit(pkg.costSharing)}</span>
              </div>
              ${pkg.depositAmount ? `
                <div style="display: flex; justify-content: space-between; width: 100%; font-size: 12px; color: var(--color-warning); margin-top: 2px;">
                  <span>Tiền cọc giữ chân:</span>
                  <strong>${formatCost(pkg.depositAmount)} (${pkg.depositHolder === 'HOST' ? 'Đội chủ nhà' : 'Đội khách'})</strong>
                </div>
              ` : ''}
            </div>
          </div>
        </div>

        <!-- Integrated Chat Shortcut -->
        <div class="card" style="padding: 12px; margin-bottom: 16px; background-color: var(--color-surface-container-low);">
          <div style="display: flex; align-items: center; justify-content: space-between;">
            <div style="display: flex; align-items: center; gap: 8px;">
              <span style="font-size: 20px;">💬</span>
              <div>
                <div style="font-weight: 600; font-size: 14px;">Kênh chat hai đội trưởng</div>
                <div style="font-size: 12px; color: var(--color-on-surface-variant);">Trao đổi màu áo, nội quy và thống nhất phương án</div>
              </div>
            </div>
            <button class="btn btn-secondary" id="pairing-chat-btn" style="min-height: 36px; padding: 6px 12px; font-size: 13px;">
              Mở chat
            </button>
          </div>
        </div>

        <!-- Actions -->
        <div style="display: flex; flex-direction: column; gap: 10px;">
          ${isNegotiating ? `
            <!-- Review & Confirm Button -->
            <button class="btn btn-primary btn-block" id="pairing-review-confirm-btn" style="min-height: 48px;">
              ${mySideConfirmed ? '✓ Bạn đã xác nhận gói (Đang chờ đối phương)' : '✓ Xem lại & Xác nhận gói này'}
            </button>
          ` : ''}

          ${isConfirmed ? `
            <button class="btn btn-primary btn-block" onclick="window.AppRouter.navigate('match-detail', { id: '${parentMatch?.id}' })">
              Xem chi tiết trận đấu đã ghép
            </button>
          ` : ''}

          <button class="btn btn-secondary btn-block" onclick="window.AppRouter.navigate('pairings')">
            Về danh sách thương lượng
          </button>
        </div>
      </div>
    `;

    // Handlers
    const editPropBtn = container.querySelector('#pairing-edit-proposal-btn');
    if (editPropBtn) {
      editPropBtn.addEventListener('click', () => {
        router.openSheet('pairing-venue-time-sheet', { pairingId: pairing.id });
      });
    }

    const editCostBtn = container.querySelector('#pairing-edit-cost-btn');
    if (editCostBtn) {
      editCostBtn.addEventListener('click', () => {
        router.openSheet('pairing-cost-sheet', { pairingId: pairing.id });
      });
    }

    const reviewConfirmBtn = container.querySelector('#pairing-review-confirm-btn');
    if (reviewConfirmBtn) {
      reviewConfirmBtn.addEventListener('click', () => {
        router.openSheet('pairing-review-sheet', { pairingId: pairing.id });
      });
    }

    const chatBtn = container.querySelector('#pairing-chat-btn');
    if (chatBtn) {
      chatBtn.addEventListener('click', () => {
        router.showToast('Mở cuộc trò chuyện hai đội trưởng', 'info');
        router.navigate('inbox');
      });
    }
  });

  /* =========================================================================
     PAIR-04: Select 1 Venue & 1 Timeslot Sheet ('pairing-venue-time-sheet')
     ========================================================================= */
  router.registerSheet('pairing-venue-time-sheet', (body, footer, params = {}, titleElem) => {
    if (titleElem) titleElem.textContent = 'Chốt sân và khung giờ (PAIR-04)';

    const pairingId = params.pairingId;
    const state = store.getState();
    const pairing = (state.pairings || []).find(p => p.id === pairingId);
    const parentMatch = pairing ? getMatch(pairing.matchId) : null;

    if (!pairing || !parentMatch) {
      body.innerHTML = `<div class="notice notice-danger">Không tìm thấy dữ liệu thương lượng</div>`;
      footer.innerHTML = `<button class="btn btn-secondary btn-block" onclick="window.AppRouter.closeSheet()">Đóng</button>`;
      return;
    }

    const proposedVenues = (parentMatch.proposedVenues || []).map(vid => getVenue(vid)).filter(Boolean);
    const proposedSlots = parentMatch.proposedTimeSlots || [];
    const curVenueId = pairing.package?.venueId;
    const curSlot = pairing.package?.timeSlot;

    body.innerHTML = `
      <div style="padding-bottom: 8px;">
        <div class="notice notice-info" style="margin-top: 0; margin-bottom: 12px;">
          <strong>Quy tắc gói:</strong> Khi thay đổi sân hoặc giờ, xác nhận của cả hai bên sẽ bị xóa và gói sẽ được nâng lên số phiên bản (revision) mới (AC-P04).
        </div>

        <!-- Venue Selector -->
        <div class="form-group">
          <label class="form-label">Chọn 1 sân bóng từ đề xuất bài đăng:</label>
          <div style="display: flex; flex-direction: column; gap: 6px; margin-top: 4px;">
            ${proposedVenues.map(v => {
              const isInactive = v.status === 'INACTIVE';
              return `
                <label style="display: flex; align-items: center; justify-content: space-between; padding: 8px 10px; border: 1.5px solid var(--color-outline-variant); border-radius: var(--radius-base); cursor: ${isInactive ? 'not-allowed' : 'pointer'}; background: ${isInactive ? '#fef2f2' : 'var(--color-surface)'};">
                  <div style="display: flex; align-items: center; gap: 8px;">
                    <input type="radio" name="p-venue-choice" value="${v.id}"
                      ${v.id === curVenueId ? 'checked' : ''} ${isInactive ? 'disabled' : ''}>
                    <div>
                      <div style="font-weight: 600; font-size: 13px;">${v.name}</div>
                      <div style="font-size: 11px; color: var(--color-on-surface-variant);">${v.address}</div>
                    </div>
                  </div>
                  ${isInactive ? '<span class="badge badge-red" style="font-size: 10px;">NGỪNG HOẠT ĐỘNG</span>' : ''}
                </label>
              `;
            }).join('')}
          </div>
        </div>

        <!-- Slot Selector -->
        <div class="form-group" style="margin-bottom: 0;">
          <label class="form-label">Chọn 1 khung giờ thi đấu:</label>
          <div style="display: flex; flex-direction: column; gap: 6px; margin-top: 4px;">
            ${proposedSlots.map(slot => `
              <label style="display: flex; align-items: center; gap: 8px; padding: 8px 10px; border: 1.5px solid var(--color-outline-variant); border-radius: var(--radius-base); cursor: pointer;">
                <input type="radio" name="p-slot-choice" value="${slot}" ${slot === curSlot ? 'checked' : ''}>
                <span style="font-weight: 600; font-size: 13px;">⏰ ${slot}</span>
              </label>
            `).join('')}
          </div>
        </div>
      </div>
    `;

    footer.innerHTML = `
      <div style="display: flex; gap: 8px; width: 100%;">
        <button class="btn btn-secondary" style="flex: 1;" onclick="window.AppRouter.closeSheet()">Hủy</button>
        <button class="btn btn-primary" style="flex: 2;" id="p-save-proposal-btn">
          Lưu phương án & Xóa xác nhận cũ
        </button>
      </div>
    `;

    const saveProposalBtn = footer.querySelector('#p-save-proposal-btn');
    if (saveProposalBtn) {
      saveProposalBtn.addEventListener('click', () => {
        const chosenVenue = body.querySelector('input[name="p-venue-choice"]:checked')?.value;
        const chosenSlot = body.querySelector('input[name="p-slot-choice"]:checked')?.value;

        if (!chosenVenue || !chosenSlot) {
          router.showToast('Vui lòng chọn 1 sân hoạt động và 1 khung giờ', 'warning');
          return;
        }

        try {
          // AC-P04: updates proposal, increments revision, clears both confirms
          store.updatePairingPackage(pairing.id, {
            venueId: chosenVenue,
            timeSlot: chosenSlot
          });

          router.closeSheet();
          router.showToast('Đã chốt phương án mới (Xóa xác nhận của cả hai bên)', 'success');
          router.navigate('pairing-detail', { id: pairing.id });
        } catch (err) {
          router.showToast(err.message, 'error');
        }
      });
    }
  });

  /* =========================================================================
     PAIR-05: Edit Cost Package Sheet ('pairing-cost-sheet')
     ========================================================================= */
  router.registerSheet('pairing-cost-sheet', (body, footer, params = {}, titleElem) => {
    if (titleElem) titleElem.textContent = 'Thỏa thuận chi phí & Cọc (PAIR-05)';

    const pairingId = params.pairingId;
    const state = store.getState();
    const pairing = (state.pairings || []).find(p => p.id === pairingId);
    const pkg = pairing?.package || {};

    body.innerHTML = `
      <div style="padding-bottom: 8px;">
        <div class="notice notice-info" style="margin-top: 0; margin-bottom: 12px;">
          Sửa đổi chi phí sẽ tự động cập nhật số phiên bản revision và yêu cầu hai bên duyệt lại (AC-P04).
        </div>

        <div class="form-group">
          <label class="form-label">Tổng tiền sân và nước dự kiến (VND)</label>
          <input type="number" class="form-input" id="pc-total-cost" value="${pkg.totalCost || 600000}" step="50000">
        </div>

        <div class="form-group">
          <label class="form-label">Tỷ lệ chia tiền</label>
          <select class="form-select" id="pc-split">
            <option value="SPLIT_50_50" ${pkg.costSharing === 'SPLIT_50_50' ? 'selected' : ''}>Chia đôi 50/50</option>
            <option value="HOST_100" ${pkg.costSharing === 'HOST_100' ? 'selected' : ''}>Chủ nhà bao sân</option>
            <option value="SEVENTY_THIRTY_LOSER" ${pkg.costSharing === 'SEVENTY_THIRTY_LOSER' ? 'selected' : ''}>70/30 (Đội thua 70%)</option>
            <option value="LOSER_PAYS_DRINKS" ${pkg.costSharing === 'LOSER_PAYS_DRINKS' ? 'selected' : ''}>Đội thua trả nước</option>
          </select>
        </div>

        <div class="form-group">
          <label class="form-label">Tiền cọc giữ chân (VND)</label>
          <input type="number" class="form-input" id="pc-deposit" value="${pkg.depositAmount || 0}" step="50000">
        </div>

        <div class="form-group" style="margin-bottom: 0;">
          <label class="form-label">Bên giữ tiền cọc</label>
          <select class="form-select" id="pc-deposit-holder">
            <option value="HOST">Đội chủ nhà giữ cọc</option>
            <option value="GUEST">Đội nhận kèo giữ cọc</option>
            <option value="VENUE">Chuyển cọc trực tiếp cho chủ sân</option>
          </select>
        </div>
      </div>
    `;

    footer.innerHTML = `
      <div style="display: flex; gap: 8px; width: 100%;">
        <button class="btn btn-secondary" style="flex: 1;" onclick="window.AppRouter.closeSheet()">Hủy</button>
        <button class="btn btn-primary" style="flex: 2;" id="pc-save-btn">
          Lưu chi phí & Tăng revision
        </button>
      </div>
    `;

    const saveCostBtn = footer.querySelector('#pc-save-btn');
    if (saveCostBtn) {
      saveCostBtn.addEventListener('click', () => {
        const total = Number(body.querySelector('#pc-total-cost')?.value) || 600000;
        const split = body.querySelector('#pc-split')?.value || 'SPLIT_50_50';
        const deposit = Number(body.querySelector('#pc-deposit')?.value) || 0;
        const holder = body.querySelector('#pc-deposit-holder')?.value || 'HOST';

        try {
          store.updatePairingPackage(pairing.id, {
            totalCost: total,
            costSharing: split,
            depositAmount: deposit,
            depositHolder: holder
          });

          router.closeSheet();
          router.showToast('Đã lưu chi phí mới (Xóa xác nhận của hai bên)', 'success');
          router.navigate('pairing-detail', { id: pairing.id });
        } catch (err) {
          router.showToast(err.message, 'error');
        }
      });
    }
  });

  /* =========================================================================
     PAIR-06: Package Preview & Confirm Sheet ('pairing-review-sheet')
     ========================================================================= */
  router.registerSheet('pairing-review-sheet', (body, footer, params = {}, titleElem) => {
    if (titleElem) titleElem.textContent = 'Xem lại & Xác nhận gói (PAIR-06)';

    const pairingId = params.pairingId;
    const state = store.getState();
    const actingTeamId = state.actingTeamId;
    const pairing = (state.pairings || []).find(p => p.id === pairingId);
    const pkg = pairing?.package || {};
    const venue = pkg.venueId ? getVenue(pkg.venueId) : null;
    const isInactiveVenue = venue && venue.status === 'INACTIVE';

    if (!pairing) {
      body.innerHTML = `<div class="notice notice-danger">Không tìm thấy thương lượng</div>`;
      footer.innerHTML = `<button class="btn btn-secondary btn-block" onclick="window.AppRouter.closeSheet()">Đóng</button>`;
      return;
    }

    const hostConfirmed = pkg.confirms?.[pairing.hostTeamId] || false;
    const guestConfirmed = pkg.confirms?.[pairing.guestTeamId] || false;
    const isHost = pairing.hostTeamId === actingTeamId;
    const mySideConfirmed = isHost ? hostConfirmed : guestConfirmed;
    const otherSideConfirmed = isHost ? guestConfirmed : hostConfirmed;

    const canConfirm = pkg.venueId && pkg.timeSlot && !isInactiveVenue;

    body.innerHTML = `
      <div style="padding-bottom: 8px;">
        ${!canConfirm ? `
          <div class="notice notice-danger" style="margin-top: 0; margin-bottom: 12px;">
            ${isInactiveVenue ? 'Sân bóng đã chọn bị ngừng hoạt động. Phải chốt lại sân khác.' : 'Chưa chọn đầy đủ 1 sân và 1 khung giờ. Không thể xác nhận gói (422 PAIRING_007).'}
          </div>
        ` : `
          <div class="notice notice-info" style="margin-top: 0; margin-bottom: 12px;">
            ${otherSideConfirmed ? '🔥 Bên đối tác ĐÃ XÁC NHẬN! Khi bạn bấm xác nhận, kèo đấu sẽ chính thức được chốt (CONFIRMED)!' :
              'Khi bạn bấm xác nhận, hệ thống sẽ ghi nhận và chờ bên đối tác xác nhận cùng phiên bản này.'}
          </div>
        `}

        <div class="card" style="padding: 10px; margin-bottom: 8px;">
          <div style="font-size: 11px; color: var(--color-on-surface-variant); font-weight: 700;">
            GÓI PHƯƠNG ÁN (PHIÊN BẢN #${pkg.revision || 1})
          </div>
          <div style="font-weight: 700; font-size: 15px; margin-top: 4px;">
            📍 ${venue ? venue.name : '(Chưa chốt sân)'}
          </div>
          <div style="font-size: 13px; font-weight: 600; color: var(--color-primary-dark); margin-top: 2px;">
            ⏰ Khung giờ: ${pkg.timeSlot || '(Chưa chốt giờ)'}
          </div>
          <div style="font-size: 13px; margin-top: 6px;">
            Chi phí: <strong>${formatCost(pkg.totalCost)}</strong> (${formatCostSplit(pkg.costSharing)})
          </div>
        </div>

        <div style="display: flex; justify-content: space-between; font-size: 12px; padding: 0 4px;">
          <span>Chủ nhà: <strong>${hostConfirmed ? '✓ Đã duyệt' : '○ Chưa duyệt'}</strong></span>
          <span>Đội nhận: <strong>${guestConfirmed ? '✓ Đã duyệt' : '○ Chưa duyệt'}</strong></span>
        </div>
      </div>
    `;

    footer.innerHTML = `
      <div style="display: flex; gap: 8px; width: 100%;">
        <button class="btn btn-secondary" style="flex: 1;" onclick="window.AppRouter.closeSheet()">Đóng</button>
        <button class="btn btn-primary" style="flex: 2;" id="p-confirm-action-btn" ${!canConfirm ? 'disabled' : ''}>
          ${mySideConfirmed ? 'Bạn đã xác nhận' : 'Xác nhận gói này'}
        </button>
      </div>
    `;

    const confirmBtn = footer.querySelector('#p-confirm-action-btn');
    if (confirmBtn) {
      confirmBtn.addEventListener('click', () => {
        try {
          const updatedPairing = store.confirmPairingPackage(pairing.id, actingTeamId);
          router.closeSheet();

          if (updatedPairing.status === 'CONFIRMED') {
            router.showToast('Chúc mừng! Cả hai bên đã xác nhận. Kèo đã ghép thành công (CONFIRMED)!', 'success');
            router.navigate('match-detail', { id: updatedPairing.matchId });
          } else {
            router.showToast('Bạn đã xác nhận gói. Đang chờ đối tác xác nhận.', 'info');
            router.navigate('pairing-detail', { id: pairing.id });
          }
        } catch (err) {
          router.showToast(err.message, 'error');
        }
      });
    }
  });
}
