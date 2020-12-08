package com.netflix.eureka.command;

import com.netflix.eureka.gson.JSONFormatter;

public class CommandResponse<R> {
	protected int code;
	protected R data;
    
	protected String desc;

    public CommandResponse() {
    	this(null);
    }
    
    public CommandResponse(R result) {
        this(result, 1000, null);
    }

    public CommandResponse(R result, int code, String desc) {
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
    public static <T> CommandResponse<T> ofSuccess(T result) {
        return new CommandResponse<T>(result);
    }

    /**
     * Construct a failed response with given exception.
     *
     * @param ex cause of the failure
     * @return constructed server response
     */
    public static <T> CommandResponse<T> ofFailure(String ex) {
        return new CommandResponse<T>(null, 1001, ex);
    }

    /**
     * Construct a failed response with given exception.
     *
     * @param ex     cause of the failure
     * @param result additional message of the failure
     * @return constructed server response
     */
    public static <T> CommandResponse<T> ofFailure(int code, String ex) {
        return new CommandResponse<T>(null, code, ex);
    }
    
    /**
     * Construct a failed response with given exception.
     *
     * @param ex     cause of the failure
     * @param result additional message of the failure
     * @return constructed server response
     */
    public static <T> CommandResponse<T> ofFailure(String ex, T result) {
        return new CommandResponse<T>(result, 1001, ex);
    }

    public boolean isSuccess() {
        return code == 1000;
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
