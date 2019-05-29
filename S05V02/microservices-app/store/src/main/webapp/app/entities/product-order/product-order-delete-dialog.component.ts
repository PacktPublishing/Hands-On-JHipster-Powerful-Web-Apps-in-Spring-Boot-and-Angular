import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

import { NgbActiveModal, NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { JhiEventManager } from 'ng-jhipster';

import { IProductOrder } from 'app/shared/model/product-order.model';
import { ProductOrderService } from './product-order.service';

@Component({
  selector: 'jhi-product-order-delete-dialog',
  templateUrl: './product-order-delete-dialog.component.html'
})
export class ProductOrderDeleteDialogComponent {
  productOrder: IProductOrder;

  constructor(
    protected productOrderService: ProductOrderService,
    public activeModal: NgbActiveModal,
    protected eventManager: JhiEventManager
  ) {}

  clear() {
    this.activeModal.dismiss('cancel');
  }

  confirmDelete(id: number) {
    this.productOrderService.delete(id).subscribe(response => {
      this.eventManager.broadcast({
        name: 'productOrderListModification',
        content: 'Deleted an productOrder'
      });
      this.activeModal.dismiss(true);
    });
  }
}

@Component({
  selector: 'jhi-product-order-delete-popup',
  template: ''
})
export class ProductOrderDeletePopupComponent implements OnInit, OnDestroy {
  protected ngbModalRef: NgbModalRef;

  constructor(protected activatedRoute: ActivatedRoute, protected router: Router, protected modalService: NgbModal) {}

  ngOnInit() {
    this.activatedRoute.data.subscribe(({ productOrder }) => {
      setTimeout(() => {
        this.ngbModalRef = this.modalService.open(ProductOrderDeleteDialogComponent as Component, { size: 'lg', backdrop: 'static' });
        this.ngbModalRef.componentInstance.productOrder = productOrder;
        this.ngbModalRef.result.then(
          result => {
            this.router.navigate(['/product-order', { outlets: { popup: null } }]);
            this.ngbModalRef = null;
          },
          reason => {
            this.router.navigate(['/product-order', { outlets: { popup: null } }]);
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
