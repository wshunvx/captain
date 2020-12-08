package com.netflix.eureka.http.utils;

import java.util.ArrayDeque;
import java.util.Deque;

import com.alibaba.csp.sentinel.Entry;

public class EntryUtils {
	Deque<EntryHolder> holders = new ArrayDeque<>();
	
	public EntryHolder pop() {
		return holders.pop();
	}
	
	public void setHolder(Entry entry, Object[] params) {
		holders.push(new EntryHolder(entry, params));
	}
	
	public boolean isEmpty() {
		return holders.isEmpty();
	}
	
	public class EntryHolder {

	    final private Entry entry;

	    final private Object[] params;

	    public EntryHolder(Entry entry, Object[] params) {
	        this.entry = entry;
	        this.params = params;
	    }

	    public Entry getEntry() {
	        return entry;
	    }

	    public Object[] getParams() {
	        return params;
	    }
	}
}
