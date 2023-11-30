package com.automationanywhere.botcommand.LoggerV2.commands;

import com.automationanywhere.toolchain.runtime.session.CloseableSessionObject;

import java.io.IOException;

public class ManageSession implements CloseableSessionObject
{

    boolean close=false;
    public String getDemo() {
        return demo;
    }

    public void setDemo(String demo) {
        this.demo = demo;
    }
    public ManageSession(String session1){
        this.demo=demo;
    }
    String demo;

    @Override
    public boolean isClosed() {
        return close;
    }

    @Override
    public void close() throws IOException {

    }
}

