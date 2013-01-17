/**
 * Copyright (c) 2013 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package test.net.geco.model.iojson;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import net.geco.model.iojson.JSONStore;

import org.json.JSONException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Simon Denier
 * @since Jan 5, 2013
 *
 */
public class JSONStoreTest {

	private JSONStore subject;
	
	@Before
	public void setUp() throws JSONException {
		subject = new JSONStore(10);
	}

	@Test
	public void register_returnsGivenObject() {
		Object o1 = new Object();
		Object o2 = subject.register(o1, 0);
		Assert.assertThat(o2, equalTo(o1));
	}

	@Test
	public void retrieve_returnsObjectRegisteredWithId() {
		Object o1 = new Object();
		subject.register(o1, 1);
		Assert.assertThat(subject.retrieve(1, Object.class), equalTo(o1));
	}

	@Test
	public void retrieve_returnsNullForIdZero() {
		Assert.assertThat(subject.retrieve(0, Object.class), nullValue());
	}

}
