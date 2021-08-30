package com.github.marschall.jsonbexecutioncontextserializer;

import java.lang.reflect.Type;
import java.util.Date;

import javax.json.bind.serializer.DeserializationContext;
import javax.json.bind.serializer.JsonbDeserializer;
import javax.json.bind.serializer.JsonbSerializer;
import javax.json.bind.serializer.SerializationContext;
import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonParser;

import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameter.ParameterType;

public final class JobParameterSerializer implements JsonbSerializer<JobParameter>, JsonbDeserializer<JobParameter> {

  private static final String IDENTIFYING_KEY_NAME = "identifying";
  private static final String TYPE_KEY_NAME = "type";
  private static final String VALUE_KEY_NAME = "value";

  @Override
  public JobParameter deserialize(JsonParser parser, DeserializationContext ctx, Type rtType) {
    // TODO Auto-generated method stub
    ParameterType parameterType = ParameterType.STRING;
    boolean identifying = true;
    String value = "n/a";
    return new JobParameter(value, identifying);
  }

  @Override
  public void serialize(JobParameter jobParameter, JsonGenerator generator, SerializationContext ctx) {

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
  }


}
