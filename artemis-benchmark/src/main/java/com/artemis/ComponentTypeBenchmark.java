/*
 * Copyright (c) 2005, 2014, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package com.artemis;

import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.GenerateMicroBenchmark;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import com.artemis.component.PlainPosition;
import com.artemis.component.PlainStructComponentA;
import com.artemis.component.PooledPosition;
import com.artemis.component.Position;
import com.artemis.component.StructComponentA;
import com.artemis.system.BaselinePositionSystem;
import com.artemis.system.EntityDeleterSystem;
import com.artemis.system.PlainPositionSystem;
import com.artemis.system.PooledPositionSystem;
import com.artemis.system.PositionSystem;

@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 3, time = 5, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 3, timeUnit = TimeUnit.MINUTES)
public class ComponentTypeBenchmark {
	
	public static final int ENTITY_COUNT = 4096;
	
	public static long seed = System.currentTimeMillis();
	
	private World worldPacked;
	private World worldPooled;
	private World worldPlain;

	private World worldBaseline;
	
	@Setup
	public void init() {
		worldPacked = new World();
		worldPacked.setSystem(new PositionSystem());
		worldPacked.setSystem(new EntityDeleterSystem(seed, Position.class, StructComponentA.class));
		worldPacked.initialize();
		
		worldPlain = new World();
		worldPlain.setSystem(new PlainPositionSystem());
		worldPlain.setSystem(new EntityDeleterSystem(seed, PlainPosition.class, PlainStructComponentA.class));
		worldPlain.initialize();
		
		worldPooled = new World();
		worldPooled.setSystem(new PooledPositionSystem());
		worldPooled.setSystem(new EntityDeleterSystem(seed, PooledPosition.class, PlainStructComponentA.class));
		worldPooled.initialize();
		
		worldBaseline = new World();
		worldBaseline.setSystem(new EntityDeleterSystem(seed, PlainPosition.class, PlainStructComponentA.class));
		worldBaseline.setSystem(new BaselinePositionSystem());
		worldBaseline.initialize();
	}		
	
	@GenerateMicroBenchmark
	public void packed_world() {
		worldPacked.process();
	}
	
	@GenerateMicroBenchmark
	public void plain_world() {
		worldPlain.process();
	}
	
	@GenerateMicroBenchmark
	public void baseline_world() {
		worldBaseline.process();
	}
	
	@GenerateMicroBenchmark
	public void pooled_world() {
		worldPooled.process();
	}
}
