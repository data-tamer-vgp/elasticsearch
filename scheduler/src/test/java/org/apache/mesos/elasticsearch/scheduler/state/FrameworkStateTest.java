package org.apache.mesos.elasticsearch.scheduler.state;

import org.apache.mesos.Protos;
import org.junit.Test;

import java.io.NotSerializableException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * Tests
 */
public class FrameworkStateTest {

    public static final Protos.FrameworkID FRAMEWORK_ID = Protos.FrameworkID.newBuilder().setValue("FrameworkID").build();
    public final State state = mock(State.class);
    private final FrameworkState frameworkState = new FrameworkState(state);

    @Test
    public void testSetFrameworkID() throws NotSerializableException {
        frameworkState.setFrameworkId(FRAMEWORK_ID);
        verify(state, times(1)).setAndCreateParents(anyString(), eq(FRAMEWORK_ID));
    }

    @Test
    public void testGetFrameworkID() {
        when(state.get(anyString())).thenReturn(FRAMEWORK_ID);
        Protos.FrameworkID frameworkID = frameworkState.getFrameworkID();
        verify(state, times(1)).get(anyString());
        assertEquals(FRAMEWORK_ID, frameworkID);
    }

    @Test
    public void testGetEmptyWhenNoFrameworkID() {
        Protos.FrameworkID frameworkID = frameworkState.getFrameworkID();
        verify(state, times(1)).get(anyString());
        assertEquals("", frameworkID.getValue());
    }

    @Test
    public void testHandleException() throws NotSerializableException {
        doThrow(NotSerializableException.class).when(state).setAndCreateParents(anyString(), any());
        frameworkState.setFrameworkId(FRAMEWORK_ID);
    }
}