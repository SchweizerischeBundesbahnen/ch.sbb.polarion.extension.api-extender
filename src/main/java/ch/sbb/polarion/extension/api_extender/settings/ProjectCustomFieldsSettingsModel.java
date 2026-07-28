package ch.sbb.polarion.extension.api_extender.settings;

import ch.sbb.polarion.extension.generic.settings.AuthorizationModel;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Who may write project custom fields: global and project roles, stored exactly as the shared
 * {@link AuthorizationModel} does. It adds nothing of its own - the type exists so the settings
 * framework can register it under its own feature name.
 */
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProjectCustomFieldsSettingsModel extends AuthorizationModel {
}
