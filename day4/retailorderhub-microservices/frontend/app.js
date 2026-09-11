// RetailOrderHub frontend - plain JS, no build step, no framework.
// Talks only to api-gateway (never to order-service/inventory-service/
// payment-service directly), the same way any other client would.

const GATEWAY_KEY = "retailorderhub.gatewayUrl";
const gatewayInput = document.getElementById("gatewayUrl");
const statusDot = document.getElementById("statusDot");
const statusText = document.getElementById("statusText");

const catalogBody = document.getElementById("catalogBody");
const itemChecklist = document.getElementById("itemChecklist");
const ordersBody = document.getElementById("ordersBody");
const orderForm = document.getElementById("orderForm");
const orderBanner = document.getElementById("orderBanner");
const amountInput = document.getElementById("amount");
const submitBtn = document.getElementById("submitOrder");

let catalog = []; // last-known catalog, used to compute the order amount

function gatewayUrl() {
  return gatewayInput.value.trim().replace(/\/+$/, "");
}

// Persist the gateway URL across reloads - handy if someone's api-gateway
// isn't on the default port.
gatewayInput.value = localStorage.getItem(GATEWAY_KEY) || gatewayInput.value;
gatewayInput.addEventListener("change", () => {
  localStorage.setItem(GATEWAY_KEY, gatewayUrl());
  loadCatalog();
  loadOrders();
  checkStatus();
});

async function api(path, options = {}) {
  const res = await fetch(gatewayUrl() + path, {
    headers: { "Content-Type": "application/json" },
    ...options,
  });
  if (!res.ok) {
    throw new Error(`${options.method || "GET"} ${path} -> HTTP ${res.status}`);
  }
  const text = await res.text();
  return text ? JSON.parse(text) : null;
}

// ================= Status check =================
async function checkStatus() {
  statusDot.className = "status-dot";
  statusText.textContent = "Checking…";
  try {
    await api("/api/inventory/products");
    statusDot.classList.add("ok");
    statusText.textContent = "Reachable";
  } catch (e) {
    statusDot.classList.add("down");
    statusText.textContent = "Unreachable";
  }
}

// ================= Catalog =================
function money(n) {
  return "$" + Number(n).toFixed(2);
}

function renderCatalog() {
  if (catalog.length === 0) {
    catalogBody.innerHTML = `<tr><td colspan="3" class="empty-row">No products returned by inventory-service.</td></tr>`;
    itemChecklist.innerHTML = `<p class="empty-row">No products to order.</p>`;
    return;
  }

  catalogBody.innerHTML = catalog.map(p => `
    <tr>
      <td>${escapeHtml(p.name)}</td>
      <td>${money(p.price)}</td>
      <td class="${p.quantity <= 0 ? "stock-low" : ""}">${p.quantity}</td>
    </tr>
  `).join("");

  itemChecklist.innerHTML = catalog.map(p => `
    <label class="${p.quantity <= 0 ? "out-of-stock" : ""}">
      <input type="checkbox" name="item" value="${escapeAttr(p.name)}"
             data-price="${p.price}" ${p.quantity <= 0 ? "disabled" : ""}>
      ${escapeHtml(p.name)} — ${money(p.price)} (${p.quantity} in stock)
    </label>
  `).join("");
}

async function loadCatalog() {
  catalogBody.innerHTML = `<tr><td colspan="3" class="empty-row">Loading catalog…</td></tr>`;
  try {
    catalog = await api("/api/inventory/products");
    renderCatalog();
  } catch (e) {
    catalog = [];
    catalogBody.innerHTML = `<tr><td colspan="3" class="empty-row">Couldn't load the catalog. Is inventory-service registered and the gateway running?</td></tr>`;
    itemChecklist.innerHTML = `<p class="empty-row">Catalog unavailable.</p>`;
  }
}

// Recompute the order amount whenever a checkbox changes - the one real
// improvement over the old form, where amount was a disconnected free-text
// field the customer had to add up themselves.
itemChecklist.addEventListener("change", () => {
  const checked = [...itemChecklist.querySelectorAll('input[type="checkbox"]:checked')];
  const total = checked.reduce((sum, box) => sum + Number(box.dataset.price), 0);
  amountInput.value = total.toFixed(2);
});

// ================= Orders =================
function renderOrders(orders) {
  if (!orders || orders.length === 0) {
    ordersBody.innerHTML = `<tr><td colspan="6" class="empty-row">No orders yet.</td></tr>`;
    return;
  }
  ordersBody.innerHTML = orders.slice().reverse().map(o => `
    <tr>
      <td>${o.id}</td>
      <td>${escapeHtml(o.customerId)}</td>
      <td>${escapeHtml(o.itemNames)}</td>
      <td>${escapeHtml(o.paymentMethod)}</td>
      <td>${money(o.amount)}</td>
      <td>${escapeHtml(o.status)}</td>
    </tr>
  `).join("");
}

async function loadOrders() {
  ordersBody.innerHTML = `<tr><td colspan="6" class="empty-row">Loading orders…</td></tr>`;
  try {
    const orders = await api("/api/orders");
    renderOrders(orders);
  } catch (e) {
    ordersBody.innerHTML = `<tr><td colspan="6" class="empty-row">Couldn't load orders. Is order-service registered and the gateway running?</td></tr>`;
  }
}

// ================= Place order =================
function showBanner(kind, message) {
  orderBanner.hidden = false;
  orderBanner.className = "banner " + kind;
  orderBanner.textContent = message;
}

orderForm.addEventListener("submit", async (e) => {
  e.preventDefault();

  const customerId = document.getElementById("customerId").value.trim();
  const paymentMethod = document.getElementById("paymentMethod").value;
  const amount = Number(amountInput.value);
  const itemNames = [...itemChecklist.querySelectorAll('input[type="checkbox"]:checked')]
    .map(box => box.value);

  if (itemNames.length === 0) {
    showBanner("error", "Check at least one item before placing an order.");
    return;
  }

  submitBtn.disabled = true;
  submitBtn.textContent = "Placing order…";

  try {
    const result = await api("/api/orders", {
      method: "POST",
      body: JSON.stringify({ customerId, itemNames, paymentMethod, amount }),
    });

    if (result && result.success) {
      showBanner("success", `Order placed — #${result.orderId} for ${customerId}.`);
      orderForm.reset();
      amountInput.value = "";
    } else {
      showBanner("error", (result && result.message) || "Order failed. Check stock and payment method.");
    }
  } catch (err) {
    showBanner("error", "Couldn't reach order-service through the gateway. Check that every service is registered in Eureka.");
  } finally {
    submitBtn.disabled = false;
    submitBtn.textContent = "Place order";
    await loadCatalog(); // stock may have changed
    await loadOrders();
  }
});

// ================= Manual refresh buttons =================
document.getElementById("refreshCatalog").addEventListener("click", loadCatalog);
document.getElementById("refreshOrders").addEventListener("click", loadOrders);

// ================= Small helpers =================
function escapeHtml(s) {
  return String(s).replace(/[&<>"']/g, c => ({
    "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;",
  }[c]));
}
function escapeAttr(s) {
  return escapeHtml(s);
}

// ================= Boot =================
checkStatus();
loadCatalog();
loadOrders();
setInterval(checkStatus, 15000);
