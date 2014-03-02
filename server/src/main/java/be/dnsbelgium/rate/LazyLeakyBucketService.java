package be.dnsbelgium.rate;

public interface LazyLeakyBucketService<T extends LazyLeakyBucketKey> {

  boolean add(T key, int amount);

}
