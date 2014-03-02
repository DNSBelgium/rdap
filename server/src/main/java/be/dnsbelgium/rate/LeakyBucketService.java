package be.dnsbelgium.rate;

public interface LeakyBucketService<T extends LeakyBucketKey> {

  boolean add(T key, int amount);

}
