package com.github.marschall.jsonbexecutioncontextserializer;

import static java.nio.charset.StandardCharsets.ISO_8859_1;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Map;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbConfig;
import javax.json.bind.adapter.JsonbAdapter;

import org.springframework.batch.core.repository.ExecutionContextSerializer;
import org.springframework.util.Assert;


public final class JsonbExecutionContextSerializer implements ExecutionContextSerializer {

  private final Jsonb jsonb;

  public JsonbExecutionContextSerializer() {
    JsonbConfig config = new JsonbConfig()
        .withEncoding(ISO_8859_1.name()) // JdbcJobExecutionDao hard codes ISO-8859-1
        .withDeserializers(new JobParameterSerializer(), new ExecutionContextWrapperSerializer())
        .withSerializers(new JobParameterSerializer(), new ExecutionContextWrapperSerializer())
        .withAdapters(new JobParametersAdapter(), new LocaleAdapter(), new SqlDateAdapter(), new SqlTimestampAdapter(), new SqlTimeAdapter());
    this.jsonb = JsonbBuilder.create(config);
  }

  public JsonbExecutionContextSerializer(JsonbConfig config) {
    this.jsonb = JsonbBuilder.create(config);
  }

  public JsonbExecutionContextSerializer(Jsonb jsonb) {
    this.jsonb = jsonb;
  }

  @Override
  public void serialize(Map<String, Object> context, OutputStream out) throws IOException {
    Assert.notNull(context, "A context is required");
    Assert.notNull(out, "An OutputStream is required");

    this.jsonb.toJson(new ExecutionContextWrapper(context), out);
  }

  @Override
  public Map<String, Object> deserialize(InputStream in) throws IOException {
    return this.jsonb.fromJson(in, ExecutionContextWrapper.class).getMap();
  }

  /**
   * Adapts a {@link Date} in the format yyyy-MM-dd. This is important because while
   * {@link Date} is a subclass of {@link java.util.Date} it is not a subtype and
   * does not have instant semantics. Therefore it should not have a time component.
   * 
   * @see Date#toString()
   */
  static final class SqlDateAdapter implements JsonbAdapter<Date, String> {

    @Override
    public String adaptToJson(Date date) {
      if (date == null) {
        return null;
      }
      return date.toString();
    }

    @Override
    public Date adaptFromJson(String s) {
      if (s == null) {
        return null;
      }
      return Date.valueOf(s);
    }

  }

  /**
   * Adapts a {@link Time} in the format hh:mm:ss. This is important because while
   * {@link Time} is a subclass of {@link java.util.Date} it is not a subtype and
   * does not have instant semantics. Therefore it should not have a time component.
   * 
   * @see Time#toString()
   */
  static final class SqlTimeAdapter implements JsonbAdapter<Time, String> {

    @Override
    public String adaptToJson(Time time) {
      if (time == null) {
        return null;
      }
      return time.toString();
    }

    @Override
    public Time adaptFromJson(String s) {
      if (s == null) {
        return null;
      }
      return Time.valueOf(s);
    }

  }

  /**
   * Adapts a {@link Timestamp} in the format yyyy-mm-dd hh:mm:ss.fffffffff. This is important because while
   * {@link Timestamp} is a subclass of {@link java.util.Date} it is not a subtype and
   * does not have instant semantics. Therefore it should not have a time component.
   * 
   * @see Timestamp#toString()
   */
  static final class SqlTimestampAdapter implements JsonbAdapter<Timestamp, String> {

    @Override
    public String adaptToJson(Timestamp timestamp) {
      if (timestamp == null) {
        return null;
      }
      return timestamp.toString();
    }

    @Override
    public Timestamp adaptFromJson(String s) {
      if (s == null) {
        return null;
      }
      return Timestamp.valueOf(s);
    }

  }

}
