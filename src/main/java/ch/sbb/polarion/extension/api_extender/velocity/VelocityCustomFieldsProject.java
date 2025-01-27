package ch.sbb.polarion.extension.api_extender.velocity;

import ch.sbb.polarion.extension.api_extender.project.CustomFieldsProject;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import javax.xml.bind.JAXBException;
import java.io.IOException;

@NoArgsConstructor
public class VelocityCustomFieldsProject extends VelocityReadOnlyCustomFieldsProject {

    public void setCustomField(@NotNull String projectId, @NotNull String key, @NotNull String value) throws JAXBException, IOException {
        new CustomFieldsProject(projectId).setCustomField(key, value);
    }
}
