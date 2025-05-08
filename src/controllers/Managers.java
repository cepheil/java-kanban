package controllers;

import exceptions.ManagerSaveException;
import model.Epic;
import model.Subtask;
import model.Task;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Managers {

    public static TaskManager getDefault() {
        HistoryManager historyManager = getDefaultHistory();
        InMemoryTaskManager taskManager = new InMemoryTaskManager(historyManager);
        return taskManager;
    }


    public static TaskManager getBackedTaskManager(File file) {
        HistoryManager historyManager = getDefaultHistory();
        FileBackedTaskManager taskManager = new FileBackedTaskManager(historyManager, file);
        return taskManager;
    }


    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }


    public static FileBackedTaskManager loadFromFile(File file) throws IOException {
        HistoryManager historyManager = getDefaultHistory();
        FileBackedTaskManager fileBackedManager = new FileBackedTaskManager(historyManager, file);
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            int maxId = -1;
            List<String> taskStingList = new ArrayList<>();
            while ((line = br.readLine()) != null) {
                taskStingList.add(line);
            }

            for (String taskLine : taskStingList) {
                if (taskLine.startsWith("id")) {  // пропуск первой строки.
                    continue;
                }

                Task task = fileBackedManager.fromString(taskLine);
                maxId = Math.max(maxId, task.getTaskID());

                switch (task.getType()) {
                    case TASK:
                        fileBackedManager.tasks.put(task.getTaskID(), task);
                        break;

                    case EPIC:
                        fileBackedManager.epics.put(task.getTaskID(), (Epic) task);
                        break;

                    case SUBTASK:
                        Subtask subtask = (Subtask) task;
                        int epicId = subtask.getEpicID();
                        fileBackedManager.subtasks.put(subtask.getTaskID(), subtask);
                        if (fileBackedManager.epics.containsKey(epicId)) {
                            fileBackedManager.epics.get(epicId).setSubtaskIdList(subtask.getTaskID());
                        } else {
                            throw new IllegalStateException("Эпик с id=" + epicId + " не найден для сабтаска: " + subtask);
                        }
                        break;
                }
            }
            fileBackedManager.setCounter(maxId + 1);
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при чтении файла: " + e.getMessage());
        }
        return fileBackedManager;
    }

}
