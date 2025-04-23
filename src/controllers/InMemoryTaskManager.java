package controllers;

import model.Epic;
import model.Subtask;
import model.Task;
import util.TaskStatus;
import util.TaskType;

import java.util.*;

public class InMemoryTaskManager implements TaskManager {
    private HashMap<Integer, Task> tasks = new HashMap<>();
    private HashMap<Integer, Epic> epics = new HashMap<>();
    private HashMap<Integer, Subtask> subtasks = new HashMap<>();
    private static int counter = 0;
    private final HistoryManager historyManager;

    public InMemoryTaskManager(HistoryManager historyManager) {
        this.historyManager = historyManager;
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
                    historyManager.remove(id);
                }
                tasks.clear();
                break;
            case EPIC:
                Set<Integer> idsSet = new HashSet<>(subtasks.keySet());
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
                    updEpicStatus(epic);
                }
                Set<Integer> subtasksIdSet = subtasks.keySet();
                for (Integer id : subtasksIdSet) {
                    historyManager.remove(id);
                }
                subtasks.clear();
                break;
        }
    }

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
        final int id = generateId();
        newTask.setTaskID(id);
        tasks.put(id, newTask);
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
        final int id = generateId();
        newSubtask.setTaskID(id);
        if (!(epics.containsKey(newSubtask.getEpicID()))) {
            return -3; // Ошибка эпика с таким EpicID не существует.
        }

        if (newSubtask.getEpicID() == newSubtask.getTaskID()) {
            return -2;  //ошибка подзадача не может стать своим эпиком.
        }
        subtasks.put(id, newSubtask);
        epics.get(newSubtask.getEpicID()).setSubtaskIdList(id);// Добавляем номер СТ в лист к эпику
        updEpicStatus(epics.get(newSubtask.getEpicID()));
        return id;
    }

    //e. Обновление. Новая версия объекта с верным идентификатором передаётся в виде параметра.
    @Override
    public void updateTask(Task updTask) {  //
        int key = updTask.getTaskID();
        tasks.get(key).setName(updTask.getName());
        tasks.get(key).setDescription(updTask.getDescription());
        tasks.get(key).setStatus(updTask.getStatus());
    }

    //e. Обновление. Новая версия объекта с верным идентификатором передаётся в виде параметра.
    @Override
    public void updateSubtask(Subtask updSubtask) {
        int key = updSubtask.getTaskID();
        subtasks.get(key).setName(updSubtask.getName());
        subtasks.get(key).setDescription(updSubtask.getDescription());
        subtasks.get(key).setStatus(updSubtask.getStatus());
        updEpicStatus(epics.get(updSubtask.getEpicID()));
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
            tasks.remove(taskId);
            historyManager.remove(taskId);
            return;
        }

        if (subtasks.containsKey(taskId)) {
            int epicId = subtasks.get(taskId).getEpicID();
            subtasks.remove(taskId);
            historyManager.remove(taskId);
            Epic epic = epics.get(epicId);

            if (epic != null) {
                epic.removeSubtaskId(taskId);
                updEpicStatus(epic);
            }
            return;
        }

        if (epics.containsKey(taskId)) {
            Epic epic = epics.get(taskId);
            ArrayList<Integer> subtaskIdList = new ArrayList<>(epic.getSubtaskIdList()); // получаем ID подзадач Эпика
            for (Integer subtaskId : subtaskIdList) {
                subtasks.remove(subtaskId);
                historyManager.remove(subtaskId);
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
        int newCounter = 0;
        int doneCounter = 0;
        for (Integer subtaskId : subtaskIdList) {
            Subtask subtask = subtasks.get(subtaskId);
            if (subtask == null) {
                continue;
            }
            TaskStatus subTaskStatus = subtask.getStatus();
            if (subTaskStatus.equals(TaskStatus.NEW)) {
                newCounter++;
            } else if (subTaskStatus.equals(TaskStatus.DONE)) {
                doneCounter++;
            }
        }
        TaskStatus result;
        if (newCounter == subtaskIdList.size()) {
            result = TaskStatus.NEW;
        } else if (doneCounter == subtaskIdList.size()) {
            result = TaskStatus.DONE;
        } else {
            result = TaskStatus.IN_PROGRESS;
        }
        epic.setStatus(result);
    }

}
