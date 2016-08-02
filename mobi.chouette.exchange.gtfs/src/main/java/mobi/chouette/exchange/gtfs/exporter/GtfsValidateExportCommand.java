package mobi.chouette.exchange.gtfs.exporter;

import java.io.File;
import java.io.IOException;

import javax.naming.InitialContext;

import lombok.extern.log4j.Log4j;
import mobi.chouette.common.Color;
import mobi.chouette.common.Context;
import mobi.chouette.common.JobData;
import mobi.chouette.common.chain.Command;
import mobi.chouette.common.chain.CommandFactory;
import mobi.chouette.exchange.ProgressionCommand;
import mobi.chouette.exchange.gtfs.Constant;
import mobi.chouette.exchange.gtfs.importer.GtfsDisposeImportCommand;
import mobi.chouette.exchange.gtfs.importer.GtfsImportParameters;
import mobi.chouette.exchange.gtfs.importer.GtfsInitImportCommand;
import mobi.chouette.exchange.gtfs.importer.GtfsValidationCommand;
import mobi.chouette.exchange.gtfs.importer.GtfsValidationRulesCommand;
import mobi.chouette.exchange.report.ActionReport;

import org.apache.commons.io.FileUtils;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

@Log4j
public class GtfsValidateExportCommand implements Command, Constant {

	public static final String COMMAND = "GtfsValidateExportCommand";

	@Override
	public boolean execute(Context context) throws Exception {
		boolean result = ERROR;
		log.info("GtfsValidateExportCommand 1");
		Monitor monitor = MonitorFactory.start(COMMAND);
		log.info("GtfsValidateExportCommand 2");
		try {
			log.info("GtfsValidateExportCommand 3");
			// create specific context
			Context validateContext = new Context();
			validateContext.putAll(context);
			// build parameter
			GtfsImportParameters parameters = new GtfsImportParameters();
			GtfsExportParameters configuration = (GtfsExportParameters) context.get(CONFIGURATION);
			parameters.setOrganisationName(configuration.getOrganisationName());
			parameters.setUserName(configuration.getUserName());
			parameters.setName(configuration.getName());
			parameters.setNoSave(true);
			parameters.setReferentialName(configuration.getReferentialName());
			parameters.setReferencesType(configuration.getReferencesType());
			parameters.setObjectIdPrefix(configuration.getObjectIdPrefix());
			validateContext.put(CONFIGURATION, parameters);
			validateContext.put(REPORT, new ActionReport());
			log.info("GtfsValidateExportCommand 4");
			// rename output folder to input folder
			JobData jobData = (JobData) context.get(JOB_DATA);
			String path = jobData.getPathName();
			File output = new File(path, OUTPUT);
			File input = new File(path, INPUT);
			log.info("GtfsValidateExportCommand 5");
			if (!output.renameTo(input))
				log.error("rename failed");
			output = new File(path, OUTPUT);
			log.info("GtfsValidateExportCommand 6");
			// run gtfs validation preparation
			InitialContext initialContext = (InitialContext) context.get(INITIAL_CONTEXT);
			log.info("GtfsValidateExportCommand 7");
			try {
				Command c = CommandFactory.create(initialContext, GtfsValidationRulesCommand.class.getName());
				c.execute(validateContext);
				log.info("GtfsValidateExportCommand 8");
				// run gtfs init command
				c = CommandFactory.create(initialContext, GtfsInitImportCommand.class.getName());
				c.execute(validateContext);
				log.info("GtfsValidateExportCommand 9");
				// run gtfs validate command
				c = CommandFactory.create(initialContext, GtfsValidationCommand.class.getName());
				log.info("GtfsValidateExportCommand 10");
				c.execute(validateContext);
			} catch (Exception ex) {
				log.error("problem in validation" + ex);
			} finally {
				// rename folder to output before dispose
				input.renameTo(output);
				log.info("GtfsValidateExportCommand 11");
				// terminate validation
				Command c = CommandFactory.create(initialContext, GtfsDisposeImportCommand.class.getName());
				log.info("GtfsValidateExportCommand 12");
				c.execute(validateContext);
			}
			// save report in folder
			context.put(MAIN_VALIDATION_REPORT, validateContext.get(MAIN_VALIDATION_REPORT));
			log.info("GtfsValidateExportCommand 13");
			result = SUCCESS;

		} catch (Exception e) {
			log.error(e, e);
			throw e;
		} finally {
			log.info(Color.MAGENTA + monitor.stop() + Color.NORMAL);
		}

		return result;
	}

	public static class DefaultCommandFactory extends CommandFactory {

		@Override
		protected Command create(InitialContext context) throws IOException {
			Command result = new GtfsValidateExportCommand();
			return result;
		}
	}

	static {
		CommandFactory.factories.put(GtfsValidateExportCommand.class.getName(), new DefaultCommandFactory());
	}

}
