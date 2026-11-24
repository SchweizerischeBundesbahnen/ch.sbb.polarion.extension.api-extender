# API Extender App (React UI)

This submodule contains the React frontend for the API Extender Polarion extension, built on the shared
`@sbb-polarion/react-sbb-polarion` (RSP) component library. It is a single Vite bundle
with feature routing by `?feature=<id>`, hosting three administration pages and the documentation site:

- **About** (`?feature=about`): the shared RSP About page.
- **Project Custom Fields** (`?feature=project-custom-fields`): which global and project roles may
  write project custom fields.
- **Global Records** (`?feature=global-records`): which global roles may write global records. Global
  records belong to the repository, so this page offers no project roles.

The last two are RSP's shared `AuthorizationSettings` page over two different named settings — the JSP
they replace was one file opened twice with a different `settings_name`.

`?feature=quick-start`, `?feature=user-guide`, `?feature=rest-api`, `?feature=velocity-tools` and
`?feature=velocity-2` are the articles of the [documentation site](#the-documentation-site), one per entry of
`src/docs/docs.config.json`. The single `documentation` admin node opens `quick-start`; the others have no menu
entry of their own. The ids must stay in sync with the `pageUrl`s in `src/main/resources/META-INF/hivemodule.xml` -
a mismatch shows up as a blank page in Polarion and no test catches it.

The app is built with Vite and React, producing a static bundle that gets embedded into the extension JAR during the Maven build.

## How it integrates with Polarion

1. **Entry points** — `hivemodule.xml` registers one administration menu entry per page, each opening
   `/polarion/api-extender-app/ui/app/index.html?feature=<id>&embedded=true&scope=$scope$`.

2. **Webapp registration** — `plugin.xml` declares a `api-extender-app` webapp. Polarion's Tomcat serves the static files through `ApiExtenderAppServlet` (mapped to `/ui/*`).

3. **REST communication** — The React app calls the existing REST API at `/polarion/api-extender/rest/internal/*` (or `/api/*` with a bearer token): the settings endpoints for the stored roles, and `/roles` for the roles the current scope offers. Both come from the generic parent; the role endpoints are opt-in and this extension registers them in `ApiExtenderRestApplication`.

4. **Build pipeline** — During `mvn package`, the `frontend-maven-plugin` runs `npm ci` and `npm run build` inside this folder. The `maven-resources-plugin` then copies `ui/dist/app/` into `src/main/resources/webapp/api-extender-app/app/`, so it ends up in the final JAR. `ci`, not `install`: the packaged bundle must come from the committed `package-lock.json`, the same graph the tests run against — so a `package.json` edit that is not reflected in the lock fails the build instead of being silently repaired. Locally you still use `npm install` (below), which is what updates the lock.

## The documentation site

Quick Start, User Guide, REST API, Velocity Tools and Velocity 2 are the markdown files at the repository root,
rendered as one documentation site: a sidebar, a search, a breadcrumb, prev/next and an "on this page"
rail. The components are RSP's (`DocsProvider`, `DocPage`, `DocLinkInterceptor`, `createAdminNav`); this
app supplies only its data. The setup mirrors pdf-exporter's and excel-importer's.

- **The manifest** is [`src/docs/docs.config.json`](src/docs/docs.config.json): one entry per article, in
  reading order - the sidebar and prev/next follow it. Each `id` is the feature id and the basename of the
  rendered `<id>.html`; `source` is its markdown file. [`src/features.tsx`](src/features.tsx) builds one
  `DocPage` feature per entry, so adding an article is a manifest entry, its markdown and its markdown2html
  execution in the pom; nothing in `src/` changes.
- **The articles** are rendered by the Maven build (markdown2html, in `generate-sources`) into
  `src/main/resources/webapp/api-extender-app/html/`, and `DocPage` fetches `../../html/<id>.html` from
  there. `npm run dev` serves that same directory at `/html/` (a plugin in `vite.config.js`), since Vite's
  SPA fallback would otherwise answer every article with `index.html`. Their relative links stay relative:
  `DocLinkInterceptor` (wrapping the whole app in [`App.tsx`](src/App.tsx)) turns a `.md` link to another
  article into a `?feature=` switch and opens any other relative link, e.g. `docs/openapi.json`, on GitHub.
  Screenshots live in `docs/user_guide/img/` and are embedded into the rendered HTML.
- **Admin-shell sync.** A link that leaves the page's admin node (an article linking to the README, which is
  the About page) switches Polarion's own node too, so its breadcrumb and left menu follow:
  [`src/services/adminNav.ts`](src/services/adminNav.ts) maps each feature to its node, and
  [`src/main.tsx`](src/main.tsx) resumes the stashed target before the first render.
- **The search index** `src/docs/search-index.json` is a build artifact and is **not committed**.
  [`scripts/build-docs-index.mjs`](scripts/build-docs-index.mjs) builds it from the rendered articles, one
  record per h2/h3/h4 with the heading id the article carries, before every build (`prebuild`, where a
  missing article or one without sections fails the build), dev server (`predev`) and test run (Vitest
  `globalSetup`). Without rendered articles it writes an empty index, and the search box is simply hidden -
  run the Maven build once to get the articles, and with them the search, into `npm run dev`.
- **Tests replace the index through `src/docs/searchIndex.ts`**, the module the app reads it from. A
  `vi.mock` of the JSON file itself is not applied in Vitest browser mode, so the wrapper is what
  `test/Documentation.test.tsx` mocks to reach the search.

## Local development

Prerequisites: Node.js 20+ installed.

```bash
# Install dependencies
npm install

# Start dev server with hot reload
npm run dev
```

By default the dev server runs on `http://localhost:5173`. A bare URL opens the dev landing page, which
lists every feature and lets you pick the project scope; to open one page directly, pass the same
parameters Polarion does:

```
http://localhost:5173/?feature=project-custom-fields&scope=project/elibrary/
```

To proxy requests to a running Polarion instance, create a `.env.local` file:

```env
VITE_BASE_URL=https://your-polarion-host
```

The Vite dev server will forward `/polarion/api-extender/rest/*` calls to that URL (configured in `vite.config.js`).

To use bearer token authentication instead of Polarion's session-based auth, also set:

```env
VITE_BEARER_TOKEN=your-personal-access-token
```

### Code formatting

This project uses [Prettier](https://prettier.io/) for consistent code formatting. The configuration is in `.prettierrc`.

```bash
# Format all source files
npm run format

# Check formatting without writing (useful in CI)
npm run format:check
```

#### IntelliJ IDEA setup

1. Go to **Settings > Plugins**, install the **Prettier** plugin if not already installed.
2. Go to **Settings > Languages & Frameworks > JavaScript > Prettier**.
3. Set **Prettier package** to `~/ui/node_modules/prettier` (or let IDEA auto-detect it).
4. Check **On 'Reformat Code' action**.
5. Check **On save** (optional, for auto-format on save).
6. In the **Run for files** field, ensure it includes: `{**/*.ts,**/*.tsx,**/*.css,**/*.html}`

Now `Ctrl+Alt+L` (Reformat Code) will use Prettier instead of the built-in formatter.

#### VS Code setup

1. Install the **Prettier - Code formatter** extension (`esbenp.prettier-vscode`).
2. Open **Settings** (`Ctrl+,`) and set:
   - **Editor: Default Formatter** to `Prettier - Code formatter`
   - **Editor: Format On Save** to `true`
3. Alternatively, add a `.vscode/settings.json` in the `ui/` folder:

```json
{
  "editor.defaultFormatter": "esbenp.prettier-vscode",
  "editor.formatOnSave": true
}
```

### Other commands

```bash
# Production build (outputs to dist/app/)
npm run build

# Preview the production build locally
npm run preview
```

### Testing & quality

Tests use Vitest in browser mode (real Chromium via Playwright); REST is mocked at the `fetch`
boundary, so no Polarion is needed. Visual-regression pixels only match inside the pinned Playwright
Docker image, so references are generated and checked there (Windows is a dev environment only).

```bash
# Behavior suite + the 80% istanbul coverage gate (runs anywhere; excludes visual tests)
npm run test:coverage

# Full suite (behavior + visual regression) + the coverage gate, inside the pinned image (canonical)
npm run test:coverage:docker

# Regenerate the committed visual reference PNGs (Docker only) after an intentional UI change
npm run test:update:docker

# Lint
npm run lint          # eslint .
npm run lint:fix
```

The repo-root pre-commit hooks run `format:check`, `lint`, and the dockerized coverage suite on `ui/`
changes; `mvn install` runs `test:coverage:docker` in the `test` phase (skip with `-DskipJsTests` on a
Docker-less host).

### Running the tests

**One command, locally and in CI: `npm run test:coverage:docker`.** It runs the full suite (behavior +
visual regression) plus the 80% istanbul coverage gate inside the pinned Playwright Docker image, which
is what the Maven `test` phase and the pre-commit hook execute. Docker must be running.

```bash
npm run test:coverage:docker   # the canonical run: full suite + coverage gate, in the pinned image
npm run test:coverage          # fast local loop: behavior only + the gate, no Docker, no pixels
npm run test:update:docker     # regenerate the committed reference PNGs after an intentional UI change
```

> `npm run test:coverage:full` is the inner command the Docker wrapper invokes. Run outside a container
> it is green, but it proves less than it looks: the reference screenshots are pixel-locked to the
> pinned image, so the visual suites detect that they are not in the reference environment and **skip
> themselves** rather than failing on the host's font metrics. It therefore reports the behavior suite
> and the coverage gate only - which is exactly what the `-DjsTestsNoDocker` Maven profile needs on a
> Docker-less host. To check the screenshots, use `test:coverage:docker`.
