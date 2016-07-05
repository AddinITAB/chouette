package mobi.chouette.exchange;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.naming.InitialContext;

import lombok.extern.log4j.Log4j;
import mobi.chouette.common.Color;
import mobi.chouette.common.Constant;
import mobi.chouette.common.Context;
import mobi.chouette.common.JobData;
import mobi.chouette.common.chain.Command;
import mobi.chouette.common.chain.CommandFactory;
import mobi.chouette.exchange.parameters.AbstractParameter;
import mobi.chouette.exchange.report.ProgressionReport;
import mobi.chouette.exchange.report.Report;
import mobi.chouette.exchange.report.ReportConstant;
import mobi.chouette.exchange.report.StepProgression;
import mobi.chouette.exchange.report.StepProgression.STEP;

@Log4j
public class ProgressionCommand implements Command, Constant, ReportConstant {

	public static final String COMMAND = "ProgressionCommand";

	public void initialize(Context context, int stepCount) {
		ProgressionReport report = (ProgressionReport) context.get(REPORT);
		report.getProgression().setCurrentStep(STEP.INITIALISATION.ordinal() + 1);
		report.getProgression().getSteps().get(STEP.INITIALISATION.ordinal()).setTotal(stepCount);
		saveReport(context);
		saveMainValidationReport(context);
	}

	public void start(Context context, int stepCount) {
		ProgressionReport report = (ProgressionReport) context.get(REPORT);
		report.getProgression().setCurrentStep(STEP.PROCESSING.ordinal() + 1);
		report.getProgression().getSteps().get(STEP.PROCESSING.ordinal()).setTotal(stepCount);
		saveReport(context);
		saveMainValidationReport(context);
	}

	public void terminate(Context context, int stepCount) {
		ProgressionReport report = (ProgressionReport) context.get(REPORT);
		report.getProgression().setCurrentStep(STEP.FINALISATION.ordinal() + 1);
		report.getProgression().getSteps().get(STEP.FINALISATION.ordinal()).setTotal(stepCount);
		saveReport(context);
	}

	public void dispose(Context context) {
		saveReport(context);
			saveMainValidationReport(context);
	}

	public static void saveReport(Context context) {
		if (context.containsKey("testng"))
			return;
		Report report = (Report) context.get(REPORT);
		JobData jobData = (JobData) context.get(JOB_DATA);
		Path path = Paths.get(jobData.getPathName(), REPORT_FILE);
		// pseudo pretty print
		try {
			PrintStream stream = new PrintStream(path.toFile(),"UTF-8");
			report.print(stream);
			stream.close();
//			String data = report.toJson().toString(2);
//			FileUtils.writeStringToFile(path.toFile(), data, "UTF-8");
		} catch (Exception e) {
			log.error("failed to save report", e);
		}

	}

	/**
	 * @param context
	 */
	public static void saveMainValidationReport(Context context) {
		if (context.containsKey("testng"))
			return;
		Report report = (Report) context.get(VALIDATION_REPORT);
		// ne pas sauver un rapport null ou vide
		if (report == null || report.isEmpty())
			return;
		JobData jobData = (JobData) context.get(JOB_DATA);
		Path path = Paths.get(jobData.getPathName(), VALIDATION_FILE);

		try {
			PrintStream stream = new PrintStream(path.toFile(),"UTF-8");
			report.print(stream);
			stream.close();
//			String data = report.toJson().toString(2);
//			FileUtils.writeStringToFile(path.toFile(), data, "UTF-8");
		} catch (Exception e) {
			log.error("failed to save validation report", e);
		}

	}


	@Override
	public boolean execute(Context context) throws Exception {
		boolean result = SUCCESS;

//		if (context.containsKey(VALIDATION_REPORT)) {
//			saveMainValidationReport(context);
//		}
		ProgressionReport report = (ProgressionReport) context.get(REPORT);
		StepProgression step = report.getProgression().getSteps().get(report.getProgression().getCurrentStep() - 1);
		step.setRealized(step.getRealized() + 1);
		saveReport(context);
		if (context.containsKey(CANCEL_ASKED) || Thread.currentThread().isInterrupted()) {
			log.info("Command cancelled");
			throw new CommandCancelledException(COMMAND_CANCELLED);
		}
		AbstractParameter params = (AbstractParameter) context.get(CONFIGURATION);
		if (params.isTest()) {
			log.info(Color.YELLOW + "Mode test on: waiting 10 s" + Color.NORMAL);
			Thread.sleep(10000);
		}
		return result;
	}

	public static class DefaultCommandFactory extends CommandFactory {

		private ProgressionCommand instance;

		@Override
		protected Command create(InitialContext context) throws IOException {
			if (instance == null) {
				instance = new ProgressionCommand();
			}
			return instance;
		}
	}

	static {
		CommandFactory.factories.put(ProgressionCommand.class.getName(), new DefaultCommandFactory());
	}
}
