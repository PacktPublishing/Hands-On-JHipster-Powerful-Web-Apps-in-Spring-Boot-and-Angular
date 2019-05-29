import { NgModule, CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { RouterModule } from '@angular/router';

@NgModule({
  imports: [
    RouterModule.forChild([
      {
        path: 'country',
        loadChildren: './country/country.module#DemoCountryModule'
      },
      {
        path: 'blog',
        loadChildren: './blog/blog.module#DemoBlogModule'
      },
      {
        path: 'blog-entry',
        loadChildren: './blog-entry/blog-entry.module#DemoBlogEntryModule'
      },
      {
        path: 'tag',
        loadChildren: './tag/tag.module#DemoTagModule'
      }
      /* jhipster-needle-add-entity-route - JHipster will add entity modules routes here */
    ])
  ],
  declarations: [],
  entryComponents: [],
  providers: [],
  schemas: [CUSTOM_ELEMENTS_SCHEMA]
})
export class DemoEntityModule {}
