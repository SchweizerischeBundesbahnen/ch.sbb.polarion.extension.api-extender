package ch.sbb.polarion.extension.api_extender.settings;

import ch.sbb.polarion.extension.generic.settings.GenericNamedSettings;
import ch.sbb.polarion.extension.generic.settings.SettingsService;
import org.jetbrains.annotations.NotNull;

public class GlobalRecordsSettings extends GenericNamedSettings<GlobalRecordsSettingsModel> {
    public static final String FEATURE_NAME = "global_records";

    public GlobalRecordsSettings() {
        super(FEATURE_NAME);
    }

    public GlobalRecordsSettings(SettingsService settingsService) {
        super(FEATURE_NAME, settingsService);
    }

    @Override
    public @NotNull GlobalRecordsSettingsModel defaultValues() {
        final GlobalRecordsSettingsModel globalRecordsSettingsModel = new GlobalRecordsSettingsModel();
        globalRecordsSettingsModel.setGlobalRoles("admin");
        return globalRecordsSettingsModel;
    }

}
