/**
 * Niramaya Smart Hospital Management System
 * Live Clock, Session Duration Tracker & System Activity Logger Component
 */

(function () {
    // 1. Session Initialization
    if (!localStorage.getItem("niramaya_session_start")) {
        localStorage.setItem("niramaya_session_start", new Date().toISOString());
    }

    const sessionStart = new Date(localStorage.getItem("niramaya_session_start"));

    // 2. Initialize Live Clock Banner on DOM Load
    document.addEventListener("DOMContentLoaded", () => {
        injectLiveClockWidget();
        initLiveTimer();
        bindActivityTriggers();
    });

    function injectLiveClockWidget() {
        if (document.getElementById("niramaya-live-clock-bar")) return;

        const clockBar = document.createElement("div");
        clockBar.id = "niramaya-live-clock-bar";
        clockBar.className = "live-clock-bar";
        clockBar.style.cssText = `
            position: fixed;
            bottom: 0;
            left: 0;
            right: 0;
            width: 100%;
            background: linear-gradient(135deg, rgba(15, 23, 42, 0.95), rgba(30, 41, 59, 0.95));
            color: #f8fafc;
            padding: 8px 18px;
            font-family: 'Inter', system-ui, -apple-system, sans-serif;
            font-size: 13px;
            display: flex;
            justify-content: space-between;
            align-items: center;
            border-top: 1px solid rgba(255, 255, 255, 0.1);
            backdrop-filter: blur(8px);
            z-index: 99999;
            box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.15);
        `;

        const loggedInUser = localStorage.getItem("loggedInUser") || localStorage.getItem("patientName") || localStorage.getItem("doctorName") || "Active User";
        const userRole = localStorage.getItem("userRole") || "System User";
        const sessionStartFormatted = sessionStart.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', second: '2-digit' });

        clockBar.innerHTML = `
            <div style="display:flex; align-items:center; gap:16px;">
                <span style="font-weight:600; color:#38bdf8; display:flex; align-items:center; gap:6px;">
                    <svg width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><circle cx="12" cy="12" r="10"/><path d="M12 6v6l4 2"/></svg>
                    Live Clock: <span id="niramaya-clock-time" style="color:#ffffff; font-family:monospace; margin-left:4px;">--:--:--</span>
                </span>
                <span style="color:rgba(255,255,255,0.4);">|</span>
                <span style="color:#cbd5e1;">Logged In: <strong style="color:#e2e8f0;">${loggedInUser}</strong> (<span style="color:#94a3b8; text-transform:capitalize;">${userRole}</span>)</span>
            </div>
            <div style="display:flex; align-items:center; gap:16px;">
                <span style="color:#cbd5e1;">Session Started: <span style="color:#f1f5f9;">${sessionStartFormatted}</span></span>
                <span style="color:rgba(255,255,255,0.4);">|</span>
                <span style="font-weight:600; color:#4ade80; display:flex; align-items:center; gap:6px;">
                    <svg width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path d="M12 8v4l3 3"/><circle cx="12" cy="12" r="10"/></svg>
                    Session Duration: <span id="niramaya-session-duration" style="color:#ffffff; font-family:monospace; margin-left:4px;">00:00:00</span>
                </span>
            </div>
        `;

        // Prepend to body or target header
        document.body.insertBefore(clockBar, document.body.firstChild);
    }

    function initLiveTimer() {
        setInterval(() => {
            const now = new Date();

            // Update Current Clock Time
            const clockEl = document.getElementById("niramaya-clock-time");
            if (clockEl) {
                clockEl.textContent = now.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' }) + ' ' + now.toLocaleTimeString();
            }

            // Update Session Duration
            const durationEl = document.getElementById("niramaya-session-duration");
            if (durationEl) {
                const diffMs = now - sessionStart;
                const hours = Math.floor(diffMs / (1000 * 60 * 60));
                const mins = Math.floor((diffMs % (1000 * 60 * 60)) / (1000 * 60));
                const secs = Math.floor((diffMs % (1000 * 60)) / 1000);

                const pad = (n) => String(n).padStart(2, '0');
                durationEl.textContent = `${pad(hours)}:${pad(mins)}:${pad(secs)}`;
            }
        }, 1000);
    }

    // 3. Activity Logging Function
    window.logSystemActivity = function (module, action, status = "Success", details = {}) {
        const userId = localStorage.getItem("userId") || localStorage.getItem("patientId") || localStorage.getItem("doctorId") || "USER";
        const userName = localStorage.getItem("loggedInUser") || localStorage.getItem("patientName") || "User";
        const role = localStorage.getItem("userRole") || "User";

        fetch("/api/activity-logs", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                userId: userId,
                userName: userName,
                role: role,
                module: module,
                action: action,
                status: status,
                details: details
            })
        }).catch(err => console.error("Activity Log Error:", err));
    };

    // 4. Bind Automatic Activity Listeners for Downloads & Actions
    function bindActivityTriggers() {
        document.addEventListener("click", (e) => {
            const target = e.target.closest("button, a, .clickable-log");
            if (!target) return;

            const text = (target.innerText || target.getAttribute("title") || "").trim();
            if (text.includes("Download Prescription") || text.includes("Download Rx")) {
                window.logSystemActivity("Prescription", "Prescription Downloaded", "Success");
            } else if (text.includes("Download Lab Report") || text.includes("Download Report")) {
                window.logSystemActivity("Lab Module", "Lab Report Downloaded", "Success");
            } else if (text.includes("Download Invoice") || text.includes("Download Bill")) {
                window.logSystemActivity("Pharmacy", "Pharmacy Invoice Downloaded", "Success");
            } else if (text.includes("Book Appointment")) {
                window.logSystemActivity("Appointments", "Book Appointment Triggered", "Success");
            } else if (text.includes("Join Meeting") || text.includes("Join Consultation")) {
                window.logSystemActivity("Telemedicine", "Joined Online Consultation Meeting", "Success");
            } else if (text.includes("Logout") || text.includes("Sign Out")) {
                fetch("/api/logout", { method: "POST" }).finally(() => {
                    localStorage.removeItem("niramaya_session_start");
                });
            }
        });
    }

    // 5. Utility to Render Activity Log Tables Dynamically on Frontends
    window.renderActivityLogsTable = function (containerId, limit = 10) {
        const container = document.getElementById(containerId);
        if (!container) return;

        fetch(`/api/activity-logs?limit=${limit}`)
            .then(res => res.json())
            .then(data => {
                if (!data.success || !data.logs || data.logs.length === 0) {
                    container.innerHTML = `<div style="text-align:center; padding:15px; color:#94a3b8;">No activity logs recorded yet.</div>`;
                    return;
                }

                let html = `
                    <div style="overflow-x:auto;">
                        <table style="width:100%; border-collapse:collapse; font-size:12px; text-align:left;">
                            <thead>
                                <tr style="background:#f1f5f9; color:#475569; border-bottom:1px solid #e2e8f0;">
                                    <th style="padding:8px;">Timestamp</th>
                                    <th style="padding:8px;">User</th>
                                    <th style="padding:8px;">Role</th>
                                    <th style="padding:8px;">Module</th>
                                    <th style="padding:8px;">Action</th>
                                    <th style="padding:8px;">Status</th>
                                </tr>
                            </thead>
                            <tbody>
                `;

                data.logs.forEach(log => {
                    const statusBadge = log.status === "Success" 
                        ? `<span style="background:#dcfce7; color:#166534; padding:2px 6px; border-radius:4px; font-weight:600;">Success</span>`
                        : `<span style="background:#fee2e2; color:#991b1b; padding:2px 6px; border-radius:4px; font-weight:600;">${log.status}</span>`;

                    html += `
                        <tr style="border-bottom:1px solid #f1f5f9;">
                            <td style="padding:8px; font-family:monospace; color:#64748b;">${log.createdAt}</td>
                            <td style="padding:8px; font-weight:500;">${log.userName || log.userId}</td>
                            <td style="padding:8px; color:#475569;">${log.role}</td>
                            <td style="padding:8px; font-weight:600; color:#0284c7;">${log.module}</td>
                            <td style="padding:8px; color:#1e293b;">${log.action}</td>
                            <td style="padding:8px;">${statusBadge}</td>
                        </tr>
                    `;
                });

                html += `
                            </tbody>
                        </table>
                    </div>
                `;
                container.innerHTML = html;
            })
            .catch(err => {
                container.innerHTML = `<div style="color:#ef4444; padding:10px;">Failed to load activity logs.</div>`;
            });
    };
})();
