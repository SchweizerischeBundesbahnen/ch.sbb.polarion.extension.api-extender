import { afterEach, describe, expect, it, vi } from 'vitest';
import { cleanup, render } from 'vitest-browser-react';
import App from '../src/App';
import { type FetchMock, installFetchMock, jsonResponse } from './mockFetch';

// Behavior tests for an authorization page (the React port of settings.jsp), driven
// through the real App (feature router + Toaster). REST is mocked at the global fetch boundary.

const SCOPE = 'project/elibrary/';
const origUrl = window.location.pathname + window.location.search;
const setUrl = (search: string) => window.history.replaceState({}, '', search);

const CONTENT = { globalRoles: ['admin'], projectRoles: [] as string[] };

const authRoutes = () => [
  { method: 'GET', match: /\/version$/, json: { bundleBuildTimestamp: '2026-01-01 00:00' } },
  { method: 'GET', match: /\/roles\?/, json: { globalRoles: ['admin', 'user'], projectRoles: ['project_admin'] } },
  { method: 'GET', match: /\/settings\/project_custom_fields\/names\/Default\/content\?/, json: CONTENT },
  { method: 'PUT', match: /\/settings\/project_custom_fields\/names\/Default\/content\?/, json: {} },
  {
    method: 'GET',
    match: /\/settings\/project_custom_fields\/default-content$/,
    json: { globalRoles: [], projectRoles: [] },
  },
  {
    method: 'GET',
    match: /\/settings\/project_custom_fields\/names\/Default\/revisions\?/,
    json: [{ name: '4321', date: '2026-01-01', author: 'jdoe' }],
  },
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

let fetchMock: FetchMock;
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
  fetchMock = installFetchMock(routes);
  setUrl(`?feature=project-custom-fields&embedded=true&scope=${encodeURIComponent(SCOPE)}`);
  render(<App />);
  await vi.waitFor(() => expect(document.querySelector('.roles-list')).not.toBeNull());
}

afterEach(() => {
  cleanup();
  vi.unstubAllGlobals();
  vi.restoreAllMocks();
  setUrl(origUrl);
  window.top?.document.querySelectorAll('script[id$="-breadcrumb-bridge"]').forEach((s) => s.remove());
});

describe('Authorization page', () => {
  it('lists global and project roles and reflects the saved selection', async () => {
    await mountLoaded();
    expect(document.body.textContent).toContain('Global Roles');
    expect(document.body.textContent).toContain('Project Roles');
    expect(roleCheckbox('admin').checked).toBe(true);
    expect(roleCheckbox('user').checked).toBe(false);
    expect(roleCheckbox('project_admin').checked).toBe(false);
    const urls = fetchMock.mock.calls.map((c) => String(c[0]));
    expect(urls.some((u) => /\/roles\?/.test(u))).toBe(true);
  });

  it('saves the selected roles, splitting them into global and project', async () => {
    await mountLoaded();
    roleCheckbox('user').click();
    roleCheckbox('project_admin').click();
    sbbButton('Save').click();
    await vi.waitFor(() => {
      const put = fetchMock.mock.calls.find((c) => (c[1]?.method ?? 'GET') === 'PUT');
      expect(put).toBeTruthy();
    });
    const put = fetchMock.mock.calls.find((c) => (c[1]?.method ?? 'GET') === 'PUT')!;
    const body = JSON.parse(String(put[1]!.body));
    expect(body.globalRoles.sort()).toEqual(['admin', 'user']);
    expect(body.projectRoles).toEqual(['project_admin']);
    await vi.waitFor(() => expect(document.body.textContent).toContain('successfully saved'));
  });

  it('reverts to the default values via the Default button', async () => {
    await mountLoaded();
    expect(roleCheckbox('admin').checked).toBe(true);
    sbbButton('Default').click();
    await answerDialog('OK');
    await vi.waitFor(() => expect(roleCheckbox('admin').checked).toBe(false));
  });

  it('restores the persisted state on Cancel', async () => {
    await mountLoaded();
    roleCheckbox('user').click();
    expect(roleCheckbox('user').checked).toBe(true);
    sbbButton('Cancel').click();
    await answerDialog('OK');
    await vi.waitFor(() => expect(roleCheckbox('user').checked).toBe(false));
  });

  it('shows revisions and reverts to a selected revision', async () => {
    // A revision whose content grants both global roles. It must precede the generic content route
    // (first match wins), otherwise the base content (admin only) would answer the revision request.
    const routes = [
      {
        method: 'GET',
        match: /\/settings\/project_custom_fields\/names\/Default\/content\?.*revision=4321/,
        json: { globalRoles: ['admin', 'user'], projectRoles: [] },
      },
      ...authRoutes(),
    ];
    await mountLoaded(routes);
    sbbButton('Revisions').click();
    await vi.waitFor(() => expect(document.querySelector('.revert-to-revision-button')).not.toBeNull());
    document.querySelector<HTMLButtonElement>('.revert-to-revision-button')!.click();
    await vi.waitFor(() => expect(roleCheckbox('user').checked).toBe(true));
  });

  it('shows an error alert when loading fails', async () => {
    installFetchMock([
      // errorMessage (not message) exercises the other branch of the settings error parser.
      { method: 'GET', match: /\/roles\?/, respond: () => jsonResponse({ errorMessage: 'boom' }, 500) },
      { method: 'GET', match: /\/settings\/project_custom_fields\/names\/Default\/content\?/, json: CONTENT },
    ]);
    setUrl(`?feature=project-custom-fields&embedded=true&scope=${encodeURIComponent(SCOPE)}`);
    render(<App />);
    await vi.waitFor(() => expect(document.querySelector('.alert-error')).not.toBeNull());
  });

  it('surfaces a save error as a toast', async () => {
    const routes = authRoutes().filter((r) => r.method !== 'PUT');
    routes.push({
      method: 'PUT',
      match: /\/settings\/project_custom_fields\/names\/Default\/content\?/,
      respond: () => jsonResponse({ message: 'save failed' }, 500),
    });
    await mountLoaded(routes);
    sbbButton('Save').click();
    await vi.waitFor(() => expect(document.body.textContent).toContain('save failed'));
  });

  it('surfaces a plain-text (non-JSON) error body from a failed save', async () => {
    const routes = authRoutes().filter((r) => r.method !== 'PUT');
    routes.push({
      method: 'PUT',
      match: /\/settings\/project_custom_fields\/names\/Default\/content\?/,
      respond: () => new Response('server exploded', { status: 500 }),
    });
    await mountLoaded(routes);
    sbbButton('Save').click();
    await vi.waitFor(() => expect(document.body.textContent).toContain('server exploded'));
  });
});
