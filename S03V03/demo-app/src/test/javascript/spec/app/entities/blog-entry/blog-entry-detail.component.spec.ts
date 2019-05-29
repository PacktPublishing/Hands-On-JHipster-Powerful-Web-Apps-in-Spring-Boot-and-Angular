/* tslint:disable max-line-length */
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';

import { DemoTestModule } from '../../../test.module';
import { BlogEntryDetailComponent } from 'app/entities/blog-entry/blog-entry-detail.component';
import { BlogEntry } from 'app/shared/model/blog-entry.model';

describe('Component Tests', () => {
  describe('BlogEntry Management Detail Component', () => {
    let comp: BlogEntryDetailComponent;
    let fixture: ComponentFixture<BlogEntryDetailComponent>;
    const route = ({ data: of({ blogEntry: new BlogEntry(123) }) } as any) as ActivatedRoute;

    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [DemoTestModule],
        declarations: [BlogEntryDetailComponent],
        providers: [{ provide: ActivatedRoute, useValue: route }]
      })
        .overrideTemplate(BlogEntryDetailComponent, '')
        .compileComponents();
      fixture = TestBed.createComponent(BlogEntryDetailComponent);
      comp = fixture.componentInstance;
    });

    describe('OnInit', () => {
      it('Should call load all on init', () => {
        // GIVEN

        // WHEN
        comp.ngOnInit();

        // THEN
        expect(comp.blogEntry).toEqual(jasmine.objectContaining({ id: 123 }));
      });
    });
  });
});
