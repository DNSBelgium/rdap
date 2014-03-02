package be.dnsbelgium.rate;

import com.google.common.cache.*;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class LazyLeakyBucketServiceImpl implements LazyLeakyBucketService {

  public static final Logger logger = LoggerFactory.getLogger(LazyLeakyBucketServiceImpl.class);

  public static class TimestampedItem<T extends Object> {
    private final T item;
    private final DateTime created = DateTime.now();

    public TimestampedItem(T item) {
      this.item = item;
    }

    public T getItem() {
      return item;
    }

    public DateTime getCreated() {
      return created;
    }
  }


  private final LoadingCache<LazyLeakyBucketKey, TimestampedItem<LazyLeakyBucket>> cache;

  private final long ttl;

  private final TimeUnit timeUnit;

  private final long refreshRate;

  private final TimeUnit refreshTimeUnit;

  private final LazyLeakyBucketDao lazyLeakyBucketDao;

  public LazyLeakyBucketServiceImpl(final long ttl, final TimeUnit timeUnit, long refreshRate, TimeUnit refreshTimeUnit, LazyLeakyBucketDao lazyLeakyBucketDao) {

    this.ttl = ttl;
    this.timeUnit = timeUnit;
    this.refreshRate = refreshRate;
    this.refreshTimeUnit = refreshTimeUnit;
    this.lazyLeakyBucketDao = lazyLeakyBucketDao;

    cache = CacheBuilder.newBuilder()
        .expireAfterAccess(this.ttl, this.timeUnit)
        .build(
            new CacheLoader<LazyLeakyBucketKey, TimestampedItem<LazyLeakyBucket>>() {

              @Override
              public TimestampedItem<LazyLeakyBucket> load(LazyLeakyBucketKey key) throws Exception {
                logger.debug("Load bucket for key {}", key);
                return new TimestampedItem<LazyLeakyBucket>(LazyLeakyBucketServiceImpl.this.lazyLeakyBucketDao.load(key));
              }

              @Override
              public ListenableFuture<TimestampedItem<LazyLeakyBucket>> reload(LazyLeakyBucketKey key, TimestampedItem<LazyLeakyBucket> oldValue) throws Exception {
                logger.debug("Reload bucket for key {}", key);
                LazyLeakyBucket newBucket = LazyLeakyBucketServiceImpl.this.lazyLeakyBucketDao.load(key);
                if (newBucket.getCapacity() == oldValue.getItem().getCapacity() && newBucket.getRate() == oldValue.getItem().getRate()) {
                  logger.debug("Configuration is the same, return the old");
                  return Futures.immediateFuture(new TimestampedItem<LazyLeakyBucket>(oldValue.getItem()));
                }
                logger.debug("Configuration has changed, return new leaky bucket with capacity {} and rate {}", newBucket.getCapacity(), newBucket.getRate());
                return Futures.immediateFuture(new TimestampedItem<LazyLeakyBucket>(newBucket));

              }
            }
        );
  }

  @Override
  public boolean add(LazyLeakyBucketKey key, int amount) {
    logger.debug("Adding {} to key {}", amount, key);
    TimestampedItem<LazyLeakyBucket> value;
    try {
      value = cache.get(key);
    } catch (ExecutionException e) {
      throw new IllegalArgumentException(e);
    }
    boolean result = value.getItem().add(amount);

    if (value.getCreated().plusMillis((int) refreshTimeUnit.toMillis(refreshRate)).isBeforeNow()) {
      logger.debug("Refresh TTL has expired. Refresh the bucket");
      cache.refresh(key);
    }
    return result;
  }


}
