package com.github.marschall.jsonbexecutioncontextserializer;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.batch.core.repository.ExecutionContextSerializer;
import org.springframework.batch.core.repository.dao.Jackson2ExecutionContextStringSerializer;

public class JacksonExecutionContextSerializerTests extends AbstractExecutionContextSerializerTests {

  private ExecutionContextSerializer serializer;

  @BeforeEach
  void onSetUp() {
    this.serializer = new Jackson2ExecutionContextStringSerializer();
  }

  @Override
  protected ExecutionContextSerializer getSerializer() {
    return this.serializer;
  }

}
