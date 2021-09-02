package com.github.marschall.jsonbexecutioncontextserializer;

import static java.util.Map.entry;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.json.bind.JsonbException;
import javax.json.bind.serializer.DeserializationContext;
import javax.json.bind.serializer.JsonbDeserializer;
import javax.json.bind.serializer.JsonbSerializer;
import javax.json.bind.serializer.SerializationContext;
import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

final class ExecutionContextWrapperSerializer implements JsonbSerializer<ExecutionContextWrapper>, JsonbDeserializer<ExecutionContextWrapper> {

  private static final String VALUE_KEY_NAME = "value";

  private static final String CLASS_KEY_NAME = "@class";

  private static final Map<String, Class<?>> JDK_CLASSES = Map.ofEntries(
      entry("java.lang.Byte", Byte.class),
      entry("java.lang.Short", Short.class),
      entry("java.lang.Integer", Integer.class),
      entry("java.lang.Long", Long.class),
      entry("java.lang.Float", Float.class),
      entry("java.lang.Double", Double.class),
      entry("java.lang.Boolean", Boolean.class),
      entry("java.math.BigDecimal", java.math.BigDecimal.class),
      entry("java.math.BigInteger", java.math.BigInteger.class),
      
      entry("java.util.Date", java.util.Date.class),
      entry("java.util.Locale", java.util.Locale.class),
      entry("java.net.URL", java.net.URL.class),
      entry("java.net.URI", java.net.URI.class),

      entry("java.sql.Date", java.sql.Date.class),
      entry("java.sql.Time", java.sql.Time.class),
      entry("java.sql.Timestamp", java.sql.Timestamp.class),
      
      entry("java.time.LocalDate", java.time.LocalDate.class),
      entry("java.time.LocalTime", java.time.LocalTime.class),
      entry("java.time.LocalDateTime", java.time.LocalDateTime.class),
      entry("java.time.OffsetDateTime", java.time.OffsetDateTime.class),
      entry("java.time.ZonedDateTime", java.time.ZonedDateTime.class),
      entry("java.time.Duration", java.time.Duration.class),
      entry("java.time.Period", java.time.Period.class)
      );
  
  private static final Map<String, Class<?>> SPRING_BATCH_CLASSES = Map.ofEntries(
      entry("org.springframework.batch.core.JobParameter", org.springframework.batch.core.JobParameter.class),
      entry("org.springframework.batch.core.JobParameters", org.springframework.batch.core.JobParameters.class)
      );

  @Override
  public ExecutionContextWrapper deserialize(JsonParser parser, DeserializationContext ctx, Type rtType) {
    Map<String, Object> map = new HashMap<>();
    Event next;

    while ((next = parser.next()) != Event.END_OBJECT) {
      if (next == Event.KEY_NAME) {
        String key = parser.getString();

        // key: {
        if (parser.next() != Event.START_OBJECT) {
          throw new JsonbException("START_OBJECT expected");
        }
        
        // @class
        if (parser.next() != Event.KEY_NAME) {
          throw new JsonbException("KEY_NAME expected");
        }
        if (!parser.getString().equals(CLASS_KEY_NAME)) {
          throw new JsonbException(CLASS_KEY_NAME + " expected");
        }
        if (parser.next() != Event.VALUE_STRING) {
          throw new JsonbException("string expected");
        }
        String className = parser.getString();
        Class<?> valueClass;
        try {
          valueClass = resolveClass(className);
        } catch (ClassNotFoundException e) {
          throw new JsonbException("could not load class: " + className, e);
        }
        
        // value {
        if (parser.next() != Event.KEY_NAME) {
          throw new JsonbException("KEY_NAME expected");
        }
        if (!parser.getString().equals(VALUE_KEY_NAME)) {
          throw new JsonbException(VALUE_KEY_NAME + " expected");
        }
        Object value = ctx.deserialize(valueClass, parser);

        map.put(key, value);

        // close key: }
        if (parser.next() != Event.END_OBJECT) {
          throw new JsonbException("END_OBJECT expected");
        }
        
      }
    }
    return new ExecutionContextWrapper(map);
  }

  private Class<?> resolveClass(String className) throws ClassNotFoundException {
    Class<?> jdkClass = JDK_CLASSES.get(className);
    if (jdkClass != null) {
      return jdkClass;
    }
    Class<?> springBatchClass = SPRING_BATCH_CLASSES.get(className);
    if (springBatchClass != null) {
      return springBatchClass;
    }
    return Thread.currentThread().getContextClassLoader().loadClass(className);
  }

  @Override
  public void serialize(ExecutionContextWrapper wrapper, JsonGenerator generator, SerializationContext ctx) {
    Map<String, Object> executionContext = wrapper.getMap();
    generator.writeStartObject();
    for (Entry<String, Object> entry : executionContext.entrySet()) {
      generator.writeStartObject(entry.getKey());

      // TODO null
      Object value = entry.getValue();
      generator.write(CLASS_KEY_NAME, value.getClass().getName());
      ctx.serialize(VALUE_KEY_NAME, value, generator);

      generator.writeEnd();
    }
    generator.writeEnd();
  }

}