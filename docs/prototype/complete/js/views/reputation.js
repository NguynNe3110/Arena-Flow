/**
 * ArenaFlow Prototype — Reputation, Review & Dispute Module
 * Views: REP-01, REP-02, REP-03, REP-04, REP-05, REP-07, REP-08, REP-10
 * Sheets: REP-06 (create-report-sheet), REP-09 (submit-dispute-sheet)
 * Compliant with AC-P07, AC-P08, J-19, J-20, J-21
 * Owned by @ui-rep-admin
 */

export function initReputationViews(router, store) {

  /* =========================================================================
     HELPER UTILITIES
     ========================================================================= */

  function escapeHtml(str) {
    if (!str) return '';
    return String(str)
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;');
  }

  function formatDate(isoStr) {
    if (!isoStr) return '--';
    try {
      const d = new Date(isoStr);
      return d.toLocaleDateString('vi-VN', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric'
      });
    } catch (e) {
      return isoStr;
    }
  }

  function formatDateTime(isoStr) {
    if (!isoStr) return '--';
    try {
      const d = new Date(isoStr);
      return d.toLocaleString('vi-VN', {
        hour: '2-digit',
        minute: '2-digit',
        day: '2-digit',
        month: '2-digit',
        year: 'numeric'
      });
    } catch (e) {
      return isoStr;
    }
  }

  function getReliabilityBadge(score) {
    if (score >= 95) {
      return `<span class="badge badge-emerald">Xuất sắc (${score})</span>`;
    } else if (score >= 70) {
      return `<span class="badge badge-green">Tốt (${score})</span>`;
    } else if (score >= 50) {
      return `<span class="badge badge-warning">Cảnh báo vàng (${score})</span>`;
    } else {
      return `<span class="badge badge-error">Báo động đỏ (${score})</span>`;
    }
  }

  function getReportTypeLabel(type) {
    switch (type) {
      case 'NOSHOW': return 'Bùng kèo (Không đến sân)';
      case 'BAD_PLAY': return 'Chơi xấu / Bạo lực';
      case 'BAD_COMMUNICATION': return 'Giao tiếp tệ / Xúc phạm';
      case 'SKILL_MISMATCH': return 'Không đúng trình độ';
      case 'FAKE_INFO': return 'Thông tin sai sự thật';
      case 'CLOSED': return 'Địa điểm đã đóng cửa';
      case 'OTHER': return 'Lý do khác';
      default: return type;
    }
  }

  function getReportPenaltyPoints(type) {
    switch (type) {
      case 'NOSHOW': return 5;
      case 'BAD_PLAY': return 3;
      case 'BAD_COMMUNICATION': return 2;
      case 'SKILL_MISMATCH': return 1;
      default: return 0;
    }
  }

  function renderStars(count) {
    let html = '<div class="star-rating-display" style="display: inline-flex; gap: 2px; color: #f59e0b;">';
    for (let i = 1; i <= 5; i++) {
      if (i <= count) {
        html += `<svg width="18" height="18" viewBox="0 0 24 24" fill="currentColor"><polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/></svg>`;
      } else {
        html += `<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/></svg>`;
      }
    }
    html += '</div>';
    return html;
  }

  /* =========================================================================
     REP-01: POST-MATCH RATING (Đánh giá sau trận)
     Route: 'rating-match' (params: { matchId, teamId })
     ========================================================================= */
  router.registerView('rating-match', (container, params) => {
    router.setHeaderTitle('Đánh giá sau trận');

    const state = store.getState();
    const currentUserId = state.currentUserId;
    const matchId = params.matchId || 'match-completed-1';
    const match = state.matches.find(m => m.id === matchId);

    if (!match) {
      container.innerHTML = `
        <div class="empty-state">
          <h3 class="empty-state-title">Không tìm thấy trận đấu</h3>
          <p class="empty-state-desc">Mã trận <code>${escapeHtml(matchId)}</code> không tồn tại.</p>
          <button class="btn btn-secondary" onclick="window.AppRouter.navigate('matches')">Về danh sách kèo</button>
        </div>
      `;
      return;
    }

    // Determine teams
    const hostTeam = state.teams.find(t => t.id === match.hostTeamId);
    const guestTeam = state.teams.find(t => t.id === (match.guestTeamId || match.confirmedGuestTeamId));

    // Determine current user's team in this match
    let myTeam = null;
    let opponentTeam = null;

    if (hostTeam && hostTeam.captainId === currentUserId) {
      myTeam = hostTeam;
      opponentTeam = guestTeam;
    } else if (guestTeam && guestTeam.captainId === currentUserId) {
      myTeam = guestTeam;
      opponentTeam = hostTeam;
    } else if (params.teamId) {
      myTeam = state.teams.find(t => t.id === params.teamId);
      opponentTeam = myTeam?.id === hostTeam?.id ? guestTeam : hostTeam;
    } else {
      // Fallback to active acting team
      myTeam = hostTeam?.captainId === currentUserId ? hostTeam : guestTeam;
      opponentTeam = myTeam?.id === hostTeam?.id ? guestTeam : hostTeam;
    }

    if (!myTeam || !opponentTeam) {
      container.innerHTML = `
        <div class="notice notice-warning" style="margin: 16px;">
          <strong>Quyền thao tác bị từ chối:</strong>
          <p>Bạn không phải là đội trưởng của một trong hai đội tham gia trận đấu này.</p>
          <button class="btn btn-secondary btn-block" style="margin-top: 10px;" onclick="window.AppRouter.goBack()">Quay lại</button>
        </div>
      `;
      return;
    }

    // Check if user is captain of both teams (RATING_003)
    if (hostTeam.captainId === currentUserId && guestTeam && guestTeam.captainId === currentUserId) {
      container.innerHTML = `
        <div class="notice notice-warning" style="margin: 16px;">
          <strong>Không thể tự đánh giá:</strong>
          <p>Bạn đang là đội trưởng của cả hai đội trong trận đấu này theo thỏa thuận ghép, không được tự đánh giá chính mình (Mã lỗi: <code>RATING_003</code>).</p>
          <button class="btn btn-secondary btn-block" style="margin-top: 10px;" onclick="window.AppRouter.goBack()">Quay lại</button>
        </div>
      `;
      return;
    }

    // Check match status: must be ARCHIVED or COMPLETED
    if (match.status !== 'ARCHIVED' && match.status !== 'COMPLETED') {
      container.innerHTML = `
        <div class="notice notice-warning" style="margin: 16px;">
          <strong>Trận đấu chưa sẵn sàng đánh giá:</strong>
          <p>Trận đấu hiện đang ở trạng thái <code>${match.status}</code>. Đánh giá chỉ khả dụng sau khi trận kết thúc (ARCHIVED / COMPLETED).</p>
          <button class="btn btn-secondary btn-block" style="margin-top: 10px;" onclick="window.AppRouter.goBack()">Quay lại</button>
        </div>
      `;
      return;
    }

    // Check if current team already submitted rating (RATING_002)
    const existingReview = state.reviews?.find(
      r => r.matchId === match.id && r.reviewerId === myTeam.id && r.reviewerType === 'TEAM'
    );
    if (existingReview) {
      container.innerHTML = `
        <div class="notice notice-info" style="margin: 16px;">
          <strong>Đội của bạn đã đánh giá trận này:</strong>
          <p>Đánh giá đã được ghi nhận và không thể sửa hoặc xóa theo quy định (AC-P07).</p>
          <div style="margin-top: 12px; display: flex; gap: 8px;">
            <button class="btn btn-primary" onclick="window.AppRouter.navigate('match-reviews', { matchId: '${match.id}', teamId: '${myTeam.id}' })">
              Xem đánh giá trận
            </button>
            <button class="btn btn-secondary" onclick="window.AppRouter.goBack()">Quay lại</button>
          </div>
        </div>
      `;
      return;
    }

    const venue = state.venues.find(v => v.id === match.venueId);

    // Initial state for form
    let selectedStars = 5;
    let isNoShow = false;

    container.innerHTML = `
      <div style="padding: 16px; display: flex; flex-direction: column; gap: 16px;">
        <!-- Match Context Card -->
        <div class="card">
          <div class="card-header">
            <div>
              <span class="badge badge-green">Đã kết thúc</span>
              <h3 class="card-title" style="margin-top: 4px;">Đối phương: ${escapeHtml(opponentTeam.name)}</h3>
              <div class="card-subtitle">
                Đội của bạn: <strong>${escapeHtml(myTeam.name)}</strong>
              </div>
            </div>
            <div class="avatar">
              ${opponentTeam.logoUrl ? `<img src="${opponentTeam.logoUrl}">` : `<span>${escapeHtml(opponentTeam.shortName || 'FC')}</span>`}
            </div>
          </div>
          <div class="card-body" style="font-size: var(--font-size-xs); line-height: 1.6;">
            <div>📅 Ngày thi đấu: ${formatDate(match.matchDate)} (${match.startTime} - ${match.endTime})</div>
            <div>🏟️ Địa điểm: ${escapeHtml(venue?.name || 'Sân bóng đã chọn')}</div>
          </div>
        </div>

        <!-- Rating Form Card -->
        <div class="card">
          <div class="card-header">
            <h3 class="card-title">Đánh giá trận đấu</h3>
          </div>
          <div class="card-body" style="display: flex; flex-direction: column; gap: 16px;">
            
            <!-- No-Show Toggle -->
            <div style="display: flex; align-items: center; justify-content: space-between; padding: 12px; background: var(--color-surface-container-low); border-radius: var(--radius-base); border: 1px solid var(--color-outline-variant);">
              <div>
                <strong style="color: var(--color-on-surface); font-size: var(--font-size-sm);">Đội kia không đến sân (No-Show)</strong>
                <p style="margin: 2px 0 0; font-size: var(--font-size-xs); color: var(--color-on-surface-variant);">
                  Đánh dấu nếu đối phương vắng mặt không báo trước
                </p>
              </div>
              <input type="checkbox" id="rating-noshow-toggle" style="width: 22px; height: 22px; cursor: pointer;">
            </div>

            <!-- Normal Rating Section -->
            <div id="rating-normal-section" style="display: flex; flex-direction: column; gap: 16px;">
              <div class="form-group">
                <label class="form-label">Chất lượng trận đấu (1 - 5 sao)</label>
                <div id="star-picker" style="display: flex; gap: 10px; margin-top: 6px;">
                  ${[1, 2, 3, 4, 5].map(star => `
                    <button type="button" class="star-btn" data-star="${star}" style="background: none; border: none; cursor: pointer; padding: 4px; color: #f59e0b; transition: transform 0.15s;" aria-label="${star} sao">
                      <svg width="34" height="34" viewBox="0 0 24 24" fill="currentColor">
                        <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/>
                      </svg>
                    </button>
                  `).join('')}
                </div>
                <div id="star-label" style="font-size: var(--font-size-xs); font-weight: var(--font-weight-medium); color: var(--color-primary); margin-top: 4px;">
                  5 sao — Rất hài lòng, đá đẹp và đúng giờ
                </div>
              </div>

              <!-- Match Scores (Optional) -->
              <div class="form-group">
                <label class="form-label">Tỷ số chung cuộc (Tùy chọn ghi nhớ)</label>
                <div style="display: grid; grid-template-columns: 1fr auto 1fr; gap: 8px; align-items: center;">
                  <div>
                    <label style="font-size: 11px; color: var(--color-on-surface-variant); display: block; margin-bottom: 2px;">
                      ${escapeHtml(myTeam.name)}
                    </label>
                    <input type="number" id="rating-my-score" class="form-input" min="0" max="99" placeholder="0">
                  </div>
                  <span style="font-size: var(--font-size-lg); font-weight: bold; color: var(--color-on-surface-variant); padding-top: 14px;">:</span>
                  <div>
                    <label style="font-size: 11px; color: var(--color-on-surface-variant); display: block; margin-bottom: 2px;">
                      ${escapeHtml(opponentTeam.name)}
                    </label>
                    <input type="number" id="rating-opp-score" class="form-input" min="0" max="99" placeholder="0">
                  </div>
                </div>
                <span class="field-error-msg" id="rating-score-error" style="display: none;">Vui lòng nhập cả hai tỷ số hoặc để trống cả hai</span>
              </div>
            </div>

            <!-- No-Show Notice Section (Hidden by default) -->
            <div id="rating-noshow-notice" class="notice notice-warning" style="display: none; margin: 0;">
              <strong>Lưu ý khi đánh giá vắng mặt:</strong>
              <p>Trận đấu sẽ không tính sao hay tỷ số. Sau khi gửi, hệ thống sẽ gợi ý bạn gửi báo cáo bùng kèo chính thức để trừ điểm uy tín đối phương.</p>
            </div>

            <!-- Comments -->
            <div class="form-group">
              <label class="form-label" for="rating-comment">Nhận xét chi tiết (Tùy chọn, tối đa 500 ký tự)</label>
              <textarea id="rating-comment" class="form-textarea" rows="3" maxlength="500" placeholder="Chia sẻ cảm nhận về thái độ, lối chơi của đội đối phương..."></textarea>
              <div style="text-align: right; font-size: 11px; color: var(--color-on-surface-variant); margin-top: 4px;">
                <span id="comment-char-count">0</span>/500
              </div>
            </div>

            <div class="field-error-msg" id="rating-general-error" style="display: none;"></div>

            <button type="button" class="btn btn-primary btn-block" id="btn-submit-rating" style="margin-top: 8px;">
              Gửi đánh giá
            </button>
          </div>
        </div>
      </div>
    `;

    // Interactive event handlers
    const noShowToggle = container.querySelector('#rating-noshow-toggle');
    const normalSection = container.querySelector('#rating-normal-section');
    const noShowNotice = container.querySelector('#rating-noshow-notice');
    const starPicker = container.querySelector('#star-picker');
    const starLabel = container.querySelector('#star-label');
    const commentInput = container.querySelector('#rating-comment');
    const commentCharCount = container.querySelector('#comment-char-count');
    const myScoreInput = container.querySelector('#rating-my-score');
    const oppScoreInput = container.querySelector('#rating-opp-score');
    const scoreError = container.querySelector('#rating-score-error');
    const generalError = container.querySelector('#rating-general-error');
    const submitBtn = container.querySelector('#btn-submit-rating');

    const starDescriptions = {
      1: '1 sao — Rất tệ (Thái độ kém, đá xấu)',
      2: '2 sao — Chưa hài lòng (Trễ giờ, thiếu chuyên nghiệp)',
      3: '3 sao — Bình thường (Tạm được)',
      4: '4 sao — Tốt (Đúng giờ, đá nhiệt tình)',
      5: '5 sao — Rất hài lòng, đá đẹp và đúng giờ'
    };

    function updateStarsDisplay() {
      const starBtns = starPicker.querySelectorAll('.star-btn');
      starBtns.forEach(btn => {
        const s = parseInt(btn.getAttribute('data-star'), 10);
        if (s <= selectedStars) {
          btn.innerHTML = `<svg width="34" height="34" viewBox="0 0 24 24" fill="currentColor"><polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/></svg>`;
        } else {
          btn.innerHTML = `<svg width="34" height="34" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/></svg>`;
        }
      });
      starLabel.textContent = starDescriptions[selectedStars];
    }

    starPicker.addEventListener('click', (e) => {
      const btn = e.target.closest('.star-btn');
      if (!btn) return;
      selectedStars = parseInt(btn.getAttribute('data-star'), 10);
      updateStarsDisplay();
    });

    noShowToggle.addEventListener('change', () => {
      isNoShow = noShowToggle.checked;
      if (isNoShow) {
        normalSection.style.display = 'none';
        noShowNotice.style.display = 'block';
      } else {
        normalSection.style.display = 'flex';
        noShowNotice.style.display = 'none';
      }
    });

    commentInput.addEventListener('input', () => {
      commentCharCount.textContent = commentInput.value.length;
    });

    submitBtn.addEventListener('click', () => {
      generalError.style.display = 'none';
      scoreError.style.display = 'none';

      const myScoreVal = myScoreInput.value.trim();
      const oppScoreVal = oppScoreInput.value.trim();

      // Score validation: both or neither
      if (!isNoShow) {
        if ((myScoreVal && !oppScoreVal) || (!myScoreVal && oppScoreVal)) {
          scoreError.style.display = 'block';
          return;
        }
      }

      submitBtn.disabled = true;
      submitBtn.textContent = 'Đang gửi...';

      try {
        const comment = commentInput.value.trim();
        const scoreStr = (myScoreVal && oppScoreVal) ? `${myScoreVal}-${oppScoreVal}` : null;

        // Perform mutation in central store
        store.setState(currState => {
          const freshReviews = [...(currState.reviews || [])];
          const newReview = {
            id: 'rev-' + Date.now(),
            matchId: match.id,
            reviewerId: myTeam.id,
            revieweeId: opponentTeam.id,
            reviewerType: 'TEAM',
            stars: isNoShow ? null : selectedStars,
            score: isNoShow ? null : (selectedStars * 2),
            comment: comment || null,
            noShow: isNoShow,
            scoreGiven: isNoShow ? null : scoreStr,
            createdAt: currState.meta.mockClock
          };
          freshReviews.push(newReview);

          // Update match status to COMPLETED if not already
          const targetMatch = currState.matches.find(m => m.id === match.id);
          if (targetMatch && targetMatch.status !== 'COMPLETED') {
            targetMatch.status = 'COMPLETED';
          }

          // Reliability score updates per Section 1.3:
          // +2 points for first rating in match if not no-show
          // +1 point for 5 stars
          const rateeTeam = currState.teams.find(t => t.id === opponentTeam.id);
          const raterTeam = currState.teams.find(t => t.id === myTeam.id);

          if (raterTeam) {
            raterTeam.reputationScore = Math.min(100, (raterTeam.reputationScore || 80) + 2);
            raterTeam.matchCount = (raterTeam.matchCount || 0) + 1;
          }

          if (rateeTeam && !isNoShow) {
            rateeTeam.matchCount = (rateeTeam.matchCount || 0) + 1;
            if (selectedStars === 5) {
              rateeTeam.reputationScore = Math.min(100, (rateeTeam.reputationScore || 80) + 1);
            }
          }

          return {
            ...currState,
            reviews: freshReviews
          };
        });

        router.showToast('Gửi đánh giá thành công!', 'success');

        if (isNoShow) {
          // If no-show, prompt to report NOSHOW
          router.openDialog('confirm-report-noshow-dialog', {
            matchId: match.id,
            reportedTeamId: opponentTeam.id,
            reportedTeamName: opponentTeam.name
          });
        } else {
          router.navigate('match-reviews', { matchId: match.id, teamId: myTeam.id });
        }
      } catch (err) {
        submitBtn.disabled = false;
        submitBtn.textContent = 'Gửi đánh giá';
        generalError.textContent = err.message || 'Lỗi khi gửi đánh giá';
        generalError.style.display = 'block';
      }
    });
  });

  /* =========================================================================
     CONFIRM REPORT NOSHOW DIALOG (Prompt after No-Show rating)
     ========================================================================= */
  router.registerDialog('confirm-report-noshow-dialog', (container, params) => {
    container.innerHTML = `
      <div style="display: flex; flex-direction: column; gap: 14px;">
        <div style="display: flex; align-items: center; gap: 10px; color: var(--color-warning);">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="12" cy="12" r="10"></circle>
            <line x1="12" y1="8" x2="12" y2="12"></line>
            <line x1="12" y1="16" x2="12.01" y2="16"></line>
          </svg>
          <h3 style="margin: 0; font-size: var(--font-size-lg); color: var(--color-on-surface);">Báo cáo bùng kèo?</h3>
        </div>
        <p style="margin: 0; font-size: var(--font-size-sm); color: var(--color-on-surface-variant); line-height: 1.5;">
          Bạn vừa đánh dấu đội <strong>${escapeHtml(params.reportedTeamName)}</strong> không đến sân. Bạn có muốn gửi báo cáo bùng kèo chính thức để hệ thống trừ 5 điểm uy tín đối phương không?
        </p>
        <div style="display: flex; gap: 8px; margin-top: 6px;">
          <button type="button" class="btn btn-primary" id="btn-dialog-go-report" style="flex: 1;">
            Gửi báo cáo bùng kèo
          </button>
          <button type="button" class="btn btn-secondary" id="btn-dialog-skip-report">
            Để sau
          </button>
        </div>
      </div>
    `;

    container.querySelector('#btn-dialog-go-report').addEventListener('click', () => {
      router.closeDialog();
      router.openSheet('create-report-sheet', {
        targetType: 'TEAM',
        targetId: params.reportedTeamId,
        matchId: params.matchId,
        reportType: 'NOSHOW'
      });
    });

    container.querySelector('#btn-dialog-skip-report').addEventListener('click', () => {
      router.closeDialog();
      router.navigate('match-reviews', { matchId: params.matchId });
    });
  });

  /* =========================================================================
     REP-02: MATCH RATINGS VIEW (Blind rating display per AC-P07)
     Route: 'match-reviews' (params: { matchId, teamId })
     ========================================================================= */
  router.registerView('match-reviews', (container, params) => {
    router.setHeaderTitle('Đánh giá trận đấu');

    const state = store.getState();
    const currentUserId = state.currentUserId;
    const matchId = params.matchId || 'match-completed-1';
    const match = state.matches.find(m => m.id === matchId);

    if (!match) {
      container.innerHTML = `
        <div class="empty-state">
          <h3 class="empty-state-title">Không tìm thấy trận đấu</h3>
          <button class="btn btn-secondary" onclick="window.AppRouter.navigate('matches')">Về danh sách kèo</button>
        </div>
      `;
      return;
    }

    const hostTeam = state.teams.find(t => t.id === match.hostTeamId);
    const guestTeam = state.teams.find(t => t.id === (match.guestTeamId || match.confirmedGuestTeamId));

    // Determine current user's team in this match
    let myTeam = null;
    let opponentTeam = null;

    if (hostTeam && hostTeam.captainId === currentUserId) {
      myTeam = hostTeam;
      opponentTeam = guestTeam;
    } else if (guestTeam && guestTeam.captainId === currentUserId) {
      myTeam = guestTeam;
      opponentTeam = hostTeam;
    } else if (params.teamId) {
      myTeam = state.teams.find(t => t.id === params.teamId);
      opponentTeam = myTeam?.id === hostTeam?.id ? guestTeam : hostTeam;
    } else {
      myTeam = hostTeam;
      opponentTeam = guestTeam;
    }

    // Reviews query
    const allMatchReviews = state.reviews?.filter(r => r.matchId === match.id && r.reviewerType === 'TEAM') || [];
    const givenReview = allMatchReviews.find(r => r.reviewerId === myTeam?.id);
    const receivedReview = allMatchReviews.find(r => r.reviewerId === opponentTeam?.id);

    // CRITICAL (AC-P07): If our team has NOT rated (givenReview == null),
    // we MUST NOT reveal whether opponent has rated or not!
    const hasRated = !!givenReview;

    container.innerHTML = `
      <div style="padding: 16px; display: flex; flex-direction: column; gap: 16px;">
        <!-- Header Summary Card -->
        <div class="card">
          <div class="card-header">
            <div>
              <span class="badge badge-green">Trận đấu đã hoàn thành</span>
              <h3 class="card-title" style="margin-top: 4px;">${escapeHtml(hostTeam?.name)} vs ${escapeHtml(guestTeam?.name)}</h3>
              <div class="card-subtitle">
                Ngày: ${formatDate(match.matchDate)} (${match.startTime} - ${match.endTime})
              </div>
            </div>
          </div>
        </div>

        ${!hasRated ? `
          <!-- AC-P07 Blind Rating State: Hidden received review! -->
          <div class="card" style="border: 2px dashed var(--color-outline);">
            <div class="card-body" style="text-align: center; padding: 24px 16px;">
              <div style="width: 48px; height: 48px; border-radius: 50%; background: var(--color-surface-container); display: grid; place-items: center; margin: 0 auto 12px; color: var(--color-primary);">
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/>
                </svg>
              </div>
              <h4 style="margin: 0 0 6px; font-size: var(--font-size-md); color: var(--color-on-surface);">
                Đội của bạn chưa gửi đánh giá
              </h4>
              <p style="margin: 0 0 16px; font-size: var(--font-size-xs); color: var(--color-on-surface-variant); max-width: 300px; margin-inline: auto; line-height: 1.5;">
                Theo quy định đánh giá ẩn (AC-P07), kết quả đánh giá hai bên chỉ được tiết lộ sau khi đội của bạn hoàn tất gửi đánh giá.
              </p>
              <button class="btn btn-primary" onclick="window.AppRouter.navigate('rating-match', { matchId: '${match.id}', teamId: '${myTeam?.id || ''}' })">
                Đánh giá đối phương ngay
              </button>
            </div>
          </div>
        ` : `
          <!-- Our side has rated: Reveal given review and received review -->
          <div class="card">
            <div class="card-header">
              <h3 class="card-title" style="font-size: var(--font-size-md);">
                Đánh giá bạn đã gửi cho ${escapeHtml(opponentTeam?.name)}
              </h3>
              <span class="badge badge-emerald">Đã gửi</span>
            </div>
            <div class="card-body" style="display: flex; flex-direction: column; gap: 8px;">
              ${givenReview.noShow ? `
                <div class="badge badge-error" style="align-self: flex-start;">Đã đánh dấu: Đối phương không đến sân</div>
              ` : `
                <div style="display: flex; align-items: center; gap: 8px;">
                  ${renderStars(givenReview.stars)}
                  <span style="font-size: var(--font-size-sm); font-weight: bold; color: var(--color-on-surface);">
                    ${givenReview.stars} / 5 sao
                  </span>
                </div>
              `}

              ${givenReview.scoreGiven ? `
                <div style="font-size: var(--font-size-xs); color: var(--color-on-surface-variant);">
                  Tỷ số ghi nhận: <strong>${escapeHtml(givenReview.scoreGiven)}</strong>
                </div>
              ` : ''}

              ${givenReview.comment ? `
                <div style="background: var(--color-surface-container-low); padding: 10px; border-radius: var(--radius-base); font-size: var(--font-size-sm); color: var(--color-on-surface);">
                  "${escapeHtml(givenReview.comment)}"
                </div>
              ` : '<div style="font-size: var(--font-size-xs); color: var(--color-on-surface-variant); font-style: italic;">Không có nhận xét bằng chữ.</div>'}

              <div style="display: flex; justify-content: space-between; align-items: center; font-size: 11px; color: var(--color-on-surface-variant); margin-top: 4px;">
                <span>Gửi lúc: ${formatDateTime(givenReview.createdAt)}</span>
                <span style="color: var(--color-outline);">Không thể sửa / xóa</span>
              </div>
            </div>
          </div>

          <!-- Opponent's rating for us -->
          <div class="card">
            <div class="card-header">
              <h3 class="card-title" style="font-size: var(--font-size-md);">
                Đánh giá từ ${escapeHtml(opponentTeam?.name)} dành cho đội bạn
              </h3>
            </div>
            <div class="card-body">
              ${receivedReview ? `
                <div style="display: flex; flex-direction: column; gap: 8px;">
                  ${receivedReview.noShow ? `
                    <div class="badge badge-error" style="align-self: flex-start;">Đối phương đánh giá: Đội bạn không đến</div>
                  ` : `
                    <div style="display: flex; align-items: center; gap: 8px;">
                      ${renderStars(receivedReview.stars)}
                      <span style="font-size: var(--font-size-sm); font-weight: bold; color: var(--color-on-surface);">
                        ${receivedReview.stars} / 5 sao
                      </span>
                    </div>
                  `}

                  ${receivedReview.comment ? `
                    <div style="background: var(--color-surface-container-low); padding: 10px; border-radius: var(--radius-base); font-size: var(--font-size-sm); color: var(--color-on-surface);">
                      "${escapeHtml(receivedReview.comment)}"
                    </div>
                  ` : '<div style="font-size: var(--font-size-xs); color: var(--color-on-surface-variant); font-style: italic;">Đối phương không để lại nhận xét.</div>'}

                  <div style="font-size: 11px; color: var(--color-on-surface-variant); margin-top: 4px;">
                    Gửi lúc: ${formatDateTime(receivedReview.createdAt)}
                  </div>
                </div>
              ` : `
                <div style="text-align: center; padding: 16px; color: var(--color-on-surface-variant); font-size: var(--font-size-sm);">
                  Đối phương chưa gửi đánh giá cho trận đấu này.
                </div>
              `}
            </div>
          </div>
        `}

        <!-- Actions -->
        <div style="display: flex; flex-direction: column; gap: 8px; margin-top: 8px;">
          <button class="btn btn-outline btn-block" onclick="window.AppRouter.openSheet('create-report-sheet', { targetType: 'TEAM', targetId: '${opponentTeam?.id}', matchId: '${match.id}' })">
            Báo cáo vi phạm đối phương
          </button>
          <button class="btn btn-secondary btn-block" onclick="window.AppRouter.navigate('matches')">
            Về danh sách kèo
          </button>
        </div>
      </div>
    `;
  });

  /* =========================================================================
     REP-03: MERCENARY RATING (Captain rates mercenary, mercenary rates team)
     Route: 'rating-mercenary' (params: { applicationId })
     ========================================================================= */
  router.registerView('rating-mercenary', (container, params) => {
    router.setHeaderTitle('Đánh giá đá lẻ');

    const state = store.getState();
    const currentUserId = state.currentUserId;
    const appId = params.applicationId || 'app-player-rec2';
    const app = state.applications.find(a => a.id === appId);

    if (!app) {
      container.innerHTML = `
        <div class="empty-state">
          <h3 class="empty-state-title">Không tìm thấy đơn đá lẻ</h3>
          <button class="btn btn-secondary" onclick="window.AppRouter.navigate('players')">Quay lại</button>
        </div>
      `;
      return;
    }

    const rec = state.recruitments.find(r => r.id === app.recruitmentId);
    const team = state.teams.find(t => t.id === rec?.teamId);
    const applicant = state.characters[app.applicantId];

    const isCaptain = team?.captainId === currentUserId;
    const isApplicant = app.applicantId === currentUserId;

    if (!isCaptain && !isApplicant) {
      container.innerHTML = `
        <div class="notice notice-danger" style="margin: 16px;">
          Bạn không tham gia vào đơn ứng tuyển này (Mã lỗi: MERCENARY_014).
        </div>
      `;
      return;
    }

    // Existing review check
    const existingRev = state.reviews?.find(
      r => r.applicationId === app.id && r.reviewerId === (isCaptain ? team.id : applicant.id)
    );

    if (existingRev) {
      container.innerHTML = `
        <div class="card" style="margin: 16px;">
          <div class="card-header">
            <h3 class="card-title">Bạn đã đánh giá</h3>
            <span class="badge badge-emerald">Đã gửi</span>
          </div>
          <div class="card-body">
            <p>Đánh giá đã được ghi nhận thành công.</p>
            <button class="btn btn-secondary" onclick="window.AppRouter.goBack()">Quay lại</button>
          </div>
        </div>
      `;
      return;
    }

    let selectedStars = 5;
    let isNoShow = false;

    container.innerHTML = `
      <div style="padding: 16px; display: flex; flex-direction: column; gap: 16px;">
        <div class="card">
          <div class="card-header">
            <div>
              <h3 class="card-title">${isCaptain ? `Đánh giá người chơi: ${escapeHtml(applicant?.displayName)}` : `Đánh giá đội tuyển: ${escapeHtml(team?.name)}`}</h3>
              <div class="card-subtitle">Vị trí: ${escapeHtml(rec?.position || 'Cầu thủ')} • Đã được duyệt</div>
            </div>
          </div>
          <div class="card-body" style="display: flex; flex-direction: column; gap: 14px;">
            ${isCaptain ? `
              <!-- Captain can mark No-Show for mercenary -->
              <div style="display: flex; align-items: center; justify-content: space-between; padding: 10px; background: var(--color-surface-container-low); border-radius: var(--radius-base);">
                <label style="font-size: var(--font-size-sm); font-weight: 500;">Người chơi không đến sân (No-Show)</label>
                <input type="checkbox" id="merc-noshow-toggle" style="width: 20px; height: 20px;">
              </div>
            ` : ''}

            <div id="merc-stars-wrapper" class="form-group">
              <label class="form-label">Chất lượng (1 - 5 sao)</label>
              <div id="merc-star-picker" style="display: flex; gap: 8px; margin-top: 4px;">
                ${[1, 2, 3, 4, 5].map(s => `
                  <button type="button" class="star-btn" data-star="${s}" style="background: none; border: none; cursor: pointer; color: #f59e0b; padding: 4px;">
                    <svg width="32" height="32" viewBox="0 0 24 24" fill="currentColor">
                      <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/>
                    </svg>
                  </button>
                `).join('')}
              </div>
            </div>

            <div class="form-group">
              <label class="form-label">Nhận xét chi tiết</label>
              <textarea id="merc-rating-comment" class="form-textarea" rows="3" placeholder="Đánh giá kỹ năng, tinh thần thi đấu..."></textarea>
            </div>

            <button type="button" class="btn btn-primary btn-block" id="btn-submit-merc-rating">
              Hoàn tất đánh giá
            </button>
          </div>
        </div>
      </div>
    `;

    const noShowCheckbox = container.querySelector('#merc-noshow-toggle');
    const starsWrapper = container.querySelector('#merc-stars-wrapper');
    const starBtns = container.querySelectorAll('.star-btn');
    const submitBtn = container.querySelector('#btn-submit-merc-rating');
    const commentInput = container.querySelector('#merc-rating-comment');

    if (noShowCheckbox) {
      noShowCheckbox.addEventListener('change', () => {
        isNoShow = noShowCheckbox.checked;
        starsWrapper.style.display = isNoShow ? 'none' : 'block';
      });
    }

    starBtns.forEach(btn => {
      btn.addEventListener('click', () => {
        selectedStars = parseInt(btn.getAttribute('data-star'), 10);
        starBtns.forEach(b => {
          const s = parseInt(b.getAttribute('data-star'), 10);
          b.innerHTML = `<svg width="32" height="32" viewBox="0 0 24 24" fill="${s <= selectedStars ? 'currentColor' : 'none'}" stroke="currentColor" stroke-width="1.8"><polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/></svg>`;
        });
      });
    });

    submitBtn.addEventListener('click', () => {
      submitBtn.disabled = true;
      submitBtn.textContent = 'Đang lưu...';

      store.setState(curr => {
        const freshRev = [...(curr.reviews || [])];
        freshRev.push({
          id: 'rev-merc-' + Date.now(),
          applicationId: app.id,
          reviewerId: isCaptain ? team.id : applicant.id,
          revieweeId: isCaptain ? applicant.id : team.id,
          reviewerType: isCaptain ? 'CAPTAIN_FOR_MERC' : 'MERC_FOR_TEAM',
          stars: isNoShow ? null : selectedStars,
          noShow: isNoShow,
          comment: commentInput.value.trim() || null,
          createdAt: curr.meta.mockClock
        });

        // Update score
        if (isCaptain && applicant) {
          if (isNoShow) {
            applicant.reputationScore = Math.max(0, (applicant.reputationScore || 100) - 5);
          } else {
            applicant.reputationScore = Math.min(100, (applicant.reputationScore || 100) + 2);
            applicant.matchCount = (applicant.matchCount || 0) + 1;
          }
        }

        return { ...curr, reviews: freshRev };
      });

      router.showToast('Đánh giá đá lẻ thành công!', 'success');
      router.navigate('players');
    });
  });

  /* =========================================================================
     REP-04: TEAM REPUTATION CARD & DETAIL
     Route: 'team-reputation' (params: { teamId })
     ========================================================================= */
  router.registerView('team-reputation', (container, params) => {
    router.setHeaderTitle('Uy tín đội bóng');

    const state = store.getState();
    const teamId = params.teamId || state.actingTeamId || 'team-alpha';
    const team = state.teams.find(t => t.id === teamId);

    if (!team) {
      container.innerHTML = `
        <div class="empty-state">
          <h3 class="empty-state-title">Không tìm thấy đội bóng</h3>
          <button class="btn btn-secondary" onclick="window.AppRouter.navigate('teams')">Về danh sách đội</button>
        </div>
      `;
      return;
    }

    const captain = state.characters[team.captainId];
    const score = team.reputationScore !== undefined ? team.reputationScore : 84;
    const matchCount = team.matchCount || 0;

    // Completion rate requirement: ONLY shown if >= 5 matches!
    const isNewcomer = matchCount < 5;
    const completionRate = isNewcomer ? null : (team.completionRate || 92);

    // Verification badge requirement:
    // 1. Captain has phone
    // 2. completedMatches > 5
    // 3. avgRating > 3.0
    // 4. score >= 70
    const isVerified = Boolean(
      captain?.phone &&
      matchCount > 5 &&
      (team.ratingAvg || 5.0) > 3.0 &&
      score >= 70
    );

    // Filter reviews
    const matchReviews = state.reviews?.filter(r => r.revieweeId === team.id && r.reviewerType === 'TEAM') || [];
    const mercReviews = state.reviews?.filter(r => r.revieweeId === team.id && r.reviewerType === 'MERC_FOR_TEAM') || [];

    container.innerHTML = `
      <div style="padding: 16px; display: flex; flex-direction: column; gap: 16px;">
        <!-- Main Reputation Card -->
        <div class="card" style="background: linear-gradient(135deg, var(--color-surface) 0%, var(--color-surface-container-low) 100%);">
          <div class="card-header" style="align-items: flex-start;">
            <div style="display: flex; gap: 12px; align-items: center;">
              <div class="avatar avatar-lg">
                ${team.logoUrl ? `<img src="${team.logoUrl}">` : `<span>${escapeHtml(team.shortName || 'FC')}</span>`}
              </div>
              <div>
                <div style="display: flex; align-items: center; gap: 6px;">
                  <h2 style="margin: 0; font-size: var(--font-size-xl); font-weight: bold; color: var(--color-on-surface);">
                    ${escapeHtml(team.name)}
                  </h2>
                  ${isVerified ? `
                    <span title="Đội bóng đã xác minh" style="color: var(--color-primary); display: inline-flex;">
                      <svg width="20" height="20" viewBox="0 0 24 24" fill="currentColor">
                        <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z"/>
                      </svg>
                    </span>
                  ` : ''}
                </div>
                <div style="font-size: var(--font-size-xs); color: var(--color-on-surface-variant); margin-top: 2px;">
                  Đội trưởng: <strong>${escapeHtml(captain?.displayName || 'Đội trưởng')}</strong>
                </div>
                <div style="margin-top: 6px;">
                  ${getReliabilityBadge(score)}
                </div>
              </div>
            </div>
          </div>

          <div class="divider" style="margin: 12px 0;"></div>

          <!-- Score Metrics Grid -->
          <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 12px;">
            <div style="background: var(--color-surface); padding: 12px; border-radius: var(--radius-base); border: 1px solid var(--color-outline-variant); text-align: center;">
              <div style="font-size: var(--font-size-xs); color: var(--color-on-surface-variant); margin-bottom: 4px;">
                Tỷ lệ hoàn thành
              </div>
              ${isNewcomer ? `
                <div style="font-size: var(--font-size-sm); font-weight: bold; color: var(--color-warning);">
                  Mới tham gia ${matchCount}/5 trận
                </div>
                <span class="badge badge-amber" style="margin-top: 4px; font-size: 10px;">Chưa đủ 5 trận</span>
              ` : `
                <div style="font-size: var(--font-size-2xl); font-weight: bold; color: var(--color-primary);">
                  ${completionRate}%
                </div>
                <span style="font-size: 11px; color: var(--color-on-surface-variant);">${matchCount} trận đã đá</span>
              `}
            </div>

            <div style="background: var(--color-surface); padding: 12px; border-radius: var(--radius-base); border: 1px solid var(--color-outline-variant); text-align: center;">
              <div style="font-size: var(--font-size-xs); color: var(--color-on-surface-variant); margin-bottom: 4px;">
                Đánh giá đối thủ
              </div>
              <div style="display: flex; align-items: center; justify-content: center; gap: 4px;">
                <span style="font-size: var(--font-size-2xl); font-weight: bold; color: var(--color-on-surface);">
                  ${(team.ratingAvg || 5.0).toFixed(1)}
                </span>
                <span style="color: #f59e0b; font-size: var(--font-size-lg);">★</span>
              </div>
              <span style="font-size: 11px; color: var(--color-on-surface-variant);">${matchReviews.length} lượt đánh giá</span>
            </div>
          </div>
        </div>

        <!-- Verification Status Notice -->
        <div class="card">
          <div class="card-header">
            <h3 class="card-title" style="font-size: var(--font-size-md);">Tiêu chí huy hiệu xác minh</h3>
            ${isVerified ? '<span class="badge badge-green">Đạt xác minh</span>' : '<span class="badge badge-gray">Chưa đạt</span>'}
          </div>
          <div class="card-body" style="font-size: var(--font-size-xs); line-height: 1.8;">
            <div style="display: flex; align-items: center; gap: 8px;">
              <span>${captain?.phone ? '✅' : '❌'}</span>
              <span>Đội trưởng có số điện thoại xác thực (${captain?.phone || 'Chưa có'})</span>
            </div>
            <div style="display: flex; align-items: center; gap: 8px;">
              <span>${matchCount > 5 ? '✅' : '❌'}</span>
              <span>Số trận hoàn thành > 5 trận (Hiện có: ${matchCount})</span>
            </div>
            <div style="display: flex; align-items: center; gap: 8px;">
              <span>${(team.ratingAvg || 5.0) > 3.0 ? '✅' : '❌'}</span>
              <span>Sao trung bình > 3.0 sao (Hiện tại: ${(team.ratingAvg || 5.0).toFixed(1)})</span>
            </div>
            <div style="display: flex; align-items: center; gap: 8px;">
              <span>${score >= 70 ? '✅' : '❌'}</span>
              <span>Điểm uy tín >= 70 điểm (Hiện tại: ${score})</span>
            </div>
          </div>
        </div>

        <!-- Mercenary Ratings Distinct Section (REP-04) -->
        <div class="card">
          <div class="card-header">
            <div>
              <h3 class="card-title" style="font-size: var(--font-size-md);">Đánh giá từ người chơi đá lẻ</h3>
              <div class="card-subtitle">Được tách riêng biệt so với đánh giá đối thủ</div>
            </div>
            <span class="badge badge-blue">${mercReviews.length} nhận xét</span>
          </div>
          <div class="card-body">
            ${mercReviews.length > 0 ? `
              <div style="display: flex; flex-direction: column; gap: 10px;">
                ${mercReviews.map(r => `
                  <div style="background: var(--color-surface-container-low); padding: 10px; border-radius: var(--radius-base);">
                    <div style="display: flex; justify-content: space-between; align-items: center;">
                      ${renderStars(r.stars)}
                      <span style="font-size: 11px; color: var(--color-on-surface-variant);">${formatDate(r.createdAt)}</span>
                    </div>
                    ${r.comment ? `<p style="margin: 6px 0 0; font-size: var(--font-size-xs); color: var(--color-on-surface);">"${escapeHtml(r.comment)}"</p>` : ''}
                  </div>
                `).join('')}
              </div>
            ` : `
              <div style="text-align: center; padding: 14px; color: var(--color-on-surface-variant); font-size: var(--font-size-xs);">
                Đội chưa có lượt đánh giá nào từ người chơi đá lẻ.
              </div>
            `}
          </div>
        </div>

        <!-- Navigation buttons -->
        <div style="display: flex; gap: 8px;">
          <button class="btn btn-outline" style="flex: 1;" onclick="window.AppRouter.navigate('reports-list', { teamId: '${team.id}' })">
            Xem lịch sử báo cáo
          </button>
          <button class="btn btn-secondary" style="flex: 1;" onclick="window.AppRouter.goBack()">
            Quay lại
          </button>
        </div>
      </div>
    `;
  });

  /* =========================================================================
     REP-05: MERCENARY / INDIVIDUAL REPUTATION CARD
     Route: 'user-reputation' (params: { userId })
     ========================================================================= */
  router.registerView('user-reputation', (container, params) => {
    router.setHeaderTitle('Uy tín cá nhân');

    const state = store.getState();
    const userId = params.userId || state.currentUserId;
    const user = state.characters[userId];

    if (!user) {
      container.innerHTML = `
        <div class="empty-state">
          <h3 class="empty-state-title">Không tìm thấy người chơi</h3>
          <button class="btn btn-secondary" onclick="window.AppRouter.navigate('players')">Quay lại</button>
        </div>
      `;
      return;
    }

    const score = user.reputationScore !== undefined ? user.reputationScore : 95;
    const matchCount = user.matchCount || 0;
    const isNewcomer = matchCount < 5;

    // Reviews given to this user
    const captainReviews = state.reviews?.filter(r => r.revieweeId === user.id && r.reviewerType === 'CAPTAIN_FOR_MERC') || [];

    container.innerHTML = `
      <div style="padding: 16px; display: flex; flex-direction: column; gap: 16px;">
        <div class="card">
          <div class="card-header">
            <div style="display: flex; gap: 12px; align-items: center;">
              <div class="avatar avatar-lg">
                ${user.avatarUrl ? `<img src="${user.avatarUrl}">` : `<span>${(user.displayName || 'U').slice(0, 1)}</span>`}
              </div>
              <div>
                <h2 style="margin: 0; font-size: var(--font-size-lg); font-weight: bold; color: var(--color-on-surface);">
                  ${escapeHtml(user.displayName || 'Người chơi')}
                </h2>
                <div style="font-size: var(--font-size-xs); color: var(--color-on-surface-variant); margin-top: 2px;">
                  Vị trí sở trường: <strong>${escapeHtml(user.preferredPosition || 'Đa năng')}</strong>
                </div>
                <div style="margin-top: 6px;">
                  ${getReliabilityBadge(score)}
                </div>
              </div>
            </div>
          </div>

          <div class="divider" style="margin: 12px 0;"></div>

          <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 12px;">
            <div style="background: var(--color-surface); padding: 12px; border-radius: var(--radius-base); border: 1px solid var(--color-outline-variant); text-align: center;">
              <div style="font-size: var(--font-size-xs); color: var(--color-on-surface-variant); margin-bottom: 4px;">
                Tỷ lệ tham gia
              </div>
              ${isNewcomer ? `
                <div style="font-size: var(--font-size-sm); font-weight: bold; color: var(--color-warning);">
                  Người chơi mới (${matchCount}/5 trận)
                </div>
              ` : `
                <div style="font-size: var(--font-size-2xl); font-weight: bold; color: var(--color-primary);">
                  ${user.completionRate || 95}%
                </div>
                <span style="font-size: 11px; color: var(--color-on-surface-variant);">${matchCount} trận đã đá</span>
              `}
            </div>

            <div style="background: var(--color-surface); padding: 12px; border-radius: var(--radius-base); border: 1px solid var(--color-outline-variant); text-align: center;">
              <div style="font-size: var(--font-size-xs); color: var(--color-on-surface-variant); margin-bottom: 4px;">
                Đánh giá từ đội trưởng
              </div>
              <div style="font-size: var(--font-size-2xl); font-weight: bold; color: var(--color-on-surface);">
                ${captainReviews.length > 0 ? (captainReviews.reduce((sum, r) => sum + (r.stars || 0), 0) / captainReviews.length).toFixed(1) : '5.0'}
                <span style="color: #f59e0b; font-size: var(--font-size-lg);">★</span>
              </div>
              <span style="font-size: 11px; color: var(--color-on-surface-variant);">${captainReviews.length} lượt nhận xét</span>
            </div>
          </div>
        </div>

        <!-- Reviews list -->
        <div class="card">
          <div class="card-header">
            <h3 class="card-title" style="font-size: var(--font-size-md);">Lịch sử nhận xét</h3>
          </div>
          <div class="card-body">
            ${captainReviews.length > 0 ? `
              <div style="display: flex; flex-direction: column; gap: 10px;">
                ${captainReviews.map(r => `
                  <div style="background: var(--color-surface-container-low); padding: 10px; border-radius: var(--radius-base);">
                    <div style="display: flex; justify-content: space-between; align-items: center;">
                      ${renderStars(r.stars)}
                      <span style="font-size: 11px; color: var(--color-on-surface-variant);">${formatDate(r.createdAt)}</span>
                    </div>
                    ${r.comment ? `<p style="margin: 6px 0 0; font-size: var(--font-size-xs); color: var(--color-on-surface);">"${escapeHtml(r.comment)}"</p>` : ''}
                  </div>
                `).join('')}
              </div>
            ` : `
              <div style="text-align: center; padding: 14px; color: var(--color-on-surface-variant); font-size: var(--font-size-xs);">
                Chưa có nhận xét nào từ các đội trưởng.
              </div>
            `}
          </div>
        </div>

        <button class="btn btn-secondary btn-block" onclick="window.AppRouter.goBack()">
          Quay lại
        </button>
      </div>
    `;
  });

  /* =========================================================================
     REP-06: SUBMIT REPORT SHEET (Gửi báo cáo vi phạm)
     Sheet: 'create-report-sheet'
     params: { targetType, targetId, matchId, conversationId, reportType }
     ========================================================================= */
  router.registerSheet('create-report-sheet', (body, footer, params, titleElem) => {
    titleElem.textContent = 'Báo cáo vi phạm';

    const state = store.getState();
    const targetType = params.targetType || 'TEAM';
    const targetId = params.targetId;
    const matchId = params.matchId || null;

    let targetName = 'Đối tượng';
    if (targetType === 'TEAM') {
      const t = state.teams.find(x => x.id === targetId);
      targetName = t ? t.name : 'Đội bóng';
    } else if (targetType === 'MERCENARY') {
      const u = state.characters[targetId];
      targetName = u ? u.displayName : 'Người chơi';
    } else if (targetType === 'VENUE') {
      const v = state.venues.find(x => x.id === targetId);
      targetName = v ? v.name : 'Địa điểm sân';
    }

    const preselectedType = params.reportType || 'NOSHOW';

    body.innerHTML = `
      <div style="display: flex; flex-direction: column; gap: 14px;">
        <div style="background: var(--color-surface-container-low); padding: 10px 12px; border-radius: var(--radius-base); font-size: var(--font-size-xs);">
          Đối tượng bị báo cáo: <strong>${escapeHtml(targetName)}</strong>
        </div>

        <div class="form-group">
          <label class="form-label">Loại vi phạm</label>
          <div style="display: flex; flex-direction: column; gap: 8px;" id="report-type-radios">
            ${targetType === 'VENUE' ? `
              <label class="radio-card" style="display: flex; gap: 10px; align-items: flex-start; padding: 10px; border: 1px solid var(--color-outline-variant); border-radius: var(--radius-base); cursor: pointer;">
                <input type="radio" name="reportType" value="CLOSED" ${preselectedType === 'CLOSED' ? 'checked' : ''} style="margin-top: 3px;">
                <div>
                  <strong style="font-size: var(--font-size-sm); color: var(--color-on-surface);">Sân đã đóng cửa / giải tỏa</strong>
                  <p style="margin: 2px 0 0; font-size: 11px; color: var(--color-on-surface-variant);">Địa điểm không còn hoạt động thể thao</p>
                </div>
              </label>
              <label class="radio-card" style="display: flex; gap: 10px; align-items: flex-start; padding: 10px; border: 1px solid var(--color-outline-variant); border-radius: var(--radius-base); cursor: pointer;">
                <input type="radio" name="reportType" value="WRONG_INFO" ${preselectedType === 'WRONG_INFO' ? 'checked' : ''} style="margin-top: 3px;">
                <div>
                  <strong style="font-size: var(--font-size-sm); color: var(--color-on-surface);">Sai thông tin liên hệ / địa chỉ</strong>
                  <p style="margin: 2px 0 0; font-size: 11px; color: var(--color-on-surface-variant);">Số điện thoại sai hoặc địa chỉ không đúng thực tế</p>
                </div>
              </label>
              <label class="radio-card" style="display: flex; gap: 10px; align-items: flex-start; padding: 10px; border: 1px solid var(--color-outline-variant); border-radius: var(--radius-base); cursor: pointer;">
                <input type="radio" name="reportType" value="OTHER" ${preselectedType === 'OTHER' ? 'checked' : ''} style="margin-top: 3px;">
                <div>
                  <strong style="font-size: var(--font-size-sm); color: var(--color-on-surface);">Lý do khác</strong>
                </div>
              </label>
            ` : `
              <label class="radio-card" style="display: flex; gap: 10px; align-items: flex-start; padding: 10px; border: 1px solid var(--color-outline-variant); border-radius: var(--radius-base); cursor: pointer;">
                <input type="radio" name="reportType" value="NOSHOW" ${preselectedType === 'NOSHOW' ? 'checked' : ''} style="margin-top: 3px;">
                <div>
                  <strong style="font-size: var(--font-size-sm); color: var(--color-on-surface);">Bùng kèo — Không đến sân (-5 điểm)</strong>
                  <p style="margin: 2px 0 0; font-size: 11px; color: var(--color-on-surface-variant);">Vắng mặt không báo trước (Bị báo cáo có 24h để khiếu nại)</p>
                </div>
              </label>
              <label class="radio-card" style="display: flex; gap: 10px; align-items: flex-start; padding: 10px; border: 1px solid var(--color-outline-variant); border-radius: var(--radius-base); cursor: pointer;">
                <input type="radio" name="reportType" value="BAD_PLAY" ${preselectedType === 'BAD_PLAY' ? 'checked' : ''} style="margin-top: 3px;">
                <div>
                  <strong style="font-size: var(--font-size-sm); color: var(--color-on-surface);">Chơi xấu / Bạo lực sân cỏ (-3 điểm)</strong>
                  <p style="margin: 2px 0 0; font-size: 11px; color: var(--color-on-surface-variant);">Triệt hạ thô bạo, phi thể thao</p>
                </div>
              </label>
              <label class="radio-card" style="display: flex; gap: 10px; align-items: flex-start; padding: 10px; border: 1px solid var(--color-outline-variant); border-radius: var(--radius-base); cursor: pointer;">
                <input type="radio" name="reportType" value="BAD_COMMUNICATION" ${preselectedType === 'BAD_COMMUNICATION' ? 'checked' : ''} style="margin-top: 3px;">
                <div>
                  <strong style="font-size: var(--font-size-sm); color: var(--color-on-surface);">Giao tiếp tệ / Xúc phạm (-2 điểm)</strong>
                  <p style="margin: 2px 0 0; font-size: 11px; color: var(--color-on-surface-variant);">Lời lẽ khiếm nhã trong phòng chat hoặc tại sân</p>
                </div>
              </label>
              <label class="radio-card" style="display: flex; gap: 10px; align-items: flex-start; padding: 10px; border: 1px solid var(--color-outline-variant); border-radius: var(--radius-base); cursor: pointer;">
                <input type="radio" name="reportType" value="SKILL_MISMATCH" ${preselectedType === 'SKILL_MISMATCH' ? 'checked' : ''} style="margin-top: 3px;">
                <div>
                  <strong style="font-size: var(--font-size-sm); color: var(--color-on-surface);">Không đúng trình độ (-1 điểm)</strong>
                  <p style="margin: 2px 0 0; font-size: 11px; color: var(--color-on-surface-variant);">Trình độ thực tế chênh lệch quá lớn so với cam kết</p>
                </div>
              </label>
            `}
          </div>
        </div>

        <div class="form-group">
          <label class="form-label" for="report-reason-input">Lý do báo cáo chi tiết (Bắt buộc, 1 - 1000 ký tự)</label>
          <textarea id="report-reason-input" class="form-textarea" rows="3" maxlength="1000" placeholder="Mô tả cụ thể sự việc đã xảy ra..."></textarea>
          <span class="field-error-msg" id="report-error-msg" style="display: none;"></span>
        </div>
      </div>
    `;

    footer.innerHTML = `
      <div style="display: flex; gap: 8px;">
        <button type="button" class="btn btn-secondary" style="flex: 1;" onclick="window.AppRouter.closeSheet()">
          Hủy bỏ
        </button>
        <button type="button" class="btn btn-danger" style="flex: 1;" id="btn-confirm-submit-report">
          Gửi báo cáo
        </button>
      </div>
    `;

    const reasonInput = body.querySelector('#report-reason-input');
    const errorMsg = body.querySelector('#report-error-msg');
    const submitBtn = footer.querySelector('#btn-confirm-submit-report');

    submitBtn.addEventListener('click', () => {
      errorMsg.style.display = 'none';
      const reason = reasonInput.value.trim();
      if (!reason || reason.length < 1) {
        errorMsg.textContent = 'Vui lòng nhập lý do báo cáo cụ thể (tối thiểu 1 ký tự).';
        errorMsg.style.display = 'block';
        return;
      }

      const checkedRadio = body.querySelector('input[name="reportType"]:checked');
      const selectedType = checkedRadio ? checkedRadio.value : 'NOSHOW';

      // Check duplicate report for same match and type (REPORT_004)
      const existingRep = state.reports?.find(
        r => r.matchId === matchId && r.reportedId === targetId && r.type === selectedType
      );
      if (existingRep) {
        errorMsg.textContent = 'Bạn đã gửi báo cáo loại này cho trận đấu này rồi (Mã lỗi: REPORT_004).';
        errorMsg.style.display = 'block';
        return;
      }

      submitBtn.disabled = true;
      submitBtn.textContent = 'Đang gửi...';

      try {
        const reporterId = targetType === 'TEAM'
          ? (state.actingTeamId || state.currentUserId)
          : state.currentUserId;

        const penalty = getReportPenaltyPoints(selectedType);

        // Submit via store mutation
        store.setState(currState => {
          const freshReports = [...(currState.reports || [])];
          const newReport = {
            id: 'rep-' + Date.now(),
            reporterType: targetType,
            reporterId,
            reportedId: targetId,
            matchId: matchId,
            type: selectedType,
            reason: reason,
            status: 'AUTO_ACCEPTED', // Accepted automatically per spec
            reliabilityPenalty: penalty,
            createdAt: currState.meta.mockClock,
            canDisputeUntil: selectedType === 'NOSHOW'
              ? new Date(new Date(currState.meta.mockClock).getTime() + 24 * 3600 * 1000).toISOString()
              : null,
            dispute: null
          };
          freshReports.unshift(newReport);

          // Deduct penalty from target team / user immediately
          if (targetType === 'TEAM') {
            const reportedTeam = currState.teams.find(t => t.id === targetId);
            if (reportedTeam) {
              reportedTeam.reputationScore = Math.max(0, (reportedTeam.reputationScore || 80) - penalty);
            }
          } else if (targetType === 'MERCENARY') {
            const reportedUser = currState.characters[targetId];
            if (reportedUser) {
              reportedUser.reputationScore = Math.max(0, (reportedUser.reputationScore || 100) - penalty);
            }
          }

          return {
            ...currState,
            reports: freshReports
          };
        });

        router.closeSheet();
        router.showToast('Đã gửi báo cáo vi phạm thành công!', 'success');
        router.navigate('reports-list', { teamId: targetId });
      } catch (err) {
        submitBtn.disabled = false;
        submitBtn.textContent = 'Gửi báo cáo';
        errorMsg.textContent = err.message || 'Lỗi gửi báo cáo';
        errorMsg.style.display = 'block';
      }
    });
  });

  /* =========================================================================
     REP-07: REPORTS LIST (Danh sách báo cáo đã gửi & đã nhận)
     Route: 'reports-list' (params: { teamId })
     ========================================================================= */
  router.registerView('reports-list', (container, params) => {
    router.setHeaderTitle('Báo cáo vi phạm');

    const state = store.getState();
    const currentUserId = state.currentUserId;
    const teamId = params.teamId || state.actingTeamId || 'team-beta';
    const team = state.teams.find(t => t.id === teamId);

    let activeTab = params.direction || 'RECEIVED'; // 'RECEIVED' or 'SENT'

    function renderContent() {
      const allReports = state.reports || [];
      const receivedReports = allReports.filter(r => r.reportedId === teamId);
      const sentReports = allReports.filter(r => r.reporterId === teamId);

      const displayList = activeTab === 'RECEIVED' ? receivedReports : sentReports;

      container.innerHTML = `
        <div style="padding: 16px; display: flex; flex-direction: column; gap: 14px;">
          <!-- Team Header -->
          <div style="display: flex; justify-content: space-between; align-items: center;">
            <div>
              <h3 style="margin: 0; font-size: var(--font-size-md); color: var(--color-on-surface);">
                Đội: <strong>${escapeHtml(team?.name || 'Đội của bạn')}</strong>
              </h3>
              <p style="margin: 2px 0 0; font-size: var(--font-size-xs); color: var(--color-on-surface-variant);">
                Lịch sử báo cáo và khiếu nại tranh chấp
              </p>
            </div>
            <button class="btn btn-sm btn-outline" onclick="window.AppRouter.navigate('team-reputation', { teamId: '${teamId}' })">
              Điểm uy tín
            </button>
          </div>

          <!-- Tabs (RECEIVED vs SENT) -->
          <div class="chip-group">
            <button class="chip ${activeTab === 'RECEIVED' ? 'is-active' : ''}" id="tab-received">
              Đội bị báo cáo (${receivedReports.length})
            </button>
            <button class="chip ${activeTab === 'SENT' ? 'is-active' : ''}" id="tab-sent">
              Đội đã báo cáo (${sentReports.length})
            </button>
          </div>

          <!-- Reports List -->
          <div style="display: flex; flex-direction: column; gap: 10px;">
            ${displayList.length > 0 ? displayList.map(rep => {
              const isReceived = activeTab === 'RECEIVED';
              const counterpartId = isReceived ? rep.reporterId : rep.reportedId;
              const counterpartTeam = state.teams.find(t => t.id === counterpartId);
              const counterpartName = counterpartTeam ? counterpartTeam.name : (rep.reporterType === 'INDIVIDUAL' ? 'Người chơi lẻ' : 'Đối phương');

              // Status badge determination
              let statusBadge = '<span class="badge badge-gray">Đang xử lý</span>';
              if (rep.status === 'AUTO_ACCEPTED') {
                statusBadge = '<span class="badge badge-warning">Đã trừ điểm</span>';
              } else if (rep.status === 'DISPUTED') {
                statusBadge = '<span class="badge badge-amber">Đang tranh chấp</span>';
              } else if (rep.status === 'CONFIRMED' || rep.status === 'RESOLVED') {
                statusBadge = '<span class="badge badge-error">Xác nhận vi phạm</span>';
              } else if (rep.status === 'REJECTED') {
                statusBadge = '<span class="badge badge-emerald">Đã bác — Hoàn điểm</span>';
              }

              // Check dispute eligibility (AC-P08)
              const now = new Date(state.meta.mockClock).getTime();
              const canDispute = Boolean(
                isReceived &&
                rep.type === 'NOSHOW' &&
                rep.status === 'AUTO_ACCEPTED' &&
                rep.canDisputeUntil &&
                now <= new Date(rep.canDisputeUntil).getTime()
              );

              return `
                <div class="card" style="cursor: pointer;" onclick="window.AppRouter.navigate('report-detail', { reportId: '${rep.id}' })">
                  <div class="card-header" style="align-items: flex-start;">
                    <div>
                      <div style="display: flex; align-items: center; gap: 6px;">
                        <span class="badge ${rep.type === 'NOSHOW' ? 'badge-error' : 'badge-warning'}">
                          ${escapeHtml(rep.type)}
                        </span>
                        ${statusBadge}
                      </div>
                      <h4 style="margin: 6px 0 2px; font-size: var(--font-size-base); color: var(--color-on-surface);">
                        ${isReceived ? `Bị báo cáo bởi: ${escapeHtml(counterpartName)}` : `Báo cáo: ${escapeHtml(counterpartName)}`}
                      </h4>
                      <div style="font-size: 11px; color: var(--color-on-surface-variant);">
                        Thời điểm: ${formatDateTime(rep.createdAt)}
                      </div>
                    </div>
                  </div>

                  <div class="card-body">
                    <p style="margin: 0; font-size: var(--font-size-sm); color: var(--color-on-surface); line-height: 1.4;">
                      "${escapeHtml(rep.reason)}"
                    </p>

                    ${isReceived && rep.reliabilityPenalty ? `
                      <div style="margin-top: 8px; font-size: var(--font-size-xs); color: var(--color-error); font-weight: 500;">
                        Mức phạt: -${rep.reliabilityPenalty} điểm uy tín
                      </div>
                    ` : ''}

                    ${canDispute ? `
                      <div style="margin-top: 10px; display: flex; justify-content: space-between; align-items: center;">
                        <span style="font-size: 11px; color: var(--color-warning); font-weight: 500;">
                          ⏱️ Còn trong hạn khiếu nại (24h)
                        </span>
                        <button class="btn btn-sm btn-primary" onclick="event.stopPropagation(); window.AppRouter.openSheet('submit-dispute-sheet', { reportId: '${rep.id}' })">
                          Tranh chấp ngay
                        </button>
                      </div>
                    ` : ''}
                  </div>
                </div>
              `;
            }).join('') : `
              <div class="empty-state">
                <div class="empty-state-icon">
                  <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path>
                    <polyline points="14 2 14 8 20 8"></polyline>
                  </svg>
                </div>
                <h3 class="empty-state-title">Chưa có báo cáo nào</h3>
                <p class="empty-state-desc">
                  ${activeTab === 'RECEIVED' ? 'Đội của bạn chưa bị báo cáo vi phạm nào.' : 'Đội của bạn chưa gửi báo cáo nào.'}
                </p>
              </div>
            `}
          </div>

          <button class="btn btn-secondary btn-block" style="margin-top: 8px;" onclick="window.AppRouter.goBack()">
            Quay lại
          </button>
        </div>
      `;

      // Tab handlers
      container.querySelector('#tab-received').addEventListener('click', () => {
        activeTab = 'RECEIVED';
        renderContent();
      });

      container.querySelector('#tab-sent').addEventListener('click', () => {
        activeTab = 'SENT';
        renderContent();
      });
    }

    renderContent();
  });

  /* =========================================================================
     REP-08 / REP-10: REPORT DETAIL & OUTCOMES
     Route: 'report-detail' (params: { reportId })
     ========================================================================= */
  router.registerView('report-detail', (container, params) => {
    router.setHeaderTitle('Chi tiết báo cáo vi phạm');

    const state = store.getState();
    const reportId = params.reportId || 'rep-noshow-active';
    const report = state.reports?.find(r => r.id === reportId);

    if (!report) {
      container.innerHTML = `
        <div class="empty-state">
          <h3 class="empty-state-title">Không tìm thấy báo cáo</h3>
          <p class="empty-state-desc">Mã báo cáo <code>${escapeHtml(reportId)}</code> không tồn tại.</p>
          <button class="btn btn-secondary" onclick="window.AppRouter.navigate('reports-list')">Về danh sách báo cáo</button>
        </div>
      `;
      return;
    }

    const reporterTeam = state.teams.find(t => t.id === report.reporterId);
    const reportedTeam = state.teams.find(t => t.id === report.reportedId);
    const match = state.matches.find(m => m.id === report.matchId);

    const now = new Date(state.meta.mockClock).getTime();
    const isNoShow = report.type === 'NOSHOW';
    const hasDisputeDeadline = Boolean(report.canDisputeUntil);
    const deadlineTime = hasDisputeDeadline ? new Date(report.canDisputeUntil).getTime() : 0;
    const isExpired = hasDisputeDeadline && now > deadlineTime;

    // AC-P08: Dispute ONLY allowed for NOSHOW within 24h
    const canDispute = Boolean(
      isNoShow &&
      report.status === 'AUTO_ACCEPTED' &&
      !isExpired
    );

    container.innerHTML = `
      <div style="padding: 16px; display: flex; flex-direction: column; gap: 14px;">
        <!-- Status Banner -->
        <div class="card">
          <div class="card-header">
            <div>
              <span class="badge ${isNoShow ? 'badge-error' : 'badge-warning'}">
                ${getReportTypeLabel(report.type)}
              </span>
              <h3 class="card-title" style="margin-top: 6px;">Báo cáo #${report.id.slice(-6)}</h3>
              <div class="card-subtitle">Thời gian gửi: ${formatDateTime(report.createdAt)}</div>
            </div>
            <div>
              ${report.status === 'AUTO_ACCEPTED' ? '<span class="badge badge-warning">Đã trừ điểm</span>' : ''}
              ${report.status === 'DISPUTED' ? '<span class="badge badge-amber">Đang chờ Admin</span>' : ''}
              ${report.status === 'CONFIRMED' || report.status === 'RESOLVED' ? '<span class="badge badge-error">Xác nhận vi phạm</span>' : ''}
              ${report.status === 'REJECTED' ? '<span class="badge badge-emerald">Bác báo cáo (Đã hoàn điểm)</span>' : ''}
            </div>
          </div>

          <div class="divider" style="margin: 10px 0;"></div>

          <div class="card-body" style="font-size: var(--font-size-sm); line-height: 1.6;">
            <div>Bên gửi báo cáo: <strong>${escapeHtml(reporterTeam?.name || report.reporterId)}</strong></div>
            <div>Bên bị báo cáo: <strong>${escapeHtml(reportedTeam?.name || report.reportedId)}</strong></div>
            ${match ? `<div>Trận đấu liên quan: ${formatDate(match.matchDate)} (${match.startTime} - ${match.endTime})</div>` : ''}
          </div>
        </div>

        <!-- Reason Card -->
        <div class="card">
          <div class="card-header">
            <h4 class="card-title" style="font-size: var(--font-size-md);">Nội dung báo cáo</h4>
          </div>
          <div class="card-body">
            <div style="background: var(--color-surface-container-low); padding: 12px; border-radius: var(--radius-base); font-size: var(--font-size-sm); color: var(--color-on-surface); line-height: 1.5;">
              "${escapeHtml(report.reason)}"
            </div>

            <div style="margin-top: 10px; font-size: var(--font-size-xs); color: var(--color-error);">
              Mức ảnh hưởng: -${report.reliabilityPenalty || getReportPenaltyPoints(report.type)} điểm uy tín
            </div>
          </div>
        </div>

        <!-- Dispute Info Section (AC-P08 & REP-10) -->
        <div class="card">
          <div class="card-header">
            <h4 class="card-title" style="font-size: var(--font-size-md);">Trạng thái khiếu nại tranh chấp</h4>
          </div>
          <div class="card-body" style="font-size: var(--font-size-sm); line-height: 1.5;">
            ${!isNoShow ? `
              <div class="notice notice-warning" style="margin: 0;">
                <strong>Không hỗ trợ khiếu nại:</strong>
                <p>Theo quy định AC-P08, chỉ có báo cáo bùng kèo (NOSHOW) mới có thể khiếu nại tranh chấp. Báo cáo loại <code>${report.type}</code> được hệ thống ghi nhận trực tiếp.</p>
              </div>
            ` : (isExpired || report.status === 'CONFIRMED' || (report.status === 'RESOLVED' && !report.adminDecision)) ? `
              <div class="notice notice-warning" style="margin: 0;">
                <strong>Đã hết thời hạn khiếu nại:</strong>
                <p>Thời hạn khiếu nại 24 giờ cho báo cáo bùng kèo này đã hết hạn (Hạn chót: ${formatDateTime(report.canDisputeUntil)}). Báo cáo đã tự động chuyển sang xác nhận.</p>
              </div>
            ` : report.status === 'DISPUTED' ? `
              <div class="notice notice-info" style="margin: 0;">
                <strong>Đang chờ Admin xử lý:</strong>
                <p>Đội bị báo cáo đã gửi bằng chứng khiếu nại vào lúc ${formatDateTime(report.dispute?.createdAt)}. Quản trị viên đang xem xét giải trình từ hai phía.</p>
                <div style="margin-top: 8px; background: rgba(255,255,255,0.7); padding: 8px; border-radius: 4px;">
                  <em>"${escapeHtml(report.dispute?.evidenceText || 'Bằng chứng đã gửi')}"</em>
                </div>
              </div>
            ` : report.adminDecision ? `
              <!-- REP-10 Final Admin Outcome -->
              <div class="notice ${report.adminDecision.outcome === 'REJECTED' ? 'notice-success' : 'notice-danger'}" style="margin: 0;">
                <strong>Quyết định của Admin: ${report.adminDecision.outcome === 'REJECTED' ? 'Bác bỏ báo cáo' : 'Xác nhận báo cáo đúng'}</strong>
                <p style="margin-top: 4px;">
                  Ghi chú quyết định: "${escapeHtml(report.adminDecision.note)}"
                </p>
                <div style="margin-top: 6px; font-size: 11px;">
                  ${report.adminDecision.outcome === 'REJECTED' ? '🎉 Đã hoàn lại điểm uy tín bị phạt cho đội bị báo cáo.' : '⚠️ Giữ nguyên mức phạt điểm uy tín.'}
                  • Xử lý lúc: ${formatDateTime(report.adminDecision.resolvedAt)}
                </div>
              </div>
            ` : `
              <div style="color: var(--color-on-surface-variant); font-size: var(--font-size-xs);">
                Thời hạn khiếu nại: Còn hiệu lực đến <strong>${formatDateTime(report.canDisputeUntil)}</strong>.
              </div>
            `}
          </div>
        </div>

        <!-- Action Button -->
        <div style="display: flex; flex-direction: column; gap: 8px; margin-top: 6px;">
          ${canDispute ? `
            <button class="btn btn-primary btn-block" onclick="window.AppRouter.openSheet('submit-dispute-sheet', { reportId: '${report.id}' })">
              Khiếu nại báo cáo bùng kèo (Trong 24h)
            </button>
          ` : ''}
          <button class="btn btn-secondary btn-block" onclick="window.AppRouter.goBack()">
            Quay lại
          </button>
        </div>
      </div>
    `;
  });

  /* =========================================================================
     REP-09: DISPUTE NOSHOW REPORT SHEET
     Sheet: 'submit-dispute-sheet' (params: { reportId })
     Complies with AC-P08: NOSHOW only, within 24h, text evidence 1-1000 chars.
     ========================================================================= */
  router.registerSheet('submit-dispute-sheet', (body, footer, params, titleElem) => {
    titleElem.textContent = 'Khiếu nại tranh chấp';

    const state = store.getState();
    const reportId = params.reportId;
    const report = state.reports?.find(r => r.id === reportId);

    if (!report) {
      body.innerHTML = `<div class="notice notice-danger">Không tìm thấy báo cáo để khiếu nại.</div>`;
      footer.innerHTML = `<button class="btn btn-secondary btn-block" onclick="window.AppRouter.closeSheet()">Đóng</button>`;
      return;
    }

    const now = new Date(state.meta.mockClock).getTime();
    const isNoShow = report.type === 'NOSHOW';
    const isExpired = report.canDisputeUntil ? now > new Date(report.canDisputeUntil).getTime() : true;

    // AC-P08 Strict Check:
    if (!isNoShow) {
      body.innerHTML = `
        <div class="notice notice-danger">
          <strong>Hành động bị chặn (AC-P08):</strong>
          <p>Chỉ cho phép khiếu nại đối với báo cáo loại Bùng kèo (NOSHOW). Báo cáo loại <code>${report.type}</code> không thể khiếu nại.</p>
        </div>
      `;
      footer.innerHTML = `<button class="btn btn-secondary btn-block" onclick="window.AppRouter.closeSheet()">Đóng</button>`;
      return;
    }

    if (isExpired) {
      body.innerHTML = `
        <div class="notice notice-danger">
          <strong>Thời hạn khiếu nại đã hết (AC-P08):</strong>
          <p>Quy định chỉ cho phép gửi giải trình trong vòng 24 giờ kể từ thời điểm nhận báo cáo. Báo cáo này đã quá hạn.</p>
        </div>
      `;
      footer.innerHTML = `<button class="btn btn-secondary btn-block" onclick="window.AppRouter.closeSheet()">Đóng</button>`;
      return;
    }

    body.innerHTML = `
      <div style="display: flex; flex-direction: column; gap: 14px;">
        <div class="notice notice-info" style="margin: 0; font-size: var(--font-size-xs);">
          <strong>Quy định khiếu nại:</strong>
          Giải trình bằng văn bản gửi tới ban quản trị Admin để xác minh lại việc có mặt tại sân. Hạn chót: <strong>${formatDateTime(report.canDisputeUntil)}</strong>.
        </div>

        <div class="form-group">
          <label class="form-label" for="dispute-evidence-text">
            Bằng chứng giải trình (Văn bản 1 - 1000 ký tự)
          </label>
          <textarea id="dispute-evidence-text" class="form-textarea" rows="4" maxlength="1000" placeholder="Mô tả cụ thể thời gian đội bạn có mặt tại sân, các tin nhắn liên lạc với đội trưởng bên kia, lý do trận đấu không diễn ra..."></textarea>
          <div style="text-align: right; font-size: 11px; color: var(--color-on-surface-variant); margin-top: 4px;">
            <span id="evidence-char-count">0</span>/1000
          </div>
          <span class="field-error-msg" id="dispute-error-msg" style="display: none;"></span>
        </div>
      </div>
    `;

    footer.innerHTML = `
      <div style="display: flex; gap: 8px;">
        <button type="button" class="btn btn-secondary" style="flex: 1;" onclick="window.AppRouter.closeSheet()">
          Hủy bỏ
        </button>
        <button type="button" class="btn btn-primary" style="flex: 1;" id="btn-submit-dispute-action">
          Gửi khiếu nại
        </button>
      </div>
    `;

    const evidenceInput = body.querySelector('#dispute-evidence-text');
    const charCount = body.querySelector('#evidence-char-count');
    const errorMsg = body.querySelector('#dispute-error-msg');
    const submitBtn = footer.querySelector('#btn-submit-dispute-action');

    evidenceInput.addEventListener('input', () => {
      charCount.textContent = evidenceInput.value.length;
    });

    submitBtn.addEventListener('click', () => {
      errorMsg.style.display = 'none';
      const text = evidenceInput.value.trim();

      if (!text || text.length < 1) {
        errorMsg.textContent = 'Vui lòng nhập bằng chứng giải trình cụ thể.';
        errorMsg.style.display = 'block';
        return;
      }

      submitBtn.disabled = true;
      submitBtn.textContent = 'Đang gửi...';

      try {
        store.submitDispute(report.id, text);
        router.closeSheet();
        router.showToast('Gửi khiếu nại thành công, đang chờ Admin xem xét!', 'success');
        router.navigate('report-detail', { reportId: report.id });
      } catch (err) {
        submitBtn.disabled = false;
        submitBtn.textContent = 'Gửi khiếu nại';
        errorMsg.textContent = err.message || 'Lỗi gửi khiếu nại';
        errorMsg.style.display = 'block';
      }
    });
  });
}
