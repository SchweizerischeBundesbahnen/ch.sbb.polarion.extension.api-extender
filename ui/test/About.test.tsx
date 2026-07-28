import { afterEach, describe, expect, it, vi } from 'vitest';
import { cleanup, render } from 'vitest-browser-react';
import App from '../src/App';
import { installFetchMock, jsonResponse } from './mockFetch';

// The About page is a thin wrapper feeding react-sbb-polarion's shared About component this app's
// sendRequest / appIcon / restApiUrl. Rendered through the router with the generic About endpoints
// mocked; RSP owns the deeper coverage of the component itself.

const origUrl = window.location.pathname + window.location.search;
const setUrl = (search: string) => window.history.replaceState({}, '', search);

const aboutRoutes = () => [
  {
    method: 'GET',
    match: /\/version$/,
    json: { bundleName: 'API Extender', bundleVendor: 'SBB', bundleVersion: '3.1.2' },
  },
  { method: 'GET', match: /\/configuration-properties$/, json: { properties: [], obsoleteProperties: [] } },
  { method: 'GET', match: /\/configuration-status/, json: [] },
  { method: 'GET', match: /\/readme$/, respond: () => new Response('<h1>Readme</h1>', { status: 200 }) },
];

afterEach(() => {
  cleanup();
  vi.unstubAllGlobals();
  setUrl(origUrl);
});

describe('About page (wrapper)', () => {
  it('renders the shared About page with the extension info and icon', async () => {
    installFetchMock(aboutRoutes());
    setUrl('?feature=about&embedded=true');
    render(<App />);
    await vi.waitFor(() => expect(document.body.textContent).toContain('API Extender'));
    expect(document.querySelector('.about-page .app-icon')).not.toBeNull();
  });

  it('shows an error alert when a required endpoint fails', async () => {
    installFetchMock([
      { method: 'GET', match: /\/version$/, respond: () => jsonResponse({ errorMessage: 'boom' }, 500) },
      { method: 'GET', match: /\/configuration-properties$/, json: { properties: [], obsoleteProperties: [] } },
      { method: 'GET', match: /\/configuration-status/, json: [] },
      { method: 'GET', match: /\/readme$/, respond: () => new Response('', { status: 404 }) },
    ]);
    setUrl('?feature=about&embedded=true');
    render(<App />);
    await vi.waitFor(() => expect(document.querySelector('.alert-error')).not.toBeNull());
  });
});
