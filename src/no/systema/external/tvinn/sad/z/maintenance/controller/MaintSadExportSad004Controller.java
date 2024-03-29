package no.systema.external.tvinn.sad.z.maintenance.controller;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.SerializationUtils;
import org.slf4j.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.ModelAndView;

import no.systema.main.model.SystemaWebUser;
import no.systema.main.service.UrlCgiProxyService;
import no.systema.main.util.AppConstants;
import no.systema.main.util.JsonDebugger;
import no.systema.z.main.maintenance.util.MainMaintenanceConstants;
import no.systema.external.tvinn.sad.z.maintenance.util.TvinnCodeDropDownMgr;
import no.systema.z.main.maintenance.mapper.url.request.UrlRequestParameterMapper;
import no.systema.external.tvinn.sad.z.maintenance.model.JsonMaintSadSadlContainer;
import no.systema.external.tvinn.sad.z.maintenance.model.JsonMaintSadSadlRecord;
import no.systema.external.tvinn.sad.z.maintenance.service.MaintSadSadlService;
import no.systema.external.tvinn.sad.z.maintenance.service.MaintSadExportKodts6Service;
import no.systema.external.tvinn.sad.z.maintenance.url.store.TvinnSadMaintenanceUrlDataStore;
import no.systema.external.tvinn.sad.z.maintenance.validator.MaintSadExportSad004Validator;
import no.systema.z.main.maintenance.controller.kund.MainMaintenanceCundfVareExportNoController;


/**
 *  TVINN Maintenance Import Sad004 Controller - Kundern vareregister
 * 
 * @author Fredrik Möller
 * @date Sept 6, 2016
 * 
 */

@Controller
@SessionAttributes(AppConstants.SYSTEMA_WEB_USER_KEY)
@Scope("session")
public class MaintSadExportSad004Controller {
	private static final JsonDebugger jsonDebugger = new JsonDebugger();
	private static final Logger logger = LoggerFactory.getLogger(MaintSadExportSad004Controller.class.getName());
	private ModelAndView loginView = new ModelAndView("login");
	private UrlRequestParameterMapper urlRequestParameterMapper = new UrlRequestParameterMapper();
	private TvinnCodeDropDownMgr codeDropDownMgr = new TvinnCodeDropDownMgr();
	private boolean hasError;


	/**
	 * This method support calls from other views that defined inside, e.g. from Bore. See {@link  MainMaintenanceCundfVareExportNoController}
	 * 
	 * 
	 * @param session
	 * @param request
	 * @param String nr the kundnr if called from Bcore.
	 * @return a populated {@link ModelAndView}. Be aware of viewname.
	 */
	@RequestMapping(value="tvinnsadmaintenanceexport_sad004.do", method={RequestMethod.GET, RequestMethod.POST})
	public ModelAndView doSadMaintImportList(HttpSession session, HttpServletRequest request, String nr){
		ModelAndView successView = new ModelAndView("tvinnsadmaintenanceexport_sad004");
		SystemaWebUser appUser = (SystemaWebUser)session.getAttribute(AppConstants.SYSTEMA_WEB_USER_KEY);
		String dbTable = request.getParameter("id");
		String kundnr = null;
		
		if (request.getParameter("searchKundnr") != null) {
			kundnr = request.getParameter("searchKundnr");
		} else if (nr != null) { //From Bcore
			kundnr = nr;
		}
		
		Map model = new HashMap();
		if(appUser==null){
			return this.loginView;
		}else{
			//get table
	    	List<JsonMaintSadSadlRecord> list = new ArrayList();
	    	if( (kundnr!=null && !"".equals(kundnr)) ){
	    		list = this.fetchList(appUser.getUser(), null, kundnr); //to accomplish wild card search
	    	}
	    	//set domain objets
			this.populateDropDowns(model, appUser.getUser());
	    	model.put("dbTable", dbTable);
	    	model.put("kundnr", kundnr);
	    	model.put(MainMaintenanceConstants.DOMAIN_LIST, list);
	    	successView.addObject(MainMaintenanceConstants.DOMAIN_MODEL , model);
			
	    	return successView;
		}
	}
	
	
	@RequestMapping(value="tvinnsadmaintenanceexport_sad004_edit.do", method={RequestMethod.GET, RequestMethod.POST})
	public ModelAndView doSadMaintImportEdit(@ModelAttribute ("record") JsonMaintSadSadlRecord recordToValidate, BindingResult bindingResult, HttpSession session, HttpServletRequest request){
		ModelAndView successView = new ModelAndView("tvinnsadmaintenanceexport_sad004");
		SystemaWebUser appUser = (SystemaWebUser)session.getAttribute(AppConstants.SYSTEMA_WEB_USER_KEY);
		
		String dbTable = request.getParameter("id");
		String updateId = request.getParameter("updateId");
		String action = request.getParameter("action");
		
		Map model = new HashMap();
		if(appUser==null){
			return this.loginView;
		}else{
			//Save for forward backup
			String originalFreeText = recordToValidate.getSltxt();
			//Move on
			MaintSadExportSad004Validator validator = new MaintSadExportSad004Validator();
			//SOBEK JsonMaintSadSadlRecord recordToValidateOrg = null; 
			if(MainMaintenanceConstants.ACTION_DELETE.equals(action)){
				validator.validateDelete(recordToValidate, bindingResult);
			}else{
				//adjust values
				//SOBEK recordToValidateOrg=SerializationUtils.clone(recordToValidate); //keep due to advanced adjustments between UI and DB
				this.adjustSomeRecordValues(recordToValidate);
				validator.validate(recordToValidate, bindingResult);
			}
			if(bindingResult.hasErrors()){
				//ERRORS
				logger.info("[BINDING ERROR Validation] Record does not validate)");
				model.put("dbTable", dbTable);
				if(updateId!=null && !"".equals(updateId)){
					//meaning bounced in an Update and not a Create new
					model.put("updateId", updateId);
				}
				//SOBEK model.put(MainMaintenanceConstants.DOMAIN_RECORD, recordToValidateOrg);
				model.put(MainMaintenanceConstants.DOMAIN_RECORD, recordToValidate);
				
			}else{
				
				//------------
				//UPDATE table
				//------------
				StringBuffer errMsg = new StringBuffer();
				int dmlRetval = 0;
				//UPDATE
				if (MainMaintenanceConstants.ACTION_UPDATE.equals(action) ){
					if(updateId!=null && !"".equals(updateId)){
						//update
						logger.info(MainMaintenanceConstants.ACTION_UPDATE);
						dmlRetval = this.updateRecord(appUser.getUser(), recordToValidate, MainMaintenanceConstants.MODE_UPDATE, errMsg);
						
					//CREATE
					}else{
						//create new
						logger.info(MainMaintenanceConstants.ACTION_CREATE);
						dmlRetval = this.updateRecord(appUser.getUser(), recordToValidate, MainMaintenanceConstants.MODE_ADD, errMsg);
					}
				}else if(MainMaintenanceConstants.ACTION_DELETE.equals(action) ){
					//delete
					logger.info(MainMaintenanceConstants.ACTION_DELETE);
					dmlRetval = this.updateRecord(appUser.getUser(), recordToValidate, MainMaintenanceConstants.MODE_DELETE, errMsg);
				}
				//check for Update errors
				if( dmlRetval < 0){
					logger.info("[ERROR on UPDATE] Record is not valid");
					logger.info("errMsg.toString():"+errMsg.toString());
					model.put("dbTable", dbTable);
					model.put("kundnr", recordToValidate.getSlknr());
					model.put("updateId", updateId);
					//adjust back the free text (prior to the appends: r31, pref and mf
					recordToValidate.setSltxt(originalFreeText);
					model.put(MainMaintenanceConstants.ASPECT_ERROR_MESSAGE, errMsg.toString());
					hasError= true;
					//SOBEK model.put(MainMaintenanceConstants.DOMAIN_RECORD, recordToValidateOrg);
					model.put(MainMaintenanceConstants.DOMAIN_RECORD, recordToValidate);
				}
				
			}
			//------------
			//FETCH table
			//------------
			if(MainMaintenanceConstants.ACTION_DELETE.equals(action) ){
				recordToValidate.setSlalfa(null);
			}
			List<JsonMaintSadSadlRecord> list = this.fetchList(appUser.getUser(), recordToValidate.getSlalfa(), recordToValidate.getSlknr());
	    	//set domain objets
	    	model.put("dbTable", dbTable);
	    	model.put("kundnr", recordToValidate.getSlknr());
			model.put(MainMaintenanceConstants.DOMAIN_LIST, list);
			this.populateDropDowns(model, appUser.getUser()); 
			successView.addObject(MainMaintenanceConstants.DOMAIN_MODEL , model);
		
	    	return successView;
		}
	}
	
	
	/**
	 * Humble trick of exposing if crud gone bad, viewed outside this controller.
	 * 
	 * @return false if record is invalid in service/db.
	 */
	public boolean hasError() {
		return hasError;
	}
 	
	private void populateDropDowns(Map model, String applicationUser){
		codeDropDownMgr.populatePrefCodesHtmlDropDownsSadExport(this.urlCgiProxyService, this.maintSadExportKodts6Service, model, applicationUser); 
		codeDropDownMgr.populateR31HtmlDropDownsSadExport( model);
		codeDropDownMgr.populateMfHtlDropDownSadExport(model);
	}
		
	/**
	 * 
	 * @param recordToValidate
	 */
	private void adjustSomeRecordValues(JsonMaintSadSadlRecord recordToValidate){
		final String ZERO = "0";
		//-----------------
		//Decimal amounts
		//-----------------
		if(recordToValidate.getSlvekt()!=null && !"".equals(recordToValidate.getSlvekt())){
			String tmp = recordToValidate.getSlvekt().replace(",", ".");
			recordToValidate.setSlvekt(tmp);
		}else{
			recordToValidate.setSlvekt(ZERO);
		}
		if(recordToValidate.getSltanr()!=null && !"".equals(recordToValidate.getSltanr())){
			String tmp = recordToValidate.getSltanr().replace(",", ".");
			recordToValidate.setSltanr(tmp);
		}else{
			recordToValidate.setSltanr(ZERO);
		}
		if(recordToValidate.getSlsats()!=null && !"".equals(recordToValidate.getSlsats())){
			String tmp = recordToValidate.getSlsats().replace(",", ".");
			recordToValidate.setSlsats(tmp);
		}else{
			recordToValidate.setSlsats(ZERO);
		}
		//-----------------------------
		//(2) START Adjust free text.
		//-----------------------------
		if(recordToValidate!=null){
			//Position 21,22,23 are reserved and must be appended
			String SPACE = " ";
			int PURE_FTX_UPPER_LIMIT_POSITION = 20;
			StringBuffer str = new StringBuffer(recordToValidate.getSltxt());
			int len = str.length();
			for (int x=len+1;x<=PURE_FTX_UPPER_LIMIT_POSITION;x++){
				str.append(SPACE);
			}
			//now append r31,pref and mf into postion 21,22,23 respectively and in order
			if(recordToValidate.getR31()!=null && !"".equals(recordToValidate.getR31())){
				str.append(recordToValidate.getR31());
			}else{
				str.append(SPACE);
			}
			if(recordToValidate.getPref()!=null && !"".equals(recordToValidate.getPref())){
				str.append(recordToValidate.getPref());
			}else{
				str.append(SPACE);
			}
			if(recordToValidate.getMf()!=null && !"".equals(recordToValidate.getMf())){
				str.append(recordToValidate.getMf());
			}else{
				str.append(SPACE);
			}
			//Update sltxt
			recordToValidate.setSltxt(str.toString());
			//END - adjust free text
		}
	}
	
	
	private List<JsonMaintSadSadlRecord> fetchList(String applicationUser, String id, String levenr){
		String BASE_URL = TvinnSadMaintenanceUrlDataStore.TVINN_SAD_MAINTENANCE_EXPORT_BASE_SAD004R_GET_LIST_URL;
		StringBuffer urlRequestParams = new StringBuffer();
		urlRequestParams.append("user="+ applicationUser);
		if(levenr!=null && !"".equals(levenr)){
			urlRequestParams.append("&slknr=" + levenr);
			if(id!=null){
				urlRequestParams.append("&slalfa=" + id);
			}
			
		}else{
			//no further search. Just return an empty list
			return new ArrayList();
		}
		
		logger.info(Calendar.getInstance().getTime() + " CGI-start timestamp");
    	logger.info("URL: " + jsonDebugger.getBASE_URL_NoHostName(BASE_URL));
    	logger.info("URL PARAMS: " + urlRequestParams);
    	String jsonPayload = this.urlCgiProxyService.getJsonContent(BASE_URL, urlRequestParams.toString());
    	//extract
    	List<JsonMaintSadSadlRecord> list = new ArrayList();
    	if(jsonPayload!=null){
			//lists
    		JsonMaintSadSadlContainer container = this.maintSadSadlService.getList(jsonPayload);
	        if(container!=null){
	        	list = (List)container.getList();
/*	        	for(JsonMaintSadSadlRecord record : list){
	        		//logger.info("VARENR:" + record.getSlalfa());
	        	}
*/	        }
    	}
    	return list;
    	
	}
	
	/**
	 * UPDATE/CREATE/DELETE
	 * 
	 * @param applicationUser
	 * @param record
	 * @param mode
	 * @return
	 */
	
	private int updateRecord(String applicationUser, JsonMaintSadSadlRecord record, String mode, StringBuffer errMsg){
		int retval = 0;
		String BASE_URL = TvinnSadMaintenanceUrlDataStore.TVINN_SAD_MAINTENANCE_EXPORT_BASE_SAD004R_DML_UPDATE_URL;
		String urlRequestParamsKeys = "user=" + applicationUser + "&mode=" + mode;
		String urlRequestParams = this.urlRequestParameterMapper.getUrlParameterValidString((record));
		//put the final valid param. string
		urlRequestParams = urlRequestParamsKeys + urlRequestParams;
		
		logger.info(Calendar.getInstance().getTime() + " CGI-start timestamp");
    	logger.warn("URL: " + jsonDebugger.getBASE_URL_NoHostName(BASE_URL));
    	logger.warn("URL PARAMS: " + urlRequestParams);
    	String jsonPayload = this.urlCgiProxyService.getJsonContent(BASE_URL, urlRequestParams);
    	
    	//extract
    	if(jsonPayload!=null){
			//lists
    		JsonMaintSadSadlContainer container = this.maintSadSadlService.doUpdate(jsonPayload);
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
	
	
	
	//SERVICES
	@Qualifier ("urlCgiProxyService")
	private UrlCgiProxyService urlCgiProxyService;
	@Autowired
	@Required
	public void setUrlCgiProxyService (UrlCgiProxyService value){ this.urlCgiProxyService = value; }
	public UrlCgiProxyService getUrlCgiProxyService(){ return this.urlCgiProxyService; }
	
	
	@Qualifier ("maintSadSadlService")
	private MaintSadSadlService maintSadSadlService;
	@Autowired
	@Required
	public void setMaintSadSadlService (MaintSadSadlService value){ this.maintSadSadlService = value; }
	public MaintSadSadlService getMaintSadSadlService(){ return this.maintSadSadlService; }
	
	@Qualifier ("maintSadExportKodts6Service")
	private MaintSadExportKodts6Service maintSadExportKodts6Service;
	@Autowired
	@Required
	public void setMaintSadExportKodts6Service (MaintSadExportKodts6Service value){ this.maintSadExportKodts6Service = value; }
	public MaintSadExportKodts6Service getMaintSadExportKodts6Service(){ return this.maintSadExportKodts6Service; }

}

