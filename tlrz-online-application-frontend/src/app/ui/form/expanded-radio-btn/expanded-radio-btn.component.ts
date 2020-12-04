import { Component, OnInit, Optional, Output, Self } from '@angular/core';
import {ControlContainer, FormGroup} from '@angular/forms';
import {distinctUntilChanged, takeUntil} from 'rxjs/operators';

import {AutoUnsubscribe} from '../../../core/auto-unsubscribe/auto-unsubscribe';

@Component({
    selector: 'tlrz-expanded-radio-btn',
    templateUrl: './expanded-radio-btn.component.html',
    styleUrls: ['./expanded-radio-btn.component.scss']
})
export class ExpandedRadioBtnComponent extends AutoUnsubscribe implements OnInit{
    public radioBtn: boolean;

    get formGroup(): FormGroup {
        return this.parent.control as FormGroup;
    }

    constructor(@Self() @Optional() private parent: ControlContainer) {
        super();
    }

    public ngOnInit(): void {

        this.toggleRadioBtn(this.formGroup.value);

        this.formGroup.valueChanges
            .pipe(
                distinctUntilChanged(),
                takeUntil(this.componentDestroyed$)
            )
            .subscribe((data) => {
                this.toggleRadioBtn(data);
        });
    }

    public clearAll() {
        this.radioBtn = true;

        for (let control of Object.values(this.formGroup.controls)) {
            control.setValue(false);
        }

    }

    private toggleRadioBtn(groupData: any) {
        for( const controlValue of Object.values(groupData)) {
            if (controlValue) {
                return this.radioBtn = false;
            }
        }

        return this.radioBtn = true;
    }

}
