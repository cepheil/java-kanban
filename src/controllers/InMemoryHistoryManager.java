package controllers;

import model.Epic;
import model.Subtask;
import model.Task;
import util.TaskType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Реализация {@link HistoryManager}, хранящая историю просмотров задач.
 * Использует двусвязный список для сохранения порядка и {@link HashMap} для быстрого доступа к узлам.
 * При добавлении задачи создается её копия, чтобы зафиксировать состояние на момент просмотра.
 */

public class InMemoryHistoryManager implements HistoryManager {

    private HashMap<Integer, Node> historyMap = new HashMap<>();
    private Node head;
    private Node tail;


    private void linkLast(Task task) {
        final Node oldTail = tail;
        final Node newNode = new Node(tail, task, null);
        tail = newNode;
        if (oldTail == null)
            this.head = newNode;
        else {
            oldTail.setNext(newNode);
        }
    }

    private List<Task> getTasks() {
        List<Task> result = new ArrayList<>(historyMap.size());
        Node current = head;
        while (current != null) {
            result.add(current.getData());
            current = current.getNext();
        }
        return result;
    }

    private void removeNode(Node node) {
        if (node == null) return;
        Node prev = node.getPrev();
        Node next = node.getNext();

        if (prev == null) {
            head = next;
        } else {
            prev.setNext(next);
        }
        if (next == null) {
            tail = prev;
        } else {
            next.setPrev(prev);
        }
    }

    /**
     * Добавляет копию задачи в историю просмотров.
     * Если задача уже присутствует в истории, её предыдущая версия удаляется, новая версия добавляется в конец.
     *
     * @param task Задача для добавления. Не может быть {@code null}.
     */
    @Override
    public void add(Task task) {
        Task taskCopy = copyTask(task);
        int id = taskCopy.getTaskID();

        remove(id);
        linkLast(taskCopy);
        Node newNode = tail;
        historyMap.put(id, newNode);
    }

    @Override
    public List<Task> getHistory() {
        return getTasks();
    }

    @Override
    public void remove(int id) {
        if (historyMap.containsKey(id)) {
            Node node = historyMap.get(id);
            removeNode(node);
            historyMap.remove(id);
        }
    }

    private Task copyTask(Task task) {
        TaskType type = task.getType();
        if (type == TaskType.EPIC) {
            return new Epic((Epic) task);
        } else if (type == TaskType.SUBTASK) {
            return new Subtask((Subtask) task);
        } else {
            return new Task(task);
        }
    }
}
