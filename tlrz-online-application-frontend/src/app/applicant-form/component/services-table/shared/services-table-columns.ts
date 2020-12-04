export class ServicesTableColumns {
    sectionIndex: number;
    sectionTitle: string;
    rowGroup?: string;
    number: string;
    invoiceDate: string;
    serviceType: string;
    invoiceAmount: string;
    reimbursement: string;
    fileInfoList: FileInfo[];
}

export class FileInfo {
    file: string;
    type: string;
}
