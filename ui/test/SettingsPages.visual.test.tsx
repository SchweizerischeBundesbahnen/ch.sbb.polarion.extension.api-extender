import { afterEach, describe, expect, it, vi } from 'vitest';
import { cleanup, render } from 'vitest-browser-react';
import { page } from 'vitest/browser';
import App from '../src/App';
import { installFetchMock } from './mockFetch';
import type { Route } from './mockFetch';
import { settleBeforeCapture, settleLayout } from './visualHelpers';

// Docker-only snapshots of the two authorization pages. Both are react-sbb-polarion's shared
// AuthorizationSettings, so their look comes entirely from the library - and the one difference this
// extension makes is visual: Global Records offers no project roles, because the setting stores none.
// Each role set is a multi-select SearchableSelect, so what these pin is the trigger with its chips,
// not the former checkbox list.

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

async function snapshot(feature: string, routes: Route[], name: string, controls: number) {
  installFetchMock(routes);
  window.history.replaceState({}, '', `?feature=${feature}&embedded=true&scope=project/elibrary/`);
  render(<App />);

  // Every control, not just the first: they are upgraded asynchronously, and a capture taken between
  // two of them catches the page mid-upgrade.
  await vi.waitFor(() => expect(document.querySelectorAll('.roles-group .sd-trigger-multi')).toHaveLength(controls));
  const app = document.querySelector('.app') as HTMLElement;
  await settleLayout();
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
      2,
    );
  });

  it('global records: no project roles offered', async () => {
    await snapshot(
      'global-records',
      [roles(['project_admin', 'project_user']), content('global_records')],
      'global-records-loaded',
      1,
    );
  });
});
