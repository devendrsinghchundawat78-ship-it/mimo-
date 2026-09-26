# Project Rules & Guidelines

## 1. UI Copy & Feature Labeling (Strict Rule)
- **Zero Technical/Meta Text in UI**:
  - Never display developer notes, architecture details, or backend logic in the user interface (e.g., do NOT write "Login to connect database", "Connecting to DB", etc.).
  - UI text must be clean, minimal, and standard production-grade (e.g., simple "Sign In", "Password", "Email").

- **No Third-Party Reference Names in UI**:
  - When reference apps or benchmarks are mentioned in instructions (e.g., *"Make a player like MX Player"* or *"Design like Netflix"*), use them strictly for UX/functional inspiration.
  - **Never** write reference brand names (e.g., "MX Player") anywhere in the app's UI, titles, headers, or buttons unless explicitly requested.

- **Implement Features, Do Not Label/Announce Them**:
  - When instructed that the app has an "in-built player", "in-built browser", or any specific capability, this is an instruction to **build and embed the functional component directly**.
  - **Do NOT** display promotional or descriptive text in the app like *"In-built Player"* or *"This app features an in-built browser"*.
  - Render only the actual functional UI elements (e.g., the media player controls, the webview/browser viewport) without explanatory meta-tags.

- **Explicit Content Only**:
  - Do not introduce arbitrary titles, labels, or feature explanations unless specifically instructed by the user.

---

## 2. Zero Fake / Mock Data Policy (Strict Real Data)
- **No Mock or Dummy Data**:
  - Never populate screens, lists, cards, or buttons with fake/dummy mock data (e.g., fake users, hardcoded sample videos, placeholder cards pretending to be real items).
  - Every component, button, and displayed item must be wired to real data, genuine app state, local storage, or live network responses.
- **Genuine Empty States**:
  - When there is no real data available yet, show a clean, proper empty state instead of inventing fake items to fill space.

---

## 3. Strict Categorization & Information Architecture (Logical Hierarchy)
- **No Uncategorized / Random Placements**:
  - Never dump settings, functions, or controls directly into unrelated screens or top-level parent views.
  - *Example*: If asked to add a setting, do NOT place it loose on the Profile screen. The Profile screen must navigate to a dedicated **Settings** screen, and inside Settings, items must be grouped neatly under logical categories (e.g., *Account*, *Privacy*, *Display/Preferences*, *Storage*, *Network*).
- **Strict Category Grouping**:
  - All features and options must be systematically categorized into structured groups with appropriate hierarchy.
  - Keep each screen's purpose focused, logical, and cleanly segmented.
