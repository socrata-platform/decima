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
  console.log("newRenderDataIntoPage");
  var source = $("#service-template").html();
  var template = Handlebars.compile(source);

  $("#services-table-rows").html("");

  data = data.sort(function(a,b) {
    if (a.service_alias < b.service_alias) { return -1; }
    if (a.service_alias > b.service_alias) { return 1; }
    return 0;
  })
  _.each(data, function(service){
    service['env_parity'] = {}
    _.each(service['environments'], function(deps, dEnv) {
      var mappedDEnv = environmentMap[dEnv];
      if (typeof mappedDEnv != "undefined") {
        var mappendDEnvParity = _.reduce(deps, function(memo, dep) { return memo && dep.parity_with_reference; }, true);
        service['env_parity'][mappedDEnv] = {
          "parity": mappendDEnvParity,
          "match_class": mappendDEnvParity? "version-match" : "version-no-match",
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

  console.log("Done");
}

var refreshPage = function() {
  console.log("Refresh");
  jQuery.get("/deploy/summary", {}, renderDataIntoPage, "json");
};

$(document).ready(function() {
  console.log("In ready");
  refreshPage();
  setInterval(refreshPage, 30000);
});
