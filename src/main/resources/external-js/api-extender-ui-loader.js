/*
 * Loads the UI additions of the API Extender extension into the main Polarion page.
 *
 * Polarion serves this file with "Cache-Control: immutable" for the whole Polarion build, so browsers never fetch
 * a changed version. That is why this loader must never change, and the real script comes from the extension's
 * web application, which Polarion serves without caching.
 */
(function () {
    'use strict';

    if (window !== window.top) {
        return;
    }
    const script = document.createElement('script');
    script.src = '/polarion/api-extender-app/ui/js/api-extender-ui.js';
    document.head.appendChild(script);
})();
