/**
 * ArenaFlow Auth & Profile Module Views
 * Implements: APP-01, APP-02, AUTH-01..06, PROF-01..06
 * Complies with AC-P01, AC-P02, J-01..J-03, J-06
 */

export function initAuthViews(router, store) {

  // Helper: SVG Icons
  const Icons = {
    back: `<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round"><polyline points="15 18 9 12 15 6"></polyline></svg>`,
    warning: `<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="m21.73 18-8-14a2 2 0 0 0-3.48 0l-8 14A2 2 0 0 0 4 21h16a2 2 0 0 0 1.73-3Z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg>`,
    errorCircle: `<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>`,
    checkCircle: `<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/></svg>`,
    soccer: `<svg width="24" height="24" viewBox="0 0 24 24" fill="currentColor"><path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 18c-4.41 0-8-3.59-8-8 0-.46.05-.91.13-1.35l3.85 2.22.4 4.45c1.13.44 2.35.68 3.62.68 1.27 0 2.49-.24 3.62-.68l.4-4.45 3.85-2.22c.08.44.13.89.13 1.35 0 4.41-3.59 8-8 8zm-5.02-6.52l-2.61-1.51c.36-1.57 1.18-2.95 2.31-4.01l2.4 1.8-2.1 3.72zm5.02-3.12l-2.35-1.71 1.45-2.51c.29-.04.59-.06.9-.06s.61.02.9.06l1.45 2.51-2.35 1.71zm5.02 3.12l-2.1-3.72 2.4-1.8c1.13 1.06 1.95 2.44 2.31 4.01l-2.61 1.51z"/></svg>`,
    edit: `<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>`,
    trash: `<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/><line x1="10" y1="11" x2="10" y2="17"/><line x1="14" y1="11" x2="14" y2="17"/></svg>`,
    shield: `<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/></svg>`,
    user: `<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>`,
    phone: `<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z"/></svg>`,
    logout: `<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><polyline points="16 17 21 12 16 7"/><line x1="21" y1="12" x2="9" y2="12"/></svg>`,
    chevronRight: `<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="9 18 15 12 9 6"/></svg>`
  };

  // Preset Avatar URLs for quick profile creation
  const AVATAR_PRESETS = [
    'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150',
    'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150',
    'https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=150',
    'https://images.unsplash.com/photo-1492562080023-ab3db95bfbce?w=150',
    'https://images.unsplash.com/photo-1522075469751-3a6694fb2f61?w=150'
  ];

  /* =========================================================================
     APP-01: Launch / Session Check / Retry
     ========================================================================= */
  router.registerView('launch', (container, params) => {
    router.setHeaderTitle('');
    const state = store.getState();
    const simErr = store.checkSimulatedError();

    container.innerHTML = `
      <div style="min-height: 80vh; display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 24px; text-align: center;">
        <div style="width: 80px; height: 80px; border-radius: 20px; background: var(--color-primary); color: #fff; display: flex; align-items: center; justify-content: center; margin-bottom: 20px; box-shadow: 0 10px 25px rgba(61,105,67,0.3);">
          <div style="transform: scale(1.6);">${Icons.soccer}</div>
        </div>
        <h1 style="font-size: 26px; font-weight: 800; color: var(--color-on-background); margin: 0 0 8px;">ArenaFlow</h1>
        <p style="font-size: 14px; color: var(--color-on-surface-variant); max-width: 280px; margin: 0 0 32px;">
          Nền tảng ghép đội & tạo kèo bóng đá phong trào
        </p>

        <div id="launch-status-box" style="width: 100%; max-width: 320px;">
          ${simErr === 'network' ? `
            <div class="notice notice-danger" style="margin-bottom: 16px; text-align: left;">
              <div style="display: flex; gap: 8px; align-items: flex-start;">
                <span style="color: var(--color-error);">${Icons.errorCircle}</span>
                <div>
                  <strong>Lỗi kết nối máy chủ</strong>
                  <p style="margin: 4px 0 0; font-size: 13px;">Không thể kiểm tra phiên đăng nhập. Vui lòng kiểm tra lại đường truyền.</p>
                </div>
              </div>
            </div>
            <button class="btn btn-primary btn-block" id="btn-launch-retry">
              Thử lại kết nối
            </button>
          ` : `
            <div style="display: flex; flex-direction: column; align-items: center; gap: 12px;">
              <div style="width: 32px; height: 32px; border: 3px solid var(--color-primary-container); border-top-color: var(--color-primary); border-radius: 50%; animation: spin 0.8s linear infinite;"></div>
              <span style="font-size: 13px; color: var(--color-on-surface-variant);">Đang kiểm tra phiên làm việc...</span>
            </div>
          `}
        </div>
      </div>
    `;

    const retryBtn = container.querySelector('#btn-launch-retry');
    if (retryBtn) {
      retryBtn.addEventListener('click', () => {
        store.consumeSimulatedError();
        router.navigate('launch', {}, true);
      });
      return;
    }

    // Normal session check transition
    setTimeout(() => {
      const onboarded = localStorage.getItem('arenaflow_onboarded_done');
      const currentUser = store.getCurrentUser();

      if (!onboarded) {
        router.navigate('onboarding', {}, true);
      } else if (!currentUser || currentUser.isDeleted) {
        router.navigate('login', {}, true);
      } else if (currentUser.isNewUser) {
        // AC-P01: new user routed to profile completion
        router.navigate('profile-edit', { isNewUser: 'true' }, true);
      } else {
        router.navigate(params?.returnUrl || 'matches', {}, true);
      }
    }, 700);
  });

  /* =========================================================================
     APP-02: Onboarding (Slides, Skip, Start)
     ========================================================================= */
  router.registerView('onboarding', (container) => {
    router.setHeaderTitle('');
    let currentSlide = 0;

    const slides = [
      {
        badge: 'TẠO KÈO DỄ DÀNG',
        title: 'Tìm đối thủ ngang trình độ nhanh chóng',
        desc: 'Hệ thống gợi ý đối thủ dựa trên trình độ, khu vực và lịch sinh hoạt của đội bóng.',
        icon: Icons.soccer,
        color: '#14532D'
      },
      {
        badge: 'ĐÁ LẺ & TUYỂN NGƯỜI',
        title: 'Không lo thiếu quân trước giờ bóng lăn',
        desc: 'Đăng tin tuyển viện binh vị trí cụ thể hoặc ứng tuyển đá lẻ với thông tin sân minh bạch.',
        icon: Icons.user,
        color: '#1E3A8A'
      },
      {
        badge: 'UY TÍN MINH BẠCH',
        title: 'Hạn chế tối đa bùng kèo, chơi xấu',
        desc: 'Đánh giá ẩn sau trận đấu, tính điểm hoàn thành trận và giải quyết tranh chấp công bằng.',
        icon: Icons.shield,
        color: '#3d6943'
      }
    ];

    function renderSlide() {
      const s = slides[currentSlide];
      container.innerHTML = `
        <div style="min-height: 84vh; display: flex; flex-direction: column; justify-content: space-between; padding: 24px 20px;">
          <!-- Top Skip Bar -->
          <div style="display: flex; justify-content: flex-end;">
            <button class="btn btn-ghost" id="btn-onboarding-skip" style="font-size: 14px; font-weight: 600; color: var(--color-on-surface-variant);">
              Bỏ qua
            </button>
          </div>

          <!-- Slide Content -->
          <div style="display: flex; flex-direction: column; align-items: center; text-align: center; padding: 20px 0;">
            <div style="width: 90px; height: 90px; border-radius: 28px; background: ${s.color}; color: #fff; display: flex; align-items: center; justify-content: center; margin-bottom: 28px; box-shadow: 0 12px 28px rgba(0,0,0,0.15);">
              <div style="transform: scale(2);">${s.icon}</div>
            </div>

            <span class="badge badge-green" style="margin-bottom: 12px; font-size: 11px; letter-spacing: 0.5px;">${s.badge}</span>
            <h2 style="font-size: 22px; font-weight: 700; color: var(--color-on-background); margin: 0 0 12px; line-height: 1.3;">
              ${s.title}
            </h2>
            <p style="font-size: 14px; color: var(--color-on-surface-variant); line-height: 1.5; max-width: 310px; margin: 0;">
              ${s.desc}
            </p>
          </div>

          <!-- Bottom Controls & Dots -->
          <div>
            <div style="display: flex; justify-content: center; gap: 8px; margin-bottom: 24px;">
              ${slides.map((_, i) => `
                <div style="width: ${i === currentSlide ? '24px' : '8px'}; height: 8px; border-radius: 4px; background: ${i === currentSlide ? 'var(--color-primary)' : 'var(--color-outline-variant)'}; transition: all 0.25s ease;"></div>
              `).join('')}
            </div>

            <button class="btn btn-primary btn-block" id="btn-onboarding-next">
              ${currentSlide === slides.length - 1 ? 'Bắt đầu ngay' : 'Tiếp tục'}
            </button>
          </div>
        </div>
      `;

      container.querySelector('#btn-onboarding-skip').addEventListener('click', () => {
        localStorage.setItem('arenaflow_onboarded_done', 'true');
        router.navigate('login');
      });

      container.querySelector('#btn-onboarding-next').addEventListener('click', () => {
        if (currentSlide < slides.length - 1) {
          currentSlide++;
          renderSlide();
        } else {
          localStorage.setItem('arenaflow_onboarded_done', 'true');
          router.navigate('login');
        }
      });
    }

    renderSlide();
  });

  /* =========================================================================
     AUTH-01: Login Method Choice (Stitch S1)
     ========================================================================= */
  router.registerView('login', (container, params) => {
    router.setHeaderTitle('');
    const returnUrl = params?.returnUrl || '';

    container.innerHTML = `
      <div style="display: flex; flex-direction: column; min-height: 100%;">
        <!-- Pitch Themed Top Banner -->
        <header style="background: #14532D; color: #fff; padding: 36px 20px 28px; position: relative; overflow: hidden;">
          <svg style="position: absolute; inset: 0; width: 100%; height: 100%; opacity: 0.12; pointer-events: none;" stroke="#fff" fill="none">
            <line stroke-dasharray="4 4" stroke-width="1.5" x1="0" x2="390" y1="20" y2="20"></line>
            <circle cx="195" cy="110" r="50" stroke-width="1.5"></circle>
            <circle cx="195" cy="110" fill="#fff" r="3"></circle>
            <line stroke-width="1.5" x1="195" x2="195" y1="0" y2="220"></line>
          </svg>
          <div style="position: relative; z-index: 1;">
            <div style="display: flex; align-items: center; gap: 10px; margin-bottom: 20px;">
              <div style="width: 36px; height: 36px; border-radius: 8px; background: rgba(255,255,255,0.2); display: flex; align-items: center; justify-content: center;">
                ${Icons.soccer}
              </div>
              <span style="font-size: 22px; font-weight: 800; letter-spacing: -0.5px;">ArenaFlow</span>
            </div>
            <h1 style="font-size: 24px; font-weight: 800; line-height: 1.25; margin: 0 0 10px;">
              Tạo kèo, ghép đội, đá bóng dễ dàng.
            </h1>
            <p style="font-size: 13px; color: rgba(255,255,255,0.85); line-height: 1.5; margin: 0;">
              Tìm đối thủ ngang trình độ, tuyển thêm người chơi và xây dựng uy tín cho đội của bạn.
            </p>
          </div>
        </header>

        <!-- Lower Body Section -->
        <div style="padding: 24px 20px; flex: 1; display: flex; flex-direction: column; justify-content: space-between;">
          <div>
            <h2 style="font-size: 18px; font-weight: 700; color: var(--color-on-surface); margin: 0 0 4px;">
              Đăng nhập hoặc đăng ký
            </h2>
            <p style="font-size: 13px; color: var(--color-on-surface-variant); margin: 0 0 20px;">
              Tài khoản mới sẽ được tạo tự động ở lần đăng nhập đầu tiên
            </p>

            ${returnUrl ? `
              <div class="notice notice-info" style="margin-bottom: 16px;">
                Vui lòng đăng nhập để tiếp tục truy cập trang đích.
              </div>
            ` : ''}

            <!-- Method Buttons -->
            <div style="display: flex; flex-direction: column; gap: 12px;">
              <!-- Google OAuth -->
              <button class="btn btn-secondary btn-block" id="btn-login-google" style="justify-content: flex-start; padding: 12px 16px; border: 1px solid var(--color-outline-variant); background: #fff;">
                <svg width="20" height="20" viewBox="0 0 24 24">
                  <path fill="#4285F4" d="M23.745 12.27c0-.7-.06-1.4-.19-2.07H12v4.51h6.6c-.29 1.52-1.14 2.82-2.4 3.68v3.05h3.88c2.27-2.09 3.66-5.17 3.66-9.17z"/>
                  <path fill="#34A853" d="M12 24c3.24 0 5.95-1.08 7.93-2.91l-3.88-3.05c-1.08.72-2.45 1.16-4.05 1.16-3.12 0-5.77-2.1-6.72-4.93H1.25v3.15C3.26 21.36 7.33 24 12 24z"/>
                  <path fill="#FBBC05" d="M5.28 14.27c-.25-.72-.38-1.49-.38-2.27s.13-1.55.38-2.27V6.58H1.25C.45 8.17 0 9.98 0 12s.45 3.83 1.25 5.42l4.03-3.15z"/>
                  <path fill="#EA4335" d="M12 4.75c1.77 0 3.35.61 4.6 1.8l3.42-3.42C17.95 1.19 15.24 0 12 0 7.33 0 3.26 2.64 1.25 6.58l4.03 3.15c.95-2.83 3.6-4.98 6.72-4.98z"/>
                </svg>
                <span style="flex: 1; text-align: center; font-weight: 600; color: #1e293b;">Tiếp tục với Google</span>
              </button>

              <!-- Facebook OAuth -->
              <button class="btn btn-secondary btn-block" id="btn-login-facebook" style="justify-content: flex-start; padding: 12px 16px; border: 1px solid var(--color-outline-variant); background: #fff;">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="#1877F2">
                  <path d="M24 12.073c0-6.627-5.373-12-12-12s-12 5.373-12 12c0 5.99 4.388 10.954 10.125 11.854v-8.385H7.078v-3.47h3.047V9.43c0-3.007 1.792-4.669 4.533-4.669 1.312 0 2.686.235 2.686.235v2.953H15.83c-1.491 0-1.956.925-1.956 1.874v2.25h3.328l-.532 3.47h-2.796v8.385C19.612 23.027 24 18.062 24 12.073z"/>
                </svg>
                <span style="flex: 1; text-align: center; font-weight: 600; color: #1e293b;">Tiếp tục với Facebook</span>
              </button>

              <div style="display: flex; align-items: center; margin: 8px 0;">
                <div style="flex: 1; height: 1px; background: var(--color-outline-variant);"></div>
                <span style="padding: 0 12px; font-size: 12px; color: var(--color-on-surface-variant); font-weight: 500;">HOẶC</span>
                <div style="flex: 1; height: 1px; background: var(--color-outline-variant);"></div>
              </div>

              <!-- Phone Login -->
              <button class="btn btn-primary btn-block" id="btn-login-phone" style="justify-content: center; padding: 12px 16px;">
                ${Icons.phone}
                <span>Tiếp tục với Số điện thoại</span>
              </button>
            </div>
          </div>

          <p style="font-size: 11px; color: var(--color-on-surface-variant); text-align: center; margin-top: 32px; line-height: 1.4;">
            Bằng việc tiếp tục, bạn đồng ý với Điều khoản sử dụng và Chính sách bảo mật của ArenaFlow.
          </p>
        </div>
      </div>
    `;

    // Handler for OAuth Mock
    const handleOAuth = (provider) => {
      // Check simulated error
      const simErr = store.checkSimulatedError();
      if (simErr) {
        store.consumeSimulatedError();
        router.showToast(`Lỗi xác thực ${provider}: Mô phỏng lỗi hệ thống (${simErr})`, 'error');
        return;
      }

      // Simulate OAuth success: logs in as existing player or new user
      const currentUser = store.getCurrentUser();
      router.showToast(`Đăng nhập qua ${provider} thành công!`, 'success');

      if (currentUser?.isNewUser) {
        router.navigate('profile-edit', { isNewUser: 'true' });
      } else {
        router.navigate(returnUrl || 'matches');
      }
    };

    container.querySelector('#btn-login-google').addEventListener('click', () => handleOAuth('Google'));
    container.querySelector('#btn-login-facebook').addEventListener('click', () => handleOAuth('Facebook'));
    container.querySelector('#btn-login-phone').addEventListener('click', () => {
      router.navigate('phone-input', { returnUrl });
    });
  });

  /* =========================================================================
     AUTH-02: Phone Input (Stitch S2a)
     ========================================================================= */
  router.registerView('phone-input', (container, params) => {
    router.setHeaderTitle('Nhập số điện thoại');
    const returnUrl = params?.returnUrl || '';
    const initialPhone = params?.phone || '';

    container.innerHTML = `
      <div style="padding: 20px; display: flex; flex-direction: column; min-height: 90vh; justify-content: space-between;">
        <div>
          <button class="btn btn-ghost" id="btn-phone-back" style="padding-left: 0; margin-bottom: 12px; gap: 4px;">
            ${Icons.back} <span style="font-size: 14px;">Quay lại</span>
          </button>

          <h1 style="font-size: 24px; font-weight: 800; color: var(--color-on-surface); margin: 0 0 6px;">
            Số điện thoại của bạn
          </h1>
          <p style="font-size: 13.5px; color: var(--color-on-surface-variant); margin: 0 0 24px; line-height: 1.45;">
            Mã OTP gồm 6 chữ số sẽ được gửi qua tin nhắn SMS để xác thực tài khoản.
          </p>

          <div class="form-group" style="margin-bottom: 16px;">
            <label class="form-label" for="input-phone">Số điện thoại</label>
            <div style="display: flex; gap: 8px;">
              <div style="display: flex; align-items: center; padding: 0 12px; background: var(--color-surface-container); border: 1px solid var(--color-outline-variant); border-radius: var(--radius-base); font-size: 14px; font-weight: 600; color: var(--color-on-surface);">
                +84 (VN)
              </div>
              <input
                type="tel"
                id="input-phone"
                class="form-input"
                placeholder="0912 345 678"
                value="${initialPhone}"
                maxlength="11"
                autocomplete="tel"
                style="font-size: 16px; font-weight: 600; letter-spacing: 0.5px;"
              />
            </div>
            <div id="phone-error" class="field-error-msg" style="display: none;"></div>
          </div>

          <!-- Quick Test Numbers Hint Box -->
          <div style="background: var(--color-surface-container-low); border: 1px dashed var(--color-outline); border-radius: var(--radius-base); padding: 12px; font-size: 12px; color: var(--color-on-surface-variant);">
            <strong style="color: var(--color-primary); display: block; margin-bottom: 4px;">Số thử nghiệm nhanh:</strong>
            <ul style="margin: 0; padding-left: 16px; line-height: 1.6;">
              <li><code style="cursor: pointer; text-decoration: underline;" onclick="document.getElementById('input-phone').value='0901000001'">0901000001</code>: User mới (Chưa xong hồ sơ)</li>
              <li><code style="cursor: pointer; text-decoration: underline;" onclick="document.getElementById('input-phone').value='0903000003'">0903000003</code>: Captain Alpha (Bình Minh FC)</li>
              <li><code style="cursor: pointer; text-decoration: underline;" onclick="document.getElementById('input-phone').value='0900000099'">0900000099</code>: Tài khoản đã xóa (AUTH-05)</li>
            </ul>
          </div>
        </div>

        <div>
          <button class="btn btn-primary btn-block" id="btn-phone-submit">
            Gửi mã xác thực
          </button>
        </div>
      </div>
    `;

    const inputPhone = container.querySelector('#input-phone');
    const phoneError = container.querySelector('#phone-error');
    const backBtn = container.querySelector('#btn-phone-back');
    const submitBtn = container.querySelector('#btn-phone-submit');

    backBtn.addEventListener('click', () => router.navigate('login'));

    function validatePhone(val) {
      const clean = val.replace(/\s+/g, '');
      if (!clean) {
        return 'Vui lòng nhập số điện thoại';
      }
      if (!/^0\d{9}$/.test(clean)) {
        return 'Số điện thoại phải gồm 10 chữ số và bắt đầu bằng số 0';
      }
      return null;
    }

    inputPhone.addEventListener('input', () => {
      phoneError.style.display = 'none';
      inputPhone.classList.remove('has-error');
    });

    submitBtn.addEventListener('click', () => {
      const val = inputPhone.value.trim().replace(/\s+/g, '');
      const err = validatePhone(val);

      if (err) {
        phoneError.textContent = err;
        phoneError.style.display = 'block';
        inputPhone.classList.add('has-error');
        inputPhone.focus();
        return;
      }

      // Check simulated error
      const simErr = store.checkSimulatedError();
      if (simErr) {
        store.consumeSimulatedError();
        router.showToast(`Mô phỏng lỗi gửi SMS: ${simErr}`, 'error');
        return;
      }

      // AUTH-05 check: If phone belongs to deleted user
      if (val === '0900000099') {
        router.navigate('auth-deleted-account', { phone: val });
        return;
      }

      // Advance to OTP view
      router.navigate('otp', { phone: val, returnUrl });
    });
  });

  /* =========================================================================
     AUTH-03: OTP Flow (Stitch S2b)
     States: default, loading, error, expired, success
     ========================================================================= */
  router.registerView('otp', (container, params) => {
    router.setHeaderTitle('Xác thực OTP');
    const phone = params?.phone || '0901000001';
    const returnUrl = params?.returnUrl || '';

    let countdown = 60;
    let timer = null;

    container.innerHTML = `
      <div style="padding: 20px; display: flex; flex-direction: column; min-height: 90vh; justify-content: space-between;">
        <div>
          <button class="btn btn-ghost" id="btn-otp-back" style="padding-left: 0; margin-bottom: 12px; gap: 4px;">
            ${Icons.back} <span style="font-size: 14px;">Quay lại</span>
          </button>

          <h1 style="font-size: 24px; font-weight: 800; color: var(--color-on-surface); margin: 0 0 6px;">
            Nhập mã xác thực
          </h1>
          <p style="font-size: 13.5px; color: var(--color-on-surface-variant); margin: 0 0 24px; line-height: 1.45;">
            Mã gồm 6 chữ số đã được gửi tới số <strong style="color: var(--color-on-surface);">${phone}</strong>
            <button id="btn-change-phone" style="background:none; border:none; padding:0; color: var(--color-primary); font-weight: 600; margin-left: 6px; cursor: pointer; text-decoration: underline;">
              Đổi số
            </button>
          </p>

          <!-- 6-digit OTP Box row -->
          <div style="margin-bottom: 16px;">
            <div style="display: flex; gap: 8px; justify-content: space-between;" id="otp-input-row">
              ${[0,1,2,3,4,5].map(i => `
                <input
                  type="text"
                  maxlength="1"
                  inputmode="numeric"
                  class="otp-digit"
                  data-idx="${i}"
                  style="width: 48px; height: 56px; text-align: center; font-size: 22px; font-weight: 700; border: 2px solid var(--color-outline-variant); border-radius: var(--radius-base); background: #fff; outline: none; transition: all 0.2s ease;"
                />
              `).join('')}
            </div>
            <div id="otp-error-msg" class="field-error-msg" style="display: none; margin-top: 8px;"></div>
          </div>

          <!-- Resend Section -->
          <div style="font-size: 13px; color: var(--color-on-surface-variant); margin-bottom: 24px;">
            Chưa nhận được mã? 
            <button id="btn-resend-otp" disabled style="background: none; border: none; padding: 0; color: var(--color-primary); font-weight: 600; cursor: pointer; text-decoration: underline;">
              Gửi lại mã (<span id="otp-countdown">60</span>s)
            </button>
          </div>

          <!-- Test Codes Guide -->
          <div style="background: var(--color-surface-container-low); border: 1px dashed var(--color-outline); border-radius: var(--radius-base); padding: 12px; font-size: 12px; color: var(--color-on-surface-variant);">
            <strong style="color: var(--color-primary); display: block; margin-bottom: 4px;">Mã OTP thử nghiệm:</strong>
            <div style="display: flex; gap: 8px; flex-wrap: wrap;">
              <button class="btn btn-secondary btn-sm" id="btn-quick-123456" style="padding: 4px 8px; font-size: 12px;">123456 (Thành công)</button>
              <button class="btn btn-secondary btn-sm" id="btn-quick-999999" style="padding: 4px 8px; font-size: 12px;">999999 (Lỗi sai mã)</button>
              <button class="btn btn-secondary btn-sm" id="btn-quick-000000" style="padding: 4px 8px; font-size: 12px;">000000 (Mã hết hạn)</button>
            </div>
          </div>
        </div>

        <div>
          <button class="btn btn-primary btn-block" id="btn-otp-submit">
            Xác nhận
          </button>
        </div>
      </div>
    `;

    const digits = container.querySelectorAll('.otp-digit');
    const errorMsg = container.querySelector('#otp-error-msg');
    const submitBtn = container.querySelector('#btn-otp-submit');
    const resendBtn = container.querySelector('#btn-resend-otp');
    const countdownSpan = container.querySelector('#otp-countdown');

    // Auto-advance OTP inputs
    digits.forEach((input, idx) => {
      input.addEventListener('input', (e) => {
        errorMsg.style.display = 'none';
        digits.forEach(d => d.style.borderColor = 'var(--color-outline-variant)');

        if (input.value && idx < 5) {
          digits[idx + 1].focus();
        }
      });

      input.addEventListener('keydown', (e) => {
        if (e.key === 'Backspace' && !input.value && idx > 0) {
          digits[idx - 1].focus();
        }
      });
    });

    if (digits[0]) digits[0].focus();

    // Start countdown
    timer = setInterval(() => {
      countdown--;
      if (countdownSpan) countdownSpan.textContent = countdown;
      if (countdown <= 0) {
        clearInterval(timer);
        if (resendBtn) {
          resendBtn.disabled = false;
          resendBtn.textContent = 'Gửi lại mã ngay';
        }
      }
    }, 1000);

    container.querySelector('#btn-otp-back').addEventListener('click', () => {
      clearInterval(timer);
      router.navigate('phone-input', { phone, returnUrl });
    });

    container.querySelector('#btn-change-phone').addEventListener('click', () => {
      clearInterval(timer);
      router.navigate('phone-input', { phone, returnUrl });
    });

    resendBtn.addEventListener('click', () => {
      countdown = 60;
      resendBtn.disabled = true;
      resendBtn.innerHTML = `Gửi lại mã (<span id="otp-countdown">60</span>s)`;
      router.showToast(`Đã gửi lại mã OTP tới ${phone}`, 'info');
    });

    // Quick fill helper
    const fillCode = (code) => {
      code.split('').forEach((c, i) => {
        if (digits[i]) digits[i].value = c;
      });
      digits[5].focus();
    };

    container.querySelector('#btn-quick-123456').addEventListener('click', () => fillCode('123456'));
    container.querySelector('#btn-quick-999999').addEventListener('click', () => fillCode('999999'));
    container.querySelector('#btn-quick-000000').addEventListener('click', () => fillCode('000000'));

    // Handle Submit & AUTH-04 routing
    submitBtn.addEventListener('click', () => {
      const code = Array.from(digits).map(d => d.value).join('');

      if (code.length < 6) {
        errorMsg.textContent = 'Vui lòng nhập đủ 6 chữ số của mã OTP';
        errorMsg.style.display = 'block';
        return;
      }

      // Check simulated error
      const simErr = store.consumeSimulatedError();
      if (simErr) {
        errorMsg.textContent = `Lỗi hệ thống: ${simErr}`;
        errorMsg.style.display = 'block';
        return;
      }

      // Verify simulated OTP values
      if (code === '999999') {
        errorMsg.textContent = 'Mã xác thực không chính xác. Vui lòng kiểm tra lại.';
        errorMsg.style.display = 'block';
        digits.forEach(d => {
          d.style.borderColor = 'var(--color-error)';
          d.value = '';
        });
        digits[0].focus();
        return;
      }

      if (code === '000000') {
        errorMsg.textContent = 'Mã xác thực đã hết hạn. Vui lòng bấm gửi lại mã mới.';
        errorMsg.style.display = 'block';
        digits.forEach(d => d.style.borderColor = 'var(--color-error)');
        return;
      }

      // Successful OTP verification
      clearInterval(timer);
      router.showToast('Xác thực số điện thoại thành công!', 'success');

      // Find or switch character matching this phone
      const characters = store.getState().characters;
      let matchedUserId = Object.keys(characters).find(k => characters[k].phone === phone);

      if (matchedUserId) {
        store.switchUser(matchedUserId);
      } else {
        // Create new user in state
        const newId = 'user-' + Date.now();
        store.state.characters[newId] = {
          id: newId,
          phone,
          displayName: '',
          preferredPosition: null,
          wardId: null,
          dateOfBirth: null,
          gender: null,
          bio: '',
          avatarUrl: null,
          reputationScore: 100,
          completionRate: null,
          matchCount: 0,
          isNewUser: true,
          isDeleted: false,
          role: 'USER',
          label: `Người dùng ${phone}`
        };
        store.switchUser(newId);
        matchedUserId = newId;
      }

      // AC-P01: If user is new -> route to PROF-02; else route to target
      const user = store.getCurrentUser();
      if (user?.isNewUser || !user?.displayName) {
        router.navigate('profile-edit', { isNewUser: 'true', returnUrl });
      } else {
        router.navigate(returnUrl || 'matches');
      }
    });
  });

  /* =========================================================================
     AUTH-05: Deleted Account Error State (AUTH_003)
     ========================================================================= */
  router.registerView('auth-deleted-account', (container, params) => {
    router.setHeaderTitle('Tài khoản đã đóng');
    const phone = params?.phone || '0900000099';

    container.innerHTML = `
      <div style="padding: 24px 20px; min-height: 85vh; display: flex; flex-direction: column; justify-content: space-between; text-align: center;">
        <div style="padding-top: 40px; display: flex; flex-direction: column; align-items: center;">
          <!-- Red error icon -->
          <div style="width: 72px; height: 72px; border-radius: 50%; background: var(--color-error-container); color: var(--color-error); display: flex; align-items: center; justify-content: center; margin-bottom: 20px;">
            <div style="transform: scale(1.6);">${Icons.warning}</div>
          </div>

          <span class="badge badge-red" style="margin-bottom: 12px;">MÃ LỖI: AUTH_003</span>
          <h1 style="font-size: 22px; font-weight: 800; color: var(--color-on-background); margin: 0 0 12px;">
            Tài khoản đã bị xóa vĩnh viễn
          </h1>
          <p style="font-size: 14px; color: var(--color-on-surface-variant); line-height: 1.5; max-width: 310px; margin: 0 0 24px;">
            Tài khoản liên kết với số điện thoại <strong>${phone}</strong> đã được đóng theo yêu cầu của người dùng.
          </p>

          <div class="notice notice-danger" style="text-align: left; max-width: 320px; font-size: 13px;">
            Theo chính sách bảo mật của ArenaFlow, dữ liệu tài khoản đã xóa không thể khôi phục. Mọi lịch sử thi đấu trước đây đã được ẩn danh.
          </div>
        </div>

        <div style="display: flex; flex-direction: column; gap: 10px;">
          <button class="btn btn-primary btn-block" id="btn-deleted-back-login">
            Quay lại trang đăng nhập
          </button>
          <button class="btn btn-secondary btn-block" id="btn-deleted-other-phone">
            Nhập số điện thoại khác
          </button>
        </div>
      </div>
    `;

    container.querySelector('#btn-deleted-back-login').addEventListener('click', () => router.navigate('login'));
    container.querySelector('#btn-deleted-other-phone').addEventListener('click', () => router.navigate('phone-input'));
  });

  /* =========================================================================
     AUTH-06: Session Expired Dialog (Invoked on 401 or token timeout)
     ========================================================================= */
  router.registerDialog('session-expired-dialog', (container, params) => {
    const returnUrl = params?.returnUrl || router.getRoute().route || 'matches';

    container.innerHTML = `
      <div style="text-align: center; padding: 8px 4px;">
        <div style="width: 56px; height: 56px; border-radius: 50%; background: var(--color-warning-container); color: var(--color-warning); display: flex; align-items: center; justify-content: center; margin: 0 auto 16px;">
          ${Icons.warning}
        </div>
        <h3 style="font-size: 18px; font-weight: 700; margin: 0 0 8px;">Phiên đăng nhập đã hết hạn</h3>
        <p style="font-size: 13.5px; color: var(--color-on-surface-variant); line-height: 1.45; margin: 0 0 20px;">
          Để bảo vệ tài khoản, phiên làm việc của bạn đã hết hạn. Vui lòng đăng nhập lại để tiếp tục.
        </p>
        <button class="btn btn-primary btn-block" id="btn-session-relogin">
          Đăng nhập lại ngay
        </button>
      </div>
    `;

    container.querySelector('#btn-session-relogin').addEventListener('click', () => {
      router.closeDialog();
      router.navigate('login', { returnUrl });
    });
  });

  /* =========================================================================
     PROF-01: My Profile View (Stitch S3)
     ========================================================================= */
  router.registerView('profile', (container) => {
    router.setHeaderTitle('Hồ sơ của tôi');
    const user = store.getCurrentUser();
    const state = store.getState();

    if (!user) {
      router.navigate('login');
      return;
    }

    const ward = state.wards?.find(w => w.id === user.wardId);
    const wardName = ward ? `${ward.name}, ${ward.district}` : 'Chưa cập nhật';

    const positionLabels = {
      FORWARD: 'Tiền đạo',
      MIDFIELDER: 'Tiền vệ',
      DEFENDER: 'Hậu vệ',
      GOALKEEPER: 'Thủ môn'
    };

    const genderLabels = {
      MALE: 'Nam',
      FEMALE: 'Nữ',
      OTHER: 'Khác'
    };

    container.innerHTML = `
      <div style="padding: 16px 20px 32px;">
        <!-- Profile Header Card -->
        <div class="card" style="margin-bottom: 16px; text-align: center; padding: 24px 16px;">
          <!-- Avatar with Position Badge -->
          <div style="position: relative; display: inline-block; margin-bottom: 12px;">
            <div class="avatar avatar-lg" style="width: 80px; height: 80px; font-size: 32px; margin: 0 auto; border: 3px solid #fff; box-shadow: 0 4px 12px rgba(0,0,0,0.1);">
              ${user.avatarUrl ? `<img src="${user.avatarUrl}" alt="${user.displayName}">` : `<span>${(user.displayName || user.phone).slice(0,1).toUpperCase()}</span>`}
            </div>
            ${user.preferredPosition ? `
              <span class="badge badge-green" style="position: absolute; bottom: -6px; left: 50%; transform: translateX(-50%); white-space: nowrap; font-size: 11px;">
                ${positionLabels[user.preferredPosition] || user.preferredPosition}
              </span>
            ` : ''}
          </div>

          <h2 style="font-size: 20px; font-weight: 800; color: var(--color-on-surface); margin: 6px 0 2px;">
            ${user.displayName || 'Chưa đặt tên'}
          </h2>
          <p style="font-size: 13px; color: var(--color-on-surface-variant); margin: 0 0 16px;">
            ${user.phone || 'Chưa có số'} • ${user.role === 'ADMIN' ? 'Quản trị viên' : 'Thành viên'}
          </p>

          <button class="btn btn-secondary btn-block" id="btn-profile-edit" style="gap: 6px;">
            ${Icons.edit}
            <span>Chỉnh sửa hồ sơ</span>
          </button>
        </div>

        <!-- Reputation & Stats Quick Card -->
        <div class="card" style="margin-bottom: 16px;">
          <div class="card-header" style="display: flex; justify-content: space-between; align-items: center;">
            <h3 class="card-title" style="font-size: 15px;">Uy tín & Hoạt động</h3>
            <button class="btn btn-ghost btn-sm" id="btn-view-rep" style="padding: 4px 8px; font-size: 12px; color: var(--color-primary);">
              Chi tiết ${Icons.chevronRight}
            </button>
          </div>
          <div class="card-body">
            <div style="display: grid; grid-cols: 3; grid-template-columns: repeat(3, 1fr); gap: 12px; text-align: center;">
              <div style="background: var(--color-surface-container-low); padding: 12px 6px; border-radius: var(--radius-base);">
                <div style="font-size: 20px; font-weight: 800; color: var(--color-primary);">${user.reputationScore || 100}</div>
                <div style="font-size: 11px; color: var(--color-on-surface-variant); margin-top: 2px;">Điểm uy tín</div>
              </div>
              <div style="background: var(--color-surface-container-low); padding: 12px 6px; border-radius: var(--radius-base);">
                <div style="font-size: 20px; font-weight: 800; color: var(--color-on-surface);">${user.completionRate !== null && user.completionRate !== undefined ? user.completionRate + '%' : '--'}</div>
                <div style="font-size: 11px; color: var(--color-on-surface-variant); margin-top: 2px;">Hoàn thành</div>
              </div>
              <div style="background: var(--color-surface-container-low); padding: 12px 6px; border-radius: var(--radius-base);">
                <div style="font-size: 20px; font-weight: 800; color: var(--color-on-surface);">${user.matchCount || 0}</div>
                <div style="font-size: 11px; color: var(--color-on-surface-variant); margin-top: 2px;">Trận đấu</div>
              </div>
            </div>
          </div>
        </div>

        <!-- Profile Details List -->
        <div class="card" style="margin-bottom: 20px;">
          <div class="card-header">
            <h3 class="card-title" style="font-size: 15px;">Thông tin cá nhân</h3>
          </div>
          <div class="card-body" style="padding: 0;">
            <div class="list-item" style="border-bottom: 1px solid var(--color-surface-container);">
              <div style="width: 100%;">
                <span style="font-size: 11px; text-transform: uppercase; font-weight: 600; color: var(--color-on-surface-variant);">Tiểu sử</span>
                <p style="margin: 4px 0 0; font-size: 13.5px; color: var(--color-on-surface); line-height: 1.45;">
                  ${user.bio || '<em style="color: var(--color-outline);">Chưa có tiểu sử giới thiệu.</em>'}
                </p>
              </div>
            </div>

            <div class="list-item" style="border-bottom: 1px solid var(--color-surface-container);">
              <div style="width: 100%; display: flex; justify-content: space-between; align-items: center;">
                <span style="font-size: 11px; text-transform: uppercase; font-weight: 600; color: var(--color-on-surface-variant);">Vị trí ưu thích</span>
                <span style="font-size: 13.5px; font-weight: 600; color: var(--color-on-surface);">
                  ${positionLabels[user.preferredPosition] || 'Chưa cập nhật'}
                </span>
              </div>
            </div>

            <div class="list-item" style="border-bottom: 1px solid var(--color-surface-container);">
              <div style="width: 100%; display: flex; justify-content: space-between; align-items: center;">
                <span style="font-size: 11px; text-transform: uppercase; font-weight: 600; color: var(--color-on-surface-variant);">Ngày sinh</span>
                <span style="font-size: 13.5px; font-weight: 600; color: var(--color-on-surface);">
                  ${user.dateOfBirth ? new Date(user.dateOfBirth).toLocaleDateString('vi-VN') : 'Chưa cập nhật'}
                </span>
              </div>
            </div>

            <div class="list-item" style="border-bottom: 1px solid var(--color-surface-container);">
              <div style="width: 100%; display: flex; justify-content: space-between; align-items: center;">
                <span style="font-size: 11px; text-transform: uppercase; font-weight: 600; color: var(--color-on-surface-variant);">Giới tính</span>
                <span style="font-size: 13.5px; font-weight: 600; color: var(--color-on-surface);">
                  ${genderLabels[user.gender] || 'Chưa cập nhật'}
                </span>
              </div>
            </div>

            <div class="list-item">
              <div style="width: 100%; display: flex; justify-content: space-between; align-items: center;">
                <span style="font-size: 11px; text-transform: uppercase; font-weight: 600; color: var(--color-on-surface-variant);">Khu vực sinh hoạt</span>
                <span style="font-size: 13.5px; font-weight: 600; color: var(--color-on-surface);">
                  ${wardName}
                </span>
              </div>
            </div>
          </div>
        </div>

        <!-- Account Options Button -->
        <button class="btn btn-secondary btn-block" id="btn-open-account-menu">
          ${Icons.user}
          <span>Quản lý tài khoản & Cài đặt</span>
        </button>
      </div>
    `;

    container.querySelector('#btn-profile-edit').addEventListener('click', () => {
      router.navigate('profile-edit');
    });

    container.querySelector('#btn-view-rep').addEventListener('click', () => {
      router.navigate('reputation-user', { userId: user.id });
    });

    container.querySelector('#btn-open-account-menu').addEventListener('click', () => {
      router.navigate('account-menu');
    });
  });

  /* =========================================================================
     PROF-02: Edit / Complete Profile Form (Stitch S4)
     Used for both New User profile completion (AC-P01) and Profile edit.
     ========================================================================= */
  router.registerView('profile-edit', (container, params) => {
    const isNewUser = params?.isNewUser === 'true';
    const returnUrl = params?.returnUrl || '';
    const user = store.getCurrentUser();
    const state = store.getState();

    router.setHeaderTitle(isNewUser ? 'Hoàn thiện hồ sơ' : 'Chỉnh sửa hồ sơ');

    let selectedAvatar = user?.avatarUrl || AVATAR_PRESETS[0];
    let selectedPosition = user?.preferredPosition || 'MIDFIELDER';
    let selectedGender = user?.gender || 'MALE';

    container.innerHTML = `
      <div style="padding: 16px 20px 32px;">
        ${isNewUser ? `
          <div class="notice notice-info" style="margin-bottom: 20px;">
            <strong>Chào mừng bạn đến với ArenaFlow!</strong>
            <p style="margin: 4px 0 0; font-size: 13px;">Vui lòng hoàn thiện hồ sơ để các đội bóng và người chơi dễ dàng nhận diện và kết nối.</p>
          </div>
        ` : ''}

        <form id="form-profile-edit" style="display: flex; flex-direction: column; gap: 16px;">
          <!-- Avatar Picker -->
          <div class="form-group" style="align-items: center; text-align: center;">
            <label class="form-label" style="margin-bottom: 8px;">Ảnh đại diện</label>
            <div class="avatar avatar-lg" id="profile-avatar-preview" style="width: 84px; height: 84px; margin-bottom: 12px; border: 3px solid var(--color-primary-container);">
              <img src="${selectedAvatar}" alt="Avatar preview" id="avatar-img-elem">
            </div>

            <!-- Avatar Presets Row -->
            <div style="display: flex; gap: 8px; justify-content: center; margin-bottom: 8px;">
              ${AVATAR_PRESETS.map((url, i) => `
                <img
                  src="${url}"
                  data-url="${url}"
                  class="avatar-preset-item"
                  style="width: 38px; height: 38px; border-radius: 50%; cursor: pointer; object-fit: cover; border: 2px solid ${url === selectedAvatar ? 'var(--color-primary)' : 'transparent'}; transition: all 0.2s ease;"
                />
              `).join('')}
            </div>
            <span class="form-hint">Chọn một ảnh đại diện có sẵn từ danh sách</span>
          </div>

          <!-- Display Name -->
          <div class="form-group">
            <label class="form-label" for="input-display-name">
              Họ và tên / Biệt danh <span style="color: var(--color-error);">*</span>
            </label>
            <input
              type="text"
              id="input-display-name"
              class="form-input"
              placeholder="VD: Nguyễn Văn A"
              value="${user?.displayName || ''}"
              maxlength="50"
            />
            <div id="err-display-name" class="field-error-msg" style="display: none;"></div>
          </div>

          <!-- Bio -->
          <div class="form-group">
            <label class="form-label" for="input-bio">Tiểu sử ngắn</label>
            <textarea
              id="input-bio"
              class="form-textarea"
              rows="3"
              placeholder="Sở trường, kinh nghiệm thi đấu, thời gian rảnh..."
              maxlength="200"
            >${user?.bio || ''}</textarea>
            <div style="display: flex; justify-content: space-between;">
              <span class="form-hint">Tối đa 200 ký tự</span>
              <span id="bio-char-count" class="form-hint">0/200</span>
            </div>
          </div>

          <!-- Preferred Position Chips -->
          <div class="form-group">
            <label class="form-label">Vị trí chơi ưa thích</label>
            <div style="display: flex; gap: 8px; flex-wrap: wrap;" id="position-chips">
              ${[
                { id: 'FORWARD', label: 'Tiền đạo' },
                { id: 'MIDFIELDER', label: 'Tiền vệ' },
                { id: 'DEFENDER', label: 'Hậu vệ' },
                { id: 'GOALKEEPER', label: 'Thủ môn' }
              ].map(p => `
                <button
                  type="button"
                  class="chip chip-selectable ${selectedPosition === p.id ? 'is-selected' : ''}"
                  data-pos="${p.id}"
                >
                  ${p.label}
                </button>
              `).join('')}
            </div>
          </div>

          <!-- Date of Birth (Age >= 13 rule) -->
          <div class="form-group">
            <label class="form-label" for="input-dob">
              Ngày sinh (Yêu cầu đủ 13 tuổi trở lên)
            </label>
            <input
              type="date"
              id="input-dob"
              class="form-input"
              value="${user?.dateOfBirth || '2000-01-01'}"
            />
            <div id="err-dob" class="field-error-msg" style="display: none;"></div>
          </div>

          <!-- Gender -->
          <div class="form-group">
            <label class="form-label">Giới tính</label>
            <div style="display: flex; gap: 8px;" id="gender-chips">
              ${[
                { id: 'MALE', label: 'Nam' },
                { id: 'FEMALE', label: 'Nữ' },
                { id: 'OTHER', label: 'Khác' }
              ].map(g => `
                <button
                  type="button"
                  class="chip chip-selectable ${selectedGender === g.id ? 'is-selected' : ''}"
                  data-gender="${g.id}"
                >
                  ${g.label}
                </button>
              `).join('')}
            </div>
          </div>

          <!-- Ward / Area Selector -->
          <div class="form-group">
            <label class="form-label" for="select-ward">Khu vực sinh hoạt chính</label>
            <select id="select-ward" class="form-select">
              <option value="">-- Chọn Phường / Xã --</option>
              ${state.wards?.map(w => `
                <option value="${w.id}" ${user?.wardId === w.id ? 'selected' : ''}>
                  ${w.name} (${w.district}, ${w.city})
                </option>
              `).join('')}
            </select>
          </div>

          <!-- Actions -->
          <div style="display: flex; flex-direction: column; gap: 10px; margin-top: 16px;">
            <button type="submit" class="btn btn-primary btn-block" id="btn-save-profile">
              ${isNewUser ? 'Hoàn tất & Tiếp tục' : 'Lưu thay đổi'}
            </button>

            ${isNewUser ? `
              <button type="button" class="btn btn-ghost btn-block" id="btn-skip-profile" style="color: var(--color-on-surface-variant);">
                Bỏ qua bước này, thiết lập sau
              </button>
            ` : `
              <button type="button" class="btn btn-secondary btn-block" id="btn-cancel-profile">
                Hủy bỏ
              </button>
            `}
          </div>
        </form>
      </div>
    `;

    // Setup interactive handlers
    const avatarImg = container.querySelector('#avatar-img-elem');
    const presetItems = container.querySelectorAll('.avatar-preset-item');

    presetItems.forEach(item => {
      item.addEventListener('click', () => {
        selectedAvatar = item.getAttribute('data-url');
        avatarImg.src = selectedAvatar;
        presetItems.forEach(p => p.style.borderColor = 'transparent');
        item.style.borderColor = 'var(--color-primary)';
      });
    });

    // Position chips handler
    const posChips = container.querySelectorAll('#position-chips .chip');
    posChips.forEach(chip => {
      chip.addEventListener('click', () => {
        selectedPosition = chip.getAttribute('data-pos');
        posChips.forEach(c => c.classList.remove('is-selected'));
        chip.classList.add('is-selected');
      });
    });

    // Gender chips handler
    const genderChips = container.querySelectorAll('#gender-chips .chip');
    genderChips.forEach(chip => {
      chip.addEventListener('click', () => {
        selectedGender = chip.getAttribute('data-gender');
        genderChips.forEach(c => c.classList.remove('is-selected'));
        chip.classList.add('is-selected');
      });
    });

    // Bio char counter
    const bioInput = container.querySelector('#input-bio');
    const bioCounter = container.querySelector('#bio-char-count');
    if (bioInput && bioCounter) {
      bioInput.addEventListener('input', () => {
        bioCounter.textContent = `${(bioInput.value || '').length}/200`;
      });
      bioCounter.textContent = `${(bioInput.value || '').length}/200`;
    }

    // Cancel / Skip buttons
    const cancelBtn = container.querySelector('#btn-cancel-profile');
    if (cancelBtn) {
      cancelBtn.addEventListener('click', () => router.navigate('profile'));
    }

    const skipBtn = container.querySelector('#btn-skip-profile');
    if (skipBtn) {
      skipBtn.addEventListener('click', () => {
        // Mark isNewUser as false and proceed
        if (user) {
          store.updateUserProfile(user.id, {
            displayName: user.displayName || user.phone,
            isNewUser: false
          });
        }
        router.navigate(returnUrl || 'matches');
      });
    }

    // Form submit
    const form = container.querySelector('#form-profile-edit');
    const nameInput = container.querySelector('#input-display-name');
    const dobInput = container.querySelector('#input-dob');
    const wardSelect = container.querySelector('#select-ward');
    const errName = container.querySelector('#err-display-name');
    const errDob = container.querySelector('#err-dob');

    form.addEventListener('submit', (e) => {
      e.preventDefault();
      errName.style.display = 'none';
      errDob.style.display = 'none';
      nameInput.classList.remove('has-error');
      dobInput.classList.remove('has-error');

      const displayName = nameInput.value.trim();
      const dateOfBirth = dobInput.value;
      const bio = bioInput.value.trim();
      const wardId = wardSelect.value || null;

      // Validation 1: displayName required & not empty whitespace
      if (!displayName) {
        errName.textContent = 'Tên hiển thị không được để trống hoặc chỉ chứa khoảng trắng';
        errName.style.display = 'block';
        nameInput.classList.add('has-error');
        nameInput.focus();
        return;
      }

      // Validation 2: date of birth >= 13 years old
      if (dateOfBirth) {
        const birthDate = new Date(dateOfBirth);
        const now = store.getClock();
        const age = (now - birthDate) / (365.25 * 24 * 60 * 60 * 1000);
        if (age < 13) {
          errDob.textContent = 'Bạn phải đủ ít nhất 13 tuổi để sử dụng ArenaFlow';
          errDob.style.display = 'block';
          dobInput.classList.add('has-error');
          dobInput.focus();
          return;
        }
      }

      try {
        store.updateUserProfile(user.id, {
          displayName,
          avatarUrl: selectedAvatar,
          bio,
          preferredPosition: selectedPosition,
          dateOfBirth,
          gender: selectedGender,
          wardId,
          isNewUser: false
        });

        router.showToast('Cập nhật hồ sơ thành công!', 'success');

        if (isNewUser) {
          router.navigate(returnUrl || 'matches');
        } else {
          router.navigate('profile');
        }
      } catch (err) {
        router.showToast(`Lỗi: ${err.message}`, 'error');
      }
    });
  });

  /* =========================================================================
     PROF-03: Account Menu (Stitch S5 context / Account Management)
     ========================================================================= */
  router.registerView('account-menu', (container) => {
    router.setHeaderTitle('Tài khoản & Cài đặt');
    const user = store.getCurrentUser();

    if (!user) {
      router.navigate('login');
      return;
    }

    container.innerHTML = `
      <div style="padding: 16px 20px 32px;">
        <!-- User Mini Card -->
        <div class="card" style="margin-bottom: 20px; padding: 16px; display: flex; align-items: center; gap: 14px;">
          <div class="avatar avatar-md" style="width: 52px; height: 52px; font-size: 22px;">
            ${user.avatarUrl ? `<img src="${user.avatarUrl}" alt="${user.displayName}">` : `<span>${(user.displayName || 'U').slice(0,1).toUpperCase()}</span>`}
          </div>
          <div style="flex: 1; overflow: hidden;">
            <div style="font-size: 16px; font-weight: 700; color: var(--color-on-surface); white-space: nowrap; overflow: hidden; text-overflow: ellipsis;">
              ${user.displayName || 'Người dùng'}
            </div>
            <div style="font-size: 12.5px; color: var(--color-on-surface-variant);">
              ${user.phone || 'Chưa có số'} • ${user.role}
            </div>
          </div>
          <button class="btn btn-ghost btn-sm" id="btn-go-profile" style="color: var(--color-primary); font-weight: 600;">
            Hồ sơ
          </button>
        </div>

        <!-- Menu Navigation List -->
        <div class="card" style="margin-bottom: 24px; padding: 0;">
          <div class="list-item" id="item-my-profile" style="cursor: pointer; border-bottom: 1px solid var(--color-surface-container);">
            <div style="display: flex; align-items: center; gap: 12px; width: 100%;">
              <span style="color: var(--color-primary);">${Icons.user}</span>
              <span style="flex: 1; font-weight: 600; font-size: 14px;">Hồ sơ của tôi</span>
              <span style="color: var(--color-outline);">${Icons.chevronRight}</span>
            </div>
          </div>

          <div class="list-item" id="item-my-teams" style="cursor: pointer; border-bottom: 1px solid var(--color-surface-container);">
            <div style="display: flex; align-items: center; gap: 12px; width: 100%;">
              <span style="color: var(--color-primary);">${Icons.shield}</span>
              <span style="flex: 1; font-weight: 600; font-size: 14px;">Đội của tôi</span>
              <span style="color: var(--color-outline);">${Icons.chevronRight}</span>
            </div>
          </div>

          <div class="list-item" id="item-help-faq" style="cursor: pointer; border-bottom: 1px solid var(--color-surface-container);">
            <div style="display: flex; align-items: center; gap: 12px; width: 100%;">
              <span style="color: var(--color-on-surface-variant);">${Icons.checkCircle}</span>
              <div style="flex: 1;">
                <span style="font-weight: 600; font-size: 14px;">Trung tâm trợ giúp</span>
                <span class="badge badge-gray" style="margin-left: 8px; font-size: 10px;">Chờ cung cấp</span>
              </div>
              <span style="color: var(--color-outline);">${Icons.chevronRight}</span>
            </div>
          </div>

          <!-- Switch Character quick trigger -->
          <div class="list-item" id="item-switch-char" style="cursor: pointer;">
            <div style="display: flex; align-items: center; gap: 12px; width: 100%;">
              <span style="color: var(--color-warning);">${Icons.soccer}</span>
              <div style="flex: 1;">
                <span style="font-weight: 600; font-size: 14px;">Đổi nhân vật thử nghiệm</span>
                <p style="margin: 2px 0 0; font-size: 12px; color: var(--color-on-surface-variant);">Mở thanh điều khiển Demo Panel</p>
              </div>
              <span style="color: var(--color-outline);">${Icons.chevronRight}</span>
            </div>
          </div>
        </div>

        <!-- Danger Zone: Logout and Delete Account -->
        <div style="display: flex; flex-direction: column; gap: 12px;">
          <button class="btn btn-outline btn-block" id="btn-logout" style="border-color: var(--color-outline); color: var(--color-on-surface);">
            ${Icons.logout}
            <span>Đăng xuất</span>
          </button>

          <button class="btn btn-danger-outline btn-block" id="btn-delete-account">
            ${Icons.trash}
            <span>Xóa tài khoản vĩnh viễn</span>
          </button>
        </div>
      </div>
    `;

    container.querySelector('#btn-go-profile').addEventListener('click', () => router.navigate('profile'));
    container.querySelector('#item-my-profile').addEventListener('click', () => router.navigate('profile'));
    container.querySelector('#item-my-teams').addEventListener('click', () => router.navigate('teams'));
    container.querySelector('#item-help-faq').addEventListener('click', () => {
      router.showToast('Nội dung Trợ giúp & Điều khoản đang chờ cung cấp văn bản chính thức (DEP-09).', 'info');
    });

    container.querySelector('#item-switch-char').addEventListener('click', () => {
      const demoFab = document.getElementById('demo-panel-fab');
      const demoContainer = document.getElementById('demo-panel-container');
      if (demoContainer) demoContainer.classList.remove('is-collapsed');
      if (demoFab) demoFab.style.display = 'none';
      router.showToast('Vui lòng chọn nhân vật từ bảng điều khiển Demo bên phải.', 'info');
    });

    container.querySelector('#btn-logout').addEventListener('click', () => {
      router.openSheet('logout-confirm-sheet');
    });

    // AC-P02: Trigger Delete Account Check
    container.querySelector('#btn-delete-account').addEventListener('click', () => {
      const check = store.canDeleteAccount(user.id);
      if (!check.canDelete) {
        // Blocked because captain of active teams
        router.openSheet('delete-blocked-sheet', { blockingTeams: check.blockingTeams });
      } else {
        // Safe to proceed to confirmation
        router.openSheet('delete-confirm-sheet');
      }
    });
  });

  /* =========================================================================
     PROF-04: Delete Account Confirmation Sheet (Stitch S5)
     ========================================================================= */
  router.registerSheet('delete-confirm-sheet', (body, footer) => {
    const user = store.getCurrentUser();

    body.innerHTML = `
      <div style="text-align: center; padding: 10px 0 20px;">
        <div style="width: 56px; height: 56px; border-radius: 50%; background: var(--color-error-container); color: var(--color-error); display: flex; align-items: center; justify-content: center; margin: 0 auto 16px;">
          ${Icons.warning}
        </div>

        <h3 style="font-size: 20px; font-weight: 800; color: var(--color-on-surface); margin: 0 0 10px;">
          Xóa tài khoản vĩnh viễn?
        </h3>

        <p style="font-size: 13.5px; color: var(--color-on-surface-variant); line-height: 1.5; text-align: left; margin: 0 0 16px;">
          Bạn sẽ bị đăng xuất và rời khỏi mọi đội bóng đang tham gia. Lời mời đang chờ sẽ bị hủy. Ở lịch sử trận đấu, tên bạn sẽ được hiển thị ẩn danh là <strong>"Người dùng đã xóa"</strong>.
        </p>

        <div class="notice notice-danger" style="text-align: left; font-size: 12.5px; margin-bottom: 20px;">
          Hành động này là vĩnh viễn và không thể khôi phục theo quy định tại mã lỗi AUTH_003.
        </div>

        <label style="display: flex; align-items: center; gap: 10px; cursor: pointer; text-align: left; font-size: 13px; font-weight: 600; color: var(--color-on-surface);">
          <input type="checkbox" id="chk-confirm-delete" style="width: 18px; height: 18px; accent-color: var(--color-error);" />
          <span>Tôi hiểu rõ hệ quả và đồng ý xóa tài khoản</span>
        </label>
      </div>
    `;

    footer.innerHTML = `
      <div style="display: flex; gap: 10px; width: 100%;">
        <button class="btn btn-secondary" style="flex: 1;" onclick="window.AppRouter.closeSheet()">
          Hủy bỏ
        </button>
        <button class="btn btn-danger" style="flex: 1;" id="btn-do-delete-account" disabled>
          Xác nhận xóa
        </button>
      </div>
    `;

    const chk = body.querySelector('#chk-confirm-delete');
    const deleteBtn = footer.querySelector('#btn-do-delete-account');

    chk.addEventListener('change', () => {
      deleteBtn.disabled = !chk.checked;
    });

    deleteBtn.addEventListener('click', () => {
      try {
        store.deleteAccount(user.id);
        router.closeSheet();
        router.showToast('Tài khoản đã được xóa vĩnh viễn.', 'info');
        router.navigate('login');
      } catch (err) {
        router.showToast(err.message, 'error');
      }
    });
  });

  /* =========================================================================
     PROF-05: Block Delete Account Sheet (Captain active teams, AC-P02)
     ========================================================================= */
  router.registerSheet('delete-blocked-sheet', (body, footer, params, titleElem) => {
    if (titleElem) titleElem.textContent = 'Chặn xóa tài khoản';
    const user = store.getCurrentUser();
    const blockingTeams = params?.blockingTeams || store.canDeleteAccount(user.id).blockingTeams;

    body.innerHTML = `
      <div style="padding: 4px 0 16px;">
        <div class="notice notice-danger" style="margin-bottom: 16px;">
          <div style="display: flex; gap: 8px; align-items: flex-start;">
            <span style="color: var(--color-error);">${Icons.warning}</span>
            <div>
              <strong>Không thể xóa tài khoản lúc này!</strong>
              <p style="margin: 4px 0 0; font-size: 13px; line-height: 1.45;">
                Bạn hiện đang là <strong>Đội trưởng</strong> của ${blockingTeams.length} đội bóng đang hoạt động. Vui lòng chuyển giao quyền đội trưởng hoặc giải tán đội trước khi xóa tài khoản.
              </p>
            </div>
          </div>
        </div>

        <div style="font-size: 13px; font-weight: 700; color: var(--color-on-surface); margin-bottom: 10px;">
          Các đội bóng cần xử lý:
        </div>

        <div style="display: flex; flex-direction: column; gap: 10px; margin-bottom: 20px;">
          ${blockingTeams.map(team => `
            <div style="background: var(--color-surface-container-low); border: 1px solid var(--color-outline-variant); border-radius: var(--radius-base); padding: 12px;">
              <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px;">
                <div>
                  <strong style="font-size: 14px; color: var(--color-on-surface);">${team.name}</strong>
                  <span class="badge badge-amber" style="margin-left: 6px; font-size: 10px;">Đội trưởng</span>
                </div>
                <span style="font-size: 12px; color: var(--color-on-surface-variant);">${team.shortName}</span>
              </div>

              <div style="display: flex; gap: 8px;">
                <button class="btn btn-secondary btn-sm" style="flex: 1; font-size: 12px;" onclick="window.AppRouter.closeSheet(); window.AppRouter.openSheet('transfer-captain-sheet', { teamId: '${team.id}' })">
                  Chuyển quyền
                </button>
                <button class="btn btn-danger-outline btn-sm" style="flex: 1; font-size: 12px;" onclick="window.AppRouter.closeSheet(); window.AppRouter.openSheet('disband-team-sheet', { teamId: '${team.id}' })">
                  Giải tán đội
                </button>
              </div>
            </div>
          `).join('')}
        </div>
      </div>
    `;

    footer.innerHTML = `
      <div style="display: flex; gap: 10px; width: 100%;">
        <button class="btn btn-secondary" style="flex: 1;" onclick="window.AppRouter.closeSheet()">
          Đóng
        </button>
        <button class="btn btn-primary" style="flex: 1;" id="btn-recheck-delete">
          Kiểm tra lại điều kiện
        </button>
      </div>
    `;

    footer.querySelector('#btn-recheck-delete').addEventListener('click', () => {
      const check = store.canDeleteAccount(user.id);
      if (check.canDelete) {
        router.closeSheet();
        router.showToast('Bạn đã đủ điều kiện xóa tài khoản.', 'success');
        router.openSheet('delete-confirm-sheet');
      } else {
        router.showToast(`Vẫn còn ${check.blockingTeams.length} đội bóng cần xử lý.`, 'error');
        router.openSheet('delete-blocked-sheet', { blockingTeams: check.blockingTeams });
      }
    });
  });

  /* =========================================================================
     PROF-06: Logout Confirmation Sheet
     ========================================================================= */
  router.registerSheet('logout-confirm-sheet', (body, footer) => {
    body.innerHTML = `
      <div style="text-align: center; padding: 12px 0 20px;">
        <div style="width: 52px; height: 52px; border-radius: 50%; background: var(--color-surface-container-high); color: var(--color-on-surface-variant); display: flex; align-items: center; justify-content: center; margin: 0 auto 16px;">
          ${Icons.logout}
        </div>
        <h3 style="font-size: 18px; font-weight: 700; color: var(--color-on-surface); margin: 0 0 8px;">
          Đăng xuất khỏi ArenaFlow?
        </h3>
        <p style="font-size: 13.5px; color: var(--color-on-surface-variant); line-height: 1.45; margin: 0;">
          Bạn có thể đăng nhập lại bất cứ lúc nào bằng số điện thoại hoặc tài khoản xã hội đã liên kết.
        </p>
      </div>
    `;

    footer.innerHTML = `
      <div style="display: flex; gap: 10px; width: 100%;">
        <button class="btn btn-secondary" style="flex: 1;" onclick="window.AppRouter.closeSheet()">
          Hủy
        </button>
        <button class="btn btn-danger" style="flex: 1;" id="btn-confirm-logout">
          Đăng xuất
        </button>
      </div>
    `;

    footer.querySelector('#btn-confirm-logout').addEventListener('click', () => {
      router.closeSheet();
      router.showToast('Đã đăng xuất thành công.', 'info');
      router.navigate('login');
    });
  });
}
