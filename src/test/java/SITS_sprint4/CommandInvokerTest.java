package SITS_sprint4;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class CommandInvokerTest
{
    private static class TestCommand implements Command
    {
        private boolean executed = false;

        @Override
        public void execute()
        {
            executed = true;
        }

        public boolean wasExecuted()
        {
            return executed;
        }
    }

    @Test
    void testSetCommandStoresCommand()
    {
        CommandInvoker invoker = new CommandInvoker();
        Command command = new TestCommand();

        invoker.setCommand(command);

        assertSame(command, invoker.getCommand());
    }

    @Test
    void testExecuteCommandRunsCommand()
    {
        CommandInvoker invoker = new CommandInvoker();
        TestCommand command = new TestCommand();

        invoker.setCommand(command);
        invoker.executeCommand();

        assertTrue(command.wasExecuted());
    }

    @Test
    void testExecuteCommandWithNoCommandDoesNothing()
    {
        CommandInvoker invoker = new CommandInvoker();

        assertDoesNotThrow(invoker::executeCommand);
        assertNull(invoker.getCommand());
    }
}