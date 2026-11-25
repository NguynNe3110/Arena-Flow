/**
 * ArenaFlow Screens: APP-01..02, AUTH-01..06 (S1, S2a, S2b), PROF-01..06 (S3, S4, S5)
 * Classic non-module script compatible with file:// protocol.
 */
(function (global) {
  'use strict';

  const R = () => global.ArenaRouter;
  const S = () => global.ArenaStore;
  const UI = () => global.ArenaUI;

  const POSITION_LABELS = {
    GOALKEEPER: 'Thủ môn',
    DEFENDER: 'Hậu vệ',
    MIDFIELDER: 'Tiền vệ',
    FORWARD: 'Tiền đạo'
  };

  const GENDER_LABELS = {
    MALE: 'Nam',
    FEMALE: 'Nữ',
    OTHER: 'Khác'
  };

  // Transient UI state for interactive Auth & Profile screens
  const authState = {
    s1Error: false,
    s1LoadingProvider: null,
    s2aPhone: '0987654321',
    s2aError: '',
    s2bDigits: ['1', '2', '3', '4', '5', '6'],
    s2bError: '',
    s2bCountdown: 45,
    s2bTimer: null,
    onboardingSlide: 0,
    pendingDest: 'MATCH-01'
  };

  function getAge(isoDate) {
    if (!isoDate) return 0;
    const dob = new Date(isoDate);
    if (Number.isNaN(dob.getTime())) return 0;
    const now = S().getMockClock();
    let age = now.getFullYear() - dob.getFullYear();
    const m = now.getMonth() - dob.getMonth();
    if (m < 0 || (m === 0 && now.getDate() < dob.getDate())) age--;
    return age;
  }

  function formatDisplayDate(isoDate) {
    if (!isoDate) return 'Chưa cập nhật';
    const parts = String(isoDate).split('-');
    if (parts.length !== 3) return isoDate;
    return `${parts[2]}/${parts[1]}/${parts[0]}`;
  }

  function getCaptainedBlockingTeams(userId) {
    if (!userId) return [];
    return S().getState().teams.filter(
      (t) => t.captainId === userId && (t.status === 'ACTIVE' || t.status === 'INACTIVE')
    );
  }

  /* -------------------------------------------------------------------------
     APP-01: Splash & Session Gate
     ------------------------------------------------------------------------- */
  function renderApp01(container) {
    const user = S().getCurrentUser();
    const state = S().getState();
    const seenOnboarding = !!(state.meta && state.meta.onboardingSeen);

    container.innerHTML = `
      <div class="w-full flex-1 flex flex-col bg-[#14532D] text-white relative overflow-hidden">
        <svg class="absolute inset-0 w-full h-full opacity-15 pointer-events-none stroke-white" fill="none" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 390 844" preserveAspectRatio="none">
          <line x1="20" y1="20" x2="370" y2="20" stroke-width="1.5" />
          <line x1="20" y1="824" x2="370" y2="824" stroke-width="1.5" />
          <line x1="20" y1="20" x2="20" y2="824" stroke-width="1.5" />
          <line x1="370" y1="20" x2="370" y2="824" stroke-width="1.5" />
          <line x1="20" y1="422" x2="370" y2="422" stroke-width="1.5" />
          <circle cx="195" cy="422" r="68" stroke-width="1.5" />
          <circle cx="195" cy="422" r="4" fill="white" />
        </svg>

        <div class="relative z-10 flex-1 flex flex-col items-center justify-center px-6 text-center">
          <div class="w-20 h-20 rounded-2xl bg-white/15 border border-white/25 flex items-center justify-center shadow-lg mb-5">
            <span class="material-symbols-outlined text-[42px] text-[#BDEFBF]">sports_soccer</span>
          </div>
          <h1 class="text-3xl font-bold tracking-tight text-white mb-2">ArenaFlow</h1>
          <p class="text-sm text-white/80 max-w-[260px] leading-relaxed">
            Kết nối đội bóng phủi, ghép kèo minh bạch &amp; quản lý đá lẻ chuyên nghiệp.
          </p>

          <div class="mt-8 flex items-center gap-2 text-xs text-[#BDEFBF] bg-black/20 px-3.5 py-2 rounded-full border border-white/15">
            <span class="w-2 h-2 rounded-full bg-[#4ADE80] animate-pulse"></span>
            <span>${user ? `Đang đăng nhập: ${UI().esc(user.displayName || user.phone)}` : 'Chưa có phiên đăng nhập'}</span>
          </div>
        </div>

        <div class="relative z-10 px-6 pb-8 space-y-2.5">
          <button type="button" id="btn-app01-continue"
                  class="w-full py-3.5 px-4 rounded-[8px] bg-[#3D6943] hover:bg-[#315C38] border border-white/20 text-white font-semibold text-sm shadow-md transition-all flex items-center justify-center gap-2">
            <span>${user ? 'Vào ứng dụng ngay' : (seenOnboarding ? 'Đăng nhập' : 'Bắt đầu khám phá')}</span>
            <span class="material-symbols-outlined text-[18px]">arrow_forward</span>
          </button>

          <div class="grid grid-cols-2 gap-2 pt-1">
            <button type="button" onclick="window.ArenaRouter.navigate('APP-02')"
                    class="py-2 px-3 rounded-[8px] bg-white/10 hover:bg-white/15 text-white/90 text-xs font-medium transition-colors">
              Xem Onboarding
            </button>
            <button type="button" onclick="window.ArenaRouter.navigate('AUTH-06')"
                    class="py-2 px-3 rounded-[8px] bg-white/10 hover:bg-white/15 text-white/90 text-xs font-medium transition-colors">
              Test Phiên hết hạn
            </button>
          </div>
        </div>
      </div>
    `;

    const btn = container.querySelector('#btn-app01-continue');
    if (btn) {
      btn.addEventListener('click', () => {
        const curUser = S().getCurrentUser();
        if (curUser) {
          if (curUser.isDeleted) {
            R().navigate('AUTH-05');
          } else {
            R().navigate('AUTH-04');
          }
        } else if (!seenOnboarding) {
          R().navigate('APP-02');
        } else {
          R().navigate('S1');
        }
      });
    }
  }

  /* -------------------------------------------------------------------------
     APP-02: Onboarding (3 slides)
     ------------------------------------------------------------------------- */
  const ONBOARDING_SLIDES = [
    {
      icon: 'handshake',
      title: 'Tìm kèo & Ghép đối ngang trình',
      desc: 'Khám phá kèo A đã có sân hoặc kèo B thương lượng linh hoạt theo khu vực, thời gian và chi phí minh bạch.'
    },
    {
      icon: 'person_search',
      title: 'Tuyển người đá lẻ chuẩn vị trí',
      desc: 'Thiếu thủ môn hay tiền đạo? Đăng tin tuyển theo vị trí, duyệt ứng viên dựa trên điểm uy tín cá nhân.'
    },
    {
      icon: 'verified_user',
      title: 'Xây dựng uy tín đội bóng',
      desc: 'Đánh giá hai chiều sau mỗi trận, ghi nhận tỷ lệ hoàn thành kèo và quản lý lịch sử thành viên rõ ràng.'
    }
  ];

  function renderApp02(container) {
    const idx = Math.max(0, Math.min(ONBOARDING_SLIDES.length - 1, authState.onboardingSlide));
    const slide = ONBOARDING_SLIDES[idx];
    const isLast = idx === ONBOARDING_SLIDES.length - 1;

    const finishOnboarding = () => {
      S().setState((st) => {
        st.meta = st.meta || {};
        st.meta.onboardingSeen = true;
      });
      R().navigate('S1');
    };

    container.innerHTML = `
      <div class="w-full flex-1 flex flex-col bg-[#F8FAF3]">
        ${UI().pitchHeader({ compact: true, showLogo: true, rightHtml: `
          <button type="button" id="btn-ob-skip" class="text-xs font-semibold text-white/85 hover:text-white px-2.5 py-1 rounded-lg bg-white/10">
            Bỏ qua
          </button>
        ` })}

        <div class="flex-1 flex flex-col items-center justify-center px-6 py-8 text-center">
          <div class="w-24 h-24 rounded-full bg-[#BDEFBF]/60 border-2 border-[#3D6943]/25 flex items-center justify-center text-[#14532D] mb-6 shadow-sm">
            <span class="material-symbols-outlined text-[46px]">${slide.icon}</span>
          </div>
          <h1 class="text-xl font-bold text-[#111827] leading-snug mb-3">${UI().esc(slide.title)}</h1>
          <p class="text-sm text-[#596158] leading-relaxed max-w-[290px]">${UI().esc(slide.desc)}</p>

          <div class="flex items-center justify-center gap-2 mt-8">
            ${ONBOARDING_SLIDES.map((_, i) => `
              <button type="button" data-slide="${i}" aria-label="Slide ${i + 1}"
                      class="h-2 rounded-full transition-all ${i === idx ? 'w-6 bg-[#3D6943]' : 'w-2 bg-[#DDE5D9]'}"></button>
            `).join('')}
          </div>
        </div>

        <div class="px-6 pb-8 pt-2 bg-white border-t border-[#E5E7EB]">
          <button type="button" id="btn-ob-next"
                  class="w-full py-3.5 px-4 rounded-[8px] bg-[#3D6943] hover:bg-[#315C38] text-white font-semibold text-sm shadow-sm transition-all flex items-center justify-center gap-1.5">
            <span>${isLast ? 'Bắt đầu ngay' : 'Tiếp tục'}</span>
            <span class="material-symbols-outlined text-[18px]">arrow_forward</span>
          </button>
        </div>
      </div>
    `;

    container.querySelector('#btn-ob-skip').addEventListener('click', finishOnboarding);
    container.querySelector('#btn-ob-next').addEventListener('click', () => {
      if (isLast) {
        finishOnboarding();
      } else {
        authState.onboardingSlide = idx + 1;
        renderApp02(container);
      }
    });
    container.querySelectorAll('[data-slide]').forEach((dot) => {
      dot.addEventListener('click', () => {
        authState.onboardingSlide = Number(dot.getAttribute('data-slide'));
        renderApp02(container);
      });
    });
  }

  /* -------------------------------------------------------------------------
     S1 / AUTH-01: Đăng nhập
     ------------------------------------------------------------------------- */
  function renderS1(container) {
    const isErr = authState.s1Error;
    const loadingProv = authState.s1LoadingProvider;

    const handleOAuth = (provider) => {
      if (authState.s1LoadingProvider) return;
      authState.s1Error = false;
      authState.s1LoadingProvider = provider;
      renderS1(container);

      setTimeout(() => {
        authState.s1LoadingProvider = null;
        if (!S().getCurrentUser()) {
          S().switchRole('Captain');
        }
        UI().toast(`Đăng nhập ${provider} thành công!`, 'success');
        R().navigate('AUTH-04');
      }, 600);
    };

    container.innerHTML = `
      <div class="w-full flex-1 flex flex-col bg-white">
        <header class="relative bg-[#14532D] text-white pt-10 pb-8 px-6 overflow-hidden shrink-0">
          <svg class="absolute inset-0 w-full h-full opacity-10 pointer-events-none stroke-white" fill="none" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 390 240" preserveAspectRatio="none">
            <line stroke-dasharray="4 4" stroke-width="1.5" x1="0" x2="390" y1="20" y2="20"></line>
            <line stroke-width="1.5" x1="0" x2="390" y1="120" y2="120"></line>
            <circle cx="195" cy="120" r="54" stroke-width="1.5"></circle>
            <circle cx="195" cy="120" fill="white" r="3"></circle>
            <line stroke-width="1.5" x1="195" x2="195" y1="0" y2="240"></line>
            <rect height="48" stroke-width="1.5" width="120" x="135" y="-1"></rect>
            <path d="M 165 47 A 30 30 0 0 0 225 47" stroke-width="1.5"></path>
          </svg>
          <div class="relative z-10 flex flex-col">
            <div class="flex items-center gap-2.5 mb-6">
              <div class="w-10 h-10 rounded-[8px] bg-white/15 border border-white/25 flex items-center justify-center text-white shadow-inner">
                <span class="material-symbols-outlined text-[22px]">sports_soccer</span>
              </div>
              <span class="font-bold text-2xl tracking-tight text-white">ArenaFlow</span>
            </div>
            <h1 class="font-bold text-[24px] leading-tight text-white mb-2.5">
              Tạo kèo, ghép đội, đá bóng dễ dàng.
            </h1>
            <p class="text-sm text-white/80 leading-relaxed">
              Tìm đối thủ ngang trình độ, tuyển thêm người chơi và xây dựng uy tín cho đội của bạn.
            </p>
          </div>
        </header>

        <section class="flex-1 bg-white px-6 pt-7 pb-6 flex flex-col justify-between">
          <div>
            <div class="mb-5">
              <h2 class="font-bold text-xl text-[#111827] mb-1">Đăng nhập hoặc đăng ký</h2>
              <p class="text-sm text-[#596158]">Tài khoản mới sẽ được tạo tự động ở lần đăng nhập đầu tiên</p>
            </div>

            ${isErr ? `
              <div class="mb-5 bg-[#FEF2F2] text-[#A73B21] border border-[#FECACA] rounded-[8px] p-3 flex items-start gap-2.5">
                <span class="material-symbols-outlined text-[#DC2626] text-xl shrink-0 mt-0.5">error</span>
                <span class="text-sm leading-snug font-medium">Đăng ký thất bại, vui lòng thử lại</span>
              </div>
            ` : ''}

            <div class="flex flex-col gap-3">
              <button type="button" id="btn-login-google"
                      ${loadingProv ? 'disabled' : ''}
                      class="w-full rounded-[8px] py-3 px-4 border border-[#E5E7EB] ${loadingProv === 'Google' || isErr ? 'bg-slate-50 text-slate-500' : 'bg-white hover:bg-[#F8FAF3] text-[#111827]'} flex items-center justify-between text-sm font-medium transition-all active:scale-[0.99]">
                <div class="flex items-center gap-3">
                  <svg class="w-5 h-5 shrink-0 ${loadingProv === 'Google' ? 'grayscale opacity-60' : ''}" viewBox="0 0 24 24">
                    <path d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z" fill="#4285F4"></path>
                    <path d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z" fill="#34A853"></path>
                    <path d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.06H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.94l2.85-2.22.81-.63z" fill="#FBBC05"></path>
                    <path d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.06l3.66 2.84c.87-2.6 3.3-4.52 6.16-4.52z" fill="#EA4335"></path>
                  </svg>
                  <span>Tiếp tục với Google</span>
                </div>
                ${loadingProv === 'Google' ? `
                  <span class="w-4 h-4 rounded-full border-2 border-[#DDE5D9] border-t-[#3D6943] animate-spin"></span>
                ` : '<span class="material-symbols-outlined text-[18px] text-[#596158]">chevron_right</span>'}
              </button>

              <button type="button" id="btn-login-fb"
                      ${loadingProv ? 'disabled' : ''}
                      class="w-full rounded-[8px] py-3 px-4 border border-[#E5E7EB] ${loadingProv === 'Facebook' ? 'bg-slate-50 text-slate-500' : 'bg-white hover:bg-[#F8FAF3] text-[#111827]'} flex items-center justify-between text-sm font-medium transition-all active:scale-[0.99]">
                <div class="flex items-center gap-3">
                  <svg class="w-5 h-5 shrink-0 text-[#1877F2] fill-current" viewBox="0 0 24 24">
                    <path d="M24 12.073c0-6.627-5.373-12-12-12s-12 5.373-12 12c0 5.99 4.388 10.954 10.125 11.854v-8.385H7.078v-3.47h3.047V9.43c0-3.007 1.792-4.669 4.533-4.669 1.312 0 2.686.235 2.686.235v2.953H15.83c-1.491 0-1.956.925-1.956 1.874v2.25h3.328l-.532 3.47h-2.796v8.385C19.612 23.027 24 18.062 24 12.073z"></path>
                  </svg>
                  <span>Tiếp tục với Facebook</span>
                </div>
                ${loadingProv === 'Facebook' ? `
                  <span class="w-4 h-4 rounded-full border-2 border-[#DDE5D9] border-t-[#3D6943] animate-spin"></span>
                ` : '<span class="material-symbols-outlined text-[18px] text-[#596158]">chevron_right</span>'}
              </button>

              <button type="button" id="btn-login-phone"
                      class="w-full rounded-[8px] py-3 px-4 border border-[#E5E7EB] bg-white hover:bg-[#F8FAF3] text-[#111827] flex items-center justify-between text-sm font-medium transition-all active:scale-[0.99]">
                <div class="flex items-center gap-3">
                  <span class="material-symbols-outlined text-[20px] text-[#3D6943]">call</span>
                  <span>Tiếp tục bằng số điện thoại</span>
                </div>
                <span class="material-symbols-outlined text-[18px] text-[#596158]">chevron_right</span>
              </button>
            </div>
          </div>

          <div class="pt-6 border-t border-[#E5E7EB] mt-6 space-y-2">
            <div class="flex items-center justify-between text-xs">
              <button type="button" id="btn-s1-toggle-error"
                      class="text-[#596158] hover:text-[#111827] underline font-medium">
                ${isErr ? 'Ẩn trạng thái lỗi' : 'Xem trạng thái lỗi (Error)'}
              </button>
              <button type="button" onclick="window.ArenaRouter.navigate('AUTH-05')"
                      class="text-[#A73B21] hover:underline font-medium">
                Test AUTH_003
              </button>
            </div>
          </div>
        </section>
      </div>
    `;

    container.querySelector('#btn-login-google').addEventListener('click', () => handleOAuth('Google'));
    container.querySelector('#btn-login-fb').addEventListener('click', () => handleOAuth('Facebook'));
    container.querySelector('#btn-login-phone').addEventListener('click', () => {
      authState.s2aError = '';
      R().navigate('S2a');
    });
    container.querySelector('#btn-s1-toggle-error').addEventListener('click', () => {
      authState.s1Error = !authState.s1Error;
      renderS1(container);
    });
  }

  /* -------------------------------------------------------------------------
     S2a / AUTH-02: Nhập số điện thoại
     ------------------------------------------------------------------------- */
  function renderS2a(container) {
    const phone = authState.s2aPhone;
    const err = authState.s2aError;

    container.innerHTML = `
      <div class="w-full flex-1 flex flex-col bg-white">
        ${UI().pitchHeader({ compact: true, showLogo: true })}

        <div class="flex-1 bg-white px-6 pt-6 pb-8 flex flex-col">
          <button type="button" id="btn-s2a-back"
                  class="inline-flex items-center gap-1 text-sm font-medium text-[#2D342C] hover:text-[#111827] mb-6 w-fit">
            <span class="material-symbols-outlined text-[18px]">arrow_back</span>
            <span>Quay lại</span>
          </button>

          <h1 class="text-2xl font-bold text-[#111827] tracking-tight mb-2">Nhập số điện thoại</h1>
          <p class="text-sm text-[#596158] leading-relaxed mb-7">
            Chúng tôi sẽ gửi mã xác thực gồm 6 chữ số tới số này
          </p>

          <div class="flex flex-col mb-6">
            <label for="s2a-phone-input" class="text-xs font-semibold text-[#2D342C] mb-1.5">
              Số điện thoại
            </label>
            <input type="tel" id="s2a-phone-input" value="${UI().esc(phone)}" maxlength="10" inputmode="numeric"
                   placeholder="Ví dụ: 0987654321"
                   class="w-full px-3.5 py-3 text-base text-[#111827] bg-white border ${err ? 'border-[#A73B21] focus:ring-[#A73B21]' : 'border-[#DDE5D9] focus:border-[#3D6943] focus:ring-[#3D6943]'} rounded-[8px] focus:outline-none focus:ring-1" />
            ${err ? `
              <div class="flex items-center gap-1.5 mt-2 text-xs font-medium text-[#A73B21]">
                <span class="material-symbols-outlined text-[15px] shrink-0">error</span>
                <span>${UI().esc(err)}</span>
              </div>
            ` : ''}
          </div>

          <button type="button" id="btn-s2a-submit"
                  class="w-full py-3.5 px-4 bg-[#3D6943] hover:bg-[#315C38] text-white font-semibold text-sm rounded-[8px] shadow-sm transition-all flex items-center justify-center">
            Gửi mã xác thực
          </button>

          <div class="mt-auto pt-6 border-t border-[#E5E7EB] flex items-center justify-between text-xs">
            <button type="button" id="btn-s2a-toggle-err" class="text-[#596158] hover:text-[#111827] underline">
              ${err ? 'Xóa lỗi' : 'Xem trạng thái lỗi (098765432)'}
            </button>
          </div>
        </div>
      </div>
    `;

    const inputEl = container.querySelector('#s2a-phone-input');
    inputEl.addEventListener('input', (e) => {
      authState.s2aPhone = e.target.value.replace(/\D/g, '').slice(0, 10);
      e.target.value = authState.s2aPhone;
      if (authState.s2aError) {
        authState.s2aError = '';
      }
    });

    container.querySelector('#btn-s2a-back').addEventListener('click', () => R().navigate('S1'));
    container.querySelector('#btn-s2a-submit').addEventListener('click', () => {
      const val = authState.s2aPhone.trim();
      if (!/^0\d{9}$/.test(val)) {
        authState.s2aError = 'Số điện thoại phải gồm 10 chữ số và bắt đầu bằng 0';
        renderS2a(container);
        return;
      }
      authState.s2aError = '';
      authState.s2bError = '';
      authState.s2bDigits = ['1', '2', '3', '4', '5', '6'];
      authState.s2bCountdown = 45;
      UI().toast(`Đã gửi mã OTP tới ${val}`, 'info');
      R().navigate('S2b');
    });

    container.querySelector('#btn-s2a-toggle-err').addEventListener('click', () => {
      if (authState.s2aError) {
        authState.s2aPhone = '0987654321';
        authState.s2aError = '';
      } else {
        authState.s2aPhone = '098765432';
        authState.s2aError = 'Số điện thoại phải gồm 10 chữ số và bắt đầu bằng 0';
      }
      renderS2a(container);
    });
  }

  /* -------------------------------------------------------------------------
     S2b / AUTH-03: Nhập mã OTP (Phase 0 fixes applied)
     ------------------------------------------------------------------------- */
  function renderS2b(container) {
    const err = authState.s2bError;
    const digits = authState.s2bDigits;
    const phone = authState.s2aPhone || '0987654321';

    container.innerHTML = `
      <div class="w-full flex-1 flex flex-col bg-white">
        ${UI().pitchHeader({ compact: true, showLogo: true })}

        <div class="flex-1 bg-white px-6 pt-6 pb-8 flex flex-col">
          <div class="mb-5">
            <button type="button" id="btn-s2b-back"
                    class="inline-flex items-center gap-1 text-sm font-medium text-[#2D342C] hover:text-[#111827]">
              <span class="material-symbols-outlined text-[18px]">arrow_back</span>
              <span>Quay lại</span>
            </button>
          </div>

          <h1 class="text-[24px] font-bold text-[#111827] tracking-tight leading-snug mb-2">
            Nhập mã xác thực
          </h1>

          <p class="text-sm text-[#596158] leading-relaxed mb-6">
            Mã gồm 6 chữ số đã được gửi tới số <span class="font-semibold text-[#111827]">${UI().esc(phone)}</span>
            <button type="button" id="btn-s2b-change-phone" class="text-[#14532D] font-semibold ml-1.5 hover:underline">
              Đổi số điện thoại
            </button>
          </p>

          <div class="space-y-4">
            <div class="grid grid-cols-6 gap-2">
              ${digits.map((d, i) => `
                <input type="text" inputmode="numeric" maxlength="1" data-otp-idx="${i}" value="${UI().esc(d)}"
                       aria-label="Chữ số OTP ${i + 1}"
                       class="h-14 w-full text-center rounded-[8px] border-2 ${err ? 'border-error' : 'border-[#DDE5D9] focus:border-[#3D6943]'} bg-white text-xl font-bold text-[#111827] shadow-sm focus:outline-none" />
              `).join('')}
            </div>

            ${err ? `
              <div class="text-[13px] font-medium text-error flex items-center gap-1.5 pt-0.5">
                <span class="material-symbols-outlined text-[16px]">error</span>
                <span>${UI().esc(err)}</span>
              </div>
            ` : `
              <p class="text-xs text-[#596158]">Mã demo hợp lệ: <strong>123456</strong> (nhập <strong>482193</strong> để xem lỗi).</p>
            `}

            <div class="flex items-center justify-between text-xs pt-1">
              <span class="text-[#596158]">Chưa nhận được mã?</span>
              <button type="button" id="btn-s2b-resend"
                      class="font-semibold ${authState.s2bCountdown > 0 ? 'text-[#596158]' : 'text-[#3D6943] hover:underline'}">
                ${authState.s2bCountdown > 0 ? `Gửi lại sau ${authState.s2bCountdown}s` : 'Gửi lại mã'}
              </button>
            </div>

            <div class="pt-3">
              <button type="button" id="btn-s2b-submit"
                      class="w-full h-12 bg-primary hover:bg-[#315C38] active:scale-[0.99] transition-all text-white font-semibold text-[15px] rounded-[8px] shadow flex items-center justify-center">
                Xác nhận
              </button>
            </div>
          </div>

          <div class="mt-auto pt-6 border-t border-[#E5E7EB] flex items-center justify-between text-xs">
            <button type="button" id="btn-s2b-toggle-err" class="text-[#596158] hover:text-[#111827] underline">
              ${err ? 'Xóa lỗi (về 123456)' : 'Xem trạng thái lỗi (482193)'}
            </button>
          </div>
        </div>
      </div>
    `;

    const otpInputs = Array.from(container.querySelectorAll('[data-otp-idx]'));
    otpInputs.forEach((inp, idx) => {
      inp.addEventListener('input', (e) => {
        const val = e.target.value.replace(/\D/g, '').slice(-1);
        e.target.value = val;
        authState.s2bDigits[idx] = val;
        authState.s2bError = '';
        if (val && idx < otpInputs.length - 1) {
          otpInputs[idx + 1].focus();
        }
      });
      inp.addEventListener('keydown', (e) => {
        if (e.key === 'Backspace' && !e.target.value && idx > 0) {
          otpInputs[idx - 1].focus();
        }
      });
    });

    container.querySelector('#btn-s2b-back').addEventListener('click', () => R().navigate('S2a'));
    container.querySelector('#btn-s2b-change-phone').addEventListener('click', () => R().navigate('S2a'));
    container.querySelector('#btn-s2b-resend').addEventListener('click', () => {
      authState.s2bCountdown = 45;
      UI().toast('Đã gửi lại mã OTP mới', 'info');
      renderS2b(container);
    });

    container.querySelector('#btn-s2b-submit').addEventListener('click', () => {
      const code = authState.s2bDigits.join('');
      if (code.length < 6 || code === '482193') {
        authState.s2bError = 'Mã xác thực không đúng';
        renderS2b(container);
        return;
      }
      authState.s2bError = '';
      if (!S().getCurrentUser()) {
        S().switchRole('Captain');
      }
      UI().toast('Xác thực OTP thành công!', 'success');
      R().navigate('AUTH-04');
    });

    container.querySelector('#btn-s2b-toggle-err').addEventListener('click', () => {
      if (authState.s2bError) {
        authState.s2bDigits = ['1', '2', '3', '4', '5', '6'];
        authState.s2bError = '';
      } else {
        authState.s2bDigits = ['4', '8', '2', '1', '9', '3'];
        authState.s2bError = 'Mã xác thực không đúng';
      }
      renderS2b(container);
    });
  }

  /* -------------------------------------------------------------------------
     AUTH-04: Phân luồng sau đăng nhập
     ------------------------------------------------------------------------- */
  function renderAuth04(container) {
    const user = S().getCurrentUser();
    const isDeleted = user && user.isDeleted;
    const isNew = user && (user.isNewUser || !user.displayName);

    let targetRoute = 'MATCH-01';
    let branchTitle = 'Người dùng đã có hồ sơ';
    let branchDesc = `Xin chào ${user ? (user.displayName || user.phone) : ''}. Hệ thống chuyển bạn về màn hình chính Tìm kèo.`;

    if (!user) {
      targetRoute = 'S1';
      branchTitle = 'Chưa có phiên đăng nhập';
      branchDesc = 'Vui lòng đăng nhập để tiếp tục.';
    } else if (isDeleted) {
      targetRoute = 'AUTH-05';
      branchTitle = 'Tài khoản đã bị xóa (AUTH_003)';
      branchDesc = 'Tài khoản này không thể tiếp tục sử dụng.';
    } else if (isNew) {
      targetRoute = 'S4';
      branchTitle = 'Tài khoản mới (isNewUser = true)';
      branchDesc = 'Bạn cần hoàn thiện hồ sơ cá nhân trước khi tham gia ghép kèo.';
    }

    container.innerHTML = `
      <div class="w-full flex-1 flex flex-col bg-[#F8FAF3]">
        ${UI().pitchHeader({ title: 'Đang phân luồng đăng nhập', subtitle: 'Kiểm tra trạng thái tài khoản & hồ sơ', compact: true })}

        <div class="p-5 flex-1 flex flex-col justify-between">
          <div class="bg-white rounded-[12px] border border-[#DDE5D9] p-5 shadow-sm space-y-4">
            <div class="flex items-center gap-3">
              <div class="w-11 h-11 rounded-full bg-[#EAF0E6] text-[#3D6943] flex items-center justify-center shrink-0">
                <span class="material-symbols-outlined text-[24px]">route</span>
              </div>
              <div>
                <h2 class="font-bold text-base text-[#111827]">${UI().esc(branchTitle)}</h2>
                <p class="text-xs text-[#596158]">Đích điều hướng: <code class="font-mono font-semibold text-[#14532D]">${targetRoute}</code></p>
              </div>
            </div>
            <p class="text-sm text-[#2D342C] leading-relaxed">${UI().esc(branchDesc)}</p>
            <button type="button" id="btn-auth04-go"
                    class="w-full py-3 px-4 rounded-[8px] bg-[#3D6943] hover:bg-[#315C38] text-white font-semibold text-sm transition-colors">
              Tiếp tục tới ${targetRoute}
            </button>
          </div>

          <div class="bg-white rounded-[12px] border border-[#DDE5D9] p-4 space-y-2">
            <p class="text-xs font-semibold text-[#596158] uppercase tracking-wider">Chuyển nhanh nhánh test</p>
            <div class="grid grid-cols-2 gap-2 text-xs">
              <button type="button" onclick="window.ArenaStore.switchRole('NewUser');window.ArenaRouter.navigate('S4');"
                      class="py-2 px-3 rounded-[8px] border border-[#DDE5D9] hover:bg-[#F8FAF3] font-medium text-[#2D342C]">
                User mới → S4
              </button>
              <button type="button" onclick="window.ArenaStore.switchRole('Captain');window.ArenaRouter.navigate('MATCH-01');"
                      class="py-2 px-3 rounded-[8px] border border-[#DDE5D9] hover:bg-[#F8FAF3] font-medium text-[#2D342C]">
                User cũ → Home
              </button>
            </div>
          </div>
        </div>
      </div>
    `;

    container.querySelector('#btn-auth04-go').addEventListener('click', () => {
      R().navigate(targetRoute, isNew ? { onboarding: '1' } : {});
    });
  }

  /* -------------------------------------------------------------------------
     AUTH-05: Tài khoản đã xóa (AUTH_003)
     ------------------------------------------------------------------------- */
  function renderAuth05(container) {
    container.innerHTML = `
      <div class="w-full flex-1 flex flex-col bg-[#F8FAF3]">
        ${UI().pitchHeader({ title: 'Tài khoản đã bị xóa', subtitle: 'Mã lỗi AUTH_003', compact: true })}

        <div class="p-5 flex-1 flex flex-col justify-center">
          <div class="bg-white rounded-[12px] border border-[#FECACA] p-6 text-center shadow-sm space-y-4">
            <div class="w-14 h-14 rounded-full bg-[#FEF2F2] text-[#A73B21] flex items-center justify-center mx-auto">
              <span class="material-symbols-outlined text-[30px]">person_off</span>
            </div>
            <h2 class="text-lg font-bold text-[#111827]">Không thể đăng nhập (AUTH_003)</h2>
            <p class="text-sm text-[#596158] leading-relaxed">
              Tài khoản này đã được xóa vĩnh viễn và không thể khôi phục. Phiên đăng nhập hiện tại đã được xóa khỏi thiết bị.
            </p>
            <button type="button" id="btn-auth05-other"
                    class="w-full py-3 px-4 rounded-[8px] bg-[#3D6943] hover:bg-[#315C38] text-white font-semibold text-sm transition-colors">
              Chọn tài khoản khác
            </button>
          </div>
        </div>
      </div>
    `;

    container.querySelector('#btn-auth05-other').addEventListener('click', () => {
      S().switchRole('Guest');
      UI().toast('Đã xóa phiên cũ. Vui lòng chọn tài khoản khác.', 'info');
      R().navigate('S1');
    });
  }

  /* -------------------------------------------------------------------------
     AUTH-06: Phiên hết hạn
     ------------------------------------------------------------------------- */
  function renderAuth06(container) {
    container.innerHTML = `
      <div class="w-full flex-1 flex flex-col bg-[#F8FAF3] relative">
        ${UI().pitchHeader({ title: 'Phiên làm việc', subtitle: 'Bảo mật tài khoản', compact: true })}

        <div class="flex-1 p-5 flex items-center justify-center">
          <div class="w-full bg-white rounded-[16px] border border-[#E5E7EB] p-6 shadow-lg text-center space-y-4">
            <div class="w-14 h-14 rounded-full bg-[#FEF3C7] text-[#B45309] flex items-center justify-center mx-auto">
              <span class="material-symbols-outlined text-[30px]">lock_clock</span>
            </div>
            <h2 class="text-lg font-bold text-[#111827]">Phiên đăng nhập đã hết hạn</h2>
            <p class="text-sm text-[#596158] leading-relaxed">
              Token làm mới không còn hợp lệ. Vui lòng đăng nhập lại để tiếp tục thao tác tại trang đang xem.
            </p>
            <div class="flex flex-col gap-2.5 pt-2">
              <button type="button" id="btn-auth06-relogin"
                      class="w-full py-3 px-4 rounded-[8px] bg-[#3D6943] hover:bg-[#315C38] text-white font-semibold text-sm transition-colors">
                Đăng nhập lại
              </button>
              <button type="button" id="btn-auth06-guest"
                      class="w-full py-3 px-4 rounded-[8px] border border-[#DDE5D9] bg-white hover:bg-[#F8FAF3] text-[#2D342C] font-semibold text-sm transition-colors">
                Về màn hình đăng nhập
              </button>
            </div>
          </div>
        </div>
      </div>
    `;

    const goLogin = () => {
      S().switchRole('Guest');
      R().navigate('S1');
    };
    container.querySelector('#btn-auth06-relogin').addEventListener('click', goLogin);
    container.querySelector('#btn-auth06-guest').addEventListener('click', goLogin);
  }

  /* -------------------------------------------------------------------------
     PROF-01 / S3: Hồ sơ cá nhân
     ------------------------------------------------------------------------- */
  function renderProf01(container) {
    const user = S().getCurrentUser();
    if (!user) {
      container.innerHTML = `<div class="p-4 flex-1">${UI().emptyState({
        icon: 'person_off',
        title: 'Bạn đang ở chế độ Khách',
        description: 'Đăng nhập để xem hồ sơ cá nhân, điểm uy tín và quản lý đội bóng.',
        actionLabel: 'Đăng nhập ngay',
        onAction: "window.ArenaRouter.navigate('S1')"
      })}</div>`;
      return;
    }

    const posLabel = POSITION_LABELS[user.preferredPosition] || 'Chưa cập nhật';
    const genderLabel = GENDER_LABELS[user.gender] || 'Chưa cập nhật';
    const wardLabel = user.wardId ? S().getWardName(user.wardId) : null;
    const blockingTeams = getCaptainedBlockingTeams(user.id);

    container.innerHTML = `
      <div class="w-full flex-1 flex flex-col px-4 py-4 gap-4 bg-[#F8FAF3]">
        <div class="flex items-center justify-between">
          <h1 class="text-xl font-bold text-[#111827] tracking-tight">Hồ sơ cá nhân</h1>
          <button type="button" onclick="window.ArenaRouter.navigate('PROF-03')"
                  class="inline-flex items-center gap-1 px-3 py-1.5 rounded-[8px] bg-white border border-[#DDE5D9] text-xs font-semibold text-[#2D342C] hover:bg-[#EAF0E6]">
            <span class="material-symbols-outlined text-[16px]">settings</span>
            <span>Tài khoản</span>
          </button>
        </div>

        <section class="bg-white rounded-[8px] border border-[#DDE5D9] shadow-sm p-4 flex flex-col items-center">
          ${UI().avatar(user, { size: 80 })}
          <h2 class="text-lg font-bold text-[#111827] mt-3 text-center">
            ${UI().esc(user.displayName || 'Chưa đặt tên')}
          </h2>
          <p class="text-xs text-[#596158] mt-0.5">${UI().esc(user.phone || '')}</p>

          <div class="flex items-center gap-2 mt-2.5">
            ${UI().badge(`Uy tín ${user.reputationScore ?? 100}đ`, 'success')}
            ${UI().badge(`${user.matchCount ?? 0} trận`, 'neutral')}
            ${user.role === 'ADMIN' ? UI().badge('ADMIN', 'dark') : ''}
          </div>

          <div class="w-full grid grid-cols-2 gap-2 mt-4">
            <button type="button" onclick="window.ArenaRouter.navigate('S4')"
                    class="py-2 px-3 rounded-[8px] border border-[#DDE5D9] text-[#2D342C] font-semibold text-xs hover:bg-[#F8FAF3] flex items-center justify-center gap-1.5">
              <span class="material-symbols-outlined text-[16px]">edit</span>
              <span>Sửa hồ sơ</span>
            </button>
            <button type="button" onclick="window.ArenaRouter.navigate('REP-05')"
                    class="py-2 px-3 rounded-[8px] bg-[#EAF0E6] text-[#1C4825] font-semibold text-xs hover:bg-[#D4E8D1] flex items-center justify-center gap-1.5">
              <span class="material-symbols-outlined text-[16px]">military_tech</span>
              <span>Uy tín cá nhân</span>
            </button>
          </div>

          <div class="w-full mt-4 divide-y divide-[#F1F5EC] border-t border-[#F1F5EC]">
            <div class="py-3">
              <span class="block text-[11px] font-semibold text-[#596158] uppercase tracking-wider mb-1">Tiểu sử</span>
              <p class="text-sm text-[#2D342C] leading-relaxed">${user.bio ? UI().esc(user.bio) : '<span class="italic text-[#757D73]">Chưa cập nhật</span>'}</p>
            </div>
            <div class="py-3">
              <span class="block text-[11px] font-semibold text-[#596158] uppercase tracking-wider mb-1">Vị trí chơi ưa thích</span>
              <p class="text-sm text-[#111827] font-medium">${UI().esc(posLabel)}</p>
            </div>
            <div class="py-3">
              <span class="block text-[11px] font-semibold text-[#596158] uppercase tracking-wider mb-1">Ngày sinh</span>
              <p class="text-sm text-[#111827] font-medium">${UI().esc(formatDisplayDate(user.dateOfBirth))}</p>
            </div>
            <div class="py-3">
              <span class="block text-[11px] font-semibold text-[#596158] uppercase tracking-wider mb-1">Giới tính</span>
              <p class="text-sm text-[#111827] font-medium">${UI().esc(genderLabel)}</p>
            </div>
            <div class="pt-3 pb-1">
              <span class="block text-[11px] font-semibold text-[#596158] uppercase tracking-wider mb-1">Khu vực</span>
              <p class="text-sm ${wardLabel ? 'text-[#111827] font-medium' : 'text-[#757D73] italic'}">${wardLabel ? UI().esc(wardLabel) : 'Chưa cập nhật'}</p>
            </div>
          </div>
        </section>

        <section class="bg-white rounded-[8px] border border-[#FECACA] p-4 flex flex-col">
          <h3 class="text-base font-bold text-[#A73B21] mb-1">Xoá tài khoản</h3>
          <p class="text-xs text-[#596158] leading-relaxed mb-3.5">
            Bạn sẽ bị đăng xuất và rời mọi đội đang tham gia. Nếu đang là đội trưởng, bạn cần chuyển quyền hoặc giải tán đội trước.
          </p>
          <button type="button" id="btn-prof01-delete"
                  class="w-full py-2.5 px-4 rounded-[8px] border border-[#A73B21] text-[#A73B21] font-semibold text-sm hover:bg-[#FEF2F2] transition-colors">
            Xoá tài khoản
          </button>
        </section>
      </div>
    `;

    container.querySelector('#btn-prof01-delete').addEventListener('click', () => {
      if (blockingTeams.length > 0) {
        R().navigate('PROF-05');
      } else {
        R().navigate('S5');
      }
    });
  }

  /* -------------------------------------------------------------------------
     S4 / PROF-02: Hoàn thiện và sửa hồ sơ (Material Symbols Outlined, rounded-[8px], avatar())
     ------------------------------------------------------------------------- */
  function renderS4(container, params) {
    const user = S().getCurrentUser();
    if (!user) {
      R().navigate('S1');
      return;
    }

    const isOnboarding = params && params.onboarding === '1';
    const wards = S().getState().wards || [];

    let draft = {
      displayName: user.displayName || '',
      bio: user.bio || '',
      preferredPosition: user.preferredPosition || 'MIDFIELDER',
      dateOfBirth: user.dateOfBirth || '1998-05-15',
      gender: user.gender || 'MALE',
      wardId: user.wardId || ''
    };
    let errors = {};

    const draw = () => {
      container.innerHTML = `
        <div class="w-full flex-1 flex flex-col bg-white">
          <div class="flex-1 overflow-y-auto px-4 pt-5 pb-6 space-y-5">
            ${isOnboarding ? `
              <div class="bg-[#EAF0E6] border border-[#BDEFBF] rounded-[8px] p-3 text-xs text-[#1C4825] flex items-start gap-2">
                <span class="material-symbols-outlined text-[18px] shrink-0">info</span>
                <span>Hoàn thiện hồ sơ để bắt đầu tìm kèo và tham gia đội bóng.</span>
              </div>
            ` : ''}

            <div class="flex flex-col items-center justify-center pb-1">
              <div class="mb-3">${UI().avatar(Object.assign({}, user, { displayName: draft.displayName || user.displayName || 'AF' }), { size: 88 })}</div>
              <button type="button" onclick="window.ArenaUI.toast('Đã cập nhật ảnh đại diện mặc định theo tên', 'info')"
                      class="inline-flex items-center gap-1.5 px-3 py-1.5 text-xs font-medium text-[#2D342C] bg-white border border-[#DDE5D9] rounded-[8px] hover:bg-[#F8FAF3]">
                <span class="material-symbols-outlined text-[16px] text-[#596158]">photo_camera</span>
                <span>Đổi ảnh</span>
              </button>
            </div>

            <div class="space-y-1.5">
              <label for="s4-name" class="block text-xs font-semibold text-[#2D342C]">
                Tên hiển thị <span class="text-[#DC2626]">*</span>
              </label>
              <input id="s4-name" type="text" maxlength="100" value="${UI().esc(draft.displayName)}"
                     placeholder="Nhập tên hiển thị của bạn"
                     class="w-full px-3 py-2.5 text-sm text-[#111827] bg-white border ${errors.displayName ? 'border-[#A73B21]' : 'border-[#DDE5D9] focus:border-[#3D6943]'} rounded-[8px] focus:outline-none" />
              <div class="flex items-center justify-between text-[11px]">
                <span class="text-[#A73B21] font-medium">${UI().esc(errors.displayName || '')}</span>
                <span id="s4-name-count" class="text-[#596158]">${draft.displayName.length}/100</span>
              </div>
            </div>

            <div class="space-y-1.5">
              <label for="s4-bio" class="block text-xs font-semibold text-[#2D342C]">Tiểu sử</label>
              <textarea id="s4-bio" rows="3" maxlength="1000"
                        class="w-full px-3 py-2 text-sm text-[#111827] bg-white border border-[#DDE5D9] rounded-[8px] focus:outline-none focus:border-[#3D6943] resize-none leading-relaxed">${UI().esc(draft.bio)}</textarea>
              <div class="text-right text-[11px] text-[#596158]">
                <span id="s4-bio-count">${draft.bio.length}/1000</span>
              </div>
            </div>

            <div class="space-y-2">
              <label class="block text-xs font-semibold text-[#2D342C]">Vị trí chơi ưa thích</label>
              <div class="flex flex-wrap gap-2">
                ${Object.entries(POSITION_LABELS).map(([key, label]) => {
                  const active = draft.preferredPosition === key;
                  return `
                    <button type="button" data-pos="${key}"
                            class="px-3.5 py-1.5 text-xs font-semibold rounded-[8px] border transition-colors ${active ? 'bg-[#3D6943] text-white border-[#3D6943]' : 'bg-white text-[#596158] border-[#DDE5D9] hover:border-[#3D6943]'}">
                      ${label}
                    </button>
                  `;
                }).join('')}
              </div>
            </div>

            <div class="space-y-1.5">
              <label for="s4-dob" class="block text-xs font-semibold text-[#2D342C]">Ngày sinh</label>
              <div class="relative">
                <input id="s4-dob" type="date" value="${UI().esc(draft.dateOfBirth)}"
                       class="w-full px-3 py-2.5 text-sm text-[#111827] bg-white border ${errors.dateOfBirth ? 'border-[#A73B21]' : 'border-[#DDE5D9] focus:border-[#3D6943]'} rounded-[8px] focus:outline-none" />
              </div>
              ${errors.dateOfBirth ? `
                <p class="text-[11px] text-[#A73B21] font-medium flex items-center gap-1">
                  <span class="material-symbols-outlined text-[14px]">error</span>
                  <span>${UI().esc(errors.dateOfBirth)}</span>
                </p>
              ` : ''}
            </div>

            <div class="space-y-1.5">
              <label for="s4-gender" class="block text-xs font-semibold text-[#2D342C]">Giới tính</label>
              <div class="relative">
                <select id="s4-gender"
                        class="w-full px-3 py-2.5 pr-10 text-sm text-[#111827] bg-white border border-[#DDE5D9] rounded-[8px] appearance-none focus:outline-none focus:border-[#3D6943]">
                  ${Object.entries(GENDER_LABELS).map(([k, lbl]) => `
                    <option value="${k}" ${draft.gender === k ? 'selected' : ''}>${lbl}</option>
                  `).join('')}
                </select>
                <span class="material-symbols-outlined text-[18px] text-[#596158] absolute right-3 top-1/2 -translate-y-1/2 pointer-events-none">expand_more</span>
              </div>
            </div>

            <div class="space-y-1.5">
              <label for="s4-ward" class="block text-xs font-semibold text-[#2D342C]">Khu vực (phường/xã)</label>
              <div class="relative">
                <span class="material-symbols-outlined text-[18px] text-[#596158] absolute left-3 top-1/2 -translate-y-1/2 pointer-events-none">search</span>
                <select id="s4-ward"
                        class="w-full pl-9 pr-10 py-2.5 text-sm text-[#111827] bg-white border border-[#DDE5D9] rounded-[8px] appearance-none focus:outline-none focus:border-[#3D6943]">
                  <option value="">Chọn phường/xã</option>
                  ${wards.map((w) => `
                    <option value="${w.id}" ${draft.wardId === w.id ? 'selected' : ''}>${UI().esc(w.name)}, ${UI().esc(w.district)}</option>
                  `).join('')}
                </select>
                <span class="material-symbols-outlined text-[18px] text-[#596158] absolute right-3 top-1/2 -translate-y-1/2 pointer-events-none">expand_more</span>
              </div>
            </div>
          </div>

          <footer class="bg-white border-t border-[#E5E7EB] px-4 py-3 grid grid-cols-2 gap-3 shrink-0">
            <button type="button" id="btn-s4-cancel"
                    class="w-full py-2.5 text-sm font-semibold text-[#2D342C] bg-white border border-[#DDE5D9] rounded-[8px] hover:bg-[#F8FAF3]">
              Huỷ
            </button>
            <button type="button" id="btn-s4-save"
                    class="w-full py-2.5 text-sm font-semibold text-white bg-[#3D6943] hover:bg-[#315C38] rounded-[8px] shadow-sm">
              Lưu thay đổi
            </button>
          </footer>
        </div>
      `;

      container.querySelector('#s4-name').addEventListener('input', (e) => {
        draft.displayName = e.target.value;
        container.querySelector('#s4-name-count').textContent = `${draft.displayName.length}/100`;
      });
      container.querySelector('#s4-bio').addEventListener('input', (e) => {
        draft.bio = e.target.value;
        container.querySelector('#s4-bio-count').textContent = `${draft.bio.length}/1000`;
      });
      container.querySelectorAll('[data-pos]').forEach((btn) => {
        btn.addEventListener('click', () => {
          draft.preferredPosition = btn.getAttribute('data-pos');
          draw();
        });
      });
      container.querySelector('#s4-dob').addEventListener('change', (e) => {
        draft.dateOfBirth = e.target.value;
      });
      container.querySelector('#s4-gender').addEventListener('change', (e) => {
        draft.gender = e.target.value;
      });
      container.querySelector('#s4-ward').addEventListener('change', (e) => {
        draft.wardId = e.target.value;
      });

      container.querySelector('#btn-s4-cancel').addEventListener('click', () => {
        R().navigate(isOnboarding ? 'S1' : 'PROF-01');
      });

      container.querySelector('#btn-s4-save').addEventListener('click', () => {
        errors = {};
        if (!draft.displayName.trim()) {
          errors.displayName = 'Tên hiển thị không được để trống';
        }
        if (draft.dateOfBirth && getAge(draft.dateOfBirth) < 13) {
          errors.dateOfBirth = 'Bạn phải đủ 13 tuổi';
        }
        if (Object.keys(errors).length > 0) {
          draw();
          return;
        }
        S().setState((st) => {
          const u = st.users[user.id];
          if (u) {
            u.displayName = draft.displayName.trim();
            u.bio = draft.bio.trim();
            u.preferredPosition = draft.preferredPosition;
            u.dateOfBirth = draft.dateOfBirth;
            u.gender = draft.gender;
            u.wardId = draft.wardId || null;
            u.isNewUser = false;
          }
        });
        UI().toast('Đã cập nhật hồ sơ cá nhân thành công!', 'success');
        R().navigate(isOnboarding ? 'MATCH-01' : 'PROF-01');
      });
    };

    draw();
  }

  /* -------------------------------------------------------------------------
     PROF-03: Menu tài khoản & cài đặt
     ------------------------------------------------------------------------- */
  function renderProf03(container) {
    const user = S().getCurrentUser();
    if (!user) {
      R().navigate('S1');
      return;
    }
    const blockingTeams = getCaptainedBlockingTeams(user.id);

    container.innerHTML = `
      <div class="w-full flex-1 flex flex-col bg-[#F8FAF3] p-4 space-y-4">
        <div class="bg-white rounded-[12px] border border-[#DDE5D9] p-4 flex items-center gap-3">
          ${UI().avatar(user, { size: 48 })}
          <div class="min-w-0 flex-1">
            <h2 class="font-bold text-base text-[#111827] truncate">${UI().esc(user.displayName || user.phone)}</h2>
            <p class="text-xs text-[#596158]">${UI().esc(user.phone)} • ${user.role === 'ADMIN' ? 'Quản trị viên' : 'Thành viên'}</p>
          </div>
          <button type="button" onclick="window.ArenaRouter.navigate('PROF-01')"
                  class="text-xs font-semibold text-[#3D6943] hover:underline">
            Xem hồ sơ
          </button>
        </div>

        <div class="bg-white rounded-[12px] border border-[#DDE5D9] divide-y divide-[#F1F5EC] overflow-hidden">
          ${[
            { icon: 'edit', label: 'Sửa thông tin cá nhân', route: 'S4' },
            { icon: 'military_tech', label: 'Uy tín cá nhân', route: 'REP-05' },
            { icon: 'flag', label: 'Báo cáo & Tranh chấp liên quan', route: 'REP-07' },
            { icon: 'groups', label: 'Đội bóng của tôi', route: 'TEAM-01' },
            { icon: 'notifications', label: 'Trung tâm thông báo', route: 'NOTIF-01' }
          ].map((item) => `
            <button type="button" onclick="window.ArenaRouter.navigate('${item.route}')"
                    class="w-full px-4 py-3.5 flex items-center justify-between hover:bg-[#F8FAF3] text-left transition-colors">
              <div class="flex items-center gap-3">
                <span class="material-symbols-outlined text-[20px] text-[#3D6943]">${item.icon}</span>
                <span class="text-sm font-medium text-[#111827]">${item.label}</span>
              </div>
              <span class="material-symbols-outlined text-[18px] text-[#757D73]">chevron_right</span>
            </button>
          `).join('')}

          ${user.role === 'ADMIN' ? `
            <button type="button" onclick="window.ArenaRouter.navigate('ADM-01')"
                    class="w-full px-4 py-3.5 flex items-center justify-between hover:bg-[#EAF0E6] text-left bg-[#F8FAF3]">
              <div class="flex items-center gap-3">
                <span class="material-symbols-outlined text-[20px] text-[#14532D]">admin_panel_settings</span>
                <span class="text-sm font-bold text-[#14532D]">Khu vực Quản trị (Admin)</span>
              </div>
              <span class="material-symbols-outlined text-[18px] text-[#14532D]">chevron_right</span>
            </button>
          ` : ''}
        </div>

        <div class="bg-white rounded-[12px] border border-[#DDE5D9] divide-y divide-[#F1F5EC] overflow-hidden">
          <button type="button" onclick="window.ArenaUI.toast('Liên hệ hỗ trợ: support@arenaflow.vn', 'info')"
                  class="w-full px-4 py-3.5 flex items-center justify-between hover:bg-[#F8FAF3] text-left">
            <div class="flex items-center gap-3">
              <span class="material-symbols-outlined text-[20px] text-[#596158]">help</span>
              <span class="text-sm font-medium text-[#111827]">Hỗ trợ &amp; Điều khoản sử dụng</span>
            </div>
            <span class="material-symbols-outlined text-[18px] text-[#757D73]">chevron_right</span>
          </button>
          <button type="button" onclick="window.ArenaRouter.navigate('PROF-06')"
                  class="w-full px-4 py-3.5 flex items-center justify-between hover:bg-[#F8FAF3] text-left">
            <div class="flex items-center gap-3">
              <span class="material-symbols-outlined text-[20px] text-[#2D342C]">logout</span>
              <span class="text-sm font-semibold text-[#2D342C]">Đăng xuất</span>
            </div>
            <span class="material-symbols-outlined text-[18px] text-[#757D73]">chevron_right</span>
          </button>
          <button type="button" id="btn-prof03-del"
                  class="w-full px-4 py-3.5 flex items-center justify-between hover:bg-[#FEF2F2] text-left">
            <div class="flex items-center gap-3">
              <span class="material-symbols-outlined text-[20px] text-[#A73B21]">delete_forever</span>
              <span class="text-sm font-semibold text-[#A73B21]">Xoá tài khoản</span>
            </div>
            <span class="material-symbols-outlined text-[18px] text-[#A73B21]">chevron_right</span>
          </button>
        </div>
      </div>
    `;

    container.querySelector('#btn-prof03-del').addEventListener('click', () => {
      R().navigate(blockingTeams.length > 0 ? 'PROF-05' : 'S5');
    });
  }

  /* -------------------------------------------------------------------------
     S5 / PROF-04: Xác nhận xóa tài khoản (Material Symbols Outlined, no lucide)
     ------------------------------------------------------------------------- */
  function renderS5(container) {
    const user = S().getCurrentUser();
    if (!user) {
      R().navigate('S1');
      return;
    }
    const blockingTeams = getCaptainedBlockingTeams(user.id);

    container.innerHTML = `
      <div class="w-full flex-1 flex flex-col bg-[#F8FAF3] p-4 justify-center">
        <div class="bg-white rounded-[16px] border border-[#E5E7EB] p-5 shadow-md space-y-4">
          <div class="flex flex-col items-center text-center">
            <div class="w-14 h-14 rounded-full bg-[#FEF2F2] text-[#A73B21] flex items-center justify-center mb-3">
              <span class="material-symbols-outlined text-[28px]">warning</span>
            </div>
            <h2 class="text-xl font-bold text-[#111827] tracking-tight mb-2">Xoá tài khoản?</h2>
            <p class="text-[13.5px] leading-relaxed text-[#596158] text-left">
              Bạn sẽ bị đăng xuất và rời mọi đội đang tham gia. Các lời mời vào đội đang chờ sẽ bị từ chối. Ở lịch sử các đội, tên bạn sẽ hiển thị là “Người dùng đã xóa”.
            </p>
          </div>

          ${blockingTeams.length > 0 ? `
            <div class="bg-[#FEF2F2] border border-[#FECACA] rounded-[8px] p-3.5 flex items-start gap-2.5 text-left">
              <span class="material-symbols-outlined text-[18px] text-[#A73B21] shrink-0 mt-0.5">error</span>
              <p class="text-[13px] font-medium leading-snug text-[#A73B21]">
                Bạn phải chuyển quyền đội trưởng hoặc giải tán đội trước khi xóa tài khoản (422 USER_003).
              </p>
            </div>
          ` : ''}

          <div class="flex flex-col gap-2.5 pt-1">
            <button type="button" id="btn-s5-confirm"
                    class="w-full py-3 px-4 rounded-[8px] bg-[#A73B21] hover:bg-[#8F321B] text-white font-semibold text-sm transition-colors shadow-sm">
              ${blockingTeams.length > 0 ? 'Xem đội cần xử lý' : 'Xoá tài khoản'}
            </button>
            <button type="button" onclick="window.ArenaRouter.navigate('PROF-01')"
                    class="w-full py-3 px-4 rounded-[8px] border border-[#DDE5D9] bg-white hover:bg-[#F8FAF3] text-[#2D342C] font-semibold text-sm transition-colors">
              Huỷ
            </button>
          </div>
        </div>
      </div>
    `;

    container.querySelector('#btn-s5-confirm').addEventListener('click', () => {
      if (blockingTeams.length > 0) {
        R().navigate('PROF-05');
        return;
      }
      S().setState((st) => {
        const u = st.users[user.id];
        if (u) {
          u.isDeleted = true;
          u.displayName = 'Người dùng đã xóa';
        }
        st.teamMembers.forEach((m) => {
          if (m.userId === user.id && m.status === 'ACTIVE') {
            m.status = 'LEFT';
            m.leftAt = st.meta.mockClock;
          }
        });
        st.invitations.forEach((inv) => {
          if (inv.invitedUserId === user.id && inv.status === 'PENDING') {
            inv.status = 'DECLINED';
          }
        });
      });
      S().switchRole('Guest');
      UI().toast('Tài khoản đã được xóa thành công.', 'success');
      R().navigate('S1');
    });
  }

  /* -------------------------------------------------------------------------
     PROF-05: Chặn xóa khi còn là Đội trưởng (422 USER_003)
     ------------------------------------------------------------------------- */
  function renderProf05(container) {
    const user = S().getCurrentUser();
    const blockingTeams = user ? getCaptainedBlockingTeams(user.id) : [];

    container.innerHTML = `
      <div class="w-full flex-1 flex flex-col bg-[#F8FAF3] p-4 space-y-4">
        <div class="bg-[#FEF2F2] border border-[#FECACA] rounded-[12px] p-4 flex items-start gap-3">
          <span class="material-symbols-outlined text-[24px] text-[#A73B21] shrink-0">block</span>
          <div>
            <h2 class="font-bold text-sm text-[#A73B21]">Không thể xoá tài khoản (422 USER_003)</h2>
            <p class="text-xs text-[#7F1D1D] mt-1 leading-relaxed">
              Bạn đang làm đội trưởng của ${blockingTeams.length} đội chưa giải tán. Hãy chuyển quyền đội trưởng hoặc giải tán từng đội trước.
            </p>
          </div>
        </div>

        ${blockingTeams.length === 0 ? UI().emptyState({
          icon: 'check_circle',
          title: 'Đã đủ điều kiện xóa tài khoản',
          description: 'Bạn không còn giữ vai trò đội trưởng ở đội nào.',
          actionLabel: 'Tiếp tục xóa tài khoản',
          onAction: "window.ArenaRouter.navigate('S5')"
        }) : `
          <div class="space-y-3">
            ${blockingTeams.map((t) => `
              <div class="bg-white rounded-[8px] border border-[#DDE5D9] p-4 space-y-3 shadow-sm">
                <div class="flex items-center justify-between">
                  <div class="flex items-center gap-3">
                    ${UI().avatar(t, { size: 40 })}
                    <div>
                      <h3 class="font-bold text-sm text-[#111827]">${UI().esc(t.name)}</h3>
                      <p class="text-xs text-[#596158]">${t.status === 'INACTIVE' ? 'Đang tạm khoá' : 'Đang hoạt động'}</p>
                    </div>
                  </div>
                  ${UI().badge(t.status === 'INACTIVE' ? 'Bị khoá' : 'Hoạt động', t.status === 'INACTIVE' ? 'warning' : 'success')}
                </div>
                <div class="grid grid-cols-2 gap-2 pt-2 border-t border-[#F1F5EC]">
                  <button type="button" onclick="window.ArenaRouter.navigate('S10', { teamId: '${t.id}' })"
                          class="py-2 px-3 rounded-[8px] bg-[#3D6943] hover:bg-[#315C38] text-white text-xs font-semibold">
                    Chuyển quyền
                  </button>
                  <button type="button" onclick="window.ArenaRouter.navigate('S12', { teamId: '${t.id}' })"
                          class="py-2 px-3 rounded-[8px] border border-[#A73B21] text-[#A73B21] hover:bg-[#FEF2F2] text-xs font-semibold">
                    Giải tán đội
                  </button>
                </div>
              </div>
            `).join('')}
          </div>
        `}
      </div>
    `;
  }

  /* -------------------------------------------------------------------------
     PROF-06: Xác nhận đăng xuất
     ------------------------------------------------------------------------- */
  function renderProf06(container) {
    container.innerHTML = `
      <div class="w-full flex-1 flex flex-col bg-[#F8FAF3] p-4 justify-center">
        <div class="bg-white rounded-[16px] border border-[#E5E7EB] p-6 shadow-md text-center space-y-4">
          <div class="w-14 h-14 rounded-full bg-[#EAF0E6] text-[#3D6943] flex items-center justify-center mx-auto">
            <span class="material-symbols-outlined text-[28px]">logout</span>
          </div>
          <h2 class="text-lg font-bold text-[#111827]">Đăng xuất khỏi ArenaFlow?</h2>
          <p class="text-sm text-[#596158] leading-relaxed">
            Phiên đăng nhập hiện tại và bộ nhớ đệm của tài khoản sẽ được xóa an toàn trên thiết bị này.
          </p>
          <div class="flex flex-col gap-2.5 pt-2">
            <button type="button" id="btn-prof06-confirm"
                    class="w-full py-3 px-4 rounded-[8px] bg-[#3D6943] hover:bg-[#315C38] text-white font-semibold text-sm">
              Đăng xuất
            </button>
            <button type="button" onclick="window.ArenaRouter.back()"
                    class="w-full py-3 px-4 rounded-[8px] border border-[#DDE5D9] bg-white hover:bg-[#F8FAF3] text-[#2D342C] font-semibold text-sm">
              Huỷ
            </button>
          </div>
        </div>
      </div>
    `;

    container.querySelector('#btn-prof06-confirm').addEventListener('click', () => {
      S().switchRole('Guest');
      UI().toast('Đã đăng xuất thành công.', 'info');
      R().navigate('S1');
    });
  }

  /* -------------------------------------------------------------------------
     Register all routes with ArenaRouter
     ------------------------------------------------------------------------- */
  const router = global.ArenaRouter;
  if (router) {
    router.register('APP-01', renderApp01, { title: 'Khởi động', chrome: false, group: 'Khởi động' });
    router.register('APP-02', renderApp02, { title: 'Giới thiệu', chrome: false, group: 'Khởi động' });

    router.register('S1', renderS1, { title: 'Đăng nhập', chrome: false, group: 'Xác thực', aliases: ['AUTH-01'] });
    router.register('S2a', renderS2a, { title: 'Nhập số điện thoại', chrome: false, group: 'Xác thực', aliases: ['AUTH-02'] });
    router.register('S2b', renderS2b, { title: 'Xác thực OTP', chrome: false, group: 'Xác thực', aliases: ['AUTH-03'] });
    router.register('AUTH-04', renderAuth04, { title: 'Phân luồng sau đăng nhập', chrome: false, group: 'Xác thực' });
    router.register('AUTH-05', renderAuth05, { title: 'Tài khoản đã xóa', chrome: false, group: 'Xác thực' });
    router.register('AUTH-06', renderAuth06, { title: 'Phiên hết hạn', chrome: false, group: 'Xác thực' });

    router.register('PROF-01', renderProf01, { title: 'Hồ sơ cá nhân', tab: 'PROF-01', chrome: true, group: 'Hồ sơ', aliases: ['S3'] });
    router.register('S4', renderS4, { title: 'Sửa hồ sơ', tab: 'PROF-01', chrome: true, showBack: true, group: 'Hồ sơ', aliases: ['PROF-02'] });
    router.register('PROF-03', renderProf03, { title: 'Tài khoản & Cài đặt', tab: 'PROF-01', chrome: true, showBack: true, group: 'Hồ sơ' });
    router.register('S5', renderS5, { title: 'Xoá tài khoản', tab: 'PROF-01', chrome: true, showBack: true, group: 'Hồ sơ', aliases: ['PROF-04'] });
    router.register('PROF-05', renderProf05, { title: 'Chặn xóa tài khoản', tab: 'PROF-01', chrome: true, showBack: true, group: 'Hồ sơ' });
    router.register('PROF-06', renderProf06, { title: 'Đăng xuất', tab: 'PROF-01', chrome: true, showBack: true, group: 'Hồ sơ' });
  }
})(window);
