/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.usr.web.handler;

import it.usr.web.producer.AppLogger;
import jakarta.faces.application.ViewExpiredException;
import jakarta.faces.context.ExceptionHandler;
import jakarta.faces.context.ExceptionHandlerFactory;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import org.omnifaces.exceptionhandler.FullAjaxExceptionHandler;
import org.omnifaces.exceptionhandler.FullAjaxExceptionHandlerFactory;
import org.slf4j.Logger;

/**
 *
 * @author riccardo.iovenitti
 */
public class AjaxExceptionHandler extends FullAjaxExceptionHandler {
    private Logger logger;
    
    public AjaxExceptionHandler(ExceptionHandler wrapped) {
        super(wrapped);
    }

    @Override
    protected void logException(FacesContext context, Throwable exception, String location, String message, Object... parameters) {
        HttpServletRequest request = (HttpServletRequest)context.getExternalContext().getRequest();
        String uri = request.getRequestURI();
        if (exception instanceof ViewExpiredException) {
            // With exception==null, no trace will be logged.
            logger.warn("Sessione scaduta. Rinvio alla pagina di login");
            super.logException(context, null, location, message, parameters);
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

    private void setLogger(Logger logger) {
        this.logger = logger;
    }
    
    public static class Factory extends FullAjaxExceptionHandlerFactory {
        @Inject
        @AppLogger
        private Logger logger;
        
        public Factory(ExceptionHandlerFactory wrapped) {
            super(wrapped);
        }

        @Override
        public ExceptionHandler getExceptionHandler() {
            AjaxExceptionHandler ea = new AjaxExceptionHandler(getWrapped().getExceptionHandler());
            ea.setLogger(logger);
            return ea;
        }
    }
}
