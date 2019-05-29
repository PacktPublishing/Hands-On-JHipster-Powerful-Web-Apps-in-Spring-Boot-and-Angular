import { NgModule, CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { RouterModule } from '@angular/router';
import { JhiLanguageService } from 'ng-jhipster';
import { JhiLanguageHelper } from 'app/core';

import { StoreSharedModule } from 'app/shared';
import {
  OrderItemComponent,
  OrderItemDetailComponent,
  OrderItemUpdateComponent,
  OrderItemDeletePopupComponent,
  OrderItemDeleteDialogComponent,
  orderItemRoute,
  orderItemPopupRoute
} from './';

const ENTITY_STATES = [...orderItemRoute, ...orderItemPopupRoute];

@NgModule({
  imports: [StoreSharedModule, RouterModule.forChild(ENTITY_STATES)],
  declarations: [
    OrderItemComponent,
    OrderItemDetailComponent,
    OrderItemUpdateComponent,
    OrderItemDeleteDialogComponent,
    OrderItemDeletePopupComponent
  ],
  entryComponents: [OrderItemComponent, OrderItemUpdateComponent, OrderItemDeleteDialogComponent, OrderItemDeletePopupComponent],
  providers: [{ provide: JhiLanguageService, useClass: JhiLanguageService }],
  schemas: [CUSTOM_ELEMENTS_SCHEMA]
})
export class StoreOrderItemModule {
  constructor(private languageService: JhiLanguageService, private languageHelper: JhiLanguageHelper) {
    this.languageHelper.language.subscribe((languageKey: string) => {
      if (languageKey !== undefined) {
        this.languageService.changeLanguage(languageKey);
      }
    });
  }
}
