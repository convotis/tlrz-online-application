# TLRZ Frontend App

This project is an angular cli ejected project. To regenerate the webpack base config run:

#### Production
`ng eject --force -prod -aot true --extract-css false --build-optimizer true --service-worker true --extract-licenses false`
#### Develop
`ng eject --force -dev -aot true --extract-css false --build-optimizer true --service-worker true --extract-licenses false`

and rename the file to the correct nomenclature.

## Fast development mode

It is possible to run the app locally to fast prototype and to avoid having to deploy for every frontend change.

Run `npm start` to serve the app locally.

Since we're using proxied webpack-dev-server, cors-related issues might occurs when calling REST API.
To avoid them - it is recommended to install this chrome plugin to disable cors.
https://chrome.google.com/webstore/detail/allow-control-allow-origi/nlfbmbojpeacfghkpbjhddihlkkiljbi

## Code scaffolding

Run `ng g component component-name` to generate a new component. You can also use `ng generate directive|pipe|service|class|guard|interface|enum|module`.

## Build

Run `npm run build` to build the project. The build artifacts will be stored in the `dist/` directory. Use the `-prod` flag for a production build.

## Git

* We use branches for features.
* Pay attention to making the commit messages concise, for example follow [these recommendations](http://alistapart.com/article/the-art-of-the-commit)
* Try to rebase the commits that are related, so that the commit history looks nicer 

## Coding guidelines

Follow the guidelines in `GUIDELINES.md`
