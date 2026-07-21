// Vercel serverless function. Matches extracted receipt item names against
// the user's uploaded photo library keywords (e.g. "2% milk" -> "milk").
// Requires OPENAI_API_KEY set as an environment variable in Vercel.

export default async function handler(req, res) {
  if (req.method !== "POST") {
    return res.status(405).json({ error: "Method not allowed" });
  }

  const { itemNames, library } = req.body;
  if (!Array.isArray(itemNames) || !Array.isArray(library)) {
    return res.status(400).json({ error: "Missing itemNames or library" });
  }

  // Nothing to match against — skip the API call entirely.
  if (library.length === 0) {
    const empty = {};
    itemNames.forEach(name => { empty[name] = null; });
    return res.status(200).json({ matches: empty });
  }

  const keywords = library.map(entry => entry.keyword);

  try {
    const response = await fetch("https://api.openai.com/v1/chat/completions", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${process.env.OPENAI_API_KEY}`,
      },
      body: JSON.stringify({
        model: "gpt-4o-mini",
        max_tokens: 500,
        messages: [
          {
            role: "user",
            content: `Match each grocery item name to the closest keyword from the list, if any reasonably applies. If nothing fits, use null.

Item names: ${JSON.stringify(itemNames)}
Available keywords: ${JSON.stringify(keywords)}

Return ONLY a JSON object mapping each item name to its matched keyword (or null), no other text, no markdown fences. Example: {"2% milk": "milk", "granny smith apples": "apples", "shampoo": null}`
          }
        ]
      })
    });

    const data = await response.json();
    if (!response.ok) {
      return res.status(502).json({ error: data.error?.message || "OpenAI request failed" });
    }

    const raw = data.choices?.[0]?.message?.content || "{}";
    const cleaned = raw.replace(/```json|```/g, "").trim();

    let keywordMatches;
    try {
      keywordMatches = JSON.parse(cleaned);
    } catch {
      const empty = {};
      itemNames.forEach(name => { empty[name] = null; });
      return res.status(200).json({ matches: empty });
    }

    // Resolve matched keywords back to actual image URLs.
    const matches = {};
    for (const name of itemNames) {
      const matchedKeyword = keywordMatches[name];
      const libraryEntry = library.find(entry => entry.keyword === matchedKeyword);
      matches[name] = libraryEntry ? libraryEntry.image_url : null;
    }

    return res.status(200).json({ matches });
  } catch (err) {
    return res.status(500).json({ error: err.message });
  }
}
