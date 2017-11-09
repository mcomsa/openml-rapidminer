package org.openml.experiment.operators;

import org.openml.apiconnector.settings.Config;

import com.rapidminer.operator.Operator;

/*
 * Wrapper for the Upload operator in RapidMiner
 */
public class Upload {
	
	private Operator operator;
	
	public Upload(Operator operator, Config config) throws Exception{
		
		if(operator == null){
			throw new Exception("The operator reference given to Download is null");
		}
		this.operator = operator;
		setUrl(config.getServer());
		setApiKey(config.getApiKey());
	}

	public void setUrl(String url){
		
		operator.setParameter("Url", url);
	}
	
	public String getUrl(){
		
		try{
			return operator.getParameter("Url");
		}
		catch(Exception e){
			return null;
		}
	}
	
	public void setApiKey(String apiKey){
		
		operator.setParameter("Api key", apiKey);
	}
	
	public String getApiKey(){
		
		try{
			return operator.getParameter("Api key");
		}
		catch(Exception e){
			return null;
		}
	}
	
	public Operator getOperator(){
		
		return operator;
	}
	
	public void setOperator(Operator operator){
		
		this.operator = operator;
	}
}