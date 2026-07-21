// Vercel serverless function. Requires OPENAI_API_KEY set as an
// environment variable in the Vercel project — never in this file.

export default async function handler(req, res) {
  if (req.method !== "POST") {
    return res.status(405).json({ error: "Method not allowed" });
  }

  const { imageBase64 } = req.body;
  if (!imageBase64) {
    return res.status(400).json({ error: "Missing imageBase64" });
  }

  try {
    const response = await fetch("https://api.openai.com/v1/chat/completions", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${process.env.OPENAI_API_KEY}`,
      },
      body: JSON.stringify({
        model: "gpt-4o",
        max_tokens: 1000,
        messages: [
          {
            role: "user",
            content: [
              {
                type: "text",
                text: `Extract the grocery items from this receipt photo. Return ONLY a JSON array, no other text, no markdown code fences. Each item: {"name": string, "quantity": number, "unit": string or null, "category": one of "produce","dairy","meat","pantry","frozen","bakery","other"}. Skip non-food lines like bags, tax, discounts, subtotal, loyalty points. Use your best guess for category based on the item name. If a quantity isn't clear, use 1.`
              },
              {
                type: "image_url",
                image_url: { url: imageBase64 }
              }
            ]
          }
        ]
      })
    });

    const data = await response.json();

    if (!response.ok) {
      return res.status(502).json({ error: data.error?.message || "OpenAI request failed" });
    }

    const raw = data.choices?.[0]?.message?.content || "[]";
    const cleaned = raw.replace(/```json|```/g, "").trim();

    let items;
    try {
      items = JSON.parse(cleaned);
    } catch {
      return res.status(502).json({ error: "Couldn't parse items from the receipt", raw: cleaned });
    }

    return res.status(200).json({ items });
  } catch (err) {
    return res.status(500).json({ error: err.message });
  }
}
