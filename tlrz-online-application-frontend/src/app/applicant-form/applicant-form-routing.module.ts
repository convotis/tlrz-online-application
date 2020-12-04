import { Routes, RouterModule } from '@angular/router';
import { NgModule } from '@angular/core';

import { ApplicantFormComponent } from "./page/applicant-form/applicant-form.component";

const routes: Routes = [
  { path: '', pathMatch: 'full', component: ApplicantFormComponent }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ApplicantFormRoutingModule {
}
