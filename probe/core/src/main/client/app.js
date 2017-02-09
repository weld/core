/*!
 * Weld Probe ${project.version}
 * Copyright 2015, Red Hat, Inc.
 * Licensed the Apache License, Version 2.0 (http://www.apache.org/licenses/LICENSE-2.0)
 */

var Probe = Ember.Application.create({});

Probe.ResetScroll = Ember.Mixin.create({
    activate : function() {
        this._super();
        window.scrollTo(0, 0);
    },
    afterModel : function() {
        window.scrollTo(0, 0);
    }
});

Probe.Router.map(function() {
    this.route('dashboard', {
        path : '/'
    });
    this.route('beanArchives', {
        path : '/beanArchives'
    });
    this.route('beanArchive', {
        path : '/bda/:id'
    });
    this.route('configuration', {
        path : '/config'
    });
    this.resource('beanList', {
        path : '/beans'
    });
    this.resource('beanDetail', {
        path : '/bean/:id'
    });
    this.route('observerList', {
        path : '/observers'
    });
    this.route('observerDetail', {
        path : '/observers/:id'
    });
    this.route('context', {
        path : '/context/:id'
    });
    this.resource("contextInstance", {
        path : "/contexts/:id"
    });
    this.route('invocationList', {
        path : '/invocations'
    });
    this.route('invocationDetail', {
        path : '/invocation/:id'
    });
    this.route('events', {
        path : '/events'
    });
    this.route('overview', {
        path : '/overview'
    });
    this.resource('availableBeans', {
        path : '/availableBeans'
    });
});

// VIEWS

Probe.ApplicationView = Ember.View.extend({
    currentPathDidChange : function() {
        // Workaround to highlight the active tab
        Ember.run.next(this, function() {
            this.$("ul.nav > li:has(a.active)").addClass('active');
            this.$("ul.nav > li:not(:has(a.active))").removeClass('active');
        });
    }.observes('controller.currentPath')
});

// ROUTES

/*
 * NOTE: this route is always initialized!
 */
Probe.ApplicationRoute = Ember.Route
    .extend({
        model : function() {
            var controller = this.controllerFor('application');
            return $
                .getJSON(controller.get('restUrlBase') + 'deployment')
                .done(
                    function(data) {
                        // All bean archives - add index and color
                        var colors = generateColors(data.bdas.length);
                        data.bdas.forEach(function(bda, index, array) {
                            bda.idx = index + 1;
                            bda.color = colors[index];
                        });
                        controller.set('bdas', data.bdas);
                        // Copy bdas for filtering purpose
                        var filterBdas = data.bdas.slice(0);
                        // Add marker to filter additional archives
                        filterBdas
                            .unshift({
                                "id" : controller.get('markerFilterAddBdas'),
                                "bdaId" : "Only application bean archives - leave out additional bean archives"
                            });
                        controller.set('filterBdas', filterBdas);
                        // Weld configuration - changed flag indicates a
                        // modified value
                        var configuration = data.configuration;
                        configuration.forEach(function(item, index, array) {
                            if (item.value != item.defaultValue) {
                                item.changed = true;
                            }
                        });
                        controller.set('configuration', configuration);
                        var qualifierStart = data.version.indexOf('(');
                        data.versionShort = qualifierStart != -1 ? data.version
                            .substring(0, qualifierStart) : data.version;
                        controller.set('dashboard', data.dashboard);
                        return data;
                    }).fail(function(jqXHR, textStatus, errorThrown) {
                    alert('Unable to get JSON data: ' + textStatus);
                });
        }
    });

Probe.DashboardRoute = Ember.Route.extend({
    model : function() {
        var appController = this.controllerFor('application');
        return $.getJSON(appController.get('restUrlBase') + 'monitoring').done(
            function(data) {
                var devmodeProperties = new Array();
                appController.get('configuration').forEach(
                    function(property, index, array) {
                        if (property.name.indexOf('probe') != -1) {
                            var devproperty = new Object();
                            devproperty.name = property.name;
                            devproperty.value = property.value;
                            devproperty.description = property.description;
                            devmodeProperties.push(devproperty);
                        }
                    });
                data.devmodeProperties = devmodeProperties;
                var appBdas = 0;
                appController.get('bdas').forEach(
                    function(bda) {
                        if (!isAdditionalBda(appController
                            .get('additionalBdaSuffix'), bda.bdaId)) {
                            appBdas++;
                        }
                    });
                data.appBdas = appBdas;
                return data;
            }).fail(function(jqXHR, textStatus, errorThrown) {
            alert('Unable to get JSON data: ' + textStatus);
        });
    },
    actions : {
        refreshData : function() {
            this.refresh();
        },
        clearData : function() {
            var route = this;
            var promises = new Array();
            promises.push($.ajax(route.controllerFor('application').get(
                'restUrlBase')
                + 'invocations', {
                'type' : 'DELETE'
            }));
            promises.push($.ajax(route.controllerFor('application').get(
                'restUrlBase')
                + 'events', {
                'type' : 'DELETE'
            }));
            return Ember.RSVP.all(promises).then(function(values) {
                route.refresh();
            });
        }
    }
});

Probe.BeanArchivesRoute = Ember.Route.extend({
    model : function() {
        return this.controllerFor('application').get('bdas');
    },
    actions : {
        overview : function() {
            this.transitionTo('overview', {
                queryParams : {
                    bda : null
                }
            });
        }
    }
});

Probe.BeanArchiveRoute = Ember.Route.extend({
    model : function(params) {
        // Use cached data
        return findBeanDeploymentArchive(this.controllerFor('application').get(
            'bdas'), params.id);
    }
});

Probe.ConfigurationRoute = Ember.Route.extend({
    model : function() {
        // Use cached data
        return this.controllerFor('application').get('configuration');
    }
});

Probe.BeanListRoute = Ember.Route.extend(Probe.ResetScroll, {
    queryParams : {
        page : {
            refreshModel : true
        },
        kind : {
            refreshModel : true
        },
        bda : {
            refreshModel : true
        },
        unused : {
            refreshModel : true
        }
    },
    setupController : function(controller, model) {
        this._super(controller, model);
        controller.set("pages", buildPages(model.page, model.lastPage));
    },
    model : function(params) {
        var appController = this.controllerFor('application');
        var query = '', filters = '';
        filters = appendToFilters(filters, 'scope', params.scope);
        filters = appendToFilters(filters, 'beanClass', params.beanClass);
        filters = appendToFilters(filters, 'beanType', params.beanType);
        filters = appendToFilters(filters, 'qualifier', params.qualifier);
        if (params.bda) {
            appController.get('filterBdas').forEach(function(bda) {
                if (bda.id == params.bda) {
                    filters = appendToFilters(filters, 'bda', bda.id);
                }
            });
        }
        if (params.kind) {
            appController.get('beanKinds').forEach(function(kind) {
                if (kind == params.kind) {
                    filters = appendToFilters(filters, 'kind', kind);
                }
            });
        }
        if (params.unused) {
            filters = appendToFilters(filters, 'unused', true);
        }
        query = appendToQuery(query, 'filters', filters);
        if (params.page) {
            query = appendToQuery(query, 'page', params.page);
        }
        return $.getJSON(appController.get('restUrlBase') + 'beans' + query)
            .done(
                function(data) {
                    data.data.forEach(function(bean) {
                        // We want to render the bda index
                        bean.bda = findBeanDeploymentArchive(appController
                            .get('bdas'), bean.bdaId);
                    });
                    return data;
                }).fail(function(jqXHR, textStatus, errorThrown) {
                alert('Unable to get JSON data: ' + textStatus);
            });
    },
    actions : {
        refreshData : function() {
            this.refresh();
        }
    }
});

Probe.BeanDetailRoute = Ember.Route.extend(Probe.ResetScroll, {
    model : function(params) {
        var appController = this.controllerFor('application');
        return $.getJSON(
            appController.get('restUrlBase') + 'beans/' + params.id
                + '?transientDependencies=true&transientDependents=true')
            .done(
                function(data) {
                    data.bda = findBeanDeploymentArchive(appController
                        .get('bdas'), data['bdaId']);
                    data.showDependencyGraph = data.dependencies
                        || data.dependents;
                    return data;
                }).fail(function(jqXHR, textStatus, errorThrown) {
                alert('Unable to get JSON data: ' + textStatus);
            });
    },
});

Probe.ObserverListRoute = Ember.Route.extend(Probe.ResetScroll,
    {
        queryParams : {
            page : {
                refreshModel : true
            },
            reception : {
                refreshModel : true
            },
            txPhase : {
                refreshModel : true
            },
            kind : {
                refreshModel : true
            },
            bda : {
                refreshModel : true
            }
        },
        setupController : function(controller, model) {
            this._super(controller, model);
            controller.set("pages", buildPages(model.page, model.lastPage));
        },
        model : function(params) {
            var appController = this.controllerFor('application');
            var query = '', filters = '';
            filters = appendToFilters(filters, 'beanClass', params.beanClass);
            filters = appendToFilters(filters, 'observedType',
                params.observedType);
            filters = appendToFilters(filters, 'qualifier', params.qualifier);
            filters = appendToFilters(filters, 'declaringBean',
                params.declaringBean);
            if (params.reception) {
                appController.get('receptions').forEach(function(reception) {
                    if (reception == params.reception) {
                        filters = appendToFilters(filters, 'reception',
                            reception);
                    }
                });
            }
            if (params.kind) {
                appController.get('beanKinds').forEach(function(kind) {
                    if (kind == params.kind) {
                        filters = appendToFilters(filters, 'kind', kind);
                    }
                });
            }
            if (params.txPhase) {
                appController.get('txPhases').forEach(function(txPhase) {
                    if (txPhase == params.txPhase) {
                        filters = appendToFilters(filters, 'txPhase', txPhase);
                    }
                });
            }
            if (params.bda) {
                appController.get('filterBdas').forEach(function(bda) {
                    if (bda.id == params.bda) {
                        filters = appendToFilters(filters, 'bda', bda.id);
                    }
                });
            }
            query = appendToQuery(query, 'filters', filters);
            if (params.page) {
                query = appendToQuery(query, 'page', params.page);
            }
            return $.getJSON(
                appController.get('restUrlBase') + 'observers' + query).done(
                function(data) {
                    data.data.forEach(function(observer) {
                        // We want to render the bda index
                        if (observer.declaringBean) {
                            observer.bda = findBeanDeploymentArchive(
                                appController.get('bdas'),
                                observer.declaringBean.bdaId);
                        }
                    });
                    return data;
                }).fail(function(jqXHR, textStatus, errorThrown) {
                alert('Unable to get JSON data: ' + textStatus);
            });
        },
        actions : {
            refreshData : function() {
                this.refresh();
            }
        }
    });

Probe.ObserverDetailRoute = Ember.Route.extend(Probe.ResetScroll, {
    model : function(params) {
        return $.getJSON(
            this.controllerFor('application').get('restUrlBase') + 'observers/'
                + params.id).done(function(data) {
            return data;
        }).fail(function(jqXHR, textStatus, errorThrown) {
            alert('Unable to get JSON data: ' + textStatus);
        });
    }
});

Probe.ContextRoute = Ember.Route.extend(Probe.ResetScroll, {
    model : function(params) {
        var appController = this.controllerFor('application');
        var url = appController.get('restUrlBase') + 'contexts/' + params.id;
        if (this.get('cid')) {
            url += '?cid=' + this.get('cid');
        }
        return $.getJSON(url).done(
            function(data) {
                if (data.instances) {
                    data.instances.forEach(function(bean) {
                        // We want to render the bda index
                        bean.bda = findBeanDeploymentArchive(appController
                            .get('bdas'), bean.bdaId);
                    });
                }
                return data;
            }).fail(function(jqXHR, textStatus, errorThrown) {
            alert('Unable to get JSON data: ' + textStatus);
        });
    },
    actions : {
        refreshData : function(paramName) {
            this.set('cid', this.get('controller.cid'));
            this.refresh();
        }
    }
});

Probe.ContextInstanceRoute = Ember.Route.extend(Probe.ResetScroll, {
    setupController : function(controller, model) {
        this._super(controller, model);
        // A bean kind css class binding
        controller.set("kindClass", model.kind + ' boxed');
    },
    model : function(params) {
        var url = this.controllerFor('application').get('restUrlBase')
            + 'beans/' + params.id + '/instance';
        if (params.cid) {
            url = url + '?cid=' + params.cid;
        }
        return $.getJSON(url).done(function(data) {
            return data;
        }).fail(function(jqXHR, textStatus, errorThrown) {
            alert('Unable to get JSON data: ' + textStatus);
        });
    },
    actions : {
        refreshData : function() {
            this.refresh();
        }
    }
});

Probe.InvocationListRoute = Ember.Route.extend(Probe.ResetScroll, {
    queryParams : {
        page : {
            refreshModel : true
        }
    },
    setupController : function(controller, model) {
        this._super(controller, model);
        controller.set("pages", buildPages(model.page, model.lastPage));
    },
    model : function(params) {
        var query = '', filters = '';
        var appController = this.controllerFor('application');
        filters = appendToFilters(filters, 'beanClass', params.beanClass);
        filters = appendToFilters(filters, 'methodName', params.methodName);
        filters = appendToFilters(filters, 'search', params.search);
        filters = appendToFilters(filters, 'description', params.description);
        query = appendToQuery(query, 'filters', filters);
        if (params.page) {
            query = appendToQuery(query, 'page', params.page);
        }
        return $.getJSON(
            appController.get('restUrlBase') + 'invocations' + query).done(
            function(data) {
                data.data.forEach(function(invocation) {
                    invocation['time'] = (invocation['time'] / 1000000)
                        .toFixed(3);
                    invocation['start'] = moment(invocation['start']).format(
                        'YYYY-MM-DD HH:mm:ss.SSS');
                });
                return data;
            }).fail(function(jqXHR, textStatus, errorThrown) {
            alert('Unable to get JSON data: ' + textStatus);
        });
    },
    actions : {
        refreshData : function() {
            this.refresh();
        },
        clearInvocations : function() {
            var route = this;
            $.ajax(
                route.controllerFor('application').get('restUrlBase')
                    + 'invocations', {
                    'type' : 'DELETE'
                }).then(function(data) {
                route.refresh();
            });
        },
    }
});

Probe.InvocationDetailRoute = Ember.Route.extend(Probe.ResetScroll, {
    model : function(params) {
        return $.getJSON(
            this.controllerFor('application').get('restUrlBase')
                + 'invocations/' + params.id).done(function(data) {
            data.transformed = getRootNode(data, null);
        }).fail(function(jqXHR, textStatus, errorThrown) {
            alert('Unable to get JSON data: ' + textStatus);
        });
    }
});

Probe.EventsRoute = Ember.Route.extend(Probe.ResetScroll, {
    queryParams : {
        page : {
            refreshModel : true
        },
        kind : {
            refreshModel : true
        }
    },
    setupController : function(controller, model) {
        this._super(controller, model);
        controller.set("pages", buildPages(model.page, model.lastPage));
    },
    model : function(params) {
        var query = '', filters = '';
        filters = appendToFilters(filters, 'eventInfo', params.eventInfo);
        filters = appendToFilters(filters, 'type', params.type);
        filters = appendToFilters(filters, 'qualifiers', params.qualifiers);
        if (params.kind) {
            filters = appendToFilters(filters, 'kind', params.kind);
        }
        query = appendToQuery(query, 'filters', filters);
        if (params.page) {
            query = appendToQuery(query, 'page', params.page);
        }
        return $.getJSON(
            this.controllerFor('application').get('restUrlBase') + 'events'
                + query).done(
            function(data) {
                data.data.forEach(function(event) {
                    event['tsShort'] = moment(event['ts']).format(
                        'HH:mm:ss.SSS');
                    event['tsLong'] = moment(event['ts']).format(
                        'YYYY-MM-DD HH:mm:ss.SSS');
                });
                return data;
            }).fail(function(jqXHR, textStatus, errorThrown) {
            alert('Unable to get JSON data: ' + textStatus);
        });
    },
    actions : {
        refreshData : function() {
            this.refresh();
        },
        clearEvents : function() {
            var route = this;
            $.ajax(
                route.controllerFor('application').get('restUrlBase')
                    + 'events', {
                    'type' : 'DELETE'
                }).then(function(data) {
                route.refresh();
            });
        },
    }
});

Probe.OverviewRoute = Ember.Route.extend({
    queryParams : {
        bda : {
            refreshModel : true
        },
    },
    model : function(params) {
        var data = new Object();
        var appController = this.controllerFor('application');

        // Determine the bdas to include
        data.filteredBdas = new Array();
        if (params.bda) {
            console.log("Build overview graph for " + params.bda);
            data.filteredBdas.push(findBeanDeploymentArchive(appController
                .get('bdas'), params.bda));
        } else {
            var beanArchivesController = this.controllerFor('beanArchives');
            if (beanArchivesController.get('lastModelUpdateTs')) {
                var selectedBdas = beanArchivesController
                    .get('selectedBdasUnwrapped');
                console.log("Build overview graph for " + selectedBdas.length
                    + " selected bean archives");
                data.filteredBdas = selectedBdas.slice(0);
            } else {
                console.log("Build overview graph for all bean archives");
                appController.get('bdas').forEach(function(bda, index, array) {
                    data.filteredBdas.push(bda);
                });
            }
        }

        // Prepare promises
        var promises = new Array();
        if (data.filteredBdas && data.filteredBdas.length > 0) {
            data.filteredBdas.forEach(function(bda, index, array) {
                var query = '';
                query = appendToQuery(query, 'filters', appendToFilters('',
                    'bda', bda.id));
                query = appendToQuery(query, 'pageSize', '0');
                query = appendToQuery(query, 'representation', 'simple');
                promises.push($.getJSON(
                    appController.get('restUrlBase') + 'beans' + query).done(
                    function(data) {
                        return data;
                    }).fail(function(jqXHR, textStatus, errorThrown) {
                    alert('Unable to get JSON data: ' + textStatus);
                }))
            });
        }

        // Load data, build graph
        return Ember.RSVP.all(promises).then(function(values) {
            var beans = new Array();
            values.forEach(function(bdaPage, index, array) {
                bdaPage.data.forEach(function(bean, index, array) {
                    beans.push(bean);
                });
            });
            data.beans = beans;
            buildOverviewGraphData(data);
            return data;
        });
    }
});

Probe.AvailableBeansRoute = Ember.Route
    .extend(
        Probe.ResetScroll,
        {
            queryParams : {
                page : {
                    refreshModel : true
                },
                bda : {
                    refreshModel : true
                },
                resolve : {
                    refreshModel : true
                }
            },
            setupController : function(controller, model) {
                this._super(controller, model);
                controller.set("pages", buildPages(model.page, model.lastPage));
            },
            model : function(params) {
                var appController = this.controllerFor('application');
                var query = '', filters = '';
                if (!params.bda) {
                    return new Array();
                }
                filters = appendToFilters(filters, 'requiredType',
                    params.requiredType);
                filters = appendToFilters(filters, 'qualifiers',
                    params.qualifiers);
                filters = appendToFilters(filters, 'resolve', params.resolve);
                if (params.bda) {
                    appController.get('bdas').forEach(
                        function(bda) {
                            if (bda.id == params.bda) {
                                filters = appendToFilters(filters, 'bdaId',
                                    bda.id);
                            }
                        });
                }
                query = appendToQuery(query, 'filters', filters);
                if (params.page) {
                    query = appendToQuery(query, 'page', params.page);
                }
                return $
                    .getJSON(
                        appController.get('restUrlBase') + 'availableBeans'
                            + query).done(
                        function(data) {
                            if (data.data) {
                                data.data.forEach(function(bean) {
                                    // We want to render the bda index
                                    bean.bda = findBeanDeploymentArchive(
                                        appController.get('bdas'), bean.bdaId);
                                });
                            }
                            return data;
                        }).fail(function(jqXHR, textStatus, errorThrown) {
                        alert('Unable to get JSON data: ' + textStatus);
                    });
            },
            actions : {
                refreshData : function() {
                    this.refresh();
                }
            }
        });

// CONTROLLERS

Probe.ApplicationController = Ember.ObjectController
    .extend({
        init : function() {
            this._super();
            this.startWatchingTime();
        },
        restUrlBase : '${rest.url.base}',
        beanKinds : [ 'MANAGED', 'SESSION', 'PRODUCER_METHOD',
                'PRODUCER_FIELD', 'RESOURCE', 'SYNTHETIC', 'INTERCEPTOR',
                'DECORATOR', 'EXTENSION', 'BUILT_IN' ],
        beanKindsShort : [ 'MB', 'SB', 'PM', 'PF', 'RE', 'SY', 'IN', 'DE',
                'EX', 'BI' ],
        observerDeclaringBeanKinds : [ 'MANAGED', 'SESSION', 'EXTENSION' ],
        eventKinds : [ 'APPLICATION', 'CONTAINER' ],
        receptions : [ 'ALWAYS', 'IF_EXISTS' ],
        txPhases : [ 'IN_PROGRESS', 'BEFORE_COMPLETION', 'AFTER_COMPLETION',
                'AFTER_FAILURE', 'AFTER_SUCCESS' ],
        additionalBdaSuffix : '.additionalClasses',
        markerFilterAddBdas : "probe-filterAdditionalBdas",
        bdas : null,
        filterBdas : null,
        configuration : null,
        initTime : function() {
            return moment(this.get("content").initTs).format(
                'YYYY-MM-DD HH:mm:ss');
        }.property(),
        initTimeFromNow : function() {
            return moment(this.get("content").initTs).fromNow(true);
        }.property("initTime"),
        startWatchingTime : function() {
            var self = this;
            Ember.run.later(this, function() {
                self.notifyPropertyChange("initTime");
                self.startWatchingTime();
            }, 5 * 1000 * 60);
        },
        dashboard : null
    });

Probe.DashboardController = Ember.ObjectController
    .extend({
        needs : [ 'application' ],
        lastUpdate : null,
        onModelChanged : function() {
            this.set('lastUpdate', moment(new Date()).format(
                'YYYY-MM-DD HH:mm:ss'));
        }.observes('model'),
        actions : {
            refresh : function() {
                this.send('refreshData');
            }
        }
    });

Probe.BeanArchivesController = Ember.ArrayController.extend({
    needs : [ 'application' ],
    lastModelUpdateTs : null,
    hideAddBda : true,
    onSettingsChanged : function() {
        this.send("settingHasChanged", false);
    }.observes('hideAddBda'),
    onModelChanged : function() {
        this.set('lastModelUpdateTs', new Date());
        this.send("settingHasChanged", true);
    }.observes('model'),
    bdas : Ember.computed.map('model', function(bda) {
        return Ember.ObjectProxy.create({
            content : bda,
            selected : true,
            visible : true,
        });
    }),
    visibleBdas : Ember.computed.filterBy('bdas', 'visible', true),
    selectedBdas : Ember.computed.filterBy('visibleBdas', 'selected', true),
    selectedBdasUnwrapped : Ember.computed.mapBy('selectedBdas', 'content'),
    queryParams : [ 'hideAddBda' ],
    actions : {
        settingHasChanged : function(checkTooMuchData) {
            var controller = this;
            var appController = this.get('controllers.application');
            this.get('bdas').forEach(
                function(proxy) {
                    if (controller.get('hideAddBda')
                        && isAdditionalBda(appController
                            .get('additionalBdaSuffix'), proxy.get('bdaId'))) {
                        proxy.set('visible', false);
                    } else {
                        proxy.set('visible', true);
                    }
                });
            this.send('rebuildGraph', checkTooMuchData);
        },
        rebuildGraph : function(checkTooMuchData) {
            this.set('graphData', buildBdaGraphData(this
                .get('selectedBdasUnwrapped'), checkTooMuchData));
        },
        selectAll : function() {
            this.get('bdas').forEach(function(proxy) {
                proxy.set('selected', true);
            });
            this.send('rebuildGraph');
        },
        selectNone : function() {
            this.get('bdas').forEach(function(proxy) {
                proxy.set('selected', false);
            });
            this.send('rebuildGraph');
        },
        invertSelection : function() {
            this.get('bdas').forEach(function(proxy) {
                proxy.set('selected', !proxy.get('selected'));
            });
            this.send('rebuildGraph');
        }
    }
});

Probe.BeanArchiveController = Ember.ObjectController.extend({});

Probe.ConfigurationController = Ember.ArrayController.extend({});

Probe.BeanListController = Ember.ObjectController.extend({
    needs : [ 'application' ],
    beanKinds : Ember.computed.alias('controllers.application.beanKinds'),
    filterBdas : Ember.computed.alias('controllers.application.filterBdas'),
    bda : Ember.computed.alias('controllers.application.markerFilterAddBdas'),
    kind : null,
    scope : '',
    beanClass : '',
    beanType : '',
    qualifier : '',
    unused : false,
    page : 1,
    queryParams : [ 'bda', 'kind', 'scope', 'beanClass', 'beanType',
            'qualifier', 'unused', 'page' ],
    actions : {
        clearFilters : function() {
            this.set('page', 1);
            this.set('bda', null);
            this.set('scope', '');
            this.set('beanClass', '');
            this.set('beanType', '');
            this.set('qualifier', '');
            this.set('kind', null);
            this.set('unused', false);
            this.send("refreshData");
        },
        filter : function() {
            this.send('refreshData');
        }
    },
});

Probe.BeanDetailController = Ember.ObjectController.extend({
    needs : [ 'application' ],
    transientDependencies : true,
    transientDependents : false,
    injectionPointInfo : true,
    bdas : Ember.computed.alias('controllers.application.bdas'),
    beanKinds : Ember.computed.alias('controllers.application.beanKinds'),
    beanKindsShort : Ember.computed
        .alias('controllers.application.beanKindsShort'),
    onModelChanged : function() {
        // A bean kind css class binding
        this.set("kindClass", this.get('model.kind') + ' boxed');
        this.send("rebuildGraph");
    }.observes('model'),
    onSettingsChanged : function() {
        this.send("rebuildGraph");
    }.observes('transientDependencies', 'transientDependents',
        'injectionPointInfo'),
    actions : {
        rebuildGraph : function() {
            this.set('graphData', buildDependencyGraphData(this.get('model'),
                this.get('model').id, this.get("transientDependencies"), this
                    .get("transientDependents")));
        }
    }
});

Probe.ObserverListController = Ember.ObjectController.extend({
    needs : [ 'application' ],
    beanKinds : Ember.computed
        .alias('controllers.application.observerDeclaringBeanKinds'),
    filterBdas : Ember.computed.alias('controllers.application.filterBdas'),
    bda : Ember.computed.alias('controllers.application.markerFilterAddBdas'),
    receptions : Ember.computed.alias('controllers.application.receptions'),
    txPhases : Ember.computed.alias('controllers.application.txPhases'),
    observedType : '',
    beanClass : '',
    reception : null,
    txPhase : null,
    qualifier : '',
    kind : null,
    page : 1,
    queryParams : [ 'bda', 'observedType', 'beanClass', 'reception', 'txPhase',
            'qualifier', 'page', 'kind' ],
    actions : {
        clearFilters : function() {
            this.set('page', 1);
            this.set('bda', null);
            this.set('observedType', '');
            this.set('beanClass', '');
            this.set('reception', null);
            this.set('txPhase', null);
            this.set('kind', null);
            this.set('qualifier', '');
            this.send("refreshData");
        },
        filter : function() {
            this.send('refreshData');
        }
    },
});

Probe.InvocationListController = Ember.ObjectController
    .extend({
        beanClass : '',
        methodName : '',
        search : '',
        description : '',
        page : 1,
        queryParams : [ 'beanClass', 'methodName', 'search', 'description',
                'page' ],
        actions : {
            clearFilters : function() {
                this.set('page', 1);
                this.set('beanClass', '');
                this.set('methodName', '');
                this.set('search', '');
                this.set('description', '');
                this.send('refreshData');
            },
            filter : function() {
                this.send('refreshData');
            }
        },
    });

Probe.InvocationDetailController = Ember.ObjectController.extend({});

Probe.EventsController = Ember.ObjectController.extend({
    needs : [ 'application' ],
    eventKinds : Ember.computed.alias('controllers.application.eventKinds'),
    eventInfo : '',
    type : '',
    qualifiers : '',
    page : 1,
    kind : null,
    queryParams : [ 'eventInfo', 'type', 'qualifiers', 'page', 'kind' ],
    actions : {
        clearFilters : function() {
            this.set('eventInfo', '');
            this.set('type', '');
            this.set('qualifiers', '');
            this.set('page', 1);
            this.set('kind', null);
            this.send('refreshData');
        },
        filter : function() {
            this.send('refreshData');
        }
    },
});

Probe.ContextController = Ember.ObjectController.extend({
    cid : null,
    onCidChanged : function() {
        this.send('refreshData');
    }.observes('cid')
});

Probe.ContextInstanceController = Ember.ObjectController.extend({
    cid : null,
    queryParams : [ 'cid' ],
});

Probe.OverviewController = Ember.ObjectController.extend({
    needs : [ 'application' ],
    bda : null,
    queryParams : [ 'bda' ],
    beanKinds : Ember.computed.alias('controllers.application.beanKinds'),
    beanKindsShort : Ember.computed
        .alias('controllers.application.beanKindsShort'),
});

Probe.AvailableBeansController = Ember.ObjectController.extend({
    needs : [ 'application' ],
    bdas : Ember.computed.alias('controllers.application.bdas'),
    bda : null,
    requiredType : '',
    qualifiers : '',
    resolve : true,
    page : 1,
    queryParams : [ 'bda', 'requiredType', 'qualifiers', 'resolve', 'page' ],
    actions : {
        clearFilters : function() {
            this.set('page', 1);
            this.set('bda', null);
            this.set('requiredType', '');
            this.set('qualifiers', '');
            this.set('resolve', 'true');
            this.send("refreshData");
        },
        lookup : function() {
            this.send('refreshData');
        }
    },
});

// HELPERS

Ember.Handlebars.registerBoundHelper('increment', function(integer) {
    return integer + 1;
});

Ember.Handlebars.registerBoundHelper('at', function() {
    return '@';
});

Ember.Handlebars.registerBoundHelper('eachLiAbbr', function(types, limit,
    options) {
    var ret = '<ul class="plain-list no-margin">';
    if (types) {
        for (var i = 0; i < types.length; i++) {
            var text = Handlebars.Utils.escapeExpression(types[i]);
            if (text.length > limit) {
                ret += '<li title="';
                ret += text;
                ret += '">';
                ret += text.charAt(0) === '@' ? abbreviateAnnotation(text,
                    true, false) : abbreviateType(text, true, false);
                ret += '</li>';
            } else {
                ret += "<li>";
                ret += text;
                ret += "</li>";
            }
        }
    }
    ret += '</ul>';
    return new Handlebars.SafeString(ret);
});

/*
 * This helper takes two params: text and limit. Furthermore it's possible to
 * specify optional hash arguments: title, suppressHtml, skipIcon and simple.
 */
Ember.Handlebars.registerBoundHelper('abbr', function(text, limit, options) {
    var escaped = Handlebars.Utils.escapeExpression(text);
    if (escaped.length <= limit) {
        return new Handlebars.SafeString(escaped);
    }
    var addTitle = options.hash.title || true;
    var suppresshtmlOutput = options.hash.suppressHtml || false;
    var skipIcon = options.hash.skipIcon || false;
    var ret = '';
    if (options.hash.simple) {
        ret = abbreviateSimple(escaped, limit, !suppresshtmlOutput, addTitle,
            skipIcon);
    } else {
        ret += escaped.charAt(0) === '@' ? abbreviateAnnotation(escaped,
            !suppresshtmlOutput, addTitle, skipIcon) : abbreviateType(escaped,
            !suppresshtmlOutput, addTitle, skipIcon);
    }
    return new Handlebars.SafeString(ret);
});

Ember.Handlebars.registerBoundHelper('detailIcon', function() {
    return new Handlebars.SafeString(
        '<span class="fa-stack"><i class="fa fa-square-o fa-stack-2x"></i><i class="fa fa-bars fa-stack-1x" title="Go to detail"></i></span>');
});

Ember.Handlebars
    .registerBoundHelper(
        'bean-kind-short-help',
        function() {
            return new Handlebars.SafeString(
                '<strong>MB</strong> - managed bean, <strong>SB</strong> - session bean, <strong>PM</strong> - producer method, <strong>PF</strong> - producer field, <strong>RE</strong> - resource, <strong>SY</strong> - synthetic bean, <strong>IN</strong> - interceptor, <strong>DE</strong> - decorator, <strong>EX</strong> - extension, <strong>BI</strong> - built-in bean');
        });

/**
 * This helper is used to render a tooltip-like icon.
 */
Ember.Handlebars.registerBoundHelper('tip', function(text, options) {
    var stripHtml = options.hash.stripHtml || false;
    if (stripHtml) {
        text = text.replace(/(<([^>]+)>)/ig, "");
    }
    return new Handlebars.SafeString(
        '<i class="fa fa-lg fa-info-circle" title="' + text + '"></i>');
});

Ember.Handlebars
    .registerBoundHelper(
        'probe-comp',
        function() {
            return new Handlebars.SafeString(
                '<i class="fa fa-lg fa-info-circle probe-comp" title="Probe internal component"></i>');
        });

Ember.Handlebars.registerBoundHelper('highlight', function(source, options) {
    if (source == undefined || source == null || source == '') {
        return new Handlebars.SafeString('');
    }
    var lang = options.hash.lang || 'Java';
    return new Handlebars.SafeString(hljs.highlight(lang, source, true).value);
});

Ember.Handlebars.registerBoundHelper('stackIcon', function(icon, options) {
    var large = options.hash.lg || false;
    var dark =  options.hash.dark || false;
    return new Handlebars.SafeString(
        '<span class="fa-stack' + (large ? ' fa-lg':'') + '"><i class="fa ' + (dark ? 'fa-square':'fa-square-o') + ' fa-stack-2x"></i><i class="fa ' + icon + ' fa-stack-1x" title="' + options.hash.title + '"></i></span>');
});

Ember.Handlebars.registerBoundHelper('inlineFormSep', function() {
    return new Handlebars.SafeString('&nbsp;<span class="separator">|</span>&nbsp;');
});

// VIEWS

Probe.DependencyGraph = Ember.View
    .extend({

        contentChanged : function() {
            this.rerender();
        }.observes('content'),

        didInsertElement : function() {

            var injectionPointInfo = this.get('controller').get(
                "injectionPointInfo");
            var controller = this.get('controller');

            var data = this.get('content');
            if (!data) {
                alert("No data to render!");
                return;
            }

            var margin = {
                top : 20,
                right : 120,
                bottom : 20,
                left : 120
            }
            // TODO responsive design
            var width = 1280;
            // var height = 900 - margin.top - margin.bottom;
            var height = 700 + ((data.links.length / 20) * 200) - margin.top
                - margin.bottom;

            var nodes = d3.values(data.nodes);
            var links = data.links;

            var force = d3.layout.force().nodes(nodes).links(links).size(
                [ width, height ]).gravity(.05).linkDistance(450).charge(-500)
                .on("tick", tick).start();

            var elementId = this.get('elementId');
            var element = d3.select('#' + elementId);
            var svg = element.append("svg").attr("height",
                height + margin.top + margin.bottom).attr("width", width);

            // Type markers
            svg.append("defs").selectAll("marker").data(
                [ "inject", "injectedBy", "declaredBy" ]).enter().append(
                "marker").attr("id", function(d) {
                return d;
            }).attr("viewBox", "0 -5 10 10").attr("refX", 20).attr("refY", 0)
                .attr("markerWidth", 5).attr("markerHeight", 5).attr("orient",
                    "auto").style("fill", "#323232").append("path").attr("d",
                    "M0,-5L10,0L0,5");

            // Links - lines
            var link = svg.selectAll("line.link").data(links).enter().append(
                "svg:line").attr("class", "link").attr("x1", function(d) {
                return d.source.x;
            }).attr("y1", function(d) {
                return d.source.y;
            }).attr("x2", function(d) {
                return d.target.x;
            }).attr("y2", function(d) {
                return d.target.y;
            }).attr("marker-end", function(d) {
                return "url(#" + d.type + ")";
            }).style("stroke-dasharray", function(d) {
                if (d.source.isDependent) {
                    return "5,5";
                }
                if (d.target.isRoot) {
                    // Circular dependency
                    return "10,15";
                }
            }).style("stroke", function(d) {
                if (!d.source.isDependent && d.target.isRoot) {
                    // Circular dependency
                    return "red";
                } else if (d.isPotential) {
                    return 'Aquamarine';
                } else if (d.type == 'declaredBy') {
                    return 'LightBlue';
                }
            }).style("stroke-width", function(d) {
                return "3px";
            });

            // Links - labels
            if (injectionPointInfo) {
                var linkLabel = svg.selectAll("g.link-label").data(links)
                    .enter().append("svg:g").attr("class", "labelText");
                linkLabel.append("circle").attr("r", 8).style("fill", "silver");
                linkLabel
                    .append("title")
                    .text(
                        function(d) {
                            if (!d.dependencies) {
                                return;
                            }
                            if (d.dependencies.length == 1) {
                                return getInjectionPointInfo(d);
                            } else {
                                return 'Multiple injection points found, click to show details.';
                            }
                        });
                linkLabel.append("svg:text").attr("class",
                    "nodetext injection-point-info").style("fill", "black")
                    .style("font-size", "90%").each(
                        function(d) {
                            var text = d3.select(this);
                            var desc;
                            if (d.info) {
                                desc = d.info;
                            } else if (d.dependencies) {
                                if (d.dependencies.length == 1) {
                                    desc = abbreviateType(
                                        d.dependencies[0].requiredType, false,
                                        false);
                                } else {
                                    desc = '(' + d.dependencies.length + ')';
                                }
                            }
                            text.append("tspan").attr("x", 10).attr("dy", 15)
                                .attr(
                                    "text-anchor",
                                    function() {
                                        if (!d.source.isDependent
                                            && d.target.isRoot) {
                                            return "end";
                                        }
                                    }).text(desc);
                        });
            }

            var node_drag = d3.behavior.drag().on("dragstart", dragstart).on(
                "drag", dragmove).on("dragend", dragend);

            function dragstart(d, i) {
                force.stop()
            }

            function dragmove(d, i) {
                d.px += d3.event.dx;
                d.py += d3.event.dy;
                d.x += d3.event.dx;
                d.y += d3.event.dy;
                tick();
            }

            function dragend(d, i) {
                d.fixed = true;
                tick();
                force.resume();
            }

            // Injection point info dialog
            svg.selectAll("text.injection-point-info").on(
                "click",
                function(d, i) {
                    $('div#ipInfoModal div.modal-body').html(
                        getInjectionPointInfoHtml(d));
                    $('div#ipInfoModal').modal('show');
                });

            var node = svg.selectAll("g.node").data(nodes).enter().append(
                "svg:g").attr("class", "node").call(node_drag);

            node.append("title").text(function(d) {
                return d.beanClass;
            });

            node.append("circle").attr("r", 16).style("stroke", function(d) {
                if (d.isRoot) {
                    return "#323232";
                }
            }).style("stroke-width", function(d) {
                if (d.isRoot) {
                    return "4";
                }
            }).style(
                "fill",
                function(d) {
                    if (d.kind == 'BUILT_IN') {
                        return 'DarkGray';
                    }
                    // At this point bda should be always non-null
                    var bda = findBeanDeploymentArchive(controller.get('bdas'),
                        d.bda);
                    return bda != null ? bda.color : "black";
                });

            var text = node
                .append("text")
                .attr(
                    "dx",
                    function(d) {
                        return (d.kind === controller.get('beanKinds')[6]
                            || d.kind === controller.get('beanKinds')[9] || d.kind === controller
                            .get('beanKinds')[3]) ? "-8" : "-12";
                    }).attr("dy", "5").style("fill", "white").text(
                    function(d) {
                        return getBeanKindShort(controller.get('beanKinds'),
                            controller.get('beanKindsShort'), d.kind);
                    });

            node.append("a").attr("xlink:href", function(d) {
                return "#/bean/" + d.id;
            }).append("svg:text").attr("dx", 20).attr("dy", "5").style("fill",
                "#428bca").text(function(d) {
                return abbreviateType(d.beanClass, false, false);
            });

            force.on("tick", tick);

            function tick() {

                link.attr("x1", function(d) {
                    return d.source.x;
                }).attr("y1", function(d) {
                    return d.source.y;
                }).attr("x2", function(d) {
                    return d.target.x;
                }).attr("y2", function(d) {
                    return d.target.y;
                });

                node.attr("transform", function(d) {
                    return "translate(" + d.x + "," + d.y + ")";
                });

                if (injectionPointInfo) {
                    linkLabel.attr("transform", function(d) {
                        return "translate("
                            + (d.source.x + (d.target.x - d.source.x) / 2)
                            + ","
                            + (d.source.y + ((d.target.y - d.source.y) / 2))
                            + ")";
                    });
                }
            }
        }
    });

function getInjectionPointInfo(d) {
    if (!d.dependencies) {
        return '';
    }
    var desc = '';
    for (var j = 0; j < d.dependencies.length; j++) {
        // Injection point info
        var qualifiers = "";
        var requiredType = "";
        if (d.dependencies[j].qualifiers) {
            for (var k = 0; k < d.dependencies[j].qualifiers.length; k++) {
                qualifiers += d.dependencies[j].qualifiers[k] + " ";
            }
        }
        if (d.dependencies[j].requiredType) {
            requiredType += d.dependencies[j].requiredType;
        }
        desc += qualifiers;
        desc += ' ' + requiredType;
        return desc;
    }
}

function getInjectionPointInfoHtml(d) {
    if (!d.dependencies) {
        return '';
    }
    var description = '<ul>';
    for (var j = 0; j < d.dependencies.length; j++) {
        // Injection point info
        var qualifiers = "";
        var requiredType = "";
        if (d.dependencies[j].qualifiers) {
            for (var k = 0; k < d.dependencies[j].qualifiers.length; k++) {
                qualifiers += Handlebars.Utils
                    .escapeExpression(d.dependencies[j].qualifiers[k])
                    + " ";
            }
        }
        if (d.dependencies[j].requiredType) {
            requiredType += '<strong>'
                + Handlebars.Utils
                    .escapeExpression(d.dependencies[j].requiredType)
                + '</strong>';
        }
        description += '<li>' + qualifiers + ' ' + requiredType + '</li>';
    }
    description += '</ul>';
    return description;
}

Probe.InvocationTree = Ember.View.extend({

    didInsertElement : function() {

        var data = this.get('content.transformed');
        if (!data) {
            alert("No data to render!");
        }

        // TODO responsive design
        var margin = {
            top : 120,
            right : 120,
            bottom : 20,
            left : 120
        }
        var width = 1280 - margin.right - margin.left;
        var height = (getChildrenCount(data) * 80) + 100;

        var i = 0;
        var tree = d3.layout.tree().size([ height, width ]);

        var elementId = this.get('elementId');
        var element = d3.select('#' + elementId);
        var svg = element.append("svg")

        svg.attr("viewBox", "0 0 1280 " + height).attr("preserveAspectRatio",
            "xMinYMin meet").attr("width", width).attr("height", height);

        // Type markers
        svg.append("defs").selectAll("marker").data([ "invocation" ]).enter()
            .append("marker").attr("id", function(d) {
                return d;
            }).attr("viewBox", "0 -5 10 10").attr("refX", 14).attr("refY", 0)
            .attr("markerWidth", 6).attr("markerHeight", 6).attr("orient",
                "auto").style("fill", "gray").append("path").attr("d",
                "M0,-5L7,0L0,5");

        var g = svg.append("g").attr("transform",
            "translate(" + margin.left + "," + margin.top + ")");

        // Compute the new tree layout
        var nodes = tree.nodes(data).reverse(), links = tree.links(nodes);
        var idx = nodes.length;

        // Normalize for fixed-depth
        nodes.forEach(function(d) {
            idx--;
            if (d.depth == 0) {
                d.y = 20;
                d.x = 20;
            } else {
                d.y = (d.depth * 220);
                d.x = (idx * 80);
            }
        });

        // Declare the nodes
        var node = svg.selectAll("g.node").data(nodes, function(d) {
            return d.id || (d.id = ++i);
        });

        // Enter the nodes
        var nodeEnter = node.enter().append("g").attr("class", "node").attr(
            "transform", function(d) {
                return "translate(" + d.y + "," + d.x + ")";
            });

        nodeEnter.append("circle").attr("r", 9).attr("class", function(d) {
            if (d.parent == null) {
                return "circle-root";
            }
            if (d.type == "PRODUCER" || d.type == "DISPOSER") {
                return "circle-producer";
            }
            if (d.type == "OBSERVER") {
                return "circle-observer";
            }
            if (d.type == "CONSTRUCTOR") {
                return "circle-constructor";
            }
            return "circle-regular";
        });

        nodeEnter.filter(function(d) {
            return d.interceptedBean;
        }).append("a").attr("xlink:href", function(d) {
            return '#/bean/' + d.interceptedBean.id;
        }).append("text").attr("dx", 12).attr("dy", -5)
            .style("fill", "#428bca").text(function(d) {
                return d.interceptedBeanClass;
            });

        nodeEnter.filter(function(d) {
            return d.declaringClass;
        }).append("text").attr("dx", 12).attr("dy", -5).text(function(d) {
            return d.declaringClass;
        });

        nodeEnter.append("text").attr("dx", 12).attr("dy", 12).style("fill",
            "#333").style("font-size", "90%").text(function(d) {
            return d.methodName ? d.methodName : '';
        });

        // Declare the links
        var link = svg.selectAll("path.link").data(links, function(d) {
            return d.target.id;
        });

        var linkLabel = svg.selectAll("g.link-label").data(links).enter()
            .append("svg:g").attr("class", "labelText");
        linkLabel.append("circle").attr("r", 5).style("fill", "silver");
        linkLabel.append("svg:text").attr("class", "nodetext").style("fill",
            "black").attr("x", "0.6em").attr("dy", "-5").style("font-size",
            "90%").style("font-weight", "normal").each(
            function(d) {
                var text = d3.select(this).text(
                    d.target.start + " (" + d.target.duration + ' ms)');
            });

        linkLabel.attr("transform", function(d) {
            return "translate(" + d.source.y + "," + d.target.x + ")";
        });

        // Enter the links
        link.enter().insert("path", "g").attr(
            "d",
            function(d) {
                return "M" + d.source.y + "," + d.source.x + "L" + d.source.y
                    + "," + d.target.x + "L" + d.target.y + "," + d.target.x;
            }).style(
            "stroke-dasharray",
            function(d) {
                if (d.target.type == "PRODUCER" || d.target.type == "DISPOSER"
                    || d.target.type == "OBSERVER" || d.target.type == "CONSTRUCTOR") {
                    return "5,5";
                }
            }).attr("marker-end", function(d) {
            return "url(#invocation)";
        }).attr("class", function(d) {
            if (d.target.type == "PRODUCER" || d.target.type == "DISPOSER") {
                return "stroke-producer link";
            } else if (d.target.type == "OBSERVER") {
                return "stroke-observer link";
            } else {
                return "link";
            }
        });

    }
});

Probe.BdaGraph = Ember.View.extend({

    contentChanged : function() {
        this.rerender();
    }.observes("content"),

    didInsertElement : function() {

        var data = this.get('content');
        if (!data) {
            alert("No data to render!");
            return;
        }
        if (data.tooMuchData) {
            return;
        }

        var margin = {
            top : 20,
            right : 120,
            bottom : 20,
            left : 120
        }
        // TODO responsive design
        var width = 1280;
        var height = 600 - margin.top - margin.bottom;

        var nodes = d3.values(data.nodes);
        var links = data.links;

        // D3 force layout
        var force = d3.layout.force().nodes(nodes).links(links).size(
            [ width, height ]).gravity(.05).linkDistance(250).charge(-500).on(
            "tick", tick).start();

        var elementId = this.get('elementId');
        var element = d3.select('#' + elementId);
        var svg = element.append("svg").attr("height",
            height + margin.top + margin.bottom).attr("width", width);

        // Arrow marker
        var marker = svg.append("svg:defs").selectAll("marker").data([ "end" ])
            .enter().append("svg:marker").attr("id", String).attr("viewBox",
                "0 -5 10 10").attr("refX", 22).attr("refY", -1.5).attr(
                "markerWidth", 6).attr("markerHeight", 6)
            .attr("orient", "auto").append("svg:path").attr("d",
                "M0,-5L10,0L0,5");

        // add the links and the arrows
        var path = svg.append("svg:g").selectAll("path").data(force.links())
            .enter().append("svg:path").attr("class", "link").attr(
                "marker-end", "url(#end)");

        var node_drag = d3.behavior.drag().on("dragstart", dragstart).on(
            "drag", dragmove).on("dragend", dragend);

        function dragstart(d, i) {
            force.stop()
        }

        function dragmove(d, i) {
            d.px += d3.event.dx;
            d.py += d3.event.dy;
            d.x += d3.event.dx;
            d.y += d3.event.dy;
            tick();
        }

        function dragend(d, i) {
            d.fixed = true;
            tick();
            force.resume();
        }

        var node = svg.selectAll("g.node").data(nodes).enter().append("svg:g")
            .attr("class", "node").call(node_drag);

        node.append("title").text(function(d) {
            return d.bdaId;
        });

        node.append("circle").attr("r", 16).style("fill", function(d) {
            return d.fill;
        });

        var text = node.append("svg:text").attr("dx", function(d) {
            return d.idx > 9 ? "-10" : "-5";
        }).attr("dy", "5").style("fill", "white").text(function(d) {
            return d.idx;
        });

        node.on('mouseover', function(d) {
            path.style('stroke', function(l) {
                if (d === l.source) {
                    return 'LightGreen';
                } else if (d === l.target) {
                    return 'Tomato';
                } else {
                    return '#fafafa';
                }
            });
            path.style('stroke-opacity', function(l) {
                return (d === l.source || d === l.target) ? 1 : 0;
            });
            path.attr("marker-end", function(l) {
                return (d === l.source || d === l.target) ? 'url(#end)' : '';
            });
        }).on('mouseout', function() {
            path.style('stroke', function(l) {
                return '#ccc';
            });
            path.attr('marker-end', function(l) {
                return 'url(#end)';
            });
            path.style('stroke-opacity', function(l) {
                return 1;
            });
        });

        force.on("tick", tick);

        function tick() {
            path.attr("d",
                function(d) {
                    var dx = d.target.x - d.source.x, dy = d.target.y
                        - d.source.y, dr = Math.sqrt(dx * dx + dy * dy);
                    return "M" + d.source.x + "," + d.source.y + "A" + dr + ","
                        + dr + " 0 0,1 " + d.target.x + "," + d.target.y;
                });
            node.attr("transform", function(d) {
                return "translate(" + d.x + "," + d.y + ")";
            });
        }

    }
});

Probe.OverviewGraph = Ember.View
    .extend({

        contentChanged : function() {
            this.rerender();
        }.observes('content'),

        didInsertElement : function() {

            var data = this.get('content');
            if (!data) {
                alert("No data to render!");
                return;
            }

            var controller = this.get('controller');

            var margin = {
                top : 20,
                right : 120,
                bottom : 20,
                left : 120
            }
            // TODO responsive design
            var width = 1280;
            var height = 600 + ((data.beans.length / 10) * 150) - margin.top
                - margin.bottom;

            var nodes = d3.values(data.nodes);
            var links = data.links;

            // D3 force layout
            var force = d3.layout.force().nodes(nodes).links(links).size(
                [ width, height ]).gravity(.05).linkDistance(250).charge(-500)
                .on("tick", tick).start();

            var elementId = this.get('elementId');
            var element = d3.select('#' + elementId);
            var svg = element.append("svg").attr("height",
                height + margin.top + margin.bottom).attr("width", width);

            // Arrow marker
            var marker = svg.append("svg:defs").selectAll("marker").data(
                [ "end" ]).enter().append("svg:marker").attr("id", String)
                .attr("viewBox", "0 -5 10 10").attr("refX", 22).attr("refY",
                    -1.5).attr("markerWidth", 6).attr("markerHeight", 6).attr(
                    "orient", "auto").append("svg:path").attr("d",
                    "M0,-5L10,0L0,5");

            // add the links and the arrows
            var path = svg.append("svg:g").selectAll("path")
                .data(force.links()).enter().append("svg:path").attr("class",
                    "link").attr("marker-end", "url(#end)");
            path.style('stroke', function(l) {
                if (l.type == 'declaredBy') {
                    return 'LightBlue';
                } else if (l.isPotential) {
                    return 'Aquamarine';
                }
            });
            path.style("stroke-dasharray", function(l) {
                if (l.type == 'declaredBy' || l.isPotential) {
                    return "5,1";
                }
            })

            var node_drag = d3.behavior.drag().on("dragstart", dragstart).on(
                "drag", dragmove).on("dragend", dragend);

            function dragstart(d, i) {
                force.stop()
            }

            function dragmove(d, i) {
                d.px += d3.event.dx;
                d.py += d3.event.dy;
                d.x += d3.event.dx;
                d.y += d3.event.dy;
                tick();
            }

            function dragend(d, i) {
                d.fixed = true;
                tick();
                force.resume();
            }

            var node = svg.selectAll("g.node").data(nodes).enter().append(
                "svg:g").attr("class", "node").call(node_drag);

            node.append("title").text(function(d) {
                return d.kind + ": " + d.beanClass;
            });

            node.append("circle").attr("r", 16).style(
                "fill",
                function(d) {
                    if (d.kind == 'BUILT_IN') {
                        return 'DarkGray';
                    }
                    // At this point bda should be always non-null
                    var bda = findBeanDeploymentArchive(controller
                        .get('filteredBdas'), d.bda);
                    return bda != null ? bda.color : "black";
                });

            var text = node
                .append("text")
                .attr(
                    "dx",
                    function(d) {
                        return (d.kind === controller.get('beanKinds')[6]
                            || d.kind === controller.get('beanKinds')[9] || d.kind === controller
                            .get('beanKinds')[3]) ? "-8" : "-12";
                    }).attr("dy", "5").style("fill", "white").text(
                    function(d) {
                        return getBeanKindShort(controller.get('beanKinds'),
                            controller.get('beanKindsShort'), d.kind);
                    });

            node.append("a").attr("xlink:href", function(d) {
                return "#/bean/" + d.id;
            }).append("svg:text").attr("dx", 20).attr("dy", "5").style("fill",
                "#428bca").text(function(d) {
                return abbreviateType(d.beanClass, false, false);
            });

            node.on(
                'mouseover',
                function(d) {
                    path.style('stroke', function(l) {
                        if (d === l.source) {
                            return 'LightGreen';
                        } else if (d === l.target) {
                            return 'Tomato';
                        } else {
                            return '#fafafa';
                        }
                    });
                    path.style('stroke-opacity', function(l) {
                        return (d === l.source || d === l.target) ? 1 : 0;
                    });
                    path.attr("marker-end", function(l) {
                        return (d === l.source || d === l.target) ? 'url(#end)'
                            : '';
                    });
                }).on('mouseout', function() {
                path.style('stroke', function(l) {
                    if (l.type == 'declaredBy') {
                        return 'LightBlue';
                    } else if (l.info) {
                        return 'Aquamarine';
                    } else {
                        return '#ccc';
                    }
                });
                path.attr('marker-end', function(l) {
                    return 'url(#end)';
                });
                path.style('stroke-opacity', function(l) {
                    return 1;
                });
            });

            force.on("tick", tick);

            function tick() {
                path.attr("d", function(d) {
                    var dx = d.target.x - d.source.x, dy = d.target.y
                        - d.source.y, dr = Math.sqrt(dx * dx + dy * dy);
                    return "M" + d.source.x + "," + d.source.y + "A" + dr + ","
                        + dr + " 0 0,1 " + d.target.x + "," + d.target.y;
                });
                node.attr("transform", function(d) {
                    return "translate(" + d.x + "," + d.y + ")";
                });
            }

        }
    });

// UTILS

function findBeanDeploymentArchiveId(bdas, id) {
    if (bdas) {
        for (var i = 0; i < bdas.length; i++) {
            if (bdas[i].id == id) {
                return bdas[i].bdaId;
            }
        }
    }
    return null;
}

function findBeanDeploymentArchive(bdas, id) {
    if (bdas) {
        for (var i = 0; i < bdas.length; i++) {
            if (bdas[i].id == id) {
                return bdas[i];
            }
        }
    }
    return null;
}

function buildPages(page, lastPage) {
    var pages = [];
    for (var i = 1; i <= lastPage; i++) {
        var data = new Object();
        data['index'] = i;
        data['active'] = (page == i ? 'active' : 'inactive');
        pages.push(data);
    }
    return pages;
}

function appendToFilters(filters, key, value) {
    if (value == undefined || value == null || value == '') {
        return filters;
    }
    if (filters.length > 0) {
        filters += ' ';
    }
    filters += encodeURIComponent(key + ':' + '"' + value + '"');
    return filters;
}

function appendToQuery(query, key, value) {
    if (value == undefined || value == null || value == '') {
        return query;
    }
    if (query == '') {
        query += '?';
    } else {
        query += '&';
    }
    query += key + '=' + value;
    return query;
}

function findNodesDependencies(bean, nodes, rootId, transientDependencies,
    rootIsFixed) {
    if (!nodes[bean.id]) {
        nodes[bean.id] = {
            id : bean.id,
            beanClass : bean.beanClass,
            kind : bean.kind,
            // Root is always found in dependencies
            isRoot : (bean.id == rootId),
            fixed : (rootIsFixed && bean.id == rootId),
            bda : bean.bdaId,
        };
        if (nodes[bean.id].isRoot) {
            nodes[bean.id].x = 150;
            nodes[bean.id].y = 150;
        }
        findNodesDeclaredBean(bean, nodes);
    }
    if (rootId == null && !transientDependencies) {
        return;
    }
    if (bean.dependencies) {
        bean.dependencies.forEach(function(dependency) {
            findNodesDependencies(dependency, nodes, null,
                transientDependencies, rootIsFixed);
        });
    }
}

function findLinksDependencies(bean, links, nodes, transientDependencies) {
    if (bean.dependencies) {
        bean.dependencies.forEach(function(dependency) {

            // Injection point info
            var info = new Object();
            info.requiredType = dependency.requiredType;
            info.qualifiers = dependency.qualifiers;

            // First check identical links
            var found;
            for (var i = 0; i < links.length; i++) {
                if ((links[i].source == nodes[bean.id])
                    && (links[i].target == nodes[dependency.id])) {
                    found = links[i];
                    break;
                }
            }

            if (found) {
                found.dependencies.push(info);
            } else {
                links.push({
                    source : nodes[bean.id],
                    target : nodes[dependency.id],
                    type : 'inject',
                    dependencies : [ info ],
                    info : dependency.info,
                    isPotential : dependency.isPotential,
                });
                findLinksDeclaredBean(dependency, links, nodes);
            }
            if (transientDependencies) {
                findLinksDependencies(dependency, links, nodes,
                    transientDependencies);
            }
        });
    }
}

function findNodesDependents(bean, nodes, rootId, transientDependents) {
    if (!nodes[bean.id]) {
        nodes[bean.id] = {
            id : bean.id,
            beanClass : bean.beanClass,
            kind : bean.kind,
            bda : bean.bdaId,
            isDependent : true
        };
        findNodesDeclaredBean(bean, nodes);
    }
    if (rootId == null && !transientDependents) {
        return;
    }
    if (bean.dependents) {
        bean.dependents.forEach(function(dependent) {
            findNodesDependents(dependent, nodes, null, transientDependents);
        });
    }
}

function findLinksDependents(bean, links, nodes, transientDependents) {
    if (bean.dependents) {
        bean.dependents.forEach(function(dependent) {
            // Injection point info
            var info = new Object();
            info.requiredType = dependent.requiredType;
            info.qualifiers = dependent.qualifiers;

            // First check identical links
            var found;
            for (var i = 0; i < links.length; i++) {
                if ((links[i].target == nodes[bean.id])
                    && (links[i].source == nodes[dependent.id])) {
                    found = links[i];
                    break;
                }
            }

            if (found) {
                found.dependencies.push(info);
            } else {
                links.push({
                    target : nodes[bean.id],
                    source : nodes[dependent.id],
                    type : 'injectedBy',
                    dependencies : [ info ],
                    info : dependent.info,
                    isPotential : dependent.isPotential,
                });
                findLinksDeclaredBean(dependent, links, nodes);
            }
            if (transientDependents) {
                findLinksDependents(dependent, links, nodes,
                    transientDependents);
            }
        });
    }
}

function findNodesDeclaredBean(bean, nodes) {
    if (bean.declaringBean) {
        if (!nodes[bean.declaringBean.id]) {
            nodes[bean.declaringBean.id] = {
                id : bean.declaringBean.id,
                beanClass : bean.declaringBean.beanClass,
                kind : bean.declaringBean.kind,
                bda : bean.declaringBean.bdaId,
            };
        }
    }
}

function findLinksDeclaredBean(bean, links, nodes) {
    if (bean.declaringBean) {
        links.push({
            source : nodes[bean.id],
            target : nodes[bean.declaringBean.id],
            type : 'declaredBy',
            info : 'Declared by'
        });
    }
}

/**
 *
 * @param data
 *            BeanDetailRoute data
 * @param transientDependencies
 * @param transientDependents
 */
function buildDependencyGraphData(data, id, transientDependencies,
    transientDependents) {
    var result = new Object();
    // Create nodes
    var nodes = new Object();
    findNodesDependencies(data, nodes, id, transientDependencies, true);
    findNodesDependents(data, nodes, id, transientDependents);
    // Create links
    var links = new Array();
    findLinksDependencies(data, links, nodes, transientDependencies);
    findLinksDependents(data, links, nodes, transientDependents);
    findNodesDeclaredBean(data, nodes);
    findLinksDeclaredBean(data, links, nodes);
    result.nodes = nodes;
    result.links = links;
    console.log('Build dependency graph data [links: ' + links.length + ']');
    return result;
}

/**
 *
 * @param data
 *            OverviewRoute data
 */
function buildOverviewGraphData(data) {
    // Create nodes and links
    var nodes = new Object();
    var links = new Array();
    data.beans.forEach(function(bean, index, array) {
        findNodesDependencies(bean, nodes, bean.id, false, false);
        findNodesDependents(bean, nodes, bean.id, false);
        findLinksDependencies(bean, links, nodes, false);
        findLinksDependents(bean, links, nodes, false);
        findNodesDeclaredBean(bean, nodes);
        findLinksDeclaredBean(bean, links, nodes);
    });
    data.nodes = nodes;
    data.links = links;
    console.log('Build overview dependency graph data [nodes: '
        + data.beans.length + ', links: ' + links.length + ']');
}

/**
 *
 * @param selectedBdas
 */
function buildBdaGraphData(selectedBdas, checkTooMuchData) {
    var data = new Object();
    // Create nodes
    var nodes = new Object();
    findNodesBdas(selectedBdas, nodes);
    // Create links
    var links = new Array();
    findLinksBdas(selectedBdas, links, nodes);
    data.nodes = nodes;
    data.links = links;
    // Don't render the graph if too much data
    if (checkTooMuchData && (selectedBdas.length > 100 || links.length > 60)) {
        data.tooMuchData = true;
    }
    console.log('Build bean archives accessible graph data [nodes: '
        + selectedBdas.length + ', links: ' + links.length + ']');
    return data;
}

function findNodesBdas(selectedBdas, nodes) {
    if (selectedBdas) {
        selectedBdas.forEach(function(bda, index, array) {
            if (!nodes[bda.id]) {
                nodes[bda.id] = {
                    id : bda.id,
                    bdaId : bda.bdaId,
                    idx : bda.idx,
                    fill : bda.color,
                }
            }
        });
    }
}

function findLinksBdas(selectedBdas, links, nodes) {
    if (selectedBdas) {
        selectedBdas.forEach(function(bda) {
            if (bda.accessibleBdas) {
                bda.accessibleBdas.forEach(function(accessible) {
                    var accessibleBda = findBeanDeploymentArchive(selectedBdas,
                        accessible);
                    if (accessibleBda == null || bda.id == accessible) {
                        return;
                    }
                    // First check identical links
                    var found;
                    for (var i = 0; i < links.length; i++) {
                        if ((links[i].source == nodes[bda.id])
                            && (links[i].target == nodes[accessible])) {
                            found = links[i];
                            break;
                        }
                    }
                    if (!found) {
                        links.push({
                            source : nodes[bda.id],
                            target : nodes[accessible],
                        });
                    }
                });
            }
        });
    }
}

/**
 *
 * @param invocation
 * @param parent
 * @returns
 */
function transformInvocation(invocation, parent) {
    var node = new Object();
    node.interceptedBean = invocation.interceptedBean;
    if (invocation.interceptedBean) {
        node.interceptedBeanClass = abbreviateType(
            invocation.interceptedBean.beanClass, false, false);
    }
    if (invocation.declaringClass) {
        node.declaringClass = abbreviateType(invocation.declaringClass, false,
            false);
    }
    node.methodName = invocation.methodName;
    node.type = invocation.type;
    if (parent == null) {
        node.startFull = moment(invocation.start).format(
            'YYYY-MM-DD HH:mm:ss.SSS');
    }
    node.start = moment(invocation.start).format('HH:mm:ss.SSS');
    node.duration = (invocation.time / 1000000).toFixed(3);
    node.parent = parent;
    if (invocation.children) {
        node.children = invocation.children.map(function(item, index, array) {
            return transformInvocation(item, invocation);
        });
    }
    return node;
}

/**
 *
 * @param invocation
 * @param parent
 * @returns a synthetic root node
 */
function getRootNode(invocation, parent) {
    var node = new Object();
    node.interceptedBean = '';
    node.methodName = '';
    node.type = '';
    node.parent = null;
    node.children = [ transformInvocation(invocation, parent) ];
    return node;
}

function isAdditionalBda(additionalBdaSuffix, bdaId) {
    return bdaId.indexOf(additionalBdaSuffix, bdaId.length
        - additionalBdaSuffix.length) !== -1;
}

/**
 * This only works if the type represents either a raw type or a parameterized
 * type with actual type params represented as simple names.
 *
 * @param type
 * @param htmlOutput
 * @param title
 * @param skipIcon
 * @returns {String}
 */
function abbreviateType(type, htmlOutput, title, skipIcon) {
    var parts = type.split('.');
    var ret = '';
    var lastIdx = parts.length - 1;
    if (htmlOutput && title) {
        ret += ' <span title="' + type + '">';
    }
    for (var i = 0; i < parts.length; i++) {
        if (i === lastIdx) {
            ret += parts[i];
        } else {
            if (i === 0 && htmlOutput) {
                ret += '<span class="abbreviated">';
            }
            ret += parts[i].charAt(0);
            ret += '.';
            if (i === (lastIdx - 1) && htmlOutput) {
                ret += '</span>';
            }
        }
    }
    if (htmlOutput) {
        if (title) {
            ret += '</span>';
        }
        if (!skipIcon) {
            ret += ' <i class="fa fa-compress abbreviated"></i>';
        }
    }
    return ret;
}

/**
 *
 * @param text
 * @param htmlOutput
 * @param title
 * @param skipIcon
 * @returns {String}
 */
function abbreviateSimple(text, limit, htmlOutput, title, skipIcon) {
    var ret = '';
    if (htmlOutput && title) {
        ret += ' <span title="' + text + '">';
    }
    ret += text.substring(0, limit - 3);
    ret += "...";
    if (htmlOutput) {
        if (title) {
            ret += '</span>';
        }
        if (!skipIcon) {
            ret += ' <i class="fa fa-compress abbreviated"></i>';
        }
    }
    return ret;
}

/**
 * This only returns the abbreviated annotation type, params are omitted.
 *
 * @param annotation
 * @param htmlOutput
 * @param title
 * @param skipIcon
 * @returns {String}
 */
function abbreviateAnnotation(annotation, htmlOutput, title, skipIcon) {
    var ret = (htmlOutput && title) ? ' <span title="' + annotation + '">@'
        : '@';
    if (annotation.indexOf('(') !== -1) {
        annotation = annotation.substring(1, annotation.indexOf('('));
    } else {
        annotation = annotation.substr(1);
    }
    var parts = annotation.split('.');
    var lastIdx = parts.length - 1;
    for (var i = 0; i < parts.length; i++) {
        if (i === lastIdx) {
            ret += parts[i];
        } else {
            if (i === 0 && htmlOutput) {
                ret += '<span class="abbreviated">';
            }
            ret += parts[i].charAt(0);
            ret += '.';
            if (i === (lastIdx - 1) && htmlOutput) {
                ret += '</span>';
            }
        }
    }
    if (htmlOutput) {
        if (title) {
            ret += '</span>';
        }
        if (!skipIcon) {
            ret += ' <i class="fa fa-compress abbreviated"></i>';
        }
    }
    return ret;
}

function getChildrenCount(node) {
    var count = 0;
    if (node.children) {
        for (var i = 0; i < node.children.length; i++) {
            count += 1;
            count += getChildrenCount(node.children[i]);
        }
    }
    return count;
}

function generateColors(total) {
    var colors = new Array();
    if (total <= 10) {
        color = d3.scale.category10()
    } else if (total <= 20) {
        color = d3.scale.category20();
    } else {
        var x = 360 / (total);
        color = function(t) {
            return d3.hcl(t * x, 100, 55);
        };
    }
    for (var i = 0; i < total; i++) {
        colors.push(color(i));
    }
    return colors;
}

function getBeanKindShort(beanKinds, beanKindsShort, beanKind) {
    for (var i = 0; i < beanKinds.length; i++) {
        if (beanKinds[i] == beanKind) {
            return beanKindsShort[i];
        }
    }
    return null;
}
