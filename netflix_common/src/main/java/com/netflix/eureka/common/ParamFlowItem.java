package com.netflix.eureka.common;

public class ParamFlowItem {
	private String object;
    private Integer count;
    private String classType;

    public ParamFlowItem() {}

    public ParamFlowItem(String object, Integer count, String classType) {
        this.object = object;
        this.count = count;
        this.classType = classType;
    }

    public static <T> ParamFlowItem newItem(T object, Integer count) {
        if (object == null) {
            throw new IllegalArgumentException("Invalid object: null");
        }
        return new ParamFlowItem(object.toString(), count, object.getClass().getName());
    }

    public String getObject() {
        return object;
    }

    public ParamFlowItem setObject(String object) {
        this.object = object;
        return this;
    }

    public Integer getCount() {
        return count;
    }

    public ParamFlowItem setCount(Integer count) {
        this.count = count;
        return this;
    }

    public String getClassType() {
        return classType;
    }

    public ParamFlowItem setClassType(String classType) {
        this.classType = classType;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        ParamFlowItem item = (ParamFlowItem)o;

        if (object != null ? !object.equals(item.object) : item.object != null) { return false; }
        if (count != null ? !count.equals(item.count) : item.count != null) { return false; }
        return classType != null ? classType.equals(item.classType) : item.classType == null;
    }

    @Override
    public int hashCode() {
        int result = object != null ? object.hashCode() : 0;
        result = 31 * result + (count != null ? count.hashCode() : 0);
        result = 31 * result + (classType != null ? classType.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ParamFlowItem{" +
            "object=" + object +
            ", count=" + count +
            ", classType='" + classType + '\'' +
            '}';
    }
}
