package ch.sbb.polarion.extension.api.extender.rest;

import ch.sbb.polarion.extension.api.extender.rest.controller.GlobalRecordApiController;
import ch.sbb.polarion.extension.api.extender.rest.controller.GlobalRecordInternalController;
import ch.sbb.polarion.extension.api.extender.rest.controller.ProjectCustomFieldApiController;
import ch.sbb.polarion.extension.api.extender.rest.controller.ProjectCustomFieldInternalController;
import ch.sbb.polarion.extension.api.extender.settings.GlobalRecordsSettings;
import ch.sbb.polarion.extension.api.extender.settings.ProjectCustomFieldsSettings;
import ch.sbb.polarion.extension.generic.rest.GenericRestApplication;
import ch.sbb.polarion.extension.generic.settings.GenericNamedSettings;
import ch.sbb.polarion.extension.generic.settings.NamedSettingsRegistry;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ApiExtenderRestApplication extends GenericRestApplication {

    public ApiExtenderRestApplication() {
        List<GenericNamedSettings<?>> settingsList = new ArrayList<>();
        settingsList.add(new GlobalRecordsSettings());
        settingsList.add(new ProjectCustomFieldsSettings());

        NamedSettingsRegistry.INSTANCE.register(settingsList);
    }

    @Override
    @NotNull
    protected Set<Class<?>> getControllerClasses() {
        final Set<Class<?>> controllerClasses = super.getControllerClasses();
        controllerClasses.addAll(Set.of(
                GlobalRecordApiController.class,
                GlobalRecordInternalController.class,
                ProjectCustomFieldApiController.class,
                ProjectCustomFieldInternalController.class
        ));
        return controllerClasses;
    }
}
