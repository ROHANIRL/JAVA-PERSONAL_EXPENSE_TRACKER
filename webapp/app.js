// ---------- Navigation ----------
document.querySelectorAll('.nav-btn').forEach(btn => {
  btn.addEventListener('click', () => {
    document.querySelectorAll('.nav-btn').forEach(b => b.classList.remove('active'));
    document.querySelectorAll('.section').forEach(s => s.classList.remove('active'));
    btn.classList.add('active');
    const section = btn.dataset.section;
    document.getElementById('section-' + section).classList.add('active');
    loadSection(section);
  });
});

function loadSection(section) {
  if (section === 'dashboard') loadDashboard();
  else if (section === 'add') loadCategoriesInto('add-category');
  else if (section === 'expenses') loadExpenses();
  else if (section === 'budgets') { loadCategoriesInto('budget-category'); loadBudgets(); }
  else if (section === 'savings') loadSavings();
  else if (section === 'family') loadFamily();
  else if (section === 'insights') loadInsights();
}

// ---------- Helpers ----------
async function postForm(url, data) {
  const body = new URLSearchParams(data);
  const res = await fetch(url, { method: 'POST', headers: { 'Content-Type': 'application/x-www-form-urlencoded' }, body });
  return readApiResponse(res);
}
async function del(url) {
  const res = await fetch(url, { method: 'DELETE' });
  return readApiResponse(res);
}
async function getJson(url) {
  return readApiResponse(await fetch(url));
}
async function readApiResponse(res) {
  let payload;
  try {
    payload = await res.json();
  } catch {
    throw new Error(`The server returned an unreadable response (HTTP ${res.status}).`);
  }
  if (!res.ok) throw new Error(payload.error || `Request failed (HTTP ${res.status}).`);
  return payload;
}
function showLoadError(error) {
  console.error(error);
  alert(error.message || 'Could not load data. Check that MySQL is running and the database settings are correct.');
}
function fmt(n) {
  return Number(n).toLocaleString('en-IN', { maximumFractionDigits: 2 });
}

// ---------- Categories ----------
async function loadCategoriesInto(selectId) {
  let categories;
  try { categories = await getJson('/api/categories'); } catch (error) { showLoadError(error); return; }
  const select = document.getElementById(selectId);
  const previous = select.value;
  select.innerHTML = categories.map(c => `<option value="${c.id}">${c.name}</option>`).join('');
  if (previous) select.value = previous;
}

// ---------- Dashboard ----------
let spendingChart = null;
async function loadDashboard() {
  const [savings, expenses, insights] = await Promise.all([
    getJson('/api/savings'),
    getJson('/api/expenses'),
    getJson('/api/insights')
  ]);

  document.getElementById('dash-spent').textContent = 'Rs.' + fmt(savings.spent);
  document.getElementById('dash-savings').textContent = 'Rs.' + fmt(savings.savings);
  document.getElementById('dash-essentials').textContent = 'Rs.' + fmt(insights.estimatedMonthlyCost);

  const byCategory = {};
  expenses.forEach(e => { byCategory[e.category] = (byCategory[e.category] || 0) + e.amount; });
  const labels = Object.keys(byCategory);
  const data = Object.values(byCategory);

  const ctx = document.getElementById('chart-spending');
  if (spendingChart) spendingChart.destroy();
  spendingChart = new Chart(ctx, {
    type: 'doughnut',
    data: {
      labels,
      datasets: [{ data, backgroundColor: ['#2E7D32', '#00897B', '#EF6C00', '#C62828', '#1B5E20', '#66BB6A', '#757575', '#455A64'] }]
    },
    options: { plugins: { legend: { position: 'right' } } }
  });
}

// ---------- Add Expense ----------
document.getElementById('add-category-new').addEventListener('click', async () => {
  const name = prompt('New category name:');
  if (!name) return;
  await postForm('/api/categories', { name });
  await loadCategoriesInto('add-category');
});

document.getElementById('add-submit').addEventListener('click', async () => {
  const categoryId = document.getElementById('add-category').value;
  const amount = document.getElementById('add-amount').value;
  const description = document.getElementById('add-description').value;
  const date = document.getElementById('add-date').value;
  const statusEl = document.getElementById('add-status');

  if (!categoryId || !amount || Number(amount) <= 0) {
    statusEl.textContent = 'Enter a valid category and positive amount.';
    statusEl.style.color = '#C62828';
    return;
  }

  const result = await postForm('/api/expenses', { categoryId, amount, description, date });
  if (result.success) {
    statusEl.textContent = 'Expense added.';
    statusEl.style.color = '#2E7D32';
    if (result.alert) alert(result.alert);
    document.getElementById('add-amount').value = '';
    document.getElementById('add-description').value = '';
  } else {
    statusEl.textContent = result.error || 'Something went wrong.';
    statusEl.style.color = '#C62828';
  }
});

// ---------- Expenses ----------
async function loadExpenses() {
  const filter = document.querySelector('input[name="expense-filter"]:checked').value;
  let expenses;
  try { expenses = await getJson('/api/expenses'); } catch (error) { showLoadError(error); return; }
  const now = new Date();
  const filtered = filter === 'month'
    ? expenses.filter(e => {
        const d = new Date(e.date);
        return d.getMonth() === now.getMonth() && d.getFullYear() === now.getFullYear();
      })
    : expenses;

  const tbody = document.querySelector('#expenses-table tbody');
  tbody.innerHTML = filtered.map(e => `
    <tr>
      <td><input type="radio" name="expense-select" value="${e.id}"></td>
      <td>${e.id}</td><td>${e.category}</td><td>Rs.${fmt(e.amount)}</td>
      <td>${e.description}</td><td>${e.date}</td>
    </tr>`).join('');

  const total = filtered.reduce((sum, e) => sum + e.amount, 0);
  document.getElementById('expenses-total').textContent = `Total: Rs.${fmt(total)} (${filtered.length} expenses)`;
}
document.querySelectorAll('input[name="expense-filter"]').forEach(r => r.addEventListener('change', loadExpenses));
document.getElementById('expenses-refresh').addEventListener('click', loadExpenses);
document.getElementById('expenses-delete').addEventListener('click', async () => {
  const selected = document.querySelector('input[name="expense-select"]:checked');
  if (!selected) { alert('Select a row first.'); return; }
  if (!confirm('Delete expense #' + selected.value + '?')) return;
  await del('/api/expenses?id=' + selected.value);
  loadExpenses();
});

// ---------- Budgets ----------
document.getElementById('budget-submit').addEventListener('click', async () => {
  const categoryId = document.getElementById('budget-category').value;
  const limit = document.getElementById('budget-limit').value;
  const statusEl = document.getElementById('budget-status');
  if (!categoryId || !limit || Number(limit) <= 0) {
    statusEl.textContent = 'Enter a valid category and positive limit.';
    statusEl.style.color = '#C62828';
    return;
  }
  const result = await postForm('/api/budgets', { categoryId, limit });
  if (result.success) {
    statusEl.textContent = 'Budget set.';
    statusEl.style.color = '#2E7D32';
    document.getElementById('budget-limit').value = '';
    loadBudgets();
  } else {
    statusEl.textContent = result.error || 'Something went wrong.';
    statusEl.style.color = '#C62828';
  }
});

async function loadBudgets() {
  let budgets;
  try { budgets = await getJson('/api/budgets'); } catch (error) { showLoadError(error); return; }
  const tbody = document.querySelector('#budgets-table tbody');
  tbody.innerHTML = budgets.map(b => `
    <tr>
      <td>${b.category}</td><td>Rs.${fmt(b.spent)}</td><td>Rs.${fmt(b.limit)}</td>
      <td>${b.percentUsed.toFixed(0)}%</td>
      <td style="color:${b.status === 'OVER BUDGET' ? '#C62828' : b.status === 'WARNING' ? '#EF6C00' : '#2E7D32'}; font-weight:bold;">${b.status}</td>
    </tr>`).join('');
}

// ---------- Savings ----------
document.getElementById('savings-submit').addEventListener('click', async () => {
  const income = document.getElementById('savings-income').value;
  const goalPercent = document.getElementById('savings-goal').value;
  await postForm('/api/savings', { income, goalPercent });
  loadSavings();
});

async function loadSavings() {
  let s;
  try { s = await getJson('/api/savings'); } catch (error) { showLoadError(error); return; }
  document.getElementById('savings-income').value = s.income;
  document.getElementById('savings-goal').value = s.goalPercent;

  document.getElementById('savings-summary').innerHTML = `
    Monthly Income: <b>Rs.${fmt(s.income)}</b><br>
    Total Spent This Month: <b>Rs.${fmt(s.spent)}</b><br>
    Actual Savings: <b>Rs.${fmt(s.savings)}</b> (${s.savingsPercent.toFixed(1)}% of income)<br>
    Savings Goal: <b>Rs.${fmt(s.goalAmount)}</b> (${s.goalPercent.toFixed(1)}% of income)<br><br>
    <span style="color:${s.onTrack ? '#2E7D32' : '#C62828'}">${s.statusMessage}</span>`;

  const pct = s.goalAmount > 0 ? Math.max(0, Math.min(100, (s.savings / s.goalAmount) * 100)) : 0;
  const fill = document.getElementById('savings-progress-fill');
  fill.style.width = pct + '%';
  fill.style.background = s.onTrack ? '#2E7D32' : '#EF6C00';
  fill.textContent = pct.toFixed(0) + '%';
}

// ---------- Family ----------
document.getElementById('member-submit').addEventListener('click', async () => {
  const name = document.getElementById('member-name').value;
  const age = document.getElementById('member-age').value;
  const gender = document.getElementById('member-gender').value;
  const heightCm = document.getElementById('member-height').value;
  const weightKg = document.getElementById('member-weight').value;
  const activityLevel = document.getElementById('member-activity').value;
  const statusEl = document.getElementById('member-status');

  if (!name || !age || !heightCm || !weightKg) {
    statusEl.textContent = 'Fill in all fields.';
    statusEl.style.color = '#C62828';
    return;
  }

  const result = await postForm('/api/family', { name, age, gender, heightCm, weightKg, activityLevel });
  if (result.success) {
    statusEl.textContent = 'Family member added.';
    statusEl.style.color = '#2E7D32';
    document.getElementById('member-name').value = '';
    document.getElementById('member-age').value = '';
    document.getElementById('member-height').value = '';
    document.getElementById('member-weight').value = '';
    loadFamily();
  } else {
    statusEl.textContent = result.error || 'Something went wrong.';
    statusEl.style.color = '#C62828';
  }
});

async function loadFamily() {
  let members;
  try { members = await getJson('/api/family'); } catch (error) { showLoadError(error); return; }
  const tbody = document.querySelector('#family-table tbody');
  tbody.innerHTML = members.map(m => `
    <tr>
      <td><button class="btn btn-danger" style="padding:4px 10px;" onclick="deleteMember(${m.id})" type="button">x</button></td>
      <td>${m.name}</td><td>${m.age}</td><td>${m.gender}</td>
      <td>${m.bmi.toFixed(1)}</td><td>${m.bmiCategory}</td>
    </tr>`).join('');
}
async function deleteMember(id) {
  if (!confirm('Remove this family member?')) return;
  await del('/api/family?id=' + id);
  loadFamily();
}

// ---------- Insights ----------
let essentialsChart = null;
async function loadInsights() {
  const [insights, budgets] = await Promise.all([
    getJson('/api/insights'),
    getJson('/api/budgets')
  ]);

  document.getElementById('insights-rice').textContent = insights.totalRiceKg.toFixed(1) + ' kg';
  document.getElementById('insights-dal').textContent = insights.totalDalKg.toFixed(1) + ' kg';
  document.getElementById('insights-oil').textContent = insights.totalOilLiters.toFixed(1) + ' L';

  const tbody = document.querySelector('#insights-member-table tbody');
  tbody.innerHTML = insights.members.map(m => `
    <tr>
      <td>${m.name}</td><td>${m.bmi.toFixed(1)}</td><td>${m.bmiCategory}</td>
      <td>${m.tdee.toFixed(0)}</td><td>${m.riceKgPerMonth.toFixed(1)}</td>
      <td>${m.dalKgPerMonth.toFixed(1)}</td><td>${m.oilLitersPerMonth.toFixed(1)}</td>
    </tr>`).join('');

  const recList = document.getElementById('insights-recommendations');
  recList.innerHTML = insights.recommendations.map(r => `<li>${r}</li>`).join('');

  const foodBudget = budgets.find(b => b.category === 'Food');
  const ctx = document.getElementById('chart-essentials');
  if (essentialsChart) essentialsChart.destroy();
  essentialsChart = new Chart(ctx, {
    type: 'bar',
    data: {
      labels: ['Essentials Cost', 'Food Budget'],
      datasets: [{
        label: 'Rs.',
        data: [insights.estimatedMonthlyCost, foodBudget ? foodBudget.limit : 0],
        backgroundColor: ['#2E7D32', '#00897B']
      }]
    },
    options: { plugins: { legend: { display: false } } }
  });
}

// ---------- Initial load ----------
document.getElementById('add-date').valueAsDate = new Date();
loadDashboard();
