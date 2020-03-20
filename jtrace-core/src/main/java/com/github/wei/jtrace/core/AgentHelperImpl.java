package com.github.wei.jtrace.core;

import com.github.wei.jtrace.api.IAgentHelper;
import com.github.wei.jtrace.api.beans.Bean;
import com.github.wei.jtrace.core.util.AgentHelperUtil;

import java.io.File;

@Bean(type = IAgentHelper.class)
public class AgentHelperImpl implements IAgentHelper {
    @Override
    public File getAgentDirectory() {
        return AgentHelperUtil.getAgentDirectory();
    }
}
