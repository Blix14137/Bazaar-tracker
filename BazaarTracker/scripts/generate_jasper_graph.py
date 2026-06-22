import sqlite3
import json
from datetime import datetime
from pathlib import Path

DB_PATH = Path(__file__).resolve().parents[1] / "data" / "bazaar.db"
OUT_HTML = Path(__file__).resolve().parents[1] / "frontend" / "jasper.html"

SQL = """
SELECT timestamp, buy_price, sell_price, item_id
FROM bazaar_history
WHERE LOWER(item_id) LIKE '%jasper%'
ORDER BY timestamp ASC
"""


def fetch_rows(db_path=DB_PATH):
    conn = sqlite3.connect(str(db_path))
    conn.row_factory = sqlite3.Row
    cur = conn.cursor()
    cur.execute(SQL)
    rows = cur.fetchall()
    conn.close()
    return rows


def rows_to_json(rows):
    data = []
    for r in rows:
        ts = r["timestamp"]
        # Try to normalize timestamp
        try:
            # Attempt parse common formats
            dt = datetime.fromisoformat(ts)
            iso = dt.isoformat()
        except Exception:
            iso = ts
        data.append({
            "timestamp": iso,
            "buy_price": r["buy_price"],
            "sell_price": r["sell_price"],
            "item_id": r["item_id"]
        })
    return data


def generate_html(data, out_path=OUT_HTML):
    js_data = json.dumps(data)
    html = """
<!doctype html>
<html>
<head>
  <meta charset="utf-8">
  <title>Jasper Gemstones - Bazaar Graph</title>
  <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
  <style>body{{font-family:Arial,Helvetica,sans-serif;max-width:900px;margin:24px auto;padding:0 12px}}#notice{{color:#666}}</style>
</head>
<body>
  <h1>Jasper Gemstones - Bazaar Prices</h1>
  <p id="notice">Data points: <span id="count">0</span></p>
  <canvas id="chart" width="800" height="400"></canvas>
  <script>
    const raw = {{JS_DATA}};
    document.getElementById('count').innerText = raw.length;

    if (raw.length === 0) {{
      document.getElementById('notice').innerText = 'No jasper items found in bazaar.db.';
    }} else {{
      // Group by timestamp (there may be many different items with same timestamps)
      const labels = raw.map(r => r.timestamp);
      const buy = raw.map(r => r.buy_price);
      const sell = raw.map(r => r.sell_price);

      const ctx = document.getElementById('chart').getContext('2d');
      new Chart(ctx, {
        type: 'line',
        data: {
          labels: labels,
          datasets: [
            {
              label: 'Buy Price',
              data: buy,
              borderColor: 'rgba(75,192,192,1)',
              backgroundColor: 'rgba(75,192,192,0.2)',
              tension: 0.2
            },
            {
              label: 'Sell Price',
              data: sell,
              borderColor: 'rgba(255,99,132,1)',
              backgroundColor: 'rgba(255,99,132,0.2)',
              tension: 0.2
            }
          ]
        },
        options: {
          scales: {
            x: {
              type: 'time',
              time: { parser: false, tooltipFormat: 'yyyy-MM-dd HH:mm:ss', displayFormats: { millisecond: 'HH:mm', second:'HH:mm:ss', minute:'HH:mm', hour:'HH:mm' } },
              title: { display: true, text: 'Timestamp' }
            },
            y: {
              title: { display: true, text: 'Price' }
            }
          },
          plugins: {
            legend: { position: 'top' }
          }
        }
      });
    }}
  </script>
</body>
</html>
""".replace('{JS_DATA}', js_data)
    out_path.parent.mkdir(parents=True, exist_ok=True)
    out_path.write_text(html, encoding='utf-8')
    print(f"Wrote {out_path}")


if __name__ == '__main__':
    rows = fetch_rows()
    data = rows_to_json(rows)
    generate_html(data)
    print('Done.')
