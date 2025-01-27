package ch.sbb.polarion.extension.api_extender.settings;

import ch.sbb.polarion.extension.generic.settings.GenericNamedSettings;
import ch.sbb.polarion.extension.generic.settings.SettingsService;
import org.jetbrains.annotations.NotNull;

public class ProjectCustomFieldsSettings extends GenericNamedSettings<ProjectCustomFieldsSettingsModel> {
    public static final String FEATURE_NAME = "project_custom_fields";

    public ProjectCustomFieldsSettings() {
        super(FEATURE_NAME);
    }

    public ProjectCustomFieldsSettings(SettingsService settingsService) {
        super(FEATURE_NAME, settingsService);
    }

    @Override
    public @NotNull ProjectCustomFieldsSettingsModel defaultValues() {
        final ProjectCustomFieldsSettingsModel projectCustomFieldsSettingsModel = new ProjectCustomFieldsSettingsModel();
        projectCustomFieldsSettingsModel.setGlobalRoles("admin");
        projectCustomFieldsSettingsModel.setProjectRoles();
        return projectCustomFieldsSettingsModel;
    }

}
