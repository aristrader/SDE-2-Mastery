package org.example.backend_fundamentals.low_level_design.case_studies.lru_cache.playground;

import java.util.Optional;

/** Entry point for the LRU Cache LLD exercise. */
public class LruCacheRun {

  public static void main(String[] args) {
    System.out.println("Start the LRU Cache LLD exercise here.");

    LruCache<Integer, Integer> lruCache = new LruCache<>(5);

    Optional<Integer> foundCacheValue = lruCache.get(1);
    if(foundCacheValue.isEmpty()){
      System.out.println("We got nothing in the LRU cache.");
    }
    lruCache.put(1, 1);
    foundCacheValue = lruCache.get(1);
    foundCacheValue.ifPresent(
        integer -> System.out.println("We found the value the cache key value is : " + integer));

    lruCache.put(2,2);
    lruCache.put(3,3);
    lruCache.put(4,4);
    lruCache.put(5,5);

    lruCache.traverseLruFromLeastToMost();

    System.out.println(lruCache.get(1).get());

    lruCache.traverseLruFromLeastToMost();

    lruCache.put(6,6);

    lruCache.traverseLruFromLeastToMost();

    lruCache.put(2, 10);

    lruCache.traverseLruFromLeastToMost();

    System.out.println("Updating key 6 at capacity; no entry should be evicted:");
    lruCache.put(6, 60);
    lruCache.traverseLruFromLeastToMost();

    try {
      lruCache = new LruCache<>(0);
    } catch (Exception e) {
      System.out.println("Exception caught with exception : " + e.getMessage());
    }

    try {
      lruCache = new LruCache<>(-1);
    } catch (Exception e) {
      System.out.println("Exception caught with exception : " + e.getMessage());
    }
  }
}
