package org.example.backend_fundamentals.low_level_design.case_studies.lru_cache.playground;

import lombok.Data;

/** Internal linked-list entry used to move a cached key between recency positions in O(1). */
@Data
public class Node<K,V> {
  private final K key;
  private V value;
  private Node<K,V> next;
  private Node<K,V> prev;

  public Node(K key){
    this.key = key;
  }

  public Node(K key, V value){
    this.key = key;
    this.value = value;
  }
}
