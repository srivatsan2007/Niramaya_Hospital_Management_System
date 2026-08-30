/**
 * Niramaya Hospital ERP - Universal Mobile & Multi-Device Responsive Controller
 * Provides sliding sidebar navigation drawers, backdrop overlays, touch interactions,
 * and responsive table wrappers across all portals (Patient, Admin, Doctor, Nurse, Pharmacy, Lab, Telemedicine).
 */

(function() {
  function initMobileResponsiveness() {
    // 1. Create or ensure Sidebar Backdrop Overlay
    let backdrop = document.querySelector('.sidebar-backdrop');
    if (!backdrop) {
      backdrop = document.createElement('div');
      backdrop.className = 'sidebar-backdrop';
      document.body.appendChild(backdrop);
    }

    // 2. Find Dashboard Sidebar
    const sidebar = document.querySelector('aside.sidebar, .sidebar, .nurse-sidebar');
    const topbar = document.querySelector('.topbar, .topnav, header.topbar, .nurse-header, header');

    if (sidebar && topbar) {
      // Check if hamburger button already exists
      let hamburger = document.querySelector('.mobile-menu-btn, .mobile-hamburger');
      if (!hamburger) {
        hamburger = document.createElement('button');
        hamburger.type = 'button';
        hamburger.className = 'mobile-menu-btn icon-btn';
        hamburger.setAttribute('aria-label', 'Toggle Navigation Menu');
        hamburger.innerHTML = `
          <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
            <line x1="3" y1="12" x2="21" y2="12"></line>
            <line x1="3" y1="6" x2="21" y2="6"></line>
            <line x1="3" y1="18" x2="21" y2="18"></line>
          </svg>
        `;
        hamburger.style.cssText = 'display: inline-flex; align-items: center; justify-content: center; cursor: pointer; border: 1px solid var(--border-color, #E2E8F0); background: var(--paper, #fff); border-radius: 8px; width: 40px; height: 40px; margin-right: 12px; z-index: 100; color: var(--text-main, #0F172A);';

        // Insert at start of topbar left group or topbar
        const topbarLeft = topbar.querySelector('.topbar-left, .topnav-left, .nurse-header-info') || topbar;
        topbarLeft.insertBefore(hamburger, topbarLeft.firstChild);
      }

      // Add close button inside sidebar header if not present
      if (!sidebar.querySelector('.sidebar-close-btn')) {
        const closeBtn = document.createElement('button');
        closeBtn.type = 'button';
        closeBtn.className = 'sidebar-close-btn';
        closeBtn.setAttribute('aria-label', 'Close Sidebar');
        closeBtn.innerHTML = '✕';
        closeBtn.style.cssText = 'display: none; position: absolute; top: 16px; right: 16px; background: rgba(255,255,255,0.15); color: #fff; border: none; border-radius: 50%; width: 32px; height: 32px; font-size: 16px; font-weight: bold; cursor: pointer; align-items: center; justify-content: center; z-index: 20;';
        
        const sidebarBrand = sidebar.querySelector('.sidebar-brand, .brand, .nurse-brand') || sidebar;
        sidebarBrand.style.position = 'relative';
        sidebarBrand.appendChild(closeBtn);

        closeBtn.addEventListener('click', () => {
          closeSidebar();
        });
      }

      function toggleSidebar() {
        const isOpen = sidebar.classList.contains('mobile-open');
        if (isOpen) {
          closeSidebar();
        } else {
          openSidebar();
        }
      }

      function openSidebar() {
        sidebar.classList.add('mobile-open');
        backdrop.classList.add('active');
        document.body.style.overflow = 'hidden';
      }

      function closeSidebar() {
        sidebar.classList.remove('mobile-open');
        backdrop.classList.remove('active');
        document.body.style.overflow = '';
      }

      hamburger.addEventListener('click', (e) => {
        e.stopPropagation();
        toggleSidebar();
      });

      backdrop.addEventListener('click', () => {
        closeSidebar();
      });

      // Close on sidebar item click on small screens
      sidebar.querySelectorAll('.nav-header, .sub-link, .menu-item, .nurse-nav-item, a').forEach(link => {
        link.addEventListener('click', () => {
          if (window.innerWidth <= 992) {
            // Small delay to allow tab switch
            setTimeout(closeSidebar, 150);
          }
        });
      });

      // Close sidebar on Escape key
      document.addEventListener('keydown', (e) => {
        if (e.key === 'Escape' && sidebar.classList.contains('mobile-open')) {
          closeSidebar();
        }
      });
    }

    // 3. Auto-wrap uncontained tables for horizontal touch swiping
    document.querySelectorAll('table').forEach(tbl => {
      const parent = tbl.parentElement;
      if (parent && !parent.classList.contains('table-container') && !parent.classList.contains('table-responsive')) {
        const wrapper = document.createElement('div');
        wrapper.className = 'table-container';
        parent.insertBefore(wrapper, tbl);
        wrapper.appendChild(tbl);
      }
    });

    // 4. Landing Page Mobile Nav Menu Toggle (if present on index.html)
    const landingNavToggle = document.querySelector('.landing-nav-toggle, #btnToggleMobileNav');
    const landingNavLinks = document.querySelector('.nav-links, #landingMobileNav');
    if (landingNavToggle && landingNavLinks) {
      landingNavToggle.addEventListener('click', () => {
        landingNavLinks.classList.toggle('mobile-open');
        backdrop.classList.toggle('active');
      });
    }
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', initMobileResponsiveness);
  } else {
    initMobileResponsiveness();
  }
})();
