package be.dnsbelgium.rate;

public class StaticLeakyBucketDao implements LeakyBucketDao {

  private final int capacity, rate;

  public StaticLeakyBucketDao(int capacity, int rate) {
    this.capacity = capacity;
    this.rate = rate;
  }

  @Override
  public LeakyBucket load(LeakyBucketKey key) {
    return new LeakyBucket(capacity, rate);
  }
}
