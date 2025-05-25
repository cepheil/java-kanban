package controllers;

import exceptions.EpicNotFoundException;
import exceptions.IntersectTaskException;
import exceptions.InvalidSubtaskException;
import model.Epic;
import model.Subtask;
import model.Task;
import util.CustomFormatter;
import util.StartTimeComparator;
import util.TaskStatus;
import util.TaskType;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

public class InMemoryTaskManager implements TaskManager {
    protected HashMap<Integer, Task> tasks = new HashMap<>();
    protected HashMap<Integer, Epic> epics = new HashMap<>();
    protected HashMap<Integer, Subtask> subtasks = new HashMap<>();
    protected int counter = 0;
    private final HistoryManager historyManager;
    private final TreeSet<Task> prioritizedTasks = new TreeSet<>(new StartTimeComparator());
    protected CustomFormatter customFormatter = new CustomFormatter();

    public InMemoryTaskManager(HistoryManager historyManager) {
        this.historyManager = historyManager;
    }

    protected void setCounter(int newValue) {
        this.counter = newValue;
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    @Override
    public int generateId() {
        int taskId = ++counter;
        return taskId;
    }

    // a. Получение списка всех задач. Task
    @Override
    public ArrayList<Task> getTasks() {
        ArrayList<Task> tasksList = new ArrayList<>(tasks.values());
        return tasksList;
    }

    // a. Получение списка всех задач. Epic
    @Override
    public ArrayList<Epic> getEpics() {
        ArrayList<Epic> epicsList = new ArrayList<>(epics.values());
        return epicsList;
    }

    // a. Получение списка всех задач. Subtask
    @Override
    public ArrayList<Subtask> getSubtasks() {
        ArrayList<Subtask> subtasksList = new ArrayList<>(subtasks.values());
        return subtasksList;
    }

    @Override
    public HashMap<Integer, Subtask> getSubtasksMap() {
        HashMap<Integer, Subtask> subtasksMap = new HashMap<>(subtasks);
        return subtasksMap;
    }

    //b. Удаление всех задач.
    @Override
    public void removeAllTasks(TaskType type) {
        switch (type) {
            case TASK:
                Set<Integer> tasksIdSet = tasks.keySet();
                for (Integer id : tasksIdSet) {
                    Task task = tasks.get(id);
                    if (task.getStartTime() != null) {
                        prioritizedTasks.remove(task);
                    }
                    historyManager.remove(id);
                }
                tasks.clear();
                break;
            case EPIC:
                Set<Integer> subtaskIdSet = subtasks.keySet();
                for (Integer id : subtaskIdSet) {
                    Subtask subtask = subtasks.get(id);
                    if (subtask.getStartTime() != null) {
                        prioritizedTasks.remove(subtask);
                    }
                }
                Set<Integer> idsSet = new HashSet<>(subtaskIdSet);
                idsSet.addAll(epics.keySet());
                for (Integer id : idsSet) {
                    historyManager.remove(id);
                }
                subtasks.clear();
                epics.clear();
                break;
            case SUBTASK:
                for (Epic epic : epics.values()) {
                    epic.clearSubtaskIdList();
                    updEpicStatus(epic);  // Обновление статуса эпика.
                    updateEpicTime(epic);  // Обновление времени эпика.
                }
                Set<Integer> subtasksIdSet = subtasks.keySet();
                for (Integer id : subtasksIdSet) {
                    Subtask subtask = subtasks.get(id);
                    if (subtask.getStartTime() != null) {
                        prioritizedTasks.remove(subtask);
                    }
                    historyManager.remove(id);
                }
                subtasks.clear();
                break;
        }
    }

    //  Optional
    //c. Получение по идентификатору.
    @Override
    public Task getTaskById(int taskId) {
        Task task = tasks.get(taskId);
        if (task == null) {
            return null;
        }
        historyManager.add(task);
        Task copyTask = new Task(task);
        return copyTask;
    }

    //c. Получение по идентификатору.
    @Override
    public Epic getEpicById(int taskId) {
        Epic epic = epics.get(taskId);
        if (epic == null) {
            return null;
        }
        historyManager.add(epic);
        Epic copyEpic = new Epic(epic);
        return copyEpic;
    }

    //c. Получение по идентификатору.
    @Override
    public Subtask getSubtaskById(int taskId) {
        Subtask subtask = subtasks.get(taskId);
        if (subtask == null) {
            return null;
        }
        historyManager.add(subtask);
        Subtask copySubtask = new Subtask(subtask);
        return copySubtask;
    }

    // d. Создание. Сам объект должен передаваться в качестве параметра.
    @Override
    public int addTask(Task newTask) {
        if (newTask.getStartTime() != null && newTask.getDuration() != null && isIntersect(newTask)) {
            throw new IntersectTaskException("Задачи пересекаются по времени " +
                    newTask.getStartTime().format(customFormatter.getFormatter()));
        }
        final int id = generateId();
        newTask.setTaskID(id);
        tasks.put(id, new Task(newTask));
        if (newTask.getStartTime() != null && newTask.getDuration() != null) {
            prioritizedTasks.add(new Task(newTask));
        }
        return id;
    }

    // d. Создание. Сам объект должен передаваться в качестве параметра.
    @Override
    public int addEpic(Epic newEpic) {
        final int id = generateId();
        newEpic.setTaskID(id);
        epics.put(id, newEpic);
        return id;
    }

    // d. Создание. Сам объект должен передаваться в качестве параметра.
    @Override
    public int addSubtask(Subtask newSubtask) {
        if (!(epics.containsKey(newSubtask.getEpicID()))) {
            throw new EpicNotFoundException("Epic с ID " + newSubtask.getEpicID() + " не существует.");
        }
        if (newSubtask.getEpicID() == newSubtask.getTaskID()) {
            throw new InvalidSubtaskException("Подзадача не может быть своим собственным эпиком.");
        }

        if (newSubtask.getStartTime() != null && newSubtask.getDuration() != null && isIntersect(newSubtask)) {
            throw new IntersectTaskException("Задачи пересекаются по времени " +
                    newSubtask.getStartTime().format(customFormatter.getFormatter()));
        }
        final int id = generateId();
        newSubtask.setTaskID(id);
        subtasks.put(id, new Subtask(newSubtask));
        Epic epic = epics.get(newSubtask.getEpicID());
        epic.setSubtaskIdList(id);  // Добавляем номер СТ в лист к эпику
        updEpicStatus(epic);        // Обновление статуса эпика.
        if (newSubtask.getStartTime() != null && newSubtask.getDuration() != null) {
            prioritizedTasks.add(new Subtask(newSubtask));
            updateEpicTime(epic);       // Обновление времени эпика.
        }
        return id;
    }

    //e. Обновление. Новая версия объекта с верным идентификатором передаётся в виде параметра.
    @Override
    public void updateTask(Task updTask) {
        int key = updTask.getTaskID();
        Task oldTask = tasks.get(key);
        if (oldTask.getStartTime() != null && oldTask.getDuration() != null) {
            prioritizedTasks.remove(oldTask);
        }
        if (updTask.getStartTime() != null && updTask.getDuration() != null && isIntersect(updTask)) {
            throw new IntersectTaskException("Задачи пересекаются по времени " +
                    updTask.getStartTime().format(customFormatter.getFormatter()));
        }
        tasks.get(key).setName(updTask.getName());
        tasks.get(key).setDescription(updTask.getDescription());
        tasks.get(key).setStatus(updTask.getStatus());

        if (updTask.getStartTime() != null && updTask.getDuration() != null) {
            prioritizedTasks.add(new Task(updTask));
        }
    }

    //e. Обновление. Новая версия объекта с верным идентификатором передаётся в виде параметра.
    @Override
    public void updateSubtask(Subtask updSubtask) {
        int key = updSubtask.getTaskID();
        Subtask oldSubtask = subtasks.get(key);
        if (oldSubtask.getStartTime() != null && oldSubtask.getDuration() != null) {
            prioritizedTasks.remove(oldSubtask);
        }

        if (updSubtask.getStartTime() != null && updSubtask.getDuration() != null && isIntersect(updSubtask)) {
            throw new IntersectTaskException("Задачи пересекаются по времени " +
                    updSubtask.getStartTime().format(customFormatter.getFormatter()));
        }

        subtasks.get(key).setName(updSubtask.getName());
        subtasks.get(key).setDescription(updSubtask.getDescription());
        subtasks.get(key).setStatus(updSubtask.getStatus());
        updEpicStatus(epics.get(updSubtask.getEpicID())); // Обновление статуса эпика.

        if (updSubtask.getStartTime() != null && updSubtask.getDuration() != null) {
            prioritizedTasks.add(new Subtask(updSubtask));
            updateEpicTime(epics.get(updSubtask.getEpicID()));  // Обновление времени эпика.
        }

    }

    //e. Обновление. Новая версия объекта с верным идентификатором передаётся в виде параметра.
    @Override
    public void updateEpic(Epic updEpic) {
        int key = updEpic.getTaskID();
        epics.get(key).setName(updEpic.getName());
        epics.get(key).setDescription(updEpic.getDescription());
    }

    // f. Удаление по идентификатору.
    @Override
    public void removeTaskById(int taskId) {
        if (tasks.containsKey(taskId)) {
            Task task = tasks.get(taskId);
            if (task.getStartTime() != null && task.getDuration() != null) {
                prioritizedTasks.remove(task);
            }
            tasks.remove(taskId);
            historyManager.remove(taskId);
            return;
        }
        if (subtasks.containsKey(taskId)) {
            Subtask subtask = subtasks.get(taskId);
            if (subtask.getStartTime() != null && subtask.getDuration() != null) {
                prioritizedTasks.remove(subtask);
            }
            int epicId = subtask.getEpicID();
            subtasks.remove(taskId);
            historyManager.remove(taskId);
            Epic epic = epics.get(epicId);

            if (epic != null) {
                epic.removeSubtaskId(taskId);
                updEpicStatus(epic);   // Обновление статуса эпика.
                updateEpicTime(epic);  // Обновление времени эпика.
            }
            return;
        }
        if (epics.containsKey(taskId)) {
            Epic epic = epics.get(taskId);
            ArrayList<Integer> subtaskIdList = new ArrayList<>(epic.getSubtaskIdList());
            for (Integer id : subtaskIdList) {
                Subtask sbt = subtasks.get(id);
                if (sbt.getStartTime() != null && sbt.getDuration() != null) {
                    prioritizedTasks.remove(sbt);
                }
                subtasks.remove(id);
                historyManager.remove(id);
            }
            epics.remove(taskId);
            historyManager.remove(taskId);
        }
    }

    //a. Получение списка всех подзадач определённого эпика.
    @Override
    public ArrayList<Integer> getSubtasksList(int taskID) {
        if (!epics.containsKey(taskID)) {
            return new ArrayList<>();  // возвращаем пустой список.
        }
        Epic epic = epics.get(taskID);
        ArrayList<Integer> subtasksList = epic.getSubtaskIdList();
        return subtasksList;
    }

    //b. Для эпиков: Управление статусами
    @Override
    public void updEpicStatus(Epic epic) {
        ArrayList<Integer> subtaskIdList = epic.getSubtaskIdList();
        if (subtaskIdList == null || subtaskIdList.isEmpty()) {
            epic.setStatus(TaskStatus.NEW);
            return;
        }
        List<Subtask> subtasksList = subtaskIdList.stream()
                .map(id -> subtasks.get(id))
                .collect(Collectors.toList());

        epic.setStatus(epic.updateStatus(subtasksList));
    }

    @Override
    public void updateEpicTime(Epic epic) {
        ArrayList<Integer> subtaskIdList = epic.getSubtaskIdList();

        if (subtaskIdList == null || subtaskIdList.isEmpty()) {
            epic.setStartTime(null);
            epic.setEndTime(null);
            epic.setDuration(Duration.ZERO);
            return;
        }
        List<Subtask> subtasksList = subtaskIdList.stream()
                .map(id -> subtasks.get(id))
                .collect(Collectors.toList());

        epic.setStartTime(epic.getStartTime(subtasksList));
        epic.setEndTime(epic.getEndTime(subtasksList));
        epic.setDuration(epic.getDuration(subtasksList));
    }

    @Override
    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedTasks);
    }

    @Override
    public boolean isIntersect(Task task) {
        if (task.getStartTime() == null || task.getEndTime() == null) {
            return false;
        }
        return prioritizedTasks.stream()
                .anyMatch(t -> !(task.getEndTime().isBefore(t.getStartTime()) ||
                        task.getStartTime().isAfter(t.getEndTime())));
    }

}
