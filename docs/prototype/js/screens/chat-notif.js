/**
 * ArenaFlow Screens: Chat & Notifications (CHAT-01..05, NOTIF-01..03)
 * Classic non-module script compatible with file:// protocol.
 */
(function (global) {
  'use strict';

  const UI = global.ArenaUI;
  const Store = global.ArenaStore;
  const Router = global.ArenaRouter;

  const REPORT_REASONS = [
    { value: 'HARASSMENT', label: 'Quấy rối / Ngôn từ xúc phạm' },
    { value: 'SPAM', label: 'Spam / Quảng cáo trái phép' },
    { value: 'SCAM', label: 'Lừa đảo / Yêu cầu chuyển khoản sai quy định' },
    { value: 'INAPPROPRIATE', label: 'Nội dung phản cảm' },
    { value: 'OTHER', label: 'Lý do khác' }
  ];

  const NOTIF_CATALOG = [
    { type: 'MATCH_REQUEST_RECEIVED', group: 'Kèo đấu', icon: 'sports_soccer', title: 'Yêu cầu ghép kèo mới', route: 'MATCH-08', refType: 'MATCH_REQUEST', desc: 'Khi đội khác gửi yêu cầu bắt kèo A' },
    { type: 'MATCH_REQUEST_ACCEPTED', group: 'Kèo đấu', icon: 'check_circle', title: 'Yêu cầu bắt kèo được duyệt', route: 'MATCH-03', refType: 'MATCH', desc: 'Đội chủ nhà chấp nhận kèo' },
    { type: 'MATCH_REQUEST_SUPERSEDED', group: 'Kèo đấu', icon: 'swap_horiz', title: 'Yêu cầu đã bị thay thế', route: 'MATCH-09', refType: 'MATCH_REQUEST', desc: 'Chủ nhà đã chốt với đội khác' },
    { type: 'MATCH_CANCELLED', group: 'Kèo đấu', icon: 'cancel', title: 'Kèo đấu bị hủy', route: 'MATCH-07', refType: 'MATCH', desc: 'Trận đấu đã bị hủy bởi một bên' },
    { type: 'PAIRING_UPDATED', group: 'Thương lượng Kèo B', icon: 'handshake', title: 'Cập nhật gói thương lượng', route: 'PAIR-03', refType: 'PAIRING', desc: 'Đối phương vừa chỉnh sân/giờ/chi phí' },
    { type: 'PAIRING_CONFIRMED', group: 'Thương lượng Kèo B', icon: 'verified', title: 'Chốt gói Kèo B thành công', route: 'PAIR-08', refType: 'PAIRING', desc: 'Cả 2 đội đã xác nhận cùng revision' },
    { type: 'MERCENARY_APPLICATION_RECEIVED', group: 'Đá lẻ', icon: 'person_add', title: 'Ứng viên đá lẻ mới', route: 'MERC-10', refType: 'MERCENARY_APPLICATION', desc: 'Có người nộp đơn vào tin tuyển' },
    { type: 'MERCENARY_APPLICATION_APPROVED', group: 'Đá lẻ', icon: 'how_to_reg', title: 'Đơn đá lẻ được duyệt', route: 'MERC-05', refType: 'MERCENARY_APPLICATION', desc: 'Mở khóa địa chỉ sân & phòng chat' },
    { type: 'MERCENARY_WITHDRAWN', group: 'Đá lẻ', icon: 'person_remove', title: 'Ứng viên rút đơn', route: 'MERC-10', refType: 'MERCENARY_APPLICATION', desc: 'Cảnh báo hoặc cập nhật slot' },
    { type: 'TEAM_INVITATION_RECEIVED', group: 'Đội bóng', icon: 'group_add', title: 'Lời mời gia nhập đội', route: 'TEAM-08', refType: 'TEAM_INVITATION', desc: 'Đội trưởng mời bạn vào đội' },
    { type: 'TEAM_CAPTAIN_TRANSFERRED', group: 'Đội bóng', icon: 'military_tech', title: 'Nhận quyền đội trưởng', route: 'TEAM-01', refType: 'TEAM', desc: 'Bạn trở thành đội trưởng mới' },
    { type: 'CHAT_NEW_MESSAGE', group: 'Tin nhắn', icon: 'chat', title: 'Tin nhắn mới', route: 'CHAT-02', refType: 'CONVERSATION', desc: 'Tin nhắn mới trong phòng chat' },
    { type: 'REVIEW_REMINDER', group: 'Uy tín', icon: 'star', title: 'Nhắc đánh giá sau trận', route: 'REP-01', refType: 'MATCH', desc: 'Trận đã kết thúc, hãy chấm sao đối thủ' },
    { type: 'REPORT_NOSHOW_RECEIVED', group: 'Tranh chấp', icon: 'report', title: 'Bị báo cáo NO-SHOW', route: 'REP-08', refType: 'REPORT', desc: 'Có 24h để gửi khiếu nại' },
    { type: 'DISPUTE_RESOLVED', group: 'Tranh chấp', icon: 'gavel', title: 'Kết quả xử lý tranh chấp', route: 'REP-10', refType: 'REPORT', desc: 'Admin đã ra quyết định cuối cùng' },
    { type: 'VENUE_STATUS_CHANGED', group: 'Hệ thống', icon: 'stadium', title: 'Trạng thái sân thay đổi', route: 'VEN-02', refType: 'VENUE', desc: 'Sân tạm ngưng hoặc mở lại' }
  ];

  function formatTime(iso) {
    if (!iso) return '';
    try {
      const d = new Date(iso);
      return d.toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' });
    } catch (e) {
      return '';
    }
  }

  function formatDateTime(iso) {
    if (!iso) return '';
    try {
      const d = new Date(iso);
      return d.toLocaleString('vi-VN', {
        hour: '2-digit',
        minute: '2-digit',
        day: '2-digit',
        month: '2-digit'
      });
    } catch (e) {
      return iso;
    }
  }

  function getMyConversations(state, uid) {
    if (!uid) return [];
    const list = state.conversations || [];
    return list.filter((c) => {
      if (Array.isArray(c.participantIds) && c.participantIds.includes(uid)) return true;
      if (c.contextType === 'MERCENARY' || c.contextType === 'MERCENARY_APPLICATION') {
        const app = (state.applications || []).find((a) => a.id === c.contextId);
        const rec = app && (state.recruitments || []).find((r) => r.id === app.recruitmentId);
        const team = rec && (state.teams || []).find((t) => t.id === rec.teamId);
        if (team && team.captainId === uid) return true;
      }
      return false;
    });
  }

  function resolveConversationMeta(conv, state, uid) {
    const isMerc = conv.contextType === 'MERCENARY' || conv.contextType === 'MERCENARY_APPLICATION';
    let title = conv.title || 'Cuộc trò chuyện';
    let subtitle = '';
    let tagText = 'Kèo đấu';
    let tagVariant = 'info';
    let avatarSeed = { displayName: title };
    let targetRoute = 'MATCH-03';
    let targetParams = {};

    if (isMerc) {
      tagText = 'Đá lẻ';
      tagVariant = 'primary';
      const app = (state.applications || []).find((a) => a.id === conv.contextId);
      const rec = app && (state.recruitments || []).find((r) => r.id === app.recruitmentId);
      const team = rec && (state.teams || []).find((t) => t.id === rec.teamId);
      const isApplicant = app && app.applicantId === uid;
      if (isApplicant) {
        title = conv.title || (team ? team.name : 'Đội tuyển quân');
        subtitle = rec ? `${rec.position} • ${rec.matchDate || ''}` : 'Ứng tuyển đá lẻ';
        avatarSeed = team || { displayName: title };
        targetRoute = 'MERC-05';
        targetParams = app ? { applicationId: app.id } : {};
      } else {
        const applicant = app && state.users ? state.users[app.applicantId] : null;
        title = (applicant && applicant.displayName) || conv.title || 'Ứng viên đá lẻ';
        subtitle = rec ? `Ứng viên ${rec.position} • ${app ? app.status : ''}` : 'Ứng viên đá lẻ';
        avatarSeed = applicant || { displayName: title };
        targetRoute = 'MERC-10';
        targetParams = rec ? { recruitmentId: rec.id } : {};
      }
    } else if (conv.contextType === 'PAIRING') {
      tagText = 'Kèo B';
      tagVariant = 'warning';
      const pairing = (state.pairings || []).find((p) => p.id === conv.contextId);
      subtitle = pairing ? `Thương lượng gói kèo • Rev ${pairing.package ? pairing.package.revision : 1}` : 'Thương lượng Kèo B';
      targetRoute = 'PAIR-03';
      targetParams = pairing ? { pairingId: pairing.id } : {};
    } else {
      const req = (state.matchRequests || []).find((r) => r.id === conv.contextId);
      const match = req && (state.matches || []).find((m) => m.id === req.matchId);
      subtitle = match ? `Kèo ${match.matchDate} (${match.startTime})` : 'Trao đổi kèo đấu';
      targetRoute = req ? 'MATCH-08' : 'MATCH-03';
      targetParams = match ? { matchId: match.id } : {};
    }

    return { isMerc, title, subtitle, tagText, tagVariant, avatarSeed, targetRoute, targetParams };
  }

  /* =========================================================================
     CHAT-05: Report Chat Bottom Sheet & Route
     ========================================================================= */
  function openChatReportSheet(convId) {
    const state = Store.getState();
    const uid = state.currentUserId;
    const conv = (state.conversations || []).find((c) => c.id === convId);
    if (!conv) {
      UI.toast('Không tìm thấy cuộc trò chuyện để báo cáo.', 'error');
      return;
    }
    const existing = (state.chatReports || []).find((r) => r.conversationId === convId && r.reporterId === uid);
    if (existing) {
      UI.toast('Bạn đã báo cáo cuộc trò chuyện này trước đó.', 'warning');
      return;
    }

    const bodyHtml = `
      <div class="space-y-3">
        <div class="p-3 rounded-[8px] bg-[#FEF2F2] border border-[#FECACA] text-xs text-[#991B1B] leading-relaxed">
          <strong>Lưu ý (CHAT-05):</strong> Hệ thống sẽ đính kèm lịch sử tin nhắn gần nhất trong phòng <strong>${UI.esc(conv.title || conv.id)}</strong> để Admin kiểm duyệt.
        </div>
        <div>
          <label class="block text-xs font-semibold text-[#2D342C] mb-1.5">Lý do báo cáo *</label>
          <select id="chat-report-reason" class="w-full rounded-[8px] border border-[#DDE5D9] bg-white px-3 py-2 text-sm text-[#111827] focus:border-[#3D6943] focus:ring-0">
            ${REPORT_REASONS.map((r) => `<option value="${r.value}">${UI.esc(r.label)}</option>`).join('')}
          </select>
        </div>
        <div>
          <label class="block text-xs font-semibold text-[#2D342C] mb-1.5">Mô tả chi tiết *</label>
          <textarea id="chat-report-desc" rows="3" placeholder="Mô tả hành vi vi phạm (tối thiểu 10 ký tự)..."
                    class="w-full rounded-[8px] border border-[#DDE5D9] bg-white px-3 py-2 text-sm text-[#111827] focus:border-[#3D6943] focus:ring-0"></textarea>
        </div>
      </div>
    `;

    const footerHtml = `
      <div class="flex items-center gap-2">
        <button type="button" onclick="window.ArenaUI.closeSheet()"
                class="flex-1 py-2.5 rounded-[8px] border border-[#DDE5D9] bg-white text-xs font-semibold text-[#2D342C]">
          Hủy
        </button>
        <button type="button" id="btn-submit-chat-report"
                class="flex-1 py-2.5 rounded-[8px] bg-[#DC2626] hover:bg-[#B91C1C] text-white text-xs font-semibold">
          Gửi báo cáo
        </button>
      </div>
    `;

    UI.openSheet({
      title: 'Báo cáo nội dung chat (CHAT-05)',
      bodyHtml,
      footerHtml
    });

    const submitBtn = document.getElementById('btn-submit-chat-report');
    if (submitBtn) {
      submitBtn.addEventListener('click', () => {
        const reason = document.getElementById('chat-report-reason')?.value || 'OTHER';
        const desc = (document.getElementById('chat-report-desc')?.value || '').trim();
        if (desc.length < 10) {
          UI.toast('Vui lòng nhập mô tả tối thiểu 10 ký tự.', 'error');
          return;
        }
        const nextReports = Array.isArray(state.chatReports) ? [...state.chatReports] : [];
        nextReports.push({
          id: 'creport-' + Date.now(),
          conversationId: convId,
          reporterId: uid,
          reason,
          description: desc,
          createdAt: Store.getMockClock().toISOString()
        });
        Store.setState({ chatReports: nextReports });
        UI.closeSheet();
        UI.toast('Đã gửi báo cáo nội dung chat tới Admin.', 'success');
      });
    }
  }

  /* =========================================================================
     CHAT-01: Hộp thư hội thoại
     ========================================================================= */
  Router.register('CHAT-01', (container) => {
    const state = Store.getState();
    const uid = state.currentUserId;
    if (!uid) {
      container.innerHTML = `<div class="p-4">${UI.emptyState({
        icon: 'lock',
        title: 'Đăng nhập để xem tin nhắn',
        description: 'Vui lòng đăng nhập hoặc chọn vai trò trong Demo Panel để xem hộp thư.',
        actionLabel: 'Đăng nhập',
        onAction: "window.ArenaRouter.navigate('S1')"
      })}</div>`;
      return;
    }

    let filter = 'ALL';
    let pageLimit = 10;

    function render() {
      const s = Store.getState();
      const myConvs = getMyConversations(s, uid);
      const filtered = myConvs.filter((c) => {
        if (filter === 'MATCH') return c.contextType === 'MATCH_REQUEST' || c.contextType === 'PAIRING';
        if (filter === 'MERC') return c.contextType === 'MERCENARY' || c.contextType === 'MERCENARY_APPLICATION';
        if (filter === 'UNREAD') {
          return (c.messages || []).some((m) => !(m.readBy || []).includes(uid));
        }
        return true;
      });

      filtered.sort((a, b) => {
        const tA = (a.messages && a.messages[a.messages.length - 1]?.timestamp) || '';
        const tB = (b.messages && b.messages[b.messages.length - 1]?.timestamp) || '';
        return tB.localeCompare(tA);
      });

      const visible = filtered.slice(0, pageLimit);
      const filterLabels = { ALL: 'Tất cả', UNREAD: 'Chưa đọc', MATCH: 'Kèo đấu', MERC: 'Đá lẻ' };
      const isFiltered = filter !== 'ALL';

      function openInboxFilterSheet() {
        const options = [
          { id: 'ALL', label: `Tất cả hội thoại (${myConvs.length})`, icon: 'forum' },
          { id: 'UNREAD', label: 'Tin nhắn chưa đọc', icon: 'mark_chat_unread' },
          { id: 'MATCH', label: 'Kèo đấu (Kèo A & Kèo B)', icon: 'sports_soccer' },
          { id: 'MERC', label: 'Đá lẻ (Tuyển quân)', icon: 'person_add' }
        ];
        UI.openSheet({
          title: 'Lọc hộp thư hội thoại',
          bodyHtml: `
            <div class="space-y-2">
              ${options.map((opt) => `
                <label class="flex items-center justify-between p-3 rounded-[8px] border ${filter === opt.id ? 'border-[#3D6943] bg-[#F4FBF5]' : 'border-[#DDE5D9] bg-white'} cursor-pointer">
                  <span class="flex items-center gap-2.5 text-xs font-semibold text-[#111827]">
                    <span class="material-symbols-outlined text-[18px] text-[#3D6943]">${opt.icon}</span>
                    ${UI.esc(opt.label)}
                  </span>
                  <input type="radio" name="inbox-filter-opt" value="${opt.id}" ${filter === opt.id ? 'checked' : ''} class="text-[#3D6943]" />
                </label>
              `).join('')}
            </div>
          `,
          footerHtml: `
            <div class="flex items-center gap-2">
              <button type="button" id="btn-chat-filter-reset"
                      class="flex-1 py-2.5 rounded-[8px] border border-[#DDE5D9] bg-white text-xs font-semibold text-[#2D342C]">
                Đặt lại
              </button>
              <button type="button" id="btn-chat-filter-apply"
                      class="flex-1 py-2.5 rounded-[8px] bg-[#3D6943] text-white text-xs font-semibold">
                Áp dụng
              </button>
            </div>
          `
        });

        document.getElementById('btn-chat-filter-reset')?.addEventListener('click', () => {
          filter = 'ALL';
          UI.closeSheet();
          render();
        });
        document.getElementById('btn-chat-filter-apply')?.addEventListener('click', () => {
          const picked = document.querySelector('input[name="inbox-filter-opt"]:checked')?.value || 'ALL';
          filter = picked;
          UI.closeSheet();
          render();
        });
      }

      container.innerHTML = `
        <div class="p-4 space-y-3">
          <div class="flex items-center justify-between gap-2">
            <div class="min-w-0">
              <h2 class="text-lg font-bold text-[#111827]">Hộp thư hội thoại</h2>
              <p class="text-xs text-[#596158] truncate">
                ${isFiltered ? `Đang lọc: ${filterLabels[filter] || filter} (${filtered.length})` : 'Lưu trữ 90 ngày sau trận đấu'}
              </p>
            </div>
            <div class="flex items-center gap-1.5 shrink-0">
              <button type="button" id="btn-chat-open-filter" aria-label="Lọc tin nhắn"
                      class="relative w-9 h-9 rounded-[8px] border ${isFiltered ? 'border-[#3D6943] bg-[#EAF0E6] text-[#3D6943]' : 'border-[#DDE5D9] bg-white text-[#2D342C]'} flex items-center justify-center">
                <span class="material-symbols-outlined text-[20px]">tune</span>
                ${isFiltered ? '<span class="absolute top-1 right-1 w-2 h-2 rounded-full bg-[#3D6943]"></span>' : ''}
              </button>
              <button type="button" id="btn-goto-notif" aria-label="Thông báo"
                      class="w-9 h-9 rounded-[8px] border border-[#DDE5D9] bg-white text-[#2D342C] flex items-center justify-center">
                <span class="material-symbols-outlined text-[20px]">notifications</span>
              </button>
            </div>
          </div>

          ${visible.length === 0 ? UI.emptyState({
            icon: 'forum',
            title: filter === 'UNREAD' ? 'Đã đọc tất cả tin nhắn' : 'Chưa có cuộc trò chuyện',
            description: filter === 'ALL'
              ? 'Cuộc trò chuyện sẽ tự tạo khi bạn gửi yêu cầu kèo hoặc ứng tuyển đá lẻ.'
              : 'Không có hội thoại nào khớp bộ lọc hiện tại.',
            actionLabel: isFiltered ? 'Xem tất cả' : '',
            onAction: "document.getElementById('btn-chat-reset-inline')?.click()"
          }) + (isFiltered ? '<button type="button" id="btn-chat-reset-inline" class="hidden"></button>' : '') : `
            <div class="space-y-2.5">
              ${visible.map((conv) => {
                const meta = resolveConversationMeta(conv, s, uid);
                const msgs = conv.messages || [];
                const unreadCount = msgs.filter((m) => !(m.readBy || []).includes(uid)).length;
                const lastMsg = msgs[msgs.length - 1];
                const lastPreview = lastMsg
                  ? `${lastMsg.senderId === uid ? 'Bạn: ' : ''}${lastMsg.text}`
                  : 'Chưa có tin nhắn';
                return `
                  <article data-open-conv="${UI.esc(conv.id)}" data-is-merc="${meta.isMerc ? '1' : '0'}"
                           class="bg-white rounded-[8px] border ${unreadCount > 0 ? 'border-[#3D6943] bg-[#F4FBF5]' : 'border-[#DDE5D9]'} p-3 flex items-center gap-3 cursor-pointer hover:border-[#3D6943] transition-colors">
                    <div class="relative shrink-0">
                      ${UI.avatar(meta.avatarSeed, { size: 44 })}
                      ${unreadCount > 0 ? `<span class="absolute -top-1 -right-1 min-w-[18px] h-[18px] px-1 rounded-full bg-[#DC2626] text-white text-[10px] font-bold flex items-center justify-center border-2 border-white">${unreadCount}</span>` : ''}
                    </div>
                    <div class="flex-1 min-w-0">
                      <div class="flex items-center justify-between gap-2">
                        <h3 class="text-sm ${unreadCount > 0 ? 'font-bold text-[#111827]' : 'font-semibold text-[#2D342C]'} truncate">${UI.esc(meta.title)}</h3>
                        <span class="text-[11px] text-[#596158] shrink-0">${lastMsg ? formatTime(lastMsg.timestamp) : ''}</span>
                      </div>
                      <div class="flex items-center gap-1.5 mt-0.5">
                        ${UI.badge(meta.tagText, meta.tagVariant)}
                        <span class="text-[11px] text-[#596158] truncate">${UI.esc(meta.subtitle)}</span>
                        ${conv.canSend === false ? UI.badge('Chỉ đọc', 'neutral') : ''}
                      </div>
                      <p class="text-xs mt-1 truncate ${unreadCount > 0 ? 'font-semibold text-[#111827]' : 'text-[#596158]'}">${UI.esc(lastPreview)}</p>
                    </div>
                  </article>
                `;
              }).join('')}
            </div>
            ${filtered.length > pageLimit ? `
              <button type="button" id="btn-chat-load-more"
                      class="w-full py-2.5 rounded-[8px] bg-white border border-[#DDE5D9] text-xs font-semibold text-[#3D6943]">
                Tải thêm hội thoại (${filtered.length - pageLimit} còn lại)
              </button>
            ` : ''}
          `}
        </div>
      `;

      container.querySelector('#btn-chat-open-filter')?.addEventListener('click', openInboxFilterSheet);
      container.querySelector('#btn-goto-notif')?.addEventListener('click', () => Router.navigate('NOTIF-01'));
      container.querySelector('#btn-chat-reset-inline')?.addEventListener('click', () => {
        filter = 'ALL';
        render();
      });
      container.querySelector('#btn-chat-load-more')?.addEventListener('click', () => {
        pageLimit += 10;
        render();
      });
      container.querySelectorAll('[data-open-conv]').forEach((card) => {
        card.addEventListener('click', () => {
          const convId = card.getAttribute('data-open-conv');
          const isMerc = card.getAttribute('data-is-merc') === '1';
          Router.navigate(isMerc ? 'CHAT-03' : 'CHAT-02', { conversationId: convId });
        });
      });
    }

    render();
  }, {
    title: 'Tin nhắn',
    tab: 'CHAT-01',
    group: 'Chat & Thông báo',
    aliases: ['inbox', 'chat-list']
  });

  /* =========================================================================
     Shared Chat Room Renderer (CHAT-02, CHAT-03, CHAT-04)
     ========================================================================= */
  function renderChatRoom(container, params, mode) {
    const state = Store.getState();
    const uid = state.currentUserId;
    const isReadOnlyTest = mode === 'READONLY';
    const convs = state.conversations || [];

    let conv = params.conversationId
      ? convs.find((c) => c.id === params.conversationId)
      : null;

    if (!conv) {
      if (mode === 'MERCENARY') {
        conv = convs.find((c) => c.contextType === 'MERCENARY' || c.contextType === 'MERCENARY_APPLICATION') || convs[0];
      } else {
        conv = convs.find((c) => c.contextType === 'MATCH_REQUEST' || c.contextType === 'PAIRING') || convs[0];
      }
    }

    if (!conv) {
      container.innerHTML = `<div class="p-4">${UI.errorState({
        title: 'Cuộc trò chuyện không tồn tại',
        message: 'Phòng chat này có thể đã hết hạn 90 ngày hoặc bị xóa.',
        code: 'CHAT_404',
        onBack: "window.ArenaRouter.navigate('CHAT-01')"
      })}</div>`;
      return;
    }

    // Mark messages as read
    if (uid && Array.isArray(conv.messages)) {
      let dirty = false;
      conv.messages.forEach((m) => {
        if (!Array.isArray(m.readBy)) m.readBy = [];
        if (!m.readBy.includes(uid)) {
          m.readBy.push(uid);
          dirty = true;
        }
      });
      if (dirty) {
        Store.setState({ conversations: [...convs] });
        return; // Store subscription will re-render
      }
    }

    const meta = resolveConversationMeta(conv, state, uid);
    const canSend = isReadOnlyTest ? false : (conv.canSend !== false && !!uid);
    const lockReason = isReadOnlyTest
      ? 'Kèo đấu đã kết thúc hoặc bị hủy — phòng chat chuyển sang chế độ chỉ đọc (canSend = false).'
      : (!uid ? 'Bạn cần đăng nhập để gửi tin nhắn.' : (conv.readOnlyReason || 'Bạn không còn quyền gửi tin nhắn trong phòng này.'));

    // Check AC-P05 for mercenary applicant
    let mercWarning = '';
    if (meta.isMerc) {
      const app = (state.applications || []).find((a) => a.id === conv.contextId);
      if (app && app.applicantId === uid && app.status !== 'APPROVED') {
        mercWarning = 'Địa chỉ sân cụ thể chỉ hiển thị sau khi đội trưởng duyệt đơn (AC-P05).';
      }
    }

    let reconnecting = false;

    function draw() {
      const s = Store.getState();
      const liveConv = (s.conversations || []).find((c) => c.id === conv.id) || conv;
      const msgs = liveConv.messages || [];

      container.innerHTML = `
        <div class="flex flex-col h-full bg-[#F8FAF3]">
          <!-- Context Header -->
          <div class="bg-white border-b border-[#DDE5D9] px-4 py-2.5 flex items-center justify-between gap-2 shrink-0">
            <div class="flex items-center gap-2.5 min-w-0">
              ${UI.avatar(meta.avatarSeed, { size: 36 })}
              <div class="min-w-0">
                <div class="flex items-center gap-1.5">
                  <h3 class="text-sm font-bold text-[#111827] truncate">${UI.esc(meta.title)}</h3>
                  ${UI.badge(meta.tagText, meta.tagVariant)}
                </div>
                <p class="text-[11px] text-[#596158] truncate">${UI.esc(meta.subtitle)}</p>
              </div>
            </div>
            <div class="flex items-center gap-1 shrink-0">
              <button type="button" id="btn-room-context"
                      class="px-2 py-1 rounded-[8px] bg-[#EAF0E6] text-[#3D6943] text-[11px] font-semibold">
                Chi tiết
              </button>
              <button type="button" id="btn-room-report" title="Báo cáo chat"
                      class="w-8 h-8 rounded-[8px] flex items-center justify-center text-[#A73B21] hover:bg-[#FEF2F2]">
                <span class="material-symbols-outlined text-[18px]">flag</span>
              </button>
            </div>
          </div>

          <!-- Notices -->
          ${reconnecting ? `
            <div class="px-4 py-2 bg-[#FEF3C7] border-b border-[#FDE68A] text-[11px] text-[#B45309] font-medium flex items-center justify-between">
              <span class="flex items-center gap-1.5">
                <span class="material-symbols-outlined text-[15px] animate-spin">sync</span>
                Đang kết nối lại máy chủ chat...
              </span>
              <button type="button" id="btn-toggle-reconnect" class="underline font-bold">Xong</button>
            </div>
          ` : ''}

          ${mercWarning ? `
            <div class="px-4 py-2 bg-[#EFF6FF] border-b border-[#BFDBFE] text-[11px] text-[#1D4ED8] flex items-center gap-1.5">
              <span class="material-symbols-outlined text-[15px]">shield</span>
              <span>${UI.esc(mercWarning)}</span>
            </div>
          ` : ''}

          ${!canSend ? `
            <div class="px-4 py-2.5 bg-[#FEF2F2] border-b border-[#FECACA] text-xs text-[#991B1B] flex items-start gap-2">
              <span class="material-symbols-outlined text-[16px] shrink-0 mt-0.5">lock</span>
              <div>
                <strong>Chế độ chỉ đọc (CHAT-04):</strong> ${UI.esc(lockReason)}
              </div>
            </div>
          ` : ''}

          <!-- Messages Stream -->
          <div id="chat-stream" class="flex-1 overflow-y-auto p-4 space-y-3">
            <div class="text-center">
              <span class="inline-block px-2.5 py-0.5 rounded-full bg-[#EAF0E6] text-[10px] text-[#596158]">
                Lịch sử được lưu 90 ngày
              </span>
            </div>
            ${msgs.map((m) => {
              const isMine = m.senderId === uid;
              const senderName = Store.getUserDisplayName(m.senderId, false);
              return `
                <div class="flex flex-col ${isMine ? 'items-end' : 'items-start'}">
                  ${!isMine ? `<span class="text-[11px] font-semibold text-[#596158] mb-0.5 px-1">${UI.esc(senderName)}</span>` : ''}
                  <div class="max-w-[78%] px-3 py-2 rounded-[12px] text-xs leading-relaxed ${
                    isMine
                      ? 'bg-[#3D6943] text-white rounded-br-[2px]'
                      : 'bg-white border border-[#DDE5D9] text-[#111827] rounded-bl-[2px]'
                  }">
                    ${UI.esc(m.text)}
                  </div>
                  <span class="text-[10px] text-[#757D73] mt-0.5 px-1">${formatTime(m.timestamp)}</span>
                </div>
              `;
            }).join('')}
          </div>

          <!-- Composer -->
          <div class="bg-white border-t border-[#DDE5D9] p-3 shrink-0">
            ${canSend ? `
              <div class="flex items-center gap-2">
                <input id="chat-input" type="text" placeholder="Nhập tin nhắn..."
                       class="flex-1 rounded-[8px] border border-[#DDE5D9] bg-[#F8FAF3] px-3 py-2 text-xs text-[#111827] focus:border-[#3D6943] focus:bg-white focus:ring-0" />
                <button type="button" id="btn-chat-send"
                        class="px-3.5 py-2 rounded-[8px] bg-[#3D6943] hover:bg-[#315C38] text-white text-xs font-semibold flex items-center gap-1">
                  <span class="material-symbols-outlined text-[16px]">send</span>
                  Gửi
                </button>
              </div>
              <div class="flex items-center justify-between mt-2 text-[10px] text-[#596158]">
                <button type="button" id="btn-sim-reconnect" class="hover:underline">Mô phỏng mất kết nối</button>
                <button type="button" id="btn-sim-readonly" class="hover:underline">Xem trạng thái chỉ đọc (CHAT-04)</button>
              </div>
            ` : `
              <div class="flex items-center justify-between gap-2 px-3 py-2 rounded-[8px] bg-[#F1F5EC] border border-[#DDE5D9] text-xs text-[#596158]">
                <span class="flex items-center gap-1.5">
                  <span class="material-symbols-outlined text-[16px]">block</span>
                  Không thể gửi tin nhắn mới
                </span>
                ${isReadOnlyTest ? `
                  <button type="button" onclick="window.ArenaRouter.navigate('CHAT-02', { conversationId: '${UI.esc(conv.id)}' })"
                          class="text-[#3D6943] font-semibold underline">Mở khóa thử</button>
                ` : ''}
              </div>
            `}
          </div>
        </div>
      `;

      const stream = container.querySelector('#chat-stream');
      if (stream) stream.scrollTop = stream.scrollHeight;

      container.querySelector('#btn-room-context')?.addEventListener('click', () => {
        Router.navigate(meta.targetRoute, meta.targetParams);
      });
      container.querySelector('#btn-room-report')?.addEventListener('click', () => {
        openChatReportSheet(conv.id);
      });
      container.querySelector('#btn-sim-reconnect')?.addEventListener('click', () => {
        reconnecting = true;
        draw();
      });
      container.querySelector('#btn-toggle-reconnect')?.addEventListener('click', () => {
        reconnecting = false;
        draw();
      });
      container.querySelector('#btn-sim-readonly')?.addEventListener('click', () => {
        Router.navigate('CHAT-04', { conversationId: conv.id });
      });

      const sendMessage = () => {
        const input = container.querySelector('#chat-input');
        const text = (input?.value || '').trim();
        if (!text) return;
        const nextConvs = (Store.getState().conversations || []).map((c) => {
          if (c.id !== conv.id) return c;
          return {
            ...c,
            messages: [
              ...(c.messages || []),
              {
                id: 'msg-' + Date.now(),
                senderId: uid,
                text,
                timestamp: Store.getMockClock().toISOString(),
                readBy: [uid]
              }
            ]
          };
        });
        Store.setState({ conversations: nextConvs });
      };

      container.querySelector('#btn-chat-send')?.addEventListener('click', sendMessage);
      container.querySelector('#chat-input')?.addEventListener('keydown', (e) => {
        if (e.key === 'Enter') {
          e.preventDefault();
          sendMessage();
        }
      });
    }

    draw();
  }

  Router.register('CHAT-02', (container, params) => {
    renderChatRoom(container, params || {}, 'MATCH');
  }, {
    title: 'Chat giữa 2 đội',
    tab: 'CHAT-01',
    showBack: true,
    group: 'Chat & Thông báo',
    aliases: ['chat-room', 'chat-match']
  });

  Router.register('CHAT-03', (container, params) => {
    renderChatRoom(container, params || {}, 'MERCENARY');
  }, {
    title: 'Chat đội trưởng & ứng viên',
    tab: 'CHAT-01',
    showBack: true,
    group: 'Chat & Thông báo',
    aliases: ['chat-mercenary']
  });

  Router.register('CHAT-04', (container, params) => {
    renderChatRoom(container, params || {}, 'READONLY');
  }, {
    title: 'Chat chỉ đọc',
    tab: 'CHAT-01',
    showBack: true,
    group: 'Chat & Thông báo',
    aliases: ['chat-readonly']
  });

  Router.register('CHAT-05', (container, params) => {
    const convs = Store.getState().conversations || [];
    const targetId = (params && params.conversationId) || (convs[0] && convs[0].id);
    container.innerHTML = `
      <div class="p-4 space-y-3">
        <div class="bg-white rounded-[8px] border border-[#DDE5D9] p-4 space-y-3">
          <div class="flex items-center gap-2 text-[#A73B21]">
            <span class="material-symbols-outlined text-[22px]">flag</span>
            <h2 class="text-base font-bold">Báo cáo nội dung chat (CHAT-05)</h2>
          </div>
          <p class="text-xs text-[#596158] leading-relaxed">
            Chọn cuộc trò chuyện và gửi báo cáo vi phạm ngôn từ, spam hoặc lừa đảo tới quản trị viên.
          </p>
          <button type="button" id="btn-open-chat-report"
                  class="w-full py-2.5 rounded-[8px] bg-[#DC2626] text-white text-xs font-semibold">
            Mở Sheet báo cáo chat
          </button>
        </div>
      </div>
    `;
    container.querySelector('#btn-open-chat-report')?.addEventListener('click', () => {
      openChatReportSheet(targetId);
    });
  }, {
    title: 'Báo cáo nội dung chat',
    tab: 'CHAT-01',
    showBack: true,
    group: 'Chat & Thông báo',
    aliases: ['chat-report-sheet']
  });

  /* =========================================================================
     NOTIF-01: Trung tâm thông báo
     ========================================================================= */
  function resolveNotifTarget(notif, state) {
    if (!notif) return { ok: false, code: '404', reason: 'Thông báo không tồn tại.' };
    const uid = state.currentUserId;
    if (!uid) return { ok: false, code: '401', reason: 'Phiên đăng nhập đã hết hạn.' };

    const refType = notif.referenceType;
    const refId = notif.referenceId;

    if (refType === 'MATCH') {
      const m = (state.matches || []).find((x) => x.id === refId);
      if (!m) return { ok: false, code: '404', reason: 'Kèo đấu đã bị xóa hoặc không còn tồn tại.' };
      return { ok: true, route: notif.targetRoute || 'MATCH-03', params: { matchId: m.id } };
    }
    if (refType === 'MATCH_REQUEST') {
      const r = (state.matchRequests || []).find((x) => x.id === refId);
      if (!r) return { ok: false, code: '404', reason: 'Yêu cầu ghép kèo không còn tồn tại.' };
      return { ok: true, route: notif.targetRoute || 'MATCH-08', params: { requestId: r.id, matchId: r.matchId } };
    }
    if (refType === 'MERCENARY_APPLICATION') {
      const a = (state.applications || []).find((x) => x.id === refId);
      if (!a) return { ok: false, code: '404', reason: 'Đơn ứng tuyển không còn tồn tại.' };
      return { ok: true, route: notif.targetRoute || 'MERC-05', params: { applicationId: a.id } };
    }
    if (refType === 'TEAM_INVITATION') {
      const inv = (state.invitations || []).find((x) => x.id === refId);
      if (!inv) return { ok: false, code: '404', reason: 'Lời mời gia nhập đội đã hết hạn hoặc bị thu hồi.' };
      if (inv.invitedUserId !== uid) return { ok: false, code: '403', reason: 'Bạn không có quyền xem lời mời này.' };
      return { ok: true, route: notif.targetRoute || 'TEAM-08', params: { invitationId: inv.id } };
    }
    if (refType === 'REPORT') {
      const rep = (state.reports || []).find((x) => x.id === refId);
      if (!rep) return { ok: false, code: '404', reason: 'Báo cáo hoặc tranh chấp không còn tồn tại.' };
      return { ok: true, route: notif.targetRoute || 'REP-08', params: { reportId: rep.id } };
    }

    return { ok: true, route: notif.targetRoute || 'MATCH-01', params: {} };
  }

  Router.register('NOTIF-01', (container) => {
    const state = Store.getState();
    const uid = state.currentUserId;
    if (!uid) {
      container.innerHTML = `<div class="p-4">${UI.emptyState({
        icon: 'notifications_off',
        title: 'Đăng nhập để xem thông báo',
        description: 'Vui lòng đăng nhập để nhận cập nhật về kèo đấu và đội bóng.',
        actionLabel: 'Đăng nhập',
        onAction: "window.ArenaRouter.navigate('S1')"
      })}</div>`;
      return;
    }

    let filter = 'ALL'; // ALL | UNREAD

    function render() {
      const s = Store.getState();
      const allMine = (s.notifications || []).filter((n) => n.userId === uid);
      const unreadCount = allMine.filter((n) => !n.read).length;
      const list = filter === 'UNREAD' ? allMine.filter((n) => !n.read) : allMine;

      container.innerHTML = `
        <div class="p-4 space-y-3">
          <div class="flex items-center justify-between gap-2">
            <div>
              <h2 class="text-lg font-bold text-[#111827]">Trung tâm thông báo</h2>
              <p class="text-[11px] text-[#596158]">Lưu trữ tối đa 30 ngày</p>
            </div>
            ${unreadCount > 0 ? `
              <button type="button" id="btn-mark-all-read"
                      class="px-2.5 py-1.5 rounded-[8px] bg-[#EAF0E6] text-[#3D6943] text-xs font-semibold">
                Đánh dấu tất cả đã đọc
              </button>
            ` : ''}
          </div>

          <div class="flex items-center justify-between gap-2">
            <div class="flex items-center gap-1.5">
              <button type="button" data-notif-filter="ALL"
                      class="px-3 py-1.5 rounded-full text-xs font-semibold border ${filter === 'ALL' ? 'bg-[#3D6943] text-white border-[#3D6943]' : 'bg-white text-[#596158] border-[#DDE5D9]'}">
                Tất cả (${allMine.length})
              </button>
              <button type="button" data-notif-filter="UNREAD"
                      class="px-3 py-1.5 rounded-full text-xs font-semibold border ${filter === 'UNREAD' ? 'bg-[#3D6943] text-white border-[#3D6943]' : 'bg-white text-[#596158] border-[#DDE5D9]'}">
                Chưa đọc (${unreadCount})
              </button>
            </div>
            <button type="button" id="btn-open-catalog"
                    class="text-[11px] font-semibold text-[#3D6943] hover:underline">
              Catalog (NOTIF-03)
            </button>
          </div>

          ${list.length === 0 ? UI.emptyState({
            icon: 'notifications_none',
            title: filter === 'UNREAD' ? 'Không có thông báo chưa đọc' : 'Chưa có thông báo',
            description: 'Mọi cập nhật về kèo đấu, đá lẻ và đội bóng sẽ hiển thị tại đây.'
          }) : `
            <div class="space-y-2.5">
              ${list.map((n) => `
                <article data-notif-id="${UI.esc(n.id)}"
                         class="bg-white rounded-[8px] border ${!n.read ? 'border-[#3D6943] bg-[#F4FBF5]' : 'border-[#DDE5D9]'} p-3 flex items-start gap-3 cursor-pointer hover:border-[#3D6943] transition-colors">
                  <div class="w-9 h-9 rounded-full ${!n.read ? 'bg-[#BDEFBF] text-[#1C4825]' : 'bg-[#EAF0E6] text-[#596158]'} flex items-center justify-center shrink-0">
                    <span class="material-symbols-outlined text-[18px]">${!n.read ? 'notifications_active' : 'notifications'}</span>
                  </div>
                  <div class="flex-1 min-w-0">
                    <div class="flex items-center justify-between gap-2">
                      <h3 class="text-xs ${!n.read ? 'font-bold text-[#111827]' : 'font-semibold text-[#2D342C]'} truncate">${UI.esc(n.title)}</h3>
                      ${!n.read ? '<span class="w-2 h-2 rounded-full bg-[#3D6943] shrink-0"></span>' : ''}
                    </div>
                    <p class="text-xs text-[#596158] mt-0.5 leading-relaxed">${UI.esc(n.body)}</p>
                    <div class="flex items-center justify-between mt-1.5 text-[10px] text-[#757D73]">
                      <span>${formatDateTime(n.createdAt)}</span>
                      <span class="font-mono">${UI.esc(n.type)}</span>
                    </div>
                  </div>
                </article>
              `).join('')}
            </div>
          `}
        </div>
      `;

      container.querySelectorAll('[data-notif-filter]').forEach((btn) => {
        btn.addEventListener('click', () => {
          filter = btn.getAttribute('data-notif-filter');
          render();
        });
      });

      container.querySelector('#btn-open-catalog')?.addEventListener('click', () => Router.navigate('NOTIF-03'));

      container.querySelector('#btn-mark-all-read')?.addEventListener('click', () => {
        const next = (Store.getState().notifications || []).map((n) =>
          n.userId === uid ? { ...n, read: true } : n
        );
        Store.setState({ notifications: next });
        UI.toast('Đã đánh dấu tất cả thông báo là đã đọc.', 'success');
      });

      container.querySelectorAll('[data-notif-id]').forEach((el) => {
        el.addEventListener('click', () => {
          const nid = el.getAttribute('data-notif-id');
          const curState = Store.getState();
          const notif = (curState.notifications || []).find((n) => n.id === nid);
          if (!notif) return;

          if (!notif.read) {
            const next = (curState.notifications || []).map((n) => (n.id === nid ? { ...n, read: true } : n));
            Store.setState({ notifications: next });
          }

          const res = resolveNotifTarget(notif, Store.getState());
          if (res.ok) {
            Router.navigate(res.route, res.params);
          } else {
            Router.navigate('NOTIF-02', { notifId: notif.id, code: res.code, reason: res.reason });
          }
        });
      });
    }

    render();
  }, {
    title: 'Thông báo',
    showBack: true,
    group: 'Chat & Thông báo',
    aliases: ['notifications']
  });

  /* =========================================================================
     NOTIF-02: Điều hướng đích an toàn
     ========================================================================= */
  Router.register('NOTIF-02', (container, params) => {
    const p = params || {};
    const code = p.code || '404';
    const reason = p.reason || 'Đối tượng hoặc kèo đấu liên kết từ thông báo này đã bị xóa hoặc bạn không còn quyền truy cập.';

    container.innerHTML = `
      <div class="p-4 space-y-4">
        <div class="bg-white rounded-[12px] border border-[#DDE5D9] p-5 text-center space-y-3">
          <div class="w-14 h-14 mx-auto rounded-full bg-[#FEF2F2] text-[#A73B21] flex items-center justify-center">
            <span class="material-symbols-outlined text-[30px]">link_off</span>
          </div>
          <h2 class="text-base font-bold text-[#111827]">Không thể mở đích thông báo (${UI.esc(code)})</h2>
          <p class="text-xs text-[#596158] leading-relaxed">${UI.esc(reason)}</p>
          <div class="p-2.5 rounded-[8px] bg-[#F8FAF3] border border-[#DDE5D9] text-[11px] text-[#596158] text-left">
            <strong>Cơ chế an toàn (NOTIF-02):</strong> Resolver kiểm tra sự tồn tại của thực thể và quyền truy cập trước khi chuyển trang để tránh crash hoặc rò rỉ dữ liệu.
          </div>
          <div class="flex items-center gap-2 pt-2">
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
  }, {
    title: 'Điều hướng an toàn',
    showBack: true,
    group: 'Chat & Thông báo',
    aliases: ['notif-safe-nav']
  });

  /* =========================================================================
     NOTIF-03: Catalog loại thông báo (Resolver test view)
     ========================================================================= */
  Router.register('NOTIF-03', (container) => {
    container.innerHTML = `
      <div class="p-4 space-y-3">
        <div class="bg-[#14532D] text-white rounded-[12px] p-4">
          <h2 class="text-base font-bold">Catalog loại thông báo (NOTIF-03)</h2>
          <p class="text-xs text-white/80 mt-1">
            Kiểm thử ánh xạ loại thông báo → route đích và xử lý an toàn khi đích không tồn tại.
          </p>
        </div>

        <div class="space-y-2">
          ${NOTIF_CATALOG.map((item) => `
            <div class="bg-white rounded-[8px] border border-[#DDE5D9] p-3 flex items-center justify-between gap-2">
              <div class="flex items-center gap-2.5 min-w-0">
                <div class="w-9 h-9 rounded-[8px] bg-[#EAF0E6] text-[#3D6943] flex items-center justify-center shrink-0">
                  <span class="material-symbols-outlined text-[18px]">${item.icon}</span>
                </div>
                <div class="min-w-0">
                  <div class="flex items-center gap-1.5">
                    <span class="text-xs font-bold text-[#111827] truncate">${UI.esc(item.title)}</span>
                    ${UI.badge(item.group, 'neutral')}
                  </div>
                  <div class="text-[11px] text-[#596158] truncate">${UI.esc(item.desc)}</div>
                  <div class="text-[10px] font-mono text-[#3D6943] mt-0.5">${item.type} → ${item.route}</div>
                </div>
              </div>
              <button type="button" data-test-route="${item.route}"
                      class="px-2.5 py-1.5 rounded-[8px] bg-[#3D6943] text-white text-[11px] font-semibold shrink-0">
                Mở
              </button>
            </div>
          `).join('')}
        </div>

        <button type="button" id="btn-test-broken-notif"
                class="w-full py-2.5 rounded-[8px] border border-[#FECACA] bg-[#FEF2F2] text-[#A73B21] text-xs font-semibold">
          Thử thông báo đích bị xóa (NOTIF-02)
        </button>
      </div>
    `;

    container.querySelectorAll('[data-test-route]').forEach((btn) => {
      btn.addEventListener('click', () => {
        Router.navigate(btn.getAttribute('data-test-route'));
      });
    });
    container.querySelector('#btn-test-broken-notif')?.addEventListener('click', () => {
      Router.navigate('NOTIF-02', {
        code: '404',
        reason: 'Kèo đấu match-non-existent-99 đã bị hủy và xóa khỏi hệ thống.'
      });
    });
  }, {
    title: 'Catalog thông báo',
    showBack: true,
    group: 'Chat & Thông báo',
    aliases: ['notif-catalog']
  });

  global.ArenaChatNotif = { openChatReportSheet };
})(window);
