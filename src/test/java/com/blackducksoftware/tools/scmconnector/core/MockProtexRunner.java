package com.blackducksoftware.tools.scmconnector.core;


public class MockProtexRunner implements ProtexRunner {
    private ConnectorConfig config;
    private int runCount = 0;
    private int scanCount = 0;
    private String lastProjectName;
    private String lastRepositoryPath;

    public MockProtexRunner(ConnectorConfig config) {
	this.config = config;
    }

    @Override
    public void run(Connector connector, int i, String projectName,
	    String aggregatedRepositoryPath) throws Exception {
	runCount++;
	lastProjectName = projectName;
	lastRepositoryPath = aggregatedRepositoryPath;

	if (config.isDoProtexScan()) {
	    scanCount++;
	}
    }

    /**
     * Use this to find out if the run() method was called, indicating that the
     * SCM Connector ran protex (well, tried to).
     *
     * @return
     */
    public int getRunCount() {
	return runCount;
    }

    public int getScanCount() {
	return scanCount;
    }

    public void resetRunCount() {
	runCount = 0;
    }

    public void resetScanCount() {
	scanCount = 0;
    }

    public String getLastProjectName() {
	return lastProjectName;
    }

    public String getLastRepositoryPath() {
	return lastRepositoryPath;
    }

    public void resetLastRepositoryPath() {
	lastRepositoryPath = null;
    }

    public void resetLastProjectName() {
	lastProjectName = null;
    }

}
