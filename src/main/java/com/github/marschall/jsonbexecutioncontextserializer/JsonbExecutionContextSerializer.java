package com.github.marschall.jsonbexecutioncontextserializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Locale;
import java.util.Map;

import org.springframework.batch.core.repository.ExecutionContextSerializer;
import org.springframework.core.convert.support.ConfigurableConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.util.Assert;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbConfig;
import jakarta.json.bind.adapter.JsonbAdapter;


/**
 * Implementation that uses JSON-B to provide (de)serialization.
 *
 * @see ExecutionContextSerializer
 */
public final class JsonbExecutionContextSerializer implements ExecutionContextSerializer {

  private final Jsonb jsonb;

  /**
   * Create a new {@link JsonbExecutionContextSerializer} using a default configuration.
   *
   * @see JsonbBuilder#create()
   */
  public JsonbExecutionContextSerializer() {
    this(new DefaultConversionService());
  }

  /**
   * Create a new {@link JsonbExecutionContextSerializer} using a default configuration.
   *
   * @param conversionService used to convert job parameters
   * @see JsonbBuilder#create()
   */
  public JsonbExecutionContextSerializer(ConfigurableConversionService conversionService) {
    JsonbConfig config = new JsonbConfig()
//        .withEncoding(ISO_8859_1.name()) // JdbcJobExecutionDao hard codes ISO-8859-1
        .withDeserializers(new JobParameterSerializer(conversionService), new ExecutionContextWrapperSerializer())
        .withSerializers(new JobParameterSerializer(conversionService), new ExecutionContextWrapperSerializer())
        .withAdapters(new JobParametersAdapter(), new LocaleAdapter(), new SqlDateAdapter(), new SqlTimestampAdapter(), new SqlTimeAdapter());
    this.jsonb = JsonbBuilder.create(config);
  }

  /**
   * Create a new {@link JsonbExecutionContextSerializer} using a default configuration.
   *
   * @param config the {@link JsonbConfig} used to create a {@link Jsonb} instance
   *
   * @see JsonbBuilder#create(JsonbConfig)
   */
  public JsonbExecutionContextSerializer(JsonbConfig config) {
    Assert.notNull(config, "A JSON-B config is required");
    this.jsonb = JsonbBuilder.create(config);
  }

  /**
   * Create a new {@link JsonbExecutionContextSerializer} the given JSON-B instance.
   *
   * @param jsonb the {@link Jsonb} instance to use
   */
  public JsonbExecutionContextSerializer(Jsonb jsonb) {
    Assert.notNull(jsonb, "A JSON-B instance is required");
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

  /**
   * Adapts a {@link Locale} to a string in ISO format.
   *
   * @see Locale#toString()
   */
  static final class LocaleAdapter implements JsonbAdapter<Locale, String> {

    @Override
    public String adaptToJson(Locale locale) {
      if (locale == null) {
        return null;
      }
      return locale.toString();
    }

    @Override
    public Locale adaptFromJson(String s) {
      if (s == null) {
        return null;
      }
      if (s.startsWith("_")) {
        throw new IllegalArgumentException("unsupported locale format: " + s);
      }
      String[] parts = s.split("_");
      switch (parts.length) {
        case 1:
          return new Locale(parts[0]);
        case 2:
          return new Locale(parts[0], parts[1]);
        case 3:
          return new Locale(parts[0], parts[1], parts[2]);
        default:
          throw new IllegalArgumentException("unsupported locale format: " + s);
      }
    }

  }

}
