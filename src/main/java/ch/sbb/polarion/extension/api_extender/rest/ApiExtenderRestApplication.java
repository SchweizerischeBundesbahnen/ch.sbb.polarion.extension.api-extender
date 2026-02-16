package ch.sbb.polarion.extension.api_extender.rest;

import ch.sbb.polarion.extension.api_extender.rest.controller.GlobalRecordApiController;
import ch.sbb.polarion.extension.api_extender.rest.controller.GlobalRecordInternalController;
import ch.sbb.polarion.extension.api_extender.rest.controller.ProjectCustomFieldApiController;
import ch.sbb.polarion.extension.api_extender.rest.controller.ProjectCustomFieldInternalController;
import ch.sbb.polarion.extension.api_extender.rest.controller.RegexToolApiController;
import ch.sbb.polarion.extension.api_extender.rest.controller.RegexToolInternalController;
import ch.sbb.polarion.extension.api_extender.settings.GlobalRecordsSettings;
import ch.sbb.polarion.extension.api_extender.settings.ProjectCustomFieldsSettings;
import ch.sbb.polarion.extension.generic.rest.GenericRestApplication;
import ch.sbb.polarion.extension.generic.settings.NamedSettingsRegistry;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Set;

public class ApiExtenderRestApplication extends GenericRestApplication {

    public ApiExtenderRestApplication() {
        NamedSettingsRegistry.INSTANCE.register(
                Arrays.asList(
                        new GlobalRecordsSettings(),
                        new ProjectCustomFieldsSettings()
                )
        );
    }

    @Override
    protected @NotNull Set<Class<?>> getExtensionControllerClasses() {
        return Set.of(
                GlobalRecordApiController.class,
                GlobalRecordInternalController.class,
                ProjectCustomFieldApiController.class,
                ProjectCustomFieldInternalController.class,
                RegexToolApiController.class,
                RegexToolInternalController.class
        );
    }
}
