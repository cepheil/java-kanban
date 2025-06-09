package server;

import com.sun.net.httpserver.HttpExchange;
import controllers.TaskManager;
import model.Task;

import java.io.IOException;
import java.util.List;

public class PrioritizedHttpHandler extends BaseHttpHandler {
    public PrioritizedHttpHandler(TaskManager taskManager) {
        super(taskManager);
    }


    @Override
    protected void processGet(HttpExchange exchange, String path) throws IOException {
        try {
            if (path.equals("/prioritized")) {
                List<Task> prioritizedTasks = taskManager.getPrioritizedTasks();
                String responseText = gson.toJson(prioritizedTasks);
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
