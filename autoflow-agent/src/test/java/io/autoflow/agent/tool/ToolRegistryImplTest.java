package io.autoflow.agent.tool;

import io.autoflow.spi.Service;
import io.autoflow.spi.Services;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ToolRegistryImplTest {

    @Mock
    private Service mockService;

    @InjectMocks
    private ToolRegistryImpl toolRegistry;

    private MockedStatic<Services> mockedServices;

    @BeforeEach
    void setUp() {
        mockedServices = mockStatic(Services.class);
    }

    @AfterEach
    void tearDown() {
        mockedServices.close();
    }

    @Test
    void getNodeId_returnsCorrectNodeId_forKnownTool() {
        String toolName = "testTool";
        String nodeId = "node-123";

        when(mockService.getName()).thenReturn(toolName);
        when(mockService.getId()).thenReturn(nodeId);
        mockedServices.when(Services::getServiceList).thenReturn(List.of(mockService));

        String result = toolRegistry.getNodeId(toolName);

        assertEquals(nodeId, result);
    }

    @Test
    void getNodeId_returnsNull_forUnknownTool() {
        mockedServices.when(Services::getServiceList).thenReturn(Collections.emptyList());

        String result = toolRegistry.getNodeId("unknownTool");

        assertNull(result);
    }

    @Test
    void register_addsNewToolMapping() {
        String toolName = "newTool";
        String nodeId = "newNode-456";

        toolRegistry.register(toolName, nodeId);

        assertEquals(nodeId, toolRegistry.getNodeId(toolName));
    }
}
