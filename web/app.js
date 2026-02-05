const el = (id) => document.getElementById(id);
const msg = (t) => el("msg").textContent = t;

let currentRestaurantId = null;
let currentMenu = [];
let orderCart = new Map(); // menuItemId -> {item, qty}

async function api(path, options = {}) {
    const res = await fetch(path, {
        headers: { "Content-Type": "application/json" },
        ...options
    });
    const text = await res.text();
    if (!res.ok) throw new Error(text || ("HTTP " + res.status));
    return text ? JSON.parse(text) : null;
}

/* ---------- Restaurants ---------- */
async function loadRestaurants() {
    const list = await api("/restaurants");
    const sel = el("restSelect");
    sel.innerHTML = "";
    list.forEach(r => {
        const opt = document.createElement("option");
        opt.value = r.id;
        opt.textContent = `${r.id}: ${r.name}`;
        sel.appendChild(opt);
    });
    msg("Restaurants loaded");
}

async function addRestaurant() {
    const name = el("restName").value.trim();
    if (!name) return msg("Enter restaurant name");
    await api("/restaurants", { method: "POST", body: JSON.stringify({ name }) });
    el("restName").value = "";
    await loadRestaurants();
    msg("Restaurant added");
}

async function openRestaurant() {
    currentRestaurantId = el("restSelect").value;
    if (!currentRestaurantId) return msg("Select a restaurant");
    await loadMenu();
    switchTab("menu");
    msg(`Opened restaurant #${currentRestaurantId}`);
}

/* ---------- Menu ---------- */
function applyMenuView() {
    const q = el("menuSearch").value.trim().toLowerCase();
    const sort = el("menuSort").value;

    let list = currentMenu.slice();

    if (q) list = list.filter(x => (x.name || "").toLowerCase().includes(q));

    if (sort === "price") list.sort((a,b) => (a.price ?? 0) - (b.price ?? 0));
    else list.sort((a,b) => (a.name || "").localeCompare(b.name || ""));

    renderMenu(list);
    renderOrderPickList(list);
}

async function loadMenu() {
    if (!currentRestaurantId) return;
    currentMenu = await api(`/restaurants/${currentRestaurantId}/menu`);
    applyMenuView();
}

async function addMenuItem() {
    if (!currentRestaurantId) return msg("Open a restaurant first");
    const name = el("itemName").value.trim();
    const price = Number(el("itemPrice").value);
    const category = el("itemCategory").value;

    if (!name) return msg("Enter item name");
    if (!Number.isFinite(price) || price < 0) return msg("Enter valid price");

    await api(`/restaurants/${currentRestaurantId}/menu`, {
        method: "POST",
        body: JSON.stringify({ name, price, category })
    });

    el("itemName").value = "";
    el("itemPrice").value = "";
    await loadMenu();
    msg("Menu item added");
}

async function updatePrice(menuItemId, newPrice) {
    if (!Number.isFinite(newPrice) || newPrice < 0) return msg("Invalid price");
    await api(`/menu-items/${menuItemId}/price`, {
        method: "PUT",
        body: JSON.stringify({ price: newPrice })
    });
    await loadMenu();
    msg("Price updated");
}

async function deleteItem(menuItemId) {
    await fetch(`/menu-items/${menuItemId}`, { method: "DELETE" });
    await loadMenu();
    msg("Item deleted ");
}

function renderMenu(list) {
    const root = el("menuList");
    root.innerHTML = "";

    if (!currentRestaurantId) {
        root.innerHTML = "<p>Open a restaurant to view menu.</p>";
        return;
    }
    if (!list.length) {
        root.innerHTML = "<p>No menu items yet.</p>";
        return;
    }

    list.forEach(m => {
        const row = document.createElement("div");
        row.style.cssText = "display:flex;gap:10px;align-items:center;padding:10px;border-top:1px solid #eee;flex-wrap:wrap";

        row.innerHTML = `
      <strong style="flex:1;min-width:180px">${m.name}</strong>
      <span style="min-width:70px">${m.category || ""}</span>
      <span style="min-width:90px">$${m.price}</span>
      <input class="newPrice" type="number" step="0.01" placeholder="New price" style="width:120px;padding:8px;border:1px solid #ddd;border-radius:10px">
      <button class="upd" style="padding:8px 10px;border-radius:10px;border:0;cursor:pointer">Update</button>
      <button class="del" style="padding:8px 10px;border-radius:10px;border:0;cursor:pointer">Delete</button>
    `;

        row.querySelector(".upd").onclick = () => {
            const v = Number(row.querySelector(".newPrice").value);
            updatePrice(m.id, v).catch(e => msg("Error: " + e.message));
        };
        row.querySelector(".del").onclick = () => {
            deleteItem(m.id).catch(e => msg("Error: " + e.message));
        };

        root.appendChild(row);
    });
}

/* ---------- Orders ---------- */
function renderOrderPickList(list) {
    const root = el("orderPickList");
    root.innerHTML = "";

    if (!currentRestaurantId) {
        root.innerHTML = "<p>Open a restaurant first.</p>";
        return;
    }
    if (!list.length) {
        root.innerHTML = "<p>No items to order.</p>";
        return;
    }

    list.forEach(m => {
        const row = document.createElement("div");
        row.style.cssText = "display:flex;gap:10px;align-items:center;padding:10px;border-top:1px solid #eee;flex-wrap:wrap";

        const qty = orderCart.get(m.id)?.qty ?? 0;

        row.innerHTML = `
      <strong style="flex:1;min-width:180px">${m.name}</strong>
      <span style="min-width:90px">$${m.price}</span>
      <button class="minus" style="padding:6px 10px;border-radius:10px;border:0;cursor:pointer">-</button>
      <span class="qty" style="min-width:20px;text-align:center">${qty}</span>
      <button class="plus" style="padding:6px 10px;border-radius:10px;border:0;cursor:pointer">+</button>
    `;

        row.querySelector(".plus").onclick = () => {
            const cur = orderCart.get(m.id) || { item: m, qty: 0 };
            cur.qty += 1;
            orderCart.set(m.id, cur);
            applyMenuView();
            updateTotal();
        };
        row.querySelector(".minus").onclick = () => {
            const cur = orderCart.get(m.id);
            if (!cur) return;
            cur.qty -= 1;
            if (cur.qty <= 0) orderCart.delete(m.id);
            else orderCart.set(m.id, cur);
            applyMenuView();
            updateTotal();
        };

        root.appendChild(row);
    });

    updateTotal();
}

function updateTotal() {
    let total = 0;
    for (const { item, qty } of orderCart.values()) total += (item.price ?? 0) * qty;
    el("orderTotal").textContent = total.toFixed(2);
}

async function createOrder() {
    if (!currentRestaurantId) return msg("Open a restaurant first");
    if (orderCart.size === 0) return msg("Pick at least 1 item");

    const items = Array.from(orderCart.values()).map(x => ({
        menuItemId: x.item.id,
        quantity: x.qty
    }));

    const total = Number(document.getElementById("orderTotal").textContent);

    const res = await api("/orders", {
        method: "POST",
        body: JSON.stringify({
            restaurantId: Number(currentRestaurantId),
            total,
            items
        })
    });

    msg((res.text || "").replaceAll("\\n", "\n"));

    orderCart.clear();
    applyMenuView();
    updateTotal();
}


async function loadOrders() {
    if (!currentRestaurantId) return msg("Open a restaurant first");

    // If your API uses another URL, change here only:
    const list = await api(`/orders?restaurantId=${currentRestaurantId}`);
    const root = el("ordersList");
    root.innerHTML = "";

    if (!list.length) {
        root.innerHTML = "<p>No orders yet.</p>";
        return;
    }

    list.forEach(o => {
        const div = document.createElement("div");
        div.style.cssText = "padding:10px;border-top:1px solid #eee";
        div.innerHTML = `<strong>Order #${o.id}</strong> — total: ${o.total ?? ""} — items: ${o.itemsCount ?? 0}`;
        root.appendChild(div);
    });
}

function switchTab(name) {
    document.querySelectorAll(".tab").forEach(t => t.style.display = "none");
    el(`tab-${name}`).style.display = "block";
}

document.querySelectorAll(".tabBtn").forEach(b => {
    b.onclick = () => switchTab(b.dataset.tab);
});

/* ---------- Events ---------- */
el("addRestBtn").onclick = () => addRestaurant().catch(e => msg("Error: " + e.message));
el("reloadRestsBtn").onclick = () => loadRestaurants().catch(e => msg("Error: " + e.message));
el("openRestaurantBtn").onclick = () => openRestaurant().catch(e => msg("Error: " + e.message));

el("reloadMenuBtn").onclick = () => loadMenu().catch(e => msg("Error: " + e.message));
el("addItemBtn").onclick = () => addMenuItem().catch(e => msg("Error: " + e.message));
el("menuSearch").oninput = applyMenuView;
el("menuSort").onchange = applyMenuView;

el("createOrderBtn").onclick = () => createOrder().catch(e => msg("Error: " + e.message));
el("reloadOrdersBtn").onclick = () => loadOrders().catch(e => msg("Error: " + e.message));

/* ---------- Init ---------- */
loadRestaurants().catch(e => msg("Error: " + e.message));
switchTab("menu");
