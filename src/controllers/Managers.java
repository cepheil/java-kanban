package controllers;

public class Managers {

    public static TaskManager getDefault() {
        HistoryManager historyManager = getDefaultHistory();
        InMemoryTaskManager taskManager = new InMemoryTaskManager(historyManager);
        return taskManager;
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }

}
