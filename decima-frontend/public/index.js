'use strict';

var environmentMatches = function(a,b){
  if ( a == undefined || b == undefined ) {
    return "version-unknown";
  }
  if (a.version == b.version && a.service_sha == b.service_sha && a.docker_tag == b.docker_tag) {
    return "version-match";
  }
  return "version-no-match";
};

var logicalEnvironments = [ "rc", "us-west", "eu-west", "fedramp" ];

var environmentMap = {
  "azure-rc": "rc",
  "rc": "rc",
  "production": "us-west",
  "prod": "us-west",
  "infrastructure": "us-west",
  "aws-us-east-1-fedramp-prod": "fedramp",
  "us-east-1-fedramp-prod": "fedramp",
  "fedramp-prod": "fedramp",
  "azure-westeurope-production": "eu-west",
  "eu-west-1-prod": "eu-west"
};

var environmentColMap = {
  "rc": "service-column left-column",
  "us-west": "service-column",
  "eu-west": "service-column",
  "fedramp": "service-column right-column"
};

var renderTableHeader = function() {
  console.log("Rendering table header.")
  var template = Handlebars.compile($("#service-header").html());
  $("#services-table-header").html("");
  $("#services-table-header").append(template(logicalEnvironments));
  $("#services-table-container").stickyTableHeaders();
  console.log("done with header");
};

var renderDataIntoPage = function(data) {

  // Ignore all staging deploys & deleted deploys for dashboard
  var data = _.reject(data, function(deploy) {
    var stagingRe = /staging/;
    return stagingRe.test(deploy.environment) || deploy.version == "DELETED";
  })

  // Build a hash of { service: { environment: deploy } }
  var deploys = {};
  _.each(data, function(deploy) {
    var service = deploy.service;
    if (service.match(/^soql-server/)) {
      service = "soql-server";
    } else if (service.match(/^secondary-watcher/)) {
      service = "secondary-watcher";
    }
    if (deploys[service] == undefined) {
      deploys[service] = {};
    }
    deploys[service][deploy.environment] = deploy;
  });

  // Condense into an array [ { service: service, environment: deploy, ... } ]
  var deployList = _.map(deploys, function(deploysByEnv, service) {
    var deployMap = {};
    deployMap["service"] = service;
    _.each(deploysByEnv, function(deploy, env) {
      if (environmentMap[env] == undefined) {
        console.log(env + " is not a recognized environment")
        return;
      }
      if (deployMap[environmentMap[env]] != undefined) {
        console.log(service + " has more than one deploy in " + environmentMap[env]);
      }
      deployMap[environmentMap[env]] = deploy;
    });
    return deployMap;
  });

  // Sort by most recent PROD deploy
  var currentTime = moment().unix()
  var deployList = _.sortBy(deployList, function(d) {
    var mostRecent = 0;
    _.each(_.without(logicalEnvironments, "rc", "staging"), function(env) {
      var deploy = d[env];
      if (deploy != undefined) {
        var time = moment(deploy.deployed_at).unix();
        if (mostRecent < time) {
          mostRecent = time;
        }
      }
    });
    return mostRecent;
  }).reverse();

  // Further simplify into [ service: service, environments [ rcDeploy, ... ] ]
  // Additionally, check for equality between RC & prod environments
  var rows = _.map(deployList, function(deployMap) {
    var row = {
      service: deployMap["service"],
      environments: []
    };
    _.each(logicalEnvironments, function(env) {
      var deploy = deployMap[env];
      if (deploy != undefined) {
        deploy["match"] = environmentMatches(deploy, deployMap["rc"]);
        deploy["timeago"] = moment(deploy["deployed_at"]).fromNow();
        deploy["environment"] = env;
        deploy["col_class"] = environmentColMap[env];
      } else {
        deploy = []
        deploy["match"] = "version-na";
        deploy["environment"] = env;
        deploy["col_class"] = environmentColMap[env];
      }
      row["environments"].push(deploy);
    });
    return row;
  });

  var source = $("#service-template").html();
  var template = Handlebars.compile(source);

  $("#services-table-rows").html("");

  _.each(rows, function(row){
    $("#services-table-rows").append(template(row));
  });

  console.log("Done");
};

var refreshPage = function() {
  console.log("Refresh");
  jQuery.get("/deploy", {}, renderDataIntoPage, "json");
  //Get the data from decima
  //Create the hash to pass to handlebars
  //Render the handlerbars templates into the document
};

$(document).ready(function() {
  console.log("In ready");
  refreshPage();
  //setInterval(refreshPage, 30000);
});
