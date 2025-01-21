package ch.sbb.polarion.extension.api.extender.project;

import ch.sbb.polarion.extension.api.extender.rest.model.Field;
import ch.sbb.polarion.extension.api.extender.rest.model.Project;
import ch.sbb.polarion.extension.generic.context.CurrentContextConfig;
import ch.sbb.polarion.extension.generic.context.CurrentContextExtension;
import ch.sbb.polarion.extension.generic.service.PolarionService;
import ch.sbb.polarion.extension.generic.test_extensions.PlatformContextMockExtension;
import com.polarion.alm.projects.model.IProject;
import com.polarion.subterra.base.location.ILocation;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;


@ExtendWith({MockitoExtension.class, CurrentContextExtension.class, PlatformContextMockExtension.class})
@CurrentContextConfig("api-extender")
class CustomFieldsProjectTest {

    private CustomFieldsProject customFieldsProject;
    private PolarionService polarionServiceMock;
    private IProject projectMock;
    private Project projectObjectMock;

    private static final String PROJECT_ID = "projectId";
    private static final String FIELD_KEY = "customKey";
    private static final String FIELD_VALUE = "customValue";

    @BeforeEach
    void setUp() {
        polarionServiceMock = mock(PolarionService.class);
        projectMock = mock(IProject.class);
        projectObjectMock = mock(Project.class);
        customFieldsProject = new CustomFieldsProject(PROJECT_ID, polarionServiceMock);
    }

    @Test
    @SneakyThrows
    void getCustomField() {
        Field expectedField = new Field(FIELD_KEY, FIELD_VALUE);
        when(projectObjectMock.getField(FIELD_KEY)).thenReturn(expectedField);

        CustomFieldsProject customFieldsProjectSpy = spy(customFieldsProject);
        doReturn(projectObjectMock).when(customFieldsProjectSpy).deserialize();

        Field actualField = customFieldsProjectSpy.getCustomField(FIELD_KEY);

        assertNotNull(actualField);
        assertEquals(FIELD_VALUE, actualField.getValue());
    }

    @Test
    @SneakyThrows
    void setCustomField() {
        CustomFieldsProject customFieldsProjectSpy = spy(new CustomFieldsProject(PROJECT_ID, polarionServiceMock));
        doReturn(projectObjectMock).when(customFieldsProjectSpy).deserialize();
        doNothing().when(customFieldsProjectSpy).serialize(projectObjectMock);

        customFieldsProjectSpy.setCustomField(FIELD_KEY, FIELD_VALUE);

        verify(customFieldsProjectSpy, times(1)).setCustomField(FIELD_KEY, FIELD_VALUE);
    }

    @Test
    void getLocation() {
        when(polarionServiceMock.getProject(PROJECT_ID)).thenReturn(projectMock);

        ILocation locationMock = mock(ILocation.class);
        when(projectMock.getLocation()).thenReturn(locationMock);
        when(locationMock.append(".polarion/polarion-project.xml")).thenReturn(locationMock);

        ILocation result = customFieldsProject.getLocation();

        assertNotNull(result);
        verify(polarionServiceMock, times(1)).getProject(PROJECT_ID);
        verify(projectMock, times(1)).getLocation();
        verify(locationMock, times(1)).append(".polarion/polarion-project.xml");
    }
}
