/*
  * LRUCache utility class
  *
  * Copyright 1998 by Oliver Steele.  You can use this software freely so long
  * as you preserve the copyright notice and this restriction, and label your
  * changes.
  *
  * Nick Rizzolo rewrote this class to improve its efficiency while
  * maintaining the same interface.
 */
package edu.brandeis.cs.steele.util;

import java.util.HashMap;


/**
  * A fixed-capacity <code>Cache</code> that stores the <var>n</var> most
  * recently used keys.
  *
  * @author Oliver Steele, steele@cs.brandeis.edu, and Nick Rizzolo
  * @version 2.0
 */
public class LRUCache<K, V> implements Cache<K, V> {
  protected int capacity;

  protected MyLinkedList<K> keys;
  protected HashMap<K, MapElement<K, V>> map;

  public LRUCache(int capacity) {
    this.capacity = capacity;
    keys = new MyLinkedList<K>();
    map = new HashMap<K, MapElement<K, V>>(capacity * 4 / 3 + 1);
  }

  public synchronized void put(K key, V value) {
    MapElement<K, V> me = map.get(key);
    if (me == null) {
      MyLinkedList.ListElement<K> e = keys.addFirst(key);
      me = new MapElement<K, V>(e, value);
      map.put(key, new MapElement<K, V>(e, value));
      if (map.size() == capacity) remove(keys.getLast());
    }
    else {
      keys.moveToFirst(me.key);
      me.value = value;
    }
  }

  public synchronized V get(K key) {
    MapElement<K, V> me = map.get(key);
    if (me == null) return null;
    keys.moveToFirst(me.key);
    return me.value;
  }

  public synchronized void remove(K key) {
    MapElement<K, V> me = map.remove(key);
    if (me == null) return;
    keys.remove(me.key);
  }

  public synchronized void clear() {
    keys.removeAllElements();
    map.clear();
  }


  private static class MyLinkedList<K>
  {
    private ListElement<K> head;

    public MyLinkedList() {
      head = new ListElement<K>(null);
      head.next = head;
      head.previous = head;
    }

    public K getLast() {
      if (head.previous == head)
        throw new IndexOutOfBoundsException(
            "LRUCache.MyLinkedList: tried to getLast() on empty list.");
      return head.previous.element;
    }

    public ListElement<K> addFirst(K o) {
      ListElement<K> e = new ListElement<K>(o);
      linkFirst(e);
      return e;
    }

    public void moveToFirst(ListElement<K> e) {
      remove(e);
      linkFirst(e);
    }

    private void linkFirst(ListElement<K> e) {
      e.next = head.next;
      e.next.previous = e;
      head.next = e;
      e.previous = head;
    }

    public void remove(ListElement<K> e) {
      e.previous.next = e.next;
      e.next.previous = e.previous;
      e.next = e.previous = null;
    }

    public void removeAllElements() {
      if (head.next == head) return;
      head.next.previous = head.previous.next = null;
      head.next = head.previous = head;
    }

    private static class ListElement<K>
    {
      public K element;
      public ListElement<K> next, previous;

      public ListElement(K e) { element = e; }
    }
  }


  private static class MapElement<K, V>
  {
    public MyLinkedList.ListElement<K> key;
    public V value;

    public MapElement(MyLinkedList.ListElement<K> k, V v) {
      key = k;
      value = v;
    }
  }
}

