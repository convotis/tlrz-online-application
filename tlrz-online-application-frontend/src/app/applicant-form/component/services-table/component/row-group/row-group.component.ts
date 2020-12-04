import {Component} from '@angular/core';
import {ICellRendererAngularComp} from "ag-grid-angular/main";
import {ICellRendererParams} from 'ag-grid-community';
import {ServicesTableColumns} from '../../shared/services-table-columns';

@Component({
    selector: 'tlrz-row-group',
    templateUrl: './row-group.component.html',
    styleUrls: ['./row-group.component.scss']
})
export class RowGroupComponent implements ICellRendererAngularComp {
    public sectionTitle: string;

    constructor() {
    }

    public agInit( params: ICellRendererParams ): void {
        this.sectionTitle = params.getValue();
    }

    refresh( params: ICellRendererParams ): boolean {
        return true;
    }

}
