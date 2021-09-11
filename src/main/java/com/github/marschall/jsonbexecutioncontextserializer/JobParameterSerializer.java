package com.github.marschall.jsonbexecutioncontextserializer;

import java.lang.reflect.Type;
import java.util.Date;

import javax.json.bind.JsonbException;
import javax.json.bind.serializer.DeserializationContext;
import javax.json.bind.serializer.JsonbDeserializer;
import javax.json.bind.serializer.JsonbSerializer;
import javax.json.bind.serializer.SerializationContext;
import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameter.ParameterType;

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
final class JobParameterSerializer implements JsonbSerializer<JobParameter>, JsonbDeserializer<JobParameter> {

  private static final String IDENTIFYING_KEY_NAME = "identifying";
  private static final String TYPE_KEY_NAME = "type";
  private static final String VALUE_KEY_NAME = "value";

  @Override
  public JobParameter deserialize(JsonParser parser, DeserializationContext ctx, Type rtType) {
    ParameterType parameterType = null;
    Object value = null;
    boolean identifying = true; // default if missing

    Event next;
    while ((next = parser.next()) != Event.END_OBJECT) {
      if (next == JsonParser.Event.KEY_NAME) {
        String key = parser.getString();

        Event valueEvent = parser.next();

        switch (key) {
          case TYPE_KEY_NAME:
            parameterType = ParameterType.valueOf(parser.getString());
            break;
          case VALUE_KEY_NAME:

            switch (parameterType) {
              case STRING:
                value = parser.getString();
                break;
              case DATE:
                value = new Date(parser.getLong());
                break;
              case LONG:
                value = parser.getLong();
                break;
              case DOUBLE:
                value = parser.getBigDecimal().doubleValue();
                break;
            }
            break;
          case IDENTIFYING_KEY_NAME:
            switch (valueEvent) {
            case VALUE_TRUE:
              identifying = true;
              break;
            case VALUE_FALSE:
              identifying = false;
              break;
            default:
              throw new JsonbException("expected boolean");
            }
            break;
        }
      }
    }

    switch (parameterType) {
      case STRING:
        return new JobParameter((String) value, identifying);
      case DATE:
        return new JobParameter((Date) value, identifying);
      case LONG:
        return new JobParameter((Long) value, identifying);
      case DOUBLE:
        return new JobParameter((Double) value, identifying);
    }
    throw new IllegalStateException("unknown parameter type: " + parameterType);
  }

  @Override
  public void serialize(JobParameter jobParameter, JsonGenerator generator, SerializationContext ctx) {
    generator.writeStartObject();

    ParameterType parameterType = jobParameter.getType();
    generator.write(TYPE_KEY_NAME, parameterType.name());
    switch (parameterType) {
      case STRING:
        generator.write(VALUE_KEY_NAME, (String) jobParameter.getValue());
        break;
      case DATE:
        generator.write(VALUE_KEY_NAME, ((Date) jobParameter.getValue()).getTime());
        break;
      case LONG:
        generator.write(VALUE_KEY_NAME, ((Long) jobParameter.getValue()).longValue());
        break;
      case DOUBLE:
        generator.write(VALUE_KEY_NAME, ((Double) jobParameter.getValue()).doubleValue());
        break;
    }
    generator.write(IDENTIFYING_KEY_NAME, jobParameter.isIdentifying());

    generator.writeEnd();
  }


}
