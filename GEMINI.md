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
