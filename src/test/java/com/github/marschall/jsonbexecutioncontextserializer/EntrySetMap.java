package com.github.marschall.jsonbexecutioncontextserializer;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

final class EntrySetMap<K, V> extends AbstractMap<K, V> {

  private final List<Object> entries;

  EntrySetMap() {
    this.entries = new ArrayList<>();
  }

  @Override
  public Set<Entry<K, V>> entrySet() {
    return new LinearEntrySet();
  }

  @Override
  public int size() {
    return this.entries.size() / 2;
  }

  @Override
  public V put(K key, V value) {
    this.entries.add(key);
    this.entries.add(value);
    return null;
  }

  final class LinearEntrySet extends AbstractSet<Entry<K, V>> {

    @Override
    public Iterator<Entry<K, V>> iterator() {
      return new EntrySetMapIterator();
    }

    @Override
    public int size() {
      return EntrySetMap.this.size();
    }

  }

  final class EntrySetMapIterator implements Iterator<Entry<K, V>> {

    private int position;

    EntrySetMapIterator() {
      this.position = 0;
    }

    @Override
    public boolean hasNext() {
      return this.position < EntrySetMap.this.entries.size();
    }

    @Override
    public Entry<K, V> next() {
      if (!this.hasNext()) {
        throw new NoSuchElementException();
      }
      K key = (K) EntrySetMap.this.entries.get(this.position);
      V value = (V) EntrySetMap.this.entries.get(this.position + 1);

      this.position += 2;

      return Map.entry(key, value);
    }

  }

}
