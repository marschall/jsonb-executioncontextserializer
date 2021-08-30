package com.github.marschall.jsonbexecutioncontextserializer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.repository.ExecutionContextSerializer;
import org.springframework.batch.core.repository.dao.Jackson2ExecutionContextStringSerializer;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

class JsonBExecutionContextStringSerializerTest extends AbstractExecutionContextSerializerTests {

  private ExecutionContextSerializer serializer;

  @BeforeEach
  void onSetUp() {
    this.serializer = new JsonBExecutionContextStringSerializer();
  }

  @Test
  public void mappedTypeTest() throws IOException {

    Person person = new Person();
    person.age = 28;
    person.name = "Bob";
    person.phone = new DomesticNumber();
    person.phone.areaCode = 555;
    person.phone.local = 1234567;

    ExecutionContextSerializer j = new JsonBExecutionContextStringSerializer();

    Map<String, Object> context = new HashMap<>(1);
    context.put("person", person);

    ByteArrayOutputStream os = new ByteArrayOutputStream();
    j.serialize(context, os);

    InputStream in = new ByteArrayInputStream(os.toByteArray());

    j.deserialize(in);
  }

  @Test
  public void testAdditionalTrustedClass() throws IOException {
    // given
    // FIXME
    Jackson2ExecutionContextStringSerializer serializer =
        new Jackson2ExecutionContextStringSerializer("java.util.Locale");
    Map<String, Object> context = new HashMap<>(1);
    context.put("locale", Locale.getDefault());

    // when
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    serializer.serialize(context, outputStream);
    InputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
    Map<String, Object> deserializedContext = serializer.deserialize(inputStream);

    // then
    Locale locale = (Locale) deserializedContext.get("locale");
    assertNotNull(locale);
  }

  @Override
  protected ExecutionContextSerializer getSerializer() {
    return this.serializer;
  }

  @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
  public static class Person {
    public String name;
    public int age;
    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
    public PhoneNumber phone;
  }

  public static abstract class PhoneNumber {
    public int areaCode, local;
  }

  public static class InternationalNumber extends PhoneNumber {
    public int countryCode;
  }

  public static class DomesticNumber extends PhoneNumber{}

  @Test
  public void unmappedTypeTest() throws IOException {

    UnmappedPerson person = new UnmappedPerson();
    person.age = 28;
    person.name = "Bob";
    person.phone = new UnmappedDomesticNumber();
    person.phone.areaCode = 555;
    person.phone.local = 1234567;

    ExecutionContextSerializer j = new JsonBExecutionContextStringSerializer();

    Map<String, Object> context = new HashMap<>(1);
    context.put("person", person);

    ByteArrayOutputStream os = new ByteArrayOutputStream();
    j.serialize(context, os);

    InputStream in = new ByteArrayInputStream(os.toByteArray());

    assertThrows(Exception.class, () -> j.deserialize(in), "An exception should have been thrown but wasn't");
  }

  public static class UnmappedPerson {
    public String name;
    public int age;
    public UnmappedPhoneNumber phone;
  }

  public static abstract class UnmappedPhoneNumber {
    public int areaCode, local;
  }

  public static class UnmappedInternationalNumber extends UnmappedPhoneNumber {
    public int countryCode;
  }

  public static class UnmappedDomesticNumber extends UnmappedPhoneNumber{}

  @Test
  public void arrayAsListSerializationTest() throws IOException {
    //given
    List<String> list = Arrays.asList("foo", "bar");
    String key = "Arrays.asList";
    Jackson2ExecutionContextStringSerializer serializer = new Jackson2ExecutionContextStringSerializer();
    Map<String, Object> context = new HashMap<>(1);
    context.put(key, list);

    // when
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    serializer.serialize(context, outputStream);
    InputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
    Map<String, Object> deserializedContext = serializer.deserialize(inputStream);

    // then
    Object deserializedValue = deserializedContext.get(key);
    assertTrue(List.class.isAssignableFrom(deserializedValue.getClass()));
    assertTrue(((List<String>)deserializedValue).containsAll(list));
  }

  @Test
  public void testSqlTimestampSerialization() throws IOException {
    //given
    Jackson2ExecutionContextStringSerializer serializer = new Jackson2ExecutionContextStringSerializer();
    Map<String, Object> context = new HashMap<>(1);
    Timestamp timestamp = new Timestamp(Instant.now().toEpochMilli());
    context.put("timestamp", timestamp);

    // when
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    serializer.serialize(context, outputStream);
    InputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
    Map<String, Object> deserializedContext = serializer.deserialize(inputStream);

    // then
    Timestamp deserializedTimestamp = (Timestamp) deserializedContext.get("timestamp");
    assertEquals(timestamp, deserializedTimestamp);
  }

}
