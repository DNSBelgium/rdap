package be.dnsbelgium.rdap.exception;

import be.dnsbelgium.rdap.core.RDAPError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;

@ControllerAdvice
public class ExceptionAdvice {

  @ExceptionHandler(value = RDAPError.class)
  @ResponseBody
  protected RDAPError handleResourceNotFoundException(RDAPError error, HttpServletResponse response) {
    response.setStatus(error.getErrorCode());
    return error;
  }

}
