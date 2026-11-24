import { type ComponentType, useMemo } from 'react';
import { AuthorizationSettings, DocPage, createAuthorizationService } from '@sbb-polarion/react-sbb-polarion';
import { DOC_ORDER } from './docs/manifest';
import About from './pages/About';
import useRemote from './services/useRemote';

/**
 * A single navigable page of the app. The `id` is what appears in the URL as `?feature=<id>` and is
 * what `hivemodule.xml` points its extenders at, so the ids here and the `pageUrl`s there must stay identical - a
 * typo is a blank page in Polarion and no test catches it. A URL that matches none of these falls back to the
 * dev Landing (see App.tsx / findFeature).
 *
 * The two authorization pages are react-sbb-polarion's shared page over two different named settings,
 * as the JSP they replace was one file opened twice with a different `settings_name`.
 */
export interface Feature {
  id: string;
  label: string;
  description: string;
  component: ComponentType;
}

/** Named settings behind the two authorization pages, as stored by the settings framework. */
export const SETTINGS = {
  projectCustomFields: 'project_custom_fields',
  globalRecords: 'global_records',
} as const;

function ProjectCustomFields() {
  const { sendRequest } = useRemote();
  const service = useMemo(() => createAuthorizationService(sendRequest, SETTINGS.projectCustomFields), [sendRequest]);
  return (
    <AuthorizationSettings
      title="Project Custom Fields"
      service={service}
      quickHelp={
        <>
          <h3>Permissions</h3>
          <p>Reading project custom fields is not restricted.</p>
          <p>Writing can be allowed for selected global and project roles.</p>
          <p>By default only the global admin role is allowed.</p>
        </>
      }
    />
  );
}

function GlobalRecords() {
  const { sendRequest } = useRemote();
  // Offer only global roles. GlobalRecordsSettingsModel stores none but the global ones, so the project
  // roles the scope reports are not grantable here - the JSP listed them anyway and dropped the ticks on
  // save, silently. An empty project list is how the shared page is told a setting has no project side.
  const service = useMemo(() => {
    const authorization = createAuthorizationService(sendRequest, SETTINGS.globalRecords);
    return {
      ...authorization,
      loadRoles: (scope: string) => authorization.loadRoles(scope).then((roles) => ({ ...roles, projectRoles: [] })),
    };
  }, [sendRequest]);
  return (
    <AuthorizationSettings
      title="Global Records"
      service={service}
      quickHelp={
        <>
          <h3>Permissions</h3>
          <p>Reading global records is not restricted.</p>
          <p>
            Writing can be allowed for selected global roles. Global records belong to the repository rather than to a
            project, so project roles do not apply to them.
          </p>
          <p>By default only the global admin role is allowed.</p>
        </>
      }
    />
  );
}

// The documentation-site articles, derived from the shared manifest (docs.config.json) rather than listed by
// hand: each renders through the single DocPage bound to its manifest entry, in the manifest's reading order.
// Adding an article is a docs.config.json item (plus its markdown and the pom render) - nothing here changes.
// They carry no hivemodule.xml menu entry of their own: they are reached from the single `documentation` node
// and the articles' cross-document links (rewritten to ?feature=<id> by the interceptor in App.tsx); the ids
// equal the generated html basenames those links point at.
const DOC_FEATURES: Feature[] = DOC_ORDER.map((doc) => {
  const Component = () => <DocPage doc={doc} />;
  Component.displayName = `DocPage(${doc.id})`;
  return {
    id: doc.id,
    label: doc.title,
    description: `${doc.title}, generated from ${doc.source}.`,
    component: Component,
  };
});

export const FEATURES: Feature[] = [
  {
    id: 'about',
    label: 'About',
    description: 'Extension version and general information.',
    component: About,
  },
  {
    id: 'project-custom-fields',
    label: 'Project Custom Fields',
    description: 'Configure which global and project roles may write project custom fields.',
    component: ProjectCustomFields,
  },
  {
    id: 'global-records',
    label: 'Global Records',
    description: 'Configure which global roles may write global records.',
    component: GlobalRecords,
  },
  // The documentation site, in manifest reading order (Quick Start, User Guide, REST API, ...).
  ...DOC_FEATURES,
];

export function findFeature(id: string | null): Feature | undefined {
  return FEATURES.find((f) => f.id === id);
}
