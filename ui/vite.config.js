import react from '@vitejs/plugin-react';
import { existsSync, readFileSync } from 'node:fs';
import { join } from 'node:path';
import { fileURLToPath } from 'node:url';
import { defineConfig, loadEnv } from 'vite';

// Where the Maven build renders the help articles (markdown2html), next to the app bundle in the webapp.
const RENDERED_ARTICLES = fileURLToPath(
  new URL('../src/main/resources/webapp/api-extender-app/html/', import.meta.url),
);

// `vite dev` counterpart of the webapp's html/: a documentation page fetches `../../html/<id>.html` relative to
// the app, which under the dev server's root is `/html/<id>.html`. Without this Vite's SPA fallback answers that
// with index.html and a 200, so the page shows an empty article instead of the article or its "not generated"
// message. A file the Maven build has not rendered yet answers 404, which is what the page reports as not
// generated. Only a flat `<name>.html` is served, so a request cannot reach outside the directory.
function serveRenderedArticles() {
  return {
    name: 'serve-rendered-articles',
    configureServer(server) {
      server.middlewares.use('/html', (req, res) => {
        let name = '';
        try {
          name = decodeURIComponent((req.url ?? '').split('?')[0]).replace(/^\//, '');
        } catch {
          // a malformed escape: no such article
        }
        const file = join(RENDERED_ARTICLES, name);
        if (!/^[\w-]+\.html$/.test(name) || !existsSync(file)) {
          res.statusCode = 404;
          res.end();
          return;
        }
        res.setHeader('Content-Type', 'text/html; charset=utf-8');
        res.end(readFileSync(file));
      });
    },
  };
}

export default defineConfig(({ command, mode }) => {
  const env = loadEnv(mode, process.cwd(), '');
  const polarionUrl = env.VITE_BASE_URL || 'http://localhost';

  // The shared @sbb-polarion/react-sbb-polarion package is linked via a `file:`
  // dependency, which npm symlinks into node_modules together with its own dev copies of React and
  // sonner. Dedupe so the app and the linked library resolve to this app's single instance of each:
  // React (avoids the dual-React "invalid hook call") and sonner (the RSP `Toaster` host and this
  // app's `toast()` calls must share one sonner instance, or fired toasts never reach the host).
  // Harmless once the package is consumed from a registry.
  const resolve = { dedupe: ['react', 'react-dom', 'sonner'] };

  if (command === 'serve') {
    return {
      plugins: [react(), serveRenderedArticles()],
      resolve,
      server: {
        proxy: {
          '/polarion/api-extender/rest': {
            target: polarionUrl,
            changeOrigin: true,
          },
          '/polarion/rest': {
            target: polarionUrl,
            changeOrigin: true,
          },
          '/polarion/ria': {
            target: polarionUrl,
            changeOrigin: true,
          },
          '/polarion/icons': {
            target: polarionUrl,
            changeOrigin: true,
          },
        },
      },
    };
  }

  return {
    plugins: [react()],
    resolve,
    // Never let a developer's personal access token reach a shipped bundle. VITE_BEARER_TOKEN is a
    // `vite dev` convenience (it switches useRemote to the token-authenticated /api endpoints); Vite
    // inlines import.meta.env.VITE_* at build time, so a local .env.local would otherwise be baked
    // into the bundle that `mvn -P local-install-into-polarion` deploys, readable by everyone the SPA is
    // served to. Forcing it undefined here keeps production on the session-authenticated /internal
    // endpoints, which is what Polarion provides anyway.
    define: { 'import.meta.env.VITE_BEARER_TOKEN': 'undefined' },
    base: '/polarion/api-extender-app/ui/app/',
    build: {
      outDir: './dist/app',
      emptyOutDir: true,
    },
  };
});
