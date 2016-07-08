/*
 * Copyright 2016 Innovative Mobile Solutions Limited and contributors
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

package com.mobilecashout.osprey.command;

import com.mobilecashout.osprey.Osprey;
import com.mobilecashout.osprey.exception.BaseException;
import com.rfksystems.commander.Command;
import com.rfksystems.commander.Input;
import com.rfksystems.commander.InputArgument;
import com.rfksystems.commander.exception.InputParseException;
import com.rfksystems.commander.exception.RuntimeArgumentException;
import org.apache.logging.log4j.Logger;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DeployCommandTest {
    @Rule
    public final ExpectedException exception = ExpectedException.none();
    @Mock
    private Osprey osprey;
    @Mock
    private Logger logger;
    @Mock
    private PrintStream printStream;
    @Mock
    private HashMap<String, InputArgument> arguments;
    @Mock
    private ArrayList<String> positional;
    @InjectMocks
    private DeployCommand command;

    @Test
    public void it_is_command() {
        assertTrue(Command.class.isAssignableFrom(DeployCommand.class));
    }

    @Test
    public void it_throws_if_no_positional_arguments() throws RuntimeArgumentException, InputParseException {
        Input input = new Input(new String[0]);
        exception.expect(RuntimeArgumentException.class);
        command.execute(input, printStream);
    }

    @Test
    public void it_throws_if_only_one_positional_argument() throws RuntimeArgumentException, InputParseException {
        Input input = new Input(new String[]{"project"});
        exception.expect(RuntimeArgumentException.class);
        command.execute(input, printStream);
    }

    @Test
    public void it_assumes_defaults_and_passes_arguments_to_master() throws RuntimeArgumentException, InputParseException, BaseException {
        Input input = new Input(new String[]{"project", "env"});
        int status = command.execute(input, printStream);
        assertEquals(0, status);

        verify(osprey, times(1)).deploy("project", "env", "osprey.json", false, false, false);
    }

    @Test
    public void it_enables_verbose() throws RuntimeArgumentException, InputParseException, BaseException {
        Input input = new Input(new String[]{"project", "env", "--verbose"});
        int status = command.execute(input, printStream);
        assertEquals(0, status);

        verify(osprey, times(1)).deploy("project", "env", "osprey.json", true, false, false);
    }

    @Test
    public void it_enables_non_interactive() throws RuntimeArgumentException, InputParseException, BaseException {
        Input input = new Input(new String[]{"project", "env", "--assume-yes"});
        int status = command.execute(input, printStream);
        assertEquals(0, status);

        verify(osprey, times(1)).deploy("project", "env", "osprey.json", false, true, false);
    }

    @Test
    public void it_enables_debug() throws RuntimeArgumentException, InputParseException, BaseException {
        Input input = new Input(new String[]{"project", "env", "--debug"});
        int status = command.execute(input, printStream);
        assertEquals(0, status);

        verify(osprey, times(1)).deploy("project", "env", "osprey.json", false, false, true);
    }

    @Test
    public void it_can_indicate_config() throws RuntimeArgumentException, InputParseException, BaseException {
        Input input = new Input(new String[]{"project", "env", "--config=custom.json"});
        int status = command.execute(input, printStream);
        assertEquals(0, status);

        verify(osprey, times(1)).deploy("project", "env", "custom.json", false, false, false);
    }

    @Test
    public void it_logs_fatal_errors_from_inherited_from_base() throws InputParseException, RuntimeArgumentException, BaseException {
        BaseException testException = mock(BaseException.class);
        when(testException.getMessage()).thenReturn("message");

        doThrow(testException)
                .when(osprey)
                .deploy(
                        "project",
                        "env",
                        "osprey.json",
                        false,
                        false,
                        false
                );

        Input input = new Input(new String[]{"project", "env"});
        int status = command.execute(input, printStream);

        assertEquals(-1, status);

        verify(logger, times(1)).fatal("message");
    }

    @Test
    public void it_logs_fatal_errors_from_inherited_from_base_in_verbose_mode() throws InputParseException, RuntimeArgumentException, BaseException {
        BaseException testException = mock(BaseException.class);
        when(testException.getMessage()).thenReturn("message");

        doThrow(testException)
                .when(osprey)
                .deploy(
                        "project",
                        "env",
                        "osprey.json",
                        true,
                        false,
                        false
                );

        Input input = new Input(new String[]{"project", "env", "--verbose"});
        int status = command.execute(input, printStream);

        assertEquals(-1, status);

        verify(logger, times(1)).fatal("message", testException);
    }
}