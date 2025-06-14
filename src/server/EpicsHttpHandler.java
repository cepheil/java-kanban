package server;

import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import controllers.TaskManager;
import exceptions.IntersectTaskException;
import model.Epic;
import util.TaskType;

import java.io.IOException;
import java.util.List;

public class EpicsHttpHandler extends BaseHttpHandler {
    public EpicsHttpHandler(TaskManager taskManager) {
        super(taskManager);
    }


    @Override
    protected void processGet(HttpExchange exchange, String path) throws IOException {
        if (path.matches("/epics/\\d+/subtasks")) {
            int taskId = parsePathId(path);
            if (taskId < 0) {
                sendResponse(exchange, 400, "Bad Request");
                return;
            }
            if (taskManager.getEpicById(taskId) != null) {
                List<Integer> subtasksList = taskManager.getSubtasksList(taskId);
                String responseText = gson.toJson(subtasksList);
                sendText(exchange, responseText);
            } else {
                sendNotFound(exchange, "Epic Not Found");
            }
        } else if (path.matches("/epics/\\d+")) {
            int taskId = parsePathId(path);
            if (taskId < 0) {
                sendResponse(exchange, 400, "Bad Request");
                return;
            }
            Epic epic = taskManager.getEpicById(taskId);
            if (epic != null) {
                String responseText = gson.toJson(epic);
                sendText(exchange, responseText);
            } else {
                sendNotFound(exchange, "Epic Not Found");
            }
        } else if (path.equals("/epics")) {
            List<Epic> epics = taskManager.getEpics();
            String responseText = gson.toJson(epics);
            sendText(exchange, responseText);
        } else {
            sendNotFound(exchange, "Path not supported");
        }
    }

    @Override
    protected void processPost(HttpExchange exchange, String path) throws IOException {
        if (!path.equals("/epics")) {
            sendResponse(exchange, 405, "Method Not Allowed");
            return;
        }
        String body = readRequestBody(exchange);
        if (body.isEmpty()) {
            sendResponse(exchange, 400, "Empty request body");
            return;
        }
        try {
            Epic epic = gson.fromJson(body, Epic.class);
            if (epic == null || epic.getName() == null || epic.getDescription() == null) {
                sendResponse(exchange, 400, "Invalid epic JSON");
                return;
            }
            int id = epic.getTaskID();
            Epic existing = taskManager.getEpicById(id);
            if (id != 0 && existing != null) {
                taskManager.updateEpic(epic);
                sendText(exchange, "Epic updated successfully");
            } else {
                int createdId = taskManager.addEpic(epic);
                sendCreated(exchange, "Epic created successfully, Epic ID: " + createdId);
            }
        } catch (JsonSyntaxException e) {
            sendResponse(exchange, 400, "Malformed JSON: " + e.getMessage());
        } catch (IntersectTaskException e) {
            sendHasInteractions(exchange, "Task time intersects with existing task: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, "Internal Server Error: " + e.getMessage());
        }
    }

    @Override
    protected void processDelete(HttpExchange exchange, String path) throws IOException {
        if (path.equals("/epics")) {
            taskManager.removeAllTasks(TaskType.EPIC);
            sendText(exchange, "All epics deleted");
        } else {
            int taskId = parsePathId(path);
            if (taskId < 0) {
                sendResponse(exchange, 400, "Bad Request");
                return;
            }
            Epic epic = taskManager.getEpicById(taskId);
            if (epic == null) {
                sendNotFound(exchange, "Epic Not Found");
            } else {
                taskManager.removeTaskById(taskId);
                sendText(exchange, "Epic deleted");
            }
        }
    }
}