package ch.sbb.polarion.extension.api.extender.settings;

import ch.sbb.polarion.extension.generic.settings.GenericNamedSettings;
import org.jetbrains.annotations.NotNull;

public class ProjectCustomFieldsSettings extends GenericNamedSettings<ProjectCustomFieldsSettingsModel> {
    public static final String FEATURE_NAME = "project_custom_fields";

    public ProjectCustomFieldsSettings() {
        super(FEATURE_NAME);
    }

    @Override
    public @NotNull ProjectCustomFieldsSettingsModel defaultValues() {
        final ProjectCustomFieldsSettingsModel projectCustomFieldsSettingsModel = new ProjectCustomFieldsSettingsModel();
        projectCustomFieldsSettingsModel.setGlobalRoles("admin");
        projectCustomFieldsSettingsModel.setProjectRoles();
        return projectCustomFieldsSettingsModel;
    }

}
