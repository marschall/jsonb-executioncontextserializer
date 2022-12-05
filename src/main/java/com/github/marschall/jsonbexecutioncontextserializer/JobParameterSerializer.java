package com.github.marschall.jsonbexecutioncontextserializer;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;

import org.springframework.batch.core.JobParameter;
import org.springframework.core.convert.support.ConfigurableConversionService;

import jakarta.json.bind.JsonbException;
import jakarta.json.bind.serializer.DeserializationContext;
import jakarta.json.bind.serializer.JsonbDeserializer;
import jakarta.json.bind.serializer.JsonbSerializer;
import jakarta.json.bind.serializer.SerializationContext;
import jakarta.json.stream.JsonGenerator;
import jakarta.json.stream.JsonParser;
import jakarta.json.stream.JsonParser.Event;

/**
 * Serializes a {@link JobParameter}
 *
 * <h1>Format used</h1>
 * <pre><code>
 * {
 *   "type":"STRING",
 *   "value":"paramValue",
 *   "identifying":true
 * }
 * </code></pre>
 *
 * <h2>Paramater value serialization</h2>
 * <table>
 * <tr>
 * <th>Parameter Type</th>
 * <th>JSON serialization</th>
 * </tr>
 * <tr>
 * <td>{@link ParameterType#STRING}</td>
 * <td>string</td>
 * </tr>
 * <tr>
 * <td>{@link ParameterType#DATE}</td>
 * <td>number, number of milliseconds since the "the epoch"</dd>
 * </tr>
 * <tr>
 * <td>{@link ParameterType#LONG}</td>
 * <td>number</td>
 * </tr>
 * <tr>
 * <td>{@link ParameterType#DOUBLE}</td>
 * <td>number</td>
 * </tr>
 * </table>
 */
@SuppressWarnings("rawtypes") // generic code
final class JobParameterSerializer implements JsonbSerializer<JobParameter>, JsonbDeserializer<JobParameter> {

  private static final String IDENTIFYING_KEY_NAME = "identifying";
  private static final String TYPE_KEY_NAME = "type";
  private static final String VALUE_KEY_NAME = "value";

  private final ConfigurableConversionService conversionService;

  JobParameterSerializer(ConfigurableConversionService conversionService) {
    this.conversionService = conversionService;
  }

  @Override
  public JobParameter deserialize(JsonParser parser, DeserializationContext ctx, Type rtType) {
    Class<?> parameterType = null;
    Object value = null;
    boolean identifying = true; // default if missing

    Event next = parser.next();
    while (next != Event.END_OBJECT) {
      if (next == JsonParser.Event.KEY_NAME) {
        String key = parser.getString();

        Event valueEvent = parser.next();

        switch (key) {
          case TYPE_KEY_NAME -> { // "type": "java.lang.String"
            String className = parser.getString();
            try {
              parameterType = Class.forName(className);
            } catch (ClassNotFoundException e) {
              throw new JsonbException("could not load class: " + className, e);
            }
          }
          case VALUE_KEY_NAME -> { // "value": "foo"

            value = switch (valueEvent) {
              case VALUE_STRING -> this.conversionService.convert(parser.getString(), parameterType);
              case VALUE_NUMBER -> this.conversionService.convert(parser.getBigDecimal(), parameterType);
              case VALUE_TRUE -> this.conversionService.convert(true, parameterType);
              case VALUE_FALSE -> this.conversionService.convert(false, parameterType);
              case VALUE_NULL -> this.conversionService.convert(null, parameterType);
              default -> throw new JsonbException("Unexpected value: " + valueEvent);
            };
          }
          case IDENTIFYING_KEY_NAME -> { // "identifying":true
            identifying = switch (valueEvent) {
              case VALUE_TRUE -> true;
              case VALUE_FALSE -> false;
              default -> throw new JsonbException("expected boolean");
            };
          }
          default -> {
            throw new JsonbException("unexpected key: " + key);
          }
        }
      }
      next = parser.next();
    }

    return new JobParameter(value, parameterType, identifying);
  }

  @Override
  public void serialize(JobParameter jobParameter, JsonGenerator generator, SerializationContext ctx) {
    generator.writeStartObject(); // {

    Class<?> parameterType = jobParameter.getType();
    generator.write(TYPE_KEY_NAME, parameterType.getName()); // "type": "java.lang.String"

    generator.writeKey(VALUE_KEY_NAME); // "value":

    // "foo"
    Object parameterValue = jobParameter.getValue();
    if (parameterValue instanceof String s) {
      generator.write(s);
    } else if (parameterValue instanceof Integer i) {
      generator.write(i);
    } else if (parameterValue instanceof Long l) {
      generator.write(l);
    } else if (parameterValue instanceof Double d) {
      generator.write(d);
    } else if (parameterValue instanceof Boolean b) {
      generator.write(b);
    } else if (parameterValue instanceof BigInteger bi) {
      generator.write(bi);
    } else if (parameterValue instanceof BigDecimal db) {
      generator.write(db);
    } else if (parameterValue == null) {
      generator.writeNull();
    } else {
      String stringValue = this.conversionService.convert(parameterValue, String.class);
      generator.write(stringValue);
    }

    // "identifying": true
    generator.write(IDENTIFYING_KEY_NAME, jobParameter.isIdentifying());

    generator.writeEnd(); // }
  }


}
