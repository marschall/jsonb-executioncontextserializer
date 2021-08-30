package com.github.marschall.jsonbexecutioncontextserializer;

import java.util.Map;

import javax.json.bind.adapter.JsonbAdapter;

import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;

public final class JobParametersAdapter implements JsonbAdapter<JobParameters, Map<String, JobParameter>> {

  @Override
  public Map<String, JobParameter> adaptToJson(JobParameters jobParameters) {
    return jobParameters.getParameters();
  }

  @Override
  public JobParameters adaptFromJson(Map<String, JobParameter> map) {
    return new JobParameters(map);
  }


}
