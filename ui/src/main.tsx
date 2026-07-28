import React from 'react';
import ReactDOM from 'react-dom/client';
import { configureGenericModules } from '@grigoriev/react-sbb-polarion';
import '@grigoriev/react-sbb-polarion/style.css';
import App from './App';
import './App.css';

// The generic data-table look (.sbb-table); RSP defines the --sbb-table-* tokens it consumes but not
// these layout rules, so the app bundles the vendored copy itself. After style.css so tokens exist.

// react-sbb-polarion's BreadcrumbInjector loads the generic BreadcrumbBridge.js from this extension's
// own Polarion webapp context at runtime. The dev proxy forwards this absolute path to Polarion, and
// in Polarion it is same-origin. (The dropdown factories are bundled and no longer need this.)
configureGenericModules('/polarion/api-extender-app/ui/generic/js/modules/');

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <App />
  </React.StrictMode>,
);
