package no.systema.z.main.maintenance.controller.avd.skat;

import java.util.*;

import org.slf4j.*;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Required;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import no.systema.main.service.UrlCgiProxyService;

//application imports
import no.systema.main.model.SystemaWebUser;
import no.systema.main.util.AppConstants;
import no.systema.main.util.JsonDebugger;
import no.systema.main.util.DateTimeManager;

//models
import no.systema.z.main.maintenance.url.store.MaintenanceMainUrlDataStore;
import no.systema.z.main.maintenance.util.MainMaintenanceConstants;
import no.systema.z.main.maintenance.service.skat.MaintMainDknstdService;
import no.systema.z.main.maintenance.service.MaintMainEdiiService;
import no.systema.z.main.maintenance.model.jsonjackson.dbtable.skat.JsonMaintMainDknstdContainer;
import no.systema.z.main.maintenance.model.jsonjackson.dbtable.skat.JsonMaintMainDknstdRecord;
import no.systema.z.main.maintenance.mapper.url.request.UrlRequestParameterMapper;
import no.systema.z.main.maintenance.validator.skat.MaintMainDknstdValidator;
import no.systema.z.main.maintenance.util.manager.CodeDropDownMgrSkat;
import no.systema.z.main.maintenance.service.MaintMainKodtaService;

import no.systema.external.skat.z.maintenance.service.MaintDktvkService;
import no.systema.external.skat.z.maintenance.service.MaintDkxkodfService;


/**
 * Gateway to the Main Maintenance Application
 * 
 * @author oscardelatorre
 * @date Apr 24, 2017
 * 
 */

@Controller
public class MainMaintenanceAvdSkatNctsImportDknstdController {
	private static final Logger logger = LoggerFactory.getLogger(MainMaintenanceAvdSkatNctsImportDknstdController.class.getName());
	private ModelAndView loginView = new ModelAndView("login");
	private static final JsonDebugger jsonDebugger = new JsonDebugger();
	private UrlRequestParameterMapper urlRequestParameterMapper = new UrlRequestParameterMapper();
	private CodeDropDownMgrSkat codeDropDownMgr = new CodeDropDownMgrSkat();
	private DateTimeManager dateTimeMgr = new DateTimeManager();
	/**
	 * 
	 * @param user
	 * @param result
	 * @param request
	 * @return
	 * 
	 */
	@RequestMapping(value="mainmaintenanceavdskatnctsimport_dkx053r.do", method={RequestMethod.GET, RequestMethod.POST })
	public ModelAndView mainmaintenanceavdskatnctsexport_dkx003r (HttpSession session, HttpServletRequest request){
		ModelAndView successView = new ModelAndView("mainmaintenanceavdskatnctsimport_dkx053r");
		SystemaWebUser appUser = (SystemaWebUser)session.getAttribute(AppConstants.SYSTEMA_WEB_USER_KEY);
		String id = request.getParameter("id");  //TRUST or TRUST_FHV, for correct selection
		Map model = new HashMap();
		if(appUser==null){
			return this.loginView;
		}else{
			logger.info("Inside method: mainmaintenanceavdskatnctsimport_dkx053r");
			logger.info("appUser user:" + appUser.getUser());
			logger.info("appUser lang:" + appUser.getUsrLang());
			logger.info("appUser userAS400:" + appUser.getUserAS400());
			
			//Get list
	 		List list = this.fetchList(appUser.getUser(), id);
			model.put("list", list);
			model.put("id", id);
			successView.addObject(MainMaintenanceConstants.DOMAIN_MODEL , model);
			
			logger.info("Host via HttpServletRequest.getHeader('Host'): " + request.getHeader("Host"));
		    
			return successView;
			
		}
	}
	/**
	 * 
	 * @param session
	 * @param request
	 * @return
	 */
	
	@RequestMapping(value="mainmaintenanceavdskatnctsimport_dkx053r_edit.do", method={RequestMethod.GET, RequestMethod.POST })
	public ModelAndView mainmaintenanceavdskatnctsexport_dkx003_edit(@ModelAttribute ("record") JsonMaintMainDknstdRecord recordToValidate, BindingResult bindingResult, HttpSession session, HttpServletRequest request){
		ModelAndView successView = new ModelAndView("mainmaintenanceavdskatnctsimport_dkx053r_edit");
		SystemaWebUser appUser = (SystemaWebUser)session.getAttribute(AppConstants.SYSTEMA_WEB_USER_KEY);
		Map model = new HashMap();
		String avd = request.getParameter("avd");
		String avdnavn = request.getParameter("avdnavn");
		String action = request.getParameter("action");
		String updateId = request.getParameter("updateId");
		String id = request.getParameter("id");
		
		
		if(appUser==null){
			return this.loginView;
		}else{
			logger.info("Inside method: mainmaintenanceavdskatnctsimport_dkx053r_edit");
			logger.info("appUser user:" + appUser.getUser());
			logger.info("appUser lang:" + appUser.getUsrLang());
			logger.info("appUser userAS400:" + appUser.getUserAS400());
			boolean isValidOnUpdate = true;
			//--------------
			//UPDATE record
			//--------------
			if (MainMaintenanceConstants.ACTION_UPDATE.equals(action)){
				
				avd = recordToValidate.getTiavd();
				//Adjust
				this.adjustSomeRecordValues(recordToValidate);
				//Validate
				MaintMainDknstdValidator validator = new MaintMainDknstdValidator();
				validator.validate(recordToValidate, bindingResult);
				if(bindingResult.hasErrors()){
					//ERRORS
					logger.info("[ERROR Validation] Record does not validate)");
					//model.put("dbTable", dbTable);
					model.put(MainMaintenanceConstants.DOMAIN_RECORD, recordToValidate);
					isValidOnUpdate = false;
				}else{
					
					//Update table
					StringBuffer errMsg = new StringBuffer();
					int dmlRetval = 0;
					if(updateId!=null && !"".equals(updateId)){
						//update
						logger.info(MainMaintenanceConstants.MODE_UPDATE);
						dmlRetval = this.updateRecord(appUser.getUser(), recordToValidate, MainMaintenanceConstants.MODE_UPDATE, errMsg);
						if(dmlRetval >= 0){
							//TODO more ?
						}
					}else{
						//create new
						logger.info(MainMaintenanceConstants.MODE_ADD);
						dmlRetval = this.updateRecord(appUser.getUser(), recordToValidate, MainMaintenanceConstants.MODE_ADD, errMsg);
						if(dmlRetval >= 0){
							//TODO more ?
						}
							
					}
					
					//check for Update errors
					if( dmlRetval < 0){
						logger.info("[ERROR Validation] Record does not validate)");
						model.put(MainMaintenanceConstants.ASPECT_ERROR_MESSAGE, errMsg.toString());
						model.put(MainMaintenanceConstants.DOMAIN_RECORD, recordToValidate);
						isValidOnUpdate = false;
					}else{
						//post successful update operations
						updateId = recordToValidate.getTiavd();
						
					}
					
				}
				model.put(MainMaintenanceConstants.DOMAIN_RECORD, recordToValidate);
				
				
			//DELETE	
			}else if(MainMaintenanceConstants.ACTION_DELETE.equals(action)){
				StringBuffer errMsg = new StringBuffer();
				int dmlRetval = 0;
				logger.info(MainMaintenanceConstants.MODE_DELETE);
				dmlRetval = this.updateRecord(appUser.getUser(), recordToValidate, MainMaintenanceConstants.MODE_DELETE, errMsg);
				
				
				//check for Update errors
				if( dmlRetval < 0){
					logger.info("[ERROR Validation] Record does not validate)");
					model.put(MainMaintenanceConstants.ASPECT_ERROR_MESSAGE, errMsg.toString());
					model.put(MainMaintenanceConstants.DOMAIN_RECORD, recordToValidate);
				}else{
					//post successful update operations
					successView = new ModelAndView("redirect:mainmaintenanceavdskatnctsimport_dkx053r.do?id=DKNSTD");
					
				}
			}
			//-------------
			//Fetch record
			//-------------
			if(isValidOnUpdate && (avd!=null && !"".equals(avd)) ){
				JsonMaintMainDknstdRecord record = this.fetchRecord(appUser.getUser(), avd);
				model.put(MainMaintenanceConstants.DOMAIN_RECORD, record);
			}
			
			//populate model
			if(action==null || "".equals(action)){
				action = "doUpdate";
			}
			this.populateDropDowns(model, appUser.getUser());
			model.put("action", action);
			model.put("avd", avd);
			model.put("avdnavn", avdnavn);
			model.put("updateId", updateId);
			model.put("id", id);
			
			successView.addObject(MainMaintenanceConstants.DOMAIN_MODEL , model);
			
			logger.info("Host via HttpServletRequest.getHeader('Host'): " + request.getHeader("Host"));
		    
			return successView;
			
		}
	}
	
	
	/**
	 * 
	 * @param applicationUser
	 * @return
	 */
	private List<JsonMaintMainDknstdRecord> fetchList(String applicationUser, String id){
		
		String BASE_URL = MaintenanceMainUrlDataStore.MAINTENANCE_MAIN_BASE_DKX053R_GET_LIST_URL;
		String urlRequestParams = "user=" + applicationUser + "&id="+id;
		logger.info(Calendar.getInstance().getTime() + " CGI-start timestamp");
    	logger.info("URL: " + jsonDebugger.getBASE_URL_NoHostName(BASE_URL));
    	logger.info("URL PARAMS: " + urlRequestParams);
    	String jsonPayload = this.urlCgiProxyService.getJsonContent(BASE_URL, urlRequestParams);
    	//DEBUG
    	this.jsonDebugger.debugJsonPayload(jsonPayload, 1000);
    	//extract
    	List<JsonMaintMainDknstdRecord> list = new ArrayList();
    	if(jsonPayload!=null){
			//lists
    		JsonMaintMainDknstdContainer container = this.maintMainDknstdService.getList(jsonPayload);
	        if(container!=null){
	        	list = (List)container.getList();
	        }
    	}
    	return list;
    	
	}
	
	/**
	 * 
	 * @param applicationUser
	 * @param avd
	 * @return
	 */
	private JsonMaintMainDknstdRecord fetchRecord(String applicationUser, String avd){
		JsonMaintMainDknstdRecord record = new JsonMaintMainDknstdRecord();
    	
		String BASE_URL = MaintenanceMainUrlDataStore.MAINTENANCE_MAIN_BASE_DKX053R_GET_LIST_URL;
		String urlRequestParams = "user=" + applicationUser + "&tiavd=" + avd;
		
		//logger.info(Calendar.getInstance().getTime() + " CGI-start timestamp");
    	logger.info("URL: " + jsonDebugger.getBASE_URL_NoHostName(BASE_URL));
    	logger.info("URL PARAMS: " + urlRequestParams);
    	String jsonPayload = this.urlCgiProxyService.getJsonContent(BASE_URL, urlRequestParams);
    	//DEBUG
    	//this.jsonDebugger.debugJsonPayload(jsonPayload, 1000);
    	//extract
    	List<JsonMaintMainDknstdRecord> list = new ArrayList();
    	
    	if(jsonPayload!=null){
			//lists
    		JsonMaintMainDknstdContainer container = this.maintMainDknstdService.getList(jsonPayload);
	        if(container!=null){
	        	list = (List)container.getList();
	        	for(JsonMaintMainDknstdRecord tmp : list){
	        		record = tmp;
	        	}
	        }
    	}
    	return record;
    	
	}
	
	
	
	/**
	 * 
	 * @param applicationUser
	 * @param record
	 * @param mode
	 * @param errMsg
	 * @return
	 */
	private int updateRecord(String applicationUser, JsonMaintMainDknstdRecord record, String mode, StringBuffer errMsg){
		int retval = 0;
		
		String BASE_URL = MaintenanceMainUrlDataStore.MAINTENANCE_MAIN_BASE_DKX053R_DML_UPDATE_URL;
		String urlRequestParamsKeys = "user=" + applicationUser + "&mode=" + mode;
		String urlRequestParams = this.urlRequestParameterMapper.getUrlParameterValidString((record));
		//put the final valid param. string
		urlRequestParams = urlRequestParamsKeys + urlRequestParams;
		
		//logger.info(Calendar.getInstance().getTime() + " CGI-start timestamp");
    	logger.info("URL: " + jsonDebugger.getBASE_URL_NoHostName(BASE_URL));
    	logger.info("URL PARAMS: " + urlRequestParams);
    	String jsonPayload = this.urlCgiProxyService.getJsonContent(BASE_URL, urlRequestParams);
    	
    	//extract
    	if(jsonPayload!=null){
			//lists
    		JsonMaintMainDknstdContainer container = this.maintMainDknstdService.doUpdate(jsonPayload);
	        if(container!=null){
	        	if(container.getErrMsg()!=null && !"".equals(container.getErrMsg())){
	        		if(container.getErrMsg().toUpperCase().startsWith("ERROR")){
	        			errMsg.append(container.getErrMsg());
	        			retval = MainMaintenanceConstants.ERROR_CODE;
	        		}
	        	}
	        }
    	}    	
    	return retval;
	}
	
	
	/**
	 * 
	 * @param recordToValidate
	 * @param sikkerhedChildRecord
	 */
	private void adjustSomeRecordValues(JsonMaintMainDknstdRecord recordToValidate){
		//----------
		//Dates
		//----------
		/*N/A for DK
		if(recordToValidate.getThddt()!=null && !"".equals(recordToValidate.getThddt()) ){
			if(recordToValidate.getThddt().length()== 6){
				recordToValidate.setThddt(dateFormatter.convertToDate_ISO(recordToValidate.getThddt()));
			}
		}
		
		if(sikkerhedChildRecord.getThdta()!=null && !"".equals(sikkerhedChildRecord.getThdta()) ){
			if(sikkerhedChildRecord.getThdta().length()== 6){
				sikkerhedChildRecord.setThdta(dateFormatter.convertToDate_ISO(sikkerhedChildRecord.getThdta()));
			}
		}
		*/
	}
	
	/**
	 * 
	 * @param model
	 * @param applicationUser
	 */
	private void populateDropDowns(Map model, String applicationUser){
		this.codeDropDownMgr.populateCurrencyCodesHtmlDropDownsSkat(this.urlCgiProxyService, this.maintDktvkService, model, applicationUser);
		this.codeDropDownMgr.populateAvdListHtmlDropDownsSkat(this.urlCgiProxyService, this.maintMainKodtaService, model, applicationUser, "snealist");
		//Code lists in NCTS domain
		this.codeDropDownMgr.populateGeneralCodesHtmlDropDownsNcts(this.urlCgiProxyService, this.maintDkxkodfService, model, applicationUser, MainMaintenanceConstants.CODE_NCTS_SIKKERHET_096_SPES_OMSTAND);
		this.codeDropDownMgr.populateGeneralCodesHtmlDropDownsNcts(this.urlCgiProxyService, this.maintDkxkodfService, model, applicationUser, MainMaintenanceConstants.CODE_NCTS_SIKKERHET_116_TRANSP_KOST_BETAL_MATE);
		this.codeDropDownMgr.populateGeneralCodesHtmlDropDownsNcts(this.urlCgiProxyService, this.maintDkxkodfService, model, applicationUser, MainMaintenanceConstants.CODE_SKAT_NCTS_EXPORT_108_TRANSPORTMATE);
		this.codeDropDownMgr.populateGeneralCodesHtmlDropDownsNcts(this.urlCgiProxyService, this.maintDkxkodfService, model, applicationUser, MainMaintenanceConstants.CODE_NCTS_DEKLARASJONS_TYPE);
	}
	
	
	
	
	//Wired - SERVICES
	@Qualifier ("urlCgiProxyService")
	private UrlCgiProxyService urlCgiProxyService;
	@Autowired
	@Required
	public void setUrlCgiProxyService (UrlCgiProxyService value){ this.urlCgiProxyService = value; }
	public UrlCgiProxyService getUrlCgiProxyService(){ return this.urlCgiProxyService; }
	
	
	@Qualifier ("maintMainDknstdService")
	private MaintMainDknstdService maintMainDknstdService;
	@Autowired
	@Required
	public void setMaintMainDknstdService (MaintMainDknstdService value){ this.maintMainDknstdService = value; }
	public MaintMainDknstdService getMaintMainDknstdService(){ return this.maintMainDknstdService; }
	
	
	@Qualifier ("maintDktvkService")
	private MaintDktvkService maintDktvkService;
	@Autowired
	@Required
	public void setMaintDktvkService (MaintDktvkService value){ this.maintDktvkService = value; }
	public MaintDktvkService getMaintDktvkService(){ return this.maintDktvkService; }
	
	
	@Qualifier ("maintMainKodtaService")
	private MaintMainKodtaService maintMainKodtaService;
	@Autowired
	@Required
	public void setMaintMainKodtaService (MaintMainKodtaService value){ this.maintMainKodtaService = value; }
	public MaintMainKodtaService getMaintMainKodtaService(){ return this.maintMainKodtaService; }
	
	
	@Qualifier ("maintMainEdiiService")
	private MaintMainEdiiService maintMainEdiiService;
	@Autowired
	@Required
	public void setMaintMainEdiiService (MaintMainEdiiService value){ this.maintMainEdiiService = value; }
	public MaintMainEdiiService getMaintMainEdiiService(){ return this.maintMainEdiiService; }
	
	
	@Qualifier ("maintDkxkodfService")
	private MaintDkxkodfService maintDkxkodfService;
	@Autowired
	@Required
	public void setMaintDkxkodfService (MaintDkxkodfService value){ this.maintDkxkodfService = value; }
	public MaintDkxkodfService getMaintDkxkodfService(){ return this.maintDkxkodfService; }
	

	
}

