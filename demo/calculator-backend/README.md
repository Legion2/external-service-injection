# Calculator service

The calculator service is a small microservice that provides an API to evaluate an mathematical expression. The backend is using [`math.js`](https://mathjs.org/) to calculate the result.

The service provides an API documentation where the API of the service can tested.

The source code is from a [workshop example](https://github.com/itdesign/kubernetes-workshop), it is modifyed to meet the requirements of the HTTP example.
## Developing the service

The service is implemented using Typescript, express.js and node.

You can start the service locally by calling `yarn dev`.

The service is then running on [localhost:8080](http://localhost:8080).
