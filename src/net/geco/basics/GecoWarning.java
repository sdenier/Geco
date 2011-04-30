/**
 * Copyright (c) 2010 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.basics;

/**
 * @author Simon Denier
 * @since Dec 15, 2010
 *
 */
public class GecoWarning extends Exception {

	public GecoWarning(String message, Throwable cause) {
		super(message, cause);
	}

	public GecoWarning(String message) {
		super(message);
	}

	public GecoWarning(Throwable cause) {
		super(cause);
	}

}
