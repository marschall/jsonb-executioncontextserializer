package com.github.marschall.jsonbexecutioncontextserializer;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.json.bind.serializer.DeserializationContext;
import javax.json.bind.serializer.JsonbDeserializer;
import javax.json.bind.serializer.JsonbSerializer;
import javax.json.bind.serializer.SerializationContext;
import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.springframework.batch.item.ExecutionContext;

public final class ExecutionContextSerializer implements JsonbSerializer<ExecutionContext>, JsonbDeserializer<ExecutionContext> {

  @Override
  public ExecutionContext deserialize(JsonParser parser, DeserializationContext ctx, Type rtType) {
    Map<String, Object> map = new HashMap<>();
    Event next;

    while ((next = parser.next()) != Event.END_OBJECT) {
        if (next == JsonParser.Event.KEY_NAME) {
            String key = parser.getString();

            // Move to json value
            parser.next();

            Object value = ctx.deserialize(Object.class, parser);

            map.put(key, value);
        }
    }
    return new ExecutionContext(map);
  }

  @Override
  public void serialize(ExecutionContext executionContext, JsonGenerator generator, SerializationContext ctx) {
    generator.writeStartObject();
    for (Entry<String, Object> entry : executionContext.entrySet()) {
      ctx.serialize(entry.getKey(), entry.getKey(), generator);
    }
    generator.writeEnd();
  }

}
