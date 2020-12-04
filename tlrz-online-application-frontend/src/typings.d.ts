/* SystemJS module definition */
declare var module: NodeModule;

interface NodeModule {
    id: string;
}

declare const Liferay: Liferay;

declare interface Liferay {
    authToken: string;
    Session: any;
    isProd: boolean;
    on: ( event: string, callback: ( data: object ) => void ) => void;
    fire: ( event: string, data: object ) => void;
}

declare interface ApplicationContext {
    // TODO: avoid using 'any' type
    user: any;
    submitFormUrl: string;
    toTheOnlineApplicationLink: string;
    informationAboutGrantLink: string;
    privacyInformationLink: string;
    offset: string;
}

declare const System: System;

interface System {
    import( request: string ): Promise<any>;
}

declare module 'xml-beautifier';
