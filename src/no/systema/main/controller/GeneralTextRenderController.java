package no.systema.main.controller;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.ResourceBundle;

 
import org.slf4j.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

//application imports
import no.systema.main.util.AppConstants;
import no.systema.main.util.AppResources;
import no.systema.main.util.ApplicationPropertiesUtil;
import no.systema.main.util.io.PayloadContentFlusher;
import no.systema.z.main.maintenance.util.manager.Log4jMgr;
import no.systema.main.context.TdsServletContext;
import no.systema.main.model.SystemaWebUser;


/**
 * 
 * Text(file) Render Controller 
 * 
 * @author oscardelatorre
 * @date Aug 25, 2014
 * 
 * 
 */

@Controller
@Scope("session")
public class GeneralTextRenderController {
	//OBSOLETE:  static final ResourceBundle resources = AppResources.getBundle();
	
	private static final Logger logger = LoggerFactory.getLogger(GeneralTextRenderController.class.getName());
	private PayloadContentFlusher payloadContentFlusher = new PayloadContentFlusher();
	
	private final String RELATIVE_LOGFILE_PATH = "logs/" + ApplicationPropertiesUtil.getProperty("logsg.logger.file");   //OBSOLETE: resources.getString("log4j.logger.file");
	private final String SERVLET_CONTEXT_WEBAPPS_ROOT = "webapps";
	
	private ModelAndView loginView = new ModelAndView("redirect:logout.do");
	
	/**
	 * 
	 * @param session
	 * @param request
	 * @return
	 */
	@RequestMapping(value="renderLocalLogsg.do", method={ RequestMethod.GET })
	public ModelAndView doRenderLocalLogsg(HttpSession session, HttpServletRequest request, HttpServletResponse response){
		logger.info("Inside doRenderLocalLogsg...");
		SystemaWebUser appUser = (SystemaWebUser)session.getAttribute(AppConstants.SYSTEMA_WEB_USER_KEY);
		Log4jMgr log4jMgr = new Log4jMgr();
		
		if(appUser==null){
			return this.loginView;
			
		}else{
			try{
				log4jMgr.doLevelUpdate(request, response);
			}catch(Exception e){
				e.toString();
			}
			
			
			String path = TdsServletContext.getTdsServletContext().getRealPath("/");
			logger.info("ServletContext:" + path);
			int pathRootIndex = path.indexOf(SERVLET_CONTEXT_WEBAPPS_ROOT);
			String logFile = null;
			if(pathRootIndex!=-1){
				logFile = path.substring(0,pathRootIndex) + RELATIVE_LOGFILE_PATH;
				logger.info("logFile:" + logFile);
			}
			
			if(logFile!=null ){
				//must know the file type in order to put the correct content type on the Servlet response.
                String fileType = this.payloadContentFlusher.getFileType(logFile);
                if(AppConstants.DOCUMENTTYPE_LOG.equals(fileType)){
                		response.setContentType(AppConstants.HTML_CONTENTTYPE_TEXTHTML);
                }else if(AppConstants.DOCUMENTTYPE_TXT.equals(fileType)){
            			response.setContentType(AppConstants.HTML_CONTENTTYPE_TEXTHTML);
                }//--> with browser dialogbox: response.setHeader ("Content-disposition", "attachment; filename=\"edifactPayload.txt\"");
                response.setHeader ("Content-disposition", "filename=\"log4jCustomer.txt" + fileType + "\"");
                
                logger.info("Start flushing file payload...");
                //send the file output to the ServletOutputStream
                try{
                		//InputStream inputStream = session.getServletContext().getResourceAsStream(logFile);
                		this.payloadContentFlusher.flushServletOutput(response, logFile);
                	
                }catch (Exception e){
                		e.printStackTrace();
                }
            }
			//this to present the output in an independent window
            return(null);
			
		}
			
	}	
	
	
	
	
	
}

