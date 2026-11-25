/**
 * ArenaFlow Shared UI Components (window.ArenaUI)
 * Classic non-module script compatible with file:// protocol.
 * Design tokens: #14532D Deep Pitch Green, #3D6943 Primary, #F8FAF3 Surface, Be Vietnam Pro, Material Symbols Outlined.
 */
(function (global) {
  'use strict';

  const AVATAR_PALETTE = [
    { bg: '#14532D', fg: '#FFFFFF' },
    { bg: '#3D6943', fg: '#FFFFFF' },
    { bg: '#2D5A36', fg: '#E9FFE6' },
    { bg: '#5C6330', fg: '#F6FFBC' },
    { bg: '#465846', fg: '#EAFFE7' },
    { bg: '#1E3A2F', fg: '#BDEFBF' }
  ];

  function esc(str) {
    if (str === null || str === undefined) return '';
    return String(str)
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#39;');
  }

  function getInitials(name) {
    const clean = String(name || 'AF').trim();
    if (!clean) return 'AF';
    const parts = clean.split(/\s+/).filter(Boolean);
    if (parts.length === 1) return parts[0].slice(0, 2).toUpperCase();
    return (parts[0][0] + parts[parts.length - 1][0]).toUpperCase();
  }

  function hashColor(seed) {
    const s = String(seed || 'AF');
    let h = 0;
    for (let i = 0; i < s.length; i++) {
      h = (h * 31 + s.charCodeAt(i)) >>> 0;
    }
    return AVATAR_PALETTE[h % AVATAR_PALETTE.length];
  }

  /**
   * Avatar component with SVG/initials fallback. Never emits external lh3.googleusercontent.com URLs.
   */
  function avatar(entity, opts) {
    const o = opts || {};
    const size = o.size || 40;
    const rounded = o.rounded || 'rounded-full';
    const className = o.className || '';
    const name = typeof entity === 'string'
      ? entity
      : (entity && (entity.shortName || entity.displayName || entity.name)) || 'AF';
    const initials = getInitials(name);
    const palette = hashColor(name);
    const url = entity && typeof entity === 'object' ? (entity.avatarUrl || entity.logoUrl) : null;
    const isSafeLocal = url && !url.includes('lh3.googleusercontent.com') && !url.startsWith('http');
    const fontSize = Math.max(11, Math.round(size * 0.36));

    if (isSafeLocal) {
      return `
        <div class="relative inline-flex items-center justify-center ${rounded} overflow-hidden shrink-0 border border-[#DDE5D9] ${className}"
             style="width:${size}px;height:${size}px;background:${palette.bg};color:${palette.fg};">
          <img src="${esc(url)}" alt="${esc(name)}" class="w-full h-full object-cover"
               onerror="this.style.display='none';this.nextElementSibling.style.display='flex';" />
          <span class="hidden w-full h-full items-center justify-center font-bold tracking-tight select-none"
                style="font-size:${fontSize}px;">${esc(initials)}</span>
        </div>
      `;
    }

    return `
      <div class="relative inline-flex items-center justify-center ${rounded} overflow-hidden shrink-0 border border-black/10 font-bold tracking-tight select-none ${className}"
           style="width:${size}px;height:${size}px;background:${palette.bg};color:${palette.fg};font-size:${fontSize}px;"
           aria-label="${esc(name)}">
        <span>${esc(initials)}</span>
      </div>
    `;
  }

  /**
   * Deep Pitch Green (#14532D) brand header with subtle SVG pitch lines.
   */
  function pitchHeader(opts) {
    const o = opts || {};
    const title = o.title || '';
    const subtitle = o.subtitle || '';
    const compact = !!o.compact;
    const showLogo = o.showLogo !== false;
    const showBack = !!o.showBack;
    const onBack = o.onBack || "window.ArenaRouter && window.ArenaRouter.back()";
    const rightHtml = o.rightHtml || '';

    return `
      <header class="relative bg-[#14532D] text-white ${compact ? 'pt-5 pb-5 px-5' : 'pt-8 pb-7 px-6'} overflow-hidden shrink-0">
        <svg class="absolute inset-0 w-full h-full opacity-15 pointer-events-none stroke-white" fill="none" xmlns="http://www.w3.org/2000/svg" preserveAspectRatio="none" viewBox="0 0 390 140">
          <line stroke-dasharray="4 4" stroke-width="1.2" x1="0" x2="390" y1="18" y2="18"></line>
          <line stroke-width="1.2" x1="195" x2="195" y1="0" y2="140"></line>
          <circle cx="195" cy="140" r="56" stroke-width="1.2"></circle>
          <circle cx="195" cy="140" fill="white" r="2.5"></circle>
          <rect height="36" stroke-width="1.2" width="110" x="140" y="-1"></rect>
          <path d="M 165 35 A 30 30 0 0 0 225 35" stroke-width="1.2"></path>
        </svg>
        <div class="relative z-10 flex flex-col">
          <div class="flex items-center justify-between gap-2 ${title || subtitle ? 'mb-3' : ''}">
            <div class="flex items-center gap-2.5">
              ${showBack ? `
                <button type="button" onclick="${onBack}" aria-label="Quay lại"
                        class="w-9 h-9 rounded-lg bg-white/10 hover:bg-white/20 border border-white/20 flex items-center justify-center text-white transition-colors">
                  <span class="material-symbols-outlined text-[20px]">arrow_back</span>
                </button>
              ` : ''}
              ${showLogo ? `
                <div class="flex items-center gap-2">
                  <div class="w-9 h-9 rounded-[8px] bg-white/15 border border-white/25 flex items-center justify-center text-white shadow-inner">
                    <span class="material-symbols-outlined text-[20px]">sports_soccer</span>
                  </div>
                  <span class="font-bold text-xl tracking-tight text-white">ArenaFlow</span>
                </div>
              ` : ''}
            </div>
            ${rightHtml ? `<div>${rightHtml}</div>` : ''}
          </div>
          ${title ? `<h1 class="font-bold ${compact ? 'text-xl' : 'text-[24px]'} leading-tight text-white">${esc(title)}</h1>` : ''}
          ${subtitle ? `<p class="text-sm text-white/80 leading-relaxed mt-1.5">${esc(subtitle)}</p>` : ''}
        </div>
      </header>
    `;
  }

  /**
   * Standard White Top App Bar (h-14) for logged-in shell or sub-views.
   */
  function topBar(opts) {
    const o = opts || {};
    const title = o.title || '';
    const showBack = !!o.showBack;
    const onBack = o.onBack || "window.ArenaRouter && window.ArenaRouter.back()";
    const user = o.user || (global.ArenaStore && global.ArenaStore.getCurrentUser());
    const unreadNotif = typeof o.unreadNotif === 'number'
      ? o.unreadNotif
      : (global.ArenaStore ? global.ArenaStore.getUnreadNotificationCount() : 0);

    return `
      <div class="h-14 bg-white border-b border-[#E5E7EB] px-3 flex items-center justify-between gap-2 shrink-0">
        <div class="flex items-center gap-2 min-w-0">
          ${showBack ? `
            <button type="button" onclick="${onBack}" aria-label="Quay lại"
                    class="w-9 h-9 rounded-lg flex items-center justify-center text-[#2D342C] hover:bg-[#EAF0E6] transition-colors shrink-0">
              <span class="material-symbols-outlined text-[22px]">arrow_back</span>
            </button>
          ` : `
            <button type="button" onclick="window.ArenaUI.toggleQuickDrawer(true)" aria-label="Menu"
                    class="w-9 h-9 rounded-lg flex items-center justify-center text-[#2D342C] hover:bg-[#EAF0E6] transition-colors shrink-0">
              <span class="material-symbols-outlined text-[22px]">menu</span>
            </button>
          `}
          ${title ? `
            <span class="font-bold text-base text-[#111827] truncate">${esc(title)}</span>
          ` : `
            <button type="button" onclick="window.ArenaRouter && window.ArenaRouter.navigate('MATCH-01')"
                    class="flex items-center gap-2 text-left">
              <div class="w-7 h-7 rounded-full bg-[#3D6943] text-white flex items-center justify-center shrink-0">
                <span class="material-symbols-outlined text-[16px]">sports_soccer</span>
              </div>
              <span class="font-bold text-lg tracking-tight text-[#14532D]">ArenaFlow</span>
            </button>
          `}
        </div>

        <div class="flex items-center gap-1.5 shrink-0">
          <button type="button" onclick="window.ArenaRouter && window.ArenaRouter.navigate('NOTIF-01')"
                  aria-label="Thông báo"
                  class="relative w-9 h-9 rounded-lg flex items-center justify-center text-[#2D342C] hover:bg-[#EAF0E6] transition-colors">
            <span class="material-symbols-outlined text-[22px]">notifications</span>
            ${unreadNotif > 0 ? `
              <span class="absolute top-1 right-1 min-w-[16px] h-4 px-1 rounded-full bg-[#DC2626] text-white text-[10px] font-bold flex items-center justify-center leading-none">
                ${unreadNotif > 9 ? '9+' : unreadNotif}
              </span>
            ` : ''}
          </button>
          <button type="button" onclick="window.ArenaRouter && window.ArenaRouter.navigate('PROF-01')"
                  aria-label="Hồ sơ cá nhân" class="rounded-full focus:outline-none">
            ${avatar(user || { displayName: 'Khách' }, { size: 34 })}
          </button>
        </div>
      </div>
    `;
  }

  /**
   * Semantic status/tag badge.
   */
  function badge(text, variant) {
    const styles = {
      primary: 'bg-[#BDEFBF] text-[#1C4825] border-[#AFE1B1]',
      success: 'bg-[#F0FDF4] text-[#16A34A] border-[#BBF7D0]',
      warning: 'bg-[#FEF3C7] text-[#B45309] border-[#FDE68A]',
      error: 'bg-[#FEF2F2] text-[#DC2626] border-[#FECACA]',
      info: 'bg-[#EFF6FF] text-[#1D4ED8] border-[#BFDBFE]',
      dark: 'bg-[#14532D] text-white border-[#14532D]',
      neutral: 'bg-[#EAF0E6] text-[#596158] border-[#DDE5D9]'
    };
    const cls = styles[variant] || styles.neutral;
    return `<span class="inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-[11px] font-semibold border ${cls}">${esc(text)}</span>`;
  }

  function emptyState(opts) {
    const o = opts || {};
    const icon = o.icon || 'inbox';
    const title = o.title || 'Chưa có dữ liệu';
    const description = o.description || 'Hiện chưa có mục nào phù hợp để hiển thị.';
    const actionLabel = o.actionLabel || '';
    const onAction = o.onAction || '';
    return `
      <div class="bg-white rounded-[12px] border border-[#DDE5D9] p-6 text-center flex flex-col items-center justify-center my-4">
        <div class="w-12 h-12 rounded-full bg-[#EAF0E6] text-[#3D6943] flex items-center justify-center mb-3">
          <span class="material-symbols-outlined text-[26px]">${esc(icon)}</span>
        </div>
        <h3 class="font-bold text-base text-[#111827] mb-1">${esc(title)}</h3>
        <p class="text-xs text-[#596158] leading-relaxed max-w-[260px]">${esc(description)}</p>
        ${actionLabel ? `
          <button type="button" onclick="${onAction}"
                  class="mt-4 px-4 py-2.5 rounded-[8px] bg-[#3D6943] hover:bg-[#315C38] text-white text-xs font-semibold transition-colors">
            ${esc(actionLabel)}
          </button>
        ` : ''}
      </div>
    `;
  }

  function errorState(opts) {
    const o = opts || {};
    const title = o.title || 'Không thể tải dữ liệu';
    const message = o.message || 'Đã có sự cố kết nối hoặc bạn không có quyền truy cập nội dung này.';
    const code = o.code ? `Mã lỗi: ${o.code}` : '';
    const onRetry = o.onRetry || 'window.ArenaRouter && window.ArenaRouter.refresh()';
    const onBack = o.onBack || 'window.ArenaRouter && window.ArenaRouter.back()';
    return `
      <div class="bg-[#FEF2F2] rounded-[12px] border border-[#FECACA] p-5 text-center flex flex-col items-center my-4">
        <div class="w-12 h-12 rounded-full bg-white border border-[#FECACA] text-[#DC2626] flex items-center justify-center mb-3">
          <span class="material-symbols-outlined text-[26px]">error</span>
        </div>
        <h3 class="font-bold text-base text-[#991B1B] mb-1">${esc(title)}</h3>
        <p class="text-xs text-[#7F1D1D] leading-relaxed max-w-[260px]">${esc(message)}</p>
        ${code ? `<span class="mt-2 text-[11px] font-mono bg-white/80 text-[#991B1B] px-2 py-0.5 rounded border border-[#FECACA]">${esc(code)}</span>` : ''}
        <div class="flex items-center gap-2 mt-4">
          <button type="button" onclick="${onBack}"
                  class="px-3.5 py-2 rounded-[8px] bg-white border border-[#E5E7EB] text-[#2D342C] text-xs font-semibold hover:bg-slate-50">
            Quay lại
          </button>
          <button type="button" onclick="${onRetry}"
                  class="px-3.5 py-2 rounded-[8px] bg-[#DC2626] text-white text-xs font-semibold hover:bg-[#B91C1C]">
            Thử lại
          </button>
        </div>
      </div>
    `;
  }

  function loadingState(opts) {
    const o = opts || {};
    const message = o.message || 'Đang tải dữ liệu...';
    return `
      <div class="py-12 px-4 flex flex-col items-center justify-center text-center">
        <div class="w-10 h-10 rounded-full border-4 border-[#DDE5D9] border-t-[#3D6943] animate-spin mb-3"></div>
        <p class="text-xs font-medium text-[#596158]">${esc(message)}</p>
      </div>
    `;
  }

  let _sheetCloseCb = null;

  function openSheet(opts) {
    const o = opts || {};
    const backdrop = document.getElementById('sheet-backdrop');
    const host = document.getElementById('sheet-host');
    if (!backdrop || !host) return;

    _sheetCloseCb = typeof o.onClose === 'function' ? o.onClose : null;

    host.innerHTML = `
      <div class="w-full bg-white rounded-t-[20px] shadow-2xl border-t border-[#E5E7EB] flex flex-col max-h-[82vh] animate-[sheetUp_.18s_ease-out]">
        <div class="pt-2.5 pb-1 flex justify-center shrink-0">
          <div class="w-10 h-1 rounded-full bg-[#DDE5D9]"></div>
        </div>
        <div class="px-4 py-2.5 border-b border-[#E5E7EB] flex items-center justify-between gap-2 shrink-0">
          <h3 class="font-bold text-base text-[#111827] truncate">${esc(o.title || '')}</h3>
          <button type="button" onclick="window.ArenaUI.closeSheet()" aria-label="Đóng"
                  class="w-8 h-8 rounded-full flex items-center justify-center text-[#596158] hover:bg-[#EAF0E6]">
            <span class="material-symbols-outlined text-[20px]">close</span>
          </button>
        </div>
        <div id="sheet-body" class="p-4 overflow-y-auto flex-1 text-sm text-[#2D342C]">
          ${o.bodyHtml || ''}
        </div>
        ${o.footerHtml ? `
          <div id="sheet-footer" class="p-4 border-t border-[#E5E7EB] bg-[#F8FAF3] shrink-0">
            ${o.footerHtml}
          </div>
        ` : ''}
      </div>
    `;

    backdrop.classList.remove('hidden');
    backdrop.classList.add('flex');
  }

  function closeSheet() {
    const backdrop = document.getElementById('sheet-backdrop');
    const host = document.getElementById('sheet-host');
    if (backdrop) {
      backdrop.classList.add('hidden');
      backdrop.classList.remove('flex');
    }
    if (host) host.innerHTML = '';
    if (_sheetCloseCb) {
      const cb = _sheetCloseCb;
      _sheetCloseCb = null;
      cb();
    }
  }

  let _toastTimer = null;
  function toast(message, type) {
    const el = document.getElementById('toast-host') || document.getElementById('toast');
    if (!el) return;
    const bgMap = {
      success: '#14532D',
      error: '#B91C1C',
      warning: '#B45309',
      info: '#1E293B'
    };
    el.style.backgroundColor = bgMap[type] || bgMap.success;
    el.textContent = message;
    el.style.display = 'block';
    if (_toastTimer) clearTimeout(_toastTimer);
    _toastTimer = setTimeout(() => {
      el.style.display = 'none';
    }, 2800);
  }

  function toggleQuickDrawer(forceOpen) {
    const drawer = document.getElementById('quick-jump-drawer');
    if (!drawer) return;
    const isHidden = drawer.classList.contains('hidden');
    const shouldOpen = typeof forceOpen === 'boolean' ? forceOpen : isHidden;
    if (shouldOpen) {
      drawer.classList.remove('hidden');
      drawer.classList.add('flex');
      if (typeof global.renderQuickJumpList === 'function') {
        global.renderQuickJumpList();
      }
    } else {
      drawer.classList.add('hidden');
      drawer.classList.remove('flex');
    }
  }

  global.ArenaUI = {
    esc: esc,
    avatar: avatar,
    pitchHeader: pitchHeader,
    topBar: topBar,
    badge: badge,
    emptyState: emptyState,
    errorState: errorState,
    loadingState: loadingState,
    openSheet: openSheet,
    closeSheet: closeSheet,
    toast: toast,
    toggleQuickDrawer: toggleQuickDrawer
  };

  // Backward-compatible global toast alias
  if (!global.toast) {
    global.toast = toast;
  }
})(window);
