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

    const VELOCITY_V2_QUERY_EXAMPLE = [
        'type:requirement AND {velocityV2}',
        '#set($statuses = [\'draft\', \'open\'])',
        'status:(#foreach($s in $statuses)$s#if($foreach.hasNext) #end#end)',
        '{velocityV2}'
    ].join('\n');

    /**
     * Creates the underlined "Velocity v2 is available" label. Hovering it shows a tooltip with an example.
     * The tooltip lives in the body of the document only while the mouse is over the label,
     * so a dialog which Polarion creates again on each opening leaves nothing behind.
     *
     * @param doc the document of the label
     * @param options.scope the word for the part which stays Velocity v1.4, for example "page"
     * @param options.align "right" aligns the tooltip with the right edge of the label, "left" with its left edge
     * @param options.background the background of the tooltip
     * @param options.example the example in the tooltip, a script by default
     */
    function createVelocityV2Label(doc, options) {
        const label = doc.createElement('span');
        label.className = 'api-extender-velocity-v2-label';
        label.textContent = 'Velocity v2 is available';
        label.style.cssText = 'cursor: help; color: #555; font-size: 12px; border-bottom: 1px dotted #888;';

        let tooltip = null;
        label.addEventListener('mouseenter', () => {
            tooltip = createVelocityV2Tooltip(doc, options);
            doc.body.appendChild(tooltip);
            const rect = label.getBoundingClientRect();
            const view = doc.defaultView;
            const margin = 4;
            const preferred = options.align === 'right' ? rect.right - tooltip.offsetWidth : rect.left;
            const maxLeft = doc.documentElement.clientWidth - tooltip.offsetWidth - margin;
            const left = Math.max(margin, Math.min(preferred, maxLeft));
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
        tooltip.style.cssText = 'position: absolute; z-index: 2147483000; max-width: min(520px, calc(100vw - 8px)); box-sizing: border-box; padding: 8px 10px; text-align: left;'
            + ` background: ${options.background}; color: #333; font-size: 12px; line-height: 1.5; border: 1px solid #ccc;`
            + ' border-radius: 3px; box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);';
        tooltip.innerHTML = '<div style="margin-bottom: 6px;">'
            + '<b style="background: #d32f2f; color: #fff; padding: 0 4px; border-radius: 2px;">Experimental</b>'
            + ' Use this feature at your own risk.</div>'
            + `You can use <b>Velocity v2.4.1</b> features: wrap a part of the ${options.scope} in `
            + '<code style="background: #fff; padding: 0 3px; border-radius: 2px;">{velocityV2}</code> markers, for example:';

        const example = doc.createElement('div');
        example.style.cssText = 'margin: 6px 0; padding: 6px 8px; border: 1px dashed #aaa; background: #fff;'
            + ' font-family: monospace; white-space: pre; overflow-x: auto;';
        example.textContent = options.example || VELOCITY_V2_EXAMPLE;
        tooltip.appendChild(example);

        const note = doc.createElement('div');
        note.innerHTML = `The rest of the ${options.scope} stays Velocity v1.4. Note that the two <b>do not share variables</b>: `
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
     * The Wiki Markup editor of a Classic Wiki Page has a formatting toolbar above the textarea, with floated buttons.
     * The label goes after the last button, with the line height of the buttons.
     */
    const wikiPageEditToolbarHint = {
        apply(doc) {
            const toolbar = doc.querySelector('#plain_wiki_editor .rich-toolbar');
            if (!toolbar || toolbar.hasAttribute(PROCESSED)) {
                return;
            }
            toolbar.setAttribute(PROCESSED, 'velocity-v2-hint');

            const lastButton = toolbar.querySelector('a:last-of-type');
            const line = doc.createElement('span');
            line.style.cssText = `float: left; margin-left: 8px; line-height: ${lastButton ? lastButton.offsetHeight : 32}px;`;
            line.appendChild(createVelocityV2Label(doc, { scope: 'page', align: 'left', background: '#f5f5f5' }));
            toolbar.appendChild(line);
        }
    };

    /**
     * The parameters sidebar of the built-in "Script - Block" and "Script - Inline" LiveReport widgets
     * gets the label next to the "Script" parameter header.
     * The sidebar title tells these widgets from "Script - Block Velocity V2", where the whole script is Velocity 2.
     */
    const scriptWidgetParameterHint = {
        apply(doc) {
            doc.querySelectorAll('.polarion-rpe-ParameterEditor-headerPanel').forEach((header) => {
                const name = header.querySelector('.polarion-rpe-ParameterEditor-parameterLabel');
                if (!name || name.textContent.trim() !== 'Script' || header.hasAttribute(PROCESSED)) {
                    return;
                }
                const title = findSidebarTitle(header);
                if (!title || !/^Script - (Block|Inline):/.test(title.textContent.trim())) {
                    return;
                }
                header.setAttribute(PROCESSED, 'velocity-v2-hint');

                name.style.display = 'inline-block';
                const label = createVelocityV2Label(doc, { scope: 'script', align: 'left', background: '#f5f5f5' });
                // The grey header strip is 20px high and its bottom edge hides the dotted underline of a baseline-aligned label,
                // so the label gets a fixed box which sits in the middle of the strip.
                label.style.cssText += 'display: inline-block; vertical-align: top; line-height: 13px; margin: 1px 0 0 16px; font-size: 11px;';
                name.insertAdjacentElement('afterend', label);
            });
        }
    };

    /**
     * The Data Set parameter of the built-in widgets shows the label next to the "Query" header
     * while "Query Type" is "Lucene + Velocity" or "SQL + Velocity".
     * Polarion keeps the "Query" header and builds the query editor again when the query type changes,
     * so the label is added and removed on each run, not once.
     */
    const widgetQueryParameterHint = {
        apply(doc) {
            doc.querySelectorAll('[id="gwt-debug-dataSetQueryType"]').forEach((combo) => {
                const panel = combo.closest('table.polarion-rpe-ParameterEditor-collapsibleContentPanel');
                const selected = combo.querySelector('.polarion-Combo-SelectedItemPanel [title]');
                if (!panel || !selected) {
                    return;
                }
                const existing = panel.querySelector('.api-extender-velocity-v2-label');
                if (!/Velocity/.test(selected.getAttribute('title'))) {
                    if (existing) {
                        existing.remove();
                    }
                    return;
                }
                const name = findQueryHeader(panel);
                if (existing || !name) {
                    return;
                }
                name.style.display = 'inline-block';
                const label = createVelocityV2Label(doc, { scope: 'query', align: 'left', background: '#f5f5f5', example: VELOCITY_V2_QUERY_EXAMPLE });
                label.style.cssText += 'margin-left: 12px; font-size: 11px; white-space: nowrap;';
                name.insertAdjacentElement('afterend', label);
            });
        }
    };

    /**
     * The "Query" header is in the row of the Data Set panel above the row with the Velocity query editor.
     * `table.rows` leaves out the rows of nested tables, so the lookup stays on the level of the panel.
     */
    function findQueryHeader(panel) {
        const area = panel.querySelector('[id="gwt-debug-dataSetQueryArea"]');
        const rows = Array.from(panel.rows);
        const index = area ? rows.findIndex((row) => row.contains(area)) : -1;
        return index > 0 ? rows[index - 1].querySelector('.polarion-rpe-ParameterEditor-label') : null;
    }

    function findSidebarTitle(element) {
        for (let parent = element.parentElement; parent; parent = parent.parentElement) {
            const title = parent.querySelector('.polarion-SidebarPanel-header-label');
            if (title) {
                return title;
            }
        }
        return null;
    }

    const features = [wikiContentDialogHint, wikiPageEditToolbarHint, scriptWidgetParameterHint, widgetQueryParameterHint];

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
