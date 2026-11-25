/**
 * ArenaFlow Match B Negotiation / Pairing Screens (PAIR-01..08)
 * Classic non-module script compatible with file:// protocol.
 * Depends on window.ArenaStore, window.ArenaUI, window.ArenaRouter.
 */
(function (global) {
  'use strict';

  const COST_SPLIT_LABELS = {
    SPLIT_50_50: 'Chia đôi 50/50',
    HOST_100: 'Chủ nhà bao sân',
    SEVENTY_THIRTY_HOME: '70/30 (Chủ nhà 70%)',
    SEVENTY_THIRTY_LOSER: '70/30 (Đội thua 70%)',
    LOSER_PAYS_DRINKS: 'Đội thua trả nước'
  };

  const PAIR_STATUS_META = {
    NEGOTIATING: { label: 'Đang thương lượng', variant: 'info' },
    CONFIRMED: { label: 'Đã chốt kèo', variant: 'success' },
    SUPERSEDED: { label: 'Đã thay thế', variant: 'neutral' },
    CANCELLED: { label: 'Đã hủy', variant: 'error' },
    EXPIRED: { label: 'Hết hạn', variant: 'warning' }
  };

  function formatMoney(n) {
    return Number(n || 0).toLocaleString('vi-VN') + ' đ';
  }

  function formatDateVi(dateStr) {
    if (!dateStr) return '—';
    const p = dateStr.split('-');
    return p.length === 3 ? `${p[2]}/${p[1]}/${p[0]}` : dateStr;
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

  function getPairing(id) {
    return global.ArenaStore.findById('pairings', id);
  }

  function renderPairingList(container, mode, params) {
    const store = global.ArenaStore;
    const ui = global.ArenaUI;
    const acting = store.getActingTeam();
    const allPairings = store.getState().pairings || [];
    const matchId = params.matchId || null;
    let tab = params.tab || 'NEGOTIATING';

    function render() {
      let scoped = allPairings.filter((p) => {
        if (matchId && p.matchId !== matchId) return false;
        if (!acting) return true;
        if (mode === 'HOST') return p.hostTeamId === acting.id;
        return p.guestTeamId === acting.id || p.hostTeamId === acting.id;
      });

      const filtered = scoped.filter((p) => {
        if (tab === 'ALL') return true;
        if (tab === 'NEGOTIATING') return p.status === 'NEGOTIATING';
        if (tab === 'CONFIRMED') return p.status === 'CONFIRMED';
        return p.status === 'SUPERSEDED' || p.status === 'CANCELLED' || p.status === 'EXPIRED';
      });

      container.innerHTML = `
        <div class="w-full flex-1 flex flex-col bg-[#F8FAF3] p-4 space-y-3">
          <div class="bg-[#EAF0E6] border border-[#DDE5D9] rounded-[8px] p-3 text-xs text-[#14532D] flex items-start gap-2">
            <span class="material-symbols-outlined text-[18px] shrink-0">lock</span>
            <span>
              ${mode === 'HOST'
                ? 'Danh sách phòng thương lượng riêng với từng đội (PAIR-01). Các đội khác không nhìn thấy nội dung phòng của nhau.'
                : 'Các phòng thương lượng Kèo B mà đội bạn đang tham gia (PAIR-02).'}
            </span>
          </div>

          <div class="flex gap-1.5 overflow-x-auto no-scrollbar">
            ${[
              { k: 'NEGOTIATING', l: 'Đang đàm phán' },
              { k: 'CONFIRMED', l: 'Đã chốt' },
              { k: 'TERMINAL', l: 'Đã kết thúc' },
              { k: 'ALL', l: 'Tất cả' }
            ].map((t) => `
              <button type="button" data-p-tab="${t.k}"
                      class="px-3 py-1.5 rounded-full text-xs font-semibold border whitespace-nowrap ${tab === t.k ? 'bg-[#3D6943] text-white border-[#3D6943]' : 'bg-white text-[#596158] border-[#DDE5D9]'}">
                ${t.l}
              </button>
            `).join('')}
          </div>

          <div class="space-y-2.5">
            ${filtered.length === 0 ? ui.emptyState({
              icon: 'handshake',
              title: 'Chưa có phòng thương lượng',
              description: 'Chưa có đội bóng nào trong mục này.',
              actionLabel: 'Khám phá Kèo B',
              onAction: "window.ArenaRouter.navigate('MATCH-01')"
            }) : filtered.map((p) => {
              const host = getTeam(p.hostTeamId);
              const guest = getTeam(p.guestTeamId);
              const match = getMatch(p.matchId);
              const venue = getVenue(p.package && p.package.venueId);
              const meta = PAIR_STATUS_META[p.status] || PAIR_STATUS_META.NEGOTIATING;
              const confirms = (p.package && p.package.confirms) || {};
              const confirmCount = (confirms[p.hostTeamId] ? 1 : 0) + (confirms[p.guestTeamId] ? 1 : 0);

              return `
                <article data-open-pair="${ui.esc(p.id)}"
                         class="bg-white rounded-[8px] border border-[#DDE5D9] p-3.5 space-y-2.5 cursor-pointer hover:border-[#3D6943]">
                  <div class="flex items-center justify-between gap-2">
                    <div class="flex items-center gap-2.5 min-w-0">
                      ${ui.avatar(mode === 'HOST' ? (guest || { name: 'Đối thủ' }) : (host || { name: 'Chủ kèo' }), { size: 38 })}
                      <div class="min-w-0">
                        <div class="font-bold text-sm text-[#111827] truncate">
                          ${ui.esc(host ? host.name : '')} × ${ui.esc(guest ? guest.name : '')}
                        </div>
                        <div class="text-[11px] text-[#596158]">
                          ${match ? formatDateVi(match.matchDate) : ''} • Rev #${(p.package && p.package.revision) || 1}
                        </div>
                      </div>
                    </div>
                    ${ui.badge(meta.label, meta.variant)}
                  </div>

                  <div class="bg-[#F8FAF3] rounded-[8px] p-2.5 border border-[#EAF0E6] text-xs text-[#2D342C] flex items-center justify-between gap-2">
                    <div class="truncate">
                      <span class="font-semibold">${ui.esc(venue ? venue.name : 'Chưa chọn sân')}</span>
                      <span class="text-[#596158]"> • ${ui.esc((p.package && p.package.timeSlot) || '—')}</span>
                    </div>
                    <span class="font-semibold text-[#3D6943] shrink-0">${confirmCount}/2 xác nhận</span>
                  </div>
                </article>
              `;
            }).join('')}
          </div>
        </div>
      `;

      container.querySelectorAll('[data-p-tab]').forEach((b) => {
        b.onclick = () => { tab = b.getAttribute('data-p-tab'); render(); };
      });
      container.querySelectorAll('[data-open-pair]').forEach((el) => {
        el.onclick = () => global.ArenaRouter.navigate('PAIR-03', { id: el.getAttribute('data-open-pair') });
      });
    }
    render();
  }

  /* =========================================================================
     PAIR-04: Chọn 1 sân & 1 giờ Sheet
     ========================================================================= */
  function openSelectVenueSlotSheet(pairingId) {
    const store = global.ArenaStore;
    const ui = global.ArenaUI;
    const pairing = getPairing(pairingId);
    if (!pairing || pairing.status !== 'NEGOTIATING') {
      ui.toast('Phòng thương lượng đã kết thúc, không thể sửa.', 'warning');
      return;
    }
    const match = getMatch(pairing.matchId);
    const allVenues = store.getState().venues || [];
    const proposedIds = (match && match.proposedVenues && match.proposedVenues.length) ? match.proposedVenues : allVenues.map((v) => v.id);
    const venueOptions = proposedIds.map((id) => getVenue(id)).filter(Boolean);
    // Ensure an inactive venue is visible if present in store to demonstrate PAIR-04 blocking
    const inactiveSample = allVenues.find((v) => v.status === 'INACTIVE');
    if (inactiveSample && !venueOptions.some((v) => v.id === inactiveSample.id)) {
      venueOptions.push(inactiveSample);
    }
    const slots = (match && match.proposedTimeSlots && match.proposedTimeSlots.length)
      ? match.proposedTimeSlots
      : ['18:00 - 19:30', '19:30 - 21:00', '20:30 - 22:00'];

    let selVenue = pairing.package.venueId;
    let selSlot = pairing.package.timeSlot;

    ui.openSheet({
      title: 'Chọn 1 sân & 1 khung giờ (PAIR-04)',
      bodyHtml: `
        <div class="space-y-4">
          <div class="bg-[#FEF3C7] border border-[#FDE68A] rounded-[8px] p-2.5 text-xs text-[#92400E]">
            Thay đổi sân hoặc giờ sẽ tăng <strong>Revision</strong> và xóa xác nhận của cả 2 bên.
          </div>
          <div>
            <label class="block text-xs font-bold text-[#2D342C] mb-1.5">Chọn 1 sân bóng</label>
            <div class="space-y-1.5">
              ${venueOptions.map((v) => {
                const inactive = v.status !== 'ACTIVE';
                const checked = selVenue === v.id;
                return `
                  <label class="flex items-center justify-between gap-2 p-2.5 rounded-[8px] border ${inactive ? 'bg-[#FEF2F2] border-[#FECACA] opacity-70 cursor-not-allowed' : (checked ? 'bg-[#EAF0E6] border-[#3D6943]' : 'bg-white border-[#DDE5D9] cursor-pointer')}">
                    <div class="flex items-center gap-2 min-w-0">
                      <input type="radio" name="pair-venue" value="${ui.esc(v.id)}" ${checked ? 'checked' : ''} ${inactive ? 'disabled' : ''} class="text-[#3D6943]" />
                      <div class="min-w-0">
                        <div class="font-semibold text-xs text-[#111827] truncate">${ui.esc(v.name)}</div>
                        <div class="text-[11px] text-[#596158] truncate">${ui.esc(v.address)}</div>
                      </div>
                    </div>
                    ${inactive ? ui.badge('Ngừng hoạt động', 'error') : ''}
                  </label>
                `;
              }).join('')}
            </div>
          </div>

          <div>
            <label class="block text-xs font-bold text-[#2D342C] mb-1.5">Chọn 1 khung giờ</label>
            <div class="grid grid-cols-2 gap-2">
              ${slots.map((s) => `
                <label class="flex items-center gap-2 p-2.5 rounded-[8px] border border-[#DDE5D9] text-xs font-semibold cursor-pointer">
                  <input type="radio" name="pair-slot" value="${ui.esc(s)}" ${selSlot === s ? 'checked' : ''} class="text-[#3D6943]" />
                  <span>${ui.esc(s)}</span>
                </label>
              `).join('')}
            </div>
          </div>
        </div>
      `,
      footerHtml: `
        <button type="button" id="btn-save-pair-vs" class="w-full py-2.5 rounded-[8px] bg-[#3D6943] text-white text-xs font-bold">
          Cập nhật gói (Tăng Revision)
        </button>
      `
    });

    setTimeout(() => {
      const btn = document.getElementById('btn-save-pair-vs');
      if (btn) {
        btn.onclick = () => {
          const vEl = document.querySelector('input[name="pair-venue"]:checked');
          const sEl = document.querySelector('input[name="pair-slot"]:checked');
          if (!vEl || !sEl) {
            ui.toast('Vui lòng chọn đủ 1 sân hoạt động và 1 khung giờ.', 'error');
            return;
          }
          const v = getVenue(vEl.value);
          if (!v || v.status !== 'ACTIVE') {
            ui.toast('Sân này đã ngừng hoạt động, không thể chọn.', 'error');
            return;
          }
          store.setState((s) => {
            const p = s.pairings.find((x) => x.id === pairingId);
            if (!p) return;
            p.package.venueId = vEl.value;
            p.package.timeSlot = sEl.value;
            p.package.revision = (p.package.revision || 1) + 1;
            p.package.confirms = { [p.hostTeamId]: false, [p.guestTeamId]: false };
          });
          ui.closeSheet();
          ui.toast('Đã cập nhật sân & giờ. Revision tăng lên #' + (pairing.package.revision), 'success');
        };
      }
    }, 0);
  }

  /* =========================================================================
     PAIR-05: Sửa chi phí & cọc Sheet
     ========================================================================= */
  function openEditPackageCostSheet(pairingId) {
    const store = global.ArenaStore;
    const ui = global.ArenaUI;
    const pairing = getPairing(pairingId);
    if (!pairing || pairing.status !== 'NEGOTIATING') {
      ui.toast('Phòng thương lượng đã kết thúc.', 'warning');
      return;
    }
    const pkg = pairing.package;
    const host = getTeam(pairing.hostTeamId);
    const guest = getTeam(pairing.guestTeamId);

    ui.openSheet({
      title: 'Sửa chi phí & đặt cọc (PAIR-05)',
      bodyHtml: `
        <div class="space-y-3">
          <div class="bg-[#FEF3C7] border border-[#FDE68A] rounded-[8px] p-2.5 text-xs text-[#92400E]">
            Mọi thay đổi sẽ tăng <strong>Revision</strong> và yêu cầu 2 đội trưởng xác nhận lại.
          </div>
          <div>
            <label class="block text-xs font-bold text-[#2D342C] mb-1">Tổng chi phí sân (VNĐ)</label>
            <input id="p-edit-cost" type="number" min="0" step="50000" value="${Number(pkg.totalCost || 0)}" class="w-full rounded-[8px] border-[#DDE5D9] text-sm" />
          </div>
          <div>
            <label class="block text-xs font-bold text-[#2D342C] mb-1">Hình thức chia tiền</label>
            <select id="p-edit-split" class="w-full rounded-[8px] border-[#DDE5D9] text-sm">
              ${Object.keys(COST_SPLIT_LABELS).map((k) => `<option value="${k}" ${pkg.costSharing === k ? 'selected' : ''}>${ui.esc(COST_SPLIT_LABELS[k])}</option>`).join('')}
            </select>
          </div>
          <div class="grid grid-cols-2 gap-2.5">
            <div>
              <label class="block text-xs font-bold text-[#2D342C] mb-1">Tiền cọc (VNĐ)</label>
              <input id="p-edit-dep" type="number" min="0" step="50000" value="${Number(pkg.depositAmount || 0)}" class="w-full rounded-[8px] border-[#DDE5D9] text-sm" />
            </div>
            <div>
              <label class="block text-xs font-bold text-[#2D342C] mb-1">Đội giữ cọc</label>
              <select id="p-edit-holder" class="w-full rounded-[8px] border-[#DDE5D9] text-sm">
                <option value="${ui.esc(pairing.hostTeamId)}" ${pkg.depositHolder === pairing.hostTeamId ? 'selected' : ''}>${ui.esc(host ? host.name : 'Chủ kèo')}</option>
                <option value="${ui.esc(pairing.guestTeamId)}" ${pkg.depositHolder === pairing.guestTeamId ? 'selected' : ''}>${ui.esc(guest ? guest.name : 'Đội khách')}</option>
              </select>
            </div>
          </div>
        </div>
      `,
      footerHtml: `
        <button type="button" id="btn-save-pair-cost" class="w-full py-2.5 rounded-[8px] bg-[#3D6943] text-white text-xs font-bold">
          Lưu gói & Tăng Revision
        </button>
      `
    });

    setTimeout(() => {
      const btn = document.getElementById('btn-save-pair-cost');
      if (btn) {
        btn.onclick = () => {
          const cost = Number(document.getElementById('p-edit-cost').value);
          const dep = Number(document.getElementById('p-edit-dep').value);
          if (isNaN(cost) || cost < 0 || isNaN(dep) || dep < 0) {
            ui.toast('Chi phí và tiền cọc không được âm.', 'error');
            return;
          }
          store.setState((s) => {
            const p = s.pairings.find((x) => x.id === pairingId);
            if (!p) return;
            p.package.totalCost = cost;
            p.package.costSharing = document.getElementById('p-edit-split').value;
            p.package.depositAmount = dep;
            p.package.depositHolder = document.getElementById('p-edit-holder').value;
            p.package.revision = (p.package.revision || 1) + 1;
            p.package.confirms = { [p.hostTeamId]: false, [p.guestTeamId]: false };
          });
          ui.closeSheet();
          ui.toast('Đã cập nhật chi phí & cọc. Revision mới #' + (pairing.package.revision), 'success');
        };
      }
    }, 0);
  }

  /* =========================================================================
     PAIR-06: Xác nhận gói Sheet
     ========================================================================= */
  function openConfirmPackageSheet(pairingId, expectedRevision) {
    const store = global.ArenaStore;
    const ui = global.ArenaUI;
    const pairing = getPairing(pairingId);
    if (!pairing) return;
    const acting = store.getActingTeam();
    const actingId = acting ? acting.id : pairing.hostTeamId;
    const venue = getVenue(pairing.package.venueId);

    ui.openSheet({
      title: `Xác nhận gói Rev #${pairing.package.revision} (PAIR-06)`,
      bodyHtml: `
        <div class="space-y-3 text-xs">
          <div class="bg-[#F8FAF3] border border-[#DDE5D9] rounded-[8px] p-3 space-y-1.5">
            <div class="flex justify-between"><span class="text-[#596158]">Sân bóng:</span><span class="font-bold">${ui.esc(venue ? venue.name : '—')}</span></div>
            <div class="flex justify-between"><span class="text-[#596158]">Khung giờ:</span><span class="font-bold">${ui.esc(pairing.package.timeSlot)}</span></div>
            <div class="flex justify-between"><span class="text-[#596158]">Chi phí:</span><span class="font-bold">${formatMoney(pairing.package.totalCost)} (${ui.esc(COST_SPLIT_LABELS[pairing.package.costSharing])})</span></div>
            <div class="flex justify-between"><span class="text-[#596158]">Tiền cọc:</span><span class="font-bold">${formatMoney(pairing.package.depositAmount)}</span></div>
          </div>
          <p class="text-[#596158]">
            Xác nhận gói theo <strong>Revision #${pairing.package.revision}</strong>. Khi cả 2 đội cùng xác nhận trên cùng một Revision, kèo sẽ tự động chốt (AC-P04).
          </p>
        </div>
      `,
      footerHtml: `
        <div class="grid grid-cols-2 gap-2">
          <button type="button" onclick="window.ArenaUI.closeSheet()" class="py-2.5 rounded-[8px] border border-[#DDE5D9] bg-white text-xs font-bold">Xem lại</button>
          <button type="button" id="btn-do-confirm-pkg" class="py-2.5 rounded-[8px] bg-[#3D6943] text-white text-xs font-bold">Xác nhận gói</button>
        </div>
      `
    });

    setTimeout(() => {
      const btn = document.getElementById('btn-do-confirm-pkg');
      if (btn) {
        btn.onclick = () => {
          const latest = getPairing(pairingId);
          if (!latest || latest.package.revision !== expectedRevision || store.consumeSimulatedError() === '409') {
            ui.closeSheet();
            global.ArenaRouter.navigate('PAIR-07', { id: pairingId, oldRev: expectedRevision });
            return;
          }

          let bothConfirmed = false;
          let supersededCount = 0;
          store.setState((s) => {
            const p = s.pairings.find((x) => x.id === pairingId);
            if (!p) return;
            p.package.confirms = p.package.confirms || {};
            p.package.confirms[actingId] = true;

            if (p.package.confirms[p.hostTeamId] && p.package.confirms[p.guestTeamId]) {
              bothConfirmed = true;
              p.status = 'CONFIRMED';
              const m = s.matches.find((x) => x.id === p.matchId);
              if (m) {
                m.status = 'CONFIRMED';
                m.venueId = p.package.venueId;
                const parts = (p.package.timeSlot || '19:00 - 20:30').split('-').map((t) => t.trim());
                m.startTime = parts[0] || '19:00';
                m.endTime = parts[1] || '20:30';
                m.guestTeamId = p.guestTeamId;
                m.confirmedGuestTeamId = p.guestTeamId;
                m.totalCost = p.package.totalCost;
                m.costSharing = p.package.costSharing;
              }
              s.pairings.forEach((other) => {
                if (other.matchId === p.matchId && other.id !== p.id && other.status === 'NEGOTIATING') {
                  other.status = 'SUPERSEDED';
                  supersededCount++;
                }
              });
            }
          });

          ui.closeSheet();
          if (bothConfirmed) {
            ui.toast(`Chốt kèo thành công! ${supersededCount > 0 ? `${supersededCount} phòng khác chuyển SUPERSEDED.` : ''}`, 'success');
          } else {
            ui.toast('Đã ghi nhận xác nhận của đội bạn. Đang chờ đối phương xác nhận.', 'info');
          }
        };
      }
    }, 0);
  }

  /* =========================================================================
     PAIR-03 & PAIR-08: Phòng thương lượng & Trạng thái kết thúc
     ========================================================================= */
  function renderPair03(container, params) {
    const store = global.ArenaStore;
    const ui = global.ArenaUI;
    const pairing = getPairing(params.id) || (store.getState().pairings || [])[0];
    if (!pairing) {
      container.innerHTML = `<div class="p-4">${ui.errorState({ title: 'Không tìm thấy phòng thương lượng', code: '404' })}</div>`;
      return;
    }

    const host = getTeam(pairing.hostTeamId);
    const guest = getTeam(pairing.guestTeamId);
    const match = getMatch(pairing.matchId);
    const venue = getVenue(pairing.package.venueId);
    const acting = store.getActingTeam();
    const isParticipant = acting && (acting.id === pairing.hostTeamId || acting.id === pairing.guestTeamId);
    const isTerminal = pairing.status !== 'NEGOTIATING';
    const meta = PAIR_STATUS_META[pairing.status] || PAIR_STATUS_META.NEGOTIATING;
    const confirms = pairing.package.confirms || {};
    const hostOk = !!confirms[pairing.hostTeamId];
    const guestOk = !!confirms[pairing.guestTeamId];
    const myOk = acting ? !!confirms[acting.id] : false;

    container.innerHTML = `
      <div class="w-full flex-1 flex flex-col bg-[#F8FAF3] pb-8">
        <div class="p-4 space-y-3">
          <!-- Teams Header -->
          <div class="bg-white rounded-[8px] border border-[#DDE5D9] p-3.5 space-y-2.5">
            <div class="flex items-center justify-between gap-2">
              ${ui.badge(meta.label, meta.variant)}
              <span class="px-2 py-0.5 rounded bg-[#EAF0E6] text-[#14532D] text-[11px] font-mono font-bold">
                Revision #${pairing.package.revision}
              </span>
            </div>
            <div class="flex items-center justify-between gap-2 pt-1">
              <div class="flex items-center gap-2 min-w-0">
                ${ui.avatar(host || { name: 'Chủ' }, { size: 34 })}
                <span class="font-bold text-xs text-[#111827] truncate">${ui.esc(host ? host.name : '')}</span>
              </div>
              <span class="text-xs font-bold text-[#596158]">VS</span>
              <div class="flex items-center gap-2 min-w-0">
                <span class="font-bold text-xs text-[#111827] truncate">${ui.esc(guest ? guest.name : '')}</span>
                ${ui.avatar(guest || { name: 'Khách' }, { size: 34 })}
              </div>
            </div>
          </div>

          ${isTerminal ? `
            <div class="p-3.5 rounded-[8px] border ${pairing.status === 'CONFIRMED' ? 'bg-[#F0FDF4] border-[#BBF7D0] text-[#166534]' : 'bg-[#FEF2F2] border-[#FECACA] text-[#991B1B]'} text-xs space-y-1.5">
              <div class="font-bold text-sm">Thương lượng đã kết thúc (${ui.esc(pairing.status)})</div>
              <p>
                ${pairing.status === 'CONFIRMED'
                  ? 'Hai đội đã xác nhận cùng Revision. Kèo đấu đã được chốt chính thức (AC-P04).'
                  : 'Phòng thương lượng này đã đóng (SUPERSEDED/CANCELLED/EXPIRED). Mọi thao tác chỉnh sửa gói đã bị khóa.'}
              </p>
              ${pairing.status === 'CONFIRMED' && match ? `
                <button type="button" onclick="window.ArenaRouter.navigate('MATCH-03', { id: '${ui.esc(match.id)}' })"
                        class="mt-1 px-3 py-1.5 rounded-[6px] bg-[#16A34A] text-white font-bold">Xem trận đã chốt</button>
              ` : ''}
            </div>
          ` : ''}

          <!-- Current Package -->
          <div class="bg-white rounded-[8px] border border-[#DDE5D9] p-3.5 space-y-2.5 text-xs">
            <div class="flex items-center justify-between">
              <h2 class="font-bold text-sm text-[#111827]">Gói phương án hiện tại</h2>
              <span class="text-[#596158]">${match ? formatDateVi(match.matchDate) : ''}</span>
            </div>

            <div class="bg-[#F8FAF3] rounded-[8px] p-3 border border-[#EAF0E6] space-y-2">
              <div class="flex justify-between gap-2">
                <span class="text-[#596158]">Sân bóng:</span>
                <span class="font-bold text-right ${venue && venue.status !== 'ACTIVE' ? 'text-[#DC2626]' : 'text-[#111827]'}">
                  ${ui.esc(venue ? venue.name : '—')} ${venue && venue.status !== 'ACTIVE' ? '(Ngừng hoạt động)' : ''}
                </span>
              </div>
              <div class="flex justify-between gap-2">
                <span class="text-[#596158]">Khung giờ:</span>
                <span class="font-bold text-[#111827]">${ui.esc(pairing.package.timeSlot)}</span>
              </div>
              <div class="flex justify-between gap-2">
                <span class="text-[#596158]">Chi phí sân:</span>
                <span class="font-bold text-[#14532D]">${formatMoney(pairing.package.totalCost)} (${ui.esc(COST_SPLIT_LABELS[pairing.package.costSharing])})</span>
              </div>
              <div class="flex justify-between gap-2">
                <span class="text-[#596158]">Đặt cọc:</span>
                <span class="font-semibold">${formatMoney(pairing.package.depositAmount)} (${ui.esc((getTeam(pairing.package.depositHolder) || {}).name || 'Chủ kèo')})</span>
              </div>
            </div>

            <!-- Mutual Confirmations -->
            <div class="grid grid-cols-2 gap-2 pt-1">
              <div class="p-2 rounded-[8px] border ${hostOk ? 'bg-[#F0FDF4] border-[#BBF7D0]' : 'bg-white border-[#DDE5D9]'}">
                <div class="text-[10px] text-[#596158]">Chủ kèo</div>
                <div class="font-bold truncate">${ui.esc(host ? host.name : '')}</div>
                <div class="mt-1">${ui.badge(hostOk ? 'Đã xác nhận' : 'Chờ xác nhận', hostOk ? 'success' : 'warning')}</div>
              </div>
              <div class="p-2 rounded-[8px] border ${guestOk ? 'bg-[#F0FDF4] border-[#BBF7D0]' : 'bg-white border-[#DDE5D9]'}">
                <div class="text-[10px] text-[#596158]">Đội tham gia</div>
                <div class="font-bold truncate">${ui.esc(guest ? guest.name : '')}</div>
                <div class="mt-1">${ui.badge(guestOk ? 'Đã xác nhận' : 'Chờ xác nhận', guestOk ? 'success' : 'warning')}</div>
              </div>
            </div>
          </div>

          <!-- Controls -->
          ${!isTerminal && isParticipant ? `
            <div class="grid grid-cols-2 gap-2">
              <button type="button" id="btn-pair-vs" class="py-2.5 rounded-[8px] bg-white border border-[#DDE5D9] text-xs font-bold text-[#2D342C]">
                Đổi sân & giờ
              </button>
              <button type="button" id="btn-pair-cost" class="py-2.5 rounded-[8px] bg-white border border-[#DDE5D9] text-xs font-bold text-[#2D342C]">
                Sửa chi phí & cọc
              </button>
            </div>
            <button type="button" id="btn-pair-confirm" ${myOk ? 'disabled' : ''}
                    class="w-full py-3 rounded-[8px] ${myOk ? 'bg-[#DDE5D9] text-[#596158]' : 'bg-[#3D6943] hover:bg-[#315C38] text-white'} text-sm font-bold">
              ${myOk ? `Bạn đã xác nhận Rev #${pairing.package.revision}` : `Xác nhận gói (Rev #${pairing.package.revision})`}
            </button>
          ` : ''}

          <div class="grid grid-cols-2 gap-2">
            <button type="button" onclick="window.ArenaRouter.navigate('CHAT-02', { contextId: '${ui.esc(pairing.id)}' })"
                    class="py-2.5 rounded-[8px] bg-[#EAF0E6] text-[#14532D] text-xs font-bold flex items-center justify-center gap-1">
              <span class="material-symbols-outlined text-[16px]">chat</span>Chat thương lượng
            </button>
            ${!isTerminal ? `
              <button type="button" onclick="window.ArenaRouter.navigate('PAIR-07', { id: '${ui.esc(pairing.id)}', oldRev: ${pairing.package.revision} })"
                      class="py-2.5 rounded-[8px] bg-white border border-[#FDE68A] text-[#B45309] text-xs font-bold">
                Test xung đột 409
              </button>
            ` : `
              <button type="button" onclick="window.ArenaRouter.navigate('PAIR-01')"
                      class="py-2.5 rounded-[8px] bg-white border border-[#DDE5D9] text-[#2D342C] text-xs font-bold">
                Về danh sách
              </button>
            `}
          </div>
        </div>
      </div>
    `;

    const vsBtn = container.querySelector('#btn-pair-vs');
    if (vsBtn) vsBtn.onclick = () => openSelectVenueSlotSheet(pairing.id);
    const costBtn = container.querySelector('#btn-pair-cost');
    if (costBtn) costBtn.onclick = () => openEditPackageCostSheet(pairing.id);
    const confBtn = container.querySelector('#btn-pair-confirm');
    if (confBtn && !myOk) confBtn.onclick = () => openConfirmPackageSheet(pairing.id, pairing.package.revision);
  }

  /* =========================================================================
     PAIR-07: Xung đột phiên bản 409 PAIRING_006
     ========================================================================= */
  function renderPair07(container, params) {
    const store = global.ArenaStore;
    const ui = global.ArenaUI;
    const pairing = getPairing(params.id) || (store.getState().pairings || [])[0];
    const oldRev = params.oldRev || (pairing ? pairing.package.revision : 1);
    const newRev = Number(oldRev) + 1;

    container.innerHTML = `
      <div class="w-full flex-1 flex flex-col bg-[#F8FAF3] p-4 space-y-3">
        ${ui.errorState({
          title: 'Xung đột phiên bản gói (409 PAIRING_006)',
          message: `Gói thương lượng vừa được đối phương cập nhật từ Revision #${oldRev} lên Revision #${newRev} trong lúc bạn thao tác. Hệ thống không tự động ghi đè; vui lòng xem lại gói mới và xác nhận lại.`,
          code: '409 PAIRING_006',
          onBack: `window.ArenaRouter.navigate('PAIR-03', { id: '${ui.esc(pairing ? pairing.id : 'pairing-beta-alpha')}' })`,
          onRetry: `window.ArenaPairing.resolveConflict('${ui.esc(pairing ? pairing.id : 'pairing-beta-alpha')}')`
        })}
      </div>
    `;
  }

  /* =========================================================================
     Route Registration
     ========================================================================= */
  const R = global.ArenaRouter;
  if (R) {
    R.register('PAIR-01', (c, p) => renderPairingList(c, 'HOST', p), { title: 'Thương lượng chủ kèo', tab: 'MATCH-01', showBack: true, group: 'Thương lượng B', aliases: ['pairings'] });
    R.register('PAIR-02', (c, p) => renderPairingList(c, 'GUEST', p), { title: 'Thương lượng của đội', tab: 'MATCH-01', showBack: true, group: 'Thương lượng B' });
    R.register('PAIR-03', renderPair03, { title: 'Phòng thương lượng', tab: 'MATCH-01', showBack: true, group: 'Thương lượng B', aliases: ['pairing-detail'] });
    R.register('PAIR-04', (c, p) => {
      renderPair03(c, p);
      const pid = p.id || ((global.ArenaStore.getState().pairings || [])[0] || {}).id;
      if (pid) setTimeout(() => openSelectVenueSlotSheet(pid), 50);
    }, { title: 'Chọn sân & giờ gói B', tab: 'MATCH-01', showBack: true, group: 'Thương lượng B' });
    R.register('PAIR-05', (c, p) => {
      renderPair03(c, p);
      const pid = p.id || ((global.ArenaStore.getState().pairings || [])[0] || {}).id;
      if (pid) setTimeout(() => openEditPackageCostSheet(pid), 50);
    }, { title: 'Sửa chi phí & cọc', tab: 'MATCH-01', showBack: true, group: 'Thương lượng B' });
    R.register('PAIR-06', (c, p) => {
      renderPair03(c, p);
      const first = (global.ArenaStore.getState().pairings || [])[0];
      const pid = p.id || (first && first.id);
      if (pid) {
        const pkg = getPairing(pid);
        setTimeout(() => openConfirmPackageSheet(pid, pkg ? pkg.package.revision : 1), 50);
      }
    }, { title: 'Xác nhận gói B', tab: 'MATCH-01', showBack: true, group: 'Thương lượng B' });
    R.register('PAIR-07', renderPair07, { title: 'Xung đột 409 PAIRING_006', tab: 'MATCH-01', showBack: true, group: 'Thương lượng B' });
    R.register('PAIR-08', (c, p) => {
      const first = (global.ArenaStore.getState().pairings || [])[0];
      renderPair03(c, { id: p.id || (first && first.id) });
    }, { title: 'Kết thúc thương lượng', tab: 'MATCH-01', showBack: true, group: 'Thương lượng B' });
  }

  global.ArenaPairing = {
    openSelectVenueSlotSheet,
    openEditPackageCostSheet,
    openConfirmPackageSheet,
    resolveConflict: function (pairingId) {
      global.ArenaStore.setState((s) => {
        const p = s.pairings.find((x) => x.id === pairingId);
        if (p) {
          p.package.revision = (p.package.revision || 1) + 1;
          p.package.confirms = { [p.hostTeamId]: false, [p.guestTeamId]: false };
        }
      });
      global.ArenaUI.toast('Đã tải gói mới nhất.', 'info');
      global.ArenaRouter.navigate('PAIR-03', { id: pairingId }, { replace: true });
    }
  };
})(window);
