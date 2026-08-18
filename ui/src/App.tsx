import { Toaster } from '@sbb-polarion/react-sbb-polarion';
import { findFeature } from './features';
import Landing from './pages/Landing';

/**
 * Top-level feature router. There is a single index.html / bundle; the page to show is chosen from the
 * `feature` query parameter, which is what hivemodule.xml points its extenders at: `?feature=about`,
 * `?feature=project-custom-fields` and `?feature=global-records`.
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
    // `.app` supplies the base font/padding (App.css); `standard-admin-page` scopes the shared generic
    // checkbox styling (bundled in react-sbb-polarion's style.css) and the --sbb-* control tokens.
    <div className="app standard-admin-page">
      {/* App-wide toast host: the shared react-sbb-polarion Toaster (top-center + richColors, so
          success toasts are green, errors red). Toasts are fired with `toast()` from sonner. */}
      <Toaster />
      <Page />
    </div>
  );
}
