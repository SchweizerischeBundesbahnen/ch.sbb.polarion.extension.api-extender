import { afterEach, describe, expect, it, vi } from 'vitest';
import { cleanup, render } from 'vitest-browser-react';
import App from '../src/App';
import { installFetchMock } from './mockFetch';

// Secondary branches of an authorization page: the confirm dialogs answered with Cancel, a
// stored setting whose role arrays are absent, unticking a role, and the error-message extraction
// fallbacks of the settings service. The main flows live in Authorization.test.tsx.

const SCOPE = 'project/elibrary/';
const origUrl = window.location.pathname + window.location.search;
const setUrl = (search: string) => window.history.replaceState({}, '', search);

const ROLES = { globalRoles: ['admin', 'user'], projectRoles: ['project_admin'] };

const authRoutes = (content: unknown = { globalRoles: ['admin'], projectRoles: [] }) => [
  { method: 'GET', match: /\/version$/, json: { bundleBuildTimestamp: '2026-01-01 00:00' } },
  { method: 'GET', match: /\/roles\?/, json: ROLES },
  { method: 'GET', match: /\/settings\/project_custom_fields\/names\/Default\/content\?/, json: content },
  { method: 'PUT', match: /\/settings\/project_custom_fields\/names\/Default\/content\?/, json: {} },
  {
    method: 'GET',
    match: /\/settings\/project_custom_fields\/default-content$/,
    json: { globalRoles: [], projectRoles: [] },
  },
  { method: 'GET', match: /\/settings\/project_custom_fields\/names\/Default\/revisions\?/, json: [] },
];

const sbbButton = (label: string): HTMLButtonElement => {
  const b = Array.from(document.querySelectorAll<HTMLButtonElement>('.sbb-btn')).find(
    (x) => (x.textContent ?? '').trim() === label,
  );
  if (!b) throw new Error(`button "${label}" not found`);
  return b;
};

const roleCheckbox = (role: string): HTMLInputElement => {
  const label = Array.from(document.querySelectorAll('.roles-list label')).find((l) => l.textContent?.trim() === role);
  const cb = label?.querySelector<HTMLInputElement>('input[type="checkbox"]');
  if (!cb) throw new Error(`role checkbox "${role}" not found`);
  return cb;
};

/** Answer the confirmation dialog the page renders in place of the former window.confirm. */
async function answerDialog(label: 'OK' | 'Cancel') {
  await vi.waitFor(() => expect(document.querySelector('.rsp-modal')).not.toBeNull());
  const button = Array.from(document.querySelectorAll<HTMLButtonElement>('.rsp-modal-footer .sbb-btn')).find(
    (b) => (b.textContent ?? '').trim() === label,
  );
  if (!button) throw new Error(`dialog button "${label}" not found`);
  button.click();
}

async function mountLoaded(routes = authRoutes()) {
  installFetchMock(routes);
  setUrl(`?feature=project-custom-fields&embedded=true&scope=${encodeURIComponent(SCOPE)}`);
  render(<App />);
  await vi.waitFor(() => expect(document.querySelector('.roles-list')).not.toBeNull(), { timeout: 5000 });
}

afterEach(() => {
  cleanup();
  vi.unstubAllGlobals();
  vi.restoreAllMocks();
  setUrl(origUrl);
});

describe('Authorization confirm dialogs', () => {
  it('keeps the edits when Cancel is not confirmed', async () => {
    await mountLoaded();
    roleCheckbox('user').click();
    await vi.waitFor(() => expect(roleCheckbox('user').checked).toBe(true));

    sbbButton('Cancel').click();
    // The dialog was dismissed, so nothing is reloaded and the pending tick survives.
    await answerDialog('Cancel');
    expect(roleCheckbox('user').checked).toBe(true);
  });

  it('keeps the edits when Revert to default is not confirmed', async () => {
    await mountLoaded();
    expect(roleCheckbox('admin').checked).toBe(true);

    sbbButton('Default').click();
    await answerDialog('Cancel');
    expect(roleCheckbox('admin').checked).toBe(true);
  });
});

describe('Authorization selection', () => {
  it('unticks a role and drops it from the saved payload', async () => {
    let saved: { globalRoles: string[]; projectRoles: string[] } | null = null;
    await mountLoaded([
      {
        method: 'PUT',
        match: /\/settings\/project_custom_fields\/names\/Default\/content\?/,
        respond: (_url, init) => {
          saved = JSON.parse(String(init?.body));
          return new Response('{}', { status: 200 });
        },
      },
      ...authRoutes(),
    ]);
    expect(roleCheckbox('admin').checked).toBe(true);
    roleCheckbox('admin').click();
    await vi.waitFor(() => expect(roleCheckbox('admin').checked).toBe(false));

    sbbButton('Save').click();
    await vi.waitFor(() => expect(saved).not.toBeNull(), { timeout: 5000 });
    expect(saved!.globalRoles).toEqual([]);
  });

  it('treats a setting without role arrays as an empty selection', async () => {
    // A configuration saved before both role kinds existed: neither array is present.
    await mountLoaded(authRoutes({}));
    expect(roleCheckbox('admin').checked).toBe(false);
    expect(roleCheckbox('project_admin').checked).toBe(false);
  });
});

describe('Authorization error reporting', () => {
  it('shows the load error when reloading on Cancel fails', async () => {
    let calls = 0;
    await mountLoaded([
      {
        method: 'GET',
        match: /\/settings\/project_custom_fields\/names\/Default\/content\?/,
        respond: () => {
          calls += 1;
          // The first call is the initial load; the reload triggered by Cancel fails.
          return calls === 1
            ? new Response(JSON.stringify({ globalRoles: ['admin'], projectRoles: [] }), { status: 200 })
            : new Response('', { status: 500 });
        },
      },
      ...authRoutes(),
    ]);
    sbbButton('Cancel').click();
    await answerDialog('OK');
    await vi.waitFor(() => expect(document.querySelector('.alert-error')).not.toBeNull(), { timeout: 5000 });
  });

  it('shows the load error when the default content cannot be read', async () => {
    await mountLoaded([
      {
        method: 'GET',
        match: /\/settings\/project_custom_fields\/default-content$/,
        respond: () => new Response('', { status: 503 }),
      },
      ...authRoutes(),
    ]);
    sbbButton('Default').click();
    await answerDialog('OK');
    await vi.waitFor(() => expect(document.querySelector('.alert-error')).not.toBeNull(), { timeout: 5000 });
  });

  it('reports the errorMessage field when the save fails with a JSON body', async () => {
    await mountLoaded([
      {
        method: 'PUT',
        match: /\/settings\/project_custom_fields\/names\/Default\/content\?/,
        respond: () =>
          new Response(JSON.stringify({ errorMessage: 'role service down' }), {
            status: 500,
            headers: { 'Content-Type': 'application/json' },
          }),
      },
      ...authRoutes(),
    ]);
    sbbButton('Save').click();
    await vi.waitFor(() => expect(document.body.textContent).toContain('role service down'), { timeout: 5000 });
  });

  it('falls back to the HTTP status when the failed save has no body', async () => {
    await mountLoaded([
      {
        method: 'PUT',
        match: /\/settings\/project_custom_fields\/names\/Default\/content\?/,
        respond: () => new Response('', { status: 502 }),
      },
      ...authRoutes(),
    ]);
    sbbButton('Save').click();
    await vi.waitFor(() => expect(document.body.textContent).toContain('HTTP 502'), { timeout: 5000 });
  });
});
