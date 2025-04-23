package controllers;

import java.util.Objects;

class Node<Task> {

    private Task data;
    private Node<Task> next;
    private Node<Task> prev;

    public Node(Node<Task> prev, Task data, Node<Task> next) {
        this.data = data;
        this.next = next;
        this.prev = prev;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node<?> node = (Node<?>) o;
        return Objects.equals(data, node.data) && Objects.equals(next, node.next) && Objects.equals(prev, node.prev);
    }

    @Override
    public int hashCode() {
        return Objects.hash(data, next, prev);
    }

    public Task getData() {
        return data;
    }

    public Node<Task> getNext() {
        return next;
    }

    public Node<Task> getPrev() {
        return prev;
    }

    void setNext(Node<Task> next) {
        this.next = next;
    }

    void setPrev(Node<Task> prev) {
        this.prev = prev;
    }
}