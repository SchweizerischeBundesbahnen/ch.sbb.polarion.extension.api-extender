package ch.sbb.polarion.extension.api.extender.rest;

import ch.sbb.polarion.extension.api.extender.rest.controller.GlobalRecordApiController;
import ch.sbb.polarion.extension.api.extender.rest.controller.GlobalRecordInternalController;
import ch.sbb.polarion.extension.api.extender.rest.controller.ProjectCustomFieldApiController;
import ch.sbb.polarion.extension.api.extender.rest.controller.ProjectCustomFieldInternalController;
import ch.sbb.polarion.extension.api.extender.settings.GlobalRecordsSettings;
import ch.sbb.polarion.extension.api.extender.settings.ProjectCustomFieldsSettings;
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
    protected @NotNull Set<Object> getExtensionControllerSingletons() {
        return Set.of(
                new GlobalRecordApiController(),
                new GlobalRecordInternalController(),
                new ProjectCustomFieldApiController(),
                new ProjectCustomFieldInternalController()
        );
    }
}
