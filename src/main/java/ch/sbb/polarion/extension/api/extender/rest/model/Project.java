package ch.sbb.polarion.extension.api.extender.rest.model;

import ch.sbb.polarion.extension.api.extender.util.CustomFieldUtils;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "project")
@ToString
public class Project extends GenericFields {

    @Override
    public void setField(@NotNull String key, @Nullable String value) {
        if (CustomFieldUtils.isStandardField(key)) {
            throw new IllegalArgumentException("'" + key + "' is a standard field of project");
        }

        super.setField(key, value);
    }

}
