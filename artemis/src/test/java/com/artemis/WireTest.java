package com.artemis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;

import com.artemis.annotations.Wire;
import com.artemis.component.ComponentX;
import com.artemis.component.ComponentY;
import com.artemis.managers.TagManager;
import com.artemis.systems.EntityProcessingSystem;
import com.artemis.systems.VoidEntitySystem;

public class WireTest {
	
	private World world;
	
	private MappedSystem mappedSystem;
	private MappedSystemAll mappedSystemAll;
	private ExtendedSystem extendedSystem;
	private MappedManager mappedManager;
	private MappedManagerAll mappedManagerAll;
	private ExtendedManager extendedManager;
	
	private Entity entity;

	@Before
	public void init() {
		world = new World();
		mappedSystem = world.setSystem(new MappedSystem());
		mappedSystemAll = world.setSystem(new MappedSystemAll());
		extendedSystem = world.setSystem(new ExtendedSystem());
		mappedManager = world.setManager(new MappedManager());
		mappedManagerAll = world.setManager(new MappedManagerAll());
		extendedManager = world.setManager(new ExtendedManager());
		world.setManager(new TagManager());
		
		
		world.initialize();
		
		entity = world.createEntity();
		entity.createComponent(ComponentX.class);
		entity.createComponent(ComponentY.class);
		entity.addToWorld();
		
		world.process();
	}
	
	@Test
	public void systems_support_wire_annotation() {
		assertNotNull(mappedSystem.x);
		assertNotNull(mappedSystem.y);
		assertNotNull(mappedSystem.tagManager);
		assertNotNull(mappedSystem.mappedSystemAll);
		assertNotNull(extendedSystem.x);
		assertNotNull(extendedSystem.y);
		
		assertEquals(ComponentX.class, mappedSystem.x.get(entity).getClass());
		assertEquals(ComponentY.class, mappedSystem.y.get(entity).getClass());
	}
	
	@Test
	public void managers_support_wire_annotation() {
		assertNotNull(mappedManager.x);
		assertNotNull(mappedManager.y);
		assertNotNull(mappedManager.tagManager);
		assertNotNull(mappedManager.mappedSystem);
		
		assertEquals(ComponentX.class, mappedSystem.x.get(entity).getClass());
		assertEquals(ComponentY.class, mappedSystem.y.get(entity).getClass());
	}
	
	@Test
	public void systems_all_support_wire_annotation() {
		assertNotNull(mappedSystemAll.x);
		assertNotNull(mappedSystemAll.y);
		assertNotNull(mappedSystemAll.tagManager);
		assertNotNull(mappedSystemAll.mappedSystem);
		
		assertEquals(ComponentX.class, mappedSystem.x.get(entity).getClass());
		assertEquals(ComponentY.class, mappedSystem.y.get(entity).getClass());
	}
	
	@Test
	public void managers_all_support_wire_annotation() {
		assertNotNull(mappedManagerAll.x);
		assertNotNull(mappedManagerAll.y);
		assertNotNull(mappedManagerAll.tagManager);
		assertNotNull(mappedManagerAll.mappedSystem);
		assertNotNull(extendedManager.x);
		assertNotNull(extendedManager.y);
		
		assertEquals(ComponentX.class, mappedSystem.x.get(entity).getClass());
		assertEquals(ComponentY.class, mappedSystem.y.get(entity).getClass());
	}
	
	@Test(expected=NullPointerException.class)
	public void ensure_inherited_systems_not_injected() {
		World world = new World();
		world.setSystem(new FailingSystem());
		world.initialize();
	}
	
	@Test
	public void ensure_inherited_managers_not_injected() {
		World world = new World();
		FailingSystem failingSystem = world.setSystem(new FailingSystem());
		FailingManager failingManager = world.setManager(new FailingManager());
		world.initialize();
		
		assertNull(failingManager.x);
		assertNull(failingSystem.x);
	}
	
	@Test(expected=NullPointerException.class)
	public void fail_on_system_not_injected() {
		World world = new World();
		FailingNpeSystem failingSystem = world.setSystem(new FailingNpeSystem());
		world.initialize();
	}
	
	@Test(expected=NullPointerException.class)
	public void fail_on_manager_not_injected() {
		World world = new World();
		FailingNpeManager failingManager = world.setManager(new FailingNpeManager());
		world.initialize();
	}
	
	@Wire
	private static class MappedSystemAll extends EntityProcessingSystem {
		private ComponentMapper<ComponentX> x;
		private ComponentMapper<ComponentY> y;
		private TagManager tagManager;
		private MappedSystem mappedSystem;
		
		@SuppressWarnings("unchecked")
		public MappedSystemAll() {
			super(Aspect.getAspectForAll(ComponentX.class, ComponentY.class));
		}
		
		@Override
		protected void process(Entity e) {}
	}
	
	private static class MappedSystem extends EntityProcessingSystem {
		@Wire private ComponentMapper<ComponentX> x;
		@Wire private ComponentMapper<ComponentY> y;
		@Wire private TagManager tagManager;
		@Wire private MappedSystemAll mappedSystemAll;
		
		@SuppressWarnings("unchecked")
		public MappedSystem() {
			super(Aspect.getAspectForAll(ComponentX.class, ComponentY.class));
		}

		@Override
		protected void process(Entity e) {}
	}
	
	private static class MappedManager extends Manager {
		@Wire private ComponentMapper<ComponentX> x;
		@Wire private ComponentMapper<ComponentY> y;
		@Wire private MappedSystem mappedSystem;
		@Wire private TagManager tagManager;
	}
	
	@Wire
	private static class MappedManagerAll extends Manager {
		private ComponentMapper<ComponentX> x;
		private ComponentMapper<ComponentY> y;
		private MappedSystem mappedSystem;
		private TagManager tagManager;
	}
	
	private static class BaseManager extends Manager {
		protected ComponentMapper<ComponentX> x;
	}
	
	@Wire(injectInherited=true)
	private static class ExtendedManager extends BaseManager {
		private ComponentMapper<ComponentY> y;
	}
	
	@Wire
	private static class FailingManager extends BaseManager {
		private ComponentMapper<ComponentY> y;
	}
	
	private static abstract class BaseSystem extends VoidEntitySystem {
		protected ComponentMapper<ComponentX> x;
	}
	
	@Wire(injectInherited=true)
	private static class ExtendedSystem extends BaseSystem {
		private ComponentMapper<ComponentY> y;
		
		@Override
		protected void processSystem() {}
	}
	
	@Wire
	private static class FailingSystem extends BaseSystem {
		private FailingManager manager;
		
		@Override
		protected void processSystem() {}
	}
	
	@Wire
	private static class FailingNpeSystem extends VoidEntitySystem {
		private FailingManager manager;
		
		@Override
		protected void processSystem() {}
	}
	
	@Wire
	private static class FailingNpeManager extends Manager {
		private FailingSystem fail;
	}
}
