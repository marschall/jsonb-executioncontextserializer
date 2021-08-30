package com.github.marschall.jsonbexecutioncontextserializer;

import java.util.Locale;

import javax.json.bind.adapter.JsonbAdapter;

public final class LocaleAdapter implements JsonbAdapter<Locale, String> {

  @Override
  public String adaptToJson(Locale locale) {
    if (locale == null) {
      return null;
    }
    return locale.toString();
  }

  @Override
  public Locale adaptFromJson(String s) {
    if (s == null) {
      return null;
    }
    if (s.startsWith("_")) {
      throw new IllegalArgumentException("unsupported locale format: " + s);
    }
    String[] parts = s.split("_");
    switch (parts.length) {
      case 1:
        return new Locale(parts[0]);
      case 2:
        return new Locale(parts[0], parts[1]);
      case 3:
        return new Locale(parts[0], parts[1], parts[2]);
      default:
        throw new IllegalArgumentException("unsupported locale format: " + s);
    }
  }

}
