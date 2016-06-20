package mobi.chouette.exchange.validation.report;

import mobi.chouette.common.Constant;
import mobi.chouette.common.Context;
import mobi.chouette.exchange.model.DataLocation;
import mobi.chouette.exchange.report.ActionReport2;

import org.codehaus.jettison.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ValidationReportTest implements Constant{
	
	@Test(groups = { "CheckPoint" }, description = "checkpoint add" ,priority=101 )
	public void verifyNewCheckPointAdd() throws Exception {
		Context context = new Context();
		context.put(VALIDATION_REPORT, new ValidationReport2());
		context.put(REPORT, new ActionReport2());
		ValidationReporter validationReporter = ValidationReporter.Factory.getInstance();
		validationReporter.addItemToValidationReport(context, "Neptune-", "Checkpoint", 1, "W");
		ValidationReport2 validationReport = (ValidationReport2) context.get(VALIDATION_REPORT);
		
		Assert.assertNotNull(validationReport.findCheckPointReportByName("Neptune-Checkpoint-1"), "checkpoint must exist in validation report");
	}
	
	@Test(groups = { "CheckPoint" }, description = "verify existing checkpoint" ,priority=102 )
	public void verifyCheckPointAdd() throws Exception {
		Context context = new Context();
		context.put(VALIDATION_REPORT, new ValidationReport2());
		context.put(REPORT, new ActionReport2());
		ValidationReporter validationReporter = ValidationReporter.Factory.getInstance();
		validationReporter.addItemToValidationReport(context, "Neptune-", "Checkpoint", 1, "W");
		
		validationReporter.addItemToValidationReport(context, "Neptune-", "Checkpoint", 1, "E");
		ValidationReport2 validationReport = (ValidationReport2) context.get(VALIDATION_REPORT);
		
		Assert.assertEquals(validationReport.findCheckPointReportByName("Neptune-Checkpoint-1").getSeverity(), CheckPointReport.SEVERITY.WARNING, "Checkpoint severity must be WARNING");
	}
	
	@Test(groups = { "CheckPointError" }, description = "checkpoint error update" ,priority=103 )
	public void verifyExistingCheckPointError() throws Exception {
		Context context = new Context();
		context.put(VALIDATION_REPORT, new ValidationReport2());
		context.put(REPORT, new ActionReport2());
		
		ValidationReporter validationReporter = ValidationReporter.Factory.getInstance();
		validationReporter.addItemToValidationReport(context, "Neptune-", "Checkpoint", 1, "W");
		DataLocation location = new DataLocation();
		location.setColumnNumber(1);
		location.setFilename("filename");
		location.setObjectId("1234");
		location.setLineNumber(3);
		ValidationReport2 validationReport = (ValidationReport2) context.get(VALIDATION_REPORT);
		validationReporter.addCheckPointReportError(context, "Neptune-Checkpoint-1", location, "test");
		Assert.assertNotNull(validationReport.findCheckPointReportByName("Neptune-Checkpoint-1"), "checkpoint must exist in validation report");
		Assert.assertEquals(validationReport.findCheckPointReportByName("Neptune-Checkpoint-1").getCheckPointErrorsKeys().size(), 1, "Checkpoint error key must be present in list");
	}
	
	@Test(groups = { "JsonGeneration" }, description = "Json generated" ,priority=104 )
	public void verifyJsonGeneration() throws Exception {
		Context context = new Context();
		context.put(VALIDATION_REPORT, new ValidationReport2());
		context.put(REPORT, new ActionReport2());
		
		ValidationReporter validationReporter = ValidationReporter.Factory.getInstance();
		validationReporter.addItemToValidationReport(context, "Neptune-", "Checkpoint", 1, "W");
		DataLocation location = new DataLocation();
		location.setColumnNumber(1);
		location.setFilename("filename");
		location.setObjectId("1234");
		location.setLineNumber(3);
		ValidationReport2 validationReport = (ValidationReport2) context.get(VALIDATION_REPORT);
		
		JSONObject array = new JSONObject("{\"validation_report\":{\"result\":\"NO_VALIDATION\",\"tests\":[{\"error_id\":\"neptune_checkpoint_1\",\"source\":{\"file\":{\"filename\":\"filename\",\"line_number\":3,\"column_number\":1},\"objectid\":\"1234\",\"label\":\"\"},\"error_value\":\"test\"}]}}");
		validationReporter.addCheckPointReportError(context, "Neptune-Checkpoint-1", location, "test");
		Assert.assertEquals(validationReport.toJson().toString(), array.toString(), "Invalid validation report json");
	
	}
}
