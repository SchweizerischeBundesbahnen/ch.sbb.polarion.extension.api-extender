package ch.sbb.polarion.extension.api_extender.rest.controller;

import ch.sbb.polarion.extension.api_extender.project.GlobalRecords;
import ch.sbb.polarion.extension.api_extender.rest.model.Field;
import ch.sbb.polarion.extension.api_extender.settings.GlobalRecordsSettingsModel;
import ch.sbb.polarion.extension.generic.service.PolarionService;
import ch.sbb.polarion.extension.generic.settings.GenericNamedSettings;
import ch.sbb.polarion.extension.generic.settings.NamedSettingsRegistry;
import ch.sbb.polarion.extension.generic.test_extensions.PlatformContextMockExtension;
import com.polarion.alm.projects.IProjectService;
import com.polarion.alm.tracker.ITrackerService;
import com.polarion.platform.IPlatformService;
import com.polarion.platform.security.ISecurityService;
import com.polarion.platform.service.repository.IRepositoryService;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.ws.rs.ForbiddenException;
import javax.ws.rs.NotFoundException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SuppressWarnings({"rawtypes", "unused", "unchecked"})
@ExtendWith({MockitoExtension.class, PlatformContextMockExtension.class})
class GlobalRecordInternalControllerTest {

    @BeforeEach
    void setup() {
        NamedSettingsRegistry.INSTANCE.getAll().clear();
    }

    @AfterEach
    void closeContext() {
        NamedSettingsRegistry.INSTANCE.getAll().clear();
    }

    @Test
    @SneakyThrows
    void testGetRecordValue() {
        try (MockedConstruction<GlobalRecords> mockedGlobalRecords = Mockito.mockConstruction(GlobalRecords.class, (mock, context) ->
                when(mock.getRecord(anyString())).thenReturn(new Field("someId", "someValue")))) {
            GlobalRecordInternalController controller = new GlobalRecordInternalController();
            Field field = controller.getRecordValue("someId");
            assertEquals("someValue", field == null ? null : field.getValue());
        }

        try (MockedConstruction<GlobalRecords> mockedGlobalRecords = Mockito.mockConstruction(GlobalRecords.class, (mock, context) ->
                when(mock.getRecord(anyString())).thenReturn(null))) {
            GlobalRecordInternalController controller = new GlobalRecordInternalController();
            NotFoundException notFoundException = Assertions.assertThrows(NotFoundException.class, () -> controller.getRecordValue("someId"));
            assertEquals("key 'someId' not found", notFoundException.getMessage());
        }
    }

    @Test
    @SneakyThrows
    void testSetRecordValue() {
        Field field = new Field("someId", "someValue");
        try (MockedConstruction<GlobalRecords> mockedGlobalRecords = Mockito.mockConstruction(GlobalRecords.class, (mock, context) ->
                when(mock.getRecord(anyString())).thenReturn(field))) {

            ISecurityService securityService = mockRoles();

            PolarionService polarionService = new PolarionService(mock(ITrackerService.class), mock(IProjectService.class), securityService, mock(IPlatformService.class), mock(IRepositoryService.class));
            GlobalRecordInternalController controller = new GlobalRecordInternalController(polarionService);

            when(securityService.getRolesForUser("userId")).thenReturn(List.of("role3"));
            ForbiddenException forbiddenException = Assertions.assertThrows(ForbiddenException.class, () -> controller.setRecordValue("someId", field));
            assertEquals("You are not authorized to modify records", forbiddenException.getMessage());

            when(securityService.getRolesForUser("userId")).thenReturn(List.of("role1"));
            controller.setRecordValue("someId", field);
            verify(mockedGlobalRecords.constructed().get(0), times(1)).setRecord("someId", "someValue");
        }
    }

    @Test
    @SneakyThrows
    void testDeleteRecordValue() {
        Field field = new Field("someId", "someValue");
        try (MockedConstruction<GlobalRecords> mockedGlobalRecords = Mockito.mockConstruction(GlobalRecords.class, (mock, context) ->
                when(mock.getRecord(anyString())).thenReturn(field))) {

            ISecurityService securityService = mockRoles();

            PolarionService polarionService = new PolarionService(mock(ITrackerService.class), mock(IProjectService.class), securityService, mock(IPlatformService.class), mock(IRepositoryService.class));
            GlobalRecordInternalController controller = new GlobalRecordInternalController(polarionService);

            when(securityService.getRolesForUser("userId")).thenReturn(List.of("role3"));
            ForbiddenException forbiddenException = Assertions.assertThrows(ForbiddenException.class, () -> controller.deleteRecordValue("someId"));
            assertEquals("You are not authorized to modify records", forbiddenException.getMessage());

            when(securityService.getRolesForUser("userId")).thenReturn(List.of("role1"));
            controller.deleteRecordValue("someId");
            verify(mockedGlobalRecords.constructed().get(0), times(1)).setRecord("someId", null);
        }
    }

    private ISecurityService mockRoles() {
        ISecurityService securityService = mock(ISecurityService.class);
        when(securityService.getCurrentUser()).thenReturn("userId");

        GenericNamedSettings settings = mock(GenericNamedSettings.class);
        when(settings.getFeatureName()).thenReturn("global_records");
        NamedSettingsRegistry.INSTANCE.register(Collections.singletonList(settings));
        GlobalRecordsSettingsModel settingsModel = mock(GlobalRecordsSettingsModel.class);
        when(settingsModel.getAllRoles()).thenReturn(Arrays.asList("role1", "role2"));
        when(settings.read(any(), any(), any())).thenReturn(settingsModel);
        return securityService;
    }

}
