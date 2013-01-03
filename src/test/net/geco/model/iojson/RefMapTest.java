/**
 * Copyright (c) 2013 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package test.net.geco.model.iojson;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import net.geco.model.iojson.RefMap;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Simon Denier
 * @since Jan 3, 2013
 *
 */
public class RefMapTest {

	private RefMap subject;
	
	public RefMap subject() {
		return subject;
	}
	
	@Before
	public void setUp() {
		this.subject = new RefMap(10);
	}
	
	@Test
	public void registersObjectAtGivenId(){
		Object object = new Object();
		subject().put(1, object);
		assertThat(subject().get(1), equalTo(object));
	}
	
	@Test
	public void alwaysReturnsSameObjectAtGivenId() {
		Object object = new Object();
		subject().put(2, object);
		Object o2 = subject().get(2);
		assertThat(subject().get(2), equalTo(o2));
		
	}
	
	@Test
	public void returnsNullAtEmptyIndex(){
		assertThat(subject().get(0), nullValue());
	}
	
	@Test(expected=IndexOutOfBoundsException.class)
	public void throwsExceptionForOutOfBoundsIndex(){
		subject.get(11);
	}
	
}
