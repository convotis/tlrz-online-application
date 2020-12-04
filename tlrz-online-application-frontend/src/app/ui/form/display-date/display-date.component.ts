import {Component, ElementRef, HostListener, Input, Optional, Renderer2, Self, ViewChild} from '@angular/core';
import {ControlValueAccessor, NgControl} from "@angular/forms";
import {DateFormat} from "./shared/date-format";


@Component({
    selector: 'tlrz-display-date',
    templateUrl: './display-date.component.html',
    styleUrls: ['./display-date.component.scss']
})
export class DisplayDateComponent implements ControlValueAccessor {

    @ViewChild('dayInput', {static: true}) dayInput: ElementRef;
    @ViewChild('monthInput', {static: true}) monthInput: ElementRef;
    @ViewChild('yearInput', {static: true}) yearInput: ElementRef;

    @Input() public delimiter: string;
    @Input() public labelledBy: string;

    public date: DateFormat = {day: null, month: null, year: null};

    public isDisabled: boolean;
    public onChange: (_: any) => void;

    @HostListener('change', ['$event.target.value']) emitValue() {

        if (this.date.day && this.date.month && this.date.year) {
            this.onChange(this.date);
        } else {
            this.onChange(null);
        }
    }

    constructor(@Self() @Optional() public control: NgControl,
                private renderer: Renderer2) {
        if (this.control) {
            this.control.valueAccessor = this;
        }
    }

    onTouched = () => {
    };

    changeDate($event) {

        let date = $event.target.value;

        this.date.day = date.match(/^[1-9]$/) ? '0' + date : date;

        this.renderer.setProperty(this.dayInput.nativeElement, 'value', this.date.day);
    }

    changeMonth($event) {

        let month = $event.target.value;

        this.date.month = month.match(/^[1-9]$/) ? '0' + month : month;

        this.renderer.setProperty(this.monthInput.nativeElement, 'value', this.date.month);
    }


    changeYear($event) {

        let year = $event.target.value;

        this.date.year = year;

        this.renderer.setProperty(this.yearInput.nativeElement, 'value', this.date.year);
    }

    writeValue(value: any): void {

        if (value) {

            if (!(value instanceof Object)) {

                if (value.match(/^\d+$/)) {

                    const date = new Date(parseInt(value));

                    this.date.day = date.getDate() < 10 ?
                        '0' + date.getDate().toString() : date.getDate().toString();

                    this.date.month = date.getMonth() < 10 ?
                        '0' + (date.getMonth() + 1).toString() : (date.getMonth() + 1).toString();

                    this.date.year = date.getFullYear().toString();

                    return;
                }

                const dateMonthYear = value.split('.');

                if (dateMonthYear.length == 3) {
                    this.date.day = dateMonthYear[0];
                    this.date.month = dateMonthYear[1];
                    this.date.year = dateMonthYear[2];

                }
            } else {

                let date = <DateFormat>value;

                this.date.day = date.day;
                this.date.month = date.month;
                this.date.year = date.year;
            }
        }
    }

    registerOnChange(fn: any): void {
        this.onChange = fn;
    }

    registerOnTouched(fn: () => void): void {
        this.onTouched = fn;
    }

    setDisabledState(isDisabled: boolean): void {
        this.isDisabled = isDisabled;
    }
}
