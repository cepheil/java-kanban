package controllers;

import model.Task;

import java.util.List;

public interface HistoryManager {

    void updateHistory(Task task);

    List<Task>  getHistory();


}
