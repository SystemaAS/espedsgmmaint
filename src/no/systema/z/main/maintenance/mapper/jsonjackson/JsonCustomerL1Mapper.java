/**
 * 
 */
package no.systema.z.main.maintenance.mapper.jsonjackson;

//jackson library
import org.slf4j.*;

//application library
import no.systema.main.mapper.jsonjackson.general.ObjectMapperAbstractGrandFather;
import no.systema.z.main.maintenance.model.jsonjackson.dbtable.JsonMaintMainKundfContainer;
import no.systema.z.main.maintenance.model.jsonjackson.dbtable.JsonMaintMainKundfRecord;


/**
 * @author oscardelatorre
 * @date OCt 2019
 * 
 */
public class JsonCustomerL1Mapper extends ObjectMapperAbstractGrandFather {
	private static final Logger logger = LoggerFactory.getLogger(JsonCustomerL1Mapper.class.getName());
	/**
	 * 
	 * @param utfPayload
	 * @return
	 * @throws Exception
	 */
	public JsonMaintMainKundfContainer getContainer(String utfPayload) throws Exception{
		
		//At this point we now have an UTF-8 payload
		JsonMaintMainKundfContainer container = super.getObjectMapper().readValue(utfPayload.getBytes(), JsonMaintMainKundfContainer.class); 
		//logger.info("[JSON-String payload status=OK]  " + container.getUser() + " Kundnr: "+ container.getKundnr());
		//for (JsonMaintMainKundfRecord record : container.getList()){
			//DEBUG
		//}
		
		return container;
	}
	
}
