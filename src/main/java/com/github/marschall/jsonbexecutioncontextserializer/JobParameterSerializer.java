package com.github.marschall.jsonbexecutioncontextserializer;

import java.lang.reflect.Type;
import java.util.Date;

import javax.json.bind.serializer.DeserializationContext;
import javax.json.bind.serializer.JsonbDeserializer;
import javax.json.bind.serializer.JsonbSerializer;
import javax.json.bind.serializer.SerializationContext;
import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameter.ParameterType;

public final class JobParameterSerializer implements JsonbSerializer<JobParameter>, JsonbDeserializer<JobParameter> {

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

        // Move to json value
        parser.next();

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
            identifying = Boolean.valueOf(parser.getString());
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
