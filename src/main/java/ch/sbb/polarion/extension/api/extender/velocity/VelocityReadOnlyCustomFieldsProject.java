package ch.sbb.polarion.extension.api.extender.velocity;

import ch.sbb.polarion.extension.api.extender.project.CustomFieldsProject;
import ch.sbb.polarion.extension.api.extender.rest.model.Field;
import ch.sbb.polarion.extension.generic.util.VersionUtils;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.xml.bind.JAXBException;

@NoArgsConstructor
public class VelocityReadOnlyCustomFieldsProject {

    @Nullable
    public String getVersion() {
        return VersionUtils.getVersion().getBundleVersion();
    }

    @Nullable
    public Field getCustomField(@NotNull String projectId, @NotNull String key) throws JAXBException {
        return new CustomFieldsProject(projectId).getCustomField(key);
    }
}
