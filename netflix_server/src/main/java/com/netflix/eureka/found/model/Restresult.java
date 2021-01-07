package com.netflix.eureka.found.model;

import com.netflix.discovery.provider.Serializer;
import com.netflix.eureka.gson.JSONFormatter;

@Serializer
public class Restresult<R> {
	protected int code;
	protected R data;
    
	protected String desc;
	
	public Restresult() {}
    
	public Restresult(R result, int code, String desc) {
        this.code = code;
        this.data = result;
        this.desc = desc;
    }
	
	/**
     * Construct a successful response with given object.
     *
     * @param result result object
     * @param <T>    type of the result
     * @return constructed server response
     */
    public static <T> Restresult<T> ofSuccess(T result) {
        return new Restresult<T>(result, 1000, "");
    }

    /**
     * Construct a failed response with given exception.
     *
     * @param ex cause of the failure
     * @return constructed server response
     */
    public static <T> Restresult<T> ofFailure(String ex) {
        return new Restresult<T>(null, 1001, ex);
    }

    /**
     * Construct a failed response with given exception.
     *
     * @param ex     cause of the failure
     * @param result additional message of the failure
     * @return constructed server response
     */
    public static <T> Restresult<T> ofFailure(int code, String ex) {
        return new Restresult<T>(null, code, ex);
    }

	public int getCode() {
		return code;
	}

	public R getData() {
		return data;
	}

	public String getDesc() {
		return desc;
	}

	@Override
	public String toString() {
		return JSONFormatter.toJSON(this);
	}
	
	
}
