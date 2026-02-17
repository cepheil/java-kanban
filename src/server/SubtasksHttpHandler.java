package server;

import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import controllers.TaskManager;
import exceptions.IntersectTaskException;
import model.Subtask;
import util.TaskType;

import java.io.IOException;
import java.util.List;

public class SubtasksHttpHandler extends BaseHttpHandler {
    public SubtasksHttpHandler(TaskManager taskManager) {
        super(taskManager);
    }


    @Override
    protected void processGet(HttpExchange exchange, String path) throws IOException {
        if (path.equals("/subtasks")) {
            List<Subtask> subtasks = taskManager.getSubtasks();
            String responseText = gson.toJson(subtasks);
            sendText(exchange, responseText);
        } else {
            int taskId = parsePathId(path);
            if (taskId < 0) {
                sendResponse(exchange, 400, "Bad Request");
                return;
            }
            Subtask subtask = taskManager.getSubtaskById(taskId);
            if (subtask != null) {
                String responseText = gson.toJson(subtask);
                sendText(exchange, responseText);
            } else {
                sendNotFound(exchange, "Task Not Found");
            }
        }
    }

    @Override
    protected void processPost(HttpExchange exchange, String path) throws IOException {
        if (!path.equals("/subtasks")) {
            sendResponse(exchange, 405, "Method Not Allowed");
            return;
        }
        String body = readRequestBody(exchange);
        if (body.isEmpty()) {
            sendResponse(exchange, 400, "Empty request body");
            return;
        }
        try {
            Subtask subtask = gson.fromJson(body, Subtask.class);
            if (subtask == null || subtask.getName() == null || subtask.getDescription() == null ||
                    subtask.getStatus() == null) {
                sendResponse(exchange, 400, "Invalid task JSON");
                return;
            }
            if (subtask.getEpicID() == 0 || taskManager.getEpicById(subtask.getEpicID()) == null) {
                sendResponse(exchange, 400, "Epic not found");
                return;
            }
            int id = subtask.getTaskID();
            Subtask existing = taskManager.getSubtaskById(id);
            if (id != 0 && existing != null) {
                taskManager.updateSubtask(subtask);
                sendText(exchange, "Task updated successfully");
            } else {
                int createdId = taskManager.addSubtask(subtask);
                sendCreated(exchange, "Task created successfully, Task ID: " + createdId);
            }
        } catch (JsonSyntaxException e) {
            sendResponse(exchange, 400, "Malformed JSON: " + e.getMessage());
        } catch (IntersectTaskException e) {
            sendHasInteractions(exchange, "Subtask time intersects with existing subtask: " + e.getMessage());
        } catch (Exception e) {
            sendResponse(exchange, 500, "Internal Server Error: " + e.getMessage());
        }
    }

    @Override
    protected void processDelete(HttpExchange exchange, String path) throws IOException {
        if (path.equals("/subtasks")) {
            taskManager.removeAllTasks(TaskType.SUBTASK);
            sendText(exchange, "All subtasks deleted");
        } else {
            int taskId = parsePathId(path);
            if (taskId < 0) {
                sendResponse(exchange, 400, "Bad Request");
                return;
            }
            Subtask subtask = taskManager.getSubtaskById(taskId);
            if (subtask == null) {
                sendNotFound(exchange, "Subtask Not Found");
            } else {
                taskManager.removeTaskById(taskId);
                sendText(exchange, "Subtask deleted");
            }
        }
    }
}
