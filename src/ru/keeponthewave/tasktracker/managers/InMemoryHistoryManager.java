package ru.keeponthewave.tasktracker.managers;

import ru.keeponthewave.tasktracker.model.Task;

import java.util.*;

public class InMemoryHistoryManager implements HistoryManager {
    private final Map<Integer, Node> nodeMap = new HashMap<>();

    private final Node tail = new Node();
    private final Node head = new Node();


    private static class Node {
        private Node next;
        private Node prev;
        private Task value;

        public Node() {}

        public Node(Node next, Node prev, Task value) {
            this.next = next;
            this.prev = prev;
            this.value = value;
        }
    }

    public InMemoryHistoryManager() {
        head.next = tail;
        tail.prev = head;
    }

    @Override
    public List<Task> getHistory() {
        List<Task> resultList = new ArrayList<>();

        for (var currentNode = head.next; currentNode != tail; currentNode = currentNode.next) {
            resultList.add(currentNode.value);
        }

        return resultList;
    }

    @Override
    public void remove(int id) {
        if (!nodeMap.containsKey(id)) {
            throw new NoSuchElementException();
        }

        var currentNode = nodeMap.get(id);
        var nextNode = currentNode.next;
        var prevNode = currentNode.prev;

        nextNode.prev = prevNode;
        prevNode.next = nextNode;

        nodeMap.remove(id);
    }

    @Override
    public void add(Task task) {
        int id = task.getId();

        if (nodeMap.containsKey(id)) {
            remove(id);
        }

        var newNode = new Node(tail, tail.prev, task);
        tail.prev.next = newNode;
        tail.prev = newNode;

        nodeMap.put(id, newNode);
    }
}
