package com.blackducksoftware.tools.scmconnector.core;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.exec.CommandLine;
import org.apache.log4j.Logger;

public class MockCommandLineExecutor implements ICommandLineExecutor {
    Logger lastLog;

    List<CommandLine> commands = new ArrayList<>();
    File lastTargetDir;

    @Override
    public int executeCommand(Logger log, CommandLine command, File targetDir,
	    String promptResponse) throws Exception {
	lastLog = log;
	commands.add(command);
	lastTargetDir = targetDir;

	System.out.println("Mock Executing: " + command + " in "
		+ targetDir.getAbsolutePath());
	return 0;
    }

    public String getLastCommandString() {
	if (commands.size() == 0) {
	    return null;
	}
	return commands.get(commands.size() - 1).toString();
    }

    public int size() {
	return commands.size();
    }

    public String getCommandString(int i) {
	return commands.get(i).toString();
    }

    public String getLastTargetDirPath() {
	return lastTargetDir.getAbsolutePath();
    }

    @Override
    public String toString() {
	if (commands.size() == 0) {
	    return "<not yet used>";
	} else {
	    StringBuilder sb = new StringBuilder(getCommandString(0));
	    for (int i = 1; i < size(); i++) {
		sb.append('\n');
		sb.append(getCommandString(i));
	    }
	    return sb.toString();
	}
    }

    @Override
    public int executeCommand(Logger log, CommandLine command, File targetDir)
	    throws Exception {
	return executeCommand(log, command, targetDir, null);
    }

    @Override
    public CommandResults executeCommandForOutput(Logger log,
	    CommandLine command, File targetDir) throws Exception {
	int status = executeCommand(log, command, targetDir, null);
	CommandResults results = new CommandResults(status,
		"<the command execution was mocked; there is no output>");
	return results;
    }

    protected Logger getLastLog() {
	return lastLog;
    }

    protected void setLastLog(Logger lastLog) {
	this.lastLog = lastLog;
    }

    protected File getLastTargetDir() {
	return lastTargetDir;
    }

    protected void setLastTargetDir(File lastTargetDir) {
	this.lastTargetDir = lastTargetDir;
    }

    protected void addCommand(CommandLine commandLine) {
	commands.add(commandLine);
    }
}
