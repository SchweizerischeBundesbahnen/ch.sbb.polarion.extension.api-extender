// Runs before every test file (see vitest.config.ts setupFiles).
//
// Load the same stylesheets the app renders with so the browser paints components realistically:
//   1. react-sbb-polarion's bundled control CSS (tokens + buttons/inputs/checkboxes/searchable-dropdown/
//      alerts + the shared component styles), the same import main.tsx uses.
//   2. this app's own App.css.
// The Polarion-served stylesheet linked in index.html (presentation.css) is baseline chrome and is not loaded
// here. Also registers jest-dom matchers.
import '@sbb-polarion/react-sbb-polarion/style.css';
import '@testing-library/jest-dom/vitest';
import '../src/App.css';

// Chromium decides per layer how to rasterize text, and the decision depends on the compositing of the
// page as a whole - which differs between "this file ran on its own" and "this file ran after that one".
// The result is the same glyphs at the same coordinates with a different gamma, and a reference that
// agrees with the runs that had the same files ahead of it and with no others. Asking for grayscale
// explicitly takes the decision away from the compositor.
//
// Test-only, and the references are regenerated with it so they and the runs agree.
const textRendering = document.createElement('style');
textRendering.textContent = '*, *::before, *::after { -webkit-font-smoothing: antialiased !important; }';
document.head.appendChild(textRendering);
