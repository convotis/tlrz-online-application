import {
    Component,
    ElementRef,
    EventEmitter,
    HostListener,
    Input,
    Optional,
    Output,
    Renderer2,
    Self,
    ViewChild
} from '@angular/core';
import {ControlValueAccessor, NgControl} from '@angular/forms';
import {SessionTimerService} from "../../../core/session-timer/session-timer.service";

@Component({
    selector: 'tlrz-file-upload',
    templateUrl: './file-upload.component.html',
    styleUrls: ['./file-upload.component.scss']
})
export class FileUploadComponent implements ControlValueAccessor {

    @Input() addButton: boolean;
    @Input() removeButton: boolean;
    @Input() additionalValid: boolean;
    @Input() ariaLabel: string;

    /* TODO: remove 'any' type */
    @Output() add: EventEmitter<any> = new EventEmitter();
    @Output() remove: EventEmitter<any> = new EventEmitter();

    @ViewChild('fileUpload', {static: true}) fileUpload: ElementRef;

    public file: File | null = null;
    public onChange: ( _: any ) => void;

    @HostListener('change', ['$event.target.files']) emitFiles(event: FileList) {
        const file = event && event.item(0);
        this.onChange(file);
        this.file = file;
    }

    constructor(@Self() @Optional() public control: NgControl,
                private host: ElementRef<HTMLInputElement>,
                private renderer: Renderer2,
                private sessionTimerService: SessionTimerService) {
        if (this.control) {
            this.control.valueAccessor = this;
        }
    }


    onTouched = () => {
    };

    writeValue(value: any): void {

        if (!value) {
            this.file = value;
            this.renderer.setProperty(this.fileUpload.nativeElement, 'value', value);
        }
    }

    registerOnChange(fn: any): void {
        this.onChange = fn;
    }

    registerOnTouched( fn: () => void ): void {
        this.onTouched = fn;
    }

    public addField(): void {
        if (Liferay && Liferay.Session) {
            Liferay.Session.extend();
        }
        this.sessionTimerService.resetTimer();

        this.add.emit();
    }

    public removeField(): void {
        if (Liferay && Liferay.Session) {
            Liferay.Session.extend();
        }
        this.sessionTimerService.resetTimer();

        this.remove.emit();
    }
}
