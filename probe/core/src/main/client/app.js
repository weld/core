/*
 * TODO comments
 */
var restUrlBase = '${rest.url.base}';

var beanKinds = [ 'MANAGED', 'SESSION', 'PRODUCER_METHOD', 'PRODUCER_FIELD',
    'RESOURCE', 'SYNTHETIC', 'INTERCEPTOR', 'DECORATOR', 'EXTENSION',
    'BUILT_IN' ];

var receptions = [ 'ALWAYS', 'IF_EXISTS' ];

var txPhases = [ 'IN_PROGRESS', 'BEFORE_COMPLETION', 'AFTER_COMPLETION',
    'AFTER_FAILURE', 'AFTER_SUCCESS' ];

var cache = new Object();

var Probe = Ember.Application.create({
// LOG_TRANSITIONS : true,
// LOG_TRANSITIONS_INTERNAL : true,
// LOG_BINDINGS : true,
// LOG_VIEW_LOOKUPS : true,
// LOG_STACKTRACE_ON_DEPRECATION : true,
// LOG_VERSION : true,
// debugMode : true
});

Probe.Router.map(function() {
  this.route('deployment', {
    path : '/'
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
  this.route('contexts', {
    path : '/contexts'
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
});

// VIEWS

Probe.ApplicationView = Ember.View.extend({
  currentPathDidChange : function() {
    // Workaround to highlight the active tab
    Ember.run.next(this, function() {
      this.$("ul.nav li:has(>a.active)").addClass('active');
      this.$("ul.nav li:not(:has(>a.active))").removeClass('active');
    });
  }.observes('controller.currentPath')
});

// ROUTES

/*
 * NOTE: this route is always initialized!
 */
Probe.ApplicationRoute = Ember.Route.extend({
  model : function() {
    return $.getJSON(restUrlBase + 'deployment').done(function(data) {
      cache.bdas = data.bdas;
      cache.configuration = data.configuration;
      return data;
    }).fail(function(jqXHR, textStatus, errorThrown) {
      alert('Unable to get JSON data: ' + textStatus);
    });
  }
});

Probe.DeploymentRoute = Ember.Route.extend({
  model : function() {
    return cache;
  }
});

Probe.BeanListRoute = Ember.Route.extend({
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
    filters = appendToFilters(filters, 'bda', params.bda);
    filters = appendToFilters(filters, 'scope', params.scope);
    filters = appendToFilters(filters, 'beanClass', params.beanClass);
    filters = appendToFilters(filters, 'beanType', params.beanType);
    filters = appendToFilters(filters, 'qualifier', params.qualifier);
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

Probe.BeanDetailRoute = Ember.Route.extend({
  setupController : function(controller, model) {
    this._super(controller, model);
    // A bean kind css class binding
    controller.set("kindClass", model.kind + ' boxed');
  },
  model : function(params) {
    this.set("beanId", params.id);
    return $.getJSON(restUrlBase + 'beans/' + params.id).done(
        function(data) {
          data.bdaIdName = findBeanDeploymentArchive(cache.bdas,
              data['bdaId']);
          buildDependencyGraphData(data, params.id)
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
      buildDependencyGraphData(data, data.id);
      this.controller.set('routeRefresh', new Date());
    }
  }
});

Probe.ObserverListRoute = Ember.Route
    .extend({
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
      },
      setupController : function(controller, model) {
        this._super(controller, model);
        controller.set("pages", buildPages(model.page, model.lastPage));
      },
      model : function(params) {
        var query = '', filters = '', page = '';
        filters = appendToFilters(filters, 'beanClass',
            params.beanClass);
        filters = appendToFilters(filters, 'observedType',
            params.observedType);
        filters = appendToFilters(filters, 'qualifier',
            params.qualifier);
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
        if (params.txPhase) {
          txPhases.forEach(function(txPhase) {
            if (txPhase == params.txPhase) {
              filters = appendToFilters(filters, 'txPhase',
                  txPhase);
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

Probe.ObserverDetailRoute = Ember.Route.extend({
  model : function(params) {
    return $.getJSON(restUrlBase + 'observers/' + params.id).done(
        function(data) {
          return data;
        }).fail(function(jqXHR, textStatus, errorThrown) {
      alert('Unable to get JSON data: ' + textStatus);
    });
  }
});

Probe.ContextsRoute = Ember.Route.extend({
  model : function() {
    return $.getJSON(restUrlBase + 'contexts').done(function(data) {
      return data;
    }).fail(function(jqXHR, textStatus, errorThrown) {
      alert('Unable to get JSON data: ' + textStatus);
    });
  },
  actions : {
    refreshContexts : function() {
      this.refresh();
    }
  }
});

Probe.ContextInstanceRoute = Ember.Route.extend({
  model : function(params) {
    return $.getJSON(restUrlBase + 'beans/' + params.id + '/instance')
        .done(function(data) {
          return data;
        }).fail(function(jqXHR, textStatus, errorThrown) {
          alert('Unable to get JSON data: ' + textStatus);
        });
  }
});

Probe.InvocationListRoute = Ember.Route.extend({
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
    query = appendToQuery(query, 'filters', filters);
    if (params.page) {
      query = appendToQuery(query, 'page', params.page);
    }
    return $.getJSON(restUrlBase + 'invocations' + query).done(
        function(data) {
          data.data.forEach(function(invocation) {
            invocation['time'] = (invocation['time'] / 1000000)
                .toFixed(3);
            invocation['start'] = moment(invocation['start'])
                .format('YYYY-MM-DD HH:mm:ss.SSS');
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

Probe.InvocationDetailRoute = Ember.Route.extend({
  model : function(params) {
    return $.getJSON(restUrlBase + 'invocations/' + params.id).done(
        function(data) {
          data.transformed = getRootNode(data, null);
        }).fail(function(jqXHR, textStatus, errorThrown) {
      alert('Unable to get JSON data: ' + textStatus);
    });
  }
});

// CONTROLLERS

Probe.DeploymentController = Ember.ObjectController.extend({});

Probe.BeanListController = Ember.ObjectController.extend({
  init : function() {
    this._super();
    this.set('initialized', true);
    this.set('beanKinds', beanKinds);
  },
  bda : '',
  bdaId : '',
  kind : null,
  scope : '',
  beanClass : '',
  beanType : '',
  qualifier : '',
  page : 1,
  queryParams : [ 'bda', 'kind', 'scope', 'beanClass', 'beanType',
      'qualifier', 'page' ],
  onBdaChanged : function() {
    var newBda = this.get('bda');
    if (newBda != null && newBda != undefined && newBda != '') {
      this.set('bdaId', findBeanDeploymentArchive(cache.bdas, this
          .get('bda')));
    }
  }.observes('bda'),
  actions : {
    clearFilters : function() {
      this.set('bda', '');
      this.set('bdaId', '');
      this.set('scope', '');
      this.set('beanClass', '');
      this.set('beanType', '');
      this.set('qualifier', '');
      this.set('kind', null);
      this.send("refreshData");
    },
    clearBda : function() {
      this.set('bda', '');
      this.set('bdaId', null);
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
  },
  observedType : '',
  beanClass : '',
  reception : null,
  txPhase : null,
  qualifier : '',
  declaringBean : '',
  page : 1,
  queryParams : [ 'observedType', 'beanClass', 'reception', 'txPhase',
      'declaringBean', 'qualifier', 'page' ],
  actions : {
    clearFilters : function() {
      this.set('observedType', '');
      this.set('beanClass', '');
      this.set('reception', null);
      this.set('txPhase', null);
      this.set('declaringBean', '');
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
  page : 1,
  queryParams : [ 'beanClass', 'methodName', 'page' ],
  actions : {
    clearFilters : function() {
      this.set('beanClass', '');
      this.set('methodName', '');
      this.send('refreshData');
    },
    filter : function() {
      this.send('refreshData');
    }
  },
});

Probe.InvocationDetailController = Ember.ObjectController.extend({});

// HELPERS

Ember.Handlebars.registerBoundHelper('increment', function(integer) {
  return integer + 1;
});

Ember.Handlebars.registerBoundHelper('eachItemOnNewLine', function(items,
    options) {
  var ret = '';
  if (items) {
    for (var i = 0; i < items.length; i++) {
      ret += Handlebars.Utils.escapeExpression(items[i]) + '<br/>';
    }
  }
  return new Handlebars.SafeString(ret);
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
        // TODO auto width
        // var width = 1280 - margin.right - margin.left;
        var width = 1280;
        var height = 600 - margin.top - margin.bottom;

        var nodes = d3.values(data.nodes);
        var links = data.links;

        // D3 force layout
        var force = d3.layout.force().nodes(nodes).links(links).size(
            [ width, height ]).gravity(.05).linkDistance(450)
            .charge(-500).on("tick", tick).start();

        var elementId = this.get('elementId');
        var element = d3.select('#' + elementId);
        var svg = element.append("svg").attr("height",
            height + margin.top + margin.bottom).attr("width",
            width);

        // Type markers
        svg.append("defs").selectAll("marker").data(
            [ "dependency", "dependent" ]).enter().append("marker")
            .attr("id", function(d) {
              return d;
            }).attr("viewBox", "0 -5 10 10").attr("refX", 20).attr(
                "refY", 0).attr("markerWidth", 6).attr(
                "markerHeight", 6).attr("orient", "auto")
            .append("path").attr("d", "M0,-5L10,0L0,5");

        // Links - lines
        var link = svg.selectAll("line.link").data(links).enter()
            .append("svg:line").attr("class", "link").attr("x1",
                function(d) {
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
          linkLabel.append("circle").attr("r", 8).style("fill",
              "silver");
          linkLabel
              .append("title")
              .text(
                  function(d) {
                    if (d.dependencies.length == 1) {
                      return getInjectionPointInfo(d,
                          false);
                    } else {
                      return 'Multiple injection points found, click to show details.';
                    }
                    return getInjectionPointInfo(d);
                  });
          linkLabel.append("svg:text").attr("class",
              "nodetext injection-point-info").style("fill",
              "black").style("font-size", "90%").each(
              function(d) {
                var text = d3.select(this);
                if (!d.dependencies) {
                  return;
                }
                var desc;
                if (d.dependencies.length == 1) {
                  desc = d.dependencies[0].requiredType;
                } else {
                  desc = '(' + d.dependencies.length + ')';
                }
                text.append("tspan").attr("x", 10).attr("dy",
                    15).attr(
                    "text-anchor",
                    function() {
                      if (!d.source.isDependent
                          && d.target.isRoot) {
                        return "end";
                      }
                    }).text(desc);
              });
        }

        var node_drag = d3.behavior.drag().on("dragstart", dragstart)
            .on("drag", dragmove).on("dragend", dragend);

        function dragstart(d, i) {
          // stops the force auto positioning before you start
          // dragging
          force.stop()
        }

        function dragmove(d, i) {
          d.px += d3.event.dx;
          d.py += d3.event.dy;
          d.x += d3.event.dx;
          d.y += d3.event.dy;
          // this is the key to make it work together with updating
          // both px,py,x,y on d !
          tick();
        }

        function dragend(d, i) {
          // set the node to fixed so the force doesn't include the
          // node in its auto positioning stuff
          d.fixed = true;
          tick();
          force.resume();
        }

        // Injection point info dialog
        svg.selectAll("text.injection-point-info").on("click",
            function(d, i) {
              // TODO use modal dialog
              alert(getInjectionPointInfo(d, true));
            });

        var node = svg.selectAll("g.node").data(nodes).enter().append(
            "svg:g").attr("class", "node").call(node_drag);

        node.append("title").text(function(d) {
          return d.kind;
        });
        node.append("circle").attr("r", 12).attr(
            "class",
            function(d) {
              if (d.isRoot) {
                return "circle-root";
              } else if (d.kind == 'PRODUCER_METHOD'
                  || d.kind == 'PRODUCER_FIELD'
                  || d.kind == 'RESOURCE') {
                return "circle-producer";
              }
              return "circle-regular";
            });

        // node.append("svg:text").attr("class", "nodetext")
        // .attr("dx", 15).attr("dy", "-1em")
        // .style("fill", "gray").text(function(d) {
        // return d.kind;
        // });

        node.append("a").attr("xlink:href", function(d) {
          return "#/bean/" + d.id;
        }).append("svg:text").attr("dx", 15).attr("dy", "0.2em").style(
            "fill", "#428bca").text(function(d) {
          return d.beanClass;
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
            linkLabel
                .attr(
                    "transform",
                    function(d) {
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

function getInjectionPointInfo(d, addIndex) {
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
    if (addIndex) {
      description += (j + 1) + '. ';
    }
    description += qualifiers;
    description += ' ' + requiredType + '\n';
    return description;
  }
}

Probe.InvocationTree = Ember.View
    .extend({

      didInsertElement : function() {

        var data = this.get('content.transformed');
        if (!data) {
          alert("No data to render!");
        }

        var margin = {
          top : 120,
          right : 120,
          bottom : 20,
          left : 120
        }, width = 1280 - margin.right - margin.left, height = 1600
            - margin.top - margin.bottom;
        // TODO height should be set dynamically

        var i = 0;
        var tree = d3.layout.tree().size([ height, width ]);

        var elementId = this.get('elementId');
        var element = d3.select('#' + elementId);
        var svg = element.append("svg").attr("height",
            height + margin.top + margin.bottom);

        // Type markers
        svg.append("defs").selectAll("marker").data([ "invocation" ])
            .enter().append("marker").attr("id", function(d) {
              return d;
            }).attr("viewBox", "0 -5 10 10").attr("refX", 14).attr(
                "refY", 0).attr("markerWidth", 6).attr(
                "markerHeight", 6).attr("orient", "auto")
            .style("fill", "gray").append("path").attr("d",
                "M0,-5L7,0L0,5");

        var g = svg.append("g").attr("transform",
            "translate(" + margin.left + "," + margin.top + ")");

        // Compute the new tree layout
        var nodes = tree.nodes(data).reverse(), links = tree
            .links(nodes);
        var idx = nodes.length;

        // Normalize for fixed-depth.
        nodes.forEach(function(d) {
          idx--;
          if (d.depth == 0) {
            d.y = 40;
            d.x = 40;
          } else {
            d.y = (d.depth * 250) + 20;
            d.x = (idx * 80) + 60;
          }
        });

        // Declare the nodes
        var node = svg.selectAll("g.node").data(nodes, function(d) {
          return d.id || (d.id = ++i);
        });

        // Enter the nodes
        var nodeEnter = node.enter().append("g").attr("class", "node")
            .attr("transform", function(d) {
              return "translate(" + d.y + "," + d.x + ")";
            });

        nodeEnter.append("circle").attr("r", 9).attr("class",
            function(d) {
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

        nodeEnter
            .append("a")
            .attr(
                "xlink:href",
                function(d) {
                  return d.interceptedBean ? '#/bean/'
                      + d.interceptedBean.id : '';
                })
            .append("text")
            .attr("dy", -14)
            .style("fill", "#428bca")
            .text(
                function(d) {
                  return d.interceptedBean ? d.interceptedBean.beanClass
                      : '';
                });

        // Declare the links
        var link = svg.selectAll("path.link").data(links, function(d) {
          return d.target.id;
        });

        var linkLabel = svg.selectAll("g.link-label").data(links)
            .enter().append("svg:g").attr("class", "labelText");
        linkLabel.append("circle").attr("r", 5).style("fill", "silver");
        linkLabel.append("svg:text").attr("class", "nodetext").style(
            "fill", "black").attr("x", "0.6em").attr("dy", "1.3em")
            .style("font-size", "90%").style("font-weight",
                "normal").each(
                function(d) {
                  var text = d3.select(this).text(
                      d.target.start + " ("
                          + d.target.duration
                          + ' ms)');
                });
        linkLabel.append("svg:text").attr("class", "nodetext").style(
            "fill", "black").attr("x", "0.6em")
            .attr("dy", "-0.6em").style("font-size", "90%").style(
                "font-weight", "bold").each(
                function(d) {
                  var text = d3.select(this).text(
                      d.target.methodName + '()');
                });

        linkLabel.attr("transform", function(d) {
          return "translate(" + d.source.y + "," + d.target.x + ")";
        });

        // Enter the links
        link.enter().insert("path", "g").attr(
            "d",
            function(d) {
              return "M" + d.source.y + "," + d.source.x + "L"
                  + d.source.y + "," + d.target.x + "L"
                  + d.target.y + "," + d.target.x;
            }).style(
            "stroke-dasharray",
            function(d) {
              if (d.target.type == "PRODUCER"
                  || d.target.type == "DISPOSER"
                  || d.target.type == "OBSERVER") {
                return "5,5";
              }
            }).attr("marker-end", function(d) {
          return "url(#invocation)";
        }).attr(
            "class",
            function(d) {
              if (d.target.type == "PRODUCER"
                  || d.target.type == "DISPOSER") {
                return "stroke-producer link";
              } else if (d.target.type == "OBSERVER") {
                return "stroke-observer link";
              } else {
                return "link";
              }
            });

      }
    });

// UTILS

function findBeanDeploymentArchive(bdas, id) {
  if (bdas) {
    for (var i = 0; i < bdas.length; i++) {
      if (bdas[i].id == id) {
        return bdas[i].bdaId;
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

function findNodesDependencies(bean, nodes, rootId) {
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
      nodes[bean.id].x = 150;
      nodes[bean.id].y = 150;
    }
  }
  if (bean.dependencies) {
    bean.dependencies.forEach(function(dependency) {
      findNodesDependencies(dependency, nodes);
    });
  }
}

function findLinksDependencies(bean, links, nodes) {
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
      findLinksDependencies(dependency, links, nodes);
    });
  }
}

function findNodesDependents(bean, nodes, rootId) {
  if (!nodes[bean.id]) {
    nodes[bean.id] = {
      id : bean.id,
      beanClass : bean.beanClass,
      kind : bean.kind,
      isDependent : true
    };
  }
  if (bean.dependents) {
    bean.dependents.forEach(function(dependent) {
      findNodesDependents(dependent, nodes);
    });
  }
}

function findLinksDependents(bean, links, nodes) {
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
      findLinksDependents(dependent, links, nodes);
    });
  }
}

/**
 *
 * @param data
 *            BeanDetailRoute data
 */
function buildDependencyGraphData(data, id) {
  // Create nodes
  var nodes = new Object();
  findNodesDependencies(data, nodes, id);
  findNodesDependents(data, nodes, id);
  // Create links
  var links = new Array();
  findLinksDependencies(data, links, nodes);
  findLinksDependents(data, links, nodes);
  data.nodes = nodes;
  data.links = links;
  console.log('Build dependency graph data [links: ' + links.length + ']');
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