package be.dnsbelgium.rdap.exception;

import be.dnsbelgium.core.LabelException;
import be.dnsbelgium.rdap.core.RDAPError;
import com.ibm.icu.text.IDNA;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@ControllerAdvice
public class ExceptionAdvice {
  private final Logger logger = LoggerFactory.getLogger(ExceptionAdvice.class);

  @ExceptionHandler(value = RDAPError.class)
  @ResponseBody
  public HttpEntity<RDAPError> handleRdapError(RDAPError error, HttpServletResponse response) {
    response.setStatus(error.getErrorCode());

    return wrapRdapErrorInHttpEntityAndSetContentType(error);
  }

  @ExceptionHandler(value = LabelException.IDNParseException.class)
  @ResponseBody
  public HttpEntity<RDAPError> handleIDNParseException(LabelException.IDNParseException ipe, HttpServletResponse response) {
    response.setStatus(HttpStatus.BAD_REQUEST.value());
    List<String> description = new ArrayList<String>();
    for (IDNA.Error error : ipe.getErrors()) {
      description.add(error.name());
    }
    return wrapRdapErrorInHttpEntityAndSetContentType(RDAPError.badRequest("Invalid domain name", description));
  }

  @ExceptionHandler(value = HttpMediaTypeNotAcceptableException.class)
  @ResponseBody
  public ResponseEntity<String> handleUnsupportedMediaType(HttpMediaTypeNotAcceptableException hmtnae, HttpServletResponse response) throws IOException {
    response.setStatus(HttpStatus.NOT_ACCEPTABLE.value());
    return null;
  }

  @ExceptionHandler(value = Exception.class)
  @ResponseBody
  public HttpEntity<RDAPError> handleUnhandledException(Exception e, HttpServletResponse response) {
    logger.error("Some errors not handled", e);
    response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
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
