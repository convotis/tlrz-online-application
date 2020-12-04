import {Component} from '@angular/core';
import {ICellRendererAngularComp} from "ag-grid-angular/main";
import {ICellRendererParams} from 'ag-grid-community';
import { ServicesTableColumns } from '../../shared/services-table-columns';

@Component({
    selector: 'tlrz-file-info-list-cell',
    templateUrl: './file-info-list-cell.component.html',
    styleUrls: ['./file-info-list-cell.component.scss']
})
export class FileInfoListCellComponent implements ICellRendererAngularComp {
    public columnData: ServicesTableColumns[] = [];

    constructor() {
    }

    public agInit(params: ICellRendererParams): void {
        this.columnData = params.getValue();
    }

    refresh(params: ICellRendererParams): boolean {
        return true;
    }
}
