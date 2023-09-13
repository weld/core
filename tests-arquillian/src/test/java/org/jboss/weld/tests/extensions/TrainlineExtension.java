package org.jboss.weld.tests.extensions;

import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.ProcessBean;
import jakarta.enterprise.inject.spi.ProcessInjectionTarget;
import jakarta.enterprise.inject.spi.ProcessManagedBean;
import jakarta.enterprise.inject.spi.ProcessObserverMethod;
import jakarta.enterprise.inject.spi.ProcessProducer;
import jakarta.enterprise.inject.spi.ProcessProducerField;
import jakarta.enterprise.inject.spi.ProcessProducerMethod;
import jakarta.enterprise.inject.spi.ProcessSessionBean;

public class TrainlineExtension implements Extension {

    private boolean processTrainBean;
    private boolean processStationBean;
    private boolean processSignalBoxBean;
    private boolean processTrainManagedBean;
    private boolean processStationManagedBean;
    private boolean processSignalBoxManagedBean;
    private boolean processTrainInjectionTarget;
    private boolean processStationInjectionTarget;
    private boolean processSignalBoxInjectionTarget;
    private boolean processDriverBean;
    private boolean processPassengerBean;
    private boolean processSignalManBean;
    private boolean processDriverProducer;
    private boolean processPassengerProducer;
    private boolean processSignalManProducer;
    private boolean processDriverProducerMethod;
    private boolean processPassengerProducerMethod;
    private boolean processSignalManProducerMethod;
    private boolean processFerretBean;
    private boolean processCatBean;
    private boolean processMouseBean;
    private boolean processFerretProducer;
    private boolean processCatProducer;
    private boolean processMouseProducer;
    private boolean processFerretProducerField;
    private boolean processCatProducerField;
    private boolean processMouseProducerField;
    private boolean processObseversCoalSupply;
    private boolean processObseversSignals;
    private boolean processObseversFatController;
    private boolean processStokerBean;
    private boolean processGuardBean;
    private boolean processStokerProducer;
    private boolean processGuardProducer;
    private boolean processStokerProducerMethod;
    private boolean processGuardProducerMethod;
    private boolean processRabbitBean;
    private boolean processWeaselBean;
    private boolean processRabbitProducer;
    private boolean processWeaselProducer;
    private boolean processRabbitProducerField;
    private boolean processWeaselProducerField;
    private boolean processSafetyInterceptor;
    private boolean processEngineeringWorks;
    private boolean processTerminusSessionBean;
    private boolean processTerminusInjectionTarget;
    private boolean processTerminusBean;

    public void processSafetyInterceptor(@Observes ProcessBean<SafetyInterceptor> event) {
        processSafetyInterceptor = true;
    }

    public void processEngineeringWorks(@Observes ProcessBean<EngineeringWorks> event) {
        processEngineeringWorks = true;
    }

    public void processTrainBean(@Observes ProcessBean<Train> event) {
        processTrainBean = true;
    }

    public void processStationBean(@Observes ProcessBean<RuralStation> event) {
        processStationBean = true;
    }

    public void processSignalBoxBean(@Observes ProcessBean<SignalBox> event) {
        processSignalBoxBean = true;
    }

    public void processTrainInjectionTarget(@Observes ProcessInjectionTarget<Train> event) {
        processTrainInjectionTarget = true;
    }

    public void processStationInjectionTarget(@Observes ProcessInjectionTarget<RuralStation> event) {
        processStationInjectionTarget = true;
    }

    public void processSignalBoxInjectionTarget(@Observes ProcessInjectionTarget<SignalBox> event) {
        processSignalBoxInjectionTarget = true;
    }

    public void processTrainManagedBean(@Observes ProcessManagedBean<Train> event) {
        processTrainManagedBean = true;
    }

    public void processStationManagedBean(@Observes ProcessManagedBean<RuralStation> event) {
        processStationManagedBean = true;
    }

    public void processTerminusBean(@Observes ProcessBean<Terminus> event) {
        processTerminusBean = true;
    }

    public void processTerminusInjectionTarget(@Observes ProcessInjectionTarget<Terminus> event) {
        processTerminusInjectionTarget = true;
    }

    public void processTerminusSessionBean(@Observes ProcessSessionBean<Terminus> event) {
        processTerminusSessionBean = true;
    }

    public void processSignalBoxManagedBean(@Observes ProcessManagedBean<SignalBox> event) {
        processSignalBoxManagedBean = true;
    }

    // This is intentionally wrong, there are bugs in the API that mean generic type parameter ordering is wrong for ProcessProducerField and ProcessProducerMethod
    public void processDriverBean(@Observes ProcessBean<Train> event) {
        processDriverBean = true;
    }

    public void processPassengerBean(@Observes ProcessBean<Passenger> event) {
        processPassengerBean = true;
    }

    public void processSignalManBean(@Observes ProcessBean<SignalMan> event) {
        processSignalManBean = true;
    }

    public void processDriverProducer(@Observes ProcessProducer<Train, Driver> event) {
        processDriverProducer = true;
    }

    public void processPassengerProducer(@Observes ProcessProducer<RuralStation, Passenger> event) {
        processPassengerProducer = true;
    }

    public void processSignalManProducer(@Observes ProcessProducer<SignalBox, SignalMan> event) {
        processSignalManProducer = true;
    }

    public void processDriverProducerMethod(@Observes ProcessProducerMethod<Driver, Train> event) {
        processDriverProducerMethod = true;
    }

    public void processPassengerProducerMethod(@Observes ProcessProducerMethod<Passenger, RuralStation> event) {
        processPassengerProducerMethod = true;
    }

    public void processSignalManProducerMethod(@Observes ProcessProducerMethod<SignalMan, SignalBox> event) {
        processSignalManProducerMethod = true;
    }

    // This is intentionally wrong, there are bugs in the API that mean generic type parameter ordering is wrong for ProcessProducerField and ProcessProducerMethod
    public void processFerretBean(@Observes ProcessBean<Train> event) {
        processFerretBean = true;
    }

    public void processCatBean(@Observes ProcessBean<Cat> event) {
        processCatBean = true;
    }

    public void processMouseBean(@Observes ProcessBean<Mouse> event) {
        processMouseBean = true;
    }

    public void processFerretProducer(@Observes ProcessProducer<Train, Ferret> event) {
        processFerretProducer = true;
    }

    public void processCatProducer(@Observes ProcessProducer<RuralStation, Cat> event) {
        processCatProducer = true;
    }

    public void processMouseProducer(@Observes ProcessProducer<SignalBox, Mouse> event) {
        processMouseProducer = true;
    }

    public void processFerretProducerField(@Observes ProcessProducerField<Ferret, Train> event) {
        processFerretProducerField = true;
    }

    public void processCatProducerField(@Observes ProcessProducerField<Cat, RuralStation> event) {
        processCatProducerField = true;
    }

    public void processStokerBean(@Observes ProcessBean<Stoker> event) {
        processStokerBean = true;
    }

    public void processGuardBean(@Observes ProcessBean<Guard> event) {
        processGuardBean = true;
    }

    public void processStokerProducer(@Observes ProcessProducer<Train, Stoker> event) {
        processStokerProducer = true;
    }

    public void processGuardProducer(@Observes ProcessProducer<RuralStation, Guard> event) {
        processGuardProducer = true;
    }

    public void processStokerProducerMethod(@Observes ProcessProducerMethod<Train, Stoker> event) {
        processStokerProducerMethod = true;
    }

    public void processGuardProducerMethod(@Observes ProcessProducerMethod<RuralStation, Guard> event) {
        processGuardProducerMethod = true;
    }

    public void processRabbitBean(@Observes ProcessBean<Rabbit> event) {
        processRabbitBean = true;
    }

    public void processWeaselBean(@Observes ProcessBean<Weasel> event) {
        processWeaselBean = true;
    }

    public void processRabbitProducer(@Observes ProcessProducer<Train, Rabbit> event) {
        processRabbitProducer = true;
    }

    public void processWeaselProducer(@Observes ProcessProducer<RuralStation, Weasel> event) {
        processWeaselProducer = true;
    }

    public void processRabbitProducerField(@Observes ProcessProducerField<Train, Rabbit> event) {
        processRabbitProducerField = true;
    }

    public void processWeaselProducerField(@Observes ProcessProducerField<RuralStation, Weasel> event) {
        processWeaselProducerField = true;
    }

    public void processMouseProducerField(@Observes ProcessProducerField<SignalBox, Mouse> event) {
        processMouseProducerField = true;
    }

    // This is intentionally wrong, there are bugs in the API that mean generic type parameter ordering is wrong for ProcessProducerField and ProcessProducerMethod
    public void processObservesCoalSupply(@Observes ProcessObserverMethod<CoalSupply, Train> event) {
        processObseversCoalSupply = true;
    }

    public void processObservesSignals(@Observes ProcessObserverMethod<SignalBox, Signals> event) {
        processObseversSignals = true;
    }

    public void processObservesFatController(@Observes ProcessObserverMethod<RuralStation, FatController> event) {
        processObseversFatController = true;
    }

    public boolean isProcessSignalBoxBean() {
        return processSignalBoxBean;
    }

    public boolean isProcessStationBean() {
        return processStationBean;
    }

    public boolean isProcessTrainBean() {
        return processTrainBean;
    }

    public boolean isProcessSignalBoxInjectionTarget() {
        return processSignalBoxInjectionTarget;
    }

    public boolean isProcessSignalBoxManagedBean() {
        return processSignalBoxManagedBean;
    }

    public boolean isProcessStationInjectionTarget() {
        return processStationInjectionTarget;
    }

    public boolean isProcessStationManagedBean() {
        return processStationManagedBean;
    }

    public boolean isProcessTrainInjectionTarget() {
        return processTrainInjectionTarget;
    }

    public boolean isProcessTrainManagedBean() {
        return processTrainManagedBean;
    }

    public boolean isProcessDriverBean() {
        return processDriverBean;
    }

    public boolean isProcessPassengerBean() {
        return processPassengerBean;
    }

    public boolean isProcessSignalManBean() {
        return processSignalManBean;
    }

    public boolean isProcessDriverProducer() {
        return processDriverProducer;
    }

    public boolean isProcessPassengerProducer() {
        return processPassengerProducer;
    }

    public boolean isProcessSignalManProducer() {
        return processSignalManProducer;
    }

    public boolean isProcessDriverProducerMethod() {
        return processDriverProducerMethod;
    }

    public boolean isProcessPassengerProducerMethod() {
        return processPassengerProducerMethod;
    }

    public boolean isProcessSignalManProducerMethod() {
        return processSignalManProducerMethod;
    }

    public boolean isProcessFerretBean() {
        return processFerretBean;
    }

    public boolean isProcessCatBean() {
        return processCatBean;
    }

    public boolean isProcessMouseBean() {
        return processMouseBean;
    }

    public boolean isProcessFerretProducer() {
        return processFerretProducer;
    }

    public boolean isProcessCatProducer() {
        return processCatProducer;
    }

    public boolean isProcessMouseProducer() {
        return processMouseProducer;
    }

    public boolean isProcessCatProducerField() {
        return processCatProducerField;
    }

    public boolean isProcessFerretProducerField() {
        return processFerretProducerField;
    }

    public boolean isProcessMouseProducerField() {
        return processMouseProducerField;
    }

    public boolean isProcessObseversCoalSupply() {
        return processObseversCoalSupply;
    }

    public boolean isProcessObseversSignals() {
        return processObseversSignals;
    }

    public boolean isProcessObseversFatController() {
        return processObseversFatController;
    }

    public boolean isProcessStokerBean() {
        return processStokerBean;
    }

    public boolean isProcessGuardBean() {
        return processGuardBean;
    }

    public boolean isProcessStokerProducer() {
        return processStokerProducer;
    }

    public boolean isProcessGuardProducer() {
        return processGuardProducer;
    }

    public boolean isProcessStokerProducerMethod() {
        return processStokerProducerMethod;
    }

    public boolean isProcessGuardProducerMethod() {
        return processGuardProducerMethod;
    }

    public boolean isProcessRabbitBean() {
        return processRabbitBean;
    }

    public boolean isProcessWeaselBean() {
        return processWeaselBean;
    }

    public boolean isProcessRabbitProducer() {
        return processRabbitProducer;
    }

    public boolean isProcessWeaselProducer() {
        return processWeaselProducer;
    }

    public boolean isProcessRabbitProducerField() {
        return processRabbitProducerField;
    }

    public boolean isProcessWeaselProducerField() {
        return processWeaselProducerField;
    }

    public boolean isProcessSafetyInterceptor() {
        return processSafetyInterceptor;
    }

    public boolean isProcessEngineeringWorks() {
        return processEngineeringWorks;
    }

    public boolean isProcessTerminusSessionBean() {
        return processTerminusSessionBean;
    }

    public boolean isProcessTerminusInjectionTarget() {
        return processTerminusInjectionTarget;
    }

    public boolean isProcessTerminusBean() {
        return processTerminusBean;
    }

}
