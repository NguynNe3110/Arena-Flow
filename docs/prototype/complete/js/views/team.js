/**
 * ArenaFlow Team Module Views & Sheets
 * Implements: TEAM-01..14 (UC-T01..T06, USER-002)
 * Complies with AC-P02, J-04..J-06
 */

export function initTeamViews(router, store) {

  // Common SVG Icons
  const Icons = {
    back: `<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round"><polyline points="15 18 9 12 15 6"></polyline></svg>`,
    shield: `<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/></svg>`,
    plus: `<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>`,
    users: `<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>`,
    userPlus: `<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="8.5" cy="7" r="4"/><line x1="20" y1="8" x2="20" y2="14"/><line x1="23" y1="11" x2="17" y2="11"/></svg>`,
    crown: `<svg width="18" height="18" viewBox="0 0 24 24" fill="currentColor"><path d="M5 16L3 5l5.5 5L12 4l3.5 6L21 5l-2 11H5zm14 3c0 .6-.4 1-1 1H6c-.6 0-1-.4-1-1v-1h14v1z"/></svg>`,
    edit: `<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>`,
    trash: `<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/><line x1="10" y1="11" x2="10" y2="17"/><line x1="14" y1="11" x2="14" y2="17"/></svg>`,
    logout: `<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><polyline points="16 17 21 12 16 7"/><line x1="21" y1="12" x2="9" y2="12"/></svg>`,
    warning: `<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="m21.73 18-8-14a2 2 0 0 0-3.48 0l-8 14A2 2 0 0 0 4 21h16a2 2 0 0 0 1.73-3Z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg>`,
    search: `<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>`,
    chevronRight: `<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="9 18 15 12 9 6"/></svg>`,
    check: `<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><polyline points="20 6 9 17 4 12"/></svg>`,
    calendar: `<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>`
  };

  const SKILL_LABELS = {
    BEGINNER: 'Mới chơi',
    INTERMEDIATE: 'Trung bình',
    ADVANCED: 'Khá - Giỏi'
  };

  /* =========================================================================
     TEAM-01: My Teams List View
     ========================================================================= */
  router.registerView('teams', (container) => {
    router.setHeaderTitle('Đội của tôi');
    const user = store.getCurrentUser();
    const myTeams = store.getMyTeams();
    const state = store.getState();

    // Check for pending team invitations for current user
    const pendingInvites = state.notifications.filter(
      n => n.userId === state.currentUserId && n.type === 'TEAM_INVITATION' && !n.read
    );

    container.innerHTML = `
      <div style="padding: 16px 20px 32px;">
        <!-- Top Action Bar -->
        <div style="display: flex; gap: 10px; margin-bottom: 16px;">
          <button class="btn btn-primary" id="btn-create-team" style="flex: 1; gap: 6px;">
            ${Icons.plus}
            <span>Tạo đội mới</span>
          </button>
          <button class="btn btn-secondary" id="btn-system-teams" style="gap: 6px;">
            ${Icons.search}
            <span>Tất cả đội</span>
          </button>
        </div>

        <!-- Pending Invitations Banner if any -->
        ${pendingInvites.length > 0 ? `
          <div class="notice notice-info" id="banner-pending-invites" style="margin-bottom: 16px; cursor: pointer;">
            <div style="display: flex; justify-content: space-between; align-items: center;">
              <div>
                <strong>Bạn có ${pendingInvites.length} lời mời gia nhập đội!</strong>
                <p style="margin: 2px 0 0; font-size: 12.5px;">Bấm vào đây để xem chi tiết và phản hồi</p>
              </div>
              <span>${Icons.chevronRight}</span>
            </div>
          </div>
        ` : ''}

        <!-- Active Context Bar (Acting Team) -->
        ${myTeams.length > 0 ? `
          <div style="background: var(--color-surface-container-low); border: 1px solid var(--color-outline-variant); border-radius: var(--radius-base); padding: 12px 14px; margin-bottom: 20px; display: flex; justify-content: space-between; align-items: center;">
            <div>
              <span style="font-size: 11px; text-transform: uppercase; font-weight: 700; color: var(--color-on-surface-variant);">Đội đại diện hiện tại</span>
              <div style="font-size: 14px; font-weight: 700; color: var(--color-primary); margin-top: 2px;">
                ${state.actingTeamId ? (state.teams.find(t => t.id === state.actingTeamId)?.name || 'Chưa chọn') : 'Chưa chọn đội đại diện'}
              </div>
            </div>
            <button class="btn btn-ghost btn-sm" id="btn-change-acting-team" style="color: var(--color-primary); font-weight: 600; padding: 4px 8px;">
              Đổi đội
            </button>
          </div>
        ` : ''}

        <!-- Teams List or Empty State -->
        ${myTeams.length === 0 ? `
          <div class="empty-state" style="margin-top: 40px;">
            <div class="empty-state-icon" style="background: var(--color-primary-container); color: var(--color-primary);">
              ${Icons.shield}
            </div>
            <h3 class="empty-state-title">Bạn chưa tham gia đội nào</h3>
            <p class="empty-state-desc">
              Hãy tạo đội bóng của riêng bạn hoặc khám phá các đội trong hệ thống để kết nối và thi đấu.
            </p>
            <div style="display: flex; gap: 10px; margin-top: 20px;">
              <button class="btn btn-primary" id="btn-empty-create">
                ${Icons.plus} Tạo đội bóng
              </button>
              <button class="btn btn-secondary" id="btn-empty-explore">
                Khám phá đội
              </button>
            </div>
          </div>
        ` : `
          <div style="display: flex; flex-direction: column; gap: 12px;">
            ${myTeams.map(team => {
              const isCaptain = team.captainId === user.id;
              const memberCount = state.teamMembers.filter(m => m.teamId === team.id && m.status === 'ACTIVE').length;
              const ward = state.wards?.find(w => w.id === team.wardId);

              return `
                <div class="card team-card-item" data-team-id="${team.id}" style="cursor: pointer; padding: 14px; transition: transform 0.15s ease;">
                  <div style="display: flex; gap: 12px; align-items: center;">
                    <!-- Team Logo / Badge -->
                    <div style="width: 52px; height: 52px; border-radius: 14px; background: ${isCaptain ? 'var(--color-primary)' : 'var(--color-secondary)'}; color: #fff; display: flex; align-items: center; justify-content: center; font-weight: 800; font-size: 18px; flex-shrink: 0;">
                      ${team.shortName || team.name.slice(0, 2).toUpperCase()}
                    </div>

                    <!-- Team Info -->
                    <div style="flex: 1; overflow: hidden;">
                      <div style="display: flex; align-items: center; gap: 6px; margin-bottom: 2px;">
                        <h3 style="font-size: 15px; font-weight: 700; color: var(--color-on-surface); margin: 0; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;">
                          ${team.name}
                        </h3>
                        <span class="badge ${team.status === 'ACTIVE' ? 'badge-green' : team.status === 'INACTIVE' ? 'badge-amber' : 'badge-red'}" style="font-size: 10px;">
                          ${team.status === 'ACTIVE' ? 'Hoạt động' : team.status === 'INACTIVE' ? 'Tạm nghỉ' : 'Giải tán'}
                        </span>
                      </div>

                      <div style="font-size: 12px; color: var(--color-on-surface-variant); margin-bottom: 6px;">
                        ${ward ? ward.name : 'TP. Thủ Đức'} • ${memberCount} thành viên
                      </div>

                      <div style="display: flex; align-items: center; gap: 8px;">
                        <span class="badge ${isCaptain ? 'badge-green' : 'badge-gray'}" style="font-size: 10px; display: inline-flex; align-items: center; gap: 3px;">
                          ${isCaptain ? Icons.crown : Icons.users}
                          ${isCaptain ? 'Đội trưởng' : 'Thành viên'}
                        </span>

                        <span style="font-size: 11px; font-weight: 600; color: var(--color-primary);">
                          Uy tín: ${team.reputationScore}
                        </span>
                      </div>
                    </div>

                    <span style="color: var(--color-outline);">${Icons.chevronRight}</span>
                  </div>
                </div>
              `;
            }).join('')}
          </div>
        `}
      </div>
    `;

    // Event Bindings
    container.querySelector('#btn-create-team')?.addEventListener('click', () => router.navigate('team-create'));
    container.querySelector('#btn-system-teams')?.addEventListener('click', () => router.navigate('team-system'));
    container.querySelector('#btn-empty-create')?.addEventListener('click', () => router.navigate('team-create'));
    container.querySelector('#btn-empty-explore')?.addEventListener('click', () => router.navigate('team-system'));

    const bannerPending = container.querySelector('#banner-pending-invites');
    if (bannerPending && pendingInvites.length > 0) {
      bannerPending.addEventListener('click', () => {
        router.openSheet('invite-detail-sheet', { notifId: pendingInvites[0].id });
      });
    }

    container.querySelector('#btn-change-acting-team')?.addEventListener('click', () => {
      router.openSheet('select-acting-team-sheet');
    });

    container.querySelectorAll('.team-card-item').forEach(card => {
      card.addEventListener('click', () => {
        const teamId = card.getAttribute('data-team-id');
        router.navigate('team-detail', { teamId });
      });
    });
  });

  /* =========================================================================
     TEAM-02: System Teams List (Paginated, DEP-01)
     ========================================================================= */
  router.registerView('team-system', (container, params) => {
    router.setHeaderTitle('Danh sách đội hệ thống');
    const state = store.getState();
    const allTeams = state.teams || [];

    let currentPage = parseInt(params?.page || '1', 10);
    const pageSize = 4;
    const totalPages = Math.ceil(allTeams.length / pageSize) || 1;

    const startIdx = (currentPage - 1) * pageSize;
    const pagedTeams = allTeams.slice(startIdx, startIdx + pageSize);

    container.innerHTML = `
      <div style="padding: 16px 20px 32px;">
        <div style="margin-bottom: 16px;">
          <button class="btn btn-ghost" id="btn-sys-back" style="padding-left: 0; margin-bottom: 8px; gap: 4px;">
            ${Icons.back} <span style="font-size: 14px;">Quay lại</span>
          </button>
          <h2 style="font-size: 18px; font-weight: 800; color: var(--color-on-surface); margin: 0 0 4px;">
            Các đội bóng toàn hệ thống
          </h2>
          <p style="font-size: 12.5px; color: var(--color-on-surface-variant); margin: 0;">
            Hiển thị danh sách phân trang theo API USER-001 (DEP-01).
          </p>
        </div>

        <!-- Teams List -->
        <div style="display: flex; flex-direction: column; gap: 12px; margin-bottom: 20px;">
          ${pagedTeams.map(team => {
            const memberCount = state.teamMembers.filter(m => m.teamId === team.id && m.status === 'ACTIVE').length;
            const ward = state.wards?.find(w => w.id === team.wardId);

            return `
              <div class="card sys-team-card" data-team-id="${team.id}" style="cursor: pointer; padding: 14px;">
                <div style="display: flex; gap: 12px; align-items: center;">
                  <div style="width: 48px; height: 48px; border-radius: 12px; background: var(--color-secondary); color: #fff; display: flex; align-items: center; justify-content: center; font-weight: 700; font-size: 16px; flex-shrink: 0;">
                    ${team.shortName || team.name.slice(0, 2).toUpperCase()}
                  </div>

                  <div style="flex: 1; overflow: hidden;">
                    <div style="display: flex; align-items: center; gap: 6px; margin-bottom: 2px;">
                      <h4 style="font-size: 14.5px; font-weight: 700; margin: 0; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;">
                        ${team.name}
                      </h4>
                      <span class="badge ${team.status === 'ACTIVE' ? 'badge-green' : team.status === 'INACTIVE' ? 'badge-amber' : 'badge-red'}" style="font-size: 10px;">
                        ${team.status === 'ACTIVE' ? 'Hoạt động' : team.status === 'INACTIVE' ? 'Tạm nghỉ' : 'Giải tán'}
                      </span>
                    </div>

                    <div style="font-size: 12px; color: var(--color-on-surface-variant); margin-bottom: 4px;">
                      ${ward ? ward.name : 'TP. Thủ Đức'} • ${memberCount} thành viên
                    </div>

                    <div style="display: flex; gap: 4px; flex-wrap: wrap;">
                      ${team.skillLevels?.map(s => `
                        <span class="badge badge-gray" style="font-size: 9.5px;">${SKILL_LABELS[s] || s}</span>
                      `).join('')}
                    </div>
                  </div>

                  <span style="color: var(--color-outline);">${Icons.chevronRight}</span>
                </div>
              </div>
            `;
          }).join('')}
        </div>

        <!-- Pagination Controls -->
        <div style="display: flex; justify-content: space-between; align-items: center; background: var(--color-surface-container-low); padding: 8px 14px; border-radius: var(--radius-base);">
          <button class="btn btn-ghost btn-sm" id="btn-page-prev" ${currentPage <= 1 ? 'disabled' : ''}>
            ${Icons.back} Trước
          </button>
          <span style="font-size: 13px; font-weight: 600; color: var(--color-on-surface);">
            Trang ${currentPage} / ${totalPages}
          </span>
          <button class="btn btn-ghost btn-sm" id="btn-page-next" ${currentPage >= totalPages ? 'disabled' : ''}>
            Sau ${Icons.chevronRight}
          </button>
        </div>
      </div>
    `;

    container.querySelector('#btn-sys-back').addEventListener('click', () => router.navigate('teams'));

    container.querySelector('#btn-page-prev').addEventListener('click', () => {
      if (currentPage > 1) router.navigate('team-system', { page: currentPage - 1 });
    });

    container.querySelector('#btn-page-next').addEventListener('click', () => {
      if (currentPage < totalPages) router.navigate('team-system', { page: currentPage + 1 });
    });

    container.querySelectorAll('.sys-team-card').forEach(card => {
      card.addEventListener('click', () => {
        const teamId = card.getAttribute('data-team-id');
        router.navigate('team-detail', { teamId });
      });
    });
  });

  /* =========================================================================
     TEAM-03: Create Team (Stitch S7)
     Constraints: name required, ward required, max 3 skill levels
     ========================================================================= */
  router.registerView('team-create', (container) => {
    router.setHeaderTitle('Tạo đội bóng mới');
    const state = store.getState();
    const wards = state.wards || [];

    let selectedSkills = ['INTERMEDIATE'];

    container.innerHTML = `
      <div style="padding: 16px 20px 32px;">
        <button class="btn btn-ghost" id="btn-create-back" style="padding-left: 0; margin-bottom: 12px; gap: 4px;">
          ${Icons.back} <span style="font-size: 14px;">Quay lại</span>
        </button>

        <h2 style="font-size: 20px; font-weight: 800; color: var(--color-on-surface); margin: 0 0 6px;">
          Thành lập đội bóng mới
        </h2>
        <p style="font-size: 13px; color: var(--color-on-surface-variant); margin: 0 0 20px; line-height: 1.45;">
          Bạn sẽ tự động là Đội trưởng của đội này và có toàn quyền quản lý thành viên, tạo kèo và tuyển quân.
        </p>

        <form id="form-team-create" style="display: flex; flex-direction: column; gap: 16px;">
          <!-- Team Name -->
          <div class="form-group">
            <label class="form-label" for="team-name">
              Tên đội bóng <span style="color: var(--color-error);">*</span>
            </label>
            <input
              type="text"
              id="team-name"
              class="form-input"
              placeholder="VD: Bình Minh FC, Sài Gòn United"
              maxlength="60"
            />
            <div id="err-team-name" class="field-error-msg" style="display: none;"></div>
          </div>

          <!-- Short Name -->
          <div class="form-group">
            <label class="form-label" for="team-short-name">
              Tên viết tắt (Tối đa 6 ký tự viết hoa)
            </label>
            <input
              type="text"
              id="team-short-name"
              class="form-input"
              placeholder="VD: BMFC, SGU"
              maxlength="6"
              style="text-transform: uppercase;"
            />
          </div>

          <!-- Description -->
          <div class="form-group">
            <label class="form-label" for="team-desc">Giới thiệu đội bóng</label>
            <textarea
              id="team-desc"
              class="form-textarea"
              rows="3"
              placeholder="Lịch sinh hoạt định kỳ, phong cách đá, tiêu chí giao lưu..."
              maxlength="300"
            ></textarea>
            <span class="form-hint">Tối đa 300 ký tự</span>
          </div>

          <!-- Area / Ward -->
          <div class="form-group">
            <label class="form-label" for="team-ward">
              Khu vực hoạt động chính <span style="color: var(--color-error);">*</span>
            </label>
            <select id="team-ward" class="form-select">
              <option value="">-- Chọn Phường / Xã --</option>
              ${wards.map(w => `
                <option value="${w.id}">${w.name} (${w.district}, ${w.city})</option>
              `).join('')}
            </select>
            <div id="err-team-ward" class="field-error-msg" style="display: none;"></div>
          </div>

          <!-- Skill Level Chips (Max 3) -->
          <div class="form-group">
            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 4px;">
              <label class="form-label" style="margin-bottom: 0;">Trình độ thi đấu</label>
              <span class="form-hint">Chọn tối đa 3 mức</span>
            </div>
            <div style="display: flex; gap: 8px; flex-wrap: wrap;" id="skill-chips-row">
              ${[
                { id: 'BEGINNER', label: 'Mới chơi (Vui là chính)' },
                { id: 'INTERMEDIATE', label: 'Trung bình (Đã có kinh nghiệm)' },
                { id: 'ADVANCED', label: 'Khá - Giỏi (Chuyên nghiệp)' }
              ].map(s => `
                <button
                  type="button"
                  class="chip chip-selectable ${selectedSkills.includes(s.id) ? 'is-selected' : ''}"
                  data-skill="${s.id}"
                >
                  ${s.label}
                </button>
              `).join('')}
            </div>
          </div>

          <div style="margin-top: 16px;">
            <button type="submit" class="btn btn-primary btn-block" id="btn-submit-team">
              Tạo đội bóng ngay
            </button>
          </div>
        </form>
      </div>
    `;

    container.querySelector('#btn-create-back').addEventListener('click', () => router.navigate('teams'));

    // Toggle skill chips with max 3 rule
    const skillChips = container.querySelectorAll('#skill-chips-row .chip');
    skillChips.forEach(chip => {
      chip.addEventListener('click', () => {
        const skill = chip.getAttribute('data-skill');
        if (selectedSkills.includes(skill)) {
          selectedSkills = selectedSkills.filter(s => s !== skill);
          chip.classList.remove('is-selected');
        } else {
          if (selectedSkills.length >= 3) {
            router.showToast('Bạn chỉ được chọn tối đa 3 mức trình độ', 'error');
            return;
          }
          selectedSkills.push(skill);
          chip.classList.add('is-selected');
        }
      });
    });

    const form = container.querySelector('#form-team-create');
    const nameInput = container.querySelector('#team-name');
    const shortNameInput = container.querySelector('#team-short-name');
    const descInput = container.querySelector('#team-desc');
    const wardSelect = container.querySelector('#team-ward');
    const errName = container.querySelector('#err-team-name');
    const errWard = container.querySelector('#err-team-ward');

    form.addEventListener('submit', (e) => {
      e.preventDefault();
      errName.style.display = 'none';
      errWard.style.display = 'none';
      nameInput.classList.remove('has-error');
      wardSelect.classList.remove('has-error');

      const name = nameInput.value.trim();
      const shortName = shortNameInput.value.trim().toUpperCase();
      const description = descInput.value.trim();
      const wardId = wardSelect.value;

      if (!name) {
        errName.textContent = 'Vui lòng nhập tên đội bóng';
        errName.style.display = 'block';
        nameInput.classList.add('has-error');
        nameInput.focus();
        return;
      }

      if (!wardId) {
        errWard.textContent = 'Vui lòng chọn khu vực hoạt động của đội';
        errWard.style.display = 'block';
        wardSelect.classList.add('has-error');
        wardSelect.focus();
        return;
      }

      try {
        const newTeam = store.createTeam({
          name,
          shortName: shortName || name.slice(0, 4).toUpperCase(),
          description,
          wardId,
          skillLevels: selectedSkills.length > 0 ? selectedSkills : ['INTERMEDIATE']
        });

        router.showToast(`Đã tạo đội bóng "${newTeam.name}" thành công!`, 'success');
        router.navigate('team-detail', { teamId: newTeam.id });
      } catch (err) {
        router.showToast(`Lỗi: ${err.message}`, 'error');
      }
    });
  });

  /* =========================================================================
     TEAM-04 & TEAM-13: Team Detail View (ACTIVE vs DISBANDED / INACTIVE)
     Role variants: CAPTAIN, MEMBER, OUTSIDER
     ========================================================================= */
  router.registerView('team-detail', (container, params) => {
    const teamId = params?.teamId;
    const state = store.getState();
    const user = store.getCurrentUser();
    const team = state.teams.find(t => t.id === teamId);

    if (!team) {
      router.setHeaderTitle('Không tìm thấy đội');
      container.innerHTML = `
        <div class="empty-state" style="margin-top: 40px;">
          <h3 class="empty-state-title">Đội bóng không tồn tại</h3>
          <p class="empty-state-desc">Đội bóng này có thể đã bị xóa hoặc đường dẫn không đúng.</p>
          <button class="btn btn-primary" onclick="window.AppRouter.navigate('teams')">Về danh sách đội</button>
        </div>
      `;
      return;
    }

    router.setHeaderTitle(team.name);

    // Determine viewer role
    const isCaptain = team.captainId === user?.id;
    const isMember = state.teamMembers.some(m => m.teamId === team.id && m.userId === user?.id && m.status === 'ACTIVE');
    const isDisbanded = team.status === 'DISBANDED';
    const isInactive = team.status === 'INACTIVE';

    const ward = state.wards?.find(w => w.id === team.wardId);
    const wardName = ward ? `${ward.name}, ${ward.district}` : 'TP. Thủ Đức';

    const captainUser = state.characters[team.captainId];
    const captainName = captainUser ? (captainUser.isDeleted ? 'Người dùng đã xóa' : captainUser.displayName) : 'Đang cập nhật';

    // Active members
    const activeMembers = state.teamMembers.filter(m => m.teamId === team.id && m.status === 'ACTIVE');

    // Matches of this team
    const teamMatches = state.matches?.filter(m => m.hostTeamId === team.id || m.confirmedGuestTeamId === team.id) || [];

    // Current tab inside detail
    let activeTab = params?.tab || 'overview';

    container.innerHTML = `
      <div style="padding: 16px 20px 32px;">
        <button class="btn btn-ghost" id="btn-detail-back" style="padding-left: 0; margin-bottom: 8px; gap: 4px;">
          ${Icons.back} <span style="font-size: 14px;">Quay lại</span>
        </button>

        <!-- TEAM-13: Status Notice Banner if Disbanded or Inactive -->
        ${isDisbanded ? `
          <div class="notice notice-danger" style="margin-bottom: 16px;">
            <div style="display: flex; gap: 8px; align-items: flex-start;">
              <span style="color: var(--color-error);">${Icons.warning}</span>
              <div>
                <strong>Đội bóng đã giải tán vĩnh viễn</strong>
                <p style="margin: 4px 0 0; font-size: 12.5px; line-height: 1.4;">
                  Thời điểm: ${new Date(team.disbandedAt || Date.now()).toLocaleDateString('vi-VN')}
                  <br>Lý do: ${team.disbandReason || 'Đội trưởng giải tán đội'}
                </p>
              </div>
            </div>
          </div>
        ` : isInactive ? `
          <div class="notice notice-warning" style="margin-bottom: 16px;">
            <strong>Đội bóng đang tạm ngừng hoạt động</strong>
            <p style="margin: 4px 0 0; font-size: 12.5px;">${team.inactiveReason || 'Tạm ngừng sinh hoạt thi đấu.'}</p>
          </div>
        ` : ''}

        <!-- Team Header Card -->
        <div class="card" style="margin-bottom: 16px; text-align: center; padding: 20px 16px;">
          <div style="width: 72px; height: 72px; border-radius: 20px; background: ${isDisbanded ? '#64748b' : 'var(--color-primary)'}; color: #fff; display: flex; align-items: center; justify-content: center; font-size: 24px; font-weight: 800; margin: 0 auto 12px; box-shadow: 0 6px 18px rgba(0,0,0,0.1);">
            ${team.shortName || team.name.slice(0, 2).toUpperCase()}
          </div>

          <div style="display: flex; justify-content: center; align-items: center; gap: 8px; margin-bottom: 4px;">
            <h1 style="font-size: 20px; font-weight: 800; color: var(--color-on-surface); margin: 0;">
              ${team.name}
            </h1>
            <span class="badge ${team.status === 'ACTIVE' ? 'badge-green' : team.status === 'INACTIVE' ? 'badge-amber' : 'badge-red'}" style="font-size: 10px;">
              ${team.status === 'ACTIVE' ? 'Hoạt động' : team.status === 'INACTIVE' ? 'Tạm nghỉ' : 'Giải tán'}
            </span>
          </div>

          <div style="font-size: 12.5px; color: var(--color-on-surface-variant); margin-bottom: 12px;">
            ${wardName} • Thành lập: ${new Date(team.createdAt || Date.now()).toLocaleDateString('vi-VN')}
          </div>

          <!-- Viewer Role Indicator -->
          <div style="display: inline-flex; align-items: center; gap: 6px; padding: 4px 12px; border-radius: var(--radius-full); background: var(--color-surface-container); font-size: 12px; font-weight: 600; color: var(--color-on-surface); margin-bottom: 16px;">
            ${isCaptain ? `${Icons.crown} Bạn là Đội trưởng` : isMember ? `${Icons.users} Bạn là Thành viên` : 'Khách xem'}
          </div>

          <!-- Skill Level Badges -->
          <div style="display: flex; justify-content: center; gap: 6px; flex-wrap: wrap;">
            ${team.skillLevels?.map(s => `
              <span class="badge badge-gray" style="font-size: 11px;">${SKILL_LABELS[s] || s}</span>
            `).join('')}
          </div>
        </div>

        <!-- Team Stats Row -->
        <div class="card" style="margin-bottom: 16px; padding: 12px;">
          <div style="display: grid; grid-template-columns: repeat(4, 1fr); gap: 6px; text-align: center;">
            <div>
              <div style="font-size: 18px; font-weight: 800; color: var(--color-primary);">${team.reputationScore}</div>
              <div style="font-size: 10.5px; color: var(--color-on-surface-variant); margin-top: 2px;">Điểm uy tín</div>
            </div>
            <div>
              <div style="font-size: 18px; font-weight: 800; color: var(--color-on-surface);">${team.ratingAvg || '5.0'}★</div>
              <div style="font-size: 10.5px; color: var(--color-on-surface-variant); margin-top: 2px;">Đánh giá</div>
            </div>
            <div>
              <div style="font-size: 18px; font-weight: 800; color: var(--color-on-surface);">${team.matchCount >= 5 && team.completionRate !== null ? team.completionRate + '%' : '<5 trận'}</div>
              <div style="font-size: 10.5px; color: var(--color-on-surface-variant); margin-top: 2px;">Hoàn thành</div>
            </div>
            <div>
              <div style="font-size: 18px; font-weight: 800; color: var(--color-on-surface);">${team.matchCount}</div>
              <div style="font-size: 10.5px; color: var(--color-on-surface-variant); margin-top: 2px;">Trận đấu</div>
            </div>
          </div>
        </div>

        <!-- Detail Internal Tab Bar -->
        <div class="tab-bar" style="margin-bottom: 16px;">
          <button class="tab-item ${activeTab === 'overview' ? 'is-active' : ''}" id="tab-btn-overview">
            Tổng quan & Lịch kèo
          </button>
          <button class="tab-item ${activeTab === 'members' ? 'is-active' : ''}" id="tab-btn-members">
            Thành viên (${activeMembers.length})
          </button>
        </div>

        <!-- Tab 1: Overview & Matches -->
        <div id="tab-content-overview" style="display: ${activeTab === 'overview' ? 'block' : 'none'};">
          <div class="card" style="margin-bottom: 16px;">
            <div class="card-header">
              <h3 class="card-title" style="font-size: 14.5px;">Mô tả & Hoạt động</h3>
            </div>
            <div class="card-body">
              <p style="font-size: 13.5px; color: var(--color-on-surface); line-height: 1.5; margin: 0 0 14px;">
                ${team.description || 'Chưa có thông tin giới thiệu đội bóng.'}
              </p>

              <!-- Captain Info Row -->
              <div style="background: var(--color-surface-container-low); padding: 10px 12px; border-radius: var(--radius-base); display: flex; justify-content: space-between; align-items: center;">
                <div style="display: flex; align-items: center; gap: 8px;">
                  <span style="color: var(--color-warning);">${Icons.crown}</span>
                  <div>
                    <span style="font-size: 11px; color: var(--color-on-surface-variant); display: block;">Đội trưởng hiện tại</span>
                    <strong style="font-size: 13.5px; color: var(--color-on-surface);">${captainName}</strong>
                  </div>
                </div>
                <button class="btn btn-ghost btn-sm" id="btn-view-captain-history" style="font-size: 12px; color: var(--color-primary); padding: 4px 8px;">
                  Lịch sử ${Icons.chevronRight}
                </button>
              </div>
            </div>
          </div>

          <!-- Team Matches List -->
          <div class="card" style="margin-bottom: 20px;">
            <div class="card-header">
              <h3 class="card-title" style="font-size: 14.5px;">Lịch thi đấu của đội</h3>
            </div>
            <div class="card-body" style="padding: 0;">
              ${teamMatches.length === 0 ? `
                <div style="padding: 24px 16px; text-align: center; color: var(--color-on-surface-variant); font-size: 13px;">
                  Chưa có trận đấu nào được lên lịch.
                </div>
              ` : `
                <div style="display: flex; flex-direction: column;">
                  ${teamMatches.map(m => `
                    <div class="list-item" style="border-bottom: 1px solid var(--color-surface-container); cursor: pointer;" onclick="window.AppRouter.navigate('match-detail', { matchId: '${m.id}' })">
                      <div style="width: 100%;">
                        <div style="display: flex; justify-content: space-between; margin-bottom: 4px;">
                          <strong style="font-size: 13.5px;">Kèo ${m.kind === 'A' ? 'A (Cố định)' : 'B (Linh hoạt)'}</strong>
                          <span class="badge ${m.status === 'CONFIRMED' ? 'badge-green' : 'badge-amber'}" style="font-size: 10px;">${m.status}</span>
                        </div>
                        <div style="font-size: 12px; color: var(--color-on-surface-variant);">
                          ${m.matchDate} • ${m.startTime} - ${m.endTime}
                        </div>
                      </div>
                    </div>
                  `).join('')}
                </div>
              `}
            </div>
          </div>
        </div>

        <!-- Tab 2: Members List (TEAM-06) -->
        <div id="tab-content-members" style="display: ${activeTab === 'members' ? 'block' : 'none'};">
          <div class="card" style="margin-bottom: 20px; padding: 0;">
            <div class="card-header" style="display: flex; justify-content: space-between; align-items: center; padding: 12px 16px;">
              <h3 class="card-title" style="font-size: 14.5px;">Danh sách thành viên</h3>
              ${isCaptain && !isDisbanded ? `
                <button class="btn btn-primary btn-sm" id="btn-invite-member-tab" style="padding: 6px 10px; font-size: 12px; gap: 4px;">
                  ${Icons.userPlus} Mời thêm
                </button>
              ` : ''}
            </div>

            <div class="card-body" style="padding: 0;">
              <div style="display: flex; flex-direction: column;">
                ${activeMembers.map(m => {
                  const mUser = state.characters[m.userId];
                  const mName = mUser ? (mUser.isDeleted ? 'Người dùng đã xóa' : mUser.displayName) : 'Thành viên';
                  const isCap = m.role === 'CAPTAIN';

                  return `
                    <div class="list-item" style="border-bottom: 1px solid var(--color-surface-container);">
                      <div style="display: flex; gap: 12px; align-items: center; width: 100%;">
                        <div class="avatar avatar-sm">
                          ${mUser?.avatarUrl && !mUser?.isDeleted ? `<img src="${mUser.avatarUrl}">` : `<span>${mName.slice(0,1).toUpperCase()}</span>`}
                        </div>
                        <div style="flex: 1;">
                          <div style="display: flex; align-items: center; gap: 6px;">
                            <strong style="font-size: 13.5px; color: var(--color-on-surface);">${mName}</strong>
                            ${m.userId === user?.id ? '<span class="badge badge-gray" style="font-size: 9px;">Bạn</span>' : ''}
                          </div>
                          <span style="font-size: 11px; color: var(--color-on-surface-variant);">
                            Tham gia: ${new Date(m.joinedAt || Date.now()).toLocaleDateString('vi-VN')}
                          </span>
                        </div>
                        <span class="badge ${isCap ? 'badge-green' : 'badge-gray'}" style="font-size: 10px;">
                          ${isCap ? 'Đội trưởng' : 'Thành viên'}
                        </span>
                      </div>
                    </div>
                  `;
                }).join('')}
              </div>
            </div>
          </div>
        </div>

        <!-- Action Buttons Section (Role & Status sensitive) -->
        <div style="display: flex; flex-direction: column; gap: 10px;">
          ${isCaptain && !isDisbanded ? `
            <!-- Captain Actions for ACTIVE team -->
            <button class="btn btn-primary btn-block" id="btn-invite-member" style="gap: 6px;">
              ${Icons.userPlus}
              <span>Mời thành viên mới</span>
            </button>

            <button class="btn btn-secondary btn-block" id="btn-edit-team" style="gap: 6px;">
              ${Icons.edit}
              <span>Chỉnh sửa thông tin đội</span>
            </button>

            <button class="btn btn-secondary btn-block" id="btn-transfer-captain" style="gap: 6px;">
              ${Icons.crown}
              <span>Chuyển giao quyền đội trưởng</span>
            </button>

            <button class="btn btn-danger-outline btn-block" id="btn-disband-team" style="gap: 6px;">
              ${Icons.trash}
              <span>Giải tán đội bóng</span>
            </button>
          ` : isMember && !isCaptain && !isDisbanded ? `
            <!-- Member Actions for ACTIVE team -->
            <button class="btn btn-danger-outline btn-block" id="btn-leave-team" style="gap: 6px;">
              ${Icons.logout}
              <span>Rời khỏi đội bóng</span>
            </button>
          ` : ''}
        </div>
      </div>
    `;

    // Tab Switching
    container.querySelector('#btn-detail-back').addEventListener('click', () => router.navigate('teams'));

    const tabBtnOverview = container.querySelector('#tab-btn-overview');
    const tabBtnMembers = container.querySelector('#tab-btn-members');
    const contentOverview = container.querySelector('#tab-content-overview');
    const contentMembers = container.querySelector('#tab-content-members');

    tabBtnOverview.addEventListener('click', () => {
      tabBtnOverview.classList.add('is-active');
      tabBtnMembers.classList.remove('is-active');
      contentOverview.style.display = 'block';
      contentMembers.style.display = 'none';
    });

    tabBtnMembers.addEventListener('click', () => {
      tabBtnMembers.classList.add('is-active');
      tabBtnOverview.classList.remove('is-active');
      contentMembers.style.display = 'block';
      contentOverview.style.display = 'none';
    });

    container.querySelector('#btn-view-captain-history')?.addEventListener('click', () => {
      router.navigate('captain-history', { teamId: team.id });
    });

    // Action button bindings
    container.querySelector('#btn-invite-member')?.addEventListener('click', () => {
      router.openSheet('team-invite-sheet', { teamId: team.id });
    });

    container.querySelector('#btn-invite-member-tab')?.addEventListener('click', () => {
      router.openSheet('team-invite-sheet', { teamId: team.id });
    });

    container.querySelector('#btn-edit-team')?.addEventListener('click', () => {
      router.navigate('team-edit', { teamId: team.id });
    });

    container.querySelector('#btn-transfer-captain')?.addEventListener('click', () => {
      router.openSheet('transfer-captain-sheet', { teamId: team.id });
    });

    container.querySelector('#btn-disband-team')?.addEventListener('click', () => {
      router.openSheet('disband-team-sheet', { teamId: team.id });
    });

    container.querySelector('#btn-leave-team')?.addEventListener('click', () => {
      router.openSheet('leave-team-sheet', { teamId: team.id });
    });
  });

  /* =========================================================================
     TEAM-05: Edit Team View
     ========================================================================= */
  router.registerView('team-edit', (container, params) => {
    const teamId = params?.teamId;
    const state = store.getState();
    const user = store.getCurrentUser();
    const team = state.teams.find(t => t.id === teamId);

    if (!team) {
      router.navigate('teams');
      return;
    }

    if (team.captainId !== user?.id) {
      router.setHeaderTitle('Không có quyền');
      container.innerHTML = `
        <div class="empty-state" style="margin-top: 40px;">
          <h3 class="empty-state-title">Từ chối truy cập</h3>
          <p class="empty-state-desc">Chỉ đội trưởng mới có quyền chỉnh sửa thông tin đội bóng.</p>
          <button class="btn btn-primary" onclick="window.AppRouter.navigate('team-detail', { teamId: '${team.id}' })">Quay lại chi tiết</button>
        </div>
      `;
      return;
    }

    router.setHeaderTitle('Chỉnh sửa đội bóng');
    let selectedSkills = [...(team.skillLevels || ['INTERMEDIATE'])];

    container.innerHTML = `
      <div style="padding: 16px 20px 32px;">
        <button class="btn btn-ghost" id="btn-edit-back" style="padding-left: 0; margin-bottom: 12px; gap: 4px;">
          ${Icons.back} <span style="font-size: 14px;">Quay lại</span>
        </button>

        <h2 style="font-size: 20px; font-weight: 800; color: var(--color-on-surface); margin: 0 0 16px;">
          Sửa thông tin: ${team.name}
        </h2>

        <form id="form-team-edit" style="display: flex; flex-direction: column; gap: 16px;">
          <div class="form-group">
            <label class="form-label" for="edit-team-name">Tên đội bóng *</label>
            <input type="text" id="edit-team-name" class="form-input" value="${team.name}" maxlength="60" />
            <div id="err-edit-name" class="field-error-msg" style="display: none;"></div>
          </div>

          <div class="form-group">
            <label class="form-label" for="edit-short-name">Tên viết tắt</label>
            <input type="text" id="edit-short-name" class="form-input" value="${team.shortName || ''}" maxlength="6" style="text-transform: uppercase;" />
          </div>

          <div class="form-group">
            <label class="form-label" for="edit-desc">Giới thiệu đội bóng</label>
            <textarea id="edit-desc" class="form-textarea" rows="3" maxlength="300">${team.description || ''}</textarea>
          </div>

          <div class="form-group">
            <label class="form-label" for="edit-ward">Khu vực sinh hoạt *</label>
            <select id="edit-ward" class="form-select">
              ${state.wards?.map(w => `
                <option value="${w.id}" ${team.wardId === w.id ? 'selected' : ''}>${w.name} (${w.district}, ${w.city})</option>
              `).join('')}
            </select>
          </div>

          <div class="form-group">
            <label class="form-label">Trình độ thi đấu (Tối đa 3)</label>
            <div style="display: flex; gap: 8px; flex-wrap: wrap;" id="edit-skill-chips">
              ${[
                { id: 'BEGINNER', label: 'Mới chơi' },
                { id: 'INTERMEDIATE', label: 'Trung bình' },
                { id: 'ADVANCED', label: 'Khá - Giỏi' }
              ].map(s => `
                <button
                  type="button"
                  class="chip chip-selectable ${selectedSkills.includes(s.id) ? 'is-selected' : ''}"
                  data-skill="${s.id}"
                >
                  ${s.label}
                </button>
              `).join('')}
            </div>
          </div>

          <div style="display: flex; gap: 10px; margin-top: 16px;">
            <button type="button" class="btn btn-secondary" style="flex: 1;" id="btn-cancel-edit">
              Hủy
            </button>
            <button type="submit" class="btn btn-primary" style="flex: 2;">
              Lưu thay đổi
            </button>
          </div>
        </form>
      </div>
    `;

    container.querySelector('#btn-edit-back').addEventListener('click', () => router.navigate('team-detail', { teamId: team.id }));
    container.querySelector('#btn-cancel-edit').addEventListener('click', () => router.navigate('team-detail', { teamId: team.id }));

    const editSkillChips = container.querySelectorAll('#edit-skill-chips .chip');
    editSkillChips.forEach(chip => {
      chip.addEventListener('click', () => {
        const skill = chip.getAttribute('data-skill');
        if (selectedSkills.includes(skill)) {
          selectedSkills = selectedSkills.filter(s => s !== skill);
          chip.classList.remove('is-selected');
        } else {
          if (selectedSkills.length >= 3) {
            router.showToast('Tối đa 3 mức trình độ', 'error');
            return;
          }
          selectedSkills.push(skill);
          chip.classList.add('is-selected');
        }
      });
    });

    const form = container.querySelector('#form-team-edit');
    const nameInput = container.querySelector('#edit-team-name');
    const shortInput = container.querySelector('#edit-short-name');
    const descInput = container.querySelector('#edit-desc');
    const wardSelect = container.querySelector('#edit-ward');
    const errName = container.querySelector('#err-edit-name');

    form.addEventListener('submit', (e) => {
      e.preventDefault();
      const name = nameInput.value.trim();
      if (!name) {
        errName.textContent = 'Tên đội bóng không được để trống';
        errName.style.display = 'block';
        nameInput.classList.add('has-error');
        return;
      }

      team.name = name;
      team.shortName = shortInput.value.trim().toUpperCase() || name.slice(0, 4).toUpperCase();
      team.description = descInput.value.trim();
      team.wardId = wardSelect.value;
      team.skillLevels = selectedSkills.length > 0 ? selectedSkills : ['INTERMEDIATE'];

      store.setState({ ...store.getState() });
      router.showToast('Cập nhật thông tin đội thành công!', 'success');
      router.navigate('team-detail', { teamId: team.id });
    });
  });

  /* =========================================================================
     TEAM-06: Team Members List Route (TEAM-06)
     ========================================================================= */
  router.registerView('team-members', (container, params) => {
    // Delegates to team-detail with members tab active
    router.navigate('team-detail', { ...params, tab: 'members' }, true);
  });

  /* =========================================================================
     TEAM-10: Captain History View (Stitch S8b / S14)
     ========================================================================= */
  router.registerView('captain-history', (container, params) => {
    const teamId = params?.teamId;
    const state = store.getState();
    const team = state.teams.find(t => t.id === teamId);

    if (!team) {
      router.navigate('teams');
      return;
    }

    router.setHeaderTitle('Lịch sử đội trưởng');
    const history = team.captainHistory || [{ captainId: team.captainId, assignedAt: team.createdAt }];

    container.innerHTML = `
      <div style="padding: 16px 20px 32px;">
        <button class="btn btn-ghost" id="btn-cap-hist-back" style="padding-left: 0; margin-bottom: 8px; gap: 4px;">
          ${Icons.back} <span style="font-size: 14px;">Quay lại</span>
        </button>

        <h2 style="font-size: 18px; font-weight: 800; color: var(--color-on-surface); margin: 0 0 4px;">
          Lịch sử ban cán sự: ${team.name}
        </h2>
        <p style="font-size: 12.5px; color: var(--color-on-surface-variant); margin: 0 0 20px;">
          Ghi nhận các mốc thời gian chuyển giao quyền đội trưởng theo USER-001.
        </p>

        <!-- Vertical Timeline Card matching Stitch S8b -->
        <div class="card" style="padding: 20px 16px;">
          <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; border-bottom: 1px solid var(--color-surface-container); padding-bottom: 8px;">
            <span style="font-weight: 700; font-size: 14px;">Nhật ký chuyển giao</span>
            <span class="badge badge-gray" style="font-size: 11px;">${history.length} bản ghi</span>
          </div>

          <div style="position: relative; padding-left: 20px;">
            <!-- Vertical line -->
            <div style="position: absolute; left: 6px; top: 8px; bottom: 8px; width: 2px; background: var(--color-outline-variant);"></div>

            <div style="display: flex; flex-direction: column; gap: 20px;">
              ${history.slice().reverse().map((item, idx) => {
                const captain = state.characters[item.captainId];
                const cName = captain ? (captain.isDeleted ? 'Người dùng đã xóa' : captain.displayName) : 'Không rõ';
                const isCurrent = idx === 0 && team.status === 'ACTIVE';

                return `
                  <div style="position: relative;">
                    <!-- Timeline Node -->
                    <div style="position: absolute; left: -20px; top: 4px; width: 14px; height: 14px; border-radius: 50%; background: ${isCurrent ? 'var(--color-primary)' : 'var(--color-outline)'}; border: 3px solid #fff; box-shadow: 0 0 0 1px var(--color-outline);"></div>

                    <div style="padding-left: 4px;">
                      <div style="display: flex; align-items: center; gap: 8px; margin-bottom: 2px;">
                        <strong style="font-size: 14px; color: var(--color-on-surface);">${cName}</strong>
                        ${isCurrent ? '<span class="badge badge-green" style="font-size: 9.5px;">Đội trưởng hiện tại</span>' : '<span class="badge badge-gray" style="font-size: 9.5px;">Trước đây</span>'}
                      </div>

                      <div style="font-size: 12px; color: var(--color-on-surface-variant); margin-bottom: 4px;">
                        ${new Date(item.assignedAt || Date.now()).toLocaleString('vi-VN')}
                      </div>

                      ${item.reason ? `
                        <div style="background: var(--color-surface-container-low); border: 1px solid var(--color-outline-variant); border-radius: var(--radius-sm); padding: 6px 10px; font-size: 12px; color: var(--color-on-surface); margin-top: 4px;">
                          <strong>Lý do:</strong> ${item.reason}
                        </div>
                      ` : ''}
                    </div>
                  </div>
                `;
              }).join('')}
            </div>
          </div>
        </div>
      </div>
    `;

    container.querySelector('#btn-cap-hist-back').addEventListener('click', () => {
      router.navigate('team-detail', { teamId: team.id });
    });
  });

  /* =========================================================================
     TEAM-07: Invite Member Sheet (Stitch S9, DEP-02)
     ========================================================================= */
  router.registerSheet('team-invite-sheet', (body, footer, params, titleElem) => {
    if (titleElem) titleElem.textContent = 'Mời thành viên vào đội';
    const teamId = params?.teamId;
    const state = store.getState();
    const currentUser = store.getCurrentUser();
    const team = state.teams.find(t => t.id === teamId);

    if (!team) {
      body.innerHTML = '<p>Đội không tồn tại.</p>';
      return;
    }

    // Fixture candidate players (excluding current user and deleted users)
    const candidates = Object.values(state.characters).filter(
      c => c.id !== currentUser?.id && !c.isDeleted && c.role !== 'ADMIN'
    );

    body.innerHTML = `
      <div style="padding: 4px 0 16px;">
        <p style="font-size: 13px; color: var(--color-on-surface-variant); margin: 0 0 16px; line-height: 1.45;">
          Gửi lời mời tham gia <strong>${team.name}</strong>. Cầu thủ sẽ nhận được thông báo trong ứng dụng (DEP-02).
        </p>

        <!-- Search Bar Simulation -->
        <div class="form-group" style="margin-bottom: 16px;">
          <input
            type="text"
            id="invite-search-input"
            class="form-input"
            placeholder="Tìm theo tên cầu thủ hoặc số điện thoại..."
            style="font-size: 13.5px;"
          />
        </div>

        <!-- Candidate List -->
        <div style="font-size: 12px; font-weight: 700; color: var(--color-on-surface-variant); text-transform: uppercase; margin-bottom: 10px;">
          Danh sách cầu thủ gợi ý
        </div>

        <div style="display: flex; flex-direction: column; gap: 8px; max-height: 260px; overflow-y: auto;" id="invite-candidates-list">
          ${candidates.map(player => {
            const isAlreadyMember = state.teamMembers.some(m => m.teamId === team.id && m.userId === player.id && m.status === 'ACTIVE');
            const hasPendingInvite = state.notifications.some(n => n.userId === player.id && n.type === 'TEAM_INVITATION' && n.data?.teamId === team.id && !n.read);

            return `
              <div class="candidate-item-row" data-name="${player.displayName?.toLowerCase() || ''}" data-phone="${player.phone || ''}" style="display: flex; align-items: center; justify-content: space-between; padding: 10px; background: var(--color-surface-container-low); border: 1px solid var(--color-outline-variant); border-radius: var(--radius-base);">
                <div style="display: flex; align-items: center; gap: 10px;">
                  <div class="avatar avatar-sm">
                    ${player.avatarUrl ? `<img src="${player.avatarUrl}">` : `<span>${(player.displayName || 'U').slice(0,1)}</span>`}
                  </div>
                  <div>
                    <strong style="font-size: 13.5px; color: var(--color-on-surface); display: block;">${player.displayName || player.phone}</strong>
                    <span style="font-size: 11.5px; color: var(--color-on-surface-variant);">${player.preferredPosition || 'Chưa chọn vị trí'} • Uy tín: ${player.reputationScore}</span>
                  </div>
                </div>

                <div>
                  ${isAlreadyMember ? `
                    <span class="badge badge-gray" style="font-size: 10px;">Đã là thành viên</span>
                  ` : hasPendingInvite ? `
                    <span class="badge badge-amber" style="font-size: 10px;">Đã gửi lời mời</span>
                  ` : `
                    <button class="btn btn-primary btn-sm btn-send-invite" data-player-id="${player.id}" style="padding: 6px 12px; font-size: 12px;">
                      Mời
                    </button>
                  `}
                </div>
              </div>
            `;
          }).join('')}
        </div>
      </div>
    `;

    footer.innerHTML = `
      <button class="btn btn-secondary btn-block" onclick="window.AppRouter.closeSheet()">
        Đóng
      </button>
    `;

    // Search filter
    const searchInput = body.querySelector('#invite-search-input');
    const rows = body.querySelectorAll('.candidate-item-row');
    searchInput.addEventListener('input', () => {
      const q = searchInput.value.toLowerCase().trim();
      rows.forEach(r => {
        const name = r.getAttribute('data-name');
        const phone = r.getAttribute('data-phone');
        if (!q || name.includes(q) || phone.includes(q)) {
          r.style.display = 'flex';
        } else {
          r.style.display = 'none';
        }
      });
    });

    // Send Invite Handler
    body.querySelectorAll('.btn-send-invite').forEach(btn => {
      btn.addEventListener('click', () => {
        const playerId = btn.getAttribute('data-player-id');
        const targetPlayer = state.characters[playerId];

        // Create new notification for invited player
        const newNotif = {
          id: 'notif-' + Date.now(),
          userId: playerId,
          type: 'TEAM_INVITATION',
          title: 'Lời mời gia nhập đội bóng',
          content: `Đội trưởng ${currentUser.displayName || 'Đội trưởng'} đã mời bạn tham gia đội bóng "${team.name}".`,
          referenceType: 'TEAM',
          referenceId: team.id,
          data: {
            teamId: team.id,
            teamName: team.name,
            inviterId: currentUser.id,
            inviterName: currentUser.displayName,
            invitedAt: state.meta.mockClock
          },
          read: false,
          createdAt: state.meta.mockClock
        };

        state.notifications.unshift(newNotif);
        store.setState({ ...state });

        router.closeSheet();
        router.showToast(`Đã gửi lời mời tới ${targetPlayer.displayName || targetPlayer.phone}!`, 'success');
      });
    });
  });

  /* =========================================================================
     TEAM-08: Invite Response Sheet (From Notification, UC-T03)
     ========================================================================= */
  router.registerSheet('invite-detail-sheet', (body, footer, params, titleElem) => {
    if (titleElem) titleElem.textContent = 'Lời mời gia nhập đội bóng';
    const notifId = params?.notifId;
    const teamId = params?.teamId;
    const state = store.getState();
    const currentUser = store.getCurrentUser();

    // Resolve notification
    const notif = state.notifications.find(n => n.id === notifId) ||
      state.notifications.find(n => n.userId === currentUser?.id && n.type === 'TEAM_INVITATION' && (teamId ? n.data?.teamId === teamId : !n.read));

    const targetTeamId = teamId || notif?.data?.teamId || notif?.referenceId;
    const team = state.teams.find(t => t.id === targetTeamId);

    if (!team) {
      body.innerHTML = `
        <div class="notice notice-danger">
          Đội bóng này không còn tồn tại trong hệ thống.
        </div>
      `;
      footer.innerHTML = `<button class="btn btn-secondary btn-block" onclick="window.AppRouter.closeSheet()">Đóng</button>`;
      return;
    }

    const isDisbanded = team.status === 'DISBANDED';
    const isAlreadyMember = state.teamMembers.some(m => m.teamId === team.id && m.userId === currentUser?.id && m.status === 'ACTIVE');
    const ward = state.wards?.find(w => w.id === team.wardId);

    body.innerHTML = `
      <div style="padding: 4px 0 16px;">
        ${isDisbanded ? `
          <div class="notice notice-danger" style="margin-bottom: 14px;">
            Đội bóng này đã giải tán. Lời mời không còn giá trị sử dụng.
          </div>
        ` : isAlreadyMember ? `
          <div class="notice notice-success" style="margin-bottom: 14px;">
            Bạn đã là thành viên của đội bóng này.
          </div>
        ` : ''}

        <!-- Inviting Team Preview Card -->
        <div style="background: var(--color-surface-container-low); border: 1px solid var(--color-outline-variant); border-radius: var(--radius-base); padding: 16px; margin-bottom: 16px; text-align: center;">
          <div style="width: 60px; height: 60px; border-radius: 16px; background: var(--color-primary); color: #fff; display: flex; align-items: center; justify-content: center; font-size: 20px; font-weight: 800; margin: 0 auto 10px;">
            ${team.shortName || team.name.slice(0, 2).toUpperCase()}
          </div>
          <h3 style="font-size: 17px; font-weight: 800; margin: 0 0 4px; color: var(--color-on-surface);">${team.name}</h3>
          <p style="font-size: 12.5px; color: var(--color-on-surface-variant); margin: 0 0 10px;">
            ${ward ? ward.name : 'TP. Thủ Đức'} • Điểm uy tín: <strong>${team.reputationScore}</strong>
          </p>

          <p style="font-size: 13px; color: var(--color-on-surface); line-height: 1.45; margin: 0; font-style: italic;">
            "${team.description || 'Tham gia cùng chúng tôi để thi đấu phong trào!'}"
          </p>
        </div>

        <p style="font-size: 13px; color: var(--color-on-surface-variant); line-height: 1.45; margin: 0;">
          Người mời: <strong>${notif?.data?.inviterName || 'Đội trưởng'}</strong>
        </p>
      </div>
    `;

    footer.innerHTML = `
      <div style="display: flex; gap: 10px; width: 100%;">
        <button class="btn btn-secondary" style="flex: 1;" id="btn-reject-invite" ${isDisbanded || isAlreadyMember ? 'disabled' : ''}>
          Từ chối
        </button>
        <button class="btn btn-primary" style="flex: 1;" id="btn-accept-invite" ${isDisbanded || isAlreadyMember ? 'disabled' : ''}>
          Chấp nhận tham gia
        </button>
      </div>
    `;

    footer.querySelector('#btn-reject-invite').addEventListener('click', () => {
      if (notif) notif.read = true;
      store.setState({ ...state });
      router.closeSheet();
      router.showToast(`Đã từ chối lời mời vào đội ${team.name}.`, 'info');
    });

    footer.querySelector('#btn-accept-invite').addEventListener('click', () => {
      // Add member to team
      state.teamMembers.push({
        teamId: team.id,
        userId: currentUser.id,
        role: 'MEMBER',
        joinedAt: state.meta.mockClock,
        status: 'ACTIVE'
      });

      if (notif) notif.read = true;
      store.setState({ ...state });

      router.closeSheet();
      router.showToast(`Chúc mừng! Bạn đã gia nhập đội "${team.name}".`, 'success');
      router.navigate('team-detail', { teamId: team.id });
    });
  });

  /* =========================================================================
     TEAM-09: Transfer Captaincy Sheet (Stitch S10, Instant effect)
     ========================================================================= */
  router.registerSheet('transfer-captain-sheet', (body, footer, params, titleElem) => {
    if (titleElem) titleElem.textContent = 'Chuyển giao quyền đội trưởng';
    const teamId = params?.teamId;
    const state = store.getState();
    const currentUser = store.getCurrentUser();
    const team = state.teams.find(t => t.id === teamId);

    if (!team) {
      body.innerHTML = '<p>Đội không tồn tại.</p>';
      return;
    }

    // Eligible members: active members excluding the current captain
    const eligibleMembers = state.teamMembers.filter(
      m => m.teamId === team.id && m.userId !== currentUser?.id && m.status === 'ACTIVE'
    );

    let selectedMemberId = eligibleMembers.length > 0 ? eligibleMembers[0].userId : null;

    body.innerHTML = `
      <div style="padding: 4px 0 16px;">
        <!-- Stitch S10 Amber Alert -->
        <div class="notice notice-warning" style="margin-bottom: 16px;">
          <div style="display: flex; gap: 8px; align-items: flex-start;">
            <span style="color: var(--color-warning);">${Icons.warning}</span>
            <p style="margin: 0; font-size: 12.5px; line-height: 1.45;">
              Người được chọn sẽ trở thành đội trưởng ngay lập tức và không thể từ chối theo quy định USER-001. Bạn sẽ trở thành thành viên thường.
            </p>
          </div>
        </div>

        ${eligibleMembers.length === 0 ? `
          <div style="text-align: center; padding: 20px 0; color: var(--color-on-surface-variant); font-size: 13.5px;">
            Đội bóng chưa có thành viên nào khác để chuyển giao. Bạn cần mời thêm thành viên trước hoặc chọn giải tán đội.
          </div>
        ` : `
          <div style="font-size: 13px; font-weight: 700; color: var(--color-on-surface); margin-bottom: 10px;">
            Chọn thành viên đang hoạt động:
          </div>

          <div style="display: flex; flex-direction: column; gap: 8px;" id="transfer-radio-group">
            ${eligibleMembers.map(m => {
              const u = state.characters[m.userId];
              const uName = u ? (u.isDeleted ? 'Người dùng đã xóa' : u.displayName) : 'Thành viên';
              const isSelected = m.userId === selectedMemberId;

              return `
                <label class="transfer-member-option" data-user-id="${m.userId}" style="display: flex; align-items: center; justify-content: space-between; padding: 12px; border-radius: var(--radius-base); border: 2px solid ${isSelected ? 'var(--color-primary)' : 'var(--color-outline-variant)'}; background: ${isSelected ? 'var(--color-primary-container)' : 'var(--color-surface-container-low)'}; cursor: pointer; transition: all 0.15s ease;">
                  <div style="display: flex; align-items: center; gap: 10px;">
                    <div class="avatar avatar-sm">
                      ${u?.avatarUrl ? `<img src="${u.avatarUrl}">` : `<span>${uName.slice(0,1)}</span>`}
                    </div>
                    <div>
                      <strong style="font-size: 13.5px; color: var(--color-on-surface); display: block;">${uName}</strong>
                      <span style="font-size: 11.5px; color: var(--color-on-surface-variant);">Tham gia: ${new Date(m.joinedAt || Date.now()).toLocaleDateString('vi-VN')}</span>
                    </div>
                  </div>
                  <input type="radio" name="transfer-target" value="${m.userId}" ${isSelected ? 'checked' : ''} style="width: 18px; height: 18px; accent-color: var(--color-primary);" />
                </label>
              `;
            }).join('')}
          </div>
        `}
      </div>
    `;

    footer.innerHTML = `
      <div style="display: flex; gap: 10px; width: 100%;">
        <button class="btn btn-secondary" style="flex: 1;" onclick="window.AppRouter.closeSheet()">
          Hủy bỏ
        </button>
        <button class="btn btn-primary" style="flex: 1;" id="btn-confirm-transfer" ${eligibleMembers.length === 0 ? 'disabled' : ''}>
          Xác nhận chuyển quyền
        </button>
      </div>
    `;

    // Radio selection handler
    body.querySelectorAll('.transfer-member-option').forEach(opt => {
      opt.addEventListener('click', () => {
        selectedMemberId = opt.getAttribute('data-user-id');
        body.querySelectorAll('.transfer-member-option').forEach(o => {
          o.style.borderColor = 'var(--color-outline-variant)';
          o.style.background = 'var(--color-surface-container-low)';
        });
        opt.style.borderColor = 'var(--color-primary)';
        opt.style.background = 'var(--color-primary-container)';
      });
    });

    footer.querySelector('#btn-confirm-transfer').addEventListener('click', () => {
      if (!selectedMemberId) {
        router.showToast('Vui lòng chọn thành viên kế nhiệm', 'error');
        return;
      }

      try {
        store.transferCaptaincy(team.id, selectedMemberId);
        const newCap = state.characters[selectedMemberId];
        router.closeSheet();
        router.showToast(`Đã chuyển giao quyền đội trưởng cho ${newCap?.displayName || 'thành viên'}!`, 'success');
        router.navigate('team-detail', { teamId: team.id });
      } catch (err) {
        router.showToast(`Lỗi: ${err.message}`, 'error');
      }
    });
  });

  /* =========================================================================
     TEAM-11: Leave Team Sheet (Stitch S11)
     ========================================================================= */
  router.registerSheet('leave-team-sheet', (body, footer, params, titleElem) => {
    if (titleElem) titleElem.textContent = 'Rời đội bóng';
    const teamId = params?.teamId;
    const state = store.getState();
    const currentUser = store.getCurrentUser();
    const team = state.teams.find(t => t.id === teamId);

    if (!team) {
      body.innerHTML = '<p>Đội không tồn tại.</p>';
      return;
    }

    const isCaptain = team.captainId === currentUser?.id;

    if (isCaptain) {
      body.innerHTML = `
        <div style="padding: 10px 0 16px; text-align: center;">
          <div style="width: 56px; height: 56px; border-radius: 50%; background: var(--color-warning-container); color: var(--color-warning); display: flex; align-items: center; justify-content: center; margin: 0 auto 14px;">
            ${Icons.warning}
          </div>
          <h3 style="font-size: 18px; font-weight: 700; margin: 0 0 8px;">Đội trưởng không thể trực tiếp rời đội</h3>
          <p style="font-size: 13px; color: var(--color-on-surface-variant); line-height: 1.45; margin: 0 0 16px;">
            Để rời đội <strong>${team.name}</strong>, bạn phải chuyển giao quyền đội trưởng cho thành viên khác hoặc thực hiện giải tán đội.
          </p>
        </div>
      `;
      footer.innerHTML = `
        <div style="display: flex; gap: 8px; width: 100%;">
          <button class="btn btn-secondary" style="flex: 1;" onclick="window.AppRouter.closeSheet()">Đóng</button>
          <button class="btn btn-primary" style="flex: 1;" onclick="window.AppRouter.closeSheet(); window.AppRouter.openSheet('transfer-captain-sheet', { teamId: '${team.id}' })">Chuyển quyền</button>
        </div>
      `;
      return;
    }

    body.innerHTML = `
      <div style="padding: 10px 0 16px; text-align: center;">
        <div style="width: 56px; height: 56px; border-radius: 50%; background: var(--color-error-container); color: var(--color-error); display: flex; align-items: center; justify-content: center; margin: 0 auto 14px;">
          ${Icons.logout}
        </div>
        <h3 style="font-size: 18px; font-weight: 800; color: var(--color-on-surface); margin: 0 0 8px;">
          Xác nhận rời đội ${team.name}?
        </h3>
        <p style="font-size: 13.5px; color: var(--color-on-surface-variant); line-height: 1.45; margin: 0;">
          Sau khi rời đội, bạn sẽ không còn tham gia các trận đấu cùng đội và cần nhận được lời mời mới nếu muốn gia nhập lại.
        </p>
      </div>
    `;

    footer.innerHTML = `
      <div style="display: flex; gap: 10px; width: 100%;">
        <button class="btn btn-secondary" style="flex: 1;" onclick="window.AppRouter.closeSheet()">
          Hủy bỏ
        </button>
        <button class="btn btn-danger" style="flex: 1;" id="btn-do-leave-team">
          Xác nhận rời đội
        </button>
      </div>
    `;

    footer.querySelector('#btn-do-leave-team').addEventListener('click', () => {
      try {
        store.leaveTeam(team.id, currentUser.id);
        router.closeSheet();
        router.showToast(`Bạn đã rời đội ${team.name}.`, 'info');
        router.navigate('teams');
      } catch (err) {
        router.showToast(`Lỗi: ${err.message}`, 'error');
      }
    });
  });

  /* =========================================================================
     TEAM-12: Disband Team Sheet (Stitch S12, Permanent)
     ========================================================================= */
  router.registerSheet('disband-team-sheet', (body, footer, params, titleElem) => {
    if (titleElem) titleElem.textContent = 'Giải tán đội bóng';
    const teamId = params?.teamId;
    const state = store.getState();
    const team = state.teams.find(t => t.id === teamId);

    if (!team) {
      body.innerHTML = '<p>Đội không tồn tại.</p>';
      return;
    }

    body.innerHTML = `
      <div style="padding: 4px 0 16px;">
        <div style="text-align: center; margin-bottom: 16px;">
          <div style="width: 52px; height: 52px; border-radius: 50%; background: var(--color-error-container); color: var(--color-error); display: flex; align-items: center; justify-content: center; margin: 0 auto 12px;">
            ${Icons.warning}
          </div>
          <h3 style="font-size: 18px; font-weight: 800; color: var(--color-on-surface); margin: 0;">
            Giải tán đội ${team.name}?
          </h3>
        </div>

        <div class="notice notice-danger" style="margin-bottom: 16px; font-size: 12.5px; line-height: 1.45;">
          <strong>CẢNH BÁO:</strong> Giải tán là hành động vĩnh viễn và không thể khôi phục. Mọi thành viên sẽ rời đội, các lời mời đang chờ và các trận đấu chưa diễn ra sẽ bị hủy.
        </div>

        <div class="form-group" style="margin-bottom: 16px;">
          <label class="form-label" for="disband-reason">Lý do giải tán (bắt buộc)</label>
          <textarea
            id="disband-reason"
            class="form-textarea"
            rows="3"
            placeholder="VD: Đội không còn đủ thành viên sinh hoạt định kỳ..."
            maxlength="200"
          ></textarea>
          <div id="err-disband-reason" class="field-error-msg" style="display: none;"></div>
        </div>

        <label style="display: flex; align-items: center; gap: 8px; cursor: pointer; font-size: 12.5px; font-weight: 600; color: var(--color-on-surface);">
          <input type="checkbox" id="chk-disband-agree" style="width: 17px; height: 17px; accent-color: var(--color-error);" />
          <span>Tôi hiểu rõ hệ quả và xác nhận giải tán vĩnh viễn</span>
        </label>
      </div>
    `;

    footer.innerHTML = `
      <div style="display: flex; gap: 10px; width: 100%;">
        <button class="btn btn-secondary" style="flex: 1;" onclick="window.AppRouter.closeSheet()">
          Hủy bỏ
        </button>
        <button class="btn btn-danger" style="flex: 1;" id="btn-do-disband" disabled>
          Giải tán đội
        </button>
      </div>
    `;

    const chk = body.querySelector('#chk-disband-agree');
    const reasonInput = body.querySelector('#disband-reason');
    const errReason = body.querySelector('#err-disband-reason');
    const disbandBtn = footer.querySelector('#btn-do-disband');

    chk.addEventListener('change', () => {
      disbandBtn.disabled = !chk.checked;
    });

    disbandBtn.addEventListener('click', () => {
      errReason.style.display = 'none';
      const reason = reasonInput.value.trim();

      if (!reason) {
        errReason.textContent = 'Vui lòng cung cấp lý do giải tán đội';
        errReason.style.display = 'block';
        reasonInput.focus();
        return;
      }

      try {
        store.disbandTeam(team.id, reason);
        router.closeSheet();
        router.showToast(`Đội bóng "${team.name}" đã được giải tán.`, 'info');
        router.navigate('team-detail', { teamId: team.id });
      } catch (err) {
        router.showToast(`Lỗi: ${err.message}`, 'error');
      }
    });
  });

  /* =========================================================================
     TEAM-14: Select Acting Team Sheet
     ========================================================================= */
  router.registerSheet('select-acting-team-sheet', (body, footer, params, titleElem) => {
    if (titleElem) titleElem.textContent = 'Chọn đội đại diện thi đấu';
    const state = store.getState();
    const captainedTeams = store.getMyCaptainedTeams();

    body.innerHTML = `
      <div style="padding: 4px 0 16px;">
        <p style="font-size: 13px; color: var(--color-on-surface-variant); margin: 0 0 16px; line-height: 1.45;">
          Chọn đội bóng bạn muốn đại diện khi tạo kèo mới, gửi yêu cầu ghép trận hoặc tuyển thêm viện binh.
        </p>

        ${captainedTeams.length === 0 ? `
          <div style="text-align: center; padding: 20px 0;">
            <p style="font-size: 13.5px; color: var(--color-on-surface-variant); margin: 0 0 14px;">
              Bạn chưa là Đội trưởng của đội bóng nào đang hoạt động.
            </p>
            <button class="btn btn-primary" onclick="window.AppRouter.closeSheet(); window.AppRouter.navigate('team-create')">
              ${Icons.plus} Tạo đội bóng mới
            </button>
          </div>
        ` : `
          <div style="display: flex; flex-direction: column; gap: 8px;">
            ${captainedTeams.map(team => {
              const isSelected = state.actingTeamId === team.id;
              const memberCount = state.teamMembers.filter(m => m.teamId === team.id && m.status === 'ACTIVE').length;

              return `
                <div class="acting-team-option" data-team-id="${team.id}" style="display: flex; align-items: center; justify-content: space-between; padding: 12px; border-radius: var(--radius-base); border: 2px solid ${isSelected ? 'var(--color-primary)' : 'var(--color-outline-variant)'}; background: ${isSelected ? 'var(--color-primary-container)' : 'var(--color-surface-container-low)'}; cursor: pointer; transition: all 0.15s ease;">
                  <div style="display: flex; align-items: center; gap: 10px;">
                    <div style="width: 40px; height: 40px; border-radius: 10px; background: var(--color-primary); color: #fff; display: flex; align-items: center; justify-content: center; font-weight: 700; font-size: 14px;">
                      ${team.shortName || team.name.slice(0, 2).toUpperCase()}
                    </div>
                    <div>
                      <strong style="font-size: 14px; color: var(--color-on-surface);">${team.name}</strong>
                      <span style="font-size: 11.5px; color: var(--color-on-surface-variant); display: block;">${memberCount} thành viên • Uy tín ${team.reputationScore}</span>
                    </div>
                  </div>

                  ${isSelected ? `
                    <span style="color: var(--color-primary);">${Icons.check}</span>
                  ` : ''}
                </div>
              `;
            }).join('')}
          </div>
        `}
      </div>
    `;

    footer.innerHTML = `
      <button class="btn btn-secondary btn-block" onclick="window.AppRouter.closeSheet()">
        Đóng
      </button>
    `;

    body.querySelectorAll('.acting-team-option').forEach(opt => {
      opt.addEventListener('click', () => {
        const teamId = opt.getAttribute('data-team-id');
        store.setActingTeam(teamId);
        router.closeSheet();
        const selectedTeam = state.teams.find(t => t.id === teamId);
        router.showToast(`Đã chuyển sang đại diện đội "${selectedTeam.name}".`, 'success');
        router.navigate('teams');
      });
    });
  });
}
