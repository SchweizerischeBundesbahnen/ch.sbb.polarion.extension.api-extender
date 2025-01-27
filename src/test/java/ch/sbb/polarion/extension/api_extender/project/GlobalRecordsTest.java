package ch.sbb.polarion.extension.api_extender.project;

import ch.sbb.polarion.extension.api_extender.rest.model.Field;
import ch.sbb.polarion.extension.api_extender.rest.model.Records;
import ch.sbb.polarion.extension.generic.jaxb.JAXBUtils;
import com.polarion.alm.shared.api.transaction.ReadOnlyTransaction;
import com.polarion.alm.shared.api.transaction.RunnableInReadOnlyTransaction;
import com.polarion.alm.shared.api.transaction.RunnableInWriteTransaction;
import com.polarion.alm.shared.api.transaction.TransactionalExecutor;
import com.polarion.alm.shared.api.transaction.WriteTransaction;
import com.polarion.platform.service.repository.IRepositoryConnection;
import com.polarion.platform.service.repository.IRepositoryReadOnlyConnection;
import com.polarion.platform.service.repository.IRepositoryService;
import com.polarion.subterra.base.location.ILocation;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.xml.bind.UnmarshalException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings({"unchecked", "rawtypes"})
class GlobalRecordsTest {

    @Test
    @SneakyThrows
    void testGetRecord() {
        try (MockedStatic<TransactionalExecutor> mockedExecutor = mockStatic(TransactionalExecutor.class);
             InputStream fieldsXml = this.getClass().getResourceAsStream("/fields.xml")) {
            IRepositoryService repositoryService = mock(IRepositoryService.class);

            IRepositoryReadOnlyConnection mockedConnection = mock(IRepositoryReadOnlyConnection.class);
            when(repositoryService.getReadOnlyConnection(any(ILocation.class))).thenReturn(mockedConnection);
            when(mockedConnection.exists(any(ILocation.class))).thenReturn(true);

            mockedExecutor.when(() -> TransactionalExecutor.executeSafelyInReadOnlyTransaction(any(RunnableInReadOnlyTransaction.class)))
                    .thenAnswer(invocation -> {
                        RunnableInReadOnlyTransaction transaction = invocation.getArgument(0);
                        return transaction.run(mock(ReadOnlyTransaction.class));
                    });

            GlobalRecords records = new GlobalRecords(repositoryService);

            // records do not exist
            when(mockedConnection.getContent(any(ILocation.class))).thenReturn(null);
            assertNull(records.getRecord("name"));

            // saved value contains nonsense
            when(mockedConnection.getContent(any(ILocation.class))).thenReturn(IOUtils.toInputStream("weirdString", StandardCharsets.UTF_8));
            Assertions.assertThrows(UnmarshalException.class, () -> records.getRecord("name"));

            // successful case
            when(mockedConnection.getContent(any(ILocation.class))).thenReturn(fieldsXml);
            Field field = records.getRecord("name");
            assertEquals("E-Library", field == null ? null : field.getValue());
        }
    }

    @Test
    @SneakyThrows
    void testSetRecord() {
        try (MockedStatic<TransactionalExecutor> mockedExecutor = mockStatic(TransactionalExecutor.class)) {
            IRepositoryService repositoryService = mock(IRepositoryService.class);

            IRepositoryReadOnlyConnection mockedConnection = mock(IRepositoryReadOnlyConnection.class);
            when(repositoryService.getReadOnlyConnection(any(ILocation.class))).thenReturn(mockedConnection);
            IRepositoryConnection mockedWriteConnection = mock(IRepositoryConnection.class);
            when(repositoryService.getConnection(any(ILocation.class))).thenReturn(mockedWriteConnection);
            when(mockedConnection.exists(any(ILocation.class))).thenReturn(true);

            mockedExecutor.when(() -> TransactionalExecutor.executeSafelyInReadOnlyTransaction(any(RunnableInReadOnlyTransaction.class)))
                    .thenAnswer(invocation -> {
                        RunnableInReadOnlyTransaction transaction = invocation.getArgument(0);
                        return transaction.run(mock(ReadOnlyTransaction.class));
                    });

            mockedExecutor.when(() -> TransactionalExecutor.executeInWriteTransaction(any(RunnableInWriteTransaction.class)))
                    .thenAnswer(invocation -> {
                        RunnableInWriteTransaction transaction = invocation.getArgument(0);
                        return transaction.run(mock(WriteTransaction.class));
                    });

            when(mockedConnection.getContent(any(ILocation.class))).thenReturn(IOUtils.toInputStream(JAXBUtils.serialize(new Records()), StandardCharsets.UTF_8));

            GlobalRecords records = new GlobalRecords(repositoryService);
            records.setRecord("name", "value");

            // must create a new one record
            verify(mockedWriteConnection, times(1)).create(any(ILocation.class), any(InputStream.class));
            verify(mockedWriteConnection, times(0)).setContent(any(ILocation.class), any(InputStream.class));

            when(mockedConnection.getContent(any(ILocation.class))).thenReturn(IOUtils.toInputStream(JAXBUtils.serialize(new Records()), StandardCharsets.UTF_8));
            when(mockedWriteConnection.exists(any(ILocation.class))).thenReturn(true);
            records.setRecord("name", "value");

            // must reuse existing one
            verify(mockedWriteConnection, times(1)).create(any(ILocation.class), any(InputStream.class));
            verify(mockedWriteConnection, times(1)).setContent(any(ILocation.class), any(InputStream.class));
        }
    }
}
