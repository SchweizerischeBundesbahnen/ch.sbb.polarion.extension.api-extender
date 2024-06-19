package ch.sbb.polarion.extension.api.extender.project;

import ch.sbb.polarion.extension.generic.jaxb.JAXBUtils;
import com.polarion.alm.shared.api.transaction.TransactionalExecutor;
import com.polarion.core.util.logging.Logger;
import com.polarion.platform.service.repository.IRepositoryConnection;
import com.polarion.platform.service.repository.IRepositoryReadOnlyConnection;
import com.polarion.platform.service.repository.IRepositoryService;
import com.polarion.subterra.base.location.ILocation;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.xml.bind.JAXBException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.ParameterizedType;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

@SuppressWarnings("unchecked")
public abstract class GenericFields<T> {
    protected static final Logger logger = Logger.getLogger(GenericFields.class);
    protected final IRepositoryService repositoryService;

    protected final Class<T> type;

    protected GenericFields(IRepositoryService repositoryService) {
        this.repositoryService = repositoryService;
        this.type = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

    protected abstract ILocation getLocation();

    @NotNull
    protected T deserialize() throws JAXBException {
        final ILocation location = getLocation();
        final String content = read(location);
        if (content == null) {
            return createT();
        } else {
            return Objects.requireNonNull(JAXBUtils.deserialize(type, content));
        }
    }

    protected void serialize(@NotNull T records) throws JAXBException, IOException {
        final ILocation location = getLocation();
        final String content = JAXBUtils.serialize(records);
        save(location, content);
    }

    @Nullable
    protected String read(@NotNull ILocation location) {
        return TransactionalExecutor.executeSafelyInReadOnlyTransaction(transaction -> {
            IRepositoryReadOnlyConnection readOnlyConnection = repositoryService.getReadOnlyConnection(location);
            if (!readOnlyConnection.exists(location)) {
                logger.warn("Location does not exist: " + location.getLocationPath());
                return null;
            }

            try (InputStream inputStream = readOnlyConnection.getContent(location)) {
                return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            } catch (Exception e) {
                logger.error("Error reading content from: " + location.getLocationPath(), e);
                return null;
            }
        });
    }

    protected void save(@NotNull ILocation location, @NotNull String content) {
        TransactionalExecutor.executeInWriteTransaction(transaction -> {
            IRepositoryConnection connection = repositoryService.getConnection(location);
            try (InputStream inputStream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8))) {
                if (connection.exists(location)) {
                    connection.setContent(location, inputStream);
                } else {
                    connection.create(location, inputStream);
                }
            } catch (IOException e) {
                logger.error("Can't save to location: " + location, e);
            }
            return null;
        });
    }


    @SneakyThrows
    T createT() {
        return type.getDeclaredConstructor().newInstance();
    }

}
