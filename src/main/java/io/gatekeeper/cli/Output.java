package io.gatekeeper.cli;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;

public class Output extends PrintStream {

    public Output() {
        super(new FileOutputStream(FileDescriptor.out));
    }
}