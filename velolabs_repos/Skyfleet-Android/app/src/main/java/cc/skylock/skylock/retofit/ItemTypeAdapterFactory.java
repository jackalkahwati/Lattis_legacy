package cc.skylock.skylock.retofit;

import android.util.ArrayMap;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.Map;

/**
 * Created by prodapt on 06/08/15.
 */
    public class ItemTypeAdapterFactory   implements TypeAdapterFactory {
    private static final String TAG = ItemTypeAdapterFactory.class.getSimpleName();

    private Map<EventType, TypeAdapter<? extends Event>> ADAPTERS = new ArrayMap<>();
    private TypeAdapter<Event> baseTypeAdapter;
    private TypeAdapter<JsonElement> elementAdapter;
    private TypeAdapter<EventType> eventTypeAdapter;

    @Override public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        if (!Event.class.isAssignableFrom(type.getRawType())) return null;

        ADAPTERS.put(EventType.TYPE_A, gson.getDelegateAdapter(this, TypeToken.get(TypeAEvent.class)));
        ADAPTERS.put(EventType.TYPE_B, gson.getDelegateAdapter(this, TypeToken.get(TypeBEvent.class)));

        baseTypeAdapter = gson.getDelegateAdapter(this, TypeToken.get(Event.class));

        elementAdapter = gson.getAdapter(JsonElement.class);
        eventTypeAdapter = gson.getAdapter(EventType.class);

        return (TypeAdapter<T>) new EventTypeAdapter().nullSafe();
    }

    private class EventTypeAdapter extends TypeAdapter<Event> {

        @Override public void write(JsonWriter out, Event value) throws IOException {
            EventType eventType = value.getType();
            TypeAdapter<? extends Event> adapter = eventType == null ? baseTypeAdapter : ADAPTERS.get(eventType);
            if (value instanceof TypeAEvent) {
                writeWrap(adapter, out, (TypeAEvent) value, TypeAEvent.class);
            } else if (value instanceof TypeBEvent) {
                writeWrap(adapter, out, (TypeBEvent) value, TypeBEvent.class);
            } else {
                writeWrap(adapter, out, value, Event.class);
            }
        }

        private <T extends Event> void writeWrap(TypeAdapter<? extends Event> adapter,
                                                 JsonWriter out, T value, Class<T> dummyForT) throws IOException {
            ((TypeAdapter<T>)adapter).write(out, value);
        }

        @Override public Event read(JsonReader in) throws IOException {
            JsonObject objectJson = elementAdapter.read(in).getAsJsonObject();
            JsonElement typeJson = objectJson.get("EventType");

            EventType eventType = eventTypeAdapter.fromJsonTree(typeJson);

            if (eventType == null) {
                Log.w(TAG, "Unsupported EventType: " + typeJson);
            }

            TypeAdapter<? extends Event> adapter = eventType == null ? baseTypeAdapter : ADAPTERS.get(eventType);
            return adapter.fromJsonTree(objectJson);
        }
    }
}

// EventType enum, change to reflect your values.
enum EventType {
    TYPE_A, TYPE_B;
}

// Base Event type and its successors.
class Event {
    @SerializedName("EventType")
    private EventType type;

    public EventType getType() {
        return type;
    }
}

class TypeAEvent extends Event {
    @SerializedName("Data")
    public String data;
}

class TypeBEvent extends Event {
    @SerializedName("OtherData")
    public int otherData;
}