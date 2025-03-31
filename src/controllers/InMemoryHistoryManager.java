package controllers;

import model.Epic;
import model.Subtask;
import model.Task;

import java.util.ArrayList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {

    ArrayList<Task> historyList = new ArrayList<>();

    @Override
    public void updateHistory(Task task) {
        Task taskCopy = copyTask(task);

        if (historyList.size() >= 10) {
            historyList.remove(0);
        }
        historyList.add(taskCopy);
    }

    @Override
    public List<Task> getHistory() {
        return new ArrayList<>(historyList);
    }

    private Task copyTask(Task task) {
        if (task instanceof Epic) {
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
        } else if (task instanceof Subtask) {
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
