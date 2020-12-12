package com.asl.service.impl;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;

import com.asl.service.ASLSessionManager;

/**
 * @author Zubayer Ahamed
 *
 * @since Dec 12, 2020
 */
@Service
public class ASLSessionManagerImpl implements ASLSessionManager {

	private Map<String, Object> sessionMap;

	public ASLSessionManagerImpl() {
		this.sessionMap = new HashMap<>();
	}

	@Override
	public void addToMap(String key, Object value) {
		sessionMap.put(key, value);
	}

	@Override
	public Object getFromMap(String key) {
		return sessionMap.get(key);
	}

	@Override
	public void removeFromMap(String key) {
		if (sessionMap.containsKey(key))
			sessionMap.remove(key);
	}

}
