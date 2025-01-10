package ch.sbb.polarion.extension.api_extender.project;

import ch.sbb.polarion.extension.api_extender.rest.model.Field;
import ch.sbb.polarion.extension.api_extender.rest.model.Project;
import ch.sbb.polarion.extension.generic.service.PolarionService;
import com.polarion.alm.projects.model.IProject;
import com.polarion.platform.core.PlatformContext;
import com.polarion.platform.service.repository.IRepositoryService;
import com.polarion.subterra.base.location.ILocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.xml.bind.JAXBException;
import java.io.IOException;

public class CustomFieldsProject extends GenericFields<Project> {
    private static final String POLARION_POLARION_PROJECT_XML = ".polarion/polarion-project.xml";

    private final String projectId;

    public CustomFieldsProject(String projectId) {
        super(PlatformContext.getPlatform().lookupService(IRepositoryService.class));
        this.projectId = projectId;
    }

    @Nullable
    public Field getCustomField(@NotNull String key) throws JAXBException {
        final Project project = deserialize();
        return project.getField(key);
    }

    public void setCustomField(@NotNull String key, @Nullable String value) throws JAXBException, IOException {
        final Project project = deserialize();
        project.setField(key, value);
        serialize(project);
    }

    @Override
    protected ILocation getLocation() {
        IProject project = new PolarionService().getProject(projectId);
        return project.getLocation().append(POLARION_POLARION_PROJECT_XML);
    }

}
