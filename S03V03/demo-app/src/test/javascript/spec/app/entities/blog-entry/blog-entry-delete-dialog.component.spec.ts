/* tslint:disable max-line-length */
import { ComponentFixture, TestBed, inject, fakeAsync, tick } from '@angular/core/testing';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { Observable, of } from 'rxjs';
import { JhiEventManager } from 'ng-jhipster';

import { DemoTestModule } from '../../../test.module';
import { BlogEntryDeleteDialogComponent } from 'app/entities/blog-entry/blog-entry-delete-dialog.component';
import { BlogEntryService } from 'app/entities/blog-entry/blog-entry.service';

describe('Component Tests', () => {
  describe('BlogEntry Management Delete Component', () => {
    let comp: BlogEntryDeleteDialogComponent;
    let fixture: ComponentFixture<BlogEntryDeleteDialogComponent>;
    let service: BlogEntryService;
    let mockEventManager: any;
    let mockActiveModal: any;

    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [DemoTestModule],
        declarations: [BlogEntryDeleteDialogComponent]
      })
        .overrideTemplate(BlogEntryDeleteDialogComponent, '')
        .compileComponents();
      fixture = TestBed.createComponent(BlogEntryDeleteDialogComponent);
      comp = fixture.componentInstance;
      service = fixture.debugElement.injector.get(BlogEntryService);
      mockEventManager = fixture.debugElement.injector.get(JhiEventManager);
      mockActiveModal = fixture.debugElement.injector.get(NgbActiveModal);
    });

    describe('confirmDelete', () => {
      it('Should call delete service on confirmDelete', inject(
        [],
        fakeAsync(() => {
          // GIVEN
          spyOn(service, 'delete').and.returnValue(of({}));

          // WHEN
          comp.confirmDelete(123);
          tick();

          // THEN
          expect(service.delete).toHaveBeenCalledWith(123);
          expect(mockActiveModal.dismissSpy).toHaveBeenCalled();
          expect(mockEventManager.broadcastSpy).toHaveBeenCalled();
        })
      ));
    });
  });
});
