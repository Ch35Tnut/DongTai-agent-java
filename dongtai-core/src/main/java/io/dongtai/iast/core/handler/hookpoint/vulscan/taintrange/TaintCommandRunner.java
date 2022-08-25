package io.dongtai.iast.core.handler.hookpoint.vulscan.taintrange;

import io.dongtai.log.DongTaiLog;

import java.util.*;

public class TaintCommandRunner {
    private String signature;

    private TaintRangesBuilder builder;

    private TaintCommand command;

    private List<RunnerParam> params = new ArrayList<RunnerParam>();

    private int paramsCount = 0;

    static class RunnerParam {
        private int position;
        private boolean isLiteral = false;

        public RunnerParam(String param) {
            if (param.startsWith("P")) {
                this.position = Integer.parseInt(param.substring(1)) - 1;
            } else {
                this.position = Integer.parseInt(param);
                this.isLiteral = true;
            }
        }

        public int getParam(Object[] params) {
            if (this.isLiteral) {
                return this.position;
            }
            if (params == null) {
                return 0;
            }

            return (Integer) params[this.position];
        }
    }

    public static TaintCommandRunner create(String signature, TaintCommand command) {
        return create(signature, command, null);
    }

    public static TaintCommandRunner create(String signature, TaintCommand command, List<String> params) {
        try {
            TaintCommandRunner r = new TaintCommandRunner();
            r.signature = signature;
            r.builder = new TaintRangesBuilder();
            r.command = command;
            if (params != null) {
                r.paramsCount = params.size();
                for (String param : params) {
                    r.params.add(new RunnerParam(param));
                }
            }
            return r;
        } catch (Exception e) {
            return null;
        }
    }

    public TaintRangesBuilder getTaintRangesBuilder() {
        return this.builder;
    }

    public TaintRanges run(Object source, Object target, Object[] params, TaintRanges oldTaintRanges, TaintRanges srcTaintRanges) {
        int p1 = 0;
        int p2 = 0;
        int p3 = 0;
        TaintRanges tr = new TaintRanges();

        try {
            if (this.paramsCount > 0) {
                p1 = this.params.get(0).getParam(params);
            }
            if (this.paramsCount > 1) {
                p2 = this.params.get(1).getParam(params);
            }
            if (this.paramsCount > 2) {
                p3 = this.params.get(2).getParam(params);
            }
        } catch (Exception e) {
            DongTaiLog.error(this.signature + " taint command parameters fetch failed: " + e.getMessage());
            return tr;
        }

        switch (this.command) {
            case KEEP:
                this.builder.keep(tr, target, this.paramsCount, srcTaintRanges);
                break;
            case APPEND:
                this.builder.append(tr, target, oldTaintRanges, source, srcTaintRanges, p1, p2, this.paramsCount);
                break;
            case SUBSET:
                this.builder.subset(tr, oldTaintRanges, source, srcTaintRanges, p1, p2, p3, this.paramsCount);
                break;
            case INSERT:
                this.builder.insert(tr, oldTaintRanges, source, srcTaintRanges, p1, p2, p3, this.paramsCount);
                break;
            default:
                break;
        }

        return tr;
    }

    public static TaintCommandRunner getCommandRunner(String signature) {
        return RUNNER_MAP.get(signature);
    }

    private static final Map<String, TaintCommandRunner> RUNNER_MAP = new HashMap<String, TaintCommandRunner>() {{
        // KEEP String
        String METHOD = "java.lang.String.<init>(java.lang.String)";
        put(METHOD, create(METHOD, TaintCommand.KEEP));
        METHOD = "java.lang.String.<init>(java.lang.StringBuilder)";
        put(METHOD, create(METHOD, TaintCommand.KEEP));
        METHOD = "java.lang.String.<init>(java.lang.StringBuffer)";
        put(METHOD, create(METHOD, TaintCommand.KEEP));
        METHOD = "java.lang.String.<init>(byte[],int,int)";
        put(METHOD, create(METHOD, TaintCommand.KEEP));
        METHOD = "java.lang.String.<init>(byte[],int,int,int)";
        put(METHOD, create(METHOD, TaintCommand.KEEP));
        METHOD = "java.lang.String.<init>(byte[],int,int,java.lang.String)";
        put(METHOD, create(METHOD, TaintCommand.KEEP));
        METHOD = "java.lang.String.<init>(char[])";
        put(METHOD, create(METHOD, TaintCommand.KEEP));
        METHOD = "java.lang.String.<init>(byte[],java.nio.charset.Charset)";    // Java-11
        put(METHOD, create(METHOD, TaintCommand.KEEP));
        METHOD = "java.lang.String.<init>(byte[],byte)";     // Java-17
        put(METHOD, create(METHOD, TaintCommand.KEEP));
        METHOD = "java.lang.String.toLowerCase(java.util.Locale)";
        put(METHOD, create(METHOD, TaintCommand.KEEP));
        METHOD = "java.lang.String.toUpperCase(java.util.Locale)";
        put(METHOD, create(METHOD, TaintCommand.KEEP));
        METHOD = "java.lang.String.getBytes()";
        put(METHOD, create(METHOD, TaintCommand.KEEP));
        METHOD = "java.lang.String.getBytes(java.lang.String)";
        put(METHOD, create(METHOD, TaintCommand.KEEP));
        METHOD = "java.lang.String.getBytes(java.nio.charset.Charset)";
        put(METHOD, create(METHOD, TaintCommand.KEEP));
        METHOD = "java.lang.String.toCharArray()";
        put(METHOD, create(METHOD, TaintCommand.KEEP));

        // KEEP StringBuilder
        METHOD = "java.lang.StringBuilder.toString()";
        put(METHOD, create(METHOD, TaintCommand.KEEP));
        METHOD = "java.lang.StringBuilder.<init>(java.lang.String)";
        put(METHOD, create(METHOD, TaintCommand.KEEP));
        METHOD = "java.lang.StringBuilder.<init>(java.lang.CharSequence)";
        put(METHOD, create(METHOD, TaintCommand.KEEP));

        // KEEP StringBuffer
        METHOD = "java.lang.StringBuffer.toString()";
        put(METHOD, create(METHOD, TaintCommand.KEEP));
        METHOD = "java.lang.StringBuffer.<init>(java.lang.String)";
        put(METHOD, create(METHOD, TaintCommand.KEEP));
        METHOD = "java.lang.StringBuffer.<init>(java.lang.CharSequence)";
        put(METHOD, create(METHOD, TaintCommand.KEEP));

        // KEEP ByteArrayOutputStream
        METHOD = "java.io.ByteArrayOutputStream.toByteArray()";
        put(METHOD, create(METHOD, TaintCommand.KEEP));
        METHOD = "java.io.ByteArrayOutputStream.toString()";
        put(METHOD, create(METHOD, TaintCommand.KEEP));
        METHOD = "java.io.ByteArrayOutputStream.toString(java.lang.String)";
        put(METHOD, create(METHOD, TaintCommand.KEEP));
        METHOD = "java.io.ByteArrayOutputStream.toString(int)";
        put(METHOD, create(METHOD, TaintCommand.KEEP));
        METHOD = "java.io.ByteArrayOutputStream.toString(java.nio.charset.Charset)";
        put(METHOD, create(METHOD, TaintCommand.KEEP));

        // KEEP StringConcatHelper
        METHOD = "java.lang.StringConcatHelper.newString(byte[],int,byte)";   // Java 9-11
        put(METHOD, create(METHOD, TaintCommand.KEEP));
        METHOD = "java.lang.StringConcatHelper.newString(byte[],long)";   // Java 12+, up to 14
        put(METHOD, create(METHOD, TaintCommand.KEEP));

        // KEEP StringWriter
        METHOD = "java.io.StringWriter.toString()";
        put(METHOD, create(METHOD, TaintCommand.KEEP));

        // APPEND String
        METHOD = "java.lang.String.<init>(char[],int,int)";
        put(METHOD, create(METHOD, TaintCommand.APPEND, Arrays.asList("P2", "P3", "0")));
        METHOD = "java.lang.String.<init>(char[],int,int,boolean)";    // in IBM JDK8 split()
        put(METHOD, create(METHOD, TaintCommand.APPEND, Arrays.asList("P2", "P3", "0")));

        // APPEND StringLatin1/StringUTF16
        METHOD = "java.lang.StringLatin1.newString(byte[],int,int)";    // Java-11
        put(METHOD, create(METHOD, TaintCommand.APPEND, Arrays.asList("P2", "P3", "0")));
        METHOD = "java.lang.StringUTF16.newString(byte[],int,int)";     // Java-11
        put(METHOD, create(METHOD, TaintCommand.APPEND, Arrays.asList("P2", "P3", "0")));

        // APPEND StringBuilder
        METHOD = "java.lang.StringBuilder.append(java.lang.String)";
        put(METHOD, create(METHOD, TaintCommand.APPEND));
        METHOD = "java.lang.StringBuilder.append(java.lang.StringBuffer)";
        put(METHOD, create(METHOD, TaintCommand.APPEND));
        METHOD = "java.lang.StringBuilder.append(java.lang.CharSequence)";
        put(METHOD, create(METHOD, TaintCommand.APPEND));
        METHOD = "java.lang.StringBuilder.append(java.lang.CharSequence,int,int)";
        put(METHOD, create(METHOD, TaintCommand.APPEND, Arrays.asList("P2", "P3")));
        METHOD = "java.lang.StringBuilder.append(char[],int,int)";
        put(METHOD, create(METHOD, TaintCommand.APPEND, Arrays.asList("P2", "P3", "0")));

        // APPEND AbstractStringBuilder
        METHOD = "java.lang.AbstractStringBuilder.append(java.lang.String)";
        put(METHOD, create(METHOD, TaintCommand.APPEND));

        // APPEND StringBuffer
        METHOD = "java.lang.StringBuffer.append(java.lang.String)";
        put(METHOD, create(METHOD, TaintCommand.APPEND));
        METHOD = "java.lang.StringBuffer.append(java.lang.StringBuffer)";
        put(METHOD, create(METHOD, TaintCommand.APPEND));
        METHOD = "java.lang.StringBuffer.append(char[])";
        put(METHOD, create(METHOD, TaintCommand.APPEND));
        METHOD = "java.lang.StringBuffer.append(java.lang.CharSequence)";
        put(METHOD, create(METHOD, TaintCommand.APPEND));
        METHOD = "java.lang.StringBuffer.append(java.lang.CharSequence,int,int)";
        put(METHOD, create(METHOD, TaintCommand.APPEND, Arrays.asList("P2", "P3")));
        METHOD = "java.lang.StringBuffer.append(char[],int,int)";
        put(METHOD, create(METHOD, TaintCommand.APPEND, Arrays.asList("P2", "P3", "0")));

        // APPEND ByteArrayOutputStream
        METHOD = "java.io.ByteArrayOutputStream.toString(java.nio.charset.Charset)";
        put(METHOD, create(METHOD, TaintCommand.APPEND, Arrays.asList("P2", "P3")));

        // APPEND StringWriter
        METHOD = "java.io.StringWriter.write(char[],int,int)";
        put(METHOD, create(METHOD, TaintCommand.APPEND, Arrays.asList("P2", "P3")));
        METHOD = "java.io.StringWriter.write(java.lang.String)";
        put(METHOD, create(METHOD, TaintCommand.APPEND));
        METHOD = "java.io.StringWriter.write(java.lang.String,int,int)";
        put(METHOD, create(METHOD, TaintCommand.APPEND, Arrays.asList("P2", "P3")));

        // APPEND apache ByteArrayOutputStream
        METHOD = "org.apache.commons.io.output.ByteArrayOutputStream.write(byte[],int,int)";
        put(METHOD, create(METHOD, TaintCommand.APPEND, Arrays.asList("P2", "P3")));

        // SUBSET String
        METHOD = "java.lang.String.substring(int)";
        put(METHOD, create(METHOD, TaintCommand.SUBSET, Collections.singletonList("P1")));
        METHOD = "java.lang.String.substring(int,int)";
        put(METHOD, create(METHOD, TaintCommand.SUBSET, Arrays.asList("P1", "P2")));
        METHOD = "java.lang.String.getBytes(int,int,byte[],int)";
        put(METHOD, create(METHOD, TaintCommand.SUBSET, Arrays.asList("P1", "P2", "P4")));
        METHOD = "java.lang.String.getChars(int,int,char[],int)";
        put(METHOD, create(METHOD, TaintCommand.SUBSET, Arrays.asList("P1", "P2", "P4")));
        METHOD = "java.lang.String.<init>(byte[],int,int,java.nio.charset.Charset)";
        put(METHOD, create(METHOD, TaintCommand.SUBSET, Arrays.asList("P2", "P3")));

        // SUBSET StringLatin1/StringUTF16 LinesSpliterator
        METHOD = "java.lang.StringLatin1$LinesSpliterator.<init>(byte[],int,int)";      // Java-11
        put(METHOD, create(METHOD, TaintCommand.SUBSET, Arrays.asList("P2", "P3")));
        METHOD = "java.lang.StringUTF16$LinesSpliterator.<init>(byte[],int,int)";      // Java-11
        put(METHOD, create(METHOD, TaintCommand.SUBSET, Arrays.asList("P2", "P3")));

        // SUBSET StringBuilder
        METHOD = "java.lang.StringBuilder.substring(int)";
        put(METHOD, create(METHOD, TaintCommand.SUBSET, Collections.singletonList("P1")));
        METHOD = "java.lang.StringBuilder.substring(int,int)";
        put(METHOD, create(METHOD, TaintCommand.SUBSET, Arrays.asList("P1", "P2")));
        METHOD = "java.lang.StringBuilder.setLength(int)";
        put(METHOD, create(METHOD, TaintCommand.SUBSET, Arrays.asList("0", "P1")));
        METHOD = "java.lang.StringBuilder.getChars(int,int,char[],int)";
        put(METHOD, create(METHOD, TaintCommand.SUBSET, Arrays.asList("P1", "P2", "P4")));

        // SUBSET AbstractStringBuilder
        METHOD = "java.lang.AbstractStringBuilder.substring(int)";
        put(METHOD, create(METHOD, TaintCommand.SUBSET, Collections.singletonList("P1")));
        METHOD = "java.lang.AbstractStringBuilder.substring(int,int)";
        put(METHOD, create(METHOD, TaintCommand.SUBSET, Arrays.asList("P1", "P2")));
        METHOD = "java.lang.AbstractStringBuilder.setLength(int)";
        put(METHOD, create(METHOD, TaintCommand.SUBSET, Arrays.asList("0", "P1")));
        METHOD = "java.lang.AbstractStringBuilder.getChars(int,int,char[],int)";
        put(METHOD, create(METHOD, TaintCommand.SUBSET, Arrays.asList("P1", "P2", "P4")));

        // SUBSET StringBuffer
        METHOD = "java.lang.StringBuffer.substring(int)";
        put(METHOD, create(METHOD, TaintCommand.SUBSET, Collections.singletonList("P1")));
        METHOD = "java.lang.StringBuffer.substring(int,int)";
        put(METHOD, create(METHOD, TaintCommand.SUBSET, Arrays.asList("P1", "P2")));
        METHOD = "java.lang.StringBuffer.setLength(int)";
        put(METHOD, create(METHOD, TaintCommand.SUBSET, Arrays.asList("0", "P1")));
        METHOD = "java.lang.StringBuffer.getChars(int,int,char[],int)";
        put(METHOD, create(METHOD, TaintCommand.SUBSET, Arrays.asList("P1", "P2", "P4")));

        // SUBSET ByteBuffer
        METHOD = "java.nio.ByteBuffer.wrap(byte[],int,int)";
        put(METHOD, create(METHOD, TaintCommand.SUBSET, Arrays.asList("P2", "P3")));

        // SUBSET Arrays
        METHOD = "java.util.Arrays.copyOf(byte[],int)";
        put(METHOD, create(METHOD, TaintCommand.SUBSET, Arrays.asList("0", "P2")));
        METHOD = "java.util.Arrays.copyOfRange(byte[],int,int)";
        put(METHOD, create(METHOD, TaintCommand.SUBSET, Arrays.asList("P2", "P3")));
        METHOD = "java.util.Arrays.copyOf(char[],int)";
        put(METHOD, create(METHOD, TaintCommand.SUBSET, Arrays.asList("0", "P2")));
        METHOD = "java.util.Arrays.copyOfRange(char[],int,int)";
        put(METHOD, create(METHOD, TaintCommand.SUBSET, Arrays.asList("P2", "P3")));

        // INSERT CharArrayReader/PipedReader/PipedInputStream
        METHOD = "java.io.CharArrayReader.<init>(char[],int,int)";
        put(METHOD, create(METHOD, TaintCommand.INSERT, Arrays.asList("0", "P2", "P3")));
        METHOD = "java.io.CharArrayReader.read(char[],int,int)";
        put(METHOD, create(METHOD, TaintCommand.INSERT, Arrays.asList("0", "P2", "P3")));
        METHOD = "java.io.PipedReader.read(char[],int,int)";
        put(METHOD, create(METHOD, TaintCommand.INSERT, Arrays.asList("0", "P2", "P3")));
        METHOD = "java.io.PipedInputStream.read(byte[],int,int)";
        put(METHOD, create(METHOD, TaintCommand.INSERT, Arrays.asList("0", "P2", "P3")));

        // INSERT StringBuilder
        METHOD = "java.lang.StringBuilder.insert(int,java.lang.String)";
        put(METHOD, create(METHOD, TaintCommand.INSERT, Collections.singletonList("P1")));
        METHOD = "java.lang.StringBuilder.insert(int,char[])";
        put(METHOD, create(METHOD, TaintCommand.INSERT, Collections.singletonList("P1")));
        METHOD = "java.lang.StringBuilder.insert(int,char)";
        put(METHOD, create(METHOD, TaintCommand.INSERT, Collections.singletonList("P1")));
        METHOD = "java.lang.StringBuilder.insert(int,java.lang.CharSequence)";
        put(METHOD, create(METHOD, TaintCommand.INSERT, Collections.singletonList("P1")));
        METHOD = "java.lang.StringBuilder.insert(int,java.lang.CharSequence,int,int)";
        put(METHOD, create(METHOD, TaintCommand.INSERT, Arrays.asList("P1", "P3", "P4")));
        METHOD = "java.lang.StringBuilder.insert(int,char[],int,int)";
        put(METHOD, create(METHOD, TaintCommand.INSERT, Arrays.asList("P1", "P3", "P4")));

        // INSERT StringBuffer
        METHOD = "java.lang.StringBuffer.insert(int,java.lang.String)";
        put(METHOD, create(METHOD, TaintCommand.INSERT, Collections.singletonList("P1")));
        METHOD = "java.lang.StringBuffer.insert(int,char[])";
        put(METHOD, create(METHOD, TaintCommand.INSERT, Collections.singletonList("P1")));
        METHOD = "java.lang.StringBuffer.insert(int,char)";
        put(METHOD, create(METHOD, TaintCommand.INSERT, Collections.singletonList("P1")));
        METHOD = "java.lang.StringBuffer.insert(int,java.lang.CharSequence)";
        put(METHOD, create(METHOD, TaintCommand.INSERT, Collections.singletonList("P1")));
        METHOD = "java.lang.StringBuffer.insert(int,java.lang.CharSequence,int,int)";
        put(METHOD, create(METHOD, TaintCommand.INSERT, Arrays.asList("P1", "P3", "P4")));
        METHOD = "java.lang.StringBuffer.insert(int,char[],int,int)";
        put(METHOD, create(METHOD, TaintCommand.INSERT, Arrays.asList("P1", "P3", "P4")));
    }};
}
