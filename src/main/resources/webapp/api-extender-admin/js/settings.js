const DEFAULT_SETTING_NAME = 'Default';
SbbCommon.init({
    extension: 'api-extender',
    setting: SbbCommon.getValueById("settings_name"),
    scope: SbbCommon.getValueById('scope')
});

function getSelectedRoles(containerId) {
    const result = [];
    const checkboxes = document.querySelectorAll(`div#${containerId} input[type=checkbox]:checked`);

    checkboxes.forEach((checkbox) => {
        result.push(checkbox.id);
    });

    return result;
}

function setSelectedRoles(containerId, allowedRoles) {
    const checkboxes = document.querySelectorAll(`div#${containerId} input[type=checkbox]`);

    checkboxes.forEach((checkbox) => {
        checkbox.checked = allowedRoles.includes(checkbox.id);
    })
}

function parseAndSetSettings(text) {
    const settings = JSON.parse(text);

    setSelectedRoles('global_roles', settings.globalRoles);
    setSelectedRoles('project_roles', settings.projectRoles);

    if (settings.bundleTimestamp !== SbbCommon.getValueById('bundle-timestamp')) {
        SbbCommon.setNewerVersionNotificationVisible(true);
    }
}

function saveSettings() {
    SbbCommon.hideActionAlerts();

    SbbCommon.callAsync({
        method: 'PUT',
        url: `/polarion/${SbbCommon.extension}/rest/internal/settings/${SbbCommon.setting}/names/${DEFAULT_SETTING_NAME}/content?scope=${SbbCommon.scope}`,
        contentType: 'application/json',
        body: JSON.stringify({
            'globalRoles': getSelectedRoles('global_roles'),
            'projectRoles': getSelectedRoles('project_roles')
        }),
        onOk: () => {
            SbbCommon.showSaveSuccessAlert();
            SbbCommon.setNewerVersionNotificationVisible(false);
            readAndFillRevisions();
        },
        onError: () => SbbCommon.showSaveErrorAlert()
    });
}

function cancelEdit() {
    if (confirm("Are you sure you want to cancel editing and revert all changes made?")) {
        readSettings();
    }
}

function readSettings() {
    SbbCommon.setLoadingErrorNotificationVisible(false);

    SbbCommon.callAsync({
        method: 'GET',
        url: `/polarion/${SbbCommon.extension}/rest/internal/settings/${SbbCommon.setting}/names/${DEFAULT_SETTING_NAME}/content?scope=${SbbCommon.scope}`,
        contentType: 'application/json',
        onOk: (responseText) => {
            parseAndSetSettings(responseText, true);
            readAndFillRevisions();
        },
        onError: () => SbbCommon.setLoadingErrorNotificationVisible(true)
    });
}

function readAndFillRevisions() {
    SbbCommon.readAndFillRevisions({
        revertToRevisionCallback: (responseText) => parseAndSetSettings(responseText)
    });
}

function revertToDefault() {
    if (confirm("Are you sure you want to return the default values?")) {
        SbbCommon.setLoadingErrorNotificationVisible(false);
        SbbCommon.hideActionAlerts();

        SbbCommon.callAsync({
            method: 'GET',
            url: `/polarion/${SbbCommon.extension}/rest/internal/settings/${SbbCommon.setting}/default-content`,
            contentType: 'application/json',
            onOk: (responseText) => {
                parseAndSetSettings(responseText);
                SbbCommon.showRevertedToDefaultAlert();
            },
            onError: () => SbbCommon.setLoadingErrorNotificationVisible(true)
        });
    }
}

readSettings();
