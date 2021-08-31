package com.github.marschall.jsonbexecutioncontextserializer;

import static java.nio.charset.StandardCharsets.ISO_8859_1;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbConfig;

import org.springframework.batch.core.repository.ExecutionContextSerializer;


public final class JsonbExecutionContextSerializer implements ExecutionContextSerializer {

  private static final Type MAP_TYPE = new ParameterizedType() {

    @Override
    public Type getRawType() {
      return Map.class;
    }

    @Override
    public Type getOwnerType() {
      return null;
    }

    @Override
    public Type[] getActualTypeArguments() {
      return new Type[] { String.class, Object.class };
    }

    @Override
    public String getTypeName() {
      return "java.util.Map<String, Object>";
    }

  };

  private final Jsonb jsonb;

  public JsonbExecutionContextSerializer() {
    JsonbConfig config = new JsonbConfig()
            .withEncoding(ISO_8859_1.name()) // JdbcJobExecutionDao hard codes ISO-8859-1
            .withDeserializers(new JobParameterSerializer(), new ExecutionContextWrapperSerializer())
            .withSerializers(new JobParameterSerializer(), new ExecutionContextWrapperSerializer())
            .withAdapters(new JobParametersAdapter(), new LocaleAdapter());
    this.jsonb = JsonbBuilder.create(config);
  }

  public JsonbExecutionContextSerializer(JsonbConfig config) {
    this.jsonb = JsonbBuilder.create(config);
  }

  public JsonbExecutionContextSerializer(Jsonb jsonb) {
    this.jsonb = jsonb;
  }

  @Override
  public void serialize(Map<String, Object> object, OutputStream outputStream) throws IOException {
    this.jsonb.toJson(new ExecutionContextWrapper(object), outputStream);
  }

  @Override
  public Map<String, Object> deserialize(InputStream inputStream) throws IOException {
    return this.jsonb.fromJson(inputStream, ExecutionContextWrapper.class).getMap();
  }

}
