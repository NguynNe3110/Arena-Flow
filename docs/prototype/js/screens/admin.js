/**
 * ArenaFlow Screens: Admin Portal (ADM-01..09)
 * Classic non-module script compatible with file:// protocol.
 * Enforces role ADMIN guard (non-admin receives 403 state).
 */
(function (global) {
  'use strict';

  const UI = global.ArenaUI;
  const Store = global.ArenaStore;
  const Router = global.ArenaRouter;

  function requireAdmin(container) {
    const user = Store.getCurrentUser();
    if (!user || user.role !== 'ADMIN') {
      container.innerHTML = `
        <div class="p-4">
          <div class="bg-white rounded-[12px] border border-[#FECACA] p-5 text-center space-y-3">
            <div class="w-14 h-14 mx-auto rounded-full bg-[#FEF2F2] text-[#DC2626] flex items-center justify-center">
              <span class="material-symbols-outlined text-[30px]">gpp_bad</span>
            </div>
            <h2 class="text-base font-bold text-[#991B1B]">Truy cập bị từ chối (403 Forbidden)</h2>
            <p class="text-xs text-[#596158] leading-relaxed">
              Khu vực này chỉ dành cho tài khoản có quyền <strong>ADMIN</strong> (Mã lỗi: <code>AUTH_004</code>).
            </p>
            <div class="p-2.5 rounded-[8px] bg-[#F8FAF3] border border-[#DDE5D9] text-[11px] text-[#596158]">
              Mẹo kiểm thử: Dùng bảng Demo bên phải để chuyển sang vai trò <strong>Admin</strong>.
            </div>
            <div class="flex items-center gap-2 pt-1">
              <button type="button" onclick="window.ArenaRouter.back()"
                      class="flex-1 py-2.5 rounded-[8px] border border-[#DDE5D9] bg-white text-xs font-semibold text-[#2D342C]">
                Quay lại
              </button>
              <button type="button" onclick="window.ArenaRouter.navigate('MATCH-01')"
                      class="flex-1 py-2.5 rounded-[8px] bg-[#3D6943] text-white text-xs font-semibold">
                Về trang chủ
              </button>
            </div>
          </div>
        </div>
      `;
      return false;
    }
    return true;
  }

  function formatDate(iso) {
    if (!iso) return '--';
    try {
      return new Date(iso).toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit', year: 'numeric' });
    } catch (e) {
      return iso;
    }
  }

  /* =========================================================================
     ADM-06: Resolve Dispute Sheet
     ========================================================================= */
  function openResolveDisputeSheet(reportId) {
    const state = Store.getState();
    const rep = (state.reports || []).find((r) => r.id === reportId);
    if (!rep) {
      UI.toast('Không tìm thấy tranh chấp.', 'error');
      return;
    }

    const bodyHtml = `
      <div class="space-y-3">
        <div class="p-3 rounded-[8px] bg-[#F8FAF3] border border-[#DDE5D9] text-xs space-y-1">
          <div><strong>Mã báo cáo:</strong> <span class="font-mono">${UI.esc(rep.id)}</span></div>
          <div><strong>Lý do gốc:</strong> ${UI.esc(rep.reason)}</div>
          ${rep.dispute ? `<div><strong>Giải trình khiếu nại:</strong> ${UI.esc(rep.dispute.note)}</div>` : ''}
        </div>

        <div>
          <label class="block text-xs font-semibold text-[#2D342C] mb-1.5">Quyết định của Admin *</label>
          <div class="space-y-2">
            <label class="flex items-center gap-2 p-2.5 rounded-[8px] border border-[#DDE5D9] bg-white text-xs cursor-pointer">
              <input type="radio" name="adm-decision" value="UPHELD" checked class="text-[#3D6943]" />
              <span><strong>Chấp thuận khiếu nại (UPHELD)</strong> — Hoàn điểm uy tín</span>
            </label>
            <label class="flex items-center gap-2 p-2.5 rounded-[8px] border border-[#DDE5D9] bg-white text-xs cursor-pointer">
              <input type="radio" name="adm-decision" value="REJECTED" class="text-[#3D6943]" />
              <span><strong>Bác bỏ khiếu nại (REJECTED)</strong> — Giữ nguyên xử phạt</span>
            </label>
          </div>
        </div>

        <div>
          <label class="block text-xs font-semibold text-[#2D342C] mb-1">Ghi chú quyết định (1–500 ký tự) *</label>
          <textarea id="adm-decision-note" rows="3" maxlength="500" placeholder="Nhập lý do quyết định..."
                    class="w-full rounded-[8px] border border-[#DDE5D9] px-3 py-2 text-xs bg-white"></textarea>
        </div>
      </div>
    `;

    const footerHtml = `
      <div class="flex items-center gap-2">
        <button type="button" onclick="window.ArenaUI.closeSheet()"
                class="flex-1 py-2.5 rounded-[8px] border border-[#DDE5D9] bg-white text-xs font-semibold">Hủy</button>
        <button type="button" id="btn-adm-resolve-save"
                class="flex-1 py-2.5 rounded-[8px] bg-[#3D6943] text-white text-xs font-semibold">Xác nhận quyết định</button>
      </div>
    `;

    UI.openSheet({ title: 'Quyết định tranh chấp (ADM-06)', bodyHtml, footerHtml });

    document.getElementById('btn-adm-resolve-save')?.addEventListener('click', () => {
      const decision = document.querySelector('input[name="adm-decision"]:checked')?.value || 'UPHELD';
      const note = (document.getElementById('adm-decision-note')?.value || '').trim();
      if (!note) {
        UI.toast('Vui lòng nhập ghi chú quyết định.', 'error');
        return;
      }

      const nextReports = (Store.getState().reports || []).map((r) => {
        if (r.id !== rep.id) return r;
        return {
          ...r,
          status: decision,
          adminDecisionNote: note,
          resolvedAt: Store.getMockClock().toISOString()
        };
      });
      Store.setState({ reports: nextReports });
      UI.closeSheet();
      UI.toast('Đã lưu quyết định xử lý tranh chấp.', 'success');
    });
  }

  /* =========================================================================
     ADM-08: Venue Status Toggle Sheet
     ========================================================================= */
  function openVenueStatusSheet(venueId) {
    const state = Store.getState();
    const venue = (state.venues || []).find((v) => v.id === venueId);
    if (!venue) {
      UI.toast('Không tìm thấy sân bóng.', 'error');
      return;
    }
    const nextStatus = venue.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE';

    const bodyHtml = `
      <div class="space-y-3">
        <div class="p-3 rounded-[8px] bg-[#F8FAF3] border border-[#DDE5D9] text-xs">
          <div class="font-bold text-[#111827]">${UI.esc(venue.name)}</div>
          <div class="text-[#596158] mt-0.5">${UI.esc(venue.address)}</div>
          <div class="mt-1">Trạng thái hiện tại: ${UI.badge(venue.status, venue.status === 'ACTIVE' ? 'success' : 'error')}</div>
        </div>
        <div>
          <label class="block text-xs font-semibold text-[#2D342C] mb-1">
            Lý do ${nextStatus === 'INACTIVE' ? 'tạm ngưng sân' : 'kích hoạt lại sân'} *
          </label>
          <textarea id="adm-venue-reason" rows="3" placeholder="Nhập lý do cập nhật trạng thái sân..."
                    class="w-full rounded-[8px] border border-[#DDE5D9] px-3 py-2 text-xs bg-white"></textarea>
        </div>
      </div>
    `;

    const footerHtml = `
      <div class="flex items-center gap-2">
        <button type="button" onclick="window.ArenaUI.closeSheet()"
                class="flex-1 py-2.5 rounded-[8px] border border-[#DDE5D9] bg-white text-xs font-semibold">Hủy</button>
        <button type="button" id="btn-adm-venue-save"
                class="flex-1 py-2.5 rounded-[8px] ${nextStatus === 'INACTIVE' ? 'bg-[#DC2626]' : 'bg-[#3D6943]'} text-white text-xs font-semibold">
          ${nextStatus === 'INACTIVE' ? 'Tắt sân (INACTIVE)' : 'Bật lại sân (ACTIVE)'}
        </button>
      </div>
    `;

    UI.openSheet({ title: 'Bật / tắt sân bóng (ADM-08)', bodyHtml, footerHtml });

    document.getElementById('btn-adm-venue-save')?.addEventListener('click', () => {
      const reason = (document.getElementById('adm-venue-reason')?.value || '').trim();
      if (!reason) {
        UI.toast('Vui lòng nhập lý do.', 'error');
        return;
      }
      const nextVenues = (Store.getState().venues || []).map((v) =>
        v.id === venue.id ? { ...v, status: nextStatus, warning: nextStatus === 'INACTIVE' ? reason : '' } : v
      );
      Store.setState({ venues: nextVenues });
      UI.closeSheet();
      UI.toast(`Đã chuyển sân sang ${nextStatus}.`, 'success');
    });
  }

  /* =========================================================================
     ADM-01: Trang quản trị
     ========================================================================= */
  Router.register('ADM-01', (container) => {
    if (!requireAdmin(container)) return;
    const state = Store.getState();
    const teams = state.teams || [];
    const reports = state.reports || [];
    const sports = state.sports || [];

    const activeTeams = teams.filter((t) => t.status === 'ACTIVE').length;
    const teamDisputes = reports.filter((r) => r.reporterType === 'TEAM' && (r.status === 'DISPUTED' || r.status === 'PENDING')).length;
    const mercDisputes = reports.filter((r) => r.reporterType === 'INDIVIDUAL' && (r.status === 'DISPUTED' || r.status === 'PENDING')).length;
    const venueReports = reports.filter((r) => r.reporterType === 'VENUE' && r.status === 'PENDING').length;

    const menuItems = [
      { route: 'ADM-02', icon: 'groups', title: 'Quản lý đội bóng (ADM-02)', desc: `${teams.length} đội • ${activeTeams} đang hoạt động` },
      { route: 'ADM-04', icon: 'gavel', title: 'Tranh chấp đội bóng (ADM-04)', desc: `${teamDisputes} hồ sơ chờ xử lý` },
      { route: 'ADM-05', icon: 'person_alert', title: 'Tranh chấp đá lẻ (ADM-05)', desc: `${mercDisputes} hồ sơ chờ xử lý` },
      { route: 'ADM-07', icon: 'stadium', title: 'Hàng chờ báo cáo sân (ADM-07)', desc: `${venueReports} phản ánh chờ duyệt` },
      { route: 'ADM-09', icon: 'sports', title: 'Danh mục bộ môn (ADM-09)', desc: `${sports.filter((s) => s.active).length}/${sports.length} bộ môn đang bật` }
    ];

    container.innerHTML = `
      <div class="p-4 space-y-4">
        <div class="bg-[#14532D] text-white rounded-[12px] p-4 flex items-center justify-between">
          <div>
            <div class="flex items-center gap-2">
              <h2 class="text-base font-bold">Cổng quản trị ArenaFlow</h2>
              <span class="px-2 py-0.5 rounded-full bg-white/20 text-[10px] font-bold">ADMIN</span>
            </div>
            <p class="text-xs text-white/80 mt-1">Giờ hệ thống: ${formatDate(state.meta.mockClock)}</p>
          </div>
          <span class="material-symbols-outlined text-[30px] text-white/80">admin_panel_settings</span>
        </div>

        <div class="grid grid-cols-2 gap-2.5">
          <div class="bg-white rounded-[8px] border border-[#DDE5D9] p-3 text-center">
            <div class="text-xl font-bold text-[#3D6943]">${teams.length}</div>
            <div class="text-[11px] text-[#596158]">Tổng đội bóng</div>
          </div>
          <div class="bg-white rounded-[8px] border border-[#DDE5D9] p-3 text-center">
            <div class="text-xl font-bold text-[#B45309]">${teamDisputes + mercDisputes + venueReports}</div>
            <div class="text-[11px] text-[#596158]">Hồ sơ chờ duyệt</div>
          </div>
        </div>

        <div class="space-y-2.5">
          ${menuItems.map((m) => `
            <button type="button" onclick="window.ArenaRouter.navigate('${m.route}')"
                    class="w-full bg-white rounded-[8px] border border-[#DDE5D9] p-3 flex items-center justify-between gap-3 text-left hover:border-[#3D6943] transition-colors">
              <div class="flex items-center gap-3 min-w-0">
                <div class="w-10 h-10 rounded-[8px] bg-[#EAF0E6] text-[#3D6943] flex items-center justify-center shrink-0">
                  <span class="material-symbols-outlined text-[20px]">${m.icon}</span>
                </div>
                <div class="min-w-0">
                  <div class="text-xs font-bold text-[#111827] truncate">${UI.esc(m.title)}</div>
                  <div class="text-[11px] text-[#596158] truncate">${UI.esc(m.desc)}</div>
                </div>
              </div>
              <span class="material-symbols-outlined text-[18px] text-[#757D73]">chevron_right</span>
            </button>
          `).join('')}
        </div>
      </div>
    `;
  }, {
    title: 'Quản trị hệ thống',
    showBack: true,
    group: 'Quản trị Admin',
    role: 'ADMIN',
    aliases: ['admin']
  });

  /* =========================================================================
     ADM-02: Quản lý đội (mark-inactive / reactivate)
     ========================================================================= */
  Router.register('ADM-02', (container) => {
    if (!requireAdmin(container)) return;
    let query = '';
    let statusFilter = 'ALL';

    function openToggleTeamSheet(team) {
      const nextStatus = team.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE';
      UI.openSheet({
        title: nextStatus === 'INACTIVE' ? 'Tạm ngưng đội bóng' : 'Kích hoạt lại đội bóng',
        bodyHtml: `
          <div class="space-y-3">
            <p class="text-xs text-[#596158]">Đội: <strong>${UI.esc(team.name)}</strong></p>
            <div>
              <label class="block text-xs font-semibold text-[#2D342C] mb-1">Lý do thao tác *</label>
              <textarea id="adm-team-reason" rows="3" placeholder="Nhập lý do..."
                        class="w-full rounded-[8px] border border-[#DDE5D9] px-3 py-2 text-xs bg-white"></textarea>
            </div>
          </div>
        `,
        footerHtml: `
          <div class="flex gap-2">
            <button type="button" onclick="window.ArenaUI.closeSheet()" class="flex-1 py-2 rounded-[8px] border border-[#DDE5D9] bg-white text-xs font-semibold">Hủy</button>
            <button type="button" id="btn-adm-team-confirm" class="flex-1 py-2 rounded-[8px] ${nextStatus === 'INACTIVE' ? 'bg-[#DC2626]' : 'bg-[#3D6943]'} text-white text-xs font-semibold">Xác nhận</button>
          </div>
        `
      });
      document.getElementById('btn-adm-team-confirm')?.addEventListener('click', () => {
        const reason = (document.getElementById('adm-team-reason')?.value || '').trim();
        if (!reason) {
          UI.toast('Vui lòng nhập lý do.', 'error');
          return;
        }
        const nextTeams = (Store.getState().teams || []).map((t) =>
          t.id === team.id ? { ...t, status: nextStatus, inactiveReason: reason } : t
        );
        Store.setState({ teams: nextTeams });
        UI.closeSheet();
        UI.toast(`Đã chuyển ${team.name} sang ${nextStatus}.`, 'success');
      });
    }

    function render() {
      const teams = (Store.getState().teams || []).filter((t) => {
        if (statusFilter !== 'ALL' && t.status !== statusFilter) return false;
        if (query && !t.name.toLowerCase().includes(query.toLowerCase())) return false;
        return true;
      });

      container.innerHTML = `
        <div class="p-4 space-y-3">
          <input id="adm02-search" type="text" value="${UI.esc(query)}" placeholder="Tìm tên đội bóng..."
                 class="w-full rounded-[8px] border border-[#DDE5D9] px-3 py-2 text-xs bg-white" />

          <div class="flex gap-1.5 overflow-x-auto no-scrollbar">
            ${['ALL', 'ACTIVE', 'INACTIVE', 'DISBANDED'].map((st) => `
              <button type="button" data-team-st="${st}"
                      class="px-3 py-1.5 rounded-full text-xs font-semibold border ${statusFilter === st ? 'bg-[#3D6943] text-white border-[#3D6943]' : 'bg-white text-[#596158] border-[#DDE5D9]'}">
                ${st}
              </button>
            `).join('')}
          </div>

          <div class="space-y-2.5">
            ${teams.map((t) => `
              <div class="bg-white rounded-[8px] border border-[#DDE5D9] p-3 space-y-2">
                <div class="flex items-center justify-between gap-2">
                  <div class="flex items-center gap-2.5 min-w-0">
                    ${UI.avatar(t, { size: 36 })}
                    <div class="min-w-0">
                      <div class="text-xs font-bold text-[#111827] truncate">${UI.esc(t.name)}</div>
                      <div class="text-[11px] text-[#596158]">Uy tín: ${t.reputationScore} • ${t.matchCount} trận</div>
                    </div>
                  </div>
                  ${UI.badge(t.status, t.status === 'ACTIVE' ? 'success' : (t.status === 'INACTIVE' ? 'warning' : 'neutral'))}
                </div>
                <div class="flex gap-2 pt-1">
                  <button type="button" data-adm-history="${UI.esc(t.id)}"
                          class="flex-1 py-1.5 rounded-[8px] bg-[#EAF0E6] text-[#3D6943] text-[11px] font-semibold">
                    Lịch sử thành viên (ADM-03)
                  </button>
                  ${t.status !== 'DISBANDED' ? `
                    <button type="button" data-adm-toggle-team="${UI.esc(t.id)}"
                            class="px-3 py-1.5 rounded-[8px] border ${t.status === 'ACTIVE' ? 'border-[#FECACA] text-[#DC2626]' : 'border-[#BBF7D0] text-[#16A34A]'} text-[11px] font-semibold">
                      ${t.status === 'ACTIVE' ? 'Tạm ngưng' : 'Mở lại'}
                    </button>
                  ` : ''}
                </div>
              </div>
            `).join('')}
          </div>
        </div>
      `;

      container.querySelector('#adm02-search')?.addEventListener('input', (e) => {
        query = e.target.value;
        render();
      });
      container.querySelectorAll('[data-team-st]').forEach((btn) => {
        btn.addEventListener('click', () => {
          statusFilter = btn.getAttribute('data-team-st');
          render();
        });
      });
      container.querySelectorAll('[data-adm-history]').forEach((btn) => {
        btn.addEventListener('click', () => {
          Router.navigate('ADM-03', { teamId: btn.getAttribute('data-adm-history') });
        });
      });
      container.querySelectorAll('[data-adm-toggle-team]').forEach((btn) => {
        btn.addEventListener('click', () => {
          const team = (Store.getState().teams || []).find((x) => x.id === btn.getAttribute('data-adm-toggle-team'));
          if (team) openToggleTeamSheet(team);
        });
      });
    }

    render();
  }, {
    title: 'Quản lý đội bóng',
    showBack: true,
    group: 'Quản trị Admin',
    role: 'ADMIN',
    aliases: ['admin-teams']
  });

  /* =========================================================================
     ADM-03: Lịch sử thành viên quản trị (real name + deleted flag)
     ========================================================================= */
  Router.register('ADM-03', (container, params) => {
    if (!requireAdmin(container)) return;
    const state = Store.getState();
    const teamId = (params && params.teamId) || 'team-alpha';
    const team = (state.teams || []).find((t) => t.id === teamId) || state.teams[0];
    const members = (state.teamMembers || []).filter((m) => m.teamId === (team && team.id));

    container.innerHTML = `
      <div class="p-4 space-y-3">
        <div class="bg-white rounded-[8px] border border-[#DDE5D9] p-3">
          <h2 class="text-sm font-bold text-[#111827]">Lịch sử thành viên: ${UI.esc(team ? team.name : '')}</h2>
          <p class="text-[11px] text-[#596158] mt-0.5">
            Hiển thị tên thật kèm cờ <strong>[Đã xóa]</strong> cho tài khoản đã xóa (ADR-0002).
          </p>
        </div>

        <div class="space-y-2">
          ${members.map((m) => {
            const u = state.users ? state.users[m.userId] : null;
            const realName = (u && (u.realName || u.displayName)) || m.userId;
            return `
              <div class="bg-white rounded-[8px] border border-[#DDE5D9] p-3 flex items-center justify-between gap-2">
                <div class="flex items-center gap-2.5 min-w-0">
                  ${UI.avatar({ displayName: realName }, { size: 36 })}
                  <div class="min-w-0">
                    <div class="flex items-center gap-1.5 flex-wrap">
                      <span class="text-xs font-bold text-[#111827]">${UI.esc(realName)}</span>
                      ${u && u.isDeleted ? UI.badge('Đã xóa', 'error') : ''}
                      ${UI.badge(m.role, 'neutral')}
                    </div>
                    <div class="text-[11px] text-[#596158]">
                      Tham gia: ${formatDate(m.joinedAt)} ${m.leftAt ? '• Rời đội: ' + formatDate(m.leftAt) : ''}
                    </div>
                  </div>
                </div>
                ${UI.badge(m.status, m.status === 'ACTIVE' ? 'success' : 'neutral')}
              </div>
            `;
          }).join('')}
        </div>
      </div>
    `;
  }, {
    title: 'Lịch sử thành viên (Admin)',
    showBack: true,
    group: 'Quản trị Admin',
    role: 'ADMIN',
    aliases: ['admin-team-history']
  });

  /* =========================================================================
     ADM-04 & ADM-05: Hàng chờ tranh chấp (Đội & Đá lẻ)
     ========================================================================= */
  function renderDisputeQueue(container, scope) {
    if (!requireAdmin(container)) return;
    let tab = 'PENDING';

    function render() {
      const reports = (Store.getState().reports || []).filter((r) =>
        scope === 'TEAM' ? r.reporterType === 'TEAM' : r.reporterType === 'INDIVIDUAL'
      );
      const list = reports.filter((r) =>
        tab === 'PENDING'
          ? (r.status === 'DISPUTED' || r.status === 'PENDING' || r.status === 'AUTO_ACCEPTED')
          : (r.status === 'UPHELD' || r.status === 'REJECTED' || r.status === 'RESOLVED')
      );

      container.innerHTML = `
        <div class="p-4 space-y-3">
          <div class="flex gap-1.5">
            <button type="button" data-qtab="PENDING"
                    class="px-3 py-1.5 rounded-full text-xs font-semibold border ${tab === 'PENDING' ? 'bg-[#3D6943] text-white border-[#3D6943]' : 'bg-white text-[#596158] border-[#DDE5D9]'}">
              Chờ xử lý
            </button>
            <button type="button" data-qtab="RESOLVED"
                    class="px-3 py-1.5 rounded-full text-xs font-semibold border ${tab === 'RESOLVED' ? 'bg-[#3D6943] text-white border-[#3D6943]' : 'bg-white text-[#596158] border-[#DDE5D9]'}">
              Đã xử lý
            </button>
          </div>

          ${list.length === 0 ? UI.emptyState({
            icon: 'task_alt',
            title: 'Hàng chờ trống',
            description: 'Không có hồ sơ tranh chấp nào trong mục này.'
          }) : `
            <div class="space-y-2.5">
              ${list.map((r) => `
                <div class="bg-white rounded-[8px] border border-[#DDE5D9] p-3 space-y-2">
                  <div class="flex items-center justify-between gap-2">
                    <span class="text-xs font-bold text-[#111827]">${UI.esc(r.type)} • ${UI.esc(r.id)}</span>
                    ${UI.badge(r.status, r.status === 'DISPUTED' ? 'warning' : 'neutral')}
                  </div>
                  <p class="text-xs text-[#596158]">${UI.esc(r.reason)}</p>
                  ${r.adminDecisionNote ? `<p class="text-[11px] text-[#3D6943]">Ghi chú: ${UI.esc(r.adminDecisionNote)}</p>` : ''}
                  <div class="flex gap-2 pt-1">
                    <button type="button" data-open-resolve="${UI.esc(r.id)}"
                            class="flex-1 py-2 rounded-[8px] bg-[#3D6943] text-white text-xs font-semibold">
                      Xem xét & quyết định (ADM-06)
                    </button>
                  </div>
                </div>
              `).join('')}
            </div>
          `}
        </div>
      `;

      container.querySelectorAll('[data-qtab]').forEach((btn) => {
        btn.addEventListener('click', () => {
          tab = btn.getAttribute('data-qtab');
          render();
        });
      });
      container.querySelectorAll('[data-open-resolve]').forEach((btn) => {
        btn.addEventListener('click', () => {
          openResolveDisputeSheet(btn.getAttribute('data-open-resolve'));
        });
      });
    }

    render();
  }

  Router.register('ADM-04', (container) => renderDisputeQueue(container, 'TEAM'), {
    title: 'Hàng chờ tranh chấp đội',
    showBack: true,
    group: 'Quản trị Admin',
    role: 'ADMIN',
    aliases: ['admin-team-disputes']
  });

  Router.register('ADM-05', (container) => renderDisputeQueue(container, 'MERC'), {
    title: 'Hàng chờ tranh chấp đá lẻ',
    showBack: true,
    group: 'Quản trị Admin',
    role: 'ADMIN',
    aliases: ['admin-merc-disputes']
  });

  /* =========================================================================
     ADM-06: Route wrapper for Xem xét & quyết định tranh chấp
     ========================================================================= */
  Router.register('ADM-06', (container, params) => {
    if (!requireAdmin(container)) return;
    const reports = Store.getState().reports || [];
    const targetId = (params && params.reportId) || (reports[0] && reports[0].id);
    container.innerHTML = `
      <div class="p-4 space-y-3">
        <div class="bg-white rounded-[8px] border border-[#DDE5D9] p-4 space-y-3">
          <h2 class="text-sm font-bold text-[#111827]">Xem xét & quyết định tranh chấp (ADM-06)</h2>
          <p class="text-xs text-[#596158]">Mở sheet ra quyết định UPHELD / REJECTED kèm ghi chú bắt buộc.</p>
          <button type="button" id="btn-adm06-open"
                  class="w-full py-2.5 rounded-[8px] bg-[#3D6943] text-white text-xs font-semibold">
            Mở Sheet quyết định
          </button>
        </div>
      </div>
    `;
    container.querySelector('#btn-adm06-open')?.addEventListener('click', () => openResolveDisputeSheet(targetId));
  }, {
    title: 'Quyết định tranh chấp',
    showBack: true,
    group: 'Quản trị Admin',
    role: 'ADMIN',
    aliases: ['admin-resolve-dispute']
  });

  /* =========================================================================
     ADM-07: Hàng chờ báo cáo sân
     ========================================================================= */
  Router.register('ADM-07', (container) => {
    if (!requireAdmin(container)) return;
    const state = Store.getState();
    const reports = (state.reports || []).filter((r) => r.reporterType === 'VENUE');

    container.innerHTML = `
      <div class="p-4 space-y-3">
        ${reports.length === 0 ? UI.emptyState({ title: 'Không có báo cáo sân nào' }) : `
          <div class="space-y-2.5">
            ${reports.map((r) => {
              const venue = (state.venues || []).find((v) => v.id === r.reportedId);
              return `
                <div class="bg-white rounded-[8px] border border-[#DDE5D9] p-3 space-y-2">
                  <div class="flex items-center justify-between gap-2">
                    <span class="text-xs font-bold text-[#111827]">${UI.esc(venue ? venue.name : r.reportedId)}</span>
                    ${UI.badge(r.status, r.status === 'PENDING' ? 'warning' : 'success')}
                  </div>
                  <p class="text-xs text-[#596158]">${UI.esc(r.reason)}</p>
                  <div class="flex gap-2">
                    <button type="button" data-venue-toggle="${UI.esc(r.reportedId)}"
                            class="flex-1 py-2 rounded-[8px] bg-[#EAF0E6] text-[#3D6943] text-xs font-semibold">
                      Bật / tắt sân (ADM-08)
                    </button>
                    ${r.status === 'PENDING' ? `
                      <button type="button" data-venue-resolve="${UI.esc(r.id)}"
                              class="px-3 py-2 rounded-[8px] bg-[#3D6943] text-white text-xs font-semibold">
                        Đánh dấu đã xử lý
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

    container.querySelectorAll('[data-venue-toggle]').forEach((btn) => {
      btn.addEventListener('click', () => openVenueStatusSheet(btn.getAttribute('data-venue-toggle')));
    });
    container.querySelectorAll('[data-venue-resolve]').forEach((btn) => {
      btn.addEventListener('click', () => {
        const rid = btn.getAttribute('data-venue-resolve');
        const nextReports = (Store.getState().reports || []).map((r) =>
          r.id === rid ? { ...r, status: 'RESOLVED' } : r
        );
        Store.setState({ reports: nextReports });
        UI.toast('Đã xử lý phản ánh sân.', 'success');
      });
    });
  }, {
    title: 'Hàng chờ báo cáo sân',
    showBack: true,
    group: 'Quản trị Admin',
    role: 'ADMIN',
    aliases: ['admin-venue-reports']
  });

  /* =========================================================================
     ADM-08: Bật / tắt sân (route wrapper)
     ========================================================================= */
  Router.register('ADM-08', (container, params) => {
    if (!requireAdmin(container)) return;
    const venues = Store.getState().venues || [];
    const targetId = (params && params.venueId) || (venues[0] && venues[0].id);

    container.innerHTML = `
      <div class="p-4 space-y-2.5">
        ${venues.map((v) => `
          <div class="bg-white rounded-[8px] border border-[#DDE5D9] p-3 flex items-center justify-between gap-2">
            <div class="min-w-0">
              <div class="text-xs font-bold text-[#111827] truncate">${UI.esc(v.name)}</div>
              <div class="text-[11px] text-[#596158] truncate">${UI.esc(v.address)}</div>
              <div class="mt-1">${UI.badge(v.status, v.status === 'ACTIVE' ? 'success' : 'error')}</div>
            </div>
            <button type="button" data-adm08-venue="${UI.esc(v.id)}"
                    class="px-3 py-2 rounded-[8px] bg-[#3D6943] text-white text-xs font-semibold shrink-0">
              Đổi trạng thái
            </button>
          </div>
        `).join('')}
      </div>
    `;

    container.querySelectorAll('[data-adm08-venue]').forEach((btn) => {
      btn.addEventListener('click', () => openVenueStatusSheet(btn.getAttribute('data-adm08-venue') || targetId));
    });
  }, {
    title: 'Bật / tắt sân bóng',
    showBack: true,
    group: 'Quản trị Admin',
    role: 'ADMIN',
    aliases: ['admin-venue-status']
  });

  /* =========================================================================
     ADM-09: Danh mục bộ môn
     ========================================================================= */
  Router.register('ADM-09', (container) => {
    if (!requireAdmin(container)) return;
    let query = '';

    function render() {
      const sports = (Store.getState().sports || []).filter((s) =>
        !query || s.name.toLowerCase().includes(query.toLowerCase())
      );

      container.innerHTML = `
        <div class="p-4 space-y-3">
          <div class="p-2.5 rounded-[8px] bg-[#EAF0E6] border border-[#DDE5D9] text-[11px] text-[#2D342C]">
            Admin có thể tìm kiếm và bật/tắt bộ môn hiện có (không tạo enum mới).
          </div>
          <input id="adm09-search" type="text" value="${UI.esc(query)}" placeholder="Tìm bộ môn..."
                 class="w-full rounded-[8px] border border-[#DDE5D9] px-3 py-2 text-xs bg-white" />
          <div class="space-y-2">
            ${sports.map((s) => `
              <div class="bg-white rounded-[8px] border border-[#DDE5D9] p-3 flex items-center justify-between gap-2">
                <div>
                  <div class="text-xs font-bold text-[#111827]">${UI.esc(s.name)}</div>
                  <div class="text-[10px] font-mono text-[#596158]">${UI.esc(s.id)}</div>
                </div>
                <button type="button" data-toggle-sport="${UI.esc(s.id)}"
                        class="px-3 py-1.5 rounded-[8px] text-xs font-semibold ${s.active ? 'bg-[#F0FDF4] text-[#16A34A] border border-[#BBF7D0]' : 'bg-[#FEF2F2] text-[#DC2626] border border-[#FECACA]'}">
                  ${s.active ? 'Đang bật' : 'Đang tắt'}
                </button>
              </div>
            `).join('')}
          </div>
        </div>
      `;

      container.querySelector('#adm09-search')?.addEventListener('input', (e) => {
        query = e.target.value;
        render();
      });
      container.querySelectorAll('[data-toggle-sport]').forEach((btn) => {
        btn.addEventListener('click', () => {
          const sid = btn.getAttribute('data-toggle-sport');
          const nextSports = (Store.getState().sports || []).map((s) =>
            s.id === sid ? { ...s, active: !s.active } : s
          );
          Store.setState({ sports: nextSports });
          UI.toast('Đã cập nhật trạng thái bộ môn.', 'success');
        });
      });
    }

    render();
  }, {
    title: 'Danh mục bộ môn',
    showBack: true,
    group: 'Quản trị Admin',
    role: 'ADMIN',
    aliases: ['admin-sports']
  });

  global.ArenaAdmin = { openResolveDisputeSheet, openVenueStatusSheet };
})(window);
