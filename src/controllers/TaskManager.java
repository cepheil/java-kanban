package controllers;

import model.Epic;
import model.Subtask;
import model.Task;
import util.TaskStatus;
import util.TaskType;

import java.util.ArrayList;
import java.util.HashMap;

public class TaskManager {
    private HashMap<Integer, Task> tasks = new HashMap<>();
    private HashMap<Integer, Epic> epics = new HashMap<>();
    private HashMap<Integer, Subtask> subtasks = new HashMap<>();
    private static int counter = 0;


    public int generateId() {
        int taskId = ++counter;
        return taskId;
    }

    // a. Получение списка всех задач. Task
    public ArrayList<Task> getTasks() {
        ArrayList<Task> tasksList = new ArrayList<>(tasks.values());
        return tasksList;
    }

    // a. Получение списка всех задач. Epic
    public ArrayList<Epic> getEpics() {
        ArrayList<Epic> epicsList = new ArrayList<>(epics.values());
        return epicsList;
    }

    // a. Получение списка всех задач. Subtask
    public ArrayList<Subtask> getSubtasks() {
        ArrayList<Subtask> subtasksList = new ArrayList<>(subtasks.values());
        return subtasksList;
    }

    //b. Удаление всех задач.
    public void removeAllTasks(TaskType type) {
        switch (type) {
            case TASK:
                tasks.clear();
                break;
            case EPIC:
                subtasks.clear();
                epics.clear();
                break;
            case SUBTASK:
                for (Epic epic : epics.values()) {
                    epic.clearSubtaskIdList();
                    updEpicStatus(epic);
                }
                subtasks.clear();
                break;
        }
    }

    //c. Получение по идентификатору.
    public Task getTaskById(int taskId) { // в вызывающем коде требуется обработка на null
        return tasks.get(taskId);
    }

    //c. Получение по идентификатору.
    public Epic getEpicById(int taskId) { // в вызывающем коде требуется обработка на null
        return epics.get(taskId);
    }

    //c. Получение по идентификатору.
    public Subtask getSubtaskById(int taskId) { // в вызывающем коде требуется обработка на null
        return subtasks.get(taskId);
    }


    // d. Создание. Сам объект должен передаваться в качестве параметра.
    public int addTask(Task newTask) {
        final int id = generateId();
        newTask.setTaskID(id);
        tasks.put(id, newTask);
        return id;
    }

    // d. Создание. Сам объект должен передаваться в качестве параметра.
    public int addEpic(Epic newEpic) {
        final int id = generateId();
        newEpic.setTaskID(id);
        epics.put(id, newEpic);
        return id;
    }

    // d. Создание. Сам объект должен передаваться в качестве параметра.
    public int addSubtask(Subtask newSubtask) {
        final int id = generateId();
        newSubtask.setTaskID(id);
        subtasks.put(id, newSubtask);
        epics.get(newSubtask.getEpicID()).setSubtaskIdList(id);// Добавляем номер СТ в лист к эпику
        updEpicStatus(epics.get(newSubtask.getEpicID()));
        return id;
    }

    //e. Обновление. Новая версия объекта с верным идентификатором передаётся в виде параметра.
    public void updateTask(Task updTask) {  //
        int key = updTask.getTaskID();
        tasks.get(key).setName(updTask.getName());
        tasks.get(key).setDescription(updTask.getDescription());
        tasks.get(key).setStatus(updTask.getStatus());
    }

    //e. Обновление. Новая версия объекта с верным идентификатором передаётся в виде параметра.
    public void updateSubtask(Subtask updSubtask) {
        int key = updSubtask.getTaskID();
        subtasks.get(key).setName(updSubtask.getName());
        subtasks.get(key).setDescription(updSubtask.getDescription());
        subtasks.get(key).setStatus(updSubtask.getStatus());
        updEpicStatus(epics.get(updSubtask.getEpicID()));
    }

    //e. Обновление. Новая версия объекта с верным идентификатором передаётся в виде параметра.
    public void updateEpic(Epic updEpic) {
        int key = updEpic.getTaskID();
        epics.get(key).setName(updEpic.getName());
        epics.get(key).setDescription(updEpic.getDescription());
    }

    // f. Удаление по идентификатору.
    public void removeTaskById(int taskId) {
        if (tasks.containsKey(taskId)) {
            tasks.remove(taskId);
            return;
        }

        if (subtasks.containsKey(taskId)) {
            int epicId = subtasks.get(taskId).getEpicID();
            subtasks.remove(taskId);
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
            }
            epics.remove(taskId);
        }
    }

    //a. Получение списка всех подзадач определённого эпика.
    public ArrayList<Integer> getSubtasksList(int taskID) {
        if (!epics.containsKey(taskID)) {
            return new ArrayList<>();  // возвращаем пустой список.
        }
        Epic epic = epics.get(taskID);
        ArrayList<Integer> subtasksList = epic.getSubtaskIdList();
        return subtasksList;
    }

    //b. Для эпиков: Управление статусами
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
