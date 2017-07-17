/*
 * Copyright 2008-2016 by Emeric Vernat
 *
 *     This file is part of Java Melody.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.howardliu.monitor.cynomys.agent.util;

import java.awt.datatransfer.DataFlavor;
import java.io.*;
import java.util.Locale;

/**
 * Liste des formats de transport entre un serveur de collecte et une
 * application monitorée (hors protocole à priori http).
 *
 * @author Emeric Vernat
 */
public enum TransportFormat {
    /**
     * Sérialisation java.
     */
    SERIALIZED(DataFlavor.javaSerializedObjectMimeType),

    /**
     * XML (avec XStream/XPP). <br/>
     * Selon
     * http://code.google.com/p/thrift-protobuf-compare/wiki/Benchmarking?ts
     * =1237772203&updated=Benchmarking, la sérialisation java est 75% plus
     * performante en temps que xml (xstream/xpp) et à peine plus gourmande en
     * taille de flux.
     */
    XML("text/xml; charset=utf-8"),

    /**
     * JSON (écriture en JSON avec XStream). Note : il serait possible aussi de
     * le faire avec Jackson (http://jackson.codehaus.org/)
     */
    JSON("application/json");

    private static final String NULL_VALUE = "null";

    private static class MyObjectInputStream extends ObjectInputStream {
        private static final String PACKAGE_NAME = TransportFormat.class.getName().substring(0,
                TransportFormat.class.getName().length() - TransportFormat.class.getSimpleName().length() - 1);

        MyObjectInputStream(InputStream input) throws IOException {
            super(input);
        }

        // during deserialization, protect ourselves from malicious payload
        // http://www.ibm.com/developerworks/library/se-lookahead/index.html
        @Override
        protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
            final String name = desc.getName();
            int i = 0;
            if (name.indexOf("[[") == 0) {
                // 2 dimensions array
                i++;
            }
            if (name.indexOf("[L", i) == i) {
                // 1 dimension array
                i += 2;
            }
            if (name.indexOf("java.lang.", i) == i || name.indexOf("java.util.", i) == i || name
                    .indexOf(PACKAGE_NAME, i) == i || name.length() <= 2) {
                return super.resolveClass(desc);
            }
            throw new ClassNotFoundException(name);
        }
    }

    private final String code; // NOPMD
    private final String mimeType; // NOPMD

    TransportFormat(String mimeType) {
        this.mimeType = mimeType;
        this.code = this.toString().toLowerCase(Locale.ENGLISH);
    }

    public static TransportFormat valueOfIgnoreCase(String transportFormat) {
        return valueOf(transportFormat.toUpperCase(Locale.ENGLISH).trim());
    }

    static boolean isATransportFormat(String format) {
        if (format == null) {
            return false;
        }
        final String upperCase = format.toUpperCase(Locale.ENGLISH).trim();
        for (final TransportFormat transportFormat : TransportFormat.values()) {
            if (transportFormat.toString().equals(upperCase)) {
                return true;
            }
        }
        return false;
    }

    public static void pump(InputStream input, OutputStream output) throws IOException {
        final byte[] bytes = new byte[4 * 1024];
        int length = input.read(bytes);
        while (length != -1) {
            output.write(bytes, 0, length);
            length = input.read(bytes);
        }
    }

    public String getCode() {
        return code;
    }

    String getMimeType() {
        return mimeType;
    }

    void checkDependencies() throws IOException {
        if (this == XML || this == JSON) {
            try {
                Class.forName("com.thoughtworks.xstream.XStream");
            } catch (final ClassNotFoundException e) {
                throw new IOException(
                        "Classes of the XStream library not found. Add the XStream dependency in your webapp for the XML or JSON formats.",
                        e);
            }
        }
        if (this == XML) {
            try {
                Class.forName("org.xmlpull.v1.XmlPullParser");
            } catch (final ClassNotFoundException e) {
                throw new IOException(
                        "Classes of the XPP3 library not found. Add the XPP3 dependency in your webapp for the XML format.",
                        e);
            }
        }
    }

    public static ObjectInputStream createObjectInputStream(InputStream input) throws IOException {
        return new MyObjectInputStream(input);
    }
}
