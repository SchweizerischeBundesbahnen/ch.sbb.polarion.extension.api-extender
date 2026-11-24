import { pageViolations } from '@sbb-polarion/react-sbb-polarion/testing';
import { afterEach, describe, expect, it, vi } from 'vitest';
import { cleanup, render } from 'vitest-browser-react';
import { userEvent } from 'vitest/browser';
import App from '../src/App';
import { DOC_ORDER } from '../src/docs/manifest';
import { installFetchMock } from './mockFetch';

// The documentation site: every article in the shared frame (breadcrumb, search, sidebar, "on this page",
// prev/next). The search index is generated from the Maven-rendered articles and is empty in a plain
// `npm test`, which hides the search box, so a fixture index stands in for it here.
vi.mock('../src/docs/searchIndex', () => ({
  SEARCH_INDEX: [
    {
      doc: 'user-guide',
      docTitle: 'User Guide',
      anchor: 'global-records',
      title: 'Global records',
      text: 'One value per key.',
    },
    { doc: 'rest-api', docTitle: 'REST API', anchor: 'regex-tool', title: 'Regex tool', text: 'Find the matches.' },
  ],
}));

const origUrl = window.location.pathname + window.location.search;

afterEach(() => {
  cleanup();
  vi.unstubAllGlobals();
  window.history.replaceState({}, '', origUrl);
  window.top?.document.querySelectorAll('script[id$="-breadcrumb-bridge"]').forEach((s) => s.remove());
});

const ARTICLE = '<h1>Article</h1><h2 id="section">Section</h2><p>Steps.</p>';

/** Opens one article and waits for it and the frame around it, the search box included. */
async function openArticle(id: string, respond = () => new Response(ARTICLE)) {
  installFetchMock([{ method: 'GET', match: new RegExp(`/html/${id}\\.html$`), respond }]);
  window.history.replaceState({}, '', `?feature=${id}&embedded=true`);
  render(<App />);
  await vi.waitFor(() => expect(document.querySelector('.docs-nav-link-active')).not.toBeNull());
  // Asserted, so the scans below fail if the fixture index stops reaching the app.
  await vi.waitFor(() => expect(document.querySelector('.docs-search-input')).not.toBeNull());
}

const search = () => document.querySelector<HTMLInputElement>('.docs-search-input')!;

describe('Documentation site', () => {
  it.each(DOC_ORDER.map((doc) => [doc.id, doc.title]))('renders the %s article in the shared frame', async (id) => {
    await openArticle(id);
    await vi.waitFor(() => expect(document.querySelector('article.markdown-body h2')).not.toBeNull());
    expect(document.querySelector('article.markdown-body')!.textContent).toContain('Steps.');
    // The sidebar lists every article of the manifest, with the open one marked.
    const nav = Array.from(document.querySelectorAll('.docs-nav-link')).map((link) => link.textContent);
    for (const doc of DOC_ORDER) {
      expect(nav).toContain(doc.title);
    }
    expect(document.querySelector('.docs-nav-link-active')!.textContent).toBe(
      DOC_ORDER.find((doc) => doc.id === id)!.title,
    );
  });

  it('says so when an article was not generated', async () => {
    await openArticle('user-guide', () => new Response('', { status: 404 }));
    await vi.waitFor(() =>
      expect(document.body.textContent).toContain('This article has not been generated during build.'),
    );
  });

  it('finds a section of another article', async () => {
    await openArticle('quick-start');
    await userEvent.fill(search(), 'regex');
    await vi.waitFor(() => expect(document.querySelector('.docs-search-result')).not.toBeNull());
    expect(document.querySelector('.docs-search-result')!.textContent).toContain('Regex tool');
  });
});

describe('Documentation site, accessibility', () => {
  it.each(DOC_ORDER.map((doc) => doc.id))('has no WCAG A/AA violations on the %s article', async (id) => {
    await openArticle(id);
    await vi.waitFor(() => expect(document.querySelector('article.markdown-body h2')).not.toBeNull());
    expect(await pageViolations()).toEqual([]);
  });

  it('has no WCAG A/AA violations on an article that was not generated', async () => {
    await openArticle('user-guide', () => new Response('', { status: 404 }));
    await vi.waitFor(() =>
      expect(document.body.textContent).toContain('This article has not been generated during build.'),
    );
    expect(await pageViolations()).toEqual([]);
  });

  it('has no WCAG A/AA violations with search results listed', async () => {
    await openArticle('quick-start');
    await userEvent.fill(search(), 'record');
    await vi.waitFor(() => expect(document.querySelector('.docs-search-result')).not.toBeNull());
    expect(await pageViolations()).toEqual([]);
  });

  it('has no WCAG A/AA violations with a search that matches nothing', async () => {
    await openArticle('quick-start');
    await userEvent.fill(search(), 'nothing-matches-this');
    await vi.waitFor(() => expect(document.querySelector('.docs-search-empty')).not.toBeNull());
    expect(await pageViolations()).toEqual([]);
  });
});
