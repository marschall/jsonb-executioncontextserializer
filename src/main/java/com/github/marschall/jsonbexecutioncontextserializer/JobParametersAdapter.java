package com.github.marschall.jsonbexecutioncontextserializer;

import java.util.Map;

import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;

import jakarta.json.bind.adapter.JsonbAdapter;

final class JobParametersAdapter implements JsonbAdapter<JobParameters, Map<String, JobParameter<?>>> {

  @Override
  public Map<String, JobParameter<?>> adaptToJson(JobParameters jobParameters) {
    return jobParameters.getParameters();
  }

  @Override
  public JobParameters adaptFromJson(Map<String, JobParameter<?>> map) {
    return new JobParameters(map);
  }

}
