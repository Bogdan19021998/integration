package ai.distil.integration.unit;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;

import java.io.IOException;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class AbstractSyncTest {

    protected static final String CUSTOMERS_TABLE_NAME = "distil_customers";

    @BeforeAll
    public void before() throws Exception {
        startInstance();
    }

    @AfterEach
    public void afterEach() {
        clean();
    }

    @AfterAll
    public void after() throws Exception {
        stopInstance();
    }

    protected abstract void startInstance() throws IOException;

    protected abstract void stopInstance();

    protected abstract void clean();


}
