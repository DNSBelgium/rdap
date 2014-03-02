package be.dnsbelgium.rate;

public interface LazyLeakyBucketDao<T extends LazyLeakyBucketKey> {

  LazyLeakyBucket load(T key);

}
