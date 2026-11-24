import { afterEach, describe, expect, it, vi } from 'vitest';
import { cleanup, render } from 'vitest-browser-react';
import { page } from 'vitest/browser';
import App from '../src/App';
import { installFetchMock } from './mockFetch';
import { settleBeforeCapture, settleLayout } from './visualHelpers';

// Docker-only snapshot of a documentation-site page: DocPage fetches the static user-guide.html (mocked) and
// wraps it in the sidebar / "on this page" / prev-next frame. What is pinned is that frame and the markdown
// styling, not the article.

// The documentation search box renders only when the search index has records, and the real index is a
// build artifact that is empty unless the articles were rendered by Maven first. Pin it to a fixed record so
// the frame looks the same whichever way the suite was started.
vi.mock('../src/docs/searchIndex', () => ({
  SEARCH_INDEX: [
    { doc: 'user-guide', docTitle: 'User Guide', anchor: 'global-records', title: 'Global records', text: 'One Two' },
  ],
}));

const origUrl = window.location.pathname + window.location.search;

const ARTICLE =
  '<h1>User Guide</h1><p>Store your own values in projects and in the repository.</p>' +
  '<h2 id="global-records">Global records</h2><ul><li>Step one</li><li>Step two</li></ul>';

afterEach(() => {
  cleanup();
  vi.unstubAllGlobals();
  window.history.replaceState({}, '', origUrl);
  window.top?.document.querySelectorAll('script[id$="-breadcrumb-bridge"]').forEach((s) => s.remove());
});

describe.skipIf(!__PIXEL_REFERENCES__)('Documentation site visual', () => {
  it('renders an article in the documentation frame', async () => {
    installFetchMock([
      { method: 'GET', match: /\/html\/user-guide\.html$/, respond: () => new Response(ARTICLE, { status: 200 }) },
    ]);
    window.history.replaceState({}, '', '?feature=user-guide&embedded=true');
    render(<App />);
    await vi.waitFor(() => expect(document.querySelector('article.markdown-body h2')).not.toBeNull());
    const app = document.querySelector('.app') as HTMLElement;
    await settleLayout();
    await page.viewport(1280, Math.ceil(app.scrollHeight) + 40);
    await settleBeforeCapture();
    await expect(page.elementLocator(app)).toMatchScreenshot('user-guide-loaded');
  });
});
