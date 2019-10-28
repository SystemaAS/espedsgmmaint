package no.systema.z.main.maintenance.url.store;

import no.systema.main.model.UrlDataStoreAnnotationForField;
import no.systema.main.util.AppConstants;

/**
 * 
 * Static URLs on external systems
 * @author oscardelatorre
 * @date Oct 2019
 * 
 * 
 */
public class ExternalUrlDataStore {

	//----------------------------
	//[1] FETCH L1 record
	//----------------------------
	//http://gw.systema.no/sycgip/todo.pgm?user=OSCAR
	@UrlDataStoreAnnotationForField (name="@EbookingMainOrderListController - ebooking_mainorderlist.do ", description=" --> EBOOKING_BASE_MAIN_ORDER_LIST_URL - fetch customer")
	static public String L1_BASE_FETCH_SPECIFIC_CUSTOMER_URL = AppConstants.HTTP_ROOT_CGI + "/sycgip/todo.pgm";
	
	//--------------------
	//[3] EDIT Customer
	//----------------------
	//http://gw.systema.no/sycgip/TJL1KUND.pgm?user=JOVO&firma=SY&kundnr=80012&aktkod=A&knavn=Fredrik_L1&adr1=test2&adr2=testadr&posntr=1000&adr3=OSLO 
	@UrlDataStoreAnnotationForField (name="@MainMaintenanceCundfVkundController - mainmaintenancecundf_kund_edit.do ", description=" --> L1_BASE_UPDATE_SPECIFIC_CUSTOMER_URL - updates specific L1-customer")
	static public String L1_BASE_UPDATE_SPECIFIC_CUSTOMER_URL = AppConstants.HTTP_ROOT_CGI + "/sycgip/TJL1KUND.pgm";
	
	
}
