/**
 * 
 */
package no.systema.z.main.maintenance.service;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.log4j.Logger;

import no.systema.z.main.maintenance.mapper.jsonjackson.JsonCustomerL1Mapper;
import no.systema.z.main.maintenance.model.jsonjackson.dbtable.JsonMaintMainKundfContainer;



/**
 * 
 * @author oscardelatorre
 * @date Oct 2019
 * 
 * 
 */
public class MaintMainCustomerL1ServiceImpl implements MaintMainCustomerL1Service {
	private static final Logger logger = Logger.getLogger(MaintMainCustomerL1ServiceImpl.class.getName());
	/**
	 * 
	 */
	public JsonMaintMainKundfContainer getContainer(String utfPayload) {
		JsonMaintMainKundfContainer container = null;
		try{
			JsonCustomerL1Mapper mapper = new JsonCustomerL1Mapper();
			container = mapper.getContainer(utfPayload);
			//logger.info(container);
		}catch(Exception e){
			e.printStackTrace();
		}
		return container;
	}
	
	
}
