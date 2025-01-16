package ch.sbb.polarion.extension.api.extender.project;

import ch.sbb.polarion.extension.api.extender.rest.model.Field;
import ch.sbb.polarion.extension.api.extender.rest.model.Project;
import ch.sbb.polarion.extension.generic.service.PolarionService;
import com.polarion.alm.projects.model.IProject;
import com.polarion.platform.core.PlatformContext;
import com.polarion.platform.service.repository.IRepositoryService;
import com.polarion.subterra.base.location.ILocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

import javax.xml.bind.JAXBException;
import java.io.IOException;

public class CustomFieldsProject extends GenericFields<Project> {
    private static final String POLARION_POLARION_PROJECT_XML = ".polarion/polarion-project.xml";

    private final String projectId;

    private final PolarionService polarionService;

    public CustomFieldsProject(String projectId) {
        super(PlatformContext.getPlatform().lookupService(IRepositoryService.class));
        polarionService = PlatformContext.getPlatform().lookupService(PolarionService.class);
        this.projectId = projectId;
    }

    @VisibleForTesting
    CustomFieldsProject(String projectId, PolarionService polarionService) {
        super(PlatformContext.getPlatform().lookupService(IRepositoryService.class));
        this.projectId = projectId;
        this.polarionService = polarionService;
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
        IProject project = polarionService.getProject(projectId);
        return project.getLocation().append(POLARION_POLARION_PROJECT_XML);
    }

}
