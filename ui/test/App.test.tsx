import { afterEach, describe, expect, it, vi } from 'vitest';
import { cleanup, render } from 'vitest-browser-react';
import App from '../src/App';
import { installFetchMock } from './mockFetch';

// The feature router (App.tsx): the `?feature=` param picks the page. The two authorization pages are
// the same component over two different named settings, which is the part worth pinning here - the
// setting name never appears in the URL, so only the router decides which one is written.

const SCOPE = 'project/elibrary/';
const origUrl = window.location.pathname + window.location.search;
const setUrl = (search: string) => window.history.replaceState({}, '', search);

const settingsRoutes = (seen: string[]) => [
  { method: 'GET', match: /\/version$/, json: { bundleBuildTimestamp: '2026-01-01 00:00' } },
  { method: 'GET', match: /\/roles\?/, json: { globalRoles: ['admin'], projectRoles: [] } },
  {
    method: 'GET',
    match: /\/settings\/[^/]+\/names\/Default\/content\?/,
    respond: (url: string) => {
      seen.push(url);
      return new Response(JSON.stringify({ globalRoles: ['admin'], projectRoles: [] }), {
        headers: { 'Content-Type': 'application/json' },
      });
    },
  },
];

afterEach(() => {
  cleanup();
  vi.unstubAllGlobals();
  setUrl(origUrl);
});

describe('feature router', () => {
  it('opens the project custom fields page against its own setting', async () => {
    const seen: string[] = [];
    installFetchMock(settingsRoutes(seen));
    setUrl(`?feature=project-custom-fields&embedded=true&scope=${encodeURIComponent(SCOPE)}`);
    render(<App />);

    await vi.waitFor(() => expect(document.querySelector('.roles-list')).not.toBeNull());
    expect(document.querySelector('h1')!.textContent).toBe('Project Custom Fields');
    expect(seen.some((url) => url.includes('/settings/project_custom_fields/'))).toBe(true);
  });

  it('opens the global records page against its own setting', async () => {
    const seen: string[] = [];
    installFetchMock(settingsRoutes(seen));
    setUrl(`?feature=global-records&embedded=true&scope=${encodeURIComponent(SCOPE)}`);
    render(<App />);

    await vi.waitFor(() => expect(document.querySelector('.roles-list')).not.toBeNull());
    expect(document.querySelector('h1')!.textContent).toBe('Global Records');
    expect(seen.some((url) => url.includes('/settings/global_records/'))).toBe(true);
  });

  it('falls back to the dev Landing for an unknown feature', async () => {
    installFetchMock([{ method: 'GET', match: /\/polarion\/rest\/v1\/projects/, json: { data: [] } }]);
    setUrl('?feature=does-not-exist');
    render(<App />);

    await vi.waitFor(() => expect(document.querySelector('.feature-list')).not.toBeNull());
  });

  it('renders the dev Landing for a bare URL, listing every page', async () => {
    installFetchMock([{ method: 'GET', match: /\/polarion\/rest\/v1\/projects/, json: { data: [] } }]);
    setUrl('?');
    render(<App />);

    await vi.waitFor(() => expect(document.querySelector('.feature-list')).not.toBeNull());
    const links = Array.from(document.querySelectorAll('.feature-list a')).map((a) => a.textContent);
    expect(links).toContain('About');
    expect(links).toContain('Project Custom Fields');
    expect(links).toContain('Global Records');
  });

  it('shows a friendly error on the Landing when projects cannot be loaded', async () => {
    installFetchMock([
      { method: 'GET', match: /\/polarion\/rest\/v1\/projects/, json: { message: 'nope' }, status: 500 },
    ]);
    setUrl('?feature=landing');
    render(<App />);

    await vi.waitFor(() => expect(document.querySelector('.alert-error')).not.toBeNull());
    expect(document.querySelector('.alert-error')!.textContent).toContain('Could not load projects');
  });
});
