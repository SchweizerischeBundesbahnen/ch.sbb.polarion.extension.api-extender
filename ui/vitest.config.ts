import react from '@vitejs/plugin-react';
import { playwright } from '@vitest/browser-playwright';
import { defineConfig } from 'vitest/config';

// Vitest browser mode (real Chromium via Playwright), the same setup as react-sbb-polarion: behavior
// assertions see real CSS/layout and the visual layer (toMatchScreenshot) captures the real look. The
// extension's REST calls are mocked at the global fetch level (see test/mockFetch.ts), so no Polarion
// is needed. Reference screenshots are committed and MUST be generated in the pinned Playwright Docker
// image (npm run test:update:docker) so Windows-dev and Linux-CI produce identical pixels.

// Per-component subfolder derived from the test file name (e.g. "About.visual.test.tsx" -> "About").
const componentDir = (testFileName: string): string => testFileName.split(/[\\/]/).pop()!.split('.')[0];

// The committed reference screenshots are pixel-locked to the pinned Playwright image, so the visual
// assertions are meaningful only there. scripts/docker-test.mjs sets PIXEL_REFERENCES=1 inside the
// container; everywhere else (a developer's macOS/Windows box, a plain CI runner) the visual suites
// skip themselves rather than failing on the host's font metrics - which shift both the antialiasing
// and the rendered element height, i.e. a red run that says nothing about the code.
const pixelReferences = process.env.PIXEL_REFERENCES === '1';

export default defineConfig({
  define: { __PIXEL_REFERENCES__: JSON.stringify(pixelReferences) },
  plugins: [react()],
  // Single instance of each shared dep (react-sbb-polarion is a file: symlink with its own copies):
  // React avoids the dual-React invalid-hook-call; sonner ensures this app's `toast()` and RSP's
  // `Toaster` host share one instance so fired toasts actually render.
  resolve: { dedupe: ['react', 'react-dom', 'sonner'] },
  // Pre-bundle these so Vite does not discover a new dependency mid-run and reload the browser page
  // (which intermittently fails a test file with "Vitest failed to find the runner"). Matters most on
  // a fresh `npm ci` in Docker where there is no warm dep-optimize cache.
  optimizeDeps: {
    include: [
      'react',
      'react-dom',
      'react-dom/client',
      'react/jsx-runtime',
      'react/jsx-dev-runtime',
      'sonner',
      'vitest-browser-react',
      '@grigoriev/react-sbb-polarion',
    ],
  },
  test: {
    include: ['test/**/*.{test,spec}.{ts,tsx}'],
    setupFiles: ['./test/setup.ts'],
    // Run test files one at a time. Under high parallelism the Playwright browser provider
    // intermittently fails a worker with "Vitest failed to find the runner"; serializing the files
    // avoids that race. The suite is small and each file is fast, so the cost is minor.
    fileParallelism: false,
    browser: {
      enabled: true,
      // deviceScaleFactor: 2 -> all visual-regression references are captured at 2x (sharper, and finer
      // diffs). Set on the provider's contextOptions (not the instance - the provider reads it there).
      provider: playwright({ contextOptions: { deviceScaleFactor: 2 } }),
      headless: true,
      instances: [{ browser: 'chromium', viewport: { width: 1280, height: 720 } }],
      expect: {
        toMatchScreenshot: {
          resolveScreenshotPath: ({ root, arg, ext, testFileName }) =>
            `${root}/test/expected/${componentDir(testFileName)}/${arg}${ext}`,
          resolveDiffPath: ({ root, arg, ext, testFileName }) =>
            `${root}/test/__diff__/${componentDir(testFileName)}/${arg}${ext}`,
        },
      },
    },
    coverage: {
      // istanbul (source instrumented at transform time), NOT v8: in browser mode v8 intermittently
      // reports 0% depending on the dep-optimization cache. `all: false` so the istanbul uncovered-files
      // pass (which can crash in browser mode) never runs.
      provider: 'istanbul',
      reporter: ['text', 'html', 'lcov'],
      reportsDirectory: './coverage',
      all: false,
      include: ['src/**'],
      // Excluded: declaration files, CSS, the bootstrap entry (main.tsx), and the dev-only Landing page
      // (vite-dev scaffolding never opened in Polarion; the router test covers its selection logic).
      // Do NOT exclude real product code to hit the gate - and do not exclude `types.ts`: it holds only
      // interfaces, which erase at transpile, so listing it hides nothing and blurs this list.
      exclude: ['src/**/*.d.ts', 'src/**/*.css', 'src/main.tsx', 'src/pages/Landing.tsx'],
      thresholds: {
        statements: 80,
        functions: 80,
        lines: 80,
        branches: 80,
      },
    },
  },
});
