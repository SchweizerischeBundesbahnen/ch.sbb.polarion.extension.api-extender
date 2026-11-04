/*
 * UI additions of the API Extender extension.
 *
 * external-js/api-extender-ui-loader.js loads this file into the main portal page, through the
 * com.polarion.portal.tomcat.externalJsFile extension in plugin.xml. So it loads once per full page load,
 * in the top window, on every Polarion page, with no administrator configuration.
 * A change in this file needs no Polarion restart: a redeploy and a page reload are enough.
 * Each feature finds its own target elements, because Polarion builds its UI later with GWT.
 */
(function () {
    'use strict';

    if (window !== window.top || window.apiExtenderUi) {
        return;
    }

    const PROCESSED = 'data-api-extender';

    /**
     * The "Wiki Content" dialog of LiveDocs (WikiBlockDialog) shows a help label above the textarea.
     * The label sits in the north part of a GWT DockLayoutPanel with a fixed height of 70px,
     * so the hint goes into that part and the panel layers move to make room.
     */
    const wikiContentDialogHint = {
        apply(root) {
            root.querySelectorAll('.polarion-dle-WikiBlockDialog textarea.polarion-dle-WikiBlockDialog-Area').forEach((area) => {
                const layers = findLayers(area);
                if (!layers || layers.north.hasAttribute(PROCESSED)) {
                    return;
                }
                layers.north.setAttribute(PROCESSED, 'velocity-v2-hint');

                const hint = document.createElement('div');
                hint.className = 'api-extender-velocity-v2-hint';
                hint.style.cssText = 'margin-top: 6px;';
                hint.innerHTML = 'You can use <b>Velocity 2.4.1</b> scripts: wrap the script in '
                    + '<code>{velocityV2}</code> markers, for example <code>{velocityV2}#set($x = 1.5)$x{velocityV2}</code>.';
                layers.north.firstElementChild ? layers.north.firstElementChild.appendChild(hint) : layers.north.appendChild(hint);

                grow(layers, hint.offsetHeight + 6);
            });
        }
    };

    /**
     * GWT DockLayoutPanel wraps each child in an absolutely positioned layer. The north layer holds the help label
     * with the "Syntax Help" link, and the center layer holds the textarea.
     */
    function findLayers(area) {
        for (let center = area; center.parentElement; center = center.parentElement) {
            const north = Array.from(center.parentElement.children).find((child) => child !== center
                && child.style.position === 'absolute'
                && child.querySelector('a[href*="SyntaxHelp"]'));
            if (north) {
                return { north, center };
            }
        }
        return null;
    }

    function grow(layers, extra) {
        const height = parseFloat(layers.north.style.height);
        const top = parseFloat(layers.center.style.top);
        if (Number.isNaN(height) || Number.isNaN(top) || extra <= 0) {
            return;
        }
        layers.north.style.height = (height + extra) + 'px';
        layers.center.style.top = (top + extra) + 'px';
    }

    const features = [wikiContentDialogHint];

    // GWT changes the DOM very often, so the features run at most once per animation frame.
    let scheduled = false;
    function applyFeatures() {
        scheduled = false;
        features.forEach((feature) => {
            try {
                feature.apply(document);
            } catch (e) {
                console.warn('[api-extender] UI feature failed', e);
            }
        });
    }

    function start() {
        new MutationObserver(() => {
            if (!scheduled) {
                scheduled = true;
                window.requestAnimationFrame(applyFeatures);
            }
        }).observe(document.body, { childList: true, subtree: true });
        applyFeatures();
    }

    window.apiExtenderUi = { features };
    if (document.body) {
        start();
    } else {
        document.addEventListener('DOMContentLoaded', start, { once: true });
    }
})();
