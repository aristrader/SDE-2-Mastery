# ChatGPT Doc-Import Prompt

Paste this at the end of any ChatGPT study thread to get a raw, complete dump of the discussion. Bring the output back here via `temp.md` (repo root) — Claude does ALL the structuring, standards pass, splitting, and Notion formatting during import (routine in `CLAUDE.md` under "ChatGPT doc-import workflow"). ChatGPT's only job is complete, faithful capture.

> History: an earlier version of this prompt asked ChatGPT to structure the page (sections, Quick recall, format rules). Dropped deliberately — structuring happens at import time anyway, and source-side filtering ("skip basics") risked silent content loss.

---

```text
Dump this entire conversation as one Markdown document for my study notes. Don't worry about structure, section order, or polish — my own system will restructure and format it later. Your only job is COMPLETE and FAITHFUL capture:

- Include EVERY topic, sub-question, example, and tangent we discussed in this thread. Nothing gets dropped or summarized away — if we spent time on it, it's in the dump.
- Most important: every point where I was confused, guessed wrong, or asked a follow-up — capture my misconception AND the correction explicitly, marked clearly (e.g. "Misconception: ... / Correction: ..."). These are the most valuable parts.
- Do NOT add material we didn't discuss (no padding for completeness, no surveys of alternatives, no historical trivia). Only what was actually covered in this chat.
- Group related points together so the dump is readable, but don't sweat the ordering beyond that.
- Plain Markdown, no HTML, no emojis.

**CRITICAL RULE FOR LONG CHATS**: 
Do NOT attempt to dump a long conversation in a single response, as you will be forced to compress and lose details to fit your output limit.
1. Evaluate the length of our chat.
2. If it is long, break your dump into multiple chronological parts.
3. Generate ONLY Part 1 right now.
4. End Part 1 by explicitly stating: **"I have completed Part 1. This dump will require approximately [X] parts. Reply 'Continue' to proceed to the next part."**
5. Wait for me to reply "Continue" before generating the next part.

Output Part 1 (or the whole thing if it easily fits) as one Markdown code block so I can copy it cleanly.
```
