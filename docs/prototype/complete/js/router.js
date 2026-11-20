/**
 * ArenaFlow Application Router & Modal Manager
 * Supports route navigation, deep-linking, back-stack history,
 * bottom-sheets, center dialogs, and active tab synchronization.
 */

class Router {
  constructor() {
    this.views = new Map();
    this.sheets = new Map();
    this.dialogs = new Map();
    this.history = [];
    this.currentRoute = null;
    this.currentParams = {};
    this.activeSheetId = null;
    this.activeDialogId = null;

    // Root tabs mapping
    this.tabMap = {
      'matches': 'nav-matches',
      'players': 'nav-players',
      'inbox': 'nav-inbox',
      'teams': 'nav-teams'
    };
  }

  init() {
    this.container = document.getElementById('view-canvas');
    this.headerTitle = document.getElementById('header-title');
    this.headerBackBtn = document.getElementById('header-back-btn');
    this.headerBrand = document.getElementById('header-brand');
    this.bottomNav = document.getElementById('app-bottom-nav');
    this.modalBackdrop = document.getElementById('modal-backdrop');
    this.bottomSheet = document.getElementById('bottom-sheet');
    this.bottomSheetTitle = document.getElementById('bottom-sheet-title');
    this.bottomSheetBody = document.getElementById('bottom-sheet-body');
    this.bottomSheetFooter = document.getElementById('bottom-sheet-footer');
    this.centerDialog = document.getElementById('center-dialog');
    this.toastContainer = document.getElementById('toast-container');

    // Setup global back button in header
    if (this.headerBackBtn) {
      this.headerBackBtn.addEventListener('click', () => this.goBack());
    }

    // Modal backdrop click to close
    if (this.modalBackdrop) {
      this.modalBackdrop.addEventListener('click', (e) => {
        if (e.target === this.modalBackdrop) {
          this.closeOverlay();
        }
      });
    }

    // Listen to hashchange
    window.addEventListener('hashchange', () => this._handleHashChange());

    // Listen to browser / phone back key
    window.addEventListener('popstate', (e) => {
      if (this.activeSheetId || this.activeDialogId) {
        this.closeOverlay();
      }
    });

    // Initial navigation
    if (window.location.hash) {
      this._handleHashChange();
    } else {
      this.navigate('matches', {}, true);
    }
  }

  /* -------------------------------------------------------------------------
     Registration APIs
     ------------------------------------------------------------------------- */
  registerView(id, handler) {
    this.views.set(id, handler);
  }

  registerSheet(id, handler) {
    this.sheets.set(id, handler);
  }

  registerDialog(id, handler) {
    this.dialogs.set(id, handler);
  }

  /* -------------------------------------------------------------------------
     Navigation
     ------------------------------------------------------------------------- */
  navigate(routeId, params = {}, replace = false) {
    this.closeOverlay();

    if (!replace && this.currentRoute) {
      this.history.push({ route: this.currentRoute, params: this.currentParams });
    }

    this.currentRoute = routeId;
    this.currentParams = params;

    // Update URL hash without triggering recursive hashchange
    const query = new URLSearchParams(params).toString();
    const hash = `#/${routeId}${query ? '?' + query : ''}`;
    if (window.location.hash !== hash) {
      window.history[replace ? 'replaceState' : 'pushState'](null, '', hash);
    }

    this._renderCurrentView();
  }

  goBack() {
    if (this.activeSheetId || this.activeDialogId) {
      this.closeOverlay();
      return;
    }

    if (this.history.length > 0) {
      const prev = this.history.pop();
      this.navigate(prev.route, prev.params, true);
    } else {
      // Default fallback to matches
      this.navigate('matches', {}, true);
    }
  }

  getRoute() {
    return {
      route: this.currentRoute,
      params: this.currentParams
    };
  }

  _handleHashChange() {
    const raw = window.location.hash.replace(/^#\/?/, '');
    if (!raw) {
      this.navigate('matches', {}, true);
      return;
    }

    const [routeId, queryString] = raw.split('?');
    const params = {};
    if (queryString) {
      const search = new URLSearchParams(queryString);
      for (const [key, value] of search.entries()) {
        params[key] = value;
      }
    }

    if (this.currentRoute !== routeId || JSON.stringify(this.currentParams) !== JSON.stringify(params)) {
      this.navigate(routeId, params, true);
    }
  }

  _renderCurrentView() {
    if (!this.container) return;

    // Close any previous content
    this.container.innerHTML = '';
    window.scrollTo({ top: 0, behavior: 'instant' });

    // Synchronize bottom nav
    this._syncBottomNav();

    // Check view registration
    const handler = this.views.get(this.currentRoute);
    if (handler) {
      try {
        handler(this.container, this.currentParams, this.currentRoute);
      } catch (err) {
        console.error(`Error rendering view ${this.currentRoute}:`, err);
        this._renderErrorFallback(`Lỗi hiển thị màn hình (${err.message})`);
      }
    } else {
      // Missing route fallback (AC-P09: no blank screens)
      this._renderMissingRouteFallback();
    }
  }

  _syncBottomNav() {
    if (!this.bottomNav) return;

    // Check if current route matches root tab
    const isRootTab = Object.keys(this.tabMap).includes(this.currentRoute);

    // Header Back button visibility: show back button on subviews
    if (this.headerBackBtn && this.headerBrand) {
      if (isRootTab) {
        this.headerBackBtn.style.display = 'none';
        this.headerBrand.style.display = 'flex';
      } else {
        this.headerBackBtn.style.display = 'flex';
        this.headerBrand.style.display = 'none';
      }
    }

    // Active tab highlight
    const tabItems = this.bottomNav.querySelectorAll('.nav-item');
    tabItems.forEach(item => {
      const tabTarget = item.getAttribute('data-tab');
      if (tabTarget === this.currentRoute) {
        item.classList.add('is-active');
      } else {
        item.classList.remove('is-active');
      }
    });
  }

  setHeaderTitle(title) {
    if (this.headerTitle) {
      this.headerTitle.textContent = title || '';
    }
  }

  _renderMissingRouteFallback() {
    this.setHeaderTitle('Không tìm thấy màn hình');
    this.container.innerHTML = `
      <div class="empty-state">
        <div class="empty-state-icon">
          <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="12" cy="12" r="10"></circle>
            <line x1="12" y1="8" x2="12" y2="12"></line>
            <line x1="12" y1="16" x2="12.01" y2="16"></line>
          </svg>
        </div>
        <h3 class="empty-state-title">Màn hình không khả dụng</h3>
        <p class="empty-state-desc">
          Đường dẫn <code>${this.currentRoute}</code> chưa được đăng ký hoặc bạn không có quyền truy cập.
        </p>
        <div style="margin-top: 16px;">
          <button class="btn btn-primary" onclick="window.AppRouter.navigate('matches')">
            Về trang tìm kèo
          </button>
        </div>
      </div>
    `;
  }

  _renderErrorFallback(message) {
    this.container.innerHTML = `
      <div class="notice notice-danger" style="margin: 20px;">
        <strong>Đã xảy ra sự cố:</strong>
        <p>${message}</p>
        <button class="btn btn-secondary btn-block" style="margin-top: 10px;" onclick="window.AppRouter.goBack()">
          Quay lại
        </button>
      </div>
    `;
  }

  /* -------------------------------------------------------------------------
     Modal Bottom Sheet & Center Dialog
     ------------------------------------------------------------------------- */
  openSheet(sheetId, params = {}) {
    if (!this.modalBackdrop || !this.bottomSheet) return;

    this.activeSheetId = sheetId;
    this.activeDialogId = null;

    if (this.centerDialog) this.centerDialog.style.display = 'none';
    this.bottomSheet.style.display = 'flex';

    this.bottomSheetTitle.textContent = '';
    this.bottomSheetBody.innerHTML = '';
    this.bottomSheetFooter.innerHTML = '';

    const handler = this.sheets.get(sheetId);
    if (handler) {
      try {
        handler(this.bottomSheetBody, this.bottomSheetFooter, params, this.bottomSheetTitle);
      } catch (err) {
        console.error(`Error rendering sheet ${sheetId}:`, err);
        this.bottomSheetBody.innerHTML = `<div class="notice notice-danger">Lỗi: ${err.message}</div>`;
      }
    } else {
      this.bottomSheetTitle.textContent = 'Thông báo';
      this.bottomSheetBody.innerHTML = `<p>Sheet <code>${sheetId}</code> chưa được xây dựng.</p>`;
    }

    this.modalBackdrop.classList.add('is-open');
  }

  closeSheet() {
    this.closeOverlay();
  }

  openDialog(dialogId, params = {}) {
    if (!this.modalBackdrop || !this.centerDialog) return;

    this.activeDialogId = dialogId;
    this.activeSheetId = null;

    if (this.bottomSheet) this.bottomSheet.style.display = 'none';
    this.centerDialog.style.display = 'block';
    this.centerDialog.innerHTML = '';

    const handler = this.dialogs.get(dialogId);
    if (handler) {
      try {
        handler(this.centerDialog, params);
      } catch (err) {
        console.error(`Error rendering dialog ${dialogId}:`, err);
        this.centerDialog.innerHTML = `<div class="notice notice-danger">Lỗi: ${err.message}</div>`;
      }
    } else {
      this.centerDialog.innerHTML = `
        <h3 class="dialog-title">Thông báo</h3>
        <p class="dialog-desc">Dialog <code>${dialogId}</code> chưa được cấu hình.</p>
        <div class="dialog-actions">
          <button class="btn btn-secondary" onclick="window.AppRouter.closeDialog()">Đóng</button>
        </div>
      `;
    }

    this.modalBackdrop.classList.add('is-open');
  }

  closeDialog() {
    this.closeOverlay();
  }

  closeOverlay() {
    if (!this.modalBackdrop) return;
    this.modalBackdrop.classList.remove('is-open');
    this.activeSheetId = null;
    this.activeDialogId = null;
  }

  /* -------------------------------------------------------------------------
     Toast Feedback
     ------------------------------------------------------------------------- */
  showToast(message, type = 'info', duration = 3500) {
    if (!this.toastContainer) return;

    const toast = document.createElement('div');
    toast.className = `toast ${type === 'error' ? 'toast-error' : type === 'success' ? 'toast-success' : ''}`;
    toast.innerHTML = `<span>${message}</span>`;

    this.toastContainer.appendChild(toast);

    setTimeout(() => {
      toast.style.opacity = '0';
      toast.style.transform = 'translateY(10px)';
      toast.style.transition = 'all 0.25s ease';
      setTimeout(() => toast.remove(), 250);
    }, duration);
  }
}

export const AppRouter = new Router();
window.AppRouter = AppRouter;
