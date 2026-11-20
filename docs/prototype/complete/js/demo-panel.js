/**
 * ArenaFlow Demo Controller Panel
 * Floating / dockable workbench for switching characters, mock clock,
 * simulating GPS, injecting errors, triggering counter-party actions, and resetting scenarios.
 */

import { AppStore } from './store.js';
import { AppRouter } from './router.js';

class DemoPanel {
  constructor() {
    this.panelContainer = null;
    this.fabBtn = null;
    this.isCollapsed = false;
  }

  init() {
    this.panelContainer = document.getElementById('demo-panel-container');
    this.fabBtn = document.getElementById('demo-panel-fab');
    this.frame = document.getElementById('mobile-frame');

    if (!this.panelContainer) return;

    this.render();
    this.bindEvents();

    // Re-render when store updates
    AppStore.subscribe(() => this.updateFormValues());
  }

  render() {
    const state = AppStore.getState();
    const characters = state.characters;
    const currentUserId = state.currentUserId;
    const currentUser = state.characters[currentUserId];
    const captainedTeams = AppStore.getMyCaptainedTeams();

    this.panelContainer.innerHTML = `
      <div class="demo-panel-header">
        <h3 class="demo-panel-title">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M14.7 6.3a1 1 0 0 0 0 1.4l1.6 1.6a1 1 0 0 0 1.4 0l3.77-3.77a6 6 0 0 1-7.94 7.94l-6.91 6.91a2.12 2.12 0 0 1-3-3l6.91-6.91a6 6 0 0 1 7.94-7.94l-3.76 3.76z"/>
          </svg>
          Demo Control Center
        </h3>
        <button class="btn btn-ghost" id="demo-collapse-btn" style="padding: 4px; min-height: 32px;" title="Thu nhỏ panel">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polyline points="4 14 10 14 10 20"></polyline>
            <polyline points="20 10 14 10 14 4"></polyline>
            <line x1="14" y1="10" x2="21" y2="3"></line>
            <line x1="3" y1="21" x2="10" y2="14"></line>
          </svg>
        </button>
      </div>

      <div class="demo-panel-body">
        <!-- 1. Nhân vật & Quyền -->
        <div class="demo-section">
          <div class="demo-section-title">
            <span>👤 Nhân vật & Vai trò</span>
            <span class="badge badge-green" id="demo-role-badge">${currentUser ? currentUser.role : 'USER'}</span>
          </div>

          <div class="demo-field">
            <label class="demo-field-label">Chọn nhân vật mô phỏng:</label>
            <select class="demo-select" id="demo-user-select">
              ${Object.values(characters).map(c => `
                <option value="${c.id}" ${c.id === currentUserId ? 'selected' : ''}>
                  ${c.label || c.displayName || c.id}
                </option>
              `).join('')}
            </select>
          </div>

          ${captainedTeams.length > 1 ? `
            <div class="demo-field" id="demo-acting-team-field">
              <label class="demo-field-label">Đội đang thao tác (Captain nhiều đội):</label>
              <select class="demo-select" id="demo-acting-team-select">
                ${captainedTeams.map(t => `
                  <option value="${t.id}" ${t.id === state.actingTeamId ? 'selected' : ''}>${t.name}</option>
                `).join('')}
              </select>
            </div>
          ` : ''}

          <div style="font-size: 11px; color: #a2b2a6; margin-top: 6px;">
            Đang đóng vai: <strong>${currentUser ? currentUser.displayName || 'Chưa đặt tên' : 'N/A'}</strong>
            ${currentUser?.isDeleted ? '<span style="color:#f87171;"> (Tài khoản đã xóa)</span>' : ''}
          </div>
        </div>

        <!-- 2. Kịch bản Preset -->
        <div class="demo-section">
          <div class="demo-section-title">
            <span>⚡ Kịch bản kiểm thử (Scenarios)</span>
          </div>
          <div class="demo-field">
            <select class="demo-select" id="demo-scenario-select">
              <option value="default">Mặc định (Default Explore)</option>
              <option value="auth_new_user">J-01: User mới & Onboarding</option>
              <option value="team_transfer_disband">J-05/06: Đội, chuyển quyền & Chặn xóa</option>
              <option value="match_a_flow">J-07/08: Kèo A & Ghép đội</option>
              <option value="pairing_b_flow">J-09/10: Kèo B & Xung đột thương lượng</option>
              <option value="mercenary_privacy">J-12/13: Đá lẻ & Bảo mật sân</option>
              <option value="noshow_dispute">J-21: Khiếu nại NOSHOW 24h</option>
              <option value="admin_moderation">J-22: Quản trị hệ thống (Admin)</option>
            </select>
          </div>
          <button class="demo-btn demo-btn-danger" id="demo-reset-btn" style="width: 100%; margin-top: 6px;">
            🔄 Reset toàn bộ dữ liệu mẫu
          </button>
        </div>

        <!-- 3. Đồng hồ mô phỏng -->
        <div class="demo-section">
          <div class="demo-section-title">
            <span>⏱ Đồng hồ mô phỏng (Mock Clock)</span>
          </div>
          <div class="demo-field">
            <input type="text" class="demo-input" id="demo-clock-input" value="${state.meta.mockClock}" readonly>
          </div>
          <div class="demo-btn-group" style="grid-template-columns: 1fr 1fr; margin-top: 6px;">
            <button class="demo-btn" data-clock="2026-11-18T10:00:00Z">Hiện tại (18/11)</button>
            <button class="demo-btn" data-clock="2026-11-21T18:30:00Z">Sát giờ đá (-30m)</button>
            <button class="demo-btn" data-clock="2026-11-21T19:30:00Z">Đang diễn ra</button>
            <button class="demo-btn" data-clock="2026-11-21T22:30:00Z">Sau trận (+2h)</button>
            <button class="demo-btn" data-clock="2026-11-22T23:30:00Z">Hết hạn khiếu nại</button>
            <button class="demo-btn" data-clock="2027-03-01T10:00:00Z">Hết retention (90d)</button>
          </div>
        </div>

        <!-- 4. Giả lập đối phương (Counter-party Actions) -->
        <div class="demo-section">
          <div class="demo-section-title">
            <span>🤝 Giả lập thao tác đối phương</span>
          </div>
          <div style="display: flex; flex-direction: column; gap: 6px;">
            <button class="demo-btn" id="demo-action-other-req">
              Đội khác gửi yêu cầu cho Kèo A
            </button>
            <button class="demo-btn" id="demo-action-other-edit-pack">
              Đội khác sửa gói B (tăng Revision)
            </button>
            <button class="demo-btn" id="demo-action-other-confirm-b">
              Đội khác xác nhận gói B
            </button>
            <button class="demo-btn" id="demo-action-other-superseded">
              Đội Gamma chốt kèo (Beta bị Superseded)
            </button>
            <button class="demo-btn" id="demo-action-new-notif">
              Bắn thông báo in-app mới
            </button>
          </div>
        </div>

        <!-- 5. GPS & Lỗi giả lập -->
        <div class="demo-section">
          <div class="demo-section-title">
            <span>📍 GPS & Giả lập lỗi</span>
          </div>
          <div class="demo-field">
            <label class="demo-field-label">Trạng thái định vị GPS:</label>
            <select class="demo-select" id="demo-gps-select">
              <option value="granted" ${state.meta.gpsStatus === 'granted' ? 'selected' : ''}>Đã cấp quyền (GPS 3km)</option>
              <option value="denied" ${state.meta.gpsStatus === 'denied' ? 'selected' : ''}>Từ chối quyền (Fallback hồ sơ)</option>
              <option value="prompt" ${state.meta.gpsStatus === 'prompt' ? 'selected' : ''}>Chưa hỏi quyền (Hỏi theo ngữ cảnh)</option>
            </select>
          </div>

          <div class="demo-field">
            <label class="demo-field-label">Lần thao tác tới báo lỗi:</label>
            <select class="demo-select" id="demo-error-select">
              <option value="none">Không có lỗi (Bình thường)</option>
              <option value="403">403 Cấm quyền truy cập (Forbidden)</option>
              <option value="404">404 Đối tượng không tìm thấy</option>
              <option value="409">409 Xung đột dữ liệu (Conflict/Revision)</option>
              <option value="network">Lỗi mạng / Mất kết nối (Offline)</option>
            </select>
          </div>
        </div>

        <!-- 6. Kích thước khung nhìn (Viewport) -->
        <div class="demo-section">
          <div class="demo-section-title">
            <span>📱 Kích thước màn hình</span>
          </div>
          <div class="demo-btn-group" style="grid-template-columns: 1fr 1fr 1fr;">
            <button class="demo-btn" data-viewport="360">360 px</button>
            <button class="demo-btn is-active" data-viewport="390">390 px</button>
            <button class="demo-btn" data-viewport="430">430 px</button>
          </div>
        </div>
      </div>
    `;
  }

  bindEvents() {
    // Collapse toggle
    const collapseBtn = document.getElementById('demo-collapse-btn');
    if (collapseBtn) {
      collapseBtn.addEventListener('click', () => this.toggleCollapse(true));
    }

    if (this.fabBtn) {
      this.fabBtn.addEventListener('click', () => this.toggleCollapse(false));
    }

    // Character Switcher
    const userSelect = document.getElementById('demo-user-select');
    if (userSelect) {
      userSelect.addEventListener('change', (e) => {
        AppStore.switchUser(e.target.value);
        AppRouter.showToast(`Đã chuyển sang: ${e.target.options[e.target.selectedIndex].text}`, 'info');
      });
    }

    // Acting team switcher
    const teamSelect = document.getElementById('demo-acting-team-select');
    if (teamSelect) {
      teamSelect.addEventListener('change', (e) => {
        AppStore.setActingTeam(e.target.value);
      });
    }

    // Scenario switcher
    const scenarioSelect = document.getElementById('demo-scenario-select');
    if (scenarioSelect) {
      scenarioSelect.addEventListener('change', (e) => {
        AppStore.loadScenario(e.target.value);
        AppRouter.showToast(`Đã nạp kịch bản: ${e.target.options[e.target.selectedIndex].text}`, 'success');
      });
    }

    // Reset All
    const resetBtn = document.getElementById('demo-reset-btn');
    if (resetBtn) {
      resetBtn.addEventListener('click', () => {
        if (confirm('Khôi phục toàn bộ dữ liệu mẫu về trạng thái gốc ban đầu?')) {
          AppStore.resetAll();
          AppRouter.navigate('matches', {}, true);
          AppRouter.showToast('Đã làm mới toàn bộ dữ liệu!', 'success');
        }
      });
    }

    // Clock presets
    this.panelContainer.querySelectorAll('[data-clock]').forEach(btn => {
      btn.addEventListener('click', (e) => {
        const time = e.currentTarget.getAttribute('data-clock');
        AppStore.setClock(time);
        document.getElementById('demo-clock-input').value = time;
        AppRouter.showToast(`Đồng hồ: ${time}`, 'info');
      });
    });

    // GPS status
    const gpsSelect = document.getElementById('demo-gps-select');
    if (gpsSelect) {
      gpsSelect.addEventListener('change', (e) => {
        AppStore.setGpsStatus(e.target.value);
        AppRouter.showToast(`Trạng thái GPS: ${e.target.value}`, 'info');
      });
    }

    // Simulated error
    const errorSelect = document.getElementById('demo-error-select');
    if (errorSelect) {
      errorSelect.addEventListener('change', (e) => {
        AppStore.setSimulatedError(e.target.value);
      });
    }

    // Viewport switcher
    this.panelContainer.querySelectorAll('[data-viewport]').forEach(btn => {
      btn.addEventListener('click', (e) => {
        this.panelContainer.querySelectorAll('[data-viewport]').forEach(b => b.classList.remove('is-active'));
        e.currentTarget.classList.add('is-active');
        const size = e.currentTarget.getAttribute('data-viewport');
        if (this.frame) {
          this.frame.setAttribute('data-viewport', size);
          AppRouter.showToast(`Khung nhìn: ${size}px`, 'info');
        }
      });
    });

    // Simulated Counter-party actions
    const btnOtherReq = document.getElementById('demo-action-other-req');
    if (btnOtherReq) {
      btnOtherReq.addEventListener('click', () => {
        AppStore.sendMatchRequest('match-a-open', 'team-gamma', 'Thảo Điền FC gửi yêu cầu giao hữu thêm!');
        AppRouter.showToast('Mô phỏng: Thảo Điền FC vừa gửi yêu cầu cho kèo A', 'info');
      });
    }

    const btnOtherEdit = document.getElementById('demo-action-other-edit-pack');
    if (btnOtherEdit) {
      btnOtherEdit.addEventListener('click', () => {
        AppStore.updatePairingPackage('pairing-beta-alpha', {
          timeSlot: '18:00 - 19:30',
          depositAmount: 300000
        });
        AppRouter.showToast('Mô phỏng: Bên kia vừa đổi giờ và tiền cọc. Xác nhận cũ bị hủy!', 'info');
      });
    }

    const btnOtherConfirm = document.getElementById('demo-action-other-confirm-b');
    if (btnOtherConfirm) {
      btnOtherConfirm.addEventListener('click', () => {
        const pairing = AppStore.getState().pairings.find(p => p.id === 'pairing-beta-alpha');
        if (pairing) {
          AppStore.confirmPairingPackage('pairing-beta-alpha', 'team-beta');
          AppRouter.showToast('Mô phỏng: Sài Gòn United đã bấm xác nhận gói!', 'success');
        }
      });
    }

    const btnOtherSuperseded = document.getElementById('demo-action-other-superseded');
    if (btnOtherSuperseded) {
      btnOtherSuperseded.addEventListener('click', () => {
        AppStore.confirmPairingPackage('pairing-beta-gamma', 'team-beta');
        AppStore.confirmPairingPackage('pairing-beta-gamma', 'team-gamma');
        AppRouter.showToast('Mô phỏng: Đội Gamma đã chốt kèo! Thương lượng của bạn chuyển sang SUPERSEDED.', 'info');
      });
    }

    const btnNewNotif = document.getElementById('demo-action-new-notif');
    if (btnNewNotif) {
      btnNewNotif.addEventListener('click', () => {
        const notif = {
          id: 'notif-' + Date.now(),
          userId: AppStore.getState().currentUserId,
          type: 'MATCH_REQUEST_RECEIVED',
          title: 'Thông báo thử nghiệm',
          body: 'Bạn có một tin nhắn hoặc yêu cầu mới.',
          referenceType: 'MATCH_REQUEST',
          referenceId: 'req-beta-for-a',
          read: false,
          createdAt: AppStore.getState().meta.mockClock
        };
        AppStore.setState(s => ({
          ...s,
          notifications: [notif, ...s.notifications]
        }));
        AppRouter.showToast('Đã thêm 1 thông báo mới vào hộp thư!', 'info');
      });
    }
  }

  toggleCollapse(collapse) {
    this.isCollapsed = collapse;
    if (collapse) {
      this.panelContainer.classList.add('is-collapsed');
      if (this.fabBtn) this.fabBtn.style.display = 'flex';
    } else {
      this.panelContainer.classList.remove('is-collapsed');
      if (this.fabBtn) this.fabBtn.style.display = 'none';
    }
  }

  updateFormValues() {
    const state = AppStore.getState();
    const userSelect = document.getElementById('demo-user-select');
    if (userSelect && userSelect.value !== state.currentUserId) {
      userSelect.value = state.currentUserId;
    }

    const currentUser = state.characters[state.currentUserId];
    const roleBadge = document.getElementById('demo-role-badge');
    if (roleBadge && currentUser) {
      roleBadge.textContent = currentUser.role;
    }

    const clockInput = document.getElementById('demo-clock-input');
    if (clockInput) {
      clockInput.value = state.meta.mockClock;
    }
  }
}

export const AppDemoPanel = new DemoPanel();
window.AppDemoPanel = AppDemoPanel;
