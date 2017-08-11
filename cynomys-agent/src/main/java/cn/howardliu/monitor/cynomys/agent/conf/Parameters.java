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
package cn.howardliu.monitor.cynomys.agent.conf;

import cn.howardliu.gear.monitor.core.NetUtils;

import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Properties;


/**
 * Classe d'accès aux paramètres du monitoring.
 *
 * @author Emeric Vernat
 */
public final class Parameters {
    public static final String PARAMETER_SYSTEM_PREFIX = "wfj-netty-monitor.";
    public static final File TEMPORARY_DIRECTORY = new File(System.getProperty("java.io.tmpdir"));
    public static final String JAVA_VERSION = System.getProperty("java.version");
    public static final String WFJ_NETTY_MONITOR_VERSION = getMonitorVersion();

    private static final int DEFAULT_RESOLUTION_SECONDS = 60;
    private static final String DEFAULT_DIRECTORY = "wfj-netty-monitor";

    private static final String COLLECTOR_APPLICATIONS_FILENAME = "applications.properties";
    private static Map<String, List<URL>> urlsByApplications;

    private static FilterConfig filterConfig;
    private static ServletContext servletContext;
    private static String lastConnectUrl;
    private static Properties lastConnectInfo;
    private static boolean dnsLookupsDisabled;

    private Parameters() {
        super();
    }

    public static void initialize(FilterConfig config) {
        filterConfig = config;
        if (config != null) {
            final ServletContext context = config.getServletContext();
            initialize(context);
        }
    }

    public static void initialize(ServletContext context) {
        if ("1.6".compareTo(JAVA_VERSION) > 0) {
            throw new IllegalStateException("La version java doit être 1.6 au minimum et non " + JAVA_VERSION);
        }
        servletContext = context;

        dnsLookupsDisabled = Boolean.parseBoolean(getParameter(Parameter.DNS_LOOKUPS_DISABLED));
    }

    public static void initJdbcDriverParameters(String connectUrl, Properties connectInfo) {
        Parameters.lastConnectUrl = connectUrl;
        Parameters.lastConnectInfo = connectInfo;
    }

    /**
     * @return Contexte de servlet de la webapp, soit celle monitorée ou soit
     * celle de collecte.
     */
    public static ServletContext getServletContext() {
        assert servletContext != null;
        return servletContext;
    }

    public static String getLastConnectUrl() {
        return lastConnectUrl;
    }

    public static Properties getLastConnectInfo() {
        return lastConnectInfo;
    }

    private static void writeCollectorApplications() throws IOException {
        final Properties properties = new Properties();

        for (final Map.Entry<String, List<URL>> entry : urlsByApplications.entrySet()) {
            final List<URL> urls = entry.getValue();
            assert urls != null && !urls.isEmpty();
            final StringBuilder sb = new StringBuilder();
            for (final URL url : urls) {
                final String urlString = url.toString();
                sb.append(urlString).append(',');
            }
            sb.delete(sb.length() - 1, sb.length());
            properties.put(entry.getKey(), sb.toString());
        }
        final File collectorApplicationsFile = getCollectorApplicationsFile();
        final File directory = collectorApplicationsFile.getParentFile();
        if (!directory.mkdirs() && !directory.exists()) {
            throw new IOException("WFJ-Netty-Monitor directory can't be created: " + directory.getPath());
        }
        try (FileOutputStream output = new FileOutputStream(collectorApplicationsFile)) {
            properties.store(output, "urls of the applications to monitor");
        }
    }

    public static File getCollectorApplicationsFile() {
        return new File(getStorageDirectory(""), COLLECTOR_APPLICATIONS_FILENAME);
    }


    /**
     * @return nom réseau de la machine
     */
    public static String getHostName() {
        if (dnsLookupsDisabled) {
            return "localhost";
        }
        return NetUtils.getLocalHostname();
    }

    /**
     * @return adresse ip de la machine
     */
    public static String getHostAddress() {
        if (dnsLookupsDisabled) {
            return "127.0.0.1"; // NOPMD
        }
        return NetUtils.getLocalHostAddress();
    }

    /**
     * @param fileName Nom du fichier de resource.
     * @return Chemin complet d'une resource.
     */
    public static String getResourcePath(String fileName) {
        final Class<Parameters> classe = Parameters.class;
        final String packageName = classe.getName()
                .substring(0, classe.getName().length() - classe.getSimpleName().length() - 1);
        return '/' + packageName.replace('.', '/') + "/resource/" + fileName;
    }

    /**
     * @return Résolution en secondes des courbes et période d'appels par le
     * serveur de collecte le cas échéant.
     */
    public static int getResolutionSeconds() {
        final String param = getParameter(Parameter.RESOLUTION_SECONDS);
        if (param != null) {
            // lance une NumberFormatException si ce n'est pas un nombre
            final int result = Integer.parseInt(param);
            if (result <= 0) {
                throw new IllegalStateException(
                        "The parameter resolution-seconds should be > 0 (between 60 and 600 recommended)");
            }
            return result;
        }
        return DEFAULT_RESOLUTION_SECONDS;
    }

    /**
     * @param application Nom de l'application
     * @return Répertoire de stockage des compteurs et des données pour les
     * courbes.
     */
    public static File getStorageDirectory(String application) {
        final String param = getParameter(Parameter.STORAGE_DIRECTORY);
        final String dir;
        if (param == null) {
            dir = DEFAULT_DIRECTORY;
        } else {
            dir = param;
        }
        // Si le nom du répertoire commence par '/' (ou "drive specifier" sur
        // Windows),
        // on considère que c'est un chemin absolu,
        // sinon on considère que c'est un chemin relatif par rapport au
        // répertoire temporaire
        // ('temp' dans TOMCAT_HOME pour tomcat).
        final String directory;
        if (dir.length() > 0 && new File(dir).isAbsolute()) {
            directory = dir;
        } else {
            directory = TEMPORARY_DIRECTORY.getPath() + '/' + dir;
        }
        if (servletContext != null) {
            return new File(directory + '/' + application);
        }
        return new File(directory);
    }

    /**
     * Booléen selon que le paramètre no-database vaut true.
     *
     * @return boolean
     */
    public static boolean isNoDatabase() {
        return Boolean.parseBoolean(Parameters.getParameter(Parameter.NO_DATABASE));
    }

    /**
     * Booléen selon que le paramètre system-actions-enabled vaut true.
     *
     * @return boolean
     */
    public static boolean isSystemActionsEnabled() {
        final String parameter = Parameters.getParameter(Parameter.SYSTEM_ACTIONS_ENABLED);
        return parameter == null || Boolean.parseBoolean(parameter);
    }

    /**
     * Retourne false si le paramètre displayed-counters n'a pas été défini ou
     * si il contient le compteur dont le nom est paramètre, et retourne true
     * sinon (c'est-à-dire si le paramètre displayed-counters est défini et si
     * il ne contient pas le compteur dont le nom est paramètre).
     *
     * @param counterName Nom du compteur
     * @return boolean
     */
    public static boolean isCounterHidden(String counterName) {
        final String displayedCounters = getParameter(Parameter.DISPLAYED_COUNTERS);
        if (displayedCounters == null) {
            return false;
        }
        for (final String displayedCounter : displayedCounters.split(",")) {
            final String displayedCounterName = displayedCounter.trim();
            if (counterName.equalsIgnoreCase(displayedCounterName)) {
                return false;
            }
        }
        return true;
    }

    /**
     * @return Nom de l'application courante et nom du sous-répertoire de
     * stockage dans une application monitorée.
     */
    public static String getCurrentApplication() {
        if (servletContext != null) {
            // Le nom de l'application et donc le stockage des fichiers est dans
            // le sous-répertoire
            // ayant pour nom le contexte de la webapp et le nom du serveur
            // pour pouvoir monitorer plusieurs webapps sur le même serveur et
            // pour pouvoir stocker sur un répertoire partagé entre plusieurs
            // serveurs
            return getContextPath(servletContext) + '_' + getHostName();
        }
        return null;
    }

    public static String getContextPath(ServletContext context) {
        // cette méthode retourne le contextPath de la webapp
        // en utilisant ServletContext.getContextPath si servlet api 2.5
        // ou en se débrouillant sinon
        // (on n'a pas encore pour l'instant de request pour appeler
        // HttpServletRequest.getContextPath)
        if (context.getMajorVersion() == 2 && context.getMinorVersion() >= 5 || context.getMajorVersion() > 2) {
            // api servlet 2.5 (Java EE 5) minimum pour appeler
            // ServletContext.getContextPath
            return context.getContextPath();
        }
        final URL webXmlUrl;
        try {
            webXmlUrl = context.getResource("/WEB-INF/web.xml");
        } catch (final MalformedURLException e) {
            throw new IllegalStateException(e);
        }
        String contextPath = webXmlUrl.toExternalForm();
        contextPath = contextPath.substring(0, contextPath.indexOf("/WEB-INF/web.xml"));
        final int indexOfWar = contextPath.indexOf(".war");
        if (indexOfWar > 0) {
            contextPath = contextPath.substring(0, indexOfWar);
        }
        // tomcat peut renvoyer une url commençant pas "jndi:/localhost"
        // (v5.5.28, webapp dans un répertoire)
        if (contextPath.startsWith("jndi:/localhost")) {
            contextPath = contextPath.substring("jndi:/localhost".length());
        }
        final int lastIndexOfSlash = contextPath.lastIndexOf('/');
        if (lastIndexOfSlash != -1) {
            contextPath = contextPath.substring(lastIndexOfSlash);
        }
        return contextPath;
    }

    private static String getMonitorVersion() {
        EnvPropertyConfig.init();
        return EnvPropertyConfig.getContextProperty(Constant.SYSTEM_SEETING_SERVER_DEFAULT_SERVER_VERSION);
    }

    /**
     * Recherche la valeur d'un paramètre qui peut être défini par ordre de
     * priorité croissant : - dans les paramètres d'initialisation du filtre
     * (fichier web.xml dans la webapp) - dans les paramètres du contexte de la
     * webapp avec le préfixe "javamelody." (fichier xml de contexte dans
     * Tomcat) - dans les variables d'environnement du système d'exploitation
     * avec le préfixe "javamelody." - dans les propriétés systèmes avec le
     * préfixe "javamelody." (commande de lancement java)
     *
     * @param parameter Enum du paramètre
     * @return valeur du paramètre ou null si pas de paramètre défini
     */
    public static String getParameter(Parameter parameter) {
        assert parameter != null;
        final String name = parameter.getCode();
        return getParameterByName(name);
    }

    static String getParameterByName(String parameterName) {
        assert parameterName != null;
        final String globalName = PARAMETER_SYSTEM_PREFIX + parameterName;
        String result = System.getProperty(globalName);
        if (result != null) {
            return result;
        }
        if (servletContext != null) {
            result = servletContext.getInitParameter(globalName);
            if (result != null) {
                return result;
            }
            // issue 463: in a ServletContextListener, it's also possible to
            // call servletContext.setAttribute("javamelody.log", "true"); for
            // example
            final Object attribute = servletContext.getAttribute(globalName);
            if (attribute instanceof String) {
                return (String) attribute;
            }
        }
        if (filterConfig != null) {
            result = filterConfig.getInitParameter(parameterName);
            if (result != null) {
                return result;
            }
        }
        return null;
    }
}
