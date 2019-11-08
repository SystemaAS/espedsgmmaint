package no.systema.z.main.maintenance.controller.kund;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import no.systema.main.model.SystemaWebUser;
import no.systema.main.service.UrlCgiProxyService;
import no.systema.z.main.maintenance.model.jsonjackson.dbtable.JsonMaintMainCundfRecord;
import no.systema.z.main.maintenance.model.jsonjackson.dbtable.JsonMaintMainKundfContainer;
import no.systema.z.main.maintenance.model.jsonjackson.dbtable.JsonMaintMainKundfRecord;
import no.systema.z.main.maintenance.service.MaintMainCustomerL1Service;
import no.systema.z.main.maintenance.url.store.ExternalUrlDataStore;
import no.systema.z.main.maintenance.util.MainMaintenanceConstants;

@Service
public class KundfManager {
	private static final Logger logger = Logger.getLogger(KundfManager.class.getName());
	private UrlCgiProxyService urlCgiProxyService;
	private MaintMainCustomerL1Service maintMainCustomerL1Service;
	
	public KundfManager( UrlCgiProxyService urlCgiProxyService, MaintMainCustomerL1Service maintMainCustomerL1Service){
		this.urlCgiProxyService = urlCgiProxyService;
		this.maintMainCustomerL1Service = maintMainCustomerL1Service;
		
	}
	/**
	 * 
	 * @param model
	 * @param appUser
	 * @param record
	 */
	public void fetchL1(Map model, SystemaWebUser appUser, JsonMaintMainCundfRecord record){
		//L1 -FETCH
		if(appUser.getKundeL1()!=null && "V".equals(appUser.getKundeL1())){
			JsonMaintMainKundfContainer containerL1 = fetchRecordL1(appUser, record.getKundnr()); 
			JsonMaintMainKundfRecord recordL1 = new JsonMaintMainKundfRecord();
			if(containerL1!=null && org.apache.commons.lang3.StringUtils.isEmpty(containerL1.getKundnr())){
				//copy the parent record in order to present default values for "create new" L1
				ModelMapper modelMapper = new ModelMapper();
				recordL1 = modelMapper.map(record, JsonMaintMainKundfRecord.class);
				//map special attributes
				recordL1.setLand(record.getSyland());
				recordL1.setKundnr("");						
			}
			
			//model.put(MainMaintenanceConstants.DOMAIN_RECORD_L1, recordL1);
			logger.info("kundnr:" + containerL1.getKundnr() + " head:" + containerL1.getL1_Head());
			model.put(MainMaintenanceConstants.DOMAIN_CONTAINER_L1, containerL1);
			
			//L1 -END FETCH
		}
	}
	
	/**
	 * 
	 * @param appUser
	 * @param kundnr
	 * @return
	 */
	private JsonMaintMainKundfContainer fetchRecordL1(SystemaWebUser appUser, String kundnr){
		JsonMaintMainKundfContainer retval = new JsonMaintMainKundfContainer();
		
		String firma = appUser.getCompanyCode();
		if(StringUtils.isEmpty(firma)){
			firma = appUser.getFallbackCompanyCode();
		}
		final String BASE_URL = ExternalUrlDataStore.L1_BASE_FETCH_SPECIFIC_CUSTOMER_URL;
		//add URL-parameters
		StringBuffer urlRequestParams = new StringBuffer();
		urlRequestParams.append("user=" + appUser.getUser() + "&mode=G&firma=" + firma + "&kundnr=" + kundnr);
		
		//session.setAttribute(TransportDispConstants.ACTIVE_URL_RPG_TRANSPORT_DISP, BASE_URL + "==>params: " + urlRequestParams.toString()); 
    	logger.info(Calendar.getInstance().getTime() + " CGI-start timestamp");
    	logger.info("URL: " + BASE_URL);
    	logger.info("URL PARAMS: " + urlRequestParams);
    	String jsonPayload = this.urlCgiProxyService.getJsonContent(BASE_URL, urlRequestParams.toString());
    	//Debug --> 
    	logger.info(jsonPayload);
    	logger.info(Calendar.getInstance().getTime() +  " CGI-end timestamp");
    	if(jsonPayload!=null){
    		JsonMaintMainKundfContainer container = this.maintMainCustomerL1Service.getContainer(jsonPayload);
    		logger.info(container);
    		if(container!=null && StringUtils.isNotEmpty(container.getKundnr())){
    			if(StringUtils.isEmpty(container.getErrMsg())){
    				retval = container;
    			}
    			/*
    			for( JsonMaintMainKundfRecord customerRecord: container.getList()){
    					record = customerRecord;
		    	}*/
    		}
    	}		
		return retval;
	}
}
