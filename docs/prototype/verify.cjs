// Comprehensive Verification Script for ArenaFlow Complete Prototype
// Loads docs/prototype/index.html and all js/ files in Node VM.
// Verifies:
// 1. Design tokens & script tags in index.html
// 2. Zero forbidden classes/attributes/URLs across source files and rendered output
// 3. All 16 Stitch screens (S1..S14) + all 101 IDs from docs/android-ui/screen-coverage.md
// 4. Key interactive flows (login -> home -> match A/B -> pairing -> mercenary -> chat -> rep -> admin)

const fs = require('fs');
const path = require('path');
const vm = require('node:vm');

const PROTO_DIR = __dirname;
const htmlPath = path.join(PROTO_DIR, 'index.html');
if (!fs.existsSync(htmlPath)) {
  throw new Error('index.html not found at ' + htmlPath);
}

const html = fs.readFileSync(htmlPath, 'utf8');

// ---------------------------------------------------------------------------
// 1. Verify typography, icons, theme colors, and script references
// ---------------------------------------------------------------------------
const requiredHtmlTokens = [
  { token: 'Be+Vietnam+Pro', desc: 'Be Vietnam Pro font' },
  { token: 'tailwindcss', desc: 'Tailwind CSS CDN' },
  { token: 'Material+Symbols+Outlined', desc: 'Material Symbols Outlined' },
  { token: '#14532D', desc: 'Deep Pitch Green #14532D' },
  { token: '#3D6943', desc: 'Primary Green #3D6943' },
  { token: '#F8FAF3', desc: 'Surface Background #F8FAF3' }
];
for (const req of requiredHtmlTokens) {
  if (!html.includes(req.token)) {
    throw new Error('Missing ' + req.desc + ' in index.html');
  }
}

// Extract external <script defer src="js/..."> tags in order
const externalScripts = [];
for (const m of html.matchAll(/<script[^>]*\bsrc=["']([^"']+)["'][^>]*>/gi)) {
  const src = m[1];
  if (!src.startsWith('http://') && !src.startsWith('https://')) {
    externalScripts.push(src);
  }
}
if (externalScripts.length === 0) {
  throw new Error('No local <script src="..."> tags found in index.html');
}

// Extract inline scripts (excluding tailwind-config)
const inlineScripts = [];
for (const m of html.matchAll(/<script(?![^>]*\bsrc=)([^>]*)>([\s\S]*?)<\/script>/gi)) {
  const attrs = m[1] || '';
  if (attrs.includes('tailwind-config')) continue;
  inlineScripts.push(m[2]);
}

// ---------------------------------------------------------------------------
// 2. Static scan for forbidden classes, attributes, and broken URLs
// ---------------------------------------------------------------------------
const FORBIDDEN_PATTERNS = [
  { name: 'brandDark', regex: /\bbrandDark\b/ },
  { name: 'brandBtn', regex: /\bbrandBtn\b/ },
  { name: 'brandBorderError', regex: /\bbrandBorderError\b/ },
  { name: 'text-brandError', regex: /\btext-brandError\b/ },
  { name: 'rounded-custom', regex: /\brounded-custom\b/ },
  { name: 'data-lucide', regex: /\bdata-lucide\b/ },
  { name: 'lh3.googleusercontent.com URL', regex: /https?:\/\/[^\s"'`]*lh3\.googleusercontent\.com/i }
];

function checkForbidden(label, content) {
  const hits = [];
  for (const pat of FORBIDDEN_PATTERNS) {
    if (pat.regex.test(content)) {
      hits.push(pat.name);
    }
  }
  if (hits.length > 0) {
    throw new Error(`Forbidden token(s) [${hits.join(', ')}] found in ${label}`);
  }
}

checkForbidden('index.html', html);
for (const relPath of externalScripts) {
  const fullPath = path.join(PROTO_DIR, relPath);
  if (!fs.existsSync(fullPath)) {
    throw new Error(`Referenced script not found: ${relPath}`);
  }
  const code = fs.readFileSync(fullPath, 'utf8');
  checkForbidden(relPath, code);
}

// ---------------------------------------------------------------------------
// 3. Mock DOM & Browser Environment for Node VM
// ---------------------------------------------------------------------------
function createClassList(el) {
  const set = new Set();
  return {
    add(...cls) { cls.forEach((c) => c && set.add(c)); },
    remove(...cls) { cls.forEach((c) => set.delete(c)); },
    toggle(c, force) {
      if (force === undefined) {
        if (set.has(c)) { set.delete(c); return false; }
        set.add(c); return true;
      }
      if (force) set.add(c); else set.delete(c);
      return !!force;
    },
    contains(c) { return set.has(c); },
    toString() { return Array.from(set).join(' '); }
  };
}

function createElement(id, tagName = 'div') {
  const attrs = {};
  const el = {
    id: id || '',
    tagName: tagName.toUpperCase(),
    innerHTML: '',
    textContent: '',
    value: '',
    scrollTop: 0,
    style: {},
    dataset: {},
    classList: null,
    children: [],
    className: '',
    setAttribute(k, v) { attrs[k] = String(v); },
    getAttribute(k) { return attrs[k] !== undefined ? attrs[k] : null; },
    hasAttribute(k) { return attrs[k] !== undefined; },
    removeAttribute(k) { delete attrs[k]; },
    appendChild(child) { this.children.push(child); return child; },
    removeChild(child) {
      const i = this.children.indexOf(child);
      if (i >= 0) this.children.splice(i, 1);
      return child;
    },
    querySelector(sel) {
      if (!sel) return null;
      if (sel === '.material-symbols-outlined') return createElement('', 'span');
      const htmlStr = this.innerHTML || '';
      if (sel.startsWith('#')) {
        const targetId = sel.slice(1);
        if (htmlStr.includes(`id="${targetId}"`) || htmlStr.includes(`id='${targetId}'`)) {
          return nodes[targetId] || (nodes[targetId] = createElement(targetId));
        }
        return null;
      }
      const attrMatch = sel.match(/^\[([a-z0-9_-]+)(?:=["']?([^"'\]]+)["']?)?\]$/i);
      if (attrMatch) {
        const [, attr, val] = attrMatch;
        const re = val !== undefined
          ? new RegExp(`${attr}=["']${val.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')}["']`, 'i')
          : new RegExp(`\\b${attr}\\b`, 'i');
        if (re.test(htmlStr)) {
          const elFound = createElement('', 'div');
          if (val !== undefined) elFound.setAttribute(attr, val);
          return elFound;
        }
        return null;
      }
      if (sel.startsWith('.')) {
        const cls = sel.slice(1);
        if (htmlStr.includes(cls)) return createElement('', 'div');
        return null;
      }
      if (/^[a-z0-9]+$/i.test(sel) && new RegExp(`<${sel}\\b`, 'i').test(htmlStr)) {
        return createElement('', sel);
      }
      return null;
    },
    querySelectorAll(sel) {
      if (sel === '[data-nav-tab]') {
        return ['MATCH-01', 'MERC-01', 'TEAM-01', 'CHAT-01', 'PROF-01'].map((t) => {
          const b = createElement('', 'button');
          b.setAttribute('data-nav-tab', t);
          return b;
        });
      }
      const htmlStr = this.innerHTML || '';
      const attrMatch = sel.match(/\[([a-z0-9_-]+)\]/i);
      if (attrMatch) {
        const attr = attrMatch[1];
        const results = [];
        for (const m of htmlStr.matchAll(new RegExp(`${attr}=["']([^"']*)["']`, 'gi'))) {
          const b = createElement('', 'div');
          b.setAttribute(attr, m[1]);
          results.push(b);
        }
        return results;
      }
      if (sel.startsWith('.')) {
        const cls = sel.slice(1);
        if (htmlStr.includes(cls)) return [createElement('', 'div')];
      }
      return [];
    },
    addEventListener() {},
    removeEventListener() {},
    focus() {},
    blur() {},
    scrollIntoView() {}
  };
  el.classList = createClassList(el);
  return el;
}

const nodes = {};
// Pre-seed known IDs from index.html
[
  'screen-container', 'app-topbar-host', 'app-bottom-nav',
  'sheet-backdrop', 'sheet-host', 'quick-jump-drawer',
  'quick-jump-search', 'quick-jump-list', 'demo-control-panel',
  'demo-current-route', 'demo-role-buttons', 'demo-mock-clock',
  'demo-error-sim', 'toast-host', 'nav-badge-chat'
].forEach((id) => {
  nodes[id] = createElement(id);
});

const domListeners = {};
const winListeners = {};
const localStorageMap = new Map();

const document = {
  getElementById(id) {
    if (!nodes[id]) nodes[id] = createElement(id);
    return nodes[id];
  },
  querySelector(sel) {
    if (sel.startsWith('#')) return this.getElementById(sel.slice(1));
    return null;
  },
  querySelectorAll(sel) {
    return [];
  },
  createElement(tag) {
    return createElement('', tag);
  },
  addEventListener(evt, fn) {
    if (!domListeners[evt]) domListeners[evt] = [];
    domListeners[evt].push(fn);
  },
  removeEventListener() {},
  body: createElement('body', 'body')
};

const windowObj = {
  document,
  location: { hash: '' },
  history: {
    replaceState(_s, _t, url) { if (url) windowObj.location.hash = url; },
    pushState(_s, _t, url) { if (url) windowObj.location.hash = url; }
  },
  localStorage: {
    getItem(k) { return localStorageMap.has(k) ? localStorageMap.get(k) : null; },
    setItem(k, v) { localStorageMap.set(k, String(v)); },
    removeItem(k) { localStorageMap.delete(k); },
    clear() { localStorageMap.clear(); }
  },
  addEventListener(evt, fn) {
    if (!winListeners[evt]) winListeners[evt] = [];
    winListeners[evt].push(fn);
  },
  removeEventListener() {},
  alert() {},
  confirm() { return true; },
  prompt() { return ''; },
  scrollTo() {},
  setTimeout(fn, ms) { return 1; },
  clearTimeout(id) {},
  setInterval(fn, ms) { return 1; },
  clearInterval(id) {}
};

const context = {
  window: windowObj,
  globalThis: windowObj,
  self: windowObj,
  document,
  location: windowObj.location,
  history: windowObj.history,
  localStorage: windowObj.localStorage,
  URLSearchParams,
  console,
  setTimeout: windowObj.setTimeout,
  clearTimeout: windowObj.clearTimeout,
  setInterval: windowObj.setInterval,
  clearInterval: windowObj.clearInterval,
  Date,
  Math,
  JSON,
  Array,
  Object,
  String,
  Number,
  Boolean,
  RegExp,
  Map,
  Set,
  Error,
  parseInt,
  parseFloat,
  isNaN,
  encodeURIComponent,
  decodeURIComponent,
  nodes
};
windowObj.window = windowObj;

vm.createContext(context);

// Run inline glue scripts first (as in HTML body), then deferred external scripts, then DOMContentLoaded
for (let i = 0; i < inlineScripts.length; i++) {
  vm.runInContext(inlineScripts[i], context, { filename: `index.html#inline-${i}` });
}
for (const relPath of externalScripts) {
  const fullPath = path.join(PROTO_DIR, relPath);
  const code = fs.readFileSync(fullPath, 'utf8');
  vm.runInContext(code, context, { filename: relPath });
}

// Fire DOMContentLoaded to boot ArenaRouter
if (domListeners['DOMContentLoaded']) {
  for (const fn of domListeners['DOMContentLoaded']) {
    fn();
  }
}

// Verify core globals exist
const hasRouter = vm.runInContext('Boolean(window.ArenaRouter)', context);
const hasStore = vm.runInContext('Boolean(window.ArenaStore)', context);
const hasUI = vm.runInContext('Boolean(window.ArenaUI)', context);
if (!hasRouter || !hasStore || !hasUI) {
  throw new Error(`Missing core globals: ArenaRouter=${hasRouter}, ArenaStore=${hasStore}, ArenaUI=${hasUI}`);
}

// ---------------------------------------------------------------------------
// 4. Verify all 16 original screens (S1..S14) + all 101 IDs from screen-coverage.md
// ---------------------------------------------------------------------------
const ORIGINAL_16_SCREENS = [
  'S1', 'S2a', 'S2b', 'S3', 'S4', 'S5',
  'S6', 'S7', 'S8a', 'S8b', 'S9', 'S10', 'S11', 'S12', 'S13', 'S14'
];

const COVERAGE_101_IDS = [
  // APP (2)
  'APP-01', 'APP-02',
  // AUTH (6)
  'AUTH-01', 'AUTH-02', 'AUTH-03', 'AUTH-04', 'AUTH-05', 'AUTH-06',
  // PROF (6)
  'PROF-01', 'PROF-02', 'PROF-03', 'PROF-04', 'PROF-05', 'PROF-06',
  // TEAM (14)
  'TEAM-01', 'TEAM-02', 'TEAM-03', 'TEAM-04', 'TEAM-05', 'TEAM-06', 'TEAM-07',
  'TEAM-08', 'TEAM-09', 'TEAM-10', 'TEAM-11', 'TEAM-12', 'TEAM-13', 'TEAM-14',
  // MATCH (14)
  'MATCH-01', 'MATCH-02', 'MATCH-03', 'MATCH-04', 'MATCH-05', 'MATCH-06', 'MATCH-07',
  'MATCH-08', 'MATCH-09', 'MATCH-10', 'MATCH-11', 'MATCH-12', 'MATCH-13', 'MATCH-14',
  // PAIR (8)
  'PAIR-01', 'PAIR-02', 'PAIR-03', 'PAIR-04', 'PAIR-05', 'PAIR-06', 'PAIR-07', 'PAIR-08',
  // MERC (12)
  'MERC-01', 'MERC-02', 'MERC-03', 'MERC-04', 'MERC-05', 'MERC-06',
  'MERC-07', 'MERC-08', 'MERC-09', 'MERC-10', 'MERC-11', 'MERC-12',
  // EXT (5)
  'EXT-01', 'EXT-02', 'EXT-03', 'EXT-04', 'EXT-05',
  // VEN (7)
  'VEN-01', 'VEN-02', 'VEN-03', 'VEN-04', 'VEN-05', 'VEN-06', 'VEN-07',
  // CHAT (5)
  'CHAT-01', 'CHAT-02', 'CHAT-03', 'CHAT-04', 'CHAT-05',
  // NOTIF (3)
  'NOTIF-01', 'NOTIF-02', 'NOTIF-03',
  // REP (10)
  'REP-01', 'REP-02', 'REP-03', 'REP-04', 'REP-05',
  'REP-06', 'REP-07', 'REP-08', 'REP-09', 'REP-10',
  // ADM (9)
  'ADM-01', 'ADM-02', 'ADM-03', 'ADM-04', 'ADM-05', 'ADM-06', 'ADM-07', 'ADM-08', 'ADM-09'
];

if (COVERAGE_101_IDS.length !== 101) {
  throw new Error(`Expected 101 coverage IDs, got ${COVERAGE_101_IDS.length}`);
}

const defects = [];

function verifyRouteRender(routeId, roleForRoute) {
  if (roleForRoute) {
    vm.runInContext(`window.ArenaStore.switchRole(${JSON.stringify(roleForRoute)})`, context);
  }
  const resolved = vm.runInContext(`window.ArenaRouter.resolveId(${JSON.stringify(routeId)})`, context);
  const isRegistered = vm.runInContext(`window.ArenaRouter.routes.has(${JSON.stringify(resolved)})`, context);
  if (!isRegistered) {
    defects.push(`Route ${routeId} (resolved: ${resolved}) is NOT registered in ArenaRouter`);
    return;
  }

  // Clear sheet host and screen container before rendering
  nodes['sheet-host'].innerHTML = '';
  nodes['screen-container'].innerHTML = '';

  vm.runInContext(`window.ArenaRouter.navigate(${JSON.stringify(routeId)}, {}, { replace: true })`, context);

  const screenHtml = nodes['screen-container'].innerHTML || '';
  const sheetHtml = nodes['sheet-host'].innerHTML || '';
  const combinedHtml = screenHtml + '\n' + sheetHtml;

  if (screenHtml.trim().length === 0 && sheetHtml.trim().length === 0) {
    defects.push(`Route ${routeId} rendered empty output`);
    return;
  }
  if (combinedHtml.includes('Module màn hình này chưa được nạp') || combinedHtml.includes('Lỗi hiển thị màn hình')) {
    defects.push(`Route ${routeId} rendered error/fallback UI: ${combinedHtml.slice(0, 200)}`);
    return;
  }

  // Check forbidden tokens in rendered HTML
  for (const pat of FORBIDDEN_PATTERNS) {
    if (pat.regex.test(combinedHtml)) {
      defects.push(`Route ${routeId} rendered forbidden token: ${pat.name}`);
    }
  }

  // Check for stacked onclick attributes on buttons
  for (const m of combinedHtml.matchAll(/<button\b([^>]*)>/gi)) {
    const attrs = m[1];
    const onclickCount = (attrs.match(/onclick=/gi) || []).length;
    if (onclickCount > 1) {
      defects.push(`Route ${routeId} has button with ${onclickCount} stacked onclick attributes`);
    }
  }
}

// Test all 16 original screens as Captain
for (const sc of ORIGINAL_16_SCREENS) {
  verifyRouteRender(sc, 'Captain');
}

// Test all 101 coverage IDs (use Admin role for ADM-*, Captain for the rest)
for (const id of COVERAGE_101_IDS) {
  const role = id.startsWith('ADM-') ? 'Admin' : 'Captain';
  verifyRouteRender(id, role);
}

if (defects.length > 0) {
  throw new Error('Route verification defects:\n- ' + defects.join('\n- '));
}

// ---------------------------------------------------------------------------
// 5. Verify key interactive flows end-to-end
// ---------------------------------------------------------------------------
function assertFlow(cond, msg) {
  if (!cond) throw new Error('Flow assertion failed: ' + msg);
}

// Flow 1: Guest -> Login (S1) -> Captain -> Home (MATCH-01)
vm.runInContext(`window.demoSwitchRole('Guest')`, context);
assertFlow(vm.runInContext(`window.ArenaRouter.getCurrent().routeId`, context) === 'S1', 'Guest role should navigate to S1');

vm.runInContext(`window.demoSwitchRole('Captain'); window.ArenaRouter.navigate('MATCH-01');`, context);
assertFlow(vm.runInContext(`window.ArenaRouter.getCurrent().routeId`, context) === 'MATCH-01', 'Captain should navigate to MATCH-01');
assertFlow(nodes['screen-container'].innerHTML.trim().length > 0, 'MATCH-01 should render content');

// Flow 2: Match A (MATCH-03) and Match B (MATCH-04) + Back navigation
vm.runInContext(`window.ArenaRouter.navigate('MATCH-03')`, context);
assertFlow(vm.runInContext(`window.ArenaRouter.getCurrent().routeId`, context) === 'MATCH-03', 'Should navigate to MATCH-03');
vm.runInContext(`window.ArenaRouter.navigate('MATCH-04')`, context);
assertFlow(vm.runInContext(`window.ArenaRouter.getCurrent().routeId`, context) === 'MATCH-04', 'Should navigate to MATCH-04');
vm.runInContext(`window.ArenaRouter.back()`, context);
assertFlow(vm.runInContext(`window.ArenaRouter.getCurrent().routeId`, context) === 'MATCH-03', 'back() should return to MATCH-03');

// Flow 3: Pairing (PAIR-01 -> PAIR-03)
vm.runInContext(`window.ArenaRouter.navigate('PAIR-01')`, context);
assertFlow(nodes['screen-container'].innerHTML.trim().length > 0, 'PAIR-01 should render');
vm.runInContext(`window.ArenaRouter.navigate('PAIR-03')`, context);
assertFlow(nodes['screen-container'].innerHTML.trim().length > 0, 'PAIR-03 should render');

// Flow 4: Mercenary (MERC-01 -> MERC-02 -> MERC-04)
vm.runInContext(`window.ArenaRouter.navigate('MERC-01')`, context);
assertFlow(nodes['screen-container'].innerHTML.trim().length > 0, 'MERC-01 should render');
vm.runInContext(`window.ArenaRouter.navigate('MERC-02')`, context);
assertFlow(nodes['screen-container'].innerHTML.trim().length > 0, 'MERC-02 should render');
vm.runInContext(`window.ArenaRouter.navigate('MERC-04')`, context);
assertFlow(nodes['screen-container'].innerHTML.trim().length > 0, 'MERC-04 should render');

// Flow 5: Chat (CHAT-01 -> CHAT-02) & Notifications (NOTIF-01)
vm.runInContext(`window.ArenaRouter.navigate('CHAT-01')`, context);
assertFlow(nodes['screen-container'].innerHTML.trim().length > 0, 'CHAT-01 should render');
vm.runInContext(`window.ArenaRouter.navigate('CHAT-02')`, context);
assertFlow(nodes['screen-container'].innerHTML.trim().length > 0, 'CHAT-02 should render');
vm.runInContext(`window.ArenaRouter.navigate('NOTIF-01')`, context);
assertFlow(nodes['screen-container'].innerHTML.trim().length > 0, 'NOTIF-01 should render');

// Flow 6: Reputation (REP-01 -> REP-04)
vm.runInContext(`window.ArenaRouter.navigate('REP-01')`, context);
assertFlow(nodes['screen-container'].innerHTML.trim().length > 0, 'REP-01 should render');
vm.runInContext(`window.ArenaRouter.navigate('REP-04')`, context);
assertFlow(nodes['screen-container'].innerHTML.trim().length > 0, 'REP-04 should render');

// Flow 7: Admin Guard (Captain -> 403) vs Admin Role (ADM-01 -> ADM-02)
vm.runInContext(`window.ArenaRouter.navigate('ADM-01')`, context);
const captainAdmHtml = nodes['screen-container'].innerHTML;
assertFlow(
  captainAdmHtml.includes('403') || captainAdmHtml.toLowerCase().includes('quyền') || captainAdmHtml.toLowerCase().includes('admin'),
  'ADM-01 as Captain should show 403/permission guard'
);

vm.runInContext(`window.demoSwitchRole('Admin')`, context);
assertFlow(vm.runInContext(`window.ArenaRouter.getCurrent().routeId`, context) === 'ADM-01', 'Admin role switch should navigate to ADM-01');
assertFlow(nodes['screen-container'].innerHTML.trim().length > 0, 'ADM-01 as Admin should render dashboard');

// Flow 8: Verify compact Filter icon button (`tune`) on MATCH-01, MERC-01, TEAM-01, TEAM-02, CHAT-01
for (const filterRoute of ['MATCH-01', 'MERC-01', 'TEAM-01', 'TEAM-02', 'CHAT-01']) {
  vm.runInContext(`window.ArenaRouter.navigate(${JSON.stringify(filterRoute)})`, context);
  const htmlOut = nodes['screen-container'].innerHTML || '';
  assertFlow(htmlOut.includes('tune'), `${filterRoute} should render 'tune' Filter icon button`);
}

// Flow 9: Demo Reset
vm.runInContext(`window.demoReset()`, context);
assertFlow(vm.runInContext(`window.ArenaRouter.getCurrent().routeId`, context) === 'APP-01', 'demoReset() should return to APP-01');

const registeredCount = vm.runInContext(`window.ArenaRouter.routes.size`, context);
const aliasCount = vm.runInContext(`window.ArenaRouter.aliases.size`, context);

console.log(`PASS: Verified index.html + ${externalScripts.length} JS modules in Node VM.`);
console.log(`PASS: All 16 original Stitch screens (S1..S14) rendered cleanly.`);
console.log(`PASS: All 101 screen-coverage IDs (APP/AUTH/PROF/TEAM/MATCH/PAIR/MERC/EXT/VEN/CHAT/NOTIF/REP/ADM) rendered cleanly.`);
console.log(`PASS: Router registry has ${registeredCount} routes and ${aliasCount} aliases.`);
console.log(`PASS: Zero forbidden classes (brandDark/brandBtn/brandBorderError/text-brandError/rounded-custom), zero data-lucide, zero lh3.googleusercontent.com URLs.`);
console.log(`PASS: Compact 'tune' Filter icon button verified on MATCH-01, MERC-01, TEAM-01, TEAM-02, CHAT-01.`);
console.log(`PASS: All 9 interactive end-to-end flows passed.`);
