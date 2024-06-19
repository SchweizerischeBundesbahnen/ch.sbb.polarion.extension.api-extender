package ch.sbb.polarion.extension.api.extender.velocity;

import ch.sbb.polarion.extension.api.extender.project.GlobalRecords;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import javax.xml.bind.JAXBException;
import java.io.IOException;

@NoArgsConstructor
public class VelocityGlobalRecords extends VelocityReadOnlyGlobalRecords {

    public void setField(@NotNull String key, @NotNull String value) throws JAXBException, IOException {
        new GlobalRecords().setRecord(key, value);
    }
}
