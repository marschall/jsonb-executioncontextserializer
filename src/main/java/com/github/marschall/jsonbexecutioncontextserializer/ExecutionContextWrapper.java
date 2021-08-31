package com.github.marschall.jsonbexecutioncontextserializer;

import java.util.Map;

final class ExecutionContextWrapper {
  
  private final Map<String, Object> map;

  ExecutionContextWrapper(Map<String, Object> map) {
    this.map = map;
  }

  Map<String, Object> getMap() {
    return map;
  }

}
