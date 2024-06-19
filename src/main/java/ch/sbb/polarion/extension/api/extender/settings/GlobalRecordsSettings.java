package ch.sbb.polarion.extension.api.extender.settings;

import ch.sbb.polarion.extension.generic.settings.GenericNamedSettings;
import org.jetbrains.annotations.NotNull;

public class GlobalRecordsSettings extends GenericNamedSettings<GlobalRecordsSettingsModel> {
    public static final String FEATURE_NAME = "global_records";

    public GlobalRecordsSettings() {
        super(FEATURE_NAME);
    }

    @Override
    public @NotNull GlobalRecordsSettingsModel defaultValues() {
        final GlobalRecordsSettingsModel globalRecordsSettingsModel = new GlobalRecordsSettingsModel();
        globalRecordsSettingsModel.setGlobalRoles("admin");
        return globalRecordsSettingsModel;
    }

}
