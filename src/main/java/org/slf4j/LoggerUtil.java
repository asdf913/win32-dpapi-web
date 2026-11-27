package org.slf4j;

public interface LoggerUtil {

	static void error(final Logger instance, final String msg, final Throwable t) {
		if (instance != null) {
			instance.error(msg, t);
		}
	}

}