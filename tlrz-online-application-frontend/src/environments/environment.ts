// The file contents for the current environment will overwrite these during build.
// The build system defaults to the dev environment which uses `environment.ts`, but if you do
// `ng build --env=prod` then `environment.prod.ts` will be used instead.
// The list of which env maps to which file can be found in `.angular-cli.json`.

export const environment = {
    production: false,
    e2e: false,
};

/* TODO: move to related module */
export const applicationContext: ApplicationContext = {
    user: null,
    submitFormUrl: '',
    toTheOnlineApplicationLink: '',
    informationAboutGrantLink: '',
    privacyInformationLink: '',
    offset: '',
    sessionLength: 90000,
    sessionTimeoutTitle: 'Hinweis',
    sessionTimeoutText: 'Ihre Session ist aufgelaufen. Sie müssen Ihren Antrag erneut starten. Alle Daten müssen erneut eingegeben werden.',
    sessionTimeoutButton: 'Hier gelangen Sie zum neuen Antrag',
    sessionTimeoutLink: 'http://localhost:3000'
};
