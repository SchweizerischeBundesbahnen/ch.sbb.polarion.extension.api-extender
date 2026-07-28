import { afterEach, describe, expect, it, vi } from 'vitest';
import { getCookie, setCookie } from '../src/services/cookies';
import { fetchProjects } from '../src/services/projects';
import { getScope } from '../src/services/scope';
import { type FetchMock, installFetchMock } from './mockFetch';

const PROJECTS_MATCH = /\/polarion\/rest\/v1\/projects/;

function setSearch(search: string): void {
  window.history.replaceState({}, '', `/${search}`);
}

describe('scope service', () => {
  afterEach(() => setSearch(''));

  it('normalizes a missing trailing slash on project scope', () => {
    setSearch('?scope=project/elibrary');
    expect(getScope()).toBe('project/elibrary/');
  });

  it('keeps an existing trailing slash and returns empty for global scope', () => {
    setSearch('?scope=project/elibrary/');
    expect(getScope()).toBe('project/elibrary/');
    setSearch('');
    expect(getScope()).toBe('');
  });
});

describe('cookie service', () => {
  it('round-trips a value and returns null for a missing key', () => {
    const key = `test-cookie-${Math.floor(performance.now())}`;
    expect(getCookie(key)).toBeNull();
    setCookie(key, 'hello world');
    expect(getCookie(key)).toBe('hello world');
  });

  it('encodes and decodes special characters', () => {
    const key = `test-cookie-enc-${Math.floor(performance.now())}`;
    setCookie(key, 'a=b; c/d');
    expect(getCookie(key)).toBe('a=b; c/d');
  });
});

describe('projects service', () => {
  afterEach(() => {
    vi.unstubAllGlobals();
    vi.unstubAllEnvs();
  });

  it('maps id/name with fallbacks, drops entries without an id, and sorts by name', async () => {
    installFetchMock([
      {
        method: 'GET',
        match: PROJECTS_MATCH,
        json: {
          data: [
            { id: 'zeta', attributes: { name: 'zebra' } },
            { attributes: { id: 'alpha', name: 'apple' } }, // id falls back to attributes.id
            { id: 'mango' }, // name falls back to the id
            { attributes: { name: 'orphan' } }, // no id anywhere -> dropped
          ],
        },
      },
    ]);

    const projects = await fetchProjects();

    // Sorted by name (apple < mango < zebra), the id-less entry filtered out.
    expect(projects).toEqual([
      { id: 'alpha', name: 'apple' },
      { id: 'mango', name: 'mango' },
      { id: 'zeta', name: 'zebra' },
    ]);
  });

  it('returns an empty list when the response has no data array', async () => {
    installFetchMock([{ method: 'GET', match: PROJECTS_MATCH, json: {} }]);
    expect(await fetchProjects()).toEqual([]);
  });

  it('sends a bearer token when VITE_BEARER_TOKEN is set', async () => {
    vi.stubEnv('VITE_BEARER_TOKEN', 'secret-token');
    const fetchMock: FetchMock = installFetchMock([{ method: 'GET', match: PROJECTS_MATCH, json: { data: [] } }]);

    await fetchProjects();

    const headers = fetchMock.mock.calls[0][1]?.headers as Record<string, string>;
    expect(headers['Authorization']).toBe('Bearer secret-token');
  });

  it('throws on a non-ok response', async () => {
    installFetchMock([{ method: 'GET', match: PROJECTS_MATCH, json: {}, status: 500 }]);
    await expect(fetchProjects()).rejects.toThrow('HTTP 500');
  });
});
