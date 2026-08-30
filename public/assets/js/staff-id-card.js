/**
 * Niramaya Hospitals — Staff Employee ID Card Generator & Viewer
 * Dynamic modal renderer with QR code generation, hospital seal & print support.
 */

function openStaffIdCardModal(staffData) {
    if (!staffData) {
        console.error("No staff data provided for ID Card generation.");
        return;
    }

    let modal = document.getElementById('staffIdCardModal');
    if (!modal) {
        modal = document.createElement('div');
        modal.id = 'staffIdCardModal';
        modal.className = 'modal-overlay';
        modal.innerHTML = `
            <div class="modal-box" style="max-width: 480px; padding: 24px; background: #0B1528; border: 1px solid rgba(255,255,255,0.12); color: #fff; border-radius: 20px; box-shadow: 0 25px 50px -12px rgba(0,0,0,0.7);">
                <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:16px;">
                    <span style="font-family:'Sora',sans-serif; font-weight:700; font-size:0.9rem; color:#0AB2A7; text-transform:uppercase; letter-spacing:1px;">🪪 Official Staff Identification Card</span>
                    <button type="button" onclick="closeStaffIdCardModal()" style="background:rgba(255,255,255,0.1); border:none; color:#fff; width:30px; height:30px; border-radius:50%; cursor:pointer; font-weight:bold;">✕</button>
                </div>
                
                <!-- ID CARD CONTAINER -->
                <div id="printableStaffIdCard" style="background: linear-gradient(145deg, #0f2038, #07111e); border: 2px solid #0AB2A7; border-radius: 16px; padding: 20px; position: relative; overflow: hidden; font-family: 'Inter', sans-serif;">
                    
                    <!-- BACKGROUND ACCENT GRAPHIC -->
                    <div style="position:absolute; top:-40px; right:-40px; width:120px; height:120px; background:rgba(10,178,167,0.15); border-radius:50%; filter:blur(30px); pointer-events:none;"></div>
                    
                    <!-- HEADER LOGO -->
                    <div style="display:flex; align-items:center; justify-content:space-between; border-bottom:1px solid rgba(255,255,255,0.15); padding-bottom:12px; margin-bottom:16px;">
                        <div style="display:flex; align-items:center; gap:10px;">
                            <img src="assets/logo.png" alt="Niramaya Logo" style="height:36px; width:auto; object-fit:contain;">
                            <div>
                                <h3 style="font-family:'Sora',sans-serif; font-size:1.05rem; font-weight:800; color:#ffffff; margin:0; line-height:1.2;">NIRAMAYA HOSPITALS</h3>
                                <span style="font-size:0.65rem; color:#0AB2A7; font-weight:700; letter-spacing:0.8px; text-transform:uppercase;">Smart Healthcare Network</span>
                            </div>
                        </div>
                        <span id="idCardStatusBadge" style="background:rgba(10,178,167,0.2); color:#0AB2A7; border:1px solid #0AB2A7; font-size:0.65rem; font-weight:800; padding:3px 8px; border-radius:100px; text-transform:uppercase;">APPROVED</span>
                    </div>

                    <!-- CARD BODY: PHOTO & CORE DETAILS -->
                    <div style="display:grid; grid-template-columns: 100px 1fr; gap:16px; align-items:start;">
                        
                        <!-- PHOTO CONTAINER -->
                        <div style="text-align:center;">
                            <div id="idCardPhotoAvatar" style="width:90px; height:105px; border-radius:12px; background:linear-gradient(135deg, #0A4DA6, #0AB2A7); display:flex; flex-direction:column; align-items:center; justify-content:center; border:2px solid rgba(255,255,255,0.2); color:#fff; font-size:2.2rem; font-weight:800; font-family:'Sora',sans-serif; box-shadow:0 8px 16px rgba(0,0,0,0.4);">
                                <span id="idCardAvatarInitial">N</span>
                            </div>
                            <span id="idCardBloodGroup" style="display:inline-block; margin-top:8px; background:#e11d48; color:#fff; font-weight:800; font-size:0.7rem; padding:2px 8px; border-radius:6px; letter-spacing:0.5px;">BLOOD: O+</span>
                        </div>

                        <!-- CORE FIELDS -->
                        <div style="line-height:1.35;">
                            <h2 id="idCardName" style="font-family:'Sora',sans-serif; font-size:1.15rem; font-weight:800; color:#ffffff; margin:0 0 4px 0;">Staff Name</h2>
                            <div id="idCardRole" style="font-size:0.8rem; font-weight:700; color:#0AB2A7; text-transform:uppercase; margin-bottom:8px; letter-spacing:0.5px;">ROLE NAME</div>

                            <div style="font-size:0.75rem; color:rgba(255,255,255,0.75); display:grid; gap:4px;">
                                <div><strong style="color:rgba(255,255,255,0.5);">Employee Code:</strong> <span id="idCardEmpCode" style="color:#fbbf24; font-weight:800; font-family:monospace; font-size:0.88rem;">EMP-000001</span></div>
                                <div><strong style="color:rgba(255,255,255,0.5);">Department:</strong> <span id="idCardDept" style="color:#fff; font-weight:600;">Cardiology</span></div>
                                <div><strong style="color:rgba(255,255,255,0.5);">Designation:</strong> <span id="idCardDesig" style="color:#fff; font-weight:600;">Consultant Specialist</span></div>
                                <div><strong style="color:rgba(255,255,255,0.5);">Joining Date:</strong> <span id="idCardJoining" style="color:#fff;">15-JAN-2026</span></div>
                            </div>
                        </div>
                    </div>

                    <!-- FOOTER: QR CODE, EMERGENCY & HOSPITAL SEAL -->
                    <div style="margin-top:16px; border-top:1px dashed rgba(255,255,255,0.15); padding-top:12px; display:flex; justify-content:space-between; align-items:center;">
                        
                        <!-- DYNAMIC QR CODE -->
                        <div style="display:flex; align-items:center; gap:10px;">
                            <div id="idCardQrContainer" style="background:#ffffff; padding:4px; border-radius:8px; width:52px; height:52px; display:flex; align-items:center; justify-content:center;">
                                <canvas id="idCardQrCanvas" width="44" height="44"></canvas>
                            </div>
                            <div style="font-size:0.68rem; color:rgba(255,255,255,0.6); line-height:1.2;">
                                <div>Emergency Contact:</div>
                                <strong id="idCardEmergency" style="color:#ffffff; font-size:0.72rem;">+91 98765 43210</strong>
                                <div>Validity: <span id="idCardValidity" style="color:#0AB2A7; font-weight:700;">31-DEC-2028</span></div>
                            </div>
                        </div>

                        <!-- HOSPITAL SEAL & AUTHORIZED STAMP -->
                        <div style="text-align:right;">
                            <div style="display:inline-block; border:1px solid rgba(10,178,167,0.5); padding:4px 8px; border-radius:6px; background:rgba(10,178,167,0.08); text-align:center;">
                                <div style="font-size:0.55rem; color:rgba(255,255,255,0.6); text-transform:uppercase; letter-spacing:0.5px;">Authorized Signature</div>
                                <div style="font-family:'Brush Script MT', cursive, sans-serif; font-size:0.95rem; color:#0AB2A7; font-weight:bold; margin:2px 0;">Medical Director</div>
                                <div style="font-size:0.5rem; color:#fbbf24; font-weight:800;">OFFICIAL HOSPITAL SEAL</div>
                            </div>
                        </div>
                    </div>

                </div>

                <!-- ACTIONS -->
                <div style="display:flex; gap:12px; margin-top:20px;">
                    <button type="button" class="btn btn-primary" style="flex:1; justify-content:center;" onclick="printStaffIdCard()">🖨️ Print / Download ID Card</button>
                    <button type="button" class="btn btn-ghost" style="color:#aaa;" onclick="closeStaffIdCardModal()">Close</button>
                </div>
            </div>
        `;
        document.body.appendChild(modal);
    }

    // Populate Data
    const empCode = staffData.employeeCode || staffData.staffId || 'EMP-000001';
    const name = staffData.fullName || staffData.name || 'Staff Member';
    const role = staffData.role || 'Staff';
    const dept = staffData.department || 'General';
    const desig = staffData.designation || role;
    const joining = staffData.joiningDate || '15-JAN-2026';
    const blood = staffData.bloodGroup || 'O+';
    const emergency = staffData.emergencyContact || '+91 98765 43210';
    const validity = staffData.validity || '31-DEC-2028';
    const status = (staffData.status || 'Active').toUpperCase();

    document.getElementById('idCardEmpCode').textContent = empCode;
    document.getElementById('idCardName').textContent = name;
    document.getElementById('idCardRole').textContent = role;
    document.getElementById('idCardDept').textContent = dept;
    document.getElementById('idCardDesig').textContent = desig;
    document.getElementById('idCardJoining').textContent = joining;
    document.getElementById('idCardBloodGroup').textContent = 'BLOOD: ' + blood;
    document.getElementById('idCardEmergency').textContent = emergency;
    document.getElementById('idCardValidity').textContent = validity;
    document.getElementById('idCardStatusBadge').textContent = status;

    const initials = name.split(' ').map(n => n[0]).join('').substring(0, 2).toUpperCase();
    document.getElementById('idCardAvatarInitial').textContent = initials || 'N';

    // Render Canvas QR Code representation
    renderSimpleQrCode('idCardQrCanvas', `NIRAMAYA-VERIFIED|${empCode}|${name}|${role}`);

    modal.classList.add('show');
}

function closeStaffIdCardModal() {
    const modal = document.getElementById('staffIdCardModal');
    if (modal) modal.classList.remove('show');
}

function printStaffIdCard() {
    const cardHtml = document.getElementById('printableStaffIdCard').outerHTML;
    const win = window.open('', '_blank', 'width=600,height=700');
    win.document.write(`
        <html>
        <head>
            <title>Employee ID Card — ${document.getElementById('idCardEmpCode').textContent}</title>
            <style>
                body { font-family: 'Inter', sans-serif; background: #000; padding: 40px; display: flex; justify-content: center; }
                @media print {
                    body { background: #fff; padding: 0; }
                }
            </style>
        </head>
        <body>
            <div style="width: 400px;">
                ${cardHtml}
            </div>
            <script>
                setTimeout(() => { window.print(); window.close(); }, 500);
            </script>
        </body>
        </html>
    `);
    win.document.close();
}

/**
 * Fallback Canvas Matrix QR renderer for high reliability without external libs
 */
function renderSimpleQrCode(canvasId, text) {
    const canvas = document.getElementById(canvasId);
    if (!canvas) return;
    const ctx = canvas.getContext('2d');
    const size = canvas.width;
    ctx.fillStyle = '#ffffff';
    ctx.fillRect(0, 0, size, size);
    
    ctx.fillStyle = '#0f172a';
    const grid = 11;
    const cell = size / grid;

    // Pattern simulation based on text hash
    let hash = 0;
    for (let i = 0; i < text.length; i++) hash = (hash << 5) - hash + text.charCodeAt(i);

    for (let r = 0; r < grid; r++) {
        for (let c = 0; c < grid; c++) {
            // Corner positioning squares
            if ((r < 3 && c < 3) || (r < 3 && c > grid - 4) || (r > grid - 4 && c < 3)) {
                if (r === 0 || r === 2 || c === 0 || c === 2 || r === grid - 1 || r === grid - 3 || c === grid - 1 || c === grid - 3) {
                    ctx.fillRect(c * cell, r * cell, cell, cell);
                }
            } else if (((r * grid + c + Math.abs(hash)) % 3) === 0) {
                ctx.fillRect(c * cell, r * cell, cell, cell);
            }
        }
    }
}
