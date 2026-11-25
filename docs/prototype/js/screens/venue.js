/**
 * ArenaFlow Venue & Location Screens
 * Covers: VEN-01..07
 * Supports picker callback to return selected venue to match/pairing/external forms,
 * CRUD, photo gallery (max 5), venue reporting, and INACTIVE venue blocking (VEN-07).
 */
(function (global) {
  'use strict';

  const VENUE_TYPES = {
    FIVE_A_SIDE: 'Sân 5 người',
    SEVEN_A_SIDE: 'Sân 7 người',
    ELEVEN_A_SIDE: 'Sân 11 người'
  };

  function formatVenueType(type) {
    return VENUE_TYPES[type] || 'Sân cỏ nhân tạo';
  }

  function makeSampleSvgDataUri(label, bg) {
    const svg = `<svg xmlns="http://www.w3.org/2000/svg" width="320" height="180" viewBox="0 0 320 180"><rect width="320" height="180" fill="${bg || '#14532D'}"/><rect x="16" y="16" width="288" height="148" fill="none" stroke="rgba(255,255,255,0.4)" stroke-width="2"/><line x1="160" y1="16" x2="160" y2="164" stroke="rgba(255,255,255,0.4)" stroke-width="2"/><circle cx="160" cy="90" r="28" fill="none" stroke="rgba(255,255,255,0.4)" stroke-width="2"/><text x="160" y="95" fill="#FFFFFF" font-family="sans-serif" font-size="14" font-weight="bold" text-anchor="middle">${label}</text></svg>`;
    return 'data:image/svg+xml;charset=utf-8,' + encodeURIComponent(svg);
  }

  function returnPickedVenue(venue, params) {
    const Router = global.ArenaRouter;
    const UI = global.ArenaUI;
    const p = params || {};
    const fieldKey = p.fieldKey || 'venueId';

    if (typeof global.__arenaVenuePickerCallback === 'function') {
      const cb = global.__arenaVenuePickerCallback;
      global.__arenaVenuePickerCallback = null;
      cb(venue);
      return;
    }

    UI.toast(`Đã chọn sân: ${venue.name}`, 'success');
    if (p.returnRoute) {
      Router.navigate(p.returnRoute, {
        [fieldKey]: venue.id,
        selectedVenueId: venue.id,
        slotIndex: p.slotIndex || ''
      });
    } else {
      Router.back();
    }
  }

  /* =========================================================================
     VEN-06 Sheet Helper: Báo cáo thông tin sân
     ========================================================================= */
  function openVenueReportSheet(venueId) {
    const Store = global.ArenaStore;
    const UI = global.ArenaUI;
    const Router = global.ArenaRouter;
    const state = Store.getState();
    const venue = (state.venues || []).find((v) => v.id === venueId);
    if (!venue) return;

    UI.openSheet({
      title: 'Báo cáo thông tin sân (VEN-06)',
      bodyHtml: `
        <div class="space-y-3">
          <div class="bg-[#F1F5EC] border border-[#DDE5D9] rounded-[8px] p-3 text-xs">
            <div class="font-bold text-[#111827]">${UI.esc(venue.name)}</div>
            <div class="text-[#596158]">${UI.esc(venue.address)}</div>
          </div>
          <div>
            <label class="block text-xs font-semibold text-[#2D342C] mb-1">Loại sai sót</label>
            <select id="ven06-type" class="w-full rounded-[8px] border border-[#DDE5D9] bg-white px-3 py-2 text-sm">
              <option value="CLOSED">Sân đã đóng cửa / giải tỏa</option>
              <option value="WRONG_INFO">Sai địa chỉ hoặc số điện thoại</option>
              <option value="DUPLICATE">Sân bị trùng lặp</option>
              <option value="OTHER">Thông tin khác</option>
            </select>
          </div>
          <div>
            <label class="block text-xs font-semibold text-[#2D342C] mb-1">Mô tả chi tiết *</label>
            <textarea id="ven06-reason" rows="3" placeholder="Ghi rõ thông tin cần Admin kiểm tra..."
              class="w-full rounded-[8px] border border-[#DDE5D9] px-3 py-2 text-sm"></textarea>
          </div>
          <div id="ven06-err" class="hidden text-xs text-[#DC2626] font-medium"></div>
        </div>
      `,
      footerHtml: `
        <div class="flex gap-2">
          <button type="button" onclick="window.ArenaUI.closeSheet()" class="flex-1 py-2.5 rounded-[8px] border border-[#DDE5D9] bg-white text-sm font-semibold">Hủy</button>
          <button type="button" id="ven06-submit" class="flex-1 py-2.5 rounded-[8px] bg-[#DC2626] text-white text-sm font-semibold">Gửi báo cáo</button>
        </div>
      `
    });

    const btn = document.getElementById('ven06-submit');
    if (btn) {
      btn.addEventListener('click', () => {
        const type = (document.getElementById('ven06-type') || {}).value || 'WRONG_INFO';
        const reason = ((document.getElementById('ven06-reason') || {}).value || '').trim();
        const errEl = document.getElementById('ven06-err');
        if (!reason) {
          if (errEl) {
            errEl.textContent = 'Vui lòng nhập mô tả sai sót.';
            errEl.classList.remove('hidden');
          }
          return;
        }
        const user = Store.getCurrentUser();
        const newReport = {
          id: 'rep-ven-' + Date.now(),
          reporterType: 'VENUE',
          reporterId: user ? user.id : 'guest',
          reportedId: venue.id,
          type,
          reason,
          status: 'PENDING',
          createdAt: Store.getMockClock().toISOString()
        };
        Store.setState((s) => ({
          ...s,
          reports: [newReport, ...(s.reports || [])],
          venues: s.venues.map((v) => (v.id === venue.id ? { ...v, reportCount: (v.reportCount || 0) + 1 } : v))
        }));
        UI.closeSheet();
        UI.toast('Đã gửi báo cáo thông tin sân cho Admin.', 'success');
        Router.refresh();
      });
    }
  }

  /* =========================================================================
     Register Routes
     ========================================================================= */
  function registerVenueRoutes() {
    const Router = global.ArenaRouter;
    const Store = global.ArenaStore;
    const UI = global.ArenaUI;
    if (!Router || !Store || !UI) return;

    /* VEN-01: Tìm & chọn sân */
    let searchQ = '';
    let filterType = 'ALL';
    let filterStatus = 'ALL'; // show both ACTIVE & INACTIVE so VEN-07 is inspectable

    Router.register('VEN-01', (container, params) => {
      const p = params || {};
      const isPicker = p.picker === 'true' || !!p.returnRoute;
      const state = Store.getState();

      function render() {
        const venues = (state.venues || []).filter((v) => {
          if (filterStatus === 'ACTIVE' && v.status !== 'ACTIVE') return false;
          if (filterStatus === 'INACTIVE' && v.status !== 'INACTIVE') return false;
          if (filterType !== 'ALL' && v.type !== filterType) return false;
          if (searchQ) {
            const q = searchQ.toLowerCase();
            if (!(v.name || '').toLowerCase().includes(q) && !(v.address || '').toLowerCase().includes(q)) return false;
          }
          return true;
        });

        container.innerHTML = `
          <div class="p-4 space-y-3">
            ${isPicker ? `
              <div class="bg-[#F0FDF4] border border-[#BBF7D0] rounded-[8px] p-3 text-xs text-[#166534] flex items-center gap-2">
                <span class="material-symbols-outlined text-[18px]">check_circle</span>
                <span><strong>Chế độ chọn sân:</strong> Chọn sân đang hoạt động hoặc thêm sân mới.</span>
              </div>
            ` : ''}

            <!-- Search -->
            <div class="relative">
              <span class="material-symbols-outlined text-[18px] text-[#596158] absolute left-3 top-2.5">search</span>
              <input id="ven01-search" type="text" value="${UI.esc(searchQ)}" placeholder="Tìm tên sân, đường, quận..."
                class="w-full rounded-[8px] border border-[#DDE5D9] bg-white pl-9 pr-3 py-2 text-sm focus:border-[#3D6943] focus:outline-none" />
            </div>

            <!-- Type Chips -->
            <div class="flex items-center gap-1.5 overflow-x-auto no-scrollbar">
              <button type="button" data-type="ALL" class="ven01-type-btn px-3 py-1.5 rounded-full text-xs font-semibold shrink-0 ${filterType === 'ALL' ? 'bg-[#14532D] text-white' : 'bg-white border border-[#DDE5D9] text-[#596158]'}">Tất cả</button>
              <button type="button" data-type="SEVEN_A_SIDE" class="ven01-type-btn px-3 py-1.5 rounded-full text-xs font-semibold shrink-0 ${filterType === 'SEVEN_A_SIDE' ? 'bg-[#14532D] text-white' : 'bg-white border border-[#DDE5D9] text-[#596158]'}">Sân 7</button>
              <button type="button" data-type="FIVE_A_SIDE" class="ven01-type-btn px-3 py-1.5 rounded-full text-xs font-semibold shrink-0 ${filterType === 'FIVE_A_SIDE' ? 'bg-[#14532D] text-white' : 'bg-white border border-[#DDE5D9] text-[#596158]'}">Sân 5</button>
              <button type="button" data-type="ELEVEN_A_SIDE" class="ven01-type-btn px-3 py-1.5 rounded-full text-xs font-semibold shrink-0 ${filterType === 'ELEVEN_A_SIDE' ? 'bg-[#14532D] text-white' : 'bg-white border border-[#DDE5D9] text-[#596158]'}">Sân 11</button>
            </div>

            <div class="flex items-center justify-between">
              <span class="text-xs font-semibold text-[#596158]">${venues.length} địa điểm sân</span>
              <button type="button" id="ven01-add-btn" class="px-3 py-1.5 rounded-[8px] bg-[#3D6943] text-white text-xs font-semibold flex items-center gap-1">
                <span class="material-symbols-outlined text-[15px]">add</span>
                Thêm sân mới
              </button>
            </div>

            ${venues.length === 0 ? UI.emptyState({
              icon: 'stadium',
              title: 'Không tìm thấy sân phù hợp',
              description: 'Bạn có thể thêm sân mới để sử dụng ngay.',
              actionLabel: 'Thêm sân mới',
              onAction: "window.ArenaRouter.navigate('VEN-03')"
            }) : `
              <div class="space-y-2.5">
                ${venues.map((v) => {
                  const isInactive = v.status === 'INACTIVE';
                  const primaryImg = (v.images || []).find((i) => i.isPrimary) || (v.images || [])[0];
                  return `
                    <article class="bg-white rounded-[8px] border ${isInactive ? 'border-[#FECACA] bg-[#FFFBFB]' : 'border-[#DDE5D9]'} p-3.5 space-y-2.5">
                      <div class="flex gap-3 cursor-pointer" onclick="window.ArenaRouter.navigate('${isInactive ? 'VEN-07' : 'VEN-02'}',{id:'${UI.esc(v.id)}',returnRoute:'${UI.esc(p.returnRoute || '')}',fieldKey:'${UI.esc(p.fieldKey || 'venueId')}'})">
                        <div class="w-16 h-16 rounded-[8px] bg-[#EAF0E6] overflow-hidden shrink-0 flex items-center justify-center">
                          ${primaryImg ? `<img src="${UI.esc(primaryImg.url)}" alt="${UI.esc(v.name)}" class="w-full h-full object-cover" />` : `<span class="material-symbols-outlined text-[28px] text-[#3D6943]">stadium</span>`}
                        </div>
                        <div class="min-w-0 flex-1 space-y-1">
                          <div class="flex items-start justify-between gap-1.5">
                            <h3 class="font-bold text-sm text-[#111827] leading-snug">${UI.esc(v.name)}</h3>
                            ${UI.badge(isInactive ? 'Ngừng HĐ' : 'Hoạt động', isInactive ? 'error' : 'success')}
                          </div>
                          <div class="text-xs text-[#596158] truncate">${UI.esc(v.address)}</div>
                          <div class="flex items-center gap-2 text-[11px] text-[#596158]">
                            <span>${UI.esc(formatVenueType(v.type))}</span>
                            ${v.distanceKm ? `<span>• ~${v.distanceKm} km</span>` : ''}
                          </div>
                        </div>
                      </div>

                      ${isInactive ? `
                        <div class="bg-[#FEF2F2] border border-[#FECACA] rounded-[8px] p-2 text-[11px] text-[#991B1B]">
                          ${UI.esc(v.warning || 'Sân đã ngừng hoạt động — Không thể chọn cho kèo mới.')}
                        </div>
                      ` : ''}

                      ${isPicker ? `
                        <div class="pt-1 border-t border-[#F1F5EC] flex justify-end">
                          ${isInactive ? `
                            <button type="button" disabled class="px-3 py-1.5 rounded-[8px] bg-[#DDE5D9] text-[#596158] text-xs font-semibold cursor-not-allowed">
                              Không thể chọn (Ngừng HĐ)
                            </button>
                          ` : `
                            <button type="button" data-pick-id="${UI.esc(v.id)}"
                              class="ven01-pick-btn px-3.5 py-1.5 rounded-[8px] bg-[#3D6943] text-white text-xs font-semibold">
                              Chọn sân này
                            </button>
                          `}
                        </div>
                      ` : ''}
                    </article>
                  `;
                }).join('')}
              </div>
            `}
          </div>
        `;

        const sInp = container.querySelector('#ven01-search');
        if (sInp) {
          sInp.addEventListener('input', (e) => {
            searchQ = e.target.value;
            render();
            const again = container.querySelector('#ven01-search');
            if (again) {
              again.focus();
              again.setSelectionRange(again.value.length, again.value.length);
            }
          });
        }
        container.querySelectorAll('.ven01-type-btn').forEach((b) => {
          b.addEventListener('click', () => {
            filterType = b.getAttribute('data-type') || 'ALL';
            render();
          });
        });
        const addBtn = container.querySelector('#ven01-add-btn');
        if (addBtn) {
          addBtn.addEventListener('click', () => {
            Router.navigate('VEN-03', { returnRoute: p.returnRoute || '', fieldKey: p.fieldKey || 'venueId' });
          });
        }
        container.querySelectorAll('.ven01-pick-btn').forEach((b) => {
          b.addEventListener('click', () => {
            const v = (state.venues || []).find((x) => x.id === b.getAttribute('data-pick-id'));
            if (v) returnPickedVenue(v, p);
          });
        });
      }
      render();
    }, { title: 'Tìm & chọn sân', tab: null, showBack: true, group: 'Sân bóng', aliases: ['venues'] });

    /* VEN-02: Chi tiết sân */
    Router.register('VEN-02', (container, params) => {
      const state = Store.getState();
      const user = Store.getCurrentUser();
      const venue = (state.venues || []).find((v) => v.id === (params && params.id)) || (state.venues || [])[0];
      if (!venue) {
        container.innerHTML = `<div class="p-4">${UI.errorState({ title: 'Không tìm thấy sân', code: '404' })}</div>`;
        return;
      }

      const isInactive = venue.status === 'INACTIVE';
      const canEdit = user && (venue.createdBy === user.id || user.role === 'ADMIN');
      const isPicker = !!(params && params.returnRoute);
      const images = venue.images || [];

      container.innerHTML = `
        <div class="p-4 space-y-3">
          ${isInactive ? `
            <div class="bg-[#FEF2F2] border border-[#FECACA] rounded-[8px] p-3 text-xs text-[#991B1B] font-semibold">
              Sân ngừng hoạt động (VEN-07): ${UI.esc(venue.warning || 'Không thể chọn cho kèo mới.')}
            </div>
          ` : ''}

          <div class="bg-white rounded-[8px] border border-[#DDE5D9] overflow-hidden">
            <div class="h-36 bg-[#14532D] relative flex items-center justify-center">
              ${images[0] ? `<img src="${UI.esc(images[0].url)}" alt="${UI.esc(venue.name)}" class="w-full h-full object-cover" />` : `
                <div class="text-white/80 flex flex-col items-center gap-1">
                  <span class="material-symbols-outlined text-[40px]">stadium</span>
                  <span class="text-xs">Chưa có ảnh sân</span>
                </div>
              `}
              <button type="button" onclick="window.ArenaRouter.navigate('VEN-05',{id:'${UI.esc(venue.id)}'})"
                class="absolute bottom-2.5 right-2.5 px-2.5 py-1 rounded-[8px] bg-black/60 text-white text-[11px] font-semibold flex items-center gap-1">
                <span class="material-symbols-outlined text-[14px]">photo_library</span>
                Thư viện (${images.length}/5)
              </button>
            </div>
            <div class="p-4 space-y-2">
              <div class="flex items-start justify-between gap-2">
                <h2 class="font-bold text-base text-[#111827]">${UI.esc(venue.name)}</h2>
                ${UI.badge(isInactive ? 'Ngừng HĐ' : 'Hoạt động', isInactive ? 'error' : 'success')}
              </div>
              <div class="text-xs text-[#596158] space-y-1">
                <div class="flex items-start gap-1.5">
                  <span class="material-symbols-outlined text-[16px] shrink-0">location_on</span>
                  <span>${UI.esc(venue.address)}</span>
                </div>
                <div><strong>Loại sân:</strong> ${UI.esc(formatVenueType(venue.type))}</div>
                <div><strong>Khu vực:</strong> ${UI.esc(Store.getWardName(venue.wardId))}</div>
                <div><strong>Liên hệ sân:</strong> ${UI.esc(venue.contactPhone || 'Chưa cập nhật')}</div>
              </div>
              <div class="bg-[#F1F5EC] rounded-[8px] p-2.5 text-[11px] text-[#596158]">
                Lưu ý: Xem thông tin sân trên ArenaFlow không đồng nghĩa đã đặt sân. Hai đội tự liên hệ chủ sân để giữ giờ.
              </div>
            </div>
          </div>

          ${isPicker && !isInactive ? `
            <button type="button" id="ven02-pick-btn" class="w-full py-3 rounded-[8px] bg-[#3D6943] text-white text-sm font-semibold">
              Chọn sân này
            </button>
          ` : ''}

          <div class="flex gap-2">
            ${canEdit ? `
              <button type="button" onclick="window.ArenaRouter.navigate('VEN-04',{id:'${UI.esc(venue.id)}'})"
                class="flex-1 py-2.5 rounded-[8px] border border-[#DDE5D9] bg-white text-xs font-semibold text-[#2D342C]">
                Sửa thông tin
              </button>
            ` : ''}
            <button type="button" id="ven02-report-btn"
              class="flex-1 py-2.5 rounded-[8px] border border-[#FECACA] bg-[#FEF2F2] text-xs font-semibold text-[#DC2626]">
              Báo cáo sai sót
            </button>
          </div>
        </div>
      `;

      const pickBtn = container.querySelector('#ven02-pick-btn');
      if (pickBtn) pickBtn.addEventListener('click', () => returnPickedVenue(venue, params));
      const repBtn = container.querySelector('#ven02-report-btn');
      if (repBtn) repBtn.addEventListener('click', () => openVenueReportSheet(venue.id));
    }, { title: 'Chi tiết sân bóng', tab: null, showBack: true, group: 'Sân bóng', aliases: ['venue-detail'] });

    /* VEN-03: Thêm sân */
    Router.register('VEN-03', (container, params) => {
      const state = Store.getState();
      const user = Store.getCurrentUser();
      if (!user) {
        container.innerHTML = `<div class="p-4">${UI.errorState({ title: 'Cần đăng nhập', message: 'Vui lòng đăng nhập để thêm sân mới.', code: '401' })}</div>`;
        return;
      }

      container.innerHTML = `
        <div class="p-4 space-y-3">
          <div class="bg-white rounded-[8px] border border-[#DDE5D9] p-4 space-y-3">
            <div>
              <label class="block text-xs font-semibold text-[#2D342C] mb-1">Tên sân bóng *</label>
              <input id="ven03-name" type="text" placeholder="VD: Sân bóng Gia Định" class="w-full rounded-[8px] border border-[#DDE5D9] px-3 py-2 text-sm" />
            </div>
            <div>
              <label class="block text-xs font-semibold text-[#2D342C] mb-1">Địa chỉ chi tiết *</label>
              <input id="ven03-addr" type="text" placeholder="Số nhà, tên đường..." class="w-full rounded-[8px] border border-[#DDE5D9] px-3 py-2 text-sm" />
            </div>
            <div>
              <label class="block text-xs font-semibold text-[#2D342C] mb-1">Phường / Khu vực</label>
              <select id="ven03-ward" class="w-full rounded-[8px] border border-[#DDE5D9] bg-white px-3 py-2 text-sm">
                ${(state.wards || []).map((w) => `<option value="${UI.esc(w.id)}">${UI.esc(w.name)}, ${UI.esc(w.district)}</option>`).join('')}
              </select>
            </div>
            <div class="grid grid-cols-2 gap-2">
              <div>
                <label class="block text-xs font-semibold text-[#2D342C] mb-1">Loại sân</label>
                <select id="ven03-type" class="w-full rounded-[8px] border border-[#DDE5D9] bg-white px-3 py-2 text-sm">
                  <option value="SEVEN_A_SIDE">Sân 7 người</option>
                  <option value="FIVE_A_SIDE">Sân 5 người</option>
                  <option value="ELEVEN_A_SIDE">Sân 11 người</option>
                </select>
              </div>
              <div>
                <label class="block text-xs font-semibold text-[#2D342C] mb-1">SĐT liên hệ</label>
                <input id="ven03-phone" type="tel" placeholder="090xxxxxxx" class="w-full rounded-[8px] border border-[#DDE5D9] px-3 py-2 text-sm" />
              </div>
            </div>
            <div id="ven03-err" class="hidden text-xs text-[#DC2626] font-medium"></div>
          </div>

          <button type="button" id="ven03-submit" class="w-full py-3 rounded-[8px] bg-[#3D6943] text-white text-sm font-semibold">
            Tạo sân (Hiệu lực ngay)
          </button>
        </div>
      `;

      const ven03SubmitBtn = container.querySelector('#ven03-submit');
      if (ven03SubmitBtn) {
        ven03SubmitBtn.addEventListener('click', () => {
        const name = ((container.querySelector('#ven03-name') || {}).value || '').trim();
        const address = ((container.querySelector('#ven03-addr') || {}).value || '').trim();
        const wardId = (container.querySelector('#ven03-ward') || {}).value || 'ward-ak';
        const type = (container.querySelector('#ven03-type') || {}).value || 'SEVEN_A_SIDE';
        const contactPhone = ((container.querySelector('#ven03-phone') || {}).value || '').trim();
        const errEl = container.querySelector('#ven03-err');

        if (!name || !address) {
          errEl.textContent = 'Vui lòng nhập đầy đủ tên sân và địa chỉ.';
          errEl.classList.remove('hidden');
          return;
        }
        const newVenue = {
          id: 'ven-' + Date.now(),
          name,
          address,
          wardId,
          coordinates: { lat: 10.792, lng: 106.744 },
          distanceKm: 1.8,
          type,
          contactPhone,
          status: 'ACTIVE',
          createdBy: user.id,
          images: [],
          reportCount: 0
        };
        Store.setState((s) => ({ ...s, venues: [newVenue, ...(s.venues || [])] }));
        if (params && params.returnRoute) {
          returnPickedVenue(newVenue, params);
        } else {
          UI.toast('Đã thêm sân mới thành công!', 'success');
          Router.navigate('VEN-02', { id: newVenue.id });
        }
        });
      }
    }, { title: 'Thêm sân bóng', tab: null, showBack: true, group: 'Sân bóng', aliases: ['venue-create'] });

    /* VEN-04: Sửa sân (creator or ADMIN only) */
    Router.register('VEN-04', (container, params) => {
      const state = Store.getState();
      const user = Store.getCurrentUser();
      const venue = (state.venues || []).find((v) => v.id === (params && params.id)) || (state.venues || [])[0];
      if (!venue) {
        container.innerHTML = `<div class="p-4">${UI.errorState({ title: 'Không tìm thấy sân', code: '404' })}</div>`;
        return;
      }
      if (!user || (venue.createdBy !== user.id && user.role !== 'ADMIN')) {
        container.innerHTML = `<div class="p-4">${UI.errorState({ title: 'Không có quyền sửa sân', message: 'Chỉ người tạo sân hoặc Admin mới được phép chỉnh sửa thông tin sân.', code: '403' })}</div>`;
        return;
      }

      container.innerHTML = `
        <div class="p-4 space-y-3">
          <div class="bg-white rounded-[8px] border border-[#DDE5D9] p-4 space-y-3">
            <div>
              <label class="block text-xs font-semibold text-[#2D342C] mb-1">Tên sân bóng</label>
              <input id="ven04-name" type="text" value="${UI.esc(venue.name)}" class="w-full rounded-[8px] border border-[#DDE5D9] px-3 py-2 text-sm" />
            </div>
            <div>
              <label class="block text-xs font-semibold text-[#2D342C] mb-1">Địa chỉ</label>
              <input id="ven04-addr" type="text" value="${UI.esc(venue.address)}" class="w-full rounded-[8px] border border-[#DDE5D9] px-3 py-2 text-sm" />
            </div>
            <div>
              <label class="block text-xs font-semibold text-[#2D342C] mb-1">SĐT liên hệ</label>
              <input id="ven04-phone" type="tel" value="${UI.esc(venue.contactPhone || '')}" class="w-full rounded-[8px] border border-[#DDE5D9] px-3 py-2 text-sm" />
            </div>
          </div>
          <button type="button" id="ven04-save" class="w-full py-3 rounded-[8px] bg-[#3D6943] text-white text-sm font-semibold">Lưu thông tin sân</button>
        </div>
      `;

      const ven04SaveBtn = container.querySelector('#ven04-save');
      if (ven04SaveBtn) {
        ven04SaveBtn.addEventListener('click', () => {
        const name = ((container.querySelector('#ven04-name') || {}).value || '').trim() || venue.name;
        const address = ((container.querySelector('#ven04-addr') || {}).value || '').trim() || venue.address;
        const contactPhone = ((container.querySelector('#ven04-phone') || {}).value || '').trim();
        Store.setState((s) => ({
          ...s,
          venues: s.venues.map((v) => (v.id === venue.id ? { ...v, name, address, contactPhone } : v))
        }));
        UI.toast('Đã cập nhật thông tin sân.', 'success');
        Router.navigate('VEN-02', { id: venue.id });
        });
      }
    }, { title: 'Sửa thông tin sân', tab: null, showBack: true, group: 'Sân bóng', aliases: ['venue-edit'] });

    /* VEN-05: Thư viện ảnh sân */
    Router.register('VEN-05', (container, params) => {
      const state = Store.getState();
      const venue = (state.venues || []).find((v) => v.id === (params && params.id)) || (state.venues || [])[0];
      if (!venue) {
        container.innerHTML = `<div class="p-4">${UI.errorState({ title: 'Không tìm thấy sân', code: '404' })}</div>`;
        return;
      }
      const images = venue.images || [];

      container.innerHTML = `
        <div class="p-4 space-y-3">
          <div class="bg-[#F1F5EC] border border-[#DDE5D9] rounded-[8px] p-3 flex items-center justify-between">
            <div class="text-xs">
              <div class="font-bold text-[#14532D]">${UI.esc(venue.name)}</div>
              <div class="text-[#596158]">Đã tải ${images.length} / 5 ảnh tối đa</div>
            </div>
            ${images.length < 5 ? `
              <button type="button" id="ven05-upload" class="px-3 py-1.5 rounded-[8px] bg-[#3D6943] text-white text-xs font-semibold">
                + Tải ảnh lên
              </button>
            ` : ''}
          </div>

          ${images.length === 0 ? UI.emptyState({
            icon: 'add_a_photo',
            title: 'Chưa có ảnh sân nào',
            description: 'Thêm tối đa 5 ảnh để các đội dễ dàng nhận diện mặt sân.',
            actionLabel: 'Thêm ảnh mẫu',
            onAction: "document.getElementById('ven05-upload') && document.getElementById('ven05-upload').click()"
          }) : `
            <div class="space-y-2.5">
              ${images.map((img, idx) => `
                <div class="bg-white rounded-[8px] border border-[#DDE5D9] overflow-hidden">
                  <img src="${UI.esc(img.url)}" alt="Ảnh sân ${idx + 1}" class="w-full h-36 object-cover" />
                  <div class="p-2.5 flex items-center justify-between">
                    ${img.isPrimary ? UI.badge('Ảnh đại diện', 'primary') : `
                      <button type="button" data-primary-id="${UI.esc(img.id)}" class="ven05-primary-btn text-xs font-semibold text-[#3D6943]">
                        Đặt làm ảnh chính
                      </button>
                    `}
                    <button type="button" data-del-id="${UI.esc(img.id)}" class="ven05-del-btn text-xs font-semibold text-[#DC2626]">
                      Xóa ảnh
                    </button>
                  </div>
                </div>
              `).join('')}
            </div>
          `}
        </div>
      `;

      const upBtn = container.querySelector('#ven05-upload');
      if (upBtn) {
        upBtn.addEventListener('click', () => {
          const idx = images.length + 1;
          const colors = ['#14532D', '#2D5A36', '#3D6943', '#1E3A2F', '#465846'];
          const newImg = {
            id: 'img-' + Date.now(),
            url: makeSampleSvgDataUri(`Mặt sân #${idx}`, colors[(idx - 1) % colors.length]),
            isPrimary: images.length === 0
          };
          Store.setState((s) => ({
            ...s,
            venues: s.venues.map((v) => (v.id === venue.id ? { ...v, images: [...(v.images || []), newImg] } : v))
          }));
          UI.toast('Đã tải ảnh sân lên thành công!', 'success');
          Router.refresh();
        });
      }
      container.querySelectorAll('.ven05-primary-btn').forEach((b) => {
        b.addEventListener('click', () => {
          const imgId = b.getAttribute('data-primary-id');
          Store.setState((s) => ({
            ...s,
            venues: s.venues.map((v) =>
              v.id === venue.id ? { ...v, images: (v.images || []).map((i) => ({ ...i, isPrimary: i.id === imgId })) } : v
            )
          }));
          Router.refresh();
        });
      });
      container.querySelectorAll('.ven05-del-btn').forEach((b) => {
        b.addEventListener('click', () => {
          const imgId = b.getAttribute('data-del-id');
          Store.setState((s) => ({
            ...s,
            venues: s.venues.map((v) =>
              v.id === venue.id ? { ...v, images: (v.images || []).filter((i) => i.id !== imgId) } : v
            )
          }));
          Router.refresh();
        });
      });
    }, { title: 'Thư viện ảnh sân', tab: null, showBack: true, group: 'Sân bóng', aliases: ['venue-photos'] });

    /* VEN-06: Báo cáo thông tin sân */
    Router.register('VEN-06', (container, params) => {
      const state = Store.getState();
      const venue = (state.venues || []).find((v) => v.id === (params && params.id)) || (state.venues || [])[0];
      container.innerHTML = `<div class="p-4">${venue ? `<button type="button" id="ven06-open" class="w-full py-3 rounded-[8px] bg-[#DC2626] text-white text-sm font-semibold">Mở form báo cáo sân</button>` : UI.emptyState({ title: 'Không có sân' })}</div>`;
      const btn = container.querySelector('#ven06-open');
      if (btn && venue) btn.addEventListener('click', () => openVenueReportSheet(venue.id));
    }, { title: 'Báo cáo thông tin sân', tab: null, showBack: true, group: 'Sân bóng', aliases: ['venue-report'] });

    /* VEN-07: Biến thể sân INACTIVE (blocked from new selection) */
    Router.register('VEN-07', (container, params) => {
      const state = Store.getState();
      const venue = (state.venues || []).find((v) => v.id === (params && params.id) && v.status === 'INACTIVE') ||
        (state.venues || []).find((v) => v.status === 'INACTIVE') ||
        (state.venues || [])[0];

      if (!venue) {
        container.innerHTML = `<div class="p-4">${UI.emptyState({ title: 'Không có sân ngừng hoạt động' })}</div>`;
        return;
      }

      container.innerHTML = `
        <div class="p-4 space-y-3">
          <div class="bg-[#FEF2F2] border border-[#FECACA] rounded-[8px] p-4 space-y-2">
            <div class="flex items-center gap-2 text-[#DC2626] font-bold text-sm">
              <span class="material-symbols-outlined text-[20px]">block</span>
              <span>Sân ngừng hoạt động (INACTIVE)</span>
            </div>
            <p class="text-xs text-[#991B1B] leading-relaxed">
              ${UI.esc(venue.warning || 'Sân này đã ngừng hoạt động theo báo cáo xác thực. Bạn vẫn có thể xem lại lịch sử kèo cũ nhưng bị chặn chọn cho kèo hoặc trận ngoài mới.')}
            </p>
          </div>

          <div class="bg-white rounded-[8px] border border-[#DDE5D9] p-4 space-y-2 opacity-90">
            <div class="flex items-center justify-between">
              <h2 class="font-bold text-base text-[#111827]">${UI.esc(venue.name)}</h2>
              ${UI.badge('NGỪNG HOẠT ĐỘNG', 'error')}
            </div>
            <div class="text-xs text-[#596158]">${UI.esc(venue.address)}</div>
            <div class="text-xs text-[#596158]">Loại sân: ${UI.esc(formatVenueType(venue.type))}</div>
          </div>

          <button type="button" disabled
            class="w-full py-3 rounded-[8px] bg-[#DDE5D9] text-[#596158] text-sm font-semibold cursor-not-allowed">
            Không thể chọn sân này cho gói mới
          </button>
          <button type="button" onclick="window.ArenaRouter.navigate('VEN-01')"
            class="w-full py-2.5 rounded-[8px] border border-[#DDE5D9] bg-white text-xs font-semibold text-[#3D6943]">
            Quay lại tìm sân khác đang hoạt động
          </button>
        </div>
      `;
    }, { title: 'Sân ngừng hoạt động', tab: null, showBack: true, group: 'Sân bóng', aliases: ['venue-inactive'] });
  }

  registerVenueRoutes();
})(window);
