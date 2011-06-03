/**
 * Copyright (c) 2011 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package test.net.geco.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import net.geco.model.Group;
import net.geco.model.GroupRegistry;

import org.junit.Assert;
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
		registry = new GroupRegistry<Group>();
		group = factory("sample");
	}

	public Group factory(String name) {
		Group group = Mockito.mock(Group.class);
		Mockito.when(group.getName()).thenReturn(name);
		return group;
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
		whenRenamingGroup(group, "XXX");
		
		registry.updateName(group, "XXX");
		Mockito.verify(group).setName("XXX");
		assertEquals(group, registry.find("XXX"));
		assertNull(registry.find("sample"));
	}
	
	@Test
	public void getGroupNames(){
		registry.add(group);
		registry.add(factory("sample2"));
		
		List<String> names = registry.getNames();
		assertEquals(2, names.size());
		assertTrue( names.contains("sample") );
		assertTrue( names.contains("sample2"));
	}
	
	@Test
	public void getSortedGroups(){
		Group a = factory("A");
		Group b = factory("B");
		Group c = factory("C");
		Group d = factory("D");
		registry.add(b);
		registry.add(d);
		registry.add(c);
		registry.add(a);
		
		List<Group> groups = registry.getSortedGroups();
		Assert.assertArrayEquals(new Group[]{ a,b,c,d }, groups.toArray());
	}
	
	@Test
	public void getSortedNames(){
		Group a = factory("A");
		Group b = factory("B");
		Group c = factory("C");
		Group d = factory("D");
		registry.add(b);
		registry.add(d);
		registry.add(c);
		registry.add(a);
		
		List<String> groupNames = registry.getSortedNames();
		Assert.assertArrayEquals(new String[]{ "A","B","C","D" }, groupNames.toArray());
	}
	
	@Test
	public void getSortedNamesAfterAdd(){
		Group a = factory("A");
		Group b = factory("B");
		Group c = factory("C");
		Group d = factory("D");
		registry.add(b);
		registry.add(d);
		registry.add(a);
		
		List<String> groupNames = registry.getSortedNames();
		Assert.assertArrayEquals(new String[]{ "A","B","D" }, groupNames.toArray());

		registry.add(c);
		groupNames = registry.getSortedNames();
		Assert.assertArrayEquals(new String[]{ "A","B","C","D" }, groupNames.toArray());	
	}
	
	@Test
	public void getSortedNamesAfterRemove(){
		Group a = factory("A");
		Group b = factory("B");
		Group c = factory("C");
		Group d = factory("D");
		registry.add(b);
		registry.add(d);
		registry.add(c);
		registry.add(a);
		
		List<String> groupNames = registry.getSortedNames();
		Assert.assertArrayEquals(new String[]{ "A","B","C","D" }, groupNames.toArray());

		registry.remove(b);
		groupNames = registry.getSortedNames();
		Assert.assertArrayEquals(new String[]{ "A","C","D" }, groupNames.toArray());	
	}
	
	@Test
	public void getSortedNamesAfterRename(){
		Group b = factory("B");
		Group c = factory("C");
		registry.add(c);
		registry.add(b);
		
		List<String> groupNames = registry.getSortedNames();
		Assert.assertArrayEquals(new String[]{ "B","C" }, groupNames.toArray());

		whenRenamingGroup(c, "A");
		registry.updateName(c, "A");
		groupNames = registry.getSortedNames();
		Assert.assertArrayEquals(new String[]{ "A","B" }, groupNames.toArray());	
	}

	private void whenRenamingGroup(final Group group, final String newName) {
		Mockito.doAnswer(new Answer<Group>() {
			public Group answer(InvocationOnMock invocation) throws Throwable {
				Mockito.when(group.getName()).thenReturn(newName);
				return null;
			}
		}).when(group).setName(newName);
	}
}
