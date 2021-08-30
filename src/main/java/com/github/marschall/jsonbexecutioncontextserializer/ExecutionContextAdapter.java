package com.github.marschall.jsonbexecutioncontextserializer;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.json.bind.adapter.JsonbAdapter;

import org.springframework.batch.item.ExecutionContext;

public final class ExecutionContextAdapter implements JsonbAdapter<ExecutionContext, Map<String, Object>> {

  @Override
  public Map<String, Object> adaptToJson(ExecutionContext executionContext) {
    if (executionContext.isEmpty()) {
      return Collections.emptyMap();
    }
    Map<String, Object> map = new HashMap<>(executionContext.size());
    for (Map.Entry<String, Object> entry : executionContext.entrySet()) {
      map.put(entry.getKey(), entry.getValue());
    }
    return map;
  }

  @Override
  public ExecutionContext adaptFromJson(Map<String, Object> map) {
    return new ExecutionContext(map);
  }

}
