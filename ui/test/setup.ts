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

// Transitions and animations are off for every capture. A screenshot taken mid-fade is a reference that
// only sometimes reproduces, and the durations are react-sbb-polarion's, which can change them without
// this repository noticing. Killing them removes the race instead of outrunning it with a sleep.
//
// Grayscale antialiasing is NOT pinned here: `-webkit-font-smoothing` is implemented only on macOS in
// Blink, so on the Linux container the rule parses and is ignored - a reference captured with it is
// byte-identical to one captured without. `--disable-lcd-text` in vitest.config.ts is the platform
// independent way to ask for the same thing.
const stillness = document.createElement('style');
stillness.textContent = '*, *::before, *::after { transition: none !important; animation: none !important; }';
document.head.appendChild(stillness);
