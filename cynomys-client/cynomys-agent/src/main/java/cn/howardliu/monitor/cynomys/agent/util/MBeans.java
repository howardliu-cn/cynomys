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

import cn.howardliu.monitor.cynomys.agent.dto.MBeanNode;

import javax.management.*;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.TabularData;
import java.lang.management.ManagementFactory;
import java.util.*;

/**
 * Objet récupérant une instance de MBeanServer lors de sa construction
 * et permettant de récupérer différentes données sur les MBeans.
 *
 * @author Emeric Vernat
 */
public final class MBeans {
    /**
     * Separator between mbeans attributes in the External API.
     */
    static final char ATTRIBUTES_SEPARATOR = '|';

    private static final String JAVA_LANG_MBEAN_DESCRIPTION = "Information on the management interface of the MBean";
    private static final Comparator<MBeanNode> NODE_COMPARATOR = new Comparator<MBeanNode>() {
        @Override
        public int compare(MBeanNode o1, MBeanNode o2) {
            return o1.getName() != null ? o1.getName().compareTo(o2.getName()) : 0;
        }
    };
    private static final Comparator<MBeanNode.MBeanAttribute> ATTRIBUTE_COMPARATOR = new Comparator<MBeanNode.MBeanAttribute>() {
        @Override
        public int compare(MBeanNode.MBeanAttribute o1, MBeanNode.MBeanAttribute o2) {
            return o1.getName().compareTo(o2.getName());
        }
    };
    private final MBeanServer mbeanServer;

    public MBeans() {
        this(getPlatformMBeanServer());
    }

    private MBeans(MBeanServer mbeanServer) {
        super();
        this.mbeanServer = mbeanServer;
    }

    public Set<ObjectName> getTomcatThreadPools() throws MalformedObjectNameException {
        return mbeanServer.queryNames(new ObjectName("*:type=ThreadPool,*"), null);
    }

    public Set<ObjectName> getTomcatGlobalRequestProcessors() throws MalformedObjectNameException {
        return mbeanServer.queryNames(new ObjectName("*:type=GlobalRequestProcessor,*"), null);
    }

    public Set<ObjectName> getNioBufferPools() throws MalformedObjectNameException {
        return mbeanServer.queryNames(new ObjectName("java.nio:type=BufferPool,*"), null);
    }

    public Object getAttribute(ObjectName name, String attribute) throws JMException {
        return mbeanServer.getAttribute(name, attribute);
    }

    public Set<ObjectName> getAttribute(ObjectName name, QueryExp attribute) {
        return mbeanServer.queryNames(name, attribute);
    }

    public static List<MBeanNode> getAllMBeanNodes() throws JMException {
        initJRockitMBeansIfNeeded();

        final List<MBeanNode> result = new ArrayList<>();
        final MBeanServer platformMBeanServer = getPlatformMBeanServer();
        final MBeanNode platformNode = new MBeanNode("");
        // MBeans pour la plateforme
        final MBeans platformMBeans = new MBeans();
        platformNode.getChildren().addAll(platformMBeans.getMBeanNodes());
        result.add(platformNode);

        // pour JBoss 5.0.x, les MBeans de JBoss sont dans un autre MBeanServer
        for (final MBeanServer mbeanServer : getMBeanServers()) {
            if (!mbeanServer.equals(platformMBeanServer)) {
                final MBeanNode node = new MBeanNode(mbeanServer.getDefaultDomain());
                final MBeans mbeans = new MBeans(mbeanServer);
                node.getChildren().addAll(mbeans.getMBeanNodes());
                result.add(node);
            }
        }
        return result;
    }

    private static void initJRockitMBeansIfNeeded() {
        // si jrockit, on initialise les MBeans spécifiques jrockit lors de la première demande
        if (System.getProperty("java.vendor").contains("BEA")) {
            try {
                // initialisation des MBeans jrockit comme indiqué dans http://blogs.oracle.com/hirt/jrockit/
                try {
                    getPlatformMBeanServer().getMBeanInfo(
                            new ObjectName("bea.jrockit.management:type=JRockitConsole"));
                } catch (final InstanceNotFoundException e1) {
                    getPlatformMBeanServer().createMBean("bea.jrockit.management.JRockitConsole",
                            null);
                }
            } catch (final JMException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    private List<MBeanNode> getMBeanNodes() throws JMException {
        final List<MBeanNode> result = new ArrayList<>();
        final Set<ObjectName> names = mbeanServer.queryNames(null, null);
        for (final ObjectName name : names) {
            final String domain = name.getDomain();
            if ("jboss.deployment".equals(domain)) {
                // la partie "jboss.deployment" dans JBoss (5.0.x) est plutôt inutile et trop lourde
                continue;
            }
            MBeanNode domainNode = getMBeanNodeFromList(result, domain);
            if (domainNode == null) {
                domainNode = new MBeanNode(domain);
                result.add(domainNode);
            }
            final String keyPropertyListString = name.getKeyPropertyListString();
            final String firstPropertyValue;
            final int indexOf = keyPropertyListString.indexOf('=');
            if (indexOf == -1) {
                // n'arrive probablement pas, mais au cas où
                firstPropertyValue = null;
            } else {
                firstPropertyValue = name
                        .getKeyProperty(keyPropertyListString.substring(0, indexOf));
            }
            if ("Servlet".equals(firstPropertyValue) && "jonas".equals(domain)) {
                // la partie "jonas:j2eeType=Servlet" dans Jonas (5.1.0) est trop lourde
                continue;
            }
            MBeanNode firstPropertyNode = getMBeanNodeFromList(domainNode.getChildren(),
                    firstPropertyValue);
            if (firstPropertyNode == null) {
                firstPropertyNode = new MBeanNode(firstPropertyValue);
                domainNode.getChildren().add(firstPropertyNode);
            }
            final MBeanNode mbean = getMBeanNode(name);
            firstPropertyNode.getChildren().add(mbean);
        }
        sortMBeanNodes(result);
        return result;
    }

    private void sortMBeanNodes(List<MBeanNode> nodes) {
        if (nodes.size() > 1) {
            Collections.sort(nodes, NODE_COMPARATOR);
        }

        for (final MBeanNode node : nodes) {
            final List<MBeanNode> children = node.getChildren();
            if (children != null) {
                sortMBeanNodes(children);
            }
            final List<MBeanNode.MBeanAttribute> attributes = node.getAttributes();
            if (attributes != null && attributes.size() > 1) {
                Collections.sort(attributes, ATTRIBUTE_COMPARATOR);
            }
        }
    }

    private static MBeanNode getMBeanNodeFromList(List<MBeanNode> list, String name) {
        for (final MBeanNode node : list) {
            if (node.getName().equals(name)) {
                return node;
            }
        }
        return null;
    }

    private MBeanNode getMBeanNode(ObjectName name) throws JMException {
        final String mbeanName = name.toString();
        final MBeanInfo mbeanInfo = mbeanServer.getMBeanInfo(name);
        final String description = formatDescription(mbeanInfo.getDescription());
        final MBeanAttributeInfo[] attributeInfos = mbeanInfo.getAttributes();
        final List<MBeanNode.MBeanAttribute> attributes = getAttributes(name, attributeInfos);
        // les attributs seront triés par ordre alphabétique dans getMBeanNodes
        return new MBeanNode(mbeanName, description, attributes);
    }

    private List<MBeanNode.MBeanAttribute> getAttributes(ObjectName name,
                                                         MBeanAttributeInfo[] attributeInfos) {
        final List<String> attributeNames = new ArrayList<>(attributeInfos.length);
        for (final MBeanAttributeInfo attribute : attributeInfos) {
            // on ne veut pas afficher l'attribut password, jamais
            // (notamment, dans users tomcat ou dans datasources tomcat)
            if (attribute.isReadable() && !"password".equalsIgnoreCase(attribute.getName())) {
                attributeNames.add(attribute.getName());
            }
        }
        final String[] attributeNamesArray = attributeNames
                .toArray(new String[attributeNames.size()]);
        final List<MBeanNode.MBeanAttribute> result = new ArrayList<>();
        try {
            // issue 116: asList sur mbeanServer.getAttributes(name, attributeNamesArray) n'existe qu'en java 1.6
            final List<Object> attributes = mbeanServer.getAttributes(name, attributeNamesArray);
            for (final Object object : attributes) {
                final Attribute attribute = (Attribute) object;
                final Object value = convertValueIfNeeded(attribute.getValue());
                final String attributeDescription = getAttributeDescription(attribute.getName(),
                        attributeInfos);
                final String formattedAttributeValue = formatAttributeValue(value);
                final MBeanNode.MBeanAttribute mbeanAttribute = new MBeanNode.MBeanAttribute(attribute.getName(),
                        attributeDescription, formattedAttributeValue);
                result.add(mbeanAttribute);
            }
        } catch (final Exception e) {
            // issue 201: do not stop to render MBeans tree when exception in mbeanServer.getAttributes
            final MBeanNode.MBeanAttribute mbeanAttribute = new MBeanNode.MBeanAttribute("exception", null,
                    e.toString());
            result.add(mbeanAttribute);
        }
        return result;
    }

    private String formatAttributeValue(Object attributeValue) {
        try {
            if (attributeValue instanceof List) {
                final StringBuilder sb = new StringBuilder();
                sb.append('[');
                boolean first = true;
                for (final Object value : (List<?>) attributeValue) {
                    if (first) {
                        first = false;
                    } else {
                        sb.append(",\n");
                    }
                    sb.append(String.valueOf(value));
                }
                sb.append(']');
                return sb.toString();
            }
            return String.valueOf(attributeValue);
        } catch (final Exception e) {
            return e.toString();
        }
    }

    private String formatDescription(String description) {
        // les descriptions des MBeans de java.lang n'apportent aucune information utile
        if (description == null || JAVA_LANG_MBEAN_DESCRIPTION.equals(description)) {
            return null;
        }
        int indexOf = description.indexOf("  ");
        if (indexOf != -1) {
            // certaines descriptions de MBeans ou d'attributs dans Tomcat 6 et 7 contiennent de nombreux espaces qui se suivent
            final StringBuilder sb = new StringBuilder(description);
            while (indexOf != -1) {
                sb.deleteCharAt(indexOf);
                indexOf = sb.indexOf("  ");
            }
            return sb.toString();
        }
        return description;
    }

    private Object convertValueIfNeeded(Object value) {
        if (value instanceof CompositeData) {
            final CompositeData data = (CompositeData) value;
            final Map<String, Object> values = new TreeMap<>();
            for (final String key : data.getCompositeType().keySet()) {
                values.put(key, convertValueIfNeeded(data.get(key)));
            }
            return values;
        } else if (value instanceof CompositeData[]) {
            final List<Object> list = new ArrayList<>();
            for (final CompositeData data : (CompositeData[]) value) {
                list.add(convertValueIfNeeded(data));
            }
            return list;
        } else if (value instanceof Object[]) {
            return Arrays.asList((Object[]) value);
        } else if (value instanceof TabularData) {
            final TabularData tabularData = (TabularData) value;
            return convertValueIfNeeded(tabularData.values());
        } else if (value instanceof Collection) {
            final List<Object> list = new ArrayList<>();
            for (final Object data : (Collection<?>) value) {
                list.add(convertValueIfNeeded(data));
            }
            return list;
        }
        return convertJRockitValueIfNeeded(value);
    }

    private static Object convertJRockitValueIfNeeded(Object value) {
        if (value instanceof double[]) {
            // pour jrockit MBeans
            final List<Double> list = new ArrayList<>();
            for (final double data : (double[]) value) {
                list.add(data);
            }
            return list;
        } else if (value instanceof int[]) {
            // pour jrockit MBeans
            final List<Integer> list = new ArrayList<>();
            for (final int data : (int[]) value) {
                list.add(data);
            }
            return list;
        }
        return value;
    }

    private static List<Object> getConvertedAttributes(List<String> mbeanAttributes) {
        initJRockitMBeansIfNeeded();

        final List<Object> result = new ArrayList<>();
        final List<MBeanServer> mBeanServers = getMBeanServers();
        for (final String mbeansAttribute : mbeanAttributes) {
            final int lastIndexOfPoint = mbeansAttribute.lastIndexOf('.');
            if (lastIndexOfPoint <= 0) {
                throw new IllegalArgumentException(mbeansAttribute);
            }
            final String name = mbeansAttribute.substring(0, lastIndexOfPoint);
            final String attribute = mbeansAttribute.substring(lastIndexOfPoint + 1);
            // on ne veut pas afficher l'attribut password, jamais
            // (notamment, dans users tomcat ou dans datasources tomcat)
            if ("password".equalsIgnoreCase(attribute)) {
                throw new IllegalArgumentException(name + '.' + attribute);
            }
            InstanceNotFoundException instanceNotFoundException = null;
            for (final MBeanServer mbeanServer : mBeanServers) {
                try {
                    final MBeans mbeans = new MBeans(mbeanServer);
                    final Object jmxValue = mbeans.convertValueIfNeeded(
                            mbeans.getAttribute(new ObjectName(name), attribute));
                    result.add(jmxValue);
                    instanceNotFoundException = null;
                    // ObjectName trouvé dans ce MBeanServer, inutile de chercher dans les suivants
                    // où il n'est d'ailleurs pas
                    break;
                } catch (final InstanceNotFoundException e) {
                    // ObjectName non trouvé dans ce MBeanServer, donc on cherche dans le suivant
                    // (nécessaire pour JBoss 5.0.x)
                    instanceNotFoundException = e;
                } catch (final JMException e) {
                    throw new IllegalArgumentException(name + '.' + attribute, e);
                }
            }
            if (instanceNotFoundException != null) {
                throw new IllegalArgumentException(name + '.' + attribute,
                        instanceNotFoundException);
            }
        }
        return result;
    }

    public static String getConvertedAttributes(String jmxValueParameter) {
        final List<String> mbeanAttributes = Arrays
                .asList(jmxValueParameter.split("[" + ATTRIBUTES_SEPARATOR + ']'));
        final List<Object> jmxValues = getConvertedAttributes(mbeanAttributes);
        final StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (final Object jmxValue : jmxValues) {
            if (first) {
                first = false;
            } else {
                sb.append(ATTRIBUTES_SEPARATOR);
            }
            sb.append(jmxValue);
        }
        return sb.toString();
    }

    private String getAttributeDescription(String name, MBeanAttributeInfo[] attributeInfos) {
        for (final MBeanAttributeInfo attributeInfo : attributeInfos) {
            if (name.equals(attributeInfo.getName())) {
                // certaines descriptions d'attributs comme les NamingResources dans Tomcat 7 contiennent aussi des espaces qui se suivent
                final String attributeDescription = formatDescription(
                        attributeInfo.getDescription());
                if (attributeDescription == null || name.equals(attributeDescription)
                        || attributeDescription.length() == 0) {
                    // les attributs des MBeans de java.lang ont des descriptions égales aux noms,
                    // ce sont des descriptions inutiles
                    return null;
                }
                return attributeDescription;
            }
        }
        return null;
    }

    /**
     * Retourne le javax.management.MBeanServer de la plateforme.
     *
     * @return MBeanServer
     */
    public static MBeanServer getPlatformMBeanServer() {
        return ManagementFactory.getPlatformMBeanServer();
    }

    /**
     * Retourne la liste de tous les javax.management.MBeanServer.
     *
     * @return List
     */
    private static List<MBeanServer> getMBeanServers() {
        // par exemple avec JBoss 5.0.x, il y a un MBeanServer de la plateforme (defaultDomain null)
        // et un MBeanServer de JBoss (defaultDomain "jboss")
        return MBeanServerFactory.findMBeanServer(null);
    }
}
