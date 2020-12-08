package com.netflix.eureka.found.transport.builder;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.netflix.eureka.found.model.Namespace;

public interface DataJsonMixIn {
	@JsonIgnore
    List<Namespace> getData();
}
