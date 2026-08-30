// Niramaya — shared front-end helpers
// (Chart setup and auth logic live inline in dashboard.html / login.html
// so each page stays easy to read on its own.)

document.addEventListener('DOMContentLoaded', () => {
  document.querySelectorAll('.chart-tabs button').forEach(btn => {
    btn.addEventListener('click', () => {
      btn.parentElement.querySelectorAll('button').forEach(b => b.classList.remove('active'));
      btn.classList.add('active');
    });
  });
});
