package data_structures;

import java.util.Objects;

@SuppressWarnings("unchecked")
public class CustomHashMap<K, V> {
    // Initial capacity and load factor for resizing
    private static final int INITIAL_CAPACITY = 16;
    private static final float LOAD_FACTOR = 0.75f;

    // Internal storage (array of CustomLinkedList for collision handling)
    private CustomLinkedList<Node<K, V>>[] buckets;
    private int size; // Number of elements in the map

    // Constructor
    public CustomHashMap() {
        this.buckets = new CustomLinkedList[INITIAL_CAPACITY];
        this.size = 0;
    }

    // Node class for key-value pairs
    private static class Node<K, V> {
        K key;
        V value;

        Node(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }

    // Hash function to map keys to bucket indices
    private int getBucketIndex(K key) {
        int hash = Objects.hashCode(key);
        return Math.abs(hash) % buckets.length;
    }

    // PUT method: Add or update a key-value pair
    public void put(K key, V value) {
        int bucketIndex = getBucketIndex(key);

        // Initialize the CustomLinkedList for the bucket if null
        if (buckets[bucketIndex] == null) {
            buckets[bucketIndex] = new CustomLinkedList<>();
        }

        CustomLinkedList<Node<K, V>> bucket = buckets[bucketIndex];

        // Iterate through the bucket to check for the key's existence
        for (int i = 0; i < bucket.size(); i++) {
            Node<K, V> node = bucket.get(i);
            if (Objects.equals(node.key, key)) {
                node.value = value; // Update the value if key exists
                return;
            }
        }

        // Key not found; add a new node
        bucket.add(new Node<>(key, value));
        size++;

        // Resize if the load factor threshold is exceeded
        if ((1.0 * size) / buckets.length > LOAD_FACTOR) {
            resize();
        }
    }

    // GET method: Retrieve the value for a given key
    public V get(K key) {
        int bucketIndex = getBucketIndex(key);
        CustomLinkedList<Node<K, V>> bucket = buckets[bucketIndex];

        if (bucket != null) {
            // Iterate through the bucket to find the key
            for (int i = 0; i < bucket.size(); i++) {
                Node<K, V> node = bucket.get(i);
                if (Objects.equals(node.key, key)) {
                    return node.value; // Key found, return the value
                }
            }
        }

        return null; // Key not found
    }

    // REMOVE method: Remove a key-value pair by key
    public V remove(K key) {
        int bucketIndex = getBucketIndex(key);
        CustomLinkedList<Node<K, V>> bucket = buckets[bucketIndex];

        if (bucket != null) {
            for (int i = 0; i < bucket.size(); i++) {
                Node<K, V> node = bucket.get(i);
                if (Objects.equals(node.key, key)) {
                    bucket.remove(i); // Remove the node
                    size--;
                    return node.value; // Return the removed value
                }
            }
        }

        return null; // Key not found
    }

    // CONTAINS KEY method: Check if the given key exists in the map
    public boolean containsKey(K key) {
        int bucketIndex = getBucketIndex(key);
        CustomLinkedList<Node<K, V>> bucket = buckets[bucketIndex];

        if (bucket != null) {
            for (int i = 0; i < bucket.size(); i++) {
                Node<K, V> node = bucket.get(i);
                if (Objects.equals(node.key, key)) {
                    return true; // Key exists
                }
            }
        }

        return false; // Key not found
    }

    // RESIZE method: Resize the buckets array and rehash keys
    private void resize() {
        CustomLinkedList<Node<K, V>>[] oldBuckets = buckets;
        buckets = new CustomLinkedList[oldBuckets.length * 2];
        size = 0;

        // Rehash and reinsert all entries
        for (CustomLinkedList<Node<K, V>> bucket : oldBuckets) {
            if (bucket != null) {
                for (int i = 0; i < bucket.size(); i++) {
                    Node<K, V> node = bucket.get(i);
                    put(node.key, node.value);
                }
            }
        }
    }

    // SIZE method: Return the number of elements in the map
    public int size() {
        return size;
    }
}