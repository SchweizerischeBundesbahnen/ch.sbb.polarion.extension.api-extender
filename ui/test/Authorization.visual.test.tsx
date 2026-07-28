import { afterEach, describe, expect, it, vi } from 'vitest';
import { cleanup, render } from 'vitest-browser-react';
import { page } from 'vitest/browser';
import App from '../src/App';
import { installFetchMock } from './mockFetch';

// Docker-only full-page snapshot of an authorization page (project custom fields): the global/project role checkboxes,
// the Save/Cancel/Default/Revisions toolbar and the Quick Help section.

const SCOPE = 'project/elibrary/';
const origUrl = window.location.pathname + window.location.search;

afterEach(() => {
  cleanup();
  vi.unstubAllGlobals();
  window.history.replaceState({}, '', origUrl);
  window.top?.document.querySelectorAll('script[id$="-breadcrumb-bridge"]').forEach((s) => s.remove());
});

describe.skipIf(!__PIXEL_REFERENCES__)('Authorization page visual', () => {
  it('loaded (global + project roles, toolbar, quick help)', async () => {
    installFetchMock([
      {
        method: 'GET',
        match: /\/roles\?/,
        json: { globalRoles: ['admin', 'user', 'developer'], projectRoles: ['project_admin', 'lead'] },
      },
      {
        method: 'GET',
        match: /\/settings\/project_custom_fields\/names\/Default\/content\?/,
        json: { globalRoles: ['admin'], projectRoles: ['project_admin'] },
      },
    ]);
    window.history.replaceState(
      {},
      '',
      `?feature=project-custom-fields&embedded=true&scope=${encodeURIComponent(SCOPE)}`,
    );
    render(<App />);
    await vi.waitFor(() => expect(document.querySelector('.roles-list')).not.toBeNull());
    const app = document.querySelector('.app') as HTMLElement;
    await page.viewport(1280, Math.ceil(app.scrollHeight) + 40);
    await expect(page.elementLocator(app)).toMatchScreenshot('authorization-loaded');
  });
});
