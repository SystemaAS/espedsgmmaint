/**
 * 
 */
package no.systema.external.tvinn.sad.z.maintenance.mapper;

//jackson library
import org.slf4j.*;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper; 

import no.systema.external.tvinn.sad.z.maintenance.model.JsonMaintSadSadlContainer;
import no.systema.external.tvinn.sad.z.maintenance.model.JsonMaintSadSadlRecord;

//
import java.util.*;

/**
 * @author oscardelatorre
 * @date Mar 29, 2018
 * 
 */
public class MaintSadSadlMapper {
	private static final Logger logger = LoggerFactory.getLogger(MaintSadSadlMapper.class.getName());
	
	public JsonMaintSadSadlContainer getContainer(String utfPayload) throws Exception{
		ObjectMapper mapper = new ObjectMapper();  
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,false);
		
		//At this point we now have an UTF-8 payload
		JsonMaintSadSadlContainer container = mapper.readValue(utfPayload.getBytes(), JsonMaintSadSadlContainer.class); 
		//logger.info("[JSON-String payload status=OK]  " + container.getUser());
		//DEBUG
		Collection<JsonMaintSadSadlRecord> list = container.getList();
		for(JsonMaintSadSadlRecord record : list){
			//logger.info(record.getKlikod());
		}
		return container;
	}
}
