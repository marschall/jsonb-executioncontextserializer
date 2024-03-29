package com.github.marschall.jsonbexecutioncontextserializer;

import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.junit.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.junit.jupiter.api.Test;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.repository.ExecutionContextSerializer;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Abstract test class for {@code ExecutionContextSerializer} implementations. Provides a minimum on test methods
 * that should pass for each {@code ExecutionContextSerializer} implementation.
 *
 * @author Thomas Risberg
 * @author Michael Minella
 * @author Marten Deinum
 * @author Mahmoud Ben Hassine
 */
public abstract class AbstractExecutionContextSerializerTests {

  @Test
  void serializeAMap() throws Exception {
    Map<String, Object> m1 = new HashMap<>();
    m1.put("object1", Long.valueOf(12345L));
    m1.put("object2", "OBJECT TWO");
    // Use a date after 1971 (otherwise daylight saving screws up)...
    m1.put("object3", new Date(123456790123L));
    m1.put("object4", 1234567.1234D);

    Map<String, Object> m2 = this.serializationRoundTrip(m1);

    this.compareContexts(m1, m2);
  }

  @Test
  void serializeStringJobParameter() throws Exception {
    Map<String, Object> m1 = new HashMap<>();
    m1.put("name", new JobParameter<>("foo", String.class));

    Map<String, Object> m2 = this.serializationRoundTrip(m1);

    this.compareContexts(m1, m2);
  }

  @Test
  void serializeDateJobParameter() throws Exception {
    Map<String, Object> m1 = new HashMap<>();
    m1.put("birthDate", new JobParameter<>(new Date(123456790123L), Date.class));

    Map<String, Object> m2 = this.serializationRoundTrip(m1);

    this.compareContexts(m1, m2);
  }

  @Test
  void serializeLocalDateJobParameter() throws Exception {
    Map<String, Object> m1 = new HashMap<>();
    m1.put("birthDate", new JobParameter<>(LocalDate.of(1973, 11, 29), LocalDate.class));

    Map<String, Object> m2 = this.serializationRoundTrip(m1);

    this.compareContexts(m1, m2);
  }

  @Test
  void serializeDoubleJobParameter() throws Exception {
    Map<String, Object> m1 = new HashMap<>();
    m1.put("weight", new JobParameter<>(80.5D, Double.class));

    Map<String, Object> m2 = this.serializationRoundTrip(m1);

    this.compareContexts(m1, m2);
  }

  @Test
  void serializeLongJobParameter() throws Exception {
    Map<String, Object> m1 = new HashMap<>();
    m1.put("age", new JobParameter<>(20L, Long.class));

    Map<String, Object> m2 = this.serializationRoundTrip(m1);

    this.compareContexts(m1, m2);
  }

  @Test
  void serializeNonIdentifyingJobParameter() throws Exception {
    Map<String, Object> m1 = new HashMap<>();
    m1.put("name", new JobParameter<>("foo", String.class, false));

    Map<String, Object> m2 = this.serializationRoundTrip(m1);

    this.compareContexts(m1, m2);
  }

  @Test
  void serializeJobParameters() throws Exception {
    Map<String, JobParameter<?>> jobParametersMap = new HashMap<>();
    jobParametersMap.put("paramName", new JobParameter<>("paramValue", String.class));

    Map<String, Object> m1 = new HashMap<>();
    m1.put("params", new JobParameters(jobParametersMap));

    Map<String, Object> m2 = this.serializationRoundTrip(m1);

    this.compareContexts(m1, m2);
  }

  @Test
  void serializeEmptyJobParameters() throws IOException {
    Map<String, Object> m1 = new HashMap<>();
    m1.put("params", new JobParameters());

    Map<String, Object> m2 = this.serializationRoundTrip(m1);

    this.compareContexts(m1, m2);
  }

  @Test
  void complexObject() throws Exception {
    Map<String, Object> m1 = new HashMap<>();
    ComplexObject o1 = new ComplexObject();
    o1.setName("02345");
    Map<String, Object> m = new HashMap<>();
    m.put("object1", BigDecimal.valueOf(12345L)); // Yasson converts Long to BigDecimal
    m.put("object2", "OBJECT TWO");
    o1.setMap(m);
    o1.setNumber(new BigDecimal("12345.67"));
    ComplexObject o2 = new ComplexObject();
    o2.setName("Inner Object");
    o2.setMap(m);
    o2.setNumber(new BigDecimal("98765.43"));
    o1.setObj(o2);
    m1.put("co", o1);

    Map<String, Object> m2 = this.serializationRoundTrip(m1);

    this.compareContexts(m1, m2);
  }

  @Test
  void nullSerialization() throws Exception {
    assertThrows(IllegalArgumentException.class, () -> this.getSerializer().serialize(null, null));
  }

  protected void compareContexts(Map<String, Object> m1, Map<String, Object> m2) {
    assertEquals(m1.size(), m2.size());

    for (Map.Entry<String, Object> entry : m1.entrySet()) {
      assertThat(m2, hasEntry(entry.getKey(), entry.getValue()));
    }
  }

  protected Map<String, Object> serializationRoundTrip(Map<String, Object> m1) throws IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    this.getSerializer().serialize(m1, out);

    InputStream in = new ByteArrayInputStream(out.toByteArray());
    return this.getSerializer().deserialize(in);
  }


  protected abstract ExecutionContextSerializer getSerializer();

  @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
  public static class ComplexObject implements Serializable {
    private static final long serialVersionUID = 1L;
    private String name;
    private BigDecimal number;
    private ComplexObject obj;
    private Map<String, Object> map;

    public String getName() {
      return this.name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public BigDecimal getNumber() {
      return this.number;
    }

    public void setNumber(BigDecimal number) {
      this.number = number;
    }

    public ComplexObject getObj() {
      return this.obj;
    }

    public void setObj(ComplexObject obj) {
      this.obj = obj;
    }

    public Map<String,Object> getMap() {
      return this.map;
    }

    public void setMap(Map<String,Object> map) {
      this.map = map;
    }


    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if ((o == null) || (this.getClass() != o.getClass())) {
        return false;
      }

      ComplexObject that = (ComplexObject) o;

      if (!Objects.equals(this.map, that.map)) {
        return false;
      }
      if (!Objects.equals(this.name, that.name)) {
        return false;
      }
      if (!Objects.equals(this.number, that.number)) {
        return false;
      }
      if (!Objects.equals(this.obj, that.obj)) {
        return false;
      }

      return true;
    }

    @Override
    public int hashCode() {
      int result;
      result = Objects.hashCode(this.name);
      result = (31 * result) + Objects.hashCode(this.number);
      result = (31 * result) + Objects.hashCode(this.obj);
      result = (31 * result) + Objects.hashCode(this.map);
      return result;
    }

    @Override
    public String toString() {
      return "ComplexObject [name=" + this.name + ", number=" + this.number + "]";
    }
  }

}