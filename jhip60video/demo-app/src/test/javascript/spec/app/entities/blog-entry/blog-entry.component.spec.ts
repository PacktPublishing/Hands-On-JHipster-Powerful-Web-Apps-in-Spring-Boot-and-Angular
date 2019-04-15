/* tslint:disable max-line-length */
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Observable, of } from 'rxjs';
import { HttpHeaders, HttpResponse } from '@angular/common/http';

import { DemoTestModule } from '../../../test.module';
import { BlogEntryComponent } from 'app/entities/blog-entry/blog-entry.component';
import { BlogEntryService } from 'app/entities/blog-entry/blog-entry.service';
import { BlogEntry } from 'app/shared/model/blog-entry.model';

describe('Component Tests', () => {
  describe('BlogEntry Management Component', () => {
    let comp: BlogEntryComponent;
    let fixture: ComponentFixture<BlogEntryComponent>;
    let service: BlogEntryService;

    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [DemoTestModule],
        declarations: [BlogEntryComponent],
        providers: []
      })
        .overrideTemplate(BlogEntryComponent, '')
        .compileComponents();

      fixture = TestBed.createComponent(BlogEntryComponent);
      comp = fixture.componentInstance;
      service = fixture.debugElement.injector.get(BlogEntryService);
    });

    it('Should call load all on init', () => {
      // GIVEN
      const headers = new HttpHeaders().append('link', 'link;link');
      spyOn(service, 'query').and.returnValue(
        of(
          new HttpResponse({
            body: [new BlogEntry(123)],
            headers
          })
        )
      );

      // WHEN
      comp.ngOnInit();

      // THEN
      expect(service.query).toHaveBeenCalled();
      expect(comp.blogEntries[0]).toEqual(jasmine.objectContaining({ id: 123 }));
    });
  });
});
