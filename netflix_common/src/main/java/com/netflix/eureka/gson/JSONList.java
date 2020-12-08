package com.netflix.eureka.gson;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import com.google.gson.internal.$Gson$Types;

public class JSONList<T> implements ParameterizedType {
	
	final Type type;
	private Class<? super T> wrapped;
	
	public JSONList(Class<T> cls) {
		this.wrapped = cls;
		this.type = null; //getSuperclassTypeParameter(cls);
	}
	
	/**
	 * Returns the type from super class's type parameter in
	 */
	static Type getSuperclassTypeParameter(Class<?> subclass) {
		Type superclass = subclass.getGenericSuperclass();
		if (superclass instanceof Class) {
			throw new RuntimeException("Missing type parameter.");
		}
		ParameterizedType parameterized = (ParameterizedType) superclass;
		return $Gson$Types.canonicalize(parameterized.getActualTypeArguments()[0]);
	}
	
	@Override
	public Type[] getActualTypeArguments() {
		return new Type[] { wrapped };
	}

	@Override
	public Type getOwnerType() {
		return type;
	}

	@Override
	public Type getRawType() {
		return List.class;
	}

}
