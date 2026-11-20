/**
 * Chat & Notification Views & Sheets (CHAT-01..05, NOTIF-01..03)
 * Owned by @ui-merc-chat
 * 
 * Strict compliance with:
 * - AC-P09: Inaccessible or expired targets handled gracefully with explanatory dialogs and return paths.
 * - CHAT-04: Chat states (read-only when canSend=false, reconnecting simulation, 90d retention notice).
 * - NOTIF-03: Catalog covering all 35 notification types with proper icon, preview, and route target.
 * - Journeys: J-17, J-18
 */

export function initChatNotifViews(router, store) {

  /* =========================================================================
     CHAT-01: Conversations List (route: 'inbox')
     ========================================================================= */
  router.registerView('inbox', (container, params) => {
    router.setHeaderTitle('Tin nhắn');

    let activeFilter = params.filter || 'ALL'; // 'ALL' | 'MATCH' | 'MERCENARY' | 'UNREAD'

    function renderView() {
      const state = store.getState();
      const currentUserId = state.currentUserId;
      const currentUser = store.getCurrentUser();

      // Find conversations involving current user
      // Also include captain who disbanded team per §1.7 & MATCH-001 §1.6
      const allConvs = state.conversations || [];
      const myConvs = allConvs.filter(c => {
        if (c.participantIds && c.participantIds.includes(currentUserId)) return true;
        // Check if user is captain of any team in the match or recruitment
        if (c.contextType === 'MERCENARY' || c.contextType === 'MERCENARY_APPLICATION') {
          const app = state.applications.find(a => a.id === c.contextId);
          if (app) {
            const rec = state.recruitments.find(r => r.id === app.recruitmentId);
            const team = state.teams.find(t => t.id === rec?.teamId);
            if (team?.captainId === currentUserId) return true;
          }
        }
        return false;
      });

      // Filter
      const filteredConvs = myConvs.filter(conv => {
        if (activeFilter === 'MATCH') {
          return conv.contextType === 'MATCH_REQUEST' || conv.contextType === 'PAIRING';
        }
        if (activeFilter === 'MERCENARY') {
          return conv.contextType === 'MERCENARY' || conv.contextType === 'MERCENARY_APPLICATION';
        }
        if (activeFilter === 'UNREAD') {
          const lastMsg = conv.messages?.[conv.messages.length - 1];
          return lastMsg && !lastMsg.readBy?.includes(currentUserId);
        }
        return true;
      });

      // Sort by last activity
      filteredConvs.sort((a, b) => {
        const timeA = a.messages?.[a.messages.length - 1]?.timestamp || a.createdAt || '';
        const timeB = b.messages?.[b.messages.length - 1]?.timestamp || b.createdAt || '';
        return new Date(timeB) - new Date(timeA);
      });

      container.innerHTML = `
        <div style="padding: 16px 0;">

          <!-- Retention notice banner per BR-N01 -->
          <div class="notice notice-info" style="font-size: 12px; margin-bottom: 12px; padding: 8px 12px;">
            💬 <strong>Lưu trữ tin nhắn (BR-N01):</strong> Các cuộc trò chuyện được lưu trữ trong 90 ngày sau khi trận đấu kết thúc.
          </div>

          <!-- Filter Chips -->
          <div style="margin-bottom: 16px;">
            <div class="chip-group">
              <button class="chip ${activeFilter === 'ALL' ? 'is-active' : ''}" data-filter="ALL">Tất cả (${myConvs.length})</button>
              <button class="chip ${activeFilter === 'MATCH' ? 'is-active' : ''}" data-filter="MATCH">Kèo đấu</button>
              <button class="chip ${activeFilter === 'MERCENARY' ? 'is-active' : ''}" data-filter="MERCENARY">Đá lẻ (Tuyển quân)</button>
              <button class="chip ${activeFilter === 'UNREAD' ? 'is-active' : ''}" data-filter="UNREAD">Chưa đọc</button>
            </div>
          </div>

          <!-- Conversations List -->
          ${filteredConvs.length === 0 ? `
            <div class="empty-state" style="padding: 40px 16px;">
              <div class="empty-state-icon">💬</div>
              <h3 class="empty-state-title">Chưa có tin nhắn nào</h3>
              <p class="empty-state-desc">
                ${activeFilter === 'UNREAD' 
                  ? 'Bạn đã đọc tất cả tin nhắn!' 
                  : 'Tin nhắn sẽ tự động xuất hiện khi bạn gửi yêu cầu ghép kèo hoặc ứng tuyển đá lẻ.'}
              </p>
            </div>
          ` : `
            <div class="list-group">
              ${filteredConvs.map(conv => renderConversationCard(conv, state, currentUserId)).join('')}
            </div>
          `}

        </div>
      `;

      _attachInboxEvents();
    }

    function renderConversationCard(conv, state, currentUserId) {
      const isMerc = conv.contextType === 'MERCENARY' || conv.contextType === 'MERCENARY_APPLICATION';
      let title = 'Cuộc trò chuyện';
      let subtitle = '';
      let badgeHtml = '';
      let avatarInitial = 'C';

      if (isMerc) {
        const app = state.applications.find(a => a.id === conv.contextId);
        const rec = state.recruitments.find(r => r.id === app?.recruitmentId);
        const team = state.teams.find(t => t.id === rec?.teamId);
        const isApplicant = app?.applicantId === currentUserId;

        if (isApplicant) {
          title = team ? team.name : 'Đội bóng';
          subtitle = `Đá lẻ: ${_formatPos(rec?.position)} · Đội trưởng`;
          badgeHtml = '<span class="badge badge-green">ĐÁ LẺ</span>';
          avatarInitial = title.slice(0, 2).toUpperCase();
        } else {
          const applicant = state.characters[app?.applicantId] || { displayName: 'Ứng viên' };
          title = applicant.displayName;
          subtitle = `Ứng viên vị trí: ${_formatPos(rec?.position)}`;
          badgeHtml = '<span class="badge badge-amber">ỨNG VIÊN</span>';
          avatarInitial = title.slice(0, 2).toUpperCase();
        }
      } else {
        // Match chat (Luồng A request hoặc Luồng B pairing)
        badgeHtml = '<span class="badge badge-blue">KÈO ĐẤU</span>';
        if (conv.contextType === 'MATCH_REQUEST') {
          const req = state.matchRequests.find(r => r.id === conv.contextId);
          const match = state.matches.find(m => m.id === req?.matchId);
          const otherTeam = state.teams.find(t => t.id === req?.requesterTeamId);
          title = otherTeam ? otherTeam.name : 'Đội đối phương';
          subtitle = `Kèo ${match?.matchDate || ''} (${match?.startTime || ''})`;
          avatarInitial = title.slice(0, 2).toUpperCase();
        } else if (conv.contextType === 'PAIRING') {
          const pairing = state.pairings.find(p => p.id === conv.contextId);
          const isHost = pairing?.hostTeamId === state.actingTeamId;
          const otherTeamId = isHost ? pairing?.guestTeamId : pairing?.hostTeamId;
          const otherTeam = state.teams.find(t => t.id === otherTeamId);
          title = otherTeam ? otherTeam.name : 'Thương lượng kèo B';
          subtitle = 'Thương lượng gói kèo (Pairing)';
          avatarInitial = title.slice(0, 2).toUpperCase();
        }
      }

      // Last message
      const messages = conv.messages || [];
      const lastMsg = messages.length > 0 ? messages[messages.length - 1] : null;
      const hasUnread = lastMsg && !lastMsg.readBy?.includes(currentUserId);
      const lastText = lastMsg ? (lastMsg.text.length > 60 ? lastMsg.text.slice(0, 60) + '...' : lastMsg.text) : 'Chưa có tin nhắn';
      const lastTime = lastMsg ? _formatTime(lastMsg.timestamp) : '';

      return `
        <article class="card card-interactive conv-item-card" data-conv-id="${conv.id}" style="${hasUnread ? 'background-color: #f4fbf5; border-color: var(--color-primary-light);' : ''}">
          <div style="display: flex; align-items: center; justify-content: space-between; gap: 12px;">
            <div style="display: flex; align-items: center; gap: 12px; flex: 1; min-width: 0;">
              <div class="avatar" style="position: relative;">
                ${avatarInitial}
                ${hasUnread ? `
                  <span style="position: absolute; top: -2px; right: -2px; width: 10px; height: 10px; background-color: var(--color-primary); border-radius: 50%; border: 2px solid white;"></span>
                ` : ''}
              </div>

              <div style="flex: 1; min-width: 0;">
                <div style="display: flex; align-items: center; justify-content: space-between;">
                  <h4 style="margin: 0; font-size: 15px; font-weight: ${hasUnread ? '700' : '600'}; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;">
                    ${title}
                  </h4>
                  <span style="font-size: 11px; color: var(--color-on-surface-variant); margin-left: 6px;">
                    ${lastTime}
                  </span>
                </div>

                <div style="display: flex; align-items: center; gap: 6px; margin: 2px 0;">
                  ${badgeHtml}
                  <span style="font-size: 12px; color: var(--color-on-surface-variant); white-space: nowrap; overflow: hidden; text-overflow: ellipsis;">
                    ${subtitle}
                  </span>
                </div>

                <div style="font-size: 13px; color: ${hasUnread ? 'var(--color-on-surface)' : 'var(--color-on-surface-variant)'}; font-weight: ${hasUnread ? '600' : 'normal'}; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; margin-top: 2px;">
                  ${lastMsg && lastMsg.senderId === currentUserId ? '<strong>Bạn:</strong> ' : ''}${lastText}
                </div>
              </div>
            </div>
          </div>
        </article>
      `;
    }

    function _attachInboxEvents() {
      container.querySelectorAll('.chip[data-filter]').forEach(btn => {
        btn.addEventListener('click', () => {
          activeFilter = btn.getAttribute('data-filter');
          renderView();
        });
      });

      container.querySelectorAll('.conv-item-card').forEach(card => {
        card.addEventListener('click', () => {
          const convId = card.getAttribute('data-conv-id');
          router.navigate('chat-room', { conversationId: convId });
        });
      });
    }

    renderView();
  });

  /* =========================================================================
     CHAT-02, CHAT-03, CHAT-04: Chat Room (route: 'chat-room')
     ========================================================================= */
  router.registerView('chat-room', (container, params) => {
    const convId = params.conversationId;
    const state = store.getState();
    const currentUserId = state.currentUserId;
    const currentUser = store.getCurrentUser();

    const conv = state.conversations.find(c => c.id === convId);

    if (!conv) {
      container.innerHTML = `
        <div class="empty-state" style="padding: 40px 16px;">
          <h3 class="empty-state-title">Cuộc trò chuyện không tồn tại</h3>
          <p class="empty-state-desc">Phòng chat này có thể đã hết hạn hoặc không tìm thấy.</p>
          <button class="btn btn-primary" onclick="window.AppRouter.navigate('inbox')">Quay lại Tin nhắn</button>
        </div>
      `;
      return;
    }

    // Mark messages as read by current user
    if (conv.messages) {
      let changed = false;
      conv.messages.forEach(m => {
        if (!m.readBy) m.readBy = [];
        if (!m.readBy.includes(currentUserId)) {
          m.readBy.push(currentUserId);
          changed = true;
        }
      });
      if (changed) store.setState({ conversations: [...state.conversations] });
    }

    // Determine chat metadata & context
    const isMerc = conv.contextType === 'MERCENARY' || conv.contextType === 'MERCENARY_APPLICATION';
    let roomTitle = 'Phòng chat';
    let roomSubtitle = '';
    let deepLinkTarget = null;
    let deepLinkLabel = '';
    let isPendingMerc = false;

    if (isMerc) {
      const app = state.applications.find(a => a.id === conv.contextId);
      const rec = state.recruitments.find(r => r.id === app?.recruitmentId);
      const team = state.teams.find(t => t.id === rec?.teamId);
      const isApplicant = app?.applicantId === currentUserId;
      isPendingMerc = app?.status === 'PENDING';

      if (isApplicant) {
        roomTitle = team ? team.name : 'Đội trưởng tuyển';
        roomSubtitle = `Tuyển đá lẻ ${_formatPos(rec?.position)} · ${app?.status || ''}`;
        deepLinkTarget = `application-detail?applicationId=${app?.id}`;
        deepLinkLabel = 'Xem đơn ứng tuyển →';
      } else {
        const applicant = state.characters[app?.applicantId] || { displayName: 'Ứng viên' };
        roomTitle = applicant.displayName;
        roomSubtitle = `Ứng tuyển ${_formatPos(rec?.position)} · ${app?.status || ''}`;
        deepLinkTarget = `candidate-detail?applicationId=${app?.id}`;
        deepLinkLabel = 'Hồ sơ ứng viên →';
      }
    } else if (conv.contextType === 'MATCH_REQUEST') {
      const req = state.matchRequests.find(r => r.id === conv.contextId);
      const match = state.matches.find(m => m.id === req?.matchId);
      const otherTeam = state.teams.find(t => t.id === req?.requesterTeamId);
      roomTitle = otherTeam ? otherTeam.name : 'Đội đối phương';
      roomSubtitle = `Kèo ngày ${match?.matchDate || ''}`;
      deepLinkTarget = `match-detail?matchId=${match?.id}`;
      deepLinkLabel = 'Chi tiết kèo →';
    } else if (conv.contextType === 'PAIRING') {
      const pairing = state.pairings.find(p => p.id === conv.contextId);
      roomTitle = 'Thương lượng gói kèo';
      roomSubtitle = 'Ghép kèo luồng B';
      deepLinkTarget = `pairing-detail?pairingId=${pairing?.id}`;
      deepLinkLabel = 'Xem gói thương lượng →';
    }

    router.setHeaderTitle(roomTitle);

    // CHAT-04 States: canSend check
    const canSend = conv.canSend !== false;

    let isSimulatingReconnect = false;

    function renderRoom() {
      const currentConv = store.getState().conversations.find(c => c.id === convId);
      const msgs = currentConv?.messages || [];

      container.innerHTML = `
        <div style="display: flex; flex-direction: column; min-height: 80vh; padding-bottom: 70px;">
          
          <!-- Chat Context Header Card -->
          <div style="background-color: var(--color-surface); border-bottom: 1px solid var(--color-outline-variant); padding: 10px 16px; margin: -16px -16px 12px -16px;">
            <div style="display: flex; justify-content: space-between; align-items: center;">
              <div>
                <div style="font-size: 14px; font-weight: 700;">${roomTitle}</div>
                <div style="font-size: 12px; color: var(--color-on-surface-variant);">${roomSubtitle}</div>
              </div>
              <div style="display: flex; gap: 8px;">
                ${deepLinkTarget ? `
                  <button class="btn btn-sm btn-ghost" id="btn-chat-deeplink" style="font-size: 12px; color: var(--color-primary);">
                    ${deepLinkLabel}
                  </button>
                ` : ''}
                <button class="btn btn-sm btn-ghost" id="btn-report-chat" title="Báo cáo vi phạm" style="font-size: 14px; padding: 4px 8px;">
                  🚩
                </button>
              </div>
            </div>

            <!-- AC-P05 Privacy Banner for Pending Mercenary -->
            ${isPendingMerc ? `
              <div class="notice notice-warning" style="font-size: 11px; margin: 8px 0 0 0; padding: 6px 10px;">
                🔒 <strong>Lưu ý bảo mật (AC-P05):</strong> Tên sân và địa chỉ chính xác được ẩn trong toàn bộ tin tuyển & đơn cho đến khi đơn được duyệt.
              </div>
            ` : ''}

            <!-- 90-day retention notice per CHAT-04 & BR-N01 -->
            <div style="font-size: 11px; color: var(--color-on-surface-variant); margin-top: 6px;">
              ⏱️ Tin nhắn được lưu trữ 90 ngày sau khi trận đấu kết thúc.
            </div>

            <!-- CHAT-04: Reconnecting Banner Simulation -->
            ${isSimulatingReconnect ? `
              <div class="notice notice-warning" style="margin: 8px 0 0 0; font-size: 12px; display: flex; align-items: center; justify-content: space-between;">
                <span>🔄 Đang kết nối lại kênh trò chuyện...</span>
                <button class="btn btn-sm btn-secondary" id="btn-sim-reconnect-toggle" style="padding: 2px 6px; font-size: 11px;">
                  Đã nối lại
                </button>
              </div>
            ` : ''}

            <!-- CHAT-04: Read-Only State notice if canSend === false -->
            ${!canSend ? `
              <div class="notice notice-danger" style="margin: 8px 0 0 0; font-size: 12px;">
                🔒 <strong>Cuộc trò chuyện ở chế độ chỉ đọc (CHAT-04):</strong> 
                Đơn ứng tuyển đã hoàn tất/rút lui hoặc đội đã giải tán. Bạn không thể gửi thêm tin nhắn.
              </div>
            ` : ''}
          </div>

          <!-- Messages Stream Container -->
          <div class="chat-container" id="chat-messages-stream" style="flex: 1; overflow-y: auto; padding: 8px 0;">
            ${msgs.length === 0 ? `
              <div style="text-align: center; color: var(--color-on-surface-variant); font-size: 13px; margin: 32px 0;">
                Bắt đầu trò chuyện để thỏa thuận chi tiết về trận đấu.
              </div>
            ` : `
              ${msgs.map(m => {
                const isMine = m.senderId === currentUserId;
                const sender = state.characters[m.senderId] || { displayName: 'Người dùng' };

                return `
                  <div style="display: flex; flex-direction: column; margin-bottom: 12px;">
                    <span style="font-size: 11px; color: var(--color-on-surface-variant); margin-bottom: 2px; ${isMine ? 'text-align: right;' : 'text-align: left;'}">
                      ${isMine ? 'Bạn' : sender.displayName}
                    </span>
                    <div class="chat-bubble ${isMine ? 'chat-bubble-mine' : 'chat-bubble-theirs'}">
                      <div>${m.text}</div>
                      <div class="chat-time">
                        ${_formatTime(m.timestamp)} ${isMine ? '· ✓✓' : ''}
                      </div>
                    </div>
                  </div>
                `;
              }).join('')}
            `}
          </div>

          <!-- Bottom Fixed Input Bar -->
          <div style="position: fixed; bottom: var(--app-bottom-nav-height, 56px); left: 0; right: 0; max-width: 430px; margin: 0 auto; background-color: var(--color-surface); border-top: 1px solid var(--color-outline-variant); padding: 8px 16px; display: flex; gap: 8px; align-items: center; z-index: 50;">
            <input 
              type="text" 
              id="input-chat-message" 
              class="form-input" 
              placeholder="${canSend ? 'Nhập tin nhắn (tối đa 1000 ký tự)...' : 'Cuộc trò chuyện đã khóa gửi tin'}" 
              maxlength="1000"
              style="flex: 1; min-height: 42px; border-radius: 20px; padding: 8px 16px;"
              ${!canSend ? 'disabled' : ''}
            >
            <button 
              class="btn btn-primary btn-icon" 
              id="btn-send-message" 
              ${!canSend ? 'disabled' : ''} 
              aria-label="Gửi tin"
              style="width: 42px; height: 42px; flex-shrink: 0;"
            >
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
                <line x1="22" y1="2" x2="11" y2="13"></line>
                <polygon points="22 2 15 22 11 13 2 9 22 2"></polygon>
              </svg>
            </button>
          </div>

        </div>
      `;

      // Scroll to bottom
      const stream = container.querySelector('#chat-messages-stream');
      if (stream) stream.scrollTop = stream.scrollHeight;

      _attachRoomEvents();
    }

    function _attachRoomEvents() {
      // Deeplink
      container.querySelector('#btn-chat-deeplink')?.addEventListener('click', () => {
        if (deepLinkTarget) {
          const [route, query] = deepLinkTarget.split('?');
          const p = {};
          if (query) {
            new URLSearchParams(query).forEach((v, k) => { p[k] = v; });
          }
          router.navigate(route, p);
        }
      });

      // Report chat (CHAT-05)
      container.querySelector('#btn-report-chat')?.addEventListener('click', () => {
        router.openSheet('chat-report-sheet', { conversationId: convId });
      });

      // Sim reconnect
      container.querySelector('#btn-sim-reconnect-toggle')?.addEventListener('click', () => {
        isSimulatingReconnect = !isSimulatingReconnect;
        renderRoom();
      });

      // Send Message handler
      const sendMsg = () => {
        const input = container.querySelector('#input-chat-message');
        const text = input?.value.trim();
        if (!text) return;

        try {
          store.sendMessage(convId, text);
          input.value = '';
          renderRoom();
        } catch (err) {
          router.showToast(err.message, 'error');
        }
      };

      container.querySelector('#btn-send-message')?.addEventListener('click', sendMsg);
      container.querySelector('#input-chat-message')?.addEventListener('keypress', (e) => {
        if (e.key === 'Enter') sendMsg();
      });
    }

    renderRoom();
  });

  /* =========================================================================
     CHAT-05: Report Chat Sheet (sheet: 'chat-report-sheet')
     ========================================================================= */
  router.registerSheet('chat-report-sheet', (body, footer, params, titleElem) => {
    titleElem.textContent = 'Báo cáo vi phạm giao tiếp';

    const state = store.getState();
    const conv = state.conversations.find(c => c.id === params.conversationId);

    body.innerHTML = `
      <div>
        <p style="font-size: 13px; color: var(--color-on-surface-variant); margin-bottom: 12px;">
          Nếu người chơi có hành vi lăng mạ, ngôn từ thô tục, quấy rối hoặc gian lận trong phòng chat, bạn có thể gửi báo cáo để Admin xử lý (CHAT-05).
        </p>

        <div class="form-group">
          <label class="form-label" for="report-reason-type">Lý do báo cáo</label>
          <select class="form-select" id="report-reason-type">
            <option value="BAD_COMMUNICATION">Ngôn từ xúc phạm, thiếu văn hóa</option>
            <option value="SPAM">Quấy rối, spam tin nhắn</option>
            <option value="FRAUD">Lừa đảo, thông tin sai lệch</option>
            <option value="OTHER">Hành vi phi thể thao khác</option>
          </select>
        </div>

        <div class="form-group">
          <label class="form-label" for="report-detail-text">Mô tả cụ thể vi phạm</label>
          <textarea 
            class="form-textarea" 
            id="report-detail-text" 
            placeholder="Nêu rõ tin nhắn vi phạm hoặc chi tiết hành vi..."
          ></textarea>
        </div>
      </div>
    `;

    footer.innerHTML = `
      <button class="btn btn-secondary" onclick="window.AppRouter.closeSheet()" style="flex: 1;">
        Hủy
      </button>
      <button class="btn btn-danger" id="btn-submit-chat-report" style="flex: 1;">
        Gửi báo cáo vi phạm
      </button>
    `;

    footer.querySelector('#btn-submit-chat-report')?.addEventListener('click', () => {
      const type = body.querySelector('#report-reason-type').value;
      const reason = body.querySelector('#report-detail-text').value;

      try {
        store.submitReport({
          reporterType: 'INDIVIDUAL',
          reporterId: state.currentUserId,
          reportedId: conv?.participantIds?.find(id => id !== state.currentUserId) || 'user-player',
          type,
          reason: reason || 'Báo cáo giao tiếp tệ trong phòng chat'
        });

        router.closeSheet();
        router.showToast('Đã gửi báo cáo vi phạm. Đội ngũ kiểm duyệt sẽ xử lý.', 'success');
      } catch (err) {
        router.showToast(err.message, 'error');
      }
    });
  });

  /* =========================================================================
     NOTIF-01: Notifications View & NOTIF-03 Catalog (route: 'notifications')
     ========================================================================= */
  router.registerView('notifications', (container, params) => {
    router.setHeaderTitle('Thông báo');

    let activeTab = params.tab || 'all'; // 'all' | 'unread' | 'catalog'

    function renderView() {
      const state = store.getState();
      const currentUserId = state.currentUserId;
      const allNotifs = state.notifications.filter(n => n.userId === currentUserId);
      const unreadCount = allNotifs.filter(n => !n.read).length;

      const displayedNotifs = activeTab === 'unread' 
        ? allNotifs.filter(n => !n.read) 
        : allNotifs;

      // Sort by newest first
      displayedNotifs.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));

      container.innerHTML = `
        <div style="padding: 16px 0;">

          <!-- Top Tabs & Mark All As Read -->
          <div style="display: flex; align-items: center; justify-content: space-between; margin-bottom: 16px; border-bottom: 1px solid var(--color-outline-variant); padding-bottom: 8px;">
            <div style="display: flex; gap: 8px;">
              <button class="btn btn-sm ${activeTab === 'all' ? 'btn-primary' : 'btn-ghost'}" id="notif-tab-all">
                Tất cả (${allNotifs.length})
              </button>
              <button class="btn btn-sm ${activeTab === 'unread' ? 'btn-primary' : 'btn-ghost'}" id="notif-tab-unread">
                Chưa đọc ${unreadCount > 0 ? `<span class="badge badge-amber" style="margin-left: 4px;">${unreadCount}</span>` : ''}
              </button>
              <button class="btn btn-sm ${activeTab === 'catalog' ? 'btn-primary' : 'btn-ghost'}" id="notif-tab-catalog">
                Catalog (35 loại)
              </button>
            </div>

            ${unreadCount > 0 && activeTab !== 'catalog' ? `
              <button class="btn btn-sm btn-ghost" id="btn-mark-all-read" style="font-size: 12px; color: var(--color-primary); white-space: nowrap;">
                Đọc tất cả ✓
              </button>
            ` : ''}
          </div>

          <!-- TAB CONTENT: CATALOG (NOTIF-03) -->
          ${activeTab === 'catalog' ? renderCatalogTab() : `
            
            <!-- Retention Notice per §1.1 -->
            <div style="font-size: 11px; color: var(--color-on-surface-variant); margin-bottom: 12px; padding: 0 4px;">
              ℹ️ Thông báo trong ứng dụng được lưu trữ trong vòng 30 ngày (BR-N02).
            </div>

            <!-- Notifications Stream -->
            ${displayedNotifs.length === 0 ? `
              <div class="empty-state" style="padding: 40px 16px;">
                <div class="empty-state-icon">🔔</div>
                <h3 class="empty-state-title">Không có thông báo nào</h3>
                <p class="empty-state-desc">
                  ${activeTab === 'unread' ? 'Bạn đã đọc toàn bộ thông báo!' : 'Các hoạt động và lời mời sẽ xuất hiện tại đây.'}
                </p>
              </div>
            ` : `
              <div class="list-group">
                ${displayedNotifs.map(n => renderNotificationItem(n)).join('')}
              </div>
            `}
          `}

        </div>
      `;

      _attachNotificationEvents();
    }

    function renderNotificationItem(n) {
      const typeInfo = _getNotificationTypeInfo(n.type);

      return `
        <article 
          class="card card-interactive notif-item-card" 
          data-notif-id="${n.id}"
          data-ref-type="${n.referenceType || ''}"
          data-ref-id="${n.referenceId || ''}"
          style="${!n.read ? 'background-color: #f7faf7; border-left: 4px solid var(--color-primary);' : ''}"
        >
          <div style="display: flex; gap: 12px; align-items: flex-start;">
            <div style="font-size: 24px; flex-shrink: 0; padding-top: 2px;">
              ${typeInfo.icon}
            </div>

            <div style="flex: 1; min-width: 0;">
              <div style="display: flex; align-items: center; justify-content: space-between;">
                <h4 style="margin: 0; font-size: 14px; font-weight: ${!n.read ? '700' : '600'}; color: var(--color-on-surface);">
                  ${n.title}
                </h4>
                ${!n.read ? `
                  <span class="badge badge-green" style="font-size: 10px; padding: 2px 6px;">Mới</span>
                ` : ''}
              </div>

              <!-- Body verbatim per §1.1 -->
              <p style="margin: 4px 0 6px 0; font-size: 13px; color: var(--color-on-surface-variant); line-height: 1.4;">
                ${n.body}
              </p>

              <div style="display: flex; align-items: center; justify-content: space-between; font-size: 11px; color: var(--color-outline);">
                <span>${_formatTime(n.createdAt)}</span>
                ${n.referenceType ? `
                  <span style="color: var(--color-primary); font-weight: 600;">Xem chi tiết →</span>
                ` : ''}
              </div>
            </div>
          </div>
        </article>
      `;
    }

    /* =======================================================================
       NOTIF-03: Catalog covering all 35 notification types per spec §4.2
       ======================================================================= */
    function renderCatalogTab() {
      const catalog = _get35NotificationCatalog();

      return `
        <div>
          <div class="notice notice-info" style="font-size: 12px; margin-bottom: 16px;">
            📚 <strong>Danh mục 35 loại thông báo hệ thống (NOTIF-03):</strong><br>
            Bao phủ 100% danh mục loại thông báo từ đặc tả <code>NOTIFICATION-001 §4.2</code>. Bạn có thể bấm "Tạo thử" để đưa mẫu thông báo vào danh sách và kiểm tra điều hướng AC-P09.
          </div>

          <div class="list-group">
            ${catalog.map((cat, idx) => `
              <div class="card" style="padding: 12px; margin-bottom: 10px;">
                <div style="display: flex; justify-content: space-between; align-items: flex-start; gap: 8px;">
                  <div style="display: flex; align-items: flex-start; gap: 10px;">
                    <span style="font-size: 24px;">${cat.icon}</span>
                    <div>
                      <div style="display: flex; align-items: center; gap: 6px;">
                        <span class="badge badge-gray" style="font-size: 10px;">#${idx + 1}</span>
                        <strong style="font-size: 13px;">${cat.type}</strong>
                      </div>
                      <div style="font-size: 13px; font-weight: 700; margin-top: 2px;">${cat.title}</div>
                      <div style="font-size: 12px; color: var(--color-on-surface-variant); margin-top: 2px; line-height: 1.4;">
                        ${cat.body}
                      </div>
                      <div style="font-size: 11px; color: var(--color-outline); margin-top: 4px;">
                        Đích liên kết: <code>${cat.referenceType || 'null (Tin tức)'}</code>
                      </div>
                    </div>
                  </div>

                  <button class="btn btn-sm btn-outline btn-trigger-catalog-item" data-idx="${idx}" style="font-size: 11px; padding: 4px 8px; flex-shrink: 0;">
                    + Tạo thử
                  </button>
                </div>
              </div>
            `).join('')}
          </div>
        </div>
      `;
    }

    function _attachNotificationEvents() {
      container.querySelector('#notif-tab-all')?.addEventListener('click', () => {
        activeTab = 'all';
        renderView();
      });
      container.querySelector('#notif-tab-unread')?.addEventListener('click', () => {
        activeTab = 'unread';
        renderView();
      });
      container.querySelector('#notif-tab-catalog')?.addEventListener('click', () => {
        activeTab = 'catalog';
        renderView();
      });

      container.querySelector('#btn-mark-all-read')?.addEventListener('click', () => {
        store.markAllNotificationsRead();
        router.showToast('Đã đánh dấu tất cả thông báo là đã đọc', 'success');
        renderView();
      });

      // Target resolver (NOTIF-02, AC-P09)
      container.querySelectorAll('.notif-item-card').forEach(card => {
        card.addEventListener('click', () => {
          const notifId = card.getAttribute('data-notif-id');
          const refType = card.getAttribute('data-ref-type');
          const refId = card.getAttribute('data-ref-id');

          // Mark this notification as read
          store.markNotificationRead(notifId);

          // Resolve target safely per NOTIF-02 & AC-P09
          _resolveNotificationTarget(refType, refId, router, store);
        });
      });

      // Catalog trigger buttons
      container.querySelectorAll('.btn-trigger-catalog-item').forEach(btn => {
        btn.addEventListener('click', () => {
          const idx = parseInt(btn.getAttribute('data-idx'), 10);
          const catalogItem = _get35NotificationCatalog()[idx];

          if (catalogItem) {
            store.setState(s => {
              s.notifications.unshift({
                id: 'notif-cat-' + Date.now(),
                userId: s.currentUserId,
                type: catalogItem.type,
                title: catalogItem.title,
                body: catalogItem.body,
                referenceType: catalogItem.referenceType,
                referenceId: catalogItem.referenceId || 'fixture-target',
                read: false,
                createdAt: s.meta.mockClock
              });
              return s;
            });

            router.showToast(`Đã tạo thông báo mẫu: ${catalogItem.type}`, 'success');
            activeTab = 'all';
            renderView();
          }
        });
      });
    }

    renderView();
  });

  /* =========================================================================
     NOTIF-02: Target Resolver (Safe navigation & AC-P09 graceful fallback)
     ========================================================================= */
  function _resolveNotificationTarget(refType, refId, router, store) {
    if (!refType || !refId) {
      router.showToast('Thông báo dạng tin tức — Không có trang đích liên kết.', 'info');
      return;
    }

    const state = store.getState();

    switch (refType) {
      case 'MATCH': {
        const match = state.matches.find(m => m.id === refId);
        if (match) {
          router.navigate('match-detail', { matchId: refId });
        } else {
          // AC-P09: Graceful fallback dialog instead of blank screen!
          _showInaccessibleTargetDialog(
            router, 
            'Kèo đấu không còn khả dụng', 
            `Kèo đấu (Mã: <code>${refId}</code>) không còn tồn tại trên hệ thống hoặc đã hết hạn lưu trữ (AC-P09).`,
            'matches',
            'Xem danh sách kèo'
          );
        }
        break;
      }

      case 'PAIRING': {
        const pairing = state.pairings.find(p => p.id === refId);
        if (pairing) {
          router.navigate('pairing-detail', { pairingId: refId });
        } else {
          _showInaccessibleTargetDialog(
            router, 
            'Thương lượng không khả dụng', 
            'Gói thương lượng này đã hoàn tất hoặc bị thay thế bởi lượt ghép khác (SUPERSEDED).',
            'matches',
            'Về tìm kèo'
          );
        }
        break;
      }

      case 'MERCENARY_APPLICATION': {
        const app = state.applications.find(a => a.id === refId);
        if (app) {
          // If captain, open applicant detail; if applicant, open my application detail
          const rec = state.recruitments.find(r => r.id === app.recruitmentId);
          const isCaptain = (store.getMyCaptainedTeams() || []).some(t => t.id === rec?.teamId);
          if (isCaptain) {
            router.navigate('candidate-detail', { applicationId: refId });
          } else {
            router.navigate('application-detail', { applicationId: refId });
          }
        } else {
          _showInaccessibleTargetDialog(
            router,
            'Đơn ứng tuyển không tồn tại',
            'Đơn ứng tuyển này đã bị xóa hoặc không còn quyền truy cập.',
            'players',
            'Về Đá lẻ'
          );
        }
        break;
      }

      case 'TEAM': {
        const team = state.teams.find(t => t.id === refId);
        if (team) {
          router.navigate('team-detail', { teamId: refId });
        } else {
          _showInaccessibleTargetDialog(
            router,
            'Đội bóng không tồn tại',
            'Đội bóng này có thể đã bị giải tán vĩnh viễn.',
            'teams',
            'Về Đội của tôi'
          );
        }
        break;
      }

      case 'TEAM_INVITATION': {
        // Navigate to team invitations response sheet / view
        router.navigate('team-invitations', { invitationId: refId });
        break;
      }

      case 'USER': {
        router.navigate('profile', { userId: refId });
        break;
      }

      case 'VENUE': {
        const venue = state.venues.find(v => v.id === refId);
        if (venue) {
          router.navigate('venue-detail', { venueId: refId });
        } else {
          _showInaccessibleTargetDialog(
            router,
            'Địa điểm không tồn tại',
            'Địa điểm sân bóng này không còn khả dụng.',
            'matches',
            'Quay lại'
          );
        }
        break;
      }

      case 'REPORT': {
        router.navigate('report-detail', { reportId: refId });
        break;
      }

      case 'DISPUTE':
      case 'MERCENARY_DISPUTE': {
        router.navigate('dispute-detail', { reportId: refId });
        break;
      }

      default: {
        router.showToast(`Điều hướng đến đối tượng ${refType} (${refId})`, 'info');
        break;
      }
    }
  }

  function _showInaccessibleTargetDialog(router, title, message, fallbackRoute, fallbackLabel) {
    router.openDialog('inaccessible-target-dialog');
    const dialogElem = document.getElementById('center-dialog');
    if (dialogElem) {
      dialogElem.innerHTML = `
        <div style="text-align: center; padding: 8px 0;">
          <div style="font-size: 36px; margin-bottom: 8px;">⚠️</div>
          <h3 class="dialog-title" style="font-size: 17px; margin-bottom: 6px;">${title}</h3>
          <p class="dialog-desc" style="font-size: 13px; margin-bottom: 16px;">
            ${message}
          </p>
          <div class="dialog-actions" style="display: flex; gap: 8px; justify-content: center;">
            <button class="btn btn-secondary btn-sm" onclick="window.AppRouter.closeDialog()">
              Đóng
            </button>
            <button class="btn btn-primary btn-sm" onclick="window.AppRouter.closeDialog(); window.AppRouter.navigate('${fallbackRoute}')">
              ${fallbackLabel}
            </button>
          </div>
        </div>
      `;
    }
  }

  // Register dialog handler for router
  router.registerDialog('inaccessible-target-dialog', (container, params) => {
    // Content rendered in _showInaccessibleTargetDialog
  });

  // Helper mappings
  function _getNotificationTypeInfo(type) {
    switch (type) {
      case 'WELCOME':
        return { icon: '👋', color: 'blue' };
      case 'TEAM_INVITATION':
      case 'TEAM_INVITATION_ACCEPTED':
      case 'TEAM_INVITATION_REJECTED':
      case 'CAPTAIN_TRANSFERRED':
        return { icon: '🛡️', color: 'green' };
      case 'TEAM_MARKED_INACTIVE':
      case 'TEAM_REACTIVATED':
        return { icon: '⚠️', color: 'amber' };
      case 'MATCH_REQUEST_RECEIVED':
      case 'REQUEST_ACCEPTED':
      case 'MATCH_CONFIRMED':
        return { icon: '⚽', color: 'green' };
      case 'REQUEST_REJECTED':
      case 'REQUEST_WITHDRAWN':
      case 'REQUEST_SUPERSEDED':
      case 'MATCH_CANCELLED':
      case 'MATCH_EXPIRED':
        return { icon: '❌', color: 'red' };
      case 'PAIRING_NEW':
      case 'PAIRING_SUPERSEDED':
      case 'PAIRING_EXPIRED':
        return { icon: '🤝', color: 'amber' };
      case 'REMINDER_BEFORE_MATCH':
      case 'MATCH_COMPLETED':
      case 'MERCENARY_RATING_REMINDER':
        return { icon: '⏰', color: 'amber' };
      case 'RATING_RECEIVED':
      case 'VERIFICATION_BADGE_GRANTED':
      case 'VERIFICATION_BADGE_REVOKED':
        return { icon: '⭐', color: 'green' };
      case 'REPORT_RECEIVED':
      case 'REPORT_DISPUTED':
      case 'DISPUTE_RESOLVED':
      case 'DISPUTE_CREATED':
        return { icon: '🚩', color: 'red' };
      case 'MERCENARY_APPLICATION':
      case 'MERCENARY_APPLICATION_APPROVED':
        return { icon: '🧤', color: 'green' };
      case 'MERCENARY_APPLICATION_REJECTED':
      case 'MERCENARY_WITHDRAWN':
      case 'MERCENARY_GAME_CANCELLED':
        return { icon: '🚫', color: 'red' };
      case 'VENUE_ADMIN_ALERT':
        return { icon: '🏟️', color: 'amber' };
      default:
        return { icon: '🔔', color: 'gray' };
    }
  }

  /* =========================================================================
     35 Notification Types Catalog definition per §4.2
     ========================================================================= */
  function _get35NotificationCatalog() {
    return [
      {
        type: 'WELCOME',
        title: 'Chào mừng bạn đến với ArenaFlow',
        body: 'Hoàn thiện hồ sơ để bắt đầu tìm đội và tìm kèo giao lưu.',
        referenceType: 'USER',
        referenceId: 'user-player',
        icon: '👋'
      },
      {
        type: 'TEAM_INVITATION',
        title: 'Lời mời gia nhập đội bóng',
        body: 'Đội trưởng Thảo Điền FC đã mời bạn gia nhập thành viên đội.',
        referenceType: 'TEAM_INVITATION',
        referenceId: 'inv-1',
        icon: '🛡️'
      },
      {
        type: 'TEAM_INVITATION_ACCEPTED',
        title: 'Lời mời đội được chấp thuận',
        body: 'Thành viên mới đã đồng ý gia nhập đội bóng của bạn.',
        referenceType: 'TEAM',
        referenceId: 'team-alpha',
        icon: '✅'
      },
      {
        type: 'TEAM_INVITATION_REJECTED',
        title: 'Lời mời đội bị từ chối',
        body: 'Người chơi đã từ chối lời mời gia nhập đội bóng.',
        referenceType: 'TEAM',
        referenceId: 'team-alpha',
        icon: '❌'
      },
      {
        type: 'CAPTAIN_TRANSFERRED',
        title: 'Bạn được chỉ định làm Đội trưởng mới',
        body: 'Đội trưởng cũ đã chuyển giao toàn quyền quản lý đội bóng cho bạn.',
        referenceType: 'TEAM',
        referenceId: 'team-alpha',
        icon: '👑'
      },
      {
        type: 'TEAM_MARKED_INACTIVE',
        title: 'Đội bóng bị tạm ngưng hoạt động',
        body: 'Ban quản trị đã tạm ngưng hoạt động của đội do phát hiện vi phạm.',
        referenceType: 'TEAM',
        referenceId: 'team-alpha',
        icon: '⚠️'
      },
      {
        type: 'TEAM_REACTIVATED',
        title: 'Đội bóng đã được kích hoạt lại',
        body: 'Đội bóng của bạn đã được mở lại sau khi giải quyết vi phạm.',
        referenceType: 'TEAM',
        referenceId: 'team-alpha',
        icon: '🟢'
      },
      {
        type: 'MATCH_REQUEST_RECEIVED',
        title: 'Yêu cầu ghép kèo mới',
        body: 'Đội FC Sấm Sét muốn ghép kèo 18:00 20/11 tại Sân bóng Phú Thọ.',
        referenceType: 'MATCH',
        referenceId: 'match-a-open',
        icon: '⚽'
      },
      {
        type: 'REQUEST_WITHDRAWN',
        title: 'Đội bạn đã rút yêu cầu ghép kèo',
        body: 'Đội đối phương đã tự rút yêu cầu ghép kèo đang chờ duyệt.',
        referenceType: 'MATCH',
        referenceId: 'match-a-open',
        icon: '↩️'
      },
      {
        type: 'REQUEST_ACCEPTED',
        title: 'Yêu cầu ghép kèo được chấp nhận',
        body: 'Đội Bình Minh FC đã chấp nhận yêu cầu ghép kèo của bạn!',
        referenceType: 'MATCH',
        referenceId: 'match-confirmed-1',
        icon: '🎉'
      },
      {
        type: 'REQUEST_REJECTED',
        title: 'Yêu cầu ghép kèo bị từ chối',
        body: 'Rất tiếc, đội chủ nhà đã từ chối yêu cầu ghép kèo này.',
        referenceType: 'MATCH',
        referenceId: 'match-a-open',
        icon: '🚫'
      },
      {
        type: 'REQUEST_SUPERSEDED',
        title: 'Kèo đấu đã được ghép với đội khác',
        body: 'Kèo đấu bạn gửi yêu cầu đã được chốt với một đội bóng khác.',
        referenceType: 'MATCH',
        referenceId: 'match-a-open',
        icon: '⏭️'
      },
      {
        type: 'REQUEST_EXPIRED',
        title: 'Yêu cầu ghép kèo đã hết hạn',
        body: 'Kèo đấu đã quá hạn bài đăng mà chưa được chấp thuận.',
        referenceType: 'MATCH',
        referenceId: 'match-a-open',
        icon: '⌛'
      },
      {
        type: 'PAIRING_NEW',
        title: 'Có đội nhận thương lượng kèo B',
        body: 'Một đội bóng đã nhận bài đăng tìm sân và mở thương lượng gói kèo.',
        referenceType: 'PAIRING',
        referenceId: 'pairing-beta-alpha',
        icon: '🤝'
      },
      {
        type: 'PAIRING_SUPERSEDED',
        title: 'Thương lượng đã bị thay thế',
        body: 'Kèo đã được chốt thành công với một đội bóng đối tác khác.',
        referenceType: 'MATCH',
        referenceId: 'match-b-open',
        icon: '⏭️'
      },
      {
        type: 'PAIRING_EXPIRED',
        title: 'Thương lượng kèo B đã hết hạn',
        body: 'Thời hạn thương lượng đã kết thúc trước khi hai bên cùng xác nhận.',
        referenceType: 'MATCH',
        referenceId: 'match-b-open',
        icon: '⌛'
      },
      {
        type: 'MATCH_CONFIRMED',
        title: 'Kèo đấu đã chính thức được chốt!',
        body: 'Hai bên đã cùng xác nhận gói thương lượng. Trận đấu đã sẵn sàng.',
        referenceType: 'MATCH',
        referenceId: 'match-confirmed-1',
        icon: '✅'
      },
      {
        type: 'MATCH_CANCELLED',
        title: 'Kèo đấu đã bị hủy',
        body: 'Đối thủ đã hủy trận đấu trước giờ bóng lăn theo quy định.',
        referenceType: 'MATCH',
        referenceId: 'match-confirmed-1',
        icon: '❌'
      },
      {
        type: 'MATCH_EXPIRED',
        title: 'Kèo đấu hết hạn bài đăng',
        body: 'Bài đăng tìm đối của đội đã hết hạn mà chưa ghép được đối thủ.',
        referenceType: 'MATCH',
        referenceId: 'match-a-open',
        icon: '⌛'
      },
      {
        type: 'REMINDER_BEFORE_MATCH',
        title: 'Nhắc nhở: Sắp đến giờ ra sân (2 giờ nữa)',
        body: 'Trận đấu sẽ diễn ra lúc 19:00 tại Sân bóng An Phú. Hãy chuẩn bị thể lực!',
        referenceType: 'MATCH',
        referenceId: 'match-confirmed-1',
        icon: '⏰'
      },
      {
        type: 'MATCH_COMPLETED',
        title: 'Trận đấu đã kết thúc — Hãy đánh giá đối thủ',
        body: 'Vui lòng dành 1 phút nhập tỷ số và đánh giá tinh thần thể thao đối phương.',
        referenceType: 'MATCH',
        referenceId: 'match-completed-1',
        icon: '📝'
      },
      {
        type: 'MERCENARY_RATING_REMINDER',
        title: 'Nhắc nhở: Đánh giá đội thuê đá lẻ',
        body: 'Trận đấu đá lẻ đã kết thúc. Hãy để lại đánh giá cho Đội trưởng và đội bóng.',
        referenceType: 'MERCENARY_APPLICATION',
        referenceId: 'app-player-rec2',
        icon: '⭐'
      },
      {
        type: 'RATING_RECEIVED',
        title: 'Bạn nhận được đánh giá mới',
        body: 'Đối phương đã gửi đánh giá và nhận xét về trận đấu vừa qua.',
        referenceType: 'MATCH',
        referenceId: 'match-completed-1',
        icon: '🌟'
      },
      {
        type: 'REPORT_RECEIVED',
        title: 'Đội của bạn bị báo cáo vi phạm',
        body: 'Bạn bị báo cáo bùng kèo (NOSHOW). Bạn có 24 giờ để gửi khiếu nại giải trình.',
        referenceType: 'REPORT',
        referenceId: 'rep-noshow-active',
        icon: '🚩'
      },
      {
        type: 'REPORT_DISPUTED',
        title: 'Đối phương đã gửi giải trình khiếu nại',
        body: 'Bên bị báo cáo đã gửi bằng chứng giải trình cho Ban quản trị xem xét.',
        referenceType: 'REPORT',
        referenceId: 'rep-noshow-active',
        icon: '⚖️'
      },
      {
        type: 'DISPUTE_RESOLVED',
        title: 'Khiếu nại tranh chấp đã có phán quyết',
        body: 'Ban quản trị đã đưa ra quyết định cuối cùng cho báo cáo tranh chấp.',
        referenceType: 'REPORT',
        referenceId: 'rep-noshow-expired',
        icon: '🔨'
      },
      {
        type: 'VERIFICATION_BADGE_GRANTED',
        title: 'Chúc mừng! Bạn đạt Huy hiệu Xác minh',
        body: 'Bạn đã đạt đủ tiêu chuẩn uy tín: >5 trận hoàn thành, điểm >=70, sao >3.0.',
        referenceType: 'USER',
        referenceId: 'user-player',
        icon: '🎖️'
      },
      {
        type: 'VERIFICATION_BADGE_REVOKED',
        title: 'Huy hiệu xác minh bị thu hồi',
        body: 'Do điểm uy tín hoặc tỷ lệ hoàn thành giảm xuống dưới ngưỡng tiêu chuẩn.',
        referenceType: 'USER',
        referenceId: 'user-player',
        icon: '📉'
      },
      {
        type: 'MERCENARY_APPLICATION',
        title: 'Có đơn ứng tuyển đá lẻ mới',
        body: 'Trần Văn Player đã nộp đơn ứng tuyển vị trí Thủ môn cho trận ngày 21/11.',
        referenceType: 'MERCENARY_APPLICATION',
        referenceId: 'app-player-rec1',
        icon: '🧤'
      },
      {
        type: 'MERCENARY_APPLICATION_APPROVED',
        title: 'Đơn ứng tuyển đã được phê duyệt!',
        body: 'Đội trưởng đã chấp nhận bạn vào đội hình. Tên sân & địa chỉ đã được mở khóa.',
        referenceType: 'MERCENARY_APPLICATION',
        referenceId: 'app-player-rec2',
        icon: '🎉'
      },
      {
        type: 'MERCENARY_APPLICATION_REJECTED',
        title: 'Đơn ứng tuyển chưa được chấp thuận',
        body: 'Đội trưởng đã từ chối đơn hoặc tin tuyển quân đã đủ người.',
        referenceType: 'MERCENARY_APPLICATION',
        referenceId: 'app-player-rec1',
        icon: '🚫'
      },
      {
        type: 'MERCENARY_WITHDRAWN',
        title: 'Mercenary đã rút lui khỏi trận',
        body: 'Một người chơi đã rút lui sau khi duyệt. Tin tuyển đã được mở lại.',
        referenceType: 'MERCENARY_APPLICATION',
        referenceId: 'app-player-rec2',
        icon: '↩️'
      },
      {
        type: 'MERCENARY_GAME_CANCELLED',
        title: 'Trận đấu đá lẻ đã bị hủy',
        body: 'Đội trưởng đã hủy trận giao lưu. Bạn không bị trừ điểm uy tín cá nhân.',
        referenceType: 'MERCENARY_APPLICATION',
        referenceId: 'app-player-rec2',
        icon: '❌'
      },
      {
        type: 'VENUE_ADMIN_ALERT',
        title: 'Cảnh báo địa điểm cần kiểm tra (Admin)',
        body: 'Sân bóng bị nhiều người báo cáo đóng cửa hoặc 180 ngày không được chọn.',
        referenceType: 'VENUE',
        referenceId: 'ven-inactive-1',
        icon: '🏟️'
      },
      {
        type: 'DISPUTE_CREATED',
        title: 'Có tranh chấp bùng kèo mới cần xử lý (Admin)',
        body: 'Đội bóng đã gửi khiếu nại giải trình bùng kèo, cần Admin phân xử trong hàng chờ.',
        referenceType: 'DISPUTE',
        referenceId: 'rep-noshow-active',
        icon: '⚖️'
      }
    ];
  }

  function _formatPos(pos) {
    switch (pos) {
      case 'GOALKEEPER': return 'Thủ môn';
      case 'DEFENDER': return 'Hậu vệ';
      case 'MIDFIELDER': return 'Tiền vệ';
      case 'FORWARD': return 'Tiền đạo';
      default: return pos || 'Cầu thủ';
    }
  }

  function _formatTime(iso) {
    if (!iso) return '';
    try {
      const d = new Date(iso);
      const hours = d.getHours().toString().padStart(2, '0');
      const mins = d.getMinutes().toString().padStart(2, '0');
      const day = d.getDate().toString().padStart(2, '0');
      const month = (d.getMonth() + 1).toString().padStart(2, '0');
      return `${hours}:${mins} ${day}/${month}`;
    } catch {
      return iso;
    }
  }
}
