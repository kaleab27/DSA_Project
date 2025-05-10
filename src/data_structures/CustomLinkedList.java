package data_structures;

public class CustomLinkedList<T> {
    // Node class for each element in the linked list
    private static class Node<T> {
        T data;      // Value of the node
        Node<T> next; // Reference to the next node

        Node(T data) {
            this.data = data;
            this.next = null;
        }
    }

    // Head and tail of the linked list
    private Node<T> head;
    private Node<T> tail;
    private int size;

    // Constructor for an empty linked list
    public CustomLinkedList() {
        this.head = null;
        this.tail = null;
        this.size = 0;
    }

    // Method to get the size of the linked list
    public int size() {
        return size;
    }

    // Method to check if the list is empty
    public boolean isEmpty() {
        return size == 0;
    }

    // Add element to the end of the list
    public void add(T data) {
        Node<T> newNode = new Node<>(data);
        if (head == null) {
            head = tail = newNode; // First element becomes head and tail
        } else {
            tail.next = newNode; // Add the new node after the current tail
            tail = newNode;      // Update tail reference
        }
        size++;
    }

    // Add element at a specific index
    public void add(int index, T data) {
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }

        Node<T> newNode = new Node<>(data);

        if (index == 0) { // Adding at the head
            newNode.next = head;
            head = newNode;
            if (size == 0) {
                tail = head;
            }
        } else { // Adding at any other position
            Node<T> prev = getNodeAt(index - 1);
            newNode.next = prev.next;
            prev.next = newNode;

            if (newNode.next == null) { // If added at the end, update tail
                tail = newNode;
            }
        }
        size++;
    }

    // Get element at a specific index
    public T get(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
        return getNodeAt(index).data;
    }

    // Helper method to traverse and get the node at a specific index
    private Node<T> getNodeAt(int index) {
        Node<T> current = head;
        for (int i = 0; i < index; i++) {
            current = current.next;
        }
        return current;
    }

    // Remove an element by index
    public T remove(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }

        Node<T> removed;
        if (index == 0) { // Remove head
            removed = head;
            head = head.next;
            if (head == null) { // If the list becomes empty, clear tail
                tail = null;
            }
        } else { // Remove any other node
            Node<T> prev = getNodeAt(index - 1);
            removed = prev.next;
            prev.next = removed.next;

            if (removed.next == null) { // If removing the last node, update tail
                tail = prev;
            }
        }
        size--;
        return removed.data;
    }

    // Remove an element by value
    public boolean remove(T data) {
        if (head == null) {
            return false;
        }

        if (head.data.equals(data)) { // Remove head node
            head = head.next;
            if (head == null) { // If the list becomes empty, clear tail
                tail = null;
            }
            size--;
            return true;
        }

        Node<T> current = head;
        while (current.next != null) {
            if (current.next.data.equals(data)) { // Remove matching node
                current.next = current.next.next;
                if (current.next == null) { // If removing the last node, update tail
                    tail = current;
                }
                size--;
                return true;
            }
            current = current.next;
        }

        return false; // Element not found
    }

    // Check if an element exists in the list
    public boolean contains(T data) {
        Node<T> current = head;
        while (current != null) {
            if (current.data.equals(data)) {
                return true;
            }
            current = current.next;
        }
        return false;
    }

    // Print the linked list for debugging
    public void printList() {
        Node<T> current = head;
        while (current != null) {
            System.out.print(current.data + " -> ");
            current = current.next;
        }
        System.out.println("null");
    }
}