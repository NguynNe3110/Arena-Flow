/**
 * ArenaFlow Prototype — Admin Module (ADM-01..ADM-09)
 * Views:
 *   - ADM-01: admin (Admin portal dashboard)
 *   - ADM-02: admin-teams (Team moderation & search)
 *   - ADM-03: admin-team-history (Team member history with real names & deleted flags)
 *   - ADM-04: admin-team-disputes (Team dispute queue)
 *   - ADM-05: admin-merc-disputes (Mercenary dispute queue)
 *   - ADM-07: admin-venue-reports (Venue report queue)
 *   - ADM-09: admin-sports (Sports management)
 * Sheets / Dialogs:
 *   - ADM-06: admin-resolve-dispute-sheet (Resolve dispute sheet with mandatory decision note 1-500 chars)
 *   - ADM-08: admin-venue-status-dialog (Venue status toggle)
 *   - admin-team-suspend-dialog (Team mark-inactive with reason)
 * Complies with ADR-0002 (soft delete / real names for admin), AC-P08, J-21, J-22
 * Owned by @ui-rep-admin
 */

export function initAdminViews(router, store) {

  /* =========================================================================
     HELPER UTILITIES
     ========================================================================= */

  function escapeHtml(str) {
    if (!str) return '';
    return String(str)
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;');
  }

  function formatDate(isoStr) {
    if (!isoStr) return '--';
    try {
      const d = new Date(isoStr);
      return d.toLocaleDateString('vi-VN', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric'
      });
    } catch (e) {
      return isoStr;
    }
  }

  function formatDateTime(isoStr) {
    if (!isoStr) return '--';
    try {
      const d = new Date(isoStr);
      return d.toLocaleString('vi-VN', {
        hour: '2-digit',
        minute: '2-digit',
        day: '2-digit',
        month: '2-digit',
        year: 'numeric'
      });
    } catch (e) {
      return isoStr;
    }
  }

  /**
   * Enforces ADMIN role security per ADM-01
   * If user is not ADMIN, renders 403 Forbidden screen and returns false.
   */
  function requireAdminRole(container) {
    const user = store.getCurrentUser();
    if (user?.role !== 'ADMIN') {
      router.setHeaderTitle('Từ chối truy cập (403)');
      container.innerHTML = `
        <div class="empty-state" style="padding: 32px 16px;">
          <div class="empty-state-icon" style="background-color: var(--color-error-container); color: var(--color-error);">
            <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <rect x="3" y="11" width="18" height="11" rx="2" ry="2"></rect>
              <path d="M7 11V7a5 5 0 0 1 10 0v4"></path>
            </svg>
          </div>
          <h3 class="empty-state-title">Khu vực quản trị bị khóa</h3>
          <p class="empty-state-desc" style="max-width: 320px; margin-inline: auto;">
            Chỉ tài khoản có quyền <strong>ADMIN</strong> mới được phép truy cập khu vực này (Mã lỗi: <code>AUTH_004</code>).
            <br><br>
            <em>Mẹo kiểm tra: Mở bảng Demo Panel bên phải và chuyển sang nhân vật "Admin Quản Trị" để truy cập.</em>
          </p>
          <div style="margin-top: 20px;">
            <button class="btn btn-secondary" onclick="window.AppRouter.navigate('matches')">
              Về trang chủ tìm kèo
            </button>
          </div>
        </div>
      `;
      return false;
    }
    return true;
  }

  /**
   * Helper to render user name for admin per ADR-0002:
   * Always displays real name, with a badge "Đã xóa" if isDeleted is true.
   */
  function renderAdminUserName(user, fallbackName = 'Người dùng') {
    if (!user) return `<span>${escapeHtml(fallbackName)}</span>`;
    const realName = user.realName || user.displayName || fallbackName;
    if (user.isDeleted) {
      return `
        <span style="font-weight: 600; color: var(--color-on-surface);">
          ${escapeHtml(realName)}
        </span>
        <span class="badge badge-gray" style="margin-left: 4px; font-size: 10px; font-style: normal;">
          Đã xóa
        </span>
      `;
    }
    return `<span style="font-weight: 600; color: var(--color-on-surface);">${escapeHtml(realName)}</span>`;
  }

  /* =========================================================================
     ADM-01: ADMIN PORTAL DASHBOARD (Cổng quản trị ArenaFlow)
     Route: 'admin'
     ========================================================================= */
  router.registerView('admin', (container) => {
    if (!requireAdminRole(container)) return;

    router.setHeaderTitle('Cổng quản trị ArenaFlow');

    const state = store.getState();
    const currentUser = store.getCurrentUser();

    // Calculate queue counts
    const teams = state.teams || [];
    const activeTeams = teams.filter(t => t.status === 'ACTIVE').length;
    const inactiveTeams = teams.filter(t => t.status === 'INACTIVE').length;
    const disbandedTeams = teams.filter(t => t.status === 'DISBANDED').length;

    const reports = state.reports || [];
    const teamDisputesPending = reports.filter(r => r.reporterType === 'TEAM' && r.status === 'DISPUTED').length;
    const mercDisputesPending = reports.filter(r => r.reporterType === 'INDIVIDUAL' && (r.status === 'DISPUTED' || r.status === 'PENDING')).length;
    const venueReportsPending = reports.filter(r => r.reporterType === 'VENUE' && r.status === 'PENDING').length;

    const sports = state.sports || [];
    const activeSports = sports.filter(s => s.active).length;

    container.innerHTML = `
      <div style="padding: 16px; display: flex; flex-direction: column; gap: 16px;">
        <!-- Admin Profile Banner -->
        <div class="card" style="background: linear-gradient(135deg, #1e2922 0%, #2e5134 100%); color: white; border: none;">
          <div style="display: flex; align-items: center; justify-content: space-between;">
            <div style="display: flex; align-items: center; gap: 12px;">
              <div class="avatar avatar-md" style="background: #3d6943; color: white; border-color: rgba(255,255,255,0.2);">
                <span>AD</span>
              </div>
              <div>
                <div style="display: flex; align-items: center; gap: 6px;">
                  <h3 style="margin: 0; font-size: var(--font-size-md); font-weight: bold; color: white;">
                    ${escapeHtml(currentUser?.displayName || 'Admin ArenaFlow')}
                  </h3>
                  <span class="badge" style="background: rgba(255,255,255,0.2); color: white; font-size: 10px;">
                    ADMIN
                  </span>
                </div>
                <div style="font-size: 11px; opacity: 0.85; margin-top: 2px;">
                  Hệ thống quản lý & kiểm duyệt dữ liệu
                </div>
              </div>
            </div>
            <div style="font-size: 11px; text-align: right; opacity: 0.8;">
              <div>Giờ hệ thống:</div>
              <strong>${formatDate(state.meta.mockClock)}</strong>
            </div>
          </div>
        </div>

        <!-- Quick Summary Metrics (2x2 Grid) -->
        <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 10px;">
          <div class="card" style="padding: 12px; text-align: center;">
            <div style="font-size: 11px; color: var(--color-on-surface-variant);">Tổng số đội bóng</div>
            <div style="font-size: var(--font-size-2xl); font-weight: bold; color: var(--color-primary); margin-top: 2px;">
              ${teams.length}
            </div>
            <div style="font-size: 10px; color: var(--color-on-surface-variant); margin-top: 2px;">
              ${activeTeams} hoạt động • ${inactiveTeams} khóa
            </div>
          </div>

          <div class="card" style="padding: 12px; text-align: center;">
            <div style="font-size: 11px; color: var(--color-on-surface-variant);">Tranh chấp chờ duyệt</div>
            <div style="font-size: var(--font-size-2xl); font-weight: bold; color: var(--color-warning); margin-top: 2px;">
              ${teamDisputesPending + mercDisputesPending}
            </div>
            <div style="font-size: 10px; color: var(--color-on-surface-variant); margin-top: 2px;">
              ${teamDisputesPending} đội • ${mercDisputesPending} đá lẻ
            </div>
          </div>
        </div>

        <!-- Admin Function Navigation Menu Cards -->
        <div style="display: flex; flex-direction: column; gap: 10px;">
          <h4 style="margin: 4px 0 2px; font-size: var(--font-size-sm); font-weight: bold; color: var(--color-on-surface-variant); text-transform: uppercase; letter-spacing: 0.5px;">
            Nhóm chức năng quản trị
          </h4>

          <!-- ADM-02 Quản lý đội bóng -->
          <div class="card list-action-card" style="cursor: pointer;" onclick="window.AppRouter.navigate('admin-teams')">
            <div style="display: flex; align-items: center; justify-content: space-between;">
              <div style="display: flex; align-items: center; gap: 12px;">
                <div style="width: 40px; height: 40px; border-radius: var(--radius-base); background: var(--color-surface-container); display: grid; place-items: center; color: var(--color-primary);">
                  <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path>
                    <circle cx="9" cy="7" r="4"></circle>
                    <path d="M23 21v-2a4 4 0 0 0-3-3.87"></path>
                    <path d="M16 3.13a4 4 0 0 1 0 7.75"></path>
                  </svg>
                </div>
                <div>
                  <h4 style="margin: 0; font-size: var(--font-size-base); color: var(--color-on-surface);">
                    Quản lý đội bóng (ADM-02)
                  </h4>
                  <div style="font-size: var(--font-size-xs); color: var(--color-on-surface-variant);">
                    Tìm kiếm, tạm ngưng hoặc mở khóa đội vi phạm
                  </div>
                </div>
              </div>
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="color: var(--color-outline);">
                <polyline points="9 18 15 12 9 6"></polyline>
              </svg>
            </div>
          </div>

          <!-- ADM-04 Tranh chấp đội -->
          <div class="card list-action-card" style="cursor: pointer;" onclick="window.AppRouter.navigate('admin-team-disputes')">
            <div style="display: flex; align-items: center; justify-content: space-between;">
              <div style="display: flex; align-items: center; gap: 12px;">
                <div style="width: 40px; height: 40px; border-radius: var(--radius-base); background: var(--color-warning-container); display: grid; place-items: center; color: var(--color-warning);">
                  <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <circle cx="12" cy="12" r="10"></circle>
                    <line x1="12" y1="8" x2="12" y2="12"></line>
                    <line x1="12" y1="16" x2="12.01" y2="16"></line>
                  </svg>
                </div>
                <div>
                  <div style="display: flex; align-items: center; gap: 6px;">
                    <h4 style="margin: 0; font-size: var(--font-size-base); color: var(--color-on-surface);">
                      Hàng chờ tranh chấp đội (ADM-04)
                    </h4>
                    ${teamDisputesPending > 0 ? `<span class="badge badge-amber">${teamDisputesPending} chờ xử lý</span>` : ''}
                  </div>
                  <div style="font-size: var(--font-size-xs); color: var(--color-on-surface-variant);">
                    Xem xét giải trình khiếu nại báo cáo bùng kèo
                  </div>
                </div>
              </div>
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="color: var(--color-outline);">
                <polyline points="9 18 15 12 9 6"></polyline>
              </svg>
            </div>
          </div>

          <!-- ADM-05 Tranh chấp đá lẻ -->
          <div class="card list-action-card" style="cursor: pointer;" onclick="window.AppRouter.navigate('admin-merc-disputes')">
            <div style="display: flex; align-items: center; justify-content: space-between;">
              <div style="display: flex; align-items: center; gap: 12px;">
                <div style="width: 40px; height: 40px; border-radius: var(--radius-base); background: var(--color-surface-container); display: grid; place-items: center; color: var(--color-primary);">
                  <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                    <circle cx="12" cy="7" r="4"></circle>
                  </svg>
                </div>
                <div>
                  <div style="display: flex; align-items: center; gap: 6px;">
                    <h4 style="margin: 0; font-size: var(--font-size-base); color: var(--color-on-surface);">
                      Tranh chấp người chơi lẻ (ADM-05)
                    </h4>
                    ${mercDisputesPending > 0 ? `<span class="badge badge-amber">${mercDisputesPending} chờ</span>` : ''}
                  </div>
                  <div style="font-size: var(--font-size-xs); color: var(--color-on-surface-variant);">
                    Hàng chờ xử lý khiếu nại mercenary
                  </div>
                </div>
              </div>
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="color: var(--color-outline);">
                <polyline points="9 18 15 12 9 6"></polyline>
              </svg>
            </div>
          </div>

          <!-- ADM-07 & ADM-08 Quản lý địa điểm & Báo cáo sân -->
          <div class="card list-action-card" style="cursor: pointer;" onclick="window.AppRouter.navigate('admin-venue-reports')">
            <div style="display: flex; align-items: center; justify-content: space-between;">
              <div style="display: flex; align-items: center; gap: 12px;">
                <div style="width: 40px; height: 40px; border-radius: var(--radius-base); background: var(--color-surface-container); display: grid; place-items: center; color: var(--color-primary);">
                  <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"></path>
                    <circle cx="12" cy="10" r="3"></circle>
                  </svg>
                </div>
                <div>
                  <div style="display: flex; align-items: center; gap: 6px;">
                    <h4 style="margin: 0; font-size: var(--font-size-base); color: var(--color-on-surface);">
                      Báo cáo & địa điểm sân (ADM-07)
                    </h4>
                    ${venueReportsPending > 0 ? `<span class="badge badge-amber">${venueReportsPending} báo cáo</span>` : ''}
                  </div>
                  <div style="font-size: var(--font-size-xs); color: var(--color-on-surface-variant);">
                    Duyệt báo cáo đóng cửa sân và khóa địa điểm
                  </div>
                </div>
              </div>
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="color: var(--color-outline);">
                <polyline points="9 18 15 12 9 6"></polyline>
              </svg>
            </div>
          </div>

          <!-- ADM-09 Quản lý bộ môn -->
          <div class="card list-action-card" style="cursor: pointer;" onclick="window.AppRouter.navigate('admin-sports')">
            <div style="display: flex; align-items: center; justify-content: space-between;">
              <div style="display: flex; align-items: center; gap: 12px;">
                <div style="width: 40px; height: 40px; border-radius: var(--radius-base); background: var(--color-surface-container); display: grid; place-items: center; color: var(--color-primary);">
                  <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <circle cx="12" cy="12" r="10"></circle>
                    <line x1="2" y1="12" x2="22" y2="12"></line>
                    <path d="M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z"></path>
                  </svg>
                </div>
                <div>
                  <h4 style="margin: 0; font-size: var(--font-size-base); color: var(--color-on-surface);">
                    Quản lý bộ môn thể thao (ADM-09)
                  </h4>
                  <div style="font-size: var(--font-size-xs); color: var(--color-on-surface-variant);">
                    Bật / tắt hoạt động của các bộ môn thể thao
                  </div>
                </div>
              </div>
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="color: var(--color-outline);">
                <polyline points="9 18 15 12 9 6"></polyline>
              </svg>
            </div>
          </div>
        </div>
      </div>
    `;
  });

  /* =========================================================================
     ADM-02: TEAM MODERATION & SEARCH (Quản lý đội)
     Route: 'admin-teams'
     ========================================================================= */
  router.registerView('admin-teams', (container) => {
    if (!requireAdminRole(container)) return;

    router.setHeaderTitle('Quản lý đội bóng');

    const state = store.getState();
    let currentFilter = 'ALL'; // 'ALL', 'ACTIVE', 'INACTIVE', 'DISBANDED'
    let searchQuery = '';

    function renderTeamList() {
      const teams = state.teams || [];

      const filtered = teams.filter(t => {
        const matchesFilter =
          currentFilter === 'ALL' ||
          (currentFilter === 'ACTIVE' && t.status === 'ACTIVE') ||
          (currentFilter === 'INACTIVE' && t.status === 'INACTIVE') ||
          (currentFilter === 'DISBANDED' && t.status === 'DISBANDED');

        const captain = state.characters[t.captainId];
        const captainName = captain ? (captain.displayName || '') : '';
        const matchesSearch =
          !searchQuery ||
          t.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
          t.shortName?.toLowerCase().includes(searchQuery.toLowerCase()) ||
          captainName.toLowerCase().includes(searchQuery.toLowerCase());

        return matchesFilter && matchesSearch;
      });

      container.innerHTML = `
        <div style="padding: 16px; display: flex; flex-direction: column; gap: 14px;">
          <!-- Search Bar -->
          <div class="form-group" style="margin: 0;">
            <input type="text" id="admin-team-search" class="form-input" placeholder="🔍 Tìm theo tên đội hoặc đội trưởng..." value="${escapeHtml(searchQuery)}">
          </div>

          <!-- Filter Chips (Stitch S13 Parity) -->
          <div class="chip-group">
            <button class="chip ${currentFilter === 'ALL' ? 'is-active' : ''}" data-filter="ALL">Tất cả (${teams.length})</button>
            <button class="chip ${currentFilter === 'ACTIVE' ? 'is-active' : ''}" data-filter="ACTIVE">Hoạt động</button>
            <button class="chip ${currentFilter === 'INACTIVE' ? 'is-active' : ''}" data-filter="INACTIVE">Bị khóa</button>
            <button class="chip ${currentFilter === 'DISBANDED' ? 'is-active' : ''}" data-filter="DISBANDED">Đã giải tán</button>
          </div>

          <!-- Teams List -->
          <div style="display: flex; flex-direction: column; gap: 12px;">
            ${filtered.length > 0 ? filtered.map(t => {
              const captain = state.characters[t.captainId];
              const memberCount = state.teamMembers?.filter(m => m.teamId === t.id && m.status === 'ACTIVE').length || 0;

              let statusBadge = '<span class="badge badge-green">Hoạt động</span>';
              if (t.status === 'INACTIVE') {
                statusBadge = '<span class="badge badge-error">Bị tạm ngưng</span>';
              } else if (t.status === 'DISBANDED') {
                statusBadge = '<span class="badge badge-gray">Đã giải tán</span>';
              }

              return `
                <div class="card" style="display: flex; flex-direction: column; gap: 10px;">
                  <div class="card-header" style="align-items: flex-start;">
                    <div style="display: flex; gap: 10px; align-items: center;">
                      <div class="avatar avatar-sm">
                        ${t.logoUrl ? `<img src="${t.logoUrl}">` : `<span>${escapeHtml(t.shortName || 'FC')}</span>`}
                      </div>
                      <div>
                        <h4 style="margin: 0; font-size: var(--font-size-base); color: var(--color-on-surface);">
                          ${escapeHtml(t.name)}
                        </h4>
                        <div style="font-size: var(--font-size-xs); color: var(--color-on-surface-variant); margin-top: 2px;">
                          Đội trưởng: ${renderAdminUserName(captain, 'Đã chuyển / Không có')}
                        </div>
                      </div>
                    </div>
                    ${statusBadge}
                  </div>

                  <div class="card-body" style="font-size: var(--font-size-xs); color: var(--color-on-surface-variant); line-height: 1.5;">
                    <div>Thành viên hoạt động: <strong>${memberCount} người</strong> • Điểm uy tín: <strong>${t.reputationScore || 80}</strong></div>
                    
                    ${t.status === 'INACTIVE' && t.inactiveReason ? `
                      <div class="notice notice-danger" style="margin: 6px 0 0; padding: 8px;">
                        Lý do tạm ngưng: "${escapeHtml(t.inactiveReason)}"
                      </div>
                    ` : ''}

                    ${t.status === 'DISBANDED' ? `
                      <div class="notice" style="margin: 6px 0 0; padding: 8px; font-size: 11px;">
                        Đã giải tán vào: ${formatDate(t.disbandedAt)} • Lý do: "${escapeHtml(t.disbandReason || 'Đội trưởng giải tán')}"
                      </div>
                    ` : ''}
                  </div>

                  <!-- Actions per ADM-02 -->
                  <div style="display: flex; gap: 8px; padding-top: 4px; border-top: 1px solid var(--color-outline-variant);">
                    <button class="btn btn-sm btn-outline" style="flex: 1;" onclick="window.AppRouter.navigate('admin-team-history', { teamId: '${t.id}' })">
                      Lịch sử thành viên (ADM-03)
                    </button>
                    ${t.status === 'ACTIVE' ? `
                      <button class="btn btn-sm btn-danger-outline" onclick="window.AppRouter.openDialog('admin-team-suspend-dialog', { teamId: '${t.id}', teamName: '${escapeHtml(t.name)}' })">
                        Tạm ngưng
                      </button>
                    ` : t.status === 'INACTIVE' ? `
                      <button class="btn btn-sm btn-primary" onclick="window.AppRouter.openDialog('admin-team-reactivate-dialog', { teamId: '${t.id}', teamName: '${escapeHtml(t.name)}' })">
                        Kích hoạt lại
                      </button>
                    ` : ''}
                  </div>
                </div>
              `;
            }).join('') : `
              <div class="empty-state">
                <h4 class="empty-state-title">Không tìm thấy đội nào</h4>
                <p class="empty-state-desc">Thử điều chỉnh từ khóa tìm kiếm hoặc bộ lọc trạng thái.</p>
              </div>
            `}
          </div>

          <button class="btn btn-secondary btn-block" onclick="window.AppRouter.navigate('admin')">
            Về menu quản trị
          </button>
        </div>
      `;

      // Filter events
      const searchInput = container.querySelector('#admin-team-search');
      searchInput.addEventListener('input', (e) => {
        searchQuery = e.target.value.trim();
        renderTeamList();
      });

      const chipBtns = container.querySelectorAll('.chip');
      chipBtns.forEach(btn => {
        btn.addEventListener('click', () => {
          currentFilter = btn.getAttribute('data-filter');
          renderTeamList();
        });
      });
    }

    renderTeamList();
  });

  /* =========================================================================
     ADM-03: TEAM MEMBER HISTORY (Lịch sử thành viên đội)
     Route: 'admin-team-history' (params: { teamId })
     Stitch S14 Parity: Unmasked real names, deleted flags, roles, dates.
     ========================================================================= */
  router.registerView('admin-team-history', (container, params) => {
    if (!requireAdminRole(container)) return;

    router.setHeaderTitle('Lịch sử thành viên (S14)');

    const state = store.getState();
    const teamId = params.teamId || 'team-alpha';
    const team = state.teams.find(t => t.id === teamId);

    if (!team) {
      container.innerHTML = `
        <div class="empty-state">
          <h3 class="empty-state-title">Không tìm thấy đội bóng</h3>
          <button class="btn btn-secondary" onclick="window.AppRouter.navigate('admin-teams')">Quay lại</button>
        </div>
      `;
      return;
    }

    let filter = 'ALL'; // 'ALL', 'ACTIVE', 'LEFT'

    function renderHistory() {
      const allMembers = state.teamMembers?.filter(m => m.teamId === team.id) || [];
      const filtered = allMembers.filter(m => {
        if (filter === 'ACTIVE') return m.status === 'ACTIVE';
        if (filter === 'LEFT') return m.status === 'LEFT';
        return true;
      });

      container.innerHTML = `
        <div style="padding: 16px; display: flex; flex-direction: column; gap: 14px;">
          <!-- Team Header -->
          <div class="card">
            <div class="card-header" style="align-items: flex-start;">
              <div style="display: flex; gap: 10px; align-items: center;">
                <div class="avatar avatar-sm">
                  ${team.logoUrl ? `<img src="${team.logoUrl}">` : `<span>${escapeHtml(team.shortName || 'FC')}</span>`}
                </div>
                <div>
                  <h3 style="margin: 0; font-size: var(--font-size-md); color: var(--color-on-surface);">
                    ${escapeHtml(team.name)}
                  </h3>
                  <div style="font-size: var(--font-size-xs); color: var(--color-on-surface-variant); margin-top: 2px;">
                    Trạng thái: <strong>${team.status}</strong>
                  </div>
                </div>
              </div>
              <span class="badge ${team.status === 'ACTIVE' ? 'badge-green' : 'badge-gray'}">${team.status}</span>
            </div>
            <div class="card-body" style="font-size: 11px; color: var(--color-on-surface-variant);">
              Gồm mọi người đã từng tham gia đội; không gồm lời mời. Tên thật được giữ nguyên cho Admin theo quy định ADR-0002.
            </div>
          </div>

          <!-- Filter Chips -->
          <div class="chip-group">
            <button class="chip ${filter === 'ALL' ? 'is-active' : ''}" data-filter="ALL">Tất cả (${allMembers.length})</button>
            <button class="chip ${filter === 'ACTIVE' ? 'is-active' : ''}" data-filter="ACTIVE">Đang trong đội</button>
            <button class="chip ${filter === 'LEFT' ? 'is-active' : ''}" data-filter="LEFT">Đã rời</button>
          </div>

          <!-- Members List (Stitch S14 Structure) -->
          <div style="display: flex; flex-direction: column; gap: 10px;">
            ${filtered.length > 0 ? filtered.map(m => {
              const user = state.characters[m.userId];
              const isCaptain = m.role === 'CAPTAIN';
              const isLeft = m.status === 'LEFT';

              return `
                <div class="card" style="padding: 12px 14px;">
                  <div style="display: flex; justify-content: space-between; align-items: center;">
                    <div style="display: flex; align-items: center; gap: 10px;">
                      <div class="avatar avatar-sm" style="font-size: 11px;">
                        ${user?.avatarUrl ? `<img src="${user.avatarUrl}">` : `<span>${(user?.displayName || 'U').slice(0, 1)}</span>`}
                      </div>
                      <div>
                        <div style="display: flex; align-items: center; gap: 6px;">
                          ${renderAdminUserName(user, 'Người dùng ID ' + m.userId)}
                        </div>
                        <div style="font-size: 11px; color: var(--color-on-surface-variant); margin-top: 2px;">
                          SĐT: ${escapeHtml(user?.phone || 'Chưa cung cấp')}
                        </div>
                      </div>
                    </div>

                    <div style="display: flex; flex-direction: column; align-items: flex-end; gap: 4px;">
                      ${isCaptain ? '<span class="badge badge-amber" style="font-size: 10px;">Đội trưởng</span>' : '<span class="badge badge-gray" style="font-size: 10px;">Thành viên</span>'}
                      ${isLeft ? '<span class="badge badge-gray" style="font-size: 10px;">Đã rời</span>' : '<span class="badge badge-green" style="font-size: 10px;">Đang tham gia</span>'}
                    </div>
                  </div>

                  <div style="margin-top: 8px; padding-top: 6px; border-top: 1px solid var(--color-outline-variant); font-size: 11px; color: var(--color-on-surface-variant); display: flex; justify-content: space-between;">
                    <span>Tham gia: ${formatDate(m.joinedAt)}</span>
                    ${isLeft ? `<span>Rời đội: ${formatDate(m.leftAt)}</span>` : '<span>Đang hoạt động</span>'}
                  </div>
                </div>
              `;
            }).join('') : `
              <div class="empty-state">
                <p class="empty-state-desc">Không có thành viên nào thỏa mãn bộ lọc.</p>
              </div>
            `}
          </div>

          <button class="btn btn-secondary btn-block" onclick="window.AppRouter.navigate('admin-teams')">
            Về danh sách đội
          </button>
        </div>
      `;

      container.querySelectorAll('.chip').forEach(btn => {
        btn.addEventListener('click', () => {
          filter = btn.getAttribute('data-filter');
          renderHistory();
        });
      });
    }

    renderHistory();
  });

  /* =========================================================================
     ADM-04: TEAM DISPUTE QUEUE & RESOLUTION (Hàng chờ tranh chấp đội)
     Route: 'admin-team-disputes'
     Order: PENDING oldest first FIFO
     ========================================================================= */
  router.registerView('admin-team-disputes', (container) => {
    if (!requireAdminRole(container)) return;

    router.setHeaderTitle('Tranh chấp đội bóng');

    const state = store.getState();
    let statusFilter = 'PENDING'; // 'PENDING', 'RESOLVED', 'ALL'

    function renderDisputeQueue() {
      const allReports = state.reports || [];
      const teamDisputes = allReports.filter(r => r.reporterType === 'TEAM' && (r.status === 'DISPUTED' || r.dispute || r.adminDecision));

      const filtered = teamDisputes.filter(r => {
        if (statusFilter === 'PENDING') return r.status === 'DISPUTED';
        if (statusFilter === 'RESOLVED') return r.status === 'RESOLVED' || r.status === 'REJECTED' || r.status === 'CONFIRMED';
        return true;
      });

      // Sort: PENDING sorted oldest first FIFO per spec!
      filtered.sort((a, b) => {
        const timeA = new Date(a.createdAt).getTime();
        const timeB = new Date(b.createdAt).getTime();
        return statusFilter === 'PENDING' ? timeA - timeB : timeB - timeA;
      });

      container.innerHTML = `
        <div style="padding: 16px; display: flex; flex-direction: column; gap: 14px;">
          <!-- Filter Chips -->
          <div class="chip-group">
            <button class="chip ${statusFilter === 'PENDING' ? 'is-active' : ''}" data-filter="PENDING">
              Chờ giải quyết (${teamDisputes.filter(x => x.status === 'DISPUTED').length})
            </button>
            <button class="chip ${statusFilter === 'RESOLVED' ? 'is-active' : ''}" data-filter="RESOLVED">
              Đã xử lý
            </button>
            <button class="chip ${statusFilter === 'ALL' ? 'is-active' : ''}" data-filter="ALL">
              Tất cả (${teamDisputes.length})
            </button>
          </div>

          <!-- Disputes List -->
          <div style="display: flex; flex-direction: column; gap: 14px;">
            ${filtered.length > 0 ? filtered.map(rep => {
              const reporterTeam = state.teams.find(t => t.id === rep.reporterId);
              const reportedTeam = state.teams.find(t => t.id === rep.reportedId);
              const reporterCaptain = state.characters[reporterTeam?.captainId];
              const reportedCaptain = state.characters[reportedTeam?.captainId];
              const match = state.matches.find(m => m.id === rep.matchId);
              const venue = state.venues.find(v => v.id === match?.venueId);

              // Reviews for this match
              const matchReviews = state.reviews?.filter(rev => rev.matchId === match?.id) || [];

              return `
                <div class="card" style="display: flex; flex-direction: column; gap: 12px; border: 1px solid var(--color-outline-variant);">
                  <!-- Dispute Header -->
                  <div class="card-header" style="align-items: flex-start; padding-bottom: 8px; border-bottom: 1px solid var(--color-outline-variant);">
                    <div>
                      <div style="display: flex; align-items: center; gap: 6px;">
                        <span class="badge badge-error">BÙNG KÈO (NOSHOW)</span>
                        ${rep.status === 'DISPUTED' ? '<span class="badge badge-amber">Chờ Admin quyết định</span>' : ''}
                        ${rep.status === 'CONFIRMED' || rep.status === 'RESOLVED' ? '<span class="badge badge-error">Xác nhận đúng</span>' : ''}
                        ${rep.status === 'REJECTED' ? '<span class="badge badge-emerald">Bác báo cáo</span>' : ''}
                      </div>
                      <h4 style="margin: 6px 0 2px; font-size: var(--font-size-base); color: var(--color-on-surface);">
                        Tranh chấp #${rep.id.slice(-6)}
                      </h4>
                      <div style="font-size: 11px; color: var(--color-on-surface-variant);">
                        Trận: ${formatDate(match?.matchDate)} • Sân: ${escapeHtml(venue?.name || 'Sân bóng')}
                      </div>
                    </div>
                  </div>

                  <!-- Two Sides Comparison -->
                  <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 8px; font-size: var(--font-size-xs);">
                    <div style="background: var(--color-surface-container-low); padding: 8px 10px; border-radius: var(--radius-base);">
                      <div style="color: var(--color-on-surface-variant); font-size: 10px; text-transform: uppercase;">Bên báo cáo:</div>
                      <strong style="color: var(--color-on-surface); font-size: var(--font-size-sm);">${escapeHtml(reporterTeam?.name || rep.reporterId)}</strong>
                      <div style="margin-top: 2px;">Đội trưởng: ${renderAdminUserName(reporterCaptain)}</div>
                      <div>Điểm uy tín: <strong>${reporterTeam?.reputationScore || 80}</strong></div>
                    </div>

                    <div style="background: var(--color-surface-container-low); padding: 8px 10px; border-radius: var(--radius-base);">
                      <div style="color: var(--color-on-surface-variant); font-size: 10px; text-transform: uppercase;">Bên khiếu nại (bị phạt):</div>
                      <strong style="color: var(--color-on-surface); font-size: var(--font-size-sm);">${escapeHtml(reportedTeam?.name || rep.reportedId)}</strong>
                      <div style="margin-top: 2px;">Đội trưởng: ${renderAdminUserName(reportedCaptain)}</div>
                      <div>Điểm uy tín: <strong>${reportedTeam?.reputationScore || 80}</strong></div>
                    </div>
                  </div>

                  <!-- Evidence & Reasons -->
                  <div style="display: flex; flex-direction: column; gap: 8px; font-size: var(--font-size-xs);">
                    <div style="background: #fef2f2; border-left: 3px solid #dc2626; padding: 8px 10px; border-radius: 4px;">
                      <strong style="color: #991b1b;">Lý do báo cáo từ đối phương:</strong>
                      <p style="margin: 2px 0 0; color: #7f1d1d;">"${escapeHtml(rep.reason)}"</p>
                    </div>

                    <div style="background: #f0fdf4; border-left: 3px solid #16a34a; padding: 8px 10px; border-radius: 4px;">
                      <strong style="color: #166534;">Bằng chứng giải trình từ bên bị báo cáo:</strong>
                      <p style="margin: 2px 0 0; color: #14532d;">
                        "${escapeHtml(rep.dispute?.evidenceText || rep.dispute?.evidence || 'Chưa cung cấp giải trình')}"
                      </p>
                    </div>
                  </div>

                  <!-- Match Ratings from both sides (Reference) -->
                  ${matchReviews.length > 0 ? `
                    <div style="font-size: 11px; color: var(--color-on-surface-variant); background: var(--color-surface-container); padding: 6px 10px; border-radius: var(--radius-base);">
                      <strong>Đánh giá thực tế trong trận:</strong>
                      ${matchReviews.map(r => `
                        <div>• ${r.reviewerId === reporterTeam?.id ? reporterTeam?.name : reportedTeam?.name}: ${r.noShow ? 'Đánh dấu Không đến' : `${r.stars} sao`} ${r.comment ? `("${escapeHtml(r.comment)}")` : ''}</div>
                      `).join('')}
                    </div>
                  ` : ''}

                  <!-- Decision Info if resolved -->
                  ${rep.adminDecision ? `
                    <div class="notice ${rep.adminDecision.outcome === 'REJECTED' ? 'notice-success' : 'notice-danger'}" style="margin: 0; font-size: var(--font-size-xs);">
                      <strong>Quyết định: ${rep.adminDecision.outcome === 'REJECTED' ? 'BÁC BÁO CÁO (Hoàn điểm)' : 'XÁC NHẬN BÁO CÁO ĐÚNG (Giữ phạt)'}</strong>
                      <div style="margin-top: 4px;">Ghi chú: "${escapeHtml(rep.adminDecision.note)}"</div>
                      <div style="font-size: 10px; margin-top: 4px; opacity: 0.8;">Xử lý lúc: ${formatDateTime(rep.adminDecision.resolvedAt)}</div>
                    </div>
                  ` : ''}

                  <!-- Action Button -->
                  ${rep.status === 'DISPUTED' ? `
                    <button class="btn btn-primary btn-block" onclick="window.AppRouter.openSheet('admin-resolve-dispute-sheet', { reportId: '${rep.id}', disputeId: '${rep.id}', type: 'TEAM' })">
                      Xử lý quyết định tranh chấp (ADM-06)
                    </button>
                  ` : ''}
                </div>
              `;
            }).join('') : `
              <div class="empty-state">
                <h4 class="empty-state-title">Hàng chờ tranh chấp trống</h4>
                <p class="empty-state-desc">Không có tranh chấp đội bóng nào cần giải quyết ở trạng thái này.</p>
              </div>
            `}
          </div>

          <button class="btn btn-secondary btn-block" onclick="window.AppRouter.navigate('admin')">
            Về menu quản trị
          </button>
        </div>
      `;

      container.querySelectorAll('.chip').forEach(btn => {
        btn.addEventListener('click', () => {
          statusFilter = btn.getAttribute('data-filter');
          renderDisputeQueue();
        });
      });
    }

    renderDisputeQueue();
  });

  /* =========================================================================
     ADM-05: MERCENARY DISPUTE QUEUE (Tranh chấp đá lẻ)
     Route: 'admin-merc-disputes'
     ========================================================================= */
  router.registerView('admin-merc-disputes', (container) => {
    if (!requireAdminRole(container)) return;

    router.setHeaderTitle('Tranh chấp đá lẻ');

    const state = store.getState();
    let statusFilter = 'ALL';

    function renderMercDisputes() {
      const allReports = state.reports || [];
      const mercReports = allReports.filter(r => r.reporterType === 'INDIVIDUAL');

      const filtered = mercReports.filter(r => {
        if (statusFilter === 'PENDING') return r.status === 'PENDING' || r.status === 'DISPUTED';
        if (statusFilter === 'RESOLVED') return r.status === 'RESOLVED' || r.status === 'CONFIRMED' || r.status === 'REJECTED';
        return true;
      });

      container.innerHTML = `
        <div style="padding: 16px; display: flex; flex-direction: column; gap: 14px;">
          <div class="chip-group">
            <button class="chip ${statusFilter === 'ALL' ? 'is-active' : ''}" data-filter="ALL">Tất cả (${mercReports.length})</button>
            <button class="chip ${statusFilter === 'PENDING' ? 'is-active' : ''}" data-filter="PENDING">Chờ xử lý</button>
            <button class="chip ${statusFilter === 'RESOLVED' ? 'is-active' : ''}" data-filter="RESOLVED">Đã giải quyết</button>
          </div>

          <div style="display: flex; flex-direction: column; gap: 12px;">
            ${filtered.length > 0 ? filtered.map(rep => {
              const reporterUser = state.characters[rep.reporterId];
              const reportedUser = state.characters[rep.reportedId];

              return `
                <div class="card" style="display: flex; flex-direction: column; gap: 10px;">
                  <div class="card-header" style="align-items: flex-start;">
                    <div>
                      <span class="badge badge-warning">${rep.type}</span>
                      <h4 style="margin: 4px 0 2px; font-size: var(--font-size-base);">Tranh chấp người chơi #${rep.id.slice(-6)}</h4>
                      <div style="font-size: 11px; color: var(--color-on-surface-variant);">
                        Người báo cáo: ${renderAdminUserName(reporterUser)} • Bị báo cáo: ${renderAdminUserName(reportedUser)}
                      </div>
                    </div>
                  </div>

                  <div class="card-body" style="font-size: var(--font-size-xs);">
                    <div style="background: var(--color-surface-container-low); padding: 8px 10px; border-radius: var(--radius-base);">
                      Lý do: "${escapeHtml(rep.reason)}"
                    </div>

                    ${rep.dispute ? `
                      <div style="margin-top: 6px; background: #f0fdf4; padding: 8px 10px; border-radius: var(--radius-base); color: #14532d;">
                        Giải trình: "${escapeHtml(rep.dispute.evidenceText || rep.dispute.evidence)}"
                      </div>
                    ` : ''}

                    ${rep.adminDecision ? `
                      <div class="notice notice-info" style="margin: 8px 0 0;">
                        Quyết định: <strong>${rep.adminDecision.outcome}</strong> • Ghi chú: "${escapeHtml(rep.adminDecision.note)}"
                      </div>
                    ` : ''}
                  </div>

                  ${rep.status === 'PENDING' || rep.status === 'DISPUTED' ? `
                    <button class="btn btn-primary btn-block" onclick="window.AppRouter.openSheet('admin-resolve-dispute-sheet', { reportId: '${rep.id}', disputeId: '${rep.id}', type: 'MERCENARY' })">
                      Xử lý tranh chấp (ADM-06)
                    </button>
                  ` : ''}
                </div>
              `;
            }).join('') : `
              <div class="empty-state">
                <p class="empty-state-desc">Không có tranh chấp đá lẻ nào.</p>
              </div>
            `}
          </div>

          <button class="btn btn-secondary btn-block" onclick="window.AppRouter.navigate('admin')">
            Về menu quản trị
          </button>
        </div>
      `;

      container.querySelectorAll('.chip').forEach(btn => {
        btn.addEventListener('click', () => {
          statusFilter = btn.getAttribute('data-filter');
          renderMercDisputes();
        });
      });
    }

    renderMercDisputes();
  });

  /* =========================================================================
     ADM-06: RESOLVE DISPUTE SHEET (Xử lý khiếu nại tranh chấp)
     Sheet: 'admin-resolve-dispute-sheet' (params: { reportId, disputeId, type })
     Mandatory decision note: 1..500 characters
     ========================================================================= */
  router.registerSheet('admin-resolve-dispute-sheet', (body, footer, params, titleElem) => {
    titleElem.textContent = 'Quyết định xử lý khiếu nại (ADM-06)';

    const state = store.getState();
    const reportId = params.reportId;
    const report = state.reports?.find(r => r.id === reportId);

    if (!report) {
      body.innerHTML = `<div class="notice notice-danger">Không tìm thấy bản ghi tranh chấp.</div>`;
      footer.innerHTML = `<button class="btn btn-secondary btn-block" onclick="window.AppRouter.closeSheet()">Đóng</button>`;
      return;
    }

    body.innerHTML = `
      <div style="display: flex; flex-direction: column; gap: 14px;">
        <div style="background: var(--color-surface-container-low); padding: 10px; border-radius: var(--radius-base); font-size: var(--font-size-xs);">
          Đang xử lý tranh chấp: <strong>#${report.id.slice(-6)}</strong> • Loại: <strong>${report.type}</strong>
        </div>

        <!-- Decision Radios -->
        <div class="form-group">
          <label class="form-label">Phán quyết của Quản trị viên</label>
          <div style="display: flex; flex-direction: column; gap: 8px;">
            <label class="radio-card" style="display: flex; gap: 10px; align-items: flex-start; padding: 10px; border: 1px solid var(--color-outline-variant); border-radius: var(--radius-base); cursor: pointer;">
              <input type="radio" name="adminDecisionChoice" value="CONFIRMED" checked style="margin-top: 3px;">
              <div>
                <strong style="font-size: var(--font-size-sm); color: var(--color-error);">Xác nhận vi phạm (CONFIRMED)</strong>
                <p style="margin: 2px 0 0; font-size: 11px; color: var(--color-on-surface-variant);">
                  Báo cáo là chính xác. Giữ nguyên hình phạt trừ điểm uy tín đã áp dụng.
                </p>
              </div>
            </label>

            <label class="radio-card" style="display: flex; gap: 10px; align-items: flex-start; padding: 10px; border: 1px solid var(--color-outline-variant); border-radius: var(--radius-base); cursor: pointer;">
              <input type="radio" name="adminDecisionChoice" value="REJECTED" style="margin-top: 3px;">
              <div>
                <strong style="font-size: var(--font-size-sm); color: var(--color-success);">Bác bỏ báo cáo & Hoàn điểm (REJECTED)</strong>
                <p style="margin: 2px 0 0; font-size: 11px; color: var(--color-on-surface-variant);">
                  Bằng chứng giải trình hợp lý. Hoàn lại điểm phạt cho bên bị báo cáo và hủy vi phạm.
                </p>
              </div>
            </label>
          </div>
        </div>

        <!-- Mandatory Decision Note (1-500 chars) -->
        <div class="form-group">
          <label class="form-label" for="admin-decision-note">
            Ghi chú căn cứ quyết định (Bắt buộc, 1 - 500 ký tự)
          </label>
          <textarea id="admin-decision-note" class="form-textarea" rows="3" maxlength="500" placeholder="Ghi rõ căn cứ (Ví dụ: Tin nhắn đối thoại chứng minh đội bị report đã có mặt đúng giờ...)"></textarea>
          <div style="text-align: right; font-size: 11px; color: var(--color-on-surface-variant); margin-top: 4px;">
            <span id="admin-note-counter">0</span>/500
          </div>
          <span class="field-error-msg" id="admin-note-error" style="display: none;"></span>
        </div>
      </div>
    `;

    footer.innerHTML = `
      <div style="display: flex; gap: 8px;">
        <button type="button" class="btn btn-secondary" style="flex: 1;" onclick="window.AppRouter.closeSheet()">
          Hủy bỏ
        </button>
        <button type="button" class="btn btn-primary" style="flex: 1;" id="btn-submit-admin-resolve">
          Xác nhận quyết định
        </button>
      </div>
    `;

    const noteInput = body.querySelector('#admin-decision-note');
    const noteCounter = body.querySelector('#admin-note-counter');
    const noteError = body.querySelector('#admin-note-error');
    const submitBtn = footer.querySelector('#btn-submit-admin-resolve');

    noteInput.addEventListener('input', () => {
      noteCounter.textContent = noteInput.value.length;
    });

    submitBtn.addEventListener('click', () => {
      noteError.style.display = 'none';
      const note = noteInput.value.trim();

      // Validation 1 - 500 chars
      if (!note || note.length < 1 || note.length > 500) {
        noteError.textContent = 'Ghi chú quyết định xử lý là bắt buộc từ 1 đến 500 ký tự.';
        noteError.style.display = 'block';
        return;
      }

      const checkedChoice = body.querySelector('input[name="adminDecisionChoice"]:checked');
      const outcome = checkedChoice ? checkedChoice.value : 'CONFIRMED';

      submitBtn.disabled = true;
      submitBtn.textContent = 'Đang xử lý...';

      try {
        store.setState(curr => {
          const freshReports = [...(curr.reports || [])];
          const target = freshReports.find(r => r.id === report.id);
          if (target) {
            target.status = outcome;
            target.adminDecision = {
              outcome,
              note,
              resolvedAt: curr.meta.mockClock,
              resolvedBy: curr.currentUserId
            };

            // If REJECTED, refund penalty points!
            if (outcome === 'REJECTED') {
              const penalty = target.reliabilityPenalty || 5;
              if (target.reporterType === 'TEAM') {
                const repTeam = curr.teams.find(t => t.id === target.reportedId);
                if (repTeam) {
                  repTeam.reputationScore = Math.min(100, (repTeam.reputationScore || 80) + penalty);
                }
              } else if (target.reporterType === 'INDIVIDUAL') {
                const repUser = curr.characters[target.reportedId];
                if (repUser) {
                  repUser.reputationScore = Math.min(100, (repUser.reputationScore || 95) + penalty);
                }
              }
            }
          }

          return { ...curr, reports: freshReports };
        });

        router.closeSheet();
        router.showToast('Đã ban hành quyết định giải quyết tranh chấp!', 'success');
        router.navigate('admin-team-disputes');
      } catch (err) {
        submitBtn.disabled = false;
        submitBtn.textContent = 'Xác nhận quyết định';
        noteError.textContent = err.message || 'Lỗi khi ban hành quyết định';
        noteError.style.display = 'block';
      }
    });
  });

  /* =========================================================================
     ADM-07: VENUE REPORT QUEUE & MODERATION (Báo cáo địa điểm)
     Route: 'admin-venue-reports'
     ========================================================================= */
  router.registerView('admin-venue-reports', (container) => {
    if (!requireAdminRole(container)) return;

    router.setHeaderTitle('Báo cáo địa điểm');

    const state = store.getState();
    let filter = 'ALL';

    function renderVenueReports() {
      const allReports = state.reports?.filter(r => r.reporterType === 'VENUE') || [];
      const filtered = allReports.filter(r => {
        if (filter === 'PENDING') return r.status === 'PENDING';
        if (filter === 'VERIFIED') return r.status === 'VERIFIED';
        if (filter === 'REJECTED') return r.status === 'REJECTED';
        return true;
      });

      container.innerHTML = `
        <div style="padding: 16px; display: flex; flex-direction: column; gap: 14px;">
          <!-- Filter Chips -->
          <div class="chip-group">
            <button class="chip ${filter === 'ALL' ? 'is-active' : ''}" data-filter="ALL">Tất cả (${allReports.length})</button>
            <button class="chip ${filter === 'PENDING' ? 'is-active' : ''}" data-filter="PENDING">Chờ xử lý</button>
            <button class="chip ${filter === 'VERIFIED' ? 'is-active' : ''}" data-filter="VERIFIED">Đã xác minh</button>
            <button class="chip ${filter === 'REJECTED' ? 'is-active' : ''}" data-filter="REJECTED">Đã bác</button>
          </div>

          <!-- Venue Reports List -->
          <div style="display: flex; flex-direction: column; gap: 12px;">
            ${filtered.length > 0 ? filtered.map(r => {
              const venue = state.venues.find(v => v.id === r.reportedId);
              const reporter = state.characters[r.reporterId];

              return `
                <div class="card" style="display: flex; flex-direction: column; gap: 10px;">
                  <div class="card-header" style="align-items: flex-start;">
                    <div>
                      <div style="display: flex; align-items: center; gap: 6px;">
                        <span class="badge ${r.type === 'CLOSED' ? 'badge-error' : 'badge-warning'}">
                          ${r.type === 'CLOSED' ? 'ĐÓNG CỬA' : r.type}
                        </span>
                        <span class="badge ${r.status === 'PENDING' ? 'badge-amber' : r.status === 'VERIFIED' ? 'badge-green' : 'badge-gray'}">
                          ${r.status === 'PENDING' ? 'Chờ kiểm duyệt' : r.status}
                        </span>
                      </div>
                      <h4 style="margin: 6px 0 2px; font-size: var(--font-size-base);">
                        ${escapeHtml(venue?.name || 'Sân bóng ' + r.reportedId)}
                      </h4>
                      <div style="font-size: 11px; color: var(--color-on-surface-variant);">
                        Địa chỉ: ${escapeHtml(venue?.address || 'Chưa rõ')}
                      </div>
                    </div>
                  </div>

                  <div class="card-body" style="font-size: var(--font-size-xs);">
                    <div style="background: var(--color-surface-container-low); padding: 8px 10px; border-radius: var(--radius-base);">
                      Lý do báo cáo: "${escapeHtml(r.reason)}"
                    </div>
                    <div style="margin-top: 6px; color: var(--color-on-surface-variant);">
                      Người báo cáo: ${renderAdminUserName(reporter)} • Gửi lúc: ${formatDateTime(r.createdAt)}
                    </div>
                  </div>

                  <div style="display: flex; gap: 8px; border-top: 1px solid var(--color-outline-variant); padding-top: 8px;">
                    ${r.status === 'PENDING' ? `
                      <button class="btn btn-sm btn-primary" onclick="window.AppRouter.openDialog('admin-venue-resolve-dialog', { reportId: '${r.id}', venueId: '${venue?.id}' })">
                        Xử lý báo cáo
                      </button>
                    ` : ''}
                    <button class="btn btn-sm btn-outline" onclick="window.AppRouter.openDialog('admin-venue-status-dialog', { venueId: '${venue?.id}' })">
                      Đổi trạng thái sân (ADM-08)
                    </button>
                  </div>
                </div>
              `;
            }).join('') : `
              <div class="empty-state">
                <p class="empty-state-desc">Không có báo cáo địa điểm nào thỏa mãn bộ lọc.</p>
              </div>
            `}
          </div>

          <button class="btn btn-secondary btn-block" onclick="window.AppRouter.navigate('admin')">
            Về menu quản trị
          </button>
        </div>
      `;

      container.querySelectorAll('.chip').forEach(btn => {
        btn.addEventListener('click', () => {
          filter = btn.getAttribute('data-filter');
          renderVenueReports();
        });
      });
    }

    renderVenueReports();
  });

  /* =========================================================================
     ADM-08: VENUE STATUS TOGGLE DIALOG (Ngừng / Kích hoạt sân)
     Dialog: 'admin-venue-status-dialog' (params: { venueId })
     ========================================================================= */
  router.registerDialog('admin-venue-status-dialog', (container, params) => {
    const state = store.getState();
    const venueId = params.venueId;
    const venue = state.venues.find(v => v.id === venueId);

    if (!venue) {
      container.innerHTML = `<p>Không tìm thấy địa điểm.</p><button class="btn btn-secondary" onclick="window.AppRouter.closeDialog()">Đóng</button>`;
      return;
    }

    const isActive = venue.status === 'ACTIVE';

    container.innerHTML = `
      <div style="display: flex; flex-direction: column; gap: 14px;">
        <h3 style="margin: 0; font-size: var(--font-size-lg); color: var(--color-on-surface);">
          ${isActive ? 'Ngừng hoạt động địa điểm' : 'Kích hoạt lại địa điểm'}
        </h3>
        
        <p style="margin: 0; font-size: var(--font-size-sm); color: var(--color-on-surface-variant); line-height: 1.5;">
          Địa điểm: <strong>${escapeHtml(venue.name)}</strong>
          <br>
          ${isActive
            ? 'Khi chuyển sang ngừng hoạt động (INACTIVE), sân này sẽ bị khóa và KHÔNG thể được chọn trong bất kỳ gói kèo hoặc thương lượng mới nào.'
            : 'Sân sẽ được chuyển lại trạng thái hoạt động (ACTIVE) và sẵn sàng xuất hiện trong bộ chọn sân cho các kèo mới.'
          }
        </p>

        ${isActive ? `
          <div class="form-group" style="margin: 0;">
            <label class="form-label" for="venue-inactive-reason">Lý do ngừng hoạt động (Bắt buộc)</label>
            <textarea id="venue-inactive-reason" class="form-textarea" rows="2" placeholder="Ví dụ: Sân đang sửa chữa / đóng cửa kinh doanh..."></textarea>
            <span class="field-error-msg" id="venue-reason-error" style="display: none;">Vui lòng nhập lý do</span>
          </div>
        ` : ''}

        <div style="display: flex; gap: 8px; margin-top: 4px;">
          <button type="button" class="btn btn-secondary" style="flex: 1;" onclick="window.AppRouter.closeDialog()">
            Hủy bỏ
          </button>
          <button type="button" class="btn ${isActive ? 'btn-danger' : 'btn-primary'}" style="flex: 1;" id="btn-toggle-venue-confirm">
            ${isActive ? 'Khóa sân' : 'Mở khóa sân'}
          </button>
        </div>
      </div>
    `;

    const confirmBtn = container.querySelector('#btn-toggle-venue-confirm');
    confirmBtn.addEventListener('click', () => {
      let reason = '';
      if (isActive) {
        const input = container.querySelector('#venue-inactive-reason');
        reason = input ? input.value.trim() : '';
        if (!reason) {
          container.querySelector('#venue-reason-error').style.display = 'block';
          return;
        }
      }

      store.setState(curr => {
        const freshVenues = [...curr.venues];
        const v = freshVenues.find(x => x.id === venue.id);
        if (v) {
          v.status = isActive ? 'INACTIVE' : 'ACTIVE';
          if (isActive) {
            v.warning = reason;
          } else {
            delete v.warning;
          }
        }
        return { ...curr, venues: freshVenues };
      });

      router.closeDialog();
      router.showToast(isActive ? 'Đã ngừng hoạt động địa điểm' : 'Đã kích hoạt lại sân', 'success');
      router.navigate('admin-venue-reports');
    });
  });

  /* =========================================================================
     ADM-09: SPORTS MANAGEMENT (Quản lý bộ môn thể thao)
     Route: 'admin-sports'
     ========================================================================= */
  router.registerView('admin-sports', (container) => {
    if (!requireAdminRole(container)) return;

    router.setHeaderTitle('Quản lý bộ môn');

    const state = store.getState();
    const sports = state.sports || [];

    container.innerHTML = `
      <div style="padding: 16px; display: flex; flex-direction: column; gap: 14px;">
        <div class="notice notice-info" style="margin: 0; font-size: var(--font-size-xs);">
          <strong>Quản lý trạng thái bộ môn:</strong>
          Tắt một bộ môn sẽ ngăn người dùng tạo kèo mới thuộc bộ môn đó. Các kèo đang diễn ra không bị ảnh hưởng.
        </div>

        <div style="display: flex; flex-direction: column; gap: 12px;">
          ${sports.map(s => `
            <div class="card" style="display: flex; align-items: center; justify-content: space-between; padding: 14px 16px;">
              <div>
                <div style="display: flex; align-items: center; gap: 8px;">
                  <h4 style="margin: 0; font-size: var(--font-size-base); color: var(--color-on-surface);">
                    ${escapeHtml(s.name)}
                  </h4>
                  <span class="badge ${s.active ? 'badge-green' : 'badge-gray'}">
                    ${s.active ? 'Đang hoạt động' : 'Tạm khóa'}
                  </span>
                </div>
                <div style="font-size: 11px; color: var(--color-on-surface-variant); margin-top: 2px;">
                  Mã bộ môn: <code>${s.id}</code>
                </div>
              </div>

              <div>
                <button class="btn btn-sm ${s.active ? 'btn-danger-outline' : 'btn-primary'}" onclick="window.AppRouter.openDialog('admin-sport-toggle-dialog', { sportId: '${s.id}', sportName: '${escapeHtml(s.name)}', active: ${s.active} })">
                  ${s.active ? 'Tắt' : 'Bật'}
                </button>
              </div>
            </div>
          `).join('')}
        </div>

        <button class="btn btn-secondary btn-block" onclick="window.AppRouter.navigate('admin')">
          Về menu quản trị
        </button>
      </div>
    `;
  });

  /* =========================================================================
     ADMIN DIALOGS: Team suspend, reactivate, sport toggle, venue resolve
     ========================================================================= */
  router.registerDialog('admin-sport-toggle-dialog', (container, params) => {
    const isActive = params.active;
    container.innerHTML = `
      <div style="display: flex; flex-direction: column; gap: 12px;">
        <h3 style="margin: 0; font-size: var(--font-size-lg);">
          ${isActive ? 'Khóa bộ môn thể thao' : 'Kích hoạt bộ môn'}
        </h3>
        <p style="margin: 0; font-size: var(--font-size-sm); color: var(--color-on-surface-variant); line-height: 1.5;">
          Bạn có chắc chắn muốn ${isActive ? 'tạm ngưng' : 'kích hoạt'} bộ môn <strong>${escapeHtml(params.sportName)}</strong> không?
        </p>
        <div style="display: flex; gap: 8px; margin-top: 4px;">
          <button class="btn btn-secondary" style="flex: 1;" onclick="window.AppRouter.closeDialog()">Hủy</button>
          <button class="btn ${isActive ? 'btn-danger' : 'btn-primary'}" style="flex: 1;" id="btn-confirm-toggle-sport">
            Xác nhận
          </button>
        </div>
      </div>
    `;

    container.querySelector('#btn-confirm-toggle-sport').addEventListener('click', () => {
      store.setState(curr => {
        const freshSports = [...curr.sports];
        const s = freshSports.find(x => x.id === params.sportId);
        if (s) s.active = !s.active;
        return { ...curr, sports: freshSports };
      });
      router.closeDialog();
      router.showToast('Đã cập nhật trạng thái bộ môn', 'success');
      router.navigate('admin-sports');
    });
  });

  router.registerDialog('admin-team-suspend-dialog', (container, params) => {
    container.innerHTML = `
      <div style="display: flex; flex-direction: column; gap: 12px;">
        <h3 style="margin: 0; font-size: var(--font-size-lg); color: var(--color-error);">
          Tạm ngưng hoạt động đội bóng
        </h3>
        <p style="margin: 0; font-size: var(--font-size-sm); color: var(--color-on-surface-variant); line-height: 1.5;">
          Đội: <strong>${escapeHtml(params.teamName)}</strong>
          <br>
          Khi bị tạm ngưng (INACTIVE), đội không thể tạo kèo, gửi yêu cầu, mời thành viên hoặc nhận lời thách đấu.
        </p>
        <div class="form-group" style="margin: 0;">
          <label class="form-label" for="suspend-reason-input">Lý do khóa đội (Bắt buộc 1-500 ký tự)</label>
          <textarea id="suspend-reason-input" class="form-textarea" rows="3" maxlength="500" placeholder="Vi phạm quy tắc ứng xử / gian lận tỷ số..."></textarea>
          <span class="field-error-msg" id="suspend-reason-error" style="display: none;">Vui lòng nhập lý do</span>
        </div>
        <div style="display: flex; gap: 8px; margin-top: 4px;">
          <button class="btn btn-secondary" style="flex: 1;" onclick="window.AppRouter.closeDialog()">Hủy</button>
          <button class="btn btn-danger" style="flex: 1;" id="btn-confirm-suspend-team">Tạm ngưng đội</button>
        </div>
      </div>
    `;

    container.querySelector('#btn-confirm-suspend-team').addEventListener('click', () => {
      const reason = container.querySelector('#suspend-reason-input').value.trim();
      if (!reason) {
        container.querySelector('#suspend-reason-error').style.display = 'block';
        return;
      }
      store.setState(curr => {
        const freshTeams = [...curr.teams];
        const t = freshTeams.find(x => x.id === params.teamId);
        if (t) {
          t.status = 'INACTIVE';
          t.inactiveReason = reason;
        }
        return { ...curr, teams: freshTeams };
      });
      router.closeDialog();
      router.showToast('Đã tạm ngưng đội bóng', 'success');
      router.navigate('admin-teams');
    });
  });

  router.registerDialog('admin-team-reactivate-dialog', (container, params) => {
    container.innerHTML = `
      <div style="display: flex; flex-direction: column; gap: 12px;">
        <h3 style="margin: 0; font-size: var(--font-size-lg);">Kích hoạt lại đội bóng</h3>
        <p style="margin: 0; font-size: var(--font-size-sm); color: var(--color-on-surface-variant); line-height: 1.5;">
          Bạn có chắc muốn mở khóa và kích hoạt lại đội <strong>${escapeHtml(params.teamName)}</strong> không? Đội sẽ có thể tham gia các hoạt động bình thường.
        </p>
        <div style="display: flex; gap: 8px; margin-top: 4px;">
          <button class="btn btn-secondary" style="flex: 1;" onclick="window.AppRouter.closeDialog()">Hủy</button>
          <button class="btn btn-primary" style="flex: 1;" id="btn-confirm-reactivate-team">Mở khóa</button>
        </div>
      </div>
    `;

    container.querySelector('#btn-confirm-reactivate-team').addEventListener('click', () => {
      store.setState(curr => {
        const freshTeams = [...curr.teams];
        const t = freshTeams.find(x => x.id === params.teamId);
        if (t) {
          t.status = 'ACTIVE';
          t.inactiveReason = null;
        }
        return { ...curr, teams: freshTeams };
      });
      router.closeDialog();
      router.showToast('Đã mở khóa đội bóng', 'success');
      router.navigate('admin-teams');
    });
  });

  router.registerDialog('admin-venue-resolve-dialog', (container, params) => {
    container.innerHTML = `
      <div style="display: flex; flex-direction: column; gap: 12px;">
        <h3 style="margin: 0; font-size: var(--font-size-lg);">Xử lý báo cáo địa điểm</h3>
        <p style="margin: 0; font-size: var(--font-size-sm); color: var(--color-on-surface-variant);">
          Chọn kết quả xác minh cho báo cáo sân này:
        </p>
        <div style="display: flex; gap: 8px;">
          <button class="btn btn-primary" style="flex: 1;" id="btn-venue-verify-ok">
            Xác minh đúng
          </button>
          <button class="btn btn-secondary" style="flex: 1;" id="btn-venue-reject-rep">
            Bác báo cáo
          </button>
        </div>
      </div>
    `;

    container.querySelector('#btn-venue-verify-ok').addEventListener('click', () => {
      store.setState(curr => {
        const freshReps = [...curr.reports];
        const r = freshReps.find(x => x.id === params.reportId);
        if (r) r.status = 'VERIFIED';
        return { ...curr, reports: freshReps };
      });
      router.closeDialog();
      router.showToast('Đã xác minh báo cáo sân', 'success');
      router.navigate('admin-venue-reports');
    });

    container.querySelector('#btn-venue-reject-rep').addEventListener('click', () => {
      store.setState(curr => {
        const freshReps = [...curr.reports];
        const r = freshReps.find(x => x.id === params.reportId);
        if (r) r.status = 'REJECTED';
        return { ...curr, reports: freshReps };
      });
      router.closeDialog();
      router.showToast('Đã bác báo cáo sân', 'info');
      router.navigate('admin-venue-reports');
    });
  });
}
