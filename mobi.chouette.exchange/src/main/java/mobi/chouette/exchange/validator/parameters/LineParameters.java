package mobi.chouette.exchange.validator.parameters;

import java.util.Arrays;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

import lombok.Data;
import mobi.chouette.model.Line;

@XmlAccessorType(XmlAccessType.FIELD)
@Data
public class LineParameters {
	
	@XmlTransient
	public static String[] fields = { "Objectid", "Name", "RegistrationNumber","Number","PublishedName"} ;
	
	static {
		ValidationParametersUtil.addFieldList(Line.class.getSimpleName(), Arrays.asList(fields));
	}

	@XmlElement(name = "objectid")
	private FieldParameters objectid;

	@XmlElement(name = "name")
	private FieldParameters name;

	@XmlElement(name = "number")
	private FieldParameters number;

	@XmlElement(name = "published_name")
	private FieldParameters publishedName;

	@XmlElement(name = "registration_number")
	private FieldParameters registrationNumber;

}