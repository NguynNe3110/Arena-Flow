/**
 * ArenaFlow Mercenary & External Match Screens
 * Covers: MERC-01..12, EXT-01..05
 * Strict AC-P05 compliance: hide venue name, street address, and coordinates
 * until application is APPROVED (or viewer is captain of the recruiting team).
 */
(function (global) {
  'use strict';

  const POSITIONS = [
    { id: 'GOALKEEPER', label: 'Thủ môn', icon: 'sports_handball' },
    { id: 'DEFENDER', label: 'Hậu vệ', icon: 'shield' },
    { id: 'MIDFIELDER', label: 'Tiền vệ', icon: 'bolt' },
    { id: 'FORWARD', label: 'Tiền đạo', icon: 'sports_soccer' }
  ];

  const SKILL_LABELS = {
    BEGINNER: 'Mới chơi / Giao lưu',
    INTERMEDIATE: 'Trung bình',
    ADVANCED: 'Khá / Tốt'
  };

  function posLabel(pos) {
    const found = POSITIONS.find((p) => p.id === pos);
    return found ? found.label : (pos || 'Tự do');
  }

  function posIcon(pos) {
    const found = POSITIONS.find((p) => p.id === pos);
    return found ? found.icon : 'person';
  }

  function recStatusBadge(status) {
    const UI = global.ArenaUI;
    switch (status) {
      case 'OPEN': return UI.badge('Đang tuyển', 'primary');
      case 'FILLED': return UI.badge('Đã đủ người', 'warning');
      case 'CLOSED': return UI.badge('Đã đóng', 'neutral');
      case 'EXPIRED': return UI.badge('Hết hạn', 'error');
      default: return UI.badge(status || 'OPEN', 'neutral');
    }
  }

  function appStatusBadge(status) {
    const UI = global.ArenaUI;
    switch (status) {
      case 'PENDING': return UI.badge('Chờ duyệt', 'warning');
      case 'APPROVED': return UI.badge('Đã duyệt', 'success');
      case 'REJECTED': return UI.badge('Từ chối', 'error');
      case 'WITHDRAWN': return UI.badge('Đã rút đơn', 'neutral');
      default: return UI.badge(status || 'PENDING', 'neutral');
    }
  }

  function calcDistanceKm(venue, gpsStatus) {
    if (!venue || gpsStatus !== 'granted') return null;
    if (typeof venue.distanceKm === 'number') return venue.distanceKm;
    if (!venue.coordinates) return null;
    const userCoords = { lat: 10.791, lng: 106.742 };
    const dLat = (venue.coordinates.lat - userCoords.lat) * 111;
    const dLng = (venue.coordinates.lng - userCoords.lng) * 111 * Math.cos((userCoords.lat * Math.PI) / 180);
    const dist = Math.sqrt(dLat * dLat + dLng * dLng);
    const rounded = Math.round(dist * 2) / 2;
    return rounded < 0.5 ? 0.5 : rounded;
  }

  function canSeeVenueDetails(rec, userId, state) {
    if (!rec || !userId) return false;
    const team = (state.teams || []).find((t) => t.id === rec.teamId);
    if (team && team.captainId === userId) return true;
    const isMember = (state.teamMembers || []).some(
      (m) => m.teamId === rec.teamId && m.userId === userId && m.status === 'ACTIVE'
    );
    if (isMember) return true;
    const app = (state.applications || []).find(
      (a) => a.recruitmentId === rec.id && a.applicantId === userId && a.status === 'APPROVED'
    );
    return !!app;
  }

  function getDistrictOnly(rec, venue, state) {
    if (rec && rec.area) {
      const parts = rec.area.split(',');
      return parts.length > 1 ? parts[parts.length - 1].trim() : rec.area;
    }
    if (venue && venue.wardId) {
      const w = (state.wards || []).find((x) => x.id === venue.wardId);
      if (w) return w.district;
    }
    return 'TP. Thủ Đức';
  }

  function parseTimeSlotStart(dateStr, timeSlot) {
    const startPart = String(timeSlot || '19:00').split('-')[0].trim();
    return new Date(`${dateStr}T${startPart}:00Z`);
  }

  function checkApplicationEligibility(rec, user, state) {
    if (!user) {
      return { ok: false, code: 'GUEST', message: 'Vui lòng đăng nhập để ứng tuyển đá lẻ.' };
    }
    if (!rec) {
      return { ok: false, code: '404', message: 'Tin tuyển người không tồn tại hoặc đã bị xóa.' };
    }
    if (rec.status !== 'OPEN') {
      return { ok: false, code: 'CLOSED', message: 'Tin tuyển này đã đủ người hoặc đã đóng.' };
    }
    const team = (state.teams || []).find((t) => t.id === rec.teamId);
    const isTeamMember = (team && team.captainId === user.id) || (state.teamMembers || []).some(
      (m) => m.teamId === rec.teamId && m.userId === user.id && m.status === 'ACTIVE'
    );
    if (isTeamMember) {
      return { ok: false, code: 'SAME_TEAM', message: 'Bạn đang thuộc đội tuyển này nên không thể nộp đơn đá lẻ.' };
    }
    const existing = (state.applications || []).find(
      (a) => a.recruitmentId === rec.id && a.applicantId === user.id && (a.status === 'PENDING' || a.status === 'APPROVED')
    );
    if (existing) {
      return { ok: false, code: 'ALREADY_APPLIED', message: 'Bạn đã nộp đơn cho tin tuyển này rồi.', existingApp: existing };
    }
    const conflict = (state.applications || []).find((a) => {
      if (a.applicantId !== user.id) return false;
      if (a.status !== 'PENDING' && a.status !== 'APPROVED') return false;
      const otherRec = (state.recruitments || []).find((r) => r.id === a.recruitmentId);
      return otherRec && otherRec.matchDate === rec.matchDate && otherRec.timeSlot === rec.timeSlot;
    });
    if (conflict) {
      return { ok: false, code: 'CONFLICT', message: `Trùng lịch thi đấu vào ${rec.matchDate} (${rec.timeSlot}).` };
    }
    return { ok: true };
  }

  function submitApplication(recId, message) {
    const Store = global.ArenaStore;
    const UI = global.ArenaUI;
    const state = Store.getState();
    const user = Store.getCurrentUser();
    const rec = (state.recruitments || []).find((r) => r.id === recId);
    const check = checkApplicationEligibility(rec, user, state);
    if (!check.ok) {
      UI.toast(check.message, 'error');
      return null;
    }

    const team = (state.teams || []).find((t) => t.id === rec.teamId);
    const newApp = {
      id: 'app-' + Date.now(),
      recruitmentId: rec.id,
      applicantId: user.id,
      status: 'PENDING',
      message: (message || '').trim() || `Em xin ứng tuyển vị trí ${posLabel(rec.position)}.`,
      createdAt: Store.getMockClock().toISOString()
    };

    const newConv = {
      id: 'conv-merc-' + newApp.id,
      contextType: 'MERCENARY',
      contextId: newApp.id,
      title: `Tuyển ${posLabel(rec.position)} • ${team ? team.name : 'Đội bóng'}`,
      participantIds: [team ? team.captainId : 'user-cap-alpha', user.id],
      canSend: true,
      messages: [
        {
          id: 'msg-' + Date.now(),
          senderId: user.id,
          text: newApp.message,
          timestamp: Store.getMockClock().toISOString(),
          readBy: [user.id]
        }
      ]
    };

    Store.setState((s) => ({
      ...s,
      applications: [newApp, ...(s.applications || [])],
      conversations: [newConv, ...(s.conversations || [])]
    }));

    UI.toast('Đã gửi đơn ứng tuyển thành công!', 'success');
    return newApp;
  }

  /* =========================================================================
     MERC-03 Sheet Helper: Nộp đơn đá lẻ
     ========================================================================= */
  function openApplySheet(recId) {
    const Store = global.ArenaStore;
    const UI = global.ArenaUI;
    const Router = global.ArenaRouter;
    const state = Store.getState();
    const user = Store.getCurrentUser();
    const rec = (state.recruitments || []).find((r) => r.id === recId);
    const check = checkApplicationEligibility(rec, user, state);

    if (!check.ok) {
      UI.openSheet({
        title: 'Không thể ứng tuyển',
        bodyHtml: `
          <div class="bg-[#FEF2F2] border border-[#FECACA] rounded-[8px] p-3.5 text-xs text-[#991B1B] flex items-start gap-2.5">
            <span class="material-symbols-outlined text-[20px] shrink-0">block</span>
            <div>
              <div class="font-bold mb-1">Điều kiện không hợp lệ</div>
              <p class="leading-relaxed">${UI.esc(check.message)}</p>
            </div>
          </div>
        `,
        footerHtml: check.code === 'GUEST'
          ? `<button type="button" onclick="window.ArenaUI.closeSheet();window.ArenaRouter.navigate('S1')" class="w-full py-2.5 rounded-[8px] bg-[#3D6943] text-white text-sm font-semibold">Đăng nhập ngay</button>`
          : check.existingApp
            ? `<button type="button" onclick="window.ArenaUI.closeSheet();window.ArenaRouter.navigate('MERC-05',{id:'${UI.esc(check.existingApp.id)}'})" class="w-full py-2.5 rounded-[8px] bg-[#3D6943] text-white text-sm font-semibold">Xem đơn của tôi</button>`
            : `<button type="button" onclick="window.ArenaUI.closeSheet()" class="w-full py-2.5 rounded-[8px] bg-[#EAF0E6] text-[#2D342C] text-sm font-semibold">Đóng</button>`
      });
      return;
    }

    const team = (state.teams || []).find((t) => t.id === rec.teamId);
    UI.openSheet({
      title: 'Nộp đơn đá lẻ (MERC-03)',
      bodyHtml: `
        <div class="space-y-3">
          <div class="bg-[#F1F5EC] border border-[#DDE5D9] rounded-[8px] p-3 text-xs space-y-1">
            <div class="font-bold text-[#14532D] text-sm">${UI.esc(team ? team.name : 'Đội bóng')}</div>
            <div class="text-[#596158]">Vị trí: <strong class="text-[#2D342C]">${UI.esc(posLabel(rec.position))}</strong></div>
            <div class="text-[#596158]">Thời gian: <strong class="text-[#2D342C]">${UI.esc(rec.matchDate)} • ${UI.esc(rec.timeSlot)}</strong></div>
            <div class="text-[#B45309] flex items-center gap-1 pt-1">
              <span class="material-symbols-outlined text-[14px]">lock</span>
              <span>Tên sân & địa chỉ chỉ mở sau khi được duyệt (AC-P05).</span>
            </div>
          </div>
          <div>
            <label class="block text-xs font-semibold text-[#2D342C] mb-1">Lời nhắn gửi đội trưởng</label>
            <textarea id="merc-apply-msg" rows="3" placeholder="Giới thiệu vị trí sở trường, kinh nghiệm của bạn..."
              class="w-full rounded-[8px] border border-[#DDE5D9] bg-white px-3 py-2 text-sm focus:border-[#3D6943] focus:outline-none"></textarea>
          </div>
        </div>
      `,
      footerHtml: `
        <div class="flex gap-2">
          <button type="button" onclick="window.ArenaUI.closeSheet()" class="flex-1 py-2.5 rounded-[8px] border border-[#DDE5D9] bg-white text-sm font-semibold text-[#2D342C]">Hủy</button>
          <button type="button" id="merc-sheet-submit-btn" class="flex-1 py-2.5 rounded-[8px] bg-[#3D6943] hover:bg-[#315C38] text-white text-sm font-semibold">Gửi đơn ứng tuyển</button>
        </div>
      `
    });

    const btn = document.getElementById('merc-sheet-submit-btn');
    if (btn) {
      btn.addEventListener('click', () => {
        const msgEl = document.getElementById('merc-apply-msg');
        const created = submitApplication(recId, msgEl ? msgEl.value : '');
        if (created) {
          UI.closeSheet();
          Router.navigate('MERC-05', { id: created.id });
        }
      });
    }
  }

  /* =========================================================================
     MERC-06 Sheet Helper: Rút đơn đá lẻ
     ========================================================================= */
  function openWithdrawSheet(appId) {
    const Store = global.ArenaStore;
    const UI = global.ArenaUI;
    const Router = global.ArenaRouter;
    const state = Store.getState();
    const app = (state.applications || []).find((a) => a.id === appId);
    const rec = app && (state.recruitments || []).find((r) => r.id === app.recruitmentId);
    if (!app || !rec) {
      UI.toast('Không tìm thấy đơn ứng tuyển.', 'error');
      return;
    }

    const now = Store.getMockClock();
    const matchStart = parseTimeSlotStart(rec.matchDate, rec.timeSlot);
    if (now >= matchStart) {
      UI.openSheet({
        title: 'Không thể rút đơn',
        bodyHtml: `<div class="bg-[#FEF2F2] border border-[#FECACA] rounded-[8px] p-3 text-xs text-[#991B1B]">Trận đấu đã bắt đầu hoặc đã kết thúc, không thể rút đơn trực tiếp.</div>`,
        footerHtml: `<button type="button" onclick="window.ArenaUI.closeSheet()" class="w-full py-2.5 rounded-[8px] bg-[#EAF0E6] text-sm font-semibold">Đóng</button>`
      });
      return;
    }

    const isApproved = app.status === 'APPROVED';
    UI.openSheet({
      title: 'Rút đơn đá lẻ (MERC-06)',
      bodyHtml: `
        <div class="space-y-3">
          ${isApproved ? `
            <div class="bg-[#FEF2F2] border border-[#FECACA] rounded-[8px] p-3 text-xs text-[#991B1B] space-y-1">
              <div class="font-bold flex items-center gap-1.5">
                <span class="material-symbols-outlined text-[18px]">warning</span>
                <span>Cảnh báo trừ -10 điểm uy tín!</span>
              </div>
              <p class="leading-relaxed">Đơn của bạn đã được duyệt. Nếu rút đơn lúc này, hệ thống sẽ trừ <strong>10 điểm uy tín cá nhân</strong> và mở lại tin tuyển cho người khác.</p>
            </div>
          ` : `
            <div class="bg-[#F0FDF4] border border-[#BBF7D0] rounded-[8px] p-3 text-xs text-[#166534]">
              Đơn đang ở trạng thái <strong>Chờ duyệt</strong>. Bạn có thể rút đơn miễn phí mà không bị trừ điểm uy tín.
            </div>
          `}
          <div>
            <label class="block text-xs font-semibold text-[#2D342C] mb-1">Lý do rút đơn</label>
            <textarea id="merc-withdraw-reason" rows="2" placeholder="Bận việc đột xuất, chấn thương..."
              class="w-full rounded-[8px] border border-[#DDE5D9] px-3 py-2 text-sm focus:border-[#3D6943] focus:outline-none"></textarea>
          </div>
        </div>
      `,
      footerHtml: `
        <div class="flex gap-2">
          <button type="button" onclick="window.ArenaUI.closeSheet()" class="flex-1 py-2.5 rounded-[8px] border border-[#DDE5D9] bg-white text-sm font-semibold">Giữ lại đơn</button>
          <button type="button" id="merc-confirm-withdraw-btn" class="flex-1 py-2.5 rounded-[8px] ${isApproved ? 'bg-[#DC2626]' : 'bg-[#3D6943]'} text-white text-sm font-semibold">Xác nhận rút</button>
        </div>
      `
    });

    const btn = document.getElementById('merc-confirm-withdraw-btn');
    if (btn) {
      btn.addEventListener('click', () => {
        const reason = (document.getElementById('merc-withdraw-reason') || {}).value || '';
        Store.setState((s) => {
          const apps = s.applications.map((a) =>
            a.id === appId ? { ...a, status: 'WITHDRAWN', withdrawReason: reason, withdrawnAt: Store.getMockClock().toISOString() } : a
          );
          let recs = s.recruitments;
          let users = s.users;
          if (isApproved) {
            recs = s.recruitments.map((r) => {
              if (r.id !== rec.id) return r;
              const nextApproved = Math.max(0, (r.approvedCount || 0) - 1);
              return { ...r, approvedCount: nextApproved, status: r.status === 'FILLED' ? 'OPEN' : r.status };
            });
            const curU = s.users[s.currentUserId];
            if (curU) {
              users = {
                ...s.users,
                [s.currentUserId]: { ...curU, reputationScore: Math.max(0, (curU.reputationScore || 100) - 10) }
              };
            }
          }
          return { ...s, applications: apps, recruitments: recs, users };
        });
        UI.closeSheet();
        UI.toast(isApproved ? 'Đã rút đơn (-10 điểm uy tín).' : 'Đã rút đơn thành công.', isApproved ? 'warning' : 'success');
        Router.refresh();
      });
    }
  }

  /* =========================================================================
     MERC-12 Sheet Helper: Sửa số lượng / Đóng tin
     ========================================================================= */
  function openEditRecruitmentSheet(recId) {
    const Store = global.ArenaStore;
    const UI = global.ArenaUI;
    const Router = global.ArenaRouter;
    const state = Store.getState();
    const rec = (state.recruitments || []).find((r) => r.id === recId);
    if (!rec) return;

    const minQty = Math.max(1, rec.approvedCount || 0);
    UI.openSheet({
      title: 'Sửa tin tuyển (MERC-12)',
      bodyHtml: `
        <div class="space-y-3">
          <div class="bg-[#F1F5EC] border border-[#DDE5D9] rounded-[8px] p-3 text-xs">
            Vị trí: <strong>${UI.esc(posLabel(rec.position))}</strong> • Đã duyệt: <strong>${rec.approvedCount || 0}</strong> người
          </div>
          <div>
            <label class="block text-xs font-semibold text-[#2D342C] mb-1">Số lượng cần tuyển (${minQty}–10)</label>
            <input id="rec-edit-qty" type="number" min="${minQty}" max="10" value="${rec.quantity}"
              class="w-full rounded-[8px] border border-[#DDE5D9] px-3 py-2 text-sm focus:border-[#3D6943] focus:outline-none" />
            <p class="text-[11px] text-[#596158] mt-1">Không thể giảm dưới số lượng đã duyệt (${rec.approvedCount || 0}).</p>
          </div>
          <div>
            <label class="block text-xs font-semibold text-[#2D342C] mb-1">Ghi chú tuyển người</label>
            <textarea id="rec-edit-note" rows="2" class="w-full rounded-[8px] border border-[#DDE5D9] px-3 py-2 text-sm focus:border-[#3D6943] focus:outline-none">${UI.esc(rec.note || '')}</textarea>
          </div>
          <div id="rec-edit-err" class="hidden text-xs text-[#DC2626] font-medium"></div>
        </div>
      `,
      footerHtml: `
        <div class="flex gap-2">
          ${rec.status !== 'CLOSED' ? `<button type="button" id="rec-close-btn" class="px-3 py-2.5 rounded-[8px] border border-[#FECACA] bg-[#FEF2F2] text-[#DC2626] text-xs font-semibold">Đóng tin</button>` : ''}
          <button type="button" id="rec-save-btn" class="flex-1 py-2.5 rounded-[8px] bg-[#3D6943] text-white text-sm font-semibold">Lưu thay đổi</button>
        </div>
      `
    });

    const saveBtn = document.getElementById('rec-save-btn');
    if (saveBtn) {
      saveBtn.addEventListener('click', () => {
        const qty = parseInt((document.getElementById('rec-edit-qty') || {}).value || '0', 10);
        const note = (document.getElementById('rec-edit-note') || {}).value || '';
        const errEl = document.getElementById('rec-edit-err');
        if (isNaN(qty) || qty < minQty || qty > 10) {
          if (errEl) {
            errEl.textContent = `Số lượng phải từ ${minQty} đến 10.`;
            errEl.classList.remove('hidden');
          }
          return;
        }
        Store.setState((s) => ({
          ...s,
          recruitments: s.recruitments.map((r) => {
            if (r.id !== recId) return r;
            const approved = r.approvedCount || 0;
            const nextStatus = r.status === 'CLOSED' ? 'CLOSED' : (approved >= qty ? 'FILLED' : 'OPEN');
            return { ...r, quantity: qty, note, status: nextStatus };
          })
        }));
        UI.closeSheet();
        UI.toast('Đã cập nhật tin tuyển.', 'success');
        Router.refresh();
      });
    }

    const closeBtn = document.getElementById('rec-close-btn');
    if (closeBtn) {
      closeBtn.addEventListener('click', () => {
        Store.setState((s) => ({
          ...s,
          recruitments: s.recruitments.map((r) => (r.id === recId ? { ...r, status: 'CLOSED' } : r))
        }));
        UI.closeSheet();
        UI.toast('Đã đóng tin tuyển.', 'info');
        Router.refresh();
      });
    }
  }

  /* =========================================================================
     EXT-04 Sheet Helper: Hủy trận ngoài
     ========================================================================= */
  function openCancelExternalSheet(extId) {
    const Store = global.ArenaStore;
    const UI = global.ArenaUI;
    const Router = global.ArenaRouter;
    UI.openSheet({
      title: 'Hủy trận ngoài (EXT-04)',
      bodyHtml: `
        <div class="space-y-3">
          <div class="bg-[#FEF2F2] border border-[#FECACA] rounded-[8px] p-3 text-xs text-[#991B1B]">
            <div class="font-bold mb-1">Xác nhận hủy trận ngoài?</div>
            <p>Toàn bộ tin tuyển đá lẻ gắn với trận này sẽ bị đóng (CLOSED) và các đơn đang chờ sẽ chuyển trạng thái.</p>
          </div>
          <div>
            <label class="block text-xs font-semibold text-[#2D342C] mb-1">Lý do hủy</label>
            <textarea id="ext-cancel-reason" rows="2" placeholder="Đối thủ hủy, thời tiết xấu..."
              class="w-full rounded-[8px] border border-[#DDE5D9] px-3 py-2 text-sm focus:border-[#3D6943] focus:outline-none"></textarea>
          </div>
        </div>
      `,
      footerHtml: `
        <div class="flex gap-2">
          <button type="button" onclick="window.ArenaUI.closeSheet()" class="flex-1 py-2.5 rounded-[8px] border border-[#DDE5D9] bg-white text-sm font-semibold">Quay lại</button>
          <button type="button" id="ext-confirm-cancel-btn" class="flex-1 py-2.5 rounded-[8px] bg-[#DC2626] text-white text-sm font-semibold">Hủy trận ngoài</button>
        </div>
      `
    });

    const btn = document.getElementById('ext-confirm-cancel-btn');
    if (btn) {
      btn.addEventListener('click', () => {
        const reason = (document.getElementById('ext-cancel-reason') || {}).value || 'Đội trưởng hủy trận';
        Store.setState((s) => {
          const closedRecIds = s.recruitments.filter((r) => r.externalMatchId === extId).map((r) => r.id);
          return {
            ...s,
            externalMatches: s.externalMatches.map((m) => (m.id === extId ? { ...m, status: 'CANCELLED', cancelReason: reason } : m)),
            recruitments: s.recruitments.map((r) => (r.externalMatchId === extId ? { ...r, status: 'CLOSED' } : r)),
            applications: s.applications.map((a) =>
              closedRecIds.includes(a.recruitmentId) && a.status === 'PENDING' ? { ...a, status: 'REJECTED' } : a
            )
          };
        });
        UI.closeSheet();
        UI.toast('Đã hủy trận ngoài và đóng các tin tuyển liên quan.', 'info');
        Router.navigate('EXT-02');
      });
    }
  }

  /* =========================================================================
     EXT-05 Sheet Helper: Thêm vị trí tuyển trận ngoài
     ========================================================================= */
  function openAddPositionSheet(extId) {
    const Store = global.ArenaStore;
    const UI = global.ArenaUI;
    const Router = global.ArenaRouter;
    const state = Store.getState();
    const ext = (state.externalMatches || []).find((m) => m.id === extId);
    if (!ext) return;

    UI.openSheet({
      title: 'Thêm vị trí tuyển (EXT-05)',
      bodyHtml: `
        <div class="space-y-3">
          <div>
            <label class="block text-xs font-semibold text-[#2D342C] mb-1">Vị trí cần tuyển</label>
            <select id="ext-pos-select" class="w-full rounded-[8px] border border-[#DDE5D9] px-3 py-2 text-sm bg-white">
              ${POSITIONS.map((p) => `<option value="${p.id}">${UI.esc(p.label)}</option>`).join('')}
            </select>
          </div>
          <div>
            <label class="block text-xs font-semibold text-[#2D342C] mb-1">Số lượng (1–10)</label>
            <input id="ext-pos-qty" type="number" min="1" max="10" value="1"
              class="w-full rounded-[8px] border border-[#DDE5D9] px-3 py-2 text-sm" />
          </div>
          <div>
            <label class="block text-xs font-semibold text-[#2D342C] mb-1">Ghi chú</label>
            <input id="ext-pos-note" type="text" placeholder="Hỗ trợ nước uống, sân cỏ đẹp..."
              class="w-full rounded-[8px] border border-[#DDE5D9] px-3 py-2 text-sm" />
          </div>
        </div>
      `,
      footerHtml: `
        <button type="button" id="ext-pos-submit-btn" class="w-full py-2.5 rounded-[8px] bg-[#3D6943] text-white text-sm font-semibold">Đăng vị trí tuyển</button>
      `
    });

    const btn = document.getElementById('ext-pos-submit-btn');
    if (btn) {
      btn.addEventListener('click', () => {
        const position = (document.getElementById('ext-pos-select') || {}).value || 'MIDFIELDER';
        const quantity = Math.min(10, Math.max(1, parseInt((document.getElementById('ext-pos-qty') || {}).value || '1', 10)));
        const note = (document.getElementById('ext-pos-note') || {}).value || '';
        const venue = (state.venues || []).find((v) => v.id === ext.venueId);
        const area = venue ? Store.getWardName(venue.wardId) : 'TP. Thủ Đức';
        const newRec = {
          id: 'rec-' + Date.now(),
          teamId: ext.teamId,
          externalMatchId: ext.id,
          position,
          quantity,
          approvedCount: 0,
          status: 'OPEN',
          area,
          distanceKm: venue ? venue.distanceKm : 2.0,
          matchDate: ext.matchDate,
          timeSlot: `${ext.startTime} - ${ext.endTime}`,
          venueId: ext.venueId || null,
          skillLevel: ext.skillLevel || 'INTERMEDIATE',
          note,
          createdAt: Store.getMockClock().toISOString()
        };
        Store.setState((s) => ({ ...s, recruitments: [newRec, ...(s.recruitments || [])] }));
        UI.closeSheet();
        UI.toast(`Đã thêm vị trí ${posLabel(position)} (${quantity} người).`, 'success');
        Router.refresh();
      });
    }
  }

  /* =========================================================================
     Register Routes
     ========================================================================= */
  function registerMercenaryRoutes() {
    const Router = global.ArenaRouter;
    const Store = global.ArenaStore;
    const UI = global.ArenaUI;
    if (!Router || !Store || !UI) return;

    /* MERC-01: Khám phá đá lẻ */
    let filterPos = 'ALL';
    let filterTimeSlot = 'ALL'; // 'ALL' | 'MORNING' | 'AFTERNOON' | 'EVENING'
    let filterRadius = 3;

    function matchTimeBucket(timeSlot, bucket) {
      if (bucket === 'ALL') return true;
      const startHour = parseInt(String(timeSlot || '19:00').split(':')[0], 10);
      if (isNaN(startHour)) return true;
      if (bucket === 'MORNING') return startHour < 12;
      if (bucket === 'AFTERNOON') return startHour >= 12 && startHour < 18;
      return startHour >= 18;
    }

    function openMercFilterSheet() {
      const gpsStatus = Store.getGpsStatus();
      const user = Store.getCurrentUser();
      UI.openSheet({
        title: 'Lọc tin đá lẻ',
        bodyHtml: `
          <div class="space-y-4">
            <div>
              <label class="block text-xs font-bold text-[#2D342C] mb-1.5">Vị trí cần tuyển</label>
              <div class="flex flex-wrap gap-1.5">
                <button type="button" data-sheet-pos="ALL"
                  class="merc-sheet-pos px-3 py-1.5 rounded-full text-xs font-semibold ${filterPos === 'ALL' ? 'bg-[#14532D] text-white' : 'bg-[#EAF0E6] text-[#596158]'}">
                  Tất cả
                </button>
                ${POSITIONS.map((p) => `
                  <button type="button" data-sheet-pos="${p.id}"
                    class="merc-sheet-pos px-3 py-1.5 rounded-full text-xs font-semibold ${filterPos === p.id ? 'bg-[#14532D] text-white' : 'bg-[#EAF0E6] text-[#596158]'}">
                    ${UI.esc(p.label)}
                  </button>
                `).join('')}
              </div>
            </div>

            <div>
              <label class="block text-xs font-bold text-[#2D342C] mb-1.5">Khung giờ thi đấu</label>
              <div class="grid grid-cols-2 gap-1.5">
                ${[
                  { id: 'ALL', label: 'Mọi khung giờ' },
                  { id: 'MORNING', label: 'Sáng (< 12:00)' },
                  { id: 'AFTERNOON', label: 'Chiều (12:00 - 18:00)' },
                  { id: 'EVENING', label: 'Tối (≥ 18:00)' }
                ].map((t) => `
                  <button type="button" data-sheet-time="${t.id}"
                    class="merc-sheet-time px-3 py-2 rounded-[8px] text-xs font-semibold border ${filterTimeSlot === t.id ? 'bg-[#14532D] text-white border-[#14532D]' : 'bg-white text-[#596158] border-[#DDE5D9]'}">
                    ${t.label}
                  </button>
                `).join('')}
              </div>
            </div>

            <div class="bg-[#F8FAF3] rounded-[8px] border border-[#DDE5D9] p-3 space-y-2.5">
              <div class="flex items-center justify-between gap-2">
                <div class="min-w-0">
                  <div class="text-xs font-bold text-[#111827]">${gpsStatus === 'granted' ? 'Đang định vị GPS' : 'GPS đang tắt — Dùng khu vực'}</div>
                  <div class="text-[11px] text-[#596158] truncate">
                    ${gpsStatus === 'granted' ? `Bán kính: ${filterRadius} km` : (user && user.wardId ? Store.getWardName(user.wardId) : 'Chưa cập nhật khu vực')}
                  </div>
                </div>
                <button type="button" id="merc-sheet-gps-toggle"
                  class="px-2.5 py-1 rounded-[8px] border border-[#DDE5D9] bg-white text-[11px] font-semibold text-[#3D6943]">
                  ${gpsStatus === 'granted' ? 'Tắt GPS' : 'Bật GPS'}
                </button>
              </div>
              ${gpsStatus === 'granted' ? `
                <div class="flex items-center gap-1.5 flex-wrap">
                  ${[1, 3, 5, 10, 20].map((r) => `
                    <button type="button" data-sheet-radius="${r}"
                      class="merc-sheet-radius px-2.5 py-1 rounded-full text-[11px] font-semibold ${filterRadius === r ? 'bg-[#3D6943] text-white' : 'bg-white border border-[#DDE5D9] text-[#596158]'}">
                      ${r} km
                    </button>
                  `).join('')}
                </div>
              ` : ''}
            </div>
          </div>
        `,
        footerHtml: `
          <div class="flex gap-2">
            <button type="button" id="merc-sheet-reset" class="flex-1 py-2.5 rounded-[8px] border border-[#DDE5D9] bg-white text-sm font-semibold text-[#2D342C]">Đặt lại</button>
            <button type="button" id="merc-sheet-apply" class="flex-1 py-2.5 rounded-[8px] bg-[#3D6943] text-white text-sm font-semibold">Áp dụng</button>
          </div>
        `
      });

      let draftPos = filterPos;
      let draftTime = filterTimeSlot;
      let draftRadius = filterRadius;

      document.querySelectorAll('.merc-sheet-pos').forEach((btn) => {
        btn.addEventListener('click', () => {
          draftPos = btn.getAttribute('data-sheet-pos') || 'ALL';
          document.querySelectorAll('.merc-sheet-pos').forEach((b) => {
            const active = b.getAttribute('data-sheet-pos') === draftPos;
            b.className = `merc-sheet-pos px-3 py-1.5 rounded-full text-xs font-semibold ${active ? 'bg-[#14532D] text-white' : 'bg-[#EAF0E6] text-[#596158]'}`;
          });
        });
      });

      document.querySelectorAll('.merc-sheet-time').forEach((btn) => {
        btn.addEventListener('click', () => {
          draftTime = btn.getAttribute('data-sheet-time') || 'ALL';
          document.querySelectorAll('.merc-sheet-time').forEach((b) => {
            const active = b.getAttribute('data-sheet-time') === draftTime;
            b.className = `merc-sheet-time px-3 py-2 rounded-[8px] text-xs font-semibold border ${active ? 'bg-[#14532D] text-white border-[#14532D]' : 'bg-white text-[#596158] border-[#DDE5D9]'}`;
          });
        });
      });

      document.querySelectorAll('.merc-sheet-radius').forEach((btn) => {
        btn.addEventListener('click', () => {
          draftRadius = parseInt(btn.getAttribute('data-sheet-radius'), 10) || 3;
          document.querySelectorAll('.merc-sheet-radius').forEach((b) => {
            const active = parseInt(b.getAttribute('data-sheet-radius'), 10) === draftRadius;
            b.className = `merc-sheet-radius px-2.5 py-1 rounded-full text-[11px] font-semibold ${active ? 'bg-[#3D6943] text-white' : 'bg-white border border-[#DDE5D9] text-[#596158]'}`;
          });
        });
      });

      const sheetGpsBtn = document.getElementById('merc-sheet-gps-toggle');
      if (sheetGpsBtn) {
        sheetGpsBtn.addEventListener('click', () => {
          Store.setGpsStatus(Store.getGpsStatus() === 'granted' ? 'denied' : 'granted');
          UI.closeSheet();
          openMercFilterSheet();
        });
      }

      const resetBtn = document.getElementById('merc-sheet-reset');
      if (resetBtn) {
        resetBtn.addEventListener('click', () => {
          filterPos = 'ALL';
          filterTimeSlot = 'ALL';
          filterRadius = 3;
          UI.closeSheet();
          Router.refresh();
        });
      }

      const applyBtn = document.getElementById('merc-sheet-apply');
      if (applyBtn) {
        applyBtn.addEventListener('click', () => {
          filterPos = draftPos;
          filterTimeSlot = draftTime;
          filterRadius = draftRadius;
          UI.closeSheet();
          Router.refresh();
        });
      }
    }

    Router.register('MERC-01', (container, params) => {
      if (params && params.position) filterPos = params.position;
      if (params && params.timeSlot) filterTimeSlot = params.timeSlot;
      if (params && params.radius) filterRadius = parseInt(params.radius, 10) || 3;

      const state = Store.getState();
      const user = Store.getCurrentUser();
      const gpsStatus = Store.getGpsStatus();
      const isCaptain = Store.getMyCaptainedTeams().length > 0;
      const myAppsCount = user ? (state.applications || []).filter((a) => a.applicantId === user.id).length : 0;

      const activeFilterCount =
        (filterPos !== 'ALL' ? 1 : 0) +
        (filterTimeSlot !== 'ALL' ? 1 : 0) +
        (gpsStatus === 'granted' && filterRadius !== 3 ? 1 : 0);

      const listings = (state.recruitments || []).filter((rec) => {
        if (rec.status !== 'OPEN' && rec.status !== 'FILLED') return false;
        if (filterPos !== 'ALL' && rec.position !== filterPos) return false;
        if (!matchTimeBucket(rec.timeSlot, filterTimeSlot)) return false;
        const venue = (state.venues || []).find((v) => v.id === rec.venueId);
        const dist = calcDistanceKm(venue, gpsStatus);
        rec._dist = dist;
        if (gpsStatus === 'granted' && dist !== null && dist > filterRadius) return false;
        return true;
      });

      container.innerHTML = `
        <div class="p-4 space-y-3">
          <!-- Compact Header Bar -->
          <div class="flex items-center justify-between gap-2 bg-white rounded-[8px] border border-[#DDE5D9] px-3 py-2.5">
            <div class="min-w-0">
              <div class="text-sm font-bold text-[#111827]">Tìm tin đá lẻ (${listings.length})</div>
              <div class="text-[11px] text-[#596158] truncate">
                ${gpsStatus === 'granted' ? `GPS • Bán kính ${filterRadius} km` : (user && user.wardId ? Store.getWardName(user.wardId) : 'Khu vực mặc định')}
              </div>
            </div>
            <div class="flex items-center gap-1.5 shrink-0">
              <button type="button" onclick="window.ArenaRouter.navigate('MERC-04')"
                class="px-2.5 py-1.5 rounded-[8px] border border-[#DDE5D9] bg-[#F8FAF3] text-xs font-semibold text-[#2D342C] flex items-center gap-1">
                <span>Đơn (${myAppsCount})</span>
              </button>
              ${isCaptain ? `
                <button type="button" onclick="window.ArenaRouter.navigate('MERC-07')"
                  class="px-2.5 py-1.5 rounded-[8px] bg-[#3D6943] text-white text-xs font-semibold flex items-center gap-0.5">
                  <span class="material-symbols-outlined text-[15px]">add</span>
                  <span>Tuyển</span>
                </button>
              ` : ''}
              <button type="button" id="merc-open-filter-btn" aria-label="Bộ lọc"
                class="relative w-9 h-9 rounded-[8px] border border-[#DDE5D9] bg-white hover:bg-[#EAF0E6] flex items-center justify-center text-[#14532D]">
                <span class="material-symbols-outlined text-[20px]">tune</span>
                ${activeFilterCount > 0 ? `
                  <span class="absolute -top-1 -right-1 min-w-[16px] h-4 px-1 rounded-full bg-[#3D6943] text-white text-[10px] font-bold flex items-center justify-center leading-none">
                    ${activeFilterCount}
                  </span>
                ` : ''}
              </button>
            </div>
          </div>

          <!-- Simplified Listings -->
          ${listings.length === 0 ? UI.emptyState({
            icon: 'person_search',
            title: 'Chưa có tin tuyển phù hợp',
            description: 'Thử điều chỉnh bộ lọc vị trí, khung giờ hoặc bán kính.',
            actionLabel: 'Đặt lại bộ lọc',
            onAction: "window.ArenaRouter.navigate('MERC-01',{position:'ALL',timeSlot:'ALL',radius:20})"
          }) : `
            <div class="space-y-2.5">
              ${listings.map((rec) => {
                const team = (state.teams || []).find((t) => t.id === rec.teamId) || { name: 'Đội bóng' };
                const venue = (state.venues || []).find((v) => v.id === rec.venueId);
                const revealVenue = canSeeVenueDetails(rec, user && user.id, state);
                const districtLabel = getDistrictOnly(rec, venue, state);
                const locationText = revealVenue && venue ? venue.name : `Khu vực ${districtLabel}`;
                return `
                  <article onclick="window.ArenaRouter.navigate('MERC-02',{id:'${UI.esc(rec.id)}'})"
                    class="bg-white rounded-[8px] border border-[#DDE5D9] p-3.5 cursor-pointer hover:border-[#3D6943] transition-colors space-y-2">
                    <div class="flex items-center justify-between gap-2">
                      <span class="text-xs font-bold text-[#14532D]">
                        ${UI.esc(posLabel(rec.position))} • Cần ${rec.quantity - (rec.approvedCount || 0) > 0 ? `${rec.approvedCount || 0}/${rec.quantity}` : 'Đủ'} slot
                      </span>
                      <span class="material-symbols-outlined text-[18px] text-[#596158]">chevron_right</span>
                    </div>

                    <div class="flex items-center gap-2.5">
                      ${UI.avatar(team, { size: 36 })}
                      <div class="min-w-0 flex-1">
                        <h3 class="font-bold text-sm text-[#111827] truncate">${UI.esc(team.name)}</h3>
                        <div class="text-xs text-[#596158] truncate">
                          ${UI.esc(rec.matchDate)} • ${UI.esc(rec.timeSlot)}
                        </div>
                      </div>
                    </div>

                    <div class="flex items-center gap-1.5 text-xs text-[#596158] pt-1 border-t border-[#F1F5EC]">
                      <span class="material-symbols-outlined text-[15px] text-[#3D6943] shrink-0">${revealVenue ? 'location_on' : 'lock'}</span>
                      <span class="truncate">${UI.esc(locationText)}</span>
                    </div>
                  </article>
                `;
              }).join('')}
            </div>
          `}
        </div>
      `;

      const filterBtn = container.querySelector('#merc-open-filter-btn');
      if (filterBtn) {
        filterBtn.addEventListener('click', openMercFilterSheet);
      }
    }, { title: 'Đá lẻ', tab: 'MERC-01', group: 'Đá lẻ', aliases: ['players'] });

    /* MERC-02: Chi tiết tin tuyển (AC-P05 privacy) */
    Router.register('MERC-02', (container, params) => {
      const state = Store.getState();
      const user = Store.getCurrentUser();
      const rec = (state.recruitments || []).find((r) => r.id === (params && params.id)) || (state.recruitments || [])[0];
      if (!rec) {
        container.innerHTML = `<div class="p-4">${UI.errorState({ title: 'Không tìm thấy tin tuyển', code: '404' })}</div>`;
        return;
      }

      const team = (state.teams || []).find((t) => t.id === rec.teamId) || { name: 'Đội bóng', reputationScore: 80 };
      const venue = (state.venues || []).find((v) => v.id === rec.venueId);
      const revealVenue = canSeeVenueDetails(rec, user && user.id, state);
      const districtLabel = getDistrictOnly(rec, venue, state);
      const myApp = user ? (state.applications || []).find((a) => a.recruitmentId === rec.id && a.applicantId === user.id) : null;
      const isCaptain = user && team.captainId === user.id;

      container.innerHTML = `
        <div class="p-4 space-y-3">
          <div class="bg-white rounded-[8px] border border-[#DDE5D9] p-4 space-y-3">
            <div class="flex items-center justify-between">
              ${UI.badge(posLabel(rec.position), 'info')}
              ${recStatusBadge(rec.status)}
            </div>
            <div class="flex items-center gap-3">
              ${UI.avatar(team, { size: 48 })}
              <div>
                <h2 class="font-bold text-base text-[#111827]">${UI.esc(team.name)}</h2>
                <div class="text-xs text-[#596158]">Uy tín đội: <strong class="text-[#14532D]">${team.reputationScore}/100</strong> • ⭐ ${team.ratingAvg || '5.0'}</div>
              </div>
            </div>
            <div class="grid grid-cols-2 gap-2 pt-2 border-t border-[#F1F5EC] text-xs">
              <div class="bg-[#F8FAF3] rounded-[8px] p-2.5">
                <div class="text-[#596158]">Ngày & Giờ</div>
                <div class="font-bold text-[#111827] mt-0.5">${UI.esc(rec.matchDate)}</div>
                <div class="font-semibold text-[#3D6943]">${UI.esc(rec.timeSlot)}</div>
              </div>
              <div class="bg-[#F8FAF3] rounded-[8px] p-2.5">
                <div class="text-[#596158]">Số lượng tuyển</div>
                <div class="font-bold text-[#111827] mt-0.5">${rec.approvedCount || 0} / ${rec.quantity} người</div>
                <div class="text-[#596158]">${UI.esc(SKILL_LABELS[rec.skillLevel] || 'Trung bình')}</div>
              </div>
            </div>
          </div>

          <!-- AC-P05 Venue Privacy Box -->
          <div class="bg-white rounded-[8px] border ${revealVenue ? 'border-[#BBF7D0]' : 'border-[#FDE68A]'} p-4 space-y-2">
            <div class="flex items-center gap-2 font-bold text-sm ${revealVenue ? 'text-[#14532D]' : 'text-[#92400E]'}">
              <span class="material-symbols-outlined text-[20px]">${revealVenue ? 'location_on' : 'lock'}</span>
              <span>Địa điểm thi đấu</span>
            </div>
            ${revealVenue && venue ? `
              <div class="text-sm font-bold text-[#111827]">${UI.esc(venue.name)}</div>
              <div class="text-xs text-[#596158]">${UI.esc(venue.address)}</div>
              <button type="button" onclick="window.ArenaRouter.navigate('VEN-02',{id:'${UI.esc(venue.id)}'})"
                class="mt-1 text-xs font-semibold text-[#3D6943] underline">
                Xem chi tiết sân & bản đồ
              </button>
            ` : `
              <div class="text-sm font-semibold text-[#111827]">Khu vực: ${UI.esc(districtLabel)}</div>
              <p class="text-xs text-[#92400E] bg-[#FEF3C7] rounded-[8px] p-2.5 leading-relaxed">
                Theo chính sách bảo mật (AC-P05), tên sân cụ thể, địa chỉ đường và tọa độ chỉ hiển thị sau khi đơn ứng tuyển của bạn được đội trưởng <strong>DUYỆT (APPROVED)</strong>.
              </p>
            `}
          </div>

          ${rec.note ? `
            <div class="bg-white rounded-[8px] border border-[#DDE5D9] p-4">
              <div class="text-xs font-bold text-[#2D342C] mb-1">Ghi chú từ đội trưởng</div>
              <p class="text-xs text-[#596158] leading-relaxed">${UI.esc(rec.note)}</p>
            </div>
          ` : ''}

          <div class="pt-1 space-y-2">
            ${isCaptain ? `
              <button type="button" onclick="window.ArenaRouter.navigate('MERC-10',{recId:'${UI.esc(rec.id)}'})"
                class="w-full py-3 rounded-[8px] bg-[#3D6943] text-white text-sm font-semibold">
                Xem danh sách ứng viên
              </button>
            ` : myApp ? `
              <button type="button" onclick="window.ArenaRouter.navigate('MERC-05',{id:'${UI.esc(myApp.id)}'})"
                class="w-full py-3 rounded-[8px] bg-[#14532D] text-white text-sm font-semibold">
                Xem chi tiết đơn của tôi (${UI.esc(myApp.status)})
              </button>
            ` : `
              <button type="button" id="merc02-apply-btn"
                class="w-full py-3 rounded-[8px] ${rec.status === 'OPEN' ? 'bg-[#3D6943] hover:bg-[#315C38] text-white' : 'bg-[#DDE5D9] text-[#596158]'} text-sm font-semibold"
                ${rec.status !== 'OPEN' ? 'disabled' : ''}>
                ${rec.status === 'OPEN' ? 'Ứng tuyển vị trí này' : 'Tin tuyển đã đóng / đủ người'}
              </button>
            `}
          </div>
        </div>
      `;

      const applyBtn = container.querySelector('#merc02-apply-btn');
      if (applyBtn) {
        applyBtn.addEventListener('click', () => openApplySheet(rec.id));
      }
    }, { title: 'Chi tiết tin tuyển', tab: 'MERC-01', showBack: true, group: 'Đá lẻ', aliases: ['recruitment-detail'] });

    /* MERC-03: Route fallback for direct navigation */
    Router.register('MERC-03', (container, params) => {
      const state = Store.getState();
      const rec = (state.recruitments || []).find((r) => r.id === (params && params.recId)) || (state.recruitments || [])[0];
      const user = Store.getCurrentUser();
      const check = checkApplicationEligibility(rec, user, state);
      const team = rec && (state.teams || []).find((t) => t.id === rec.teamId);

      container.innerHTML = `
        <div class="p-4 space-y-3">
          ${!check.ok ? UI.errorState({ title: 'Không thể nộp đơn', message: check.message, code: check.code }) : `
            <div class="bg-white rounded-[8px] border border-[#DDE5D9] p-4 space-y-3">
              <div class="font-bold text-sm text-[#14532D]">Nộp đơn đá lẻ • ${UI.esc(team ? team.name : '')}</div>
              <div class="text-xs text-[#596158]">Vị trí: <strong>${UI.esc(posLabel(rec.position))}</strong> • ${UI.esc(rec.matchDate)} (${UI.esc(rec.timeSlot)})</div>
              <textarea id="merc03-msg" rows="3" placeholder="Lời nhắn gửi đội trưởng..."
                class="w-full rounded-[8px] border border-[#DDE5D9] px-3 py-2 text-sm"></textarea>
              <button type="button" id="merc03-submit" class="w-full py-2.5 rounded-[8px] bg-[#3D6943] text-white text-sm font-semibold">
                Gửi đơn ứng tuyển
              </button>
            </div>
          `}
        </div>
      `;
      const btn = container.querySelector('#merc03-submit');
      if (btn && rec) {
        btn.addEventListener('click', () => {
          const msg = (container.querySelector('#merc03-msg') || {}).value || '';
          const created = submitApplication(rec.id, msg);
          if (created) Router.navigate('MERC-05', { id: created.id });
        });
      }
    }, { title: 'Nộp đơn đá lẻ', tab: 'MERC-01', showBack: true, group: 'Đá lẻ', aliases: ['mercenary-apply'] });

    /* MERC-04: Đơn ứng tuyển của tôi */
    let appFilter = 'ALL';
    Router.register('MERC-04', (container) => {
      const state = Store.getState();
      const user = Store.getCurrentUser();
      if (!user) {
        container.innerHTML = `<div class="p-4">${UI.emptyState({ icon: 'login', title: 'Chưa đăng nhập', description: 'Đăng nhập để xem đơn ứng tuyển đá lẻ của bạn.', actionLabel: 'Đăng nhập', onAction: "window.ArenaRouter.navigate('S1')" })}</div>`;
        return;
      }

      const myApps = (state.applications || []).filter((a) => a.applicantId === user.id);
      const filtered = appFilter === 'ALL' ? myApps : myApps.filter((a) => a.status === appFilter);

      container.innerHTML = `
        <div class="p-4 space-y-3">
          <div class="flex items-center gap-1.5 overflow-x-auto no-scrollbar">
            ${[
              { k: 'ALL', l: 'Tất cả' },
              { k: 'PENDING', l: 'Chờ duyệt' },
              { k: 'APPROVED', l: 'Đã duyệt' },
              { k: 'REJECTED', l: 'Từ chối' },
              { k: 'WITHDRAWN', l: 'Đã rút' }
            ].map((f) => `
              <button type="button" data-status="${f.k}"
                class="merc04-filter-btn px-3 py-1.5 rounded-full text-xs font-semibold shrink-0 ${appFilter === f.k ? 'bg-[#14532D] text-white' : 'bg-white border border-[#DDE5D9] text-[#596158]'}">
                ${f.l}
              </button>
            `).join('')}
          </div>

          ${filtered.length === 0 ? UI.emptyState({
            icon: 'assignment',
            title: 'Chưa có đơn ứng tuyển',
            description: 'Khám phá các đội đang tuyển người gần bạn ngay.',
            actionLabel: 'Tìm tin tuyển',
            onAction: "window.ArenaRouter.navigate('MERC-01')"
          }) : `
            <div class="space-y-2.5">
              ${filtered.map((app) => {
                const rec = (state.recruitments || []).find((r) => r.id === app.recruitmentId) || {};
                const team = (state.teams || []).find((t) => t.id === rec.teamId) || { name: 'Đội bóng' };
                return `
                  <div onclick="window.ArenaRouter.navigate('MERC-05',{id:'${UI.esc(app.id)}'})"
                    class="bg-white rounded-[8px] border border-[#DDE5D9] p-3.5 cursor-pointer hover:border-[#3D6943] space-y-2">
                    <div class="flex items-center justify-between">
                      <span class="font-bold text-sm text-[#111827]">${UI.esc(team.name)}</span>
                      ${appStatusBadge(app.status)}
                    </div>
                    <div class="text-xs text-[#596158] flex items-center gap-2">
                      <span>${UI.esc(posLabel(rec.position))}</span>
                      <span>•</span>
                      <span>${UI.esc(rec.matchDate || '')} ${UI.esc(rec.timeSlot || '')}</span>
                    </div>
                    ${app.status === 'APPROVED' ? `
                      <div class="text-[11px] text-[#16A34A] font-semibold flex items-center gap-1">
                        <span class="material-symbols-outlined text-[14px]">lock_open</span>
                        Đã mở khóa địa chỉ sân & chat với đội trưởng
                      </div>
                    ` : ''}
                  </div>
                `;
              }).join('')}
            </div>
          `}
        </div>
      `;

      container.querySelectorAll('.merc04-filter-btn').forEach((b) => {
        b.addEventListener('click', () => {
          appFilter = b.getAttribute('data-status') || 'ALL';
          Router.refresh();
        });
      });
    }, { title: 'Đơn ứng tuyển của tôi', tab: 'MERC-01', showBack: true, group: 'Đá lẻ', aliases: ['my-applications'] });

    /* MERC-05: Chi tiết đơn ứng tuyển */
    Router.register('MERC-05', (container, params) => {
      const state = Store.getState();
      const user = Store.getCurrentUser();
      const app = (state.applications || []).find((a) => a.id === (params && params.id)) ||
        (user && (state.applications || []).find((a) => a.applicantId === user.id));
      if (!app) {
        container.innerHTML = `<div class="p-4">${UI.errorState({ title: 'Không tìm thấy đơn', code: '404' })}</div>`;
        return;
      }

      const rec = (state.recruitments || []).find((r) => r.id === app.recruitmentId) || {};
      const team = (state.teams || []).find((t) => t.id === rec.teamId) || { name: 'Đội bóng' };
      const venue = (state.venues || []).find((v) => v.id === rec.venueId);
      const isApproved = app.status === 'APPROVED';
      const canWithdraw = app.status === 'PENDING' || app.status === 'APPROVED';
      const conv = (state.conversations || []).find((c) => c.contextId === app.id || c.contextId === rec.id);

      container.innerHTML = `
        <div class="p-4 space-y-3">
          <div class="bg-white rounded-[8px] border border-[#DDE5D9] p-4 space-y-3">
            <div class="flex items-center justify-between">
              <span class="text-xs font-semibold text-[#596158]">Trạng thái đơn</span>
              ${appStatusBadge(app.status)}
            </div>
            <div class="flex items-center gap-3">
              ${UI.avatar(team, { size: 44 })}
              <div>
                <h2 class="font-bold text-base text-[#111827]">${UI.esc(team.name)}</h2>
                <div class="text-xs text-[#596158]">Vị trí: <strong>${UI.esc(posLabel(rec.position))}</strong></div>
              </div>
            </div>
            <div class="bg-[#F8FAF3] rounded-[8px] p-3 text-xs space-y-1">
              <div><strong>Thời gian:</strong> ${UI.esc(rec.matchDate)} • ${UI.esc(rec.timeSlot)}</div>
              <div><strong>Lời nhắn của bạn:</strong> "${UI.esc(app.message || '')}"</div>
            </div>
          </div>

          <!-- Venue Unlock Box (AC-P05) -->
          <div class="bg-white rounded-[8px] border ${isApproved ? 'border-[#BBF7D0]' : 'border-[#DDE5D9]'} p-4 space-y-2">
            <div class="font-bold text-sm flex items-center gap-1.5 ${isApproved ? 'text-[#14532D]' : 'text-[#596158]'}">
              <span class="material-symbols-outlined text-[18px]">${isApproved ? 'lock_open' : 'lock'}</span>
              <span>Thông tin sân thi đấu</span>
            </div>
            ${isApproved && venue ? `
              <div class="text-sm font-bold text-[#111827]">${UI.esc(venue.name)}</div>
              <div class="text-xs text-[#596158]">${UI.esc(venue.address)}</div>
              ${venue.contactPhone ? `<div class="text-xs text-[#3D6943]">SĐT sân: ${UI.esc(venue.contactPhone)}</div>` : ''}
              <button type="button" onclick="window.ArenaRouter.navigate('VEN-02',{id:'${UI.esc(venue.id)}'})"
                class="text-xs font-semibold text-[#3D6943] underline">
                Mở chi tiết sân
              </button>
            ` : `
              <div class="text-xs text-[#596158]">Khu vực: <strong>${UI.esc(getDistrictOnly(rec, venue, state))}</strong></div>
              <div class="text-[11px] text-[#B45309]">Địa chỉ chi tiết chỉ hiển thị khi đơn được duyệt (APPROVED).</div>
            `}
          </div>

          <div class="space-y-2">
            ${isApproved ? `
              <button type="button" onclick="window.ArenaRouter.navigate('CHAT-03',{convId:'${UI.esc(conv ? conv.id : 'conv-merc-rec1')}'})"
                class="w-full py-2.5 rounded-[8px] bg-[#3D6943] text-white text-sm font-semibold flex items-center justify-center gap-1.5">
                <span class="material-symbols-outlined text-[18px]">chat</span>
                Nhắn tin với đội trưởng
              </button>
            ` : ''}
            ${canWithdraw ? `
              <button type="button" id="merc05-withdraw-btn"
                class="w-full py-2.5 rounded-[8px] border border-[#FECACA] bg-[#FEF2F2] text-[#DC2626] text-sm font-semibold">
                Rút đơn ứng tuyển ${isApproved ? '(Cảnh báo -10 uy tín)' : '(Miễn phạt)'}
              </button>
            ` : ''}
          </div>
        </div>
      `;

      const wBtn = container.querySelector('#merc05-withdraw-btn');
      if (wBtn) {
        wBtn.addEventListener('click', () => openWithdrawSheet(app.id));
      }
    }, { title: 'Chi tiết đơn ứng tuyển', tab: 'MERC-01', showBack: true, group: 'Đá lẻ', aliases: ['application-detail'] });

    /* MERC-06: Route fallback for withdraw */
    Router.register('MERC-06', (container, params) => {
      const state = Store.getState();
      const user = Store.getCurrentUser();
      const app = (state.applications || []).find((a) => a.id === (params && params.id)) ||
        (user && (state.applications || []).find((a) => a.applicantId === user.id && (a.status === 'PENDING' || a.status === 'APPROVED')));
      container.innerHTML = `
        <div class="p-4">
          ${app ? `
            <div class="bg-white rounded-[8px] border border-[#DDE5D9] p-4 space-y-3">
              <div class="font-bold text-sm text-[#111827]">Xác nhận rút đơn ứng tuyển</div>
              <p class="text-xs text-[#596158]">Trạng thái hiện tại: ${app.status === 'APPROVED' ? 'Đã duyệt (-10 uy tín nếu rút)' : 'Chờ duyệt (miễn phạt)'}</p>
              <button type="button" id="merc06-open-sheet" class="w-full py-2.5 rounded-[8px] bg-[#DC2626] text-white text-sm font-semibold">Mở xác nhận rút đơn</button>
            </div>
          ` : UI.emptyState({ icon: 'assignment', title: 'Không có đơn khả dụng để rút' })}
        </div>
      `;
      const btn = container.querySelector('#merc06-open-sheet');
      if (btn && app) btn.addEventListener('click', () => openWithdrawSheet(app.id));
    }, { title: 'Rút đơn đá lẻ', tab: 'MERC-01', showBack: true, group: 'Đá lẻ', aliases: ['application-withdraw'] });

    /* MERC-07: Chọn trận cần tuyển */
    Router.register('MERC-07', (container) => {
      const state = Store.getState();
      const actingTeam = Store.getActingTeam();
      if (!actingTeam) {
        container.innerHTML = `<div class="p-4">${UI.errorState({ title: 'Cần chọn đội bóng', message: 'Bạn cần là đội trưởng của một đội đang hoạt động để đăng tin tuyển.', code: '403' })}</div>`;
        return;
      }

      const confirmedMatches = (state.matches || []).filter(
        (m) => m.status === 'CONFIRMED' && (m.hostTeamId === actingTeam.id || m.guestTeamId === actingTeam.id)
      );
      const extMatches = (state.externalMatches || []).filter(
        (m) => m.teamId === actingTeam.id && m.status === 'SCHEDULED'
      );

      container.innerHTML = `
        <div class="p-4 space-y-4">
          <div class="bg-[#F1F5EC] border border-[#DDE5D9] rounded-[8px] p-3 flex items-center justify-between">
            <div class="text-xs">
              <div class="text-[#596158]">Đội đang thao tác</div>
              <div class="font-bold text-[#14532D] text-sm">${UI.esc(actingTeam.name)}</div>
            </div>
            <button type="button" onclick="window.ArenaRouter.navigate('EXT-01',{returnRoute:'MERC-07'})"
              class="px-3 py-1.5 rounded-[8px] bg-[#3D6943] text-white text-xs font-semibold">
              + Trận ngoài
            </button>
          </div>

          <div class="space-y-2">
            <h3 class="text-xs font-bold uppercase tracking-wider text-[#596158]">Kèo hệ thống đã chốt (CONFIRMED)</h3>
            ${confirmedMatches.length === 0 ? `<div class="text-xs text-[#596158] bg-white rounded-[8px] border border-[#DDE5D9] p-3">Chưa có kèo CONFIRMED nào.</div>` : confirmedMatches.map((m) => {
              const venue = (state.venues || []).find((v) => v.id === m.venueId);
              return `
                <div class="bg-white rounded-[8px] border border-[#DDE5D9] p-3.5 flex items-center justify-between gap-2">
                  <div class="min-w-0">
                    <div class="font-bold text-sm text-[#111827]">${UI.esc(m.matchDate)} • ${UI.esc(m.startTime)} - ${UI.esc(m.endTime)}</div>
                    <div class="text-xs text-[#596158] truncate">${UI.esc(venue ? venue.name : 'Sân bóng')}</div>
                  </div>
                  <button type="button" onclick="window.ArenaRouter.navigate('MERC-08',{matchId:'${UI.esc(m.id)}'})"
                    class="px-3 py-1.5 rounded-[8px] bg-[#3D6943] text-white text-xs font-semibold shrink-0">
                    Chọn trận
                  </button>
                </div>
              `;
            }).join('')}
          </div>

          <div class="space-y-2">
            <h3 class="text-xs font-bold uppercase tracking-wider text-[#596158]">Trận ngoài hệ thống</h3>
            ${extMatches.length === 0 ? `<div class="text-xs text-[#596158] bg-white rounded-[8px] border border-[#DDE5D9] p-3">Chưa có trận ngoài nào.</div>` : extMatches.map((ext) => {
              const venue = (state.venues || []).find((v) => v.id === ext.venueId);
              return `
                <div class="bg-white rounded-[8px] border border-[#DDE5D9] p-3.5 flex items-center justify-between gap-2">
                  <div class="min-w-0">
                    <div class="font-bold text-sm text-[#111827] truncate">${UI.esc(ext.title)}</div>
                    <div class="text-xs text-[#596158]">${UI.esc(ext.matchDate)} • ${UI.esc(ext.startTime)}-${UI.esc(ext.endTime)} • ${UI.esc(venue ? venue.name : 'Chưa chọn sân')}</div>
                  </div>
                  <button type="button" onclick="window.ArenaRouter.navigate('MERC-08',{externalMatchId:'${UI.esc(ext.id)}'})"
                    class="px-3 py-1.5 rounded-[8px] bg-[#3D6943] text-white text-xs font-semibold shrink-0">
                    Chọn trận
                  </button>
                </div>
              `;
            }).join('')}
          </div>
        </div>
      `;
    }, { title: 'Chọn trận cần tuyển', tab: 'TEAM-01', showBack: true, group: 'Đá lẻ', aliases: ['recruitment-select-match'] });

    /* MERC-08: Đăng tin tuyển (1-4 positions, 1-10 slots each) */
    Router.register('MERC-08', (container, params) => {
      const state = Store.getState();
      const actingTeam = Store.getActingTeam();
      if (!actingTeam) {
        container.innerHTML = `<div class="p-4">${UI.errorState({ title: 'Không có quyền', message: 'Chỉ đội trưởng mới được đăng tin tuyển người.', code: '403' })}</div>`;
        return;
      }

      const match = params && params.matchId ? (state.matches || []).find((m) => m.id === params.matchId) : null;
      const ext = params && params.externalMatchId ? (state.externalMatches || []).find((m) => m.id === params.externalMatchId) : null;
      const target = match || ext || (state.matches || []).find((m) => m.status === 'CONFIRMED');
      if (!target) {
        container.innerHTML = `<div class="p-4">${UI.emptyState({ icon: 'event_busy', title: 'Chưa chọn trận đấu', actionLabel: 'Chọn trận', onAction: "window.ArenaRouter.navigate('MERC-07')" })}</div>`;
        return;
      }

      let rows = [{ position: 'GOALKEEPER', quantity: 1 }];

      function renderForm() {
        const venue = (state.venues || []).find((v) => v.id === target.venueId);
        container.innerHTML = `
          <div class="p-4 space-y-3">
            <div class="bg-[#F1F5EC] border border-[#DDE5D9] rounded-[8px] p-3 text-xs space-y-1">
              <div class="font-bold text-[#14532D] text-sm">${UI.esc(target.title || 'Trận giao hữu hệ thống')}</div>
              <div class="text-[#596158]">${UI.esc(target.matchDate)} • ${UI.esc(target.startTime)} - ${UI.esc(target.endTime)}</div>
              <div class="text-[#596158]">Sân: ${UI.esc(venue ? venue.name : 'Chưa gắn sân')}</div>
            </div>

            <div class="bg-white rounded-[8px] border border-[#DDE5D9] p-3.5 space-y-3">
              <div class="flex items-center justify-between">
                <span class="font-bold text-sm text-[#111827]">Vị trí tuyển (1–4 vị trí)</span>
                ${rows.length < 4 ? `<button type="button" id="merc08-add-row" class="text-xs font-semibold text-[#3D6943]">+ Thêm vị trí</button>` : ''}
              </div>

              <div class="space-y-2.5">
                ${rows.map((r, idx) => `
                  <div class="flex items-center gap-2 bg-[#F8FAF3] p-2.5 rounded-[8px] border border-[#DDE5D9]">
                    <select data-idx="${idx}" class="merc08-pos flex-1 rounded-[8px] border border-[#DDE5D9] bg-white px-2.5 py-1.5 text-xs">
                      ${POSITIONS.map((p) => `<option value="${p.id}" ${r.position === p.id ? 'selected' : ''}>${UI.esc(p.label)}</option>`).join('')}
                    </select>
                    <input data-idx="${idx}" type="number" min="1" max="10" value="${r.quantity}"
                      class="merc08-qty w-16 rounded-[8px] border border-[#DDE5D9] bg-white px-2 py-1.5 text-xs text-center" />
                    ${rows.length > 1 ? `
                      <button type="button" data-idx="${idx}" class="merc08-del text-[#DC2626] p-1">
                        <span class="material-symbols-outlined text-[18px]">delete</span>
                      </button>
                    ` : ''}
                  </div>
                `).join('')}
              </div>

              <div>
                <label class="block text-xs font-semibold text-[#2D342C] mb-1">Ghi chú tuyển quân</label>
                <textarea id="merc08-note" rows="2" placeholder="Yêu cầu trình độ, hỗ trợ nước uống..."
                  class="w-full rounded-[8px] border border-[#DDE5D9] px-3 py-2 text-xs"></textarea>
              </div>
              <div id="merc08-err" class="hidden text-xs text-[#DC2626] font-medium"></div>
            </div>

            <button type="button" id="merc08-submit" class="w-full py-3 rounded-[8px] bg-[#3D6943] hover:bg-[#315C38] text-white text-sm font-semibold">
              Đăng tin tuyển người
            </button>
          </div>
        `;

        const addBtn = container.querySelector('#merc08-add-row');
        if (addBtn) {
          addBtn.addEventListener('click', () => {
            const used = rows.map((x) => x.position);
            const nextPos = POSITIONS.find((p) => !used.includes(p.id)) || POSITIONS[0];
            rows.push({ position: nextPos.id, quantity: 1 });
            renderForm();
          });
        }
        container.querySelectorAll('.merc08-pos').forEach((sel) => {
          sel.addEventListener('change', (e) => {
            rows[parseInt(sel.getAttribute('data-idx'), 10)].position = e.target.value;
          });
        });
        container.querySelectorAll('.merc08-qty').forEach((inp) => {
          inp.addEventListener('input', (e) => {
            rows[parseInt(inp.getAttribute('data-idx'), 10)].quantity = parseInt(e.target.value || '1', 10);
          });
        });
        container.querySelectorAll('.merc08-del').forEach((del) => {
          del.addEventListener('click', () => {
            rows.splice(parseInt(del.getAttribute('data-idx'), 10), 1);
            renderForm();
          });
        });

        const submitBtn = container.querySelector('#merc08-submit');
        if (submitBtn) {
          submitBtn.addEventListener('click', () => {
            const errEl = container.querySelector('#merc08-err');
            const posSet = new Set(rows.map((r) => r.position));
            if (posSet.size !== rows.length) {
              if (errEl) {
                errEl.textContent = 'Không được chọn trùng vị trí trong cùng một lần đăng.';
                errEl.classList.remove('hidden');
              }
              return;
            }
            if (rows.some((r) => isNaN(r.quantity) || r.quantity < 1 || r.quantity > 10)) {
              if (errEl) {
                errEl.textContent = 'Số lượng mỗi vị trí phải từ 1 đến 10 người.';
                errEl.classList.remove('hidden');
              }
              return;
            }
            const note = (container.querySelector('#merc08-note') || {}).value || '';
            const area = venue ? Store.getWardName(venue.wardId) : Store.getWardName(actingTeam.wardId);
            const created = rows.map((r, i) => ({
              id: 'rec-' + Date.now() + '-' + i,
              teamId: actingTeam.id,
              matchId: match ? target.id : undefined,
              externalMatchId: ext ? target.id : undefined,
              position: r.position,
              quantity: r.quantity,
              approvedCount: 0,
              status: 'OPEN',
              area,
              distanceKm: venue ? venue.distanceKm : 2.0,
              matchDate: target.matchDate,
              timeSlot: `${target.startTime} - ${target.endTime}`,
              venueId: target.venueId || null,
              skillLevel: target.skillLevel || 'INTERMEDIATE',
              note,
              createdAt: Store.getMockClock().toISOString()
            }));
            Store.setState((s) => ({ ...s, recruitments: [...created, ...(s.recruitments || [])] }));
            UI.toast(`Đã đăng ${created.length} tin tuyển người thành công!`, 'success');
            Router.navigate('MERC-09');
          });
        }
      }
      renderForm();
    }, { title: 'Đăng tin tuyển người', tab: 'TEAM-01', showBack: true, group: 'Đá lẻ', aliases: ['recruitment-create'] });

    /* MERC-09: Tin tuyển của đội */
    Router.register('MERC-09', (container) => {
      const state = Store.getState();
      const actingTeam = Store.getActingTeam();
      if (!actingTeam) {
        container.innerHTML = `<div class="p-4">${UI.errorState({ title: 'Chưa chọn đội', message: 'Vui lòng chọn đội bạn làm đội trưởng.', code: '403' })}</div>`;
        return;
      }

      const teamRecs = (state.recruitments || []).filter((r) => r.teamId === actingTeam.id);
      container.innerHTML = `
        <div class="p-4 space-y-3">
          <div class="flex items-center justify-between">
            <div>
              <div class="text-xs text-[#596158]">Tin tuyển của đội</div>
              <div class="font-bold text-sm text-[#14532D]">${UI.esc(actingTeam.name)}</div>
            </div>
            <button type="button" onclick="window.ArenaRouter.navigate('MERC-07')"
              class="px-3 py-1.5 rounded-[8px] bg-[#3D6943] text-white text-xs font-semibold">
              + Đăng tin mới
            </button>
          </div>

          ${teamRecs.length === 0 ? UI.emptyState({
            icon: 'campaign',
            title: 'Đội chưa có tin tuyển nào',
            description: 'Chọn một trận đấu đã chốt hoặc trận ngoài để tuyển thêm người đá lẻ.',
            actionLabel: 'Đăng tin tuyển',
            onAction: "window.ArenaRouter.navigate('MERC-07')"
          }) : `
            <div class="space-y-2.5">
              ${teamRecs.map((rec) => {
                const apps = (state.applications || []).filter((a) => a.recruitmentId === rec.id);
                const pendingCount = apps.filter((a) => a.status === 'PENDING').length;
                return `
                  <div class="bg-white rounded-[8px] border border-[#DDE5D9] p-3.5 space-y-2.5">
                    <div class="flex items-center justify-between">
                      ${UI.badge(posLabel(rec.position), 'info')}
                      ${recStatusBadge(rec.status)}
                    </div>
                    <div class="text-xs text-[#2D342C] font-semibold">
                      ${UI.esc(rec.matchDate)} • ${UI.esc(rec.timeSlot)}
                    </div>
                    <div class="flex items-center justify-between text-xs text-[#596158]">
                      <span>Đã duyệt: <strong>${rec.approvedCount || 0}/${rec.quantity}</strong></span>
                      <span>Chờ duyệt: <strong class="text-[#B45309]">${pendingCount}</strong></span>
                    </div>
                    <div class="flex gap-2 pt-2 border-t border-[#F1F5EC]">
                      <button type="button" onclick="window.ArenaRouter.navigate('MERC-10',{recId:'${UI.esc(rec.id)}'})"
                        class="flex-1 py-2 rounded-[8px] bg-[#3D6943] text-white text-xs font-semibold">
                        Ứng viên (${apps.length})
                      </button>
                      <button type="button" data-edit-rec="${UI.esc(rec.id)}"
                        class="merc09-edit-btn px-3 py-2 rounded-[8px] border border-[#DDE5D9] bg-white text-xs font-semibold text-[#2D342C]">
                        Sửa / Đóng
                      </button>
                    </div>
                  </div>
                `;
              }).join('')}
            </div>
          `}
        </div>
      `;

      container.querySelectorAll('.merc09-edit-btn').forEach((b) => {
        b.addEventListener('click', () => openEditRecruitmentSheet(b.getAttribute('data-edit-rec')));
      });
    }, { title: 'Tin tuyển của đội', tab: 'TEAM-01', showBack: true, group: 'Đá lẻ', aliases: ['team-recruitments'] });

    /* MERC-10: Danh sách ứng viên */
    Router.register('MERC-10', (container, params) => {
      const state = Store.getState();
      const actingTeam = Store.getActingTeam();
      const rec = (state.recruitments || []).find((r) => r.id === (params && params.recId)) ||
        (actingTeam && (state.recruitments || []).find((r) => r.teamId === actingTeam.id)) ||
        (state.recruitments || [])[0];
      if (!rec) {
        container.innerHTML = `<div class="p-4">${UI.emptyState({ icon: 'group', title: 'Chưa có tin tuyển nào' })}</div>`;
        return;
      }

      const apps = (state.applications || []).filter((a) => a.recruitmentId === rec.id);
      container.innerHTML = `
        <div class="p-4 space-y-3">
          <div class="bg-[#F1F5EC] border border-[#DDE5D9] rounded-[8px] p-3 flex items-center justify-between">
            <div class="text-xs">
              <div class="font-bold text-[#14532D]">${UI.esc(posLabel(rec.position))} • ${UI.esc(rec.matchDate)}</div>
              <div class="text-[#596158]">Đã duyệt: ${rec.approvedCount || 0}/${rec.quantity}</div>
            </div>
            ${recStatusBadge(rec.status)}
          </div>

          ${apps.length === 0 ? UI.emptyState({
            icon: 'person_off',
            title: 'Chưa có ứng viên nào',
            description: 'Khi có người chơi nộp đơn, danh sách sẽ hiển thị tại đây.'
          }) : `
            <div class="space-y-2.5">
              ${apps.map((app) => {
                const applicant = state.users[app.applicantId] || { displayName: 'Ứng viên', reputationScore: 80 };
                return `
                  <div class="bg-white rounded-[8px] border border-[#DDE5D9] p-3.5 space-y-2.5">
                    <div class="flex items-center justify-between gap-2">
                      <div class="flex items-center gap-2.5 min-w-0">
                        ${UI.avatar(applicant, { size: 40 })}
                        <div class="min-w-0">
                          <div class="font-bold text-sm text-[#111827] truncate">${UI.esc(applicant.displayName || 'Người chơi')}</div>
                          <div class="text-[11px] text-[#596158]">Uy tín cá nhân: <strong class="text-[#14532D]">${applicant.reputationScore || 100}/100</strong> • ${applicant.matchCount || 0} trận</div>
                        </div>
                      </div>
                      ${appStatusBadge(app.status)}
                    </div>
                    ${app.message ? `<p class="text-xs text-[#596158] bg-[#F8FAF3] p-2 rounded-[8px]">"${UI.esc(app.message)}"</p>` : ''}
                    <div class="flex gap-2">
                      <button type="button" onclick="window.ArenaRouter.navigate('MERC-11',{appId:'${UI.esc(app.id)}'})"
                        class="flex-1 py-2 rounded-[8px] bg-[#3D6943] text-white text-xs font-semibold">
                        ${app.status === 'PENDING' ? 'Xem & Duyệt' : 'Chi tiết đơn'}
                      </button>
                      <button type="button" onclick="window.ArenaRouter.navigate('CHAT-03',{convId:'conv-merc-${UI.esc(app.id)}'})"
                        class="px-3 py-2 rounded-[8px] border border-[#DDE5D9] bg-white text-xs font-semibold text-[#14532D]">
                        Chat
                      </button>
                    </div>
                  </div>
                `;
              }).join('')}
            </div>
          `}
        </div>
      `;
    }, { title: 'Danh sách ứng viên', tab: 'TEAM-01', showBack: true, group: 'Đá lẻ', aliases: ['recruitment-applicants'] });

    /* MERC-11: Chi tiết và duyệt ứng viên */
    Router.register('MERC-11', (container, params) => {
      const state = Store.getState();
      const app = (state.applications || []).find((a) => a.id === (params && params.appId)) || (state.applications || [])[0];
      if (!app) {
        container.innerHTML = `<div class="p-4">${UI.errorState({ title: 'Không tìm thấy ứng viên', code: '404' })}</div>`;
        return;
      }

      const rec = (state.recruitments || []).find((r) => r.id === app.recruitmentId) || {};
      const applicant = state.users[app.applicantId] || { displayName: 'Ứng viên', reputationScore: 90, matchCount: 5 };
      const isFull = (rec.approvedCount || 0) >= (rec.quantity || 1);

      container.innerHTML = `
        <div class="p-4 space-y-3">
          <div class="bg-white rounded-[8px] border border-[#DDE5D9] p-4 space-y-3">
            <div class="flex items-center justify-between">
              <span class="text-xs font-semibold text-[#596158]">Hồ sơ ứng viên</span>
              ${appStatusBadge(app.status)}
            </div>
            <div class="flex items-center gap-3">
              ${UI.avatar(applicant, { size: 52 })}
              <div>
                <h2 class="font-bold text-base text-[#111827]">${UI.esc(applicant.displayName)}</h2>
                <div class="text-xs text-[#596158]">Sở trường: <strong>${UI.esc(posLabel(applicant.preferredPosition))}</strong></div>
                <div class="text-xs text-[#3D6943] font-semibold">Uy tín: ${applicant.reputationScore}/100 • ${applicant.matchCount || 0} trận</div>
              </div>
            </div>
            <div class="bg-[#F8FAF3] rounded-[8px] p-3 text-xs">
              <div class="font-semibold text-[#2D342C] mb-1">Lời nhắn ứng tuyển</div>
              <p class="text-[#596158]">"${UI.esc(app.message || 'Không có lời nhắn.')}"</p>
            </div>
          </div>

          ${app.status === 'PENDING' ? `
            ${isFull ? `
              <div class="bg-[#FEF3C7] border border-[#FDE68A] rounded-[8px] p-3 text-xs text-[#92400E]">
                Tin tuyển đã đủ số lượng (${rec.approvedCount}/${rec.quantity}). Không thể duyệt thêm ứng viên.
              </div>
            ` : ''}
            <div class="flex gap-2">
              <button type="button" id="merc11-reject-btn"
                class="flex-1 py-2.5 rounded-[8px] border border-[#FECACA] bg-[#FEF2F2] text-[#DC2626] text-sm font-semibold">
                Từ chối
              </button>
              <button type="button" id="merc11-approve-btn"
                class="flex-1 py-2.5 rounded-[8px] ${isFull ? 'bg-[#DDE5D9] text-[#596158]' : 'bg-[#3D6943] text-white'} text-sm font-semibold"
                ${isFull ? 'disabled' : ''}>
                Duyệt ứng viên
              </button>
            </div>
          ` : app.status === 'APPROVED' ? `
            <div class="bg-[#F0FDF4] border border-[#BBF7D0] rounded-[8px] p-3 text-xs text-[#166534]">
              Ứng viên đã được duyệt và đã nhận thông tin sân bóng. Không thể hủy duyệt trực tiếp.
            </div>
          ` : ''}
        </div>
      `;

      const approveBtn = container.querySelector('#merc11-approve-btn');
      if (approveBtn && !isFull) {
        approveBtn.addEventListener('click', () => {
          Store.setState((s) => {
            const nextApps = s.applications.map((a) =>
              a.id === app.id ? { ...a, status: 'APPROVED', approvedAt: Store.getMockClock().toISOString() } : a
            );
            const nextRecs = s.recruitments.map((r) => {
              if (r.id !== rec.id) return r;
              const nextCount = (r.approvedCount || 0) + 1;
              return { ...r, approvedCount: nextCount, status: nextCount >= r.quantity ? 'FILLED' : r.status };
            });
            return { ...s, applications: nextApps, recruitments: nextRecs };
          });
          UI.toast('Đã duyệt ứng viên! Địa chỉ sân đã được mở cho ứng viên.', 'success');
          Router.refresh();
        });
      }

      const rejectBtn = container.querySelector('#merc11-reject-btn');
      if (rejectBtn) {
        rejectBtn.addEventListener('click', () => {
          Store.setState((s) => ({
            ...s,
            applications: s.applications.map((a) => (a.id === app.id ? { ...a, status: 'REJECTED' } : a))
          }));
          UI.toast('Đã từ chối đơn ứng tuyển.', 'info');
          Router.refresh();
        });
      }
    }, { title: 'Duyệt ứng viên', tab: 'TEAM-01', showBack: true, group: 'Đá lẻ', aliases: ['applicant-review'] });

    /* MERC-12: Route fallback for edit/close */
    Router.register('MERC-12', (container, params) => {
      const state = Store.getState();
      const rec = (state.recruitments || []).find((r) => r.id === (params && params.recId)) || (state.recruitments || [])[0];
      container.innerHTML = `<div class="p-4">${rec ? `<button type="button" id="merc12-open" class="w-full py-3 rounded-[8px] bg-[#3D6943] text-white text-sm font-semibold">Mở sửa số lượng / Đóng tin</button>` : UI.emptyState({ title: 'Không có tin tuyển' })}</div>`;
      const btn = container.querySelector('#merc12-open');
      if (btn && rec) btn.addEventListener('click', () => openEditRecruitmentSheet(rec.id));
    }, { title: 'Sửa / Đóng tin tuyển', tab: 'TEAM-01', showBack: true, group: 'Đá lẻ', aliases: ['recruitment-edit'] });

    /* =========================================================================
       EXT-01: Khai báo trận ngoài
       ========================================================================= */
    Router.register('EXT-01', (container, params) => {
      const state = Store.getState();
      const actingTeam = Store.getActingTeam();
      if (!actingTeam) {
        container.innerHTML = `<div class="p-4">${UI.errorState({ title: 'Cần chọn đội trưởng', message: 'Vui lòng chuyển sang vai Đội trưởng để khai báo trận ngoài.', code: '403' })}</div>`;
        return;
      }

      const draft = global.__arenaExtDraft || {
        title: '',
        opponentName: '',
        venueId: (params && (params.selectedVenueId || params.venueId)) || '',
        matchDate: '2026-11-23',
        startTime: '19:00',
        endTime: '20:30',
        skillLevel: 'INTERMEDIATE'
      };
      if (params && (params.selectedVenueId || params.venueId)) {
        draft.venueId = params.selectedVenueId || params.venueId;
      }
      global.__arenaExtDraft = draft;

      const selectedVenue = (state.venues || []).find((v) => v.id === draft.venueId);

      container.innerHTML = `
        <div class="p-4 space-y-3">
          <div class="bg-white rounded-[8px] border border-[#DDE5D9] p-4 space-y-3">
            <div>
              <label class="block text-xs font-semibold text-[#2D342C] mb-1">Tên / Mô tả trận đấu *</label>
              <input id="ext01-title" type="text" value="${UI.esc(draft.title)}" placeholder="VD: Giao hữu cúp Tứ Hùng"
                class="w-full rounded-[8px] border border-[#DDE5D9] px-3 py-2 text-sm" />
            </div>
            <div>
              <label class="block text-xs font-semibold text-[#2D342C] mb-1">Tên đối thủ (tùy chọn)</label>
              <input id="ext01-opp" type="text" value="${UI.esc(draft.opponentName)}" placeholder="VD: Bình Trưng FC"
                class="w-full rounded-[8px] border border-[#DDE5D9] px-3 py-2 text-sm" />
            </div>
            <div>
              <label class="block text-xs font-semibold text-[#2D342C] mb-1">Sân bóng (tùy chọn)</label>
              <div class="flex items-center justify-between gap-2 bg-[#F8FAF3] border border-[#DDE5D9] rounded-[8px] p-2.5">
                <span class="text-xs ${selectedVenue ? 'font-bold text-[#14532D]' : 'text-[#596158]'} truncate">
                  ${selectedVenue ? UI.esc(selectedVenue.name) : 'Chưa chọn sân bóng'}
                </span>
                <button type="button" id="ext01-pick-venue" class="px-3 py-1 rounded-[8px] bg-[#EAF0E6] text-[#14532D] text-xs font-semibold shrink-0">
                  Chọn sân
                </button>
              </div>
            </div>
            <div class="grid grid-cols-3 gap-2">
              <div class="col-span-3 sm:col-span-1">
                <label class="block text-xs font-semibold text-[#2D342C] mb-1">Ngày đá</label>
                <input id="ext01-date" type="date" value="${UI.esc(draft.matchDate)}" class="w-full rounded-[8px] border border-[#DDE5D9] px-2.5 py-2 text-xs" />
              </div>
              <div>
                <label class="block text-xs font-semibold text-[#2D342C] mb-1">Bắt đầu</label>
                <input id="ext01-start" type="time" value="${UI.esc(draft.startTime)}" class="w-full rounded-[8px] border border-[#DDE5D9] px-2.5 py-2 text-xs" />
              </div>
              <div>
                <label class="block text-xs font-semibold text-[#2D342C] mb-1">Kết thúc</label>
                <input id="ext01-end" type="time" value="${UI.esc(draft.endTime)}" class="w-full rounded-[8px] border border-[#DDE5D9] px-2.5 py-2 text-xs" />
              </div>
            </div>
            <div id="ext01-err" class="hidden text-xs text-[#DC2626] font-medium"></div>
          </div>

          <button type="button" id="ext01-submit" class="w-full py-3 rounded-[8px] bg-[#3D6943] text-white text-sm font-semibold">
            Lưu trận ngoài hệ thống
          </button>
        </div>
      `;

      const saveDraft = () => {
        draft.title = (container.querySelector('#ext01-title') || {}).value || '';
        draft.opponentName = (container.querySelector('#ext01-opp') || {}).value || '';
        draft.matchDate = (container.querySelector('#ext01-date') || {}).value || '2026-11-23';
        draft.startTime = (container.querySelector('#ext01-start') || {}).value || '19:00';
        draft.endTime = (container.querySelector('#ext01-end') || {}).value || '20:30';
      };

      const pickVenueBtn = container.querySelector('#ext01-pick-venue');
      if (pickVenueBtn) {
        pickVenueBtn.addEventListener('click', () => {
          saveDraft();
          Router.navigate('VEN-01', { picker: 'true', returnRoute: 'EXT-01', fieldKey: 'venueId' });
        });
      }

      const ext01SubmitBtn = container.querySelector('#ext01-submit');
      if (ext01SubmitBtn) {
        ext01SubmitBtn.addEventListener('click', () => {
        saveDraft();
        if (!draft.title.trim()) {
          const err = container.querySelector('#ext01-err');
          err.textContent = 'Vui lòng nhập tên trận đấu.';
          err.classList.remove('hidden');
          return;
        }
        const newExt = {
          id: 'ext-' + Date.now(),
          teamId: actingTeam.id,
          title: draft.title.trim(),
          opponentName: draft.opponentName.trim() || 'Đối thủ tự do',
          venueId: draft.venueId || null,
          matchDate: draft.matchDate,
          startTime: draft.startTime,
          endTime: draft.endTime,
          status: 'SCHEDULED',
          skillLevel: draft.skillLevel || 'INTERMEDIATE',
          createdAt: Store.getMockClock().toISOString()
        };
        Store.setState((s) => ({ ...s, externalMatches: [newExt, ...(s.externalMatches || [])] }));
        global.__arenaExtDraft = null;
        UI.toast('Đã khai báo trận ngoài thành công!', 'success');
        if (params && params.returnRoute) {
          Router.navigate(params.returnRoute);
        } else {
          Router.navigate('EXT-02', { id: newExt.id });
        }
        });
      }
    }, { title: 'Khai báo trận ngoài', tab: 'TEAM-01', showBack: true, group: 'Trận ngoài', aliases: ['external-create'] });

    /* EXT-02: Danh sách & Chi tiết trận ngoài */
    Router.register('EXT-02', (container, params) => {
      const state = Store.getState();
      const actingTeam = Store.getActingTeam();
      const extId = params && params.id;

      if (extId) {
        const ext = (state.externalMatches || []).find((m) => m.id === extId);
        if (!ext) {
          container.innerHTML = `<div class="p-4">${UI.errorState({ title: 'Không tìm thấy trận ngoài', code: '404' })}</div>`;
          return;
        }
        const venue = (state.venues || []).find((v) => v.id === ext.venueId);
        const linkedRecs = (state.recruitments || []).filter((r) => r.externalMatchId === ext.id);
        const isCancelled = ext.status === 'CANCELLED';

        container.innerHTML = `
          <div class="p-4 space-y-3">
            <div class="bg-white rounded-[8px] border border-[#DDE5D9] p-4 space-y-2.5">
              <div class="flex items-center justify-between">
                ${UI.badge(isCancelled ? 'Đã hủy' : 'Sắp diễn ra', isCancelled ? 'error' : 'primary')}
                <span class="text-xs text-[#596158]">Trận ngoài hệ thống</span>
              </div>
              <h2 class="font-bold text-base text-[#111827]">${UI.esc(ext.title)}</h2>
              <div class="text-xs text-[#596158] space-y-1">
                <div><strong>Đối thủ:</strong> ${UI.esc(ext.opponentName || 'Tự do')}</div>
                <div><strong>Thời gian:</strong> ${UI.esc(ext.matchDate)} • ${UI.esc(ext.startTime)} - ${UI.esc(ext.endTime)}</div>
                <div><strong>Sân:</strong> ${UI.esc(venue ? `${venue.name} (${venue.address})` : 'Chưa chọn sân')}</div>
              </div>
              ${!isCancelled ? `
                <div class="flex gap-2 pt-2 border-t border-[#F1F5EC]">
                  <button type="button" onclick="window.ArenaRouter.navigate('EXT-03',{id:'${UI.esc(ext.id)}'})"
                    class="flex-1 py-2 rounded-[8px] border border-[#DDE5D9] text-xs font-semibold text-[#2D342C]">
                    Sửa trận
                  </button>
                  <button type="button" id="ext02-cancel-btn"
                    class="flex-1 py-2 rounded-[8px] border border-[#FECACA] bg-[#FEF2F2] text-xs font-semibold text-[#DC2626]">
                    Hủy trận
                  </button>
                </div>
              ` : ''}
            </div>

            <div class="flex items-center justify-between">
              <h3 class="text-xs font-bold uppercase text-[#596158]">Tin tuyển đá lẻ đi kèm (${linkedRecs.length})</h3>
              ${!isCancelled ? `
                <button type="button" id="ext02-add-pos-btn" class="px-3 py-1.5 rounded-[8px] bg-[#3D6943] text-white text-xs font-semibold">
                  + Thêm vị trí
                </button>
              ` : ''}
            </div>

            ${linkedRecs.length === 0 ? UI.emptyState({ icon: 'person_add', title: 'Chưa có vị trí tuyển nào' }) : `
              <div class="space-y-2">
                ${linkedRecs.map((r) => `
                  <div class="bg-white rounded-[8px] border border-[#DDE5D9] p-3 flex items-center justify-between">
                    <div>
                      <div class="font-bold text-xs text-[#111827]">${UI.esc(posLabel(r.position))} (${r.approvedCount || 0}/${r.quantity})</div>
                      <div class="text-[11px] text-[#596158]">Trạng thái: ${UI.esc(r.status)}</div>
                    </div>
                    <button type="button" onclick="window.ArenaRouter.navigate('MERC-10',{recId:'${UI.esc(r.id)}'})"
                      class="px-2.5 py-1.5 rounded-[8px] bg-[#EAF0E6] text-[#14532D] text-xs font-semibold">
                      Ứng viên
                    </button>
                  </div>
                `).join('')}
              </div>
            `}
          </div>
        `;

        const cBtn = container.querySelector('#ext02-cancel-btn');
        if (cBtn) cBtn.addEventListener('click', () => openCancelExternalSheet(ext.id));
        const aBtn = container.querySelector('#ext02-add-pos-btn');
        if (aBtn) aBtn.addEventListener('click', () => openAddPositionSheet(ext.id));
        return;
      }

      // List view
      const list = actingTeam
        ? (state.externalMatches || []).filter((m) => m.teamId === actingTeam.id)
        : (state.externalMatches || []);

      container.innerHTML = `
        <div class="p-4 space-y-3">
          <div class="flex items-center justify-between">
            <span class="text-xs font-semibold text-[#596158]">Trận ngoài (${list.length})</span>
            <button type="button" onclick="window.ArenaRouter.navigate('EXT-01')"
              class="px-3 py-1.5 rounded-[8px] bg-[#3D6943] text-white text-xs font-semibold">
              + Khai báo trận ngoài
            </button>
          </div>
          ${list.length === 0 ? UI.emptyState({
            icon: 'event',
            title: 'Chưa có trận ngoài hệ thống',
            actionLabel: 'Khai báo ngay',
            onAction: "window.ArenaRouter.navigate('EXT-01')"
          }) : `
            <div class="space-y-2.5">
              ${list.map((m) => {
                const venue = (state.venues || []).find((v) => v.id === m.venueId);
                return `
                  <div onclick="window.ArenaRouter.navigate('EXT-02',{id:'${UI.esc(m.id)}'})"
                    class="bg-white rounded-[8px] border border-[#DDE5D9] p-3.5 cursor-pointer hover:border-[#3D6943] space-y-1.5">
                    <div class="flex items-center justify-between">
                      <span class="font-bold text-sm text-[#111827]">${UI.esc(m.title)}</span>
                      ${UI.badge(m.status === 'CANCELLED' ? 'Đã hủy' : 'Sắp đá', m.status === 'CANCELLED' ? 'error' : 'primary')}
                    </div>
                    <div class="text-xs text-[#596158]">${UI.esc(m.matchDate)} • ${UI.esc(m.startTime)}-${UI.esc(m.endTime)}</div>
                    <div class="text-xs text-[#3D6943]">${UI.esc(venue ? venue.name : 'Chưa chọn sân')}</div>
                  </div>
                `;
              }).join('')}
            </div>
          `}
        </div>
      `;
    }, { title: 'Trận ngoài hệ thống', tab: 'TEAM-01', showBack: true, group: 'Trận ngoài', aliases: ['external-matches', 'external-detail'] });

    /* EXT-03: Sửa trận ngoài (locks venue/time if approved mercenaries exist) */
    Router.register('EXT-03', (container, params) => {
      const state = Store.getState();
      const ext = (state.externalMatches || []).find((m) => m.id === (params && params.id)) || (state.externalMatches || [])[0];
      if (!ext) {
        container.innerHTML = `<div class="p-4">${UI.errorState({ title: 'Không tìm thấy trận ngoài', code: '404' })}</div>`;
        return;
      }

      const hasApproved = (state.recruitments || []).some(
        (r) => r.externalMatchId === ext.id && (r.approvedCount || 0) > 0
      );
      const selectedVenueId = (params && (params.selectedVenueId || params.venueId)) || ext.venueId;
      const venue = (state.venues || []).find((v) => v.id === selectedVenueId);

      container.innerHTML = `
        <div class="p-4 space-y-3">
          ${hasApproved ? `
            <div class="bg-[#FEF3C7] border border-[#FDE68A] rounded-[8px] p-3 text-xs text-[#92400E] flex items-start gap-2">
              <span class="material-symbols-outlined text-[18px] shrink-0">lock</span>
              <span>Trận đã có ứng viên đá lẻ được duyệt. <strong>Sân bóng và giờ thi đấu đã bị khóa</strong> để bảo vệ quyền lợi người chơi.</span>
            </div>
          ` : ''}
          <div class="bg-white rounded-[8px] border border-[#DDE5D9] p-4 space-y-3">
            <div>
              <label class="block text-xs font-semibold text-[#2D342C] mb-1">Tên trận đấu</label>
              <input id="ext03-title" type="text" value="${UI.esc(ext.title)}" class="w-full rounded-[8px] border border-[#DDE5D9] px-3 py-2 text-sm" />
            </div>
            <div>
              <label class="block text-xs font-semibold text-[#2D342C] mb-1">Đối thủ</label>
              <input id="ext03-opp" type="text" value="${UI.esc(ext.opponentName || '')}" class="w-full rounded-[8px] border border-[#DDE5D9] px-3 py-2 text-sm" />
            </div>
            <div>
              <label class="block text-xs font-semibold text-[#2D342C] mb-1">Sân bóng ${hasApproved ? '(Đã khóa)' : ''}</label>
              <div class="flex items-center justify-between bg-[#F8FAF3] border border-[#DDE5D9] rounded-[8px] p-2.5">
                <span class="text-xs font-semibold text-[#2D342C] truncate">${UI.esc(venue ? venue.name : 'Chưa chọn sân')}</span>
                ${!hasApproved ? `<button type="button" id="ext03-pick-venue" class="px-2.5 py-1 rounded-[8px] bg-[#EAF0E6] text-[#14532D] text-xs font-semibold">Đổi sân</button>` : ''}
              </div>
            </div>
            <div class="grid grid-cols-3 gap-2">
              <div class="col-span-3 sm:col-span-1">
                <label class="block text-xs font-semibold text-[#2D342C] mb-1">Ngày đá</label>
                <input id="ext03-date" type="date" value="${UI.esc(ext.matchDate)}" ${hasApproved ? 'disabled' : ''} class="w-full rounded-[8px] border border-[#DDE5D9] px-2.5 py-2 text-xs disabled:bg-[#F1F5EC]" />
              </div>
              <div>
                <label class="block text-xs font-semibold text-[#2D342C] mb-1">Bắt đầu</label>
                <input id="ext03-start" type="time" value="${UI.esc(ext.startTime)}" ${hasApproved ? 'disabled' : ''} class="w-full rounded-[8px] border border-[#DDE5D9] px-2.5 py-2 text-xs disabled:bg-[#F1F5EC]" />
              </div>
              <div>
                <label class="block text-xs font-semibold text-[#2D342C] mb-1">Kết thúc</label>
                <input id="ext03-end" type="time" value="${UI.esc(ext.endTime)}" ${hasApproved ? 'disabled' : ''} class="w-full rounded-[8px] border border-[#DDE5D9] px-2.5 py-2 text-xs disabled:bg-[#F1F5EC]" />
              </div>
            </div>
          </div>
          <button type="button" id="ext03-save" class="w-full py-3 rounded-[8px] bg-[#3D6943] text-white text-sm font-semibold">Lưu cập nhật</button>
        </div>
      `;

      const pickBtn = container.querySelector('#ext03-pick-venue');
      if (pickBtn) {
        pickBtn.addEventListener('click', () => {
          Router.navigate('VEN-01', { picker: 'true', returnRoute: 'EXT-03', fieldKey: 'venueId' });
        });
      }

      const ext03SaveBtn = container.querySelector('#ext03-save');
      if (ext03SaveBtn) {
        ext03SaveBtn.addEventListener('click', () => {
        const title = (container.querySelector('#ext03-title') || {}).value || ext.title;
        const opp = (container.querySelector('#ext03-opp') || {}).value || ext.opponentName;
        const date = hasApproved ? ext.matchDate : ((container.querySelector('#ext03-date') || {}).value || ext.matchDate);
        const start = hasApproved ? ext.startTime : ((container.querySelector('#ext03-start') || {}).value || ext.startTime);
        const end = hasApproved ? ext.endTime : ((container.querySelector('#ext03-end') || {}).value || ext.endTime);

        Store.setState((s) => ({
          ...s,
          externalMatches: s.externalMatches.map((m) =>
            m.id === ext.id ? { ...m, title, opponentName: opp, venueId: selectedVenueId, matchDate: date, startTime: start, endTime: end } : m
          )
        }));
        UI.toast('Đã lưu thay đổi trận ngoài.', 'success');
        Router.navigate('EXT-02', { id: ext.id });
        });
      }
    }, { title: 'Sửa trận ngoài', tab: 'TEAM-01', showBack: true, group: 'Trận ngoài', aliases: ['external-edit'] });

    /* EXT-04 & EXT-05 Route Fallbacks */
    Router.register('EXT-04', (container, params) => {
      const state = Store.getState();
      const ext = (state.externalMatches || []).find((m) => m.id === (params && params.id)) || (state.externalMatches || [])[0];
      container.innerHTML = `<div class="p-4">${ext ? `<button type="button" id="ext04-open" class="w-full py-3 rounded-[8px] bg-[#DC2626] text-white text-sm font-semibold">Mở xác nhận hủy trận ngoài</button>` : UI.emptyState({ title: 'Không có trận ngoài' })}</div>`;
      const btn = container.querySelector('#ext04-open');
      if (btn && ext) btn.addEventListener('click', () => openCancelExternalSheet(ext.id));
    }, { title: 'Hủy trận ngoài', tab: 'TEAM-01', showBack: true, group: 'Trận ngoài', aliases: ['external-cancel'] });

    Router.register('EXT-05', (container, params) => {
      const state = Store.getState();
      const ext = (state.externalMatches || []).find((m) => m.id === (params && params.id)) || (state.externalMatches || [])[0];
      container.innerHTML = `<div class="p-4">${ext ? `<button type="button" id="ext05-open" class="w-full py-3 rounded-[8px] bg-[#3D6943] text-white text-sm font-semibold">Thêm vị trí tuyển trận ngoài</button>` : UI.emptyState({ title: 'Không có trận ngoài' })}</div>`;
      const btn = container.querySelector('#ext05-open');
      if (btn && ext) btn.addEventListener('click', () => openAddPositionSheet(ext.id));
    }, { title: 'Thêm vị trí tuyển', tab: 'TEAM-01', showBack: true, group: 'Trận ngoài', aliases: ['external-add-position'] });
  }

  registerMercenaryRoutes();
})(window);
