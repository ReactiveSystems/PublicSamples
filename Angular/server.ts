// These are important and needed before anything else
import 'zone.js/dist/zone-node';
import 'reflect-metadata';

import {enableProdMode} from '@angular/core';

import * as express from 'express';
import {join} from 'path';

// Faster server renders w/ Prod mode (dev mode never needed)
enableProdMode();

// Express server
const app = express();

const PORT = process.env.PORT || 4000;
const DIST_FOLDER = process.cwd();

// * NOTE :: leave this as require() since this file is built Dynamically from webpack
const {AppServerModuleNgFactory, LAZY_MODULE_MAP} = require('./dist/server/main');

// Express Engine
import {ngExpressEngine} from '@nguniversal/express-engine';

// Import module map for lazy loading
import {provideModuleMap} from '@nguniversal/module-map-ngfactory-loader';
import {appRoutes} from './src/app/modules/routing/routing.module';

// Speed up page load with memory cache.
const mcache = require('memory-cache');
const cache = (duration) => {
    return (req, res, next) => {
        let key = '_express_' + req.originalUrl || req.url;
        let cachedBody = null;
        if (req.url.indexOf('?nc=') < 0) {
            cachedBody = mcache.get(key);
        } else {
            key = '_express_' + req.originalUrl.substring(0, req.originalUrl.indexOf('?nc='));
        }
        if (cachedBody) {
            res.send(cachedBody);
            return;
        } else {
            res.sendResponse = res.send;
            res.send = (body) => {
                console.log('Add body to cache: ' + key);
                mcache.put(key, body, duration * 1000);
                res.sendResponse(body);
            };
            next();
        }
    };
};

app.engine('html', ngExpressEngine({
    bootstrap: AppServerModuleNgFactory,
    providers: [
        provideModuleMap(LAZY_MODULE_MAP)
    ]
}));

app.set('view engine', 'html');
app.set('views', join(DIST_FOLDER, 'browser'));

// Server static files from /browser
app.get('*.*', express.static(join(DIST_FOLDER, 'browser')));

app.post('/clearcache', (req, res) => {
    console.log('clear cache');
    mcache.clear();
    res.status(200);
    res.end();
});

appRoutes.forEach(route => {
    if (route.path === '**') {
        return;
    }

    console.log('register route:' + route.path);
    app.get('/' + route.path, cache(86400), (req, res) => {
        // console.log("route:" + req.originalUrl);
        if ((req.originalUrl === '/404')) {
            res.status(404);
        }
        res.render('index', {
            req: req,
            res: res
        });
    });
});

app.get('*', (req, res) => {
    let check = 0;
    for (const route in appRoutes) {
        if (appRoutes[route].path === '') {
            continue;
        }
        if (req.originalUrl.startsWith('/' + appRoutes[route].path + '/')) {
            check = 1;
        }
    }

    if (check === 0 || req.originalUrl === '/404') {
        res.status(404);
    }

    if (req.originalUrl.endsWith('.map')) {
        res.end();
        return;
    }

    res.render('index', {
        req: req,
        res: res
    });
});

// Start up the Node server
app.listen(PORT, () => {
    console.log(`Node server listening on http://localhost:${PORT}`);
});
