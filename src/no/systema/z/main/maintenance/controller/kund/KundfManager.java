package no.systema.z.main.maintenance.controller.kund;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.*;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import no.systema.main.model.SystemaWebUser;
import no.systema.main.service.UrlCgiProxyService;
import no.systema.z.main.maintenance.model.jsonjackson.dbtable.JsonMaintMainCundfRecord;
import no.systema.z.main.maintenance.model.jsonjackson.dbtable.JsonMaintMainKundfContainer;
import no.systema.z.main.maintenance.model.jsonjackson.dbtable.JsonMaintMainKundfRecord;
import no.systema.z.main.maintenance.model.jsonjackson.dbtable.JsonMaintMainKundfRecordL1AuxList;
import no.systema.z.main.maintenance.service.MaintMainCustomerL1Service;
import no.systema.z.main.maintenance.url.store.ExternalUrlDataStore;
import no.systema.z.main.maintenance.util.MainMaintenanceConstants;

@Service
public class KundfManager {
	private static final Logger logger = LogManager.getLogger(KundfManager.class.getName());
	private UrlCgiProxyService urlCgiProxyService;
	private MaintMainCustomerL1Service maintMainCustomerL1Service;
	public final static String L1_EXISTS_VISIBLE = "V";
	public final static String L1_EXISTS_INVISIBLE = "J";
	public final static String TRANSACTION_UPDATE = "doUpdate";
	public final static String TRANSACTION_CREATE = "doCreate";
	
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
		if(appUser.getKundeL1()!=null && KundfManager.L1_EXISTS_VISIBLE.equals(appUser.getKundeL1())){
			String kundeNr = "";
			if(record.getKundnr()!=null){ kundeNr = record.getKundnr(); }
			JsonMaintMainKundfContainer containerL1 = fetchRecordL1(appUser, kundeNr); 
			
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
    		//logger.info(container);
    		if(container!=null){
    			if(StringUtils.isEmpty(container.getErrMsg())){
    				retval = container;
    				//logger.warn(container.getL1_KgrupList().size());
    			}
    			/*DEBUG
    			for( JsonMaintMainKundfRecordL1AuxList record: container.getL1_KhenvList()){
    				logger.warn(record.getKode());
    				logger.warn(record.getTekst());
		    	}
    			for( JsonMaintMainKundfRecordL1AuxList record: container.getL1_KgrupList()){
    				logger.warn("Grp:" + record.getKode());
    				logger.warn("Grp:" + record.getTekst());
		    	}
		    	*/
    		}
    	}		
		return retval;
	}
	/**
	 * Used in every failed validation before "SAVE"
	 *  
	 * @param model
	 * @param request
	 * @param recordToValidate
	 * @param appUser
	 */
	public void rescueL1Params(Map model, HttpServletRequest request, JsonMaintMainCundfRecord recordToValidate, SystemaWebUser appUser){
		JsonMaintMainKundfContainer containerL1 = new JsonMaintMainKundfContainer();
		
		//Override values with those from user input
		containerL1.setKundnr(request.getParameter("l1_Kundnr"));
		containerL1.setL1_Kundnr(request.getParameter("l1_Kundnr"));
		containerL1.setL1_Feks(request.getParameter("l1_Feks"));
		containerL1.setL1_Head(request.getParameter("l1_Head"));
		containerL1.setL1_Khenv(request.getParameter("l1_Khenv"));
		containerL1.setL1_KundGr(request.getParameter("l1_KundGr"));
		containerL1.setL1_Kundnr(request.getParameter("l1_Kundnr"));
		containerL1.setL1_Kutdr(request.getParameter("l1_Kutdr"));
		containerL1.setL1_Pgebyr(request.getParameter("l1_Pgebyr"));
		containerL1.setL1_Pkod(request.getParameter("l1_Pkod"));
		containerL1.setL1_Rkod(request.getParameter("l1_Rkod"));
		//
		containerL1.setL1_DaoAar(request.getParameter("l1_DaoAar"));
		containerL1.setL1_DaoMnd(request.getParameter("l1_DaoMnd"));
		containerL1.setL1_DaoDag(request.getParameter("l1_DaoDag"));
		//L1 -FETCH dropdowns
		String KUNDNR_EMPTY = "";
		JsonMaintMainKundfContainer containerTmp = this.fetchRecordL1(appUser, KUNDNR_EMPTY);
		containerL1.setL1_KgrupList(containerTmp.getL1_KgrupList());
		containerL1.setL1_KhenvList(containerTmp.getL1_KhenvList());
		
		model.put(MainMaintenanceConstants.DOMAIN_CONTAINER_L1, containerL1);
		
	}
}
