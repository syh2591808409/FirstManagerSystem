package com.first.project.common.domain;

import java.util.HashMap;

public class FirstResponse extends HashMap<String,Object> {
    private static final long serialVersionUID = -1749937097125709265L;

    public FirstResponse message(String message) {
        this.put("message", message);
        return this;
    }

    public FirstResponse data(Object data) {
        this.put("data", data);
        return this;
    }

    @Override
    public FirstResponse put(String key, Object value) {
        super.put(key, value);
        return this;
    }
}
