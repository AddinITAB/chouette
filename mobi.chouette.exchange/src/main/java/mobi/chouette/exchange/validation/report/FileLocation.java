package mobi.chouette.exchange.validation.report;

import java.io.PrintStream;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import mobi.chouette.exchange.report.AbstractReport;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

@Data
@EqualsAndHashCode(callSuper = false)
@ToString
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = { "filename", "lineNumber", "columnNumber" })
public class FileLocation extends AbstractReport {

	@XmlElement(name = "filename", required = true)
	private String filename;

	@XmlElement(name = "line_number")
	private Integer lineNumber;

	@XmlElement(name = "column_number")
	private Integer columnNumber;

	public FileLocation(String fileName) {
		this.filename = fileName;
	}

	public FileLocation(String fileName, int lineNumber, int columnNumber) {
		this.filename = fileName;
		if (Integer.valueOf(lineNumber) >= 0)
			this.lineNumber = Integer.valueOf(lineNumber);
		if (Integer.valueOf(columnNumber) >= 0)
			this.columnNumber = Integer.valueOf(columnNumber);
	}

	public FileLocation(String fileName, int lineNumber, int columnNumber, String objectId) {
		this.filename = fileName;
		if (Integer.valueOf(lineNumber) >= 0)
			this.lineNumber = Integer.valueOf(lineNumber);
		if (Integer.valueOf(columnNumber) >= 0)
			this.columnNumber = Integer.valueOf(columnNumber);
	}

	public JSONObject toJson() throws JSONException {
		JSONObject object = new JSONObject();
		object.put("filename", filename);
		if (lineNumber != null) {
			object.put("line_number", lineNumber);
		}
		if (columnNumber != null) {
			object.put("column_number", columnNumber);
		}
		return object;
	}

	@Override
	public void print(PrintStream out, StringBuilder ret , int level, boolean first) {
		ret.setLength(0);
		out.print(addLevel(ret, level).append('{'));
		out.print(toJsonString(ret, level + 1, "filename", filename, true));
		if (lineNumber != null) {
			out.print(toJsonString(ret, level + 1, "line_number", lineNumber, false));
		}
		if (columnNumber != null) {
			out.print(toJsonString(ret, level + 1, "column_number", columnNumber, false));
		}
		ret.setLength(0);
		out.print(addLevel(ret.append('\n'), level).append('}'));

	}

}
