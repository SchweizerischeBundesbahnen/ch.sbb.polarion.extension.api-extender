package ch.sbb.polarion.extension.api.extender.rest.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "field")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class Field {
    @XmlAttribute(name = "id")
    @JsonIgnore
    public String key;
    @XmlValue
    public String value;
}
