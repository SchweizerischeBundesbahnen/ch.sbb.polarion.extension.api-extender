/*
 * UI additions of the API Extender extension.
 *
 * external-js/api-extender-ui-loader.js loads this file into the main portal page, through the
 * com.polarion.portal.tomcat.externalJsFile extension in plugin.xml. So it loads once per full page load,
 * in the top window, on every Polarion page, with no administrator configuration.
 * A change in this file needs no Polarion restart: a redeploy and a page reload are enough.
 * Each feature finds its own target elements, because Polarion builds its UI later with GWT.
 * Features also run in same-origin iframes, for example the iframe of a Classic Wiki Page.
 */
(function () {
    'use strict';

    if (window !== window.top || window.apiExtenderUi) {
        return;
    }

    const PROCESSED = 'data-api-extender';

    const VELOCITY_V2_EXAMPLE = [
        '{velocityV2}',
        '#set($user = {\'name\': \'Alice\'})',
        '#set($roles = [\'admin\', \'editor\'])',
        '#foreach($role in $roles)',
        '  $user.name: $role',
        '  #if($foreach.hasNext)<br/>#end',
        '#end',
        '{velocityV2}'
    ].join('\n');

    /**
     * Creates the underlined "Velocity 2 is available" label. Hovering it shows a tooltip with an example.
     * The tooltip lives in the body of the document only while the mouse is over the label,
     * so a dialog which Polarion creates again on each opening leaves nothing behind.
     *
     * @param doc the document of the label
     * @param options.scope the word for the part which stays Velocity 1.4, for example "page"
     * @param options.align "right" aligns the tooltip with the right edge of the label, "left" with its left edge
     * @param options.background the background of the tooltip
     */
    function createVelocityV2Label(doc, options) {
        const label = doc.createElement('span');
        label.className = 'api-extender-velocity-v2-label';
        label.textContent = 'Velocity 2 is available';
        label.style.cssText = 'cursor: help; color: #555; font-size: 12px; border-bottom: 1px dotted #888;';

        let tooltip = null;
        label.addEventListener('mouseenter', () => {
            tooltip = createVelocityV2Tooltip(doc, options);
            doc.body.appendChild(tooltip);
            const rect = label.getBoundingClientRect();
            const view = doc.defaultView;
            const left = options.align === 'right' ? Math.max(4, rect.right - tooltip.offsetWidth) : rect.left;
            tooltip.style.left = (left + view.scrollX) + 'px';
            tooltip.style.top = (rect.bottom + 6 + view.scrollY) + 'px';
        });
        label.addEventListener('mouseleave', () => {
            if (tooltip) {
                tooltip.remove();
                tooltip = null;
            }
        });
        return label;
    }

    function createVelocityV2Tooltip(doc, options) {
        const tooltip = doc.createElement('div');
        tooltip.className = 'api-extender-velocity-v2-tooltip';
        // GWT dialogs use high z-index values, and the tooltip must stay above them.
        tooltip.style.cssText = 'position: absolute; z-index: 2147483000; max-width: 520px; padding: 8px 10px; text-align: left;'
            + ` background: ${options.background}; color: #333; font-size: 12px; line-height: 1.5; border: 1px solid #ccc;`
            + ' border-radius: 3px; box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);';
        tooltip.innerHTML = 'You can use <b>Velocity 2.4.1</b> features: wrap the script in '
            + '<code style="background: #fff; padding: 0 3px; border-radius: 2px;">{velocityV2}</code> markers, for example:';

        const example = doc.createElement('div');
        example.style.cssText = 'margin: 6px 0; padding: 6px 8px; border: 1px dashed #aaa; background: #fff;'
            + ' font-family: monospace; white-space: pre; overflow-x: auto;';
        example.textContent = VELOCITY_V2_EXAMPLE;
        tooltip.appendChild(example);

        const note = doc.createElement('div');
        note.innerHTML = `The rest of the ${options.scope} stays Velocity 1.4. Note that the two <b>do not share variables</b>: `
            + 'a variable set inside <code style="background: #fff; padding: 0 3px; border-radius: 2px;">{velocityV2}</code> '
            + 'is not visible outside it, and the other way round.';
        tooltip.appendChild(note);

        const attribution = doc.createElement('div');
        attribution.style.cssText = 'margin-top: 8px; text-align: right; color: #888; font-size: 11px;';
        attribution.innerHTML = 'Provided via: <img src="/polarion/api-extender-app/ui/images/menu/30x30/_parent.svg" alt=""'
            + ' style="width: 14px; height: 14px; vertical-align: -3px; margin: 0 2px;"> API Extender by SBB';
        tooltip.appendChild(attribution);
        return tooltip;
    }

    /**
     * The "Wiki Content" dialog of LiveDocs (WikiBlockDialog) shows a help label above the textarea.
     * The label sits in the north part of a GWT DockLayoutPanel with a fixed height of 70px,
     * so the Velocity 2 label goes into that part and the panel layers move to fit it.
     */
    const wikiContentDialogHint = {
        apply(doc) {
            doc.querySelectorAll('.polarion-dle-WikiBlockDialog textarea.polarion-dle-WikiBlockDialog-Area').forEach((area) => {
                const layers = findLayers(area);
                if (!layers || layers.north.hasAttribute(PROCESSED)) {
                    return;
                }
                layers.north.setAttribute(PROCESSED, 'velocity-v2-hint');

                const gap = 6;
                const line = doc.createElement('div');
                line.style.cssText = `margin-top: ${gap}px;`;
                line.appendChild(createVelocityV2Label(doc, { scope: 'block', align: 'left', background: '#f5f5f5' }));
                layers.north.firstElementChild ? layers.north.firstElementChild.appendChild(line) : layers.north.appendChild(line);

                // The fixed 70px leave free space below the help text, so the textarea moves up to the label.
                // The text above ends a few pixels above its box, but the dotted underline of the label is the bottom
                // of its box. So the gap below is larger in pixels, and both gaps look the same.
                const gapBelow = 14;
                const lineBottom = line.getBoundingClientRect().bottom;
                const areaTop = area.getBoundingClientRect().top;
                shift(layers, areaTop > 0 ? lineBottom + gapBelow - areaTop : line.offsetHeight + gapBelow);
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

    function shift(layers, delta) {
        const height = parseFloat(layers.north.style.height);
        const top = parseFloat(layers.center.style.top);
        if (Number.isNaN(height) || Number.isNaN(top) || delta === 0) {
            return;
        }
        layers.north.style.height = (height + delta) + 'px';
        layers.center.style.top = (top + delta) + 'px';
    }

    /**
     * The toolbar of a Classic Wiki Page in view mode (#menuView) gets the Velocity 2 label before the Attachments button.
     */
    const wikiPageToolbarHint = {
        apply(doc) {
            const attachments = doc.querySelector('#menuView div[onclick*="attachmentPanel"]');
            const cell = attachments && attachments.closest('td');
            if (!cell || cell.hasAttribute(PROCESSED)) {
                return;
            }
            cell.setAttribute(PROCESSED, 'velocity-v2-hint');

            cell.parentElement.insertBefore(createToolbarLabelCell(doc), cell);
        }
    };

    /**
     * The toolbar of a Classic Wiki Page in edit mode has the same #menuView, but no Attachments button.
     * The label goes after the full-width spacer cell which follows the Preview button, so it sits at the right edge.
     */
    const wikiPageEditToolbarHint = {
        apply(doc) {
            const preview = doc.querySelector('#menuView #formaction_preview');
            const row = preview && preview.closest('td') && preview.closest('td').parentElement;
            const spacer = row && Array.from(row.children).find((cell) => cell.getAttribute('width') === '100%');
            if (!spacer || row.hasAttribute(PROCESSED)) {
                return;
            }
            row.setAttribute(PROCESSED, 'velocity-v2-hint');
            spacer.insertAdjacentElement('afterend', createToolbarLabelCell(doc));
        }
    };

    function createToolbarLabelCell(doc) {
        const menu = doc.getElementById('menuView');
        const background = doc.defaultView.getComputedStyle(menu).backgroundColor || '#f5f5f5';
        const labelCell = doc.createElement('td');
        labelCell.style.cssText = 'white-space: nowrap; vertical-align: middle; padding: 0 10px;';
        labelCell.appendChild(createVelocityV2Label(doc, { scope: 'page', align: 'right', background }));
        return labelCell;
    }

    const features = [wikiContentDialogHint, wikiPageToolbarHint, wikiPageEditToolbarHint];

    // GWT changes the DOM very often, so the features run at most once per animation frame.
    let scheduled = false;
    function schedule() {
        if (!scheduled) {
            scheduled = true;
            window.requestAnimationFrame(applyFeatures);
        }
    }

    function applyFeatures() {
        scheduled = false;
        documents().forEach((doc) => features.forEach((feature) => {
            try {
                feature.apply(doc);
            } catch (e) {
                console.warn('[api-extender] UI feature failed', e);
            }
        }));
    }

    /**
     * The top document and the documents of its same-origin iframes.
     * An iframe loads a new document on navigation without a change in the top document, so its load event schedules the features.
     */
    function documents() {
        const result = [document];
        document.querySelectorAll('iframe').forEach((frame) => {
            if (!frame.hasAttribute(PROCESSED)) {
                frame.setAttribute(PROCESSED, 'observed');
                frame.addEventListener('load', schedule);
            }
            try {
                if (frame.contentDocument && frame.contentDocument.body) {
                    result.push(frame.contentDocument);
                }
            } catch (e) {
                // A cross-origin iframe is not accessible, and has no Polarion UI.
            }
        });
        return result;
    }

    function start() {
        new MutationObserver(schedule).observe(document.body, { childList: true, subtree: true });
        applyFeatures();
    }

    window.apiExtenderUi = { features };
    if (document.body) {
        start();
    } else {
        document.addEventListener('DOMContentLoaded', start, { once: true });
    }
})();
