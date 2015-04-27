package be.dnsbelgium.rdap;

import be.dnsbelgium.rdap.core.Error;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;

public abstract class AbstractController {
  @ExceptionHandler(value = be.dnsbelgium.rdap.core.Error.class)
  @ResponseBody
  protected Error handleResourceNotFoundException(Error error, HttpServletResponse response) {
    response.setStatus(error.getErrorCode());
    return error;
  }
}
