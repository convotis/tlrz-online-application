import {enableProdMode} from '@angular/core';
import {platformBrowserDynamic} from '@angular/platform-browser-dynamic';
import {AppModule} from './app/app.module';

import {applicationContext, environment} from './environments/environment';
import './polyfills';

export const init = (toTheOnlineApplicationLink, informationAboutGrantLink, privacyInformationLink,
                     submitFormUrl, currentUser, offset, sessionLength, sessionTimeoutTitle, sessionTimeoutText, sessionTimeoutButton, sessionTimeoutLink) => {

    // check prodMode because after page navigation in Liferay the Zone is still active
    if (environment.production && !Liferay.isProd) {
        enableProdMode();
        Liferay.isProd = true;
    }
    platformBrowserDynamic()
        .bootstrapModule(AppModule)
        .catch(( err ) => console.error(err));

    applicationContext.toTheOnlineApplicationLink = toTheOnlineApplicationLink;
    applicationContext.informationAboutGrantLink = informationAboutGrantLink;
    applicationContext.privacyInformationLink = privacyInformationLink;

    applicationContext.user = currentUser;

    applicationContext.offset = offset;

    applicationContext.submitFormUrl = submitFormUrl;

    applicationContext.sessionLength = sessionLength;

    applicationContext.sessionTimeoutTitle = sessionTimeoutTitle;
    applicationContext.sessionTimeoutText = sessionTimeoutText;
    applicationContext.sessionTimeoutButton = sessionTimeoutButton;
    applicationContext.sessionTimeoutLink = sessionTimeoutLink;
};

if (!environment.production) {
    init("",
        "",
        "",
        "",
        `{"firstName":"Test","lastName":"Test","birthday":"930700800000"}`,
        "",
        90000,
        'Hinweis',
        'Ihre Session ist aufgelaufen. Sie müssen Ihren Antrag erneut starten. Alle Daten müssen erneut eingegeben werden.',
        'Hier gelangen Sie zum neuen Antrag',
        'http://localhost:3000'
    );
}

/* tslint:disable:no-default-export */
export default init;
/* tslint:enable:no-default-export */
