package com.robin.io;

import java.io.InputStream;

public class Resources {

    // Depending on the path of configuration file, the configuration file
    // is loaded into a byte stream and stored in memory.
    public static InputStream getSourceAsStream(String path) {
        InputStream resourceAsStream = Resources.class.getClassLoader().getResourceAsStream(path);
        return resourceAsStream;
    }
}
