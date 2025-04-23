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

    /**
     * Создает глубокую копию задачи, чтобы зафиксировать её состояние на момент добавления в историю.
     *
     * @param task Исходная задача для копирования.
     * @return Копия задачи.
     */
    private Task copyTask(Task task) {
        TaskType type = task.getType();
        if (type == TaskType.EPIC) {
            Epic epic = (Epic) task;
            Epic copyEpic = new Epic(epic.getName(), epic.getDescription());
            copyEpic.setTaskID(epic.getTaskID());
            copyEpic.setStatus(epic.getStatus());
            if (epic.getSubtaskIdList() != null) {
                copyEpic.setSubtaskIdList(new ArrayList<>(epic.getSubtaskIdList()));
            } else {
                copyEpic.setSubtaskIdList(new ArrayList<>());
            }
            return copyEpic;
        } else if (type == TaskType.SUBTASK) {
            Subtask subtask = (Subtask) task;
            Subtask copySubtask = new Subtask(subtask.getName(), subtask.getDescription(), subtask.getStatus(),
                    subtask.getEpicID());
            copySubtask.setTaskID(subtask.getTaskID());
            return copySubtask;
        } else {
            Task copyTask = new Task(task.getName(), task.getDescription(), task.getStatus());
            copyTask.setTaskID(task.getTaskID());
            return copyTask;
        }
    }
}
