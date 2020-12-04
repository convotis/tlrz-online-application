import {ServiceInformation} from "../../../shared/service-information";

export interface Section {
    sectionIndex: number;
    title: string;
    rows: ServiceInformation[];
}
