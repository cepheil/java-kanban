package server;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import controllers.TaskManager;
import model.Task;

import java.io.IOException;
import java.util.List;

public class PrioritizedHttpHandler extends BaseHttpHandler {
    public PrioritizedHttpHandler(TaskManager taskManager, Gson gson) {
        super(taskManager, gson);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();
            if (method.equals("GET") && path.equals("/prioritized")) {
                List<Task> history = taskManager.getPrioritizedTasks();
                String responseText = gson.toJson(history);
                sendText(exchange, responseText);
            } else {
                sendResponse(exchange, 405, "Method Not Allowed");
            }
        } catch (Exception exception) {
            exception.printStackTrace();
            sendResponse(exchange, 500, "Internal Server Error");
        } finally {
            exchange.close();
        }
    }
}
