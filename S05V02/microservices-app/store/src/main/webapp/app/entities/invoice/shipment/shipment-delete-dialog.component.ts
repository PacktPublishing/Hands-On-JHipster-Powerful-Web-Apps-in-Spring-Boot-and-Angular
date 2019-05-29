import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

import { NgbActiveModal, NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { JhiEventManager } from 'ng-jhipster';

import { IShipment } from 'app/shared/model/invoice/shipment.model';
import { ShipmentService } from './shipment.service';

@Component({
  selector: 'jhi-shipment-delete-dialog',
  templateUrl: './shipment-delete-dialog.component.html'
})
export class ShipmentDeleteDialogComponent {
  shipment: IShipment;

  constructor(protected shipmentService: ShipmentService, public activeModal: NgbActiveModal, protected eventManager: JhiEventManager) {}

  clear() {
    this.activeModal.dismiss('cancel');
  }

  confirmDelete(id: number) {
    this.shipmentService.delete(id).subscribe(response => {
      this.eventManager.broadcast({
        name: 'shipmentListModification',
        content: 'Deleted an shipment'
      });
      this.activeModal.dismiss(true);
    });
  }
}

@Component({
  selector: 'jhi-shipment-delete-popup',
  template: ''
})
export class ShipmentDeletePopupComponent implements OnInit, OnDestroy {
  protected ngbModalRef: NgbModalRef;

  constructor(protected activatedRoute: ActivatedRoute, protected router: Router, protected modalService: NgbModal) {}

  ngOnInit() {
    this.activatedRoute.data.subscribe(({ shipment }) => {
      setTimeout(() => {
        this.ngbModalRef = this.modalService.open(ShipmentDeleteDialogComponent as Component, { size: 'lg', backdrop: 'static' });
        this.ngbModalRef.componentInstance.shipment = shipment;
        this.ngbModalRef.result.then(
          result => {
            this.router.navigate(['/shipment', { outlets: { popup: null } }]);
            this.ngbModalRef = null;
          },
          reason => {
            this.router.navigate(['/shipment', { outlets: { popup: null } }]);
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
