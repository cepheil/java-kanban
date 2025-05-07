package controllers;

import model.Epic;
import model.Subtask;
import model.Task;
import util.TaskStatus;
import util.TaskType;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;


public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File file;

    public FileBackedTaskManager(HistoryManager historyManager, File file) {
        super(historyManager);
        this.file = file;
    }


    @Override
    public void removeAllTasks(TaskType type) {
        super.removeAllTasks(type);
        save();
    }

    @Override
    public int addTask(Task newTask) {
        super.addTask(newTask);
        save();
        return newTask.getTaskID();
    }

    @Override
    public int addEpic(Epic newEpic) {
        super.addEpic(newEpic);
        save();
        return newEpic.getTaskID();
    }

    @Override
    public int addSubtask(Subtask newSubtask) {
        super.addSubtask(newSubtask);
        save();
        return newSubtask.getTaskID();
    }

    @Override
    public void updateTask(Task updTask) {
        super.updateTask(updTask);
        save();
    }

    @Override
    public void updateSubtask(Subtask updSubtask) {
        super.updateSubtask(updSubtask);
        save();
    }

    @Override
    public void updateEpic(Epic updEpic) {
        super.updateEpic(updEpic);
        save();
    }

    @Override
    public void removeTaskById(int taskId) {
        super.removeTaskById(taskId);
        save();
    }

    public void save() {
        Map<Integer, Task> tasksToSave = new TreeMap<>();
        tasksToSave.putAll(tasks);
        tasksToSave.putAll(epics);
        tasksToSave.putAll(subtasks);

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            String headLine = "id,type,name,status,description,epicID\n";
            bw.write(headLine);
            for (Task task : tasksToSave.values()) {
                String line = task.toCsv();
                bw.write(line);
            }

        } catch (IOException exp) {
            throw new ManagerSaveException("Ошибка при сохранении", exp);
        }
    }

    public Task fromString(String value) {
        try {
            String[] split = value.split(",");
            if (split.length < 5) {
                throw new IllegalArgumentException("Недостаточно данных в строке: " + value);
            }
            int taskId = Integer.parseInt(split[0].trim());
            TaskType type = TaskType.valueOf(split[1].trim());
            String name = split[2];
            TaskStatus status = TaskStatus.valueOf(split[3].trim());
            String description = split[4];

            switch (type) {
                case TASK:
                    Task task = new Task(name, description, status);
                    task.setTaskID(taskId);
                    return task;
                case EPIC:
                    Epic epic = new Epic(name, description);
                    epic.setTaskID(taskId);
                    epic.setStatus(status);
                    return epic;
                case SUBTASK:
                    if (split.length < 6 || split[5].isBlank()) {
                        throw new IllegalArgumentException("Subtask без epicId: " + value);
                    }
                    int epicId = Integer.parseInt(split[5].trim());
                    Subtask subtask = new Subtask(name, description, status, epicId);
                    subtask.setTaskID(taskId);
                    return subtask;
                default:
                    throw new IllegalStateException("Неизвестный тип задачи: " + type);
            }
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new IllegalArgumentException("Ошибка при разборе строки: " + value, e);
        }
    }

}
