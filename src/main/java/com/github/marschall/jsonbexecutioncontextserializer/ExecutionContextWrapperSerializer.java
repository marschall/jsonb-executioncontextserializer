package com.github.marschall.jsonbexecutioncontextserializer;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toUnmodifiableMap;

import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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

  private static final Map<String, Class<?>> JDK_CLASSES;
  
  private static final Map<String, Class<?>> SPRING_BATCH_CLASSES;
  
  static {
    List<Class<?>> jdkClasses = List.of(
        Byte.class,
        Short.class,
        Integer.class,
        Long.class,
        Float.class,
        Double.class,
        Boolean.class,
        java.math.BigDecimal.class,
        java.math.BigInteger.class,
        
        java.util.Date.class,
        java.util.Locale.class,
        java.net.URL.class,
        java.net.URI.class,

        java.sql.Date.class,
        java.sql.Time.class,
        java.sql.Timestamp.class,
        
        java.time.LocalDate.class,
        java.time.LocalTime.class,
        java.time.LocalDateTime.class,
        java.time.OffsetDateTime.class,
        java.time.ZonedDateTime.class,
        java.time.Duration.class,
        java.time.Period.class);
    
    List<Class<?>> springBatchClasses = List.of(
        org.springframework.batch.core.JobParameter.class,
        org.springframework.batch.core.JobParameters.class);
    
    JDK_CLASSES = toClassMap(jdkClasses);
    SPRING_BATCH_CLASSES = toClassMap(springBatchClasses);
  }
  
  private static Map<String, Class<?>> toClassMap(List<Class<?>> classes) {
    return classes.stream()
                  .collect(toUnmodifiableMap(Class::getName, identity()));
  }

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
      generator.write(CLASS_KEY_NAME, getPublicClassName(value.getClass()));
      ctx.serialize(VALUE_KEY_NAME, value, generator);

      generator.writeEnd();
    }
    generator.writeEnd();
  }
  
  private static String getPublicClassName(Class<?> valueClass) {
    if (!Modifier.isPublic(valueClass.getModifiers()) && Collection.class.isAssignableFrom(valueClass)) {
      if (Map.class.isAssignableFrom(valueClass)) {
        return Map.class.getName();
      }
      if (List.class.isAssignableFrom(valueClass)) {
        return List.class.getName();
      }
      if (Set.class.isAssignableFrom(valueClass)) {
        return Set.class.getName();
      }
    }
    return valueClass.getName();
  }

}
