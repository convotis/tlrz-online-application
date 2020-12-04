import {Component, Input, OnDestroy, OnInit} from '@angular/core';
import {FormControl, FormGroup} from "@angular/forms";
import {takeUntil} from "rxjs/operators";

import {ErrMsgService} from "../../../core/err-msg/err-msg.service";
import {CustomValidator} from "../../../core/custom-validator/custom-validator";
import {CardService} from "../../shared/card.service";
import {AutoUnsubscribe} from "../../../core/auto-unsubscribe/auto-unsubscribe";


@Component({
  selector: 'tlrz-service-data-inputs',
  templateUrl: './service-data-inputs.component.html',
  styleUrls: ['./service-data-inputs.component.scss']
})
export class ServiceDataInputsComponent extends AutoUnsubscribe implements OnInit {

    @Input() parentGroup: FormGroup;
    @Input() parentName: string;
    @Input() cardIndex: number;

    public get invoiceDate(): FormControl {
        return <FormControl>this.parentGroup.get('invoiceDate');
    }

    public get invoiceAmount(): FormControl {
        return <FormControl>this.parentGroup.get('invoiceAmount');
    }

    public get serviceType(): FormControl {
        return <FormControl>this.parentGroup.get('serviceType');
    }

    public get reimbursement(): FormControl {
        return <FormControl>this.parentGroup.get('reimbursement');
    }

    constructor(
        public errMsgService: ErrMsgService,
        private cardService: CardService
    ) {
        super();
    }

    ngOnInit(): void {

        this.reimbursement.setValidators([
            CustomValidator.requiredIntValues(3),
            CustomValidator.smallerThan(this.invoiceAmount)
        ]);

        this.invoiceAmount.valueChanges.pipe(takeUntil(this.componentDestroyed$)).subscribe(value => {
            this.reimbursement.updateValueAndValidity();
        });

        this.cardService.pushCard(this.parentName);
    }

    public ngOnDestroy(): void {

        super.ngOnDestroy();

        this.cardService.removeCard(this.parentName);
    }
}
