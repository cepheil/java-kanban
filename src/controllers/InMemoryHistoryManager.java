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

    private HashMap<Integer, Node<Task>> historyMap = new HashMap<>();
    private final HistoryManagerLinkList<Task> historyLinkList = new HistoryManagerLinkList<>();


    /**
     * Вложенный класс для управления двусвязным списком задач.
     * Хранит ссылки на первый ({@code head}) и последний ({@code tail}) элементы списка,
     * а также текущий размер списка ({@code size}).
     */
    private class HistoryManagerLinkList<T> {
        private Node<T> head;
        private Node<T> tail;
        private int size = 0;

        public void linkLast(T task) {
            final Node<T> oldTail = tail;
            final Node<T> newNode = new Node<>(tail, task, null);
            tail = newNode;
            if (oldTail == null)
                this.head = newNode;
            else {
                oldTail.setNext(newNode);
            }
            this.size++;
        }

        public List<T> getTasks() {
            List<T> result = new ArrayList<>(size);
            Node<T> current = head;
            while (current != null) {
                result.add(current.getData());
                current = current.getNext();
            }
            return result;
        }

        public Node<T> getHead() {
            return head;
        }

        public Node<T> getTail() {
            return tail;
        }

        public int getSize() {
            return size;
        }

        private void setHead(Node<T> head) {
            this.head = head;
        }

        private void setTail(Node<T> tail) {
            this.tail = tail;
        }

        private void setSize(int size) {
            this.size = size;
        }
    }

    /**
     * Удаляет узел из двусвязного списка.
     * Обновляет ссылки соседних узлов ({@code prev} и {@code next}).
     *
     * @param node Узел для удаления. Если {@code null}, метод завершается без изменений.
     */
    private void removeNode(Node<Task> node) {
        if (node == null) return;
        Node<Task> prev = node.getPrev();
        Node<Task> next = node.getNext();

        if (prev == null) {
            historyLinkList.setHead(next);
        } else {
            prev.setNext(next);
        }
        if (next == null) {
            historyLinkList.setTail(prev);
        } else {
            next.setPrev(prev);
        }
        historyLinkList.setSize(historyLinkList.getSize() - 1);
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

        historyLinkList.linkLast(taskCopy);
        Node<Task> newNode = historyLinkList.getTail();
        historyMap.put(id, newNode);

    }

    @Override
    public List<Task> getHistory() {
        return historyLinkList.getTasks();
    }

    @Override
    public void remove(int id) {
        if (historyMap.containsKey(id)) {
            Node<Task> node = historyMap.get(id);
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
