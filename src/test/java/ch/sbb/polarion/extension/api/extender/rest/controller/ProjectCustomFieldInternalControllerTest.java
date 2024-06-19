package ch.sbb.polarion.extension.api.extender.rest.controller;

import ch.sbb.polarion.extension.api.extender.project.CustomFieldsProject;
import ch.sbb.polarion.extension.api.extender.rest.model.Field;
import ch.sbb.polarion.extension.api.extender.settings.ProjectCustomFieldsSettingsModel;
import ch.sbb.polarion.extension.generic.service.PolarionService;
import ch.sbb.polarion.extension.generic.settings.GenericNamedSettings;
import ch.sbb.polarion.extension.generic.settings.NamedSettingsRegistry;
import com.polarion.alm.projects.IProjectService;
import com.polarion.alm.tracker.ITrackerService;
import com.polarion.platform.IPlatformService;
import com.polarion.platform.core.IPlatform;
import com.polarion.platform.core.PlatformContext;
import com.polarion.platform.security.ISecurityService;
import com.polarion.platform.service.repository.IRepositoryService;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import javax.ws.rs.ForbiddenException;
import javax.ws.rs.NotFoundException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SuppressWarnings({"rawtypes", "unused", "unchecked", "ResultOfMethodCallIgnored"})
class ProjectCustomFieldInternalControllerTest {

    @Test
    @SneakyThrows
    void testGetCustomValue() {
        try (MockedStatic<PlatformContext> platformContextMockedStatic = mockStatic(PlatformContext.class)) {

            mockPlatform(platformContextMockedStatic);

            try (MockedConstruction<CustomFieldsProject> mockedCustomFieldsProject = Mockito.mockConstruction(CustomFieldsProject.class, (mock, context) ->
                    when(mock.getCustomField(anyString())).thenReturn(new Field("someId", "someValue")))) {
                ProjectCustomFieldInternalController controller = new ProjectCustomFieldInternalController();
                Field field = controller.getCustomValue("testProj", "someId");
                assertEquals("someValue", field == null ? null : field.getValue());
            }

            try (MockedConstruction<CustomFieldsProject> mockedCustomFieldsProject = Mockito.mockConstruction(CustomFieldsProject.class, (mock, context) ->
                    when(mock.getCustomField(anyString())).thenReturn(null))) {
                ProjectCustomFieldInternalController controller = new ProjectCustomFieldInternalController();
                NotFoundException notFoundException = Assertions.assertThrows(NotFoundException.class, () -> controller.getCustomValue("testProj", "someId"));
                assertEquals("key 'someId' for project 'testProj' not found", notFoundException.getMessage());
            }
        }
    }

    @Test
    @SneakyThrows
    void testSetRecordValue() {
        try (MockedStatic<PlatformContext> platformContextMockedStatic = mockStatic(PlatformContext.class)) {

            mockPlatform(platformContextMockedStatic);

            Field field = new Field("someId", "someValue");
            try (MockedConstruction<CustomFieldsProject> mockedCustomFieldsProject = Mockito.mockConstruction(CustomFieldsProject.class, (mock, context) ->
                    when(mock.getCustomField(anyString())).thenReturn(field))) {

                ISecurityService securityService = mockRoles();

                PolarionService polarionService = new PolarionService(mock(ITrackerService.class), mock(IProjectService.class), securityService, mock(IPlatformService.class), mock(IRepositoryService.class));
                ProjectCustomFieldInternalController controller = new ProjectCustomFieldInternalController(polarionService);

                when(securityService.getRolesForUser("userId")).thenReturn(List.of("role3"));
                ForbiddenException forbiddenException = Assertions.assertThrows(ForbiddenException.class, () -> controller.setCustomValue("testProj", "someId", field));
                assertEquals("You are not authorized to modify custom fields", forbiddenException.getMessage());

                when(securityService.getRolesForUser("userId")).thenReturn(List.of("role1"));
                controller.setCustomValue("testProj", "someId", field);
                verify(mockedCustomFieldsProject.constructed().get(0), times(1)).setCustomField("someId", "someValue");
            }
        }
    }

    @Test
    @SneakyThrows
    void testDeleteRecordValue() {
        try (MockedStatic<PlatformContext> platformContextMockedStatic = mockStatic(PlatformContext.class)) {

            mockPlatform(platformContextMockedStatic);

            Field field = new Field("someId", "someValue");
            try (MockedConstruction<CustomFieldsProject> mockedCustomFieldsProject = Mockito.mockConstruction(CustomFieldsProject.class, (mock, context) ->
                    when(mock.getCustomField(anyString())).thenReturn(field))) {

                ISecurityService securityService = mockRoles();

                PolarionService polarionService = new PolarionService(mock(ITrackerService.class), mock(IProjectService.class), securityService, mock(IPlatformService.class), mock(IRepositoryService.class));
                ProjectCustomFieldInternalController controller = new ProjectCustomFieldInternalController(polarionService);

                when(securityService.getRolesForUser("userId")).thenReturn(List.of("role3"));
                ForbiddenException forbiddenException = Assertions.assertThrows(ForbiddenException.class, () -> controller.deleteCustomValue("testProj", "someId"));
                assertEquals("You are not authorized to modify custom fields", forbiddenException.getMessage());

                when(securityService.getRolesForUser("userId")).thenReturn(List.of("role1"));
                controller.deleteCustomValue("testProj", "someId");
                verify(mockedCustomFieldsProject.constructed().get(0), times(1)).setCustomField("someId", null);
            }
        }
    }

    private ISecurityService mockRoles() {
        ISecurityService securityService = mock(ISecurityService.class);
        when(securityService.getCurrentUser()).thenReturn("userId");

        GenericNamedSettings settings = mock(GenericNamedSettings.class);
        when(settings.getFeatureName()).thenReturn("project_custom_fields");
        NamedSettingsRegistry.INSTANCE.register(Collections.singletonList(settings));
        ProjectCustomFieldsSettingsModel settingsModel = mock(ProjectCustomFieldsSettingsModel.class);
        when(settingsModel.getAllRoles()).thenReturn(Arrays.asList("role1", "role2"));
        when(settings.read(any(), any(), any())).thenReturn(settingsModel);
        return securityService;
    }

    private void mockPlatform(MockedStatic<PlatformContext> platformContextMockedStatic) {
        IPlatform platform = mock(IPlatform.class);
        IRepositoryService repositoryService = mock(IRepositoryService.class);
        platformContextMockedStatic.when(PlatformContext::getPlatform).thenReturn(platform);
        when(platform.lookupService(IRepositoryService.class)).thenReturn(repositoryService);
    }

}
