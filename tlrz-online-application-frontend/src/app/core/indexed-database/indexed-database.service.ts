import { Injectable } from '@angular/core';
import {Observable, Observer} from "rxjs";
import {AbstractControl} from "@angular/forms";
import {debounceTime, distinctUntilChanged, mergeMap, takeUntil} from "rxjs/operators";
import {FileStorageService} from "../../applicant-form/shared/file-storage.service";
import {AutoUnsubscribe} from "../auto-unsubscribe/auto-unsubscribe";

@Injectable({
    providedIn: 'root'
})
export class IndexedDatabaseService extends AutoUnsubscribe {

    private baseName: string = 'applicantStorage';
    private storeName: string = 'applicantData';

    constructor(
        private fileStorage: FileStorageService,
    ) {
        super();
    }

    public connectToStorage(func) {

        let storage = indexedDB.open(this.baseName, 1);

        storage.onupgradeneeded = () => {

            if (!storage.result.objectStoreNames.contains(this.storeName)) {
                storage.result.createObjectStore(this.storeName);
            }

            this.connectToStorage(func);
        };

        storage.onsuccess = () => {
            func(storage.result);
        };
    }

    public setItem(value: any) {

        this.connectToStorage((connection: IDBDatabase) => {

            let transaction = connection.transaction([this.storeName], 'readwrite');

            let request = transaction.objectStore(this.storeName).put(value,1);

            request.onsuccess = () => {
                return request.result;
            }
        });
    }

    public getItem(){

        return new Observable((observer: Observer<any>) => {

            this.connectToStorage((connection: IDBDatabase) => {

                let transaction = connection.transaction([this.storeName], 'readonly');

                let request = transaction.objectStore(this.storeName).get(1);

                request.onsuccess = () => {
                    observer.next(request.result);
                };
            });
        });
    }

    public clearData() {

        this.connectToStorage((connection: IDBDatabase) => {

            let transaction = connection.transaction([this.storeName], 'readwrite');

            let request = transaction.objectStore(this.storeName).delete(1);

            request.onsuccess = () => {
                return request.result;
            }
        });
    }

    public storeOnChange(control: AbstractControl) {

        control.valueChanges.pipe(
            debounceTime(200),
            distinctUntilChanged(),
            takeUntil(this.componentDestroyed$)
        ).subscribe(data => {
            this.setItem(data);
        });
    }
}
