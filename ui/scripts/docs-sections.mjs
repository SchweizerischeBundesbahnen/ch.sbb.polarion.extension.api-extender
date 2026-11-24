// Splits one article rendered by markdown2html into search-index sections. Kept apart from build-docs-index.mjs,
// which reads and writes files, so that test/docsSearchIndex.test.ts can run it on a fixture in the browser.
import { parse } from 'node-html-parser';

/** Collapse runs of whitespace and trim. */
const clean = (text) => text.replace(/\s+/g, ' ').trim();

/**
 * One record per h2/h3/h4 heading: its anchor id, title, and the plain text beneath it up to the next heading. An h4
 * is a subsection of its own, such as a subsection under an h3 of an article; leaving it out
 * would drop its text from the search.
 * The text is kept whole: the search matches on it and never displays it (results show the titles), so a cap
 * would only hide terms further down a section. All articles together are some 60 KB of text.
 *
 * Only the top-level elements are walked: markdown2html renders an article as a flat sequence of headings,
 * paragraphs, lists, tables and code blocks. build-docs-index.mjs --require fails when an article yields no
 * section, so a change of that structure breaks the build instead of shipping an empty search.
 *
 * @param {string} html
 * @returns {{anchor: string, title: string, text: string}[]}
 */
export function sectionsOf(html) {
  // `blockTextElements` lists the elements whose content is raw text rather than markup. Name only the real
  // raw-text elements: every key counts, whatever its value - `true` keeps the content as one text node,
  // `false` DISCARDS it - so `pre` must be absent, not false, or no code block (property keys, CSS, config
  // snippets) would reach the index. Absent, <pre>/<code> parse as elements and `.text` drops their
  // syntax-highlighting <span>s.
  const root = parse(html, { blockTextElements: { script: true, style: true, noscript: true } });
  const elements = root.childNodes.filter((node) => node.nodeType === 1);
  const sections = [];
  let current = null;
  for (const element of elements) {
    const tag = element.tagName?.toLowerCase() ?? '';
    if (tag === 'h2' || tag === 'h3' || tag === 'h4') {
      current = { anchor: element.getAttribute('id') ?? '', title: clean(element.text), body: [] };
      sections.push(current);
    } else if (/^h[1-6]$/.test(tag)) {
      current = null; // h1 / h5-h6 end the current section but are not indexed themselves
    } else if (current) {
      current.body.push(element.text);
    }
  }
  return sections.map((section) => ({
    anchor: section.anchor,
    title: section.title,
    text: clean(section.body.join(' ')),
  }));
}
