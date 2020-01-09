package no.systema.main.controller;

import java.util.Calendar;
import java.util.Enumeration;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Category;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import java.net.URLEncoder;

import no.systema.main.cookie.SessionCookieManager;
//application imports
import no.systema.main.util.AppConstants;
import no.systema.z.main.maintenance.util.manager.Log4jMgr;


@Controller
public class LogoutController {
	private static final Logger logger = Logger.getLogger(LogoutController.class.getName());
	
	
	/**
	 * 
	 * @param session
	 * @param response
	 * @param request
	 */
	@RequestMapping(value="logout.do", method={RequestMethod.POST, RequestMethod.GET} )
	public void logout(HttpSession session, HttpServletResponse response, HttpServletRequest request){
		
		if (session!=null){ 
			
			//go back to WARN level since we might have put lower levels (DEBUG, INFO) for debugging reasons
			Log4jMgr log4jMgr = new Log4jMgr();
			log4jMgr.doLogoutLogger();
			//remove token cookie (init)
			new SessionCookieManager(request).removeLocalCookie(response);
			
			//go on
            session.removeAttribute(AppConstants.SYSTEMA_WEB_USER_KEY);
            session.invalidate();
            logger.info("Session invalidated..." + Calendar.getInstance().getTime());       
        }
		try{
			//issue a redirect for a fresh start. POST (and not GET) should be the final version of a sendRedirect (Maybe via RedirectView)
			//response.sendRedirect("/espedsg2/logonDashboard.do?ru=" + URLEncoder.encode(user,"UTF-8") + "&dp=" + URLEncoder.encode(pwd,"UTF-8") + "&aes=" + aes);
			response.sendRedirect("/espedsg2/dashboard.do");
			
		}catch (Exception e){
			e.printStackTrace();
		}
	}

	
    
}

