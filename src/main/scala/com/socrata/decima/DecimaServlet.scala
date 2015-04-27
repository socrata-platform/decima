package com.socrata.decima

class DecimaServlet extends LachesisStack {

  get("/") {
    <html>
      <body>
        <h1>Hello, world!</h1>
        This is Lachesis, a service for keeping track of deploys and managing service dependencies.
      </body>
    </html>
  }

}
