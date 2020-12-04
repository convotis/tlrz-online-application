import {Component, Input} from '@angular/core';
import {FormArray, FormControl, FormGroup} from '@angular/forms';

@Component({
    selector: 'app-services-usage',
    templateUrl: './services-usage.component.html',
    styleUrls: ['./services-usage.component.scss']
})
export class ServicesUsageComponent {

    @Input() public servicesUsageGroup: FormGroup;

    constructor() {
    }

    public get activityExpenses(): FormControl {
        return <FormControl>this.servicesUsageGroup.get('activityExpenses');
    }

    public get illnessExpenses(): FormControl {
        return <FormControl>this.servicesUsageGroup.get('illnessExpenses');
    }

    public get insuranceBenefits(): FormControl {
        return <FormControl>this.servicesUsageGroup.get('insuranceBenefits');
    }

    public get activityPersonList(): FormArray {
        return <FormArray>this.servicesUsageGroup.get('activityPersonList');
    }

    public get illnessPersonList(): FormArray {
        return <FormArray>this.servicesUsageGroup.get('illnessPersonList');
    }

    public get insurancePersonList(): FormArray {
        return <FormArray>this.servicesUsageGroup.get('insurancePersonList');
    }
}
