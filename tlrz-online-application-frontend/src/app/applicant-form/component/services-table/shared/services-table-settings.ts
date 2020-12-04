import { ColDef, ColSpanParams } from 'ag-grid-community';
import { FileInfoListCellComponent } from '../component/file-info-list-cell/file-info-list-cell.component';
import { FileInfoListHeaderComponent } from '../component/file-info-list-header/file-info-list-header.component';
import { RowGroupComponent } from '../component/row-group/row-group.component';

const columnNumbers = 7;

export const columnDefs: ColDef[] = [
    {
        headerName: 'sectionTitle',
        field: 'sectionTitle',
        pinned: 'left',
        lockPosition: true,
        hide: true
    },
    {
        headerName: '',
        field: 'rowGroup',
        headerClass: 'row-group-cell',
        cellClass: "row-group-cell",
        cellRendererFramework: RowGroupComponent,
        width: 0,
        colSpan: (params: ColSpanParams) => {
            return params.data.rowGroup ? columnNumbers : 0;
        },
        lockPosition: true,
        autoHeight:false,
    },
    {
        headerName: 'Belegnr',
        field: 'number',
        flex: 2,
        cellClass: "cell-text",
        lockPosition: true,
        autoHeight:false
    },
    {
        headerName: 'Datum der Rechnung',
        field: 'invoiceDate',
        cellClass: "cell-text",
        flex: 2,
        lockPosition: true,
        autoHeight:false
    },
    {
        headerName: 'Art der Leistung',
        field: 'serviceType',
        cellClass: "cell-text",
        flex: 2,
        lockPosition: true,
        autoHeight:false
    },
    {
        headerName: 'Rechnungsbetrag',
        field: 'invoiceAmount',
        cellClass: "cell-text",
        flex: 2,
        lockPosition: true,
        autoHeight:false
    },
    {
        headerName: 'Kostenerstattung von anderer Seite',
        field: 'reimbursement',
        cellClass: "cell-text",
        flex: 2,
        lockPosition: true,
        autoHeight:false
    },
    {
        field: 'fileInfoList',
        cellRendererFramework: FileInfoListCellComponent,
        headerComponentFramework: FileInfoListHeaderComponent,
        flex: 5,
        lockPosition: true,
        autoHeight:false
    }
];

