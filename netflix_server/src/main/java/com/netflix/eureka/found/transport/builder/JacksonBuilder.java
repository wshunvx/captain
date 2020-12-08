package com.netflix.eureka.found.transport.builder;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.netflix.eureka.found.model.Restresult;

public class JacksonBuilder<T> {

	private T data;
	
    @JsonProperty("data")
    public void withApplication(T data) {
        this.data = data;
    }

    @JsonAnySetter
    public void with(String fieldName, Object value) {
        if (fieldName == null || value == null) {
            return;
        }
    }

    public Restresult<T> build() {
    	return new Restresult<T>(data);
    }
}
