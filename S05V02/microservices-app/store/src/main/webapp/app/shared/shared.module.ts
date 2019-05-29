import { NgModule, CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { StoreSharedLibsModule, StoreSharedCommonModule, JhiLoginModalComponent, HasAnyAuthorityDirective } from './';

@NgModule({
  imports: [StoreSharedLibsModule, StoreSharedCommonModule],
  declarations: [JhiLoginModalComponent, HasAnyAuthorityDirective],
  entryComponents: [JhiLoginModalComponent],
  exports: [StoreSharedCommonModule, JhiLoginModalComponent, HasAnyAuthorityDirective],
  schemas: [CUSTOM_ELEMENTS_SCHEMA]
})
export class StoreSharedModule {
  static forRoot() {
    return {
      ngModule: StoreSharedModule
    };
  }
}
