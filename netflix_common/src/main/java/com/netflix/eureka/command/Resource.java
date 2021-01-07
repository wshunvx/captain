package com.netflix.eureka.command;

public class Resource {
	public enum RuleType {
		FLOW_RULE_TYPE, DEGRADE_RULE_TYPE, SYSTEM_RULE_TYPE, AUTHORITY_RULE_TYPE, GATEWAY_API_TYPE, GATEWAY_RULE_TYPE, PARAM_RULE_TYPE
    }
	
	private final RuleType ruleType;
	private final String hashKey;
	private final String id;
	
	public Resource(RuleType ruleType, String id) {
		this.ruleType = ruleType;
		this.id = id;
		this.hashKey = ruleType.name() + this.id;
	}

	public RuleType getRuleType() {
		return ruleType;
	}

	public String getHashKey() {
		return hashKey;
	}
	
	@Override
    public int hashCode() {
        String hashKey = getHashKey();
        return hashKey.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof Resource) {
            return getHashKey().equals(((Resource) other).getHashKey());
        } else {
            return false;
        }
    }
}
