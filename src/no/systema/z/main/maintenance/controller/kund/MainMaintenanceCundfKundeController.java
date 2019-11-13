package no.systema.z.main.maintenance.controller.kund;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.log4j.Logger;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import no.systema.jservices.common.dao.VadrDao;
import no.systema.jservices.common.util.StringUtils;
import no.systema.main.model.SystemaWebUser;
import no.systema.main.service.UrlCgiProxyService;
import no.systema.main.util.AppConstants;
import no.systema.main.util.JsonDebugger;
import no.systema.z.main.maintenance.mapper.url.request.UrlRequestParameterMapper;
import no.systema.z.main.maintenance.model.jsonjackson.dbtable.JsonMaintMainCundcContainer;
import no.systema.z.main.maintenance.model.jsonjackson.dbtable.JsonMaintMainCundcRecord;
import no.systema.z.main.maintenance.model.jsonjackson.dbtable.JsonMaintMainCundfContainer;
import no.systema.z.main.maintenance.model.jsonjackson.dbtable.JsonMaintMainCundfRecord;
import no.systema.z.main.maintenance.model.jsonjackson.dbtable.JsonMaintMainKundfContainer;
import no.systema.z.main.maintenance.model.jsonjackson.dbtable.JsonMaintMainKundfRecord;
import no.systema.z.main.maintenance.service.MaintMainCundcService;
import no.systema.z.main.maintenance.service.MaintMainCundfService;
import no.systema.z.main.maintenance.service.MaintMainCustomerL1Service;
import no.systema.z.main.maintenance.url.store.ExternalUrlDataStore;
import no.systema.z.main.maintenance.url.store.MaintenanceMainUrlDataStore;
import no.systema.z.main.maintenance.util.MainMaintenanceConstants;
import no.systema.z.main.maintenance.util.manager.CodeDropDownMgr;
import no.systema.z.main.maintenance.validator.MaintMainCundfValidator;


/**
 * Gateway for Kunde detaljer
 * 
 * 
 * @author Fredrik Möller
 * @date Okt 26, 2016
 * 
 * 	
 */

@Controller
public class MainMaintenanceCundfKundeController {
	private static final Logger logger = Logger.getLogger(MainMaintenanceCundfKundeController.class.getName());
	private ModelAndView loginView = new ModelAndView("login");
	private static final JsonDebugger jsonDebugger = new JsonDebugger();
	private UrlRequestParameterMapper urlRequestParameterMapper = new UrlRequestParameterMapper();
	private CodeDropDownMgr codeDropDownMgr = new CodeDropDownMgr();
	private KundfManager kundfManager;
	
	@Autowired
	VkundControllerUtil vkundControllerUtil;	

	@RequestMapping(value="mainmaintenancecundf_kunde_edit.do", method={RequestMethod.GET, RequestMethod.POST })
	public ModelAndView mainmaintenancecundf_vkund_edit(@ModelAttribute ("record") JsonMaintMainCundfRecord recordToValidate, BindingResult bindingResult, HttpSession session, HttpServletRequest request){
		ModelAndView successView = new ModelAndView("mainmaintenancecundf_kunde_edit");
		SystemaWebUser appUser = (SystemaWebUser)session.getAttribute(AppConstants.SYSTEMA_WEB_USER_KEY);
		Map model = new HashMap();
		String action = request.getParameter("action");
		StringBuffer errMsg = new StringBuffer();
		JsonMaintMainCundfRecord savedRecord = null;
		JsonMaintMainCundfRecord record = null;
		this.kundfManager = new KundfManager(this.urlCgiProxyService, this.maintMainCustomerL1Service);
		
		logger.info("recordToValidate="+ReflectionToStringBuilder.toString(recordToValidate));
		logger.info("action="+action);
		
		try {
			
	
		if (appUser == null) {
			return this.loginView;
		} else {
			KundeSessionParams kundeSessionParams = null;
			kundeSessionParams = (KundeSessionParams)session.getAttribute(MainMaintenanceConstants.KUNDE_SESSION_PARAMS);
			
			if (MainMaintenanceConstants.ACTION_CREATE.equals(action)) {  //New

				theBetBetFix(appUser, recordToValidate, action);
				specialRules(recordToValidate);
				
				// Validate
				MaintMainCundfValidator validator = new MaintMainCundfValidator();
				validator.validate(recordToValidate, bindingResult);
				if (bindingResult.hasErrors()) {
					logger.info("[ERROR Validation] Record does not validate)");
					model.put(MainMaintenanceConstants.DOMAIN_RECORD, recordToValidate);
					action = MainMaintenanceConstants.ACTION_CREATE;
					//Rescue L1 fields
					if(this.isValidL1(appUser, recordToValidate, KundfManager.TRANSACTION_CREATE)){
						this.kundfManager.rescueL1Params(model, request, recordToValidate, appUser);
					}
					 
				} else {
					//===========================================
					//Now update the L1 record (when applicable)
					//===========================================
					if(this.isValidL1(appUser, recordToValidate, KundfManager.TRANSACTION_CREATE)){
						JsonMaintMainKundfContainer savedL1Record = this.updateL1(model, appUser, request, recordToValidate);
						if(savedL1Record!=null){
							recordToValidate.setKundnr(savedL1Record.getKundnr());
							logger.info("handover to recordToValidate OK");
							//proceed with cundf create
							//DELETE L1 is not possible according to JOVO. This means that if Cundf- fails then there will be an L1-"lost in translation"
							//Don't blame me ...
							action = this.updateCundfAndFetchKundf(model, appUser, kundeSessionParams, recordToValidate, record, savedRecord, errMsg);
							
							if(errMsg!=null && !"".equals(errMsg.toString())){
								recordToValidate.setKundnr("");
								savedL1Record.setKundnr("");
								model.put(MainMaintenanceConstants.DOMAIN_CONTAINER_L1, savedL1Record);
							}
						}	
					}else{
						action = this.updateCundfAndFetchKundf(model, appUser, kundeSessionParams, recordToValidate, record, savedRecord, errMsg);
					}					
				}

			} else if (MainMaintenanceConstants.ACTION_UPDATE.equals(action)) { //Update

				adjustRecordToValidate(recordToValidate, kundeSessionParams);
				theBetBetFix(appUser, recordToValidate, action);
				specialRules(recordToValidate);
				
				
				MaintMainCundfValidator validator = new MaintMainCundfValidator();
				validator.validate(recordToValidate, bindingResult);
				if (bindingResult.hasErrors()) {
					logger.error("[ERROR Validation] Record does not validate)");
					model.put(MainMaintenanceConstants.DOMAIN_RECORD, recordToValidate);
					//Rescue L1 fields
					if(this.isValidL1(appUser, recordToValidate,KundfManager.TRANSACTION_UPDATE)){
						this.kundfManager.rescueL1Params(model, request, recordToValidate, appUser);
					}
					
				} else {
					
					savedRecord = updateRecord(appUser, recordToValidate, MainMaintenanceConstants.MODE_UPDATE, errMsg);
					if (savedRecord == null) {            
						logger.error("[ERROR Update] Record could not be updated, errMsg="+errMsg.toString());
						model.put(MainMaintenanceConstants.ASPECT_ERROR_MESSAGE, errMsg.toString());
						model.put(MainMaintenanceConstants.DOMAIN_RECORD, recordToValidate);
						//Rescue L1 fields
						if(this.isValidL1(appUser, recordToValidate, KundfManager.TRANSACTION_UPDATE)){
							this.kundfManager.rescueL1Params(model, request, recordToValidate, appUser);
						}
					} else if (StringUtils.hasValue(errMsg.toString())){
						logger.error("[ERROR Update] Record could not be updated, errMsg="+errMsg.toString());
						model.put(MainMaintenanceConstants.ASPECT_ERROR_MESSAGE, errMsg.toString());
						record = this.fetchRecord(appUser.getUser(), kundeSessionParams.getKundnr(), kundeSessionParams.getFirma());
						model.put(MainMaintenanceConstants.DOMAIN_RECORD, record);
						//Rescue L1 fields
						if(this.isValidL1(appUser, recordToValidate, KundfManager.TRANSACTION_UPDATE)){
							this.kundfManager.rescueL1Params(model, request, recordToValidate, appUser);
						}
					} else {
						record = this.fetchRecord(appUser.getUser(), kundeSessionParams.getKundnr(), kundeSessionParams.getFirma());
						model.put(MainMaintenanceConstants.DOMAIN_RECORD, record);
						//===========================================
						//Now update the L1 record (when applicable)
						//===========================================
						this.updateL1(model, appUser, request, record);
						this.kundfManager.fetchL1(model, appUser, record);
							
					}
				}
			} else { // Fetch
				record = fetchRecord(appUser.getUser(), kundeSessionParams.getKundnr(), kundeSessionParams.getFirma());
				model.put(MainMaintenanceConstants.DOMAIN_RECORD, record);
				//L1 -FETCH
				this.kundfManager.fetchL1(model, appUser, record);

				action = MainMaintenanceConstants.ACTION_UPDATE;
				
			}

			populateDropDowns(model, appUser.getUser());
			
			model.put("action", action);
			model.put("kundnr", kundeSessionParams.getKundnr());
			model.put("firma", kundeSessionParams.getFirma());
			model.put("invoiceCustomerAllowed", vkundControllerUtil.getInvoiceCustomerAllowed(appUser));
			if (kundeSessionParams.getKundnr() != null) {
				model.put("isAdressCustomer", vkundControllerUtil.isAdressCustomer(appUser, new Integer(kundeSessionParams.getKundnr())));
			}
			model.put("orgNrMulti", vkundControllerUtil.orgNrMulti(recordToValidate.getSyrg(), appUser));
			model.put("hasSypogeAndNO", vkundControllerUtil.hasSypogeAndNO(recordToValidate.getSypoge(), recordToValidate.getSyland() , appUser));
			if(record != null) {
				model.put("hasVareAddresseNr1", vkundControllerUtil.hasVadrValues(record));
			}

			
			successView.addObject(MainMaintenanceConstants.DOMAIN_MODEL, model);
			successView.addObject("tab_knavn_display", VkundControllerUtil.getTrimmedKnav(kundeSessionParams.getKnavn()));
			
			return successView;		

		}
		} catch (Exception e) {
			logger.error("ERROR:", e);
			String errorMessage = "Teknisk feil. Kontakt helpdesk. Error:"+e;
			model.put(MainMaintenanceConstants.DOMAIN_RECORD, recordToValidate);
			model.put(MainMaintenanceConstants.ASPECT_ERROR_MESSAGE, errorMessage);
			//Rescue L1 fields
			if(this.isValidL1(appUser, recordToValidate, null)){
				this.kundfManager.rescueL1Params(model, request, recordToValidate, appUser);
			}
			successView.addObject(MainMaintenanceConstants.DOMAIN_MODEL, model);
			successView.addObject("tab_knavn_display", VkundControllerUtil.getTrimmedKnav(recordToValidate.getKnavn()));
			
			return successView;		

		}

	}
	/**
	 * 
	 * @param model
	 * @param appUser
	 * @param kundeSessionParams
	 * @param recordToValidate
	 * @param record
	 * @param savedRecord
	 * @param errMsg
	 * @return
	 */
	private String updateCundfAndFetchKundf(Map model, SystemaWebUser appUser, KundeSessionParams kundeSessionParams, JsonMaintMainCundfRecord recordToValidate, 
						JsonMaintMainCundfRecord record, JsonMaintMainCundfRecord savedRecord, StringBuffer errMsg){
		
		String action = "";
		savedRecord = updateRecord(appUser, recordToValidate, MainMaintenanceConstants.MODE_ADD, errMsg);
		if (savedRecord == null) {
			logger.info("[ERROR Validation] Record does not validate)");
			model.put(MainMaintenanceConstants.ASPECT_ERROR_MESSAGE, errMsg.toString());
			model.put(MainMaintenanceConstants.DOMAIN_RECORD, recordToValidate);
			//L1 -FETCH
			this.kundfManager.fetchL1(model, appUser, recordToValidate);
			action = MainMaintenanceConstants.ACTION_CREATE;
			
		} else {
			kundeSessionParams.setKundnr(savedRecord.getKundnr());
			kundeSessionParams.setFirma(savedRecord.getFirma());
			kundeSessionParams.setSonavn(savedRecord.getSonavn());
			kundeSessionParams.setKnavn(savedRecord.getKnavn());

			record = fetchRecord(appUser.getUser(), kundeSessionParams.getKundnr(), kundeSessionParams.getFirma());
			//L1 -FETCH
			this.kundfManager.fetchL1(model, appUser, record);
			model.put(MainMaintenanceConstants.DOMAIN_RECORD, record);
			action = MainMaintenanceConstants.ACTION_UPDATE;

		}
		return action;
	}
	/**
	 * Checks if the customer (at a firm level) has L1 as Master Economy System
	 * @param appUser
	 * @param kundeType
	 * @return
	 */
	private boolean isValidL1(SystemaWebUser appUser, JsonMaintMainCundfRecord recordToValidate, String transactionType){
		boolean retval = false;
		//===========================================
		//Now update the L1 record (when applicable)
		//===========================================
		String FAKTURAKUNDE = "F"; //adressekunder("A") skall inte ingå här
		String ACTIVE_KUNDE = "A";
		
		//L1_EXISTS = visible in GUI for end user interaction
		//L1_EXISTS_INVISIBLE = no GUI for end user but implicit CRUD for all CUNDF-transactions
		if(KundfManager.L1_EXISTS_VISIBLE.equals(appUser.getKundeL1()) || KundfManager.L1_EXISTS_INVISIBLE.equals(appUser.getKundeL1())){
			if(KundfManager.TRANSACTION_CREATE.equals(transactionType)){
				if(FAKTURAKUNDE.equals(recordToValidate.getKundetype()) && ACTIVE_KUNDE.equals(recordToValidate.getAktkod())){
					retval = true;
				}
			}else if(KundfManager.TRANSACTION_UPDATE.equals(transactionType)){
				if(ACTIVE_KUNDE.equals(recordToValidate.getAktkod())){
					retval = true;
				}
			}else{
				if(transactionType!=null){
					//could be a "doFetch"
					retval = true;
				}
			}
		}
		
		return retval;
	}
	
	
	/**
	 * 
	 * @param model
	 * @param appUser
	 * @param request
	 * @param recordToValidate
	 * @return
	 */
	private JsonMaintMainKundfContainer updateL1(Map model, SystemaWebUser appUser, HttpServletRequest request, JsonMaintMainCundfRecord record){
		JsonMaintMainKundfContainer retval = null;
		
		if(appUser.getKundeL1()!=null){
			
			if( KundfManager.L1_EXISTS_VISIBLE.equals(appUser.getKundeL1()) || KundfManager.L1_EXISTS_INVISIBLE.equals(appUser.getKundeL1())){
				JsonMaintMainKundfRecord params = this.setL1Params(request, record, appUser);
				logger.info("############### - L1PARAMS:" + params);
				JsonMaintMainKundfContainer savedL1Record = updateRecordL1(appUser, params);
				if(savedL1Record!=null && StringUtils.hasValue(savedL1Record.getKundnr())){
					logger.info("############### - CREATE NEW/UPDATE L1 = SUCCESS --> L1-kundnr:" + savedL1Record.getKundnr());
					retval = savedL1Record;
				}else{
					logger.info("############### - ERROR SEVERE on update L1:");
				}
			}
		}
		return retval;
	}
	
	
	/**
	 * transfer object before update
	 * @param request
	 * @param recordCundf
	 * @return
	 */
	private JsonMaintMainKundfRecord setL1Params(HttpServletRequest request, JsonMaintMainCundfRecord recordCundf, SystemaWebUser appUser){
		JsonMaintMainKundfRecord recordL1 = new JsonMaintMainKundfRecord();
		ModelMapper modelMapper = new ModelMapper();
		recordL1 = modelMapper.map(recordCundf, JsonMaintMainKundfRecord.class);
		if(!StringUtils.hasValue(recordL1.getFirma())){
			String firma = appUser.getCompanyCode();
			if(!StringUtils.hasValue(firma)){
				firma = appUser.getFallbackCompanyCode();
			}
			recordL1.setFirma(firma);
		}
		recordL1.setLand(recordCundf.getSyland());
		recordL1.setKundeL1(appUser.getKundeL1());
		//delta fields found only in L1 GUI
		if(KundfManager.L1_EXISTS_VISIBLE.equals(appUser.getKundeL1())){
			recordL1.setKundnr(request.getParameter("l1_Kundnr"));
			recordL1.setHead(request.getParameter("l1_Head"));
			recordL1.setKundgr(request.getParameter("l1_KundGr"));
			recordL1.setFeks(request.getParameter("l1_Feks"));
			recordL1.setPkod(request.getParameter("l1_Pkod"));
			recordL1.setPgebyr(request.getParameter("l1_Pgebyr"));
			recordL1.setKutdr(request.getParameter("l1_Kutdr"));
			recordL1.setKhenv(request.getParameter("l1_Khenv"));
			recordL1.setDaoaar(request.getParameter("l1_DaoAar"));
			recordL1.setDaomnd(request.getParameter("l1_DaoMnd"));
			recordL1.setDaodag(request.getParameter("l1_DaoDag"));
			
		}
		return recordL1;
	}
	
	/**
	 * 
	 * @param user
	 * @param recordL1ToValidate
	 * @return
	 */
	private JsonMaintMainKundfContainer updateRecordL1(SystemaWebUser appUser, JsonMaintMainKundfRecord recordL1){
		JsonMaintMainKundfContainer record = new JsonMaintMainKundfContainer();
		
		final String BASE_URL = ExternalUrlDataStore.L1_BASE_UPDATE_SPECIFIC_CUSTOMER_URL;
		//add URL-parameters
		StringBuffer urlRequestParamsKeys = new StringBuffer();
		urlRequestParamsKeys.append("user=" + appUser.getUser());
		String urlRequestParams = this.urlRequestParameterMapper.getUrlParameterValidString((recordL1));
		//put the final valid param. string
		urlRequestParams = urlRequestParamsKeys + urlRequestParams;
		
		//session.setAttribute(TransportDispConstants.ACTIVE_URL_RPG_TRANSPORT_DISP, BASE_URL + "==>params: " + urlRequestParams.toString()); 
    	logger.info(Calendar.getInstance().getTime() + " CGI-start timestamp");
    	logger.info("URL: " + BASE_URL);
    	logger.info("URL PARAMS: " + urlRequestParams);
    	String jsonPayload = this.urlCgiProxyService.getJsonContent(BASE_URL, urlRequestParams.toString());
    	//Debug --> 
    	logger.debug(jsonDebugger.debugJsonPayloadWithLog4J(jsonPayload));
    	logger.info(Calendar.getInstance().getTime() +  " CGI-end timestamp");
    	if(jsonPayload!=null){
    		JsonMaintMainKundfContainer container = this.maintMainCustomerL1Service.getContainer(jsonPayload);
    		if(container!=null && !StringUtils.hasValue(container.getErrMsg())){ 
    			record = container;
    			
    		}
    	}		
		return record;
	}
	
	/**
	 * Check orgnr in ELMA. 
	 * 
	 * @param applicationUser
	 * @param syrg
	 * @return J if exist, else N.
	 */
	@RequestMapping(value = "existInElma.do", method = { RequestMethod.GET, RequestMethod.POST })
	@ResponseBody
	public String existInElma(@RequestParam String applicationUser, @RequestParam String syrg) {
		return vkundControllerUtil.existInElma(syrg); 

	}	

	private JsonMaintMainCundfRecord fetchRecord(String applicationUser, String kundnr, String firma) {
		logger.info("::fetchRecord::");
		String BASE_URL = MaintenanceMainUrlDataStore.MAINTENANCE_MAIN_BASE_SYCUNDFR_GET_LIST_URL;
		StringBuilder urlRequestParams = new StringBuilder();
		urlRequestParams.append("user=" + applicationUser);
		urlRequestParams.append("&kundnr=" + kundnr);
		urlRequestParams.append("&firma=" + firma);

		logger.info("URL: " + BASE_URL);
		logger.info("PARAMS: " + urlRequestParams.toString());
		String jsonPayload = this.urlCgiProxyService.getJsonContent(BASE_URL, urlRequestParams.toString());
//		logger.info(jsonPayload);

		JsonMaintMainCundfRecord record = new JsonMaintMainCundfRecord(), fmotRecord = new JsonMaintMainCundfRecord();
		if (jsonPayload != null) {
			jsonPayload = jsonPayload.replaceFirst("Customerlist", "customerlist"); //??
			JsonMaintMainCundfContainer container = this.maintMainCundfService.getList(jsonPayload);
			if (container != null) {
				for (Iterator<JsonMaintMainCundfRecord> iterator = container.getList().iterator(); iterator.hasNext();) {
					record = (JsonMaintMainCundfRecord) iterator.next();
					if (StringUtils.hasValueIgnoreZero(record.getFmot()) && !kundnr.equals(record.getFmot())) { // to avoid circular-ref
						fmotRecord= fetchRecord(applicationUser,record.getFmot(),firma);
						record.setFmotname(fmotRecord.getKnavn());
					}
					if (StringUtils.hasValueIgnoreZero(record.getPostnr())) {
						String leftPaddedPostnr = org.apache.commons.lang3.StringUtils.leftPad(record.getPostnr(), 4, '0');
						record.setPostnr(leftPaddedPostnr);
					}
					record.setElma(vkundControllerUtil.existInElma(record.getSyrg()));
					JsonMaintMainCundcRecord cundc = vkundControllerUtil.getInvoiceEmailRecord(applicationUser,firma, kundnr );
					if (cundc != null) {
						record.setEpost("J");
						if (!StringUtils.hasValue(cundc.getCemail())) {
							logger.error("Invalid setup of SINGELFAKTURA and SAMLEFAKTURA! email is empty!");
							record.setEpostmott("varning: epost saknes på SINGELFAKTURA/SAMLEFAKTURA");
						} else {
							record.setEpostmott(cundc.getCemail());
						}
					} else {
						logger.info("cundc is null");
					}
					VadrDao vadrDao = vkundControllerUtil.getVareAdressRecordNr1(applicationUser,firma, kundnr );
					if (vadrDao != null) {
						record.setVadrna(vadrDao.getVadrna());
						record.setVadrn1(vadrDao.getVadrn1());
						record.setVadrn2(vadrDao.getVadrn2());
						record.setVadrn3(vadrDao.getVadrn3());
						record.setValand(vadrDao.getValand());
					} else {
						logger.info("vadrDao is null");
					}
				
				}
			}
		}

		return record;
	}
	

	private JsonMaintMainCundfRecord updateRecord(SystemaWebUser appUser, JsonMaintMainCundfRecord record, String mode, StringBuffer errMsg) {
		logger.info("::updateRecord::");
		JsonMaintMainCundfRecord savedRecord = null;
		String BASE_URL = MaintenanceMainUrlDataStore.MAINTENANCE_MAIN_BASE_SYCUNDFR_DML_UPDATE_URL;
		String urlRequestParamsKeys = "user=" + appUser.getUser() + "&mode=" + mode + "&lang=" +appUser.getUsrLang();
		String urlRequestParams = urlRequestParameterMapper.getUrlParameterValidString((record));
		urlRequestParams = urlRequestParamsKeys + urlRequestParams;

		logger.info("URL: " + jsonDebugger.getBASE_URL_NoHostName(BASE_URL));
		logger.info("URL PARAMS: " + urlRequestParams);

		List<JsonMaintMainCundfRecord> list = new ArrayList();
		String jsonPayload = this.urlCgiProxyService.getJsonContent(BASE_URL, urlRequestParams);
	//	logger.info("jsonPayload=" + jsonPayload);
		if (jsonPayload != null) {
			JsonMaintMainCundfContainer container = this.maintMainCundfService.doUpdate(jsonPayload);
			if (container != null) {
				if (container.getErrMsg() != null && !"".equals(container.getErrMsg())) {
					errMsg.append(container.getErrMsg());
					return null;
				}
				list = (List) container.getList();
				for (JsonMaintMainCundfRecord cundfEntity : list) {
					savedRecord = cundfEntity;
				}
			}
		}

		logger.info("savedRecord="+ReflectionToStringBuilder.toString(savedRecord, ToStringStyle.DEFAULT_STYLE));
		if (savedRecord != null) {  

			manageInvoiceEmail(appUser, record, errMsg, savedRecord);

			manageVareAdresseNr1(appUser, record,  errMsg, savedRecord);
			
		}
		
		return savedRecord;

	}

	private void manageVareAdresseNr1(SystemaWebUser appUser, JsonMaintMainCundfRecord record,  StringBuffer errMsg, JsonMaintMainCundfRecord savedRecord) {
		logger.info("::manageVareAdresseNr1::");
		int retval;
		VadrDao existVadrDao = vkundControllerUtil.getVareAdressRecordNr1(appUser.getUser(),savedRecord.getFirma(), savedRecord.getKundnr() );

		VadrDao dao = new VadrDao();
		dao.setVadrnr(1);
		dao.setVadrna(record.getVadrna());
		dao.setVadrn1(record.getVadrn1());
		dao.setVadrn2(record.getVadrn2());
		dao.setVadrn3(record.getVadrn3());
		dao.setValand(record.getValand());
		dao.setFirma(savedRecord.getFirma());
		dao.setKundnr(Integer.parseInt(savedRecord.getKundnr()));
		
		if (existVadrDao == null) {
			if (isEmpty(dao)) {
				//do nothing
			} else {
				retval = vkundControllerUtil.saveVareAdressRecordNr1(appUser,dao, MainMaintenanceConstants.MODE_ADD, errMsg);
				if (retval == MainMaintenanceConstants.ERROR_CODE) {
					logger.error("Could not create VADR , error="+errMsg);
				}
				logger.info("ADDED, dao="+ReflectionToStringBuilder.toString(dao));
			}

		} else  {
			if (isEmpty(dao)) {
				retval = vkundControllerUtil.saveVareAdressRecordNr1(appUser,dao, MainMaintenanceConstants.MODE_DELETE, errMsg);
				if (retval == MainMaintenanceConstants.ERROR_CODE) {
					logger.error("Could not delete VADR for , error="+errMsg);
				}
				logger.info("DELETED, dao="+ReflectionToStringBuilder.toString(dao));
			} else {
				retval = vkundControllerUtil.saveVareAdressRecordNr1(appUser,dao, MainMaintenanceConstants.MODE_UPDATE, errMsg);
				if (retval == MainMaintenanceConstants.ERROR_CODE) {
					logger.error("Could not update VADR for , error="+errMsg);
				}
				logger.info("UPDATED, dao="+ReflectionToStringBuilder.toString(dao));
			}
		} 
	}

	private boolean isEmpty(VadrDao dao) {
		if (   StringUtils.hasValue(dao.getVadrna()) 
			|| StringUtils.hasValue(dao.getVadrn1()) 
			|| StringUtils.hasValue(dao.getVadrn2()) 
			|| StringUtils.hasValue(dao.getVadrn3()) 
			|| StringUtils.hasValue(dao.getValand())) {
			logger.info("::isEmpty, false::");
			return false;
		} else {
			logger.info("::isEmpty, true::");
			return true;
		}
	}

	private void manageInvoiceEmail(SystemaWebUser appUser, JsonMaintMainCundfRecord record, StringBuffer errMsg, JsonMaintMainCundfRecord savedRecord) {
		logger.info("::manageInvoiceEmail::");
		int retval;
		JsonMaintMainCundcRecord cundInvoiceRecord = vkundControllerUtil.getInvoiceEmailRecord(appUser.getUser(),savedRecord.getFirma(), savedRecord.getKundnr() );
		if (StringUtils.hasValue(record.getEpostmott()) && cundInvoiceRecord == null) {
			savedRecord.setEpostmott(record.getEpostmott());
			retval = createCundcInvoicesCtype(appUser, savedRecord, errMsg);
			if (retval == MainMaintenanceConstants.ERROR_CODE) {
				logger.error("Could not create invoice ctype for "+record.getEpostmott());
			}
		}
	}
	
	private int createCundcInvoicesCtype(SystemaWebUser appUser, JsonMaintMainCundfRecord cundf, StringBuffer errMsg) {
		logger.info("::createCundcInvoicesCtype::");
		int retval = 0;
		JsonMaintMainCundcRecord cundc = new JsonMaintMainCundcRecord();
		cundc.setCcompn(cundf.getKundnr());
		cundc.setCfirma(cundf.getFirma());
		cundc.setCemail(cundf.getEpostmott());

		cundc.setCtype("*SINGELFAKTURA");
		cundc.setCconta(cundf.getEpostmott());
		retval =  createCundc(appUser, cundc, errMsg);
		
		cundc.setCtype("*SAMLEFAKTURA");
		cundc.setCconta(cundf.getEpostmott());
		retval =  createCundc(appUser, cundc, errMsg);
		
		return retval;
	}
	
	private int createCundc(SystemaWebUser appUser, JsonMaintMainCundcRecord record, StringBuffer errMsg) {
		logger.info("::createCundc::");
		int retval = 0;
		String BASE_URL = MaintenanceMainUrlDataStore.MAINTENANCE_MAIN_BASE_CUNDC_DML_UPDATE_URL;
		String urlRequestParamsKeys = "user=" + appUser.getUser() + "&mode=" + MainMaintenanceConstants.MODE_ADD + "&lang=" +appUser.getUsrLang();
		String urlRequestParams = urlRequestParameterMapper.getUrlParameterValidString((record));
		urlRequestParams = urlRequestParamsKeys + urlRequestParams;

		logger.info("URL: " + jsonDebugger.getBASE_URL_NoHostName(BASE_URL));
		logger.info("URL PARAMS: " + urlRequestParams);
		String jsonPayload = this.urlCgiProxyService.getJsonContent(BASE_URL, urlRequestParams);
		if (jsonPayload != null) {
			JsonMaintMainCundcContainer container = maintMainCundcService.doUpdate(jsonPayload);
			if (container != null) {
				if (container.getErrMsg() != null && !"".equals(container.getErrMsg())) {
						errMsg.append(container.getErrMsg());
						retval = MainMaintenanceConstants.ERROR_CODE;
				}
			}
		}

		return retval;
		
	}
	
	/* betbet can has empty or something like it, this one is ugly */
	private void theBetBetFix(SystemaWebUser appUser, JsonMaintMainCundfRecord recordToValidate, String action) {
		if (MainMaintenanceConstants.ACTION_CREATE.equals(action)) {
			if ("F".equals(recordToValidate.getKundetype())) { // Fakturakunde
				// validation downstreams
			} else if ("A".equals(recordToValidate.getKundetype())) { // Adressekunde
				if ("NOT_SET".equals(recordToValidate.getBetbet())) {
					recordToValidate.setBetbet("");
				}
			}
		}

		if (MainMaintenanceConstants.ACTION_UPDATE.equals(action)) {
			if ("N".equals(vkundControllerUtil.isAdressCustomer(appUser, new Integer(recordToValidate.getKundnr())))) { // Fakturakunde
				// validation downstreams
			} else { // Adressekunde
				if ("NOT_SET".equals(recordToValidate.getBetbet())) {
					recordToValidate.setBetbet("");
				}
			}
		}

	}
	
	
	private void adjustRecordToValidate(JsonMaintMainCundfRecord recordToValidate, KundeSessionParams kundeSessionParams) {
		recordToValidate.setFirma(kundeSessionParams.getFirma());
		recordToValidate.setKundnr(kundeSessionParams.getKundnr());
	}
	
	private void specialRules(JsonMaintMainCundfRecord recordToValidate) {
		logger.info("::specialRules::");
		if ("J".equals(recordToValidate.getSyfr06())) {
			logger.info("Removing sfakt value");
			recordToValidate.setSfakt("");
		}
		
	}

	private void populateDropDowns(Map model, String user) {
		codeDropDownMgr.populateBetBetDropDown(this.urlCgiProxyService,  model, user);
	}	
	
	//Wired - SERVICES
	@Qualifier ("urlCgiProxyService")
	private UrlCgiProxyService urlCgiProxyService;
	@Autowired
	@Required
	public void setUrlCgiProxyService (UrlCgiProxyService value){ this.urlCgiProxyService = value; }
	public UrlCgiProxyService getUrlCgiProxyService(){ return this.urlCgiProxyService; }
	
	@Qualifier ("maintMainCundfService")
	private MaintMainCundfService maintMainCundfService;
	@Autowired
	@Required
	public void setMaintMainCundfService (MaintMainCundfService value){ this.maintMainCundfService = value; }
	public MaintMainCundfService getMaintMainCundfService(){ return this.maintMainCundfService; }

	
	@Qualifier ("maintMainCundcService")
	private MaintMainCundcService maintMainCundcService;
	@Autowired
	@Required
	public void setMaintMainCundcService (MaintMainCundcService value){ this.maintMainCundcService = value; }
	public MaintMainCundcService getMaintMainCundcService(){ return this.maintMainCundcService; }
	
	@Qualifier ("maintMainCustomerL1Service")
	private MaintMainCustomerL1Service maintMainCustomerL1Service;
	@Autowired
	@Required
	public void setMaintMainCutomerL1Service (MaintMainCustomerL1Service value){ this.maintMainCustomerL1Service = value; }
	public MaintMainCustomerL1Service getMaintMainCutomerL1Service(){ return this.maintMainCustomerL1Service; }		
	
	
	
}

