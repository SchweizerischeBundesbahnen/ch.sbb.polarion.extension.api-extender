import { DocLinkInterceptor, DocsProvider, Toaster, buildDocsConfig } from '@sbb-polarion/react-sbb-polarion';
import { DOC_ORDER } from './docs/manifest';
import { SEARCH_INDEX } from './docs/searchIndex';
import { SOURCE_BASE_URL } from './docs/source';
import { findFeature } from './features';
import Landing from './pages/Landing';
import { switchToFeatureNode } from './services/adminNav';

/**
 * Documentation-site configuration for the shared components (@sbb-polarion/react-sbb-polarion): the article
 * manifest, the build-generated search index, and the admin-shell sync a cross-document link triggers. The
 * help articles cross-link with plain relative markdown links so they work as-is on GitHub; the build leaves
 * them relative, and DocLinkInterceptor resolves a click at runtime - a `.md` source through `mdLinkMap` to a
 * `?feature=` switch, and any other relative link (docs/openapi.json) to `sourceBaseUrl` on GitHub.
 * `onDocLinkNavigate` prefers switching the admin shell's node (so Polarion's breadcrumb and left menu
 * follow); it returns false when it cannot, and the interceptor then falls back to a plain in-frame navigation.
 */
const docsConfig = buildDocsConfig({
  docs: DOC_ORDER,
  searchIndex: SEARCH_INDEX,
  sourceBaseUrl: SOURCE_BASE_URL,
  // The README is not a doc-site article, but it is the About page: a link to it switches there.
  extraMdLinks: { 'README.md': 'about' },
  onDocLinkNavigate: switchToFeatureNode,
});

/**
 * Top-level feature router. There is a single index.html / bundle; the page to show is chosen from the
 * `feature` query parameter, which is what hivemodule.xml points its extenders at: `?feature=about`,
 * `?feature=project-custom-fields`, `?feature=global-records` and the documentation articles of
 * `src/docs/docs.config.json` (`?feature=quick-start` and the others).
 *
 * The two settings features are the same page over two different named settings - the JSP they replace
 * was one file opened twice with a `settings_name` parameter. Giving each its own feature id keeps the
 * ids aligned with the extender ids and keeps the setting out of the URL.
 *
 * A missing or unknown feature (including the bare root `/`) renders the dev-only Landing stub - a
 * scope picker plus links to every feature - so the app can be exercised in `vite dev` without Polarion.
 *
 * No BreadcrumbInjector here: this extension contributes no navigation topic, only administration
 * pages, which Polarion opens embedded.
 */
export default function App() {
  const feature = new URLSearchParams(window.location.search).get('feature');
  const Page = findFeature(feature)?.component ?? Landing;

  return (
    <DocsProvider config={docsConfig}>
      {/* Delegates cross-document help-article links to in-app feature navigation (see docsConfig). */}
      <DocLinkInterceptor>
        {/* `.app` supplies the base font/padding (App.css); `standard-admin-page` scopes the shared generic
            checkbox styling (bundled in react-sbb-polarion's style.css) and the --sbb-* control tokens. */}
        <div className="app standard-admin-page">
          {/* App-wide toast host: the shared react-sbb-polarion Toaster (top-center + richColors, so
              success toasts are green, errors red). Toasts are fired with `toast()` from sonner. */}
          <Toaster />
          <Page />
        </div>
      </DocLinkInterceptor>
    </DocsProvider>
  );
}
