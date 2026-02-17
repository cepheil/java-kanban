package server;

import com.sun.net.httpserver.HttpExchange;
import controllers.TaskManager;
import model.Task;

import java.io.IOException;
import java.util.List;

public class HistoryHttpHandler extends BaseHttpHandler {
    public HistoryHttpHandler(TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    protected void processGet(HttpExchange exchange, String path) throws IOException {
        try {
            if (path.equals("/history")) {
                List<Task> history = taskManager.getHistory();
                String responseText = gson.toJson(history);
                sendText(exchange, responseText);
            } else {
                sendResponse(exchange, 405, path + "Path Not Allowed");
            }
        } catch (Exception exception) {
            exception.printStackTrace();
            sendResponse(exchange, 500, "Internal Server Error");
        }
    }
}
