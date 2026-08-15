const menuButton = document.querySelector('[data-menu-button]');
const navigation = document.querySelector('[data-nav]');
const header = document.querySelector('[data-header]');
const reduceMotion = matchMedia('(prefers-reduced-motion: reduce)').matches;
if (reduceMotion) document.querySelectorAll('.hero-video, .download-video').forEach((video) => video.pause());

function closeMenu() {
  menuButton?.setAttribute('aria-expanded', 'false');
  navigation?.classList.remove('open');
  document.body.classList.remove('menu-open');
}

menuButton?.addEventListener('click', () => {
  const opening = menuButton.getAttribute('aria-expanded') !== 'true';
  menuButton.setAttribute('aria-expanded', String(opening));
  navigation?.classList.toggle('open', opening);
  document.body.classList.toggle('menu-open', opening);
});
navigation?.querySelectorAll('a').forEach((link) => link.addEventListener('click', closeMenu));
addEventListener('keydown', (event) => { if (event.key === 'Escape') closeMenu(); });
addEventListener('scroll', () => header?.classList.toggle('scrolled', scrollY > 30), { passive: true });

const revealObserver = new IntersectionObserver((entries) => {
  entries.forEach((entry) => {
    if (!entry.isIntersecting) return;
    entry.target.classList.add('visible');
    entry.target.closest('[data-timeline-item]')?.classList.add('reached');
    revealObserver.unobserve(entry.target);
  });
}, { threshold: .2, rootMargin: '0px 0px -30px' });
document.querySelectorAll('[data-reveal]').forEach((element) => reduceMotion ? element.classList.add('visible') : revealObserver.observe(element));

const timeline = document.querySelector('[data-timeline]');
const timelineProgress = document.querySelector('[data-timeline-progress]');
let scrollQueued = false;
function updateScrollEffects() {
  scrollQueued = false;
  if (timeline && timelineProgress) {
    const rect = timeline.getBoundingClientRect();
    const start = innerHeight * .62;
    const progress = Math.max(0, Math.min(1, (start - rect.top) / Math.max(1, rect.height - innerHeight * .3)));
    timeline.style.setProperty('--line-progress', `${progress * 100}%`);
  }
  if (!reduceMotion && innerWidth > 767) {
    document.querySelectorAll('[data-parallax]').forEach((element) => {
      const rect = element.getBoundingClientRect();
      const offset = Math.max(-18, Math.min(18, (innerHeight / 2 - (rect.top + rect.height / 2)) * .035));
      element.style.translate = `0 ${offset}px`;
    });
  }
}
addEventListener('scroll', () => {
  if (!scrollQueued) { scrollQueued = true; requestAnimationFrame(updateScrollEffects); }
}, { passive: true });
addEventListener('resize', updateScrollEffects);
updateScrollEffects();

const cinematicVideos = document.querySelectorAll('[data-cinematic-video]');
const videoObserver = new IntersectionObserver((entries) => {
  entries.forEach((entry) => {
    const video = entry.target;
    if (entry.isIntersecting) {
      const source = video.querySelector('source[data-src]');
      if (source?.dataset.src) {
        source.src = source.dataset.src;
        delete source.dataset.src;
        video.load();
      }
      if (!reduceMotion) video.play().catch(() => {});
    } else {
      video.pause();
    }
  });
}, { rootMargin: '300px' });
cinematicVideos.forEach((video) => videoObserver.observe(video));

fetch('https://wiwymusic-admin.angelanda023.workers.dev/api/ota/releases', { headers: { Accept: 'application/json' } })
  .then((response) => response.ok ? response.json() : Promise.reject())
  .then((releases) => {
    const tag = Array.isArray(releases) ? releases[0]?.tag_name : null;
    if (tag) document.querySelectorAll('[data-release-label]').forEach((label) => { label.textContent = `Versión estable ${tag}`; });
  }).catch(() => {});
