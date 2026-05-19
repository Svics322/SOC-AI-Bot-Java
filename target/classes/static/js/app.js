const messages = document.getElementById("messages");
const form = document.getElementById("chat-form");
const input = document.getElementById("message-input");
const analysis = document.getElementById("analysis");
const shutdownButton = document.getElementById("shutdown-button");

function addMessage(role, text) {
  const item = document.createElement("div");
  item.className = `message ${role}`;
  const avatar = document.createElement("div");
  avatar.className = "avatar";
  avatar.textContent = role === "user" ? "Ви" : "SOC";
  const bubble = document.createElement("div");
  bubble.className = "bubble";
  bubble.textContent = text;
  item.appendChild(avatar);
  item.appendChild(bubble);
  messages.appendChild(item);
  messages.scrollTop = messages.scrollHeight;
}

function esc(text) {
  return text.replace(/[&<>"']/g, symbol => ({
    "&": "&amp;",
    "<": "&lt;",
    ">": "&gt;",
    '"': "&quot;",
    "'": "&#039;"
  })[symbol]);
}

function renderAnalysis(response) {
  const entitiesHtml = Object.entries(response.entities || {})
    .map(([key, values]) => {
      const tags = values
        .map(value => `<span class="entity">${esc(value)}</span>`)
        .join("");
      return `<div class="kv"><strong>${key}:</strong><br>${tags}</div>`;
    })
    .join("");
  const topHtml = (response.topIntents || [])
    .map(intent => {
      const score = (intent.score * 100).toFixed(1);
      return `<div class="kv"><strong>${esc(intent.intent)}</strong>: ${score}%</div>`;
    })
    .join("");
  const cardHtml = response.incidentCard
    ? `<div class="kv"><strong>Картка:</strong><pre>${esc(JSON.stringify(response.incidentCard, null, 2))}</pre></div>`
    : "";
  const confidence = (response.confidence * 100).toFixed(1);

  analysis.innerHTML = `
    <div class="kv"><strong>Intent:</strong> ${esc(response.intent)}</div>
    <div class="kv"><strong>Confidence:</strong> ${confidence}%</div>
    <div class="kv"><strong>Severity:</strong> ${esc(response.severity)}</div>
    <div class="kv"><strong>Top intents:</strong>${topHtml}</div>
    <div class="kv"><strong>Entities:</strong>${entitiesHtml || "<p class='muted'>Не виявлено.</p>"}</div>
    ${cardHtml}
  `;
}

form.addEventListener("submit", async event => {
  event.preventDefault();
  const text = input.value.trim();
  if (!text) return;

  addMessage("user", text);
  input.value = "";

  const response = await fetch("/api/chat", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ message: text })
  });
  const data = await response.json();
  let answer = data.answer;

  if (data.recommendations?.length) {
    answer += "\n\nРекомендовані дії:";
    data.recommendations.forEach((item, index) => answer += `\n${index + 1}. ${item}`);
  }
  if (data.incidentCard) {
    answer += "\n\nКартку інциденту сформовано. Деталі показано в панелі NLU-аналізу.";
  }

  addMessage("bot", answer);
  renderAnalysis(data);
});

shutdownButton.addEventListener("click", async () => {
  shutdownButton.disabled = true;
  shutdownButton.textContent = "Завершення...";
  try {
    const response = await fetch("/api/system/shutdown", { method: "POST" });
    const data = await response.json();
    addMessage("bot", `${data.message}\nМожна закрити цю вкладку браузера.`);
  } catch {
    addMessage("bot", "Застосунок уже завершується. Можна закрити цю вкладку браузера.");
  }
});
