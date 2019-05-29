/* tslint:disable max-line-length */
import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { HttpResponse } from '@angular/common/http';
import { FormBuilder } from '@angular/forms';
import { Observable, of } from 'rxjs';

import { DemoTestModule } from '../../../test.module';
import { BlogEntryUpdateComponent } from 'app/entities/blog-entry/blog-entry-update.component';
import { BlogEntryService } from 'app/entities/blog-entry/blog-entry.service';
import { BlogEntry } from 'app/shared/model/blog-entry.model';

describe('Component Tests', () => {
  describe('BlogEntry Management Update Component', () => {
    let comp: BlogEntryUpdateComponent;
    let fixture: ComponentFixture<BlogEntryUpdateComponent>;
    let service: BlogEntryService;

    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [DemoTestModule],
        declarations: [BlogEntryUpdateComponent],
        providers: [FormBuilder]
      })
        .overrideTemplate(BlogEntryUpdateComponent, '')
        .compileComponents();

      fixture = TestBed.createComponent(BlogEntryUpdateComponent);
      comp = fixture.componentInstance;
      service = fixture.debugElement.injector.get(BlogEntryService);
    });

    describe('save', () => {
      it('Should call update service on save for existing entity', fakeAsync(() => {
        // GIVEN
        const entity = new BlogEntry(123);
        spyOn(service, 'update').and.returnValue(of(new HttpResponse({ body: entity })));
        comp.updateForm(entity);
        // WHEN
        comp.save();
        tick(); // simulate async

        // THEN
        expect(service.update).toHaveBeenCalledWith(entity);
        expect(comp.isSaving).toEqual(false);
      }));

      it('Should call create service on save for new entity', fakeAsync(() => {
        // GIVEN
        const entity = new BlogEntry();
        spyOn(service, 'create').and.returnValue(of(new HttpResponse({ body: entity })));
        comp.updateForm(entity);
        // WHEN
        comp.save();
        tick(); // simulate async

        // THEN
        expect(service.create).toHaveBeenCalledWith(entity);
        expect(comp.isSaving).toEqual(false);
      }));
    });
  });
});
