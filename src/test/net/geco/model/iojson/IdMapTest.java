/**
 * Copyright (c) 2013 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package test.net.geco.model.iojson;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import net.geco.model.iojson.IdMap;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Simon Denier
 * @since Jan 3, 2013
 *
 */
public class IdMapTest {

	private IdMap subject;
	
	public IdMap subject() {
		return subject;
	}

	@Before
	public void setUp() {
		subject = new IdMap();
	}
	
	@Test
	public void idFor_returnsIdForAnyGivenObject() {
		int id = subject().idFor(new Object());
		assertThat(id, instanceOf(Integer.class));
	}

	@Test
	public void idFor_returnsDifferentIdsForDifferentObjects() {
		Object object1 = new Object();
		Object object2 = new Object();
		int id1 = subject().idFor(object1);
		int id2 = subject().idFor(object2);
		
		assertThat(id2, not(equalTo(id1)));
	}
	
	@Test
	public void idFor_alwaysReturnsSameIdForSameObject() {
		Object object = new Object();
		int id = subject().idFor(object);
		assertThat(subject().idFor(object), equalTo(id));
	}

	@Test
	public void findId_returnsExistingId() {
		Object object = new Object();
		int id  = subject().idFor(object);
		assertThat(subject().findId(object), equalTo(id));
	}

	@Test(expected=NullPointerException.class)
	public void findId_throwNullPointerExceptionForUnknownObject() {
		subject().findId(new Object());
	}

	@Test
	public void maxIdReturnsZeroAfterInit() {
		assertThat(subject().maxId(), equalTo(0));
	}

	@Test
	public void maxIdReturnsTheLastGivenId() {
		int id = subject().idFor(new Object());
		assertThat(subject().maxId(), equalTo(id));
	}

}
