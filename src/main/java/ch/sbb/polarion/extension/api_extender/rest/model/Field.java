package ch.sbb.polarion.extension.api_extender.rest.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.XmlValue;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "field")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Schema(description = "Represents a field object with an ID and value")
public class Field {

    @XmlAttribute(name = "id")
    @JsonIgnore
    @Schema(description = "The key of the field, ignored in JSON serialization")
    public String key;

    @XmlValue
    @Schema(description = "The value associated with the field")
    public String value;
}
