import {Component} from "@angular/core";
import {IHeaderAngularComp} from "ag-grid-angular/main";

@Component({
  selector: 'tlrz-file-info-list-header',
  templateUrl: './file-info-list-header.component.html',
  styleUrls: ['./file-info-list-header.component.scss']
})
export class FileInfoListHeaderComponent implements IHeaderAngularComp {

  constructor() { }

  public agInit(): void {
  }

}
