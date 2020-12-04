// Needed for older Firefox versions, e.g. 45.0 ESR FIXME: try to only load if necessary
import 'core-js/es7/object';
/***************************************************************************************************
 * Zone JS is required by default for Angular itself.
 */
import {environment} from './environments/environment';

/** IE9, IE10 and IE11 requires all of the following polyfills. */
// FIXME try to only load if necessary
import 'core-js/es6/symbol';
import 'core-js/es6/object';
import 'core-js/es6/function';
import 'core-js/es6/parse-int';
import 'core-js/es6/parse-float';
import 'core-js/es6/number';
import 'core-js/es6/math';
import 'core-js/es6/string';
import 'core-js/es6/date';
import 'core-js/es6/array';
import 'core-js/es6/regexp';
import 'core-js/es6/map';
import 'core-js/es6/weak-map';
import 'core-js/es6/set';
/** IE10 and IE11 requires the following for NgClass support on SVG elements */
// import 'classlist.js'; // Run `npm install --save classlist.js`.
/** IE10 and IE11 requires the following for the Reflect API. */
// import 'core-js/es6/reflect';

/**
 * By default, zone.js will patch all possible macroTask and DomEvents
 * user can disable parts of macroTask/DomEvents patch by setting following flags
 */
// (window as any).__Zone_disable_requestAnimationFrame = true; // disable patch requestAnimationFrame
// (window as any).__Zone_disable_on_property = true; // disable patch onProperty such as onclick
// (window as any).__zone_symbol__BLACK_LISTED_EVENTS = ['scroll', 'mousemove']; // disable patch specified eventNames

/*
 * in IE/Edge developer tools, the addEventListener will also be wrapped by zone.js
 * with the following flag, it will bypass `zone.js` patch for IE/Edge
 */
// (window as any).__Zone_enable_cross_context_check = true;

/** Evergreen browsers require these. */
    // Used for reflect-metadata in JIT. If you use AOT (and only Angular decorators), you can remove.
    // import 'core-js/es7/reflect';

declare const window: any;

// check Zone because after page navigation in Liferay the Zone is still active
if (!window['Zone']) {
    require('zone.js/dist/zone'); // Included with Angular CLI.
}

if (!environment.production) {
    Error['stackTraceLimit'] = Infinity;
    require('zone.js/dist/long-stack-trace-zone');
}
