import { afterEach, describe, expect, it, vi } from 'vitest';
import { cleanup, render } from 'vitest-browser-react';
import { page } from 'vitest/browser';
import App from '../src/App';
import { installFetchMock } from './mockFetch';
import { settleBeforeCapture } from './visualHelpers';

// Docker-only full-page snapshot of the About page (shared RSP About component fed this app's
// endpoints, mocked). Covers the extension-info / properties / status tables and the README article.

const origUrl = window.location.pathname + window.location.search;

afterEach(() => {
  cleanup();
  vi.unstubAllGlobals();
  window.history.replaceState({}, '', origUrl);
  window.top?.document.querySelectorAll('script[id$="-breadcrumb-bridge"]').forEach((s) => s.remove());
});

describe.skipIf(!__PIXEL_REFERENCES__)('About page visual', () => {
  it('loaded (info + properties + status tables, README article)', async () => {
    installFetchMock([
      {
        method: 'GET',
        match: /\/version$/,
        json: {
          bundleName: 'API Extender',
          bundleVendor: 'SBB',
          supportEmail: 'support@example.com',
          automaticModuleName: 'ch.sbb.polarion.extension.api_extender',
          bundleVersion: '3.1.2',
          bundleBuildTimestamp: '2026-07-01 10:00',
        },
      },
      {
        method: 'GET',
        match: /\/configuration-properties$/,
        json: {
          properties: [
            { key: 'ch.sbb.api-extender.debug', value: 'false', defaultValue: 'false', description: 'Debug logging' },
          ],
          obsoleteProperties: [],
        },
      },
      {
        method: 'GET',
        match: /\/configuration-status/,
        json: [{ name: 'Settings', status: 'OK', details: 'all healthy' }],
      },
      {
        method: 'GET',
        match: /\/readme$/,
        respond: () => new Response('<h1>API Extender</h1><p>Extra REST endpoints for Polarion.</p>', { status: 200 }),
      },
    ]);
    window.history.replaceState({}, '', '?feature=about&embedded=true');
    render(<App />);
    await vi.waitFor(() => expect(document.querySelector('article.markdown-body')).not.toBeNull());
    const app = document.querySelector('.app') as HTMLElement;
    await page.viewport(1280, Math.ceil(app.scrollHeight) + 40);
    await settleBeforeCapture();
    await expect(page.elementLocator(app)).toMatchScreenshot('about-loaded');
  });
});
