/*
 * Copyright (c) 2008-2019, Hazelcast, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hazelcast.scheduledexecutor.impl;

import com.hazelcast.nio.Address;
import com.hazelcast.scheduledexecutor.ScheduledTaskHandler;
import com.hazelcast.test.HazelcastParallelClassRunner;
import com.hazelcast.test.annotation.ParallelTest;
import com.hazelcast.test.annotation.QuickTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(HazelcastParallelClassRunner.class)
@Category({QuickTest.class, ParallelTest.class})
public class ScheduledTaskHandlerImplTest {

    @Test(expected = NullPointerException.class)
    public void of_withNull() {
        ScheduledTaskHandler.of(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void of_withWrongURN() {
        ScheduledTaskHandler.of("iamwrong");
    }

    @Test(expected = IllegalArgumentException.class)
    public void of_withWrongBase() {
        ScheduledTaskHandler.of("wrongbase:-\u00000\u0000Scheduler\u0000Task");
    }

    @Test(expected = IllegalArgumentException.class)
    public void of_withWrongParts() {
        ScheduledTaskHandler.of("urn:hzScheduledTaskHandler:-\u00000\u0000Scheduler");
    }

    @Test
    public void of_withValidPartition() {
        ScheduledTaskHandler handler = ScheduledTaskHandler.of("urn:hzScheduledTaskHandler:-\u00000\u0000Scheduler\u0000Task");
        assertTrue(handler.isAssignedToPartition());
        assertEquals(0, handler.getPartitionId());
        assertNull(handler.getAddress());
        assertEquals("Scheduler", handler.getSchedulerName());
        assertEquals("Task", handler.getTaskName());
    }

    @Test
    public void of_withValidAddress() throws Exception {
        Address addr = new Address("127.0.0.1", 0);
        ScheduledTaskHandler handler = ScheduledTaskHandler.of("urn:hzScheduledTaskHandler:127.0.0.1:0 -1 Scheduler Task");
        assertTrue(handler.isAssignedToMember());
        assertEquals(-1, handler.getPartitionId());
        assertEquals(addr, handler.getAddress());
        assertEquals("Scheduler", handler.getSchedulerName());
        assertEquals("Task", handler.getTaskName());
    }

    @Test(expected = IllegalArgumentException.class)
    public void of_withInvalidAddress() {
        ScheduledTaskHandler.of("urn:hzScheduledTaskHandler:foobar:0 -1 Scheduler Task");
    }

    @Test
    public void of_toURN() {
        String initialURN = "urn:hzScheduledTaskHandler:127.0.0.1:0 -1 Scheduler Task";
        assertEquals(initialURN, ScheduledTaskHandler.of(initialURN).toUrn());
    }

    @Test
    public void of_toURN_withIPv6() throws Exception {
        Address address = new Address("fe80:9a5a:ebff:fe8e::f228", 5701);
        ScheduledTaskHandler expected = ScheduledTaskHandlerImpl.of(address, "scheduler", "task");
        ScheduledTaskHandler actual = ScheduledTaskHandlerImpl.of(expected.toUrn());
        assertEquals(expected, actual);
    }

    @Test
    public void of_equality() {
        String initialURN = "urn:hzScheduledTaskHandler:127.0.0.1:0 -1 Scheduler Task";
        assertEquals(ScheduledTaskHandler.of(initialURN), ScheduledTaskHandler.of(initialURN));
    }

    @Test
    public void of_equalitySameRef() {
        String initialURN = "urn:hzScheduledTaskHandler:127.0.0.1:0 -1 Scheduler Task";
        ScheduledTaskHandler handler = ScheduledTaskHandler.of(initialURN);
        assertEquals(handler, handler);
    }

    @Test
    public void of_equalityDifferentAddress() {
        String urnA = "urn:hzScheduledTaskHandler:127.0.0.1:0 -1 Scheduler Task";
        String urnB = "urn:hzScheduledTaskHandler:127.0.0.1:1 -1 Scheduler Task";
        assertNotEquals(ScheduledTaskHandler.of(urnA), ScheduledTaskHandler.of(urnB));
    }

    @Test
    public void of_equalityDifferentTypes() {
        String urnA = "urn:hzScheduledTaskHandler:127.0.0.1:0 -1 Scheduler Task";
        String urnB = "urn:hzScheduledTaskHandler:- 2 Scheduler Task";
        assertNotEquals(ScheduledTaskHandler.of(urnA), ScheduledTaskHandler.of(urnB));
    }

    @Test
    public void of_equalityDifferentSchedulers() {
        String urnA = "urn:hzScheduledTaskHandler:127.0.0.1:0 -1 Scheduler Task";
        String urnB = "urn:hzScheduledTaskHandler:127.0.0.1:0 -1 Scheduler2 Task";
        assertNotEquals(ScheduledTaskHandler.of(urnA), ScheduledTaskHandler.of(urnB));
    }

    @Test
    public void of_equalityDifferentTasks() {
        String urnA = "urn:hzScheduledTaskHandler:127.0.0.1:0 -1 Scheduler Task";
        String urnB = "urn:hzScheduledTaskHandler:127.0.0.1:0 -1 Scheduler Task2";
        assertNotEquals(ScheduledTaskHandler.of(urnA), ScheduledTaskHandler.of(urnB));
    }

    @Test
    public void of_equalityNull() {
        String urnA = "urn:hzScheduledTaskHandler:127.0.0.1:0 -1 Scheduler Task";
        assertNotNull(ScheduledTaskHandler.of(urnA));
    }

    @Test
    public void of_addressConstructor() throws Exception {
        Address addr = new Address("127.0.0.1", 0);
        ScheduledTaskHandler handler = ScheduledTaskHandlerImpl.of(addr, "Scheduler", "Task");

        String expectedURN = "urn:hzScheduledTaskHandler:127.0.0.1:0 -1 Scheduler Task";

        assertTrue(handler.isAssignedToMember());
        assertEquals(-1, handler.getPartitionId());
        assertEquals(addr, handler.getAddress());
        assertEquals("Scheduler", handler.getSchedulerName());
        assertEquals("Task", handler.getTaskName());
        assertEquals(expectedURN, handler.toUrn());
    }

    @Test
    public void of_partitionConstructor() {
        ScheduledTaskHandler handler = ScheduledTaskHandlerImpl.of(2, "Scheduler1", "Task1");

        String expectedURN = "urn:hzScheduledTaskHandler:- 2 Scheduler1 Task1";

        assertTrue(handler.isAssignedToPartition());
        assertEquals(2, handler.getPartitionId());
        assertNull(handler.getAddress());
        assertEquals("Scheduler1", handler.getSchedulerName());
        assertEquals("Task1", handler.getTaskName());
        assertEquals(expectedURN, handler.toUrn());
    }
}
