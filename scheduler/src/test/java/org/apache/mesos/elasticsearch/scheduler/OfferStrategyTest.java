package org.apache.mesos.elasticsearch.scheduler;

import com.google.protobuf.InvalidProtocolBufferException;
import org.apache.mesos.Protos;
import org.apache.mesos.elasticsearch.scheduler.state.ClusterState;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.apache.mesos.elasticsearch.scheduler.Resources.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class OfferStrategyTest {

    @Mock
    Configuration configuration;

    @Mock
    ClusterState clusterState;

    @InjectMocks
    OfferStrategy offerStrategy;

    @Before
    public void setUp() throws Exception {
        when(configuration.getFrameworkRole()).thenReturn("testRole");
    }

    @Test
    public void willDeclineIfHostIsAlreadyRunningTask() throws Exception {
        when(clusterState.getTaskList()).thenReturn(singletonList(createTask("host1")));

        final OfferStrategy.OfferResult result = offerStrategy.evaluate(validOffer("host1"));
        assertFalse(result.accepted);
        assertEquals("Host already running task", result.reason.get());
    }

    @Test
    public void willDeclineIfClusterSizeIsFulfilled() throws Exception {
        when(clusterState.getTaskList()).thenReturn(asList(createTask("host1"), createTask("host2"), createTask("host3")));
        when(configuration.getElasticsearchNodes()).thenReturn(3);

        final OfferStrategy.OfferResult result = offerStrategy.evaluate(validOffer("host4"));
        assertFalse(result.accepted);
        assertEquals("Cluster size already fulfilled", result.reason.get());
    }

    @Test
    public void willDeclineIfOfferDoesNotHaveTwoPorts() throws Exception {
        when(clusterState.getTaskList()).thenReturn(asList(createTask("host1"), createTask("host2")));
        when(configuration.getElasticsearchNodes()).thenReturn(3);

        final OfferStrategy.OfferResult offerResult = offerStrategy.evaluate(baseOfferBuilder("host3")
                .addResources(portRange(9200, 9200, configuration.getFrameworkRole()))
                .build());
        assertFalse(offerResult.accepted);
        assertEquals("Offer did not have 2 ports", offerResult.reason.get());
    }

    @Test
    public void willAcceptValidOffer() throws Exception {
        when(clusterState.getTaskList()).thenReturn(asList(createTask("host1"), createTask("host2")));
        when(configuration.getElasticsearchNodes()).thenReturn(3);

        final OfferStrategy.OfferResult offerResult = offerStrategy.evaluate(baseOfferBuilder("host3")
                .addResources(portRange(9200, 9200, configuration.getFrameworkRole()))
                .addResources(portRange(9300, 9300, configuration.getFrameworkRole()))
                .addResources(cpus(configuration.getCpus(), configuration.getFrameworkRole()))
                .addResources(mem(configuration.getMem(), configuration.getFrameworkRole()))
                .addResources(disk(configuration.getDisk(), configuration.getFrameworkRole()))
                .build());
        assertTrue(offerResult.accepted);
        assertFalse(offerResult.reason.isPresent());
    }

    private Protos.TaskInfo createTask(String hostname) throws InvalidProtocolBufferException {
        return Protos.TaskInfo.newBuilder()
                .setName("Test")
                .setTaskId(Protos.TaskID.newBuilder().setValue("TestId").build())
                .setSlaveId(Protos.SlaveID.newBuilder().setValue(hostname).build())
                .build();
    }

    private Protos.Offer validOffer(String slaveId) {
        return baseOfferBuilder(slaveId)
                .build();
    }

    private Protos.Offer.Builder baseOfferBuilder(String slaveId) {
        return Protos.Offer.newBuilder()
                .setId(Protos.OfferID.newBuilder().setValue("offerId").build())
                .setFrameworkId(Protos.FrameworkID.newBuilder().setValue("testframework").build())
                .setHostname("hostname")
                .setSlaveId(Protos.SlaveID.newBuilder().setValue(slaveId).build());
    }
}