package be.dnsbelgium.rdap.core;

public interface HttpStatus {
  public static final int MOVED_PERMANENTLY = 301;
  public static final int BAD_REQUEST = 400;
  public static final int NOT_FOUND = 404;
  public static final int INTERNAL_SERVER_ERROR = 500;
  public static final int NOT_IMPLEMENTED = 501;
}
