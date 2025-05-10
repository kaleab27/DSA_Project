package data_structures;

/**
 * A custom implementation of a singly linked list.
 *
 * @param <T> The type of elements to be stored in the linked list.
 */

public class CustomLinkedList<T> {

    /**
     * A static nested class representing a node in the linked list.
     *
     * @param <T> The type of data stored in the node.
     */
    private static class Node<T> {
        T data;
        Node<T> next;

        /**
         * Constructs a new node with the given data.
         *
         * @param data The data to be stored in the node.
         */
        Node(T data) {
            this.data = data;
            this.next = null;
        }
    }

    private Node<T> head;
    private Node<T> tail;
    private int size;

    /**
     * Constructs an empty linked list.
     */
    public CustomLinkedList() {
        this.head = null;
        this.tail = null;
        this.size = 0;
    }

    /**
     * Gets the current size of the linked list.
     *
     * @return The number of elements in the linked list.
     */
    public int size() {
        return size;
    }

    /**
     * Checks if the linked list is empty.
     *
     * @return {@code true} if the linked list is empty, {@code false} otherwise.
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Adds a new element to the end of the linked list.
     *
     * @param data The data to be added.
     */
    public void add(T data) {
        Node<T> newNode = new Node<>(data);
        if (head == null) {
            head = tail = newNode;
        } else {
            tail.next = newNode;
            tail = newNode;
        }
        size++;
    }

    /**
     * Adds a new element at a specific index in the linked list.
     * Shifts the element currently at that position, if any, and any subsequent elements to the right.
     *
     * @param index The index at which the element should be inserted (0-based).
     * @param data  The data to be added.
     * @throws IndexOutOfBoundsException if the index is out of range (index < 0 || index > size).
     */
    public void add(int index, T data) {
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }

        Node<T> newNode = new Node<>(data);

        if (index == 0) {
            newNode.next = head;
            head = newNode;
            if (size == 0) {
                tail = head;
            }
        } else {
            Node<T> prev = getNodeAt(index - 1);
            newNode.next = prev.next;
            prev.next = newNode;

            if (newNode.next == null) {
                tail = newNode;
            }
        }
        size++;
    }

    /**
     * Retrieves the element at the specified index in the linked list.
     *
     * @param index The index of the element to be retrieved (0-based).
     * @return The element at the specified index.
     * @throws IndexOutOfBoundsException if the index is out of range (index < 0 || index >= size).
     */
    public T get(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
        return getNodeAt(index).data;
    }

    /**
     * Retrieves the node at the specified index.
     *
     * @param index The index of the node to retrieve (0-based).
     * @return The node at the specified index.
     */
    private Node<T> getNodeAt(int index) {
        Node<T> current = head;
        for (int i = 0; i < index; i++) {
            current = current.next;
        }
        return current;
    }

    /**
     * Removes the element at the specified index in the linked list.
     * Shifts any subsequent elements to the left (subtracts one from their indices).
     *
     * @param index The index of the element to be removed (0-based).
     * @return The data of the removed element.
     * @throws IndexOutOfBoundsException if the index is out of range (index < 0 || index >= size).
     */
    public T remove(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }

        Node<T> removed;
        if (index == 0) {
            removed = head;
            head = head.next;
            if (head == null) {
                tail = null;
            }
        } else {
            Node<T> prev = getNodeAt(index - 1);
            removed = prev.next;
            prev.next = removed.next;

            if (removed.next == null) {
                tail = prev;
            }
        }
        size--;
        return removed.data;
    }

    /**
     * Removes the first occurrence of the specified element from the linked list, if it exists.
     *
     * @param data The element to be removed.
     * @return {@code true} if the element was found and removed, {@code false} otherwise.
     */
    public boolean remove(T data) {
        if (head == null) {
            return false;
        }

        if (head.data.equals(data)) {
            head = head.next;
            if (head == null) {
                tail = null;
            }
            size--;
            return true;
        }

        Node<T> current = head;
        while (current.next != null) {
            if (current.next.data.equals(data)) {
                current.next = current.next.next;
                if (current.next == null) {
                    tail = current;
                }
                size--;
                return true;
            }
            current = current.next;
        }

        return false;
    }

    /**
     * Checks if the linked list contains the specified element.
     *
     * @param data The element to search for.
     * @return {@code true} if the element is found, {@code false} otherwise.
     */
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

    /**
     * Prints the elements of the linked list in order, separated by an arrow (" -> ").
     * Prints "null" at the end to indicate the end of the list.
     * Example: A -> B -> C -> null
     */
    public void printList() {
        Node<T> current = head;
        while (current != null) {
            System.out.print(current.data + " -> ");
            current = current.next;
        }
        System.out.println("null");
    }
}