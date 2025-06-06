package server;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import controllers.TaskManager;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public abstract class BaseHttpHandler implements HttpHandler {
    protected final TaskManager taskManager;
    protected final Gson gson;

    public BaseHttpHandler(TaskManager taskManager, Gson gson) {
        this.taskManager = taskManager;
        this.gson = gson;
    }


    protected String readRequestBody(HttpExchange exchange) throws IOException {
        return new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
    }


    protected void sendText(HttpExchange h, String text) throws IOException {
        sendResponse(h, 200, text);
    }

    protected void sendCreated(HttpExchange h, String text) throws IOException {
        sendResponse(h, 201, text);
    }

    //Not Found
    protected void sendNotFound(HttpExchange h, String text) throws IOException {
        sendResponse(h, 404, text);
    }

    //Not Acceptable
    protected void sendHasInteractions(HttpExchange h, String text) throws IOException {
        sendResponse(h, 406, text);
    }


    protected void sendResponse(HttpExchange h, int statusCode, String text) throws IOException {
        byte[] resp = text.getBytes(StandardCharsets.UTF_8);
        h.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        h.sendResponseHeaders(statusCode, resp.length);
        try (OutputStream os = h.getResponseBody()) {
            os.write(resp);
        }
    }


    protected int parsePathId(String path) {
        String[] splitStrings = path.split("/");
        if (splitStrings.length < 3) {
            return -1;
        }
        try {
            return Integer.parseInt(splitStrings[2]);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
