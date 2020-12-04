import {PreloadAllModules, RouterModule, Routes} from '@angular/router';
import {NgModule} from '@angular/core';

const routes: Routes = [
    { path: '', pathMatch: 'full', redirectTo: '/home' },
    { path: 'home', loadChildren: './home/home.module#HomeModule' },
    {path: 'antrag', loadChildren: './applicant-form/applicant-form.module#ApplicantFormModule'},
];

@NgModule({
    imports: [RouterModule.forRoot(routes, {
        preloadingStrategy: PreloadAllModules,
        useHash: true
    })],
    exports: [RouterModule]
})
export class AppRoutingModule {
}
