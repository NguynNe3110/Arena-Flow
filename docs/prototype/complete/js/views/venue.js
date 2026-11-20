/**
 * Venue Views & Sheets (VEN-01..07)
 * Owned by @ui-match-pair
 *
 * Implements:
 * - VEN-01: Venue picker with return-to-form support & directory
 * - VEN-02: Venue detail (full info, gallery, map, actions)
 * - VEN-03: Add venue with immediate availability (ACTIVE)
 * - VEN-04: Edit venue (creator or admin only)
 * - VEN-05: Photo manager (max 5 photos, primary toggle, upload simulation)
 * - VEN-06: Report venue (closed / wrong info / other)
 * - VEN-07: Inactive venue handling (warnings, blocked from selection)
 */

export function initVenueViews(router, store) {
  // Helper to get ward label
  const getWardName = (wardId) => {
    const wards = store.getState().wards || [];
    const w = wards.find(item => item.id === wardId);
    return w ? `${w.name}, ${w.district}` : 'TP. Hồ Chí Minh';
  };

  const formatVenueType = (type) => {
    switch (type) {
      case 'FIVE_A_SIDE': return 'Sân 5 người';
      case 'SEVEN_A_SIDE': return 'Sân 7 người';
      case 'ELEVEN_A_SIDE': return 'Sân 11 người';
      default: return 'Sân cỏ nhân tạo';
    }
  };

  /* =========================================================================
     VEN-01: Venue Directory & Picker View ('venues')
     ========================================================================= */
  router.registerView('venues', (container, params = {}) => {
    const isPicker = params.picker === 'true' || !!params.returnRoute;
    const returnRoute = params.returnRoute || '';
    const fieldKey = params.fieldKey || 'venueId';
    const multiple = params.multiple === 'true';

    router.setHeaderTitle(isPicker ? 'Chọn sân bóng' : 'Địa điểm sân');

    let searchQuery = (params.q || '').trim().toLowerCase();
    let selectedType = params.type || 'ALL';
    let selectedStatus = params.status || 'ACTIVE';

    function renderList() {
      const state = store.getState();
      const allVenues = state.venues || [];

      // Filter venues
      const filtered = allVenues.filter(v => {
        // Status filter
        if (selectedStatus === 'ACTIVE' && v.status !== 'ACTIVE') return false;
        if (selectedStatus === 'INACTIVE' && v.status !== 'INACTIVE') return false;

        // Type filter
        if (selectedType !== 'ALL' && v.type !== selectedType) return false;

        // Search filter
        if (searchQuery) {
          const nameMatch = (v.name || '').toLowerCase().includes(searchQuery);
          const addrMatch = (v.address || '').toLowerCase().includes(searchQuery);
          if (!nameMatch && !addrMatch) return false;
        }

        return true;
      });

      container.innerHTML = `
        <div style="padding: 16px 0;">
          ${isPicker ? `
            <div class="notice notice-info" style="margin-top: 0; margin-bottom: 14px;">
              <strong>Chế độ chọn sân:</strong> Vui lòng chọn địa điểm cho kèo của bạn, hoặc thêm sân mới nếu chưa có trong danh sách.
            </div>
          ` : ''}

          <!-- Search & Filter Controls -->
          <div class="form-group" style="margin-bottom: 10px;">
            <div style="position: relative;">
              <input type="text" class="form-input" id="venue-search-input"
                placeholder="Tìm tên sân hoặc địa chỉ..." value="${searchQuery}">
              ${searchQuery ? `
                <button type="button" id="venue-clear-search-btn" class="btn btn-ghost btn-icon"
                  style="position: absolute; right: 4px; top: 4px; min-height: 40px; height: 40px; width: 40px;">
                  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <line x1="18" y1="6" x2="6" y2="18"></line>
                    <line x1="6" y1="6" x2="18" y2="18"></line>
                  </svg>
                </button>
              ` : ''}
            </div>
          </div>

          <!-- Type Filter Chips -->
          <div class="chip-group" style="margin-bottom: 14px;">
            <button class="chip ${selectedType === 'ALL' ? 'is-active' : ''}" data-type="ALL">Tất cả loại sân</button>
            <button class="chip ${selectedType === 'SEVEN_A_SIDE' ? 'is-active' : ''}" data-type="SEVEN_A_SIDE">Sân 7 người</button>
            <button class="chip ${selectedType === 'FIVE_A_SIDE' ? 'is-active' : ''}" data-type="FIVE_A_SIDE">Sân 5 người</button>
            <button class="chip ${selectedType === 'ELEVEN_A_SIDE' ? 'is-active' : ''}" data-type="ELEVEN_A_SIDE">Sân 11 người</button>
          </div>

          <!-- Action bar with Add Venue button -->
          <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 14px;">
            <span style="font-size: var(--font-size-sm); color: var(--color-on-surface-variant); font-weight: var(--font-weight-medium);">
              Tìm thấy ${filtered.length} địa điểm
            </span>
            <button class="btn btn-primary" id="venue-add-nav-btn" style="min-height: 40px; padding: 6px 14px;">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <line x1="12" y1="5" x2="12" y2="19"></line>
                <line x1="5" y1="12" x2="19" y2="12"></line>
              </svg>
              <span>Thêm sân mới</span>
            </button>
          </div>

          <!-- Venues List -->
          ${filtered.length === 0 ? `
            <div class="empty-state">
              <div class="empty-state-icon">
                <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
                  <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"></path>
                  <circle cx="12" cy="10" r="3"></circle>
                </svg>
              </div>
              <h3 class="empty-state-title">Không tìm thấy địa điểm phù hợp</h3>
              <p class="empty-state-desc">
                ${searchQuery ? `Không có kết quả nào khớp với "${searchQuery}".` : 'Chưa có địa điểm sân trong danh mục này.'}
              </p>
              <button class="btn btn-primary" id="venue-empty-add-btn" style="margin-top: 12px;">
                Thêm sân bóng ngay (Hiệu lực tức thì)
              </button>
            </div>
          ` : `
            <div class="list-group">
              ${filtered.map(v => {
                const primaryImg = (v.images || []).find(img => img.isPrimary) || v.images?.[0];
                const isInactive = v.status === 'INACTIVE';

                return `
                  <div class="card card-interactive venue-card-item" data-id="${v.id}"
                    style="${isInactive ? 'opacity: 0.85; border-color: #fca5a5; background-color: #fffaf9;' : ''}">
                    <div style="display: flex; gap: 12px;">
                      <!-- Venue Thumbnail -->
                      <div style="width: 80px; height: 80px; border-radius: var(--radius-md); overflow: hidden; background-color: var(--color-surface-container-high); flex-shrink: 0; position: relative;">
                        ${primaryImg ? `
                          <img src="${primaryImg.url}" alt="${v.name}" style="width: 100%; height: 100%; object-fit: cover;">
                        ` : `
                          <div style="width: 100%; height: 100%; display: grid; place-items: center; color: var(--color-outline);">
                            <svg width="30" height="30" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
                              <rect x="3" y="3" width="18" height="18" rx="2" ry="2"></rect>
                              <circle cx="8.5" cy="8.5" r="1.5"></circle>
                              <polyline points="21 15 16 10 5 21"></polyline>
                            </svg>
                          </div>
                        `}
                        ${(v.images || []).length > 0 ? `
                          <span style="position: absolute; bottom: 3px; right: 3px; background: rgba(0,0,0,0.65); color: #fff; font-size: 10px; padding: 1px 4px; border-radius: 4px; font-weight: 600;">
                            ${v.images.length} ẢNH
                          </span>
                        ` : ''}
                      </div>

                      <!-- Venue Main Content -->
                      <div style="flex: 1; min-width: 0;">
                        <div style="display: flex; align-items: flex-start; justify-content: space-between; gap: 6px;">
                          <h4 style="margin: 0 0 4px 0; font-size: var(--font-size-md); font-weight: var(--font-weight-bold); color: var(--color-on-surface); line-height: 1.3;">
                            ${v.name}
                          </h4>
                          ${isInactive ? `
                            <span class="badge badge-red">NGỪNG HOẠT ĐỘNG</span>
                          ` : `
                            <span class="badge badge-green">HOẠT ĐỘNG</span>
                          `}
                        </div>

                        <div style="font-size: var(--font-size-xs); color: var(--color-on-surface-variant); margin-bottom: 4px; display: flex; align-items: center; gap: 4px;">
                          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="flex-shrink: 0;">
                            <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"></path>
                            <circle cx="12" cy="10" r="3"></circle>
                          </svg>
                          <span class="text-truncate">${v.address}</span>
                        </div>

                        <div style="display: flex; align-items: center; gap: 6px; flex-wrap: wrap;">
                          <span class="badge badge-gray">${formatVenueType(v.type)}</span>
                          ${v.contactPhone ? `
                            <span style="font-size: var(--font-size-xs); color: var(--color-on-surface-variant);">
                              📞 ${v.contactPhone}
                            </span>
                          ` : ''}
                        </div>
                      </div>
                    </div>

                    ${isInactive && v.warning ? `
                      <div class="notice notice-warning" style="margin: 10px 0 0 0; padding: 6px 10px; font-size: 11px;">
                        ⚠️ ${v.warning}
                      </div>
                    ` : ''}

                    <!-- Picker Select Button if in picker mode -->
                    ${isPicker ? `
                      <div style="margin-top: 10px; padding-top: 8px; border-top: 1px solid var(--color-outline-variant); display: flex; justify-content: flex-end;">
                        ${isInactive ? `
                          <button class="btn btn-secondary" disabled style="min-height: 38px; font-size: 13px;">
                            Sân ngừng hoạt động - Không thể chọn
                          </button>
                        ` : `
                          <button class="btn btn-primary venue-pick-action-btn" data-id="${v.id}" data-name="${v.name}" style="min-height: 38px; font-size: 13px;">
                            ✓ Chọn sân này
                          </button>
                        `}
                      </div>
                    ` : ''}
                  </div>
                `;
              }).join('')}
            </div>
          `}
        </div>
      `;

      // Event handlers
      const searchInput = container.querySelector('#venue-search-input');
      if (searchInput) {
        searchInput.addEventListener('input', (e) => {
          searchQuery = e.target.value.trim().toLowerCase();
          renderList();
        });
      }

      const clearSearchBtn = container.querySelector('#venue-clear-search-btn');
      if (clearSearchBtn) {
        clearSearchBtn.addEventListener('click', () => {
          searchQuery = '';
          renderList();
        });
      }

      const chips = container.querySelectorAll('.chip[data-type]');
      chips.forEach(chip => {
        chip.addEventListener('click', () => {
          selectedType = chip.getAttribute('data-type');
          renderList();
        });
      });

      const addBtn = container.querySelector('#venue-add-nav-btn');
      if (addBtn) {
        addBtn.addEventListener('click', () => {
          router.navigate('venue-create', { returnRoute, fieldKey, multiple });
        });
      }

      const emptyAddBtn = container.querySelector('#venue-empty-add-btn');
      if (emptyAddBtn) {
        emptyAddBtn.addEventListener('click', () => {
          router.navigate('venue-create', { returnRoute, fieldKey, multiple });
        });
      }

      // Pick venue action
      const pickBtns = container.querySelectorAll('.venue-pick-action-btn');
      pickBtns.forEach(btn => {
        btn.addEventListener('click', (e) => {
          e.stopPropagation();
          const vId = btn.getAttribute('data-id');
          if (returnRoute) {
            router.navigate(returnRoute, { [fieldKey]: vId, selectedVenueId: vId });
          } else {
            router.goBack();
          }
        });
      });

      // Click card to open detail
      const cards = container.querySelectorAll('.venue-card-item');
      cards.forEach(card => {
        card.addEventListener('click', (e) => {
          if (e.target.closest('.venue-pick-action-btn')) return;
          const vId = card.getAttribute('data-id');
          router.navigate('venue-detail', { id: vId, returnRoute, fieldKey, isPicker: isPicker ? 'true' : 'false' });
        });
      });
    }

    renderList();
  });

  /* =========================================================================
     VEN-02: Venue Detail View ('venue-detail')
     ========================================================================= */
  router.registerView('venue-detail', (container, params = {}) => {
    const venueId = params.id;
    const isPicker = params.isPicker === 'true' || !!params.returnRoute;
    const returnRoute = params.returnRoute || '';
    const fieldKey = params.fieldKey || 'venueId';

    const state = store.getState();
    const currentUserId = state.currentUserId;
    const currentUser = store.getCurrentUser();
    const venue = (state.venues || []).find(v => v.id === venueId);

    if (!venue) {
      router.setHeaderTitle('Chi tiết sân');
      container.innerHTML = `
        <div class="empty-state">
          <h3 class="empty-state-title">Không tìm thấy địa điểm</h3>
          <p class="empty-state-desc">Địa điểm này không tồn tại hoặc đã bị xóa khỏi hệ thống.</p>
          <button class="btn btn-primary" onclick="window.AppRouter.navigate('venues')">Quay lại danh sách</button>
        </div>
      `;
      return;
    }

    router.setHeaderTitle(venue.name);

    const isCreator = venue.createdBy === currentUserId;
    const isAdmin = currentUser?.role === 'ADMIN';
    const canEdit = isCreator || isAdmin;
    const isInactive = venue.status === 'INACTIVE';
    const primaryImg = (venue.images || []).find(img => img.isPrimary) || venue.images?.[0];

    container.innerHTML = `
      <div style="padding: 16px 0 24px 0;">
        <!-- Inactive Venue Alert (VEN-07) -->
        ${isInactive ? `
          <div class="notice notice-danger" style="margin-top: 0; margin-bottom: 16px;">
            <div style="display: flex; gap: 8px; align-items: flex-start;">
              <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="flex-shrink: 0; color: var(--color-error);">
                <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"></path>
                <line x1="12" y1="9" x2="12" y2="13"></line>
                <line x1="12" y1="17" x2="12.01" y2="17"></line>
              </svg>
              <div>
                <strong>Địa điểm đã ngừng hoạt động</strong>
                <p style="margin: 2px 0 0 0; font-size: 13px;">
                  ${venue.warning || 'Sân bóng này đã tạm ngừng nhận đặt trận và giao lưu. Không thể chọn cho các kèo đấu mới.'}
                </p>
              </div>
            </div>
          </div>
        ` : ''}

        <!-- Primary Photo Banner / Gallery Preview -->
        <div style="position: relative; border-radius: var(--radius-lg); overflow: hidden; background-color: var(--color-surface-container-high); height: 210px; margin-bottom: 16px;">
          ${primaryImg ? `
            <img src="${primaryImg.url}" alt="${venue.name}" style="width: 100%; height: 100%; object-fit: cover;">
          ` : `
            <div style="width: 100%; height: 100%; display: grid; place-items: center; color: var(--color-outline);">
              <div style="text-align: center;">
                <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                  <rect x="3" y="3" width="18" height="18" rx="2" ry="2"></rect>
                  <circle cx="8.5" cy="8.5" r="1.5"></circle>
                  <polyline points="21 15 16 10 5 21"></polyline>
                </svg>
                <div style="font-size: 12px; margin-top: 6px;">Chưa có ảnh sân</div>
              </div>
            </div>
          `}

          <!-- Photo Manager Badge Button -->
          <button class="btn btn-secondary" id="venue-photos-btn"
            style="position: absolute; bottom: 12px; right: 12px; min-height: 36px; padding: 6px 12px; background: rgba(255,255,255,0.92); backdrop-filter: blur(4px); box-shadow: var(--shadow-sm); font-size: 12px;">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M23 19a2 2 0 0 1-2 2H3a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h4l2-3h6l2 3h4a2 2 0 0 1 2 2z"></path>
              <circle cx="12" cy="13" r="4"></circle>
            </svg>
            <span>${(venue.images || []).length} / 5 Ảnh</span>
          </button>
        </div>

        <!-- Venue Core Details Card -->
        <div class="card">
          <div style="display: flex; justify-content: space-between; align-items: flex-start; gap: 8px; margin-bottom: 8px;">
            <div>
              <h2 style="margin: 0; font-size: var(--font-size-2xl); font-weight: var(--font-weight-extrabold); color: var(--color-on-surface);">
                ${venue.name}
              </h2>
              <span class="badge ${isInactive ? 'badge-red' : 'badge-green'}" style="margin-top: 4px;">
                ${isInactive ? 'NGỪNG HOẠT ĐỘNG' : 'ĐANG HOẠT ĐỘNG'}
              </span>
            </div>
            <span class="badge badge-gray" style="font-size: 13px;">${formatVenueType(venue.type)}</span>
          </div>

          <div class="list-group" style="margin-top: 12px;">
            <div class="list-item">
              <div style="display: flex; gap: 10px; align-items: flex-start;">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="color: var(--color-primary); flex-shrink: 0; margin-top: 2px;">
                  <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"></path>
                  <circle cx="12" cy="10" r="3"></circle>
                </svg>
                <div>
                  <div style="font-weight: var(--font-weight-medium); color: var(--color-on-surface); font-size: 14px;">
                    ${venue.address}
                  </div>
                  <div style="font-size: 12px; color: var(--color-on-surface-variant);">
                    Khu vực: ${getWardName(venue.wardId)}
                  </div>
                </div>
              </div>
            </div>

            ${venue.contactPhone ? `
              <div class="list-item">
                <div style="display: flex; gap: 10px; align-items: center;">
                  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="color: var(--color-primary); flex-shrink: 0;">
                    <path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z"></path>
                  </svg>
                  <div>
                    <span style="font-size: 12px; color: var(--color-on-surface-variant);">Hotline / Đặt sân:</span>
                    <a href="tel:${venue.contactPhone}" style="display: block; font-weight: var(--font-weight-semibold); color: var(--color-primary); text-decoration: none;">
                      ${venue.contactPhone}
                    </a>
                  </div>
                </div>
              </div>
            ` : ''}

            <div class="list-item">
              <div style="display: flex; gap: 10px; align-items: center;">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="color: var(--color-secondary); flex-shrink: 0;">
                  <circle cx="12" cy="12" r="10"></circle>
                  <line x1="2" y1="12" x2="22" y2="12"></line>
                  <path d="M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z"></path>
                </svg>
                <div style="font-size: 12px; color: var(--color-on-surface-variant);">
                  Tọa độ GPS: <code>${venue.coordinates?.lat || 10.78}, ${venue.coordinates?.lng || 106.70}</code>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Simulated Map Box -->
        <div class="card" style="padding: 12px;">
          <div style="height: 120px; border-radius: var(--radius-base); background: #e2e8f0; display: flex; flex-direction: column; align-items: center; justify-content: center; color: #64748b; font-size: 13px; text-align: center;">
            <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <polygon points="1 6 1 22 8 18 16 22 23 18 23 2 16 6 8 2 1 6"></polygon>
              <line x1="8" y1="2" x2="8" y2="18"></line>
              <line x1="16" y1="6" x2="16" y2="22"></line>
            </svg>
            <span style="margin-top: 6px; font-weight: 500;">Bản đồ định vị GPS (${venue.coordinates?.lat || 10.78}, ${venue.coordinates?.lng || 106.70})</span>
          </div>
        </div>

        <!-- Action Buttons -->
        <div style="display: flex; flex-direction: column; gap: 10px; margin-top: 14px;">
          ${isPicker && !isInactive ? `
            <button class="btn btn-primary btn-block" id="venue-detail-select-btn" style="min-height: 48px;">
              ✓ Chọn sân bóng này cho kèo
            </button>
          ` : ''}

          ${canEdit ? `
            <button class="btn btn-secondary btn-block" id="venue-detail-edit-btn">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"></path>
                <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"></path>
              </svg>
              <span>Sửa thông tin sân (Người tạo / Admin)</span>
            </button>
          ` : `
            <div style="font-size: 12px; color: var(--color-on-surface-variant); text-align: center;">
              Bạn không phải người tạo địa điểm này. Nếu phát hiện thông tin sai, hãy gửi báo cáo.
            </div>
          `}

          ${!isAdmin ? `
            <button class="btn btn-ghost btn-block" id="venue-detail-report-btn" style="color: var(--color-error);">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M4 15s1-1 4-1 5 2 8 2 4-1 4-1V3s-1 1-4 1-5-2-8-2-4 1-4 1z"></path>
                <line x1="4" y1="22" x2="4" y2="15"></line>
              </svg>
              <span>Báo cáo sai thông tin / Sân đóng cửa (VEN-06)</span>
            </button>
          ` : `
            <!-- Admin Toggle Inactive/Reactivate Action -->
            <button class="btn ${isInactive ? 'btn-primary' : 'btn-danger'} btn-block" id="venue-admin-toggle-status-btn">
              ${isInactive ? 'Kích hoạt lại sân bóng (Admin)' : 'Đánh dấu sân ngừng hoạt động (Admin)'}
            </button>
          `}
        </div>
      </div>
    `;

    // Handlers
    const selectBtn = container.querySelector('#venue-detail-select-btn');
    if (selectBtn) {
      selectBtn.addEventListener('click', () => {
        if (returnRoute) {
          router.navigate(returnRoute, { [fieldKey]: venue.id, selectedVenueId: venue.id });
        } else {
          router.goBack();
        }
      });
    }

    const photosBtn = container.querySelector('#venue-photos-btn');
    if (photosBtn) {
      photosBtn.addEventListener('click', () => {
        router.navigate('venue-photos', { id: venue.id });
      });
    }

    const editBtn = container.querySelector('#venue-detail-edit-btn');
    if (editBtn) {
      editBtn.addEventListener('click', () => {
        router.navigate('venue-edit', { id: venue.id });
      });
    }

    const reportBtn = container.querySelector('#venue-detail-report-btn');
    if (reportBtn) {
      reportBtn.addEventListener('click', () => {
        router.openSheet('venue-report-sheet', { venueId: venue.id, venueName: venue.name });
      });
    }

    const adminToggleBtn = container.querySelector('#venue-admin-toggle-status-btn');
    if (adminToggleBtn) {
      adminToggleBtn.addEventListener('click', () => {
        store.setState(s => {
          const target = s.venues.find(v => v.id === venue.id);
          if (target) {
            if (target.status === 'ACTIVE') {
              target.status = 'INACTIVE';
              target.warning = 'Admin đã đánh dấu sân ngừng hoạt động.';
            } else {
              target.status = 'ACTIVE';
              target.warning = null;
            }
          }
          return s;
        });
        router.showToast(`Đã cập nhật trạng thái sân thành ${venue.status === 'ACTIVE' ? 'NGỪNG HOẠT ĐỘNG' : 'HOẠT ĐỘNG'}`, 'info');
        router.navigate('venue-detail', { id: venue.id });
      });
    }
  });

  /* =========================================================================
     VEN-03: Add Venue View ('venue-create')
     ========================================================================= */
  router.registerView('venue-create', (container, params = {}) => {
    router.setHeaderTitle('Thêm địa điểm sân');

    const returnRoute = params.returnRoute || '';
    const fieldKey = params.fieldKey || 'venueId';
    const state = store.getState();
    const wards = state.wards || [];

    container.innerHTML = `
      <div style="padding: 16px 0 24px 0;">
        <div class="notice notice-info" style="margin-top: 0; margin-bottom: 16px;">
          <strong>Hiệu lực tức thì:</strong> Địa điểm bạn thêm sẽ có hiệu lực ngay trong hệ thống và bạn có thể chọn ngay cho kèo đấu của mình (AC-V01).
        </div>

        <form id="venue-create-form" novalidate>
          <!-- Tên sân -->
          <div class="form-group">
            <label class="form-label">
              Tên sân bóng <span class="required-mark">*</span>
            </label>
            <input type="text" class="form-input" id="v-name" placeholder="Ví dụ: Sân bóng Hoa Lư" maxlength="200" required>
            <div class="field-error-msg" id="v-name-error" style="display: none;"></div>
          </div>

          <!-- Địa chỉ -->
          <div class="form-group">
            <label class="form-label">
              Địa chỉ chi tiết <span class="required-mark">*</span>
            </label>
            <input type="text" class="form-input" id="v-address" placeholder="Ví dụ: 2 Đinh Tiên Hoàng, Phường Đa Kao" maxlength="500" required>
            <div class="field-error-msg" id="v-address-error" style="display: none;"></div>
          </div>

          <!-- Phường / Xã -->
          <div class="form-group">
            <label class="form-label">
              Phường / Xã <span class="required-mark">*</span>
            </label>
            <select class="form-select" id="v-ward" required>
              <option value="">-- Chọn Phường / Xã --</option>
              ${wards.map(w => `<option value="${w.id}">${w.name} (${w.district}, ${w.city})</option>`).join('')}
            </select>
            <div class="field-error-msg" id="v-ward-error" style="display: none;"></div>
          </div>

          <!-- Loại sân -->
          <div class="form-group">
            <label class="form-label">Quy mô sân</label>
            <select class="form-select" id="v-type">
              <option value="SEVEN_A_SIDE" selected>Sân 7 người (Phổ biến)</option>
              <option value="FIVE_A_SIDE">Sân 5 người (Futsal / Mini)</option>
              <option value="ELEVEN_A_SIDE">Sân 11 người (Tiêu chuẩn)</option>
            </select>
          </div>

          <!-- Số điện thoại liên hệ -->
          <div class="form-group">
            <label class="form-label">Số điện thoại liên hệ sân</label>
            <input type="tel" class="form-input" id="v-phone" placeholder="0901234567 (10-11 số)" maxlength="11">
            <div class="field-error-msg" id="v-phone-error" style="display: none;"></div>
          </div>

          <!-- Website -->
          <div class="form-group">
            <label class="form-label">Website hoặc Fanpage</label>
            <input type="url" class="form-input" id="v-website" placeholder="https://..." maxlength="500">
            <div class="field-error-msg" id="v-website-error" style="display: none;"></div>
          </div>

          <!-- Buttons -->
          <div style="margin-top: 24px; display: flex; flex-direction: column; gap: 10px;">
            <button type="submit" class="btn btn-primary btn-block" id="v-submit-btn">
              Lưu địa điểm & Sử dụng ngay
            </button>
            <button type="button" class="btn btn-secondary btn-block" id="v-cancel-btn">
              Hủy bỏ
            </button>
          </div>
        </form>
      </div>
    `;

    const form = container.querySelector('#venue-create-form');
    const cancelBtn = container.querySelector('#v-cancel-btn');

    if (cancelBtn) {
      cancelBtn.addEventListener('click', () => {
        router.goBack();
      });
    }

    if (form) {
      form.addEventListener('submit', (e) => {
        e.preventDefault();

      const nameInput = container.querySelector('#v-name');
      const addrInput = container.querySelector('#v-address');
      const wardInput = container.querySelector('#v-ward');
      const typeInput = container.querySelector('#v-type');
      const phoneInput = container.querySelector('#v-phone');
      const webInput = container.querySelector('#v-website');

      const nameErr = container.querySelector('#v-name-error');
      const addrErr = container.querySelector('#v-address-error');
      const wardErr = container.querySelector('#v-ward-error');
      const phoneErr = container.querySelector('#v-phone-error');
      const webErr = container.querySelector('#v-website-error');

      // Reset errors
      [nameErr, addrErr, wardErr, phoneErr, webErr].forEach(el => {
        el.style.display = 'none';
        el.textContent = '';
      });
      [nameInput, addrInput, wardInput, phoneInput, webInput].forEach(el => {
        el.classList.remove('is-invalid');
      });

      let hasError = false;

      const nameVal = nameInput.value.trim();
      if (!nameVal) {
        nameErr.textContent = 'Vui lòng nhập tên sân bóng';
        nameErr.style.display = 'block';
        nameInput.classList.add('is-invalid');
        hasError = true;
      }

      const addrVal = addrInput.value.trim();
      if (!addrVal) {
        addrErr.textContent = 'Vui lòng nhập địa chỉ sân';
        addrErr.style.display = 'block';
        addrInput.classList.add('is-invalid');
        hasError = true;
      }

      const wardVal = wardInput.value;
      if (!wardVal) {
        wardErr.textContent = 'Vui lòng chọn phường / xã';
        wardErr.style.display = 'block';
        wardInput.classList.add('is-invalid');
        hasError = true;
      }

      const phoneVal = phoneInput.value.trim();
      if (phoneVal && !/^0[0-9]{9,10}$/.test(phoneVal)) {
        phoneErr.textContent = 'Số điện thoại phải gồm 10-11 chữ số và bắt đầu bằng số 0';
        phoneErr.style.display = 'block';
        phoneInput.classList.add('is-invalid');
        hasError = true;
      }

      const webVal = webInput.value.trim();
      if (webVal && !/^https?:\/\//i.test(webVal)) {
        webErr.textContent = 'Địa chỉ website phải bắt đầu bằng http:// hoặc https://';
        webErr.style.display = 'block';
        webInput.classList.add('is-invalid');
        hasError = true;
      }

      if (hasError) return;

      // Simulated error trigger check
      const simErr = store.consumeSimulatedError();
      if (simErr) {
        router.showToast(`Lỗi mô phỏng: ${simErr}`, 'error');
        return;
      }

      // Create venue
      const currentUserId = store.getState().currentUserId;
      const newVenue = {
        id: 'ven-' + Date.now(),
        name: nameVal,
        address: addrVal,
        wardId: wardVal,
        type: typeInput.value,
        contactPhone: phoneVal || null,
        website: webVal || null,
        coordinates: { lat: 10.785, lng: 106.712 },
        status: 'ACTIVE',
        createdBy: currentUserId,
        images: [
          {
            id: 'img-' + Date.now(),
            url: 'https://images.unsplash.com/photo-1575361204480-aadea25e6e68?w=500&q=80',
            isPrimary: true
          }
        ],
        reportCount: 0,
        createdAt: store.getState().meta.mockClock
      };

      store.setState(s => {
        s.venues.unshift(newVenue);
        return s;
      });

      router.showToast('Thêm địa điểm sân thành công!', 'success');

      // Return to form support (J-16)
      if (returnRoute) {
        router.navigate(returnRoute, { [fieldKey]: newVenue.id, selectedVenueId: newVenue.id });
      } else {
        router.navigate('venue-detail', { id: newVenue.id });
      }
    });
  }
});

  /* =========================================================================
     VEN-04: Edit Venue View ('venue-edit')
     ========================================================================= */
  router.registerView('venue-edit', (container, params = {}) => {
    const venueId = params.id;
    const state = store.getState();
    const currentUserId = state.currentUserId;
    const currentUser = store.getCurrentUser();
    const venue = (state.venues || []).find(v => v.id === venueId);
    const wards = state.wards || [];

    if (!venue) {
      router.setHeaderTitle('Sửa sân');
      container.innerHTML = `<div class="notice notice-danger">Không tìm thấy địa điểm</div>`;
      return;
    }

    const isCreator = venue.createdBy === currentUserId;
    const isAdmin = currentUser?.role === 'ADMIN';

    if (!isCreator && !isAdmin) {
      router.setHeaderTitle('Không có quyền');
      container.innerHTML = `
        <div class="empty-state">
          <div class="empty-state-icon" style="color: var(--color-error);">
            <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <rect x="3" y="11" width="18" height="11" rx="2" ry="2"></rect>
              <path d="M7 11V7a5 5 0 0 1 10 0v4"></path>
            </svg>
          </div>
          <h3 class="empty-state-title">Không thể sửa thông tin</h3>
          <p class="empty-state-desc">Chỉ người tạo địa điểm hoặc Quản trị viên (Admin) mới có quyền chỉnh sửa thông tin sân.</p>
          <div style="margin-top: 14px; display: flex; gap: 8px; justify-content: center;">
            <button class="btn btn-secondary" onclick="window.AppRouter.goBack()">Quay lại</button>
            <button class="btn btn-primary" onclick="window.AppRouter.openSheet('venue-report-sheet', { venueId: '${venue.id}', venueName: '${venue.name}' })">
              Báo cáo thông tin sai
            </button>
          </div>
        </div>
      `;
      return;
    }

    router.setHeaderTitle(`Sửa: ${venue.name}`);

    container.innerHTML = `
      <div style="padding: 16px 0 24px 0;">
        <form id="venue-edit-form" novalidate>
          <div class="form-group">
            <label class="form-label">Tên sân bóng <span class="required-mark">*</span></label>
            <input type="text" class="form-input" id="ve-name" value="${venue.name}" maxlength="200" required>
            <div class="field-error-msg" id="ve-name-error" style="display: none;"></div>
          </div>

          <div class="form-group">
            <label class="form-label">Địa chỉ chi tiết <span class="required-mark">*</span></label>
            <input type="text" class="form-input" id="ve-address" value="${venue.address}" maxlength="500" required>
            <div class="field-error-msg" id="ve-address-error" style="display: none;"></div>
          </div>

          <div class="form-group">
            <label class="form-label">Phường / Xã <span class="required-mark">*</span></label>
            <select class="form-select" id="ve-ward" required>
              ${wards.map(w => `
                <option value="${w.id}" ${w.id === venue.wardId ? 'selected' : ''}>
                  ${w.name} (${w.district}, ${w.city})
                </option>
              `).join('')}
            </select>
            <div class="field-error-msg" id="ve-ward-error" style="display: none;"></div>
          </div>

          <div class="form-group">
            <label class="form-label">Quy mô sân</label>
            <select class="form-select" id="ve-type">
              <option value="SEVEN_A_SIDE" ${venue.type === 'SEVEN_A_SIDE' ? 'selected' : ''}>Sân 7 người</option>
              <option value="FIVE_A_SIDE" ${venue.type === 'FIVE_A_SIDE' ? 'selected' : ''}>Sân 5 người</option>
              <option value="ELEVEN_A_SIDE" ${venue.type === 'ELEVEN_A_SIDE' ? 'selected' : ''}>Sân 11 người</option>
            </select>
          </div>

          <div class="form-group">
            <label class="form-label">Số điện thoại liên hệ</label>
            <input type="tel" class="form-input" id="ve-phone" value="${venue.contactPhone || ''}" maxlength="11">
            <div class="field-error-msg" id="ve-phone-error" style="display: none;"></div>
          </div>

          <div class="form-group">
            <label class="form-label">Website hoặc Fanpage</label>
            <input type="url" class="form-input" id="ve-website" value="${venue.website || ''}" maxlength="500">
            <div class="field-error-msg" id="ve-website-error" style="display: none;"></div>
          </div>

          <div style="margin-top: 24px; display: flex; flex-direction: column; gap: 10px;">
            <button type="submit" class="btn btn-primary btn-block">
              Lưu thay đổi
            </button>
            <button type="button" class="btn btn-secondary btn-block" id="ve-cancel-btn">
              Hủy bỏ
            </button>
          </div>
        </form>
      </div>
    `;

    const form = container.querySelector('#venue-edit-form');
    const cancelBtn = container.querySelector('#ve-cancel-btn');

    if (cancelBtn) cancelBtn.addEventListener('click', () => router.goBack());

    if (form) {
      form.addEventListener('submit', (e) => {
        e.preventDefault();

      const nameVal = container.querySelector('#ve-name').value.trim();
      const addrVal = container.querySelector('#ve-address').value.trim();
      const wardVal = container.querySelector('#ve-ward').value;
      const typeVal = container.querySelector('#ve-type').value;
      const phoneVal = container.querySelector('#ve-phone').value.trim();
      const webVal = container.querySelector('#ve-website').value.trim();

      if (!nameVal || !addrVal || !wardVal) {
        router.showToast('Vui lòng điền đầy đủ các thông tin bắt buộc', 'error');
        return;
      }

      store.setState(s => {
        const target = s.venues.find(v => v.id === venue.id);
        if (target) {
          target.name = nameVal;
          target.address = addrVal;
          target.wardId = wardVal;
          target.type = typeVal;
          target.contactPhone = phoneVal || null;
          target.website = webVal || null;
          target.updatedAt = s.meta.mockClock;
        }
        return s;
      });

      router.showToast('Cập nhật địa điểm thành công', 'success');
      router.navigate('venue-detail', { id: venue.id });
    });
  }
});

  /* =========================================================================
     VEN-05: Photo Manager View ('venue-photos')
     ========================================================================= */
  router.registerView('venue-photos', (container, params = {}) => {
    const venueId = params.id;
    const state = store.getState();
    const currentUserId = state.currentUserId;
    const currentUser = store.getCurrentUser();
    const venue = (state.venues || []).find(v => v.id === venueId);

    if (!venue) {
      router.setHeaderTitle('Quản lý ảnh');
      container.innerHTML = `<div class="notice notice-danger">Không tìm thấy địa điểm</div>`;
      return;
    }

    router.setHeaderTitle(`Ảnh sân (${(venue.images || []).length}/5)`);

    const isCreator = venue.createdBy === currentUserId;
    const isAdmin = currentUser?.role === 'ADMIN';
    const canDelete = isCreator || isAdmin;
    const isInactive = venue.status === 'INACTIVE';
    const canUpload = !isInactive || isAdmin;
    const images = venue.images || [];

    function renderPhotoManager() {
      container.innerHTML = `
        <div style="padding: 16px 0 24px 0;">
          <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 14px;">
            <div>
              <h3 style="margin: 0; font-size: var(--font-size-lg); font-weight: var(--font-weight-bold);">
                Bộ sưu tập ảnh (${images.length} / 5)
              </h3>
              <p style="margin: 2px 0 0 0; font-size: 12px; color: var(--color-on-surface-variant);">
                Tối đa 5 ảnh. Luôn có đúng 1 ảnh chính đại diện cho địa điểm.
              </p>
            </div>
            ${images.length < 5 && canUpload ? `
              <button class="btn btn-primary" id="photo-upload-sim-btn" style="min-height: 40px; padding: 6px 14px;">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path>
                  <polyline points="17 8 12 3 7 8"></polyline>
                  <line x1="12" y1="3" x2="12" y2="15"></line>
                </svg>
                <span>Tải ảnh lên</span>
              </button>
            ` : ''}
          </div>

          <!-- Upload Progress Simulation Indicator (Hidden by default) -->
          <div id="photo-upload-progress" class="notice notice-info" style="display: none; margin-bottom: 14px;">
            <div style="display: flex; align-items: center; justify-content: space-between; margin-bottom: 6px;">
              <span style="font-weight: 600;">Đang tải ảnh lên Cloudinary...</span>
              <span id="photo-progress-percent">0%</span>
            </div>
            <div style="width: 100%; height: 6px; background: rgba(0,0,0,0.1); border-radius: 3px; overflow: hidden;">
              <div id="photo-progress-bar" style="width: 0%; height: 100%; background: var(--color-primary); transition: width 0.2s ease;"></div>
            </div>
          </div>

          ${images.length === 0 ? `
            <div class="empty-state">
              <div class="empty-state-icon">
                <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <rect x="3" y="3" width="18" height="18" rx="2" ry="2"></rect>
                  <circle cx="8.5" cy="8.5" r="1.5"></circle>
                  <polyline points="21 15 16 10 5 21"></polyline>
                </svg>
              </div>
              <h4 class="empty-state-title">Chưa có ảnh nào</h4>
              <p class="empty-state-desc">Hãy tải lên ảnh thực tế của sân để các đội bóng dễ dàng nhận diện và chọn sân.</p>
            </div>
          ` : `
            <div style="display: flex; flex-direction: column; gap: 14px;">
              ${images.map((img, idx) => `
                <div class="card" style="padding: 12px; margin-bottom: 0;">
                  <div style="display: flex; gap: 12px; align-items: center;">
                    <div style="width: 100px; height: 75px; border-radius: var(--radius-base); overflow: hidden; background: #e2e8f0; flex-shrink: 0; position: relative;">
                      <img src="${img.url}" alt="Ảnh sân ${idx + 1}" style="width: 100%; height: 100%; object-fit: cover;">
                      ${img.isPrimary ? `
                        <span style="position: absolute; top: 4px; left: 4px; background: var(--color-primary); color: #fff; font-size: 10px; font-weight: 700; padding: 2px 6px; border-radius: 4px;">
                          ẢNH CHÍNH
                        </span>
                      ` : ''}
                    </div>

                    <div style="flex: 1; min-width: 0;">
                      <div style="font-weight: 600; font-size: 14px; margin-bottom: 2px;">
                        Ảnh #${idx + 1}
                      </div>
                      <div style="font-size: 12px; color: var(--color-on-surface-variant); margin-bottom: 8px;">
                        ${img.isPrimary ? 'Đang được đặt làm ảnh đại diện chính' : 'Ảnh phụ trong bộ sưu tập'}
                      </div>

                      <div style="display: flex; gap: 8px; flex-wrap: wrap;">
                        ${!img.isPrimary ? `
                          <button class="btn btn-outline set-primary-btn" data-img-id="${img.id}" style="min-height: 34px; padding: 4px 10px; font-size: 12px;">
                            ⭐ Đặt làm ảnh chính
                          </button>
                        ` : ''}

                        ${canDelete ? `
                          <button class="btn btn-danger-outline delete-img-btn" data-img-id="${img.id}" style="min-height: 34px; padding: 4px 10px; font-size: 12px;">
                            🗑 Xóa ảnh
                          </button>
                        ` : ''}
                      </div>
                    </div>
                  </div>
                </div>
              `).join('')}
            </div>
          `}

          <div style="margin-top: 24px;">
            <button class="btn btn-secondary btn-block" onclick="window.AppRouter.navigate('venue-detail', { id: '${venue.id}' })">
              Quay lại chi tiết sân
            </button>
          </div>
        </div>
      `;

      // Handlers
      const uploadBtn = container.querySelector('#photo-upload-sim-btn');
      if (uploadBtn) {
        uploadBtn.addEventListener('click', () => {
          if (images.length >= 5) {
            router.showToast('Địa điểm đã đạt tối đa 5 ảnh (BR-V02)', 'error');
            return;
          }

          const progressBox = container.querySelector('#photo-upload-progress');
          const progressBar = container.querySelector('#photo-progress-bar');
          const progressPercent = container.querySelector('#photo-progress-percent');

          progressBox.style.display = 'block';
          uploadBtn.disabled = true;

          let percent = 0;
          const interval = setInterval(() => {
            percent += 25;
            progressBar.style.width = percent + '%';
            progressPercent.textContent = percent + '%';

            if (percent >= 100) {
              clearInterval(interval);
              setTimeout(() => {
                const sampleUrls = [
                  'https://images.unsplash.com/photo-1575361204480-aadea25e6e68?w=500&q=80',
                  'https://images.unsplash.com/photo-1529900748604-07564a03e7a6?w=500&q=80',
                  'https://images.unsplash.com/photo-1489944445301-ff510f8699ae?w=500&q=80',
                  'https://images.unsplash.com/photo-1518091043644-c1d4457512c6?w=500&q=80'
                ];
                const newImg = {
                  id: 'img-' + Date.now(),
                  url: sampleUrls[images.length % sampleUrls.length],
                  isPrimary: images.length === 0 // If first image, auto-primary
                };

                store.setState(s => {
                  const target = s.venues.find(v => v.id === venue.id);
                  if (target) {
                    if (!target.images) target.images = [];
                    target.images.push(newImg);
                  }
                  return s;
                });

                router.showToast('Đã tải ảnh lên thành công!', 'success');
                router.navigate('venue-photos', { id: venue.id });
              }, 200);
            }
          }, 120);
        });
      }

      // Set primary handlers
      const primaryBtns = container.querySelectorAll('.set-primary-btn');
      primaryBtns.forEach(btn => {
        btn.addEventListener('click', () => {
          const imgId = btn.getAttribute('data-img-id');
          store.setState(s => {
            const target = s.venues.find(v => v.id === venue.id);
            if (target && target.images) {
              target.images.forEach(img => {
                img.isPrimary = (img.id === imgId);
              });
            }
            return s;
          });
          router.showToast('Đã đổi ảnh chính cho sân', 'success');
          router.navigate('venue-photos', { id: venue.id });
        });
      });

      // Delete image handlers
      const delBtns = container.querySelectorAll('.delete-img-btn');
      delBtns.forEach(btn => {
        btn.addEventListener('click', () => {
          const imgId = btn.getAttribute('data-img-id');
          store.setState(s => {
            const target = s.venues.find(v => v.id === venue.id);
            if (target && target.images) {
              const wasPrimary = target.images.find(i => i.id === imgId)?.isPrimary;
              target.images = target.images.filter(i => i.id !== imgId);
              // BR-V08: If primary deleted, next remaining becomes primary
              if (wasPrimary && target.images.length > 0) {
                target.images[0].isPrimary = true;
              }
            }
            return s;
          });
          router.showToast('Đã xóa ảnh khỏi sân bóng', 'info');
          router.navigate('venue-photos', { id: venue.id });
        });
      });
    }

    renderPhotoManager();
  });

  /* =========================================================================
     VEN-06: Report Venue Sheet ('venue-report-sheet')
     ========================================================================= */
  router.registerSheet('venue-report-sheet', (body, footer, params = {}, titleElem) => {
    const venueId = params.venueId;
    const venueName = params.venueName || 'sân bóng';
    const state = store.getState();
    const currentUserId = state.currentUserId;
    const currentUser = store.getCurrentUser();

    if (titleElem) titleElem.textContent = 'Báo cáo địa điểm';

    if (currentUser?.role === 'ADMIN') {
      body.innerHTML = `
        <div class="notice notice-warning">
          Quản trị viên (Admin) không thể gửi báo cáo sân bóng mà thực hiện trực tiếp chức năng sửa hoặc ngừng hoạt động trong cổng quản trị.
        </div>
      `;
      footer.innerHTML = `
        <button class="btn btn-secondary btn-block" onclick="window.AppRouter.closeSheet()">Đóng</button>
      `;
      return;
    }

    body.innerHTML = `
      <div style="padding-bottom: 8px;">
        <p style="font-size: 13px; color: var(--color-on-surface-variant); margin-top: 0;">
          Báo cáo vấn đề về <strong>${venueName}</strong>. Báo cáo sẽ được chuyển tới ban quản trị xác minh (BR-V04).
        </p>

        <div class="form-group">
          <label class="form-label">Lý do báo cáo <span class="required-mark">*</span></label>
          <div style="display: flex; flex-direction: column; gap: 8px; margin-top: 4px;">
            <label style="display: flex; align-items: center; gap: 8px; font-size: 14px; cursor: pointer;">
              <input type="radio" name="venue-report-reason" value="CLOSED" checked>
              <span>Sân đã ngừng hoạt động / đóng cửa giải tỏa</span>
            </label>
            <label style="display: flex; align-items: center; gap: 8px; font-size: 14px; cursor: pointer;">
              <input type="radio" name="venue-report-reason" value="WRONG_INFO">
              <span>Sai địa chỉ / số điện thoại / thông tin sân</span>
            </label>
            <label style="display: flex; align-items: center; gap: 8px; font-size: 14px; cursor: pointer;">
              <input type="radio" name="venue-report-reason" value="OTHER">
              <span>Lý do khác</span>
            </label>
          </div>
        </div>

        <div class="form-group" style="margin-bottom: 0;">
          <label class="form-label">Mô tả chi tiết</label>
          <textarea class="form-textarea" id="venue-report-detail" placeholder="Cung cấp thêm thông tin giúp ban quản trị xử lý nhanh hơn..." maxlength="1000"></textarea>
        </div>
      </div>
    `;

    footer.innerHTML = `
      <div style="display: flex; gap: 8px; width: 100%;">
        <button class="btn btn-secondary" style="flex: 1;" onclick="window.AppRouter.closeSheet()">Hủy</button>
        <button class="btn btn-danger" style="flex: 2;" id="venue-report-submit-btn">Gửi báo cáo</button>
      </div>
    `;

    const submitBtn = footer.querySelector('#venue-report-submit-btn');
    if (submitBtn) {
      submitBtn.addEventListener('click', () => {
        const checkedRadio = body.querySelector('input[name="venue-report-reason"]:checked');
        const reason = checkedRadio ? checkedRadio.value : 'OTHER';
        const detail = body.querySelector('#venue-report-detail').value.trim();

        // Check if pending report already exists (BR-V09)
        const existingReport = (state.reports || []).find(
          r => r.reporterId === currentUserId && r.reportedId === venueId && r.reason === reason && r.status === 'PENDING'
        );
        if (existingReport) {
          router.showToast('Bạn đã có báo cáo đang chờ xử lý cùng lý do cho sân này (BR-V09)', 'warning');
          router.closeSheet();
          return;
        }

        const newReport = {
          id: 'rep-ven-' + Date.now(),
          reporterType: 'USER',
          reporterId: currentUserId,
          reportedId: venueId,
          type: 'VENUE_REPORT',
          reason: reason,
          detail: detail || '',
          status: 'PENDING',
          createdAt: state.meta.mockClock
        };

        store.setState(s => {
          if (!s.reports) s.reports = [];
          s.reports.push(newReport);
          const v = (s.venues || []).find(item => item.id === venueId);
          if (v) v.reportCount = (v.reportCount || 0) + 1;
          return s;
        });

        router.closeSheet();
        router.showToast('Gửi báo cáo sân bóng thành công! Admin sẽ tiến hành xác minh.', 'success');
      });
    }
  });

  /* =========================================================================
     Bottom Sheet: Venue Quick Picker ('venue-picker-sheet')
     ========================================================================= */
  router.registerSheet('venue-picker-sheet', (body, footer, params = {}, titleElem) => {
    if (titleElem) titleElem.textContent = 'Chọn sân bóng';
    const onSelect = params.onSelect;

    const state = store.getState();
    const activeVenues = (state.venues || []).filter(v => v.status === 'ACTIVE');

    body.innerHTML = `
      <div style="max-height: 380px; overflow-y: auto; padding-right: 4px;">
        <div style="display: flex; flex-direction: column; gap: 8px;">
          ${activeVenues.map(v => `
            <div class="card card-interactive venue-sheet-pick-item" data-id="${v.id}" data-name="${v.name}" style="padding: 10px; margin-bottom: 0;">
              <div style="font-weight: 600; font-size: 14px;">${v.name}</div>
              <div style="font-size: 12px; color: var(--color-on-surface-variant); margin-top: 2px;">${v.address}</div>
              <span class="badge badge-gray" style="margin-top: 4px; font-size: 11px;">${formatVenueType(v.type)}</span>
            </div>
          `).join('')}
        </div>
      </div>
    `;

    footer.innerHTML = `
      <button class="btn btn-secondary btn-block" onclick="window.AppRouter.closeSheet()">Đóng</button>
    `;

    const items = body.querySelectorAll('.venue-sheet-pick-item');
    items.forEach(item => {
      item.addEventListener('click', () => {
        const id = item.getAttribute('data-id');
        const name = item.getAttribute('data-name');
        if (typeof onSelect === 'function') {
          onSelect({ id, name });
        }
        router.closeSheet();
      });
    });
  });
}
