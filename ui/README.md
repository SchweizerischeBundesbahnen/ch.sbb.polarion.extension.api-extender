# API Extender App (React UI)

This submodule contains the React frontend for the API Extender Polarion extension, built on the shared
`@grigoriev/react-sbb-polarion` (RSP) component library. It is a single Vite bundle
with feature routing by `?feature=<id>`, hosting three administration pages:

- **About** (`?feature=about`): the shared RSP About page.
- **Project Custom Fields** (`?feature=project-custom-fields`): which global and project roles may
  write project custom fields.
- **Global Records** (`?feature=global-records`): which global roles may write global records. Global
  records belong to the repository, so this page offers no project roles.

The last two are RSP's shared `AuthorizationSettings` page over two different named settings — the JSP
they replace was one file opened twice with a different `settings_name`.

The app is built with Vite and React, producing a static bundle that gets embedded into the extension JAR during the Maven build.

## How it integrates with Polarion

1. **Entry points** — `hivemodule.xml` registers one administration menu entry per page, each opening
   `/polarion/api-extender-app/ui/app/index.html?feature=<id>&embedded=true&scope=$scope$`.

2. **Webapp registration** — `plugin.xml` declares a `api-extender-app` webapp. Polarion's Tomcat serves the static files through `ApiExtenderAppServlet` (mapped to `/ui/*`).

3. **REST communication** — The React app calls the existing REST API at `/polarion/api-extender/rest/internal/*` (or `/api/*` with a bearer token): the settings endpoints for the stored roles, and `/roles` for the roles the current scope offers. Both come from the generic parent; the role endpoints are opt-in and this extension registers them in `ApiExtenderRestApplication`.

4. **Build pipeline** — During `mvn package`, the `frontend-maven-plugin` runs `npm ci` and `npm run build` inside this folder. The `maven-resources-plugin` then copies `ui/dist/app/` into `src/main/resources/webapp/api-extender-app/app/`, so it ends up in the final JAR. `ci`, not `install`: the packaged bundle must come from the committed `package-lock.json`, the same graph the tests run against — so a `package.json` edit that is not reflected in the lock fails the build instead of being silently repaired. Locally you still use `npm install` (below), which is what updates the lock.

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
