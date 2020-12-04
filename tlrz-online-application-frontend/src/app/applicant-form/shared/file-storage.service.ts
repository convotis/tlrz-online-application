import {Injectable} from '@angular/core';
import {forkJoin, Observable, of as ObservableOf} from 'rxjs';
import {map, tap} from 'rxjs/operators';

import {FileBase64} from './file-base64.model';

@Injectable({
  providedIn: 'root'
})
export class FileStorageService {

  constructor() { }

    /* TODO: remove 'any' type */
    public wizardFileToBase64(data: any): Observable<any> {
        /* TODO: second page images upload */
        /* TODO: move hardcoded files to some enum */

        const fileDataURIList$ = [
            this.createFileDataUriObj(data.applicantAndFunds.partnerIncomeConfirmation.taxAssessmentFile)
                .pipe(tap((fileObj: FileBase64) => {
                    data.applicantAndFunds.partnerIncomeConfirmation.taxAssessmentFile = fileObj;
                })),
            ...this.createCardFileListURI(data.applicantExpenses.applicantExpensesArray),
            ...this.createCardFileListURI(data.expensesForPartner.expensesForPartnerArray),
            ...this.createCardFileListURI(data.expensesForChildren.expensesForChildrenArray)
        ];

        /* TODO: now data changes by links. but should be by subscriiption */
        return forkJoin(fileDataURIList$)
            .pipe(map(() => {
                return data;
            }));
    }

    /* TODO: remove 'any' type*/
    public wizardBase64ToFile(data: any): any {
        let taxAssessmentFile = data.applicantAndFunds.partnerIncomeConfirmation.taxAssessmentFile;
        if(taxAssessmentFile) {
            data.applicantAndFunds.partnerIncomeConfirmation.taxAssessmentFile = this.base64toFile(taxAssessmentFile.dataURI, taxAssessmentFile.name);
        }

        this.setCardBase64toFileList(data.applicantExpenses.applicantExpensesArray);
        this.setCardBase64toFileList(data.expensesForPartner.expensesForPartnerArray);
        this.setCardBase64toFileList(data.expensesForChildren.expensesForChildrenArray);

        return data;
    }

    /* TODO: remove 'any' type */
    private createCardFileListURI( cardList: any[]): Observable<File | FileBase64>[] {
        const fileDataURIList$ = [];

        cardList.reduce(( acc: any, val: any ) => {
                return [...acc, ...val.files];
            }, [])
            .map(( fileContainer: any ) => {
                if (fileContainer.file && fileContainer.file instanceof File) {
                    const fileBase64$ = this.createFileDataUriObj(fileContainer.file)
                        .pipe(tap((fileObj: FileBase64) => {
                            /* TODO: here we set fileObj to main data by link but should by subscription */
                            fileContainer.file = fileObj;
                        }));
                    fileDataURIList$.push(fileBase64$);
                }
            });

        return fileDataURIList$;
    }

    private createFileDataUriObj( file: File ): Observable<File | FileBase64> {
        if (!file || (file instanceof File) == false) {
            return ObservableOf(file);
        }

        return this.fileToBase64(file).pipe(
            map(( dataURI: string ) => ({dataURI, name: file.name}))
        );
    }

    private fileToBase64(file: File): Observable<string> {
        return  new Observable(subscriber => {
            let reader = new FileReader();
            reader.onload = () => {
                subscriber.next(reader.result as string);
                subscriber.complete();
            };

            reader.readAsDataURL(file);
        });
    }

    private base64toFile(base64: string, fileName: string): File {
        var base64Parts = base64.split(",");
        var fileFormat = base64Parts[0].split(";")[1];
        var fileContent = base64Parts[1];
        var file = new File([fileContent], fileName, {type: fileFormat});
        return file;
    }

    private setCardBase64toFileList( fileList: any ): void {
        fileList.map(( expenses ) => {
                return expenses.files;
            })
            .map(( fileList ) => {
                fileList.forEach(( fileContainer ) => {
                    if (fileContainer.file && fileContainer.file.dataURI) {
                        fileContainer.file = this.base64toFile(fileContainer.file.dataURI, fileContainer.file.name);
                    }
                });
            });
    }
}
