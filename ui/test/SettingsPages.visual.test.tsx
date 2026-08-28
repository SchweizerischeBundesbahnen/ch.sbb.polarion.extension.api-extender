import { afterEach, describe, expect, it, vi } from 'vitest';
import { cleanup, render } from 'vitest-browser-react';
import { page } from 'vitest/browser';
import App from '../src/App';
import { installFetchMock } from './mockFetch';
import type { Route } from './mockFetch';
import { settleBeforeCapture } from './visualHelpers';

// Docker-only snapshots of the two authorization pages. Both are react-sbb-polarion's shared
// AuthorizationSettings, so their look comes entirely from the library - and the one difference this
// extension makes is visual: Global Records offers no project roles, because the setting stores none.

const origUrl = window.location.pathname + window.location.search;

const roles = (projectRoles: string[]): Route => ({
  method: 'GET',
  match: /\/roles\?/,
  json: { globalRoles: ['admin', 'user'], projectRoles },
});

const content = (feature: string): Route => ({
  method: 'GET',
  match: new RegExp(`/settings/${feature}/names/Default/content`),
  json: { globalRoles: ['admin'], projectRoles: [] },
});

async function snapshot(feature: string, routes: Route[], name: string) {
  installFetchMock(routes);
  window.history.replaceState({}, '', `?feature=${feature}&embedded=true&scope=project/elibrary/`);
  render(<App />);

  await vi.waitFor(() =>
    expect(document.querySelectorAll('.roles-list input[type="checkbox"]').length).toBeGreaterThan(0),
  );
  const app = document.querySelector('.app') as HTMLElement;
  await page.viewport(1280, Math.ceil(app.scrollHeight) + 40);
  await settleBeforeCapture();
  await expect(page.elementLocator(app)).toMatchScreenshot(name);
}

afterEach(() => {
  cleanup();
  vi.unstubAllGlobals();
  window.history.replaceState({}, '', origUrl);
});

describe.skipIf(!__PIXEL_REFERENCES__)('Authorization pages visual', () => {
  it('project custom fields: global and project roles', async () => {
    await snapshot(
      'project-custom-fields',
      [roles(['project_admin', 'project_user']), content('project_custom_fields')],
      'project-custom-fields-loaded',
    );
  });

  it('global records: no project roles offered', async () => {
    await snapshot(
      'global-records',
      [roles(['project_admin', 'project_user']), content('global_records')],
      'global-records-loaded',
    );
  });
});
