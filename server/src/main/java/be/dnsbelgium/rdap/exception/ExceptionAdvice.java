package be.dnsbelgium.rdap.exception;

import be.dnsbelgium.core.LabelException;
import be.dnsbelgium.rdap.core.RDAPError;
import com.ibm.icu.text.IDNA;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
public class ExceptionAdvice extends ResponseEntityExceptionHandler {
  private final Logger logger = LoggerFactory.getLogger(ExceptionAdvice.class);

  @ExceptionHandler(value = RDAPError.class)
  public HttpEntity<RDAPError> handleRdapError(RDAPError error, HttpServletResponse response) {
    response.setStatus(error.getErrorCode());

    return wrapRdapErrorInHttpEntityAndSetContentType(error);
  }

  @ExceptionHandler(value = LabelException.IDNParseException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public HttpEntity<RDAPError> handleIDNParseException(LabelException.IDNParseException ipe) {
    List<String> description = new ArrayList<String>();
    for (IDNA.Error error : ipe.getErrors()) {
      description.add(error.name());
    }
    return wrapRdapErrorInHttpEntityAndSetContentType(RDAPError.badRequest("Invalid domain name", description));
  }

  @ExceptionHandler(value = Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public HttpEntity<RDAPError> handleUnhandledException(Exception e) {
    logger.error("Some errors not handled", e);
    return wrapRdapErrorInHttpEntityAndSetContentType(new RDAPError(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal server error"));
  }

  private HttpEntity<RDAPError> wrapRdapErrorInHttpEntityAndSetContentType(RDAPError response) {
    // make sure spring returns "application/rdap+json;charset=UTF-8", would be "application/json" without this
    // see https://github.com/spring-projects/spring-framework/issues/21927
    HttpHeaders headers = new HttpHeaders();
    headers.add("Content-Type", "application/rdap+json;charset=UTF-8");

    return new HttpEntity<>(response, headers);
  }
}
