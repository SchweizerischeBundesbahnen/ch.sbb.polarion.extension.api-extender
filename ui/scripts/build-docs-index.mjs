// Builds the documentation-site search index by PARSING the HTML the markdown2html-maven-plugin renders for
// each article, rather than parsing the markdown here. The plugin owns the canonical heading-slug algorithm
// (ICU transliteration + the -2/-3 disambiguation) and stamps it onto the headings (generateHeadingIds); this
// reads those ids straight out of the HTML, so search anchors are identical to the ids the rendered headings
// carry, with no second slug implementation to drift.
//
// The plugin renders each article to the shipped `webapp/api-extender-app/html/<id>.html` in generate-sources
// (before this frontend build, with the Table of contents excluded); this reads those same shipped files -
// there is no separate render - parsing each into one record per h2/h3/h4 heading (its anchor id, title and the
// plain text beneath it up to the next heading) and concatenates them in the docs.config.json reading order
// into src/docs/search-index.json.
//
// Two modes:
//   --require (used by `prebuild`, i.e. the real Maven/production build): the rendered article MUST be present
//      and yield at least one section; otherwise the build fails (exit 1) rather than silently shipping a stale
//      or empty index.
//   default  (used by `predev` and the Vitest globalSetup, i.e. runs without Maven): a missing article is a
//      warning and an EMPTY index is written, so a fresh clone still runs and tests - the
//      documentation search is then simply hidden.
//
// src/docs/search-index.json is a pure build artifact and is not committed (.gitignore): App.tsx imports it
// statically, so every entry point that needs it (build, dev, test) generates it first.
//
// The article directory defaults to the shipped webapp html (src/main/resources/webapp/api-extender-app/html,
// where markdown2html renders the articles in generate-sources); override it with DOCS_SECTION_INDEX_DIR.
import { existsSync, readFileSync, writeFileSync } from 'node:fs';
import { dirname, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';
import { sectionsOf } from './docs-sections.mjs';

const scriptDir = dirname(fileURLToPath(import.meta.url));
const uiDir = resolve(scriptDir, '..');
const repoRoot = resolve(uiDir, '..');
const config = JSON.parse(readFileSync(resolve(uiDir, 'src/docs/docs.config.json'), 'utf8'));
const strict = process.argv.includes('--require');
const indexDir = process.env.DOCS_SECTION_INDEX_DIR
  ? resolve(process.env.DOCS_SECTION_INDEX_DIR)
  : resolve(repoRoot, 'src/main/resources/webapp/api-extender-app/html');
const outFile = resolve(uiDir, 'src/docs/search-index.json');

const missing = config.items.filter((item) => !existsSync(resolve(indexDir, `${item.id}.html`)));
if (missing.length > 0) {
  const message =
    `build-docs-index: no rendered article in ${indexDir} for ${missing.map((i) => i.id).join(', ')}. ` +
    'Run the Maven build (it renders them in generate-sources) before the frontend build.';
  if (strict) {
    console.error(message);
    process.exit(1);
  }
  console.warn(`${message} Writing an empty search-index.json (dev mode): the documentation search is hidden.`);
  writeFileSync(outFile, '[]\n', 'utf8');
  process.exit(0);
}

const records = [];
for (const item of config.items) {
  const html = readFileSync(resolve(indexDir, `${item.id}.html`), 'utf8');
  const sections = sectionsOf(html);
  if (sections.length === 0) {
    // An article always has an h2. None found means the rendered HTML no longer has the flat structure
    // sectionsOf walks, and the search would silently miss the whole article.
    const message = `build-docs-index: no h2/h3/h4 section found in ${item.id}.html`;
    if (strict) {
      console.error(message);
      process.exit(1);
    }
    console.warn(message);
  }
  for (const section of sections) {
    records.push({
      doc: item.id,
      docTitle: item.title,
      anchor: section.anchor,
      title: section.title,
      text: section.text,
    });
  }
}

writeFileSync(outFile, `${JSON.stringify(records, null, 2)}\n`, 'utf8');
console.log(`build-docs-index: built ${records.length} sections from ${config.items.length} docs into ${outFile}`);
