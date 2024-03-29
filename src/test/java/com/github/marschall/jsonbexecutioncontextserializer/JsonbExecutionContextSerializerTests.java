package com.github.marschall.jsonbexecutioncontextserializer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.batch.core.repository.ExecutionContextSerializer;

import jakarta.json.bind.JsonbException;
import jakarta.json.bind.annotation.JsonbTypeDeserializer;
import jakarta.json.bind.annotation.JsonbTypeSerializer;
import jakarta.json.bind.serializer.DeserializationContext;
import jakarta.json.bind.serializer.JsonbDeserializer;
import jakarta.json.bind.serializer.JsonbSerializer;
import jakarta.json.bind.serializer.SerializationContext;
import jakarta.json.stream.JsonGenerator;
import jakarta.json.stream.JsonParser;
import jakarta.json.stream.JsonParser.Event;

class JsonbExecutionContextSerializerTests extends AbstractExecutionContextSerializerTests {

  private ExecutionContextSerializer serializer;

  @BeforeEach
  void onSetUp() {
    this.serializer = new JsonbExecutionContextSerializer();
  }

  @Test
  void mappedType() throws IOException {

    Person person = new Person();
    person.age = 28;
    person.name = "Bob";
    person.phone = new DomesticNumber();
    person.phone.areaCode = 555;
    person.phone.local = 1234567;

    Map<String, Object> context = new HashMap<>(1);
    context.put("person", person);

    ByteArrayOutputStream os = new ByteArrayOutputStream();
    this.serializer.serialize(context, os);

    InputStream in = new ByteArrayInputStream(os.toByteArray());

    Map<String, Object> deserialized = this.serializer.deserialize(in);
    Person deserializedPerson = (Person) deserialized.get("person");
    assertNotNull(deserializedPerson);
    assertNotNull(deserializedPerson.phone);
  }

  @Test
  void additionalTrustedClass() throws IOException {
    // given
    Map<String, Object> context = new HashMap<>(1);
    context.put("locale", Locale.getDefault());

    // when
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    this.serializer.serialize(context, outputStream);
    InputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
    Map<String, Object> deserializedContext = this.serializer.deserialize(inputStream);

    // then
    Locale locale = (Locale) deserializedContext.get("locale");
    assertNotNull(locale);
  }

  @Override
  protected ExecutionContextSerializer getSerializer() {
    return this.serializer;
  }

  public static class Person {
    public String name;
    public int age;

    @JsonbTypeSerializer(PhoneNumberSerializer.class)
    @JsonbTypeDeserializer(PhoneNumberSerializer.class)
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
  void unmappedType() throws IOException {

    UnmappedPerson person = new UnmappedPerson();
    person.age = 28;
    person.name = "Bob";
    person.phone = new UnmappedDomesticNumber();
    person.phone.areaCode = 555;
    person.phone.local = 1234567;

    Map<String, Object> context = new HashMap<>(1);
    context.put("person", person);

    ByteArrayOutputStream os = new ByteArrayOutputStream();
    this.serializer.serialize(context, os);

    InputStream in = new ByteArrayInputStream(os.toByteArray());

    assertThrows(Exception.class, () -> this.serializer.deserialize(in), "An exception should have been thrown but wasn't");
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

  static Stream<Arguments> nonPublicLists() {
    return Stream.of(
        arguments("Arrays.asList", Arrays.asList("foo", "bar")),
        arguments("List.of", List.of("foo", "bar")),
        arguments("Collections.singletonList", Collections.singletonList("foo")),
        arguments("Collections.unmodifiableList", Collections.unmodifiableList(new ArrayList<>(Collections.singletonList("foo"))))
    );
}

  @ParameterizedTest
  @MethodSource("nonPublicLists")
  void arrayAsListSerialization(String key, List<String> list) throws IOException {
    //given
    Map<String, Object> context = new HashMap<>(1);
    context.put(key, list);

    // when
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    this.serializer.serialize(context, outputStream);
    InputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
    Map<String, Object> deserializedContext = this.serializer.deserialize(inputStream);

    // then
    Object deserializedValue = deserializedContext.get(key);
    assertTrue(List.class.isAssignableFrom(deserializedValue.getClass()));
    assertTrue(((List<?>)deserializedValue).containsAll(list));
  }

  @Test
  void sqlTimestampSerialization() throws IOException {
    //given
    Map<String, Object> context = new HashMap<>(1);
    Timestamp timestamp = new Timestamp(Instant.now().toEpochMilli());
    context.put("timestamp", timestamp);

    // when
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    this.serializer.serialize(context, outputStream);
    InputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
    Map<String, Object> deserializedContext = this.serializer.deserialize(inputStream);

    // then
    Timestamp deserializedTimestamp = (Timestamp) deserializedContext.get("timestamp");
    assertEquals(timestamp, deserializedTimestamp);
  }

  public static void main(String[] args) {
    System.out.println(Instant.now().getEpochSecond());
  }

  @Test
  void scalarTypeSerialization() throws Exception {
    Map<String, Object> m1 = new HashMap<>();
    Instant instant = Instant.ofEpochSecond(1630348638L, 123456789L);
    LocalDate localDate = LocalDate.of(2021, 8, 30);
    LocalTime localTime = LocalTime.of(20, 38, 21, 123456789);
    LocalDateTime localDateTime = LocalDateTime.of(localDate, localTime);

    // jdk
    m1.put("string", "\u00C4");
    m1.put("util.date", new java.util.Date(System.currentTimeMillis()));
    m1.put("sql.date", java.sql.Date.valueOf("2021-08-30"));
    m1.put("sql.time", java.sql.Time.valueOf("20:38:21"));
    m1.put("timestamp", Timestamp.from(instant));
    m1.put("url", new URL("https://www.example.com"));
    m1.put("uri", new URI("https://www.example.com"));

    // java.time
    m1.put("localDate", localDate);
    m1.put("localTime", localTime);
    m1.put("localDateTime", localDateTime);
    m1.put("offsetDateTime", OffsetDateTime.of(localDateTime, ZoneOffset.ofHoursMinutes(2, 30)));
    m1.put("zonedDateTime", ZonedDateTime.of(localDateTime, ZoneId.of("Europe/Zurich")));
    m1.put("instant", instant);
    m1.put("duration", Duration.ofSeconds(1630348638L, 123456789L));
    m1.put("period", Period.of(1, 2, 3));

    // wrappers
    m1.put("byte", (byte) 1);
    m1.put("short", (short) 1);
    m1.put("integer", 1);
    m1.put("long", 1L);
    m1.put("double", 1.0d);
    m1.put("float", 1.0f);
    m1.put("bigDecimal", BigDecimal.ONE);
    m1.put("bigInteger", BigInteger.ONE);
    m1.put("boolean", true);

    Map<String, Object> m2 = this.serializationRoundTrip(m1);

    this.compareContexts(m1, m2);
  }

  public static class PhoneNumberSerializer implements JsonbSerializer<PhoneNumber>, JsonbDeserializer<PhoneNumber> {

    @Override
    public PhoneNumber deserialize(JsonParser parser, DeserializationContext ctx, Type rtType) {
      PhoneNumber phoneNumber;
      if (parser.next() != Event.KEY_NAME) {
        throw new JsonbException("KEY_NAME expected");
      }
      if (!parser.getString().equals("type")) {
        throw new JsonbException("type expected");
      }
      if (parser.next() != Event.VALUE_STRING) {
        throw new JsonbException("string expected");
      }
      String type = parser.getString();
      switch (type) {
      case "domestic":
        phoneNumber = new DomesticNumber();
        break;
      case "international":
        phoneNumber = new InternationalNumber();
        break;
      default:
        throw new JsonbException("unknown phone number type: " + type);
      }

      phoneNumber.areaCode = this.parseInt("area", parser);
      phoneNumber.local = this.parseInt("local", parser);
      if (phoneNumber instanceof InternationalNumber) {
        ((InternationalNumber) phoneNumber).countryCode = this.parseInt("countryCode", parser);
      }

      return phoneNumber;
    }

    private int parseInt(String key, JsonParser parser) {
      if (parser.next() != Event.KEY_NAME) {
        throw new JsonbException("KEY_NAME expected");
      }
      if (!parser.getString().equals(key)) {
        throw new JsonbException(key + " expected");
      }
      if (parser.next() != Event.VALUE_NUMBER) {
        throw new JsonbException("number expected");
      }
      return parser.getInt();
    }

    @Override
    public void serialize(PhoneNumber phoneNumber, JsonGenerator generator, SerializationContext ctx) {
      generator.writeStartObject();

      if (phoneNumber instanceof DomesticNumber) {
        generator.write("type", "domestic");
      } else if (phoneNumber instanceof InternationalNumber) {
        generator.write("type", "international");
      }

      generator.write("area", phoneNumber.areaCode);
      generator.write("local", phoneNumber.local);

      if (phoneNumber instanceof InternationalNumber) {
        generator.write("countryCode", ((InternationalNumber) phoneNumber).countryCode);
      }

      generator.writeEnd();

    }

  }

}
