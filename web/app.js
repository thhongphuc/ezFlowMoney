/**
 * ezFlowMoney - Anti-Budget & Burn-Rate Tracker
 * Full-featured Web Application
 * Local-First, Offline-capable, Vanilla JS Architecture
 */

// DEFAULT DEMO STATE
const DEFAULT_STATE = {
  isPrivacyMode: false,
  cycle: {
    totalIncome: 42000000,
    savingsTarget: 12000000,
    fixedBills: 17000000,
    variablePool: 13000000,
    month: 9,
    year: 2026,
    daysRemaining: 22,
    daysInMonth: 30
  },
  fixedBills: [
    { id: 'rent', name: 'Tiền thuê nhà', amount: 8000000, isPaid: true, dueDate: 'Hạn ngày 05 hàng tháng', icon: 'fa-house' },
    { id: 'tuition', name: 'Học phí 2 bé (Mầm non & Tiểu học)', amount: 7500000, isPaid: false, dueDate: 'Hạn ngày 10 hàng tháng', icon: 'fa-graduation-cap' },
    { id: 'utilities', name: 'Điện nước sinh hoạt (EVN & Cấp nước)', amount: 1500000, isPaid: false, dueDate: 'Hạn ngày 15 hàng tháng', icon: 'fa-bolt' }
  ],
  goal: {
    birthYear: 1994,
    targetAge: 40,
    targetAmount: 600000000,
    expectedAnnualReturn: 8.0
  },
  transactions: [
    { id: 1, source: 'VCB Digibank', note: 'Chuyển tiền thuê nhà tháng 09', amount: 8000000, time: '29/09 07:30', isBank: true, bucket: 'FIXED' },
    { id: 2, source: 'Techcombank', note: 'Mua 1 chỉ vàng nhẫn 9999 PNJ', amount: 8200000, time: '28/09 15:40', isBank: true, bucket: 'SAVINGS' },
    { id: 3, source: 'Techcombank', note: 'Siêu thị WinMart mua rau quả gia đình', amount: 210000, time: '28/09 18:30', isBank: true, bucket: 'VARIABLE' },
    { id: 4, source: 'VCB Digibank', note: 'Cafe sáng trao đổi công việc', amount: 65000, time: '29/09 08:15', isBank: true, bucket: 'VARIABLE' },
    { id: 5, source: 'Tiền mặt', note: 'Bánh mì & sữa ăn sáng 2 con', amount: 55000, time: '27/09 07:10', isBank: false, bucket: 'VARIABLE' }
  ],
  assets: [
    { id: 1, bucket: 'BANK_SAVINGS', name: 'Sổ tiết kiệm VCB 6 tháng (5.2%/năm)', units: 1, unitPrice: 120000000, total: 120000000, label: 'Sổ' },
    { id: 2, bucket: 'GOLD_9999', name: 'Vàng nhẫn trơn 9999 PNJ', units: 6, unitPrice: 8200000, total: 49200000, label: 'Chỉ' },
    { id: 3, bucket: 'FUND_NAV', name: 'Chứng chỉ quỹ Dragon Capital DCDS', units: 1850, unitPrice: 28400, total: 52540000, label: 'CCQ' }
  ]
};

// STORAGE MANAGER
class StorageManager {
  static STORAGE_KEY = 'ezflowmoney_data_v1';

  static load() {
    try {
      const data = localStorage.getItem(this.STORAGE_KEY);
      if (data) {
        return JSON.parse(data);
      }
    } catch (e) {
      console.error('Lỗi đọc dữ liệu từ LocalStorage', e);
    }
    return JSON.parse(JSON.stringify(DEFAULT_STATE));
  }

  static save(state) {
    try {
      localStorage.setItem(this.STORAGE_KEY, JSON.stringify(state));
      if (typeof triggerCloudSync === 'function') {
        triggerCloudSync();
      }
    } catch (e) {
      console.error('Lỗi lưu LocalStorage', e);
    }
  }

  static exportJSON(state) {
    const dataStr = "data:text/json;charset=utf-8," + encodeURIComponent(JSON.stringify(state, null, 2));
    const downloadAnchor = document.createElement('a');
    downloadAnchor.setAttribute("href", dataStr);
    downloadAnchor.setAttribute("download", `ezflowmoney_backup_${new Date().toISOString().slice(0, 10)}.json`);
    document.body.appendChild(downloadAnchor);
    downloadAnchor.click();
    downloadAnchor.remove();
  }

  static reset() {
    localStorage.removeItem(this.STORAGE_KEY);
    return JSON.parse(JSON.stringify(DEFAULT_STATE));
  }
}

// APPLICATION STATE
let state = StorageManager.load();
let currentScreen = 'dashboard';
let currentEditingTxId = null;

// FORMAT UTILITIES
function formatVnd(num) {
  if (state.isPrivacyMode) return '•••••• ₫';
  return new Intl.NumberFormat('vi-VN').format(Math.round(num)) + ' ₫';
}

function formatVndShort(num) {
  if (state.isPrivacyMode) return '•••••• ₫';
  if (num >= 1000000000) return (num / 1000000000).toFixed(1) + ' Tỷ';
  if (num >= 1000000) return (num / 1000000).toFixed(1) + ' Tr';
  if (num >= 1000) return (num / 1000).toFixed(0) + ' K';
  return Math.round(num) + ' ₫';
}

function formatRawVnd(num) {
  return new Intl.NumberFormat('vi-VN').format(Math.round(num)) + ' ₫';
}

// BURN-RATE SAFE-TO-SPEND ENGINE
function calculateBurnRate() {
  // Sum spent in VARIABLE bucket
  const spentVariable = state.transactions
    .filter(t => t.bucket === 'VARIABLE')
    .reduce((sum, t) => sum + t.amount, 0);

  const remainingPool = Math.max(0, state.cycle.variablePool - spentVariable);
  const days = Math.max(1, state.cycle.daysRemaining);
  const dailySafe = remainingPool <= 0 ? 0 : remainingPool / days;
  const initialDaily = state.cycle.variablePool / state.cycle.daysInMonth;
  const burnPct = Math.min(100, (spentVariable / state.cycle.variablePool) * 100);

  let status = 'SAFE';
  let label = 'An toàn';
  let badgeClass = 'status-badge';
  let iconClass = 'fa-solid fa-circle-check';
  let message = 'Hạn mức tối đa bạn có thể thoải mái chi tiêu hôm nay mà không lo thâm hụt ngân sách.';

  if (spentVariable >= state.cycle.variablePool || dailySafe <= 0) {
    status = 'DANGER';
    label = 'Bội chi';
    badgeClass = 'status-badge danger';
    iconClass = 'fa-solid fa-circle-exclamation';
    message = 'Cảnh báo: Quỹ tự do đã cạn kiệt! Hãy hạn chế tối đa mọi khoản chi không thiết yếu trong những ngày còn lại.';
  } else if (dailySafe < initialDaily * 0.7) {
    status = 'WARNING';
    label = 'Sát hạn mức';
    badgeClass = 'status-badge warning';
    iconClass = 'fa-solid fa-triangle-exclamation';
    message = 'Lưu ý: Tốc độ chi tiêu đang nhanh hơn nhịp độ thời gian. Hãy tiết chế trong các ngày tới.';
  }

  return {
    spentVariable,
    remainingPool,
    dailySafe,
    burnPct,
    status,
    label,
    badgeClass,
    iconClass,
    message
  };
}

// WEALTH PROJECTION & COMPOUND INTEREST ENGINE
function calculateGoalReport(customGoal = state.goal) {
  const currentYear = new Date().getFullYear();
  const currentAge = Math.max(18, currentYear - customGoal.birthYear);
  const targetAge = Math.max(currentAge + 1, customGoal.targetAge);
  const yearsRemaining = targetAge - currentAge;
  const monthsRemaining = yearsRemaining * 12;

  const currentNetWorth = state.assets.reduce((sum, a) => sum + a.total, 0);
  const pv = Math.max(0, currentNetWorth);
  const pmt = Math.max(0, state.cycle.savingsTarget);
  const target = Math.max(1000000, customGoal.targetAmount);
  const annualRate = Math.min(50, Math.max(0, customGoal.expectedAnnualReturn));
  const monthlyRate = (annualRate / 100) / 12;

  // Compound FV Formula: FV = PV*(1+r)^n + PMT*[((1+r)^n - 1)/r]
  const compoundFactor = monthlyRate > 0 ? Math.pow(1 + monthlyRate, monthsRemaining) : 1;
  const fvFromPv = pv * compoundFactor;
  const annuityFactor = monthlyRate > 0 ? (compoundFactor - 1) / monthlyRate : monthsRemaining;
  const fvFromPmt = pmt * annuityFactor;
  const projectedFutureValue = fvFromPv + fvFromPmt;

  const remainingNeeded = Math.max(0, target - fvFromPv);
  const requiredMonthlySavings = annuityFactor > 0 ? remainingNeeded / annuityFactor : 0;
  const progressPct = Math.min(100, (pv / target) * 100);

  let status = 'BALANCED';
  let statusLabel = 'Đúng kế hoạch';
  let statusBadgeClass = 'status-badge';
  let adviceMessage = 'Kế hoạch rất cân đối! Bạn đang bám sát tiến độ đạt mục tiêu một cách bền vững.';

  if (projectedFutureValue >= target * 1.05) {
    status = 'SURPLUS';
    statusLabel = 'Vượt mục tiêu';
    statusBadgeClass = 'status-badge';
    adviceMessage = `Tuyệt vời! Với mức tích sản ${formatVndShort(pmt)}/tháng và lãi kép ${annualRate.toFixed(1)}%/năm, bạn dự kiến sẽ vượt mục tiêu ở tuổi ${targetAge}.`;
  } else if (projectedFutureValue < target * 0.95) {
    status = 'NEEDS_BOOST';
    statusLabel = 'Cần tăng tốc';
    statusBadgeClass = 'status-badge warning';
    const gap = Math.max(0, requiredMonthlySavings - pmt);
    adviceMessage = `Cần tăng cường: Hãy trích thêm khoảng ${formatVnd(gap)}/tháng hoặc nâng tỷ trọng Quỹ tăng trưởng để kịp về đích.`;
  }

  // Yearly Milestone Roadmap
  const milestones = [];
  for (let y = 0; y <= yearsRemaining; y++) {
    const age = currentAge + y;
    const year = currentYear + y;
    const months = y * 12;
    const cFactor = monthlyRate > 0 ? Math.pow(1 + monthlyRate, months) : 1;
    const aFactor = monthlyRate > 0 ? (cFactor - 1) / monthlyRate : months;
    const valAtAge = (pv * cFactor) + (pmt * aFactor);
    const deposited = pv + (pmt * months);
    const interest = Math.max(0, valAtAge - deposited);
    milestones.push({ age, year, valAtAge, deposited, interest });
  }

  return {
    currentAge,
    targetAge,
    yearsRemaining,
    target,
    pv,
    pmt,
    annualRate,
    progressPct,
    projectedFutureValue,
    requiredMonthlySavings,
    status,
    statusLabel,
    statusBadgeClass,
    adviceMessage,
    milestones
  };
}

// SMART BANK NOTIFICATION REGEX ENGINE
function parseBankMessage(rawText) {
  if (!rawText || !rawText.trim()) return null;
  const text = rawText.trim();

  // Detect bank sender
  let bank = 'Ngân hàng';
  const upper = text.toUpperCase();
  if (upper.includes('VCB') || upper.includes('VIETCOMBANK')) bank = 'VCB Digibank';
  else if (upper.includes('TCB') || upper.includes('TECHCOMBANK')) bank = 'Techcombank';
  else if (upper.includes('MB') || upper.includes('MBBANK')) bank = 'MBBank';
  else if (upper.includes('ACB')) bank = 'ACB ONE';
  else if (upper.includes('VPB') || upper.includes('VPBANK')) bank = 'VPBank';
  else if (upper.includes('BIDV')) bank = 'BIDV SmartBanking';
  else if (upper.includes('AGRIBANK')) bank = 'Agribank';
  else if (upper.includes('TPB') || upper.includes('TPBANK')) bank = 'TPBank';
  else if (upper.includes('MOMO')) bank = 'Ví MoMo';

  // Amount extraction
  // Matches patterns: -65,000 VND, -8.000.000d, giam 1,500,000 VND, GD: -320000
  const amtMatch = text.match(/(?:[-−]\s*|giam\s+|GD:\s*[-−]?\s*)([\d.,]+)\s*(?:VND|VND\b|d|D|đ|₫)?/i);
  let amount = 0;
  if (amtMatch) {
    const cleanNum = amtMatch[1].replace(/[,.]/g, '');
    amount = parseFloat(cleanNum) || 0;
  }

  // Description / Note extraction
  let note = text;
  const ndMatch = text.match(/(?:ND|Noi dung|Noi dung chuyen khoan|Ref|Ly do):\s*([^.\n]+)/i);
  if (ndMatch) {
    note = ndMatch[1].trim();
  }

  // Normalized text for keyword matching
  const norm = (note + ' ' + text).toLowerCase().normalize("NFD").replace(/[\u0300-\u036f]/g, "");

  // Keywords
  const fixedKeywords = [
    'tien nha', 'thue nha', 'hoc phi', 'tien hoc', 'mam non', 'tieu hoc', 
    'tien dien', 'tien nuoc', 'evn', 'cap nuoc', 'internet', 'phi chung cu'
  ];
  const savingsKeywords = [
    'tiet kiem', 'gui tiet kiem', 'so tiet kiem', 'vang', 'chi vang', 
    'pnj', 'sjc', 'dcds', 'veof', 'chung khoan', 'nav'
  ];

  let bucket = 'VARIABLE';
  let matchedBill = null;

  for (const kw of fixedKeywords) {
    if (norm.includes(kw)) {
      bucket = 'FIXED';
      if (norm.includes('nha')) matchedBill = 'rent';
      else if (norm.includes('hoc') || norm.includes('mam non')) matchedBill = 'tuition';
      else if (norm.includes('dien') || norm.includes('nuoc') || norm.includes('evn')) matchedBill = 'utilities';
      break;
    }
  }

  if (bucket === 'VARIABLE') {
    for (const kw of savingsKeywords) {
      if (norm.includes(kw)) {
        bucket = 'SAVINGS';
        break;
      }
    }
  }

  return {
    bank,
    amount,
    note,
    bucket,
    matchedBill
  };
}

// MAIN RENDER FUNCTION
function renderApp() {
  const burn = calculateBurnRate();
  const wealth = calculateGoalReport();

  // Header Cycle text
  document.getElementById('navCycleTxt').innerText = `Tháng ${state.cycle.month}/${state.cycle.year} • Còn ${state.cycle.daysRemaining} ngày`;

  // Privacy buttons state
  const privBtn = document.getElementById('btnPrivacy');
  if (state.isPrivacyMode) {
    privBtn.classList.add('active');
    privBtn.innerHTML = '<i class="fa-regular fa-eye-slash"></i>';
  } else {
    privBtn.classList.remove('active');
    privBtn.innerHTML = '<i class="fa-regular fa-eye"></i>';
  }

  // 1. DASHBOARD SCREEN
  document.getElementById('dashDailySafe').innerText = formatVnd(burn.dailySafe);
  document.getElementById('dashDailySafe').style.color = burn.status === 'SAFE' ? 'var(--status-safe)' : (burn.status === 'WARNING' ? 'var(--status-warn)' : 'var(--status-danger)');
  
  const statusBadge = document.getElementById('dashStatusBadge');
  statusBadge.className = burn.badgeClass;
  statusBadge.innerHTML = `<i class="${burn.iconClass}"></i> <span>${burn.label}</span>`;

  document.getElementById('dashRemainingPool').innerText = formatVnd(burn.remainingPool);
  document.getElementById('dashSpentVariable').innerText = formatVnd(burn.spentVariable);
  document.getElementById('dashDaysLeft').innerText = `${state.cycle.daysRemaining} ngày`;
  document.getElementById('dashBurnPct').innerText = `${Math.round(burn.burnPct)}%`;
  
  const progFill = document.getElementById('dashProgressFill');
  progFill.style.width = `${burn.burnPct}%`;
  progFill.style.backgroundColor = burn.status === 'SAFE' ? 'var(--status-safe)' : (burn.status === 'WARNING' ? 'var(--status-warn)' : 'var(--status-danger)');

  document.getElementById('dashHeroTip').innerText = burn.message;

  // Payday 70/30 Breakdown Pills
  document.getElementById('pillSavingsAmt').innerText = formatVndShort(state.cycle.savingsTarget);
  document.getElementById('pillFixedAmt').innerText = formatVndShort(state.cycle.fixedBills);
  document.getElementById('pillVariableAmt').innerText = formatVndShort(state.cycle.variablePool);

  // Fixed Bills Checklist Preview in Dashboard
  renderFixedBillsList('dashBillsList');

  // Recent Transactions in Dashboard
  renderTransactionsList('dashTxList', 5);

  // 2. FIXED BILLS SCREEN
  renderFixedBillsFull();

  // 3. WEALTH 3 BUCKETS SCREEN
  renderWealthScreen(wealth);

  // 4. TRANSACTIONS SCREEN
  renderTransactionsFull();

  // Save changes to LocalStorage
  StorageManager.save(state);
}

// RENDER FIXED BILLS LIST
function renderFixedBillsList(containerId) {
  const container = document.getElementById(containerId);
  if (!container) return;
  container.innerHTML = '';

  const totalFixed = state.fixedBills.reduce((s, b) => s + b.amount, 0);
  const paidFixed = state.fixedBills.filter(b => b.isPaid).reduce((s, b) => s + b.amount, 0);
  const allPaid = paidFixed >= totalFixed;

  const headerBadge = document.getElementById('billsHeaderBadge');
  if (headerBadge) {
    headerBadge.innerText = allPaid ? `Đã hoàn thành ${formatVndShort(paidFixed)}` : `Đã trả ${formatVndShort(paidFixed)} / ${formatVndShort(totalFixed)}`;
    headerBadge.className = allPaid ? 'status-badge' : 'status-badge warning';
  }

  state.fixedBills.forEach(b => {
    const item = document.createElement('div');
    item.className = 'bill-row' + (b.isPaid ? ' paid' : '');
    item.onclick = () => toggleBillPaid(b.id);
    item.innerHTML = `
      <div class="bill-left">
        <div class="bill-check"><i class="fa-solid fa-check"></i></div>
        <div>
          <div class="b-title"><i class="fa-solid ${b.icon}" style="margin-right: 6px; color: #60A5FA;"></i> ${b.name}</div>
          <div class="b-due">${b.isPaid ? '✓ Đã thanh toán tháng này' : b.dueDate}</div>
        </div>
      </div>
      <div class="b-amount">${formatVnd(b.amount)}</div>
    `;
    container.appendChild(item);
  });
}

function renderFixedBillsFull() {
  const total = state.fixedBills.reduce((s, b) => s + b.amount, 0);
  const paid = state.fixedBills.filter(b => b.isPaid).reduce((s, b) => s + b.amount, 0);
  const remain = Math.max(0, total - paid);

  document.getElementById('fbTotalAmt').innerText = formatVnd(total);
  document.getElementById('fbPaidAmt').innerText = formatVnd(paid);
  document.getElementById('fbRemainAmt').innerText = formatVnd(remain);

  const pct = total > 0 ? (paid / total) * 100 : 0;
  document.getElementById('fbProgFill').style.width = `${pct}%`;
  document.getElementById('fbProgPct').innerText = `${Math.round(pct)}%`;

  renderFixedBillsList('fullBillsList');
}

function toggleBillPaid(billId) {
  const b = state.fixedBills.find(x => x.id === billId);
  if (b) {
    b.isPaid = !b.isPaid;
    renderApp();
    showToast(b.isPaid ? `Đã đánh dấu đã trả: ${b.name}` : `Đã mở lại hóa đơn: ${b.name}`);
  }
}

// RENDER WEALTH SCREEN
function renderWealthScreen(w) {
  document.getElementById('wCurrentNetWorth').innerText = formatVnd(w.pv);
  document.getElementById('wGoalSub').innerText = `Mục tiêu ${formatVndShort(w.target)} tuổi ${w.targetAge} (${w.yearsRemaining} năm nữa)`;
  
  const statusBadge = document.getElementById('wFeasibilityBadge');
  statusBadge.className = w.statusBadgeClass;
  statusBadge.innerText = w.statusLabel;

  document.getElementById('wProgressPct').innerText = `${w.progressPct.toFixed(1)}%`;
  document.getElementById('wProgressFill').style.width = `${w.progressPct}%`;

  document.getElementById('wProjectedFutureVal').innerText = formatVnd(w.projectedFutureValue);
  document.getElementById('wAnnualRate').innerText = `${w.annualRate.toFixed(1)}%/năm`;
  document.getElementById('wAdviceTip').innerText = w.adviceMessage;

  // 3 Lọ Totals
  const b1 = state.assets.filter(a => a.bucket === 'BANK_SAVINGS').reduce((s, a) => s + a.total, 0);
  const b2 = state.assets.filter(a => a.bucket === 'GOLD_9999').reduce((s, a) => s + a.total, 0);
  const b3 = state.assets.filter(a => a.bucket === 'FUND_NAV').reduce((s, a) => s + a.total, 0);

  document.getElementById('b1Val').innerText = formatVndShort(b1);
  document.getElementById('b2Val').innerText = formatVndShort(b2);
  document.getElementById('b3Val').innerText = formatVndShort(b3);

  // Asset Cards Detailed List
  const assetContainer = document.getElementById('assetListContainer');
  assetContainer.innerHTML = '';
  state.assets.forEach(a => {
    const item = document.createElement('div');
    item.className = 'tx-row';
    const icon = a.bucket === 'BANK_SAVINGS' ? 'fa-building-columns' : (a.bucket === 'GOLD_9999' ? 'fa-coins' : 'fa-chart-line');
    const color = a.bucket === 'BANK_SAVINGS' ? 'var(--primary-emerald)' : (a.bucket === 'GOLD_9999' ? 'var(--gold)' : 'var(--blue)');
    
    item.innerHTML = `
      <div class="tx-left">
        <div class="tx-icon-box" style="color: ${color};">
          <i class="fa-solid ${icon}"></i>
        </div>
        <div class="tx-info">
          <div class="tx-title">${a.name}</div>
          <div class="tx-meta">
            <span>${a.units} ${a.label}</span> • <span>Đơn giá: ${formatVndShort(a.unitPrice)}</span>
          </div>
        </div>
      </div>
      <div class="tx-right">
        <div class="tx-amt" style="color: ${color};">${formatVnd(a.total)}</div>
        <button class="tx-del-btn" onclick="deleteAsset(${a.id})" title="Xóa tài sản">
          <i class="fa-regular fa-trash-can"></i>
        </button>
      </div>
    `;
    assetContainer.appendChild(item);
  });

  // Yearly Milestones Table
  const tbody = document.getElementById('roadmapTableBody');
  tbody.innerHTML = '';
  w.milestones.forEach(m => {
    const tr = document.createElement('tr');
    tr.innerHTML = `
      <td style="font-weight: 700; color: var(--text-main);">Tuổi ${m.age} (${m.year})</td>
      <td style="color: var(--text-muted);">${formatVndShort(m.deposited)}</td>
      <td style="font-weight: 700; color: var(--gold);">${formatVnd(m.valAtAge)}</td>
      <td style="font-weight: 700; color: var(--primary-emerald);">+ ${formatVndShort(m.interest)}</td>
    `;
    tbody.appendChild(tr);
  });
}

// RENDER TRANSACTIONS LIST
function renderTransactionsList(containerId, limit = null) {
  const container = document.getElementById(containerId);
  if (!container) return;
  container.innerHTML = '';

  const list = limit ? state.transactions.slice(0, limit) : state.transactions;

  if (list.length === 0) {
    container.innerHTML = `
      <div style="text-align: center; padding: 30px; color: var(--text-dim); font-size: 13px;">
        Chưa có giao dịch nào được ghi nhận.
      </div>
    `;
    return;
  }

  list.forEach(tx => {
    const item = document.createElement('div');
    item.className = 'tx-row';

    let bucketHtml = '';
    let bucketClass = '';
    let amtClass = '';

    if (tx.bucket === 'FIXED') {
      bucketClass = 'fixed';
      amtClass = 'fixed';
      bucketHtml = `<span class="bucket-badge fixed" onclick="openBucketModal(${tx.id})" title="Bấm để đổi Lọ"><i class="fa-solid fa-receipt"></i> Cố định (17 Tr)</span>`;
    } else if (tx.bucket === 'SAVINGS') {
      bucketClass = 'savings';
      amtClass = 'savings';
      bucketHtml = `<span class="bucket-badge savings" onclick="openBucketModal(${tx.id})" title="Bấm để đổi Lọ"><i class="fa-solid fa-piggy-bank"></i> Tích sản 3 Lọ</span>`;
    } else {
      bucketClass = 'variable';
      amtClass = 'variable';
      bucketHtml = `<span class="bucket-badge variable" onclick="openBucketModal(${tx.id})" title="Bấm để đổi Lọ"><i class="fa-solid fa-wallet"></i> Quỹ tự do</span>`;
    }

    const icon = tx.isBank ? 'fa-building-columns' : 'fa-money-bill-wave';

    item.innerHTML = `
      <div class="tx-left">
        <div class="tx-icon-box ${bucketClass}">
          <i class="fa-solid ${icon}"></i>
        </div>
        <div class="tx-info">
          <div class="tx-title">${tx.note}</div>
          <div class="tx-meta">
            <span>${tx.source}</span> • <span>${tx.time}</span> • ${bucketHtml}
          </div>
        </div>
      </div>
      <div class="tx-right">
        <div class="tx-amt ${amtClass}">-${formatVnd(tx.amount)}</div>
        <button class="tx-del-btn" onclick="deleteTransaction(${tx.id})" title="Xóa giao dịch">
          <i class="fa-regular fa-trash-can"></i>
        </button>
      </div>
    `;
    container.appendChild(item);
  });
}

function renderTransactionsFull() {
  const query = (document.getElementById('txSearchInput')?.value || '').toLowerCase().trim();
  const filter = document.getElementById('txFilterBucket')?.value || 'ALL';

  let filtered = state.transactions;
  if (filter !== 'ALL') {
    filtered = filtered.filter(t => t.bucket === filter);
  }
  if (query) {
    filtered = filtered.filter(t => 
      t.note.toLowerCase().includes(query) || 
      t.source.toLowerCase().includes(query)
    );
  }

  const container = document.getElementById('fullTxList');
  container.innerHTML = '';

  if (filtered.length === 0) {
    container.innerHTML = `
      <div style="text-align: center; padding: 40px; color: var(--text-dim); font-size: 14px;">
        Không tìm thấy giao dịch nào phù hợp với bộ lọc.
      </div>
    `;
    return;
  }

  filtered.forEach(tx => {
    const item = document.createElement('div');
    item.className = 'tx-row';

    let bucketHtml = '';
    let bucketClass = '';
    let amtClass = '';

    if (tx.bucket === 'FIXED') {
      bucketClass = 'fixed';
      amtClass = 'fixed';
      bucketHtml = `<span class="bucket-badge fixed" onclick="openBucketModal(${tx.id})" title="Bấm để đổi Lọ"><i class="fa-solid fa-receipt"></i> Cố định (17 Tr)</span>`;
    } else if (tx.bucket === 'SAVINGS') {
      bucketClass = 'savings';
      amtClass = 'savings';
      bucketHtml = `<span class="bucket-badge savings" onclick="openBucketModal(${tx.id})" title="Bấm để đổi Lọ"><i class="fa-solid fa-piggy-bank"></i> Tích sản 3 Lọ</span>`;
    } else {
      bucketClass = 'variable';
      amtClass = 'variable';
      bucketHtml = `<span class="bucket-badge variable" onclick="openBucketModal(${tx.id})" title="Bấm để đổi Lọ"><i class="fa-solid fa-wallet"></i> Quỹ tự do</span>`;
    }

    const icon = tx.isBank ? 'fa-building-columns' : 'fa-money-bill-wave';

    item.innerHTML = `
      <div class="tx-left">
        <div class="tx-icon-box ${bucketClass}">
          <i class="fa-solid ${icon}"></i>
        </div>
        <div class="tx-info">
          <div class="tx-title">${tx.note}</div>
          <div class="tx-meta">
            <span>${tx.source}</span> • <span>${tx.time}</span> • ${bucketHtml}
          </div>
        </div>
      </div>
      <div class="tx-right">
        <div class="tx-amt ${amtClass}">-${formatVnd(tx.amount)}</div>
        <button class="tx-del-btn" onclick="deleteTransaction(${tx.id})" title="Xóa giao dịch">
          <i class="fa-regular fa-trash-can"></i>
        </button>
      </div>
    `;
    container.appendChild(item);
  });
}

// SCREEN NAVIGATION
function switchScreen(screenId) {
  currentScreen = screenId;

  document.querySelectorAll('.screen-view').forEach(s => s.classList.remove('active'));
  document.getElementById(`screen-${screenId}`)?.classList.add('active');

  // Update desktop tabs
  document.querySelectorAll('.nav-desktop-tabs .tab-btn').forEach(btn => {
    btn.classList.toggle('active', btn.dataset.screen === screenId);
  });

  // Update mobile bottom bar
  document.querySelectorAll('.mobile-bottom-bar .mob-tab-btn').forEach(btn => {
    btn.classList.toggle('active', btn.dataset.screen === screenId);
  });

  window.scrollTo({ top: 0, behavior: 'smooth' });
}

// PRIVACY MODE TOGGLE
function togglePrivacyMode() {
  state.isPrivacyMode = !state.isPrivacyMode;
  renderApp();
  showToast(state.isPrivacyMode ? 'Đã bật chế độ riêng tư (Ẩn số tiền)' : 'Đã hiện số tiền');
}

// BUCKET SWITCHER MODAL LOGIC
function openBucketModal(txId) {
  currentEditingTxId = txId;
  const tx = state.transactions.find(t => t.id === txId);
  if (!tx) return;

  document.getElementById('bModalDesc').innerText = `Khoản chi: "${tx.note}" (-${formatVnd(tx.amount)})`;

  document.getElementById('optVar').className = 'bucket-option' + (tx.bucket === 'VARIABLE' ? ' selected' : '');
  document.getElementById('optFix').className = 'bucket-option' + (tx.bucket === 'FIXED' ? ' selected-blue' : '');
  document.getElementById('optSav').className = 'bucket-option' + (tx.bucket === 'SAVINGS' ? ' selected-gold' : '');

  openModal('bucketModal');
}

function selectBucket(newBucket) {
  const tx = state.transactions.find(t => t.id === currentEditingTxId);
  if (!tx) return;

  const oldBucket = tx.bucket;
  tx.bucket = newBucket;

  // Auto-mark bill paid if moved into FIXED
  if (newBucket === 'FIXED') {
    const norm = tx.note.toLowerCase().normalize("NFD").replace(/[\u0300-\u036f]/g, "");
    if (norm.includes('nha')) state.fixedBills[0].isPaid = true;
    else if (norm.includes('hoc') || norm.includes('mam non')) state.fixedBills[1].isPaid = true;
    else if (norm.includes('dien') || norm.includes('nuoc') || norm.includes('evn')) state.fixedBills[2].isPaid = true;
  }

  closeModal('bucketModal');
  renderApp();
  showToast(`Đã chuyển giao dịch sang: ${newBucket === 'VARIABLE' ? 'Quỹ tự do' : (newBucket === 'FIXED' ? 'Chi phí cố định 17 Tr' : 'Tích sản 3 Lọ')}`);
}

// TRANSACTION DELETION
function deleteTransaction(id) {
  if (!confirm('Bạn có chắc muốn xóa giao dịch này không?')) return;
  state.transactions = state.transactions.filter(t => t.id !== id);
  renderApp();
  showToast('Đã xóa giao dịch');
}

// ASSET DELETION
function deleteAsset(id) {
  if (!confirm('Bạn có chắc muốn xóa tài sản này khỏi danh mục tích lũy?')) return;
  state.assets = state.assets.filter(a => a.id !== id);
  renderApp();
  showToast('Đã xóa tài sản');
}

// PARSE BANK MESSAGE ON-SCREEN
function handleParseBankMessage() {
  const input = document.getElementById('bankParserInput');
  const text = input.value.trim();
  if (!text) {
    alert('Vui lòng dán nội dung tin nhắn ngân hàng (SMS hoặc thông báo App)!');
    return;
  }

  const parsed = parseBankMessage(text);
  if (!parsed || parsed.amount <= 0) {
    alert('Không nhận diện được số tiền biến động (-). Hãy kiểm tra cú pháp ví dụ: "VCB Digibank: -8,000,000 VND. ND: Tien nha thang 09"');
    return;
  }

  // Add transaction
  const now = new Date();
  const timeStr = `${String(now.getHours()).padStart(2, '0')}:${String(now.getMinutes()).padStart(2, '0')}`;
  
  state.transactions.unshift({
    id: Date.now(),
    source: parsed.bank,
    note: parsed.note,
    amount: parsed.amount,
    time: `Hôm nay ${timeStr}`,
    isBank: true,
    bucket: parsed.bucket
  });

  // Mark bill paid if matched
  if (parsed.matchedBill) {
    const b = state.fixedBills.find(x => x.id === parsed.matchedBill);
    if (b) b.isPaid = true;
  }

  // If gold savings
  if (parsed.bucket === 'SAVINGS' && parsed.note.toLowerCase().includes('vang')) {
    state.assets[1].units += 1;
    state.assets[1].total = state.assets[1].units * state.assets[1].unitPrice;
  }

  input.value = '';
  renderApp();

  let tagDesc = 'Quỹ chi tiêu tự do';
  if (parsed.bucket === 'FIXED') tagDesc = 'Chi phí cố định 17 Tr (Không thâm hụt Safe-to-Spend)';
  else if (parsed.bucket === 'SAVINGS') tagDesc = 'Tích sản 3 Lọ (Gia tăng tài sản)';

  showToast(`Đã nhận diện: -${formatRawVnd(parsed.amount)} • Phân loại: ${tagDesc}`);
}

function pasteSampleMessage(sample) {
  document.getElementById('bankParserInput').value = sample;
}

// QUICK ADD MODAL
function handleApplyQuickAdd() {
  const amt = parseFloat(document.getElementById('qaAmount').value) || 0;
  const note = document.getElementById('qaNote').value.trim() || 'Chi tiêu tiền mặt';
  const bucket = document.getElementById('qaBucket').value;
  const source = document.getElementById('qaSource').value;

  if (amt <= 0) {
    alert('Vui lòng nhập số tiền lớn hơn 0!');
    return;
  }

  const now = new Date();
  const timeStr = `${String(now.getHours()).padStart(2, '0')}:${String(now.getMinutes()).padStart(2, '0')}`;

  state.transactions.unshift({
    id: Date.now(),
    source,
    note,
    amount: amt,
    time: `Hôm nay ${timeStr}`,
    isBank: source !== 'Tiền mặt',
    bucket
  });

  closeModal('quickAddModal');
  document.getElementById('qaAmount').value = '';
  document.getElementById('qaNote').value = '';
  renderApp();
  showToast(`Đã thêm chi tiêu: -${formatRawVnd(amt)}`);
}

// PAYDAY 70/30 MODAL
function openPaydayModal() {
  document.getElementById('pdIncome').value = state.cycle.totalIncome;
  document.getElementById('pdFixed').value = state.cycle.fixedBills;
  calcPaydayPreview();
  openModal('paydayModal');
}

function calcPaydayPreview() {
  const income = parseFloat(document.getElementById('pdIncome').value) || 0;
  const fixed = parseFloat(document.getElementById('pdFixed').value) || 0;
  const savings = income * 0.3;
  const variable = Math.max(0, income - savings - fixed);

  document.getElementById('pdPreviewSavings').innerText = formatRawVnd(savings);
  document.getElementById('pdPreviewVariable').innerText = formatRawVnd(variable);
}

function handleApplyPayday() {
  const income = parseFloat(document.getElementById('pdIncome').value) || 0;
  const fixed = parseFloat(document.getElementById('pdFixed').value) || 0;
  const savings = income * 0.3;
  const variable = Math.max(0, income - savings - fixed);

  state.cycle.totalIncome = income;
  state.cycle.savingsTarget = savings;
  state.cycle.fixedBills = fixed;
  state.cycle.variablePool = variable;

  closeModal('paydayModal');
  renderApp();
  showToast('Đã kích hoạt chu kỳ lương mới thành công!');
}

// WEALTH GOAL MODAL
function openGoalModal() {
  document.getElementById('gBirthYear').value = state.goal.birthYear;
  document.getElementById('gTargetAge').value = state.goal.targetAge;
  document.getElementById('gTargetAmount').value = state.goal.targetAmount;
  document.getElementById('gReturnRate').value = state.goal.expectedAnnualReturn;
  document.getElementById('gReturnRateVal').innerText = `${state.goal.expectedAnnualReturn.toFixed(1)}%`;
  calcGoalPreview();
  openModal('goalModal');
}

function calcGoalPreview() {
  const tempGoal = {
    birthYear: parseInt(document.getElementById('gBirthYear').value) || 1994,
    targetAge: parseInt(document.getElementById('gTargetAge').value) || 40,
    targetAmount: parseFloat(document.getElementById('gTargetAmount').value) || 600000000,
    expectedAnnualReturn: parseFloat(document.getElementById('gReturnRate').value) || 8.0
  };

  document.getElementById('gReturnRateVal').innerText = `${tempGoal.expectedAnnualReturn.toFixed(1)}%`;

  const w = calculateGoalReport(tempGoal);
  document.getElementById('gPreviewFutureVal').innerText = formatRawVnd(w.projectedFutureValue);
  document.getElementById('gPreviewRequiredPmt').innerText = formatRawVnd(w.requiredMonthlySavings);
  document.getElementById('gPreviewTip').innerText = w.adviceMessage;
}

function setPresetGoalAmount(amt) {
  document.getElementById('gTargetAmount').value = amt;
  calcGoalPreview();
}

function handleApplyGoal() {
  state.goal.birthYear = parseInt(document.getElementById('gBirthYear').value) || 1994;
  state.goal.targetAge = parseInt(document.getElementById('gTargetAge').value) || 40;
  state.goal.targetAmount = parseFloat(document.getElementById('gTargetAmount').value) || 600000000;
  state.goal.expectedAnnualReturn = parseFloat(document.getElementById('gReturnRate').value) || 8.0;

  closeModal('goalModal');
  renderApp();
  showToast('Đã cập nhật mục tiêu tài chính & lộ trình lãi kép!');
}

// ADD ASSET MODAL
function openAddAssetModal(presetBucket = 'BANK_SAVINGS') {
  document.getElementById('astBucket').value = presetBucket;
  onAssetBucketSelect();
  openModal('addAssetModal');
}

function onAssetBucketSelect() {
  const b = document.getElementById('astBucket').value;
  if (b === 'BANK_SAVINGS') {
    document.getElementById('astUnitsLabel').innerText = 'Số lượng (Sổ)';
    document.getElementById('astName').value = 'Sổ tiết kiệm kỳ hạn 12 tháng';
    document.getElementById('astUnits').value = '1';
    document.getElementById('astUnitPrice').value = '50000000';
  } else if (b === 'GOLD_9999') {
    document.getElementById('astUnitsLabel').innerText = 'Số lượng (Chỉ)';
    document.getElementById('astName').value = 'Vàng nhẫn trơn 9999 PNJ';
    document.getElementById('astUnits').value = '2';
    document.getElementById('astUnitPrice').value = '8200000';
  } else {
    document.getElementById('astUnitsLabel').innerText = 'Số lượng (CCQ)';
    document.getElementById('astName').value = 'Chứng chỉ quỹ DCDS / VEOF';
    document.getElementById('astUnits').value = '1000';
    document.getElementById('astUnitPrice').value = '28500';
  }
}

function handleApplyAddAsset() {
  const bucket = document.getElementById('astBucket').value;
  const name = document.getElementById('astName').value.trim() || 'Tài sản tích lũy';
  const units = parseFloat(document.getElementById('astUnits').value) || 1;
  const unitPrice = parseFloat(document.getElementById('astUnitPrice').value) || 0;
  const label = bucket === 'BANK_SAVINGS' ? 'Sổ' : (bucket === 'GOLD_9999' ? 'Chỉ' : 'CCQ');

  state.assets.push({
    id: Date.now(),
    bucket,
    name,
    units,
    unitPrice,
    total: units * unitPrice,
    label
  });

  closeModal('addAssetModal');
  renderApp();
  showToast(`Đã thêm tài sản mới: ${name}`);
}

// BACKUP & RESTORE
function exportDataBackup() {
  StorageManager.exportJSON(state);
  showToast('Đã tải xuống file sao lưu dữ liệu (.json)');
}

function importDataBackup() {
  const input = document.createElement('input');
  input.type = 'file';
  input.accept = '.json';
  input.onchange = e => {
    const file = e.target.files[0];
    if (!file) return;

    const reader = new FileReader();
    reader.onload = event => {
      try {
        const imported = JSON.parse(event.target.result);
        if (imported.cycle && imported.goal && imported.transactions && imported.assets) {
          state = imported;
          StorageManager.save(state);
          renderApp();
          showToast('Khôi phục dữ liệu thành công!');
        } else {
          alert('File JSON không đúng cấu trúc của ezFlowMoney.');
        }
      } catch (err) {
        alert('Lỗi đọc file: ' + err.message);
      }
    };
    reader.readAsText(file);
  };
  input.click();
}

function resetToDefaultData() {
  if (!confirm('Bạn có chắc muốn đặt lại toàn bộ dữ liệu mẫu ban đầu không?')) return;
  state = StorageManager.reset();
  renderApp();
  showToast('Đã khôi phục dữ liệu mặc định');
}

// MODAL CONTROLLERS
function openModal(id) {
  document.getElementById(id)?.classList.add('open');
}

function closeModal(id) {
  document.getElementById(id)?.classList.remove('open');
}

// TOAST CONTROLLER
let toastTimeout = null;
function showToast(msg) {
  const toast = document.getElementById('toastMsg');
  if (!toast) return;
  toast.innerHTML = `<i class="fa-solid fa-circle-check" style="color: var(--primary-emerald); font-size: 16px;"></i> <span>${msg}</span>`;
  toast.classList.add('show');

  clearTimeout(toastTimeout);
  toastTimeout = setTimeout(() => {
    toast.classList.remove('show');
  }, 3500);
}

// SERVICE WORKER REGISTRATION (PWA)
if ('serviceWorker' in navigator) {
  window.addEventListener('load', () => {
    navigator.serviceWorker.register('./sw.js')
      .then(reg => console.log('PWA ServiceWorker registered with scope:', reg.scope))
      .catch(err => console.log('ServiceWorker registration failed:', err));
  });
}

// ==========================================
// SUPABASE AUTHENTICATION & CLOUD SYNC LOGIC
// ==========================================
let authMode = 'login';
let cloudSyncTimer = null;

function triggerCloudSync() {
  if (!window.SupabaseService || !SupabaseService.currentUser) return;
  clearTimeout(cloudSyncTimer);
  cloudSyncTimer = setTimeout(async () => {
    try {
      await SupabaseService.syncUserData(SupabaseService.currentUser.id, SupabaseService.currentUser.email, state);
      console.log('Đã tự động đồng bộ đám mây Supabase thành công!');
    } catch (err) {
      console.warn('Lỗi tự động đồng bộ đám mây:', err.message);
    }
  }, 1200);
}

function openAuthModal() {
  if (!SupabaseService.isConfigured()) {
    openSupabaseConfigModal();
    return;
  }
  switchAuthTab('login');
  openModal('authModal');
}

function switchAuthTab(mode) {
  authMode = mode;
  const tabLogin = document.getElementById('tabBtnLogin');
  const tabReg = document.getElementById('tabBtnRegister');
  const modalTitle = document.getElementById('authModalTitle');
  const submitBtn = document.getElementById('btnAuthSubmit');
  const noticeBox = document.getElementById('authNoticeBox');

  if (noticeBox) noticeBox.style.display = 'none';

  if (mode === 'login') {
    tabLogin.classList.add('active');
    tabReg.classList.remove('active');
    modalTitle.innerText = 'Đăng Nhập Tài Khoản';
    submitBtn.innerHTML = '<i class="fa-solid fa-right-to-bracket"></i> Đăng Nhập';
  } else {
    tabLogin.classList.remove('active');
    tabReg.classList.add('active');
    modalTitle.innerText = 'Đăng Ký Tài Khoản Mới';
    submitBtn.innerHTML = '<i class="fa-solid fa-user-plus"></i> Tạo Tài Khoản';
  }
}

async function handleAuthSubmit() {
  const emailInput = document.getElementById('authEmail');
  const passInput = document.getElementById('authPassword');
  const submitBtn = document.getElementById('btnAuthSubmit');

  const email = emailInput.value.trim();
  const password = passInput.value;

  if (!email || !password) {
    showAuthNotice('Vui lòng nhập đầy đủ Email và Mật khẩu!', 'error');
    return;
  }

  if (password.length < 6) {
    showAuthNotice('Mật khẩu cần tối thiểu 6 ký tự!', 'error');
    return;
  }

  submitBtn.disabled = true;
  submitBtn.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Đang xử lý...';

  try {
    if (authMode === 'login') {
      await SupabaseService.signIn(email, password);
      closeModal('authModal');
      showToast(`Chào mừng bạn quay lại, ${email}!`);
    } else {
      await SupabaseService.signUp(email, password);
      showAuthNotice('Đăng ký thành công! Hãy kiểm tra hòm thư xác thực hoặc đăng nhập ngay.', 'success');
      setTimeout(() => {
        switchAuthTab('login');
      }, 1500);
    }
  } catch (err) {
    showAuthNotice(err.message || 'Lỗi xác thực người dùng!', 'error');
  } finally {
    submitBtn.disabled = false;
    submitBtn.innerHTML = authMode === 'login' ? '<i class="fa-solid fa-right-to-bracket"></i> Đăng Nhập' : '<i class="fa-solid fa-user-plus"></i> Tạo Tài Khoản';
  }
}

async function handleGoogleLogin() {
  if (!SupabaseService.isConfigured()) {
    openSupabaseConfigModal();
    return;
  }
  try {
    await SupabaseService.signInWithGoogle();
  } catch (err) {
    showAuthNotice(err.message, 'error');
  }
}

function showAuthNotice(msg, type) {
  const box = document.getElementById('authNoticeBox');
  if (!box) return;
  box.style.display = 'block';
  if (type === 'error') {
    box.style.background = 'rgba(239, 68, 68, 0.15)';
    box.style.border = '1px solid rgba(239, 68, 68, 0.3)';
    box.style.color = '#fca5a5';
  } else {
    box.style.background = 'rgba(16, 185, 129, 0.15)';
    box.style.border = '1px solid rgba(16, 185, 129, 0.3)';
    box.style.color = '#6ee7b7';
  }
  box.innerText = msg;
}

function openSupabaseConfigModal() {
  document.getElementById('sbProjectUrl').value = SupabaseService.getUrl();
  document.getElementById('sbAnonKey').value = SupabaseService.getAnonKey();
  openModal('supabaseConfigModal');
}

function handleSaveSupabaseConfig() {
  const url = document.getElementById('sbProjectUrl').value.trim();
  const key = document.getElementById('sbAnonKey').value.trim();

  if (!url || !key) {
    alert('Vui lòng nhập cả Supabase Project URL và Anon Key!');
    return;
  }

  const ok = SupabaseService.saveConfig(url, key);
  closeModal('supabaseConfigModal');
  if (ok) {
    showToast('Đã kết nối thành công với Supabase Cloud!');
    openAuthModal();
  } else {
    showToast('Đã lưu cấu hình Supabase!');
  }
}

function openProfileModal() {
  const user = SupabaseService.currentUser;
  if (!user) {
    openAuthModal();
    return;
  }

  const email = user.email || 'Người dùng';
  document.getElementById('modalUserEmail').innerText = email;
  document.getElementById('modalAvatarTxt').innerText = email.charAt(0).toUpperCase();

  openModal('profileModal');
}

async function handleSaveFamilyCode() {
  const user = SupabaseService.currentUser;
  if (!user) return;
  const input = document.getElementById('familyCodeInput');
  const code = input.value.trim().toUpperCase();

  try {
    await SupabaseService.updateFamilyCode(user.id, code);
    showToast(`Đã lưu Mã Gia Đình: ${code || 'Đã xóa mã'}`);
  } catch (err) {
    alert('Lỗi lưu mã gia đình: ' + err.message);
  }
}

async function handleManualCloudSync() {
  const user = SupabaseService.currentUser;
  if (!user) return;
  try {
    await SupabaseService.syncUserData(user.id, user.email, state);
    showToast('Đã sao lưu dữ liệu lên đám mây thành công!');
    closeModal('profileModal');
  } catch (err) {
    alert('Lỗi đồng bộ: ' + err.message);
  }
}

async function handleSignOut() {
  if (!confirm('Bạn có chắc muốn đăng xuất khỏi tài khoản này?')) return;
  await SupabaseService.signOut();
  closeModal('profileModal');
  showToast('Đã đăng xuất tài khoản.');
}

async function handleAuthStateChanged(user) {
  const authBtn = document.getElementById('btnOpenAuth');
  const userMenu = document.getElementById('userProfileMenu');
  const avatarTxt = document.getElementById('userAvatarTxt');
  const emailTxt = document.getElementById('userEmailTxt');

  if (user) {
    if (authBtn) authBtn.style.display = 'none';
    if (userMenu) userMenu.style.display = 'flex';
    if (avatarTxt) avatarTxt.innerText = (user.email || 'U').charAt(0).toUpperCase();
    if (emailTxt) emailTxt.innerText = user.email || 'User';

    // Fetch cloud data for this user
    try {
      const cloudData = await SupabaseService.fetchUserData(user.id);
      if (cloudData && cloudData.cycle) {
        state = {
          isPrivacyMode: state.isPrivacyMode,
          cycle: cloudData.cycle,
          fixedBills: cloudData.fixed_bills || state.fixedBills,
          goal: cloudData.goal || state.goal,
          transactions: cloudData.transactions || state.transactions,
          assets: cloudData.assets || state.assets
        };
        StorageManager.save(state);
        renderApp();
        showToast(`Đã nạp dữ liệu đám mây (${user.email})`);
      } else {
        // First time cloud sync
        await SupabaseService.syncUserData(user.id, user.email, state);
      }

      // Subscribe to real-time changes
      SupabaseService.subscribeToRealtime(user.id, (remoteRow) => {
        if (remoteRow && remoteRow.cycle) {
          state.cycle = remoteRow.cycle;
          if (remoteRow.fixed_bills) state.fixedBills = remoteRow.fixed_bills;
          if (remoteRow.goal) state.goal = remoteRow.goal;
          if (remoteRow.transactions) state.transactions = remoteRow.transactions;
          if (remoteRow.assets) state.assets = remoteRow.assets;
          StorageManager.save(state);
          renderApp();
          showToast('Ngân sách vừa được cập nhật từ thiết bị gia đình!');
        }
      });
    } catch (err) {
      console.warn('Lỗi tải dữ liệu người dùng:', err);
    }
  } else {
    if (authBtn) authBtn.style.display = 'inline-flex';
    if (userMenu) userMenu.style.display = 'none';
  }
}

// INITIALIZATION
window.addEventListener('DOMContentLoaded', () => {
  renderApp();

  // Initialize Supabase auth & sync listener
  if (window.SupabaseService) {
    window.onAuthChangeCallback = (user) => {
      handleAuthStateChanged(user);
    };
    SupabaseService.init();
  }
});
