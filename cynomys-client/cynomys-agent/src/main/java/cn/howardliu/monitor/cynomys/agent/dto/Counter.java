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
package cn.howardliu.monitor.cynomys.agent.dto;

import cn.howardliu.monitor.cynomys.common.ThreadMXBeanUtils;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Données statistiques des requêtes pour un compteur nommé comme http ou sql.
 * Ces données sont accumulées au fil du temps selon les requêtes dans
 * l'application. Elles correspondent soit aux statistiques courantes depuis une
 * date initiale, soit à une période donnée pour un jour, une semaine, un mois
 * ou une année.
 * <p>
 * Toutes les méthodes sur une instance de cette classe sont conçues pour être
 * thread-safe, c'est-à-dire qu'elles gère un état qui est non modifiable ou
 * alors synchronisé pour être accessible et modifiable depuis plusieurs
 * threads. Les instances sont sérialisables pour pouvoir être persistées sur
 * disque et transmises au serveur de collecte.
 *
 * @author Emeric Vernat
 */
public class Counter implements Cloneable, Serializable { // NOPMD
    /**
     * Nom du counter des requêtes http.
     */
    public static final String HTTP_COUNTER_NAME = "http";
    /**
     * Nom du counter des erreurs systèmes http.
     */
    public static final String ERROR_COUNTER_NAME = "error";
    /**
     * Nom du counter des logs d'erreurs systèmes.
     */
    public static final String LOG_COUNTER_NAME = "log";
    /**
     * Nom du counter des JSPs.
     */
    public static final String JSP_COUNTER_NAME = "jsp";
    /**
     * Nom du counter des actions Struts.
     */
    public static final String STRUTS_COUNTER_NAME = "struts";
    /**
     * Nom du counter des actions JSF RI (Mojarra).
     */
    public static final String JSF_COUNTER_NAME = "jsf";
    /**
     * Nom du counter des requêtes SQL.
     */
    public static final String SQL_COUNTER_NAME = "sql";
    /**
     * Nom du counter des jobs.
     */
    public static final String JOB_COUNTER_NAME = "job";
    /**
     * Nom du counter des builds Jenkins.
     */
    public static final String BUILDS_COUNTER_NAME = "builds";
    /**
     * Caractère de remplacement s'il y a des paramètres *-transform-pattern.
     */
    public static final char TRANSFORM_REPLACEMENT_CHAR = '$';
    /**
     * Nombre max d'erreurs conservées par le counter (si counter d'erreurs http
     * ou de log d'erreurs).
     */
    public static final int MAX_ERRORS_COUNT = 100;
    /**
     * Nombre max par défaut de requêtes conservées par counter, <br>
     * mais peut être redéfini par exemple pour le counter des erreurs http ou
     * celui des logs.
     */
    public static final int MAX_REQUESTS_COUNT = 10000;
    private static final String TRANSFORM_REPLACEMENT = "\\" + TRANSFORM_REPLACEMENT_CHAR;
    private static final long serialVersionUID = 6759729262180992976L;
    private String application;
    private boolean displayed = true;
    private transient boolean used;
    private final String name;
    private final boolean errorCounter;
    private final String storageName;
    private final String iconName;
    private final String childCounterName;
    private final ConcurrentMap<String, CounterRequest> requests = new ConcurrentHashMap<>();
    private final ConcurrentMap<Long, CounterRequestContext> rootCurrentContextsByThreadId = new ConcurrentHashMap<>();
    private final LinkedList<CounterError> errors;
    private Date startDate = new Date();
    private int maxRequestsCount = MAX_REQUESTS_COUNT;

    private final transient ThreadLocal<CounterRequestContext> contextThreadLocal;
    private transient Pattern requestTransformPattern;

    private void closeCounterRequestContext(CounterRequestContext requestContext) {
        if (requestContext != null) {
            final List<CounterRequestContext> childContexts = requestContext.getChildContexts();
            if (childContexts != null) {
                for (final CounterRequestContext childContext : childContexts) {
                    closeCounterRequestContext(childContext);
                }
            }
            requestContext.close();
        }
    }

    /**
     * Comparateur pour ordonner les requêtes par sommes des durées.
     */
    static final class CounterRequestComparator implements Comparator<CounterRequest>, Serializable {
        private static final long serialVersionUID = 1L;

        /**
         * {@inheritDoc}
         */
        @Override
        public int compare(CounterRequest request1, CounterRequest request2) {
            return Long.compare(request1.getDurationsSum(), request2.getDurationsSum());
        }
    }

    /**
     * Comparateur pour ordonner les requêtes par nombre d'exécutions.
     */
    static final class CounterRequestByHitsComparator implements Comparator<CounterRequest>, Serializable {
        private static final long serialVersionUID = 1L;

        /**
         * {@inheritDoc}
         */
        @Override
        public int compare(CounterRequest request1, CounterRequest request2) {
            return Long.compare(request1.getHits(), request2.getHits());
        }
    }

    /**
     * Comparateur pour ordonner les erreurs par heures d'exécution.
     */
    static final class CounterErrorComparator implements Comparator<CounterError>, Serializable {
        private static final long serialVersionUID = 1L;

        /**
         * {@inheritDoc}
         */
        @Override
        public int compare(CounterError error1, CounterError error2) {
            return Long.compare(error1.getTime(), error2.getTime());
        }
    }

    /**
     * Comparateur pour ordonner les requêtes en cours par durées écoulées.
     */
    static final class CounterRequestContextComparator implements Comparator<CounterRequestContext>, Serializable {
        private static final long serialVersionUID = 1L;
        private final long timeOfSnapshot;

        CounterRequestContextComparator(long timeOfSnapshot) {
            super();
            this.timeOfSnapshot = timeOfSnapshot;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int compare(CounterRequestContext context1, CounterRequestContext context2) {
            return Integer.compare(context1.getDuration(timeOfSnapshot), context2.getDuration(timeOfSnapshot));
        }
    }

    /**
     * Constructeur d'un compteur.
     *
     * @param name     Nom du compteur (par exemple: sql...)
     * @param iconName Icône du compteur (par exemple: db.png)
     */
    public Counter(String name, String iconName) {
        // ici, pas de compteur fils
        this(name, name, iconName, null, new ThreadLocal<>());
    }

    /**
     * Constructeur d'un compteur.
     *
     * @param name         Nom du compteur (par exemple: http...)
     * @param iconName     Icône du compteur (par exemple: db.png)
     * @param childCounter Compteur fils (par exemple: sqlCounter)
     */
    public Counter(String name, String iconName, Counter childCounter) {
        this(name, name, iconName, childCounter.getName(), childCounter.contextThreadLocal);
    }

    private Counter(String name, String storageName, String iconName, String childCounterName, ThreadLocal<CounterRequestContext> contextThreadLocal) {
        super();
        assert name != null;
        assert storageName != null;
        this.name = name;
        this.storageName = storageName;
        this.errorCounter = ERROR_COUNTER_NAME.equals(name) || LOG_COUNTER_NAME.equals(name) || JOB_COUNTER_NAME.equals(name);
        this.iconName = iconName;
        this.childCounterName = childCounterName;
        this.contextThreadLocal = contextThreadLocal;
        if (errorCounter) {
            this.errors = new LinkedList<>();
        } else {
            this.errors = null;
        }
    }

    /**
     * Définit le code de l'application de ce counter (non null).
     *
     * @param application String
     */
    public void setApplication(String application) {
        assert application != null;
        this.application = application;
    }

    /**
     * Retourne le code de l'application.
     *
     * @return String
     */
    public String getApplication() {
        return application;
    }

    /**
     * Retourne le nom de ce counter (non null).
     *
     * @return String
     */
    public String getName() {
        return name;
    }

    /**
     * Retourne le nom de ce counter quand il est stocké sur disque (non null).
     *
     * @return String
     */
    String getStorageName() {
        return storageName;
    }

    /**
     * Retourne le nom de l'icône de ce counter (peut être null).
     *
     * @return String
     */
    public String getIconName() {
        return iconName;
    }

    /**
     * Retourne le nom de l'éventuel counter fils (peut être null).
     *
     * @return String
     */
    public String getChildCounterName() {
        return childCounterName;
    }

    boolean hasChildHits() {
        for (final CounterRequest request : requests.values()) {
            if (request.hasChildHits()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Retourne la date et l'heure de début (non null).
     *
     * @return Date
     */
    public Date getStartDate() {
        return startDate;
    }

    /**
     * Définit la date et l'heure de début (non null).
     *
     * @param startDate Date
     */
    public void setStartDate(Date startDate) {
        assert startDate != null;
        this.startDate = startDate;
    }

    /**
     * Retourne true si ce counter est affiché dans les rapports.
     *
     * @return boolean
     */
    public boolean isDisplayed() {
        return displayed;
    }

    /**
     * Définit si ce counter est affiché dans les rapports.
     *
     * @param displayed boolean
     */
    public void setDisplayed(boolean displayed) {
        this.displayed = displayed;
    }

    /**
     * Retourne true si ce counter est utilisé (servira éventuellement à
     * initialiser displayed dans FilterContext).
     *
     * @return boolean
     */
    boolean isUsed() {
        return used;
    }

    /**
     * Définit si ce counter est utilisé (servira éventuellement à initialiser
     * displayed dans FilterContext).
     *
     * @param used boolean
     */
    public void setUsed(boolean used) {
        this.used = used;
    }

    /**
     * Retourne l'expression régulière permettant de transformer les requêtes de
     * ce counter avant agrégation dans les statistiques (peut être null).
     *
     * @return Pattern
     */
    public Pattern getRequestTransformPattern() {
        return requestTransformPattern;
    }

    /**
     * Définit l'expression régulière permettant de transformer les requêtes de
     * ce counter avant agrégation dans les statistiques.
     *
     * @param requestTransformPattern Pattern
     */
    public void setRequestTransformPattern(Pattern requestTransformPattern) {
        this.requestTransformPattern = requestTransformPattern;
    }

    /**
     * Retourne le nombre maximum de requêtes dans ce counter (entier positif).
     *
     * @return int
     */
    public int getMaxRequestsCount() {
        return maxRequestsCount;
    }

    /**
     * Définit le nombre maximum de requêtes dans ce counter (entier positif).
     *
     * @param maxRequestsCount int
     */
    public void setMaxRequestsCount(int maxRequestsCount) {
        assert maxRequestsCount > 0;
        this.maxRequestsCount = maxRequestsCount;
    }

    public void bindContextIncludingCpu(String requestName) {
        bindContext(requestName, requestName, null, ThreadMXBeanUtils.getCurrentThreadCpuTime());
    }

    public void bindContext(String requestName, String completeRequestName, String remoteUser, long startCpuTime) {
        final CounterRequestContext context = new CounterRequestContext(this, contextThreadLocal.get(), requestName, completeRequestName, remoteUser, startCpuTime);
        contextThreadLocal.set(context);
        if (context.getParentContext() == null) {
            rootCurrentContextsByThreadId.put(context.getThreadId(), context);
        }
    }

    public void unbindContext() {
        try {
            contextThreadLocal.remove();
        } finally {
            rootCurrentContextsByThreadId.remove(Thread.currentThread().getId());
        }
    }

    public void addRequestForCurrentContext(boolean systemError) {
        final CounterRequestContext context = contextThreadLocal.get();
        if (context != null) {
            final long duration = context.getDuration(System.currentTimeMillis());
            final long cpuUsedMillis = context.getCpuTime();
            addRequest(context.getRequestName(), duration, cpuUsedMillis, systemError, -1);
        }
    }

    public void addRequestForCurrentContext(String systemErrorStackTrace) {
        assert errorCounter;
        final CounterRequestContext context = contextThreadLocal.get();
        // context peut être null (depuis JobGlobalListener, cf issue 34)
        if (context != null) {
            final long duration = context.getDuration(System.currentTimeMillis());
            final long cpuUsedMillis = context.getCpuTime();
            addRequest(context.getRequestName(), duration, cpuUsedMillis, systemErrorStackTrace != null,
                    systemErrorStackTrace, -1);
        }
    }

    public void addRequest(String requestName, long duration, long cpuTime, boolean systemError, int responseSize) {
        addRequest(requestName, duration, cpuTime, systemError, null, responseSize);
    }

    private void addRequest(String requestName, long duration, long cpuTime, boolean systemError, String systemErrorStackTrace, int responseSize) {
        assert requestName != null;
        assert duration >= 0;
        assert cpuTime >= -1; // -1 pour requêtes sql
        assert responseSize >= -1; // -1 pour requêtes sql
        final String aggregateRequestName = getAggregateRequestName(requestName);
        final CounterRequestContext context = contextThreadLocal.get();
        final CounterRequest request = getCounterRequestInternal(aggregateRequestName);
        request.addHit(duration, cpuTime, systemError, systemErrorStackTrace, responseSize);
        if (context != null) {
            // on ajoute dans la requête parente toutes les requêtes filles
            // du contexte
            if (context.getParentCounter() == this) {
                request.addChildHits(context);
            }
            request.addChildRequests(context.getChildRequestsExecutionsByRequestId());
        }
        // perf: on fait le reste hors du synchronized sur request
        if (context != null) {
            if (context.getParentCounter() == this) {
                final CounterRequestContext parentContext = context.getParentContext();
                if (parentContext == null) {
                    // enlève du threadLocal le contexte que j'ai créé
                    // si je suis le counter parent et s'il n'y a pas de
                    // contexte parent
                    unbindContext();
                } else {
                    // on ajoute une requête fille dans le contexte
                    context.addChildRequest(this, aggregateRequestName, request.getId(), duration, systemError, responseSize);
                    // et reporte les requêtes filles dans le contexte parent et
                    // rebinde celui-ci
                    parentContext.closeChildContext();
                    contextThreadLocal.set(parentContext);
                }
            } else {
                // on ajoute une requête fille dans le contexte
                // (à priori il s'agit d'une requête sql)
                context.addChildRequest(this, aggregateRequestName, request.getId(), duration, systemError, responseSize);
            }
        }
        if (systemErrorStackTrace != null) {
            assert errorCounter;
            synchronized (errors) {
                errors.addLast(new CounterError(requestName, systemErrorStackTrace));
                if (errors.size() > MAX_ERRORS_COUNT) {
                    errors.removeFirst();
                }
            }
        }
    }

    public void addRequestForSystemError(String requestName, long duration, long cpuTime, String stackTrace) {
        // comme la méthode addRequest, cette méthode n'est pas synchronisée
        // pour ne pas avoir
        // de synchronisation globale à l'application sur cette instance d'objet
        // ce qui pourrait faire une contention et des ralentissements,
        // par contre on synchronise request et errors
        assert requestName != null;
        assert duration >= -1; // -1 pour le counter de log
        assert cpuTime >= -1;
        // on ne doit conserver les stackTraces que pour les compteurs d'erreurs
        // et de logs plus limités en taille
        // car sinon cela risquerait de donner des compteurs trop gros en
        // mémoire et sur disque
        assert errorCounter;
        // le code ci-après suppose qu'il n'y a pas de contexte courant pour les
        // erreurs systèmes
        // contrairement à la méthode addRequest
        assert contextThreadLocal.get() == null;
        final String aggregateRequestName = getAggregateRequestName(requestName);
        final CounterRequest request = getCounterRequestInternal(aggregateRequestName);
        synchronized (request) {
            request.addHit(duration, cpuTime, true, stackTrace, -1);
        }
        synchronized (errors) {
            errors.addLast(new CounterError(requestName, stackTrace));
            if (errors.size() > MAX_ERRORS_COUNT) {
                errors.removeFirst();
            }
        }
    }

    /**
     * Retourne true si ce counter est un counter d'error (c'est-à-dire si son
     * nom est "error", "log" ou "job")
     *
     * @return boolean
     */
    public boolean isErrorCounter() {
        return errorCounter;
    }

    /**
     * Retourne true si ce counter est un counter de job (c'est-à-dire si son
     * nom est "job")
     *
     * @return boolean
     */
    public boolean isJobCounter() {
        return JOB_COUNTER_NAME.equals(name);
    }

    /**
     * Retourne true si ce counter est un counter de jsp ou d'actions Struts
     * (c'est-à-dire si son nom est "jsp")
     *
     * @return boolean
     */
    public boolean isJspOrStrutsCounter() {
        return JSP_COUNTER_NAME.equals(name) || STRUTS_COUNTER_NAME.equals(name);
    }

    /**
     * Retourne true si ce counter est un counter de "façades métiers" ou
     * "business façades" (c'est-à-dire si son nom est "ejb", "spring", "guice"
     * ou "services")
     *
     * @return boolean
     */
    public boolean isBusinessFacadeCounter() {
        return "services".equals(name) || "ejb".equals(name) || "spring".equals(name) || "guice".equals(name);
    }

    public boolean isRequestIdFromThisCounter(String requestId) {
        // cela marche car requestId commence par counter.getName() selon
        // CounterRequest.buildId
        return requestId.startsWith(getName());
    }

    private String getAggregateRequestName(String requestName) {
        final String aggregateRequestName;
        if (requestTransformPattern == null) {
            aggregateRequestName = requestName;
        } else {
            // ce pattern optionnel permet de transformer la description de la
            // requête
            // pour supprimer des parties variables (identifiant d'objet par
            // exemple)
            // et pour permettre l'agrégation sur cette requête
            final Matcher matcher = requestTransformPattern.matcher(requestName);
            try {
                aggregateRequestName = matcher.replaceAll(TRANSFORM_REPLACEMENT);
            } catch (final StackOverflowError e) {
                return requestName;
            }
        }
        return aggregateRequestName;
    }

    public void addRequestsAndErrors(Counter newCounter) {
        assert getName().equals(newCounter.getName());

        // Pour toutes les requêtes du compteur en paramètre,
        // on ajoute les hits aux requêtes de ce compteur
        // (utilisée dans serveur de collecte).

        // Rq: cette méthode est thread-safe comme les autres méthodes dans
        // cette classe,
        // bien que cela ne soit à priori pas nécessaire telle qu'elle est
        // utilisée dans CollectorServlet
        for (final CounterRequest newRequest : newCounter.getRequests()) {
            if (newRequest.getHits() > 0) {
                final CounterRequest request = getCounterRequestInternal(newRequest.getName());
                synchronized (request) {
                    request.addHits(newRequest);
                }
            }
        }

        int size = requests.size();
        final int maxRequests = getMaxRequestsCount();
        if (size > maxRequests) {
            // Si le nombre de requêtes est supérieur à 10000 (sql non bindé par
            // ex.),
            // on essaye ici d'éviter de saturer la mémoire (et le disque dur)
            // avec toutes ces requêtes différentes en éliminant celles ayant
            // moins de 10 hits.
            // (utile pour une agrégation par année dans PeriodCounterFactory
            // par ex.)
            // Mais inutile de le faire dans d'autres méthodes de Counter
            // car ce serait mauvais pour les perfs, cela ne laisserait aucune
            // chance
            // à une nouvelle requête et car cela sera fait par la classe
            // collector
            for (final CounterRequest request : requests.values()) {
                if (request.getHits() < 10) {
                    removeRequest(request.getName());
                    size--;
                    if (size <= maxRequests) {
                        break;
                    }
                }
            }
        }

        if (isErrorCounter()) {
            addErrors(newCounter.getErrors());
        }
    }

    public void addHits(CounterRequest counterRequest) {
        if (counterRequest.getHits() > 0) {
            // clone pour être thread-safe ici
            final CounterRequest newRequest = counterRequest.clone();
            final CounterRequest request = getCounterRequestInternal(newRequest.getName());
            synchronized (request) {
                request.addHits(newRequest);
            }
        }
    }

    public void addErrors(List<CounterError> counterErrorList) {
        assert errorCounter;
        if (counterErrorList.isEmpty()) {
            return;
        }
        synchronized (errors) {
            if (!errors.isEmpty() && errors.get(0).getTime() > counterErrorList.get(0).getTime()) {
                // tant que faire se peut on les met à peu près dans l'ordre
                // pour le sort ci après
                errors.addAll(0, counterErrorList);
            } else {
                errors.addAll(counterErrorList);
            }
            if (errors.size() > 1) {
                // "sort" a les mêmes performances sur LinkedList que sur
                // ArrayList car il y a un tableau intermédiaire
                // (selon Implementation Patterns, Kent Beck)
                Collections.sort(errors, new CounterErrorComparator());

                while (errors.size() > MAX_ERRORS_COUNT) {
                    errors.removeFirst();
                }
            }
        }
    }

    public void removeRequest(String requestName) {
        assert requestName != null;
        requests.remove(requestName);
    }

    /**
     * Retourne l'objet CounterRequest correspondant au contexte de requête en
     * cours en paramètre.
     *
     * @param context CounterRequestContext
     * @return CounterRequest
     */
    public CounterRequest getCounterRequest(CounterRequestContext context) {
        return getCounterRequestByName(context.getRequestName());
    }

    /**
     * Retourne l'objet CounterRequest correspondant au nom sans agrégation en
     * paramètre.
     *
     * @param requestName Nom de la requête sans agrégation par requestTransformPattern
     * @return CounterRequest
     */
    public CounterRequest getCounterRequestByName(String requestName) {
        final String aggregateRequestName = getAggregateRequestName(requestName);
        final CounterRequest request = getCounterRequestInternal(aggregateRequestName);
        return request.clone();
    }

    private CounterRequest getCounterRequestInternal(String requestName) {
//        System.out.println("\n\n\n");
//        System.out.println("请求数：" + getRequestsCount());
//        System.out.println("当前请求：" + requestName);
//        System.out.println("\n\n\n");
        CounterRequest request = requests.get(requestName);
        if (request == null) {
            request = new CounterRequest(requestName, getName());
            final CounterRequest precedentRequest = requests.putIfAbsent(requestName, request);
            if (precedentRequest != null) {
                request = precedentRequest;
            }
        }
        return request;
    }

    /**
     * Retourne le nombre de requêtes dans ce counter.
     *
     * @return int
     */
    public int getRequestsCount() {
        return requests.size();
    }

    /**
     * @return Liste des requêtes non triées, la liste et ses objets peuvent
     * être utilisés sans synchronized et sans crainte d'accès
     * concurrents.
     */
    public List<CounterRequest> getRequests() {
        final List<CounterRequest> result = new ArrayList<>(requests.size());
        for (final CounterRequest request : requests.values()) {
            result.add(request.clone());
        }
        return result;
    }

    /**
     * @return Liste des requêtes triées par durée cumulée décroissante, la
     * liste et ses objets peuvent être utilisés sans synchronized et
     * sans crainte d'accès concurrents.
     */
    public List<CounterRequest> getOrderedRequests() {
        final List<CounterRequest> requestList = getRequests();
        if (requestList.size() > 1) {
            requestList.sort(Collections.reverseOrder(new CounterRequestComparator()));
        }
        return requestList;
    }

    /**
     * @return Liste des requêtes triées par hits décroissants, la liste et ses
     * objets peuvent être utilisés sans synchronized et sans crainte
     * d'accès concurrents.
     */
    public List<CounterRequest> getOrderedByHitsRequests() {
        final List<CounterRequest> requestList = getRequests();
        if (requestList.size() > 1) {
            Collections.sort(requestList, Collections.reverseOrder(new CounterRequestByHitsComparator()));
        }
        return requestList;
    }

    /**
     * @return Liste des contextes de requêtes courantes triées par durée
     * écoulée décroissante, la liste peut être utilisée sans
     * synchronized et sans crainte d'accès concurrents, toutefois les
     * contextes ne sont pas actuellement clonés dans cette méthode.
     */
    public List<CounterRequestContext> getOrderedRootCurrentContexts() {
        final List<CounterRequestContext> contextList = new ArrayList<>(
                rootCurrentContextsByThreadId.size());
        for (final CounterRequestContext rootCurrentContext : rootCurrentContextsByThreadId.values()) {
            contextList.add(rootCurrentContext.clone());
        }
        if (contextList.size() > 1) {
            Collections.sort(contextList,
                    Collections.reverseOrder(new CounterRequestContextComparator(System.currentTimeMillis())));
        }
        return contextList;
    }

    /**
     * @return Liste des erreurs triée par date croissante, la liste et ses
     * objets peuvent être utilisés sans synchronized et sans crainte
     * d'accès concurrents.
     */
    public List<CounterError> getErrors() {
        if (errors == null) {
            return Collections.emptyList();
        }
        synchronized (errors) {
            return new ArrayList<>(errors);
        }
    }

    /**
     * Retourne le nombre d'erreurs dans ce counter.
     *
     * @return int
     */
    public int getErrorsCount() {
        if (errors == null) {
            return 0;
        }
        synchronized (errors) {
            return errors.size();
        }
    }

    /**
     * Purge les requêtes, requêtes en cours et erreurs puis positionne la date
     * et heure de début à l'heure courante.
     */
    public void clear() {
        for (final Long threadId : rootCurrentContextsByThreadId.keySet()) {
            closeCounterRequestContext(rootCurrentContextsByThreadId.get(threadId));
        }
        closeCounterRequestContext(contextThreadLocal.get());
        rootCurrentContextsByThreadId.clear();
        contextThreadLocal.remove();
        requests.clear();
        if (errors != null) {
            synchronized (errors) {
                errors.clear();
            }
        }
        startDate = new Date();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    // CHECKSTYLE:OFF
    public Counter clone() { // NOPMD
        // CHECKSTYLE:ON
        final Counter clone = new Counter(getName(), getStorageName(), getIconName(), getChildCounterName(), new ThreadLocal<>());
        clone.application = getApplication();
        clone.startDate = getStartDate();
        clone.maxRequestsCount = getMaxRequestsCount();
        clone.displayed = isDisplayed();
        clone.requestTransformPattern = getRequestTransformPattern();
        // on ne copie pas rootCurrentContextsByThreadId car on ne fournit pas
        // les requêtes en cours
        // qui sont très rapidement obsolètes au serveur de collecte (et sinon
        // cela poserait la question
        // des clones de parentCounter, de l'agrégation, de la synchro d'horloge
        // pour la durée
        // et des threadId pour la stack-trace),
        // et on ne copie pas contextThreadLocal,
        // et la méthode getRequests() clone les instances de CounterRequest
        for (final CounterRequest request : getRequests()) {
            clone.requests.put(request.getName(), request);
        }
        if (errors != null) {
            clone.errors.addAll(getErrors());
        }
        return clone;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return getClass()
                .getSimpleName() + "[application=" + getApplication() + ", name=" + getName() + ", storageName=" + getStorageName() + ", startDate=" + getStartDate() + ", childCounterName="
                + getChildCounterName() + ", " + requests.size() + " requests, " + (errors == null ? "" : errors
                .size() + " errors, ") + "maxRequestsCount=" + getMaxRequestsCount() + ", displayed="
                + isDisplayed() + ']';
    }
}
