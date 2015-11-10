window.decima = window.decima || {}
window.decima.environmentMap = {
  "staging": "staging",
  "rc": "rc",
  "us_west_2": "us-west",
  "eu_west_1": "eu-west",
  "fedramp": "fedramp"
}

window.decima.service = (function(w, $, undefined) {
  jsonSyntax = function(json) {
    json = json.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
    return json.replace(/("(\\u[a-zA-Z0-9]{4}|\\[^u]|[^\\"])*"(\s*:)?|\b(true|false|null)\b|-?\d+(?:\.\d*)?(?:[eE][+\-]?\d+)?)/g, function (match) {
        var cls = 'json-number';
        if (/^"/.test(match)) {
            if (/:$/.test(match)) {
                cls = 'json-key';
            } else {
                cls = 'json-string';
            }
        } else if (/true|false/.test(match)) {
            cls = 'json-boolean';
        } else if (/null/.test(match)) {
            cls = 'json-null';
        }
        //return $('</span>').addClass(cls).text(match);
        return '<span class="' + cls + '">' + match + '</span>';
    });
  }

  Handlebars.registerHelper("printJson", function(str) {
    if (!str) { return "NONE"; }
    var obj = JSON.parse(str)
    return new Handlebars.SafeString(jsonSyntax(JSON.stringify(obj, undefined, 4)));
  });

  Handlebars.registerHelper("timeAgo", function(str) {
    return moment(str).fromNow();
  });

  Handlebars.registerHelper("mapEnv", function(env) {
    return window.decima.environmentMap[env];
  });

  return {
    name: undefined,
    setServiceName: function(name) {
      w.decima.service.name = name
    },
    init: function() {
      w.decima.service.load()
    },
    bind: function() {
      $('span.label').on('click', function() {
        if ($(this).siblings('.config').is(':visible')) {
          $(this).siblings('.config').addClass('hide');
          $(this).siblings('.place-holder').removeClass('hide');
        } else {
          $(this).siblings('.place-holder').addClass('hide');
          $(this).siblings('.config').removeClass('hide');
        }
      });
    },
    load: function() {
      jQuery.get("/deploy/summary?service=" + w.decima.service.name, {}, w.decima.service.renderDataIntoPage, "json");
    },
    renderDataIntoPage: function(data) {
      if (data.length != 1) { alert("An error occoured while loading deploy information for " + w.decima.service.name); return; }
      var service = data[0],
          source = $("#service-deploys-template").html(),
          template = Handlebars.compile(source);
      $("#services-deploys").html(template(service));
      w.decima.service.bind();
    }
  };
})(window, jQuery); 
