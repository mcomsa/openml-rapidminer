package org.openml.rapidminer;

import java.util.List;

import org.openml.apiconnector.algorithms.Conversion;
import org.openml.apiconnector.algorithms.TaskInformation;
import org.openml.apiconnector.xml.DataSetDescription;
import org.openml.apiconnector.xml.Task;
import org.openml.apiconnector.xml.Task.Input.Data_set;
import org.openml.rapidminer.models.OpenmlConfigurable;
import org.openml.rapidminer.models.OpenmlConfigurator;
import org.openml.rapidminer.models.OpenmlTask;
import org.openml.rapidminer.utils.OpenmlConnectorJson;

import com.rapidminer.MacroHandler;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.parameter.UndefinedParameterError;
import com.rapidminer.tools.config.ConfigurationException;
import com.rapidminer.tools.config.ConfigurationManager;
import com.rapidminer.tools.config.ParameterTypeConfigurable;
public class DownloadOpenmlTask extends Operator {

	private OpenmlConnectorJson openmlConnector;
	
	private static String PARAMETER_TASKID = "Task_id";
	private static String PARAMETER_CONFIG = "OpenML_Connection";
	
	private OutputPort taskOutput = getOutputPorts().createPort("task");

	public DownloadOpenmlTask(OperatorDescription description) {
		super(description);
		
		getTransformer().addGenerationRule(taskOutput, OpenmlTask.class);
	}
	
	@Override
	public void doWork() throws OperatorException {
		int task_id = -1;
		OpenmlConfigurable config;
		String apikey;
		String url;
		MacroHandler mHandler = this.getProcess().getMacroHandler();
		// if a macro is set for apikey and url, get the values from there
		if(mHandler.getMacro("apikey")!= null && mHandler.getMacro("url")!= null)
		{
			apikey = mHandler.getMacro("apikey");
			url = mHandler.getMacro("url");
		}
		else if(this.isParameterSet("Url") && this.isParameterSet("Api key"))
		{
			url = this.getParameter("Url");
			apikey = this.getParameter("Api key");
		}
		else
		{
			try 
			{
				config = (OpenmlConfigurable) ConfigurationManager.getInstance().lookup(
				OpenmlConfigurator.TYPE_ID, getParameterAsString(PARAMETER_CONFIG), 
				getProcess().getRepositoryAccessor());
				apikey = config.getApiKey();
				url = config.getUrl();
			} 
			catch (ConfigurationException e) 
			{
				throw new UserError(this, e, "openml.configuration_read");
			}
		}
		// if there is a macro set for task_id, get the value from the macro
		if(mHandler.getMacro("task_id")!= null)
		{
			task_id = Integer.parseInt(mHandler.getMacro("task_id"));
		}
		else
		{
			try 
			{ 
				task_id  = getParameterAsInt(PARAMETER_TASKID);      
			} 
			catch(UndefinedParameterError eupe ) 
			{ 
				Conversion.log("Error","Parameter","Can't find parameter: " + PARAMETER_TASKID );
			}
		}
		openmlConnector = new OpenmlConnectorJson(url, apikey, true);
		Task task = null;
		DataSetDescription dsd = null;
		try 
		{
			task = openmlConnector.taskGet(task_id);
			Data_set sourceData = TaskInformation.getSourceData(task);
			dsd = openmlConnector.dataGet(sourceData.getData_set_id());
		} 
		catch (Exception e) {
			throw new OperatorException("Error retrieving task from Openml: " + e.getMessage());
		}

		OpenmlTask openmltask = new OpenmlTask(task,dsd);
		taskOutput.deliver(openmltask);
	}
	
	@Override
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		types.add(new ParameterTypeConfigurable(PARAMETER_CONFIG, "Choose an OpenML Connection", OpenmlConfigurator.TYPE_ID));
		types.add(new ParameterTypeInt(PARAMETER_TASKID, "The Openml Task that needs to be executed", 1, Integer.MAX_VALUE, false));
		return types;
	}
	
}