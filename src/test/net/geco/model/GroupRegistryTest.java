/**
 * Copyright (c) 2011 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package test.net.geco.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import net.geco.model.Group;
import net.geco.model.GroupRegistry;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * @author Simon Denier
 * @since Jun 3, 2011
 *
 */
public class GroupRegistryTest {

	private GroupRegistry<Group> registry;
	private Group group;

	@Before
	public void setUp(){
		group = Mockito.mock(Group.class);
		Mockito.when(group.getName()).thenReturn("sample");
		registry = new GroupRegistry<Group>();
	}
	
	@Test
	public void addGroup(){
		registry.add(group);
		assertEquals(1, registry.getGroups().size());
		assertEquals(group, registry.getGroups().iterator().next());
	}
	
	@Test
	public void findGroup(){
		registry.add(group);
		assertEquals(null, registry.find(""));
		assertEquals(group, registry.find("sample"));
	}
	
	@Test
	public void removeGroup(){
		addGroup();
		registry.remove(group);
		assertTrue(registry.getGroups().isEmpty());
		assertNull(registry.find(group.getName()));
	}
	
	@Test
	public void updateGroupName(){
		addGroup();
		
		Mockito.doAnswer(new Answer<Group>() {
			public Group answer(InvocationOnMock invocation) throws Throwable {
				Mockito.when(group.getName()).thenReturn("XXX");
				return null;
			}
		}).when(group).setName("XXX");
		
		registry.updateName(group, "XXX");
		Mockito.verify(group).setName("XXX");
		assertEquals(group, registry.find("XXX"));
		assertNull(registry.find("sample"));
	}
}
