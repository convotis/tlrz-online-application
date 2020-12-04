import {Injectable} from '@angular/core';
import {BehaviorSubject, Observable} from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class CardService {

    private cards: string[] = [];
    private cardsSource: BehaviorSubject<string[]> = new BehaviorSubject([]);

    constructor() {}

    public get cards$(): Observable<string[]> {
        return this.cardsSource.asObservable();
    }

    public pushCard(pageTitle: string) {

        let sectionStartIndex = 0;

        switch (pageTitle) {
            case 'applicantExpenses':
                this.cards.unshift(pageTitle);
                break;
            case 'expensesForPartner' :

                sectionStartIndex = 0;

                while (this.cards[sectionStartIndex] == 'applicantExpenses') {
                    sectionStartIndex++;
                }

                this.cards.splice(sectionStartIndex, 0, pageTitle);

                break;
            case 'expensesForChildren':

                sectionStartIndex = 0;

                while (this.cards[sectionStartIndex] == 'applicantExpenses' ||
                this.cards[sectionStartIndex] == 'expensesForPartner') {
                    sectionStartIndex++;
                }

                this.cards.splice(sectionStartIndex, 0, pageTitle);

                break;
        }

        this.cardsSource.next(this.cards);
    }

    public removeCard(pageTitle: string) {

        let sectionStartIndex = 0;

        switch (pageTitle) {
            case 'applicantExpenses':
                this.cards.shift();
                break;
            case 'expensesForPartner' :

                sectionStartIndex = 0;

                while (this.cards[sectionStartIndex] == 'applicantExpenses') {
                    sectionStartIndex++;
                }

                this.cards.splice(sectionStartIndex, 1);

                break;
            case 'expensesForChildren':

                sectionStartIndex = 0;

                while (this.cards[sectionStartIndex] == 'applicantExpenses' ||
                this.cards[sectionStartIndex] == 'expensesForPartner') {
                    sectionStartIndex++;
                }

                this.cards.splice(sectionStartIndex, 1);

                break;
        }

        this.cardsSource.next(this.cards);
    }

    public removeCards(pageTitle: string) {

        while (this.cards.indexOf(pageTitle) != -1) {
            this.cards.splice(this.cards.indexOf(pageTitle), 1);
        }
        this.cardsSource.next(this.cards);
    }
}
