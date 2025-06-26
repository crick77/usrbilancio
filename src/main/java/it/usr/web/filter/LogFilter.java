/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.usr.web.filter;

import jakarta.faces.application.ViewExpiredException;
import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import org.omnifaces.filter.FacesExceptionFilter;
import org.slf4j.Logger;
import it.usr.web.producer.AppLogger;

/**
 *
 * @author riccardo.iovenitti
 */
public class LogFilter extends FacesExceptionFilter  {
    @Inject
    @AppLogger
    private Logger logger;
               
    @Override
    protected void logException(HttpServletRequest request, Throwable exception, String location, String message, Object... parameters) {
        String uri = request.getRequestURI();
        if (exception instanceof ViewExpiredException) {
            // With exception==null, no trace will be logged.
            super.logException(request, null, location, message, parameters);
        }
        else {   
            request.setAttribute("UUID", request.getAttribute("org.omnifaces.exception_uuid"));
            logger.error("UUID [{}]  -- INIZIO --", request.getAttribute("org.omnifaces.exception_uuid"));
            logger.error("Pagina [{}]", uri);            
            logger.error("Destinazione [{}]", location);            
            logger.error("Messaggio [{}]", message);
            logger.error("Eccezione di base [{}]", exception!=null ? exception.toString() : null);
            logger.error("StackTrace:\n{}", stackTraceToString(exception!=null ? exception.getCause() : exception));
            logger.error("UUID [{}] -- FINE --", request.getAttribute("org.omnifaces.exception_uuid"));
        } 
    }
    
    private String stackTraceToString(Throwable t) {                
        if(t!=null) {
            StringBuilder sb = new StringBuilder();
            int i = 0;
            for(StackTraceElement el : t.getStackTrace()) {
                if(i>0) sb = sb.append("\t");                
                sb = sb.append(el.toString()).append("\n");
                i++;
            }
            return sb.toString();
        }
        else {
            return "No Exception Trace.";
        }        
    }

    @Override
    public void init() throws ServletException {
        super.init(); //To change body of generated methods, choose Tools | Templates.
        System.out.println(getClass().getName()+": initialized (logger: "+logger+").");
    }
    
    
}
