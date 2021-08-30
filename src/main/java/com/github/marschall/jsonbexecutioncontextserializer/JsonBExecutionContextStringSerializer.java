package com.github.marschall.jsonbexecutioncontextserializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbConfig;

import org.springframework.batch.core.repository.ExecutionContextSerializer;

public final class JsonBExecutionContextStringSerializer implements ExecutionContextSerializer {

  private static final Type MAP_TYPE = new HashMap<String, Object>().getClass().getGenericSuperclass();

  private final Jsonb jsonb;

  public JsonBExecutionContextStringSerializer() {
    JsonbConfig config = new JsonbConfig()
      .withDeserializers(new JobParameterSerializer())
      .withSerializers(new JobParameterSerializer())
      .withAdapters(new ExecutionContextAdapter());
    this.jsonb = JsonbBuilder.create(config);
  }

  public JsonBExecutionContextStringSerializer(JsonbConfig config) {
    this.jsonb = JsonbBuilder.create(config);
  }

  public JsonBExecutionContextStringSerializer(Jsonb jsonb) {
    this.jsonb = jsonb;
  }

  @Override
  public void serialize(Map<String, Object> object, OutputStream outputStream) throws IOException {
    this.jsonb.toJson(object, outputStream);
  }

  @Override
  public Map<String, Object> deserialize(InputStream inputStream) throws IOException {
    return this.jsonb.fromJson(inputStream, Map.class);
  }

}
