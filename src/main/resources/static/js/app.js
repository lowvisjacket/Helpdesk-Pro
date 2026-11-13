// helpdesk-pro front-end behaviour: navigation, preferences, live ticket updates.
(function () {
  'use strict';
  var root = document.documentElement, body = document.body;
  var $ = function (s, el) { return (el || document).querySelector(s); };
  var $$ = function (s, el) { return Array.prototype.slice.call((el || document).querySelectorAll(s)); };
  var csrf = ($('meta[name="_csrf"]') || {}).content;
  var csrfHeader = ($('meta[name="_csrf_header"]') || {}).content;

  function post(url, params) {
    var headers = { 'Content-Type': 'application/x-www-form-urlencoded' };
    if (csrf && csrfHeader) headers[csrfHeader] = csrf;
    return fetch(url, { method: 'POST', headers: headers, body: new URLSearchParams(params), credentials: 'same-origin' });
  }

  // ----- Account menu, mobile navigation, dropdowns -----
  var menu = $('#menu'), acct = $('#acct');
  function setMenu(open) {
    if (!menu) return;
    menu.classList.toggle('open', open);
    acct.setAttribute('aria-expanded', open);
  }
  document.addEventListener('click', function (e) {
    var t = e.target;
    if (acct && t.closest('#acct')) { setMenu(!menu.classList.contains('open')); return; }
    if (menu && !t.closest('#menu')) setMenu(false);
    if (t.closest('[data-nav-toggle]')) body.classList.toggle('nav-open');
    $$('details.dd[open]').forEach(function (d) { if (!d.contains(t)) d.removeAttribute('open'); });

    var theme = t.closest('[data-theme]');
    if (theme) { setTheme(theme.dataset.theme); return; }

    // Whole table rows are clickable, but real links and buttons inside them keep working.
    var row = t.closest('tr[data-href]');
    if (row && !t.closest('a,button,input,select,label,form')) location.href = row.dataset.href;
  });
  document.addEventListener('keydown', function (e) {
    if (e.key !== 'Escape') return;
    setMenu(false);
    body.classList.remove('nav-open');
    $$('details.dd[open]').forEach(function (d) { d.removeAttribute('open'); });
  });
  document.addEventListener('submit', function (e) {
    var message = e.target.getAttribute('data-confirm');
    if (message && !window.confirm(message)) e.preventDefault();
  });

  // ----- Theme (light / dark / follow device), stored per user -----
  function markTheme(mode) {
    $$('[data-theme]').forEach(function (b) { b.setAttribute('aria-pressed', b.dataset.theme === mode); });
  }
  function setTheme(mode) {
    root.classList.remove('light', 'dark');
    if (mode !== 'system') root.classList.add(mode);
    markTheme(mode);
    post('/settings/preferences', { theme: mode });
  }
  markTheme(root.classList.contains('dark') ? 'dark' : root.classList.contains('light') ? 'light' : 'system');

  // ----- Timestamps are stored in UTC; show them in the viewer's own time zone and locale -----
  function localizeTimes(scope) {
    if (!window.Intl) return;
    $$('time[datetime]', scope).forEach(function (el) {
      var date = new Date(el.getAttribute('datetime'));
      if (isNaN(date)) return;
      var dateOnly = el.hasAttribute('data-date-only');
      el.textContent = new Intl.DateTimeFormat(undefined, dateOnly
        ? { dateStyle: 'medium' } : { dateStyle: 'medium', timeStyle: 'short' }).format(date);
      el.title = date.toLocaleString();
    });
  }
  localizeTimes(document);

  // ----- Notification sound: an original two-note tone made with the Web Audio API -----
  var muted = body.dataset.muted === 'true', interacted = false, audio = null;
  ['pointerdown', 'keydown'].forEach(function (type) {
    document.addEventListener(type, function () { interacted = true; }, { capture: true, passive: true });
  });
  $$('[data-sound]').forEach(function (box) {
    box.checked = !muted;
    box.addEventListener('change', function () {
      muted = !box.checked;
      $$('[data-sound]').forEach(function (other) { other.checked = !muted; });
      post('/settings/preferences', { soundMuted: muted });
      if (!muted) chime();
    });
  });
  function chime() {
    var Ctx = window.AudioContext || window.webkitAudioContext;
    // Browsers block audio until the user has interacted with the page.
    if (muted || !interacted || !Ctx) return;
    try {
      audio = audio || new Ctx();
      if (audio.state === 'suspended') audio.resume();
      [[659.25, 0], [880, 0.13]].forEach(function (note) {
        var osc = audio.createOscillator(), gain = audio.createGain(), start = audio.currentTime + note[1];
        osc.type = 'sine';
        osc.frequency.value = note[0];
        gain.gain.setValueAtTime(0.0001, start);
        gain.gain.exponentialRampToValueAtTime(0.12, start + 0.02);
        gain.gain.exponentialRampToValueAtTime(0.0001, start + 0.4);
        osc.connect(gain).connect(audio.destination);
        osc.start(start);
        osc.stop(start + 0.45);
      });
    } catch (ignored) { /* sound is optional */ }
  }

  // ----- Live updates: poll for changes, refresh the affected region, notify -----
  var since = Number(body.dataset.now || 0), toasts = $('#toasts');
  if (!since || !toasts) return;
  var POLL_MS = 15000, timer = null;

  function toast(change) {
    var el = document.createElement('div'), link = document.createElement('a');
    el.className = 'pop toast';
    el.setAttribute('role', 'status');
    el.appendChild(document.createTextNode(change.created ? 'New ticket: ' : 'Ticket updated: '));
    link.href = body.dataset.ticketBase + change.id;
    link.textContent = '#' + change.id + ' ' + change.title;
    el.appendChild(link);
    toasts.appendChild(el);
    setTimeout(function () { el.remove(); }, 8000);
  }

  // Re-render this page on the server and swap in only the live region, so role-based
  // filtering stays server-side and anything being typed elsewhere on the page is kept.
  function refreshRegion() {
    var region = $('[data-live]');
    if (!region) return;
    fetch(location.href, { credentials: 'same-origin', headers: { 'X-Requested-With': 'fetch' } })
      .then(function (r) { return r.ok ? r.text() : null; })
      .then(function (html) {
        if (!html) return;
        var fresh = new DOMParser().parseFromString(html, 'text/html').querySelector('[data-live]');
        if (fresh) { region.innerHTML = fresh.innerHTML; localizeTimes(region); }
      }).catch(function () {});
  }

  function poll() {
    if (document.hidden) return;
    fetch('/api/v1/tickets/changes?since=' + since, { credentials: 'same-origin' })
      .then(function (r) { return r.ok ? r.json() : null; })
      .then(function (data) {
        if (!data) return;
        since = data.now;
        if (!data.changes.length) return;
        var own = body.dataset.ticketId;
        var relevant = own ? data.changes.filter(function (c) { return String(c.id) === own; }) : data.changes;
        if (relevant.length || !own) refreshRegion();
        data.changes.slice(0, 3).forEach(toast);
        chime();
      }).catch(function () {});
  }
  timer = setInterval(poll, POLL_MS);
  document.addEventListener('visibilitychange', function () { if (!document.hidden) poll(); });
})();
