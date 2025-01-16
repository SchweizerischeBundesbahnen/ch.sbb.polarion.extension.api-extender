package ch.sbb.polarion.extension.api.extender.velocity;

import ch.sbb.polarion.extension.api.extender.project.CustomFieldsProject;
import ch.sbb.polarion.extension.api.extender.rest.model.Field;
import ch.sbb.polarion.extension.generic.context.CurrentContextConfig;
import ch.sbb.polarion.extension.generic.context.CurrentContextExtension;
import ch.sbb.polarion.extension.generic.rest.model.Version;
import ch.sbb.polarion.extension.generic.test_extensions.PlatformContextMockExtension;
import ch.sbb.polarion.extension.generic.util.VersionUtils;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.xml.bind.JAXBException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith({MockitoExtension.class, PlatformContextMockExtension.class})
@CurrentContextConfig("api-extender")
public class VelocityReadOnlyCustomFieldsProjectTest {
    private VelocityReadOnlyCustomFieldsProject project;

    @BeforeEach
    void setUp() {
        project = new VelocityReadOnlyCustomFieldsProject();
    }

    @Test
    void getVersionReturnsCorrectVersionTest() {
        try (MockedStatic<VersionUtils> versionsUtilsMockedStatic = mockStatic(VersionUtils.class)) {
            Version mockVersion = mock(Version.class);
            versionsUtilsMockedStatic.when(VersionUtils::getVersion).thenReturn(mockVersion);
            when(mockVersion.getBundleVersion()).thenReturn("1.0.0");

            String version = project.getVersion();
            assertEquals("1.0.0", version);
        }
    }

    @Test
    void getVersionHandlesNullGracefullyTest() {
        try (MockedStatic<VersionUtils> versionsUtilsMockedStatic = mockStatic(VersionUtils.class)) {
            Version mockVersion = mock(Version.class);
            versionsUtilsMockedStatic.when(VersionUtils::getVersion).thenReturn(mockVersion);
            when(mockVersion.getBundleVersion()).thenReturn(null);
            String version = project.getVersion();
            assertNull(version);
        }
    }

    @Test
    @SneakyThrows
    void getCustomFieldReturnsField() {
        Field mockField = mock(Field.class);
        try (MockedConstruction<CustomFieldsProject> mockedCustomFieldsProject = Mockito.mockConstruction(CustomFieldsProject.class, (mock, context) ->
                when(mock.getCustomField(anyString())).thenReturn(mockField))) {
            // Use the mock to test the behavior
            Field field = project.getCustomField("project1", "key1");
            assertNotNull(field);
            assertEquals(mockField, field);
        }
    }

    @Test
    @SneakyThrows
    void testGetCustomFieldReturnsNullForMissingField() {
        try (MockedConstruction<CustomFieldsProject> mockedCustomFieldsProject = Mockito.mockConstruction(CustomFieldsProject.class, (mock, context) ->
                when(mock.getCustomField(anyString())).thenReturn(null))) {
            Field field = project.getCustomField("project1", "key1");
            assertNull(field);
        }
    }

    @Test
    @SneakyThrows
    void testGetCustomFieldThrowsJAXBException() {
        try (MockedConstruction<CustomFieldsProject> mockedCustomFieldsProject = Mockito.mockConstruction(CustomFieldsProject.class, (mock, context) ->
                when(mock.getCustomField(anyString())).thenThrow(new JAXBException("Test exception")))) {
            // Assert the exception is thrown
            assertThrows(JAXBException.class, () -> project.getCustomField("project1", "key1"));
        }
    }
}
