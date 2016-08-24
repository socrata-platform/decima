// ReactDOM.render(
//   <h1>Hello, world!</h1>,
//   document.getElementById('decima')
// );


var {PageHeader, ListGroup, ListGroupItem, Navbar, MenuItem, 
      Nav, NavDropdown, NavItem, Jumbotron, Media, MediaItem, 
      DropdownButton, Button, ButtonToolbar, Grid, Row, Col, 
      Panel, Label} = window.ReactBootstrap

var Decima = React.createClass({
  getInitialState: function() {
    return {data: []};
  },
  componentDidMount: function() {
    $.ajax({
      url: this.props.url,
      dataType: 'json',
      cache: false,
      success: function(data) {
        this.setState({data: data});
      }.bind(this),
      error: function(xhr, status, err) {
        console.error(this.props.url, status, err.toString());
      }.bind(this)
    });
  },
  render: function() {
    return (
      <div>
          <PageHeader>Decima <small>Service version tracking</small></PageHeader>
        <ServicesList data={this.state.data} />
      </div>
    );
  }
});

var ServicesList = React.createClass({
  render: function() {
    var serviceNodes = this.props.data.map(function(service) {
      return (
        <Service key={service.service_alias} serviceData={service}/>
      );
    });
    return (
      <ListGroup>
        {serviceNodes}
      </ListGroup>
    );
  }
});

//var TeamsList = React.createClass({
//  render: function() {
//    return (
//      <div className="teamsList">
//        <Team teamName="Admin" />
//        <Team teamName="Discovery" />
//        <Team teamName="Other" />
//      </div>
//    );
//  }
//});
//
//var Team = React.createClass({
//  render: function() {
//    return (
//      <div className="team">
//        {this.props.teamName}
//      </div>
//    );
//  }
//});

function getProductionStatus(environments){
  if (environments.fedramp && environments.fedramp[0] && 
    environments.eu_west_1 && environments.eu_west_1[0] && 
    environments.fedramp[0].parity_status == environments.eu_west_1[0].parity_status) {

    return (<Label>Production matches</Label>); 
  } else {
    return (<Label bsStyle="danger">Production Mismatch</Label>);
  };
}

function getRCStatus(environments){
  if (environments.fedramp && environments.fedramp[0] && 
    environments.rc && environments.rc[0] && 
    environments.fedramp[0].parity_status == environments.rc[0].parity_status) {

    return (<Label>RC matches</Label>); 
  } else {
    return (<Label bsStyle="warning">RC is ahead</Label>);
  };
}


var Service = React.createClass({
  render: function() {
    return (
      <ListGroupItem header={this.props.serviceData.service_alias}>
        <Grid>
          <Row>
            <Col md={3}>{getProductionStatus(this.props.serviceData.environments)}</Col>
            <Col md={3}>{getRCStatus(this.props.serviceData.environments)}</Col>
            <Col md={3} mdOffset={3}><ActionDropdown /></Col>
          </Row>
        </Grid>
      </ListGroupItem>
    );
  }
});

var ActionDropdown = React.createClass({
  render: function() {
    return (
      <DropdownButton bsSize="xsmall" title = "Actions">
        <MenuItem eventKey="1">Hide from UI</MenuItem>
        <MenuItem eventKey="2">Move to another team</MenuItem>
      </DropdownButton>
    );
  }

});

//var Deployment = React.createClass({
//  render: function() {
//    return (
//      <div className="deployment">
//        Hello, world! I am a Deployment of a Service in an environment.
//      </div>
//    );
//  }
//});


ReactDOM.render(
  <Decima url="/deploy/summary" />,
  document.getElementById('decima')
);