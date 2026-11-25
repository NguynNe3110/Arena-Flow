/**
 * ArenaFlow Application Router (window.ArenaRouter)
 * Classic non-module script compatible with file:// protocol.
 * Supports route registry, history stack back(), active bottom-nav tab sync,
 * Top App Bar chrome control, and initial boot.
 */
(function (global) {
  'use strict';

  const TAB_ROUTES = ['MATCH-01', 'MERC-01', 'TEAM-01', 'CHAT-01', 'PROF-01'];

  class ArenaRouterImpl {
    constructor() {
      this.routes = new Map();
      this.aliases = new Map();
      this.history = [];
      this.currentRoute = null;
      this.currentParams = {};
      this._booted = false;
    }

    /**
     * Register a screen or sheet handler.
     * @param {string} routeId - e.g., 'MATCH-01', 'S1', 'TEAM-01'
     * @param {Function} renderFn - (container, params, routeId) => void | string
     * @param {Object} meta - { title, tab, chrome, group, aliases }
     */
    register(routeId, renderFn, meta) {
      const m = meta || {};
      this.routes.set(routeId, {
        id: routeId,
        renderFn: renderFn,
        meta: {
          title: m.title || routeId,
          tab: m.tab !== undefined ? m.tab : null,
          chrome: m.chrome !== false,
          showBack: !!m.showBack,
          group: m.group || 'Khác',
          role: m.role || null
        }
      });

      if (Array.isArray(m.aliases)) {
        m.aliases.forEach((alias) => {
          this.aliases.set(alias, routeId);
        });
      }
    }

    resolveId(routeId) {
      if (this.routes.has(routeId)) return routeId;
      if (this.aliases.has(routeId)) return this.aliases.get(routeId);
      return routeId;
    }

    getRoutes() {
      return Array.from(this.routes.values()).map((r) => ({
        id: r.id,
        title: r.meta.title,
        group: r.meta.group,
        tab: r.meta.tab
      }));
    }

    getCurrent() {
      return {
        routeId: this.currentRoute,
        params: this.currentParams
      };
    }

    navigate(routeId, params, options) {
      const opts = options || {};
      const resolved = this.resolveId(routeId);

      if (global.ArenaUI && typeof global.ArenaUI.closeSheet === 'function') {
        global.ArenaUI.closeSheet();
      }

      if (!opts.replace && this.currentRoute && (this.currentRoute !== resolved || JSON.stringify(this.currentParams) !== JSON.stringify(params || {}))) {
        this.history.push({
          routeId: this.currentRoute,
          params: Object.assign({}, this.currentParams)
        });
      }

      this.currentRoute = resolved;
      this.currentParams = params || {};

      // Sync URL hash safely on file:// and http://
      const query = new URLSearchParams(this.currentParams).toString();
      const targetHash = `#${resolved}${query ? '?' + query : ''}`;
      if (global.location.hash !== targetHash) {
        try {
          global.history[opts.replace ? 'replaceState' : 'pushState'](null, '', targetHash);
        } catch (e) {
          global.location.hash = targetHash;
        }
      }

      this.render();
    }

    go(routeId, params, options) {
      this.navigate(routeId, params, options);
    }

    back() {
      if (this.history.length > 0) {
        const prev = this.history.pop();
        this.navigate(prev.routeId, prev.params, { replace: true });
      } else {
        const user = global.ArenaStore && global.ArenaStore.getCurrentUser();
        this.navigate(user ? 'MATCH-01' : 'S1', {}, { replace: true });
      }
    }

    refresh() {
      this.render();
    }

    render() {
      const container = document.getElementById('screen-container');
      const topbarHost = document.getElementById('app-topbar-host');
      const bottomNavHost = document.getElementById('app-bottom-nav');
      if (!container) return;

      const entry = this.routes.get(this.currentRoute);
      const meta = entry ? entry.meta : { title: this.currentRoute, chrome: true, tab: null, showBack: true };

      // Check simulated global error
      const simErr = global.ArenaStore && global.ArenaStore.getSimulatedError();

      // Chrome visibility (Top Bar & Bottom Nav)
      const showChrome = meta.chrome !== false;
      if (topbarHost) {
        if (showChrome && global.ArenaUI) {
          const isRootTab = TAB_ROUTES.includes(this.currentRoute);
          topbarHost.classList.remove('hidden');
          topbarHost.innerHTML = global.ArenaUI.topBar({
            title: isRootTab ? '' : meta.title,
            showBack: !isRootTab && meta.showBack !== false
          });
        } else {
          topbarHost.classList.add('hidden');
          topbarHost.innerHTML = '';
        }
      }

      if (bottomNavHost) {
        if (showChrome) {
          bottomNavHost.classList.remove('hidden');
          this._syncBottomNav(meta.tab || (TAB_ROUTES.includes(this.currentRoute) ? this.currentRoute : null));
        } else {
          bottomNavHost.classList.add('hidden');
        }
      }

      // Render view
      container.innerHTML = '';
      container.scrollTop = 0;

      if (simErr && simErr !== 'none' && showChrome && global.ArenaUI) {
        const msgMap = {
          '403': 'Bạn không có quyền thực hiện thao tác hoặc truy cập màn hình này (403 Forbidden).',
          '404': 'Tài nguyên hoặc kèo đấu không còn tồn tại (404 Not Found).',
          '409': 'Xung đột dữ liệu: Phiên bản gói kèo đã thay đổi (409 Conflict).',
          'network': 'Mất kết nối mạng. Vui lòng kiểm tra đường truyền và thử lại.'
        };
        container.innerHTML = `<div class="p-4">${global.ArenaUI.errorState({
          title: 'Lỗi mô phỏng (' + simErr + ')',
          message: msgMap[simErr] || 'Đã xảy ra lỗi hệ thống.',
          code: simErr,
          onRetry: "window.ArenaStore.setSimulatedError('none');window.ArenaRouter.refresh();"
        })}</div>`;
        this._syncDemoPanel();
        return;
      }

      if (entry && typeof entry.renderFn === 'function') {
        try {
          const result = entry.renderFn(container, this.currentParams, this.currentRoute);
          if (typeof result === 'string') {
            container.innerHTML = result;
          }
        } catch (err) {
          console.error('Render error on route ' + this.currentRoute, err);
          if (global.ArenaUI) {
            container.innerHTML = `<div class="p-4">${global.ArenaUI.errorState({
              title: 'Lỗi hiển thị màn hình',
              message: err.message || 'Không thể dựng giao diện.'
            })}</div>`;
          }
        }
      } else {
        this._renderUnregisteredFallback(container);
      }

      this._syncDemoPanel();
    }

    _syncBottomNav(activeTab) {
      const nav = document.getElementById('app-bottom-nav');
      if (!nav) return;

      const unreadChat = global.ArenaStore ? global.ArenaStore.getUnreadChatCount() : 0;
      const chatBadge = document.getElementById('nav-badge-chat');
      if (chatBadge) {
        if (unreadChat > 0) {
          chatBadge.textContent = unreadChat > 9 ? '9+' : String(unreadChat);
          chatBadge.classList.remove('hidden');
        } else {
          chatBadge.classList.add('hidden');
        }
      }

      nav.querySelectorAll('[data-nav-tab]').forEach((btn) => {
        const tabId = btn.getAttribute('data-nav-tab');
        const isActive = tabId === activeTab;
        const iconEl = btn.querySelector('.material-symbols-outlined');
        if (isActive) {
          btn.classList.add('text-[#3D6943]', 'font-bold');
          btn.classList.remove('text-[#596158]', 'font-medium');
          if (iconEl) iconEl.style.fontVariationSettings = "'FILL' 1, 'wght' 500, 'GRAD' 0, 'opsz' 24";
        } else {
          btn.classList.remove('text-[#3D6943]', 'font-bold');
          btn.classList.add('text-[#596158]', 'font-medium');
          if (iconEl) iconEl.style.fontVariationSettings = "'FILL' 0, 'wght' 400, 'GRAD' 0, 'opsz' 24";
        }
      });
    }

    _syncDemoPanel() {
      const indicator = document.getElementById('demo-current-route');
      if (indicator) {
        indicator.textContent = this.currentRoute || '—';
      }
      if (typeof global.syncDemoPanelUI === 'function') {
        global.syncDemoPanelUI();
      }
    }

    _renderUnregisteredFallback(container) {
      if (!global.ArenaUI) {
        container.innerHTML = `<div class="p-6 text-center text-sm">Route "${this.currentRoute}" chưa được đăng ký.</div>`;
        return;
      }
      container.innerHTML = `<div class="p-4">${global.ArenaUI.emptyState({
        icon: 'construction',
        title: `Màn hình ${this.currentRoute}`,
        description: 'Module màn hình này chưa được nạp hoặc chưa đăng ký với ArenaRouter.',
        actionLabel: 'Về màn hình chính',
        onAction: "window.ArenaRouter.navigate('APP-01')"
      })}</div>`;
    }

    _parseHash() {
      const raw = (global.location.hash || '').replace(/^#\/?/, '');
      if (!raw) return null;
      const [routePart, queryPart] = raw.split('?');
      const params = {};
      if (queryPart) {
        new URLSearchParams(queryPart).forEach((v, k) => {
          params[k] = v;
        });
      }
      return { routeId: routePart, params: params };
    }

    boot() {
      if (this._booted) return;
      this._booted = true;

      if (global.ArenaStore) {
        global.ArenaStore.subscribe(() => {
          this.render();
        });
      }

      global.addEventListener('hashchange', () => {
        const parsed = this._parseHash();
        if (parsed && parsed.routeId) {
          const resolved = this.resolveId(parsed.routeId);
          if (resolved !== this.currentRoute || JSON.stringify(parsed.params) !== JSON.stringify(this.currentParams)) {
            this.navigate(resolved, parsed.params, { replace: true });
          }
        }
      });

      const parsed = this._parseHash();
      if (parsed && parsed.routeId) {
        this.navigate(parsed.routeId, parsed.params, { replace: true });
        return;
      }

      // Initial boot priority: APP-01 -> S1 -> MATCH-01
      const user = global.ArenaStore && global.ArenaStore.getCurrentUser();
      if (this.routes.has('APP-01')) {
        this.navigate('APP-01', {}, { replace: true });
      } else if (user && this.routes.has('MATCH-01')) {
        this.navigate('MATCH-01', {}, { replace: true });
      } else if (this.routes.has('S1')) {
        this.navigate('S1', {}, { replace: true });
      } else {
        const first = this.routes.keys().next().value;
        this.navigate(first || 'APP-01', {}, { replace: true });
      }
    }
  }

  global.ArenaRouter = new ArenaRouterImpl();

  document.addEventListener('DOMContentLoaded', function () {
    global.ArenaRouter.boot();
  });
})(window);
