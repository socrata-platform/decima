'use strict';

window._OR = ",";

function getUrlVars() {
    // Blatantly stolen from http://stackoverflow.com/questions/4656843/jquery-get-querystring-from-url for
    // reasons of expedience
    var vars = [], hash;
    var hashes = window.location.href.slice(window.location.href.indexOf('?') + 1).split('&');
    for(var i = 0; i < hashes.length; i++) {
        hash = hashes[i].split('=');
        vars.push(hash[0]);
        vars[hash[0]] = hash[1];
    }
    return vars;
}

var logicalEnvironments = [ "us-west", "eu-west", "fedramp" ];


var environmentMap = {
  "staging": "staging",
  "rc": "rc",
  "us_west_2": "us-west",
  "eu_west_1": "eu-west",
  "fedramp": "fedramp"
}

// var environmentMap = {
//   "azure_rc": "rc",
//   "rc": "rc",
//   "production": "us-west",
//   "prod": "us-west",
//   "infrastructure": "us-west",
//   "aws-us-east-1-fedramp-prod": "fedramp",
//   "aws_us_east_1_fedramp_prod": "fedramp",
//   "us-east-1-fedramp-prod": "fedramp",
//   "us_east_1_fedramp_prod": "fedramp",
//   "fedramp-prod": "fedramp",
//   "fedramp_prod": "fedramp",
//   "azure-westeurope-production": "eu-west",
//   "azure_westeurope_production": "eu-west",
//   "eu-west-1-prod": "eu-west",
//   "eu_west_1_prod": "eu-west"
// };

var environmentColMap = {
  "rc": "service-column left-column",
  "us-west": "service-column left-column",
  "eu-west": "service-column",
  "fedramp": "service-column right-column"
};

var renderTableHeader = function() {
  var template = Handlebars.compile($("#service-header").html());
  $("#services-table-header").html("");
  $("#services-table-header").append(template(logicalEnvironments));
  $("#services-table-container").stickyTableHeaders();
};

window._data = undefined;

var updateSearch = function() {
  var newSearch = $("#service-filter").val(),
      proto = window.location.protocol,
      host = window.location.host,
      path = window.location.pathname,
      newurl = proto + "//" + host + path + '?filter=' + newSearch;
  window.history.pushState({path:newurl},'',newurl);
  renderDataIntoPage();
}

var filterServices = function(services, sFilter) {
  if (typeof sFilter == "string") { sFilter = sFilter.split(_OR); }
  if (sFilter.indexOf("") > -1) { delete sFilter.splice(sFilter.indexOf(""), 1); }
  if (sFilter.length == 0) { return services; }

  var newServices = [];
  _.each(services, function(service) {
    if (sFilter.reduce(function(p,n) { return p || !!service["service_alias"].match(n); }, false)) {
      newServices.push(service)
    }
  });
  return newServices;
}

var renderDataIntoPage = function(data) {
  if (typeof data == "undefined") { data = window._data; }
  else { window._data = data; }
  var source = $("#service-template").html(),
      template = Handlebars.compile(source),
      qs = getUrlVars();

  if (qs.indexOf('filter') > -1) { data = filterServices(data, qs['filter']); }

  $("#services-table-rows").html("");

  _.each(data, function(service){
    service['env_parity'] = {}
    _.each(service['environments'], function(deps, dEnv) {
      var mappedDEnv = environmentMap[dEnv];
      if (typeof mappedDEnv != "undefined") {
        var mappendDEnvParity = _.reduce(deps, function(memo, dep) { return memo == dep.parity_status ? memo : 'version-match-error'; }, deps[0].parity_status);
        service['env_parity'][mappedDEnv] = {
          "parity": mappendDEnvParity,
          "match_class": mappendDEnvParity,
          "col_class": environmentColMap[mappedDEnv],
          "environment": mappedDEnv
        };
      } else {
        service['env_parity'][mappedDEnv] = {
          "parity": false,
          "match_class": "version-unknown",
          "col_class": environmentColMap[mappedDEnv],
          "environment": mappedDEnv
        };
      }
    });
    service['env_parity'] = _.map(logicalEnvironments, function(env) {
      if (service['env_parity'][env]) {
        return service['env_parity'][env];
      } else {
        return { "parity": false, "match_class": "version-na", "col_class": environmentColMap[env], "environment": env};
      }
    })
    $("#services-table-rows").append(template(service));
  });
}

var refreshPage = function() {
  jQuery.get("/deploy/summary", {}, renderDataIntoPage, "json");
};

$(document).ready(function() {
  var qs = getUrlVars();
  if (qs.indexOf('filter') > -1) {
    var sFilter = qs['filter'];
    if (typeof sFilter == "string") { sFilter = sFilter.split(_OR); }
    if (sFilter.indexOf("") > -1) { delete sFilter.splice(sFilter.indexOf(""), 1); }
    if (sFilter.length != 0) {
      $("#service-filter").val(sFilter.join(_OR));
      updateSearch()
    }
  }

  $("#service-filter").keypress(updateSearch);
  $("#service-filter").keyup(updateSearch);
  refreshPage();
//  setInterval(refreshPage, 30000);
});
