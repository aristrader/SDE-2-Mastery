package org.example.backend_fundamentals.low_level_design.case_studies.lru_cache.playground;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Fixed-capacity in-memory cache that evicts the least recently used entry.
 *
 * <p>The list runs from least recently used after {@code head} to most recently used before
 * {@code tail}. The map finds the corresponding list node in O(1) average time.</p>
 */
public class LruCache<K,V> {
  private final Node<K,V> head;
  private final Node<K,V> tail;
  private final Map<K, Node<K,V>> nodeMap = new HashMap<>();
  private final int capacity;

  /** Creates a cache with a positive maximum number of entries. */
  public LruCache(int capacity) {
    if(capacity <= 0){
      throw new IllegalArgumentException("The capacity cannot be zero or less than zero. " + capacity);
    }
    this.head = new Node<>(null, null);
    this.tail = new Node<>(null, null);
    head.setNext(tail);
    tail.setPrev(head);
    this.capacity = capacity;
  }

  /**
   * Returns the cached value when present and refreshes that entry as most recently used.
   */
  public Optional<V> get(K key){
    if(key == null){
      throw new IllegalArgumentException("Passed Key is null.");
    }

    if(!nodeMap.containsKey(key)){
      return Optional.empty();
    }

    Node<K,V> node = nodeMap.get(key);
    removeNode(node);
    addAsMostRecentlyUsed(node);

    return Optional.of(node.getValue());
  }

  /**
   * Inserts or updates a value, marking its key as most recently used and evicting when necessary.
   */
  public void put(K key, V value){
    if(key == null || value == null){
      throw new IllegalArgumentException("Key or value cannot be null");
    }

    if(!nodeMap.containsKey(key)){
      if(nodeMap.size() == capacity){
        Node<K,V> deletedNode = removeLeastRecentlyUsed();
        nodeMap.remove(deletedNode.getKey());
        Node<K,V> node = new Node<>(key, value);
        addAsMostRecentlyUsed(node);
        nodeMap.put(key, node);
      } else {
        Node<K,V> node = new Node<>(key, value);
        addAsMostRecentlyUsed(node);
        nodeMap.put(key, node);
      }
      return;
    }

    Node<K,V> node = nodeMap.get(key);
    removeNode(node);
    addAsMostRecentlyUsed(node);
    node.setValue(value);
  }

  /**
   * Prints entries in recency order for the exercise runner; it is not part of the cache API.
   */
  public void traverseLruFromLeastToMost(){
    Node<K,V> temp = head.getNext();
    while (temp != tail){
      System.out.println(temp.getKey() + " " + temp.getValue());
      temp = temp.getNext();
    }
  }

  private void removeNode(Node<K,V> node){
    node.getPrev().setNext(node.getNext());
    node.getNext().setPrev(node.getPrev());
  }

  private void addAsMostRecentlyUsed(Node<K,V> node){
    node.setPrev(tail.getPrev());
    node.setNext(tail);
    tail.getPrev().setNext(node);
    tail.setPrev(node);
  }

  private Node<K, V> removeLeastRecentlyUsed(){
    Node<K,V> deleteNode = head.getNext();
    head.setNext(head.getNext().getNext());
    head.getNext().setPrev(head);
    deleteNode.setNext(null);
    deleteNode.setPrev(null);
    return deleteNode;
  }
}
