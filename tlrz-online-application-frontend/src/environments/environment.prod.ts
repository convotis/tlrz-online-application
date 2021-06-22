export const environment = {
    production: true,
    e2e: false,
};

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
