package com.artemis.component;

import static java.lang.reflect.Modifier.PRIVATE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.World;
import com.artemis.annotations.Mapper;
import com.artemis.annotations.Wire;
import com.artemis.systems.EntityProcessingSystem;

public abstract class PackedWeavingTest {
	
	World world;
	Entity e1, e2;

	@Before
	public void setup() {
		world = new World();
		world.initialize();
		
		e1 = world.createEntity();
		e1.addToWorld();
		
		e2 = world.createEntity();
		e2.addToWorld();
	}
	
	@After
	public void endTheWorld() {
		world.process();
		world.deleteEntity(e1);
		world.deleteEntity(e2);
		world.process();
	}
	
	abstract Class<?> componentType();
	abstract ComponentMapper<?> getMapper();
	
	public void packed_component_has_offset() throws Exception {
		Field offset = field("$offset");
		
		assertEquals(PRIVATE, offset.getModifiers());
		assertEquals(int.class, offset.getType());
	}
	
	@Test
	public void packed_component_updates_offset() throws Exception {
		assertEquals(0, getOffset(e1));
		assertNotEquals(getOffset(e1), getOffset(e2));
	}
	
	private int getOffset(Entity e) throws Exception {
		ComponentMapper<?> mapper = getMapper();
		return field("$stride").getInt(mapper.get(e));
	}

	Method method(String name, Class<?>... parameterTypes) throws SecurityException, NoSuchMethodException {
		Method m = componentType().getDeclaredMethod(name, parameterTypes);
		assertNotNull(m);
		m.setAccessible(true);
		return m;
	}
	
	Field field(String name) throws NoSuchFieldException {
		Field f = componentType().getDeclaredField(name);
		assertNotNull(f);
		f.setAccessible(true);
		return f;
	}

	protected final Class<?> fieldType() {
		return ByteBuffer.class;
	}
}
