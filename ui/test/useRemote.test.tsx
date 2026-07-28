import { afterEach, describe, expect, it, vi } from 'vitest';
import { cleanup, render } from 'vitest-browser-react';
import useRemote from '../src/services/useRemote';
import { type FetchMock, installFetchMock, jsonResponse } from './mockFetch';

// useRemote is exercised end-to-end through the pages; this covers its wrapper directly: the session
// REST base is used, and a fetch rejection becomes a 503 "network error" Response rather than throwing.

let api: ReturnType<typeof useRemote>;
function Capture() {
  api = useRemote();
  return null;
}

afterEach(() => {
  cleanup();
  vi.unstubAllGlobals();
  vi.unstubAllEnvs();
});

describe('useRemote', () => {
  it('prefixes the extension REST base and returns the response on success', async () => {
    const fetchMock: FetchMock = installFetchMock([
      { method: 'GET', match: /\/rest\/(internal|api)\/version$/, json: { versionNumber: '1.2.3' } },
    ]);
    render(<Capture />);
    await vi.waitFor(() => expect(api).toBeTruthy());
    const res = await api.sendRequest({ method: 'GET', url: '/version' });
    expect(res.status).toBe(200);
    expect((await res.json()).versionNumber).toBe('1.2.3');
    expect(String(fetchMock.mock.calls[0][0])).toMatch(/\/polarion\/api-extender\/rest\/(internal|api)\/version$/);
  });

  it('returns a 503 network-error response when fetch rejects', async () => {
    vi.stubGlobal(
      'fetch',
      vi.fn(async () => {
        throw new Error('down');
      }),
    );
    render(<Capture />);
    await vi.waitFor(() => expect(api).toBeTruthy());
    const res = await api.sendRequest({ method: 'POST', url: '/scan', body: '{}', contentType: 'application/json' });
    expect(res.status).toBe(503);
    expect((await res.json()).message).toContain('Network error');
  });

  it('uses the /api base and sends bearer auth when VITE_BEARER_TOKEN is set', async () => {
    vi.stubEnv('VITE_BEARER_TOKEN', 'secret');
    const fetchMock: FetchMock = installFetchMock([
      { method: 'GET', match: /\/rest\/api\/version$/, json: { ok: true } },
    ]);
    render(<Capture />);
    await vi.waitFor(() => expect(api).toBeTruthy());
    const res = await api.sendRequest({ method: 'GET', url: '/version' });
    expect(res.status).toBe(200);
    expect(String(fetchMock.mock.calls[0][0])).toMatch(/\/polarion\/api-extender\/rest\/api\/version$/);
    const headers = fetchMock.mock.calls[0][1]?.headers as Record<string, string>;
    expect(headers['Authorization']).toBe('Bearer secret');
  });

  it('does not throw on an error status', async () => {
    installFetchMock([{ method: 'GET', match: /\/boom$/, json: { message: 'nope' }, status: 500 }]);
    render(<Capture />);
    await vi.waitFor(() => expect(api).toBeTruthy());
    const res = await api.sendRequest({ method: 'GET', url: '/boom' });
    expect(res.status).toBe(500);
  });
});

describe('mockFetch helpers', () => {
  it('builds a JSON response with the given status', async () => {
    const res = jsonResponse({ a: 1 }, 201);
    expect(res.status).toBe(201);
    expect(await res.json()).toEqual({ a: 1 });
  });
});
