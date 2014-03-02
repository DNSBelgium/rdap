package be.dnsbelgium.rate;

public class StaticLazyLeakyBucketDao implements LazyLeakyBucketDao {

  private final int capacity, rate;

  public StaticLazyLeakyBucketDao(int capacity, int rate) {
    this.capacity = capacity;
    this.rate = rate;
  }

  @Override
  public LazyLeakyBucket load(LazyLeakyBucketKey key) {
    return new LazyLeakyBucket(capacity, rate);
  }
}
