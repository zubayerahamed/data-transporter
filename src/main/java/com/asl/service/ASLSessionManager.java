package com.asl.service;

import org.springframework.stereotype.Component;

/**
 * @author Zubayer Ahamed
 *
 * @since Dec 12, 2020
 */
//@Component
public interface ASLSessionManager {

	public void addToMap(String key, Object value);

	public Object getFromMap(String key);

	public void removeFromMap(String key);
}
