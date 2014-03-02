package be.dnsbelgium.rate;

public interface LeakyBucketDao<T extends LeakyBucketKey> {

  LeakyBucket load(T key);

}
