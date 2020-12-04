import {Component, ElementRef, HostListener, Input, Optional, Renderer2, Self, ViewChild} from '@angular/core';
import {ControlValueAccessor, NgControl} from "@angular/forms";

@Component({
  selector: 'tlrz-display-currency',
  templateUrl: './display-currency.component.html',
  styleUrls: ['./display-currency.component.scss']
})
export class DisplayCurrencyComponent implements ControlValueAccessor {

    @ViewChild('euroInput', {static: true}) euroInput: ElementRef;
    @ViewChild('centInput', {static: true}) centInput: ElementRef;

    @Input() public delimiter: string;
    @Input() public labelledBy: string;

    public euro: string;
    public cent: string;

    public onChange: (_: any) => void;

    constructor(@Self() @Optional() public control: NgControl,
                private host: ElementRef<HTMLInputElement>,
                private renderer: Renderer2) {
        if (this.control) {
            this.control.valueAccessor = this;
        }
    }

    onTouched = () => {
    };

    onFocus($event) {
        let euro = this.parseEuro($event.target.value);

        this.renderer.setProperty(this.euroInput.nativeElement, 'value', euro);

        this.euroInput.nativeElement.select();
    }

    onBlur($event) {
        let euro = this.transformEuro($event.target.value);

        this.renderer.setProperty(this.euroInput.nativeElement, 'value', euro);
    }

    @HostListener('change', ['$event.target.value']) emitValue() {

        if (this.euro != null && this.euro != '') {
            if (this.cent == '' || this.cent == null) {
                this.onChange(this.euro + '.00');
            } else {
                this.onChange(this.euro + '.' + this.cent);
            }
        } else {
            this.onChange(null);
        }
    }

    changeEuro($event) {

        let euro = this.transformEuro($event.target.value);

        this.euro = euro;

        this.renderer.setProperty(this.euroInput.nativeElement, 'value', this.euro);
    }

    changeCent($event) {

        let cent = $event.target.value;

        cent = cent.match(/^[0-9]$/) ? '0' + cent : cent;

        this.cent = cent;

        this.renderer.setProperty(this.centInput.nativeElement, 'value', this.cent);
    }

    writeValue(value: string): void {

        if (value != null) {
            if (value.includes('.')) {
                this.euro = value.substr(0, value.lastIndexOf('.'));
                this.cent = value.substr(value.lastIndexOf('.') + 1);
            } else {
                this.euro = value
            }
        }
    }

    registerOnChange(fn: any): void {
        this.onChange = fn;
    }

    registerOnTouched(fn: () => void): void {
        this.onTouched = fn;
    }

    private transformEuro(number: string): string {

        if (number) {
            if (! (number === '0')) {
                //remove leading zeros
                number = number.replace(/^0+/, '');
            }

            if (number.replace('.', '').match(/^\d+$/)) {

                number = number.replace('.', '');

                number = number.replace(/\B(?=(\d{3})+(?!\d))/g, ".");
            }
        }
        return number;
    }

    private parseEuro(number: string): string {
        if (number) {
            number = number.replace('.', '');
        }
        return number;
    }
}
