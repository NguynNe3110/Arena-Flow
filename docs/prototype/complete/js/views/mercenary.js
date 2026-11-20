/**
 * Mercenary & External Match Views & Sheets (MERC-01..12, EXT-01..05)
 * Owned by @ui-merc-chat
 * 
 * Strict compliance with:
 * - AC-P05: Strictly hide venue name/address/coords/map from users who are NOT approved mercenaries or captain!
 * - AC-P06: Approved withdrawal deducts reliability score and re-opens listing if filled; pending withdrawal free.
 * - Journeys: J-12, J-13, J-14, J-15
 */

export function initMercenaryViews(router, store) {

  // Extend store with missing mutators for External Matches and Recruitments if not present
  _ensureStoreMethods(store);

  /* =========================================================================
     MERC-01: Find mercenary listings (route: 'players')
     ========================================================================= */
  router.registerView('players', (container, params) => {
    router.setHeaderTitle('Đá lẻ — Tìm đội');

    const state = store.getState();
    const currentUser = store.getCurrentUser();
    const isCaptain = store.getMyCaptainedTeams().length > 0;

    // Filters state
    let selectedPosition = params.position || 'ALL';
    let selectedRadius = parseInt(params.radius || '3', 10); // default 3km per MERC-01
    let activeSubTab = params.tab || 'listings'; // 'listings' | 'my-apps' | 'team-rec'

    function renderView() {
      const currentState = store.getState();
      const currentU = store.getCurrentUser();
      const gpsStatus = currentState.meta?.gpsStatus || 'granted';

      // Base user coordinates (An Phú area reference)
      const userCoords = { lat: 10.791, lng: 106.742 };

      // Helper to calculate approximate distance rounded to 0.5km per §1.4
      function calcDistance(venueCoords) {
        if (!venueCoords || gpsStatus !== 'granted') return null;
        const dLat = (venueCoords.lat - userCoords.lat) * 111;
        const dLng = (venueCoords.lng - userCoords.lng) * 111 * Math.cos(userCoords.lat * Math.PI / 180);
        const dist = Math.sqrt(dLat * dLat + dLng * dLng);
        const rounded = Math.round(dist * 2) / 2;
        return rounded < 0.5 ? 0.5 : rounded;
      }

      // Filter recruitments
      const allRecruitments = currentState.recruitments || [];
      const filteredListings = allRecruitments.filter(rec => {
        // Exclude closed/expired in public search unless applied
        if (rec.status !== 'OPEN' && rec.status !== 'FILLED') return false;

        // Position filter
        if (selectedPosition !== 'ALL' && rec.position !== selectedPosition) return false;

        // Venue & distance
        const venue = currentState.venues.find(v => v.id === rec.venueId);
        if (venue) {
          const dist = calcDistance(venue.coordinates);
          rec._distKm = dist;
          if (gpsStatus === 'granted' && dist !== null && dist > selectedRadius) {
            return false;
          }
        }
        return true;
      });

      // User's own applications count
      const myApps = (currentState.applications || []).filter(a => a.applicantId === currentState.currentUserId);
      const myPendingCount = myApps.filter(a => a.status === 'PENDING').length;
      const myApprovedCount = myApps.filter(a => a.status === 'APPROVED').length;

      // Captain's team recruitments count
      const myCaptainedTeams = store.getMyCaptainedTeams();
      const myTeamIds = myCaptainedTeams.map(t => t.id);
      const teamRecs = (currentState.recruitments || []).filter(r => myTeamIds.includes(r.teamId));

      container.innerHTML = `
        <div style="padding: 16px 0;">
          
          <!-- Top Sub-Navigation Tabs -->
          <div style="display: flex; gap: 8px; margin-bottom: 16px; border-bottom: 1px solid var(--color-outline-variant); padding-bottom: 8px;">
            <button class="btn btn-sm ${activeSubTab === 'listings' ? 'btn-primary' : 'btn-ghost'}" id="tab-btn-listings">
              Tìm tin tuyển
            </button>
            <button class="btn btn-sm ${activeSubTab === 'my-apps' ? 'btn-primary' : 'btn-ghost'}" id="tab-btn-my-apps">
              Đơn của tôi ${myApps.length > 0 ? `<span class="badge ${myPendingCount > 0 ? 'badge-amber' : 'badge-gray'}" style="margin-left: 4px;">${myApps.length}</span>` : ''}
            </button>
            ${isCaptain ? `
              <button class="btn btn-sm ${activeSubTab === 'team-rec' ? 'btn-primary' : 'btn-ghost'}" id="tab-btn-team-rec">
                Tin đội tuyển (${teamRecs.length})
              </button>
            ` : ''}
          </div>

          <!-- TAB CONTENT: MY APPLICATIONS (MERC-04 EMBEDDED / ROUTABLE) -->
          ${activeSubTab === 'my-apps' ? renderMyApplicationsTab(myApps, currentState) : ''}

          <!-- TAB CONTENT: TEAM RECRUITMENTS (MERC-09 EMBEDDED / ROUTABLE) -->
          ${activeSubTab === 'team-rec' ? renderTeamRecruitmentsTab(teamRecs, currentState) : ''}

          <!-- TAB CONTENT: FIND LISTINGS (MERC-01) -->
          ${activeSubTab === 'listings' ? `
            
            <!-- GPS & Location Status Banner -->
            <div class="card" style="padding: 12px 16px; margin-bottom: 16px; background-color: var(--color-surface-container-low);">
              <div style="display: flex; align-items: center; justify-content: space-between;">
                <div style="display: flex; align-items: center; gap: 8px;">
                  <span style="font-size: 18px;">${gpsStatus === 'granted' ? '📍' : '⚠️'}</span>
                  <div>
                    <div style="font-size: 13px; font-weight: 600;">
                      ${gpsStatus === 'granted' ? 'Đang định vị bằng GPS' : 'GPS chưa bật — Dùng khu vực hồ sơ'}
                    </div>
                    <div style="font-size: 11px; color: var(--color-on-surface-variant);">
                      ${gpsStatus === 'granted' 
                        ? `Bán kính tìm kiếm: <strong>${selectedRadius} km</strong> (mặc định 3km)` 
                        : (currentU?.wardId ? 'Phường An Khánh, TP. Thủ Đức' : 'Chưa có khu vực trong hồ sơ')}
                    </div>
                  </div>
                </div>

                <div style="display: flex; gap: 6px;">
                  <button class="btn btn-sm btn-outline" id="btn-toggle-gps" style="font-size: 12px; padding: 4px 8px;">
                    ${gpsStatus === 'granted' ? 'Tắt GPS' : 'Bật GPS'}
                  </button>
                </div>
              </div>

              ${!currentU?.wardId && gpsStatus !== 'granted' ? `
                <div class="notice notice-warning" style="margin-top: 10px; font-size: 12px;">
                  Bạn chưa cập nhật khu vực trong hồ sơ. 
                  <a href="#/profile-edit" style="color: var(--color-primary); font-weight: 700; text-decoration: underline;">
                    Hoàn thiện hồ sơ ngay
                  </a> để tìm tin chính xác.
                </div>
              ` : ''}

              <!-- Radius Selector (1 - 20km) per MERC-01 -->
              ${gpsStatus === 'granted' ? `
                <div style="margin-top: 10px; display: flex; align-items: center; gap: 8px; overflow-x: auto; padding-top: 4px;">
                  <span style="font-size: 12px; color: var(--color-on-surface-variant); white-space: nowrap;">Bán kính:</span>
                  ${[1, 3, 5, 10, 20].map(r => `
                    <button class="chip ${selectedRadius === r ? 'is-active' : ''}" data-radius="${r}" style="font-size: 11px; padding: 4px 10px; min-height: 28px;">
                      ${r} km
                    </button>
                  `).join('')}
                </div>
              ` : ''}
            </div>

            <!-- Position Filter Chips -->
            <div style="margin-bottom: 16px;">
              <div class="chip-group">
                <button class="chip ${selectedPosition === 'ALL' ? 'is-active' : ''}" data-pos="ALL">Tất cả vị trí</button>
                <button class="chip ${selectedPosition === 'GOALKEEPER' ? 'is-active' : ''}" data-pos="GOALKEEPER">🧤 Thủ môn</button>
                <button class="chip ${selectedPosition === 'DEFENDER' ? 'is-active' : ''}" data-pos="DEFENDER">🛡️ Hậu vệ</button>
                <button class="chip ${selectedPosition === 'MIDFIELDER' ? 'is-active' : ''}" data-pos="MIDFIELDER">⚡ Tiền vệ</button>
                <button class="chip ${selectedPosition === 'FORWARD' ? 'is-active' : ''}" data-pos="FORWARD">⚽ Tiền đạo</button>
              </div>
            </div>

            <!-- Header and Action for Captain -->
            <div style="display: flex; align-items: center; justify-content: space-between; margin-bottom: 12px;">
              <span style="font-size: 13px; font-weight: 600; color: var(--color-on-surface-variant);">
                Tìm thấy ${filteredListings.length} tin tuyển quân
              </span>
              ${isCaptain ? `
                <button class="btn btn-sm btn-outline" id="btn-quick-post-rec" style="font-size: 12px;">
                  + Đăng tin tuyển
                </button>
              ` : ''}
            </div>

            <!-- Listings Stream -->
            ${filteredListings.length === 0 ? `
              <div class="empty-state" style="padding: 32px 16px;">
                <div class="empty-state-icon">⚽</div>
                <h3 class="empty-state-title">Chưa có tin tuyển phù hợp</h3>
                <p class="empty-state-desc">Hãy thử mở rộng bán kính tìm kiếm hoặc chọn vị trí thi đấu khác.</p>
                <button class="btn btn-secondary btn-sm" id="btn-reset-filters" style="margin-top: 12px;">
                  Đặt lại bộ lọc
                </button>
              </div>
            ` : `
              <div class="list-group">
                ${filteredListings.map(rec => renderRecruitmentCard(rec, currentState)).join('')}
              </div>
            `}
          ` : ''}

        </div>
      `;

      // Event handlers
      _attachTabEvents();
    }

    function renderRecruitmentCard(rec, currentState) {
      const team = currentState.teams.find(t => t.id === rec.teamId) || { name: 'Đội bóng', reputationScore: 80 };
      const venue = currentState.venues.find(v => v.id === rec.venueId);
      const isMyTeam = (store.getMyCaptainedTeams() || []).some(t => t.id === rec.teamId);
      
      // Check viewer's application
      const myApp = (currentState.applications || []).find(
        a => a.recruitmentId === rec.id && a.applicantId === currentState.currentUserId
      );

      // CRITICAL AC-P05: STRICT PRIVACY CHECK
      // Only APPROVED applicant or Captain can see Venue!
      const isApprovedMercenary = myApp && myApp.status === 'APPROVED';
      const canSeeVenue = isMyTeam || isApprovedMercenary;

      const positionName = _formatPosition(rec.position);
      const statusBadge = _formatRecStatusBadge(rec.status);

      return `
        <article class="card card-interactive rec-listing-card" data-id="${rec.id}">
          <div class="card-header">
            <div>
              <span class="badge ${rec.status === 'OPEN' ? 'badge-green' : 'badge-amber'}">
                ${positionName}
              </span>
              <span class="badge badge-gray" style="margin-left: 4px;">
                Cần ${rec.quantity} người (Đã duyệt ${rec.approvedCount || 0}/${rec.quantity})
              </span>
            </div>
            ${statusBadge}
          </div>

          <div style="display: flex; align-items: center; justify-content: space-between; margin: 10px 0;">
            <div>
              <h4 style="margin: 0; font-size: 16px; font-weight: 700;">${team.name}</h4>
              <div style="font-size: 12px; color: var(--color-on-surface-variant); display: flex; align-items: center; gap: 6px; margin-top: 2px;">
                <span>Uy tín: <strong>${team.reputationScore}/100</strong></span>
                <span>•</span>
                <span>⭐ ${team.ratingAvg || 5.0}</span>
              </div>
            </div>
            <div class="avatar avatar-sm">
              ${team.logoUrl ? `<img src="${team.logoUrl}" alt="${team.name}">` : team.name.slice(0, 2).toUpperCase()}
            </div>
          </div>

          <div style="background-color: var(--color-surface-container-low); padding: 10px 12px; border-radius: 8px; margin: 8px 0; font-size: 13px;">
            <div style="display: flex; align-items: center; gap: 6px; margin-bottom: 4px;">
              <span>📅</span>
              <strong>${rec.matchDate} · ${rec.timeSlot}</strong>
            </div>
            
            <!-- AC-P05: Privacy compliance -->
            <div style="display: flex; align-items: flex-start; gap: 6px; color: var(--color-on-surface-variant);">
              <span>📍</span>
              <div>
                ${canSeeVenue && venue ? `
                  <strong style="color: var(--color-primary-dark);">${venue.name}</strong>
                  <div>${venue.address}</div>
                ` : `
                  <div><strong>${rec.area || 'Khu vực TP. Thủ Đức'}</strong></div>
                  <div style="font-size: 11px; color: var(--color-warning); font-style: italic;">
                    🔒 Tên sân & địa chỉ hiển thị sau khi đơn được duyệt (AC-P05)
                  </div>
                `}
                ${rec._distKm ? `<div style="font-size: 11px; margin-top: 2px;">Cách bạn: ~${rec._distKm} km</div>` : ''}
              </div>
            </div>
          </div>

          ${rec.note ? `
            <p style="font-size: 12px; color: var(--color-on-surface-variant); margin: 6px 0 10px 0; line-height: 1.4;">
              "${rec.note}"
            </p>
          ` : ''}

          <!-- Application status / Quick CTA -->
          <div style="display: flex; align-items: center; justify-content: space-between; border-top: 1px solid var(--color-outline-variant); padding-top: 10px; margin-top: 6px;">
            ${myApp ? `
              <div>
                <span style="font-size: 12px; color: var(--color-on-surface-variant);">Đơn của bạn:</span>
                ${_formatAppStatusBadge(myApp.status)}
              </div>
              <button class="btn btn-sm btn-secondary btn-view-app" data-app-id="${myApp.id}">
                Xem đơn →
              </button>
            ` : `
              ${isMyTeam ? `
                <span class="badge badge-blue">Đội của bạn</span>
                <button class="btn btn-sm btn-secondary btn-manage-rec" data-rec-id="${rec.id}">
                  Quản lý tin →
                </button>
              ` : `
                ${rec.status === 'FILLED' ? `
                  <span style="font-size: 12px; color: var(--color-on-surface-variant);">Đã đủ số lượng</span>
                  <button class="btn btn-sm btn-secondary" disabled>Đã đủ người</button>
                ` : `
                  <span style="font-size: 12px; color: var(--color-primary);">Đang tuyển gấp</span>
                  <button class="btn btn-sm btn-primary btn-apply-now" data-rec-id="${rec.id}">
                    Ứng tuyển ngay
                  </button>
                `}
              `}
            `}
          </div>
        </article>
      `;
    }

    function renderMyApplicationsTab(myApps, currentState) {
      if (myApps.length === 0) {
        return `
          <div class="empty-state" style="padding: 40px 16px;">
            <div class="empty-state-icon">📋</div>
            <h3 class="empty-state-title">Bạn chưa có đơn ứng tuyển nào</h3>
            <p class="empty-state-desc">Hãy tìm kiếm tin tuyển người phù hợp và gửi đơn ứng tuyển.</p>
            <button class="btn btn-primary btn-sm" id="btn-go-search" style="margin-top: 12px;">
              Tìm tin tuyển quân
            </button>
          </div>
        `;
      }

      return `
        <div class="list-group">
          ${myApps.map(app => {
            const rec = currentState.recruitments.find(r => r.id === app.recruitmentId) || {};
            const team = currentState.teams.find(t => t.id === rec.teamId) || { name: 'Đội tuyển' };
            const venue = currentState.venues.find(v => v.id === rec.venueId);
            const isApproved = app.status === 'APPROVED';

            return `
              <div class="card card-interactive app-card" data-app-id="${app.id}">
                <div class="card-header">
                  <div>
                    <span class="badge badge-green">${_formatPosition(rec.position)}</span>
                    <span style="font-size: 12px; color: var(--color-on-surface-variant); margin-left: 6px;">
                      Mã: <code>${app.id}</code>
                    </span>
                  </div>
                  ${_formatAppStatusBadge(app.status)}
                </div>

                <div style="margin: 8px 0;">
                  <h4 style="margin: 0; font-size: 16px;">${team.name}</h4>
                  <div style="font-size: 13px; color: var(--color-on-surface-variant); margin-top: 4px;">
                    📅 ${rec.matchDate || 'N/A'} · ${rec.timeSlot || 'N/A'}
                  </div>
                  
                  <!-- AC-P05: Privacy in application card -->
                  <div style="font-size: 12px; margin-top: 4px;">
                    📍 ${isApproved && venue ? `
                      <strong style="color: var(--color-primary-dark);">${venue.name}</strong> (${venue.address})
                    ` : `
                      <span>${rec.area || 'Khu vực'} (Tên sân hiển thị khi được duyệt)</span>
                    `}
                  </div>
                </div>

                <div style="display: flex; gap: 8px; justify-content: flex-end; border-top: 1px solid var(--color-outline-variant); padding-top: 8px; margin-top: 8px;">
                  <button class="btn btn-sm btn-outline btn-open-chat-for-app" data-app-id="${app.id}">
                    💬 Nhắn tin
                  </button>
                  <button class="btn btn-sm btn-primary btn-view-app-detail" data-app-id="${app.id}">
                    Chi tiết đơn →
                  </button>
                </div>
              </div>
            `;
          }).join('')}
        </div>
      `;
    }

    function renderTeamRecruitmentsTab(teamRecs, currentState) {
      return `
        <div>
          <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px;">
            <h4 style="margin: 0; font-size: 15px;">Tin tuyển của đội bạn</h4>
            <button class="btn btn-sm btn-primary" id="btn-create-new-rec">
              + Tạo tin tuyển mới
            </button>
          </div>

          ${teamRecs.length === 0 ? `
            <div class="empty-state" style="padding: 30px 16px;">
              <p>Đội của bạn hiện chưa có tin tuyển quân nào đang mở.</p>
              <button class="btn btn-sm btn-outline" id="btn-create-new-rec-2">
                Tuyển cho kèo đã ghép / trận ngoài
              </button>
            </div>
          ` : `
            <div class="list-group">
              ${teamRecs.map(r => {
                const venue = currentState.venues.find(v => v.id === r.venueId);
                const apps = (currentState.applications || []).filter(a => a.recruitmentId === r.id);
                const pendingApps = apps.filter(a => a.status === 'PENDING').length;

                return `
                  <div class="card card-interactive team-rec-card" data-rec-id="${r.id}">
                    <div class="card-header">
                      <div>
                        <span class="badge badge-green">${_formatPosition(r.position)}</span>
                        <span style="font-size: 12px; margin-left: 6px;">
                          Đã duyệt: <strong>${r.approvedCount}/${r.quantity}</strong>
                        </span>
                      </div>
                      ${_formatRecStatusBadge(r.status)}
                    </div>

                    <div style="margin: 8px 0; font-size: 13px;">
                      <div>📅 ${r.matchDate} · ${r.timeSlot}</div>
                      <div>📍 ${venue ? venue.name : r.area}</div>
                      ${pendingApps > 0 ? `
                        <div style="margin-top: 6px;">
                          <span class="badge badge-amber">⚡ Có ${pendingApps} ứng viên đang chờ duyệt!</span>
                        </div>
                      ` : ''}
                    </div>

                    <div style="display: flex; gap: 8px; justify-content: flex-end; border-top: 1px solid var(--color-outline-variant); padding-top: 8px;">
                      <button class="btn btn-sm btn-outline btn-edit-rec-qty" data-rec-id="${r.id}">
                        Sửa số lượng / Đóng
                      </button>
                      <button class="btn btn-sm btn-primary btn-view-candidates" data-rec-id="${r.id}">
                        Ứng viên (${apps.length}) →
                      </button>
                    </div>
                  </div>
                `;
              }).join('')}
            </div>
          `}
        </div>
      `;
    }

    function _attachTabEvents() {
      // Tab switching
      container.querySelector('#tab-btn-listings')?.addEventListener('click', () => {
        activeSubTab = 'listings';
        renderView();
      });
      container.querySelector('#tab-btn-my-apps')?.addEventListener('click', () => {
        activeSubTab = 'my-apps';
        renderView();
      });
      container.querySelector('#tab-btn-team-rec')?.addEventListener('click', () => {
        activeSubTab = 'team-rec';
        renderView();
      });
      container.querySelector('#btn-go-search')?.addEventListener('click', () => {
        activeSubTab = 'listings';
        renderView();
      });

      // Filter chips
      container.querySelectorAll('.chip[data-pos]').forEach(btn => {
        btn.addEventListener('click', () => {
          selectedPosition = btn.getAttribute('data-pos');
          renderView();
        });
      });

      container.querySelectorAll('.chip[data-radius]').forEach(btn => {
        btn.addEventListener('click', () => {
          selectedRadius = parseInt(btn.getAttribute('data-radius'), 10);
          renderView();
        });
      });

      // Toggle GPS
      container.querySelector('#btn-toggle-gps')?.addEventListener('click', () => {
        const currentGps = store.getState().meta.gpsStatus;
        const newGps = currentGps === 'granted' ? 'denied' : 'granted';
        store.setGpsStatus(newGps);
        router.showToast(newGps === 'granted' ? 'Đã bật GPS định vị' : 'Đã tắt GPS (dùng khu vực hồ sơ)');
        renderView();
      });

      // Reset filters
      container.querySelector('#btn-reset-filters')?.addEventListener('click', () => {
        selectedPosition = 'ALL';
        selectedRadius = 3;
        renderView();
      });

      // Actions
      container.querySelectorAll('.rec-listing-card').forEach(card => {
        card.addEventListener('click', (e) => {
          if (e.target.closest('button')) return;
          const recId = card.getAttribute('data-id');
          router.navigate('recruitment-detail', { recruitmentId: recId });
        });
      });

      container.querySelectorAll('.btn-apply-now').forEach(btn => {
        btn.addEventListener('click', (e) => {
          e.stopPropagation();
          const recId = btn.getAttribute('data-rec-id');
          router.openSheet('apply-sheet', { recruitmentId: recId });
        });
      });

      container.querySelectorAll('.btn-view-app').forEach(btn => {
        btn.addEventListener('click', (e) => {
          e.stopPropagation();
          const appId = btn.getAttribute('data-app-id');
          router.navigate('application-detail', { applicationId: appId });
        });
      });

      container.querySelectorAll('.btn-view-app-detail').forEach(btn => {
        btn.addEventListener('click', (e) => {
          e.stopPropagation();
          const appId = btn.getAttribute('data-app-id');
          router.navigate('application-detail', { applicationId: appId });
        });
      });

      container.querySelectorAll('.btn-open-chat-for-app').forEach(btn => {
        btn.addEventListener('click', (e) => {
          e.stopPropagation();
          const appId = btn.getAttribute('data-app-id');
          const conv = store.getState().conversations.find(c => c.contextId === appId);
          if (conv) {
            router.navigate('chat-room', { conversationId: conv.id });
          } else {
            router.showToast('Chưa có cuộc trò chuyện cho đơn này');
          }
        });
      });

      container.querySelectorAll('.btn-manage-rec').forEach(btn => {
        btn.addEventListener('click', (e) => {
          e.stopPropagation();
          const recId = btn.getAttribute('data-rec-id');
          router.navigate('recruitment-detail', { recruitmentId: recId });
        });
      });

      container.querySelectorAll('.btn-view-candidates').forEach(btn => {
        btn.addEventListener('click', (e) => {
          e.stopPropagation();
          const recId = btn.getAttribute('data-rec-id');
          router.navigate('recruitment-candidates', { recruitmentId: recId });
        });
      });

      container.querySelectorAll('.btn-edit-rec-qty').forEach(btn => {
        btn.addEventListener('click', (e) => {
          e.stopPropagation();
          const recId = btn.getAttribute('data-rec-id');
          router.openSheet('edit-recruitment-quantity-sheet', { recruitmentId: recId });
        });
      });

      const openRecCreate = () => router.navigate('recruitment-select-match');
      container.querySelector('#btn-quick-post-rec')?.addEventListener('click', openRecCreate);
      container.querySelector('#btn-create-new-rec')?.addEventListener('click', openRecCreate);
      container.querySelector('#btn-create-new-rec-2')?.addEventListener('click', openRecCreate);
    }

    renderView();
  });

  /* =========================================================================
     MERC-02: Recruitment Listing Detail (route: 'recruitment-detail')
     ========================================================================= */
  router.registerView('recruitment-detail', (container, params) => {
    router.setHeaderTitle('Chi tiết tin tuyển quân');

    const state = store.getState();
    const rec = state.recruitments.find(r => r.id === params.recruitmentId);

    if (!rec) {
      container.innerHTML = `
        <div class="empty-state" style="padding: 40px 16px;">
          <h3 class="empty-state-title">Không tìm thấy tin tuyển quân</h3>
          <p class="empty-state-desc">Tin này có thể đã bị xóa hoặc không tồn tại.</p>
          <button class="btn btn-primary" onclick="window.AppRouter.navigate('players')">Quay lại tìm đá lẻ</button>
        </div>
      `;
      return;
    }

    const team = state.teams.find(t => t.id === rec.teamId) || { name: 'Đội bóng', reputationScore: 80 };
    const venue = state.venues.find(v => v.id === rec.venueId);
    const isCaptainOfTeam = (store.getMyCaptainedTeams() || []).some(t => t.id === rec.teamId);

    // Check my application status
    const myApp = state.applications.find(
      a => a.recruitmentId === rec.id && a.applicantId === state.currentUserId
    );
    const isApprovedMercenary = myApp && myApp.status === 'APPROVED';

    // CRITICAL AC-P05 PRIVACY CHECK
    const canSeeVenue = isCaptainOfTeam || isApprovedMercenary;

    // Check match start time constraint per §1.3
    const now = store.getClock();
    const matchStart = new Date(`${rec.matchDate}T${rec.timeSlot?.split(' - ')[0] || '19:00'}:00Z`);
    const matchStarted = now >= matchStart;

    container.innerHTML = `
      <div style="padding: 16px 0;">
        
        <!-- Header Info Card -->
        <div class="card">
          <div class="card-header">
            <div>
              <span class="badge badge-green" style="font-size: 14px; padding: 4px 10px;">
                ${_formatPosition(rec.position)}
              </span>
              <span class="badge badge-gray" style="margin-left: 6px;">
                Cần ${rec.quantity} người
              </span>
            </div>
            ${_formatRecStatusBadge(rec.status)}
          </div>

          <div style="display: flex; align-items: center; justify-content: space-between; margin: 16px 0 12px 0;">
            <div>
              <h3 style="margin: 0; font-size: 18px; font-weight: 800;">${team.name}</h3>
              <div style="font-size: 13px; color: var(--color-on-surface-variant); margin-top: 4px;">
                Độ uy tín: <strong>${team.reputationScore}/100</strong> · ⭐ ${team.ratingAvg || 5.0}
              </div>
            </div>
            <div class="avatar avatar-lg">
              ${team.logoUrl ? `<img src="${team.logoUrl}" alt="${team.name}">` : team.name.slice(0, 2).toUpperCase()}
            </div>
          </div>

          <div class="divider"></div>

          <!-- Schedule & Venue Information -->
          <div style="display: grid; gap: 12px; font-size: 14px;">
            <div style="display: flex; align-items: center; gap: 8px;">
              <span>📅</span>
              <div>
                <strong>Ngày thi đấu:</strong> ${rec.matchDate}
              </div>
            </div>

            <div style="display: flex; align-items: center; gap: 8px;">
              <span>⏰</span>
              <div>
                <strong>Khung giờ:</strong> ${rec.timeSlot}
              </div>
            </div>

            <div style="display: flex; align-items: flex-start; gap: 8px;">
              <span>📍</span>
              <div style="flex: 1;">
                <strong>Địa điểm thi đấu:</strong>
                
                ${canSeeVenue && venue ? `
                  <div style="margin-top: 4px; padding: 10px; background-color: var(--color-primary-light); border-radius: 8px;">
                    <div style="font-weight: 700; color: var(--color-primary-dark); font-size: 15px;">
                      🏟️ ${venue.name}
                    </div>
                    <div style="color: var(--color-on-surface); font-size: 13px; margin-top: 2px;">
                      ${venue.address}
                    </div>
                    ${venue.contactPhone ? `<div style="font-size: 12px; margin-top: 4px;">Hotline sân: <strong>${venue.contactPhone}</strong></div>` : ''}
                    <div style="margin-top: 6px;">
                      <a href="https://maps.google.com/?q=${venue.coordinates?.lat},${venue.coordinates?.lng}" target="_blank" class="btn btn-sm btn-outline" style="min-height: 32px; padding: 4px 10px;">
                        Mở bản đồ chỉ đường ↗
                      </a>
                    </div>
                  </div>
                ` : `
                  <div style="margin-top: 4px; padding: 10px; background-color: var(--color-surface-container-low); border-radius: 8px;">
                    <div style="font-weight: 600;">${rec.area || 'Khu vực TP. Thủ Đức'}</div>
                    <div class="notice notice-warning" style="margin-top: 6px; padding: 8px 10px; font-size: 12px;">
                      🔒 <strong>Bảo vệ riêng tư (AC-P05):</strong> Tên sân, địa chỉ chính xác và số điện thoại liên hệ sẽ tự động mở khóa sau khi đơn ứng tuyển của bạn được Đội trưởng phê duyệt.
                    </div>
                  </div>
                `}
              </div>
            </div>

            <div style="display: flex; align-items: center; gap: 8px;">
              <span>⚡</span>
              <div>
                <strong>Trình độ yêu cầu:</strong> ${_formatSkillLevel(rec.skillLevel)}
              </div>
            </div>

            <div style="display: flex; align-items: center; gap: 8px;">
              <span>👥</span>
              <div>
                <strong>Slot đã duyệt:</strong> 
                <span class="badge ${rec.approvedCount >= rec.quantity ? 'badge-amber' : 'badge-green'}">
                  ${rec.approvedCount || 0} / ${rec.quantity}
                </span>
              </div>
            </div>
          </div>

          ${rec.note ? `
            <div style="margin-top: 14px; padding-top: 12px; border-top: 1px solid var(--color-outline-variant);">
              <strong style="font-size: 13px;">Ghi chú từ đội trưởng:</strong>
              <p style="margin: 4px 0 0 0; font-size: 13px; color: var(--color-on-surface-variant); line-height: 1.5;">
                ${rec.note}
              </p>
            </div>
          ` : ''}
        </div>

        <!-- Application / Management Actions -->
        <div style="margin-top: 16px;">
          ${isCaptainOfTeam ? `
            <div class="card" style="background-color: var(--color-surface-container-low);">
              <h4 style="margin: 0 0 8px 0; font-size: 15px;">Quyền quản lý đội trưởng</h4>
              <p style="font-size: 13px; color: var(--color-on-surface-variant); margin-bottom: 12px;">
                Bạn là đội trưởng đăng tin này. Bạn có thể xem các ứng viên hoặc điều chỉnh số lượng.
              </p>
              <div style="display: grid; gap: 8px;">
                <button class="btn btn-primary btn-block" id="btn-captain-view-candidates">
                  Xem danh sách ứng viên (${state.applications.filter(a => a.recruitmentId === rec.id).length})
                </button>
                <button class="btn btn-secondary btn-block" id="btn-captain-edit-rec">
                  Chỉnh sửa số lượng / Đóng tin
                </button>
              </div>
            </div>
          ` : `
            ${myApp ? `
              <div class="card" style="border-left: 4px solid var(--color-primary);">
                <div style="display: flex; align-items: center; justify-content: space-between; margin-bottom: 8px;">
                  <span style="font-size: 14px; font-weight: 700;">Đơn ứng tuyển của bạn:</span>
                  ${_formatAppStatusBadge(myApp.status)}
                </div>
                
                <p style="font-size: 13px; color: var(--color-on-surface-variant); margin-bottom: 12px;">
                  ${myApp.status === 'PENDING' ? 'Đơn đang chờ đội trưởng xét duyệt. Bạn có thể nhắn tin trao đổi thêm.' : ''}
                  ${myApp.status === 'APPROVED' ? 'Chúc mừng! Đơn đã được duyệt. Hãy chuẩn bị trang phục và đến sân đúng giờ.' : ''}
                  ${myApp.status === 'REJECTED' ? 'Đơn ứng tuyển đã bị từ chối hoặc tin đã đóng.' : ''}
                  ${myApp.status === 'WITHDRAWN' ? 'Bạn đã rút đơn ứng tuyển này.' : ''}
                </p>

                <div style="display: grid; gap: 8px;">
                  <button class="btn btn-primary btn-block" id="btn-open-chat-from-detail">
                    💬 Nhắn tin với đội trưởng
                  </button>
                  <button class="btn btn-secondary btn-block" id="btn-view-my-app-detail">
                    Xem chi tiết đơn & Hủy/Rút
                  </button>
                </div>
              </div>
            ` : `
              ${rec.status === 'FILLED' ? `
                <button class="btn btn-secondary btn-block" disabled>
                  Đã đủ số lượng người tuyển (${rec.quantity}/${rec.quantity})
                </button>
              ` : rec.status === 'CLOSED' || rec.status === 'EXPIRED' ? `
                <button class="btn btn-secondary btn-block" disabled>
                  Tin tuyển đã đóng hoặc hết hạn
                </button>
              ` : matchStarted ? `
                <button class="btn btn-secondary btn-block" disabled>
                  Trận đấu đã bắt đầu — Không nhận đơn mới
                </button>
              ` : `
                <button class="btn btn-primary btn-block" id="btn-open-apply-sheet" style="font-size: 16px; font-weight: 700;">
                  Ứng tuyển vị trí này ngay
                </button>
              `}
            `}
          `}
        </div>

      </div>
    `;

    // Event attachments
    container.querySelector('#btn-captain-view-candidates')?.addEventListener('click', () => {
      router.navigate('recruitment-candidates', { recruitmentId: rec.id });
    });

    container.querySelector('#btn-captain-edit-rec')?.addEventListener('click', () => {
      router.openSheet('edit-recruitment-quantity-sheet', { recruitmentId: rec.id });
    });

    container.querySelector('#btn-open-apply-sheet')?.addEventListener('click', () => {
      router.openSheet('apply-sheet', { recruitmentId: rec.id });
    });

    container.querySelector('#btn-open-chat-from-detail')?.addEventListener('click', () => {
      if (!myApp) return;
      const conv = state.conversations.find(c => c.contextId === myApp.id);
      if (conv) {
        router.navigate('chat-room', { conversationId: conv.id });
      } else {
        router.showToast('Đang mở cuộc trò chuyện...');
      }
    });

    container.querySelector('#btn-view-my-app-detail')?.addEventListener('click', () => {
      if (myApp) {
        router.navigate('application-detail', { applicationId: myApp.id });
      }
    });
  });

  /* =========================================================================
     MERC-03: Apply Confirmation Sheet (sheet: 'apply-sheet')
     ========================================================================= */
  router.registerSheet('apply-sheet', (body, footer, params, titleElem) => {
    titleElem.textContent = 'Xác nhận ứng tuyển đá lẻ';

    const state = store.getState();
    const rec = state.recruitments.find(r => r.id === params.recruitmentId);
    const currentUser = store.getCurrentUser();

    if (!rec) {
      body.innerHTML = '<p class="notice notice-danger">Không tìm thấy thông tin tin tuyển quân.</p>';
      return;
    }

    const team = state.teams.find(t => t.id === rec.teamId) || { name: 'Đội bóng' };

    // Validation checks per MERC-03
    let validationError = null;

    // Check duplicate pending application
    const existing = state.applications.find(
      a => a.recruitmentId === rec.id && a.applicantId === state.currentUserId && (a.status === 'PENDING' || a.status === 'APPROVED')
    );
    if (existing) {
      validationError = 'Bạn đã có đơn đang được xử lý cho tin tuyển này (409 MERCENARY_008).';
    }

    // Check member of team
    const isMember = (state.teamMembers || []).some(
      m => m.teamId === rec.teamId && m.userId === state.currentUserId && m.status === 'ACTIVE'
    );
    if (isMember) {
      validationError = 'Bạn là thành viên của đội tuyển quân này, không thể ứng tuyển đá lẻ (422 MERCENARY_010).';
    }

    body.innerHTML = `
      <div>
        <div class="card" style="background-color: var(--color-surface-container-low); margin-bottom: 16px;">
          <div style="font-size: 13px; color: var(--color-on-surface-variant);">Đội tuyển:</div>
          <div style="font-size: 16px; font-weight: 700;">${team.name}</div>
          <div style="font-size: 13px; margin-top: 4px;">
            Vị trí: <strong>${_formatPosition(rec.position)}</strong> · 📅 ${rec.matchDate} (${rec.timeSlot})
          </div>
          <div style="font-size: 12px; color: var(--color-on-surface-variant); margin-top: 2px;">
            Khu vực: ${rec.area || 'TP. Thủ Đức'}
          </div>
        </div>

        <!-- AC-P05 Venue Notice -->
        <div class="notice notice-warning" style="font-size: 12px; margin-bottom: 16px;">
          🔒 <strong>Chính sách riêng tư:</strong> Tên sân và vị trí bản đồ sẽ được giữ kín và chỉ mở sau khi bạn được Đội trưởng phê duyệt.
        </div>

        ${validationError ? `
          <div class="notice notice-danger" style="margin-bottom: 16px;">
            ${validationError}
          </div>
        ` : ''}

        <div class="form-group">
          <label class="form-label" for="apply-message">
            Lời nhắn / Giới thiệu bản thân (Tùy chọn)
          </label>
          <textarea 
            class="form-textarea" 
            id="apply-message" 
            placeholder="Ví dụ: Tôi có kinh nghiệm đá gôn 3 năm sân 7, có giày và trang phục đầy đủ..."
          >${currentUser?.bio || ''}</textarea>
          <span class="field-helper-msg">Đội trưởng sẽ đọc lời nhắn này và xem hồ sơ uy tín của bạn.</span>
        </div>
      </div>
    `;

    footer.innerHTML = `
      <button class="btn btn-secondary" onclick="window.AppRouter.closeSheet()" style="flex: 1;">
        Hủy bỏ
      </button>
      <button class="btn btn-primary" id="btn-submit-apply" style="flex: 1;" ${validationError ? 'disabled' : ''}>
        Gửi đơn ứng tuyển
      </button>
    `;

    footer.querySelector('#btn-submit-apply')?.addEventListener('click', () => {
      const msg = body.querySelector('#apply-message')?.value || '';
      try {
        const newApp = store.applyRecruitment(rec.id, state.currentUserId, msg);

        // Dispatch in-app notification to captain per spec
        store.setState(s => {
          s.notifications.push({
            id: 'notif-' + Date.now(),
            userId: team.captainId,
            type: 'MERCENARY_APPLICATION',
            title: 'Có ứng viên ứng tuyển mới',
            body: `${currentUser?.displayName || 'Người chơi'} đã nộp đơn ứng tuyển vị trí ${_formatPosition(rec.position)}.`,
            referenceType: 'MERCENARY_APPLICATION',
            referenceId: newApp.id,
            read: false,
            createdAt: s.meta.mockClock
          });
          return s;
        });

        router.closeSheet();
        router.showToast('Gửi đơn ứng tuyển thành công! Đang chờ đội trưởng duyệt.', 'success');
        router.navigate('application-detail', { applicationId: newApp.id });
      } catch (err) {
        router.showToast(err.message, 'error');
      }
    });
  });

  /* =========================================================================
     MERC-04: My Applications List (route: 'my-applications')
     ========================================================================= */
  router.registerView('my-applications', (container, params) => {
    router.setHeaderTitle('Đơn ứng tuyển của tôi');
    // Forward to players view with tab=my-apps for unified navigation
    router.navigate('players', { tab: 'my-apps' }, true);
  });

  /* =========================================================================
     MERC-05: Application Detail (route: 'application-detail')
     ========================================================================= */
  router.registerView('application-detail', (container, params) => {
    router.setHeaderTitle('Chi tiết đơn ứng tuyển');

    const state = store.getState();
    const app = state.applications.find(a => a.id === params.applicationId);

    if (!app) {
      container.innerHTML = `
        <div class="empty-state" style="padding: 40px 16px;">
          <h3 class="empty-state-title">Đơn ứng tuyển không tồn tại</h3>
          <p class="empty-state-desc">Đơn có thể đã bị xóa hoặc liên kết không hợp lệ.</p>
          <button class="btn btn-primary" onclick="window.AppRouter.navigate('players')">Quay lại</button>
        </div>
      `;
      return;
    }

    const rec = state.recruitments.find(r => r.id === app.recruitmentId) || {};
    const team = state.teams.find(t => t.id === rec.teamId) || { name: 'Đội tuyển', captainId: 'user-cap-alpha' };
    const captain = state.characters[team.captainId] || { displayName: 'Đội trưởng' };
    const venue = state.venues.find(v => v.id === rec.venueId);

    const isApplicant = app.applicantId === state.currentUserId;
    const isCaptain = team.captainId === state.currentUserId;
    const isApproved = app.status === 'APPROVED';

    // CRITICAL AC-P05: STRICT PRIVACY ENFORCEMENT
    const canSeeVenue = isCaptain || (isApplicant && isApproved);

    // Timing check
    const now = store.getClock();
    const matchStart = new Date(`${rec.matchDate}T${rec.timeSlot?.split(' - ')[0] || '19:00'}:00Z`);
    const isPastMatch = now >= matchStart;

    container.innerHTML = `
      <div style="padding: 16px 0;">

        <!-- Status Hero Card -->
        <div class="card" style="border-top: 4px solid ${isApproved ? 'var(--color-primary)' : app.status === 'PENDING' ? 'var(--color-warning)' : 'var(--color-outline)'};">
          <div class="card-header">
            <div>
              <span class="card-subtitle">Mã đơn: <code>${app.id}</code></span>
              <h3 style="margin: 4px 0 0 0; font-size: 18px;">${_formatPosition(rec.position)}</h3>
            </div>
            ${_formatAppStatusBadge(app.status)}
          </div>

          <div style="margin: 12px 0;">
            <div style="font-size: 13px; color: var(--color-on-surface-variant);">Đội bóng:</div>
            <div style="font-size: 16px; font-weight: 700;">${team.name}</div>
            ${canSeeVenue ? `
              <div style="font-size: 13px; color: var(--color-on-surface-variant); margin-top: 2px;">
                Đội trưởng: <strong>${captain.displayName}</strong> (${captain.phone || 'Chưa cập nhật SĐT'})
              </div>
            ` : ''}
          </div>

          <div class="divider"></div>

          <!-- Schedule & Venue Details -->
          <div style="display: grid; gap: 10px; font-size: 14px;">
            <div>📅 <strong>Thời gian:</strong> ${rec.matchDate} (${rec.timeSlot})</div>
            
            <!-- AC-P05: Privacy compliance -->
            <div>
              📍 <strong>Địa điểm:</strong>
              ${canSeeVenue && venue ? `
                <div style="margin-top: 6px; padding: 12px; background-color: var(--color-primary-light); border-radius: 8px;">
                  <div style="font-weight: 800; color: var(--color-primary-dark); font-size: 16px;">
                    🏟️ ${venue.name}
                  </div>
                  <div style="font-size: 13px; margin-top: 2px;">${venue.address}</div>
                  <div style="margin-top: 6px;">
                    <a href="https://maps.google.com/?q=${venue.coordinates?.lat},${venue.coordinates?.lng}" target="_blank" class="btn btn-sm btn-outline">
                      Chỉ đường tới sân ↗
                    </a>
                  </div>
                </div>
              ` : `
                <div style="margin-top: 6px; padding: 10px; background-color: var(--color-surface-container-low); border-radius: 8px;">
                  <div>${rec.area || 'Khu vực TP. Thủ Đức'}</div>
                  <div class="notice notice-warning" style="margin-top: 6px; font-size: 12px;">
                    🔒 <strong>AC-P05 Bảo vệ riêng tư:</strong> Tên sân và vị trí chính xác chỉ hiển thị sau khi đơn được phê duyệt.
                  </div>
                </div>
              `}
            </div>

            <div>⚡ <strong>Trình độ yêu cầu:</strong> ${_formatSkillLevel(rec.skillLevel)}</div>
          </div>

          ${app.message ? `
            <div style="margin-top: 14px; padding-top: 10px; border-top: 1px solid var(--color-outline-variant);">
              <span style="font-size: 12px; color: var(--color-on-surface-variant);">Lời nhắn của ứng viên:</span>
              <p style="margin: 4px 0 0 0; font-size: 13px; font-style: italic;">"${app.message}"</p>
            </div>
          ` : ''}
        </div>

        <!-- Action Buttons -->
        <div style="margin-top: 16px; display: grid; gap: 10px;">
          
          <!-- 1-1 Chat with Captain (CHAT-03) -->
          <button class="btn btn-primary btn-block" id="btn-chat-captain">
            💬 Nhắn tin trực tiếp với đội trưởng
          </button>

          <!-- Withdrawal action per AC-P06 -->
          ${isApplicant && (app.status === 'PENDING' || app.status === 'APPROVED') && !isPastMatch ? `
            <button class="btn btn-danger-outline btn-block" id="btn-withdraw-application">
              ${app.status === 'PENDING' ? 'Rút đơn ứng tuyển (Không trừ điểm)' : 'Rút lui khỏi trận (Có trừ điểm uy tín)'}
            </button>
          ` : ''}

          <!-- Post-Match Rating (REP-03) -->
          ${isApplicant && isApproved && isPastMatch ? `
            <button class="btn btn-secondary btn-block" id="btn-rate-team">
              ⭐ Đánh giá đội trưởng & đội bóng
            </button>
          ` : ''}

          <!-- Return button -->
          <button class="btn btn-ghost btn-block" onclick="window.AppRouter.navigate('players', { tab: 'my-apps' })">
            ← Quay lại danh sách đơn
          </button>
        </div>

      </div>
    `;

    // Event attachments
    container.querySelector('#btn-chat-captain')?.addEventListener('click', () => {
      const conv = state.conversations.find(c => c.contextId === app.id);
      if (conv) {
        router.navigate('chat-room', { conversationId: conv.id });
      } else {
        router.showToast('Mở cuộc trò chuyện...');
      }
    });

    container.querySelector('#btn-withdraw-application')?.addEventListener('click', () => {
      router.openSheet('withdraw-application-sheet', { applicationId: app.id });
    });

    container.querySelector('#btn-rate-team')?.addEventListener('click', () => {
      router.navigate('rate-match', { matchId: rec.matchId || rec.externalMatchId, targetType: 'TEAM' });
    });
  });

  /* =========================================================================
     MERC-06: Withdraw Application Sheet (sheet: 'withdraw-application-sheet')
     ========================================================================= */
  router.registerSheet('withdraw-application-sheet', (body, footer, params, titleElem) => {
    titleElem.textContent = 'Xác nhận rút đơn ứng tuyển';

    const state = store.getState();
    const app = state.applications.find(a => a.id === params.applicationId);

    if (!app) {
      body.innerHTML = '<p class="notice notice-danger">Không tìm thấy đơn ứng tuyển.</p>';
      return;
    }

    const rec = state.recruitments.find(r => r.id === app.recruitmentId) || {};
    const isApproved = app.status === 'APPROVED';

    // Calculate penalty per AC-P06 & Business Rules §1.5
    let penaltyPoints = 0;
    let penaltyDescription = '';

    if (isApproved) {
      const now = store.getClock();
      const matchStart = new Date(`${rec.matchDate}T${rec.timeSlot?.split(' - ')[0] || '19:00'}:00Z`);
      const hoursUntilMatch = (matchStart - now) / (1000 * 60 * 60);

      if (hoursUntilMatch >= 24) {
        penaltyPoints = 1;
        penaltyDescription = 'Rút trước trận >= 24 giờ: Trừ 1 điểm uy tín cá nhân.';
      } else if (hoursUntilMatch >= 4) {
        penaltyPoints = 3;
        penaltyDescription = 'Rút trước trận từ 4 đến 24 giờ: Trừ 3 điểm uy tín cá nhân.';
      } else {
        penaltyPoints = 5;
        penaltyDescription = 'Rút sát giờ bắt đầu (< 4 giờ): Trừ 5 điểm uy tín cá nhân.';
      }
    }

    body.innerHTML = `
      <div>
        <div class="card" style="background-color: var(--color-surface-container-low); margin-bottom: 16px;">
          <div style="font-size: 13px; color: var(--color-on-surface-variant);">Trạng thái hiện tại:</div>
          <div style="font-size: 16px; font-weight: 700; margin-top: 2px;">
            ${_formatAppStatusBadge(app.status)}
          </div>
          <div style="font-size: 13px; margin-top: 6px;">
            Vị trí: <strong>${_formatPosition(rec.position)}</strong> · Trận ngày ${rec.matchDate}
          </div>
        </div>

        ${!isApproved ? `
          <!-- Pending withdrawal is free per AC-P06 -->
          <div class="notice notice-info" style="font-size: 13px;">
            ℹ️ <strong>Rút đơn đang chờ duyệt:</strong> Thao tác này hoàn toàn <strong>KHÔNG bị trừ điểm uy tín cá nhân</strong>. Bạn có thể tự do tìm và ứng tuyển vào các kèo khác.
          </div>
        ` : `
          <!-- Approved withdrawal penalty warning per AC-P06 -->
          <div class="notice notice-danger" style="font-size: 13px;">
            ⚠️ <strong>Cảnh báo trừ điểm uy tín (AC-P06):</strong><br>
            Bạn đã được đội trưởng phê duyệt vào đội hình chính thức. Rút lui lúc này sẽ làm đội thiếu người:
            <ul style="margin: 6px 0 0 16px; padding: 0;">
              <li><strong>${penaltyDescription}</strong></li>
              <li>Chuỗi trận sạch cá nhân sẽ bị cài lại về <strong>0</strong>.</li>
              <li>Tin tuyển quân của đội sẽ được tự động mở lại cho ứng viên khác.</li>
            </ul>
          </div>
        `}

        <p style="font-size: 13px; color: var(--color-on-surface-variant); margin-top: 12px;">
          Bạn có chắc chắn muốn xác nhận rút lui khỏi đơn này?
        </p>
      </div>
    `;

    footer.innerHTML = `
      <button class="btn btn-secondary" onclick="window.AppRouter.closeSheet()" style="flex: 1;">
        Giữ lại đơn
      </button>
      <button class="btn btn-danger" id="btn-confirm-withdraw" style="flex: 1;">
        Xác nhận rút
      </button>
    `;

    footer.querySelector('#btn-confirm-withdraw')?.addEventListener('click', () => {
      try {
        store.withdrawApplication(app.id);

        router.closeSheet();
        if (isApproved) {
          router.showToast(`Đã rút lui. Bị khấu trừ ${penaltyPoints} điểm uy tín theo quy định AC-P06.`, 'warning');
        } else {
          router.showToast('Đã rút đơn ứng tuyển thành công (không bị trừ điểm).', 'success');
        }
        router.navigate('players', { tab: 'my-apps' });
      } catch (err) {
        router.showToast(err.message, 'error');
      }
    });
  });

  /* =========================================================================
     MERC-07: Select match for recruiting (route: 'recruitment-select-match')
     ========================================================================= */
  router.registerView('recruitment-select-match', (container, params) => {
    router.setHeaderTitle('Chọn trận để tuyển quân');

    const state = store.getState();
    const myCaptainedTeams = store.getMyCaptainedTeams();
    const myTeamIds = myCaptainedTeams.map(t => t.id);

    // Confirmed matches involving captain's teams
    const confirmedMatches = state.matches.filter(
      m => m.status === 'CONFIRMED' && (myTeamIds.includes(m.hostTeamId) || myTeamIds.includes(m.guestTeamId))
    );

    // External matches
    const extMatches = state.externalMatches.filter(
      em => myTeamIds.includes(em.teamId) && em.status === 'SCHEDULED'
    );

    container.innerHTML = `
      <div style="padding: 16px 0;">
        <p style="font-size: 14px; color: var(--color-on-surface-variant); margin-bottom: 16px;">
          Chọn một trận đấu đã chốt trên hệ thống hoặc khai báo trận đấu tự do ngoài hệ thống:
        </p>

        <!-- Option 1: Declaring External Match (EXT-01) -->
        <div class="card card-interactive" id="card-declare-ext" style="border: 2px dashed var(--color-primary); background-color: var(--color-surface-container-low);">
          <div style="display: flex; align-items: center; gap: 12px;">
            <div style="font-size: 32px;">🏟️</div>
            <div>
              <h4 style="margin: 0; font-size: 16px; color: var(--color-primary-dark);">+ Khai báo trận ngoài hệ thống (EXT-01)</h4>
              <p style="margin: 4px 0 0 0; font-size: 12px; color: var(--color-on-surface-variant);">
                Trận tự liên hệ sân và đối thủ ngoài app. Tuyển đá lẻ nhanh chóng.
              </p>
            </div>
          </div>
        </div>

        <h4 style="font-size: 15px; margin: 24px 0 12px 0;">Kèo đã ghép trên ArenaFlow (${confirmedMatches.length})</h4>

        ${confirmedMatches.length === 0 ? `
          <div class="notice notice-info" style="font-size: 13px;">
            Đội bạn hiện chưa có kèo đấu nào ở trạng thái ĐÃ GHÉP (CONFIRMED). Bạn có thể khai báo trận ngoài ở trên để tuyển quân ngay.
          </div>
        ` : `
          <div class="list-group">
            ${confirmedMatches.map(m => {
              const hostTeam = state.teams.find(t => t.id === m.hostTeamId) || { name: 'Đội nhà' };
              const guestTeam = state.teams.find(t => t.id === m.guestTeamId) || { name: 'Đội khách' };
              const venue = state.venues.find(v => v.id === m.venueId) || { name: 'Sân bóng' };

              return `
                <div class="card card-interactive match-select-card" data-match-id="${m.id}">
                  <div class="card-header">
                    <span class="badge badge-green">KÈO ĐÃ GHÉP</span>
                    <span style="font-size: 12px; color: var(--color-on-surface-variant);">${m.matchDate}</span>
                  </div>
                  <h4 style="margin: 8px 0 4px 0; font-size: 16px;">${hostTeam.name} ⚔️ ${guestTeam.name}</h4>
                  <div style="font-size: 13px; color: var(--color-on-surface-variant);">
                    ⏰ ${m.startTime} - ${m.endTime} · 🏟️ ${venue.name}
                  </div>
                  <div style="margin-top: 10px; display: flex; justify-content: flex-end;">
                    <button class="btn btn-sm btn-primary">Tuyển quân cho kèo này →</button>
                  </div>
                </div>
              `;
            }).join('')}
          </div>
        `}

        ${extMatches.length > 0 ? `
          <h4 style="font-size: 15px; margin: 24px 0 12px 0;">Trận ngoài đã khai báo (${extMatches.length})</h4>
          <div class="list-group">
            ${extMatches.map(em => {
              const venue = state.venues.find(v => v.id === em.venueId) || { name: 'Sân bóng' };
              return `
                <div class="card card-interactive ext-select-card" data-ext-id="${em.id}">
                  <div class="card-header">
                    <span class="badge badge-amber">TRẬN NGOÀI HỆ THỐNG</span>
                    <span style="font-size: 12px;">${em.matchDate}</span>
                  </div>
                  <h4 style="margin: 6px 0; font-size: 15px;">${em.title || 'Trận giao lưu'} vs ${em.opponentName}</h4>
                  <div style="font-size: 13px; color: var(--color-on-surface-variant);">
                    ⏰ ${em.startTime} - ${em.endTime} · 🏟️ ${venue.name}
                  </div>
                  <div style="margin-top: 10px; display: flex; justify-content: flex-end;">
                    <button class="btn btn-sm btn-outline">Thêm vị trí tuyển →</button>
                  </div>
                </div>
              `;
            }).join('')}
          </div>
        ` : ''}

      </div>
    `;

    container.querySelector('#card-declare-ext')?.addEventListener('click', () => {
      router.navigate('external-create');
    });

    container.querySelectorAll('.match-select-card').forEach(card => {
      card.addEventListener('click', () => {
        const matchId = card.getAttribute('data-match-id');
        router.navigate('recruitment-create', { matchId });
      });
    });

    container.querySelectorAll('.ext-select-card').forEach(card => {
      card.addEventListener('click', () => {
        const extId = card.getAttribute('data-ext-id');
        router.openSheet('add-external-recruitment-position-sheet', { externalMatchId: extId });
      });
    });
  });

  /* =========================================================================
     MERC-08: Post Recruitment (route: 'recruitment-create')
     ========================================================================= */
  router.registerView('recruitment-create', (container, params) => {
    router.setHeaderTitle('Đăng tin tuyển quân');

    const state = store.getState();
    const match = state.matches.find(m => m.id === params.matchId);
    const myCaptainedTeams = store.getMyCaptainedTeams();
    const actingTeam = myCaptainedTeams.find(t => t.id === match?.hostTeamId || t.id === match?.guestTeamId) || myCaptainedTeams[0];

    if (!match && !params.externalMatchId) {
      container.innerHTML = `
        <div class="notice notice-danger" style="margin: 20px;">
          Cần chọn trận đấu trước khi đăng tin tuyển quân.
          <button class="btn btn-primary btn-block" style="margin-top: 10px;" onclick="window.AppRouter.navigate('recruitment-select-match')">
            Chọn trận
          </button>
        </div>
      `;
      return;
    }

    const venue = state.venues.find(v => v.id === match?.venueId) || { name: 'Sân bóng' };

    container.innerHTML = `
      <div style="padding: 16px 0;">
        <div class="card" style="background-color: var(--color-surface-container-low); margin-bottom: 16px;">
          <div style="font-size: 13px; color: var(--color-on-surface-variant);">Kèo thi đấu:</div>
          <div style="font-size: 16px; font-weight: 700; margin-top: 2px;">
            ${match?.matchDate} (${match?.startTime} - ${match?.endTime})
          </div>
          <div style="font-size: 13px; color: var(--color-on-surface-variant); margin-top: 2px;">
            🏟️ ${venue.name} · Đội đăng tin: <strong>${actingTeam?.name}</strong>
          </div>
        </div>

        <form id="form-rec-create">
          <p style="font-size: 14px; font-weight: 700; margin-bottom: 8px;">
            Chọn vị trí và số lượng cần tuyển (1–4 vị trí, mỗi vị trí thành 1 tin riêng per BR-ME01):
          </p>

          <div style="display: grid; gap: 12px; margin-bottom: 16px;">
            ${[
              { key: 'GOALKEEPER', label: '🧤 Thủ môn' },
              { key: 'DEFENDER', label: '🛡️ Hậu vệ' },
              { key: 'MIDFIELDER', label: '⚡ Tiền vệ' },
              { key: 'FORWARD', label: '⚽ Tiền đạo' }
            ].map(pos => `
              <div class="card" style="padding: 12px; margin: 0;">
                <div style="display: flex; align-items: center; justify-content: space-between;">
                  <label style="display: flex; align-items: center; gap: 8px; font-weight: 600; cursor: pointer;">
                    <input type="checkbox" name="pos_check" value="${pos.key}" style="width: 20px; height: 20px;">
                    ${pos.label}
                  </label>
                  <div style="display: flex; align-items: center; gap: 6px;">
                    <span style="font-size: 12px; color: var(--color-on-surface-variant);">Số lượng:</span>
                    <input 
                      type="number" 
                      name="qty_${pos.key}" 
                      value="1" 
                      min="1" 
                      max="10" 
                      class="form-input" 
                      style="width: 70px; min-height: 36px; padding: 6px;"
                      disabled
                    >
                  </div>
                </div>
              </div>
            `).join('')}
          </div>

          <div class="form-group">
            <label class="form-label" for="rec-note">Ghi chú / Quyền lợi cho mercenary</label>
            <textarea 
              class="form-textarea" 
              id="rec-note" 
              placeholder="Ví dụ: Đội đá giao lưu vui vẻ, hỗ trợ nước uống và áo đấu..."
            ></textarea>
          </div>

          <button type="submit" class="btn btn-primary btn-block" style="font-size: 16px; font-weight: 700; margin-top: 16px;">
            Đăng các tin tuyển quân
          </button>
        </form>
      </div>
    `;

    // Enable/disable quantity input on checkbox change
    const form = container.querySelector('#form-rec-create');
    form.querySelectorAll('input[name="pos_check"]').forEach(cb => {
      cb.addEventListener('change', () => {
        const qtyInput = form.querySelector(`input[name="qty_${cb.value}"]`);
        if (qtyInput) {
          qtyInput.disabled = !cb.checked;
        }
      });
    });

    form.addEventListener('submit', (e) => {
      e.preventDefault();
      const checkedBoxes = Array.from(form.querySelectorAll('input[name="pos_check"]:checked'));

      if (checkedBoxes.length === 0) {
        router.showToast('Vui lòng chọn ít nhất một vị trí cần tuyển', 'error');
        return;
      }

      const positions = checkedBoxes.map(cb => ({
        position: cb.value,
        quantity: parseInt(form.querySelector(`input[name="qty_${cb.value}"]`).value, 10) || 1
      }));

      const note = form.querySelector('#rec-note').value;

      try {
        store.createRecruitmentsForMatch({
          teamId: actingTeam.id,
          matchId: match.id,
          venueId: match.venueId,
          area: venue.wardId === 'ward-ak' ? 'Phường An Khánh, TP. Thủ Đức' : 'TP. Thủ Đức',
          matchDate: match.matchDate,
          timeSlot: `${match.startTime} - ${match.endTime}`,
          skillLevel: match.skillLevel,
          positions,
          note
        });

        router.showToast(`Đã tạo thành công ${positions.length} tin tuyển quân!`, 'success');
        router.navigate('players', { tab: 'team-rec' });
      } catch (err) {
        router.showToast(err.message, 'error');
      }
    });
  });

  /* =========================================================================
     MERC-09: Team Recruitment Listings (route: 'team-recruitments')
     ========================================================================= */
  router.registerView('team-recruitments', (container, params) => {
    router.setHeaderTitle('Tin tuyển quân của đội');
    router.navigate('players', { tab: 'team-rec' }, true);
  });

  /* =========================================================================
     MERC-10: Recruitment Candidates List (route: 'recruitment-candidates')
     ========================================================================= */
  router.registerView('recruitment-candidates', (container, params) => {
    router.setHeaderTitle('Danh sách ứng viên');

    const state = store.getState();
    const rec = state.recruitments.find(r => r.id === params.recruitmentId);

    if (!rec) {
      container.innerHTML = '<div class="notice notice-danger">Không tìm thấy tin tuyển quân.</div>';
      return;
    }

    const apps = state.applications.filter(a => a.recruitmentId === rec.id);

    container.innerHTML = `
      <div style="padding: 16px 0;">
        <div class="card" style="background-color: var(--color-surface-container-low); margin-bottom: 16px;">
          <div style="display: flex; justify-content: space-between; align-items: center;">
            <h4 style="margin: 0; font-size: 16px;">Vị trí: ${_formatPosition(rec.position)}</h4>
            ${_formatRecStatusBadge(rec.status)}
          </div>
          <div style="font-size: 13px; color: var(--color-on-surface-variant); margin-top: 4px;">
            Đã duyệt: <strong>${rec.approvedCount}/${rec.quantity}</strong> slot
          </div>
        </div>

        <h4 style="font-size: 15px; margin-bottom: 12px;">Các ứng viên đã nộp đơn (${apps.length})</h4>

        ${apps.length === 0 ? `
          <div class="empty-state" style="padding: 30px 16px;">
            <div class="empty-state-icon">👥</div>
            <p>Chưa có ứng viên nào nộp đơn vào tin tuyển này.</p>
          </div>
        ` : `
          <div class="list-group">
            ${apps.map(app => {
              const applicant = state.characters[app.applicantId] || { displayName: 'Người chơi', reputationScore: 80 };
              const isPending = app.status === 'PENDING';

              return `
                <div class="card" style="margin-bottom: 12px;">
                  <div style="display: flex; align-items: center; justify-content: space-between;">
                    <div style="display: flex; align-items: center; gap: 10px;">
                      <div class="avatar">
                        ${applicant.avatarUrl ? `<img src="${applicant.avatarUrl}" alt="${applicant.displayName}">` : applicant.displayName.slice(0, 2).toUpperCase()}
                      </div>
                      <div>
                        <h4 style="margin: 0; font-size: 15px;">${applicant.displayName}</h4>
                        <div style="font-size: 12px; color: var(--color-on-surface-variant); margin-top: 2px;">
                          Uy tín cá nhân: <strong>${applicant.reputationScore || 90}/100</strong>
                          ${applicant.completionRate ? ` · Hoàn thành: ${applicant.completionRate}%` : ' · (Mới tham gia)'}
                        </div>
                      </div>
                    </div>
                    ${_formatAppStatusBadge(app.status)}
                  </div>

                  ${app.message ? `
                    <div style="margin: 8px 0; font-size: 13px; background-color: var(--color-surface-container-low); padding: 8px 10px; border-radius: 6px;">
                      "${app.message}"
                    </div>
                  ` : ''}

                  <div style="display: flex; gap: 8px; justify-content: flex-end; margin-top: 10px; border-top: 1px solid var(--color-outline-variant); padding-top: 8px;">
                    <button class="btn btn-sm btn-outline btn-chat-candidate" data-app-id="${app.id}">
                      💬 Nhắn tin
                    </button>
                    <button class="btn btn-sm btn-secondary btn-view-candidate-detail" data-app-id="${app.id}">
                      Xem chi tiết đơn →
                    </button>
                    ${isPending ? `
                      <button class="btn btn-sm btn-primary btn-quick-approve" data-app-id="${app.id}" ${rec.approvedCount >= rec.quantity ? 'disabled' : ''}>
                        Duyệt
                      </button>
                    ` : ''}
                  </div>
                </div>
              `;
            }).join('')}
          </div>
        `}
      </div>
    `;

    container.querySelectorAll('.btn-chat-candidate').forEach(btn => {
      btn.addEventListener('click', () => {
        const appId = btn.getAttribute('data-app-id');
        const conv = state.conversations.find(c => c.contextId === appId);
        if (conv) router.navigate('chat-room', { conversationId: conv.id });
      });
    });

    container.querySelectorAll('.btn-view-candidate-detail').forEach(btn => {
      btn.addEventListener('click', () => {
        const appId = btn.getAttribute('data-app-id');
        router.navigate('candidate-detail', { applicationId: appId });
      });
    });

    container.querySelectorAll('.btn-quick-approve').forEach(btn => {
      btn.addEventListener('click', () => {
        const appId = btn.getAttribute('data-app-id');
        try {
          store.approveApplication(appId);
          router.showToast('Đã phê duyệt ứng viên thành công!', 'success');
          router.navigate('recruitment-candidates', { recruitmentId: rec.id });
        } catch (err) {
          router.showToast(err.message, 'error');
        }
      });
    });
  });

  /* =========================================================================
     MERC-11: Applicant Detail + Approve/Reject (route: 'candidate-detail')
     ========================================================================= */
  router.registerView('candidate-detail', (container, params) => {
    router.setHeaderTitle('Đánh giá ứng viên');

    const state = store.getState();
    const app = state.applications.find(a => a.id === params.applicationId);

    if (!app) {
      container.innerHTML = '<div class="notice notice-danger">Không tìm thấy đơn ứng tuyển.</div>';
      return;
    }

    const rec = state.recruitments.find(r => r.id === app.recruitmentId) || {};
    const applicant = state.characters[app.applicantId] || { displayName: 'Ứng viên' };
    const isPending = app.status === 'PENDING';

    container.innerHTML = `
      <div style="padding: 16px 0;">
        
        <!-- Applicant Profile Card -->
        <div class="card">
          <div style="display: flex; align-items: center; gap: 14px; margin-bottom: 16px;">
            <div class="avatar avatar-lg">
              ${applicant.avatarUrl ? `<img src="${applicant.avatarUrl}" alt="${applicant.displayName}">` : applicant.displayName.slice(0, 2).toUpperCase()}
            </div>
            <div>
              <h3 style="margin: 0; font-size: 18px;">${applicant.displayName}</h3>
              <div style="font-size: 13px; color: var(--color-on-surface-variant); margin-top: 2px;">
                Vị trí sở trường: <strong>${_formatPosition(applicant.preferredPosition || rec.position)}</strong>
              </div>
              <div style="margin-top: 4px;">
                ${_formatAppStatusBadge(app.status)}
              </div>
            </div>
          </div>

          <div class="divider"></div>

          <!-- Personal Reputation Stats per §1.5 & §1.6 -->
          <h4 style="font-size: 14px; margin-bottom: 8px;">Chỉ số uy tín cá nhân</h4>
          <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 10px; margin-bottom: 12px;">
            <div style="background-color: var(--color-surface-container-low); padding: 10px; border-radius: 8px;">
              <div style="font-size: 11px; color: var(--color-on-surface-variant);">Điểm uy tín</div>
              <div style="font-size: 20px; font-weight: 800; color: var(--color-primary);">${applicant.reputationScore || 90}/100</div>
            </div>
            <div style="background-color: var(--color-surface-container-low); padding: 10px; border-radius: 8px;">
              <div style="font-size: 11px; color: var(--color-on-surface-variant);">Tỉ lệ hoàn thành</div>
              <div style="font-size: 16px; font-weight: 700; margin-top: 4px;">
                ${applicant.completionRate ? `${applicant.completionRate}%` : 'Mới tham gia (< 5 trận)'}
              </div>
            </div>
          </div>

          ${applicant.bio ? `
            <div style="font-size: 13px; color: var(--color-on-surface-variant); margin-bottom: 10px;">
              <strong>Giới thiệu:</strong> "${applicant.bio}"
            </div>
          ` : ''}

          ${app.message ? `
            <div style="background-color: var(--color-surface-container-low); padding: 10px; border-radius: 8px; font-size: 13px;">
              <strong>Lời nhắn ứng tuyển:</strong><br>
              "${app.message}"
            </div>
          ` : ''}
        </div>

        <!-- Approval / Rejection Controls -->
        <div style="margin-top: 16px; display: grid; gap: 10px;">
          <button class="btn btn-outline btn-block" id="btn-chat-cand-room">
            💬 Nhắn tin trao đổi thêm
          </button>

          ${isPending ? `
            <div style="display: flex; gap: 10px;">
              <button class="btn btn-danger-outline" id="btn-reject-candidate" style="flex: 1;">
                Từ chối
              </button>
              <button class="btn btn-primary" id="btn-approve-candidate" style="flex: 2;" ${rec.approvedCount >= rec.quantity ? 'disabled' : ''}>
                ${rec.approvedCount >= rec.quantity ? 'Đã đủ slot' : 'Chấp thuận duyệt'}
              </button>
            </div>
          ` : ''}
        </div>

      </div>
    `;

    container.querySelector('#btn-chat-cand-room')?.addEventListener('click', () => {
      const conv = state.conversations.find(c => c.contextId === app.id);
      if (conv) router.navigate('chat-room', { conversationId: conv.id });
    });

    container.querySelector('#btn-approve-candidate')?.addEventListener('click', () => {
      try {
        store.approveApplication(app.id);

        // Send notification to applicant per spec
        store.setState(s => {
          s.notifications.push({
            id: 'notif-' + Date.now(),
            userId: app.applicantId,
            type: 'MERCENARY_APPLICATION_APPROVED',
            title: 'Đơn ứng tuyển đã được duyệt!',
            body: `Đội trưởng đã phê duyệt đơn đá lẻ của bạn cho trận ngày ${rec.matchDate}. Bạn có thể xem địa chỉ sân ngay bây giờ.`,
            referenceType: 'MERCENARY_APPLICATION',
            referenceId: app.id,
            read: false,
            createdAt: s.meta.mockClock
          });
          return s;
        });

        router.showToast('Đã phê duyệt ứng viên thành công!', 'success');
        router.navigate('recruitment-candidates', { recruitmentId: rec.id });
      } catch (err) {
        router.showToast(err.message, 'error');
      }
    });

    container.querySelector('#btn-reject-candidate')?.addEventListener('click', () => {
      try {
        store.rejectApplication(app.id);

        store.setState(s => {
          s.notifications.push({
            id: 'notif-' + Date.now(),
            userId: app.applicantId,
            type: 'MERCENARY_APPLICATION_REJECTED',
            title: 'Đơn ứng tuyển chưa được chấp thuận',
            body: `Đội trưởng đã từ chối đơn ứng tuyển vị trí ${_formatPosition(rec.position)}.`,
            referenceType: 'MERCENARY_APPLICATION',
            referenceId: app.id,
            read: false,
            createdAt: s.meta.mockClock
          });
          return s;
        });

        router.showToast('Đã từ chối đơn ứng tuyển.', 'info');
        router.navigate('recruitment-candidates', { recruitmentId: rec.id });
      } catch (err) {
        router.showToast(err.message, 'error');
      }
    });
  });

  /* =========================================================================
     MERC-12: Edit Quantity / Close Listing Sheet (sheet: 'edit-recruitment-quantity-sheet')
     ========================================================================= */
  router.registerSheet('edit-recruitment-quantity-sheet', (body, footer, params, titleElem) => {
    titleElem.textContent = 'Quản lý số lượng & Đóng tin';

    const state = store.getState();
    const rec = state.recruitments.find(r => r.id === params.recruitmentId);

    if (!rec) {
      body.innerHTML = '<div class="notice notice-danger">Không tìm thấy tin tuyển quân.</div>';
      return;
    }

    body.innerHTML = `
      <div>
        <div class="card" style="background-color: var(--color-surface-container-low); margin-bottom: 16px;">
          <div>Vị trí: <strong>${_formatPosition(rec.position)}</strong></div>
          <div style="font-size: 13px; color: var(--color-on-surface-variant); margin-top: 4px;">
            Số slot đã duyệt hiện tại: <strong>${rec.approvedCount || 0}</strong>
          </div>
        </div>

        <div class="form-group">
          <label class="form-label" for="edit-rec-qty">
            Số lượng cần tuyển mới:
          </label>
          <input 
            type="number" 
            id="edit-rec-qty" 
            value="${rec.quantity}" 
            min="${rec.approvedCount || 1}" 
            max="10" 
            class="form-input"
          >
          <span class="field-helper-msg">
            Không được nhỏ hơn số ứng viên đã duyệt (${rec.approvedCount || 0}).
          </span>
        </div>

        <div class="divider"></div>

        <div style="margin-top: 12px;">
          <h4 style="color: var(--color-error); font-size: 14px; margin-bottom: 4px;">Đóng tin tuyển quân</h4>
          <p style="font-size: 12px; color: var(--color-on-surface-variant); line-height: 1.4;">
            Đóng tin sẽ chuyển trạng thái sang <strong>CLOSED</strong>. Mọi đơn đang chờ duyệt (PENDING) sẽ tự động chuyển thành BỊ TỪ CHỐI (REJECTED).
          </p>
          <button class="btn btn-danger-outline btn-block btn-sm" id="btn-close-rec-now" style="margin-top: 8px;">
            Đóng tin tuyển này vĩnh viễn
          </button>
        </div>
      </div>
    `;

    footer.innerHTML = `
      <button class="btn btn-secondary" onclick="window.AppRouter.closeSheet()" style="flex: 1;">
        Đóng
      </button>
      <button class="btn btn-primary" id="btn-save-rec-qty" style="flex: 1;">
        Cập nhật số lượng
      </button>
    `;

    footer.querySelector('#btn-save-rec-qty')?.addEventListener('click', () => {
      const newQty = parseInt(body.querySelector('#edit-rec-qty').value, 10);
      if (newQty < (rec.approvedCount || 0)) {
        router.showToast(`Số lượng không được nhỏ hơn số slot đã duyệt (${rec.approvedCount})`, 'error');
        return;
      }

      store.setState(s => {
        const targetRec = s.recruitments.find(r => r.id === rec.id);
        if (targetRec) {
          targetRec.quantity = newQty;
          if (targetRec.approvedCount >= newQty) {
            targetRec.status = 'FILLED';
          } else if (targetRec.status === 'FILLED' && newQty > targetRec.approvedCount) {
            targetRec.status = 'OPEN';
          }
        }
        return s;
      });

      router.closeSheet();
      router.showToast('Đã cập nhật số lượng tuyển!', 'success');
      router.navigate('players', { tab: 'team-rec' });
    });

    body.querySelector('#btn-close-rec-now')?.addEventListener('click', () => {
      store.setState(s => {
        const targetRec = s.recruitments.find(r => r.id === rec.id);
        if (targetRec) {
          targetRec.status = 'CLOSED';
        }
        // Auto-reject pending applications per §1.3
        s.applications.forEach(a => {
          if (a.recruitmentId === rec.id && a.status === 'PENDING') {
            a.status = 'REJECTED';
          }
        });
        return s;
      });

      router.closeSheet();
      router.showToast('Đã đóng tin tuyển quân.', 'info');
      router.navigate('players', { tab: 'team-rec' });
    });
  });

  /* =========================================================================
     EXT-01: Declare External Match (route: 'external-create')
     ========================================================================= */
  router.registerView('external-create', (container, params) => {
    router.setHeaderTitle('Khai báo trận ngoài');

    const state = store.getState();
    const myCaptainedTeams = store.getMyCaptainedTeams();
    const actingTeam = myCaptainedTeams[0];

    container.innerHTML = `
      <div style="padding: 16px 0;">
        <p style="font-size: 13px; color: var(--color-on-surface-variant); margin-bottom: 16px;">
          Khai báo thông tin trận đấu tự liên hệ sân & đối thủ ngoài hệ thống để tuyển quân (EXT-01).
        </p>

        <form id="form-ext-create">
          <div class="form-group">
            <label class="form-label" for="ext-team">Đội đại diện</label>
            <select class="form-select" id="ext-team">
              ${myCaptainedTeams.map(t => `<option value="${t.id}">${t.name}</option>`).join('')}
            </select>
          </div>

          <div class="form-group">
            <label class="form-label" for="ext-title">Tiêu đề trận đấu</label>
            <input type="text" class="form-input" id="ext-title" placeholder="VD: Giao lưu nội bộ cúp Tứ Hùng" required>
          </div>

          <div class="form-group">
            <label class="form-label" for="ext-opponent">Tên đội đối thủ (Tùy chọn)</label>
            <input type="text" class="form-input" id="ext-opponent" placeholder="VD: FC Bạn Bè, Sài Gòn Friends...">
          </div>

          <div class="form-group">
            <label class="form-label" for="ext-venue">Địa điểm / Sân thi đấu</label>
            <select class="form-select" id="ext-venue">
              ${state.venues.filter(v => v.status === 'ACTIVE').map(v => `
                <option value="${v.id}">${v.name} (${v.address})</option>
              `).join('')}
            </select>
          </div>

          <div class="form-group">
            <label class="form-label" for="ext-date">Ngày thi đấu</label>
            <input type="date" class="form-input" id="ext-date" value="2026-11-22" required>
          </div>

          <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 10px;">
            <div class="form-group">
              <label class="form-label" for="ext-start">Giờ bắt đầu</label>
              <input type="time" class="form-input" id="ext-start" value="18:00" required>
            </div>
            <div class="form-group">
              <label class="form-label" for="ext-end">Giờ kết thúc</label>
              <input type="time" class="form-input" id="ext-end" value="19:30" required>
            </div>
          </div>

          <div class="form-group">
            <label class="form-label" for="ext-skill">Trình độ thi đấu</label>
            <select class="form-select" id="ext-skill">
              <option value="BEGINNER">Mới chơi</option>
              <option value="INTERMEDIATE" selected>Trung bình</option>
              <option value="ADVANCED">Khá</option>
              <option value="SEMI_PRO">Bán chuyên</option>
            </select>
          </div>

          <div class="divider"></div>

          <h4 style="font-size: 15px; margin-bottom: 8px;">Vị trí cần tuyển ban đầu</h4>
          <div class="form-group">
            <label class="form-label" for="ext-init-pos">Vị trí</label>
            <select class="form-select" id="ext-init-pos">
              <option value="MIDFIELDER">Tiền vệ</option>
              <option value="GOALKEEPER">Thủ môn</option>
              <option value="DEFENDER">Hậu vệ</option>
              <option value="FORWARD">Tiền đạo</option>
            </select>
          </div>

          <div class="form-group">
            <label class="form-label" for="ext-init-qty">Số lượng cần</label>
            <input type="number" class="form-input" id="ext-init-qty" value="1" min="1" max="10">
          </div>

          <button type="submit" class="btn btn-primary btn-block" style="margin-top: 16px; font-weight: 700;">
            Khai báo và Đăng tin tuyển
          </button>
        </form>
      </div>
    `;

    container.querySelector('#form-ext-create')?.addEventListener('submit', (e) => {
      e.preventDefault();
      const form = e.target;

      const teamId = form.querySelector('#ext-team').value;
      const title = form.querySelector('#ext-title').value;
      const opponentName = form.querySelector('#ext-opponent').value || 'Đối thủ tự do';
      const venueId = form.querySelector('#ext-venue').value;
      const matchDate = form.querySelector('#ext-date').value;
      const startTime = form.querySelector('#ext-start').value;
      const endTime = form.querySelector('#ext-end').value;
      const skillLevel = form.querySelector('#ext-skill').value;
      const initPos = form.querySelector('#ext-init-pos').value;
      const initQty = parseInt(form.querySelector('#ext-init-qty').value, 10);

      const extId = 'ext-match-' + Date.now();
      const newExt = {
        id: extId,
        teamId,
        title,
        opponentName,
        venueId,
        matchDate,
        startTime,
        endTime,
        status: 'SCHEDULED',
        skillLevel,
        createdAt: store.getState().meta.mockClock
      };

      const venue = store.getState().venues.find(v => v.id === venueId);

      const newRec = {
        id: 'rec-' + Date.now(),
        teamId,
        externalMatchId: extId,
        position: initPos,
        quantity: initQty,
        approvedCount: 0,
        status: 'OPEN',
        area: venue?.wardId === 'ward-ak' ? 'Phường An Khánh, TP. Thủ Đức' : 'TP. Thủ Đức',
        matchDate,
        timeSlot: `${startTime} - ${endTime}`,
        venueId,
        skillLevel,
        note: `Trận ngoài hệ thống: ${title}`,
        createdAt: store.getState().meta.mockClock
      };

      store.setState(s => {
        s.externalMatches.push(newExt);
        s.recruitments.push(newRec);
        return s;
      });

      router.showToast('Khai báo trận ngoài thành công!', 'success');
      router.navigate('external-detail', { externalMatchId: extId });
    });
  });

  /* =========================================================================
     EXT-02: External Matches List (route: 'external-list')
     ========================================================================= */
  router.registerView('external-list', (container, params) => {
    router.setHeaderTitle('Trận ngoài hệ thống');

    const state = store.getState();
    const myCaptainedTeams = store.getMyCaptainedTeams();
    const myTeamIds = myCaptainedTeams.map(t => t.id);

    const extMatches = state.externalMatches.filter(em => myTeamIds.includes(em.teamId));

    container.innerHTML = `
      <div style="padding: 16px 0;">
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px;">
          <h4 style="margin: 0; font-size: 16px;">Trận ngoài của đội (${extMatches.length})</h4>
          <button class="btn btn-sm btn-primary" onclick="window.AppRouter.navigate('external-create')">
            + Khai báo trận mới
          </button>
        </div>

        ${extMatches.length === 0 ? `
          <div class="empty-state" style="padding: 40px 16px;">
            <div class="empty-state-icon">🏟️</div>
            <h3 class="empty-state-title">Chưa có trận ngoài nào</h3>
            <p class="empty-state-desc">Khai báo trận tự do ngoài app để tuyển mercenary thuận tiện.</p>
            <button class="btn btn-primary btn-sm" onclick="window.AppRouter.navigate('external-create')" style="margin-top: 12px;">
              Khai báo trận ngoài ngay
            </button>
          </div>
        ` : `
          <div class="list-group">
            ${extMatches.map(em => {
              const venue = state.venues.find(v => v.id === em.venueId) || { name: 'Sân bóng' };
              const recs = state.recruitments.filter(r => r.externalMatchId === em.id);
              const approvedCount = recs.reduce((sum, r) => sum + (r.approvedCount || 0), 0);
              const neededCount = recs.reduce((sum, r) => sum + (r.quantity || 0), 0);

              return `
                <div class="card card-interactive ext-list-card" data-ext-id="${em.id}" style="margin-bottom: 12px;">
                  <div class="card-header">
                    <span class="badge ${em.status === 'SCHEDULED' ? 'badge-green' : 'badge-red'}">
                      ${em.status === 'SCHEDULED' ? 'ĐÃ KHAI BÁO' : 'ĐÃ HỦY'}
                    </span>
                    <span style="font-size: 12px; color: var(--color-on-surface-variant);">${em.matchDate}</span>
                  </div>

                  <h4 style="margin: 6px 0 2px 0; font-size: 16px;">${em.title || 'Trận giao lưu'}</h4>
                  <div style="font-size: 13px; color: var(--color-on-surface-variant);">
                    Đối thủ: <strong>${em.opponentName}</strong>
                  </div>

                  <div style="font-size: 13px; margin: 6px 0;">
                    ⏰ ${em.startTime} - ${em.endTime} · 🏟️ ${venue.name}
                  </div>

                  <div style="display: flex; justify-content: space-between; align-items: center; border-top: 1px solid var(--color-outline-variant); padding-top: 8px; margin-top: 8px;">
                    <span style="font-size: 12px; color: var(--color-on-surface-variant);">
                      Đá lẻ: <strong>${approvedCount}/${neededCount}</strong> người
                    </span>
                    <button class="btn btn-sm btn-secondary" onclick="window.AppRouter.navigate('external-detail', { externalMatchId: '${em.id}' })">
                      Xem chi tiết →
                    </button>
                  </div>
                </div>
              `;
            }).join('')}
          </div>
        `}
      </div>
    `;
  });

  /* =========================================================================
     EXT-02: External Match Detail (route: 'external-detail')
     ========================================================================= */
  router.registerView('external-detail', (container, params) => {
    router.setHeaderTitle('Chi tiết trận ngoài');

    const state = store.getState();
    const ext = state.externalMatches.find(e => e.id === params.externalMatchId);

    if (!ext) {
      container.innerHTML = '<div class="notice notice-danger">Không tìm thấy trận đấu ngoài hệ thống.</div>';
      return;
    }

    const team = state.teams.find(t => t.id === ext.teamId) || { name: 'Đội bóng' };
    const venue = state.venues.find(v => v.id === ext.venueId) || { name: 'Sân bóng' };
    const recs = state.recruitments.filter(r => r.externalMatchId === ext.id);

    // Check if any applicant has been approved (used to lock fields per EXT-03)
    const hasApprovedApplicants = recs.some(r => r.approvedCount > 0);

    container.innerHTML = `
      <div style="padding: 16px 0;">
        <div class="card">
          <div class="card-header">
            <span class="badge ${ext.status === 'SCHEDULED' ? 'badge-green' : 'badge-red'}">
              ${ext.status === 'SCHEDULED' ? 'ĐÃ KHAI BÁO (SCHEDULED)' : 'ĐÃ HỦY (CANCELLED)'}
            </span>
            <span style="font-size: 12px; color: var(--color-on-surface-variant);">ID: <code>${ext.id}</code></span>
          </div>

          <h3 style="margin: 8px 0 4px 0; font-size: 18px;">${ext.title || 'Trận giao lưu'}</h3>
          <div style="font-size: 14px; color: var(--color-on-surface-variant);">
            Đối thủ: <strong>${ext.opponentName}</strong>
          </div>

          <div class="divider"></div>

          <div style="display: grid; gap: 8px; font-size: 13px;">
            <div>📅 <strong>Ngày thi đấu:</strong> ${ext.matchDate}</div>
            <div>⏰ <strong>Khung giờ:</strong> ${ext.startTime} - ${ext.endTime}</div>
            <div>🏟️ <strong>Sân bóng:</strong> ${venue.name} (${venue.address})</div>
            <div>⚡ <strong>Trình độ:</strong> ${_formatSkillLevel(ext.skillLevel)}</div>
          </div>
        </div>

        <div style="display: flex; justify-content: space-between; align-items: center; margin: 20px 0 10px 0;">
          <h4 style="margin: 0; font-size: 15px;">Các vị trí đang tuyển (${recs.length})</h4>
          ${ext.status === 'SCHEDULED' ? `
            <button class="btn btn-sm btn-outline" id="btn-add-pos-ext">
              + Thêm vị trí (EXT-05)
            </button>
          ` : ''}
        </div>

        <div class="list-group">
          ${recs.map(r => `
            <div class="card" style="padding: 12px; margin-bottom: 8px;">
              <div style="display: flex; justify-content: space-between; align-items: center;">
                <div>
                  <span class="badge badge-green">${_formatPosition(r.position)}</span>
                  <span style="font-size: 13px; margin-left: 6px;">Đã duyệt: <strong>${r.approvedCount}/${r.quantity}</strong></span>
                </div>
                ${_formatRecStatusBadge(r.status)}
              </div>
              <div style="margin-top: 8px; display: flex; justify-content: flex-end; gap: 6px;">
                <button class="btn btn-sm btn-secondary" onclick="window.AppRouter.navigate('recruitment-candidates', { recruitmentId: '${r.id}' })">
                  Ứng viên →
                </button>
              </div>
            </div>
          `).join('')}
        </div>

        ${ext.status === 'SCHEDULED' ? `
          <div style="margin-top: 24px; display: grid; gap: 8px;">
            <button class="btn btn-secondary btn-block" id="btn-edit-ext">
              Chỉnh sửa thông tin trận (EXT-03)
            </button>
            <button class="btn btn-danger-outline btn-block" id="btn-cancel-ext">
              Hủy trận ngoài hệ thống (EXT-04)
            </button>
          </div>
        ` : ''}
      </div>
    `;

    container.querySelector('#btn-add-pos-ext')?.addEventListener('click', () => {
      router.openSheet('add-external-recruitment-position-sheet', { externalMatchId: ext.id });
    });

    container.querySelector('#btn-edit-ext')?.addEventListener('click', () => {
      router.navigate('external-edit', { externalMatchId: ext.id });
    });

    container.querySelector('#btn-cancel-ext')?.addEventListener('click', () => {
      router.openSheet('cancel-external-match-sheet', { externalMatchId: ext.id });
    });
  });

  /* =========================================================================
     EXT-03: Edit External Match (route: 'external-edit')
     ========================================================================= */
  router.registerView('external-edit', (container, params) => {
    router.setHeaderTitle('Sửa trận ngoài');

    const state = store.getState();
    const ext = state.externalMatches.find(e => e.id === params.externalMatchId);

    if (!ext) {
      container.innerHTML = '<div class="notice notice-danger">Trận không tồn tại.</div>';
      return;
    }

    const recs = state.recruitments.filter(r => r.externalMatchId === ext.id);
    // Lock fields if ANY applicant has been approved per EXT-03 & §2.31
    const isLocked = recs.some(r => r.approvedCount > 0);

    container.innerHTML = `
      <div style="padding: 16px 0;">
        ${isLocked ? `
          <div class="notice notice-warning" style="margin-bottom: 16px; font-size: 13px;">
            🔒 <strong>Khóa trường dữ liệu (EXT-03):</strong> Đã có ứng viên được duyệt cho trận này. Địa điểm, ngày giờ và trình độ đã bị khóa để bảo đảm quyền lợi cho mercenary.
          </div>
        ` : ''}

        <form id="form-ext-edit">
          <div class="form-group">
            <label class="form-label" for="edit-title">Tiêu đề trận đấu</label>
            <input type="text" class="form-input" id="edit-title" value="${ext.title || ''}" required>
          </div>

          <div class="form-group">
            <label class="form-label" for="edit-opponent">Tên đối thủ</label>
            <input type="text" class="form-input" id="edit-opponent" value="${ext.opponentName || ''}">
          </div>

          <div class="form-group">
            <label class="form-label" for="edit-venue">Sân thi đấu</label>
            <select class="form-select" id="edit-venue" ${isLocked ? 'disabled' : ''}>
              ${state.venues.map(v => `
                <option value="${v.id}" ${v.id === ext.venueId ? 'selected' : ''}>${v.name}</option>
              `).join('')}
            </select>
          </div>

          <div class="form-group">
            <label class="form-label" for="edit-date">Ngày thi đấu</label>
            <input type="date" class="form-input" id="edit-date" value="${ext.matchDate}" ${isLocked ? 'disabled' : ''}>
          </div>

          <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 10px;">
            <div class="form-group">
              <label class="form-label" for="edit-start">Bắt đầu</label>
              <input type="time" class="form-input" id="edit-start" value="${ext.startTime}" ${isLocked ? 'disabled' : ''}>
            </div>
            <div class="form-group">
              <label class="form-label" for="edit-end">Kết thúc</label>
              <input type="time" class="form-input" id="edit-end" value="${ext.endTime}" ${isLocked ? 'disabled' : ''}>
            </div>
          </div>

          <button type="submit" class="btn btn-primary btn-block" style="margin-top: 16px;">
            Lưu thay đổi
          </button>
        </form>
      </div>
    `;

    container.querySelector('#form-ext-edit')?.addEventListener('submit', (e) => {
      e.preventDefault();
      const form = e.target;

      store.setState(s => {
        const targetExt = s.externalMatches.find(em => em.id === ext.id);
        if (targetExt) {
          targetExt.title = form.querySelector('#edit-title').value;
          targetExt.opponentName = form.querySelector('#edit-opponent').value;
          if (!isLocked) {
            targetExt.venueId = form.querySelector('#edit-venue').value;
            targetExt.matchDate = form.querySelector('#edit-date').value;
            targetExt.startTime = form.querySelector('#edit-start').value;
            targetExt.endTime = form.querySelector('#edit-end').value;
          }
        }
        return s;
      });

      router.showToast('Cập nhật thông tin trận thành công!', 'success');
      router.navigate('external-detail', { externalMatchId: ext.id });
    });
  });

  /* =========================================================================
     EXT-04: Cancel External Match Sheet (sheet: 'cancel-external-match-sheet')
     ========================================================================= */
  router.registerSheet('cancel-external-match-sheet', (body, footer, params, titleElem) => {
    titleElem.textContent = 'Hủy trận ngoài hệ thống';

    const state = store.getState();
    const ext = state.externalMatches.find(e => e.id === params.externalMatchId);

    if (!ext) {
      body.innerHTML = '<div class="notice notice-danger">Không tìm thấy trận ngoài.</div>';
      return;
    }

    body.innerHTML = `
      <div>
        <div class="notice notice-danger" style="font-size: 13px;">
          ⚠️ <strong>Hệ quả khi hủy trận ngoài (EXT-04):</strong>
          <ul style="margin: 6px 0 0 16px; padding: 0;">
            <li>Trận chuyển sang trạng thái <strong>CANCELLED</strong>.</li>
            <li>Toàn bộ tin tuyển quân của trận sẽ tự động chuyển sang <strong>CLOSED</strong>.</li>
            <li>Các đơn đang chờ duyệt (PENDING) sẽ tự động bị từ chối (REJECTED).</li>
            <li>Các ứng viên đã được duyệt (APPROVED) được giữ nguyên lịch sử, không ai bị trừ điểm uy tín. Mercenary nhận được thông báo <code>MERCENARY_GAME_CANCELLED</code>.</li>
          </ul>
        </div>

        <div class="form-group" style="margin-top: 14px;">
          <label class="form-label" for="cancel-reason">Lý do hủy trận (Tùy chọn)</label>
          <input type="text" class="form-input" id="cancel-reason" placeholder="VD: Đối thủ báo bận, mưa lớn...">
        </div>
      </div>
    `;

    footer.innerHTML = `
      <button class="btn btn-secondary" onclick="window.AppRouter.closeSheet()" style="flex: 1;">
        Không hủy
      </button>
      <button class="btn btn-danger" id="btn-confirm-cancel-ext" style="flex: 1;">
        Xác nhận hủy trận
      </button>
    `;

    footer.querySelector('#btn-confirm-cancel-ext')?.addEventListener('click', () => {
      store.setState(s => {
        const targetExt = s.externalMatches.find(em => em.id === ext.id);
        if (targetExt) {
          targetExt.status = 'CANCELLED';
        }

        const relatedRecs = s.recruitments.filter(r => r.externalMatchId === ext.id);
        relatedRecs.forEach(r => {
          r.status = 'CLOSED';
          s.applications.forEach(a => {
            if (a.recruitmentId === r.id) {
              if (a.status === 'PENDING') {
                a.status = 'REJECTED';
              } else if (a.status === 'APPROVED') {
                // Send notification MERCENARY_GAME_CANCELLED
                s.notifications.push({
                  id: 'notif-' + Date.now() + '-' + a.applicantId,
                  userId: a.applicantId,
                  type: 'MERCENARY_GAME_CANCELLED',
                  title: 'Trận đấu đá lẻ đã bị hủy',
                  body: `Đội trưởng đã hủy trận giao lưu ngày ${targetExt.matchDate}. Bạn không bị trừ điểm uy tín.`,
                  referenceType: 'MERCENARY_APPLICATION',
                  referenceId: a.id,
                  read: false,
                  createdAt: s.meta.mockClock
                });
              }
            }
          });
        });

        return s;
      });

      router.closeSheet();
      router.showToast('Đã hủy trận ngoài và thông báo cho mercenary.', 'info');
      router.navigate('players', { tab: 'team-rec' });
    });
  });

  /* =========================================================================
     EXT-05: Add Position to External Match Sheet (sheet: 'add-external-recruitment-position-sheet')
     ========================================================================= */
  router.registerSheet('add-external-recruitment-position-sheet', (body, footer, params, titleElem) => {
    titleElem.textContent = 'Thêm vị trí tuyển quân';

    const state = store.getState();
    const ext = state.externalMatches.find(e => e.id === params.externalMatchId);

    if (!ext) {
      body.innerHTML = '<div class="notice notice-danger">Không tìm thấy trận ngoài.</div>';
      return;
    }

    const existingRecs = state.recruitments.filter(r => r.externalMatchId === ext.id && (r.status === 'OPEN' || r.status === 'FILLED'));
    const usedPositions = existingRecs.map(r => r.position);

    body.innerHTML = `
      <div>
        <div class="card" style="background-color: var(--color-surface-container-low); margin-bottom: 14px;">
          <div>Trận: <strong>${ext.title || 'Trận giao lưu'}</strong></div>
          <div style="font-size: 13px; color: var(--color-on-surface-variant);">
            📅 ${ext.matchDate} (${ext.startTime} - ${ext.endTime})
          </div>
        </div>

        <div class="form-group">
          <label class="form-label" for="add-pos-type">Vị trí cần thêm</label>
          <select class="form-select" id="add-pos-type">
            <option value="GOALKEEPER" ${usedPositions.includes('GOALKEEPER') ? 'disabled' : ''}>🧤 Thủ môn ${usedPositions.includes('GOALKEEPER') ? '(Đã có)' : ''}</option>
            <option value="DEFENDER" ${usedPositions.includes('DEFENDER') ? 'disabled' : ''}>🛡️ Hậu vệ ${usedPositions.includes('DEFENDER') ? '(Đã có)' : ''}</option>
            <option value="MIDFIELDER" ${usedPositions.includes('MIDFIELDER') ? 'disabled' : ''}>⚡ Tiền vệ ${usedPositions.includes('MIDFIELDER') ? '(Đã có)' : ''}</option>
            <option value="FORWARD" ${usedPositions.includes('FORWARD') ? 'disabled' : ''}>⚽ Tiền đạo ${usedPositions.includes('FORWARD') ? '(Đã có)' : ''}</option>
          </select>
        </div>

        <div class="form-group">
          <label class="form-label" for="add-pos-qty">Số lượng cần tuyển (1–10)</label>
          <input type="number" class="form-input" id="add-pos-qty" value="1" min="1" max="10">
        </div>
      </div>
    `;

    footer.innerHTML = `
      <button class="btn btn-secondary" onclick="window.AppRouter.closeSheet()" style="flex: 1;">
        Hủy
      </button>
      <button class="btn btn-primary" id="btn-confirm-add-pos" style="flex: 1;">
        Thêm vị trí
      </button>
    `;

    footer.querySelector('#btn-confirm-add-pos')?.addEventListener('click', () => {
      const pos = body.querySelector('#add-pos-type').value;
      const qty = parseInt(body.querySelector('#add-pos-qty').value, 10);

      const venue = state.venues.find(v => v.id === ext.venueId);

      store.setState(s => {
        s.recruitments.push({
          id: 'rec-' + Date.now(),
          teamId: ext.teamId,
          externalMatchId: ext.id,
          position: pos,
          quantity: qty,
          approvedCount: 0,
          status: 'OPEN',
          area: venue?.wardId === 'ward-ak' ? 'Phường An Khánh, TP. Thủ Đức' : 'TP. Thủ Đức',
          matchDate: ext.matchDate,
          timeSlot: `${ext.startTime} - ${ext.endTime}`,
          venueId: ext.venueId,
          skillLevel: ext.skillLevel,
          note: `Trận: ${ext.title}`,
          createdAt: s.meta.mockClock
        });
        return s;
      });

      router.closeSheet();
      router.showToast('Đã thêm vị trí tuyển quân mới!', 'success');
      router.navigate('external-detail', { externalMatchId: ext.id });
    });
  });

  // Helper functions for format & UI display
  function _formatPosition(pos) {
    switch (pos) {
      case 'GOALKEEPER': return 'Thủ môn';
      case 'DEFENDER': return 'Hậu vệ';
      case 'MIDFIELDER': return 'Tiền vệ';
      case 'FORWARD': return 'Tiền đạo';
      default: return pos || 'Cầu thủ';
    }
  }

  function _formatSkillLevel(lvl) {
    switch (lvl) {
      case 'BEGINNER': return 'Mới chơi';
      case 'INTERMEDIATE': return 'Trung bình';
      case 'ADVANCED': return 'Khá';
      case 'SEMI_PRO': return 'Bán chuyên';
      default: return lvl || 'Tất cả';
    }
  }

  function _formatRecStatusBadge(status) {
    switch (status) {
      case 'OPEN': return '<span class="badge badge-green">ĐANG TUYỂN</span>';
      case 'FILLED': return '<span class="badge badge-amber">ĐÃ ĐỦ NGƯỜI</span>';
      case 'CLOSED': return '<span class="badge badge-gray">ĐÃ ĐÓNG</span>';
      case 'EXPIRED': return '<span class="badge badge-red">HẾT HẠN</span>';
      default: return `<span class="badge badge-gray">${status}</span>`;
    }
  }

  function _formatAppStatusBadge(status) {
    switch (status) {
      case 'PENDING': return '<span class="badge badge-amber">Chờ duyệt</span>';
      case 'APPROVED': return '<span class="badge badge-green">Đã duyệt</span>';
      case 'REJECTED': return '<span class="badge badge-red">Bị từ chối</span>';
      case 'WITHDRAWN': return '<span class="badge badge-gray">Đã rút</span>';
      default: return `<span class="badge badge-gray">${status}</span>`;
    }
  }

  function _ensureStoreMethods(storeInstance) {
    if (!storeInstance.createRecruitmentsForMatch) {
      storeInstance.createRecruitmentsForMatch = function(payload) {
        this.setState(s => {
          payload.positions.forEach(p => {
            s.recruitments.push({
              id: 'rec-' + Date.now() + '-' + Math.random().toString(36).substr(2, 4),
              teamId: payload.teamId,
              matchId: payload.matchId,
              externalMatchId: payload.externalMatchId || null,
              position: p.position,
              quantity: p.quantity,
              approvedCount: 0,
              status: 'OPEN',
              area: payload.area,
              matchDate: payload.matchDate,
              timeSlot: payload.timeSlot,
              venueId: payload.venueId,
              skillLevel: payload.skillLevel,
              note: payload.note || '',
              createdAt: s.meta.mockClock
            });
          });
          return s;
        });
      };
    }
  }
}
