import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

import { NgbActiveModal, NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { JhiEventManager } from 'ng-jhipster';

import { IProductCategory } from 'app/shared/model/product-category.model';
import { ProductCategoryService } from './product-category.service';

@Component({
  selector: 'jhi-product-category-delete-dialog',
  templateUrl: './product-category-delete-dialog.component.html'
})
export class ProductCategoryDeleteDialogComponent {
  productCategory: IProductCategory;

  constructor(
    protected productCategoryService: ProductCategoryService,
    public activeModal: NgbActiveModal,
    protected eventManager: JhiEventManager
  ) {}

  clear() {
    this.activeModal.dismiss('cancel');
  }

  confirmDelete(id: number) {
    this.productCategoryService.delete(id).subscribe(response => {
      this.eventManager.broadcast({
        name: 'productCategoryListModification',
        content: 'Deleted an productCategory'
      });
      this.activeModal.dismiss(true);
    });
  }
}

@Component({
  selector: 'jhi-product-category-delete-popup',
  template: ''
})
export class ProductCategoryDeletePopupComponent implements OnInit, OnDestroy {
  protected ngbModalRef: NgbModalRef;

  constructor(protected activatedRoute: ActivatedRoute, protected router: Router, protected modalService: NgbModal) {}

  ngOnInit() {
    this.activatedRoute.data.subscribe(({ productCategory }) => {
      setTimeout(() => {
        this.ngbModalRef = this.modalService.open(ProductCategoryDeleteDialogComponent as Component, { size: 'lg', backdrop: 'static' });
        this.ngbModalRef.componentInstance.productCategory = productCategory;
        this.ngbModalRef.result.then(
          result => {
            this.router.navigate(['/product-category', { outlets: { popup: null } }]);
            this.ngbModalRef = null;
          },
          reason => {
            this.router.navigate(['/product-category', { outlets: { popup: null } }]);
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
