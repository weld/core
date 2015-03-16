/*!
 * Weld Probe ${project.version}
 * Copyright 2015, Red Hat, Inc.
 * Licensed the Apache License, Version 2.0 (http://www.apache.org/licenses/LICENSE-2.0)
 */
var restUrlBase = '${rest.url.base}';

var beanKinds = [ 'MANAGED', 'SESSION', 'PRODUCER_METHOD', 'PRODUCER_FIELD',
        'RESOURCE', 'SYNTHETIC', 'INTERCEPTOR', 'DECORATOR', 'EXTENSION',
        'BUILT_IN' ];

var observerDeclaringBeanKinds = [ 'MANAGED', 'SESSION', 'EXTENSION',
        'BUILT_IN' ];

var eventKinds = [ 'APPLICATION', 'CONTAINER' ];

var receptions = [ 'ALWAYS', 'IF_EXISTS' ];

var txPhases = [ 'IN_PROGRESS', 'BEFORE_COMPLETION', 'AFTER_COMPLETION',
        'AFTER_FAILURE', 'AFTER_SUCCESS' ];

var additionalBdaSuffix = '.additionalClasses';

var markerFilterAddBdas = "probe-filterAdditionalBdas";

var cache = new Object();

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
    this.route('beanArchives', {
        path : '/'
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
            return $
                .getJSON(restUrlBase + 'deployment')
                .done(
                    function(data) {
                        cache.bdas = data.bdas;
                        cache.bdas.forEach(function(bda, index, array) {
                            bda.selected = true;
                        });
                        // Copy bdas for filtering purpose
                        cache.filterBdas = cache.bdas.slice(0);
                        // Add marker to filter additional archives
                        cache.filterBdas
                            .unshift({
                                "id" : markerFilterAddBdas,
                                "bdaId" : "Only application bean archives - filter out beans from additional bean archives"
                            });
                        cache.configuration = data.configuration;
                        cache.configuration
                            .forEach(function(item, index, array) {
                                if (item.value != item.defaultValue) {
                                    item.changed = true;
                                }
                            });
                        return data;
                    }).fail(function(jqXHR, textStatus, errorThrown) {
                    alert('Unable to get JSON data: ' + textStatus);
                });
        }
    });

Probe.BeanArchivesRoute = Ember.Route.extend({
    model : function() {
        var data = new Object();
        var hideAddBda = this.get('hideAddBda');
        if (hideAddBda == null) {
            hideAddBda = true;
        }
        data.filteredBdas = new Array();
        cache.bdas.forEach(function(bda, index, array) {
            if (!hideAddBda || !isAdditionalBda(bda.bdaId)) {
                data.filteredBdas.push(bda);
            }
        });
        buildBdaGraphData(data, hideAddBda);
        return data;
    },
    actions : {
        settingHasChanged : function() {
            this.set("hideAddBda", this.controller.get('hideAddBda'));
            this.refresh();
            this.controller.set('routeRefresh', new Date());
        },
        selectAll : function() {
            cache.bdas.forEach(function(bda, index, array) {
                bda.selected = true;
            });
            this.send('settingHasChanged');
        },
        selectNone : function() {
            cache.bdas.forEach(function(bda, index, array) {
                bda.selected = false;
            });
            this.send('settingHasChanged');
        },
        invertSelection : function() {
            cache.bdas.forEach(function(bda, index, array) {
                bda.selected = !bda.selected;
            });
            this.send('settingHasChanged');
        }
    }
});

Probe.BeanArchiveRoute = Ember.Route.extend({
    model : function(params) {
        return findBeanDeploymentArchive(cache.bdas, params.id);
    }
});

Probe.ConfigurationRoute = Ember.Route.extend({
    model : function() {
        return cache;
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
        }
    },
    setupController : function(controller, model) {
        this._super(controller, model);
        controller.set("pages", buildPages(model.page, model.lastPage));
        controller.set("cache", cache);
    },
    model : function(params) {
        var query = '', filters = '', page = '';
        filters = appendToFilters(filters, 'scope', params.scope);
        filters = appendToFilters(filters, 'beanClass', params.beanClass);
        filters = appendToFilters(filters, 'beanType', params.beanType);
        filters = appendToFilters(filters, 'qualifier', params.qualifier);
        if (params.bda) {
            cache.filterBdas.forEach(function(bda) {
                if (bda.id == params.bda) {
                    filters = appendToFilters(filters, 'bda', bda.id);
                }
            });
        }
        if (params.kind) {
            beanKinds.forEach(function(kind) {
                if (kind == params.kind) {
                    filters = appendToFilters(filters, 'kind', kind);
                }
            });
        }
        query = appendToQuery(query, 'filters', filters);
        if (params.page) {
            query = appendToQuery(query, 'page', params.page);
        }
        return $.getJSON(restUrlBase + 'beans' + query).done(function(data) {
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
    setupController : function(controller, model) {
        this._super(controller, model);
        // A bean kind css class binding
        controller.set("kindClass", model.kind + ' boxed');
    },
    model : function(params) {
        this.set("beanId", params.id);
        if (this.get("transientDependencies")) {
            this.set("transientDependencies", true);
        }
        if (this.get("transientDependents")) {
            this.set("transientDependents", true);
        }
        var route = this;
        return $.getJSON(
            restUrlBase + 'beans/' + params.id
                + '?transientDependencies=true&transientDependents=true').done(
            function(data) {
                data.bdaIdName = findBeanDeploymentArchiveId(cache.bdas,
                    data['bdaId']);
                buildDependencyGraphData(data, params.id, route
                    .get("transientDependencies"), route
                    .get("transientDependents"))
                return data;
            }).fail(function(jqXHR, textStatus, errorThrown) {
            alert('Unable to get JSON data: ' + textStatus);
        });
    },
    actions : {
        settingHasChanged : function() {
            this.set("transientDependencies", this.controller
                .get('transientDependencies'));
            this.set("transientDependents", this.controller
                .get('transientDependents'));
            // Rebuild dependency graph data
            var data = this.get('controller.model');
            buildDependencyGraphData(data, data.id, this.controller
                .get('transientDependencies'), this.controller
                .get('transientDependents'));
            this.controller.set('routeRefresh', new Date());
        }
    }
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
            controller.set("cache", cache);
        },
        model : function(params) {
            var query = '', filters = '', page = '';
            filters = appendToFilters(filters, 'beanClass', params.beanClass);
            filters = appendToFilters(filters, 'observedType',
                params.observedType);
            filters = appendToFilters(filters, 'qualifier', params.qualifier);
            filters = appendToFilters(filters, 'declaringBean',
                params.declaringBean);
            if (params.reception) {
                receptions.forEach(function(reception) {
                    if (reception == params.reception) {
                        filters = appendToFilters(filters, 'reception',
                            reception);
                    }
                });
            }
            if (params.kind) {
                beanKinds.forEach(function(kind) {
                    if (kind == params.kind) {
                        filters = appendToFilters(filters, 'kind', kind);
                    }
                });
            }
            if (params.txPhase) {
                txPhases.forEach(function(txPhase) {
                    if (txPhase == params.txPhase) {
                        filters = appendToFilters(filters, 'txPhase', txPhase);
                    }
                });
            }
            if (params.bda) {
                cache.filterBdas.forEach(function(bda) {
                    if (bda.id == params.bda) {
                        filters = appendToFilters(filters, 'bda', bda.id);
                    }
                });
            }
            query = appendToQuery(query, 'filters', filters);
            if (params.page) {
                query = appendToQuery(query, 'page', params.page);
            }
            return $.getJSON(restUrlBase + 'observers' + query).done(
                function(data) {
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
        return $.getJSON(restUrlBase + 'observers/' + params.id).done(
            function(data) {
                return data;
            }).fail(function(jqXHR, textStatus, errorThrown) {
            alert('Unable to get JSON data: ' + textStatus);
        });
    }
});

Probe.ContextRoute = Ember.Route.extend(Probe.ResetScroll, {
    model : function(params) {
        var url = restUrlBase + 'contexts/' + params.id;
        if (this.get('cid')) {
            url += '?cid=' + this.get('cid');
        }
        return $.getJSON(url).done(function(data) {
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
        var url = restUrlBase + 'beans/' + params.id + '/instance';
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
        var query = '', filters = '', page = '';
        filters = appendToFilters(filters, 'beanClass', params.beanClass);
        filters = appendToFilters(filters, 'methodName', params.methodName);
        filters = appendToFilters(filters, 'search', params.search);
        query = appendToQuery(query, 'filters', filters);
        if (params.page) {
            query = appendToQuery(query, 'page', params.page);
        }
        return $.getJSON(restUrlBase + 'invocations' + query).done(
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
            $.ajax(restUrlBase + 'invocations', {
                'type' : 'DELETE'
            }).then(function(data) {
                route.refresh();
            });
        },
    }
});

Probe.InvocationDetailRoute = Ember.Route.extend(Probe.ResetScroll, {
    model : function(params) {
        return $.getJSON(restUrlBase + 'invocations/' + params.id).done(
            function(data) {
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
        var query = '', filters = '', page = '';
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
        return $.getJSON(restUrlBase + 'events' + query).done(
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
            $.ajax(restUrlBase + 'events', {
                'type' : 'DELETE'
            }).then(function(data) {
                route.refresh();
            });
        },
    }
});

// CONTROLLERS

Probe.BeanArchivesController = Ember.ObjectController.extend({
    hideAddBda : true,
    onSettingsChanged : function() {
        this.send("settingHasChanged");
    }.observes('hideAddBda')
});

Probe.BeanArchiveController = Ember.ObjectController.extend({});

Probe.ConfigurationController = Ember.ObjectController.extend({});

Probe.BeanListController = Ember.ObjectController.extend({
    init : function() {
        this._super();
        this.set('initialized', true);
        this.set('beanKinds', beanKinds);
    },
    bda : markerFilterAddBdas,
    kind : null,
    scope : '',
    beanClass : '',
    beanType : '',
    qualifier : '',
    page : 1,
    queryParams : [ 'bda', 'kind', 'scope', 'beanClass', 'beanType',
            'qualifier', 'page' ],
    actions : {
        clearFilters : function() {
            this.set('page', 1);
            this.set('bda', null);
            this.set('scope', '');
            this.set('beanClass', '');
            this.set('beanType', '');
            this.set('qualifier', '');
            this.set('kind', null);
            this.send("refreshData");
        },
        filter : function() {
            this.send('refreshData');
        }
    },
});

Probe.BeanDetailController = Ember.ObjectController.extend({
    transientDependencies : true,
    transientDependents : false,
    injectionPointInfo : true,
    onSettingsChanged : function() {
        this.send("settingHasChanged");
    }.observes('transientDependencies', 'transientDependents',
        'injectionPointInfo')
});

Probe.ObserverListController = Ember.ObjectController.extend({
    init : function() {
        this._super();
        this.set('initialized', true);
        this.set('receptions', receptions);
        this.set('txPhases', txPhases);
        this.set('beanKinds', observerDeclaringBeanKinds);
    },
    bda : markerFilterAddBdas,
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

Probe.InvocationListController = Ember.ObjectController.extend({
    init : function() {
        this._super();
        this.set('initialized', true);
    },
    beanClass : '',
    methodName : '',
    search : '',
    page : 1,
    queryParams : [ 'beanClass', 'methodName', 'search', 'page' ],
    actions : {
        clearFilters : function() {
            this.set('page', 1);
            this.set('beanClass', '');
            this.set('methodName', '');
            this.set('search', '');
            this.send('refreshData');
        },
        filter : function() {
            this.send('refreshData');
        }
    },
});

Probe.InvocationDetailController = Ember.ObjectController.extend({});

Probe.EventsController = Ember.ObjectController.extend({
    init : function() {
        this._super();
        this.set('initialized', true);
        this.set('eventKinds', eventKinds);
    },
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

// HELPERS

Ember.Handlebars.registerBoundHelper('increment', function(integer) {
    return integer + 1;
});

Ember.Handlebars.registerBoundHelper('at', function() {
    return '@';
});

Ember.Handlebars.registerBoundHelper('substr', function(text, limit) {
    if (text.length > limit) {
        return abbreviate(text, limit);
    } else {
        return text;
    }
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
 * specify optional hash arguments: title and suppressHtml.
 */
Ember.Handlebars.registerBoundHelper('abbr', function(text, limit, options) {
    var addTitle = options.hash.title || true;
    var suppresshtmlOutput = options.hash.suppressHtml || false;
    var escaped = Handlebars.Utils.escapeExpression(text);
    if (escaped.length > limit) {
        var ret = '';
        ret += escaped.charAt(0) === '@' ? abbreviateAnnotation(escaped,
            !suppresshtmlOutput, addTitle) : abbreviateType(escaped,
            !suppresshtmlOutput, addTitle);
        escaped = ret;
    }
    return new Handlebars.SafeString(escaped);
});

// VIEWS

Probe.DependencyGraph = Ember.View
    .extend({

        dataChanged : function() {
            this.rerender();
        }.observes('beanId', 'routeRefresh'),

        didInsertElement : function() {

            var injectionPointInfo = this.get('controller').get(
                "injectionPointInfo");

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
            var height = 800 - margin.top - margin.bottom;

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
                [ "dependency", "dependent" ]).enter().append("marker").attr(
                "id", function(d) {
                    return d;
                }).attr("viewBox", "0 -5 10 10").attr("refX", 20).attr("refY",
                0).attr("markerWidth", 6).attr("markerHeight", 6).attr(
                "orient", "auto").append("path").attr("d", "M0,-5L10,0L0,5");

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
                }
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
                            if (d.dependencies.length == 1) {
                                return getInjectionPointInfo(d, false);
                            } else {
                                return 'Multiple injection points found, click to show details.';
                            }
                        });
                linkLabel.append("svg:text").attr("class",
                    "nodetext injection-point-info").style("fill", "black")
                    .style("font-size", "90%").each(
                        function(d) {
                            var text = d3.select(this);
                            if (!d.dependencies) {
                                return;
                            }
                            var desc;
                            if (d.dependencies.length == 1) {
                                desc = abbreviateType(
                                    d.dependencies[0].requiredType, false,
                                    false);
                            } else {
                                desc = '(' + d.dependencies.length + ')';
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
                        getInjectionPointInfoHtml(d, true));
                    $('div#ipInfoModal').modal('show');
                });

            var node = svg.selectAll("g.node").data(nodes).enter().append(
                "svg:g").attr("class", "node").call(node_drag);

            node.append("title").text(function(d) {
                return d.beanClass;
            });
            node.append("circle").attr("r", 12).attr(
                "class",
                function(d) {
                    if (d.isRoot) {
                        return "circle-root";
                    } else if (d.kind == 'PRODUCER_METHOD'
                        || d.kind == 'PRODUCER_FIELD' || d.kind == 'RESOURCE') {
                        return "circle-producer";
                    }
                    return "circle-regular";
                });

            node.append("a").attr("xlink:href", function(d) {
                return "#/bean/" + d.id;
            }).append("svg:text").attr("dx", 15).attr("dy", "0.2em").style(
                "fill", "#428bca").text(function(d) {
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
    var description = '';
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
        description += qualifiers;
        description += ' ' + requiredType;
        return description;
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
            if (d.type == "circle-observer") {
                return "orange";
            }
            return "circle-regular";
        });

        nodeEnter.filter(function(d) {
            return d.interceptedBean;
        }).append("a").attr("xlink:href", function(d) {
            return '#/bean/' + d.interceptedBean.id;
        }).append("text").attr("dx", 12).attr("dy", -5).style("fill",
            "#428bca").text(function(d) {
            return d.interceptedBeanClass;
        });

        nodeEnter.filter(function(d) {
            return d.declaringClass;
        }).append("text").attr("dx", 12).attr("dy", -5).text(function(d) {
            return d.declaringClass;
        });

        nodeEnter.append("text").attr("dx", 12).attr("dy", 12).style("fill",
            "black").style("font-size", "90%").text(function(d) {
            return d.methodName ? '#' + d.methodName : '';
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
                    || d.target.type == "OBSERVER") {
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

    dataChanged : function() {
        this.rerender();
    }.observes('routeRefresh'),

    didInsertElement : function() {

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

        node.append("circle").attr("r", 16).attr("class", function(d) {
            return isAdditionalBda(d.bdaId) ? "circle-bda-add" : "circle-bda";
        });

        var text = node.append("svg:text").attr("dx", function(d) {
            return d.idx > 8 ? "-10" : "-5";
        }).attr("dy", "5").style("fill", "black").text(function(d) {
            return d.idx + 1;
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
    filters += key + ':' + value;
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

function findNodesDependencies(bean, nodes, rootId, transientDependencies) {
    if (!nodes[bean.id]) {
        nodes[bean.id] = {
            id : bean.id,
            beanClass : bean.beanClass,
            kind : bean.kind,
            // Root is always found in dependencies
            isRoot : (bean.id == rootId),
            fixed : (bean.id == rootId)
        };
        if (nodes[bean.id].isRoot) {
            nodes[bean.id].x = 300;
            nodes[bean.id].y = 100;
        }
    }
    if (rootId == null && !transientDependencies) {
        return;
    }
    if (bean.dependencies) {
        bean.dependencies.forEach(function(dependency) {
            findNodesDependencies(dependency, nodes, null,
                transientDependencies);
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
                    type : 'dependency',
                    dependencies : [ info ],
                });
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
            isDependent : true
        };
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
                    type : 'dependent',
                    dependencies : [ info ],
                });
            }
            if (transientDependents) {
                findLinksDependents(dependent, links, nodes,
                    transientDependents);
            }
        });
    }
}

/**
 *
 * @param data
 *            BeanDetailRoute data
 */
function buildDependencyGraphData(data, id, transientDependencies,
    transientDependents) {
    // Create nodes
    var nodes = new Object();
    findNodesDependencies(data, nodes, id, transientDependencies);
    findNodesDependents(data, nodes, id, transientDependents);
    // Create links
    var links = new Array();
    findLinksDependencies(data, links, nodes, transientDependencies);
    findLinksDependents(data, links, nodes, transientDependents);
    data.nodes = nodes;
    data.links = links;
    console.log('Build dependency graph data [links: ' + links.length + ']');
}

/**
 *
 * @param data
 *            BeanArchivesRoute data
 * @param hideAddBda
 */
function buildBdaGraphData(data, hideAddBda) {
    // Create nodes
    var nodes = new Object();
    findNodesBdas(data.filteredBdas, nodes);
    // Create links
    var links = new Array();
    findLinksBdas(data.filteredBdas, links, nodes, hideAddBda);
    data.nodes = nodes;
    data.links = links;
    console.log('Build BDA graph data [links: ' + links.length + ']');
}

function findNodesBdas(bdas, nodes) {
    if (bdas) {
        bdas.forEach(function(bda, index, array) {
            if (bda.selected && !nodes[bda.id]) {
                nodes[bda.id] = {
                    id : bda.id,
                    bdaId : bda.bdaId,
                    idx : index,
                }
            }
        });
    }
}

function findLinksBdas(bdas, links, nodes, hideAddBda) {
    if (bdas) {
        bdas
            .forEach(function(bda) {
                if (bda.selected && bda.accessibleBdas) {
                    bda.accessibleBdas
                        .forEach(function(accessible) {
                            if (!findBeanDeploymentArchive(cache.bdas,
                                accessible).selected
                                || bda.id == accessible
                                || (hideAddBda && isAdditionalBda(findBeanDeploymentArchiveId(
                                    cache.bdas, accessible)))) {
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

function abbreviate(text, limit) {
    var start = text.length - limit + 3;
    var end = text.length;
    return '...' + text.substring(start, end);
}

function isAdditionalBda(bdaId) {
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
 * @returns {String}
 */
function abbreviateType(type, htmlOutput, title) {
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
        ret += ' <i class="fa fa-compress abbreviated"></i>';
    }
    return ret;
}

/**
 * This only returns the abbreviated annotation type, params are omitted.
 *
 * @param annotation
 * @param htmlOutput
 * @param title
 * @returns {String}
 */
function abbreviateAnnotation(annotation, htmlOutput, title) {
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
        ret += ' <i class="fa fa-compress abbreviated"></i>';
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
