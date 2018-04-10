package org.mobicents.servlet.sip.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.Logger;
import static org.mobicents.servlet.sip.core.SipNetworkInterfaceManagerImpl.LISTENING_POINT_ATT;

/**
 * This FSM helps coordinate server bootstrapping, so adding applications, and
 * adding connectors parallel flows are in line. These flows need to be
 * coordinated in order to notify different Listener specs events at proper
 * time.
 *
 * Since the FSM doesn't know how many connectors will be added in advance, an
 * intermediary state approach is taken when adding the first connector. After
 * some time without receiving new connectors, the applications will be notified
 * about listener events
 *
 * @author jaime
 */
public class DispatcherFSM {

    private static final Logger logger = Logger.getLogger(SipApplicationDispatcherImpl.class);

    private static final String TIMER_ID_EVENT_DATA = "TimerId";
    private static final String ADD_CONN_TIMER_ID = "addConnTimerId";
    private static final long ADD_CONN_TIMER_DELAY = 5000;

    private final Context context;

    private final Map<DispatcherState, State> states = new HashMap();

    class Context {

        Event lastEvent;
        final SipApplicationDispatcherImpl dispatcher;
        final Map<String, Object> data = new HashMap();

        public Context(SipApplicationDispatcherImpl dispatcher) {
            this.dispatcher = dispatcher;
        }

    }

    enum DispatcherState {
        NOT_INIT, INITIATED, STARTED, IN_SERVICE, STOPPED;
    }

    enum EventType {
        INIT, START, STOP, ADD_CONNECTOR, REMOVE_CONNECTOR, ADD_APP, REMOVE_APP,
        SET_CONGESTION, TIMER,IN_SERVICE;

    }

    class Event {

        final EventType type;
        final Map<String, Object> data = new HashMap();

        public Event(EventType type) {
            this.type = type;
        }

    }

    class SetCongestionCondition implements GuardCondition {

        @Override
        public boolean isEnabled(Event eventData, Context ctx) {
            Long interval = (Long) eventData.data.get(SipApplicationDispatcherImpl.INTERVAL_ATT);
            return interval != ctx.dispatcher.congestionControlCheckingInterval;
        }
    }

    class IsTypeCondition implements GuardCondition {

        private final EventType expectedType;

        public IsTypeCondition(EventType type) {
            this.expectedType = type;
        }

        @Override
        public boolean isEnabled(Event ev, Context ctx) {
            return ev.type.equals(expectedType);
        }
    }

    class IsTimerCondition implements GuardCondition {

        private final String timerId;

        public IsTimerCondition(String timerId) {
            this.timerId = timerId;
        }

        @Override
        public boolean isEnabled(Event ev, Context ctx) {
            return ev.type.equals(EventType.TIMER) && ev.data.get(TIMER_ID_EVENT_DATA).equals(timerId);
        }
    }

    interface Action {

        void execute(Context ctx);
    }

    class NotififyServletInitialized implements Action {

        @Override
        public void execute(Context ctx) {
            Collection<SipContext> appCtxs = ctx.dispatcher.applicationDeployed.values();
            for (SipContext sipContext : appCtxs) {
                if (!ctx.data.containsKey(sipContext.getApplicationName())) {
                    sipContext.notifySipContextListeners(new SipContextEventImpl(SipContextEventType.SERVLET_INITIALIZED, null));
                    ctx.data.put(sipContext.getApplicationName(), "NOTIFIED");
                } else {
                    logger.debug("Application already notified");
                }
            }
        }
    }

    class NotififyConnectorAdded implements Action {

        @Override
        public void execute(Context ctx) {
            MobicentsExtendedListeningPoint extendedListeningPoint = (MobicentsExtendedListeningPoint) ctx.lastEvent.data.get(LISTENING_POINT_ATT);
            Iterator<SipContext> sipContextIterator = ctx.dispatcher.findSipApplications();
            while (sipContextIterator.hasNext()) {
                SipContext sipContext = (SipContext) sipContextIterator.next();
                sipContext.notifySipContextListeners(new SipContextEventImpl(SipContextEventType.SIP_CONNECTOR_ADDED, extendedListeningPoint.getSipConnector()));
            }
        }
    }

    class NotififyConnectorRemoved implements Action {

        @Override
        public void execute(Context ctx) {
            MobicentsExtendedListeningPoint extendedListeningPoint = (MobicentsExtendedListeningPoint) ctx.lastEvent.data.get(LISTENING_POINT_ATT);
            Iterator<SipContext> sipContextIterator = ctx.dispatcher.findSipApplications();
            while (sipContextIterator.hasNext()) {
                SipContext sipContext = (SipContext) sipContextIterator.next();
                sipContext.notifySipContextListeners(new SipContextEventImpl(SipContextEventType.SIP_CONNECTOR_REMOVED, extendedListeningPoint.getSipConnector()));
            }
        }
    }

    class ScheduleTimer implements Action, Runnable {

        private final long delay;
        private final String timerId;

        public ScheduleTimer(long delay, String timerId) {
            this.delay = delay;
            this.timerId = timerId;
        }

        @Override
        public void execute(Context ctx) {
            ScheduledFuture future = ctx.dispatcher.getAsynchronousScheduledExecutor().schedule(this, delay, TimeUnit.MILLISECONDS);
            ctx.data.put(timerId, future);
        }

        @Override
        public void run() {
            Event event = new Event(EventType.TIMER);
            event.data.put(TIMER_ID_EVENT_DATA, timerId);
            fireEvent(event);
        }
    }

    class RemoveCtxDataByEvent implements Action {
        String dataKey;

        public RemoveCtxDataByEvent(String dataKey) {
            this.dataKey = dataKey;
        }

        @Override
        public void execute(Context ctx) {
            String ctxDataKey = (String) ctx.lastEvent.data.get(dataKey);
            ctx.data.remove(ctxDataKey);
        }

    }

    class CancelTimer implements Action {

        private final String timerId;

        public CancelTimer(String timerId) {
            this.timerId = timerId;
        }

        @Override
        public void execute(Context ctx) {
            Object obj = ctx.data.get(timerId);
            if (obj != null) {
                ScheduledFuture future = (ScheduledFuture) obj;
                future.cancel(false);
            }
        }
    }

    interface GuardCondition {

        boolean isEnabled(Event event, Context ctx);
    }

    class Transition {

        GuardCondition condition;
        List<Action> actions = new ArrayList();
        DispatcherState targetState;
    }

    class State {

        List<Transition> transitions = new ArrayList();
        List<Action> entryActions = new ArrayList();
        List<Action> exitActions = new ArrayList();
    }

    private DispatcherState currentState = DispatcherState.NOT_INIT;

    public DispatcherFSM(SipApplicationDispatcherImpl dispatcher) {
        context = new Context(dispatcher);

        /* Non Initiated state */
        State notInitState = new State();

        Transition addAppOnNonInit = new Transition();
        addAppOnNonInit.condition = new IsTypeCondition(EventType.ADD_APP);
        addAppOnNonInit.targetState = DispatcherState.NOT_INIT;
        addAppOnNonInit.actions.add(dispatcher.new AddAppAction());
        notInitState.transitions.add(addAppOnNonInit);

        Transition initTransition = new Transition();
        initTransition.condition = new IsTypeCondition(EventType.INIT);
        initTransition.targetState = DispatcherState.INITIATED;
        initTransition.actions.add(dispatcher.new InitAction());
        notInitState.transitions.add(initTransition);
        states.put(DispatcherState.NOT_INIT, notInitState);
        /* Non Initiated state */


        /* Initiated state */
        State initiatedState = new State();

        Transition addConnectorOnInitiated = new Transition();
        addConnectorOnInitiated.condition = new IsTypeCondition(EventType.ADD_CONNECTOR);
        addConnectorOnInitiated.targetState = DispatcherState.INITIATED;
        addConnectorOnInitiated.actions.add(dispatcher.sipNetworkInterfaceManager.new AddPointAction());
        addConnectorOnInitiated.actions.add(new NotififyConnectorAdded());
        initiatedState.transitions.add(addConnectorOnInitiated);

        Transition startTransition = new Transition();
        startTransition.condition = new IsTypeCondition(EventType.START);
        startTransition.targetState = DispatcherState.STARTED;
        startTransition.actions.add(dispatcher.new StartAction());
        initiatedState.transitions.add(startTransition);

        Transition addAppOnInit = new Transition();
        addAppOnInit.condition = new IsTypeCondition(EventType.ADD_APP);
        addAppOnInit.targetState = DispatcherState.INITIATED;
        addAppOnInit.actions.add(dispatcher.new AddAppAction());
        initiatedState.transitions.add(addAppOnInit);

        states.put(DispatcherState.INITIATED, initiatedState);
        /* Initiated state */


 /* Started state */
        State startedState = new State();

        Transition addConnectorTransition = new Transition();
        addConnectorTransition.condition = new IsTypeCondition(EventType.ADD_CONNECTOR);
        addConnectorTransition.targetState = DispatcherState.STARTED;
        addConnectorTransition.actions.add(dispatcher.sipNetworkInterfaceManager.new AddPointAction());
        addConnectorTransition.actions.add(new NotififyConnectorAdded());
        startedState.transitions.add(addConnectorTransition);

        Transition inserviceTransition = new Transition();
        inserviceTransition.condition = new IsTypeCondition(EventType.IN_SERVICE);
        inserviceTransition.targetState = DispatcherState.IN_SERVICE;
        inserviceTransition.actions.add(new NotififyServletInitialized());
        startedState.transitions.add(inserviceTransition);

        Transition stopTransition = new Transition();
        stopTransition.condition = new IsTypeCondition(EventType.STOP);
        stopTransition.targetState = DispatcherState.STOPPED;
        stopTransition.actions.add(dispatcher.new StopAction());
        startedState.transitions.add(stopTransition);

        Transition addApp = new Transition();
        addApp.condition = new IsTypeCondition(EventType.ADD_APP);
        addApp.targetState = DispatcherState.STARTED;
        addApp.actions.add(dispatcher.new AddAppAction());
        startedState.transitions.add(addApp);

        Transition setCongestion = new Transition();
        setCongestion.condition = new IsTypeCondition(EventType.SET_CONGESTION);
        setCongestion.targetState = DispatcherState.STARTED;
        setCongestion.actions.add(dispatcher.new SetCongAction());
        startedState.transitions.add(setCongestion);

        states.put(DispatcherState.STARTED, startedState);
        /* Started state */

        /* inserviceState state */
        State inserviceState = new State();

        Transition addConnectorOnInservice= new Transition();
        addConnectorOnInservice.condition = new IsTypeCondition(EventType.ADD_CONNECTOR);
        addConnectorOnInservice.targetState = DispatcherState.IN_SERVICE;
        addConnectorOnInservice.actions.add(dispatcher.sipNetworkInterfaceManager.new AddPointAction());
        addConnectorOnInservice.actions.add(new NotififyConnectorAdded());
        inserviceState.transitions.add(addConnectorOnInservice);

        Transition rmConnectorTransition = new Transition();
        rmConnectorTransition.condition = new IsTypeCondition(EventType.REMOVE_CONNECTOR);
        rmConnectorTransition.targetState = DispatcherState.IN_SERVICE;
        rmConnectorTransition.actions.add(dispatcher.sipNetworkInterfaceManager.new RemovePointAction());
        rmConnectorTransition.actions.add(new NotififyConnectorRemoved());
        inserviceState.transitions.add(rmConnectorTransition);

        Transition addAppTransition = new Transition();
        addAppTransition.condition = new IsTypeCondition(EventType.ADD_APP);
        addAppTransition.targetState = DispatcherState.IN_SERVICE;
        addAppTransition.actions.add(dispatcher.new AddAppAction());
        addAppTransition.actions.add(new NotififyServletInitialized());
        inserviceState.transitions.add(addAppTransition);

        Transition removeAppTransition = new Transition();
        removeAppTransition.condition = new IsTypeCondition(EventType.REMOVE_APP);
        removeAppTransition.targetState = DispatcherState.IN_SERVICE;
        removeAppTransition.actions.add(dispatcher.new RemoveApp());
        removeAppTransition.actions.add(new RemoveCtxDataByEvent(SipApplicationDispatcherImpl.APP_NAME));
        inserviceState.transitions.add(removeAppTransition);

        inserviceState.transitions.add(stopTransition);

        Transition setCongestionOnConn = new Transition();
        setCongestionOnConn.condition = new IsTypeCondition(EventType.SET_CONGESTION);
        setCongestionOnConn.targetState = DispatcherState.IN_SERVICE;
        setCongestionOnConn.actions.add(dispatcher.new SetCongAction());
        inserviceState.transitions.add(setCongestionOnConn);

        states.put(DispatcherState.IN_SERVICE, inserviceState);
        /* inserviceState state */

        State stoppedState = new State();
        states.put(DispatcherState.STOPPED, stoppedState);

    }

    private void executeActions(List<Action> actions) {
        for (Action aAux : actions) {
            if (logger.isDebugEnabled()) {
                logger.debug("State(" + currentState + ").Executing action:" + aAux);
            }
            aAux.execute(context);
        }

    }

    private void switchToState(DispatcherState newState) {
        if (logger.isDebugEnabled()) {
            logger.debug("State(" + currentState + ").New state:" + newState);
        }
        executeActions(states.get(currentState).exitActions);
        currentState = newState;
        executeActions(states.get(currentState).entryActions);
    }

    synchronized void fireEvent(Event event) {
        State cState = states.get(currentState);
        boolean matched = false;
        Iterator<Transition> iterator = cState.transitions.iterator();
        Transition tAux = null;
        while (!matched && iterator.hasNext()) {
            tAux = iterator.next();
            if (tAux.condition.isEnabled(event, context)) {
                matched = true;
            }
        }
        if (matched && tAux != null) {
            context.lastEvent = event;
            executeActions(tAux.actions);
            switchToState(tAux.targetState);
        } else if (logger.isDebugEnabled()) {
            logger.debug("State(" + currentState + ").Event discarded:" + event.type);
        }
    }
}
