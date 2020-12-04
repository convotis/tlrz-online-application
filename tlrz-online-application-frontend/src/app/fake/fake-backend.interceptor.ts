import { Injectable } from '@angular/core';
import { HTTP_INTERCEPTORS, HttpEvent, HttpHandler, HttpInterceptor, HttpRequest, HttpResponse } from '@angular/common/http';
import { of as observableOf, Observable } from 'rxjs';
import { mergeMap, materialize, dematerialize } from 'rxjs/operators';


@Injectable()
export class FakeBackendInterceptor implements HttpInterceptor {
    constructor() {
    }

    public intercept( request: HttpRequest<any>, next: HttpHandler ): Observable<HttpEvent<any>> {
        // wrap in delayed observable to simulate server api call
        return observableOf(null)
            .pipe(
                mergeMap(() => {
                    // pass through any requests not handled above
                    return next.handle(request);
                }),
                // call materialize and dematerialize to ensure delay even if an error is thrown
                materialize(),
                // .delay(250)
                dematerialize()
            );

    }
}

export let fakeBackendProvider = {
    // use fake backend in place of Http service for backend-less development
    provide: HTTP_INTERCEPTORS,
    useClass: FakeBackendInterceptor,
    multi: true
};
