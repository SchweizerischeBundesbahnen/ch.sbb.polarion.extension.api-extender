import { describe, expect, it } from 'vitest';
// @ts-expect-error - a plain ESM build script without type declarations
import { sectionsOf } from '../scripts/docs-sections.mjs';
import config from '../src/docs/docs.config.json';
import searchIndex from '../src/docs/search-index.json';

// The documentation search index (scripts/build-docs-index.mjs). The other documentation tests replace the index
// with fixed records, so they would pass whatever the builder produces: this file tests the builder itself.

interface Section {
  anchor: string;
  title: string;
  text: string;
}

// The shape markdown2html renders: a flat sequence of headings carrying generated ids, paragraphs, tables and
// code blocks whose tokens are wrapped in syntax-highlighting spans.
const ARTICLE = `
<h1 id="user-guide">User Guide</h1>
<p>Intro before the first section.</p>
<h2 id="project-custom-fields">Project custom fields</h2>
<p>A project can store any number of values.</p>
<h3 id="who-may-write">Who may write</h3>
<table><tbody><tr><td>Project Custom Fields</td><td>Global and project roles.</td></tr></tbody></table>
<h4 id="velocity-scripts">Velocity scripts</h4>
<p>A write call runs each time the page is rendered.</p>
<h5 id="detail">Detail</h5>
<p>Text under an h5 belongs to no section.</p>
<h2 id="rest-api-2">REST API</h2>
<pre><code class="language-bash"><span style="color:#8250df">curl</span> -F <span>"regex=(\\d+)"</span></code></pre>
`;

describe('docs search index builder', () => {
  const sections: Section[] = sectionsOf(ARTICLE);

  it('makes one section per h2, h3 and h4 with the id of the heading as anchor', () => {
    expect(sections.map((s) => [s.anchor, s.title])).toEqual([
      ['project-custom-fields', 'Project custom fields'],
      ['who-may-write', 'Who may write'],
      ['velocity-scripts', 'Velocity scripts'],
      ['rest-api-2', 'REST API'],
    ]);
  });

  it('ends a section at the next heading of any level', () => {
    expect(sections[0].text).toBe('A project can store any number of values.');
    expect(sections[1].text).toContain('Project Custom Fields');
    expect(sections[1].text).not.toContain('each time the page');
    expect(sections[2].text).toBe('A write call runs each time the page is rendered.');
  });

  it('leaves out text before the first section and under h5/h6', () => {
    const all = sections.map((s) => s.text).join(' ');
    expect(all).not.toContain('Intro before the first section');
    expect(all).not.toContain('belongs to no section');
  });

  it('keeps the text of code blocks without their highlighting markup', () => {
    expect(sections[3].text).toBe('curl -F "regex=(\\d+)"');
  });

  it('finds no section in markup of another structure', () => {
    // build-docs-index.mjs --require fails the build on this, rather than shipping an empty search.
    expect(sectionsOf('<div><h2 id="a">A</h2><p>Text.</p></div>')).toEqual([]);
  });
});

// The index the build generated from the real articles. The Maven build renders them before the JS suite runs,
// so these assertions are real there; a plain `npm test` without them gets the empty dev-mode index and skips.
interface SearchRecord extends Section {
  doc: string;
  docTitle: string;
}

const RECORDS = searchIndex as SearchRecord[];

describe.skipIf(RECORDS.length === 0)('generated documentation search index', () => {
  it('covers every article of the manifest and nothing else', () => {
    expect(new Set(RECORDS.map((r) => r.doc))).toEqual(new Set(config.items.map((i) => i.id)));
  });

  it('carries a title and a slug-form anchor per record', () => {
    for (const record of RECORDS) {
      expect(record.title.length).toBeGreaterThan(0);
      expect(record.anchor).toMatch(/^[a-z0-9]+(?:-[a-z0-9]+)*$/);
    }
  });

  it('indexes the h3 subsections of the Velocity tools', () => {
    const velocityTools = RECORDS.filter((r) => r.doc === 'velocity-tools');
    expect(velocityTools.find((r) => r.anchor === 'collection')?.text).toContain('CollectionTool');
    expect(velocityTools.find((r) => r.anchor === 'render')?.text).toContain('Velocity Engine 2.4.1');
  });

  it('carries the text of code blocks', () => {
    const restApi = RECORDS.filter((r) => r.doc === 'rest-api').map((r) => r.text);
    expect(restApi.some((text) => text.includes('release_manager'))).toBe(true);
  });
});
