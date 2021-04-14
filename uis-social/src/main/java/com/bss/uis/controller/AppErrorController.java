package com.bss.uis.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorController;
//import org.springframework.boot.autoconfigure.web.ErrorAttributes;
//import org.springframework.boot.autoconfigure.web.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.ModelAndView;

/**
 * Basic Controller which is called for unhandled errors
 */
@Controller
public class AppErrorController implements ErrorController{

    /**
     * Error Attributes in the Application
     */
    private ErrorAttributes errorAttributes;

    private static final String ERROR_PATH = "/error";

    /**
     * Controller for the Error Controller
     * @param errorAttributes
     */
    public AppErrorController(ErrorAttributes errorAttributes) {
        this.errorAttributes = errorAttributes;
    }

    /**
     * Supports the HTML Error View
     * @param request
     * @return
     */
    @RequestMapping(value = ERROR_PATH, produces = "text/html")
    public ModelAndView errorHtml(HttpServletRequest request) {
        return new ModelAndView("errors/error", getErrorAttributes(request, getTraceParameter(request)));
    }

    /**
     * Supports other formats like JSON, XML
     * @param request
     * @return
     */
    @GetMapping(value = ERROR_PATH)
    @ResponseBody
    public ResponseEntity<Map<String, Object>> error(HttpServletRequest request) {
        Map<String, Object> body = getErrorAttributes(request, getTraceParameter(request));
        HttpStatus status = getStatus(request);
        return new ResponseEntity<Map<String, Object>>(body, status);
    }

    /**
     * Returns the path of the error page.
     *
     * @return the error path
     */
    @Override
    public String getErrorPath() {
        return ERROR_PATH;
    }


    private boolean getTraceParameter(HttpServletRequest request) {
        String parameter = request.getParameter("trace");
        if (parameter == null) {
            return false;
        }
        return !"false".equalsIgnoreCase(parameter);
    }

	private Map<String, Object> getErrorAttributes(HttpServletRequest request, boolean includeStackTrace) {
		WebRequest webRequest = new ServletWebRequest(request);
		return this.errorAttributes.getErrorAttributes(webRequest, includeStackTrace);
	}

    private HttpStatus getStatus(HttpServletRequest request) {
        Integer statusCode = (Integer) request
                .getAttribute("javax.servlet.error.status_code");
        if (statusCode != null) {
            try {
                return HttpStatus.valueOf(statusCode);
            }
            catch (Exception ex) {
            }
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }
}