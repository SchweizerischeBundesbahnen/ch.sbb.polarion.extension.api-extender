import type { DocSearchRecord } from '@sbb-polarion/react-sbb-polarion';
import searchIndex from './search-index.json';

/**
 * The documentation search index: one record per h2/h3/h4 section of the rendered articles. The JSON is a
 * build artifact (scripts/build-docs-index.mjs) and is empty when the articles were not rendered, which hides
 * the search box. It is re-exported from a module of its own so that a test can replace it: a mock of the
 * JSON file itself does not reach the app in Vitest browser mode.
 */
export const SEARCH_INDEX: DocSearchRecord[] = searchIndex;
