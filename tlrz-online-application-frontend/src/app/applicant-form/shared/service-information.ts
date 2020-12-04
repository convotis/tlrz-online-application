import {DateFormat} from "../../ui/form/display-date/shared/date-format";

export interface ServiceInformation {
    invoiceAmount: string;
    invoiceDate: DateFormat;
    reimbursement: string;
    serviceType: string;
    files: FilesAndTypes[];
}

interface FilesAndTypes {
    file: File;
    type: string;
}
