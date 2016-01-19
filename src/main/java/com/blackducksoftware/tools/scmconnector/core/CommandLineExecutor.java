/*******************************************************************************
 * Copyright (C) 2015 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License version 2 only
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License version 2
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *******************************************************************************/
package com.blackducksoftware.tools.scmconnector.core;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.exec.environment.EnvironmentUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.blackducksoftware.tools.scmconnector.utils.cli.ExecLogHangler;

public class CommandLineExecutor implements ICommandLineExecutor {

    @Override
    public int executeCommand(Logger log, CommandLine command, File targetDir,
            String promptResponse) throws Exception {
        DefaultExecutor executor = new DefaultExecutor();
        executor.setExitValue(0);

        PumpStreamHandler psh = new PumpStreamHandler(new ExecLogHangler(log,
                Level.INFO));
        executor.setStreamHandler(psh);

        providePromptResponse(psh, promptResponse);

        executor.setWorkingDirectory(targetDir);

        // This log msg would reveal password
        // log.info("Command: " + command.toString() + " executed in: "
        // + targetDir.toString());

        int exitStatus = 1;
        try {
            exitStatus = executor.execute(command,
                    EnvironmentUtils.getProcEnvironment());
        } catch (Exception e) {
            log.error("Failure executing Command: " + e.getMessage());
        }
        return exitStatus;
    }

    private void providePromptResponse(PumpStreamHandler psh,
            String promptResponse) throws IOException {
        if (promptResponse == null) {
            return;
        }

        OutputStream out = new ByteArrayOutputStream();
        psh.setProcessInputStream(out);
        String responseWithEOL = promptResponse + "\n";
        out.write(responseWithEOL.getBytes());
    }

    @Override
    public int executeCommand(Logger log, CommandLine command, File targetDir)
            throws Exception {
        return executeCommand(log, command, targetDir, null);
    }

    @Override
    public CommandResults executeCommandForOutput(Logger log,
            CommandLine command, File targetDir) throws Exception {
        DefaultExecutor executor = new DefaultExecutor();
        executor.setExitValue(0);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PumpStreamHandler psh = new PumpStreamHandler(outputStream);
        executor.setStreamHandler(psh);

        int exitStatus = 1;
        executor.setWorkingDirectory(targetDir);
        exitStatus = executor.execute(command);
        String outputString = outputStream.toString();

        // This log msg would reveal password
        // log.info("Command: " + command.toString() + " executed in: "
        // + targetDir.toString() + "; output: " + outputString);

        return new CommandResults(exitStatus, outputString);
    }

}
