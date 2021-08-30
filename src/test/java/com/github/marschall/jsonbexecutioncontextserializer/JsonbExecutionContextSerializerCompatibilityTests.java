package com.github.marschall.jsonbexecutioncontextserializer;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.batch.core.jsr.partition.JsrPartitionHandler.PartitionPlanState;
import org.springframework.batch.core.repository.ExecutionContextSerializer;
import org.springframework.batch.core.repository.dao.Jackson2ExecutionContextStringSerializer;
import org.springframework.batch.item.ExecutionContext;

class JsonBExecutionContextStringSerializerCompatibilityTests {

  static List<ExecutionContextSerializer> serializers() {
    return List.of(new Jackson2ExecutionContextStringSerializer(), new JsonBExecutionContextStringSerializer());
  }

  @ParameterizedTest
  @MethodSource("serializers")
  void serialize(ExecutionContextSerializer serializer) {
    ExecutionContext stepContext = new ExecutionContext();
    stepContext.put("type", "step context");

    PartitionPlanState partitionPlanState = new PartitionPlanState();
//    partitionPlanState.setPartitionPlan(plan);

    stepContext.put("partitionPlanState", partitionPlanState);
    stepContext.put("batch.lastSteps", List.of("step1", "step2", "step3"));
    stepContext.put("batch.restart", true);

    ExecutionContext jobContext = new ExecutionContext();
    jobContext.putDouble("d", 1.23d);
    jobContext.putInt("i", 2);
    jobContext.putLong("l", 3L);
    jobContext.putString("s", "4");
    jobContext.putString("t", null);
    jobContext.put("u", null);
    jobContext.put("stepContext", stepContext);

    assertEquals("", serialize(jobContext, serializer));
  }

  private static String serialize(ExecutionContext executionContext, ExecutionContextSerializer serializer) {
    Map<String, Object> map = new HashMap<>();
    for (Entry<String, Object> me : executionContext.entrySet()) {
      map.put(me.getKey(), me.getValue());
    }

    ByteArrayOutputStream out = new ByteArrayOutputStream();

    try {
      serializer.serialize(map, out);
      return new String(out.toByteArray(), ISO_8859_1);
    } catch (IOException ioe) {
      throw new IllegalArgumentException("Could not serialize the execution context", ioe);
    }
  }

}
