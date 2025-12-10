package in.afi.codekosh.components;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class JsonParser {

    private final ExecutorService executorService;

    public JsonParser() {
        executorService = Executors.newSingleThreadExecutor();
    }

    public void parseJsonResponse(String jsonResponse, JsonParseListener listener) {
        executorService.submit(new JsonParserTask(jsonResponse, listener));
    }

    public interface JsonParseListener {
        void onParseComplete(ArrayList<HashMap<String, Object>> parsedData);

        void onError(String errorMessage);
    }

    private static class JsonParserTask implements Callable<Void> {
        private final String jsonResponse;
        private final JsonParseListener listener;

        public JsonParserTask(String jsonResponse, JsonParseListener listener) {
            this.jsonResponse = jsonResponse;
            this.listener = listener;
        }

        @Override
        public Void call() {
            ArrayList<HashMap<String, Object>> parsedData = new ArrayList<>();

            try (JsonReader reader = new JsonReader(new StringReader(jsonResponse))) {
                try {
                    reader.beginArray();
                    while (reader.hasNext()) {
                        HashMap<String, Object> item = new HashMap<>();
                        reader.beginObject();
                        while (reader.hasNext()) {
                            String key = reader.nextName();
                            Object value = null;
                            if (reader.peek() != JsonToken.NULL) {
                                value = reader.nextString(); // You might need to handle other data types accordingly
                            } else {
                                reader.nextNull();
                            }
                            item.put(key, value);
                        }
                        reader.endObject();
                        parsedData.add(item);
                    }
                    reader.endArray();

                    listener.onParseComplete(parsedData);
                } catch (IOException e) {
                    e.printStackTrace();
                    listener.onError("Error occurred while parsing JSON response.");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }
    }
}
