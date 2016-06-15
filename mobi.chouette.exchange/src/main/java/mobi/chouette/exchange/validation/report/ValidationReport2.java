package mobi.chouette.exchange.validation.report;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

@ToString
@XmlRootElement(name = "validation_report")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = { "result", "checkPoints", "checkPointErrors" })
public class ValidationReport2 {

	@XmlElement(name = "result")
	@Getter
	@Setter
	private String result = "NO_VALIDATION";

	@XmlElement(name = "tests")
	@Getter
	@Setter
	private List<CheckPointReport> checkPoints = new ArrayList<CheckPointReport>();
	
	
	@XmlElement(name = "errors")
	@Getter
	@Setter
	private List<CheckPointErrorReport> checkPointErrors = new ArrayList<CheckPointErrorReport>();

	@XmlTransient
	@Getter
	@Setter
	private boolean maxByFile = true;


	protected void checkResult() {
		result = checkPoints.isEmpty() ? "NO_VALIDATION" : "VALIDATION_PROCEDEED";
	}
	
	protected void addCheckPointReport(CheckPointReport checkPoint)
	{
		checkPoint.setMaxByFile(maxByFile);
		checkPoints.add(checkPoint);
	}
	
	protected void addCheckPointErrorReport(CheckPointErrorReport checkPointError)
	{
		checkPointErrors.add(checkPointError);
	}

	protected void addAllCheckPoints(Collection<CheckPointReport> list)
	{
		for (CheckPointReport checkPoint : checkPoints) {
			checkPoint.setMaxByFile(maxByFile);
		}
		checkPoints.addAll(list);
	}

	public JSONObject toJson() throws JSONException {
		JSONObject validationReport = new JSONObject();
		validationReport.put("result", result);
		if (!checkPoints.isEmpty()) {
			JSONArray tests = new JSONArray();
			for (CheckPointReport checkPoint : checkPoints) {
				tests.put(checkPoint.toJson());
			}
			validationReport.put("tests", tests);
		}
		if (!checkPointErrors.isEmpty()) {
			JSONArray tests = new JSONArray();
			for (CheckPointErrorReport checkPointError : checkPointErrors) {
				tests.put(checkPointError.toJson());
			}
			validationReport.put("tests", tests);
		}
		JSONObject object = new JSONObject();
		object.put("validation_report", validationReport);
		return object;
	}
}
