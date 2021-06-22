import {CommonModule} from '@angular/common';
import {NgModule} from '@angular/core';
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {AgGridModule} from 'ag-grid-angular';
import {AlertConfig, AlertModule, TooltipConfig, TooltipModule} from "ngx-bootstrap";

import {ApplicantFormRoutingModule} from "./applicant-form-routing.module";
import {ArchwizardModule} from "angular-archwizard";
import {NgxDatatableModule} from "@swimlane/ngx-datatable";
import {PreviosNextBtnModule} from "../ui/btn/previos-next-btn/previos-next-btn.module";

import {ApplicantAndFundsComponent} from "./page/applicant-form/page/applicant-and-funds/applicant-and-funds.component";
import {ApplicantExpensesComponent} from "./page/applicant-form/page/applicant-expenses/applicant-expenses.component";
import {ApplicantFormComponent} from "./page/applicant-form/applicant-form.component";
import {ExpensesForPartnerComponent} from "./page/applicant-form/page/expenses-for-partner/expenses-for-partner.component";
import {ExpensesForChildrenComponent} from "./page/applicant-form/page/expenses-for-children/expenses-for-children.component";
import {ServicesUsageComponent} from "./page/applicant-form/page/services-usage/services-usage.component";
import {SummaryComponent} from "./page/applicant-form/page/summary/summary.component";

import {ActivityExpensesComponent} from "./component/activity-expenses/activity-expenses.component";
import {ApplicantAndFundsComponent as SummaryApplicantAndFunds} from "./component/applicant-and-funds/applicant-and-funds.component";
import {ApplicantDetailsComponent} from "./component/applicant-details/applicant-details.component";
import {FileTypeListComponent} from "./component/file-type-list/file-type-list.component";
import {FundsDetailsComponent} from "./component/funds-details/funds-details.component";
import {IllnessExpensesComponent} from "./component/illness-expenses/illness-expenses.component";
import {InsuranceBenefitsComponent} from "./component/insurance-benefits/insurance-benefits.component";
import {ServiceDataInputsComponent} from "./component/service-data-inputs/service-data-inputs.component";
import {ServicesTableComponent as SummaryServicesTable} from "./component/services-table/services-table.component";
import {ServicesUsageComponent as SummaryServicesUsage} from "./component/services-usage/services-usage.component";
import {FileInfoListCellComponent} from './component/services-table/component/file-info-list-cell/file-info-list-cell.component';
import {FileInfoListHeaderComponent} from './component/services-table/component/file-info-list-header/file-info-list-header.component';
import { RowGroupComponent } from './component/services-table/component/row-group/row-group.component';

import {TotalComponent as SummaryTotal} from "./component/total/total.component";
import {DisplayDateModule} from "../ui/form/display-date/display-date.module";
import {DisplayCurrencyModule} from "../ui/form/display-currency/display-currency.module";
import {FileUploadModule} from "../ui/form/file-upload/file-upload.module";
import {ExpandedRadioBtnModule} from '../ui/form/expanded-radio-btn/expanded-radio-btn.module';
import {CardService} from "./shared/card.service";

import {ProcessIdModule} from "../ui/form/process-id/process-id.module";
import {FileListComponent} from "./component/file-list/file-list.component";
import {SessionTimerModule} from "../ui/form/session-timer/session-timer.module";

@NgModule({
    entryComponents: [
        FileInfoListCellComponent,
        FileInfoListHeaderComponent,
        RowGroupComponent
    ],
    declarations: [
        ApplicantFormComponent,
        ActivityExpensesComponent,
        SummaryApplicantAndFunds,
        SummaryServicesUsage,
        ApplicantDetailsComponent,
        FileTypeListComponent,
        FileListComponent,
        FundsDetailsComponent,
        IllnessExpensesComponent,
        ServiceDataInputsComponent,
        SummaryServicesTable,
        ServicesUsageComponent,
        SummaryTotal,
        ApplicantAndFundsComponent,
        ApplicantExpensesComponent,
        ExpensesForPartnerComponent,
        ExpensesForChildrenComponent,
        SummaryComponent,
        InsuranceBenefitsComponent,
        FileInfoListCellComponent,
        FileInfoListHeaderComponent,
        RowGroupComponent
    ],
    imports: [
        ApplicantFormRoutingModule,
        CommonModule,
        PreviosNextBtnModule,
        AlertModule,
        ReactiveFormsModule,
        ArchwizardModule,
        TooltipModule,
        DisplayDateModule,
        DisplayCurrencyModule,
        FileUploadModule,
        NgxDatatableModule,
        FormsModule,
        AgGridModule.withComponents([]),
        ExpandedRadioBtnModule,
        ProcessIdModule,
        SessionTimerModule
    ],
    providers: [
        AlertConfig,
        TooltipConfig,
        CardService
    ]
})
export class ApplicantFormModule { }
