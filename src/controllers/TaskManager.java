package controllers;

import model.Epic;
import model.Subtask;
import model.Task;
import util.TaskType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public interface TaskManager {

    List<Task> getHistory();


    int generateId();

    // a. Получение списка всех задач. Task
    ArrayList<Task> getTasks();

    // a. Получение списка всех задач. Epic
    ArrayList<Epic> getEpics();

    // a. Получение списка всех задач. Subtask
    ArrayList<Subtask> getSubtasks();

    //b. Удаление всех задач.
    void removeAllTasks(TaskType type);

    //c. Получение по идентификатору.
    Task getTaskById(int taskId);

    //c. Получение по идентификатору.
    Epic getEpicById(int taskId);

    //c. Получение по идентификатору.
    Subtask getSubtaskById(int taskId);

    // d. Создание. Сам объект должен передаваться в качестве параметра.
    int addTask(Task newTask);

    // d. Создание. Сам объект должен передаваться в качестве параметра.
    int addEpic(Epic newEpic);

    // d. Создание. Сам объект должен передаваться в качестве параметра.
    int addSubtask(Subtask newSubtask);

    //e. Обновление. Новая версия объекта с верным идентификатором передаётся в виде параметра.
    void updateTask(Task updTask);

    //e. Обновление. Новая версия объекта с верным идентификатором передаётся в виде параметра.
    void updateSubtask(Subtask updSubtask);

    //e. Обновление. Новая версия объекта с верным идентификатором передаётся в виде параметра.
    void updateEpic(Epic updEpic);

    // f. Удаление по идентификатору.
    void removeTaskById(int taskId);

    //a. Получение списка всех подзадач определённого эпика.
    ArrayList<Integer> getSubtasksList(int taskID);

    //b. Для эпиков: Управление статусами
    void updEpicStatus(Epic epic);

    HashMap<Integer,Subtask> getSubtasksMap();
}
