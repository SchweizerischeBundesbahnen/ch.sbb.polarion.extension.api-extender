import type { ComponentType } from 'react';
import About from './pages/About';
import Authorization from './pages/Authorization';

/**
 * A single navigable page of the app. The `id` is what appears in the URL as `?feature=<id>` and is
 * what `hivemodule.xml` points its extenders at. A URL that matches none of these falls back to the
 * dev Landing (see App.tsx / findFeature).
 *
 * The two authorization pages differ only in which named setting they read and write, so they share
 * one component and carry the setting name with them.
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
    component: () => <Authorization settingName={SETTINGS.projectCustomFields} title="Project Custom Fields" />,
  },
  {
    id: 'global-records',
    label: 'Global Records',
    description: 'Configure which global and project roles may write global records.',
    component: () => <Authorization settingName={SETTINGS.globalRecords} title="Global Records" />,
  },
];

export function findFeature(id: string | null): Feature | undefined {
  return FEATURES.find((f) => f.id === id);
}
