/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shenyu.register.instance.zookeeper;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.listen.Listenable;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.shenyu.common.config.ShenyuConfig;
import org.apache.shenyu.register.common.dto.InstanceRegisterDTO;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

public class ZookeeperInstanceRegisterRepositoryTest {

    @Test
    public void testZookeeperInstanceRegisterRepository() {
        final Listenable listenable = mock(Listenable.class);
        try (MockedConstruction<ZookeeperClient> construction = mockConstruction(ZookeeperClient.class, (mock, context) -> {
            final CuratorFramework curatorFramework = mock(CuratorFramework.class);
            when(mock.getClient()).thenReturn(curatorFramework);
            when(curatorFramework.getConnectionStateListenable()).thenReturn(listenable);
        })) {
            final ZookeeperInstanceRegisterRepository repository = new ZookeeperInstanceRegisterRepository();
            ShenyuConfig.InstanceConfig config = new ShenyuConfig.InstanceConfig();
            repository.init(config);
            final Properties configProps = config.getProps();
            configProps.setProperty("digest", "digest");
            List<ConnectionStateListener> connectionStateListeners = new ArrayList<>();
            doAnswer(invocationOnMock -> {
                connectionStateListeners.add(invocationOnMock.getArgument(0));
                return null;
            }).when(listenable).addListener(any());
            repository.init(config);
            repository.persistInstance(mock(InstanceRegisterDTO.class));
            connectionStateListeners.forEach(connectionStateListener -> {
                connectionStateListener.stateChanged(null, ConnectionState.RECONNECTED);
            });
            repository.close();
        }
    }
}
