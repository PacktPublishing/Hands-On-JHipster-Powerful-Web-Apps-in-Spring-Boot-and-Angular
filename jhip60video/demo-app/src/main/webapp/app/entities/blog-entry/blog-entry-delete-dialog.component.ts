import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

import { NgbActiveModal, NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { JhiEventManager } from 'ng-jhipster';

import { IBlogEntry } from 'app/shared/model/blog-entry.model';
import { BlogEntryService } from './blog-entry.service';

@Component({
  selector: 'jhi-blog-entry-delete-dialog',
  templateUrl: './blog-entry-delete-dialog.component.html'
})
export class BlogEntryDeleteDialogComponent {
  blogEntry: IBlogEntry;

  constructor(protected blogEntryService: BlogEntryService, public activeModal: NgbActiveModal, protected eventManager: JhiEventManager) {}

  clear() {
    this.activeModal.dismiss('cancel');
  }

  confirmDelete(id: number) {
    this.blogEntryService.delete(id).subscribe(response => {
      this.eventManager.broadcast({
        name: 'blogEntryListModification',
        content: 'Deleted an blogEntry'
      });
      this.activeModal.dismiss(true);
    });
  }
}

@Component({
  selector: 'jhi-blog-entry-delete-popup',
  template: ''
})
export class BlogEntryDeletePopupComponent implements OnInit, OnDestroy {
  protected ngbModalRef: NgbModalRef;

  constructor(protected activatedRoute: ActivatedRoute, protected router: Router, protected modalService: NgbModal) {}

  ngOnInit() {
    this.activatedRoute.data.subscribe(({ blogEntry }) => {
      setTimeout(() => {
        this.ngbModalRef = this.modalService.open(BlogEntryDeleteDialogComponent as Component, { size: 'lg', backdrop: 'static' });
        this.ngbModalRef.componentInstance.blogEntry = blogEntry;
        this.ngbModalRef.result.then(
          result => {
            this.router.navigate(['/blog-entry', { outlets: { popup: null } }]);
            this.ngbModalRef = null;
          },
          reason => {
            this.router.navigate(['/blog-entry', { outlets: { popup: null } }]);
            this.ngbModalRef = null;
          }
        );
      }, 0);
    });
  }

  ngOnDestroy() {
    this.ngbModalRef = null;
  }
}
