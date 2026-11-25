/**
 * ArenaFlow Screens: TEAM-01..14 (S6, S7, S8a, S8b, S9, S10, S11, S12, S13, S14)
 * Classic non-module script compatible with file:// protocol.
 */
(function (global) {
  'use strict';

  const R = () => global.ArenaRouter;
  const S = () => global.ArenaStore;
  const UI = () => global.ArenaUI;

  const SKILL_TAGS = [
    'Chill', 'Nhiệt tình', 'Máu lửa',
    'Vui chơi', 'Rèn luyện', 'Thi đấu',
    'Mới bắt đầu', 'Đã có kinh nghiệm', 'Lão làng'
  ];

  const SKILL_MAP = {
    BEGINNER: 'Mới bắt đầu',
    INTERMEDIATE: 'Rèn luyện',
    ADVANCED: 'Thi đấu'
  };

  function mapSkills(skills) {
    if (!Array.isArray(skills) || skills.length === 0) return ['Nhiệt tình'];
    return skills.map((s) => SKILL_MAP[s] || s);
  }

  function formatDateShort(iso) {
    if (!iso) return '—';
    const d = new Date(iso);
    if (Number.isNaN(d.getTime())) return String(iso).slice(0, 10);
    const dd = String(d.getDate()).padStart(2, '0');
    const mm = String(d.getMonth() + 1).padStart(2, '0');
    const yyyy = d.getFullYear();
    return `${dd}/${mm}/${yyyy}`;
  }

  function formatDateTimeShort(iso) {
    if (!iso) return '—';
    const d = new Date(iso);
    if (Number.isNaN(d.getTime())) return String(iso);
    const hh = String(d.getHours()).padStart(2, '0');
    const min = String(d.getMinutes()).padStart(2, '0');
    return `${formatDateShort(iso)} ${hh}:${min}`;
  }

  function getTeamById(teamId) {
    const st = S().getState();
    const id = teamId || st.actingTeamId || (st.teams[0] && st.teams[0].id);
    return st.teams.find((t) => t.id === id) || st.teams[0] || null;
  }

  function getTeamMembers(teamId, includeLeft) {
    const st = S().getState();
    return st.teamMembers.filter((m) => m.teamId === teamId && (includeLeft || m.status === 'ACTIVE'));
  }

  function getCaptainHistory(team) {
    const st = S().getState();
    if (!st.captainHistory) {
      st.captainHistory = {
        'team-alpha': [
          {
            id: 'ch-1',
            type: 'CREATED',
            toUserId: 'user-cap-alpha',
            at: '2026-01-15T08:00:00Z',
            reason: null
          }
        ],
        'team-disbanded-1': [
          {
            id: 'ch-d1',
            type: 'TRANSFER',
            fromUserId: 'user-del-99',
            toUserId: 'user-cap-alpha',
            at: '2026-04-10T09:30:00Z',
            reason: 'Bận công tác dài ngày'
          },
          {
            id: 'ch-d2',
            type: 'CREATED',
            toUserId: 'user-del-99',
            at: '2026-01-01T08:00:00Z',
            reason: null
          }
        ]
      };
    }
    if (!st.captainHistory[team.id]) {
      st.captainHistory[team.id] = [
        {
          id: `ch-init-${team.id}`,
          type: 'CREATED',
          toUserId: team.captainId,
          at: team.createdAt || '2026-01-01T08:00:00Z',
          reason: null
        }
      ];
    }
    return st.captainHistory[team.id];
  }

  function teamStatusBadge(status) {
    if (status === 'ACTIVE') return UI().badge('• Hoạt động', 'success');
    if (status === 'INACTIVE') return UI().badge('• Bị khoá', 'warning');
    return UI().badge('Đã giải tán', 'neutral');
  }

  /* -------------------------------------------------------------------------
     Shared Team Filter State & Bottom Sheet (TEAM-01 / TEAM-02)
     ------------------------------------------------------------------------- */
  const teamListFilter = {
    status: 'ALL',
    wardId: 'ALL',
    level: 'ALL'
  };

  function countActiveTeamFilters() {
    let n = 0;
    if (teamListFilter.status !== 'ALL') n++;
    if (teamListFilter.wardId !== 'ALL') n++;
    if (teamListFilter.level !== 'ALL') n++;
    return n;
  }

  function matchesTeamFilters(t) {
    if (teamListFilter.status !== 'ALL' && t.status !== teamListFilter.status) return false;
    if (teamListFilter.wardId !== 'ALL' && t.wardId !== teamListFilter.wardId) return false;
    if (teamListFilter.level !== 'ALL') {
      const targetLabel = SKILL_MAP[teamListFilter.level] || teamListFilter.level;
      const teamSkills = Array.isArray(t.skillLevels) ? t.skillLevels : [];
      const mapped = mapSkills(teamSkills);
      if (!teamSkills.includes(teamListFilter.level) && !mapped.includes(targetLabel)) return false;
    }
    return true;
  }

  function openTeamFilterSheet(currentScope, onApply) {
    const wards = S().getState().wards || [];
    let draft = {
      scope: currentScope || 'MY',
      status: teamListFilter.status,
      wardId: teamListFilter.wardId,
      level: teamListFilter.level
    };

    const bodyHtml = `
      <div class="space-y-4">
        <div>
          <label class="block text-xs font-semibold text-[#2D342C] mb-1.5">Phạm vi danh sách</label>
          <div class="grid grid-cols-2 gap-2">
            <button type="button" id="tf-scope-my" data-tf-scope="MY"
                    class="py-2 px-3 rounded-[8px] text-xs font-semibold border ${draft.scope === 'MY' ? 'bg-[#3D6943] text-white border-[#3D6943]' : 'bg-white text-[#596158] border-[#DDE5D9]'}">
              Đội của tôi
            </button>
            <button type="button" id="tf-scope-all" data-tf-scope="ALL"
                    class="py-2 px-3 rounded-[8px] text-xs font-semibold border ${draft.scope === 'ALL' ? 'bg-[#3D6943] text-white border-[#3D6943]' : 'bg-white text-[#596158] border-[#DDE5D9]'}">
              Đội hệ thống
            </button>
          </div>
        </div>

        <div>
          <label class="block text-xs font-semibold text-[#2D342C] mb-1.5">Trạng thái đội</label>
          <div class="grid grid-cols-2 gap-2">
            ${[
              { k: 'ALL', label: 'Tất cả' },
              { k: 'ACTIVE', label: 'Đang hoạt động' },
              { k: 'INACTIVE', label: 'Bị khoá' },
              { k: 'DISBANDED', label: 'Đã giải tán' }
            ].map((s) => `
              <button type="button" data-tf-status="${s.k}"
                      class="py-2 px-3 rounded-[8px] text-xs font-semibold border ${draft.status === s.k ? 'bg-[#3D6943] text-white border-[#3D6943]' : 'bg-white text-[#596158] border-[#DDE5D9]'}">
                ${s.label}
              </button>
            `).join('')}
          </div>
        </div>

        <div>
          <label for="tf-ward" class="block text-xs font-semibold text-[#2D342C] mb-1.5">Khu vực</label>
          <select id="tf-ward" class="w-full h-10 px-3 rounded-[8px] border border-[#DDE5D9] text-xs bg-white text-[#111827] focus:outline-none focus:border-[#3D6943]">
            <option value="ALL" ${draft.wardId === 'ALL' ? 'selected' : ''}>Tất cả khu vực</option>
            ${wards.map((w) => `<option value="${w.id}" ${draft.wardId === w.id ? 'selected' : ''}>${UI().esc(w.name)}, ${UI().esc(w.district)}</option>`).join('')}
          </select>
        </div>

        <div>
          <label class="block text-xs font-semibold text-[#2D342C] mb-1.5">Trình độ</label>
          <div class="grid grid-cols-2 gap-2">
            ${[
              { k: 'ALL', label: 'Tất cả' },
              { k: 'BEGINNER', label: 'Mới bắt đầu' },
              { k: 'INTERMEDIATE', label: 'Rèn luyện' },
              { k: 'ADVANCED', label: 'Thi đấu' }
            ].map((lv) => `
              <button type="button" data-tf-level="${lv.k}"
                      class="py-2 px-3 rounded-[8px] text-xs font-semibold border ${draft.level === lv.k ? 'bg-[#3D6943] text-white border-[#3D6943]' : 'bg-white text-[#596158] border-[#DDE5D9]'}">
                ${lv.label}
              </button>
            `).join('')}
          </div>
        </div>
      </div>
    `;

    const footerHtml = `
      <div class="grid grid-cols-2 gap-2.5">
        <button type="button" id="tf-reset"
                class="py-2.5 rounded-[8px] border border-[#DDE5D9] bg-white text-xs font-semibold text-[#2D342C]">
          Đặt lại
        </button>
        <button type="button" id="tf-apply"
                class="py-2.5 rounded-[8px] bg-[#3D6943] hover:bg-[#315C38] text-xs font-semibold text-white">
          Áp dụng
        </button>
      </div>
    `;

    UI().openSheet({ title: 'Bộ lọc đội bóng', bodyHtml, footerHtml });

    const host = document.getElementById('sheet-host');
    if (!host) return;

    const syncButtons = (attr, val) => {
      host.querySelectorAll(`[${attr}]`).forEach((btn) => {
        const active = btn.getAttribute(attr) === val;
        btn.className = `py-2 px-3 rounded-[8px] text-xs font-semibold border ${active ? 'bg-[#3D6943] text-white border-[#3D6943]' : 'bg-white text-[#596158] border-[#DDE5D9]'}`;
      });
    };

    host.querySelectorAll('[data-tf-scope]').forEach((b) => {
      b.addEventListener('click', () => {
        draft.scope = b.getAttribute('data-tf-scope');
        syncButtons('data-tf-scope', draft.scope);
      });
    });
    host.querySelectorAll('[data-tf-status]').forEach((b) => {
      b.addEventListener('click', () => {
        draft.status = b.getAttribute('data-tf-status');
        syncButtons('data-tf-status', draft.status);
      });
    });
    host.querySelectorAll('[data-tf-level]').forEach((b) => {
      b.addEventListener('click', () => {
        draft.level = b.getAttribute('data-tf-level');
        syncButtons('data-tf-level', draft.level);
      });
    });
    const wardEl = host.querySelector('#tf-ward');
    if (wardEl) {
      wardEl.addEventListener('change', (e) => { draft.wardId = e.target.value; });
    }
    const resetBtn = host.querySelector('#tf-reset');
    if (resetBtn) {
      resetBtn.addEventListener('click', () => {
        teamListFilter.status = 'ALL';
        teamListFilter.wardId = 'ALL';
        teamListFilter.level = 'ALL';
        UI().closeSheet();
        if (typeof onApply === 'function') onApply(currentScope);
      });
    }
    const applyBtn = host.querySelector('#tf-apply');
    if (applyBtn) {
      applyBtn.addEventListener('click', () => {
        teamListFilter.status = draft.status;
        teamListFilter.wardId = draft.wardId;
        teamListFilter.level = draft.level;
        UI().closeSheet();
        if (draft.scope !== currentScope) {
          R().navigate(draft.scope === 'ALL' ? 'TEAM-02' : 'TEAM-01');
        } else if (typeof onApply === 'function') {
          onApply(draft.scope);
        }
      });
    }
  }

  function renderFilterIconButton(activeCount) {
    return `
      <button type="button" id="btn-team-filter" aria-label="Bộ lọc"
              class="relative w-9 h-9 rounded-[8px] bg-white border border-[#DDE5D9] hover:bg-[#EAF0E6] text-[#2D342C] flex items-center justify-center shrink-0 transition-colors">
        <span class="material-symbols-outlined text-[20px]">tune</span>
        ${activeCount > 0 ? `
          <span class="absolute -top-1 -right-1 min-w-[16px] h-4 px-1 rounded-full bg-[#3D6943] text-white text-[10px] font-bold flex items-center justify-center leading-none">
            ${activeCount}
          </span>
        ` : ''}
      </button>
    `;
  }

  /* -------------------------------------------------------------------------
     TEAM-01 / S6: Đội của tôi (My Teams)
     ------------------------------------------------------------------------- */
  function renderTeam01(container) {
    const user = S().getCurrentUser();
    const st = S().getState();

    const draw = () => {
      const pendingInvs = user
        ? st.invitations.filter((inv) => inv.invitedUserId === user.id && inv.status === 'PENDING')
        : [];

      const myMemberTeamIds = user
        ? st.teamMembers.filter((m) => m.userId === user.id && m.status === 'ACTIVE').map((m) => m.teamId)
        : [];
      const myTeams = user
        ? st.teams.filter((t) => myMemberTeamIds.includes(t.id) || t.captainId === user.id)
        : [];

      const filtered = myTeams.filter(matchesTeamFilters);
      const activeFilters = countActiveTeamFilters();

      container.innerHTML = `
        <div class="w-full flex-1 flex flex-col bg-[#F8FAF3] px-4 py-4 space-y-3.5">
          <div class="flex items-center justify-between gap-2">
            <h1 class="text-xl font-bold text-[#111827] tracking-tight">Đội của tôi (${myTeams.length})</h1>
            <div class="flex items-center gap-2">
              ${renderFilterIconButton(activeFilters)}
              ${user ? `
                <button type="button" onclick="window.ArenaRouter.navigate('S7')"
                        class="inline-flex items-center gap-1 bg-[#3D6943] hover:bg-[#315C38] text-white text-xs font-semibold px-3 py-2 rounded-[8px] shadow-sm active:scale-95 transition-all">
                  <span class="material-symbols-outlined text-[16px]">add</span>
                  <span>Tạo đội</span>
                </button>
              ` : ''}
            </div>
          </div>

          ${!user ? UI().emptyState({
            icon: 'groups',
            title: 'Đăng nhập để quản lý đội',
            description: 'Bạn có thể xem danh sách đội hệ thống hoặc đăng nhập để tạo và tham gia đội bóng.',
            actionLabel: 'Đăng nhập ngay',
            onAction: "window.ArenaRouter.navigate('S1')"
          }) : `
            ${pendingInvs.map((inv) => {
              const invTeam = st.teams.find((t) => t.id === inv.teamId);
              if (!invTeam) return '';
              return `
                <div class="bg-white border border-[#BDEFBF] rounded-[8px] p-3.5 shadow-sm space-y-3">
                  <div class="flex items-start gap-3">
                    ${UI().avatar(invTeam, { size: 42 })}
                    <div class="flex-1 min-w-0">
                      <span class="inline-block text-[11px] font-semibold text-[#1C4825] bg-[#EAF0E6] px-2 py-0.5 rounded mb-1">
                        Lời mời đang chờ
                      </span>
                      <h2 class="text-sm font-bold text-[#111827] leading-snug cursor-pointer hover:underline"
                          onclick="window.ArenaRouter.navigate('TEAM-08', { invitationId: '${inv.id}' })">
                        ${UI().esc(invTeam.name)} mời bạn vào đội
                      </h2>
                      <p class="text-xs text-[#596158] mt-0.5">Mời ngày ${formatDateShort(inv.createdAt)}</p>
                    </div>
                  </div>
                  <div class="grid grid-cols-2 gap-2">
                    <button type="button" data-accept-inv="${inv.id}"
                            class="py-2 px-3 bg-[#3D6943] hover:bg-[#315C38] text-white text-xs font-semibold rounded-[8px]">
                      Chấp nhận
                    </button>
                    <button type="button" data-decline-inv="${inv.id}"
                            class="py-2 px-3 bg-white border border-[#DDE5D9] hover:bg-[#F8FAF3] text-[#2D342C] text-xs font-semibold rounded-[8px]">
                      Từ chối
                    </button>
                  </div>
                </div>
              `;
            }).join('')}

            ${filtered.length === 0 ? UI().emptyState({
              icon: 'group_add',
              title: myTeams.length === 0 ? 'Bạn chưa tham gia đội nào' : 'Không có đội phù hợp bộ lọc',
              description: myTeams.length === 0
                ? 'Hãy tạo đội bóng mới hoặc khám phá các đội trong hệ thống.'
                : 'Thử đặt lại bộ lọc để xem toàn bộ đội của bạn.',
              actionLabel: myTeams.length === 0 ? 'Tạo đội mới' : 'Đặt lại bộ lọc',
              onAction: myTeams.length === 0 ? "window.ArenaRouter.navigate('S7')" : "window.ArenaRouter.navigate('TEAM-01')"
            }) : `
              <div class="space-y-3">
                ${filtered.map((t) => {
                  const activeCount = getTeamMembers(t.id, false).length;
                  const capName = S().getUserDisplayName(t.captainId, user.role === 'ADMIN');
                  const isDisbanded = t.status === 'DISBANDED';
                  const targetRoute = t.status === 'ACTIVE' ? 'S8a' : 'S8b';
                  return `
                    <div onclick="window.ArenaRouter.navigate('${targetRoute}', { teamId: '${t.id}' })"
                         class="bg-white border border-[#DDE5D9] rounded-[8px] p-3.5 shadow-sm space-y-2.5 cursor-pointer hover:border-[#3D6943] transition-colors ${isDisbanded ? 'opacity-70' : ''}">
                      <div class="flex items-center justify-between gap-2">
                        <div class="flex items-center gap-3 min-w-0">
                          ${UI().avatar(t, { size: 40 })}
                          <div class="min-w-0">
                            <h3 class="font-bold text-sm ${isDisbanded ? 'text-[#596158] line-through' : 'text-[#111827]'} truncate">
                              ${UI().esc(t.name)}
                            </h3>
                            <p class="text-[11px] text-[#596158] truncate">${UI().esc(S().getWardName(t.wardId))}</p>
                          </div>
                        </div>
                        ${teamStatusBadge(t.status)}
                      </div>

                      <div class="flex flex-wrap gap-1.5">
                        ${mapSkills(t.skillLevels).map((sk) => `
                          <span class="text-[11px] bg-[#F1F5EC] text-[#465846] px-2 py-0.5 rounded">${UI().esc(sk)}</span>
                        `).join('')}
                        ${t.captainId === user.id ? `<span class="text-[11px] bg-[#FEF3C7] text-[#B45309] font-semibold px-2 py-0.5 rounded">Bạn là Đội trưởng</span>` : ''}
                      </div>

                      <div class="flex items-center justify-between text-xs text-[#596158] pt-2 border-t border-[#F1F5EC]">
                        <span>${isDisbanded ? 'Người giải tán' : 'Đội trưởng'}: <strong class="text-[#111827]">${UI().esc(capName)}</strong></span>
                        <span>${isDisbanded ? 0 : activeCount} thành viên</span>
                      </div>
                    </div>
                  `;
                }).join('')}
              </div>
            `}
          `}
        </div>
      `;

      const filterBtn = container.querySelector('#btn-team-filter');
      if (filterBtn) {
        filterBtn.addEventListener('click', () => {
          openTeamFilterSheet('MY', draw);
        });
      }

      container.querySelectorAll('[data-accept-inv]').forEach((btn) => {
        btn.addEventListener('click', () => {
          const invId = btn.getAttribute('data-accept-inv');
          const inv = st.invitations.find((x) => x.id === invId);
          if (!inv) return;
          const team = st.teams.find((t) => t.id === inv.teamId);
          if (!team || team.status === 'DISBANDED') {
            UI().toast('Đội này đã giải tán, không thể tham gia.', 'error');
            return;
          }
          S().setState((s) => {
            const targetInv = s.invitations.find((x) => x.id === invId);
            if (targetInv) targetInv.status = 'ACCEPTED';
            const exists = s.teamMembers.find((m) => m.teamId === inv.teamId && m.userId === user.id && m.status === 'ACTIVE');
            if (!exists) {
              s.teamMembers.push({
                id: 'tm-' + Date.now(),
                teamId: inv.teamId,
                userId: user.id,
                role: 'MEMBER',
                joinedAt: s.meta.mockClock,
                status: 'ACTIVE'
              });
            }
          });
          UI().toast(`Đã gia nhập ${team.name}!`, 'success');
          R().navigate('S8a', { teamId: team.id });
        });
      });

      container.querySelectorAll('[data-decline-inv]').forEach((btn) => {
        btn.addEventListener('click', () => {
          const invId = btn.getAttribute('data-decline-inv');
          S().setState((s) => {
            const targetInv = s.invitations.find((x) => x.id === invId);
            if (targetInv) targetInv.status = 'DECLINED';
          });
          UI().toast('Đã từ chối lời mời vào đội.', 'info');
          draw();
        });
      });
    };

    draw();
  }

  /* -------------------------------------------------------------------------
     TEAM-02: Danh sách đội hệ thống (Explore System Teams)
     ------------------------------------------------------------------------- */
  function renderTeam02(container) {
    const st = S().getState();
    const user = S().getCurrentUser();
    let query = '';

    const draw = () => {
      const myMemberIds = user
        ? st.teamMembers.filter((m) => m.userId === user.id && m.status === 'ACTIVE').map((m) => m.teamId)
        : [];
      const list = st.teams.filter((t) => {
        const matchText = !query || t.name.toLowerCase().includes(query.toLowerCase());
        return matchText && matchesTeamFilters(t);
      });
      const activeFilters = countActiveTeamFilters();

      container.innerHTML = `
        <div class="w-full flex-1 flex flex-col bg-[#F8FAF3] px-4 py-4 space-y-3.5">
          <div class="flex items-center gap-2">
            <div class="relative flex-1">
              <span class="material-symbols-outlined text-[18px] text-[#596158] absolute left-3 top-1/2 -translate-y-1/2">search</span>
              <input id="team02-search" type="text" value="${UI().esc(query)}" placeholder="Tìm theo tên đội bóng..."
                     class="w-full pl-9 pr-3 h-9 text-sm bg-white border border-[#DDE5D9] rounded-[8px] focus:outline-none focus:border-[#3D6943]" />
            </div>
            ${renderFilterIconButton(activeFilters)}
          </div>

          <div class="space-y-3">
            ${list.length === 0 ? UI().emptyState({
              icon: 'search_off',
              title: 'Không tìm thấy đội phù hợp',
              description: 'Thử từ khóa khác hoặc đặt lại bộ lọc.'
            }) : list.map((t) => {
              const isMine = myMemberIds.includes(t.id) || (user && t.captainId === user.id);
              const route = t.status === 'ACTIVE' ? 'S8a' : 'S8b';
              return `
                <div onclick="window.ArenaRouter.navigate('${route}', { teamId: '${t.id}' })"
                     class="bg-white border border-[#DDE5D9] rounded-[8px] p-3.5 shadow-sm space-y-2 cursor-pointer hover:border-[#3D6943]">
                  <div class="flex items-center justify-between gap-2">
                    <div class="flex items-center gap-3 min-w-0">
                      ${UI().avatar(t, { size: 40 })}
                      <div class="min-w-0">
                        <h3 class="font-bold text-sm text-[#111827] truncate">${UI().esc(t.name)}</h3>
                        <p class="text-[11px] text-[#596158] truncate">${UI().esc(S().getWardName(t.wardId))}</p>
                      </div>
                    </div>
                    <div class="flex items-center gap-1.5 shrink-0">
                      ${isMine ? UI().badge('Đội của tôi', 'primary') : ''}
                      ${teamStatusBadge(t.status)}
                    </div>
                  </div>
                  <div class="flex items-center justify-between text-xs text-[#596158] pt-1.5 border-t border-[#F1F5EC]">
                    <span>Uy tín: <strong class="text-[#111827]">${t.reputationScore}đ</strong> (${t.ratingAvg}★)</span>
                    <span>${t.matchCount} trận</span>
                  </div>
                </div>
              `;
            }).join('')}
          </div>
        </div>
      `;

      container.querySelector('#team02-search').addEventListener('input', (e) => {
        query = e.target.value;
        draw();
      });
      const filterBtn = container.querySelector('#btn-team-filter');
      if (filterBtn) {
        filterBtn.addEventListener('click', () => {
          openTeamFilterSheet('ALL', draw);
        });
      }
    };

    draw();
  }

  /* -------------------------------------------------------------------------
     S7 / TEAM-03: Tạo đội mới
     ------------------------------------------------------------------------- */
  function renderS7(container) {
    const user = S().getCurrentUser();
    if (!user) {
      R().navigate('S1');
      return;
    }
    const wards = S().getState().wards || [];
    let draft = {
      name: '',
      description: 'Đá giao lưu tối thứ 4 hằng tuần.',
      wardId: wards[0] ? wards[0].id : 'ward-ak',
      skills: ['Nhiệt tình', 'Rèn luyện', 'Đã có kinh nghiệm']
    };
    let nameError = '';
    let submitting = false;

    const draw = () => {
      container.innerHTML = `
        <div class="w-full flex-1 flex flex-col bg-white">
          <div class="flex-1 overflow-y-auto px-5 pt-5 pb-6 space-y-5">
            <div class="flex flex-col items-center">
              <label class="w-full text-xs font-semibold text-[#2D342C] mb-2 text-left">Logo đội</label>
              <div class="w-20 h-20 rounded-full bg-[#F8FAF3] border-2 border-dashed border-[#DDE5D9] flex items-center justify-center text-[#3D6943] mb-2.5">
                <span class="material-symbols-outlined text-[36px]">shield</span>
              </div>
              <button type="button" onclick="window.ArenaUI.toast('Logo đội sẽ được tạo theo tên viết tắt', 'info')"
                      class="px-3.5 py-1.5 border border-[#DDE5D9] rounded-[8px] text-xs font-medium text-[#2D342C] hover:bg-[#F8FAF3]">
                Chọn ảnh
              </button>
            </div>

            <div>
              <label for="s7-name" class="block text-xs font-semibold text-[#2D342C] mb-1.5">
                Tên đội <span class="text-[#DC2626]">*</span>
              </label>
              <input id="s7-name" type="text" maxlength="100" value="${UI().esc(draft.name)}" placeholder="Ví dụ: FC Phượng Hoàng"
                     class="w-full h-11 px-3.5 rounded-[8px] border ${nameError ? 'border-[#A73B21]' : 'border-[#DDE5D9] focus:border-[#3D6943]'} text-sm text-[#111827] focus:outline-none" />
              <div class="flex items-center justify-between mt-1 text-xs">
                <span class="text-[#A73B21] font-medium">${UI().esc(nameError)}</span>
                <span id="s7-name-cnt" class="text-[#596158]">${draft.name.length}/100</span>
              </div>
            </div>

            <div>
              <label for="s7-ward" class="block text-xs font-semibold text-[#2D342C] mb-1.5">Khu vực hoạt động</label>
              <select id="s7-ward" class="w-full h-11 px-3.5 rounded-[8px] border border-[#DDE5D9] text-sm text-[#111827] bg-white focus:outline-none focus:border-[#3D6943]">
                ${wards.map((w) => `<option value="${w.id}" ${draft.wardId === w.id ? 'selected' : ''}>${UI().esc(w.name)}, ${UI().esc(w.district)}</option>`).join('')}
              </select>
            </div>

            <div>
              <label for="s7-desc" class="block text-xs font-semibold text-[#2D342C] mb-1.5">Mô tả</label>
              <textarea id="s7-desc" rows="3" maxlength="1000"
                        class="w-full p-3 rounded-[8px] border border-[#DDE5D9] focus:outline-none focus:border-[#3D6943] text-sm text-[#111827] resize-none leading-relaxed">${UI().esc(draft.description)}</textarea>
              <div class="text-right mt-1 text-xs text-[#596158]">
                <span id="s7-desc-cnt">${draft.description.length}/1000</span>
              </div>
            </div>

            <div>
              <div class="flex items-center justify-between mb-2">
                <div>
                  <span class="text-xs font-semibold text-[#2D342C]">Trình độ đội</span>
                  <span class="text-[11px] text-[#596158] ml-1">(Tối đa 3 nhãn)</span>
                </div>
                <span class="text-xs font-semibold text-[#3D6943] bg-[#EAF0E6] px-2 py-0.5 rounded-[8px]">
                  Đã chọn ${draft.skills.length}/3
                </span>
              </div>
              <div class="flex flex-wrap gap-2">
                ${SKILL_TAGS.map((tag) => {
                  const selected = draft.skills.includes(tag);
                  return `
                    <button type="button" data-skill="${UI().esc(tag)}"
                            class="inline-flex items-center gap-1 px-3 py-1.5 rounded-[8px] text-xs font-medium border transition-colors ${selected ? 'bg-[#3D6943] text-white border-[#3D6943]' : 'bg-[#F8FAF3] text-[#596158] border-[#DDE5D9]'}">
                      ${selected ? '<span class="material-symbols-outlined text-[14px]">check</span>' : ''}
                      <span>${UI().esc(tag)}</span>
                    </button>
                  `;
                }).join('')}
              </div>
            </div>
          </div>

          <footer class="bg-white border-t border-[#E5E7EB] px-4 py-3 grid grid-cols-2 gap-3 shrink-0">
            <button type="button" onclick="window.ArenaRouter.navigate('TEAM-01')"
                    class="h-11 border border-[#DDE5D9] rounded-[8px] font-semibold text-sm text-[#2D342C] bg-white hover:bg-[#F8FAF3]">
              Huỷ
            </button>
            <button type="button" id="btn-s7-submit"
                    class="h-11 bg-[#3D6943] hover:bg-[#315C38] rounded-[8px] font-semibold text-sm text-white shadow-sm">
              Tạo đội
            </button>
          </footer>
        </div>
      `;

      container.querySelector('#s7-name').addEventListener('input', (e) => {
        draft.name = e.target.value;
        nameError = '';
        container.querySelector('#s7-name-cnt').textContent = `${draft.name.length}/100`;
      });
      container.querySelector('#s7-ward').addEventListener('change', (e) => {
        draft.wardId = e.target.value;
      });
      container.querySelector('#s7-desc').addEventListener('input', (e) => {
        draft.description = e.target.value;
        container.querySelector('#s7-desc-cnt').textContent = `${draft.description.length}/1000`;
      });
      container.querySelectorAll('[data-skill]').forEach((btn) => {
        btn.addEventListener('click', () => {
          const sk = btn.getAttribute('data-skill');
          if (draft.skills.includes(sk)) {
            draft.skills = draft.skills.filter((x) => x !== sk);
          } else {
            if (draft.skills.length >= 3) {
              UI().toast('Chỉ được chọn tối đa 3 nhãn trình độ', 'warning');
              return;
            }
            draft.skills.push(sk);
          }
          draw();
        });
      });

      container.querySelector('#btn-s7-submit').addEventListener('click', () => {
        if (submitting) return;
        const cleanName = draft.name.trim();
        if (!cleanName) {
          nameError = 'Tên đội không được để trống';
          draw();
          return;
        }
        submitting = true;
        const newTeamId = 'team-' + Date.now();
        const short = cleanName.split(/\s+/).map((p) => p[0]).join('').slice(0, 3).toUpperCase() || 'FC';
        S().setState((s) => {
          s.teams.unshift({
            id: newTeamId,
            name: cleanName,
            shortName: short,
            logoUrl: null,
            description: draft.description.trim(),
            captainId: user.id,
            wardId: draft.wardId,
            status: 'ACTIVE',
            skillLevels: draft.skills.slice(),
            reputationScore: 100,
            ratingAvg: 5.0,
            completionRate: null,
            matchCount: 0,
            createdAt: s.meta.mockClock
          });
          s.teamMembers.push({
            id: 'tm-' + Date.now(),
            teamId: newTeamId,
            userId: user.id,
            role: 'CAPTAIN',
            joinedAt: s.meta.mockClock,
            status: 'ACTIVE'
          });
          s.actingTeamId = newTeamId;
        });
        UI().toast(`Tạo đội bóng ${cleanName} thành công!`, 'success');
        R().navigate('S8a', { teamId: newTeamId });
      });
    };

    draw();
  }

  /* -------------------------------------------------------------------------
     S8a / TEAM-04: Chi tiết đội đang hoạt động
     ------------------------------------------------------------------------- */
  function renderS8a(container, params) {
    const team = getTeamById(params && params.teamId);
    if (!team) {
      container.innerHTML = `<div class="p-4">${UI().errorState({ title: 'Không tìm thấy đội', code: '404' })}</div>`;
      return;
    }
    if (team.status !== 'ACTIVE') {
      renderS8b(container, { teamId: team.id });
      return;
    }

    const user = S().getCurrentUser();
    const isAdmin = user && user.role === 'ADMIN';
    const isCaptain = user && team.captainId === user.id;
    const members = getTeamMembers(team.id, false);
    const isMember = user && members.some((m) => m.userId === user.id);
    const history = getCaptainHistory(team);
    let activeTab = (params && params.tab) || 'members';

    const draw = () => {
      container.innerHTML = `
        <div class="w-full flex-1 flex flex-col bg-[#F8FAF3] px-4 py-3.5 space-y-3.5">
          <nav class="flex items-center text-xs text-[#596158] font-medium">
            <button type="button" onclick="window.ArenaRouter.navigate('TEAM-01')" class="hover:text-[#3D6943]">Đội của tôi</button>
            <span class="mx-1.5">›</span>
            <span class="text-[#111827] font-semibold truncate">${UI().esc(team.name)}</span>
          </nav>

          <section class="bg-white rounded-[8px] border border-[#DDE5D9] p-5 shadow-sm flex flex-col items-center text-center">
            <div class="relative mb-3">
              ${UI().avatar(team, { size: 76 })}
              <div class="absolute -bottom-1 -right-1 w-6 h-6 rounded-full bg-white border border-[#DDE5D9] flex items-center justify-center shadow-xs">
                <span class="material-symbols-outlined text-[14px] text-[#3D6943]">sports_soccer</span>
              </div>
            </div>

            <div class="flex items-center justify-center gap-2 mb-1.5 flex-wrap">
              <h1 class="text-xl font-bold text-[#111827] tracking-tight">${UI().esc(team.name)}</h1>
              ${teamStatusBadge(team.status)}
            </div>

            <div class="flex flex-col items-center gap-1 text-xs text-[#596158] mb-2.5">
              <div class="flex items-center gap-1">
                <span class="material-symbols-outlined text-[15px]">calendar_today</span>
                <span>Tạo ngày ${formatDateShort(team.createdAt)} • ${UI().esc(S().getWardName(team.wardId))}</span>
              </div>
              <div class="flex items-center gap-1">
                <span class="material-symbols-outlined text-[15px]">military_tech</span>
                <span>Đội trưởng: <strong class="text-[#111827]">${UI().esc(S().getUserDisplayName(team.captainId, isAdmin))}</strong></span>
              </div>
            </div>

            <div class="flex flex-wrap items-center justify-center gap-1.5 mb-3">
              ${mapSkills(team.skillLevels).map((sk) => `
                <span class="px-2.5 py-0.5 rounded-[6px] bg-[#F8FAF3] border border-[#DDE5D9] text-[11px] font-medium text-[#2D342C]">${UI().esc(sk)}</span>
              `).join('')}
            </div>

            <p class="text-xs text-[#596158] leading-relaxed mb-3">${UI().esc(team.description || 'Chưa có mô tả đội.')}</p>

            <!-- Quick Reputation & Schedule bar -->
            <div class="w-full grid grid-cols-3 gap-2 py-2.5 mb-3 border-y border-[#F1F5EC] text-xs">
              <button type="button" onclick="window.ArenaRouter.navigate('REP-04', { teamId: '${team.id}' })" class="flex flex-col items-center">
                <span class="font-bold text-[#14532D]">${team.reputationScore}đ</span>
                <span class="text-[11px] text-[#596158]">Uy tín (${team.ratingAvg}★)</span>
              </button>
              <button type="button" onclick="window.ArenaRouter.navigate('MATCH-07', { teamId: '${team.id}' })" class="flex flex-col items-center border-x border-[#F1F5EC]">
                <span class="font-bold text-[#111827]">${team.matchCount}</span>
                <span class="text-[11px] text-[#596158]">Lịch đấu</span>
              </button>
              <button type="button" onclick="window.ArenaRouter.navigate('EXT-02', { teamId: '${team.id}' })" class="flex flex-col items-center">
                <span class="material-symbols-outlined text-[16px] text-[#3D6943]">event_note</span>
                <span class="text-[11px] text-[#596158]">Trận ngoài</span>
              </button>
            </div>

            ${isCaptain ? `
              <div class="w-full space-y-2">
                <button type="button" onclick="window.ArenaRouter.navigate('S9', { teamId: '${team.id}' })"
                        class="w-full h-10 rounded-[8px] bg-[#3D6943] hover:bg-[#315C38] text-white text-xs font-semibold flex items-center justify-center gap-1.5">
                  <span class="material-symbols-outlined text-[18px]">person_add</span>
                  <span>Mời thành viên</span>
                </button>
                <div class="flex items-center gap-2">
                  <button type="button" onclick="window.ArenaRouter.navigate('TEAM-05', { teamId: '${team.id}' })"
                          class="flex-1 h-9 rounded-[8px] border border-[#DDE5D9] bg-white hover:bg-[#F8FAF3] text-[#2D342C] text-xs font-semibold flex items-center justify-center gap-1.5">
                    <span class="material-symbols-outlined text-[16px]">edit</span>
                    <span>Sửa thông tin</span>
                  </button>
                  <button type="button" onclick="window.ArenaRouter.navigate('S13', { teamId: '${team.id}' })"
                          aria-label="Quản lý đội"
                          class="w-9 h-9 rounded-[8px] border border-[#DDE5D9] bg-white hover:bg-[#F8FAF3] text-[#2D342C] flex items-center justify-center">
                    <span class="material-symbols-outlined text-[20px]">more_vert</span>
                  </button>
                </div>
              </div>
            ` : isMember ? `
              <button type="button" onclick="window.ArenaRouter.navigate('S11', { teamId: '${team.id}' })"
                      class="w-full h-10 rounded-[8px] border border-[#A73B21] text-[#A73B21] hover:bg-[#FEF2F2] text-xs font-semibold flex items-center justify-center gap-1.5">
                <span class="material-symbols-outlined text-[18px]">logout</span>
                <span>Rời đội</span>
              </button>
            ` : `
              <div class="w-full text-xs text-[#596158] bg-[#F8FAF3] py-2 rounded-[8px] border border-[#DDE5D9]">
                Chế độ xem công khai (Bạn không thuộc đội này)
              </div>
            `}
          </section>

          <!-- Tabs: Thành viên | Lịch sử đội trưởng -->
          <div class="w-full bg-white rounded-[8px] border border-[#DDE5D9] p-1 grid grid-cols-2 shadow-sm">
            <button type="button" data-s8a-tab="members"
                    class="py-2 text-center text-xs font-semibold rounded-[6px] ${activeTab === 'members' ? 'bg-[#3D6943] text-white' : 'text-[#596158]'}">
              Thành viên (${members.length})
            </button>
            <button type="button" data-s8a-tab="history"
                    class="py-2 text-center text-xs font-semibold rounded-[6px] ${activeTab === 'history' ? 'bg-[#3D6943] text-white' : 'text-[#596158]'}">
              Lịch sử đội trưởng
            </button>
          </div>

          ${activeTab === 'members' ? `
            <section class="bg-white rounded-[8px] border border-[#DDE5D9] p-4 shadow-sm">
              <div class="flex items-center justify-between mb-3 pb-2 border-b border-[#F1F5EC]">
                <h2 class="text-xs font-bold uppercase tracking-wider text-[#596158]">Thành viên đang hoạt động</h2>
                <button type="button" onclick="window.ArenaRouter.navigate('S14', { teamId: '${team.id}' })"
                        class="text-xs font-semibold text-[#3D6943] hover:underline">Lịch sử thành viên</button>
              </div>
              <div class="divide-y divide-[#F1F5EC]">
                ${members.map((m) => {
                  const u = st.users[m.userId];
                  const isDeleted = u && u.isDeleted;
                  const display = S().getUserDisplayName(m.userId, isAdmin);
                  const isMe = user && m.userId === user.id;
                  return `
                    <div class="py-3 flex items-center justify-between gap-2">
                      <div class="flex items-center gap-3 min-w-0">
                        ${isDeleted ? `
                          <div class="w-10 h-10 rounded-full bg-gray-100 border border-gray-200 flex items-center justify-center text-gray-400 shrink-0">
                            <span class="material-symbols-outlined text-[20px]">person_off</span>
                          </div>
                        ` : UI().avatar(u || display, { size: 40 })}
                        <div class="min-w-0">
                          <div class="flex items-center gap-1.5 flex-wrap">
                            <span class="text-xs ${isDeleted ? 'italic text-gray-400' : 'font-bold text-[#111827]'} truncate">${UI().esc(display)}</span>
                            ${isMe ? `<span class="px-1.5 py-0.5 rounded text-[10px] font-semibold bg-[#EAF0E6] text-[#3D6943]">Bạn</span>` : ''}
                          </div>
                          <p class="text-[11px] text-[#596158]">Tham gia ${formatDateShort(m.joinedAt)}</p>
                        </div>
                      </div>
                      ${m.role === 'CAPTAIN' ? UI().badge('Đội trưởng', 'warning') : UI().badge('Thành viên', 'neutral')}
                    </div>
                  `;
                }).join('')}
              </div>
            </section>
          ` : renderCaptainHistoryCard(history, isAdmin)}
        </div>
      `;

      container.querySelectorAll('[data-s8a-tab]').forEach((btn) => {
        btn.addEventListener('click', () => {
          activeTab = btn.getAttribute('data-s8a-tab');
          draw();
        });
      });
    };

    const st = S().getState();
    draw();
  }

  function renderCaptainHistoryCard(history, isAdmin) {
    if (!history || history.length === 0) {
      return UI().emptyState({ icon: 'history', title: 'Chưa có lịch sử', description: 'Chưa ghi nhận thay đổi đội trưởng.' });
    }
    return `
      <section class="bg-white rounded-[8px] border border-[#DDE5D9] p-4 shadow-sm">
        <div class="flex items-center justify-between mb-4 pb-2 border-b border-[#F1F5EC]">
          <h2 class="text-sm font-bold text-[#111827]">Nhật ký ban cán sự</h2>
          ${UI().badge(`${history.length} bản ghi`, 'neutral')}
        </div>
        <div class="relative pl-6 space-y-5 before:content-[''] before:absolute before:left-[9px] before:top-2 before:bottom-3 before:w-[2px] before:bg-[#DDE5D9]">
          ${history.map((item) => {
            const toName = S().getUserDisplayName(item.toUserId, isAdmin);
            const fromName = item.fromUserId ? S().getUserDisplayName(item.fromUserId, isAdmin) : null;
            return `
              <div class="relative">
                <span class="absolute -left-[20px] top-1.5 w-3 h-3 rounded-full ${item.type === 'TRANSFER' ? 'bg-[#3D6943]' : 'bg-[#757D73]'} ring-4 ring-white"></span>
                <div class="text-[13px] font-bold text-[#111827] flex items-center flex-wrap gap-1">
                  ${item.type === 'TRANSFER' ? `
                    <span>${UI().esc(fromName)}</span>
                    <span class="text-[#596158] font-normal">→</span>
                    <span class="text-[#3D6943]">${UI().esc(toName)}</span>
                  ` : `
                    <span>Tạo đội — ${UI().esc(toName)}</span>
                    <span class="font-normal text-[#596158]">trở thành đội trưởng</span>
                  `}
                </div>
                <div class="text-[11px] text-[#596158] mt-0.5">${formatDateTimeShort(item.at)}</div>
                ${item.reason ? `
                  <div class="mt-1.5 text-xs text-[#2D342C] bg-[#F8FAF3] p-2 rounded-[6px] border border-[#DDE5D9]">
                    Lý do: ${UI().esc(item.reason)}
                  </div>
                ` : ''}
              </div>
            `;
          }).join('')}
        </div>
      </section>
    `;
  }

  /* -------------------------------------------------------------------------
     S8b / TEAM-13: Đội ngừng hoạt động (INACTIVE) / Đã giải tán (DISBANDED)
     ------------------------------------------------------------------------- */
  function renderS8b(container, params) {
    const st = S().getState();
    const fallbackDisbanded = st.teams.find((t) => t.status === 'DISBANDED' || t.status === 'INACTIVE');
    const team = (params && params.teamId) ? getTeamById(params.teamId) : (fallbackDisbanded || getTeamById());
    if (!team) return;

    const user = S().getCurrentUser();
    const isAdmin = user && user.role === 'ADMIN';
    const isInactive = team.status === 'INACTIVE';
    const members = getTeamMembers(team.id, false);
    const history = getCaptainHistory(team);
    let activeTab = 'history';

    const draw = () => {
      container.innerHTML = `
        <div class="w-full flex-1 flex flex-col bg-[#F8FAF3] px-4 py-3.5 space-y-3.5">
          <nav class="text-xs text-[#596158] font-medium">
            <button type="button" onclick="window.ArenaRouter.navigate('TEAM-01')" class="hover:text-[#3D6943]">Đội của tôi</button>
            <span class="mx-1">›</span>
            <span class="text-[#111827] font-semibold">${UI().esc(team.name)}</span>
          </nav>

          <section class="bg-white rounded-[8px] border border-[#DDE5D9] p-4 text-center shadow-sm ${isInactive ? '' : 'opacity-80'}">
            <div class="flex justify-center mb-2.5">${UI().avatar(team, { size: 64 })}</div>
            <div class="flex items-center justify-center gap-2 mb-1.5">
              <h1 class="text-lg font-bold ${isInactive ? 'text-[#111827]' : 'text-[#596158]'}">${UI().esc(team.name)}</h1>
              ${teamStatusBadge(team.status)}
            </div>
            <p class="text-xs text-[#596158]">
              Tạo ngày ${formatDateShort(team.createdAt)} · ${isInactive ? 'Đội trưởng' : 'Người giải tán'}:
              <strong>${UI().esc(S().getUserDisplayName(team.captainId, isAdmin))}</strong>
            </p>
            <div class="flex justify-center gap-1.5 mt-2">
              ${mapSkills(team.skillLevels).map((sk) => `<span class="px-2 py-0.5 rounded text-[11px] bg-[#F1F5EC] text-[#596158]">${UI().esc(sk)}</span>`).join('')}
            </div>
          </section>

          <aside class="${isInactive ? 'bg-[#FEF3C7] border-[#FDE68A] text-[#92400E]' : 'bg-[#EAF0E6] border-[#DDE5D9] text-[#2D342C]'} border rounded-[8px] p-3 text-xs flex items-start gap-2.5">
            <span class="material-symbols-outlined text-[18px] shrink-0">${isInactive ? 'pause_circle' : 'info'}</span>
            <span class="leading-relaxed">
              ${isInactive
                ? `Đội đang tạm ngừng hoạt động (INACTIVE). Lý do: ${UI().esc(team.inactiveReason || 'Bị tạm khoá')}`
                : `Đội đã giải tán ngày ${formatDateShort(team.disbandedAt)}. Lý do: ${UI().esc(team.disbandReason || 'Đội trưởng giải tán đội')}`}
            </span>
          </aside>

          <div class="grid grid-cols-2 bg-white rounded-[8px] p-1 border border-[#DDE5D9] text-xs font-semibold">
            <button type="button" data-s8b-tab="members"
                    class="py-2 rounded-[6px] ${activeTab === 'members' ? 'bg-[#3D6943] text-white' : 'text-[#596158]'}">
              Thành viên (${team.status === 'DISBANDED' ? 0 : members.length})
            </button>
            <button type="button" data-s8b-tab="history"
                    class="py-2 rounded-[6px] ${activeTab === 'history' ? 'bg-[#3D6943] text-white' : 'text-[#596158]'}">
              Lịch sử đội trưởng
            </button>
          </div>

          ${activeTab === 'history'
            ? renderCaptainHistoryCard(history, isAdmin)
            : (members.length === 0
                ? UI().emptyState({ icon: 'group_off', title: 'Không còn thành viên hoạt động', description: 'Mọi thành viên đã rời đội khi giải tán.', actionLabel: 'Xem lịch sử thành viên', onAction: `window.ArenaRouter.navigate('S14', { teamId: '${team.id}' })` })
                : `<div class="bg-white rounded-[8px] border border-[#DDE5D9] p-4 divide-y divide-[#F1F5EC]">
                    ${members.map((m) => `
                      <div class="py-2.5 flex items-center justify-between text-xs">
                        <span class="font-semibold text-[#111827]">${UI().esc(S().getUserDisplayName(m.userId, isAdmin))}</span>
                        ${m.role === 'CAPTAIN' ? UI().badge('Đội trưởng', 'warning') : UI().badge('Thành viên', 'neutral')}
                      </div>
                    `).join('')}
                  </div>`
              )
          }
        </div>
      `;

      container.querySelectorAll('[data-s8b-tab]').forEach((b) => {
        b.addEventListener('click', () => {
          activeTab = b.getAttribute('data-s8b-tab');
          draw();
        });
      });
    };

    draw();
  }

  /* -------------------------------------------------------------------------
     TEAM-05: Sửa thông tin đội
     ------------------------------------------------------------------------- */
  function renderTeam05(container, params) {
    const team = getTeamById(params && params.teamId);
    const user = S().getCurrentUser();
    if (!team || !user || team.captainId !== user.id) {
      container.innerHTML = `<div class="p-4">${UI().errorState({
        title: 'Không có quyền sửa đội (403)',
        message: 'Chỉ đội trưởng hiện tại của đội mới có thể chỉnh sửa thông tin.',
        code: '403'
      })}</div>`;
      return;
    }

    const wards = S().getState().wards || [];
    let draft = {
      name: team.name,
      description: team.description || '',
      wardId: team.wardId || '',
      skills: mapSkills(team.skillLevels).slice(0, 3)
    };
    let nameError = '';

    const draw = () => {
      container.innerHTML = `
        <div class="w-full flex-1 flex flex-col bg-white">
          <div class="flex-1 overflow-y-auto px-5 pt-5 pb-6 space-y-5">
            <div>
              <label for="t5-name" class="block text-xs font-semibold text-[#2D342C] mb-1.5">Tên đội <span class="text-[#DC2626]">*</span></label>
              <input id="t5-name" type="text" maxlength="100" value="${UI().esc(draft.name)}"
                     class="w-full h-11 px-3.5 rounded-[8px] border ${nameError ? 'border-[#A73B21]' : 'border-[#DDE5D9] focus:border-[#3D6943]'} text-sm focus:outline-none" />
              ${nameError ? `<p class="text-xs text-[#A73B21] mt-1">${UI().esc(nameError)}</p>` : ''}
            </div>

            <div>
              <label for="t5-ward" class="block text-xs font-semibold text-[#2D342C] mb-1.5">Khu vực</label>
              <select id="t5-ward" class="w-full h-11 px-3.5 rounded-[8px] border border-[#DDE5D9] text-sm bg-white">
                ${wards.map((w) => `<option value="${w.id}" ${draft.wardId === w.id ? 'selected' : ''}>${UI().esc(w.name)}, ${UI().esc(w.district)}</option>`).join('')}
              </select>
            </div>

            <div>
              <label for="t5-desc" class="block text-xs font-semibold text-[#2D342C] mb-1.5">Mô tả</label>
              <textarea id="t5-desc" rows="3" maxlength="1000"
                        class="w-full p-3 rounded-[8px] border border-[#DDE5D9] text-sm resize-none">${UI().esc(draft.description)}</textarea>
            </div>

            <div>
              <div class="flex items-center justify-between mb-2">
                <span class="text-xs font-semibold text-[#2D342C]">Trình độ đội (tối đa 3)</span>
                <span class="text-xs font-semibold text-[#3D6943]">${draft.skills.length}/3</span>
              </div>
              <div class="flex flex-wrap gap-2">
                ${SKILL_TAGS.map((tag) => {
                  const sel = draft.skills.includes(tag);
                  return `
                    <button type="button" data-t5-sk="${UI().esc(tag)}"
                            class="px-3 py-1.5 rounded-[8px] text-xs font-medium border ${sel ? 'bg-[#3D6943] text-white border-[#3D6943]' : 'bg-[#F8FAF3] text-[#596158] border-[#DDE5D9]'}">
                      ${UI().esc(tag)}
                    </button>
                  `;
                }).join('')}
              </div>
            </div>
          </div>

          <footer class="bg-white border-t border-[#E5E7EB] px-4 py-3 grid grid-cols-2 gap-3 shrink-0">
            <button type="button" onclick="window.ArenaRouter.navigate('S8a', { teamId: '${team.id}' })"
                    class="h-11 border border-[#DDE5D9] rounded-[8px] font-semibold text-sm text-[#2D342C]">Huỷ</button>
            <button type="button" id="btn-t5-save"
                    class="h-11 bg-[#3D6943] hover:bg-[#315C38] rounded-[8px] font-semibold text-sm text-white">Lưu thay đổi</button>
          </footer>
        </div>
      `;

      container.querySelector('#t5-name').addEventListener('input', (e) => { draft.name = e.target.value; nameError = ''; });
      container.querySelector('#t5-ward').addEventListener('change', (e) => { draft.wardId = e.target.value; });
      container.querySelector('#t5-desc').addEventListener('input', (e) => { draft.description = e.target.value; });
      container.querySelectorAll('[data-t5-sk]').forEach((b) => {
        b.addEventListener('click', () => {
          const sk = b.getAttribute('data-t5-sk');
          if (draft.skills.includes(sk)) draft.skills = draft.skills.filter((x) => x !== sk);
          else if (draft.skills.length < 3) draft.skills.push(sk);
          draw();
        });
      });
      container.querySelector('#btn-t5-save').addEventListener('click', () => {
        if (!draft.name.trim()) {
          nameError = 'Tên đội không được để trống';
          draw();
          return;
        }
        S().setState((s) => {
          const t = s.teams.find((x) => x.id === team.id);
          if (t) {
            t.name = draft.name.trim();
            t.description = draft.description.trim();
            t.wardId = draft.wardId;
            t.skillLevels = draft.skills.slice();
          }
        });
        UI().toast('Đã cập nhật thông tin đội!', 'success');
        R().navigate('S8a', { teamId: team.id });
      });
    };

    draw();
  }

  /* -------------------------------------------------------------------------
     TEAM-06: Thành viên đội (standalone route)
     ------------------------------------------------------------------------- */
  function renderTeam06(container, params) {
    renderS8a(container, Object.assign({}, params, { tab: 'members' }));
  }

  /* -------------------------------------------------------------------------
     S9 / TEAM-07: Mời thành viên
     ------------------------------------------------------------------------- */
  function renderS9(container, params) {
    const team = getTeamById(params && params.teamId);
    if (!team) return;
    const st = S().getState();
    const activeMembers = getTeamMembers(team.id, false).map((m) => m.userId);
    const candidates = Object.values(st.users).filter((u) => !u.isDeleted && !activeMembers.includes(u.id) && u.role !== 'ADMIN');

    let inputVal = 'hoangnam_99';
    let errorMsg = '';

    const draw = () => {
      container.innerHTML = `
        <div class="w-full flex-1 flex flex-col bg-[#F8FAF3] p-4 justify-center">
          <div class="bg-white rounded-[16px] border border-[#DDE5D9] p-5 shadow-md space-y-4">
            <div>
              <h2 class="text-lg font-bold text-[#111827]">Mời thành viên vào ${UI().esc(team.name)}</h2>
              <p class="text-xs text-[#596158] mt-1">Người được mời cần chấp nhận lời mời để trở thành thành viên của đội.</p>
            </div>

            <div>
              <label for="s9-input" class="block text-xs font-semibold text-[#2D342C] mb-1.5">ID hoặc SĐT người được mời</label>
              <input id="s9-input" type="text" value="${UI().esc(inputVal)}" placeholder="Nhập SĐT hoặc chọn gợi ý bên dưới"
                     class="w-full h-11 px-3.5 rounded-[8px] border ${errorMsg ? 'border-[#A73B21]' : 'border-[#DDE5D9] focus:border-[#3D6943]'} text-sm focus:outline-none" />
              ${errorMsg ? `
                <div class="flex items-center gap-1.5 mt-1.5 text-[#A73B21] text-xs font-medium">
                  <span class="material-symbols-outlined text-[15px]">error</span>
                  <span>${UI().esc(errorMsg)}</span>
                </div>
              ` : ''}
            </div>

            ${candidates.length > 0 ? `
              <div>
                <p class="text-[11px] font-semibold text-[#596158] uppercase tracking-wider mb-2">Gợi ý người chơi hợp lệ</p>
                <div class="flex flex-wrap gap-1.5">
                  ${candidates.map((c) => `
                    <button type="button" data-pick-user="${UI().esc(c.phone || c.id)}"
                            class="px-2.5 py-1 rounded-full bg-[#EAF0E6] hover:bg-[#D4E8D1] text-[#1C4825] text-xs font-medium">
                      ${UI().esc(c.displayName || c.phone)} (${UI().esc(c.phone)})
                    </button>
                  `).join('')}
                </div>
              </div>
            ` : ''}

            <div class="flex flex-col gap-2.5 pt-2">
              <button type="button" id="btn-s9-send"
                      class="w-full h-11 bg-[#3D6943] hover:bg-[#315C38] text-white font-semibold text-sm rounded-[8px] shadow-sm">
                Gửi lời mời
              </button>
              <button type="button" onclick="window.ArenaRouter.navigate('S8a', { teamId: '${team.id}' })"
                      class="w-full h-11 bg-white border border-[#DDE5D9] text-[#2D342C] font-semibold text-sm rounded-[8px]">
                Huỷ
              </button>
            </div>
          </div>
        </div>
      `;

      container.querySelector('#s9-input').addEventListener('input', (e) => {
        inputVal = e.target.value;
        errorMsg = '';
      });
      container.querySelectorAll('[data-pick-user]').forEach((b) => {
        b.addEventListener('click', () => {
          inputVal = b.getAttribute('data-pick-user');
          errorMsg = '';
          draw();
        });
      });
      container.querySelector('#btn-s9-send').addEventListener('click', () => {
        const q = inputVal.trim();
        const found = Object.values(st.users).find((u) => !u.isDeleted && (u.id === q || u.phone === q));
        if (!found) {
          errorMsg = 'Không tìm thấy người dùng';
          draw();
          return;
        }
        if (activeMembers.includes(found.id)) {
          errorMsg = 'Người dùng này đã là thành viên của đội';
          draw();
          return;
        }
        const alreadyInv = st.invitations.some((i) => i.teamId === team.id && i.invitedUserId === found.id && i.status === 'PENDING');
        if (alreadyInv) {
          errorMsg = 'Đã gửi lời mời tới người dùng này trước đó';
          draw();
          return;
        }
        S().setState((s) => {
          s.invitations.unshift({
            id: 'inv-' + Date.now(),
            teamId: team.id,
            invitedUserId: found.id,
            invitedBy: s.currentUserId,
            status: 'PENDING',
            message: `Mời bạn gia nhập ${team.name}`,
            createdAt: s.meta.mockClock
          });
        });
        UI().toast(`Đã gửi lời mời tới ${found.displayName || found.phone}!`, 'success');
        R().navigate('S8a', { teamId: team.id });
      });
    };

    draw();
  }

  /* -------------------------------------------------------------------------
     TEAM-08: Phản hồi lời mời vào đội
     ------------------------------------------------------------------------- */
  function renderTeam08(container, params) {
    const st = S().getState();
    const user = S().getCurrentUser();
    const inv = (params && params.invitationId)
      ? st.invitations.find((i) => i.id === params.invitationId)
      : st.invitations.find((i) => user && i.invitedUserId === user.id) || st.invitations[0];

    if (!inv) {
      container.innerHTML = `<div class="p-4">${UI().emptyState({ icon: 'mail', title: 'Không có lời mời', description: 'Lời mời không tồn tại hoặc đã bị xóa.' })}</div>`;
      return;
    }

    const team = st.teams.find((t) => t.id === inv.teamId);
    const inviter = S().getUserDisplayName(inv.invitedBy, false);
    const isDisbanded = !team || team.status === 'DISBANDED';

    container.innerHTML = `
      <div class="w-full flex-1 flex flex-col bg-[#F8FAF3] p-4 justify-center">
        <div class="bg-white rounded-[16px] border border-[#DDE5D9] p-5 shadow-md space-y-4 text-center">
          <div class="flex justify-center">${UI().avatar(team || 'AF', { size: 64 })}</div>
          <div>
            <h2 class="text-lg font-bold text-[#111827]">Lời mời gia nhập ${UI().esc(team ? team.name : 'Đội bóng')}</h2>
            <p class="text-xs text-[#596158] mt-1">Từ đội trưởng <strong>${UI().esc(inviter)}</strong> • ${formatDateShort(inv.createdAt)}</p>
          </div>
          ${inv.message ? `<p class="text-sm text-[#2D342C] bg-[#F8FAF3] p-3 rounded-[8px] border border-[#DDE5D9]">“${UI().esc(inv.message)}”</p>` : ''}

          ${isDisbanded ? `
            <div class="bg-[#FEF2F2] border border-[#FECACA] text-[#A73B21] rounded-[8px] p-3 text-xs font-medium">
              Đội bóng này đã giải tán. Lời mời không còn hiệu lực.
            </div>
          ` : inv.status !== 'PENDING' ? `
            <div class="bg-[#EAF0E6] text-[#1C4825] rounded-[8px] p-3 text-xs font-semibold">
              Lời mời đã được xử lý (${inv.status === 'ACCEPTED' ? 'Đã chấp nhận' : 'Đã từ chối'}).
            </div>
          ` : `
            <div class="grid grid-cols-2 gap-2.5 pt-2">
              <button type="button" id="btn-t8-decline" class="py-3 rounded-[8px] border border-[#DDE5D9] text-sm font-semibold text-[#2D342C]">Từ chối</button>
              <button type="button" id="btn-t8-accept" class="py-3 rounded-[8px] bg-[#3D6943] text-white text-sm font-semibold">Chấp nhận</button>
            </div>
          `}
        </div>
      </div>
    `;

    const acceptBtn = container.querySelector('#btn-t8-accept');
    if (acceptBtn) {
      acceptBtn.addEventListener('click', () => {
        S().setState((s) => {
          const target = s.invitations.find((x) => x.id === inv.id);
          if (target) target.status = 'ACCEPTED';
          if (user) {
            s.teamMembers.push({
              id: 'tm-' + Date.now(),
              teamId: inv.teamId,
              userId: user.id,
              role: 'MEMBER',
              joinedAt: s.meta.mockClock,
              status: 'ACTIVE'
            });
          }
        });
        UI().toast(`Chào mừng bạn gia nhập ${team.name}!`, 'success');
        R().navigate('S8a', { teamId: team.id });
      });
    }

    const declineBtn = container.querySelector('#btn-t8-decline');
    if (declineBtn) {
      declineBtn.addEventListener('click', () => {
        S().setState((s) => {
          const target = s.invitations.find((x) => x.id === inv.id);
          if (target) target.status = 'DECLINED';
        });
        UI().toast('Đã từ chối lời mời.', 'info');
        R().navigate('TEAM-01');
      });
    }
  }

  /* -------------------------------------------------------------------------
     S10 / TEAM-09: Chuyển quyền đội trưởng
     ------------------------------------------------------------------------- */
  function renderS10(container, params) {
    const team = getTeamById(params && params.teamId);
    if (!team) return;
    const members = getTeamMembers(team.id, false).filter((m) => m.userId !== team.captainId);
    let selectedUserId = members[0] ? members[0].userId : '';
    let reason = '';

    const draw = () => {
      container.innerHTML = `
        <div class="w-full flex-1 flex flex-col bg-[#F8FAF3] p-4 space-y-4">
          <div class="bg-white rounded-[12px] border border-[#DDE5D9] p-4 space-y-4 shadow-sm">
            <h2 class="text-lg font-bold text-[#111827]">Chuyển quyền đội trưởng (${UI().esc(team.name)})</h2>

            <div class="bg-[#FEF3C7] border border-[#FDE68A] text-[#92400E] rounded-[8px] p-3 flex gap-2.5 items-start text-xs leading-relaxed">
              <span class="material-symbols-outlined text-[18px] shrink-0">warning</span>
              <p>Người được chọn sẽ trở thành đội trưởng ngay lập tức và không thể từ chối. Bạn sẽ trở thành thành viên thường.</p>
            </div>

            ${members.length === 0 ? UI().emptyState({
              icon: 'person_off',
              title: 'Không có thành viên nào khác',
              description: 'Đội cần ít nhất 1 thành viên hoạt động khác để chuyển quyền đội trưởng.'
            }) : `
              <div class="space-y-2">
                <p class="text-xs font-semibold text-[#2D342C]">Chọn thành viên đang hoạt động</p>
                ${members.map((m) => {
                  const sel = selectedUserId === m.userId;
                  const name = S().getUserDisplayName(m.userId, false);
                  return `
                    <button type="button" data-pick-cap="${m.userId}"
                            class="w-full flex items-center justify-between p-3 rounded-[8px] border-2 ${sel ? 'border-[#3D6943] bg-[#EAF0E6]/50' : 'border-[#DDE5D9] bg-white'} text-left">
                      <div class="flex items-center gap-3">
                        ${UI().avatar(name, { size: 36 })}
                        <div>
                          <div class="text-sm font-semibold text-[#111827]">${UI().esc(name)}</div>
                          <div class="text-xs text-[#596158]">Tham gia ${formatDateShort(m.joinedAt)}</div>
                        </div>
                      </div>
                      <span class="material-symbols-outlined text-[20px] ${sel ? 'text-[#3D6943]' : 'text-[#DDE5D9]'}">
                        ${sel ? 'radio_button_checked' : 'radio_button_unchecked'}
                      </span>
                    </button>
                  `;
                }).join('')}
              </div>

              <div>
                <label for="s10-reason" class="block text-xs font-semibold text-[#2D342C] mb-1.5">Lý do (không bắt buộc)</label>
                <textarea id="s10-reason" rows="3" maxlength="500" placeholder="Ví dụ: Bận công tác dài ngày"
                          class="w-full p-3 text-sm border border-[#DDE5D9] rounded-[8px] resize-none focus:outline-none focus:border-[#3D6943]">${UI().esc(reason)}</textarea>
                <div class="text-right text-[11px] text-[#596158] mt-1"><span id="s10-cnt">${reason.length}/500</span></div>
              </div>

              <div class="flex flex-col gap-2 pt-1">
                <button type="button" id="btn-s10-confirm"
                        class="w-full py-3 bg-[#3D6943] hover:bg-[#315C38] text-white font-semibold text-sm rounded-[8px]">
                  Chuyển quyền
                </button>
                <button type="button" onclick="window.ArenaRouter.back()"
                        class="w-full py-3 border border-[#DDE5D9] bg-white text-[#2D342C] font-semibold text-sm rounded-[8px]">
                  Huỷ
                </button>
              </div>
            `}
          </div>
        </div>
      `;

      container.querySelectorAll('[data-pick-cap]').forEach((b) => {
        b.addEventListener('click', () => {
          selectedUserId = b.getAttribute('data-pick-cap');
          draw();
        });
      });
      const reasonEl = container.querySelector('#s10-reason');
      if (reasonEl) {
        reasonEl.addEventListener('input', (e) => {
          reason = e.target.value;
          container.querySelector('#s10-cnt').textContent = `${reason.length}/500`;
        });
      }
      const confirmBtn = container.querySelector('#btn-s10-confirm');
      if (confirmBtn) {
        confirmBtn.addEventListener('click', () => {
          if (!selectedUserId) return;
          const oldCap = team.captainId;
          S().setState((s) => {
            const t = s.teams.find((x) => x.id === team.id);
            if (t) t.captainId = selectedUserId;
            s.teamMembers.forEach((m) => {
              if (m.teamId === team.id && m.status === 'ACTIVE') {
                m.role = m.userId === selectedUserId ? 'CAPTAIN' : 'MEMBER';
              }
            });
            const hist = getCaptainHistory(team);
            hist.unshift({
              id: 'ch-' + Date.now(),
              type: 'TRANSFER',
              fromUserId: oldCap,
              toUserId: selectedUserId,
              at: s.meta.mockClock,
              reason: reason.trim() || null
            });
          });
          UI().toast('Đã chuyển quyền đội trưởng thành công!', 'success');
          R().navigate('S8a', { teamId: team.id });
        });
      }
    };

    draw();
  }

  /* -------------------------------------------------------------------------
     TEAM-10: Lịch sử đội trưởng (standalone)
     ------------------------------------------------------------------------- */
  function renderTeam10(container, params) {
    renderS8a(container, Object.assign({}, params, { tab: 'history' }));
  }

  /* -------------------------------------------------------------------------
     S11 / TEAM-11: Rời đội
     ------------------------------------------------------------------------- */
  function renderS11(container, params) {
    const team = getTeamById(params && params.teamId);
    const user = S().getCurrentUser();
    if (!team || !user) return;
    const isCaptain = team.captainId === user.id;

    container.innerHTML = `
      <div class="w-full flex-1 flex flex-col bg-[#F8FAF3] p-4 justify-center">
        <div class="bg-white rounded-[16px] border border-[#DDE5D9] p-6 shadow-md text-center space-y-4">
          <div class="w-14 h-14 rounded-full bg-[#FEF2F2] text-[#A73B21] flex items-center justify-center mx-auto">
            <span class="material-symbols-outlined text-[28px]">logout</span>
          </div>
          <h2 class="text-lg font-bold text-[#111827]">Rời đội ${UI().esc(team.name)}?</h2>

          ${isCaptain ? `
            <div class="bg-[#FEF2F2] border border-[#FECACA] text-[#A73B21] rounded-[8px] p-3 text-xs font-medium text-left">
              Đội trưởng không thể rời đội trực tiếp. Hãy chuyển quyền đội trưởng cho thành viên khác hoặc giải tán đội.
            </div>
            <div class="flex flex-col gap-2">
              <button type="button" onclick="window.ArenaRouter.navigate('S10', { teamId: '${team.id}' })"
                      class="w-full py-3 rounded-[8px] bg-[#3D6943] text-white text-sm font-semibold">Chuyển quyền đội trưởng</button>
              <button type="button" onclick="window.ArenaRouter.back()"
                      class="w-full py-3 rounded-[8px] border border-[#DDE5D9] text-[#2D342C] text-sm font-semibold">Quay lại</button>
            </div>
          ` : `
            <p class="text-sm text-[#596158] leading-relaxed">
              Bạn sẽ không còn là thành viên của đội. Muốn quay lại, bạn cần được đội trưởng mời lại.
            </p>
            <div class="flex flex-col gap-2.5 pt-2">
              <button type="button" id="btn-s11-leave"
                      class="w-full py-3 rounded-[8px] bg-[#A73B21] hover:bg-[#8F321B] text-white font-semibold text-sm">
                Rời đội
              </button>
              <button type="button" onclick="window.ArenaRouter.back()"
                      class="w-full py-3 rounded-[8px] border border-[#DDE5D9] text-[#2D342C] font-semibold text-sm">
                Huỷ
              </button>
            </div>
          `}
        </div>
      </div>
    `;

    const leaveBtn = container.querySelector('#btn-s11-leave');
    if (leaveBtn) {
      leaveBtn.addEventListener('click', () => {
        S().setState((s) => {
          s.teamMembers.forEach((m) => {
            if (m.teamId === team.id && m.userId === user.id && m.status === 'ACTIVE') {
              m.status = 'LEFT';
              m.leftAt = s.meta.mockClock;
            }
          });
        });
        UI().toast(`Đã rời khỏi ${team.name}.`, 'info');
        R().navigate('TEAM-01');
      });
    }
  }

  /* -------------------------------------------------------------------------
     S12 / TEAM-12: Giải tán đội
     ------------------------------------------------------------------------- */
  function renderS12(container, params) {
    const team = getTeamById(params && params.teamId);
    if (!team) return;
    let reason = '';
    let err = '';

    const draw = () => {
      container.innerHTML = `
        <div class="w-full flex-1 flex flex-col bg-[#F8FAF3] p-4 justify-center">
          <div class="bg-white rounded-[16px] border border-[#DDE5D9] p-5 shadow-md space-y-4">
            <div class="flex flex-col items-center text-center">
              <div class="w-12 h-12 rounded-full bg-[#FEF2F2] text-[#A73B21] flex items-center justify-center mb-2.5">
                <span class="material-symbols-outlined text-[26px]">warning</span>
              </div>
              <h2 class="text-lg font-bold text-[#111827]">Giải tán đội ${UI().esc(team.name)}?</h2>
            </div>

            <div class="rounded-[8px] bg-[#FEF2F2] border border-[#FECACA] p-3 flex items-start gap-2 text-xs text-[#A73B21]">
              <span class="material-symbols-outlined text-[16px] shrink-0 mt-0.5">error</span>
              <p>Giải tán là vĩnh viễn và không thể khôi phục. Mọi thành viên (kể cả bạn) sẽ rời đội, các lời mời đang chờ sẽ bị huỷ.</p>
            </div>

            <div>
              <label for="s12-reason" class="block text-xs font-semibold text-[#2D342C] mb-1.5">Lý do (không bắt buộc)</label>
              <textarea id="s12-reason" rows="3" placeholder="Nhập lý do giải tán (tối đa 100 ký tự)..."
                        class="w-full rounded-[8px] border ${err ? 'border-[#A73B21]' : 'border-[#DDE5D9] focus:border-[#3D6943]'} p-3 text-xs text-[#111827] resize-none focus:outline-none">${UI().esc(reason)}</textarea>
              <div class="flex items-center justify-between mt-1 text-[11px]">
                <span class="text-[#A73B21] font-medium">${UI().esc(err)}</span>
                <span id="s12-cnt" class="${reason.length > 100 ? 'text-[#A73B21] font-semibold' : 'text-[#596158]'}">${reason.length}/100</span>
              </div>
              <p class="text-[11px] text-[#596158] mt-1">Để trống sẽ ghi lý do mặc định “Đội trưởng giải tán đội”.</p>
            </div>

            <div class="flex flex-col gap-2">
              <button type="button" id="btn-s12-disband"
                      class="w-full py-3 rounded-[8px] bg-[#A73B21] hover:bg-[#8F321B] text-white text-sm font-semibold">
                Giải tán đội
              </button>
              <button type="button" onclick="window.ArenaRouter.back()"
                      class="w-full py-3 rounded-[8px] border border-[#DDE5D9] bg-white text-[#2D342C] text-sm font-semibold">
                Huỷ
              </button>
            </div>
          </div>
        </div>
      `;

      container.querySelector('#s12-reason').addEventListener('input', (e) => {
        reason = e.target.value;
        err = reason.length > 100 ? 'Lý do tối đa 100 ký tự' : '';
        const cntEl = container.querySelector('#s12-cnt');
        if (cntEl) cntEl.textContent = `${reason.length}/100`;
      });

      container.querySelector('#btn-s12-disband').addEventListener('click', () => {
        if (reason.length > 100) {
          err = 'Lý do tối đa 100 ký tự';
          draw();
          return;
        }
        const finalReason = reason.trim() || 'Đội trưởng giải tán đội';
        S().setState((s) => {
          const t = s.teams.find((x) => x.id === team.id);
          if (t) {
            t.status = 'DISBANDED';
            t.disbandReason = finalReason;
            t.disbandedAt = s.meta.mockClock;
          }
          s.teamMembers.forEach((m) => {
            if (m.teamId === team.id && m.status === 'ACTIVE') {
              m.status = 'LEFT';
              m.leftAt = s.meta.mockClock;
            }
          });
          s.invitations.forEach((inv) => {
            if (inv.teamId === team.id && inv.status === 'PENDING') {
              inv.status = 'DECLINED';
            }
          });
        });
        UI().toast(`Đội bóng ${team.name} đã được giải tán.`, 'success');
        R().navigate('S8b', { teamId: team.id });
      });
    };

    draw();
  }

  /* -------------------------------------------------------------------------
     S13: Đội trưởng quản lý đội
     ------------------------------------------------------------------------- */
  function renderS13(container, params) {
    const team = getTeamById(params && params.teamId);
    if (!team) return;

    container.innerHTML = `
      <div class="w-full flex-1 flex flex-col bg-[#F8FAF3] p-4 space-y-4">
        <div class="bg-white rounded-[12px] border border-[#DDE5D9] p-4 flex items-center gap-3">
          ${UI().avatar(team, { size: 46 })}
          <div class="min-w-0 flex-1">
            <h2 class="font-bold text-base text-[#111827] truncate">${UI().esc(team.name)}</h2>
            <p class="text-xs text-[#596158]">Bảng điều khiển của Đội trưởng</p>
          </div>
          ${teamStatusBadge(team.status)}
        </div>

        <div class="bg-white rounded-[12px] border border-[#DDE5D9] divide-y divide-[#F1F5EC] overflow-hidden">
          ${[
            { icon: 'edit', label: 'Sửa thông tin đội', route: 'TEAM-05' },
            { icon: 'person_add', label: 'Mời thành viên mới', route: 'S9' },
            { icon: 'swap_horiz', label: 'Chuyển quyền đội trưởng', route: 'S10' },
            { icon: 'history', label: 'Lịch sử thành viên đội', route: 'S14' },
            { icon: 'calendar_month', label: 'Lịch thi đấu đội', route: 'MATCH-07' },
            { icon: 'person_search', label: 'Tuyển người đá lẻ', route: 'MERC-07' }
          ].map((item) => `
            <button type="button" onclick="window.ArenaRouter.navigate('${item.route}', { teamId: '${team.id}' })"
                    class="w-full px-4 py-3.5 flex items-center justify-between hover:bg-[#F8FAF3] text-left">
              <div class="flex items-center gap-3">
                <span class="material-symbols-outlined text-[20px] text-[#3D6943]">${item.icon}</span>
                <span class="text-sm font-medium text-[#111827]">${item.label}</span>
              </div>
              <span class="material-symbols-outlined text-[18px] text-[#757D73]">chevron_right</span>
            </button>
          `).join('')}

          <button type="button" onclick="window.ArenaRouter.navigate('S12', { teamId: '${team.id}' })"
                  class="w-full px-4 py-3.5 flex items-center justify-between hover:bg-[#FEF2F2] text-left">
            <div class="flex items-center gap-3">
              <span class="material-symbols-outlined text-[20px] text-[#A73B21]">delete_forever</span>
              <span class="text-sm font-semibold text-[#A73B21]">Giải tán đội</span>
            </div>
            <span class="material-symbols-outlined text-[18px] text-[#A73B21]">chevron_right</span>
          </button>
        </div>
      </div>
    `;
  }

  /* -------------------------------------------------------------------------
     S14: Lịch sử thành viên đội
     ------------------------------------------------------------------------- */
  function renderS14(container, params) {
    const team = getTeamById(params && params.teamId);
    if (!team) return;
    const user = S().getCurrentUser();
    const isAdmin = user && user.role === 'ADMIN';
    const members = getTeamMembers(team.id, true);
    const st = S().getState();

    container.innerHTML = `
      <div class="w-full flex-1 flex flex-col bg-[#F8FAF3] p-4 space-y-4">
        <div>
          <div class="flex items-center gap-2.5 mb-1">
            ${UI().avatar(team, { size: 34 })}
            <h2 class="text-base font-bold text-[#111827]">${UI().esc(team.name)}</h2>
            ${teamStatusBadge(team.status)}
          </div>
          <p class="text-xs text-[#596158]">Gồm mọi người đã từng tham gia đội; không gồm lời mời.</p>
        </div>

        <div class="space-y-2.5">
          ${members.map((m) => {
            const u = st.users[m.userId];
            const isDeleted = u && u.isDeleted;
            const name = S().getUserDisplayName(m.userId, isAdmin);
            return `
              <article class="bg-white rounded-[8px] p-3.5 border border-[#DDE5D9] shadow-xs">
                <div class="flex items-center justify-between gap-2 mb-1.5">
                  <div class="flex items-center gap-2.5 min-w-0">
                    ${isDeleted && !isAdmin ? `
                      <div class="w-9 h-9 rounded-full bg-gray-100 border border-gray-200 flex items-center justify-center text-gray-400 shrink-0">
                        <span class="material-symbols-outlined text-[18px]">person_off</span>
                      </div>
                    ` : UI().avatar(u || name, { size: 36 })}
                    <span class="text-sm ${isDeleted && !isAdmin ? 'italic text-gray-400' : 'font-semibold text-[#111827]'} truncate">${UI().esc(name)}</span>
                  </div>
                  <div class="flex items-center gap-1.5 shrink-0">
                    ${m.role === 'CAPTAIN' ? UI().badge('Đội trưởng', 'warning') : UI().badge('Thành viên', 'neutral')}
                    ${m.status === 'LEFT' ? UI().badge('Đã rời', 'neutral') : UI().badge('Đang ở đội', 'success')}
                  </div>
                </div>
                <div class="text-xs text-[#596158] pl-[46px]">
                  Tham gia ${formatDateShort(m.joinedAt)}${m.leftAt ? ` • Rời ${formatDateShort(m.leftAt)}` : ''}
                </div>
              </article>
            `;
          }).join('')}
        </div>
      </div>
    `;
  }

  /* -------------------------------------------------------------------------
     TEAM-14: Chọn đội đang thao tác (Full Screen + Bottom Sheet helper)
     ------------------------------------------------------------------------- */
  function renderTeam14(container, params) {
    const capTeams = S().getMyCaptainedTeams();
    const currentActing = S().getActingTeam();
    const returnTo = params && params.returnTo;

    container.innerHTML = `
      <div class="w-full flex-1 flex flex-col bg-[#F8FAF3] p-4 space-y-4">
        <div class="bg-[#EAF0E6] border border-[#BDEFBF] rounded-[8px] p-3 text-xs text-[#1C4825] flex items-start gap-2">
          <span class="material-symbols-outlined text-[18px] shrink-0">info</span>
          <span>Chỉ các đội đang hoạt động mà bạn làm Đội trưởng mới hiển thị để thao tác đăng kèo, tuyển người và đánh giá.</span>
        </div>

        ${capTeams.length === 0 ? UI().emptyState({
          icon: 'sports_soccer',
          title: 'Bạn chưa làm đội trưởng đội nào',
          description: 'Tạo đội mới hoặc nhận chuyển quyền đội trưởng để sử dụng tính năng này.',
          actionLabel: 'Tạo đội mới',
          onAction: "window.ArenaRouter.navigate('S7')"
        }) : `
          <div class="space-y-2.5">
            ${capTeams.map((t) => {
              const selected = currentActing && currentActing.id === t.id;
              return `
                <button type="button" data-select-acting="${t.id}"
                        class="w-full bg-white rounded-[8px] border-2 ${selected ? 'border-[#3D6943] bg-[#EAF0E6]/30' : 'border-[#DDE5D9]'} p-3.5 flex items-center justify-between text-left transition-colors">
                  <div class="flex items-center gap-3 min-w-0">
                    ${UI().avatar(t, { size: 42 })}
                    <div class="min-w-0">
                      <h3 class="font-bold text-sm text-[#111827] truncate">${UI().esc(t.name)}</h3>
                      <p class="text-xs text-[#596158]">${UI().esc(S().getWardName(t.wardId))} • ${t.matchCount} trận</p>
                    </div>
                  </div>
                  <span class="material-symbols-outlined text-[22px] ${selected ? 'text-[#3D6943]' : 'text-[#DDE5D9]'}">
                    ${selected ? 'check_circle' : 'radio_button_unchecked'}
                  </span>
                </button>
              `;
            }).join('')}
          </div>
        `}
      </div>
    `;

    container.querySelectorAll('[data-select-acting]').forEach((b) => {
      b.addEventListener('click', () => {
        const tid = b.getAttribute('data-select-acting');
        const team = S().getState().teams.find((x) => x.id === tid);
        S().setActingTeam(tid);
        UI().toast(`Đang thao tác với đội ${team ? team.name : ''}`, 'success');
        if (returnTo) R().navigate(returnTo);
        else R().back();
      });
    });
  }

  function openActingTeamSheet(onSelectedRoute) {
    const capTeams = S().getMyCaptainedTeams();
    const currentActing = S().getActingTeam();
    const bodyHtml = capTeams.length === 0
      ? `<p class="text-sm text-[#596158]">Bạn chưa làm đội trưởng đội nào đang hoạt động.</p>`
      : `<div class="space-y-2">
          ${capTeams.map((t) => {
            const sel = currentActing && currentActing.id === t.id;
            return `
              <button type="button" onclick="window.ArenaStore.setActingTeam('${t.id}');window.ArenaUI.closeSheet();window.ArenaUI.toast('Đã chọn ${UI().esc(t.name)}');${onSelectedRoute ? `window.ArenaRouter.navigate('${onSelectedRoute}')` : ''}"
                      class="w-full p-3 rounded-[8px] border ${sel ? 'border-[#3D6943] bg-[#EAF0E6]/40' : 'border-[#DDE5D9]'} flex items-center justify-between text-left">
                <div class="flex items-center gap-2.5">
                  ${UI().avatar(t, { size: 36 })}
                  <div>
                    <div class="text-sm font-bold text-[#111827]">${UI().esc(t.name)}</div>
                    <div class="text-xs text-[#596158]">${t.matchCount} trận</div>
                  </div>
                </div>
                <span class="material-symbols-outlined text-[20px] ${sel ? 'text-[#3D6943]' : 'text-[#DDE5D9]'}">${sel ? 'check_circle' : 'radio_button_unchecked'}</span>
              </button>
            `;
          }).join('')}
        </div>`;
    UI().openSheet({ title: 'Chọn đội đang thao tác', bodyHtml });
  }

  global.ArenaTeamUI = { openActingTeamSheet: openActingTeamSheet };

  /* -------------------------------------------------------------------------
     Register all TEAM routes
     ------------------------------------------------------------------------- */
  const router = global.ArenaRouter;
  if (router) {
    router.register('TEAM-01', renderTeam01, { title: 'Đội của tôi', tab: 'TEAM-01', chrome: true, group: 'Đội bóng', aliases: ['S6'] });
    router.register('TEAM-02', renderTeam02, { title: 'Khám phá đội bóng', tab: 'TEAM-01', chrome: true, showBack: true, group: 'Đội bóng' });
    router.register('S7', renderS7, { title: 'Tạo đội mới', tab: 'TEAM-01', chrome: true, showBack: true, group: 'Đội bóng', aliases: ['TEAM-03'] });
    router.register('S8a', renderS8a, { title: 'Chi tiết đội', tab: 'TEAM-01', chrome: true, showBack: true, group: 'Đội bóng', aliases: ['TEAM-04'] });
    router.register('TEAM-05', renderTeam05, { title: 'Sửa thông tin đội', tab: 'TEAM-01', chrome: true, showBack: true, group: 'Đội bóng' });
    router.register('TEAM-06', renderTeam06, { title: 'Thành viên đội', tab: 'TEAM-01', chrome: true, showBack: true, group: 'Đội bóng' });
    router.register('S9', renderS9, { title: 'Mời thành viên', tab: 'TEAM-01', chrome: true, showBack: true, group: 'Đội bóng', aliases: ['TEAM-07'] });
    router.register('TEAM-08', renderTeam08, { title: 'Lời mời vào đội', tab: 'TEAM-01', chrome: true, showBack: true, group: 'Đội bóng' });
    router.register('S10', renderS10, { title: 'Chuyển quyền đội trưởng', tab: 'TEAM-01', chrome: true, showBack: true, group: 'Đội bóng', aliases: ['TEAM-09'] });
    router.register('TEAM-10', renderTeam10, { title: 'Lịch sử đội trưởng', tab: 'TEAM-01', chrome: true, showBack: true, group: 'Đội bóng' });
    router.register('S11', renderS11, { title: 'Rời đội', tab: 'TEAM-01', chrome: true, showBack: true, group: 'Đội bóng', aliases: ['TEAM-11'] });
    router.register('S12', renderS12, { title: 'Giải tán đội', tab: 'TEAM-01', chrome: true, showBack: true, group: 'Đội bóng', aliases: ['TEAM-12'] });
    router.register('S8b', renderS8b, { title: 'Chi tiết đội', tab: 'TEAM-01', chrome: true, showBack: true, group: 'Đội bóng', aliases: ['TEAM-13'] });
    router.register('S13', renderS13, { title: 'Quản lý đội', tab: 'TEAM-01', chrome: true, showBack: true, group: 'Đội bóng' });
    router.register('S14', renderS14, { title: 'Lịch sử thành viên', tab: 'TEAM-01', chrome: true, showBack: true, group: 'Đội bóng' });
    router.register('TEAM-14', renderTeam14, { title: 'Chọn đội đang thao tác', tab: 'TEAM-01', chrome: true, showBack: true, group: 'Đội bóng' });
  }
})(window);
