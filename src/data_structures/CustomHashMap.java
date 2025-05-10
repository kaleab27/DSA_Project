package data_structures;

import java.util.Objects;

/**
 * A custom implementation of a HashMap data structure that maps keys to values.
 * This implementation uses a hash table with separate chaining for collision resolution.
 *
 * @param <K> The type of keys maintained by this map.
 * @param <V> The type of values associated with keys in this map.
 */
@SuppressWarnings("unchecked")
public class CustomHashMap<K, V> {
    private static final int INITIAL_CAPACITY = 16;
    private static final float LOAD_FACTOR = 0.75f;

    private CustomLinkedList<Node<K, V>>[] buckets;
    private int size;

    /**
     * Constructs an empty {@code CustomHashMap} with the default initial capacity.
     */
    public CustomHashMap() {
        this.buckets = new CustomLinkedList[INITIAL_CAPACITY];
        this.size = 0;
    }

    /**
     * Represents an individual key-value pair in the hash map.
     *
     * @param <K> The type of the key.
     * @param <V> The type of the value.
     */
    private static class Node<K, V> {
        K key;
        V value;

        /**
         * Constructs a new {@code Node} with the specified key and value.
         *
         * @param key   The key of the mapping.
         * @param value The value of the mapping.
         */
        Node(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }

    /**
     * Calculates the index of the bucket for a given key by computing its hash.
     *
     * @param key The key for which the bucket index is calculated.
     * @return The index of the bucket in the hash table.
     */
    private int getBucketIndex(K key) {
        int hash = Objects.hashCode(key);
        return Math.abs(hash) % buckets.length;
    }

    /**
     * Associates the specified value with the specified key in this map.
     * If the map previously contained a mapping for the key, the old value is replaced.
     *
     * @param key   The key with which the specified value is to be associated.
     * @param value The value to be associated with the specified key.
     */
    public void put(K key, V value) {
        int bucketIndex = getBucketIndex(key);

        if (buckets[bucketIndex] == null) {
            buckets[bucketIndex] = new CustomLinkedList<>();
        }

        CustomLinkedList<Node<K, V>> bucket = buckets[bucketIndex];

        for (int i = 0; i < bucket.size(); i++) {
            Node<K, V> node = bucket.get(i);
            if (Objects.equals(node.key, key)) {
                node.value = value;
                return;
            }
        }

        bucket.add(new Node<>(key, value));
        size++;

        if ((1.0 * size) / buckets.length > LOAD_FACTOR) {
            resize();
        }
    }

    /**
     * Returns the value to which the specified key is mapped, or {@code null}
     * if this map contains no mapping for the key.
     *
     * @param key The key whose associated value is to be returned.
     * @return The value associated with the specified key, or {@code null} if no mapping exists.
     */
    public V get(K key) {
        int bucketIndex = getBucketIndex(key);
        CustomLinkedList<Node<K, V>> bucket = buckets[bucketIndex];

        if (bucket != null) {
            for (int i = 0; i < bucket.size(); i++) {
                Node<K, V> node = bucket.get(i);
                if (Objects.equals(node.key, key)) {
                    return node.value;
                }
            }
        }

        return null;
    }

    /**
     * Removes the mapping for a key from this map if it is present.
     *
     * @param key The key whose mapping is to be removed.
     * @return The value that was associated with the key, or {@code null} if no mapping existed.
     */
    public V remove(K key) {
        int bucketIndex = getBucketIndex(key);
        CustomLinkedList<Node<K, V>> bucket = buckets[bucketIndex];

        if (bucket != null) {
            for (int i = 0; i < bucket.size(); i++) {
                Node<K, V> node = bucket.get(i);
                if (Objects.equals(node.key, key)) {
                    bucket.remove(i);
                    size--;
                    return node.value;
                }
            }
        }

        return null;
    }

    /**
     * Returns {@code true} if this map contains a mapping for the specified key.
     *
     * @param key The key whose presence is to be tested.
     * @return {@code true} if this map contains a mapping for the key, {@code false} otherwise.
     */
    public boolean containsKey(K key) {
        int bucketIndex = getBucketIndex(key);
        CustomLinkedList<Node<K, V>> bucket = buckets[bucketIndex];

        if (bucket != null) {
            for (int i = 0; i < bucket.size(); i++) {
                Node<K, V> node = bucket.get(i);
                if (Objects.equals(node.key, key)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Resizes the hash table when the current load factor exceeds the threshold.
     * This involves doubling the size of the buckets array and rehashing all entries.
     */
    private void resize() {
        CustomLinkedList<Node<K, V>>[] oldBuckets = buckets;
        buckets = new CustomLinkedList[oldBuckets.length * 2];
        size = 0;

        for (CustomLinkedList<Node<K, V>> bucket : oldBuckets) {
            if (bucket != null) {
                for (int i = 0; i < bucket.size(); i++) {
                    Node<K, V> node = bucket.get(i);
                    put(node.key, node.value);
                }
            }
        }
    }

    /**
     * Returns the number of key-value mappings in this map.
     *
     * @return The number of key-value mappings in this map.
     */
    public int size() {
        return size;
    }
}