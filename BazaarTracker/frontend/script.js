
const params = new URLSearchParams(window.location.search);
const itemId = params.get("item") || "FINE_JASPER_GEM";
let chart = null;

function displayName(id) {
    return id.replaceAll("_", " ");
}

function formatCoins(value) {
    return Math.round(value).toLocaleString();
}

function formatPrice(value) {
    return value.toLocaleString(undefined, {
        minimumFractionDigits: 1,
        maximumFractionDigits: 1
    });
}

const itemName = document.getElementById("itemName");
const itemImage = document.getElementById("itemImage");

itemName.textContent = displayName(itemId);
itemImage.src =
    `https://sky.coflnet.com/static/icon/${encodeURIComponent(itemId)}`;
itemImage.alt = displayName(itemId);
itemImage.addEventListener("error", () => {
    itemImage.style.display = "none";
});

// --------------------
// Load Graph History
// --------------------

async function loadHistory(range = "all") {

    try {

        const response = await fetch(
            `http://localhost:8080/api/history/${encodeURIComponent(itemId)}?range=${encodeURIComponent(range)}`
        );

        if (!response.ok) {
            throw new Error("Failed to fetch history");
        }

        const history = await response.json();

        const labels = [];
        const buyPrices = [];
        const sellPrices = [];

        history.forEach(point => {

            labels.push(point.timestamp);
            buyPrices.push(point.buyPrice);
            sellPrices.push(point.sellPrice);

        });

        drawChart(labels, buyPrices, sellPrices);

        if (history.length > 0) {

            const latest = history[history.length - 1];

            document.getElementById("buyPrice").textContent =
                formatPrice(latest.buyPrice);

            document.getElementById("sellPrice").textContent =
                formatPrice(latest.sellPrice);
        }

    } catch (error) {

        console.error(error);

        alert("Failed to load history from backend.");
    }
}

// --------------------
// Load Craft Recipe
// --------------------

async function loadCraftRecipe() {

    const craftSection =
        document.getElementById("craftSection");

    const craftOutput =
        document.getElementById("craftOutput");

    const craftIngredients =
        document.getElementById("craftIngredients");

    const craftSummary =
        document.getElementById("craftSummary");

    try {

        const response = await fetch(
            `http://localhost:8080/api/craft/${encodeURIComponent(itemId)}`
        );

        if (!response.ok) {
            throw new Error("Failed to fetch recipe");
        }

        const text = await response.text();

        if (!text) {

            craftSection.style.display = "none";
            return;
        }

        const recipe = JSON.parse(text);

        craftOutput.textContent =
            recipe.outputCount > 1
                ? `${recipe.outputCount}× ${displayName(recipe.outputItemId)}`
                : displayName(recipe.outputItemId);

        craftIngredients.innerHTML = "";

        recipe.ingredients.forEach(ingredient => {

            const row = document.createElement("div");

            row.className = "craft-ingredient";

            row.innerHTML = `
                <img
                    src="https://sky.coflnet.com/static/icon/${encodeURIComponent(ingredient.itemId)}"
                    alt=""
                >
                <span>
                    <strong>${ingredient.amount.toLocaleString()}× ${displayName(ingredient.itemId)}</strong>
                    <small>${formatCoins(ingredient.unitPrice)} coins each</small>
                </span>
                <b>${formatCoins(ingredient.totalPrice)} coins</b>
            `;

            row.querySelector("img").addEventListener("error", event => {
                event.currentTarget.style.visibility = "hidden";
            });

            craftIngredients.appendChild(row);
        });

        craftSummary.innerHTML = `
            <span>Craft cost <strong>${formatCoins(recipe.craftCost)} coins</strong></span>
            <span>Sell value <strong>${formatCoins(recipe.sellPrice)} coins</strong></span>
            <span class="${recipe.profit >= 0 ? "profit" : "loss"}">
                Profit <strong>${recipe.profit >= 0 ? "+" : ""}${formatCoins(recipe.profit)} coins</strong>
            </span>
        `;

    } catch (error) {

        console.error(error);

        craftIngredients.innerHTML =
            "<div class=\"empty-state\">Could not load this recipe.</div>";
    }
}

// --------------------
// Draw Chart
// --------------------

function drawChart(labels, buyPrices, sellPrices) {

    const ctx = document.getElementById("priceChart");

    if (chart) {
        chart.destroy();
    }

    chart = new Chart(ctx, {

        type: "line",

        data: {

            labels: labels,

            datasets: [

                {
                    label: "Buy Price",
                    data: buyPrices,
                    borderColor: "#36A2EB",
                    fill: false
                },

                {
                    label: "Sell Price",
                    data: sellPrices,
                    borderColor: "#ff5555",
                    fill: false
                }

            ]

        },

        options: {

            responsive: true,

            interaction: {

                mode: "index",
                intersect: false

            }

        }

    });

}

// --------------------
// Search Autocomplete
// --------------------

const searchBox = document.getElementById("searchBox");
const suggestions = document.getElementById("suggestions");

let allItems = [];

async function loadItems() {

    try {

        const response = await fetch(
            "http://localhost:8080/api/items"
        );

        allItems = await response.json();

        console.log("Loaded", allItems.length, "items.");

    } catch (error) {

        console.error(error);

    }

}

loadItems();
searchBox.addEventListener("input", () => {

    const query =
        searchBox.value
            .trim()
            .toUpperCase();

    suggestions.innerHTML = "";

    if (query.length < 2) {

        suggestions.style.display = "none";

        return;

    }

    const matches = allItems
        .filter(item =>
            item.replaceAll("_", " ").includes(query)
        )
        .slice(0, 15);

    matches.forEach(item => {

        const div = document.createElement("div");

        div.className = "suggestion";

        div.textContent =
            item.replaceAll("_", " ");

        div.onclick = () => {

            window.location.href =
                `item.html?item=${encodeURIComponent(item)}`;

        };

        suggestions.appendChild(div);

    });

    suggestions.style.display =
        matches.length > 0
            ? "block"
            : "none";

});

// --------------------
// Initial Load
// --------------------

loadHistory("all");
loadCraftRecipe();
